package com.litelauncher.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * MC Runtime Service — 运行在独立进程 (:mc_runtime) 中
 * 负责加载嵌入式 JVM、管理 Surface、分发输入事件
 */
class MCRuntimeService : Service() {

    companion object {
        private const val TAG = "MCRuntimeService"

        init {
            System.loadLibrary("litelauncher_engine")
        }
    }

    // native 方法
    private external fun nativeInitEngine(): Boolean
    private external fun nativeBootstrapJVM(
        jrePath: String,
        classpath: String,
        mainClass: String,
        jvmArgs: Array<String>,
        mcArgs: Array<String>
    ): Boolean

    private external fun nativeSetSurface(surface: android.view.Surface)
    private external fun nativeInjectInput(events: IntArray)
    private external fun nativeStop()
    private external fun nativeGetCrashReport(): String
    private external fun nativeGetState(): Int
    private external fun nativeSetRenderMode(mode: Int)

    // AIDL Binder 实现
    private val binder = object : IMCRuntimeService.Stub() {
        override fun startGame(
            gameDir: String,
            javaHome: String,
            classpath: String,
            mainClass: String,
            jvmArgs: Array<String>,
            mcArgs: Array<String>
        ): Int {
            Log.i(TAG, "startGame: $mainClass, jre=$javaHome")

            val ok = nativeBootstrapJVM(javaHome, classpath, mainClass, jvmArgs, mcArgs)
            return if (ok) android.os.Process.myPid() else -1
        }

        override fun stopGame() {
            Log.i(TAG, "stopGame")
            nativeStop()
            stopSelf()
        }

        override fun getIpcFd(): Int {
            // IPC fd will be passed via shared memory
            // For now return a placeholder; the actual fd exchange happens through the engine
            return -1
        }

        override fun setSurface(surface: android.view.Surface?) {
            surface?.let { nativeSetSurface(it) }
        }

        override fun injectInput(events: IntArray?) {
            events?.let { nativeInjectInput(it) }
        }

        override fun setAudioEnabled(enabled: Boolean) {
            // TODO: audio bridge
        }

        override fun setRenderMode(mode: Int) {
            nativeSetRenderMode(mode)
        }

        override fun getCrashReport(): String = nativeGetCrashReport()

        override fun getState(): Int = nativeGetState()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MCRuntimeService onCreate — process ID: ${android.os.Process.myPid()}")
        val ok = nativeInitEngine()
        Log.i(TAG, "Engine init: $ok")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: LMK kill → system auto-restarts service
        Log.i(TAG, "onStartCommand flags=$flags startId=$startId")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "MCRuntimeService onDestroy")
        nativeStop()
        super.onDestroy()
    }
}
