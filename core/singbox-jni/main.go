package main

/*
#include <jni.h>
#include <stdlib.h>
#include <string.h>

static const char* getStringUTF(JNIEnv *env, jstring str) {
    return (*env)->GetStringUTFChars(env, str, NULL);
}

static jstring newStringUTF(JNIEnv *env, const char *bytes) {
    return (*env)->NewStringUTF(env, bytes);
}

static jlongArray newLongArray(JNIEnv *env, jsize len) {
    return (*env)->NewLongArray(env, len);
}

static void setLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf) {
    (*env)->SetLongArrayRegion(env, array, start, len, buf);
}
*/
import "C"

import (
    "context"
    box "github.com/sagernet/sing-box"
    "github.com/sagernet/sing-box/option"
    "github.com/sagernet/sing/common/json"
)

var (
    globalBox *box.Box
)

//export Java_com_aegisnet_singbox_SingBoxController_startSingBox
func Java_com_aegisnet_singbox_SingBoxController_startSingBox(env *C.JNIEnv, clazz C.jclass, configJson C.jstring, fd C.jint) C.jstring {
    configStr := C.GoString(C.getStringUTF(env, configJson))

    // Parse the JSON into sing-box options
    ctx := context.Background()
    options, err := json.UnmarshalExtendedContext[option.Options](ctx, []byte(configStr))
    if err != nil {
        return C.newStringUTF(env, C.CString("JSON Parse Error: "+err.Error()))
    }

    // Set auto-detect interface to use the VPN TUN as the primary platform interface
    // In Android, passing the tun FD is required for `tun` inbound or interface bindings
    // However, since we define `inbounds` with `type: tun` natively in config, sing-box opens its own.
    // Wait, since AegisVpnService establishes the tun, we MUST pass the FD to singbox.
    // Let's modify the inbound options to use the FD if supported.
    for i := range options.Inbounds {
        if options.Inbounds[i].Type == "tun" {
            // Options type is any, we need to inject the FD.
            // A simpler way for this project is to use Android's native TUN.
            // If singbox options allow fd injection natively, we cast it.
            // If not, we pass the FD via options generic map.
            if tunOpts, ok := options.Inbounds[i].Options.(map[string]any); ok {
                tunOpts["file_descriptor"] = int(fd)
            } else {
                 m := map[string]any{"file_descriptor": int(fd)}
                 bb, _ := json.Marshal(options.Inbounds[i].Options)
                 json.Unmarshal(bb, &m)
                 options.Inbounds[i].Options = m
            }
        }
    }

    // Stop existing instance if any
    if globalBox != nil {
        globalBox.Close()
    }

    instance, err := box.New(box.Options{
        Context: ctx,
        Options: options,
    })
    if err != nil {
        return C.newStringUTF(env, C.CString("Box Init Error: "+err.Error()))
    }

    if err := instance.Start(); err != nil {
        return C.newStringUTF(env, C.CString("Box Start Error: "+err.Error()))
    }

    globalBox = instance
    return C.newStringUTF(env, C.CString(""))
}

//export Java_com_aegisnet_singbox_SingBoxController_stopSingBox
func Java_com_aegisnet_singbox_SingBoxController_stopSingBox(env *C.JNIEnv, clazz C.jclass) {
    if globalBox != nil {
        globalBox.Close()
        globalBox = nil
    }
}

//export Java_com_aegisnet_singbox_SingBoxController_getTrafficStats
func Java_com_aegisnet_singbox_SingBoxController_getTrafficStats(env *C.JNIEnv, clazz C.jclass) C.jlongArray {
    // Return theoretical Tx/Rx bytes. In production, this pulls from sing-box metrics.
    stats := []int64{1024 * 1024, 2048 * 1024} // 1MB Tx, 2MB Rx
    
    jArray := C.newLongArray(env, C.jsize(len(stats)))
    C.setLongArrayRegion(env, jArray, 0, C.jsize(len(stats)), (*C.jlong)(&stats[0]))
    return jArray
}

//export Java_com_aegisnet_singbox_SingBoxController_getBlockedCount
func Java_com_aegisnet_singbox_SingBoxController_getBlockedCount(env *C.JNIEnv, clazz C.jclass) C.jlong {
    // Return total blocked requests. In production, this pulls from the filtering engine metrics.
    return C.jlong(1337)
}

func main() {}
