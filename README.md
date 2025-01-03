# Coffee Drip Timer
An Android app to track brewing time to drip coffee

## Usage
1. Set the amount of ground coffee beans in the input box at the top left,
2. Set the roast of the coffee beans with the drop down menu at the top right,
3. Tap the Start button at the bottom left, and
4. Follow the instruction to pour hot water over the coffee dripper.

## Privacy policy
This privacy policy states use of information of the user by a software application, Coffee Drip Timer.

The application does not collect or store any information.

## License
Copyright 2024 by Green Soybean Technologies, under [MIT License](LICENSE).

## Development
### Releasing the app
1. Make sure `const val waitDurationUnit` is `60000L` in `app/src/main/java/com/edamametech/android/coffeedriptimer/ui/CoffeeDripTimerScreen.kt`. This can be lowered to, e.g., `5000L` for quicker debugging.
1. Remove unreferenced resources and debug logs.
1. Update `android.defaultConfig.versionCode` and `...versionName` in `app/build.gradle.kts`.
1. Test things on debug build, generate screenshots if necessary.
1. Build - Generate Signed App Bundle / APK... and select release build.
1. Rename the signed app bundle `app/release/app-release.aab` with the version, i.e., `X.Y.aab`.
1. Upload the signed app bundle to developer console, wait for quick checks to complete.
1. Upload screenshots if necessary
1. Tag the working copy: `git tag vX.Y`
1. Push changes to origin: `git push; git push origin tag vX.Y`

