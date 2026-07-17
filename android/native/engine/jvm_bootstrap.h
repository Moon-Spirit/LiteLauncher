// JVM 引导 — 通过 JLI_Launch 启动嵌入式 JVM
#ifndef LL_JVM_BOOTSTRAP_H
#define LL_JVM_BOOTSTRAP_H

#include <stdbool.h>
#include "engine.h"

// JVM 引导结果
typedef struct {
    int  exit_code;          // 0 = 正常退出
    char crash_report[4096]; // 崩溃信息（JVM 异常退出时填充）
} ll_jvm_result_t;

// 引导 JVM 并等待 Minecraft 退出
// jre_path:    JRE 根目录（如 /data/data/.../jre/17）
// classpath:   类路径（用 : 分隔的 JAR 列表）
// main_class:  主类名（如 net.minecraft.client.main.Main）
// jvm_args:    JVM 参数数组
// mc_args:     Minecraft 游戏参数数组
// jvm_args_count / mc_args_count: 参数个数
// result:      输出 — JVM 退出码 + 崩溃信息
// 返回:        true 成功启动，false 加载失败
bool ll_jvm_bootstrap(
    const char* jre_path,
    const char* classpath,
    const char* main_class,
    const char* const* jvm_args, int jvm_args_count,
    const char* const* mc_args,  int mc_args_count,
    ll_jvm_result_t* result
);

#endif // LL_JVM_BOOTSTRAP_H
