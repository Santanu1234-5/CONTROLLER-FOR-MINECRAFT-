package com.example.ui.controller

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.KeyBindings
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * 8-Way Floating Movement Joystick.
 * Sends simultaneous WASD keys for diagonal movement and releases all keys when centered or released.
 */
@Composable
fun MovementJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    keyBindings: KeyBindings,
    opacity: Float = 0.85f,
    hapticEnabled: Boolean = true,
    isEditMode: Boolean = false,
    accentColor: Color = EmeraldGreen,
    accentDarkColor: Color = EmeraldDark,
    onDirectionChanged: (Set<Byte>) -> Unit
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val sizePx = with(density) { size.toPx() }
    val radiusPx = sizePx / 2f
    val deadZonePx = radiusPx * 0.20f
    val knobRadiusPx = radiusPx * 0.38f

    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var activeDirectionKeys by remember { mutableStateOf<Set<Byte>>(emptySet()) }

    fun updateTouch(touchX: Float, touchY: Float) {
        val dist = hypot(touchX, touchY)
        val clampedDist = dist.coerceAtMost(radiusPx - knobRadiusPx)
        val angleRad = atan2(touchY, touchX)

        knobOffset = if (dist > 0) {
            Offset(
                x = cos(angleRad) * clampedDist,
                y = sin(angleRad) * clampedDist
            )
        } else {
            Offset.Zero
        }

        val newKeys = if (dist < deadZonePx) {
            emptySet()
        } else {
            // angle in degrees [-180, 180]
            val angleDeg = Math.toDegrees(angleRad.toDouble())
            when {
                // Forward (W): around -90°
                angleDeg >= -112.5 && angleDeg < -67.5 -> setOf(keyBindings.forward)
                // Forward-Right (W+D): around -45°
                angleDeg >= -67.5 && angleDeg < -22.5 -> setOf(keyBindings.forward, keyBindings.right)
                // Right (D): around 0°
                angleDeg >= -22.5 && angleDeg < 22.5 -> setOf(keyBindings.right)
                // Backward-Right (S+D): around +45°
                angleDeg >= 22.5 && angleDeg < 67.5 -> setOf(keyBindings.backward, keyBindings.right)
                // Backward (S): around +90°
                angleDeg >= 67.5 && angleDeg < 112.5 -> setOf(keyBindings.backward)
                // Backward-Left (S+A): around +135°
                angleDeg >= 112.5 && angleDeg < 157.5 -> setOf(keyBindings.backward, keyBindings.left)
                // Forward-Left (W+A): around -135°
                angleDeg >= -157.5 && angleDeg < -112.5 -> setOf(keyBindings.forward, keyBindings.left)
                // Left (A): around 180° or -180°
                else -> setOf(keyBindings.left)
            }
        }

        if (newKeys != activeDirectionKeys) {
            if (hapticEnabled && newKeys.isNotEmpty()) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            activeDirectionKeys = newKeys
            onDirectionChanged(newKeys)
        }
    }

    fun releaseJoystick() {
        knobOffset = Offset.Zero
        if (activeDirectionKeys.isNotEmpty()) {
            activeDirectionKeys = emptySet()
            onDirectionChanged(emptySet())
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("movement_joystick")
            .then(
                if (isEditMode) {
                    Modifier
                } else {
                    Modifier.pointerInput(keyBindings, size, hapticEnabled) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            val pointerId = down.id
                            updateTouch(down.position.x - radiusPx, down.position.y - radiusPx)

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId }
                                    if (change != null) {
                                        if (!change.pressed) {
                                            break
                                        }
                                        change.consume()
                                        updateTouch(change.position.x - radiusPx, change.position.y - radiusPx)
                                    }
                                }
                            } finally {
                                releaseJoystick()
                            }
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)

            // Outer Base Ring
            drawCircle(
                color = Color(0xFF1E2822).copy(alpha = 0.75f * opacity),
                radius = radiusPx,
                center = center
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.5f * opacity),
                radius = radiusPx,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Deadzone boundary indicator
            drawCircle(
                color = Color(0xFF2E3D34).copy(alpha = 0.6f * opacity),
                radius = deadZonePx,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Directional cross markers (W, A, S, D)
            val isForward = activeDirectionKeys.contains(keyBindings.forward)
            val isBackward = activeDirectionKeys.contains(keyBindings.backward)
            val isLeft = activeDirectionKeys.contains(keyBindings.left)
            val isRight = activeDirectionKeys.contains(keyBindings.right)

            // W marker (Top)
            drawLine(
                color = if (isForward) accentColor else Color(0x66FFFFFF),
                start = Offset(center.x, center.y - radiusPx * 0.45f),
                end = Offset(center.x, center.y - radiusPx * 0.85f),
                strokeWidth = if (isForward) 5.dp.toPx() else 2.dp.toPx()
            )

            // S marker (Bottom)
            drawLine(
                color = if (isBackward) accentColor else Color(0x66FFFFFF),
                start = Offset(center.x, center.y + radiusPx * 0.45f),
                end = Offset(center.x, center.y + radiusPx * 0.85f),
                strokeWidth = if (isBackward) 5.dp.toPx() else 2.dp.toPx()
            )

            // A marker (Left)
            drawLine(
                color = if (isLeft) accentColor else Color(0x66FFFFFF),
                start = Offset(center.x - radiusPx * 0.45f, center.y),
                end = Offset(center.x - radiusPx * 0.85f, center.y),
                strokeWidth = if (isLeft) 5.dp.toPx() else 2.dp.toPx()
            )

            // D marker (Right)
            drawLine(
                color = if (isRight) accentColor else Color(0x66FFFFFF),
                start = Offset(center.x + radiusPx * 0.45f, center.y),
                end = Offset(center.x + radiusPx * 0.85f, center.y),
                strokeWidth = if (isRight) 5.dp.toPx() else 2.dp.toPx()
            )

            // Thumb Knob
            val knobCenter = center + knobOffset
            drawCircle(
                color = accentDarkColor.copy(alpha = 0.95f * opacity),
                radius = knobRadiusPx,
                center = knobCenter
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.95f * opacity),
                radius = knobRadiusPx * 0.7f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * opacity),
                radius = knobRadiusPx,
                center = knobCenter,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}
