package com.example.usb

import android.content.Context
import android.util.Log
import com.example.model.UsbHidKeyCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

enum class UsbConnectionState {
    TETHERING_NOT_DETECTED,
    RECEIVER_DISCONNECTED,
    CONNECTING,
    AUTHENTICATING,
    CONNECTED,
    ERROR
}

/**
 * Manages local offline USB tethering communication with the Windows PC receiver.
 * Uses TCP with NO_DELAY and an ordered FIFO writer queue for sub-millisecond response
 * without out-of-order key drops.
 */
class UsbTetherManager(private val context: Context) {

    companion object {
        private const val TAG = "UsbTetherManager"
        const val DEFAULT_PORT = 25565
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var connectionJob: Job? = null
    private var udpDiscoveryJob: Job? = null
    private var usbMouseDispatchJob: Job? = null
    private var writerJob: Job? = null

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var commandChannel = Channel<String>(Channel.UNLIMITED)

    private val _connectionState = MutableStateFlow(UsbConnectionState.TETHERING_NOT_DETECTED)
    val connectionState: StateFlow<UsbConnectionState> = _connectionState.asStateFlow()

    private val _autoDetectEnabled = MutableStateFlow(false)
    val autoDetectEnabled: StateFlow<Boolean> = _autoDetectEnabled.asStateFlow()

    private val _detectedPcIp = MutableStateFlow("192.168.42.129")
    val detectedPcIp: StateFlow<String> = _detectedPcIp.asStateFlow()

    private val _localPhoneIp = MutableStateFlow<String?>(null)
    val localPhoneIp: StateFlow<String?> = _localPhoneIp.asStateFlow()

    private val _isTetheringActive = MutableStateFlow(false)
    val isTetheringActive: StateFlow<Boolean> = _isTetheringActive.asStateFlow()

    private val _pairingCode = MutableStateFlow("4291")
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Tracking for live visualizer
    private val heldKeys = ConcurrentHashMap.newKeySet<Byte>()
    private var modifierMask: Byte = 0
    private var mouseButtonsMask: Byte = 0

    private val _activeKeysList = MutableStateFlow<Set<Byte>>(emptySet())
    val activeKeysList: StateFlow<Set<Byte>> = _activeKeysList.asStateFlow()

    private val _activeModifiers = MutableStateFlow<Byte>(0)
    val activeModifiers: StateFlow<Byte> = _activeModifiers.asStateFlow()

    private val _activeMouseButtons = MutableStateFlow<Byte>(0)
    val activeMouseButtons: StateFlow<Byte> = _activeMouseButtons.asStateFlow()

    // Mouse movement accumulator for USB mode
    private val pendingUsbMouseDx = AtomicInteger(0)
    private val pendingUsbMouseDy = AtomicInteger(0)

    init {
        checkTetheringInterface()
        if (_autoDetectEnabled.value) {
            startUdpDiscovery()
        }
    }

    fun setAutoDetectEnabled(enabled: Boolean) {
        _autoDetectEnabled.value = enabled
        if (enabled) {
            checkTetheringInterface()
            startUdpDiscovery()
        } else {
            stopUdpDiscovery()
        }
    }

    fun setPairingCode(code: String) {
        _pairingCode.value = code
    }

    fun setTargetPcIp(ip: String) {
        _detectedPcIp.value = ip
    }

    /**
     * Checks network interfaces for active USB Tethering (rndis, usb, or ncm interfaces).
     */
    fun checkTetheringInterface(): Boolean {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val name = iface.name.lowercase()
                if (iface.isUp && (name.startsWith("rndis") || name.startsWith("usb") || name.startsWith("ncm"))) {
                    val addrs = iface.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr.hostAddress != null && addr.hostAddress.contains(".")) {
                            val ip = addr.hostAddress
                            _localPhoneIp.value = ip
                            _isTetheringActive.value = true

                            if (_autoDetectEnabled.value) {
                                val arpHost = findArpHost()
                                if (arpHost != null) {
                                    _detectedPcIp.value = arpHost
                                } else {
                                    val subnet = ip.substringBeforeLast(".")
                                    val hostLast = ip.substringAfterLast(".").toIntOrNull() ?: 1
                                    val pcHost = if (hostLast == 129) "1" else if (hostLast == 1) "129" else "1"
                                    _detectedPcIp.value = "$subnet.$pcHost"
                                }
                            }
                            Log.d(TAG, "Detected USB tethering interface: $name (IP: $ip), PC IP: ${_detectedPcIp.value}")
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking tethering interface", e)
        }
        _isTetheringActive.value = false
        _localPhoneIp.value = null
        return false
    }

    /**
     * Parse Linux kernel ARP table to locate exact PC IP connected on rndis/usb interface.
     */
    private fun findArpHost(): String? {
        try {
            val file = File("/proc/net/arp")
            if (file.exists() && file.canRead()) {
                val lines = file.readLines()
                for (line in lines) {
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 6) {
                        val ip = parts[0]
                        val device = parts[5].lowercase()
                        if (device.startsWith("rndis") || device.startsWith("usb") || device.startsWith("ncm")) {
                            if (ip.contains(".") && !ip.endsWith(".0") && !ip.endsWith(".255")) {
                                Log.d(TAG, "Found ARP entry on $device: $ip")
                                return ip
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not read /proc/net/arp: ${e.message}")
        }
        return null
    }

    /**
     * Listen for receiver.py UDP discovery beacons (MC_RECEIVER_BEACON <PIN> <IP>).
     */
    fun startUdpDiscovery() {
        if (!_autoDetectEnabled.value) return
        if (udpDiscoveryJob?.isActive == true) return
        udpDiscoveryJob = scope.launch {
            var udpSocket: DatagramSocket? = null
            try {
                udpSocket = DatagramSocket(null)
                udpSocket.reuseAddress = true
                udpSocket.bind(InetSocketAddress(DEFAULT_PORT))
                udpSocket.soTimeout = 4000
                val buffer = ByteArray(512)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive) {
                    try {
                        udpSocket.receive(packet)
                        if (!_autoDetectEnabled.value) continue
                        val text = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                        if (text.startsWith("MC_RECEIVER_BEACON")) {
                            val parts = text.split(" ")
                            if (parts.size >= 3) {
                                val beaconPin = parts[1]
                                val pcIp = parts[2]
                                Log.d(TAG, "Discovered PC receiver beacon at $pcIp (PIN: $beaconPin)")
                                _detectedPcIp.value = pcIp
                                _isTetheringActive.value = true

                                if (_connectionState.value == UsbConnectionState.TETHERING_NOT_DETECTED ||
                                    _connectionState.value == UsbConnectionState.RECEIVER_DISCONNECTED) {
                                    connect()
                                }
                            }
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        // Periodic check if tethering active
                    } catch (e: Exception) {
                        if (!isActive) break
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "UDP discovery listener note: ${e.message}")
            } finally {
                try {
                    udpSocket?.close()
                } catch (e: Exception) {}
            }
        }
    }

    fun stopUdpDiscovery() {
        udpDiscoveryJob?.cancel()
        udpDiscoveryJob = null
    }

    private fun startWriterLoop(out: OutputStream) {
        writerJob?.cancel()
        commandChannel.close()
        commandChannel = Channel(Channel.UNLIMITED)
        writerJob = scope.launch {
            try {
                for (cmd in commandChannel) {
                    out.write(cmd.toByteArray(Charsets.UTF_8))
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Writer loop error: ${e.message}")
                disconnect()
            }
        }
    }

    /**
     * Connects directly to the user-specified PC IP address and PIN.
     * Bypasses interface name restrictions and candidate probing.
     */
    fun connectManual(targetIp: String, pin: String, port: Int = DEFAULT_PORT) {
        val cleanIp = targetIp.trim()
        val cleanPin = pin.trim()

        if (cleanIp.isBlank()) {
            _lastError.value = "Please enter your PC IP address (e.g. 192.168.42.129)"
            _connectionState.value = UsbConnectionState.ERROR
            return
        }

        _detectedPcIp.value = cleanIp
        _pairingCode.value = cleanPin

        connectionJob?.cancel()
        connectionJob = scope.launch {
            _connectionState.value = UsbConnectionState.CONNECTING
            _lastError.value = null

            Log.d(TAG, "Manual connection initiated to $cleanIp:$port with PIN $cleanPin")

            var connectedSocket: Socket? = null
            try {
                val sock = Socket()
                sock.tcpNoDelay = true
                sock.soTimeout = 4000
                sock.connect(InetSocketAddress(cleanIp, port), 2500)
                connectedSocket = sock
                Log.d(TAG, "Connected socket directly to $cleanIp:$port")
            } catch (e: Exception) {
                Log.e(TAG, "Manual connection failed to $cleanIp:$port: ${e.message}")
                _connectionState.value = UsbConnectionState.RECEIVER_DISCONNECTED
                _lastError.value = "Could not reach PC at $cleanIp:$port. Check that receiver.py is running as Admin and the IP address matches."
                disconnect()
                return@launch
            }

            socket = connectedSocket
            val out = connectedSocket.getOutputStream()
            outputStream = out

            try {
                _connectionState.value = UsbConnectionState.AUTHENTICATING
                val reader = BufferedReader(InputStreamReader(connectedSocket.getInputStream()))

                out.write("AUTH $cleanPin\n".toByteArray(Charsets.UTF_8))
                out.flush()
                val response = reader.readLine()

                if (response != null && response.startsWith("AUTH_OK")) {
                    Log.d(TAG, "Authenticated successfully with PC receiver at $cleanIp!")
                    startWriterLoop(out)
                    _connectionState.value = UsbConnectionState.CONNECTED
                    _lastError.value = null

                    startUsbMouseDispatchLoop()

                    // Keepalive monitor
                    while (isActive && connectedSocket.isConnected && !connectedSocket.isClosed) {
                        delay(2500)
                        sendRawCommand("PING\n")
                    }
                } else {
                    _connectionState.value = UsbConnectionState.ERROR
                    _lastError.value = "PIN verification failed with response: $response. Check PIN in receiver.py (default: 4291)."
                    disconnect()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Connection lost to PC receiver: ${e.message}")
                _connectionState.value = UsbConnectionState.RECEIVER_DISCONNECTED
                _lastError.value = "Connection lost: ${e.message}"
                disconnect()
            }
        }
    }

    /**
     * Connects to the PC receiver over USB tethering.
     */
    fun connect() {
        if (!_autoDetectEnabled.value) {
            connectManual(_detectedPcIp.value, _pairingCode.value, DEFAULT_PORT)
            return
        }

        connectionJob?.cancel()
        connectionJob = scope.launch {
            val hasTether = checkTetheringInterface()
            if (!hasTether && _localPhoneIp.value == null) {
                _connectionState.value = UsbConnectionState.TETHERING_NOT_DETECTED
                _lastError.value = "USB Tethering is not turned on. Enable USB Tethering in phone Settings."
                return@launch
            }

            _connectionState.value = UsbConnectionState.CONNECTING
            _lastError.value = null

            val candidates = mutableListOf<String>()
            _detectedPcIp.value.takeIf { it.isNotBlank() }?.let { candidates.add(it) }
            findArpHost()?.let { candidates.add(it) }

            val localIp = _localPhoneIp.value
            if (localIp != null && localIp.contains(".")) {
                val subnet = localIp.substringBeforeLast(".")
                val localLast = localIp.substringAfterLast(".").toIntOrNull() ?: 1
                val probeHosts = listOf(1, 2, 129, 130, 100, 254)
                for (h in probeHosts) {
                    if (h != localLast) {
                        candidates.add("$subnet.$h")
                    }
                }
            }

            val uniqueCandidates = candidates.distinct()
            Log.d(TAG, "Probing PC receiver candidates: $uniqueCandidates")

            var connectedSocket: Socket? = null
            var successfulIp: String? = null

            for (targetIp in uniqueCandidates) {
                try {
                    val sock = Socket()
                    sock.tcpNoDelay = true
                    sock.soTimeout = 4000
                    sock.connect(InetSocketAddress(targetIp, DEFAULT_PORT), 750)
                    connectedSocket = sock
                    successfulIp = targetIp
                    Log.d(TAG, "Successfully connected to PC receiver at $targetIp!")
                    break
                } catch (e: Exception) {
                    // Try next candidate
                }
            }

            if (connectedSocket == null || successfulIp == null) {
                _connectionState.value = UsbConnectionState.RECEIVER_DISCONNECTED
                _lastError.value = "PC Receiver not found. Ensure receiver.py is running on your PC and USB Tethering is ON."
                disconnect()
                return@launch
            }

            _detectedPcIp.value = successfulIp
            socket = connectedSocket
            val out = connectedSocket.getOutputStream()
            outputStream = out

            try {
                _connectionState.value = UsbConnectionState.AUTHENTICATING
                val reader = BufferedReader(InputStreamReader(connectedSocket.getInputStream()))

                out.write("AUTH ${_pairingCode.value}\n".toByteArray(Charsets.UTF_8))
                out.flush()
                val response = reader.readLine()

                if (response != null && response.startsWith("AUTH_OK")) {
                    Log.d(TAG, "Authenticated with Windows PC receiver at $successfulIp!")
                    startWriterLoop(out)
                    _connectionState.value = UsbConnectionState.CONNECTED
                    _lastError.value = null

                    startUsbMouseDispatchLoop()

                    // Keepalive monitor
                    while (isActive && connectedSocket.isConnected && !connectedSocket.isClosed) {
                        delay(2500)
                        sendRawCommand("PING\n")
                    }
                } else {
                    _connectionState.value = UsbConnectionState.ERROR
                    _lastError.value = "Authentication failed. Server response: $response"
                    disconnect()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Connection lost to PC receiver: ${e.message}")
                _connectionState.value = UsbConnectionState.RECEIVER_DISCONNECTED
                _lastError.value = "Lost connection to PC receiver: ${e.message}"
                disconnect()
            }
        }
    }

    fun disconnect() {
        usbMouseDispatchJob?.cancel()
        writerJob?.cancel()
        commandChannel.close()
        pendingUsbMouseDx.set(0)
        pendingUsbMouseDy.set(0)
        try {
            outputStream?.flush()
            outputStream?.close()
        } catch (e: Exception) {}
        try {
            socket?.close()
        } catch (e: Exception) {}
        socket = null
        outputStream = null
        clearHeldStatesOnly()
        if (_connectionState.value == UsbConnectionState.CONNECTED ||
            _connectionState.value == UsbConnectionState.CONNECTING ||
            _connectionState.value == UsbConnectionState.AUTHENTICATING) {
            _connectionState.value = UsbConnectionState.RECEIVER_DISCONNECTED
        }
    }

    private fun clearHeldStatesOnly() {
        heldKeys.clear()
        modifierMask = 0
        mouseButtonsMask = 0
        pendingUsbMouseDx.set(0)
        pendingUsbMouseDy.set(0)
        _activeKeysList.value = emptySet()
        _activeModifiers.value = 0
        _activeMouseButtons.value = 0
    }

    private fun sendRawCommand(cmd: String) {
        if (outputStream == null || socket?.isConnected != true) return
        commandChannel.trySend(cmd)
    }

    /**
     * High frequency throttled mouse dispatch loop (~100Hz / 10ms) over USB.
     * Prevents TCP packet flooding and eliminates input lag.
     */
    private fun startUsbMouseDispatchLoop() {
        usbMouseDispatchJob?.cancel()
        usbMouseDispatchJob = scope.launch {
            while (isActive && socket?.isConnected == true) {
                delay(10)
                val dx = pendingUsbMouseDx.getAndSet(0)
                val dy = pendingUsbMouseDy.getAndSet(0)
                if (dx != 0 || dy != 0) {
                    sendRawCommand("MM $dx $dy\n")
                }
            }
        }
    }

    fun sendTestKey(keyName: String = "SPACE") {
        sendRawCommand("TEST_KEY $keyName\n")
    }

    fun pressKey(vararg keyCodes: Byte) {
        for (k in keyCodes) {
            if (k != 0.toByte() && heldKeys.add(k)) {
                val keyName = UsbHidKeyCodes.getKeyLabel(k)
                sendRawCommand("KD $keyName\n")
            }
        }
        _activeKeysList.value = heldKeys.toSet()
    }

    fun releaseKey(vararg keyCodes: Byte) {
        for (k in keyCodes) {
            if (heldKeys.remove(k)) {
                val keyName = UsbHidKeyCodes.getKeyLabel(k)
                sendRawCommand("KU $keyName\n")
            }
        }
        _activeKeysList.value = heldKeys.toSet()
    }

    fun setHeldKeys(keysToHold: Set<Byte>) {
        val toRelease = heldKeys.filter { !keysToHold.contains(it) }
        val toPress = keysToHold.filter { !heldKeys.contains(it) }

        toRelease.forEach { releaseKey(it) }
        toPress.forEach { pressKey(it) }
    }

    fun setModifier(modifierBit: Byte, isPressed: Boolean) {
        modifierMask = if (isPressed) {
            (modifierMask.toInt() or modifierBit.toInt()).toByte()
        } else {
            (modifierMask.toInt() and modifierBit.toInt().inv()).toByte()
        }
        _activeModifiers.value = modifierMask

        val modName = when (modifierBit) {
            UsbHidKeyCodes.MOD_LEFT_SHIFT -> "LSHIFT"
            UsbHidKeyCodes.MOD_LEFT_CTRL -> "LCTRL"
            UsbHidKeyCodes.MOD_LEFT_ALT -> "LALT"
            else -> "LSHIFT"
        }

        if (isPressed) {
            sendRawCommand("KD $modName\n")
        } else {
            sendRawCommand("KU $modName\n")
        }
    }

    fun sendMouseMove(dx: Float, dy: Float, sensitivity: Float, invertY: Boolean = false) {
        val ySign = if (invertY) -1f else 1f
        val totalDx = (dx * sensitivity).roundToInt()
        val totalDy = (dy * sensitivity * ySign).roundToInt()

        if (totalDx != 0 || totalDy != 0) {
            pendingUsbMouseDx.addAndGet(totalDx)
            pendingUsbMouseDy.addAndGet(totalDy)
        }
    }

    fun sendMouseWheel(scroll: Int) {
        if (scroll != 0) {
            sendRawCommand("MW $scroll\n")
        }
    }

    fun setMouseButton(buttonBit: Byte, isPressed: Boolean) {
        mouseButtonsMask = if (isPressed) {
            (mouseButtonsMask.toInt() or buttonBit.toInt()).toByte()
        } else {
            (mouseButtonsMask.toInt() and buttonBit.toInt().inv()).toByte()
        }
        _activeMouseButtons.value = mouseButtonsMask

        val btnStr = when (buttonBit) {
            UsbHidKeyCodes.MOUSE_BUTTON_LEFT -> "L"
            UsbHidKeyCodes.MOUSE_BUTTON_RIGHT -> "R"
            else -> "M"
        }

        if (isPressed) {
            sendRawCommand("MD $btnStr\n")
        } else {
            sendRawCommand("MU $btnStr\n")
        }
    }

    fun releaseAllHeldInputs() {
        clearHeldStatesOnly()
        sendRawCommand("RELEASE_ALL\n")
    }

    fun close() {
        udpDiscoveryJob?.cancel()
        releaseAllHeldInputs()
        disconnect()
    }
}
