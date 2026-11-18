package com.example.ui.controller

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RedstoneRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Large camera look trackpad.
 * Supports:
 * 1. HOLD TO FIRE / MINE: Holds left mouse button continuously while pressed,
 *    releasing immediately when finger is lifted.
 * 2. CAMERA LOOK: Relative mouse deltas steer camera smoothly.
 * 3. TAP TO CLICK: Single tap and double tap when hold mode is disabled.
 */
@Composable
fun LookArea(
    modifier: Modifier = Modifier,
    opacity: Float = 0.85f,
    tapToClickEnabled: Boolean = true,
    holdLeftClickEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
    onRelativeMove: (dx: Float, dy: Float) -> Unit,
    onHoldLeftClickChanged: (isPressed: Boolean) -> Unit = {},
    onToggleHoldLeftClick: ((Boolean) -> Unit)? = null,
    onSingleClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onTap: (isDoubleTap: Boolean) -> Unit = { isDouble ->
        if (isDouble) onDoubleClick() else onSingleClick()
    }
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var isHoldingDown by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var lastTapPos by remember { mutableStateOf(Offset.Zero) }

    // Animated ripple state for tap/double-tap feedback
    var ripplePos by remember { mutableStateOf<Offset?>(null) }
    var rippleIsDouble by remember { mutableStateOf(false) }
    val rippleAnim = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .testTag("look_area_trackpad")
            .pointerInput(tapToClickEnabled, holdLeftClickEnabled, hapticEnabled) {
                val touchSlop = viewConfiguration.touchSlop
                val doubleTapTimeout = 320L
                val doubleTapMaxDist = 60.dp.toPx()

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    val startTime = System.currentTimeMillis()
                    val startPos = down.position
                    touchPos = startPos

                    if (holdLeftClickEnabled) {
                        isHoldingDown = true
                        if (hapticEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                        onHoldLeftClickChanged(true)
                    }

                    var maxDistFromStart = 0f
                    var isDrag = false
                    var accumulatedDx = 0f
                    var accumulatedDy = 0f

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }
                            if (change != null) {
                                if (!change.pressed) {
                                    touchPos = null
                                    val elapsed = System.currentTimeMillis() - startTime
                                    val releasePos = change.position

                                    if (holdLeftClickEnabled) {
                                        isHoldingDown = false
                                        onHoldLeftClickChanged(false)
                                    } else if (tapToClickEnabled && !isDrag && elapsed < 300L && maxDistFromStart < touchSlop * 1.6f) {
                                        val now = System.currentTimeMillis()
                                        val isDouble = (now - lastTapTime <= doubleTapTimeout) &&
                                                ((releasePos - lastTapPos).getDistance() <= doubleTapMaxDist)

                                        if (isDouble) {
                                            lastTapTime = 0L
                                            if (hapticEnabled) {
                                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            }
                                            ripplePos = releasePos
                                            rippleIsDouble = true
                                            coroutineScope.launch {
                                                if (hapticEnabled) {
                                                    delay(60)
                                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                                }
                                                rippleAnim.snapTo(0f)
                                                rippleAnim.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                                                )
                                                ripplePos = null
                                            }
                                            onTap(true)
                                        } else {
                                            lastTapTime = now
                                            lastTapPos = releasePos
                                            if (hapticEnabled) {
                                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            }
                                            ripplePos = releasePos
                                            rippleIsDouble = false
                                            coroutineScope.launch {
                                                rippleAnim.snapTo(0f)
                                                rippleAnim.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                                )
                                                ripplePos = null
                                            }
                                            onTap(false)
                                        }
                                    }
                                    break
                                }

                                val rawDelta = change.position - change.previousPosition
                                val dist = (change.position - startPos).getDistance()
                                if (dist > maxDistFromStart) {
                                    maxDistFromStart = dist
                                }
                                accumulatedDx += rawDelta.x
                                accumulatedDy += rawDelta.y

                                if (!isDrag && maxDistFromStart > touchSlop) {
                                    isDrag = true
                                    if (accumulatedDx != 0f || accumulatedDy != 0f) {
                                        onRelativeMove(accumulatedDx, accumulatedDy)
                                        accumulatedDx = 0f
                                        accumulatedDy = 0f
                                    }
                                } else if (isDrag) {
                                    if (rawDelta.x != 0f || rawDelta.y != 0f) {
                                        onRelativeMove(rawDelta.x, rawDelta.y)
                                    }
                                }

                                if (rawDelta.x != 0f || rawDelta.y != 0f) {
                                    change.consume()
                                    touchPos = change.position
                                }
                            }
                        }
                    } finally {
                        touchPos = null
                        if (isHoldingDown) {
                            isHoldingDown = false
                            onHoldLeftClickChanged(false)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle dashed border for look zone boundaries
            val borderColor = if (isHoldingDown) RedstoneRed else Color(0xFF38BDF8)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawRect(
                color = borderColor.copy(alpha = if (isHoldingDown) 0.35f * opacity else 0.18f * opacity),
                style = Stroke(width = if (isHoldingDown) 2.dp.toPx() else 1.5.dp.toPx(), pathEffect = dashEffect)
            )

            // Center subtle crosshair watermark
            val cx = w / 2f
            val cy = h / 2f
            val crossSize = 18.dp.toPx()
            val crosshairColor = if (isHoldingDown) RedstoneRed else Color.White
            drawLine(
                color = crosshairColor.copy(alpha = if (isHoldingDown) 0.45f * opacity else 0.15f * opacity),
                start = Offset(cx - crossSize, cy),
                end = Offset(cx + crossSize, cy),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = crosshairColor.copy(alpha = if (isHoldingDown) 0.45f * opacity else 0.15f * opacity),
                start = Offset(cx, cy - crossSize),
                end = Offset(cx, cy + crossSize),
                strokeWidth = 1.5.dp.toPx()
            )

            // Dynamic touch point reticle when finger is dragging / holding
            touchPos?.let { pos ->
                val auraColor = if (isHoldingDown) RedstoneRed else Color(0xFF38BDF8)
                drawCircle(
                    color = auraColor.copy(alpha = if (isHoldingDown) 0.55f * opacity else 0.4f * opacity),
                    radius = if (isHoldingDown) 34.dp.toPx() else 28.dp.toPx(),
                    center = pos
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f * opacity),
                    radius = 8.dp.toPx(),
                    center = pos
                )
            }

            // Expanding ripple on tap (single or double click)
            ripplePos?.let { pos ->
                val progress = rippleAnim.value
                val baseRadius = 14.dp.toPx() + progress * 36.dp.toPx()
                val alpha = (1f - progress) * 0.75f * opacity
                val color = if (rippleIsDouble) EmeraldGreen else DiamondCyan

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = baseRadius,
                    center = pos,
                    style = Stroke(width = if (rippleIsDouble) 3.dp.toPx() else 2.dp.toPx())
                )

                if (rippleIsDouble) {
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.9f),
                        radius = (baseRadius - 10.dp.toPx()).coerceAtLeast(0f),
                        center = pos,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        // Quick Hold-to-Fire Mode Pill Toggle (Top-Right of Trackpad)
        if (onToggleHoldLeftClick != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (holdLeftClickEnabled) Color(0x55EF4444) else Color(0x331E293B),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (holdLeftClickEnabled) RedstoneRed else Color(0xFF475569)
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable {
                        onToggleHoldLeftClick(!holdLeftClickEnabled)
                    }
                    .testTag("toggle_trackpad_hold_fire")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (holdLeftClickEnabled) RedstoneRed else Color.Gray, RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = if (holdLeftClickEnabled) " HOLD FIRE: ON " else " HOLD FIRE: OFF ",
                        color = if (holdLeftClickEnabled) Color.White else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Watermark guidance text centered at bottom of trackpad
        val watermarkText = when {
            isHoldingDown -> "🔥 HOLDING LEFT CLICK (MINING / FIRING)..."
            holdLeftClickEnabled -> "TRACKPAD • TOUCH & HOLD TO FIRE / MINE"
            tapToClickEnabled -> "TRACKPAD • TAP: CLICK • 2x: DBL-CLICK"
            else -> "LOOK TRACKPAD"
        }

        Text(
            text = watermarkText,
            color = if (isHoldingDown) RedstoneRed.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.25f * opacity),
            fontSize = 10.sp,
            fontWeight = if (isHoldingDown) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}
