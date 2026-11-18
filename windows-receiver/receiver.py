#!/usr/bin/env python3
"""
Minecraft & Free Fire Controller - Offline Windows PC USB Receiver v1.2
-----------------------------------------------------------------------
Listens on local port 25565 over USB Tethering and translates incoming
controller events into native Windows keyboard and mouse events using
Win32 SendInput.

Includes Emulator-Compatible Dual VK+ScanCode mode + Admin UIPI detection
for MSI App Player, BlueStacks, LDPlayer, Java/Bedrock Minecraft & Eaglercraft.
"""

import socket
import sys
import ctypes
import time
import threading
import msvcrt
from ctypes import wintypes

# Win32 Constants for SendInput
INPUT_MOUSE = 0
INPUT_KEYBOARD = 1

KEYEVENTF_EXTENDEDKEY = 0x0001
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_SCANCODE = 0x0008

MOUSEEVENTF_MOVE = 0x0001
MOUSEEVENTF_LEFTDOWN = 0x0002
MOUSEEVENTF_LEFTUP = 0x0004
MOUSEEVENTF_RIGHTDOWN = 0x0008
MOUSEEVENTF_RIGHTUP = 0x0010
MOUSEEVENTF_MIDDLEDOWN = 0x0020
MOUSEEVENTF_MIDDLEUP = 0x0040
MOUSEEVENTF_WHEEL = 0x0800

ULONG_PTR = ctypes.c_size_t

# Hardware Scan Codes (PS/2 Set 1)
SCAN_CODES = {
    'W': 0x11,
    'A': 0x1E,
    'S': 0x1F,
    'D': 0x20,
    'E': 0x12,
    'Q': 0x10,
    'R': 0x13,
    'T': 0x14,
    'F': 0x21,
    'G': 0x22,
    'H': 0x23,
    'Z': 0x2C,
    'X': 0x2D,
    'C': 0x2E,
    'V': 0x2F,
    'B': 0x30,
    'M': 0x32,
    'SPACE': 0x39,
    'ESC': 0x01,
    'TAB': 0x0F,
    'ENTER': 0x1C,
    'BACKSPACE': 0x0E,
    'LSHIFT': 0x2A,
    'LCTRL': 0x1D,
    'LALT': 0x38,
    'F1': 0x3B,
    'F2': 0x3C,
    'F3': 0x3D,
    'F4': 0x3E,
    'F5': 0x3F,
    'F11': 0x57,
    '1': 0x02,
    '2': 0x03,
    '3': 0x04,
    '4': 0x05,
    '5': 0x06,
    '6': 0x07,
    '7': 0x08,
    '8': 0x09,
    '9': 0x0A,
    '0': 0x0B,
}

# Win32 Virtual Key Codes (Required by MSI App Player, BlueStacks, LDPlayer & Chromium)
VK_CODES = {
    'W': 0x57,
    'A': 0x41,
    'S': 0x53,
    'D': 0x44,
    'E': 0x45,
    'Q': 0x51,
    'R': 0x52,
    'T': 0x54,
    'F': 0x46,
    'G': 0x47,
    'H': 0x48,
    'Z': 0x5A,
    'X': 0x58,
    'C': 0x43,
    'V': 0x56,
    'B': 0x42,
    'M': 0x4D,
    'SPACE': 0x20,
    'ESC': 0x1B,
    'TAB': 0x09,
    'ENTER': 0x0D,
    'BACKSPACE': 0x08,
    'LSHIFT': 0xA0,
    'LCTRL': 0xA2,
    'LALT': 0xA4,
    'F1': 0x70,
    'F2': 0x71,
    'F3': 0x72,
    'F4': 0x73,
    'F5': 0x74,
    'F11': 0x7A,
    '1': 0x31,
    '2': 0x32,
    '3': 0x33,
    '4': 0x34,
    '5': 0x35,
    '6': 0x36,
    '7': 0x37,
    '8': 0x38,
    '9': 0x39,
    '0': 0x30,
}

# Win32 Struct Definitions (Exact 32/64-bit ABI)
class MOUSEINPUT(ctypes.Structure):
    _fields_ = [
        ("dx", wintypes.LONG),
        ("dy", wintypes.LONG),
        ("mouseData", wintypes.DWORD),
        ("dwFlags", wintypes.DWORD),
        ("time", wintypes.DWORD),
        ("dwExtraInfo", ULONG_PTR)
    ]

