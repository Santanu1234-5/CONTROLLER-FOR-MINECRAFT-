package com.example.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.BondedDeviceInfo
import com.example.model.HidConnectionState
import com.example.model.UsbHidKeyCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

/**
 * Handles Bluetooth HID Device profile proxy, app registration, host connection,
 * and sending USB HID Keyboard (Report ID 1) and Mouse (Report ID 2) reports.
 */
class HidDeviceManager(private val context: Context) {

    companion object {
        private const val TAG = "HidDeviceManager"

        const val REPORT_ID_KEYBOARD: Byte = 1
        const val REPORT_ID_MOUSE: Byte = 2

        /**
         * USB HID Report Descriptor defining a composite Keyboard (Report ID 1)
         * and Mouse (Report ID 2) interface.
         * Uses strict 1-byte logical/usage max (0x65 = 101 keys) for universal Windows/Mac/Linux compatibility.
         */
        val HID_REPORT_DESCRIPTOR: ByteArray = byteArrayOf(
            // ==========================================
            // Keyboard Collection (Report ID 1)
            // ==========================================
            0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
            0x09.toByte(), 0x06.toByte(), // Usage (Keyboard)
            0xA1.toByte(), 0x01.toByte(), // Collection (Application)
            0x85.toByte(), 0x01.toByte(), //   Report ID (1)
            0x05.toByte(), 0x07.toByte(), //   Usage Page (Key Codes)
            0x19.toByte(), 0xE0.toByte(), //   Usage Minimum (224: Left Control)
            0x29.toByte(), 0xE7.toByte(), //   Usage Maximum (231: Right GUI)
            0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(), //   Logical Maximum (1)
            0x75.toByte(), 0x01.toByte(), //   Report Size (1 bit)
            0x95.toByte(), 0x08.toByte(), //   Report Count (8 bits)
            0x81.toByte(), 0x02.toByte(), //   Input (Data, Variable, Absolute) -> Modifiers byte

            0x95.toByte(), 0x01.toByte(), //   Report Count (1)
            0x75.toByte(), 0x08.toByte(), //   Report Size (8 bits)
            0x81.toByte(), 0x01.toByte(), //   Input (Constant) -> 1 Reserved Byte

            // LED Indicators Output (Caps Lock, Num Lock, etc.)
            0x95.toByte(), 0x05.toByte(), //   Report Count (5)
            0x75.toByte(), 0x01.toByte(), //   Report Size (1 bit)
            0x05.toByte(), 0x08.toByte(), //   Usage Page (LEDs)
            0x19.toByte(), 0x01.toByte(), //   Usage Minimum (1)
            0x29.toByte(), 0x05.toByte(), //   Usage Maximum (5)
            0x91.toByte(), 0x02.toByte(), //   Output (Data, Variable, Absolute)
            0x95.toByte(), 0x01.toByte(), //   Report Count (1)
            0x75.toByte(), 0x03.toByte(), //   Report Size (3 bits)
            0x91.toByte(), 0x01.toByte(), //   Output (Constant) -> Padding to full byte

            // 6-Key Array for simultaneous key presses (up to 101 keys: letters, numbers, F1-F12, Space, Enter, Esc)
            0x95.toByte(), 0x06.toByte(), //   Report Count (6 keys)
            0x75.toByte(), 0x08.toByte(), //   Report Size (8 bits per key)
            0x15.toByte(), 0x00.toByte(), //   Logical Minimum (0)
            0x25.toByte(), 0x65.toByte(), //   Logical Maximum (101: Keyboard Application / key codes)
            0x05.toByte(), 0x07.toByte(), //   Usage Page (Key Codes)
            0x19.toByte(), 0x00.toByte(), //   Usage Minimum (0)
            0x29.toByte(), 0x65.toByte(), //   Usage Maximum (101)
            0x81.toByte(), 0x00.toByte(), //   Input (Data, Array) -> 6-byte key buffer
            0xC0.toByte(),                 // End Collection (Keyboard)

            // ==========================================
            // Mouse Collection (Report ID 2)
            // ==========================================
            0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
            0x09.toByte(), 0x02.toByte(), // Usage (Mouse)
            0xA1.toByte(), 0x01.toByte(), // Collection (Application)
            0x85.toByte(), 0x02.toByte(), //   Report ID (2)
            0x09.toByte(), 0x01.toByte(), //   Usage (Pointer)
            0xA1.toByte(), 0x00.toByte(), //   Collection (Physical)
            // Buttons (Left, Right, Middle)
            0x05.toByte(), 0x09.toByte(), //     Usage Page (Buttons)
            0x19.toByte(), 0x01.toByte(), //     Usage Minimum (1: Left Button)
            0x29.toByte(), 0x03.toByte(), //     Usage Maximum (3: Middle Button)
            0x15.toByte(), 0x00.toByte(), //     Logical Minimum (0)
            0x25.toByte(), 0x01.toByte(), //     Logical Maximum (1)
            0x95.toByte(), 0x03.toByte(), //     Report Count (3 buttons)
            0x75.toByte(), 0x01.toByte(), //     Report Size (1 bit)
            0x81.toByte(), 0x02.toByte(), //     Input (Data, Variable, Absolute) -> 3 bits
            0x95.toByte(), 0x01.toByte(), //     Report Count (1)
            0x75.toByte(), 0x05.toByte(), //     Report Size (5 bits)
            0x81.toByte(), 0x01.toByte(), //     Input (Constant) -> 5 bits padding
            // Relative Motion (X, Y, Wheel)
            0x05.toByte(), 0x01.toByte(), //     Usage Page (Generic Desktop)
            0x09.toByte(), 0x30.toByte(), //     Usage (X)
            0x09.toByte(), 0x31.toByte(), //     Usage (Y)
            0x09.toByte(), 0x38.toByte(), //     Usage (Wheel)
            0x15.toByte(), 0x81.toByte(), //     Logical Minimum (-127)
            0x25.toByte(), 0x7F.toByte(), //     Logical Maximum (127)
            0x75.toByte(), 0x08.toByte(), //     Report Size (8 bits)
            0x95.toByte(), 0x03.toByte(), //     Report Count (3 axes: X, Y, Wheel)
            0x81.toByte(), 0x06.toByte(), //     Input (Data, Variable, Relative)
            0xC0.toByte(),                 //   End Collection (Physical)
            0xC0.toByte()                  // End Collection (Application)
        )
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionState = MutableStateFlow(HidConnectionState.INITIALIZING)
    val connectionState: StateFlow<HidConnectionState> = _connectionState.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice.asStateFlow()

    private val _bondedDevices = MutableStateFlow<List<BondedDeviceInfo>>(emptyList())
    val bondedDevices: StateFlow<List<BondedDeviceInfo>> = _bondedDevices.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    // Active input indicators for visual feedback
    private val _activeKeysList = MutableStateFlow<Set<Byte>>(emptySet())
    val activeKeysList: StateFlow<Set<Byte>> = _activeKeysList.asStateFlow()

    private val _activeModifiers = MutableStateFlow<Byte>(0)
    val activeModifiers: StateFlow<Byte> = _activeModifiers.asStateFlow()

    private val _activeMouseButtons = MutableStateFlow<Byte>(0)
    val activeMouseButtons: StateFlow<Byte> = _activeMouseButtons.asStateFlow()

    private var hidDevice: BluetoothHidDevice? = null

    // Track currently held keys thread-safely
    private val heldKeys = ConcurrentHashMap.newKeySet<Byte>()
    private var modifierMask: Byte = 0
    private var mouseButtonsMask: Byte = 0

    // Coroutine scope for background tasks and throttled mouse report dispatch
    private val scope = CoroutineScope(Dispatchers.Default)

    // Mouse movement accumulator and throttling to prevent Bluetooth L2CAP buffer bloat / multi-second lag
    private val pendingMouseDx = AtomicInteger(0)
    private val pendingMouseDy = AtomicInteger(0)
    private val pendingMouseWheel = AtomicInteger(0)
    private var mouseDispatchJob: Job? = null

    // Callback for Bluetooth HID Device service proxy
    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Bluetooth HID Device profile proxy connected")
                hidDevice = proxy as BluetoothHidDevice
                checkExistingConnections()
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Bluetooth HID Device profile disconnected")
                mouseDispatchJob?.cancel()
                hidDevice = null
                _isRegistered.value = false
                _connectedDevice.value = null
                _connectionState.value = HidConnectionState.READY_DISCONNECTED
            }
        }
    }

    // Callback for Bluetooth HID Device events
    private val hidDeviceCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered, device=${pluggedDevice?.address}")
            _isRegistered.value = registered
            if (registered) {
                if (pluggedDevice != null) {
                    _connectedDevice.value = pluggedDevice
                    _connectionState.value = HidConnectionState.CONNECTED
                    startMouseDispatchLoop()
                    Log.d(TAG, "Host plugged immediately: ${pluggedDevice.address}")
                } else {
                    checkExistingConnections()
                    if (_connectedDevice.value == null && _connectionState.value != HidConnectionState.CONNECTED) {
                        _connectionState.value = HidConnectionState.READY_DISCONNECTED
                    }
                }
                refreshBondedDevices(skipCheckConnections = true)
            } else {
                _connectionState.value = HidConnectionState.REGISTRATION_FAILED
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d(TAG, "onConnectionStateChanged: device=${device?.address}, state=$state")
            when (state) {
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = HidConnectionState.CONNECTING
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectedDevice.value = device
                    _connectionState.value = HidConnectionState.CONNECTED
                    startMouseDispatchLoop()
                    refreshBondedDevices(skipCheckConnections = true)
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    _connectionState.value = HidConnectionState.DISCONNECTING
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    mouseDispatchJob?.cancel()
                    if (_connectedDevice.value?.address == device?.address) {
                        _connectedDevice.value = null
                    }
                    releaseAllHeldInputs()
                    _connectionState.value = if (_isRegistered.value) {
                        HidConnectionState.READY_DISCONNECTED
                    } else {
                        HidConnectionState.INITIALIZING
                    }
                    refreshBondedDevices(skipCheckConnections = true)
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(TAG, "onGetReport: type=$type, id=$id, bufferSize=$bufferSize")
            val target = device ?: _connectedDevice.value
            val proxy = hidDevice
            if (target != null && proxy != null) {
                try {
                    when (type) {
                        BluetoothHidDevice.REPORT_TYPE_INPUT -> {
                            when (id) {
                                REPORT_ID_KEYBOARD -> {
                                    proxy.replyReport(target, type, id, buildKeyboardReport())
                                }
                                REPORT_ID_MOUSE -> {
                                    val report = byteArrayOf(mouseButtonsMask, 0, 0, 0)
                                    proxy.replyReport(target, type, id, report)
                                }
                                else -> {
                                    // Host requesting report with unspecified/0 ID - reply with keyboard report
                                    proxy.replyReport(target, type, id, buildKeyboardReport())
                                }
                            }
                        }
                        BluetoothHidDevice.REPORT_TYPE_OUTPUT -> {
                            // Output report is for keyboard LEDs (Caps/Num Lock) - exactly 1 byte
                            proxy.replyReport(target, type, id, byteArrayOf(0))
                        }
                        BluetoothHidDevice.REPORT_TYPE_FEATURE -> {
                            // Feature reports: acknowledge with matching-sized zero buffer
                            val size = if (bufferSize > 0) bufferSize.coerceAtMost(64) else 1
                            proxy.replyReport(target, type, id, ByteArray(size))
                        }
                        else -> {
                            proxy.reportError(target, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in onGetReport", e)
                }
            }
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            Log.d(TAG, "onSetReport: type=$type, id=$id, dataSize=${data?.size}")
            val target = device ?: _connectedDevice.value
            if (target != null) {
                try {
                    hidDevice?.reportError(target, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error replying onSetReport", e)
                }
            }
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            Log.d(TAG, "onSetProtocol: protocol=$protocol")
            val target = device ?: _connectedDevice.value
            if (target != null) {
                try {
                    hidDevice?.reportError(target, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error replying onSetProtocol", e)
                }
            }
        }

        override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
            // Unused
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice?) {
            Log.d(TAG, "onVirtualCableUnplug: device=${device?.address}")
            if (_connectedDevice.value?.address == device?.address) {
                _connectedDevice.value = null
            }
            releaseAllHeldInputs()
            _connectionState.value = HidConnectionState.READY_DISCONNECTED
            refreshBondedDevices(skipCheckConnections = true)
        }
    }

    /**
     * Inspects proxy to see if any Bluetooth device is currently connected.
     */
    @SuppressLint("MissingPermission")
    fun checkExistingConnections() {
        val proxy = hidDevice ?: return
        if (!hasRequiredPermissions()) return
        try {
            val connected = proxy.connectedDevices
            if (!connected.isNullOrEmpty()) {
                val dev = connected.first()
                _connectedDevice.value = dev
                _connectionState.value = HidConnectionState.CONNECTED
                startMouseDispatchLoop()
                Log.d(TAG, "Found active connected device: ${dev.address}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking connected devices", e)
        }
    }

    /**
     * Initializes the Bluetooth HID profile and registers the device.
     */
    fun initialize() {
        if (bluetoothAdapter == null) {
            _connectionState.value = HidConnectionState.NO_BLUETOOTH_HARDWARE
            return
        }

        if (!hasRequiredPermissions()) {
            _connectionState.value = HidConnectionState.PERMISSION_NEEDED
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = HidConnectionState.BLUETOOTH_OFF
            return
        }

        if (hidDevice != null) {
            checkExistingConnections()
            if (!_isRegistered.value) {
                registerHidApp()
            }
            return
        }

        _connectionState.value = HidConnectionState.INITIALIZING

        try {
            val supported = bluetoothAdapter.getProfileProxy(
                context,
                serviceListener,
                BluetoothProfile.HID_DEVICE
            )
            if (!supported) {
                Log.e(TAG, "Bluetooth HID Device profile is unsupported by this device's Bluetooth stack")
                _connectionState.value = HidConnectionState.HID_UNSUPPORTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting HID_DEVICE profile proxy", e)
            _connectionState.value = HidConnectionState.HID_UNSUPPORTED
        }
    }

    /**
     * Check if necessary Bluetooth permissions are granted.
     */
    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Register HID application with SDP settings.
     */
    @SuppressLint("MissingPermission")
    private fun registerHidApp() {
        val proxy = hidDevice ?: return
        if (_isRegistered.value) return

        _connectionState.value = HidConnectionState.REGISTERING_APP

        val sdp = BluetoothHidDeviceAppSdpSettings(
            "Minecraft BT Controller",
            "Bluetooth Keyboard and Mouse Controller for Minecraft",
            "Android",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            HID_REPORT_DESCRIPTOR
        )

        try {
            val success = proxy.registerApp(
                sdp,
                null,
                null,
                ContextCompat.getMainExecutor(context),
                hidDeviceCallback
            )
            if (!success) {
                Log.e(TAG, "registerApp returned false")
                _connectionState.value = HidConnectionState.REGISTRATION_FAILED
            } else {
                Log.d(TAG, "registerApp request dispatched successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling registerApp", e)
            _connectionState.value = HidConnectionState.REGISTRATION_FAILED
        }
    }

    /**
     * Connect to a selected bonded host PC.
     */
    @SuppressLint("MissingPermission")
    fun connectToHost(device: BluetoothDevice) {
        val proxy = hidDevice
        if (proxy == null) {
            initialize()
            return
        }
        if (_connectedDevice.value?.address == device.address && _connectionState.value == HidConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected to host ${device.address}")
            return
        }
        _connectionState.value = HidConnectionState.CONNECTING
        try {
            val success = proxy.connect(device)
            Log.d(TAG, "connect() returned $success for ${device.address}")
            if (!success) {
                Log.w(TAG, "Note: On Windows/Mac, you can also initiate connection from PC Bluetooth settings.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to host", e)
            _connectionState.value = HidConnectionState.READY_DISCONNECTED
        }
    }

    /**
     * Attempts to reconnect to the first available bonded host PC.
     */
    @SuppressLint("MissingPermission")
    fun reconnectToAnyBondedHost(): Boolean {
        if (!hasRequiredPermissions() || bluetoothAdapter == null) return false
        try {
            val bonded = bluetoothAdapter.bondedDevices ?: emptySet()
            if (bonded.isNotEmpty()) {
                connectToHost(bonded.first())
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reconnecting to bonded host", e)
        }
        return false
    }

    /**
     * Disconnect from current host.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        val device = _connectedDevice.value ?: return
        val proxy = hidDevice ?: return
        releaseAllHeldInputs()
        _connectionState.value = HidConnectionState.DISCONNECTING
        try {
            proxy.disconnect(device)
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    /**
     * Refresh the list of bonded (paired) devices.
     */
    @SuppressLint("MissingPermission")
    fun refreshBondedDevices(skipCheckConnections: Boolean = false) {
        if (!skipCheckConnections) {
            checkExistingConnections()
        }
        if (!hasRequiredPermissions() || bluetoothAdapter == null) {
            _bondedDevices.value = emptyList()
            return
        }

        try {
            val bonded = bluetoothAdapter.bondedDevices ?: emptySet()
            val currentAddress = _connectedDevice.value?.address
            _bondedDevices.value = bonded.map { dev ->
                val name = try {
                    dev.name ?: "Unknown Host"
                } catch (e: Exception) {
                    "Bluetooth Device"
                }
                BondedDeviceInfo(
                    name = name,
                    address = dev.address,
                    isConnected = dev.address == currentAddress
                )
            }.sortedByDescending { it.isConnected }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading bonded devices", e)
            _bondedDevices.value = emptyList()
        }
    }

    /**
     * Build standard 8-byte USB HID Keyboard report.
     * Byte 0: Modifier bitmask
     * Byte 1: Reserved (0)
     * Byte 2..7: Up to 6 simultaneous key codes
     */
    private fun buildKeyboardReport(): ByteArray {
        val report = ByteArray(8)
        report[0] = modifierMask
        report[1] = 0 // reserved
        var index = 2
        for (code in heldKeys) {
            if (index < 8) {
                report[index++] = code
            }
        }
        return report
    }

    /**
     * Send updated keyboard report to connected host.
     * Always updates visual feedback indicators regardless of connection.
     */
    @SuppressLint("MissingPermission")
    private fun sendKeyboardReport() {
        _activeKeysList.value = heldKeys.toSet()
        _activeModifiers.value = modifierMask

        val device = _connectedDevice.value ?: return
        val proxy = hidDevice ?: return
        if (!_isRegistered.value) return

        val report = buildKeyboardReport()
        try {
            val sent = proxy.sendReport(device, REPORT_ID_KEYBOARD.toInt(), report)
            if (!sent) {
                Log.w(TAG, "sendReport returned false for keyboard report")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending keyboard report", e)
        }
    }

    /**
     * Press one or more keyboard keys (e.g. W, A, S, D, Space, E, Esc).
     */
    fun pressKey(vararg keyCodes: Byte) {
        var changed = false
        for (k in keyCodes) {
            if (k != 0.toByte() && heldKeys.add(k)) {
                changed = true
            }
        }
        if (changed) {
            sendKeyboardReport()
        }
    }

    /**
     * Release one or more keyboard keys.
     */
    fun releaseKey(vararg keyCodes: Byte) {
        var changed = false
        for (k in keyCodes) {
            if (heldKeys.remove(k)) {
                changed = true
            }
        }
        if (changed) {
            sendKeyboardReport()
        }
    }

    /**
     * Atomically set the exact set of pressed keys (e.g., from joystick direction).
     */
    fun setHeldKeys(keysToHold: Set<Byte>) {
        if (heldKeys != keysToHold) {
            heldKeys.clear()
            heldKeys.addAll(keysToHold)
            sendKeyboardReport()
        }
    }

    /**
     * Set or clear a keyboard modifier (e.g. Left Shift for Sneak).
     */
    fun setModifier(modifierBit: Byte, isPressed: Boolean) {
        val original = modifierMask
        modifierMask = if (isPressed) {
            (modifierMask.toInt() or modifierBit.toInt()).toByte()
        } else {
            (modifierMask.toInt() and modifierBit.toInt().inv()).toByte()
        }
        if (modifierMask != original) {
            sendKeyboardReport()
        }
    }

    /**
     * Start high-frequency throttled mouse dispatch loop (~83Hz / 12ms interval).
     * Accumulates all touch drag deltas and dispatches at most one mouse report per tick.
     * Prevents Bluetooth L2CAP queue backlog (eliminates the multi-second mouse delay!).
     */
    @SuppressLint("MissingPermission")
    fun startMouseDispatchLoop() {
        if (mouseDispatchJob?.isActive == true) return
        mouseDispatchJob?.cancel()
        mouseDispatchJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(12) // ~83 Hz, optimal for Bluetooth HID standard polling interval
                val device = _connectedDevice.value
                val proxy = hidDevice
                if (device == null || proxy == null || !_isRegistered.value) continue

                val dx = pendingMouseDx.getAndSet(0)
                val dy = pendingMouseDy.getAndSet(0)
                val wheel = pendingMouseWheel.getAndSet(0)

                if (dx != 0 || dy != 0 || wheel != 0) {
                    val stepDx = dx.coerceIn(-125, 125)
                    val stepDy = dy.coerceIn(-125, 125)
                    val stepWheel = wheel.coerceIn(-125, 125)
                    val remX = dx - stepDx
                    val remY = dy - stepDy
                    val remWheel = wheel - stepWheel
                    if (remX != 0) pendingMouseDx.addAndGet(remX)
                    if (remY != 0) pendingMouseDy.addAndGet(remY)
                    if (remWheel != 0) pendingMouseWheel.addAndGet(remWheel)

                    val report = byteArrayOf(
                        mouseButtonsMask,
                        stepDx.toByte(),
                        stepDy.toByte(),
                        stepWheel.toByte()
                    )

                    try {
                        proxy.sendReport(device, REPORT_ID_MOUSE.toInt(), report)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending throttled mouse report", e)
                    }
                }
            }
        }
    }

    /**
     * Accumulates relative mouse movement.
     * Thread-safely buffered and dispatched at ~83Hz to eliminate input lag.
     */
    fun sendMouseMove(dx: Float, dy: Float, sensitivity: Float, invertY: Boolean = false) {
        val ySign = if (invertY) -1f else 1f
        val deltaX = (dx * sensitivity).roundToInt()
        val deltaY = (dy * sensitivity * ySign).roundToInt()

        if (deltaX == 0 && deltaY == 0) return

        pendingMouseDx.addAndGet(deltaX)
        pendingMouseDy.addAndGet(deltaY)

        // Fail-safe: ensure mouse dispatch loop is running
        if (mouseDispatchJob?.isActive != true && _connectionState.value == HidConnectionState.CONNECTED) {
            startMouseDispatchLoop()
        }
    }

    /**
     * Sends mouse wheel scroll (e.g. for cycling hotbar slots in Minecraft).
     */
    fun sendMouseWheel(scroll: Int) {
        if (scroll == 0) return
        pendingMouseWheel.addAndGet(scroll)
        if (mouseDispatchJob?.isActive != true && _connectionState.value == HidConnectionState.CONNECTED) {
            startMouseDispatchLoop()
        }
    }

    /**
     * Set mouse button state (Left Click = Attack, Right Click = Use/Place, Middle = Pick Block).
     * Dispatches immediately so button clicks have zero latency.
     */
    @SuppressLint("MissingPermission")
    fun setMouseButton(buttonBit: Byte, isPressed: Boolean) {
        val original = mouseButtonsMask
        mouseButtonsMask = if (isPressed) {
            (mouseButtonsMask.toInt() or buttonBit.toInt()).toByte()
        } else {
            (mouseButtonsMask.toInt() and buttonBit.toInt().inv()).toByte()
        }

        _activeMouseButtons.value = mouseButtonsMask

        if (mouseButtonsMask != original) {
            val device = _connectedDevice.value ?: return
            val proxy = hidDevice ?: return
            if (!_isRegistered.value) return

            // Mouse button changes are sent immediately with zero movement so clicks register instantly
            val report = byteArrayOf(mouseButtonsMask, 0, 0, 0)
            try {
                proxy.sendReport(device, REPORT_ID_MOUSE.toInt(), report)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending mouse button report", e)
            }
        }
    }

    /**
     * Safely releases every held keyboard key, modifier, and mouse button.
     * Prevents stuck keys when the user lifts fingers, leaves controller mode, or app pauses.
     */
    @SuppressLint("MissingPermission")
    fun releaseAllHeldInputs() {
        heldKeys.clear()
        modifierMask = 0
        mouseButtonsMask = 0
        pendingMouseDx.set(0)
        pendingMouseDy.set(0)
        pendingMouseWheel.set(0)

        _activeKeysList.value = emptySet()
        _activeModifiers.value = 0
        _activeMouseButtons.value = 0

        val device = _connectedDevice.value ?: return
        val proxy = hidDevice ?: return
        if (!_isRegistered.value) return

        try {
            // Empty keyboard report
            proxy.sendReport(device, REPORT_ID_KEYBOARD.toInt(), ByteArray(8))
            // Empty mouse report
            proxy.sendReport(device, REPORT_ID_MOUSE.toInt(), ByteArray(4))
        } catch (e: Exception) {
            Log.e(TAG, "Error sending release-all reports", e)
        }
    }

    /**
     * Unregisters the application and closes profile proxy on destroy.
     */
    @SuppressLint("MissingPermission")
    fun close() {
        mouseDispatchJob?.cancel()
        releaseAllHeldInputs()
        val proxy = hidDevice
        if (proxy != null) {
            try {
                if (_isRegistered.value) {
                    proxy.unregisterApp()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering app", e)
            }
            try {
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, proxy)
            } catch (e: Exception) {
                Log.e(TAG, "Error closing profile proxy", e)
            }
            hidDevice = null
        }
        _isRegistered.value = false
        _connectedDevice.value = null
    }
}
