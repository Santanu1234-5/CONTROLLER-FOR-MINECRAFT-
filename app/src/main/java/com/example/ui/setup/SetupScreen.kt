package com.example.ui.setup

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ControllerPreferences
import com.example.hid.HidDeviceManager
import com.example.model.ConnectionMode
import com.example.model.HidConnectionState
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NetheriteCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.StoneBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.usb.UsbConnectionState
import com.example.usb.UsbTetherManager

/**
 * Setup screen allowing users to choose between:
 * 1. Bluetooth HID mode (Direct, no PC receiver)
 * 2. USB Cable mode (Offline USB tethering + Windows PC receiver)
 */
@SuppressLint("MissingPermission")
@Composable
fun SetupScreen(
    hidManager: HidDeviceManager,
    usbManager: UsbTetherManager,
    prefs: ControllerPreferences,
    onBackToController: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(prefs.connectionMode) }

    // Bluetooth States
    val btConnectionState by hidManager.connectionState.collectAsState()
    val btConnectedDevice by hidManager.connectedDevice.collectAsState()
    val bondedDevices by hidManager.bondedDevices.collectAsState()

    // USB States
    val usbConnectionState by usbManager.connectionState.collectAsState()
    val autoDetectEnabled by usbManager.autoDetectEnabled.collectAsState()
    val isTetheringActive by usbManager.isTetheringActive.collectAsState()
    val detectedPcIp by usbManager.detectedPcIp.collectAsState()
    val localPhoneIp by usbManager.localPhoneIp.collectAsState()
    val pairingCode by usbManager.pairingCode.collectAsState()
    val usbLastError by usbManager.lastError.collectAsState()

    var manualIp by remember { mutableStateOf(prefs.usbTargetIp.ifBlank { "192.168.42.129" }) }
    var manualPairingCode by remember { mutableStateOf(prefs.usbPairingCode.ifBlank { "4291" }) }

    LaunchedEffect(detectedPcIp, autoDetectEnabled) {
        if (autoDetectEnabled && detectedPcIp.isNotBlank()) {
            manualIp = detectedPcIp
        }
    }

    // Discoverability launcher
    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hidManager.refreshBondedDevices()
    }

    val phoneBluetoothName = remember(hidManager.bluetoothAdapter) {
        try {
            hidManager.bluetoothAdapter?.name ?: "Android Device"
        } catch (e: Exception) {
            "Android Device"
        }
    }

    // Function to launch phone's USB Tethering settings directly
    fun openTetheringSettings() {
        val intents = listOf(
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.TetherSettings")),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                break
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .testTag("setup_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToController,
                        modifier = Modifier
                            .size(38.dp)
                            .background(NetheriteCard, CircleShape)
                            .testTag("setup_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Connection Setup",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Choose Bluetooth HID or USB Cable mode",
                            fontSize = 12.sp,
                            color = DiamondCyan
                        )
                    }
                }
            }

            // Connection Mode Tabs (Bluetooth vs USB Cable)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NetheriteCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Bluetooth Tab
                        val isBtSelected = selectedMode == ConnectionMode.BLUETOOTH
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (selectedMode != ConnectionMode.BLUETOOTH) {
                                        usbManager.releaseAllHeldInputs()
                                        selectedMode = ConnectionMode.BLUETOOTH
                                        prefs.connectionMode = ConnectionMode.BLUETOOTH
                                    }
                                }
                                .testTag("tab_bluetooth"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBtSelected) EmeraldGreen else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (isBtSelected) ObsidianDark else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1. Bluetooth HID",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBtSelected) ObsidianDark else TextSecondary
                                )
                            }
                        }

                        // USB Cable Tab
                        val isUsbSelected = selectedMode == ConnectionMode.USB
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (selectedMode != ConnectionMode.USB) {
                                        hidManager.releaseAllHeldInputs()
                                        selectedMode = ConnectionMode.USB
                                        prefs.connectionMode = ConnectionMode.USB
                                        usbManager.checkTetheringInterface()
                                    }
                                }
                                .testTag("tab_usb"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUsbSelected) DiamondCyan else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = null,
                                    tint = if (isUsbSelected) ObsidianDark else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. USB Cable",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUsbSelected) ObsidianDark else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // CONTENT: BLUETOOTH MODE
            // ==========================================
            if (selectedMode == ConnectionMode.BLUETOOTH) {
                // Bluetooth Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (color, label) = when (btConnectionState) {
                                        HidConnectionState.CONNECTED -> Pair(EmeraldGreen, "CONNECTED")
                                        HidConnectionState.CONNECTING -> Pair(GoldYellow, "CONNECTING")
                                        HidConnectionState.READY_DISCONNECTED -> Pair(DiamondCyan, "READY TO CONNECT")
                                        HidConnectionState.HID_UNSUPPORTED -> Pair(RedstoneRed, "HID UNSUPPORTED")
                                        HidConnectionState.BLUETOOTH_OFF -> Pair(RedstoneRed, "BLUETOOTH OFF")
                                        HidConnectionState.PERMISSION_NEEDED -> Pair(GoldYellow, "PERMISSION NEEDED")
                                        else -> Pair(Color.Gray, btConnectionState.name)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        hidManager.initialize()
                                        hidManager.refreshBondedDevices()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val explanation = when (btConnectionState) {
                                HidConnectionState.CONNECTED -> {
                                    val name = try { btConnectedDevice?.name ?: "PC" } catch (e: Exception) { "PC" }
                                    "Connected to $name as a standard Bluetooth keyboard and mouse. Ready to play!"
                                }
                                HidConnectionState.CONNECTING -> "Establishing Bluetooth HID connection with host PC..."
                                HidConnectionState.READY_DISCONNECTED -> "Bluetooth HID registered. Select your paired PC below and tap Connect, or connect from PC."
                                HidConnectionState.BLUETOOTH_OFF -> "Bluetooth is turned off. Please turn on Bluetooth in settings."
                                HidConnectionState.PERMISSION_NEEDED -> "Bluetooth permissions required to connect to host PCs."
                                HidConnectionState.HID_UNSUPPORTED -> "Notice: Bluetooth HID Device profile is unsupported by this phone's manufacturer firmware. Use USB Cable mode instead!"
                                else -> "Initializing Bluetooth subsystem..."
                            }

                            Text(
                                text = explanation,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Funtouch OS / iQOO 9 SE Runtime Compatibility Notice
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF15211B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23362B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Device Compatibility Check (iQOO 9 SE / Funtouch OS)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = EmeraldGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Device: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})\n" +
                                        "Native Bluetooth HID: ${if (btConnectionState != HidConnectionState.HID_UNSUPPORTED) "Supported by firmware" else "Restricted by manufacturer"}\n" +
                                        "Note: If Funtouch OS restricts Bluetooth HID on your device, switch to 'USB Cable' mode above for a guaranteed zero-latency connection.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Step 1: Make Phone Discoverable
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "1. Make Phone Discoverable",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your phone will appear on your PC as: \"$phoneBluetoothName\"",
                                fontSize = 12.sp,
                                color = DiamondCyan
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                                    }
                                    discoverableLauncher.launch(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothSearching,
                                    contentDescription = null,
                                    tint = ObsidianDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Make Discoverable (120s)", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Step 2: Bonded PCs list
                item {
                    Text(
                        text = "2. Paired PC Hosts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (bondedDevices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No paired PC found. Open Bluetooth on your PC, search for \"$phoneBluetoothName\", and pair.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(bondedDevices) { device ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (device.isConnected) EmeraldGreen else StoneBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Computer,
                                        contentDescription = null,
                                        tint = if (device.isConnected) EmeraldGreen else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(device.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(device.address, fontSize = 10.sp, color = TextMuted)
                                    }
                                }

                                if (device.isConnected) {
                                    Button(
                                        onClick = { hidManager.disconnect() },
                                        colors = ButtonDefaults.buttonColors(containerColor = RedstoneRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Disconnect", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val btDev = hidManager.bluetoothAdapter?.getRemoteDevice(device.address)
                                            if (btDev != null) hidManager.connectToHost(btDev)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Connect", color = ObsidianDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // CONTENT: USB CABLE MODE
            // ==========================================
            if (selectedMode == ConnectionMode.USB) {
                // USB Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (color, label) = when (usbConnectionState) {
                                        UsbConnectionState.CONNECTED -> Pair(EmeraldGreen, "CONNECTED TO PC")
                                        UsbConnectionState.CONNECTING -> Pair(GoldYellow, "CONNECTING...")
                                        UsbConnectionState.AUTHENTICATING -> Pair(GoldYellow, "AUTHENTICATING")
                                        UsbConnectionState.TETHERING_NOT_DETECTED -> if (autoDetectEnabled) Pair(RedstoneRed, "USB TETHERING OFF") else Pair(DiamondCyan, "MANUAL IP READY")
                                        UsbConnectionState.RECEIVER_DISCONNECTED -> Pair(DiamondCyan, "RECEIVER DISCONNECTED")
                                        UsbConnectionState.ERROR -> Pair(RedstoneRed, "ERROR")
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }

                                IconButton(
                                    onClick = { usbManager.checkTetheringInterface() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            val usbExplanation = when (usbConnectionState) {
                                UsbConnectionState.CONNECTED -> "Active local link established! Inputs are transmitted directly to the Windows PC receiver."
                                UsbConnectionState.CONNECTING -> "Connecting to PC receiver at $manualIp:25565..."
                                UsbConnectionState.AUTHENTICATING -> "Verifying security pairing code ($manualPairingCode) with PC receiver..."
                                UsbConnectionState.TETHERING_NOT_DETECTED -> if (autoDetectEnabled) {
                                    "USB Tethering is not detected. Connect USB cable to PC and turn on USB Tethering in phone settings."
                                } else {
                                    "Manual IP mode active. Enter PC IP & code below and tap Connect."
                                }
                                UsbConnectionState.RECEIVER_DISCONNECTED -> "Not connected to PC receiver ($manualIp). Tap Connect to Windows PC Receiver below."
                                UsbConnectionState.ERROR -> usbLastError ?: "Connection error. Check PC receiver."
                            }

                            Text(
                                text = usbExplanation,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Step 1: Enable USB Tethering on Phone
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = null,
                                    tint = DiamondCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step 1: Enable USB Tethering",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Connect phone to PC via USB cable.\n2. Tap the button below to open settings and toggle USB Tethering ON.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { openTetheringSettings() },
                                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Phone Tethering Settings", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Step 2: Run Windows PC Receiver
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step 2: Start Windows PC Receiver",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "A receiver program is included in the project folder (/windows-receiver/):\n" +
                                        "• Double-click 'run_receiver.bat' (it auto-requests Administrator permission so MSI App Player / BlueStacks does not block keys).\n" +
                                        "• Uses Hybrid Virtual-Key + ScanCode Win32 SendInput.\n" +
                                        "• Works for MSI App Player, BlueStacks, Eaglercraft & PC Minecraft.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Step 3: PC IP & Pairing Code (Manual Input & Auto-detect Toggle)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = DiamondCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Step 3: PC IP & Security PIN",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (autoDetectEnabled) EmeraldDark else Color(0xFF26332B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (autoDetectEnabled) EmeraldGreen else StoneBorder)
                                ) {
                                    Text(
                                        text = if (autoDetectEnabled) "AUTO DISCOVERY" else "MANUAL IP MODE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (autoDetectEnabled) EmeraldGreen else DiamondCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Auto-detect Toggle Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF131A16), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-detect PC IP Address",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (autoDetectEnabled) "Scanning network for PC receiver beacons" else "Off: enter PC IP & PIN manually without overwriting",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                Switch(
                                    checked = autoDetectEnabled,
                                    onCheckedChange = { isChecked ->
                                        prefs.autoDetectPcIp = isChecked
                                        usbManager.setAutoDetectEnabled(isChecked)
                                        if (isChecked && detectedPcIp.isNotBlank()) {
                                            manualIp = detectedPcIp
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = EmeraldGreen,
                                        checkedTrackColor = EmeraldDark,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color(0xFF232D27)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Manual Editable Inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualIp,
                                    onValueChange = {
                                        manualIp = it
                                        prefs.usbTargetIp = it
                                        usbManager.setTargetPcIp(it)
                                    },
                                    label = { Text("PC IP Address", fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Computer, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(16.dp))
                                    },
                                    trailingIcon = {
                                        if (manualIp.isNotEmpty()) {
                                            IconButton(onClick = { manualIp = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    },
                                    placeholder = { Text("192.168.42.129", fontSize = 11.sp, color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                    modifier = Modifier.weight(1.3f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DiamondCyan,
                                        unfocusedBorderColor = StoneBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = manualPairingCode,
                                    onValueChange = {
                                        manualPairingCode = it
                                        prefs.usbPairingCode = it
                                        usbManager.setPairingCode(it)
                                    },
                                    label = { Text("PIN Code", fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(16.dp))
                                    },
                                    trailingIcon = {
                                        if (manualPairingCode.isNotEmpty()) {
                                            IconButton(onClick = { manualPairingCode = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    },
                                    placeholder = { Text("4291", fontSize = 11.sp, color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.9f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DiamondCyan,
                                        unfocusedBorderColor = StoneBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )
                            }

                            // Error banner if any
                            if (usbLastError != null && usbConnectionState != UsbConnectionState.CONNECTED) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(RedstoneRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .border(1.dp, RedstoneRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RedstoneRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = usbLastError ?: "",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (usbConnectionState == UsbConnectionState.CONNECTED) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { usbManager.sendTestKey("SPACE") },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("usb_test_key_btn")
                                    ) {
                                        Text("Test Key (Space)", color = DiamondCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { usbManager.disconnect() },
                                        colors = ButtonDefaults.buttonColors(containerColor = RedstoneRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text("Disconnect USB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        prefs.usbTargetIp = manualIp.trim()
                                        prefs.usbPairingCode = manualPairingCode.trim()
                                        usbManager.connectManual(manualIp.trim(), manualPairingCode.trim())
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = usbConnectionState != UsbConnectionState.CONNECTING && usbConnectionState != UsbConnectionState.AUTHENTICATING
                                ) {
                                    Text(
                                        text = if (usbConnectionState == UsbConnectionState.CONNECTING) "Connecting to PC..." else "Connect to Windows PC Receiver",
                                        color = ObsidianDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "MSI App Player Tip: Run 'run_receiver.bat' as Administrator, click inside MSI App Player window to focus it, and turn ON 'Full Keyboard Gameplay' in Minecraft Settings (or configure MSI Keymapping + F1 Aim Lock).",
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Return to Controller Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onBackToController,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Return to Controller", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
