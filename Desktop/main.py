import tkinter as tk
from tkinter import messagebox
import asyncio
from bleak import BleakScanner
import threading

# Globals
scanning = False
scanner = None
loop = None

# Start scanning
def start_scanning():
    global scanning, stop_button
    scanning = True
    start_button.config(state="disabled")
    stop_button.pack(pady=10)
    threading.Thread(target=run_ble_scan).start()

# Stop scanning
def stop_scanning():
    global scanning
    scanning = False
    stop_button.pack_forget()
    start_button.config(state="normal")

# Async BLE scan loop
async def scan_ble_loop():
    global scanner
    scanner = BleakScanner()
    while scanning:
        print("Scanning for BLE devices...")
        devices = await scanner.discover(timeout=2.0)
        if not devices:
            print("No devices found.")
        for device in devices:
            print(f"{device.name} - {device.address}")
        await asyncio.sleep(2)

# Runs the async loop in a thread
def run_ble_scan():
    global loop
    try:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        loop.run_until_complete(scan_ble_loop())
    except Exception as e:
        print("Scan error:", e)

# GUI setup
root = tk.Tk()
root.title("BLE Scanner")
root.geometry("300x200")

label = tk.Label(root, text="BLE Device Scanner")
label.pack(pady=10)

start_button = tk.Button(root, text="Start Scanning", command=start_scanning)
start_button.pack(pady=10)

stop_button = tk.Button(root, text="Stop Scanning", command=stop_scanning)
# Not packing until scanning starts

root.mainloop()
