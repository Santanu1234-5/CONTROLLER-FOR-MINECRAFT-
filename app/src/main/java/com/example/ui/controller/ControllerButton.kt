package com.example.ui.controller

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High-performance tactile controller button.
 * Uses direct pointer tracking so the button remains held down as long as the finger
 * touches the glass, and releases immediately when the finger is lifted.
 */
@Composable
fun ControllerButton(
    modifier: Modifier = Modifier,
    label: String,
    subLabel: String? = null,
    testTag: String,
    size: Dp = 64.dp,
    width: Dp = size,
    height: Dp = size,
    baseColor: Color,
    borderColor: Color,
    opacity: Float = 0.85f,
    hapticEnabled: Boolean = true,
    handcamGlow: Boolean = true,
    isEditMode: Boolean = false,
    isToggle: Boolean = false,
    isToggledOn: Boolean = false,
    onStateChanged: (Boolean) -> Unit
) {
    val view = LocalView.current
    var isPressedLocally by remember { mutableStateOf(false) }

    val isActive = if (isToggle) isToggledOn else isPressedLocally
    val scale by animateFloatAsState(
        targetValue = if (isPressedLocally) 0.90f else 1.0f,
        label = "btn_scale"
    )

    val glowModifier = if (isActive && handcamGlow) {
        Modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(14.dp),
            ambientColor = borderColor,
            spotColor = borderColor
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .scale(scale)
            .then(glowModifier)
            .testTag(testTag)
            .then(
                if (isEditMode) {
                    Modifier
                } else {
                    Modifier.pointerInput(isToggle, isToggledOn, hapticEnabled) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val pointerId = down.id
                            isPressedLocally = true
                            if (hapticEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                            if (!isToggle) {
                                onStateChanged(true)
                            }

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId }
                                    if (change == null || !change.pressed) {
                                        break
                                    }
                                    change.consume()
                                }
                                if (isToggle) {
                                    onStateChanged(!isToggledOn)
                                }
                            } finally {
                                isPressedLocally = false
                                if (!isToggle) {
                                    onStateChanged(false)
                                }
                            }
                        }
                    }
                }
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isEditMode -> borderColor.copy(alpha = 0.28f)
                    isActive && handcamGlow -> borderColor.copy(alpha = (0.78f * opacity).coerceAtMost(0.95f))
                    isActive -> borderColor.copy(alpha = 0.65f * opacity)
                    else -> baseColor.copy(alpha = 0.42f * opacity)
                }
            )
            .border(
                width = if (isActive) (if (handcamGlow) 3.dp else 2.5.dp) else 1.5.dp,
                color = when {
                    isEditMode -> Color.White.copy(alpha = 0.65f)
                    isActive -> Color.White.copy(alpha = (0.98f * opacity).coerceAtMost(1f))
                    else -> borderColor.copy(alpha = 0.85f * opacity)
                },
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = if (isActive) Color.White else borderColor,
                fontWeight = FontWeight.Bold,
                fontSize = when {
                    label.length > 6 -> 10.sp
                    label.length > 4 -> 11.sp
                    height < 42.dp -> 11.sp
                    else -> 13.sp
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    color = if (isActive) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.65f),
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
