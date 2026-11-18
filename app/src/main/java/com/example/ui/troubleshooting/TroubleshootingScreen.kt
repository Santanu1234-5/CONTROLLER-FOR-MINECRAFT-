package com.example.ui.troubleshooting

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hid.HidDeviceManager
import com.example.model.HidConnectionState
import com.example.model.UsbHidKeyCodes
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
import com.example.usb.UsbConnectionState
import com.example.usb.UsbTetherManager

/**
 * Troubleshooting & Diagnostic Sandbox Screen.
 * Covers Bluetooth HID, USB Tethering, Windows Receiver, and iQOO 9 SE (Funtouch OS) specific guidance.
 */
@Composable
fun TroubleshootingScreen(
    hidManager: HidDeviceManager,
    usbManager: UsbTetherManager,
    onBack: () -> Unit
) {
    val activeKeys by hidManager.activeKeysList.collectAsState()
    val activeModifiers by hidManager.activeModifiers.collectAsState()
    val activeMouseButtons by hidManager.activeMouseButtons.collectAsState()
    val isRegistered by hidManager.isRegistered.collectAsState()
    val btConnectionState by hidManager.connectionState.collectAsState()
    val connectedDevice by hidManager.connectedDevice.collectAsState()

    val usbConnectionState by usbManager.connectionState.collectAsState()
    val isTetheringActive by usbManager.isTetheringActive.collectAsState()
    val localPhoneIp by usbManager.localPhoneIp.collectAsState()
    val detectedPcIp by usbManager.detectedPcIp.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .testTag("troubleshooting_screen_root")
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
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(NetheriteCard, CircleShape)
                            .testTag("troubleshooting_back_btn")
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
                            text = "Troubleshooting & Diagnostics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Resolve Bluetooth, USB, and phone-specific issues",
                            fontSize = 12.sp,
                            color = GoldYellow
                        )
                    }
                }
            }

            // Live Input Diagnostic Monitor
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
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Input Diagnostic Monitor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bluetooth HID status row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bluetooth HID Status:", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = btConnectionState.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (btConnectionState == HidConnectionState.CONNECTED) EmeraldGreen else DiamondCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // USB Tethering status row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("USB Tethering Interface:", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = if (isTetheringActive) "ACTIVE (${localPhoneIp ?: "192.168.42.x"})" else "NOT DETECTED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTetheringActive) EmeraldGreen else RedstoneRed
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Windows PC Receiver:", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = usbConnectionState.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (usbConnectionState == UsbConnectionState.CONNECTED) EmeraldGreen else GoldYellow
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Active Keyboard buffer display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF141C18),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Active Held Keys (Live Buffer):", fontSize = 11.sp, color = DiamondCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if ((activeModifiers.toInt() and UsbHidKeyCodes.MOD_LEFT_SHIFT.toInt()) != 0) {
                                        Text("[Shift 0x02]", color = GoldYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    if (activeKeys.isEmpty() && activeModifiers == 0.toByte()) {
                                        Text("None (Idle)", color = TextMuted, fontSize = 12.sp)
                                    } else {
                                        activeKeys.forEach { key ->
                                            Text(
                                                "[${UsbHidKeyCodes.getKeyLabel(key)}: 0x%02X]".format(key),
                                                color = EmeraldGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text("Active Mouse Buttons:", fontSize = 11.sp, color = DiamondCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val leftPressed = (activeMouseButtons.toInt() and UsbHidKeyCodes.MOUSE_BUTTON_LEFT.toInt()) != 0
                                    val rightPressed = (activeMouseButtons.toInt() and UsbHidKeyCodes.MOUSE_BUTTON_RIGHT.toInt()) != 0
                                    if (!leftPressed && !rightPressed) {
                                        Text("None (Idle)", color = TextMuted, fontSize = 12.sp)
                                    } else {
                                        if (leftPressed) Text("[Left Button (Attack)]", color = RedstoneRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (rightPressed) Text("[Right Button (Place)]", color = DiamondCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Topic 1: iQOO 9 SE & Funtouch OS Specific Advice
            item {
                TroubleshootCard(
                    icon = Icons.Default.Smartphone,
                    iconTint = DiamondCyan,
                    title = "iQOO 9 SE & Funtouch OS Optimization",
                    body = "• Bluetooth Profile Limitation: Vivo/iQOO Funtouch OS builds sometimes restrict the native Android Bluetooth HID Device service proxy in their Bluetooth stack. If your phone shows 'HID Unsupported', switch to 'USB Cable' mode above for an instant, guaranteed connection!\n\n" +
                            "• Background App Management: Funtouch OS has aggressive battery-saver policies. In phone Settings -> Battery -> Background power consumption management -> select Minecraft Controller -> set to 'Don't restrict background power consumption' so the OS does not freeze the app while playing.\n\n" +
                            "• USB Tethering on Funtouch OS: Connect USB cable -> open Settings -> 'Other networks & connections' -> 'Personal Hotspot' (or 'USB Tethering') -> toggle 'USB Tethering' ON."
                )
            }

            // Topic 2: USB Cable & PC Receiver Troubleshooting
            item {
                TroubleshootCard(
                    icon = Icons.Default.Usb,
                    iconTint = EmeraldGreen,
                    title = "USB Cable & Windows Receiver Setup",
                    body = "• Why is a receiver needed? Standard Android phones cannot act as a plug-and-play USB hardware keyboard without kernel-level root. USB Tethering creates a direct, offline local network over the cable, and the included 'receiver.py' translates events into native Windows keystrokes.\n\n" +
                            "• Starting Receiver: In the project folder on your PC (/windows-receiver/), double-click 'run_receiver.bat' (or run 'python receiver.py').\n\n" +
                            "• Windows Firewall: If Windows asks for network access, allow it for Private networks so the phone can reach port 25565 over the local USB tether interface.\n\n" +
                            "• Security Pairing Code: Default PIN is '4291'. Both the phone app and receiver.py use this code to prevent any unauthorized device from injecting input."
                )
            }

            // Topic 3: Bluetooth HID Troubleshooting
            item {
                TroubleshootCard(
                    icon = Icons.Default.Bluetooth,
                    iconTint = GoldYellow,
                    title = "Bluetooth HID Pairing & Discovery",
                    body = "• Make Discoverable: Tap the Bluetooth button in Setup to trigger the 120-second discoverability window. Note the phone's Bluetooth name.\n\n" +
                            "• Connecting: After pairing from Windows/Mac/Linux Bluetooth settings, tap 'Connect' next to your PC in the app, or click 'Connect' under your phone's name in Windows Bluetooth settings.\n\n" +
                            "• Stale Pairing: If connection drops repeatedly, delete/unpair the phone on Windows, unpair the PC on phone, turn Bluetooth off for 5 seconds, turn it back on, and re-pair."
                )
            }

            // Topic 4: MSI App Player / BlueStacks, Minecraft & Browser Focus
            item {
                TroubleshootCard(
                    icon = Icons.Default.Mouse,
                    iconTint = RedstoneRed,
                    title = "Connected, but Buttons Don't Work on MSI App Player / Game",
                    body = "• Run Receiver as Administrator (CRITICAL for MSI App Player): MSI App Player & BlueStacks run as Admin on Windows. If receiver.py runs without Admin rights, Windows blocks EVERY button and mouse event! Double-click 'run_receiver.bat' and click 'Yes' on the Admin UAC prompt.\n\n" +
                            "• Click Inside MSI App Player Window: Windows sends keys only to the focused window. Click once inside the MSI App Player screen after starting the receiver.\n\n" +
                            "• Enable Keyboard & Mouse in Minecraft PE/Bedrock (MSI Emulator): Inside Minecraft settings -> 'Keyboard & Mouse' -> enable 'Full Keyboard Gameplay'. Or open MSI 'Game Controls' (Ctrl+Shift+A) to assign WASD, Space, Left/Right Click, and F1 Aim Lock.\n\n" +
                            "• Browser Games (Eaglercraft): Click directly onto the 3D canvas to activate Pointer Lock.\n\n" +
                            "• Receiver Mode Toggle: Press 'M' inside the receiver window to switch between Hybrid (VK+ScanCode, best for MSI/BlueStacks) and ScanCode-only mode."
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TroubleshootCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NetheriteCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StoneBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
