// LiteLauncher 引擎 — 公共入口
// 引擎初始化、JNI 注册、全局状态
#ifndef LL_ENGINE_H
#define LL_ENGINE_H

#include <jni.h>
#include <stdbool.h>
#include <android/log.h>

#define LL_TAG "LiteLauncherEngine"
#define LL_LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LL_TAG, __VA_ARGS__)
#define LL_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LL_TAG, __VA_ARGS__)

// 全局引擎状态
typedef enum {
    LL_STATE_IDLE,
    LL_STATE_JVM_LOADING,
    LL_STATE_JVM_READY,
    LL_STATE_SURFACE_READY,
    LL_STATE_RUNNING,
    LL_STATE_CRASHED,
    LL_STATE_STOPPED
} ll_engine_state_t;

// 渲染模式
typedef enum {
    LL_RENDER_UNKNOWN = 0,
    LL_RENDER_LWJGL2_GL4ES = 1,     // ≤1.12: LWJGL2 + GL4ES → GLES 2.0
    LL_RENDER_LWJGL3_MOBILEGLUES = 2, // 1.13-1.16: LWJGL3 + MobileGlues → GLES 3.1
    LL_RENDER_LWJGL3_ZINK = 3       // ≥1.17: LWJGL3 + Zink → Vulkan
} ll_render_mode_t;

// 引擎初始化
bool ll_engine_init(void);

// 获取引擎状态
ll_engine_state_t ll_engine_get_state(void);

// 引擎关闭
void ll_engine_shutdown(void);

// ====== JNI 入口 ======
// 从 Kotlin MCRuntimeService 调用

JNIEXPORT jboolean JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeInitEngine(JNIEnv* env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeBootstrapJVM(
    JNIEnv* env, jobject thiz,
    jstring jrePath,
    jstring classpath,
    jstring mainClass,
    jobjectArray jvmArgs,
    jobjectArray mcArgs
);

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeSetSurface(
    JNIEnv* env, jobject thiz, jobject surface
);

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeInjectInput(
    JNIEnv* env, jobject thiz, jintArray events
);

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeStop(JNIEnv* env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeGetCrashReport(JNIEnv* env, jobject thiz);

JNIEXPORT jint JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeGetState(JNIEnv* env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeSetRenderMode(
    JNIEnv* env, jobject thiz, jint mode
);

#endif // LL_ENGINE_H