class KEYBDINPUT(ctypes.Structure):
    _fields_ = [
        ("wVk", wintypes.WORD),
        ("wScan", wintypes.WORD),
        ("dwFlags", wintypes.DWORD),
        ("time", wintypes.DWORD),
        ("dwExtraInfo", ULONG_PTR)
    ]

class HARDWAREINPUT(ctypes.Structure):
    _fields_ = [
        ("uMsg", wintypes.DWORD),
        ("wParamL", wintypes.WORD),
        ("wParamH", wintypes.WORD)
    ]

class INPUT_UNION(ctypes.Union):
    _fields_ = [
        ("mi", MOUSEINPUT),
        ("ki", KEYBDINPUT),
        ("hi", HARDWAREINPUT)
    ]

class INPUT(ctypes.Structure):
    _fields_ = [
        ("type", wintypes.DWORD),
        ("u", INPUT_UNION)
    ]

user32 = ctypes.WinDLL('user32', use_last_error=True)
user32.SendInput.argtypes = (wintypes.UINT, ctypes.POINTER(INPUT), ctypes.c_int)
user32.SendInput.restype = wintypes.UINT

# Runtime mode:
# "HYBRID" sends both wVk + wScan (works for MSI App Player, BlueStacks, LDPlayer, PC & Browser)
# "SCANCODE" sends wVk=0 + KEYEVENTF_SCANCODE (for strict DirectInput legacy games)
input_mode = "HYBRID"
verbose_log = True
last_uipi_warn_time = 0.0

def is_admin():
    try:
        return bool(ctypes.windll.shell32.IsUserAnAdmin())
    except Exception:
        return False

def get_active_window_title():
    try:
        hwnd = user32.GetForegroundWindow()
        if not hwnd:
            return "Unknown"
        length = user32.GetWindowTextLengthW(hwnd)
        if length <= 0:
            return "Desktop/Untitled"
        buf = ctypes.create_unicode_buffer(length + 1)
        user32.GetWindowTextW(hwnd, buf, length + 1)
        return buf.value or "Unknown"
    except Exception:
        return "Unknown"

def check_send_result(sent, action_desc):
    global last_uipi_warn_time
    if sent == 0:
        err = ctypes.get_last_error()
        now = time.time()
        if now - last_uipi_warn_time > 3.0:
            last_uipi_warn_time = now
            win = get_active_window_title()
            print(f"[!] SendInput BLOCKED (err={err}) on '{action_desc}' -> Window: '{win}'")
            if not is_admin():
                print("    >>> FIX: Close this window and run 'run_receiver.bat' as ADMINISTRATOR!")
                print("    >>> MSI App Player / BlueStacks runs elevated and blocks non-admin input.")

def send_key(key_name, is_down):
    key_upper = key_name.upper()
    scan_code = SCAN_CODES.get(key_upper)
    vk_code = VK_CODES.get(key_upper, 0)

    if not scan_code and not vk_code:
        if verbose_log:
            print(f"[?] Unknown key skipped: {key_name}")
        return

    inp = INPUT()
    inp.type = INPUT_KEYBOARD

    if input_mode == "SCANCODE" and scan_code:
        flags = KEYEVENTF_SCANCODE
        if not is_down:
            flags |= KEYEVENTF_KEYUP
        inp.u.ki = KEYBDINPUT(0, scan_code, flags, 0, 0)
    else:
        # HYBRID (MSI App Player / BlueStacks / Java / Bedrock / Eaglercraft compatible):
        # Sends BOTH real Win32 Virtual Key (wVk) AND Hardware Scan Code (wScan)
        flags = 0 if is_down else KEYEVENTF_KEYUP
        inp.u.ki = KEYBDINPUT(vk_code, scan_code or 0, flags, 0, 0)

    sent = user32.SendInput(1, ctypes.byref(inp), ctypes.sizeof(INPUT))
    check_send_result(sent, f"{'KD' if is_down else 'KU'} {key_upper}")

    if verbose_log and is_down:
        win = get_active_window_title()
        print(f"[KEY] {key_upper} (VK=0x{vk_code:02X}, Scan=0x{(scan_code or 0):02X}) -> '{win}'")

