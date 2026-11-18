# Minecraft Controller for Android (Bluetooth HID & USB Cable)

**Minecraft Controller** turns your Android smartphone into an offline controller for Minecraft (PC Java, Bedrock, and browser editions like Eaglercraft). The app features two dedicated connection modes:

1. **Bluetooth HID Mode:** Connects directly to your PC as a native Bluetooth keyboard & mouse. Works 100% offline without any companion software on the PC.
2. **USB Cable Mode (USB Tethering + PC Receiver):** Uses USB tethering to send lag-free controller packets over an offline local link to an included lightweight Windows receiver program (`windows-receiver/receiver.py`). Ideal if your phone manufacturer restricts the Bluetooth HID profile.

---

## Target Phone: iQOO 9 SE (Funtouch OS) Compatibility

- **Runtime Detection:** The app detects Bluetooth HID profile proxy support dynamically at runtime (`BluetoothProfile.HID_DEVICE`).
- **Funtouch OS Advice:** Some Vivo/iQOO devices running Funtouch OS have Bluetooth stacks that restrict third-party Bluetooth HID Device registration. If your device displays *"HID Unsupported"*, switch to **USB Cable Mode** for an instant, guaranteed connection!
- **Battery Optimization:** In Funtouch OS Settings -> **Battery** -> **Background power consumption management** -> set **Minecraft Controller** to *"Don't restrict background power consumption"* so the OS does not throttle the controller during gameplay.

---

## Connection Modes Compared

| Feature | Bluetooth HID Mode | USB Cable Mode |
|---|---|---|
| **Connection Type** | Wireless Bluetooth Radio | Physical USB Cable (USB Tethering) |
| **PC Companion Program** | None required (Native Bluetooth HID) | Requires included `receiver.py` |
| **Latency** | ~8–15 ms | < 1 ms (Sub-millisecond) |
| **Device Compatibility** | Depends on phone Bluetooth firmware | Works on 100% of Android phones |
| **Security** | Bluetooth PIN Pairing | Local 4-Digit Security PIN (Default: `4291`) |
| **Offline** | 100% Offline | 100% Offline (Local USB socket) |

---

## Setup Guide: Bluetooth HID Mode

1. In the app, select **Bluetooth HID** mode.
2. Tap **"Make Phone Discoverable (120s)"**.
3. On your PC (Windows/Mac/Linux), open **Bluetooth Settings** -> **Add device** -> select your phone.
4. Confirm the pairing PIN code on both your PC and phone.
5. In the app, find your PC in the list and tap **Connect** (or click Connect under your phone in Windows Bluetooth settings).
6. Click inside the Minecraft game window with your physical mouse to capture input focus!

---

## Setup Guide: USB Cable Mode

### On the Phone:
1. Connect your phone to your PC using a USB cable.
2. In the app, switch to **USB Cable** mode.
3. Tap **"Open Phone Tethering Settings"** and toggle **USB Tethering** to **ON**.
4. The app will detect your phone's USB IP address (typically `192.168.42.x`).

### On the Windows PC:
1. Ensure Python 3.7+ is installed from [python.org](https://www.python.org/) with *"Add Python to PATH"* checked.
2. In the project folder under `windows-receiver/`, double-click **`run_receiver.bat`** (or open command prompt and run `python receiver.py`).
3. You will see:
   ```
   ==========================================================
    Minecraft BT & USB Controller - Windows PC Receiver
   ==========================================================
   Default Pairing PIN: 4291
   Listening on 0.0.0.0:25565 (Local USB Tethering only)...
   ```
4. Back in the Android app, tap **"Connect to Windows PC Receiver"**.
5. Once connected, click inside the Minecraft or Eaglercraft browser window to play!

---

## Controller Controls & Mappings

- **8-Way Movement Joystick:** WASD directional movement with diagonal dual-key support (W+A, W+D, S+A, S+D).
- **Camera Look Trackpad:** Center trackpad with relative delta mouse movement and reticle feedback.
- **Attack (Left Click):** Prominent redstone button for breaking blocks and attacking.
- **Use / Place (Right Click):** Cyan button for placing blocks and interacting.
- **Jump:** Spacebar button.
- **Sneak:** Left Shift with Hold or Toggle modes in Settings.
- **Hotbar Slots 1–5:** Quick item selection.
- **Utility Buttons:** Inventory (E), Menu/Pause (Esc), Drop item (Q), Camera Perspective (F5), Player List (Tab), Debug screen (F3).

---

## Building the APK

```bash
# Compile and build the debug APK
gradle assembleDebug

# Run unit and Robolectric tests
gradle testDebugUnitTest
```
Output APK location:
`app/build/outputs/apk/debug/app-debug.apk`
