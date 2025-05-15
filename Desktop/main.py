import tkinter as tk
from tkinter import messagebox
import asyncio
from bleak import BleakScanner
import threading

# Globals
scanning = False
scanner = None
loop = None
device_list = []

# Start scanning
def start_scanning():
    global scanning, stop_button
    scanning = True
    start_button.config(state="disabled")
    stop_button.pack(pady=10)
    threading.Thread(target=run_ble_scan, daemon=True).start()

# Stop scanning
def stop_scanning():
    global scanning
    scanning = False
    stop_button.pack_forget()
    start_button.config(state="normal")

# Update the device list and refresh UI safely
def update_device_list(devices):
    global device_list
    device_list = [f"{d.name or 'Unknown'} - {d.address}" for d in devices]
    root.after(0, refresh_listbox)

def refresh_listbox():
    listbox.delete(0, tk.END)
    for item in device_list:
        listbox.insert(tk.END, item)

# Async BLE scan loop
async def scan_ble_loop():
    global scanner, scanning
    scanner = BleakScanner()
    while scanning:
        print("Scanning for BLE devices...")
        try:
            devices = await scanner.discover(timeout=2.0)
            if not devices:
                print("No devices found.")
            update_device_list(devices)
        except Exception as e:
            print("Error during scan:", e)
        # Short sleep to allow quick stop
        await asyncio.sleep(0.1)

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
root.geometry("400x400")

label = tk.Label(root, text="BLE Device Scanner")
label.pack(pady=10)

start_button = tk.Button(root, text="Start Scanning", command=start_scanning)
start_button.pack(pady=10)

stop_button = tk.Button(root, text="Stop Scanning", command=stop_scanning)
# Not packing stop_button until scanning starts

listbox = tk.Listbox(root)
listbox.pack(pady=10, fill=tk.BOTH, expand=True)

root.mainloop()
