package com.example.model

/**
 * Truthful, distinct connection states for Bluetooth HID device lifecycle.
 */
enum class HidConnectionState {
    NO_BLUETOOTH_HARDWARE,
    BLUETOOTH_OFF,
    PERMISSION_NEEDED,
    HID_UNSUPPORTED,
    INITIALIZING,
    REGISTERING_APP,
    READY_DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    REGISTRATION_FAILED
}

/**
 * Supported Game Controller Layouts within the app.
 */
enum class GameProfile(val displayName: String, val shortName: String, val badgeText: String, val description: String) {
    MINECRAFT(
        displayName = "Minecraft",
        shortName = "Minecraft",
        badgeText = "MINECRAFT",
        description = "Block-building layout with WASD, Attack/Use, Sneak/Sprint, Hotbar 1-9, and E/Q/F3/F5 controls."
    ),
    FREE_FIRE_INDIA(
        displayName = "Free Fire India",
        shortName = "FF India",
        badgeText = "FF INDIA",
        description = "Battle Royale layout for BlueStacks/LDPlayer/PC with Fire, Scope, Reload, Gloo Wall, Medkit, Crouch, Prone, Skill & Quick Weapon Switch."
    )
}

/**
 * Controller Presets for Minecraft.
 */
enum class PresetType(val displayName: String, val note: String) {
    EAGLERCRAFT_BROWSER(
        displayName = "Eaglercraft / Browser",
        note = "Optimized for Minecraft in a web browser. Note: Click inside the browser window first so it captures keyboard and mouse focus on your PC."
    ),
    STANDARD_JAVA_BEDROCK(
        displayName = "Standard Java / Bedrock",
        note = "Default standard PC Minecraft bindings (WASD, Space jump, Shift sneak, E inventory, Esc pause, Left/Right click)."
    ),
    CREATIVE_BUILDER(
        displayName = "Creative Builder",
        note = "Optimized for building: Space to ascend, Shift to descend, rapid block place & break, quick hotbar access."
    ),
    MSI_BLUESTACKS_EMULATOR(
        displayName = "MSI / BlueStacks",
        note = "For Minecraft inside MSI App Player or BlueStacks: Run receiver as Administrator, click inside the emulator window, turn ON 'Full Keyboard Gameplay' in Minecraft Settings (or map keys + F1 Aim Lock)."
    )
}

/**
 * Configurable keyboard mapping for Minecraft actions.
 */
data class KeyBindings(
    val forward: Byte = UsbHidKeyCodes.KEY_W,
    val backward: Byte = UsbHidKeyCodes.KEY_S,
    val left: Byte = UsbHidKeyCodes.KEY_A,
    val right: Byte = UsbHidKeyCodes.KEY_D,
    val jump: Byte = UsbHidKeyCodes.KEY_SPACE,
    val sneakModifier: Byte = UsbHidKeyCodes.MOD_LEFT_SHIFT,
    val inventory: Byte = UsbHidKeyCodes.KEY_E,
    val menu: Byte = UsbHidKeyCodes.KEY_ESCAPE,
    val drop: Byte = UsbHidKeyCodes.KEY_Q,
    val perspective: Byte = UsbHidKeyCodes.KEY_F5,
    val playerList: Byte = UsbHidKeyCodes.KEY_TAB,
    val debug: Byte = UsbHidKeyCodes.KEY_F3
)

/**
 * Configurable keyboard mapping for Free Fire India PC / Emulator controls.
 */
data class FreeFireKeyBindings(
    val forward: Byte = UsbHidKeyCodes.KEY_W,
    val backward: Byte = UsbHidKeyCodes.KEY_S,
    val left: Byte = UsbHidKeyCodes.KEY_A,
    val right: Byte = UsbHidKeyCodes.KEY_D,
    val jump: Byte = UsbHidKeyCodes.KEY_SPACE,
    val crouch: Byte = UsbHidKeyCodes.KEY_C,
    val prone: Byte = UsbHidKeyCodes.KEY_Z,
    val reload: Byte = UsbHidKeyCodes.KEY_R,
    val loot: Byte = UsbHidKeyCodes.KEY_F,
    val glooWall: Byte = UsbHidKeyCodes.KEY_G,
    val grenade: Byte = UsbHidKeyCodes.KEY_5,
    val medkit: Byte = UsbHidKeyCodes.KEY_H,
    val skill: Byte = UsbHidKeyCodes.KEY_E,
    val quickSwitch: Byte = UsbHidKeyCodes.KEY_Q,
    val backpack: Byte = UsbHidKeyCodes.KEY_TAB,
    val map: Byte = UsbHidKeyCodes.KEY_M,
    val ping: Byte = UsbHidKeyCodes.KEY_V,
    val emote: Byte = UsbHidKeyCodes.KEY_B,
    val cursorToggle: Byte = UsbHidKeyCodes.KEY_F1,
    val menu: Byte = UsbHidKeyCodes.KEY_ESCAPE
)

/**
 * Handcam grip / finger layout presets.
 */
enum class HandcamClawPreset(val displayName: String, val subtitle: String) {
    TWO_FINGER("2-Finger", "Classic dual-thumb comfort grip"),
    THREE_FINGER("3-Finger Claw", "2nd Left Fire ready at top-left for index finger"),
    FOUR_FINGER("4-Finger Claw", "Full claw: Top-left Fire + Top-right Scope/Gloo access")
}

/**
 * Per-game Handcam & HUD layout customization config.
 */
data class HudCustomization(
    val clawPreset: HandcamClawPreset = HandcamClawPreset.THREE_FINGER,
    val buttonScale: Float = 1.0f,
    val joystickScale: Float = 1.0f,
    val handcamGlow: Boolean = true,
    val cleanHandcamBar: Boolean = false,
    val showLeftFireButton: Boolean = true,
    val showExtraTacticalButtons: Boolean = true,
    val leftZoneOffsetX: Float = 0f,
    val leftZoneOffsetY: Float = 0f,
    val rightZoneOffsetX: Float = 0f,
    val rightZoneOffsetY: Float = 0f,
    val leftFireOffsetX: Float = 0f,
    val leftFireOffsetY: Float = 0f
)

/**
 * Information about a bonded Bluetooth host (PC, Mac, Laptop).
 */
data class BondedDeviceInfo(
    val name: String,
    val address: String,
    val isConnected: Boolean = false
)
