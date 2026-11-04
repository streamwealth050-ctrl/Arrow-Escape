# Implementation Plan - Logo Download Feature

Allow users to download the authentic Arrow Escape game logo as a high-resolution PNG image directly from the app (such as in the Me Screen or About Dialog).

## Proposed Changes

### 1. Logo Export Utility / ViewModel Action
- Add a helper function in `GameViewModel` or a utility class to rasterize the vector game logo (`ic_game_logo.xml`) or draw it onto a `Bitmap` (e.g. 512x512 pixels).
- Save the generated PNG image to public shared storage (`Environment.DIRECTORY_PICTURES` or `Environment.DIRECTORY_DOWNLOADS`) via `MediaStore` or share it via a system share sheet (`Intent.ACTION_SEND`).

### 2. UI Integration
- Add a **"Download Logo"** button in the **Me Screen** and/or **About Dialog** with a camera/download icon.
- Request runtime permission (WRITE_EXTERNAL_STORAGE on older Android versions, or MediaStore on Android 10+) or use scoped storage MediaStore API so no storage permissions are required on modern Android versions.
- Show a confirmation toast or snackbar when the PNG logo is successfully downloaded or ready to share.

## Verification Plan
- Compile applet to verify build success.
- Test download functionality via UI test tag.