def send_mouse_move(dx, dy):
    inp = INPUT()
    inp.type = INPUT_MOUSE
    inp.u.mi = MOUSEINPUT(dx, dy, 0, MOUSEEVENTF_MOVE, 0, 0)
    sent = user32.SendInput(1, ctypes.byref(inp), ctypes.sizeof(INPUT))
    check_send_result(sent, f"MM {dx} {dy}")

def send_mouse_button(button, is_down):
    flag = 0
    if button == 'L':
        flag = MOUSEEVENTF_LEFTDOWN if is_down else MOUSEEVENTF_LEFTUP
    elif button == 'R':
        flag = MOUSEEVENTF_RIGHTDOWN if is_down else MOUSEEVENTF_RIGHTUP
    elif button == 'M':
        flag = MOUSEEVENTF_MIDDLEDOWN if is_down else MOUSEEVENTF_MIDDLEUP

    if flag != 0:
        inp = INPUT()
        inp.type = INPUT_MOUSE
        inp.u.mi = MOUSEINPUT(0, 0, 0, flag, 0, 0)
        sent = user32.SendInput(1, ctypes.byref(inp), ctypes.sizeof(INPUT))
        check_send_result(sent, f"{'MD' if is_down else 'MU'} {button}")
        if verbose_log and is_down:
            print(f"[MOUSE] {'DOWN' if is_down else 'UP'} {button} -> '{get_active_window_title()}'")

def send_mouse_wheel(scroll):
    inp = INPUT()
    inp.type = INPUT_MOUSE
    inp.u.mi = MOUSEINPUT(0, 0, scroll * 120, MOUSEEVENTF_WHEEL, 0, 0)
    sent = user32.SendInput(1, ctypes.byref(inp), ctypes.sizeof(INPUT))
    check_send_result(sent, f"MW {scroll}")

def release_all():
    for key in SCAN_CODES.keys():
        send_key(key, False)
    send_mouse_button('L', False)
    send_mouse_button('R', False)
    send_mouse_button('M', False)

def get_local_ips():
    ips = set()
    try:
        hostname = socket.gethostname()
        for info in socket.getaddrinfo(hostname, None, socket.AF_INET):
            ip = info[4][0]
            if not ip.startswith("127."):
                ips.add(ip)
    except Exception:
        pass
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('192.168.42.129', 80))
        ips.add(s.getsockname()[0])
        s.close()
    except Exception:
        pass
    return sorted(list(ips), key=lambda x: (0 if "192.168.42." in x else 1, x))

def start_udp_beacon_worker(pin="4291", port=25565):
    """Broadcasts discovery beacon over local subnets so Android phone auto-detects PC IP."""
    def worker():
        beacon_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        beacon_sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        while True:
            try:
                local_ips = get_local_ips()
                for ip in local_ips:
                    msg = f"MC_RECEIVER_BEACON {pin} {ip}\n".encode('utf-8')
                    beacon_sock.sendto(msg, ('255.255.255.255', port))
                    parts = ip.split('.')
                    if len(parts) == 4:
                        sub_bcast = f"{parts[0]}.{parts[1]}.{parts[2]}.255"
                        beacon_sock.sendto(msg, (sub_bcast, port))
            except Exception:
                pass
            time.sleep(1.0)

    t = threading.Thread(target=worker, daemon=True)
    t.start()

def start_console_hotkeys():
    """Allows toggling input mode (M) or logging (L) right from the receiver console."""
    def worker():
        global input_mode, verbose_log
        while True:
            try:
                if msvcrt.kbhit():
                    ch = msvcrt.getch().decode('utf-8', errors='ignore').lower()
                    if ch == 'm':
                        input_mode = "SCANCODE" if input_mode == "HYBRID" else "HYBRID"
                        print(f"\n[*] Switched Input Mode -> {input_mode}")
                    elif ch == 'l':
                        verbose_log = not verbose_log
                        print(f"\n[*] Live Input Log -> {'ON' if verbose_log else 'OFF'}")
            except Exception:
                pass
            time.sleep(0.1)

    t = threading.Thread(target=worker, daemon=True)
    t.start()

