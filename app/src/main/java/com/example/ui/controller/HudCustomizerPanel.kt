package com.example.ui.controller

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameProfile
import com.example.model.HandcamClawPreset
import com.example.model.HudCustomization
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NetheriteCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * Interactive floating HUD & Handcam Customizer panel.
 * Lets users tune button scale, joystick size, sensitivity, claw grip preset,
 * Handcam glow & clean top-bar toggles, and drag control clusters live on screen.
 */
@Composable
fun HudCustomizerPanel(
    modifier: Modifier = Modifier,
    activeGame: GameProfile,
    hudConfig: HudCustomization,
    opacity: Float,
    sensitivity: Float,
    onUpdateHudConfig: (HudCustomization) -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onUpdateSensitivity: (Float) -> Unit,
    onResetLayout: () -> Unit,
    onDone: () -> Unit
) {
    val accentColor = if (activeGame == GameProfile.FREE_FIRE_INDIA) BlazeOrange else EmeraldGreen

    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .padding(8.dp)
            .testTag("hud_customizer_panel"),
        shape = RoundedCornerShape(16.dp),
        color = NetheriteCard.copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.85f)),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activeGame.shortName} Handcam HUD",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2B1D1D),
                        border = BorderStroke(1.dp, RedstoneRed.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { onResetLayout() }
                            .testTag("hud_reset_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset HUD",
                                tint = RedstoneRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Reset", color = RedstoneRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("hud_done_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ObsidianDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Done", color = ObsidianDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Drag hint pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141C18), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = DiamondCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Drag Left / Right zones or 2nd Fire button directly on screen!",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            // Claw Preset Pills
            Text("Grip / Handcam Preset", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HandcamClawPreset.values().forEach { preset ->
                    val selected = hudConfig.clawPreset == preset
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val updated = when (preset) {
                                    HandcamClawPreset.TWO_FINGER -> hudConfig.copy(
                                        clawPreset = preset,
                                        showLeftFireButton = false,
                                        leftFireOffsetX = 0f,
                                        leftFireOffsetY = 0f
                                    )
                                    HandcamClawPreset.THREE_FINGER -> hudConfig.copy(
                                        clawPreset = preset,
                                        showLeftFireButton = true,
                                        leftFireOffsetX = 0f,
                                        leftFireOffsetY = 0f
                                    )
                                    HandcamClawPreset.FOUR_FINGER -> hudConfig.copy(
                                        clawPreset = preset,
                                        showLeftFireButton = true,
                                        showExtraTacticalButtons = true,
                                        leftFireOffsetX = 0f,
                                        leftFireOffsetY = 0f
                                    )
                                }
                                onUpdateHudConfig(updated)
                            }
                            .testTag("claw_preset_${preset.name}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) accentColor else Color(0xFF17201C),
                        border = BorderStroke(1.dp, if (selected) accentColor else StoneBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.displayName,
                                color = if (selected) ObsidianDark else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Compact Sliders
            CompactHudSlider(
                label = "Button Size",
                valueText = String.format(Locale.US, "%.2fx", hudConfig.buttonScale),
                value = hudConfig.buttonScale,
                valueRange = 0.75f..1.35f,
                accentColor = accentColor,
                onValueChange = { onUpdateHudConfig(hudConfig.copy(buttonScale = it)) }
            )

            CompactHudSlider(
                label = "Joystick Size",
                valueText = String.format(Locale.US, "%.2fx", hudConfig.joystickScale),
                value = hudConfig.joystickScale,
                valueRange = 0.80f..1.30f,
                accentColor = accentColor,
                onValueChange = { onUpdateHudConfig(hudConfig.copy(joystickScale = it)) }
            )

            CompactHudSlider(
                label = "${activeGame.shortName} Aim Sensitivity",
                valueText = String.format(Locale.US, "%.2fx", sensitivity),
                value = sensitivity,
                valueRange = 0.25f..3.0f,
                accentColor = accentColor,
                onValueChange = onUpdateSensitivity
            )

            CompactHudSlider(
                label = "HUD Opacity",
                valueText = "${(opacity * 100).toInt()}%",
                value = opacity,
                valueRange = 0.30f..1.0f,
                accentColor = DiamondCyan,
                onValueChange = onUpdateOpacity
            )

            // Handcam Toggles (2x2 Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactToggleChip(
                    modifier = Modifier.weight(1f),
                    label = "Press Glow",
                    checked = hudConfig.handcamGlow,
                    accentColor = accentColor,
                    onCheckedChange = { onUpdateHudConfig(hudConfig.copy(handcamGlow = it)) }
                )
                CompactToggleChip(
                    modifier = Modifier.weight(1f),
                    label = "Clean Top Bar",
                    checked = hudConfig.cleanHandcamBar,
                    accentColor = accentColor,
                    onCheckedChange = { onUpdateHudConfig(hudConfig.copy(cleanHandcamBar = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactToggleChip(
                    modifier = Modifier.weight(1f),
                    label = "2nd Left Fire",
                    checked = hudConfig.showLeftFireButton,
                    accentColor = RedstoneRed,
                    onCheckedChange = { onUpdateHudConfig(hudConfig.copy(showLeftFireButton = it)) }
                )
                CompactToggleChip(
                    modifier = Modifier.weight(1f),
                    label = "Extra Pro Keys",
                    checked = hudConfig.showExtraTacticalButtons,
                    accentColor = DiamondCyan,
                    onCheckedChange = { onUpdateHudConfig(hudConfig.copy(showExtraTacticalButtons = it)) }
                )
            }
        }
    }
}

@Composable
private fun CompactHudSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(valueText, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = StoneBorder
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}

@Composable
private fun CompactToggleChip(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17201C),
        border = BorderStroke(1.dp, if (checked) accentColor.copy(alpha = 0.7f) else StoneBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (checked) TextPrimary else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accentColor,
                    checkedTrackColor = accentColor.copy(alpha = 0.45f)
                ),
                modifier = Modifier.height(24.dp)
            )
        }
    }
}
