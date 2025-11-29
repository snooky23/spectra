# Open android-native in Android Studio - Quick Start

## 🚀 The Easiest Way

### Step 1: Open Android Studio
Launch Android Studio (or switch to it if already open)

### Step 2: Open Project
**Choose ONE of these options:**

**Option A: Using File Menu**
```
File → Open → Navigate to:
/Users/avilevin/Workspace/Personal/Spectra/examples/android-native
→ Click "Open"
```

**Option B: Using Command Line**
```bash
open -a "Android Studio" /Users/avilevin/Workspace/Personal/Spectra/examples/android-native
```

**Option C: Using Terminal**
```bash
cd /Users/avilevin/Workspace/Personal/Spectra/examples/android-native
open -a "Android Studio" .
```

### Step 3: Wait for Gradle Sync
Android Studio will automatically:
1. Recognize the project as an Android project ✅
2. Start Gradle sync automatically ✅
3. Index all files and dependencies ✅
4. Show "Gradle sync finished" message at the bottom ✅

**That's it! The project is now open and ready to use.** 🎉

---

## ✅ What You Should See

After opening, you'll see:

```
Project Structure (Left Panel):
├── android-native (project root)
│   ├── app (module with blue Android icon)
│   │   ├── manifests
│   │   ├── java (Kotlin files)
│   │   ├── res (resources)
│   │   └── build.gradle.kts
│   └── gradle
```

**Build Output (Bottom Panel):**
```
Gradle sync finished in X seconds
```

**Status Bar:** Should show the Gradle logo on the right side (not red/error)

---

## 🎮 Run the App

Once the project is open:

1. **Start an Android Emulator:**
   - Tools → Device Manager → Create/Launch an emulator
   - Or: AVD Manager → Launch an existing emulator

2. **Run the App:**
   - Click **Run → Run 'app'** (Keyboard: Ctrl+R on Windows/Linux, Cmd+R on Mac)
   - Select your emulator when prompted
   - Wait for the app to build and launch (~30-60 seconds)

3. **See the App:**
   - The Spectra Logger example app appears on your emulator
   - You can tap buttons to generate logs
   - Click "Open Spectra Logger" to view captured logs

---

## ❌ If Something Goes Wrong

### "Gradle sync failed" or "Cannot find SDK"

**Solution:** Click **File → Project Structure**

1. Under **Project**, set:
   - SDK Location: `/Users/avilevin/Library/Android/sdk`
   - JDK Location: (leave as Android Studio default JDK)

2. Click **OK**

3. Click **File → Sync Now** (or Ctrl+Alt+Y)

4. Wait for sync to complete

### "Cannot find sources" or "Code is red"

Click **File → Invalidate Caches** → **Invalidate and Restart**

Then Android Studio will re-index everything.

### App won't launch on emulator

Make sure:
1. ✅ Emulator is running and shows in Android Studio's Devices panel
2. ✅ Build succeeded (check Build Output panel)
3. ✅ Check logcat for errors: View → Tool Windows → Logcat

---

## 📚 File Structure

All these files enable Android Studio support:

| File/Folder | Purpose |
|-------------|---------|
| `.idea/` | Android Studio project configuration |
| `.idea/modules.xml` | Project modules definition |
| `.idea/misc.xml` | Project settings (SDK, language level) |
| `.idea/vcs.xml` | Version control integration |
| `.idea/codeStyles/Project.xml` | Kotlin code style settings |
| `.idea/modules/*.iml` | Module configuration files |
| `local.properties` | Android SDK path configuration |
| `settings.gradle.kts` | Gradle settings |
| `build.gradle.kts` | Root build configuration |
| `app/build.gradle.kts` | App module build configuration |

---

## 🔧 Keyboard Shortcuts

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Run App | Cmd+R | Ctrl+R |
| Rebuild | Cmd+Shift+B | Ctrl+F9 |
| Sync Gradle | Cmd+Shift+Y | Ctrl+Alt+Y |
| Open File | Cmd+O | Ctrl+O |
| Find in Files | Cmd+Shift+F | Ctrl+Shift+F |
| Debug | Cmd+D | Shift+F9 |

---

## 💡 Pro Tips

1. **Make sure emulator is running BEFORE clicking Run**
   - The app won't build if no device is available

2. **Use "Run" not "Debug" if first time**
   - Debug mode is slower the first time

3. **Check Gradle build output if errors occur**
   - View → Tool Windows → Build → shows detailed errors

4. **Use "Rebuild Project" if you see strange errors**
   - Build → Rebuild Project

5. **Keyboard shortcut for Run: Cmd+R (Mac) or Ctrl+R (Windows)**
   - Much faster than using the menu

---

## 📖 For More Information

- **Setup Guide:** See `ANDROID_STUDIO_SETUP.md` for detailed troubleshooting
- **Project Docs:** See `README.md` for complete project documentation
- **Code:** All source code is in `app/src/main/java/com/spectra/logger/example/`

---

## ✨ You're All Set!

The project should now open immediately in Android Studio without any configuration needed. If it doesn't, try the troubleshooting steps above or check `ANDROID_STUDIO_SETUP.md` for more detailed help.

**Enjoy building!** 🚀
