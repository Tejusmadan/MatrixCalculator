# 🧮 Matrix Calculator (Jetpack Compose + JNI/C++)

A native-powered **Android app** built with **Kotlin + Jetpack Compose + C++ (JNI)** to perform core matrix operations efficiently using native code.

---

## ✨ Features

- Perform **matrix addition, subtraction, multiplication, and division**
- Input matrix dimensions and values via a clean Compose-based UI
- Results are displayed in a **formatted grid**
- Core computation runs in **native C++ via JNI**, ensuring performance

---

## 🧰 Prerequisites

- Android Studio (with **NDK** and **CMake** installed via SDK Manager)
- Gradle Kotlin DSL enabled
- **Min SDK**: 24  
- **Target SDK**: 35

---

## 📁 Project Structure

/app
  ├─ src/main
  │    ├─ AndroidManifest.xml
  │    ├─ java/.../MainActivity.kt   # Compose UI + JNI declarations
  ├─ CMakeLists.txt                 # Native library build
  └─ jni/native-lib.cpp             # add/sub/mul/div implementations

Build & Run

Clone the repo and open in Android Studio.

Enable NDK & CMake in SDK Manager.

Sync Gradle; CMake builds native-lib.

Run on device/emulator; input matrices and tap Calculate.