def main():
    admin_ok = is_admin()
    print("==========================================================")
    print(" Minecraft & FF Controller - Windows PC Receiver v1.2")
    print("==========================================================")
    if admin_ok:
        print(" Privileges  : ADMINISTRATOR [OK - MSI App Player / BlueStacks Ready]")
    else:
        print(" Privileges  : STANDARD USER [!] WARNING!")
        print("               MSI App Player / BlueStacks runs as Admin and BLOCKS")
        print("               inputs from non-Admin receivers! Run run_receiver.bat")
        print("               (it auto-requests Admin) if buttons don't respond.")
    print(f" Input Mode  : {input_mode} (VK + ScanCode | Press 'M' to toggle)")
    print(f" Live Logs   : {'ON' if verbose_log else 'OFF'} (Press 'L' to toggle)")
    print("----------------------------------------------------------")

    local_ips = get_local_ips()
    print("Detected Local IPv4 Addresses on this PC:")
    if not local_ips:
        print("  [!] No active network interfaces found yet.")
        print("      Turn on USB Tethering on your phone or use Wi-Fi IP!")
    else:
        for ip in local_ips:
            tag = " <- [USB Tethering Adapter!]" if ip.startswith("192.168.42.") else ""
            print(f"  * {ip}{tag}")

    print("\nDefault Security PIN: 4291")
    print("Listening on 0.0.0.0:25565")
    print("IMPORTANT: Click inside MSI App Player / Minecraft window to focus it!")
    print("==========================================================\n")

    start_udp_beacon_worker(pin="4291", port=25565)
    start_console_hotkeys()

    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    try:
        server_sock.bind(('0.0.0.0', 25565))
        server_sock.listen(5)
    except Exception as e:
        print(f"[!] Error binding to port 25565: {e}")
        print("[!] Ensure no other receiver instance is running.")
        sys.exit(1)

    while True:
        print("[*] Waiting for phone to connect...")
        client_sock, client_addr = server_sock.accept()
        client_sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        print(f"[+] Phone connected from {client_addr[0]}!")

        authenticated = False
        buffer = ""

        try:
            while True:
                data = client_sock.recv(1024).decode('utf-8', errors='ignore')
                if not data:
                    break
                buffer += data

                while '\n' in buffer:
                    line, buffer = buffer.split('\n', 1)
                    line = line.strip()
                    if not line:
                        continue

                    parts = line.split()
                    cmd = parts[0]

                    if not authenticated:
                        if cmd == "AUTH":
                            authenticated = True
                            client_sock.sendall(b"AUTH_OK\n")
                            print(f"[+] Phone authenticated! Active window: '{get_active_window_title()}'")
                        else:
                            client_sock.sendall(b"AUTH_FAIL\n")
                            print(f"[!] Authentication rejected from {client_addr[0]}")
                            break
                        continue

                    if cmd == "KD" and len(parts) > 1:
                        send_key(parts[1], True)
                    elif cmd == "KU" and len(parts) > 1:
                        send_key(parts[1], False)
                    elif cmd == "MM" and len(parts) > 2:
                        try:
                            dx = int(parts[1])
                            dy = int(parts[2])
                            send_mouse_move(dx, dy)
                        except ValueError:
                            pass
                    elif cmd == "MD" and len(parts) > 1:
                        send_mouse_button(parts[1], True)
                    elif cmd == "MU" and len(parts) > 1:
                        send_mouse_button(parts[1], False)
                    elif cmd == "MW" and len(parts) > 1:
                        try:
                            scroll = int(parts[1])
                            send_mouse_wheel(scroll)
                        except ValueError:
                            pass
                    elif cmd == "TEST_KEY":
                        target = parts[1] if len(parts) > 1 else "SPACE"
                        send_key(target, True)
                        time.sleep(0.06)
                        send_key(target, False)
                    elif cmd == "RELEASE_ALL":
                        release_all()
                    elif cmd == "PING":
                        client_sock.sendall(b"PONG\n")

        except Exception as e:
            print(f"[-] Client connection error: {e}")
        finally:
            print("[-] Phone disconnected. Releasing all inputs.")
            release_all()
            try:
                client_sock.close()
            except Exception:
                pass

if __name__ == "__main__":
    main()
