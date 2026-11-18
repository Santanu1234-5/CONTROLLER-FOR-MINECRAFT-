package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ConnectionMode
import com.example.model.FreeFireKeyBindings
import com.example.model.GameProfile
import com.example.model.HandcamClawPreset
import com.example.model.HudCustomization
import com.example.model.KeyBindings
import com.example.model.PresetType

/**
 * Manages persistent preferences for per-game sensitivity, Handcam HUD layout,
 * presets, and key mappings.
 */
class ControllerPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minecraft_bt_controller_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SENSITIVITY = "look_sensitivity"
        private const val KEY_FF_SENSITIVITY = "ff_look_sensitivity"
        private const val KEY_OPACITY = "control_opacity"
        private const val KEY_BUTTON_SCALE = "button_scale"
        private const val KEY_INVERT_Y = "invert_y"
        private const val KEY_FF_INVERT_Y = "ff_invert_y"
        private const val KEY_TOGGLE_SNEAK = "toggle_sneak"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_TRACKPAD_TAP_TO_CLICK = "trackpad_tap_to_click"
        private const val KEY_TRACKPAD_HOLD_LEFT_CLICK = "trackpad_hold_left_click"
        private const val KEY_FF_TRACKPAD_HOLD_LEFT_CLICK = "ff_trackpad_hold_left_click"
        private const val KEY_PRESET = "active_preset"
        private const val KEY_GAME_PROFILE = "active_game_profile"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_CONNECTION_MODE = "connection_mode"
        private const val KEY_USB_TARGET_IP = "usb_target_ip"
        private const val KEY_USB_PAIRING_CODE = "usb_pairing_code"
        private const val KEY_AUTO_DETECT_PC_IP = "auto_detect_pc_ip"

        // Minecraft Keybinding keys
        private const val KEY_BIND_FORWARD = "bind_forward"
        private const val KEY_BIND_BACKWARD = "bind_backward"
        private const val KEY_BIND_LEFT = "bind_left"
        private const val KEY_BIND_RIGHT = "bind_right"
        private const val KEY_BIND_JUMP = "bind_jump"
        private const val KEY_BIND_INVENTORY = "bind_inventory"
        private const val KEY_BIND_MENU = "bind_menu"
        private const val KEY_BIND_DROP = "bind_drop"
        private const val KEY_BIND_PERSPECTIVE = "bind_perspective"

        // Free Fire India Keybinding keys
        private const val KEY_FF_CROUCH = "ff_bind_crouch"
        private const val KEY_FF_PRONE = "ff_bind_prone"
        private const val KEY_FF_RELOAD = "ff_bind_reload"
        private const val KEY_FF_LOOT = "ff_bind_loot"
        private const val KEY_FF_GLOO_WALL = "ff_bind_gloo_wall"
        private const val KEY_FF_GRENADE = "ff_bind_grenade"
        private const val KEY_FF_MEDKIT = "ff_bind_medkit"
        private const val KEY_FF_SKILL = "ff_bind_skill"
        private const val KEY_FF_QUICK_SWITCH = "ff_bind_quick_switch"
        private const val KEY_FF_BACKPACK = "ff_bind_backpack"
        private const val KEY_FF_MAP = "ff_bind_map"
        private const val KEY_FF_PING = "ff_bind_ping"
        private const val KEY_FF_EMOTE = "ff_bind_emote"
        private const val KEY_FF_CURSOR = "ff_bind_cursor"
        private const val KEY_FF_JUMP = "ff_bind_jump"
        private const val KEY_FF_MENU = "ff_bind_menu"
    }

    var sensitivity: Float
        get() = prefs.getFloat(KEY_SENSITIVITY, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SENSITIVITY, value).apply()

    var freeFireSensitivity: Float
        get() = prefs.getFloat(KEY_FF_SENSITIVITY, 1.35f)
        set(value) = prefs.edit().putFloat(KEY_FF_SENSITIVITY, value).apply()

    fun getActiveSensitivity(game: GameProfile): Float =
        if (game == GameProfile.FREE_FIRE_INDIA) freeFireSensitivity else sensitivity

    fun setActiveSensitivity(game: GameProfile, value: Float) {
        if (game == GameProfile.FREE_FIRE_INDIA) freeFireSensitivity = value else sensitivity = value
    }

    var opacity: Float
        get() = prefs.getFloat(KEY_OPACITY, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_OPACITY, value).apply()

    var buttonScale: Float
        get() = prefs.getFloat(KEY_BUTTON_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_BUTTON_SCALE, value).apply()

    var invertY: Boolean
        get() = prefs.getBoolean(KEY_INVERT_Y, false)
        set(value) = prefs.edit().putBoolean(KEY_INVERT_Y, value).apply()

    var freeFireInvertY: Boolean
        get() = prefs.getBoolean(KEY_FF_INVERT_Y, false)
        set(value) = prefs.edit().putBoolean(KEY_FF_INVERT_Y, value).apply()

    fun getActiveInvertY(game: GameProfile): Boolean =
        if (game == GameProfile.FREE_FIRE_INDIA) freeFireInvertY else invertY

    fun setActiveInvertY(game: GameProfile, value: Boolean) {
        if (game == GameProfile.FREE_FIRE_INDIA) freeFireInvertY = value else invertY = value
    }

    var toggleSneak: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_SNEAK, false)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_SNEAK, value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var trackpadTapToClick: Boolean
        get() = prefs.getBoolean(KEY_TRACKPAD_TAP_TO_CLICK, true)
        set(value) = prefs.edit().putBoolean(KEY_TRACKPAD_TAP_TO_CLICK, value).apply()

    var trackpadHoldLeftClick: Boolean
        get() = prefs.getBoolean(KEY_TRACKPAD_HOLD_LEFT_CLICK, true)
        set(value) = prefs.edit().putBoolean(KEY_TRACKPAD_HOLD_LEFT_CLICK, value).apply()

    // In Battle Royale (Free Fire India), pure aiming on the trackpad defaults to OFF hold-fire
    // so you don't waste ammo while turning 360°, while Minecraft keeps Hold-to-Mine ON by default.
    var freeFireTrackpadHoldLeftClick: Boolean
        get() = prefs.getBoolean(KEY_FF_TRACKPAD_HOLD_LEFT_CLICK, false)
        set(value) = prefs.edit().putBoolean(KEY_FF_TRACKPAD_HOLD_LEFT_CLICK, value).apply()

    fun getActiveTrackpadHoldLeftClick(game: GameProfile): Boolean =
        if (game == GameProfile.FREE_FIRE_INDIA) freeFireTrackpadHoldLeftClick else trackpadHoldLeftClick

    fun setActiveTrackpadHoldLeftClick(game: GameProfile, value: Boolean) {
        if (game == GameProfile.FREE_FIRE_INDIA) {
            freeFireTrackpadHoldLeftClick = value
        } else {
            trackpadHoldLeftClick = value
        }
    }

    var preset: PresetType
        get() {
            val name = prefs.getString(KEY_PRESET, PresetType.EAGLERCRAFT_BROWSER.name)
            return try {
                PresetType.valueOf(name ?: PresetType.EAGLERCRAFT_BROWSER.name)
            } catch (e: Exception) {
                PresetType.EAGLERCRAFT_BROWSER
            }
        }
        set(value) = prefs.edit().putString(KEY_PRESET, value.name).apply()

    var gameProfile: GameProfile
        get() {
            val name = prefs.getString(KEY_GAME_PROFILE, GameProfile.MINECRAFT.name)
            return try {
                GameProfile.valueOf(name ?: GameProfile.MINECRAFT.name)
            } catch (e: Exception) {
                GameProfile.MINECRAFT
            }
        }
        set(value) = prefs.edit().putString(KEY_GAME_PROFILE, value.name).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var connectionMode: ConnectionMode
        get() {
            val name = prefs.getString(KEY_CONNECTION_MODE, ConnectionMode.BLUETOOTH.name)
            return try {
                ConnectionMode.valueOf(name ?: ConnectionMode.BLUETOOTH.name)
            } catch (e: Exception) {
                ConnectionMode.BLUETOOTH
            }
        }
        set(value) = prefs.edit().putString(KEY_CONNECTION_MODE, value.name).apply()

    var usbTargetIp: String
        get() = prefs.getString(KEY_USB_TARGET_IP, "192.168.42.129") ?: "192.168.42.129"
        set(value) = prefs.edit().putString(KEY_USB_TARGET_IP, value).apply()

    var usbPairingCode: String
        get() = prefs.getString(KEY_USB_PAIRING_CODE, "4291") ?: "4291"
        set(value) = prefs.edit().putString(KEY_USB_PAIRING_CODE, value).apply()

    var autoDetectPcIp: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DETECT_PC_IP, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DETECT_PC_IP, value).apply()

    fun getKeyBindings(): KeyBindings {
        val defaults = KeyBindings()
        return KeyBindings(
            forward = prefs.getInt(KEY_BIND_FORWARD, defaults.forward.toInt()).toByte(),
            backward = prefs.getInt(KEY_BIND_BACKWARD, defaults.backward.toInt()).toByte(),
            left = prefs.getInt(KEY_BIND_LEFT, defaults.left.toInt()).toByte(),
            right = prefs.getInt(KEY_BIND_RIGHT, defaults.right.toInt()).toByte(),
            jump = prefs.getInt(KEY_BIND_JUMP, defaults.jump.toInt()).toByte(),
            inventory = prefs.getInt(KEY_BIND_INVENTORY, defaults.inventory.toInt()).toByte(),
            menu = prefs.getInt(KEY_BIND_MENU, defaults.menu.toInt()).toByte(),
            drop = prefs.getInt(KEY_BIND_DROP, defaults.drop.toInt()).toByte(),
            perspective = prefs.getInt(KEY_BIND_PERSPECTIVE, defaults.perspective.toInt()).toByte()
        )
    }

    fun saveKeyBindings(bindings: KeyBindings) {
        prefs.edit()
            .putInt(KEY_BIND_FORWARD, bindings.forward.toInt())
            .putInt(KEY_BIND_BACKWARD, bindings.backward.toInt())
            .putInt(KEY_BIND_LEFT, bindings.left.toInt())
            .putInt(KEY_BIND_RIGHT, bindings.right.toInt())
            .putInt(KEY_BIND_JUMP, bindings.jump.toInt())
            .putInt(KEY_BIND_INVENTORY, bindings.inventory.toInt())
            .putInt(KEY_BIND_MENU, bindings.menu.toInt())
            .putInt(KEY_BIND_DROP, bindings.drop.toInt())
            .putInt(KEY_BIND_PERSPECTIVE, bindings.perspective.toInt())
            .apply()
    }

    fun getFreeFireKeyBindings(): FreeFireKeyBindings {
        val defaults = FreeFireKeyBindings()
        return FreeFireKeyBindings(
            crouch = prefs.getInt(KEY_FF_CROUCH, defaults.crouch.toInt()).toByte(),
            prone = prefs.getInt(KEY_FF_PRONE, defaults.prone.toInt()).toByte(),
            reload = prefs.getInt(KEY_FF_RELOAD, defaults.reload.toInt()).toByte(),
            loot = prefs.getInt(KEY_FF_LOOT, defaults.loot.toInt()).toByte(),
            glooWall = prefs.getInt(KEY_FF_GLOO_WALL, defaults.glooWall.toInt()).toByte(),
            grenade = prefs.getInt(KEY_FF_GRENADE, defaults.grenade.toInt()).toByte(),
            medkit = prefs.getInt(KEY_FF_MEDKIT, defaults.medkit.toInt()).toByte(),
            skill = prefs.getInt(KEY_FF_SKILL, defaults.skill.toInt()).toByte(),
            quickSwitch = prefs.getInt(KEY_FF_QUICK_SWITCH, defaults.quickSwitch.toInt()).toByte(),
            backpack = prefs.getInt(KEY_FF_BACKPACK, defaults.backpack.toInt()).toByte(),
            map = prefs.getInt(KEY_FF_MAP, defaults.map.toInt()).toByte(),
            ping = prefs.getInt(KEY_FF_PING, defaults.ping.toInt()).toByte(),
            emote = prefs.getInt(KEY_FF_EMOTE, defaults.emote.toInt()).toByte(),
            cursorToggle = prefs.getInt(KEY_FF_CURSOR, defaults.cursorToggle.toInt()).toByte(),
            jump = prefs.getInt(KEY_FF_JUMP, defaults.jump.toInt()).toByte(),
            menu = prefs.getInt(KEY_FF_MENU, defaults.menu.toInt()).toByte()
        )
    }

    fun saveFreeFireKeyBindings(bindings: FreeFireKeyBindings) {
        prefs.edit()
            .putInt(KEY_FF_CROUCH, bindings.crouch.toInt())
            .putInt(KEY_FF_PRONE, bindings.prone.toInt())
            .putInt(KEY_FF_RELOAD, bindings.reload.toInt())
            .putInt(KEY_FF_LOOT, bindings.loot.toInt())
            .putInt(KEY_FF_GLOO_WALL, bindings.glooWall.toInt())
            .putInt(KEY_FF_GRENADE, bindings.grenade.toInt())
            .putInt(KEY_FF_MEDKIT, bindings.medkit.toInt())
            .putInt(KEY_FF_SKILL, bindings.skill.toInt())
            .putInt(KEY_FF_QUICK_SWITCH, bindings.quickSwitch.toInt())
            .putInt(KEY_FF_BACKPACK, bindings.backpack.toInt())
            .putInt(KEY_FF_MAP, bindings.map.toInt())
            .putInt(KEY_FF_PING, bindings.ping.toInt())
            .putInt(KEY_FF_EMOTE, bindings.emote.toInt())
            .putInt(KEY_FF_CURSOR, bindings.cursorToggle.toInt())
            .putInt(KEY_FF_JUMP, bindings.jump.toInt())
            .putInt(KEY_FF_MENU, bindings.menu.toInt())
            .apply()
    }

    fun defaultHudForGame(game: GameProfile): HudCustomization {
        return if (game == GameProfile.FREE_FIRE_INDIA) {
            HudCustomization(
                clawPreset = HandcamClawPreset.THREE_FINGER,
                buttonScale = 1.0f,
                joystickScale = 1.0f,
                handcamGlow = true,
                cleanHandcamBar = false,
                showLeftFireButton = true,
                showExtraTacticalButtons = true
            )
        } else {
            HudCustomization(
                clawPreset = HandcamClawPreset.TWO_FINGER,
                buttonScale = 1.0f,
                joystickScale = 1.0f,
                handcamGlow = true,
                cleanHandcamBar = false,
                showLeftFireButton = false,
                showExtraTacticalButtons = true
            )
        }
    }

    fun getHudCustomization(game: GameProfile): HudCustomization {
        val prefix = if (game == GameProfile.FREE_FIRE_INDIA) "hud_ff_" else "hud_mc_"
        val defaults = defaultHudForGame(game)
        val presetName = prefs.getString("${prefix}claw_preset", defaults.clawPreset.name)
        val clawPreset = try {
            HandcamClawPreset.valueOf(presetName ?: defaults.clawPreset.name)
        } catch (e: Exception) {
            defaults.clawPreset
        }

        return HudCustomization(
            clawPreset = clawPreset,
            buttonScale = prefs.getFloat("${prefix}btn_scale", defaults.buttonScale),
            joystickScale = prefs.getFloat("${prefix}joy_scale", defaults.joystickScale),
            handcamGlow = prefs.getBoolean("${prefix}glow", defaults.handcamGlow),
            cleanHandcamBar = prefs.getBoolean("${prefix}clean_bar", defaults.cleanHandcamBar),
            showLeftFireButton = prefs.getBoolean("${prefix}left_fire", defaults.showLeftFireButton),
            showExtraTacticalButtons = prefs.getBoolean("${prefix}extra_btns", defaults.showExtraTacticalButtons),
            leftZoneOffsetX = prefs.getFloat("${prefix}left_x", 0f),
            leftZoneOffsetY = prefs.getFloat("${prefix}left_y", 0f),
            rightZoneOffsetX = prefs.getFloat("${prefix}right_x", 0f),
            rightZoneOffsetY = prefs.getFloat("${prefix}right_y", 0f),
            leftFireOffsetX = prefs.getFloat("${prefix}lfire_x", 0f),
            leftFireOffsetY = prefs.getFloat("${prefix}lfire_y", 0f)
        )
    }

    fun saveHudCustomization(game: GameProfile, config: HudCustomization) {
        val prefix = if (game == GameProfile.FREE_FIRE_INDIA) "hud_ff_" else "hud_mc_"
        prefs.edit()
            .putString("${prefix}claw_preset", config.clawPreset.name)
            .putFloat("${prefix}btn_scale", config.buttonScale)
            .putFloat("${prefix}joy_scale", config.joystickScale)
            .putBoolean("${prefix}glow", config.handcamGlow)
            .putBoolean("${prefix}clean_bar", config.cleanHandcamBar)
            .putBoolean("${prefix}left_fire", config.showLeftFireButton)
            .putBoolean("${prefix}extra_btns", config.showExtraTacticalButtons)
            .putFloat("${prefix}left_x", config.leftZoneOffsetX)
            .putFloat("${prefix}left_y", config.leftZoneOffsetY)
            .putFloat("${prefix}right_x", config.rightZoneOffsetX)
            .putFloat("${prefix}right_y", config.rightZoneOffsetY)
            .putFloat("${prefix}lfire_x", config.leftFireOffsetX)
            .putFloat("${prefix}lfire_y", config.leftFireOffsetY)
            .apply()
    }

    fun resetHudCustomization(game: GameProfile): HudCustomization {
        val defaults = defaultHudForGame(game)
        saveHudCustomization(game, defaults)
        return defaults
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
