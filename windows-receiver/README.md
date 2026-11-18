# Windows PC Receiver for Minecraft USB Mode

This standalone receiver runs on your Windows PC and receives offline controller events over USB tethering, injecting them into Windows using hardware scan codes via `SendInput`.

---

## Why USB Mode?
Some Android smartphones (including certain Vivo and iQOO devices running Funtouch OS) omit or restrict the native Android Bluetooth HID Device profile in their Bluetooth firmware stack. 
**USB Mode bypasses all Bluetooth restrictions** by sending inputs over a local USB tethering cable.

---

## Requirements on Windows PC
1. **Windows 10 or 11**
2. **Python 3.7+** (Download from [python.org](https://www.python.org/downloads/)). Make sure to check **"Add Python to PATH"** during installation.
3. Zero pip packages required (uses Windows built-in `ctypes` and `SendInput`).

---

## 3-Step Setup Guide

### Step 1: Connect Phone & Enable USB Tethering
1. Connect your phone to your PC with a USB cable.
2. On your phone (e.g. iQOO 9 SE), open **Settings** -> **Other networks & connections** (or **Network & Internet**) -> **Personal Hotspot / Tethering** -> Enable **USB Tethering**.
3. Windows will recognize a new wired network adapter (Remote NDIS).

### Step 2: Run the Receiver (Administrator for MSI App Player / BlueStacks)
Double-click `run_receiver.bat` and click **Yes** on the Administrator prompt (MSI App Player & BlueStacks run elevated on Windows, so Windows blocks inputs unless the receiver also runs as Administrator).
You will see:
```
==========================================================
 Minecraft & FF Controller - Windows PC Receiver v1.2
==========================================================
 Privileges  : ADMINISTRATOR [OK - MSI App Player / BlueStacks Ready]
 Input Mode  : HYBRID (VK + ScanCode | Press 'M' to toggle)
 Live Logs   : ON (Press 'L' to toggle)
----------------------------------------------------------
Detected Local IPv4 Addresses on this PC:
  * 192.168.42.2 <- [USB Tethering Adapter!]
  * 192.168.1.15

Default Security PIN: 4291
Listening on 0.0.0.0:25565
IMPORTANT: Click inside MSI App Player / Minecraft window to focus it!
==========================================================
```

### Step 3: Connect & Play on MSI App Player / Minecraft
1. On your phone, tap **"USB Cable"** mode, enter the PC IP & PIN (`4291`), and tap **Connect to Windows PC Receiver**.
2. Click inside your **MSI App Player** / **Minecraft** window so it has Windows focus.
3. For Minecraft inside **MSI App Player**: open Minecraft **Settings → Keyboard & Mouse → Full Keyboard Gameplay = ON** (or configure MSI Game Controls `Ctrl+Shift+A` + `F1` Aim Lock).
4. Enjoy lag-free gaming!
