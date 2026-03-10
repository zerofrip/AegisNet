# AegisNet

A production-grade Android privacy firewall, smart router, and VPN client built using sing-box as the core networking engine.

## Features
- Full VPN traffic interception via Android `VpnService`
- Configurable DNS (UDP, DoH, DoT, DoQ) and FakeDNS support
- Filter Engine with AdGuard/uBlock syntax support for domains
- Smart Routing by Domain, IP, GeoIP
- WireGuard profile import (`.conf` and ZIP support)
- Per-App Firewall Configurator
- Auto-updating Filter Lists via WorkManager
- Local Room Database storage
- Jetpack Compose Material 3 Dark UI

## Architecture
- `app/`: Main Android Application module containing UI, database, routing rules, and VPN service configuration
- `core/singbox/`: Abstraction module for the sing-box engine (routing, DNS)

## How to Build

### Prerequisites
- Android Studio Iguana (2023.2.1) or newer
- JDK 17
- Android SDK 34

### Building from Command Line
You can build the debug version utilizing Gradle Wrapper:

```bash
# Provide execution permission to gradlew
chmod +x gradlew

# Build the debug APK
./gradlew assembleDebug
```

The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

### Note on sing-box libraries
Currently, the `core:singbox` module uses a stub interface `SingboxEngine`. For a production release, the native AAR (e.g. `libsingbox.aar`) must be included in the module and initialized via JNI / direct instantiation within the interface implementations. Ensure you are using the BSD-3-Clause version of sing-box.
