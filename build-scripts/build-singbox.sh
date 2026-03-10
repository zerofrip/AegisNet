#!/bin/bash
set -e

echo "Starting sing-box native compilation for Android..."

# Ensure NDK is set
if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "ERROR: ANDROID_NDK_HOME environment variable is not set."
    echo "Please set it to your Android NDK path (e.g. ~/Android/Sdk/ndk/25.1.8937393)"
    exit 1
fi

PROJECT_ROOT=$(pwd)
JNI_DIR="$PROJECT_ROOT/core/singbox-jni"
PREBUILT_DIR="$PROJECT_ROOT/prebuilt"

mkdir -p "$PREBUILT_DIR"

cd "$JNI_DIR"

# Initialize Go module and replace with local submodule if not configured
if [ ! -f go.mod ]; then
    go mod init singbox-jni
    go mod edit -replace github.com/sagernet/sing-box=../sing-box
    go mod tidy
fi

API=26
HOST_TAG="linux-x86_64" # We assume linux for standard environments/Github Actions
TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG/bin"

build_for_arch() {
    local arch=$1
    local goarch=$2
    local goarm=$3
    local target=$4
    local outdir="$PREBUILT_DIR/$arch"
    
    mkdir -p "$outdir"
    echo "Building libsingbox.so for $arch..."
    
    export GOOS=android
    export GOARCH=$goarch
    if [ -n "$goarm" ]; then
        export GOARM=$goarm
    else
        unset GOARM
    fi
    export CGO_ENABLED=1
    export CC="$TOOLCHAIN/$target$API-clang"
    export CXX="$TOOLCHAIN/$target$API-clang++"
    
    go build -trimpath -buildmode=c-shared -o "$outdir/libsingbox.so" main.go
}

# arm64-v8a
build_for_arch "arm64-v8a" "arm64" "" "aarch64-linux-android"

# armeabi-v7a
build_for_arch "armeabi-v7a" "arm" "7" "armv7a-linux-androideabi"

# x86_64
build_for_arch "x86_64" "amd64" "" "x86_64-linux-android"

echo "Build complete! Libraries are located in $PREBUILT_DIR"
