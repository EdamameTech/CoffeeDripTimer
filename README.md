# Coffee Drip Timer
An Android app to track brewing time to drip coffee

## Usage
1. Set the amount of ground coffee beans in the input box at the top left,
2. Set the roast of the coffee beans with the drop down menu at the top right,
3. Tap the Start button at the bottom left, and
4. Follow the instruction to pour hot water over the coffee dripper.

## Privacy policy
This privacy policy states use of information of the user by a software application, Coffee Drip Timer.

The application does not store any information in the device.

## License
Copyright 2024 by Greensoybean Technologies, under [MIT License](LICENSE).

## Development
### Releasing the app
1. Make sure `const val waitDurationUnit` is `60000L` in `app/src/main/java/com/edamametech/android/coffeedriptimer/ui/CoffeeDripTimerScreen.kt`. This can be lowered to, e.g., `5000L` for quicker debugging.
2. Remove unreferenced resources and debug logs.
3. Update `android.defaultConfig.versionCode` and `...versionName` in `app/build.gradle.kts`.
4. Test things on debug build.
5. Build - Generate Signed App Bundle / APK... and select release build.

