package com.example.model

/**
 * Supported connection methods between phone and PC.
 */
enum class ConnectionMode(val displayName: String, val shortName: String) {
    BLUETOOTH(
        displayName = "Bluetooth HID (Direct, No PC Software)",
        shortName = "Bluetooth"
    ),
    USB(
        displayName = "USB Cable (USB Tethering + PC Receiver)",
        shortName = "USB Cable"
    )
}
