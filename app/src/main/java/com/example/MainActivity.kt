package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.ControllerPreferences
import com.example.hid.HidDeviceManager
import com.example.model.ConnectionMode
import com.example.ui.controller.ControllerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.setup.SetupScreen
import com.example.ui.theme.MinecraftControllerTheme
import com.example.ui.troubleshooting.TroubleshootingScreen
import com.example.usb.UsbTetherManager

enum class AppScreen {
    CONTROLLER,
    SETUP,
    SETTINGS,
    TROUBLESHOOTING
}

class MainActivity : ComponentActivity() {

    private lateinit var hidManager: HidDeviceManager
    private lateinit var usbManager: UsbTetherManager
    private lateinit var prefs: ControllerPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on while playing Minecraft so controller doesn't timeout
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prefs = ControllerPreferences(this)
        hidManager = HidDeviceManager(this)
        usbManager = UsbTetherManager(this)

        usbManager.setAutoDetectEnabled(prefs.autoDetectPcIp)
        usbManager.setTargetPcIp(prefs.usbTargetIp)
        usbManager.setPairingCode(prefs.usbPairingCode)

        setContent {
            MinecraftControllerTheme {
                MainContent(
                    hidManager = hidManager,
                    usbManager = usbManager,
                    prefs = prefs
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (prefs.connectionMode == ConnectionMode.BLUETOOTH) {
            if (hasBluetoothPermissions()) {
                hidManager.initialize()
                hidManager.refreshBondedDevices()
            }
        } else {
            usbManager.checkTetheringInterface()
        }
    }

    override fun onPause() {
        super.onPause()
        // Safely release any pressed keys or mouse buttons when app loses focus
        hidManager.releaseAllHeldInputs()
        usbManager.releaseAllHeldInputs()
    }

    override fun onStop() {
        super.onStop()
        hidManager.releaseAllHeldInputs()
        usbManager.releaseAllHeldInputs()
    }

    override fun onDestroy() {
        super.onDestroy()
        hidManager.close()
        usbManager.close()
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun MainContent(
    hidManager: HidDeviceManager,
    usbManager: UsbTetherManager,
    prefs: ControllerPreferences
) {
    // Show Setup screen on first launch to guide user through connection choice
    var currentScreen by remember {
        mutableStateOf(if (prefs.isFirstLaunch) AppScreen.SETUP else AppScreen.CONTROLLER)
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            hidManager.initialize()
            hidManager.refreshBondedDevices()
        }
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (prefs.connectionMode == ConnectionMode.BLUETOOTH) {
            if (!hidManager.hasRequiredPermissions()) {
                requestPermissions()
            } else {
                hidManager.initialize()
                hidManager.refreshBondedDevices()
            }
        } else {
            usbManager.checkTetheringInterface()
        }
    }

    // Handle Android system back gesture/button cleanly
    if (currentScreen != AppScreen.CONTROLLER) {
        BackHandler {
            currentScreen = AppScreen.CONTROLLER
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Crossfade(
            targetState = currentScreen,
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.CONTROLLER -> {
                    ControllerScreen(
                        hidManager = hidManager,
                        usbManager = usbManager,
                        prefs = prefs,
                        onNavigateSetup = { currentScreen = AppScreen.SETUP },
                        onNavigateSettings = { currentScreen = AppScreen.SETTINGS },
                        onNavigateHelp = { currentScreen = AppScreen.TROUBLESHOOTING }
                    )
                }
                AppScreen.SETUP -> {
                    SetupScreen(
                        hidManager = hidManager,
                        usbManager = usbManager,
                        prefs = prefs,
                        onBackToController = {
                            prefs.isFirstLaunch = false
                            currentScreen = AppScreen.CONTROLLER
                        },
                        onRequestPermissions = { requestPermissions() }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        prefs = prefs,
                        onBack = { currentScreen = AppScreen.CONTROLLER }
                    )
                }
                AppScreen.TROUBLESHOOTING -> {
                    TroubleshootingScreen(
                        hidManager = hidManager,
                        usbManager = usbManager,
                        onBack = { currentScreen = AppScreen.CONTROLLER }
                    )
                }
            }
        }
    }
}
