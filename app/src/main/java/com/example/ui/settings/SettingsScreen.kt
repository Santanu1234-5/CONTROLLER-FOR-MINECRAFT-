package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ControllerPreferences
import com.example.model.GameProfile
import com.example.model.KeyBindings
import com.example.model.PresetType
import com.example.model.UsbHidKeyCodes
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NetheriteCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * Settings screen for adjusting look sensitivity, control opacity,
 * selecting Minecraft presets (including Eaglercraft), and mapping USB HID keys.
 */
@Composable
fun SettingsScreen(
    prefs: ControllerPreferences,
    onBack: () -> Unit
) {
    var activeGame by remember { mutableStateOf(prefs.gameProfile) }
    var sensitivity by remember(activeGame) { mutableFloatStateOf(prefs.getActiveSensitivity(activeGame)) }
    var opacity by remember { mutableFloatStateOf(prefs.opacity) }
    var invertY by remember(activeGame) { mutableStateOf(prefs.getActiveInvertY(activeGame)) }
    var toggleSneak by remember { mutableStateOf(prefs.toggleSneak) }
    var haptic by remember { mutableStateOf(prefs.hapticFeedback) }
    var trackpadTapToClick by remember { mutableStateOf(prefs.trackpadTapToClick) }
    var trackpadHoldLeftClick by remember(activeGame) { mutableStateOf(prefs.getActiveTrackpadHoldLeftClick(activeGame)) }
    var hudConfig by remember(activeGame) { mutableStateOf(prefs.getHudCustomization(activeGame)) }
    var activePreset by remember { mutableStateOf(prefs.preset) }
    var keyBindings by remember { mutableStateOf(prefs.getKeyBindings()) }
    var ffKeyBindings by remember { mutableStateOf(prefs.getFreeFireKeyBindings()) }

    var editingActionName by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .testTag("settings_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(NetheriteCard, CircleShape)
                            .testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Controller Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Active Game Switcher Card (Minecraft vs Free Fire India)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Active Game Controller",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GameProfile.values().forEach { profile ->
                                val isSelected = profile == activeGame
                                val accentColor = if (profile == GameProfile.FREE_FIRE_INDIA) BlazeOrange else EmeraldGreen
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeGame = profile
                                            prefs.gameProfile = profile
                                        }
                                        .testTag("game_select_${profile.name}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) accentColor else Color(0xFF16201B),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) accentColor else StoneBorder
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ObsidianDark else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = activeGame.description,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Preset Selector Card (Minecraft Presets)
            if (activeGame == GameProfile.MINECRAFT) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Control Preset",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset choices (2x2 clean grid)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PresetType.values().toList().chunked(2).forEach { rowPresets ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPresets.forEach { preset ->
                                        val isSelected = preset == activePreset
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    activePreset = preset
                                                    prefs.preset = preset
                                                }
                                                .testTag("preset_${preset.name}"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) EmeraldGreen else Color(0xFF16201B),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) EmeraldGreen else StoneBorder
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = preset.displayName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) ObsidianDark else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF141E18), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = DiamondCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activePreset.note,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                }
            }

            // Look Sensitivity & Opacity
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val isFreeFire = activeGame == GameProfile.FREE_FIRE_INDIA
                        val accentColor = if (isFreeFire) BlazeOrange else EmeraldGreen

                        Text(
                            text = "${activeGame.displayName} Camera & Handcam HUD",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Per-game Sensitivity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${activeGame.shortName} Look / Aim Sensitivity", fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = String.format(Locale.US, "%.2fx", sensitivity),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        Slider(
                            value = sensitivity,
                            onValueChange = {
                                sensitivity = it
                                prefs.setActiveSensitivity(activeGame, it)
                            },
                            valueRange = 0.25f..3.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = StoneBorder
                            ),
                            modifier = Modifier.testTag("slider_sensitivity")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button Size Scale Slider (Handcam HUD)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Button Size Scale", fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = String.format(Locale.US, "%.2fx", hudConfig.buttonScale),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        Slider(
                            value = hudConfig.buttonScale,
                            onValueChange = {
                                val updated = hudConfig.copy(buttonScale = it)
                                hudConfig = updated
                                prefs.saveHudCustomization(activeGame, updated)
                            },
                            valueRange = 0.75f..1.35f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = StoneBorder
                            ),
                            modifier = Modifier.testTag("slider_button_scale")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Joystick Size Scale Slider (Handcam HUD)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Joystick Size Scale", fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = String.format(Locale.US, "%.2fx", hudConfig.joystickScale),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        Slider(
                            value = hudConfig.joystickScale,
                            onValueChange = {
                                val updated = hudConfig.copy(joystickScale = it)
                                hudConfig = updated
                                prefs.saveHudCustomization(activeGame, updated)
                            },
                            valueRange = 0.80f..1.30f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = StoneBorder
                            ),
                            modifier = Modifier.testTag("slider_joystick_scale")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Opacity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Control Button Opacity", fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = "${(opacity * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DiamondCyan
                            )
                        }
                        Slider(
                            value = opacity,
                            onValueChange = {
                                opacity = it
                                prefs.opacity = it
                            },
                            valueRange = 0.3f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = DiamondCyan,
                                activeTrackColor = DiamondCyan,
                                inactiveTrackColor = StoneBorder
                            ),
                            modifier = Modifier.testTag("slider_opacity")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Invert Y Axis (per-game)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Invert Y Axis", fontSize = 13.sp, color = TextPrimary)
                                Text("Invert vertical camera look for ${activeGame.shortName}", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = invertY,
                                onCheckedChange = {
                                    invertY = it
                                    prefs.setActiveInvertY(activeGame, it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                            )
                        }

                        if (!isFreeFire) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Toggle Sneak Mode (Minecraft only)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("Toggle Sneak", fontSize = 13.sp, color = TextPrimary)
                                    Text("Tap to toggle sneak instead of holding", fontSize = 11.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = toggleSneak,
                                    onCheckedChange = {
                                        toggleSneak = it
                                        prefs.toggleSneak = it
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Haptic Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Haptic Feedback", fontSize = 13.sp, color = TextPrimary)
                                Text("Vibrate slightly on button presses and joystick directions", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = haptic,
                                onCheckedChange = {
                                    haptic = it
                                    prefs.hapticFeedback = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Handcam Press Glow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Handcam Press Glow", fontSize = 13.sp, color = TextPrimary)
                                Text("Highlight pressed buttons vividly for handcam visibility", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = hudConfig.handcamGlow,
                                onCheckedChange = {
                                    val updated = hudConfig.copy(handcamGlow = it)
                                    hudConfig = updated
                                    prefs.saveHudCustomization(activeGame, updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("switch_handcam_glow")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clean Handcam Top Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Clean Handcam Top Bar", fontSize = 13.sp, color = TextPrimary)
                                Text("Minimize top toolbar clutter when recording handcam gameplay", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = hudConfig.cleanHandcamBar,
                                onCheckedChange = {
                                    val updated = hudConfig.copy(cleanHandcamBar = it)
                                    hudConfig = updated
                                    prefs.saveHudCustomization(activeGame, updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("switch_clean_handcam")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2nd Left Fire Button (Claw)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("2nd Left Fire Button (Claw)", fontSize = 13.sp, color = TextPrimary)
                                Text("Show top-left FIRE 2 button for 3/4-finger claw grip", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = hudConfig.showLeftFireButton,
                                onCheckedChange = {
                                    val updated = hudConfig.copy(showLeftFireButton = it)
                                    hudConfig = updated
                                    prefs.saveHudCustomization(activeGame, updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("switch_left_fire")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trackpad Tap to Click
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Trackpad Tap to Click", fontSize = 13.sp, color = TextPrimary)
                                Text("Tap once for single click, tap twice quickly for double click", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = trackpadTapToClick,
                                onCheckedChange = {
                                    trackpadTapToClick = it
                                    prefs.trackpadTapToClick = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("switch_trackpad_tap")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trackpad Hold Left Click (per-game)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Trackpad Hold Left Click", fontSize = 13.sp, color = TextPrimary)
                                Text("Hold left mouse button while dragging on ${activeGame.shortName} trackpad", fontSize = 11.sp, color = TextMuted)
                            }
                            Switch(
                                checked = trackpadHoldLeftClick,
                                onCheckedChange = {
                                    trackpadHoldLeftClick = it
                                    prefs.setActiveTrackpadHoldLeftClick(activeGame, it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("switch_trackpad_hold")
                            )
                        }
                    }
                }
            }

            // Keyboard Mappings Editor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val isFreeFire = activeGame == GameProfile.FREE_FIRE_INDIA
                        val keyColor = if (isFreeFire) BlazeOrange else EmeraldGreen

                        Text(
                            text = if (isFreeFire) "Free Fire India Key Mappings (USB HID)" else "Minecraft Key Mappings (USB HID)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap any action to customize its USB HID key code.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val actions = if (isFreeFire) {
                            listOf(
                                Pair("Crouch", ffKeyBindings.crouch),
                                Pair("Prone", ffKeyBindings.prone),
                                Pair("Reload", ffKeyBindings.reload),
                                Pair("Loot / Use / Enter Vehicle", ffKeyBindings.loot),
                                Pair("Gloo Wall", ffKeyBindings.glooWall),
                                Pair("Grenade", ffKeyBindings.grenade),
                                Pair("Medkit", ffKeyBindings.medkit),
                                Pair("Active Skill", ffKeyBindings.skill),
                                Pair("Quick Weapon Switch", ffKeyBindings.quickSwitch),
                                Pair("Backpack", ffKeyBindings.backpack),
                                Pair("Map", ffKeyBindings.map),
                                Pair("Ping / Mark", ffKeyBindings.ping),
                                Pair("Emote", ffKeyBindings.emote),
                                Pair("Aim Lock Toggle", ffKeyBindings.cursorToggle),
                                Pair("Jump", ffKeyBindings.jump),
                                Pair("Pause / Menu", ffKeyBindings.menu)
                            )
                        } else {
                            listOf(
                                Pair("Forward", keyBindings.forward),
                                Pair("Backward", keyBindings.backward),
                                Pair("Left", keyBindings.left),
                                Pair("Right", keyBindings.right),
                                Pair("Jump", keyBindings.jump),
                                Pair("Inventory", keyBindings.inventory),
                                Pair("Pause / Menu", keyBindings.menu),
                                Pair("Drop Item", keyBindings.drop),
                                Pair("Perspective Toggle", keyBindings.perspective),
                                Pair("Player List", keyBindings.playerList),
                                Pair("Debug Screen", keyBindings.debug)
                            )
                        }

                        actions.forEach { (actionName, currentKey) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingActionName = actionName }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(actionName, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E2822),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                                ) {
                                    Text(
                                        text = UsbHidKeyCodes.getKeyLabel(currentKey),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = keyColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reset to Defaults Button
            item {
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261D1D)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedstoneRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reset_defaults_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = RedstoneRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Settings to Defaults", color = RedstoneRed, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Key selection dialog
        if (editingActionName != null) {
            val action = editingActionName!!
            val commonKeys = listOf(
                Pair("W", UsbHidKeyCodes.KEY_W),
                Pair("A", UsbHidKeyCodes.KEY_A),
                Pair("S", UsbHidKeyCodes.KEY_S),
                Pair("D", UsbHidKeyCodes.KEY_D),
                Pair("Space", UsbHidKeyCodes.KEY_SPACE),
                Pair("E", UsbHidKeyCodes.KEY_E),
                Pair("Q", UsbHidKeyCodes.KEY_Q),
                Pair("Esc", UsbHidKeyCodes.KEY_ESCAPE),
                Pair("Tab", UsbHidKeyCodes.KEY_TAB),
                Pair("F5", UsbHidKeyCodes.KEY_F5),
                Pair("F3", UsbHidKeyCodes.KEY_F3),
                Pair("F1", UsbHidKeyCodes.KEY_F1),
                Pair("F11", UsbHidKeyCodes.KEY_F11),
                Pair("C", UsbHidKeyCodes.KEY_C),
                Pair("F", UsbHidKeyCodes.KEY_F),
                Pair("G", UsbHidKeyCodes.KEY_G),
                Pair("H", UsbHidKeyCodes.KEY_H),
                Pair("M", UsbHidKeyCodes.KEY_M),
                Pair("R", UsbHidKeyCodes.KEY_R),
                Pair("T", UsbHidKeyCodes.KEY_T),
                Pair("V", UsbHidKeyCodes.KEY_V),
                Pair("X", UsbHidKeyCodes.KEY_X),
                Pair("B", UsbHidKeyCodes.KEY_B),
                Pair("Z", UsbHidKeyCodes.KEY_Z),
                Pair("1", UsbHidKeyCodes.KEY_1),
                Pair("2", UsbHidKeyCodes.KEY_2),
                Pair("3", UsbHidKeyCodes.KEY_3),
                Pair("4", UsbHidKeyCodes.KEY_4),
                Pair("5", UsbHidKeyCodes.KEY_5),
                Pair("6", UsbHidKeyCodes.KEY_6),
                Pair("7", UsbHidKeyCodes.KEY_7),
                Pair("8", UsbHidKeyCodes.KEY_8),
                Pair("9", UsbHidKeyCodes.KEY_9),
                Pair("Enter", UsbHidKeyCodes.KEY_ENTER)
            )

            AlertDialog(
                onDismissRequest = { editingActionName = null },
                containerColor = NetheriteCard,
                title = { Text("Select Key for $action", color = TextPrimary) },
                text = {
                    Column {
                        Text("Pick a USB HID key usage code:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(commonKeys) { (label, code) ->
                                Button(
                                    onClick = {
                                        if (activeGame == GameProfile.FREE_FIRE_INDIA) {
                                            val updatedFf = when (action) {
                                                "Crouch" -> ffKeyBindings.copy(crouch = code)
                                                "Prone" -> ffKeyBindings.copy(prone = code)
                                                "Reload" -> ffKeyBindings.copy(reload = code)
                                                "Loot / Use / Enter Vehicle" -> ffKeyBindings.copy(loot = code)
                                                "Gloo Wall" -> ffKeyBindings.copy(glooWall = code)
                                                "Grenade" -> ffKeyBindings.copy(grenade = code)
                                                "Medkit" -> ffKeyBindings.copy(medkit = code)
                                                "Active Skill" -> ffKeyBindings.copy(skill = code)
                                                "Quick Weapon Switch" -> ffKeyBindings.copy(quickSwitch = code)
                                                "Backpack" -> ffKeyBindings.copy(backpack = code)
                                                "Map" -> ffKeyBindings.copy(map = code)
                                                "Ping / Mark" -> ffKeyBindings.copy(ping = code)
                                                "Emote" -> ffKeyBindings.copy(emote = code)
                                                "Aim Lock Toggle" -> ffKeyBindings.copy(cursorToggle = code)
                                                "Jump" -> ffKeyBindings.copy(jump = code)
                                                "Pause / Menu" -> ffKeyBindings.copy(menu = code)
                                                else -> ffKeyBindings
                                            }
                                            ffKeyBindings = updatedFf
                                            prefs.saveFreeFireKeyBindings(updatedFf)
                                        } else {
                                            val updated = when (action) {
                                                "Forward" -> keyBindings.copy(forward = code)
                                                "Backward" -> keyBindings.copy(backward = code)
                                                "Left" -> keyBindings.copy(left = code)
                                                "Right" -> keyBindings.copy(right = code)
                                                "Jump" -> keyBindings.copy(jump = code)
                                                "Inventory" -> keyBindings.copy(inventory = code)
                                                "Pause / Menu" -> keyBindings.copy(menu = code)
                                                "Drop Item" -> keyBindings.copy(drop = code)
                                                "Perspective Toggle" -> keyBindings.copy(perspective = code)
                                                "Player List" -> keyBindings.copy(playerList = code)
                                                "Debug Screen" -> keyBindings.copy(debug = code)
                                                else -> keyBindings
                                            }
                                            keyBindings = updated
                                            prefs.saveKeyBindings(updated)
                                        }
                                        editingActionName = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2822)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label, color = if (activeGame == GameProfile.FREE_FIRE_INDIA) BlazeOrange else EmeraldGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { editingActionName = null }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = NetheriteCard,
                title = { Text("Reset to Default Settings?", color = TextPrimary) },
                text = {
                    Text(
                        "This will restore look sensitivity to defaults, opacity to 85%, Handcam HUD layouts, and all key mappings for both Minecraft and Free Fire India.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            prefs.resetToDefaults()
                            activeGame = prefs.gameProfile
                            sensitivity = prefs.getActiveSensitivity(activeGame)
                            opacity = prefs.opacity
                            invertY = prefs.getActiveInvertY(activeGame)
                            toggleSneak = prefs.toggleSneak
                            haptic = prefs.hapticFeedback
                            trackpadTapToClick = prefs.trackpadTapToClick
                            trackpadHoldLeftClick = prefs.getActiveTrackpadHoldLeftClick(activeGame)
                            hudConfig = prefs.getHudCustomization(activeGame)
                            activePreset = prefs.preset
                            keyBindings = prefs.getKeyBindings()
                            ffKeyBindings = prefs.getFreeFireKeyBindings()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedstoneRed)
                    ) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
