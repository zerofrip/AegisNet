package main

/*
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>

#define LOG_TAG "AegisSingBox"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static void log_info(const char* msg) {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%s", msg);
}

static void log_error(const char* msg) {
    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "%s", msg);
}

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

static jmethodID getMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
    return (*env)->GetMethodID(env, clazz, name, sig);
}

static jboolean callBooleanMethod(JNIEnv *env, jobject obj, jmethodID methodID, jint arg) {
    return (*env)->CallBooleanMethod(env, obj, methodID, arg);
}

static void deleteGlobalRef(JNIEnv *env, jobject obj) {
    (*env)->DeleteGlobalRef(env, obj);
}

static jobject newGlobalRef(JNIEnv *env, jobject obj) {
    return (*env)->NewGlobalRef(env, obj);
}

static jclass getObjectClass(JNIEnv *env, jobject obj) {
    return (*env)->GetObjectClass(env, obj);
}

static JavaVM* getJavaVM(JNIEnv *env) {
    JavaVM *vm;
    (*env)->GetJavaVM(env, &vm);
    return vm;
}

static JNIEnv* getEnv(JavaVM *vm) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return NULL;
    }
    return env;
}

static JNIEnv* attachCurrentThread(JavaVM *vm) {
    JNIEnv *env;
    (*vm)->AttachCurrentThread(vm, &env, NULL);
    return env;
}

static void detachCurrentThread(JavaVM *vm) {
    (*vm)->DetachCurrentThread(vm);
}

static jboolean isNull(jobject obj) {
    return obj == NULL;
}

static jboolean isMethodNull(jmethodID method) {
    return method == NULL;
}
*/
import "C"

import (
	"context"
	"fmt"
	"os"
	"strconv"
	"strings"
	"sync/atomic"
	"syscall"
	"unsafe"

	box "github.com/sagernet/sing-box"
	"github.com/sagernet/sing-box/adapter"
	"github.com/sagernet/sing-box/include"
	"github.com/sagernet/sing-box/option"
	tun "github.com/sagernet/sing-tun"
	"github.com/sagernet/sing/common/json"
	"github.com/sagernet/sing/common/logger"
	"github.com/sagernet/sing/service"
)

var (
	globalBox         *box.Box
	javaVM            *C.JavaVM
	javaControllerObj C.jobject
	javaProtectMethod C.jmethodID
	activeTunFd       int

	blockedCount atomic.Int64
)

// platformInterface implements adapter.PlatformInterface for Android VpnService.
type platformInterface struct{}

func (p *platformInterface) Initialize(_ adapter.NetworkManager) error { return nil }

func (p *platformInterface) UsePlatformAutoDetectInterfaceControl() bool { return true }

func (p *platformInterface) AutoDetectInterfaceControl(fd int) error {
	if javaVM == nil || C.isNull(javaControllerObj) != 0 || C.isMethodNull(javaProtectMethod) != 0 {
		return nil
	}
	env := C.getEnv(javaVM)
	detached := false
	if env == nil {
		env = C.attachCurrentThread(javaVM)
		detached = true
	}
	res := C.callBooleanMethod(env, javaControllerObj, javaProtectMethod, C.jint(fd))
	msg := C.CString(fmt.Sprintf("protected socket: fd=%d res=%v", fd, res != 0))
	if res == 0 {
		C.log_error(msg)
	} else {
		C.log_info(msg)
	}
	C.free(unsafe.Pointer(msg))
	if detached {
		C.detachCurrentThread(javaVM)
	}
	return nil
}

func (p *platformInterface) UsePlatformInterface() bool { return true }

// OpenInterface is called by sing-box when starting the TUN inbound.
// It dups the Android VpnService TUN fd stored in activeTunFd.
func (p *platformInterface) OpenInterface(opts *tun.Options, _ option.TunPlatformOptions) (tun.Tun, error) {
	if activeTunFd <= 0 {
		return nil, fmt.Errorf("no active TUN fd")
	}
	dupFd, err := syscall.Dup(activeTunFd)
	if err != nil {
		return nil, fmt.Errorf("dup TUN fd: %w", err)
	}
	opts.FileDescriptor = dupFd
	if opts.Name == "" {
		opts.Name = "vpntun0"
	}
	return tun.New(*opts)
}

func (p *platformInterface) UsePlatformDefaultInterfaceMonitor() bool { return false }
func (p *platformInterface) CreateDefaultInterfaceMonitor(_ logger.Logger) tun.DefaultInterfaceMonitor {
	return nil
}
func (p *platformInterface) UsePlatformNetworkInterfaces() bool                     { return false }
func (p *platformInterface) NetworkInterfaces() ([]adapter.NetworkInterface, error) { return nil, nil }
func (p *platformInterface) UnderNetworkExtension() bool                            { return false }
func (p *platformInterface) NetworkExtensionIncludeAllNetworks() bool               { return false }
func (p *platformInterface) ClearDNSCache()                                         {}
func (p *platformInterface) RequestPermissionForWIFIState() error                   { return nil }
func (p *platformInterface) ReadWIFIState() adapter.WIFIState                       { return adapter.WIFIState{} }
func (p *platformInterface) SystemCertificates() []string                           { return nil }
func (p *platformInterface) UsePlatformConnectionOwnerFinder() bool                 { return false }
func (p *platformInterface) FindConnectionOwner(_ *adapter.FindConnectionOwnerRequest) (*adapter.ConnectionOwner, error) {
	return nil, nil
}
func (p *platformInterface) UsePlatformWIFIMonitor() bool                   { return false }
func (p *platformInterface) UsePlatformNotification() bool                  { return false }
func (p *platformInterface) SendNotification(_ *adapter.Notification) error { return nil }
func (p *platformInterface) UsePlatformNeighborResolver() bool              { return false }
func (p *platformInterface) StartNeighborMonitor(_ adapter.NeighborUpdateListener) error {
	return nil
}
func (p *platformInterface) CloseNeighborMonitor(_ adapter.NeighborUpdateListener) error {
	return nil
}

