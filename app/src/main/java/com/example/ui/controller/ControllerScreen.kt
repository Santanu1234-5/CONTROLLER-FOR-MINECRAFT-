package com.example.ui.controller

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ControllerPreferences
import com.example.hid.HidDeviceManager
import com.example.model.ConnectionMode
import com.example.model.GameProfile
import com.example.model.HidConnectionState
import com.example.model.HudCustomization
import com.example.model.UsbHidKeyCodes
import com.example.ui.theme.BlazeOrange
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.TextMuted
import com.example.usb.UsbConnectionState
import com.example.usb.UsbTetherManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Landscape-first, 3-zone responsive dual-game controller touch layout.
 * Supports distinct Minecraft and Free Fire India layouts, per-game sensitivity/settings,
 * live Handcam HUD customization, and both Bluetooth HID and USB Cable connection modes.
 */
@Composable
fun ControllerScreen(
    hidManager: HidDeviceManager,
    usbManager: UsbTetherManager,
    prefs: ControllerPreferences,
    onNavigateSetup: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHelp: () -> Unit
) {
    var activeMode by remember { mutableStateOf(prefs.connectionMode) }
    var activeGame by remember { mutableStateOf(prefs.gameProfile) }

    // Bluetooth States
    val btConnectionState by hidManager.connectionState.collectAsState()
    val btConnectedDevice by hidManager.connectedDevice.collectAsState()
    val bondedDevices by hidManager.bondedDevices.collectAsState()
    val btActiveKeys by hidManager.activeKeysList.collectAsState()
    val btActiveModifiers by hidManager.activeModifiers.collectAsState()
    val btActiveMouseButtons by hidManager.activeMouseButtons.collectAsState()

    // USB States
    val usbConnectionState by usbManager.connectionState.collectAsState()
    val usbActiveKeys by usbManager.activeKeysList.collectAsState()
    val usbActiveModifiers by usbManager.activeModifiers.collectAsState()
    val usbActiveMouseButtons by usbManager.activeMouseButtons.collectAsState()

    // Per-game and shared reactive settings
    var opacity by remember { mutableFloatStateOf(prefs.opacity) }
    var activeSensitivity by remember(activeGame) {
        mutableFloatStateOf(prefs.getActiveSensitivity(activeGame))
    }
    val activeInvertY = prefs.getActiveInvertY(activeGame)
    val toggleSneakMode = prefs.toggleSneak
    val haptic = prefs.hapticFeedback
    val trackpadTapToClick = prefs.trackpadTapToClick
    var trackpadHoldLeftClick by remember(activeGame) {
        mutableStateOf(prefs.getActiveTrackpadHoldLeftClick(activeGame))
    }
    var hudConfig by remember(activeGame) {
        mutableStateOf(prefs.getHudCustomization(activeGame))
    }
    var isEditHudMode by remember { mutableStateOf(false) }

    var isTrackpadHoldingLeft by remember { mutableStateOf(false) }
    var isRightFireHoldingLeft by remember { mutableStateOf(false) }
    var isLeftClawFireHoldingLeft by remember { mutableStateOf(false) }

    val keyBindings = prefs.getKeyBindings()
    val ffBindings = prefs.getFreeFireKeyBindings()
    val coroutineScope = rememberCoroutineScope()

    var isSneakToggled by remember { mutableStateOf(false) }
    var isSprintToggled by remember { mutableStateOf(false) }

    // When leaving this screen, safely release every input
    DisposableEffect(Unit) {
        onDispose {
            isTrackpadHoldingLeft = false
            isRightFireHoldingLeft = false
            isLeftClawFireHoldingLeft = false
            hidManager.releaseAllHeldInputs()
            usbManager.releaseAllHeldInputs()
        }
    }

    // Helper functions to dispatch input to active mode
    fun handlePressKey(vararg keys: Byte) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.pressKey(*keys)
        } else {
            usbManager.pressKey(*keys)
        }
    }

    fun handleReleaseKey(vararg keys: Byte) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.releaseKey(*keys)
        } else {
            usbManager.releaseKey(*keys)
        }
    }

    fun handleSetHeldKeys(keys: Set<Byte>) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.setHeldKeys(keys)
        } else {
            usbManager.setHeldKeys(keys)
        }
    }

    fun handleSetModifier(modifierBit: Byte, isPressed: Boolean) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.setModifier(modifierBit, isPressed)
        } else {
            usbManager.setModifier(modifierBit, isPressed)
        }
    }

    fun handleMouseMove(dx: Float, dy: Float) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.sendMouseMove(dx, dy, activeSensitivity, activeInvertY)
        } else {
            usbManager.sendMouseMove(dx, dy, activeSensitivity, activeInvertY)
        }
    }

    fun handleMouseWheel(scroll: Int) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.sendMouseWheel(scroll)
        } else {
            usbManager.sendMouseWheel(scroll)
        }
    }

    fun handleSetMouseButton(buttonBit: Byte, isPressed: Boolean) {
        if (activeMode == ConnectionMode.BLUETOOTH) {
            hidManager.setMouseButton(buttonBit, isPressed)
        } else {
            usbManager.setMouseButton(buttonBit, isPressed)
        }
    }

    fun updateLeftMouseButton() {
        val shouldHold = isRightFireHoldingLeft || isLeftClawFireHoldingLeft || isTrackpadHoldingLeft
        handleSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_LEFT, shouldHold)
    }

    fun handleMouseClick(isDoubleClick: Boolean = false) {
        coroutineScope.launch {
            handleSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_LEFT, true)
            delay(35)
            handleSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_LEFT, false)
            if (isDoubleClick) {
                delay(45)
                handleSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_LEFT, true)
                delay(35)
                handleSetMouseButton(UsbHidKeyCodes.MOUSE_BUTTON_LEFT, false)
            }
        }
    }

    fun switchGameProfile() {
        isTrackpadHoldingLeft = false
        isRightFireHoldingLeft = false
        isLeftClawFireHoldingLeft = false
        isSneakToggled = false
        isSprintToggled = false
        hidManager.releaseAllHeldInputs()
        usbManager.releaseAllHeldInputs()

        val nextGame = if (activeGame == GameProfile.FREE_FIRE_INDIA) {
            GameProfile.MINECRAFT
        } else {
            GameProfile.FREE_FIRE_INDIA
        }
        activeGame = nextGame
        prefs.gameProfile = nextGame
    }

    // Active inputs for visualization
    val activeKeys = if (activeMode == ConnectionMode.BLUETOOTH) btActiveKeys else usbActiveKeys
    val activeModifiers = if (activeMode == ConnectionMode.BLUETOOTH) btActiveModifiers else usbActiveModifiers
    val activeMouseButtons = if (activeMode == ConnectionMode.BLUETOOTH) btActiveMouseButtons else usbActiveMouseButtons

    val isFreeFire = activeGame == GameProfile.FREE_FIRE_INDIA
    val gameAccent = if (isFreeFire) BlazeOrange else EmeraldGreen

    val (statusColor, statusText) = if (activeMode == ConnectionMode.BLUETOOTH) {
        when (btConnectionState) {
            HidConnectionState.CONNECTED -> {
                val hostName = try { btConnectedDevice?.name ?: "PC" } catch (e: Exception) { "PC" }
                Pair(EmeraldGreen, "Connected: $hostName")
            }
            HidConnectionState.CONNECTING -> Pair(GoldYellow, "Connecting...")
            HidConnectionState.READY_DISCONNECTED -> {
                if (bondedDevices.isNotEmpty()) Pair(DiamondCyan, "Tap to Connect (${bondedDevices.first().name})")
                else Pair(DiamondCyan, "Ready (Tap to pair)")
            }
            HidConnectionState.BLUETOOTH_OFF -> Pair(RedstoneRed, "Bluetooth Off")
            HidConnectionState.PERMISSION_NEEDED -> Pair(GoldYellow, "Permission Required")
            HidConnectionState.HID_UNSUPPORTED -> Pair(RedstoneRed, "HID Unsupported (Use USB)")
            else -> Pair(Color.Gray, btConnectionState.name.replace("_", " "))
        }
    } else {
        when (usbConnectionState) {
            UsbConnectionState.CONNECTED -> Pair(EmeraldGreen, "USB Connected: PC")
            UsbConnectionState.CONNECTING -> Pair(GoldYellow, "Connecting USB...")
            UsbConnectionState.AUTHENTICATING -> Pair(GoldYellow, "Authenticating PIN...")
            UsbConnectionState.TETHERING_NOT_DETECTED -> Pair(RedstoneRed, "USB Tethering Off (Tap)")
            UsbConnectionState.RECEIVER_DISCONNECTED -> Pair(DiamondCyan, "USB Ready (Tap to Connect)")
            UsbConnectionState.ERROR -> Pair(RedstoneRed, "USB Error (Tap)")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .testTag("controller_screen_root")
    ) {
        // ==========================================
        // Top Bar: Full or Clean Handcam Minimal Mode
        // ==========================================
        if (hudConfig.cleanHandcamBar && !isEditHudMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact status dot + Game Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF18201C).copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, gameAccent.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { switchGameProfile() }
                            .testTag("game_profile_switcher")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(7.dp).background(statusColor, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeGame.badgeText,
                                color = gameAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Active Input Visualizer (looks great on Handcam!)
                    if (activeKeys.isNotEmpty() || activeModifiers != 0.toByte() || activeMouseButtons != 0.toByte()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x44000000)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if ((activeModifiers.toInt() and UsbHidKeyCodes.MOD_LEFT_SHIFT.toInt()) != 0) {
                                    Text("Shift", color = DiamondCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                activeKeys.forEach { code ->
                                    Text(UsbHidKeyCodes.getKeyLabel(code), color = gameAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if ((activeMouseButtons.toInt() and UsbHidKeyCodes.MOUSE_BUTTON_LEFT.toInt()) != 0) {
                                    Text("FIRE", color = RedstoneRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if ((activeMouseButtons.toInt() and UsbHidKeyCodes.MOUSE_BUTTON_RIGHT.toInt()) != 0) {
                                    Text("SCOPE/USE", color = DiamondCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isEditHudMode = true },
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color(0xFF1E2822).copy(alpha = 0.75f), CircleShape)
                            .testTag("nav_hud_edit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Handcam HUD",
                            tint = gameAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color(0xFF1E2822).copy(alpha = 0.75f), CircleShape)
                            .testTag("nav_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Switcher Pill (One-tap toggle between BT and USB)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E2822),
                    border = BorderStroke(1.dp, StoneBorder),
                    modifier = Modifier.clickable {
                        hidManager.releaseAllHeldInputs()
                        usbManager.releaseAllHeldInputs()

                        val nextMode = if (activeMode == ConnectionMode.BLUETOOTH) ConnectionMode.USB else ConnectionMode.BLUETOOTH
                        activeMode = nextMode
                        prefs.connectionMode = nextMode
                        if (nextMode == ConnectionMode.USB) {
                            usbManager.checkTetheringInterface()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (activeMode == ConnectionMode.BLUETOOTH) Icons.Default.Bluetooth else Icons.Default.Usb,
                            contentDescription = null,
                            tint = if (activeMode == ConnectionMode.BLUETOOTH) EmeraldGreen else DiamondCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (activeMode == ConnectionMode.BLUETOOTH) "BT HID" else "USB",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Connection Mode",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Connection Status Chip (Clickable: opens setup)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF18201C).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFF2E3D34)),
                    modifier = Modifier.clickable {
                        if (activeMode == ConnectionMode.BLUETOOTH) {
                            if (btConnectionState != HidConnectionState.CONNECTED) {
                                val reconnected = hidManager.reconnectToAnyBondedHost()
                                if (!reconnected) onNavigateSetup()
                            } else {
                                onNavigateSetup()
                            }
                        } else {
                            if (usbConnectionState != UsbConnectionState.CONNECTED) {
                                usbManager.connect()
                            }
                            onNavigateSetup()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Center: One-tap Game Switcher (Minecraft <-> Free Fire India) & Active Input visualizer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = gameAccent.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, gameAccent),
                        modifier = Modifier
                            .clickable { switchGameProfile() }
                            .testTag("game_profile_switcher")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Switch Game Controller",
                                tint = gameAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = activeGame.badgeText,
                                color = gameAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Active Keys Visualizer
                    if (activeKeys.isNotEmpty() || activeModifiers != 0.toByte() || activeMouseButtons != 0.toByte()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if ((activeModifiers.toInt() and UsbHidKeyCodes.MOD_LEFT_SHIFT.toInt()) != 0) {
                                    Text("Shift", color = DiamondCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                activeKeys.forEach { code ->
                                    Text(
                                        text = UsbHidKeyCodes.getKeyLabel(code),
                                        color = gameAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if ((activeMouseButtons.toInt() and UsbHidKeyCodes.MOUSE_BUTTON_LEFT.toInt()) != 0) {
                                    Text("L-Click", color = RedstoneRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if ((activeMouseButtons.toInt() and UsbHidKeyCodes.MOUSE_BUTTON_RIGHT.toInt()) != 0) {
                                    Text("R-Click", color = DiamondCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Quick Navigation & HUD Customize Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Live Handcam HUD Customize Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isEditHudMode) gameAccent else Color(0xFF1E2822).copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, if (isEditHudMode) gameAccent else StoneBorder),
                        modifier = Modifier
                            .clickable {
                                if (!isEditHudMode) {
                                    isTrackpadHoldingLeft = false
                                    isRightFireHoldingLeft = false
                                    isLeftClawFireHoldingLeft = false
                                    hidManager.releaseAllHeldInputs()
                                    usbManager.releaseAllHeldInputs()
                                }
                                isEditHudMode = !isEditHudMode
                            }
                            .testTag("nav_hud_edit_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Customize Handcam HUD",
                                tint = if (isEditHudMode) ObsidianDark else gameAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HUD",
                                color = if (isEditHudMode) ObsidianDark else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateSetup,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF1E2822).copy(alpha = 0.9f), CircleShape)
                            .testTag("nav_setup_btn")
                    ) {
                        Icon(
                            imageVector = if (activeMode == ConnectionMode.BLUETOOTH) Icons.Default.Bluetooth else Icons.Default.Usb,
                            contentDescription = "Connection Setup",
                            tint = if (activeMode == ConnectionMode.BLUETOOTH) EmeraldGreen else DiamondCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF1E2822).copy(alpha = 0.9f), CircleShape)
                            .testTag("nav_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateHelp,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF1E2822).copy(alpha = 0.9f), CircleShape)
                            .testTag("nav_help_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help & Troubleshooting",
                            tint = GoldYellow,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3-Zone Controller Partition + Floating Handcam HUD Customizer Overlay
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (activeGame == GameProfile.FREE_FIRE_INDIA) {
                FreeFireControllerLayout(
                    modifier = Modifier.fillMaxSize(),
                    bindings = ffBindings,
                    hudConfig = hudConfig,
                    opacity = opacity,
                    haptic = haptic,
                    trackpadTapToClick = trackpadTapToClick,
                    trackpadHoldLeftClick = trackpadHoldLeftClick,
                    isEditMode = isEditHudMode,
                    isSprintToggled = isSprintToggled,
                    onSprintToggled = { state ->
                        isSprintToggled = state
                        handleSetModifier(UsbHidKeyCodes.MOD_LEFT_SHIFT, state)
                    },
                    onPressKey = { key -> handlePressKey(key) },
                    onReleaseKey = { key -> handleReleaseKey(key) },
                    onSetHeldKeys = { keys -> handleSetHeldKeys(keys) },
                    onMouseMove = { dx, dy -> handleMouseMove(dx, dy) },
                    onTrackpadHoldLeftChanged = { pressed ->
                        isTrackpadHoldingLeft = pressed
                        updateLeftMouseButton()
                    },
                    onToggleTrackpadHoldLeft = { enabled ->
                        trackpadHoldLeftClick = enabled
                        prefs.setActiveTrackpadHoldLeftClick(activeGame, enabled)
                    },
                    onTrackpadTap = { isDoubleTap -> handleMouseClick(isDoubleTap) },
                    onLeftFireButtonChanged = { pressed ->
                        isLeftClawFireHoldingLeft = pressed
                        updateLeftMouseButton()
                    },
                    onRightFireButtonChanged = { pressed ->
                        isRightFireHoldingLeft = pressed
                        updateLeftMouseButton()
                    },
                    onSetMouseButton = { btn, pressed -> handleSetMouseButton(btn, pressed) },
                    onUpdateHudConfig = { updated ->
                        hudConfig = updated
                        prefs.saveHudCustomization(activeGame, updated)
                    }
                )
            } else {
                MinecraftControllerLayout(
                    modifier = Modifier.fillMaxSize(),
                    keyBindings = keyBindings,
                    hudConfig = hudConfig,
                    opacity = opacity,
                    haptic = haptic,
                    toggleSneakMode = toggleSneakMode,
                    trackpadTapToClick = trackpadTapToClick,
                    trackpadHoldLeftClick = trackpadHoldLeftClick,
                    isEditMode = isEditHudMode,
                    isSprintToggled = isSprintToggled,
                    isSneakToggled = isSneakToggled,
                    onSprintToggled = { state ->
                        isSprintToggled = state
                        handleSetModifier(UsbHidKeyCodes.MOD_LEFT_CTRL, state)
                    },
                    onSneakToggled = { state ->
                        isSneakToggled = state
                        handleSetModifier(keyBindings.sneakModifier, state)
                    },
                    onPressKey = { key -> handlePressKey(key) },
                    onReleaseKey = { key -> handleReleaseKey(key) },
                    onSetHeldKeys = { keys -> handleSetHeldKeys(keys) },
                    onMouseMove = { dx, dy -> handleMouseMove(dx, dy) },
                    onMouseWheel = { scroll -> handleMouseWheel(scroll) },
                    onTrackpadHoldLeftChanged = { pressed ->
                        isTrackpadHoldingLeft = pressed
                        updateLeftMouseButton()
                    },
                    onToggleTrackpadHoldLeft = { enabled ->
                        trackpadHoldLeftClick = enabled
                        prefs.setActiveTrackpadHoldLeftClick(activeGame, enabled)
                    },
                    onTrackpadTap = { isDoubleTap -> handleMouseClick(isDoubleTap) },
                    onLeftFireButtonChanged = { pressed ->
                        isLeftClawFireHoldingLeft = pressed
                        updateLeftMouseButton()
                    },
                    onRightFireButtonChanged = { pressed ->
                        isRightFireHoldingLeft = pressed
                        updateLeftMouseButton()
                    },
                    onSetMouseButton = { btn, pressed -> handleSetMouseButton(btn, pressed) },
                    onUpdateHudConfig = { updated ->
                        hudConfig = updated
                        prefs.saveHudCustomization(activeGame, updated)
                    }
                )
            }

            if (isEditHudMode) {
                HudCustomizerPanel(
                    modifier = Modifier.align(Alignment.Center),
                    activeGame = activeGame,
                    hudConfig = hudConfig,
                    opacity = opacity,
                    sensitivity = activeSensitivity,
                    onUpdateHudConfig = { updated ->
                        hudConfig = updated
                        prefs.saveHudCustomization(activeGame, updated)
                    },
                    onUpdateOpacity = { newOpacity ->
                        opacity = newOpacity
                        prefs.opacity = newOpacity
                    },
                    onUpdateSensitivity = { newSens ->
                        activeSensitivity = newSens
                        prefs.setActiveSensitivity(activeGame, newSens)
                    },
                    onResetLayout = {
                        val resetConfig = prefs.resetHudCustomization(activeGame)
                        hudConfig = resetConfig
                    },
                    onDone = { isEditHudMode = false }
                )
            }
        }
    }
}
