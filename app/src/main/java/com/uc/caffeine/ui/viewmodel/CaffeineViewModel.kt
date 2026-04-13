package com.uc.caffeine.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uc.caffeine.data.AppDateFormat
import com.uc.caffeine.data.CaffeineDatabase
import com.uc.caffeine.data.SettingsRepository
import com.uc.caffeine.data.ThemeMode
import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.DEFAULT_CONSUMPTION_DURATION_MINUTES
import com.uc.caffeine.data.model.DrinkPreset
import com.uc.caffeine.data.model.DrinkUnit
import com.uc.caffeine.data.model.RecentDrink
import com.uc.caffeine.util.CaffeineCalculator
import com.uc.caffeine.util.AnalyticsRange
import com.uc.caffeine.util.AnalyticsUiState
import com.uc.caffeine.util.calculateNextBedtimeMillis
import com.uc.caffeine.util.calculateServingTotalCaffeine
import com.uc.caffeine.util.buildAnalyticsUiState
import com.uc.caffeine.util.CategoryUtils
import com.uc.caffeine.util.ChartData
import com.uc.caffeine.util.ChartDataGenerator
import com.uc.caffeine.util.ConsumptionContributionDetail
import com.uc.caffeine.util.groupConsumptionEntriesByLocalDate
import com.uc.caffeine.util.nextStartOfDayMillis
import com.uc.caffeine.util.resolvedZoneId
import com.uc.caffeine.util.startOfDayMillis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
sealed interface AddScreenUiEvent {
    data class DrinkLogged(val drinkName: String) : AddScreenUiEvent
}

sealed interface HomeScreenUiEvent {
    data class LogActionCompleted(val message: String) : HomeScreenUiEvent
}

class CaffeineViewModel(application: Application) : AndroidViewModel(application) {

    private val db        = CaffeineDatabase.getDatabase(application)
    private val presetDao = db.drinkPresetDao()
    private val unitDao   = db.drinkUnitDao()
    private val logDao    = db.consumptionLogDao()
    private val settingsRepo = SettingsRepository(application)