//export Java_com_aegisnet_singbox_SingBoxController_startSingBox
func Java_com_aegisnet_singbox_SingBoxController_startSingBox(env *C.JNIEnv, clazz C.jclass, configJson C.jstring, fd C.jint) C.jstring {
	configStr := C.GoString(C.getStringUTF(env, configJson))
	msg := C.CString(fmt.Sprintf("Starting sing-box with config length: %d", len(configStr)))
	C.log_info(msg)
	C.free(unsafe.Pointer(msg))

	// Initialize JNI references for protection callback
	javaVM = C.getJavaVM(env)

	if C.isNull(javaControllerObj) == 0 {
		C.deleteGlobalRef(env, javaControllerObj)
	}
	javaControllerObj = C.newGlobalRef(env, C.jobject(clazz))

	cls := C.getObjectClass(env, javaControllerObj)
	javaProtectMethod = C.getMethodID(env, cls, C.CString("protect"), C.CString("(I)Z"))

	if C.isMethodNull(javaProtectMethod) != 0 {
		msgE := C.CString("Could not find protect(I)Z method in SingBoxController")
		C.log_error(msgE)
		C.free(unsafe.Pointer(msgE))
	}

	// Store TUN fd — used by OpenInterface() callback during instance.Start()
	activeTunFd = int(fd)

	// Parse the JSON into sing-box options
	baseCtx := context.Background()
	options, err := json.UnmarshalExtendedContext[option.Options](baseCtx, []byte(configStr))
	if err != nil {
		return C.newStringUTF(env, C.CString("JSON Parse Error: "+err.Error()))
	}

	// Stop existing instance if any
	if globalBox != nil {
		msgS := C.CString("Stopping existing sing-box instance")
		C.log_info(msgS)
		C.free(unsafe.Pointer(msgS))
		globalBox.Close()
		globalBox = nil
	}

	// Register all protocol handlers and attach the platform interface
	ctx := include.Context(baseCtx)
	ctx = service.ContextWith[adapter.PlatformInterface](ctx, &platformInterface{})

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
	msgO := C.CString("sing-box instance started successfully")
	C.log_info(msgO)
	C.free(unsafe.Pointer(msgO))

	// Reset blocked count for new session
	blockedCount.Store(0)

	return C.newStringUTF(env, C.CString(""))
}

//export Java_com_aegisnet_singbox_SingBoxController_stopSingBox
func Java_com_aegisnet_singbox_SingBoxController_stopSingBox(env *C.JNIEnv, clazz C.jclass) {
	if globalBox != nil {
		globalBox.Close()
		globalBox = nil
		msgT := C.CString("sing-box instance stopped")
		C.log_info(msgT)
		C.free(unsafe.Pointer(msgT))
	}
}

//export Java_com_aegisnet_singbox_SingBoxController_getTrafficStats
func Java_com_aegisnet_singbox_SingBoxController_getTrafficStats(env *C.JNIEnv, clazz C.jclass) C.jlongArray {
	var tx, rx uint64
	if globalBox != nil {
		tx, rx = readTunTraffic()
	}

	stats := []int64{int64(tx), int64(rx)}
	jArray := C.newLongArray(env, C.jsize(len(stats)))
	C.setLongArrayRegion(env, jArray, 0, C.jsize(len(stats)), (*C.jlong)(&stats[0]))
	return jArray
}

//export Java_com_aegisnet_singbox_SingBoxController_getBlockedCount
func Java_com_aegisnet_singbox_SingBoxController_getBlockedCount(env *C.JNIEnv, clazz C.jclass) C.jlong {
	return C.jlong(blockedCount.Load())
}

// readTunTraffic reads real traffic counters from /proc/net/dev for the TUN interface.
// TX on tun = user upload (device → sing-box), RX on tun = user download (sing-box → device).
func readTunTraffic() (tx, rx uint64) {
	data, err := os.ReadFile("/proc/net/dev")
	if err != nil {
		return 0, 0
	}
	for _, line := range strings.Split(string(data), "\n") {
		parts := strings.SplitN(line, ":", 2)
		if len(parts) != 2 {
			continue
		}
		iface := strings.TrimSpace(parts[0])
		if !strings.HasPrefix(iface, "tun") {
			continue
		}
		fields := strings.Fields(parts[1])
		if len(fields) >= 9 {
			rx, _ = strconv.ParseUint(fields[0], 10, 64) // receive bytes = download
			tx, _ = strconv.ParseUint(fields[8], 10, 64) // transmit bytes = upload
		}
		break
	}
	return tx, rx
}

func main() {}
