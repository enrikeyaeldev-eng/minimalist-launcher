# minimalist-launcher

An Android home screen that is one scrollable, text-only list of your apps.
It has no icons, wallpaper, widgets or app drawer.

- The device's default camera is pinned at the top. It is looked up at runtime, not hardcoded.
- All other apps follow in alphabetical order. Tap an app to open it.
- Long-press an app to get `[ uninstall ]`, which opens Android's uninstall dialog.
  System apps that can't be uninstalled show `[ app info ]` instead, which opens their App Info screen.
- The list updates by itself when apps are installed or removed.
- Always dark, in the "Phosphor" style from [`mockup.html`](mockup.html): JetBrains Mono on near-black with a green accent.
- Back does nothing on the home screen.

Built with Kotlin and Jetpack Compose. Minimum Android version is 8.0 (API 26).

## Get the APK

Every push runs the **Build debug APK** workflow in GitHub Actions.

1. In the repository on GitHub, open **Actions → Build debug APK**, then open the latest green run.
2. Under **Artifacts**, download `launcher-debug-apk`. It is a zip containing `app-debug.apk`.
3. On the phone, unzip it and open `app-debug.apk`. If Android asks, allow your browser or file manager to install unknown apps.
4. Press Home. Android asks which launcher to use: pick **launcher** and choose **Always**.
   You can switch back any time in **Settings → Apps → Default apps → Home app**.

All builds are signed with the same debug key (`app/debug.keystore`, committed on purpose),
so a new APK installs over the old one without uninstalling first.
That key is public and only good for debug builds. A Play Store release would need its own private key.

## Permissions

| What | Why |
|---|---|
| `REQUEST_DELETE_PACKAGES` | Required to open the system uninstall dialog. Android grants it at install time without a prompt, and still asks you to confirm each uninstall. It can't remove anything by itself. |
| `<queries>` for the launcher intent | Since Android 11, apps can't see other installed apps unless they say what they're looking for. This asks only for apps that have a launcher entry, so the broad `QUERY_ALL_PACKAGES` permission isn't needed. |
| `<queries>` for the camera intent | Lets the launcher find which app is the default camera. |

No internet, storage, contacts or location access is requested.

## Building

You don't need to build anything locally: GitHub Actions does it on every push.
To build in a Codespace instead, install the Android SDK and set `ANDROID_HOME`, then run `./gradlew assembleDebug`.
Build output goes to `app/build/`, which git ignores.

## Credits

JetBrains Mono is © The JetBrains Mono Project Authors and licensed under the SIL Open Font License 1.1
(see `app/src/main/assets/licenses/JetBrainsMono-OFL.txt`). The bundled copy is trimmed to Latin characters.
