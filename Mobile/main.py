import kivy
from kivy.app import App
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.boxlayout import BoxLayout

class MyApp(App):
    def build(self):
        layout = BoxLayout(orientation='vertical')

        # Create a label
        self.label = Label(text="Welcome to Kivy!")

        # Create a button and attach the click event
        button = Button(text="Click Me")
        button.bind(on_press=self.on_button_click)

        # Add widgets to the layout
        layout.add_widget(self.label)
        layout.add_widget(button)

        return layout

    def on_button_click(self, instance):
        # Update the label text when the button is clicked
        self.label.text = "Button Clicked!"

# Run the app
if __name__ == '__main__':
    MyApp().run()
