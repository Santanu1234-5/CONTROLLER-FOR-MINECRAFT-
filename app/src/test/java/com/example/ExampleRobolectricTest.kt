package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ControllerPreferences
import com.example.hid.HidDeviceManager
import com.example.model.ConnectionMode
import com.example.model.FreeFireKeyBindings
import com.example.model.GameProfile
import com.example.model.HandcamClawPreset
import com.example.model.HudCustomization
import com.example.model.KeyBindings
import com.example.model.PresetType
import com.example.model.UsbHidKeyCodes
import com.example.usb.UsbTetherManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Minecraft BT Controller", appName)
    }

    @Test
    fun `usb hid keycodes are correct standards`() {
        // USB HID Standard usages
        assertEquals(0x1A.toByte(), UsbHidKeyCodes.KEY_W)
        assertEquals(0x04.toByte(), UsbHidKeyCodes.KEY_A)
        assertEquals(0x16.toByte(), UsbHidKeyCodes.KEY_S)
        assertEquals(0x07.toByte(), UsbHidKeyCodes.KEY_D)
        assertEquals(0x08.toByte(), UsbHidKeyCodes.KEY_E)
        assertEquals(0x2C.toByte(), UsbHidKeyCodes.KEY_SPACE)
        assertEquals(0x29.toByte(), UsbHidKeyCodes.KEY_ESCAPE)
        assertEquals(0x02.toByte(), UsbHidKeyCodes.MOD_LEFT_SHIFT)
        assertEquals("Backspace", UsbHidKeyCodes.getKeyLabel(UsbHidKeyCodes.KEY_BACKSPACE))
        assertEquals("F2", UsbHidKeyCodes.getKeyLabel(UsbHidKeyCodes.KEY_F2))
        assertEquals("F4", UsbHidKeyCodes.getKeyLabel(UsbHidKeyCodes.KEY_F4))
        assertEquals("MSI / BlueStacks", PresetType.MSI_BLUESTACKS_EMULATOR.displayName)
    }

    @Test
    fun `controller preferences save and retrieve values`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ControllerPreferences(context)
        prefs.resetToDefaults()

        assertEquals(1.0f, prefs.sensitivity, 0.01f)
        prefs.sensitivity = 1.8f
        assertEquals(1.8f, prefs.sensitivity, 0.01f)

        assertEquals(PresetType.EAGLERCRAFT_BROWSER, prefs.preset)
        prefs.preset = PresetType.STANDARD_JAVA_BEDROCK
        assertEquals(PresetType.STANDARD_JAVA_BEDROCK, prefs.preset)

        assertEquals(ConnectionMode.BLUETOOTH, prefs.connectionMode)
        prefs.connectionMode = ConnectionMode.USB
        assertEquals(ConnectionMode.USB, prefs.connectionMode)

        val customBindings = KeyBindings(forward = UsbHidKeyCodes.KEY_Z)
        prefs.saveKeyBindings(customBindings)
        val loaded = prefs.getKeyBindings()
        assertEquals(UsbHidKeyCodes.KEY_Z, loaded.forward)
    }

    @Test
    fun `hid report descriptor is valid`() {
        val descriptor = HidDeviceManager.HID_REPORT_DESCRIPTOR
        assertNotNull(descriptor)
        assertTrue(descriptor.isNotEmpty())
        assertEquals(HidDeviceManager.REPORT_ID_KEYBOARD, 1.toByte())
        assertEquals(HidDeviceManager.REPORT_ID_MOUSE, 2.toByte())
    }

    @Test
    fun `usb tether manager initializes cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val usbManager = UsbTetherManager(context)
        assertNotNull(usbManager)
        assertEquals("4291", usbManager.pairingCode.value)
        usbManager.setPairingCode("9999")
        assertEquals("9999", usbManager.pairingCode.value)
    }

    @Test
    fun `manual pc ip and pin settings work and do not overwrite`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ControllerPreferences(context)
        val usbManager = UsbTetherManager(context)

        // Default auto-detect should be false per user preference
        assertEquals(false, prefs.autoDetectPcIp)

        // Set manual IP & code
        prefs.usbTargetIp = "192.168.1.100"
        prefs.usbPairingCode = "7788"
        assertEquals("192.168.1.100", prefs.usbTargetIp)
        assertEquals("7788", prefs.usbPairingCode)

        usbManager.setAutoDetectEnabled(false)
        assertEquals(false, usbManager.autoDetectEnabled.value)

        usbManager.setTargetPcIp("192.168.1.100")
        usbManager.setPairingCode("7788")
        assertEquals("192.168.1.100", usbManager.detectedPcIp.value)
        assertEquals("7788", usbManager.pairingCode.value)

        // checkTetheringInterface should NOT overwrite detectedPcIp when autoDetect is false
        usbManager.checkTetheringInterface()
        assertEquals("192.168.1.100", usbManager.detectedPcIp.value)
    }

    @Test
    fun `trackpad tap to click preference is enabled by default and saves correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ControllerPreferences(context)
        prefs.resetToDefaults()

        // Default should be true
        assertTrue(prefs.trackpadTapToClick)

        prefs.trackpadTapToClick = false
        assertEquals(false, prefs.trackpadTapToClick)

        prefs.trackpadTapToClick = true
        assertEquals(true, prefs.trackpadTapToClick)
    }

    @Test
    fun `trackpad hold left click preference is enabled by default and saves correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ControllerPreferences(context)
        prefs.resetToDefaults()

        // Default should be true for Minecraft and false for Free Fire India (360 BR aim)
        assertTrue(prefs.getActiveTrackpadHoldLeftClick(GameProfile.MINECRAFT))
        assertFalse(prefs.getActiveTrackpadHoldLeftClick(GameProfile.FREE_FIRE_INDIA))

        prefs.setActiveTrackpadHoldLeftClick(GameProfile.MINECRAFT, false)
        assertFalse(prefs.getActiveTrackpadHoldLeftClick(GameProfile.MINECRAFT))
        assertFalse(prefs.getActiveTrackpadHoldLeftClick(GameProfile.FREE_FIRE_INDIA))

        prefs.setActiveTrackpadHoldLeftClick(GameProfile.FREE_FIRE_INDIA, true)
        assertTrue(prefs.getActiveTrackpadHoldLeftClick(GameProfile.FREE_FIRE_INDIA))
        assertFalse(prefs.getActiveTrackpadHoldLeftClick(GameProfile.MINECRAFT))
    }

    @Test
    fun `game profiles have separate sensitivity invertY and extended free fire bindings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ControllerPreferences(context)
        prefs.resetToDefaults()

        assertEquals(GameProfile.MINECRAFT, prefs.gameProfile)
        assertEquals(1.0f, prefs.getActiveSensitivity(GameProfile.MINECRAFT), 0.01f)
        assertEquals(1.35f, prefs.getActiveSensitivity(GameProfile.FREE_FIRE_INDIA), 0.01f)

        prefs.setActiveSensitivity(GameProfile.FREE_FIRE_INDIA, 2.2f)
        prefs.setActiveInvertY(GameProfile.FREE_FIRE_INDIA, true)
        assertEquals(2.2f, prefs.getActiveSensitivity(GameProfile.FREE_FIRE_INDIA), 0.01f)
        assertEquals(1.0f, prefs.getActiveSensitivity(GameProfile.MINECRAFT), 0.01f)
        assertTrue(prefs.getActiveInvertY(GameProfile.FREE_FIRE_INDIA))
        assertFalse(prefs.getActiveInvertY(GameProfile.MINECRAFT))

        prefs.gameProfile = GameProfile.FREE_FIRE_INDIA
        assertEquals(GameProfile.FREE_FIRE_INDIA, prefs.gameProfile)

        val ffDefaults = prefs.getFreeFireKeyBindings()
        assertEquals(UsbHidKeyCodes.KEY_R, ffDefaults.reload)
        assertEquals(UsbHidKeyCodes.KEY_G, ffDefaults.glooWall)
        assertEquals(UsbHidKeyCodes.KEY_H, ffDefaults.medkit)
        assertEquals(UsbHidKeyCodes.KEY_5, ffDefaults.grenade)
        assertEquals(UsbHidKeyCodes.KEY_Q, ffDefaults.quickSwitch)
        assertEquals(UsbHidKeyCodes.KEY_V, ffDefaults.ping)
        assertEquals(UsbHidKeyCodes.KEY_B, ffDefaults.emote)
        assertEquals(UsbHidKeyCodes.KEY_SPACE, ffDefaults.jump)
        assertEquals(UsbHidKeyCodes.KEY_ESCAPE, ffDefaults.menu)

        val updatedFf = ffDefaults.copy(
            glooWall = UsbHidKeyCodes.KEY_V,
            quickSwitch = UsbHidKeyCodes.KEY_X,
            emote = UsbHidKeyCodes.KEY_T
        )
        prefs.saveFreeFireKeyBindings(updatedFf)
        val loadedFf = prefs.getFreeFireKeyBindings()
        assertEquals(UsbHidKeyCodes.KEY_V, loadedFf.glooWall)
        assertEquals(UsbHidKeyCodes.KEY_X, loadedFf.quickSwitch)
        assertEquals(UsbHidKeyCodes.KEY_T, loadedFf.emote)
        assertEquals("G", UsbHidKeyCodes.getKeyLabel(UsbHidKeyCodes.KEY_G))
        assertEquals("F1", UsbHidKeyCodes.getKeyLabel(UsbHidKeyCodes.KEY_F1))
    }

    @Test
    fun `handcam hud customization saves per game and resets cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ControllerPreferences(context)
        prefs.resetToDefaults()

        val mcDefaultHud = prefs.getHudCustomization(GameProfile.MINECRAFT)
        val ffDefaultHud = prefs.getHudCustomization(GameProfile.FREE_FIRE_INDIA)
        assertEquals(HandcamClawPreset.TWO_FINGER, mcDefaultHud.clawPreset)
        assertFalse(mcDefaultHud.showLeftFireButton)
        assertEquals(HandcamClawPreset.THREE_FINGER, ffDefaultHud.clawPreset)
        assertTrue(ffDefaultHud.showLeftFireButton)

        val customFfHud = HudCustomization(
            clawPreset = HandcamClawPreset.FOUR_FINGER,
            buttonScale = 1.25f,
            joystickScale = 1.15f,
            handcamGlow = true,
            cleanHandcamBar = true,
            showLeftFireButton = true,
            showExtraTacticalButtons = false,
            leftZoneOffsetX = 24f,
            leftZoneOffsetY = -12f,
            rightZoneOffsetX = -18f,
            rightZoneOffsetY = 14f,
            leftFireOffsetX = 30f,
            leftFireOffsetY = 10f
        )
        prefs.saveHudCustomization(GameProfile.FREE_FIRE_INDIA, customFfHud)

        val loadedFfHud = prefs.getHudCustomization(GameProfile.FREE_FIRE_INDIA)
        assertEquals(HandcamClawPreset.FOUR_FINGER, loadedFfHud.clawPreset)
        assertEquals(1.25f, loadedFfHud.buttonScale, 0.01f)
        assertEquals(1.15f, loadedFfHud.joystickScale, 0.01f)
        assertTrue(loadedFfHud.cleanHandcamBar)
        assertFalse(loadedFfHud.showExtraTacticalButtons)
        assertEquals(24f, loadedFfHud.leftZoneOffsetX, 0.01f)
        assertEquals(30f, loadedFfHud.leftFireOffsetX, 0.01f)

        // Minecraft HUD must remain untouched
        val mcStillDefault = prefs.getHudCustomization(GameProfile.MINECRAFT)
        assertEquals(HandcamClawPreset.TWO_FINGER, mcStillDefault.clawPreset)
        assertEquals(1.0f, mcStillDefault.buttonScale, 0.01f)

        // Reset Free Fire HUD back to defaults
        val resetFf = prefs.resetHudCustomization(GameProfile.FREE_FIRE_INDIA)
        assertEquals(HandcamClawPreset.THREE_FINGER, resetFf.clawPreset)
        assertEquals(1.0f, resetFf.buttonScale, 0.01f)
        assertEquals(0f, resetFf.leftZoneOffsetX, 0.01f)
    }
}
