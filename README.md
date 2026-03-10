# AegisNet

AegisNet is a production-grade Android privacy firewall, smart router, DNS filter, and VPN application built on top of the high-performance [sing-box](https://github.com/SagerNet/sing-box) networking core. It integrates functionalities similar to AdGuard, RethinkDNS, and Clash into a single, cohesive, open-source Android client.

## Key Features

- **Advanced Filtering**: Sub-1ms matching against over 200,000+ rules simultaneously using highly optimized memory indexes (Domain Suffix Tries, Aho-Corasick Keyword Matchers, and Path Tries).
- **Multi-Syntax Support**: Natively compiles `AdGuard`, `uBlock Origin`, and `Clash` lists directly into `sing-box` rules.
- **WireGuard Integration**: Supports importing individual `.conf` files and batch `.zip` archives. WireGuard tunnels are efficiently offloaded to the pure Go implementation built inside `sing-box`.
- **Smart Routing & Bypass**: Transparently split-tunnels traffic bridging whitelisted domains directly to the internet while restricting trackers explicitly to blackholes.
- **DNS Control**: Features an embedded FakeDNS (198.18.0.0/15) proxy engine intercepting DNS requests locally, alongside custom encrypted upstream providers (DoH, DoQ, DoT).
- **Automated List Management**: Utilizes Android WorkManager to silently update massive URL Filter Lists in the background (supporting WiFi-only restrictions and backoff retries).
- **Privacy First via QUIC Blocking**: Hard-blocks UDP port 443 ensuring all HTTP/3 connections fallback smoothly to inspectable TLS SNI requests.

## Architecture Overview

AegisNet bypasses standard GPL WireGuard libraries, embedding `sing-box` directly via a Git Submodule compiled into shared JNI objects (`libsingbox.so`) via the Android NDK. The Android codebase is purely Kotlin, utilizing Jetpack Compose for the UI, Coroutines for asynchronous states, Hilt for Dependency Injection, and Room for SQLite data persistence.

### Networking Pipeline

All intercepted device traffic descends through the following pipeline:

```text
Android VPNService
      ↓
  sing-box
      ↓
   FakeDNS
      ↓
  DNS filter
  SNI filter
HTTP rule engine
  QUIC block
      ↓
  tun2stack
      ↓
 Smart routing
      ↓
WireGuard outbound
      ↓
   Internet
```

## Project Structure

- `app/`: The core Jetpack Compose Kotlin Android application (MIT License).
- `app/src/main/java/com/aegisnet/filter/`: High-Performance Filtering Engine storing rule indexes.
- `app/src/main/java/com/aegisnet/singbox/builders/`: Modular Singbox JSON config generation.
- `core/sing-box/`: The upstream sing-box repository (Git Submodule - BSD-3-Clause).
- `core/singbox-jni/`: Contains the C-Shared Go wrapper bridging JNI payloads to the `sing-box` engine.
- `build-scripts/`: Contains `build-singbox.sh` for driving the Android NDK cross-compilation matrix.
- `prebuilt/`: The target directory containing compiled architecture binaries (`arm64-v8a`, `armeabi-v7a`, `x86_64`).

## Installation

Begin by cloning the repository and initializing the required `sing-box` submodules:

```bash
git clone https://github.com/zerof/AegisNet.git
cd AegisNet
git submodule update --init --recursive
```

## Build Instructions

To build the native components, you must have **Go** and the **Android NDK** configured on your machine.

1. Export your NDK home directory:
   ```bash
   export ANDROID_NDK_HOME=/path/to/android/ndk
   ```
2. Run the build script to compile the shared library binaries:
   ```bash
   chmod +x build-scripts/build-singbox.sh
   ./build-scripts/build-singbox.sh
   ```
3. Open the `AegisNet/` root folder in **Android Studio**.
4. Sync Gradle and run the Application on an Emulator or Physical Device.

## Continuous Integration (CI/CD)

AegisNet ships with a comprehensive GitHub Actions ecosystem ensuring deterministic builds and security.

**Workflows**:
- **Android Debug CI** (`android-debug.yml`): Triggers on PR/Pushes. Builds `assembleDebug` with short-hash versioning.
- **Android Release CI** (`android-release.yml`): Triggers on `v*` tags. Publishes production APKs to GitHub Releases.
- **Nightly Build** (`nightly-build.yml`): Automatic daily builds (`0 3 * * *`) with `YYYYMMDD` datestamps.
- **SingBox Native** (`build-singbox.yml`): Validates Core C-Shared Go/NDK matrix integrity.
- **Security Scan** (`codeql-analysis.yml`): Scans Java, Kotlin, and C++ paths using GitHub CodeQL weekly.
- **Dependency Guard**: Automated `Dependabot` monitoring for Gradle and Actions updates.

*To activate release attachments*, navigate to **Settings > Actions > General > Workflow permissions** and select **Read and write permissions**.

## Configuration System

AegisNet utilizes a dynamic `ConfigGenerator` responding to state mutations natively.

1. **`UserSettings` Model**: Intercepts active DataStore settings (e.g., Target WireGuard profile, active DNS).
2. **Builders**: Delegates generation to isolated stateless builders (`FilterConfigBuilder`, `RoutingConfigBuilder`, `DNSConfigBuilder`).
3. **Caching**: Merges the builder arrays into a cohesive JSON string, writes it instantly to `context.filesDir/config.json`, and triggers the JNI Engine bridging the `tun` FileDescriptor.

## Filter Engine & Rule Formats

The app maintains a distinct module parsing rules linearly:
- **RuleStore**: Manages `ExactDomainMap`, `DomainSuffixTrie`, and `KeywordMatcher`.
- Supported Syntaxes:
  - AdGuard (`||example.com^`, `@@||whitelisted.com^`)
  - uBlock (`||ads.example.com^`)
  - Clash (`DOMAIN-SUFFIX,tracker.com,REJECT`)

Modifiers parsing UI/CSS cosmetic blocking (`##`, `#@#`) are automatically expunged before reaching memory.

## WireGuard Profile Support

WireGuard support abandons Android kernel modules in favor of `sing-box`'s native outbound implementation.
- You can import single `profile.conf` definitions.
- You can import batch archives via `profiles.zip`, inflating endpoints natively.
- While multiple profiles persist safely inside the `RoomDatabase`, only one profile maps to `RoutingConfigBuilder` simultaneously.

## UI Screens

The User Interface leverages standard Material 3 Jetpack Compose components:
- **Dashboard**: High-level real-time overview displaying TX/RX traffic scopes and the master VPN power toggle.
- **Filters**: Add and toggle specific Lists synchronously invoking URL triggers.
- **DNS**: Append explicit Upstream DNS profiles (e.g., DoH endpoint configurations).
- **VPN**: Inspect configurations locally.
- **Settings**: Control WorkManager WiFi Update parameters and tweak underlying Engine bounds (e.g., QUIC Blocking).
- **Logs**: Surface debug output files.

## Contributing

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingUpdate`).
3. Commit your Changes (`git commit -m 'Add some AmazingUpdate'`).
4. Push to the Branch (`git push origin feature/AmazingUpdate`).
5. Open a Pull Request.

## License

This project is licensed under the [MIT License](LICENSE).

**Third-Party Code**:
- [sing-box](https://github.com/SagerNet/sing-box) adheres to the **BSD-3-Clause License**.
- Various Android Jetpack and Kotlin Coroutine libraries adhere to the Apache-2.0 License.