    // User settings flow
    val userSettings = settingsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = settingsRepo.defaultSettings
    )

    val isUserSettingsLoaded: StateFlow<Boolean> = settingsRepo.settingsFlow
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // Fast ticker — 1-second interval for the live caffeine counter.
    // WhileSubscribed stops it when the app is backgrounded.
    private val liveTickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = System.currentTimeMillis()
    )

    // Slow ticker — 60-second interval for chart data & bedtime prediction.
    // Regenerating the full 24-hour curve every second was causing massive GC churn.
    private val chartTickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000L)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = System.currentTimeMillis()
    )

    // Selected category filter (null = "All", shows all categories)
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // Search query filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _analyticsRange = MutableStateFlow(AnalyticsRange.LAST_30_DAYS)
    val analyticsRange: StateFlow<AnalyticsRange> = _analyticsRange.asStateFlow()
    private val _customRangeStart = MutableStateFlow<java.time.LocalDate?>(null)
    private val _customRangeEnd = MutableStateFlow<java.time.LocalDate?>(null)
    private val addScreenEventsChannel = Channel<AddScreenUiEvent>(capacity = Channel.BUFFERED)
    val addScreenEvents: Flow<AddScreenUiEvent> = addScreenEventsChannel.receiveAsFlow()
    private val homeScreenEventsChannel = Channel<HomeScreenUiEvent>(capacity = Channel.BUFFERED)
    val homeScreenEvents: Flow<HomeScreenUiEvent> = homeScreenEventsChannel.receiveAsFlow()

    private val allDrinkPresets = presetDao.getAllPresets()
    private val allConsumptionEntries = logDao.getAllEntries()

    val isDrinkCatalogLoading: StateFlow<Boolean> = allDrinkPresets
        .map { false }
        .onStart { emit(true) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val isConsumptionEntriesLoading: StateFlow<Boolean> = allConsumptionEntries
        .map { false }
        .onStart { emit(true) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    val hasExistingConsumptionHistory: StateFlow<Boolean> = allConsumptionEntries
        .map { entries -> entries.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Full drink catalog — used by the Add screen
    val drinkPresets: StateFlow<List<DrinkPreset>> = allDrinkPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Grouped drink catalog by category — used by the Add screen for categorized display
    val groupedDrinkPresets: StateFlow<Map<String, List<DrinkPreset>>> = 
        combine(
            drinkPresets,
            selectedCategoryFilter,
            searchQuery  // Add search query
        ) { drinks, filter, query ->
            // First apply category filter
            val categoryFiltered = if (filter != null) {
                val lowercaseKey = CategoryUtils.getAllCategories()
                    .entries.find { it.value == filter }?.key ?: filter.lowercase()
                drinks.filter { it.category.lowercase() == lowercaseKey }
            } else {
                drinks
            }
            
            // Then apply search filter
            val searchFiltered = if (query.isNotBlank()) {
                categoryFiltered.filter { drink ->
                    drink.name.contains(query, ignoreCase = true) ||
                    drink.brand.contains(query, ignoreCase = true) ||
                    drink.description?.contains(query, ignoreCase = true) == true
                }
            } else {
                categoryFiltered
            }
            
            // Group and sort
            searchFiltered
                .groupBy { it.category.lowercase() }
                .mapKeys { (category, _) -> CategoryUtils.getCategoryDisplayName(category) }
                .toSortedMap(compareBy { displayName ->
                    val lowercaseKey = CategoryUtils.getAllCategories()
                        .entries.find { it.value == displayName }?.key ?: ""
                    CategoryUtils.getCategoryOrder().indexOf(lowercaseKey)
                })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    // The 2 most recently logged serving combos — used by quick add on AddScreen.
    val recentDrinks: StateFlow<List<RecentDrink>> = logDao
        .getRecentlyUsedDrinks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Today's caffeine total — the big number
    val todayTotalMg: StateFlow<Int> = combine(
        allConsumptionEntries,
        userSettings,
        chartTickerFlow
    ) { entries, settings, currentTime ->
        val zoneId = settings.resolvedZoneId()
        val startOfDay = startOfDayMillis(currentTime, zoneId)
        val startOfNextDay = nextStartOfDayMillis(currentTime, zoneId)
        entries.asSequence()
            .filter { entry -> entry.startedAtMillis in startOfDay until startOfNextDay }
            .sumOf { entry -> entry.caffeineMg }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    // Full historical consumption timeline for the Home screen.
    val groupedConsumptionEntries: StateFlow<Map<LocalDate, List<ConsumptionEntry>>> = combine(
        allConsumptionEntries,
        userSettings
    ) { entries, settings ->
        groupConsumptionEntriesByLocalDate(entries, settings)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    // Current active caffeine level with real-time decay
    val currentCaffeineLevel: StateFlow<Double> = combine(
        allConsumptionEntries,
        liveTickerFlow,
        userSettings
    ) { allEntries, currentTime, settings ->
        CaffeineCalculator.calculateCurrentLevel(
            entries = allEntries,
            currentTimeMillis = currentTime,
            halfLifeMinutes = settings.halfLifeMinutes
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    /**
     * Predicts caffeine level at user's next bedtime.
     * Returns: Pair<caffeineLevelAtBedtime, timeUntilBedtime>
     */
    val caffeineAtBedtime: StateFlow<Pair<Double, Long>> = combine(
        allConsumptionEntries,
        userSettings,
        chartTickerFlow
    ) { allEntries, settings, _ ->
        val now = System.currentTimeMillis()
        val bedtime = calculateNextBedtimeMillis(now, settings)
        val caffeineLevel = CaffeineCalculator.calculateCurrentLevel(
            entries = allEntries,
            currentTimeMillis = bedtime,  // Calculate AT bedtime, not now
            halfLifeMinutes = settings.halfLifeMinutes
        )
        
        Pair(caffeineLevel, bedtime)
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Pair(0.0, System.currentTimeMillis())
    )

    // Time until peak absorption - shows when caffeine is still being absorbed
    val timeUntilPeak: StateFlow<Long?> = combine(
        allConsumptionEntries,
        liveTickerFlow,
        userSettings,
    ) { allEntries, _, settings ->
        if (allEntries.isEmpty()) return@combine null
        val now = System.currentTimeMillis()
        val peakTime = allEntries
            .asSequence()
            .map { entry ->
                CaffeineCalculator.calculatePeakTime(
                    entry = entry,
                    halfLifeMinutes = settings.halfLifeMinutes,
                )
            }
            .filter { candidatePeakTime -> candidatePeakTime > now }
            .minOrNull()
            ?: return@combine null
        if (peakTime > now) peakTime else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    // Reactive all-history caffeine curve data for charting
    val chartData: StateFlow<ChartData> = combine(
        allConsumptionEntries,
        chartTickerFlow,
        userSettings
    ) { entries, currentTime, settings ->
        ChartDataGenerator.generateChartData(
            entries = entries,
            settings = settings,
            currentTime = currentTime
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ChartDataGenerator.generateChartData(
            entries = emptyList(),
            settings = settingsRepo.defaultSettings,
            currentTime = System.currentTimeMillis()
        )
    )

    val analyticsUiState: StateFlow<AnalyticsUiState> = combine(
        allConsumptionEntries,
        drinkPresets,
        userSettings,
        analyticsRange,
        chartTickerFlow,
        _customRangeStart,
        _customRangeEnd,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        buildAnalyticsUiState(
            entries = values[0] as List<ConsumptionEntry>,
            presets = values[1] as List<DrinkPreset>,
            settings = values[2] as UserSettings,
            selectedRange = values[3] as AnalyticsRange,
            nowMillis = values[4] as Long,
            customStartDate = values[5] as? java.time.LocalDate,
            customEndDate = values[6] as? java.time.LocalDate,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = buildAnalyticsUiState(
                entries = emptyList(),
                presets = emptyList(),
                settings = settingsRepo.defaultSettings,
                selectedRange = AnalyticsRange.LAST_30_DAYS,
                nowMillis = System.currentTimeMillis(),
            ),
        )

    // ── Vico ModelProducer ─────────────────────────────────────────────────
    // Per Vico docs: "Store it in a place with sufficient persistence, such
    // as a view model." The producer shouldn't be replaced — updates happen
    // via runTransaction which already runs off the main thread.
    val chartModelProducer = CartesianChartModelProducer()
    val analyticsSourcePieChartModelProducer = PieChartModelProducer()
    val analyticsBedtimeChartModelProducer = CartesianChartModelProducer()
    val analyticsTimeOfDayChartModelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch {
            chartData.collect { data ->
                if (data.dataPoints.isNotEmpty()) {
                    val xValues = data.dataPoints.map { point ->
                        ChartDataGenerator.timestampToDomainX(
                            domainStartMillis = data.domainStartMillis,
                            targetTimestampMillis = point.timestampMillis,
                        )
                    }
                    val yValues = data.dataPoints.map { it.caffeineLevel }

                    chartModelProducer.runTransaction {
                        lineSeries {
                            series(xValues, yValues)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            analyticsUiState.collect { state ->
                if (!state.hasData) return@collect

                analyticsSourcePieChartModelProducer.runTransaction {
                    pieSeries {
                        series(state.sourceValues)
                    }
                }
                analyticsBedtimeChartModelProducer.runTransaction {
                    columnSeries {
                        series(state.bedtimeValues)
                    }
                }
                analyticsTimeOfDayChartModelProducer.runTransaction {
                    columnSeries {
                        series(state.timeOfDayValues)
                    }
                }
            }
        }
    }

    fun logDrink(preset: DrinkPreset) {
        viewModelScope.launch {
            val defaultUnit = unitDao.getDefaultUnit(preset.id)
                ?: fallbackUnitForPreset(preset)
            logDao.logDrink(
                buildConsumptionEntry(
                    preset = preset,
                    quantity = 1,
                    unit = defaultUnit,
                    startedAtMillis = System.currentTimeMillis(),
                    durationMinutes = DEFAULT_CONSUMPTION_DURATION_MINUTES,
                )
            )
        }
    }

    fun logDrinkFromAddScreen(
        preset: DrinkPreset,
        quantity: Int,
        unit: DrinkUnit,
        startedAtMillis: Long,
        durationMinutes: Int,
    ) {
        viewModelScope.launch {
            logDao.logDrink(
                buildConsumptionEntry(
                    preset = preset,
                    quantity = quantity,
                    unit = unit,
                    startedAtMillis = startedAtMillis,
                    durationMinutes = durationMinutes,
                )
            )
            addScreenEventsChannel.send(AddScreenUiEvent.DrinkLogged(preset.name))
        }
    }

    // Log from a RecentDrink (tapped from AddScreen quick add)
    fun logRecentDrink(recent: RecentDrink) {
        viewModelScope.launch {
            logDao.logDrink(
                ConsumptionEntry(
                    drinkName  = recent.drinkName,
                    caffeineMg = recent.caffeineMg,
                    emoji      = recent.emoji,
                    presetItemId = recent.presetItemId,
                    quantity = recent.quantity,
                    unitKey = recent.unitKey,
                    unitCaffeineMg = recent.unitCaffeineMg,
                    imageName  = recent.imageName,
                    absorptionRate = recent.absorptionRate,
                    startedAtMillis = System.currentTimeMillis(),
                    durationMinutes = recent.durationMinutes,
                )
            )
            addScreenEventsChannel.send(AddScreenUiEvent.DrinkLogged(recent.drinkName))
        }
    }

    fun getContributionDetail(
        entry: ConsumptionEntry,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): ConsumptionContributionDetail {
        return ChartDataGenerator.generateContributionDetail(
            entry = entry,
            settings = userSettings.value,
            currentTime = currentTimeMillis
        )
    }

    fun updateLoggedEntry(
        entry: ConsumptionEntry,
        quantity: Int,
        unit: DrinkUnit,
        startedAtMillis: Long,
        durationMinutes: Int,
    ) {
        viewModelScope.launch {
            logDao.updateEntryById(
                entryId = entry.id,
                caffeineMg = calculateServingTotalCaffeine(quantity, unit.caffeineMg),
                quantity = quantity,
                unitKey = unit.unitKey,
                unitCaffeineMg = unit.caffeineMg,
                startedAtMillis = startedAtMillis,
                durationMinutes = durationMinutes.coerceAtLeast(1),
            )
            homeScreenEventsChannel.send(
                HomeScreenUiEvent.LogActionCompleted("Updated ${entry.drinkName}")
            )
        }
    }

    fun duplicateLoggedEntry(entry: ConsumptionEntry) {
        viewModelScope.launch {
            logDao.logDrink(
                entry.copy(
                    id = 0,
                    startedAtMillis = System.currentTimeMillis()
                )
            )
            homeScreenEventsChannel.send(
                HomeScreenUiEvent.LogActionCompleted("Logged ${entry.drinkName} again")
            )
        }
    }

    fun deleteLoggedEntry(entry: ConsumptionEntry) {
        viewModelScope.launch {
            logDao.deleteEntryById(entry.id)
            homeScreenEventsChannel.send(
                HomeScreenUiEvent.LogActionCompleted("Deleted ${entry.drinkName}")
            )
        }
    }

    fun resetToday() {
        viewModelScope.launch {
            logDao.clearToday(
                startOfDayMillis(
                    currentTimeMillis = System.currentTimeMillis(),
                    zoneId = userSettings.value.resolvedZoneId()
                )
            )
        }
    }

    // Function to update filter
    fun selectCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAnalyticsRange(range: AnalyticsRange) {
        _analyticsRange.value = range
    }

    fun setCustomRange(start: java.time.LocalDate, end: java.time.LocalDate) {
        _customRangeStart.value = start
        _customRangeEnd.value = end
        _analyticsRange.value = AnalyticsRange.CUSTOM
    }

    // Helper function to get all available categories
    fun getAvailableCategories(): List<String> {
        return CategoryUtils.getCategoryDisplayNamesOrdered()
    }

    // Settings update functions
    fun updateHalfLife(hours: Int) {
        viewModelScope.launch {
            settingsRepo.updateHalfLife(hours * 60) // Convert hours to minutes
        }
    }

    fun updateSleepTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepo.updateSleepTime(hour, minute)
        }
    }

    fun updateSleepThreshold(milligrams: Int) {
        viewModelScope.launch {
            settingsRepo.updateSleepThreshold(milligrams)
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepo.updateThemeMode(themeMode)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateDynamicColor(enabled)
        }
    }

    fun updateUse24HourClock(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateUse24HourClock(enabled)
        }
    }

    fun updateDateFormat(dateFormat: AppDateFormat) {
        viewModelScope.launch {
            settingsRepo.updateDateFormat(dateFormat)
        }
    }

    fun updateTimeZoneId(timeZoneId: String) {
        viewModelScope.launch {
            settingsRepo.updateTimeZoneId(timeZoneId)
        }
    }

    suspend fun getUnitsForDrink(drinkId: Int): List<DrinkUnit> {
        return unitDao.getUnitsForDrink(drinkId)
    }

    suspend fun getUnitsForPresetItemId(presetItemId: String): List<DrinkUnit> {
        if (presetItemId.isBlank()) return emptyList()
        return unitDao.getUnitsForPresetItemId(presetItemId)
    }

    private fun buildConsumptionEntry(
        preset: DrinkPreset,
        quantity: Int,
        unit: DrinkUnit,
        startedAtMillis: Long,
        durationMinutes: Int,
    ): ConsumptionEntry {
        val safeQuantity = quantity.coerceAtLeast(1)
        return ConsumptionEntry(
            drinkName = preset.name,
            caffeineMg = calculateServingTotalCaffeine(safeQuantity, unit.caffeineMg),
            emoji = preset.emoji,
            presetItemId = preset.itemId,
            quantity = safeQuantity,
            unitKey = unit.unitKey,
            unitCaffeineMg = unit.caffeineMg,
            imageName = preset.imageName,
            absorptionRate = preset.absorptionRate,
            startedAtMillis = startedAtMillis,
            durationMinutes = durationMinutes.coerceAtLeast(1),
        )
    }

    private fun fallbackUnitForPreset(preset: DrinkPreset): DrinkUnit {
        return DrinkUnit(
            drinkId = preset.id,
            unitKey = preset.defaultUnit,
            caffeineMg = preset.defaultCaffeineMg.toDouble(),
            milliliters = null,
            grams = null,
            isDefault = true,
        )
    }
}
