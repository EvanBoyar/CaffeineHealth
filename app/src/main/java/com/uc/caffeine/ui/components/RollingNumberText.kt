package com.uc.caffeine.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text

@Composable
fun RollingNumberText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Bottom,
    labelPrefix: String = "rolling_number",
) {
    val resolvedColor = if (style.color == Color.Unspecified) {
        LocalContentColor.current
    } else {
        style.color
    }
    val resolvedStyle = style.copy(
        color = resolvedColor,
        fontWeight = style.fontWeight ?: FontWeight.Bold,
    )

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        text.forEachIndexed { index, char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> height } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "${labelPrefix}_digit_$index",
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        style = resolvedStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    style = resolvedStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}
