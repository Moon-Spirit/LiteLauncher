// JVM 引导 — 通过 dlopen/dlsym 加载 JLI_Launch
#include "jvm_bootstrap.h"
#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

// JLI_Launch 函数签名（来自 JDK libjli）
typedef int (*jli_launch_t)(int argc, char** argv,
                             int jargc, const char** jargv,
                             int appclassc, const char** appclassv,
                             const char* fullversion,
                             const char* dotversion,
                             const char* pname,
                             const char* lname,
                             jboolean javaargs,
                             jboolean cpwildcard,
                             jboolean javaw,
                             jint ergo);

bool ll_jvm_bootstrap(
    const char* jre_path,
    const char* classpath,
    const char* main_class,
    const char* const* jvm_args, int jvm_args_count,
    const char* const* mc_args,  int mc_args_count,
    ll_jvm_result_t* result
) {
    result->exit_code = -1;
    result->crash_report[0] = '\0';

    // 1. 构建 JLI 库路径
    char jli_path[512];
    snprintf(jli_path, sizeof(jli_path), "%s/lib/jli/libjli.so", jre_path);

    // 2. 动态加载 libjli
    void* jli_handle = dlopen(jli_path, RTLD_NOW | RTLD_GLOBAL);
    if (!jli_handle) {
        snprintf(result->crash_report, sizeof(result->crash_report),
                 "无法加载 libjli: %s\n路径: %s", dlerror(), jli_path);
        LL_LOGE("%s", result->crash_report);
        return false;
    }

    // 3. 查找 JLI_Launch 符号
    jli_launch_t jli_launch = (jli_launch_t)dlsym(jli_handle, "JLI_Launch");
    if (!jli_launch) {
        snprintf(result->crash_report, sizeof(result->crash_report),
                 "无法找到 JLI_Launch 符号: %s", dlerror());
        LL_LOGE("%s", result->crash_report);
        dlclose(jli_handle);
        return false;
    }

    // 4. 构建完整参数列表
    // 格式: java [JVM args] -cp <classpath> <main class> [MC args]
    int total_args = jvm_args_count + mc_args_count + 3; // +3: -cp, classpath, main_class
    char* argv[total_args + 1]; // +1: NULL 终止
    int argc = total_args;

    int idx = 0;
    // JVM 参数
    for (int i = 0; i < jvm_args_count; i++) {
        argv[idx++] = (char*)jvm_args[i];
    }
    // -cp classpath
    argv[idx++] = (char*)"-cp";
    argv[idx++] = (char*)classpath;
    // 主类
    argv[idx++] = (char*)main_class;
    // MC 参数
    for (int i = 0; i < mc_args_count; i++) {
        argv[idx++] = (char*)mc_args[i];
    }
    argv[idx] = NULL;

    LL_LOGI("启动 JVM: %s 类 %s", jre_path, main_class);
    LL_LOGI("  总参数: %d (JVM: %d, MC: %d)", argc, jvm_args_count, mc_args_count);

    // 5. 调用 JLI_Launch（阻塞直到游戏退出）
    // 参数含义见 JDK 源码 jli_util.h
    result->exit_code = jli_launch(
        argc, argv,           // 总参数
        1, (const char*[]){""},  // jargc, jargv (额外参数，通常空)
        0, NULL,              // appclassc, appclassv (应用类)
        "",                    // fullversion
        "",                    // dotversion
        "LiteLauncher",        // pname (程序名)
        "LiteLauncher",        // lname (启动器名)
        JNI_FALSE,             // javaargs
        JNI_TRUE,              // cpwildcard (支持通配符类路径)
        JNI_FALSE,             // javaw
        0                      // ergo
    );

    LL_LOGI("JVM 退出，退出码: %d", result->exit_code);

    dlclose(jli_handle);
    return true;
}
