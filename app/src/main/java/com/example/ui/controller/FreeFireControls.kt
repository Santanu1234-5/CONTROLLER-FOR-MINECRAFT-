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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.FreeFireKeyBindings
import com.example.model.HandcamClawPreset
import com.example.model.HudCustomization
import com.example.model.KeyBindings
import com.example.model.UsbHidKeyCodes
import com.example.ui.theme.AttackButtonBg
import com.example.ui.theme.AttackButtonBorder
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.BlazeOrangeDark
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.GlooButtonBg
import com.example.ui.theme.GlooButtonBorder
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.JumpButtonBg
import com.example.ui.theme.JumpButtonBorder
import com.example.ui.theme.MedkitButtonBg
import com.example.ui.theme.MedkitButtonBorder
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.UseButtonBg
import com.example.ui.theme.UseButtonBorder
import com.example.ui.theme.UtilityButtonBg
import com.example.ui.theme.UtilityButtonBorder
import kotlin.math.roundToInt

/**
 * 3-Zone Free Fire India PC / Emulator Controller Layout.
 * Tailored for BlueStacks, LDPlayer, and PC Battle Royale smart controls with
 * dedicated 2-Finger / 3-Finger / 4-Finger Handcam HUD customization.
 */
@Composable
fun FreeFireControllerLayout(
    modifier: Modifier = Modifier,
    bindings: FreeFireKeyBindings,
    hudConfig: HudCustomization,
    opacity: Float,
    haptic: Boolean,
    trackpadTapToClick: Boolean,
    trackpadHoldLeftClick: Boolean,
    isEditMode: Boolean,
    isSprintToggled: Boolean,
    onSprintToggled: (Boolean) -> Unit,
    onPressKey: (Byte) -> Unit,
    onReleaseKey: (Byte) -> Unit,
    onSetHeldKeys: (Set<Byte>) -> Unit,
    onMouseMove: (Float, Float) -> Unit,
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
    val isFourFinger = hudConfig.clawPreset == HandcamClawPreset.FOUR_FINGER

    val joystickBindings = KeyBindings(
        forward = bindings.forward,
        backward = bindings.backward,
        left = bindings.left,
        right = bindings.right
    )

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            // ==========================================
            // Zone 1: Left Controls (Utilities + Movement Joystick + Run/Crouch/Prone)
            // ==========================================
            Box(
                modifier = Modifier
                    .width((218f * btnScale.coerceAtMost(1.15f)).dp)
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
                                .border(1.5.dp, BlazeOrange.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
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
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Top Left Utility Row: MAP, BAG, AIM LOCK, PING
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val utilW = (if (hudConfig.showExtraTacticalButtons) 48f else 62f) * btnScale.coerceAtMost(1.12f)
                        val utilH = 36f * btnScale.coerceAtMost(1.15f)

                        ControllerButton(
                            label = "MAP",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.map),
                            testTag = "btn_ff_map",
                            width = utilW.dp,
                            height = utilH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.map) else onReleaseKey(bindings.map)
                            }
                        )

                        ControllerButton(
                            label = "BAG",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.backpack),
                            testTag = "btn_ff_bag",
                            width = utilW.dp,
                            height = utilH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.backpack) else onReleaseKey(bindings.backpack)
                            }
                        )

                        ControllerButton(
                            label = "AIM",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.cursorToggle),
                            testTag = "btn_ff_cursor",
                            width = utilW.dp,
                            height = utilH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = BlazeOrange,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.cursorToggle) else onReleaseKey(bindings.cursorToggle)
                            }
                        )

                        if (hudConfig.showExtraTacticalButtons) {
                            ControllerButton(
                                label = "PING",
                                subLabel = UsbHidKeyCodes.getKeyLabel(bindings.ping),
                                testTag = "btn_ff_ping",
                                width = utilW.dp,
                                height = utilH.dp,
                                baseColor = UtilityButtonBg,
                                borderColor = DiamondCyan,
                                opacity = opacity,
                                hapticEnabled = haptic,
                                handcamGlow = glow,
                                isEditMode = isEditMode,
                                onStateChanged = { pressed ->
                                    if (pressed) onPressKey(bindings.ping) else onReleaseKey(bindings.ping)
                                }
                            )
                        }
                    }

                    // 8-Way Movement Joystick (Blaze Orange tactical style, scaled for handcam)
                    MovementJoystick(
                        size = (150f * hudConfig.joystickScale.coerceIn(0.8f, 1.3f)).dp,
                        keyBindings = joystickBindings,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        isEditMode = isEditMode,
                        accentColor = BlazeOrange,
                        accentDarkColor = BlazeOrangeDark,
                        onDirectionChanged = { keys ->
                            if (!isEditMode) onSetHeldKeys(keys)
                        }
                    )

                    // Run / Crouch / Prone Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stanceW = 64f * btnScale.coerceAtMost(1.12f)
                        val stanceH = 40f * btnScale.coerceAtMost(1.15f)

                        ControllerButton(
                            label = if (isSprintToggled) "RUN ON" else "RUN",
                            subLabel = "Shift",
                            testTag = "btn_ff_sprint",
                            width = stanceW.dp,
                            height = stanceH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = BlazeOrange,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            isToggle = true,
                            isToggledOn = isSprintToggled,
                            onStateChanged = onSprintToggled
                        )

                        ControllerButton(
                            label = "CROUCH",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.crouch),
                            testTag = "btn_ff_crouch",
                            width = stanceW.dp,
                            height = stanceH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = DiamondCyan,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.crouch) else onReleaseKey(bindings.crouch)
                            }
                        )

                        ControllerButton(
                            label = "PRONE",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.prone),
                            testTag = "btn_ff_prone",
                            width = stanceW.dp,
                            height = stanceH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = TacticalAmber,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.prone) else onReleaseKey(bindings.prone)
                            }
                        )
                    }
                }
            }

            // ==========================================
            // Zone 2: Center (Aim Trackpad + Weapon/Quick-Switch/Gloo/Nade/Medkit Dock)
            // ==========================================
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
                    onTap = { isDouble ->
                        if (!isEditMode) onTrackpadTap(isDouble)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Weapons & Tactical Utility Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val slotW = (if (hudConfig.showExtraTacticalButtons) 36f else 44f) * btnScale.coerceAtMost(1.1f)
                    val dockH = 44f * btnScale.coerceAtMost(1.15f)

                    val weaponSlots = listOf(
                        Triple("1", "AR", UsbHidKeyCodes.KEY_1),
                        Triple("2", "SMG", UsbHidKeyCodes.KEY_2),
                        Triple("3", "PST", UsbHidKeyCodes.KEY_3),
                        Triple("4", "PAN", UsbHidKeyCodes.KEY_4)
                    )

                    weaponSlots.forEach { (slotNum, tagLabel, keyCode) ->
                        ControllerButton(
                            label = slotNum,
                            subLabel = tagLabel,
                            testTag = "btn_ff_weapon_$slotNum",
                            width = slotW.dp,
                            height = dockH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(keyCode) else onReleaseKey(keyCode)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (hudConfig.showExtraTacticalButtons) {
                        // Quick Weapon Switch (Q) — sniper/shotgun swap
                        ControllerButton(
                            label = "Q-SW",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.quickSwitch),
                            testTag = "btn_ff_quick_switch",
                            width = (44f * btnScale.coerceAtMost(1.1f)).dp,
                            height = dockH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = GoldYellow,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.quickSwitch) else onReleaseKey(bindings.quickSwitch)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Gloo Wall button
                    ControllerButton(
                        label = "GLOO",
                        subLabel = UsbHidKeyCodes.getKeyLabel(bindings.glooWall),
                        testTag = "btn_ff_gloo",
                        width = (54f * btnScale.coerceAtMost(1.1f)).dp,
                        height = dockH.dp,
                        baseColor = GlooButtonBg,
                        borderColor = GlooButtonBorder,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        handcamGlow = glow,
                        isEditMode = isEditMode,
                        onStateChanged = { pressed ->
                            if (pressed) onPressKey(bindings.glooWall) else onReleaseKey(bindings.glooWall)
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    if (hudConfig.showExtraTacticalButtons) {
                        // Grenade button
                        ControllerButton(
                            label = "NADE",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.grenade),
                            testTag = "btn_ff_grenade",
                            width = (44f * btnScale.coerceAtMost(1.1f)).dp,
                            height = dockH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = TacticalAmber,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.grenade) else onReleaseKey(bindings.grenade)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Medkit Heal button
                    ControllerButton(
                        label = "MED",
                        subLabel = UsbHidKeyCodes.getKeyLabel(bindings.medkit),
                        testTag = "btn_ff_medkit",
                        width = (54f * btnScale.coerceAtMost(1.1f)).dp,
                        height = dockH.dp,
                        baseColor = MedkitButtonBg,
                        borderColor = MedkitButtonBorder,
                        opacity = opacity,
                        hapticEnabled = haptic,
                        handcamGlow = glow,
                        isEditMode = isEditMode,
                        onStateChanged = { pressed ->
                            if (pressed) onPressKey(bindings.medkit) else onReleaseKey(bindings.medkit)
                        }
                    )
                }
            }

            // ==========================================
            // Zone 3: Right Controls (Loot / Skill / Emote / Esc + Fire / Scope / Reload / Jump)
            // ==========================================
            Box(
                modifier = Modifier
                    .width((234f * btnScale.coerceAtMost(1.15f)).dp)
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
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Top Right Utility Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val topW = (if (hudConfig.showExtraTacticalButtons) 50f else 66f) * btnScale.coerceAtMost(1.12f)
                        val topH = 38f * btnScale.coerceAtMost(1.15f)

                        ControllerButton(
                            label = "LOOT",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.loot),
                            testTag = "btn_ff_loot",
                            width = topW.dp,
                            height = topH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = GoldYellow,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.loot) else onReleaseKey(bindings.loot)
                            }
                        )

                        ControllerButton(
                            label = "SKILL",
                            subLabel = UsbHidKeyCodes.getKeyLabel(bindings.skill),
                            testTag = "btn_ff_skill",
                            width = topW.dp,
                            height = topH.dp,
                            baseColor = GlooButtonBg,
                            borderColor = BlazeOrange,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.skill) else onReleaseKey(bindings.skill)
                            }
                        )

                        if (hudConfig.showExtraTacticalButtons) {
                            ControllerButton(
                                label = "EMOTE",
                                subLabel = UsbHidKeyCodes.getKeyLabel(bindings.emote),
                                testTag = "btn_ff_emote",
                                width = topW.dp,
                                height = topH.dp,
                                baseColor = UtilityButtonBg,
                                borderColor = Color(0xFFA855F7),
                                opacity = opacity,
                                hapticEnabled = haptic,
                                handcamGlow = glow,
                                isEditMode = isEditMode,
                                onStateChanged = { pressed ->
                                    if (pressed) onPressKey(bindings.emote) else onReleaseKey(bindings.emote)
                                }
                            )
                        }

                        ControllerButton(
                            label = "ESC",
                            subLabel = "Menu",
                            testTag = "btn_ff_escape",
                            width = (48f * btnScale.coerceAtMost(1.12f)).dp,
                            height = topH.dp,
                            baseColor = UtilityButtonBg,
                            borderColor = UtilityButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = { pressed ->
                                if (pressed) onPressKey(bindings.menu) else onReleaseKey(bindings.menu)
                            }
                        )
                    }

                    // Combat Action Cluster (arranges Scope/Jump higher in 4-finger claw!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val fireW = 84f * btnScale
                        val fireH = 88f * btnScale
                        val sideColW = 116f * btnScale
                        val halfColW = (sideColW - 6f) / 2f

                        // FIRE (Left Mouse Button — holds while pressed, releases on lift)
                        ControllerButton(
                            label = "FIRE",
                            subLabel = "L-Click",
                            testTag = "btn_attack",
                            width = fireW.dp,
                            height = fireH.dp,
                            baseColor = AttackButtonBg,
                            borderColor = AttackButtonBorder,
                            opacity = opacity,
                            hapticEnabled = haptic,
                            handcamGlow = glow,
                            isEditMode = isEditMode,
                            onStateChanged = onRightFireButtonChanged
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top sub-row of right cluster
                            if (isFourFinger) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ControllerButton(
                                        label = "SCOPE",
                                        subLabel = "R-Click",
                                        testTag = "btn_ff_scope",
                                        width = halfColW.dp,
                                        height = (58f * btnScale).dp,
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
                                    ControllerButton(
                                        label = "JUMP",
                                        subLabel = "Space",
                                        testTag = "btn_ff_jump",
                                        width = halfColW.dp,
                                        height = (58f * btnScale).dp,
                                        baseColor = JumpButtonBg,
                                        borderColor = JumpButtonBorder,
                                        opacity = opacity,
                                        hapticEnabled = haptic,
                                        handcamGlow = glow,
                                        isEditMode = isEditMode,
                                        onStateChanged = { pressed ->
                                            if (pressed) onPressKey(bindings.jump) else onReleaseKey(bindings.jump)
                                        }
                                    )
                                }
                                // Bottom full-width RELOAD in 4-Finger Claw
                                ControllerButton(
                                    label = "RELOAD",
                                    subLabel = UsbHidKeyCodes.getKeyLabel(bindings.reload),
                                    testTag = "btn_ff_reload",
                                    width = sideColW.dp,
                                    height = (48f * btnScale).dp,
                                    baseColor = UtilityButtonBg,
                                    borderColor = TacticalAmber,
                                    opacity = opacity,
                                    hapticEnabled = haptic,
                                    handcamGlow = glow,
                                    isEditMode = isEditMode,
                                    onStateChanged = { pressed ->
                                        if (pressed) onPressKey(bindings.reload) else onReleaseKey(bindings.reload)
                                    }
                                )
                            } else {
                                // Standard 2/3-Finger layout: Wide SCOPE on top, RELOAD + JUMP below
                                ControllerButton(
                                    label = "SCOPE",
                                    subLabel = "R-Click",
                                    testTag = "btn_ff_scope",
                                    width = sideColW.dp,
                                    height = (56f * btnScale).dp,
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

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ControllerButton(
                                        label = "RLD",
                                        subLabel = UsbHidKeyCodes.getKeyLabel(bindings.reload),
                                        testTag = "btn_ff_reload",
                                        width = halfColW.dp,
                                        height = (56f * btnScale).dp,
                                        baseColor = UtilityButtonBg,
                                        borderColor = TacticalAmber,
                                        opacity = opacity,
                                        hapticEnabled = haptic,
                                        handcamGlow = glow,
                                        isEditMode = isEditMode,
                                        onStateChanged = { pressed ->
                                            if (pressed) onPressKey(bindings.reload) else onReleaseKey(bindings.reload)
                                        }
                                    )

                                    ControllerButton(
                                        label = "JUMP",
                                        subLabel = "Space",
                                        testTag = "btn_ff_jump",
                                        width = halfColW.dp,
                                        height = (56f * btnScale).dp,
                                        baseColor = JumpButtonBg,
                                        borderColor = JumpButtonBorder,
                                        opacity = opacity,
                                        hapticEnabled = haptic,
                                        handcamGlow = glow,
                                        isEditMode = isEditMode,
                                        onStateChanged = { pressed ->
                                            if (pressed) onPressKey(bindings.jump) else onReleaseKey(bindings.jump)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // Dedicated 2nd LEFT FIRE Button (Essential for 3/4-Finger Claw & Sit-Up Gloo Wall!)
        // Positioned cleanly near Top-Left of center/left boundary and draggable in Edit Mode.
        // ==========================================
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
                    testTag = "btn_ff_left_fire",
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
