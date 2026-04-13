package com.uc.caffeine.data

import android.content.Context
import android.text.format.DateFormat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import java.util.Locale

// Extension property to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

internal object SettingsKeys {
    val HALF_LIFE_MINUTES = intPreferencesKey("half_life_minutes")
    val SLEEP_THRESHOLD_MG = intPreferencesKey("sleep_threshold_mg")
    val ABSORPTION_RATE_MINUTES = intPreferencesKey("absorption_rate_minutes")
    val SLEEP_TIME_HOUR = intPreferencesKey("sleep_time_hour")
    val SLEEP_TIME_MINUTE = intPreferencesKey("sleep_time_minute")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val USE_24_HOUR_CLOCK = booleanPreferencesKey("use_24_hour_clock")
    val DATE_FORMAT = stringPreferencesKey("date_format")
    val TIME_ZONE_ID = stringPreferencesKey("time_zone_id")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}

/**
 * Repository for user preferences using DataStore.
 * 
 * Provides persistent storage for caffeine tracking personalization:
 * - Half-life (varies by person: 3-7 hours)
 * - Sleep threshold (how much caffeine is "safe" for sleep)
 * - Absorption rate (how fast caffeine enters bloodstream)
 */
class SettingsRepository(private val context: Context) {

    val defaultSettings = UserSettings(
        use24HourClock = DateFormat.is24HourFormat(context),
        dateFormat = AppDateFormat.fromLocale(Locale.getDefault()),
        timeZoneId = ZoneId.systemDefault().id,
    )
    
    /**
     * Flow of current user settings.
     * Emits UserSettings with defaults if not yet configured.
     */
    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        prefs.toUserSettings(defaultSettings)
    }
    
    /**
     * Update caffeine half-life setting.
     * @param minutes Half-life in minutes (typically 180-420 for 3-7 hours)
     */
    suspend fun updateHalfLife(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.HALF_LIFE_MINUTES] = minutes
        }
    }
    
    /**
     * Update sleep threshold setting.
     * @param mg Caffeine level in mg below which sleep is considered safe
     */
    suspend fun updateSleepThreshold(mg: Int) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.SLEEP_THRESHOLD_MG] = mg
        }
    }
    
    /**
     * Update absorption rate setting.
     * @param minutes Time to peak blood concentration (typically 15-60 minutes)
     */
    suspend fun updateAbsorptionRate(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.ABSORPTION_RATE_MINUTES] = minutes
        }
    }
    
    /**
     * Update sleep time setting.
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     */
    suspend fun updateSleepTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.SLEEP_TIME_HOUR] = hour
            prefs[SettingsKeys.SLEEP_TIME_MINUTE] = minute
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateUse24HourClock(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.USE_24_HOUR_CLOCK] = enabled
        }
    }

    suspend fun updateDateFormat(dateFormat: AppDateFormat) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.DATE_FORMAT] = dateFormat.name
        }
    }

    suspend fun updateTimeZoneId(timeZoneId: String) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.TIME_ZONE_ID] = timeZoneId
        }
    }

    suspend fun completeOnboarding(profile: DerivedOnboardingProfile) {
        context.dataStore.edit { prefs ->
            prefs.writeOnboardingCompletion(profile)
        }
    }

    suspend fun markOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.ONBOARDING_COMPLETE] = true
        }
    }
}

internal fun Preferences.toUserSettings(defaultSettings: UserSettings): UserSettings {
    val hasLegacyProfilePrefs = this.hasLegacyProfilePrefs()

    return UserSettings(
        halfLifeMinutes = this[SettingsKeys.HALF_LIFE_MINUTES] ?: defaultSettings.halfLifeMinutes,
        sleepThresholdMg = this[SettingsKeys.SLEEP_THRESHOLD_MG] ?: defaultSettings.sleepThresholdMg,
        absorptionRateMinutes = this[SettingsKeys.ABSORPTION_RATE_MINUTES] ?: defaultSettings.absorptionRateMinutes,
        sleepTimeHour = this[SettingsKeys.SLEEP_TIME_HOUR] ?: defaultSettings.sleepTimeHour,
        sleepTimeMinute = this[SettingsKeys.SLEEP_TIME_MINUTE] ?: defaultSettings.sleepTimeMinute,
        themeMode = ThemeMode.fromStorage(this[SettingsKeys.THEME_MODE]),
        useDynamicColor = this[SettingsKeys.USE_DYNAMIC_COLOR] ?: defaultSettings.useDynamicColor,
        use24HourClock = this[SettingsKeys.USE_24_HOUR_CLOCK] ?: defaultSettings.use24HourClock,
        dateFormat = AppDateFormat.fromStorage(this[SettingsKeys.DATE_FORMAT]),
        timeZoneId = this[SettingsKeys.TIME_ZONE_ID] ?: defaultSettings.timeZoneId,
        isOnboardingComplete = this[SettingsKeys.ONBOARDING_COMPLETE] ?: hasLegacyProfilePrefs,
    )
}

internal fun MutablePreferences.writeOnboardingCompletion(profile: DerivedOnboardingProfile) {
    this[SettingsKeys.HALF_LIFE_MINUTES] = profile.halfLifeMinutes
    this[SettingsKeys.SLEEP_THRESHOLD_MG] = profile.sleepThresholdMg
    this[SettingsKeys.SLEEP_TIME_HOUR] = profile.sleepTimeHour
    this[SettingsKeys.SLEEP_TIME_MINUTE] = profile.sleepTimeMinute
    this[SettingsKeys.ONBOARDING_COMPLETE] = true
}

internal fun Preferences.hasLegacyProfilePrefs(): Boolean {
    return this[SettingsKeys.HALF_LIFE_MINUTES] != null ||
        this[SettingsKeys.SLEEP_THRESHOLD_MG] != null ||
        this[SettingsKeys.SLEEP_TIME_HOUR] != null ||
        this[SettingsKeys.SLEEP_TIME_MINUTE] != null
}
