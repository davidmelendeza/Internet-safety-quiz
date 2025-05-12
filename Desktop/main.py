# Makes a simple GUI with tkinter
import tkinter as tk
from tkinter import messagebox

def on_button_click():
    # Show a message box when the button is clicked
    messagebox.showinfo("Button Clicked", "Hello, Tkinter!")

# Create the main window
root = tk.Tk()
root.title("Simple GUI")
root.geometry("300x200")
# Create a button and attach the click event
button = tk.Button(root, text="Click Me", command=on_button_click)
button.pack(pady=20)
# Create a label
label = tk.Label(root, text="Welcome to Tkinter!")
label.pack(pady=10)
# Start the main event loop
root.mainloop()
