package com.example.ui.controller

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.HudCustomization
import com.example.model.KeyBindings
import com.example.model.UsbHidKeyCodes
import com.example.ui.theme.AttackButtonBg
import com.example.ui.theme.AttackButtonBorder
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.JumpButtonBg
import com.example.ui.theme.JumpButtonBorder
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.UseButtonBg
import com.example.ui.theme.UseButtonBorder
import com.example.ui.theme.UtilityButtonBg
import com.example.ui.theme.UtilityButtonBorder
import kotlin.math.roundToInt

/**
 * 3-Zone Minecraft PC / Eaglercraft Controller Layout.
 * Supports Handcam HUD scaling, press glow, draggable zones, and classic Minecraft controls.
 */
@Composable
fun MinecraftControllerLayout(
    modifier: Modifier = Modifier,
    keyBindings: KeyBindings,
    hudConfig: HudCustomization,
    opacity: Float,
    haptic: Boolean,
    toggleSneakMode: Boolean,
    trackpadTapToClick: Boolean,
    trackpadHoldLeftClick: Boolean,
    isEditMode: Boolean,
    isSprintToggled: Boolean,
    isSneakToggled: Boolean,
    onSprintToggled: (Boolean) -> Unit,
    onSneakToggled: (Boolean) -> Unit,
    onPressKey: (Byte) -> Unit,
    onReleaseKey: (Byte) -> Unit,
    onSetHeldKeys: (Set<Byte>) -> Unit,
    onMouseMove: (Float, Float) -> Unit,
    onMouseWheel: (Int) -> Unit,
    onTrackpadHoldLeftChanged: (Boolean) -> Unit,
    onToggleTrackpadHoldLeft: (Boolean) -> Unit,
    onTrackpadTap: (Boolean) -> Unit,
    onLeftFireButtonChanged: (Boolean) -> Unit,
    onRightFireButtonChanged: (Boolean) -> Unit,
    onSetMouseButton: (Byte, Boolean) -> Unit,
    onUpdateHudConfig: (HudCustomization) -> Unit
) {
    val density = LocalDensity.current
    val btnScale = hudConfig.buttonScale.coerceIn(0.75f, 1.35f)
    val glow = hudConfig.handcamGlow

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Zone 1: Left Controls (Joystick & Sneak & Utilities)
            Box(
                modifier = Modifier
                    .width((212f * btnScale.coerceAtMost(1.15f)).dp)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            hudConfig.leftZoneOffsetX.roundToInt(),
                            hudConfig.leftZoneOffsetY.roundToInt()
                        )
                    }
                    .then(
                        if (isEditMode) {
                            Modifier
                                .border(1.5.dp, EmeraldGreen.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .pointerInput(hudConfig) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val maxOffsetPx = with(density) { 120.dp.toPx() }
                                        onUpdateHudConfig(
                                            hudConfig.copy(
                                                leftZoneOffsetX = (hudConfig.leftZoneOffsetX + dragAmount.x).coerceIn(-40f, maxOffsetPx),
                                                leftZoneOffsetY = (hudConfig.leftZoneOffsetY + dragAmount.y).coerceIn(-maxOffsetPx, 40f)
                                            )
                                        )
                                    }
                                }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // Secondary keys row above joystick (Tab, F5, F3, optional F1 Aim Lock for MSI/BlueStacks)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val topBtnW = (if (hudConfig.showExtraTacticalButtons) 44f else 54f) * btnScale.coerceAtMost(1.12f)
                        val topBtnH = 36f * btnScale.coerceAtMost(1.15f)

                        ControllerButton(
                            label = "Tab",
                            subLabel = "List",
                            testTag = "btn_tab",
                            width = topBtnW.dp,
                            height = topBtnH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(keyBindings.playerList)
                                else onReleaseKey(keyBindings.playerList)
                            }
                        )

                        ControllerButton(
                            label = "F5",
                            subLabel = "Cam",
                            testTag = "btn_f5",
                            width = topBtnW.dp,
                            height = topBtnH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(keyBindings.perspective)
                                else onReleaseKey(keyBindings.perspective)
                            }
                        )

                        ControllerButton(
                            label = "F3",
                            subLabel = "Debug",
                            testTag = "btn_f3",
                            width = topBtnW.dp,
                            height = topBtnH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(keyBindings.debug)
                                else onReleaseKey(keyBindings.debug)
                            }
                        )

                        if (hudConfig.showExtraTacticalButtons) {
                            ControllerButton(
                                label = "AIM",
                                subLabel = "F1",
                                testTag = "btn_mc_f1_aim",
                                width = topBtnW.dp,
                                height = topBtnH.dp,
                                baseColor = UtilityButtonBg,
                                borderColor = DiamondCyan,
                                opacity = opacity,
                                hapticEnabled = haptic,
                                handcamGlow = glow,
                                isEditMode = isEditMode,
                                onStateChanged = { pressed ->
                                    if (pressed) onPressKey(UsbHidKeyCodes.KEY_F1)
                                    else onReleaseKey(UsbHidKeyCodes.KEY_F1)
                                }
                            )
                        }
                    }

                    // 8-Way Movement Joystick
                    MovementJoystick(
                        size = (160f * hudConfig.joystickScale.coerceIn(0.8f, 1.3f)).dp,
                        keyBindings = keyBindings,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        isEditMode = isEditMode,
                        accentColor = EmeraldGreen,
                        accentDarkColor = EmeraldDark,
                        onDirectionChanged = { keys ->
                            if (!isEditMode) onSetHeldKeys(keys)
                        }
                    )

                    // Sprint (L-Ctrl) and Sneak (L-Shift) Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val modW = 96f * btnScale.coerceAtMost(1.12f)
                        val modH = 40f * btnScale.coerceAtMost(1.15f)

                        ControllerButton(
                            label = if (isSprintToggled) "SPRINT ON" else "SPRINT",
                            subLabel = "L-Ctrl",
                            testTag = "btn_sprint",
                            width = modW.dp,
                            height = modH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = EmeraldGreen,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            isToggle = true,
                            isToggledOn = isSprintToggled,
                            onStateChanged = onSprintToggled
                        )

                        ControllerButton(
                            label = if (toggleSneakMode && isSneakToggled) "SNEAK ON" else "SNEAK",
                            subLabel = "L-Shift",
                            testTag = "btn_sneak",
                            width = modW.dp,
                            height = modH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = DiamondCyan,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            isToggle = toggleSneakMode,
                            isToggledOn = isSneakToggled,
                            onStateChanged = onSneakToggled
                        )
                    }
                }
            }

            // Zone 2: Center (Camera Look Trackpad at top, Separated Hotbar at bottom)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp)
            ) {
                LookArea(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    opacity = opacity,
                    tapToClickEnabled = trackpadTapToClick,
                    holdLeftClickEnabled = trackpadHoldLeftClick,
                    hapticEnabled = haptic,
                    onRelativeMove = { dx, dy ->
                        if (!isEditMode) onMouseMove(dx, dy)
                    },
                    onHoldLeftClickChanged = { pressed ->
                        if (!isEditMode) onTrackpadHoldLeftChanged(pressed)
                    },
                    onToggleHoldLeftClick = onToggleTrackpadHoldLeft,
                    onTap = { isDoubleTap ->
                        if (!isEditMode) onTrackpadTap(isDoubleTap)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Center Hotbar: Complete slots 1-9, mouse wheel cycle buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barH = 42f * btnScale.coerceAtMost(1.15f)
                    ControllerButton(
                        label = "INV",
                        subLabel = "E",
                        testTag = "btn_inventory",
                        width = 40.dp,
                        height = barH.dp,
                        baseColor = UtilityButtonBg,
                        borderColor = EmeraldGreen,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        handcamGlow = glow,
                        isEditMode = isEditMode,
                        onStateChanged = { pressed ->
                            if (pressed) onPressKey(keyBindings.inventory)
                            else onReleaseKey(keyBindings.inventory)
                        }
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    ControllerButton(
                        label = "◄",
                        testTag = "btn_slot_prev",
                        width = 30.dp,
                        height = barH.dp,
                        baseColor = UtilityButtonBg,
                        borderColor = GoldYellow,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        handcamGlow = glow,
                        isEditMode = isEditMode,
                        onStateChanged = { pressed ->
                            if (pressed) onMouseWheel(-1)
                        }
                    )

                    val hotbarKeys = listOf(
                        Pair("1", UsbHidKeyCodes.KEY_1),
                        Pair("2", UsbHidKeyCodes.KEY_2),
                        Pair("3", UsbHidKeyCodes.KEY_3),
                        Pair("4", UsbHidKeyCodes.KEY_4),
                        Pair("5", UsbHidKeyCodes.KEY_5),
                        Pair("6", UsbHidKeyCodes.KEY_6),
                        Pair("7", UsbHidKeyCodes.KEY_7),
                        Pair("8", UsbHidKeyCodes.KEY_8),
                        Pair("9", UsbHidKeyCodes.KEY_9)
                    )

                    hotbarKeys.forEach { (slotLabel, keyCode) ->
                        Spacer(modifier = Modifier.width(3.dp))
                        ControllerButton(
                            label = slotLabel,
                            testTag = "btn_slot_$slotLabel",
                            width = 28.dp,
                            height = barH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(keyCode)
                                else onReleaseKey(keyCode)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    ControllerButton(
                        label = "►",
                        testTag = "btn_slot_next",
                        width = 30.dp,
                        height = barH.dp,
                        baseColor = UtilityButtonBg,
                        borderColor = GoldYellow,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        handcamGlow = glow,
                        isEditMode = isEditMode,
                        onStateChanged = { pressed ->
                            if (pressed) onMouseWheel(1)
                        }
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    ControllerButton(
                        label = "DROP",
                        subLabel = "Q",
                        testTag = "btn_drop",
                        width = 40.dp,
                        height = barH.dp,
                        baseColor = UtilityButtonBg,
                        borderColor = RedstoneRed,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        handcamGlow = glow,
                        isEditMode = isEditMode,
                        onStateChanged = { pressed ->
                            if (pressed) onPressKey(keyBindings.drop)
                            else onReleaseKey(keyBindings.drop)
                        }
                    )
                }
            }

            // Zone 3: Right Controls (Chat, Offhand/Pick, Esc + Fire/Mine, Use/Place, Jump)
            Box(
                modifier = Modifier
                    .width((228f * btnScale.coerceAtMost(1.15f)).dp)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            hudConfig.rightZoneOffsetX.roundToInt(),
                            hudConfig.rightZoneOffsetY.roundToInt()
                        )
                    }
                    .then(
                        if (isEditMode) {
                            Modifier
                                .border(1.5.dp, DiamondCyan.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .pointerInput(hudConfig) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val maxOffsetPx = with(density) { 120.dp.toPx() }
                                        onUpdateHudConfig(
                                            hudConfig.copy(
                                                rightZoneOffsetX = (hudConfig.rightZoneOffsetX + dragAmount.x).coerceIn(-maxOffsetPx, 40f),
                                                rightZoneOffsetY = (hudConfig.rightZoneOffsetY + dragAmount.y).coerceIn(-maxOffsetPx, 40f)
                                            )
                                        )
                                    }
                                }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // Utility row: Chat, Offhand (F), Pick Block, Pause / Menu (Escape)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val utilW = (if (hudConfig.showExtraTacticalButtons) 48f else 54f) * btnScale.coerceAtMost(1.12f)
                        val utilH = 36f * btnScale.coerceAtMost(1.15f)

                        ControllerButton(
                            label = "CHAT",
                            subLabel = "T",
                            testTag = "btn_chat",
                            width = utilW.dp,
                            height = utilH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(UsbHidKeyCodes.KEY_T)
                                else onReleaseKey(UsbHidKeyCodes.KEY_T)
                            }
                        )

                        if (hudConfig.showExtraTacticalButtons) {
                            ControllerButton(
                                label = "SWAP",
                                subLabel = "F",
                                testTag = "btn_offhand",
                                width = utilW.dp,
                                height = utilH.dp,
                                baseColor = UtilityButtonBg,
                                borderColor = EmeraldGreen,
                                opacity = opacity,
                                hapticEnabled = haptic,
                                handcamGlow = glow,
                                isEditMode = isEditMode,
                                onStateChanged = { pressed ->
                                    if (pressed) onPressKey(UsbHidKeyCodes.KEY_F)
                                    else onReleaseKey(UsbHidKeyCodes.KEY_F)
                                }
                            )
                        }

                        ControllerButton(
                            label = "PICK",
                            subLabel = "M-Click",
                            testTag = "btn_pick_block",
                            width = utilW.dp,
                            height = utilH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = GoldYellow,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                onSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_MIDDLE, pressed)
                            }
                        )

                        ControllerButton(
                            label = "ESC",
                            subLabel = "Pause",
                            testTag = "btn_escape",
                            width = utilW.dp,
                            height = utilH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(keyBindings.menu)
                                else onReleaseKey(keyBindings.menu)
                            }
                        )
                    }

                    // Action buttons cluster
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Fire / Mine (Left Click) - Redstone Red
                        ControllerButton(
                            label = "FIRE",
                            subLabel = "L-Click",
                            testTag = "btn_attack",
                            width = (82f * btnScale).dp,
                            height = (82f * btnScale).dp,
                            baseColor = AttackButtonBg,
                            borderColor = AttackButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = onRightFireButtonChanged
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Use / Place (Right Click) - Diamond Cyan
                            ControllerButton(
                                label = "USE/PLACE",
                                subLabel = "R-Click",
                                testTag = "btn_use_place",
                                width = (94f * btnScale).dp,
                                height = (64f * btnScale).dp,
                                baseColor = UseButtonBg,
                                borderColor = UseButtonBorder,
                                opacity = opacity,
                                hapticEnabled = haptic,
                                handcamGlow = glow,
                                isEditMode = isEditMode,
                                onStateChanged = { pressed ->
                                    onSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_RIGHT, pressed)
                                }
                            )

                            // Jump (Space) - Gold Yellow
                            ControllerButton(
                                label = "JUMP",
                                subLabel = "Space",
                                testTag = "btn_jump",
                                width = (94f * btnScale).dp,
                                height = (64f * btnScale).dp,
                                baseColor = JumpButtonBg,
                                borderColor = JumpButtonBorder,
                                opacity = opacity,
                                hapticEnabled = haptic,
                                handcamGlow = glow,
                                isEditMode = isEditMode,
                                onStateChanged = { pressed ->
                                    if (pressed) onPressKey(keyBindings.jump)
                                    else onReleaseKey(keyBindings.jump)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Optional 2nd Left Fire Button (when enabled in 3/4-Finger Claw Handcam settings)
        if (hudConfig.showLeftFireButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 224.dp, top = 8.dp)
                    .offset {
                        IntOffset(
                            hudConfig.leftFireOffsetX.roundToInt(),
                            hudConfig.leftFireOffsetY.roundToInt()
                        )
                    }
                    .then(
                        if (isEditMode) {
                            Modifier
                                .border(1.5.dp, AttackButtonBorder, RoundedCornerShape(14.dp))
                                .pointerInput(hudConfig) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val maxX = with(density) { 180.dp.toPx() }
                                        val maxY = with(density) { 180.dp.toPx() }
                                        onUpdateHudConfig(
                                            hudConfig.copy(
                                                leftFireOffsetX = (hudConfig.leftFireOffsetX + dragAmount.x).coerceIn(-200f, maxX),
                                                leftFireOffsetY = (hudConfig.leftFireOffsetY + dragAmount.y).coerceIn(-10f, maxY)
                                            )
                                        )
                                    }
                                }
                        } else {
                            Modifier
                        }
                    )
            ) {
                ControllerButton(
                    label = "FIRE 2",
                    subLabel = "L-Claw",
                    testTag = "btn_mc_left_fire",
                    width = (64f * btnScale).dp,
                    height = (52f * btnScale).dp,
                    baseColor = AttackButtonBg,
                    borderColor = AttackButtonBorder,
                    opacity = opacity,
                    hapticEnabled = haptic,
                    handcamGlow = glow,
                    isEditMode = isEditMode,
                    onStateChanged = onLeftFireButtonChanged
                )
            }
        }
    }
}
