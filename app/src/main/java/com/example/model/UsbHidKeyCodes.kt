package com.example.model

/**
 * Standard USB HID Usage IDs for keyboard keys.
 * Note: These are USB HID usage codes, NOT Android KeyEvent codes.
 */
object UsbHidKeyCodes {
    // Modifier bitmasks for Keyboard Report Byte 0
    const val MOD_LEFT_CTRL: Byte = 0x01
    const val MOD_LEFT_SHIFT: Byte = 0x02
    const val MOD_LEFT_ALT: Byte = 0x04
    const val MOD_LEFT_GUI: Byte = 0x08
    const val MOD_RIGHT_CTRL: Byte = 0x10
    const val MOD_RIGHT_SHIFT: Byte = 0x20
    const val MOD_RIGHT_ALT: Byte = 0x40
    const val MOD_RIGHT_GUI: Byte = 0x80.toByte()

    // Letter keys (0x04 - 0x1D)
    const val KEY_A: Byte = 0x04
    const val KEY_B: Byte = 0x05
    const val KEY_C: Byte = 0x06
    const val KEY_D: Byte = 0x07
    const val KEY_E: Byte = 0x08
    const val KEY_F: Byte = 0x09
    const val KEY_G: Byte = 0x0A
    const val KEY_H: Byte = 0x0B
    const val KEY_I: Byte = 0x0C
    const val KEY_J: Byte = 0x0D
    const val KEY_K: Byte = 0x0E
    const val KEY_L: Byte = 0x0F
    const val KEY_M: Byte = 0x10
    const val KEY_N: Byte = 0x11
    const val KEY_O: Byte = 0x12
    const val KEY_P: Byte = 0x13
    const val KEY_Q: Byte = 0x14
    const val KEY_R: Byte = 0x15
    const val KEY_S: Byte = 0x16
    const val KEY_T: Byte = 0x17
    const val KEY_U: Byte = 0x18
    const val KEY_V: Byte = 0x19
    const val KEY_W: Byte = 0x1A
    const val KEY_X: Byte = 0x1B
    const val KEY_Y: Byte = 0x1C
    const val KEY_Z: Byte = 0x1D

    // Number keys (1 - 0)
    const val KEY_1: Byte = 0x1E
    const val KEY_2: Byte = 0x1F
    const val KEY_3: Byte = 0x20
    const val KEY_4: Byte = 0x21
    const val KEY_5: Byte = 0x22
    const val KEY_6: Byte = 0x23
    const val KEY_7: Byte = 0x24
    const val KEY_8: Byte = 0x25
    const val KEY_9: Byte = 0x26
    const val KEY_0: Byte = 0x27

    // Control & Navigation keys
    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESCAPE: Byte = 0x29
    const val KEY_BACKSPACE: Byte = 0x2A
    const val KEY_TAB: Byte = 0x2B
    const val KEY_SPACE: Byte = 0x2C

    // Function keys
    const val KEY_F1: Byte = 0x3A
    const val KEY_F2: Byte = 0x3B
    const val KEY_F3: Byte = 0x3C // Debug screen in Minecraft
    const val KEY_F4: Byte = 0x3D
    const val KEY_F5: Byte = 0x3E // Camera perspective toggle in Minecraft
    const val KEY_F11: Byte = 0x44 // Fullscreen

    // Mouse buttons bitmask for Mouse Report Byte 0
    const val MOUSE_BUTTON_LEFT: Byte = 0x01
    const val MOUSE_BUTTON_RIGHT: Byte = 0x02
    const val MOUSE_BUTTON_MIDDLE: Byte = 0x04

    /**
     * Map USB HID keycode to a human-readable display label.
     */
    fun getKeyLabel(keyCode: Byte, isModifier: Boolean = false): String {
        if (isModifier) {
            return when (keyCode) {
                MOD_LEFT_SHIFT -> "L-Shift"
                MOD_LEFT_CTRL -> "L-Ctrl"
                MOD_LEFT_ALT -> "L-Alt"
                else -> "Mod"
            }
        }
        return when (keyCode) {
            KEY_W -> "W"
            KEY_A -> "A"
            KEY_S -> "S"
            KEY_D -> "D"
            KEY_E -> "E"
            KEY_Q -> "Q"
            KEY_SPACE -> "Space"
            KEY_ESCAPE -> "Esc"
            KEY_BACKSPACE -> "Backspace"
            KEY_TAB -> "Tab"
            KEY_F1 -> "F1"
            KEY_F2 -> "F2"
            KEY_F3 -> "F3"
            KEY_F4 -> "F4"
            KEY_F5 -> "F5"
            KEY_F11 -> "F11"
            KEY_1 -> "1"
            KEY_2 -> "2"
            KEY_3 -> "3"
            KEY_4 -> "4"
            KEY_5 -> "5"
            KEY_6 -> "6"
            KEY_7 -> "7"
            KEY_8 -> "8"
            KEY_9 -> "9"
            KEY_0 -> "0"
            KEY_ENTER -> "Enter"
            KEY_C -> "C"
            KEY_F -> "F"
            KEY_G -> "G"
            KEY_H -> "H"
            KEY_M -> "M"
            KEY_R -> "R"
            KEY_T -> "T"
            KEY_V -> "V"
            KEY_X -> "X"
            KEY_B -> "B"
            KEY_Z -> "Z"
            else -> "0x%02X".format(keyCode)
        }
    }
}
