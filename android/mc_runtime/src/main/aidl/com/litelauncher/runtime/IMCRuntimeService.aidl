// IMCRuntimeService.aidl — AIDL contract between Flutter app and MC Runtime process
package com.litelauncher.runtime;

interface IMCRuntimeService {
    // Start the game in MC Runtime process
    // gameDir:   .minecraft directory path
    // javaHome:  JRE root path
    // classpath: colon-separated JAR list
    // mainClass: MC main class (e.g. net.minecraft.client.main.Main)
    // jvmArgs:   JVM arguments (e.g. -Xmx4G)
    // mcArgs:    Minecraft game arguments
    // Returns:   PID of the JVM process on success, -1 on failure
    int startGame(in String gameDir, in String javaHome, in String classpath,
                  in String mainClass, in String[] jvmArgs, in String[] mcArgs);

    // Stop the game
    void stopGame();

    // Get IPC shared memory file descriptor for status monitoring
    int getIpcFd();

    // Pass a Surface for rendering (from Flutter Texture widget)
    void setSurface(in Surface surface);

    // Inject input events (touch/keyboard) — encoded int array
    void injectInput(in int[] events);

    // Enable/disable audio
    void setAudioEnabled(boolean enabled);

    // Set render mode
    void setRenderMode(int mode);

    // Get crash report from shared memory
    String getCrashReport();

    // Get engine state (0=idle, 1=jvm_loading, 3=surface_ready, 4=running, 5=crashed, 6=stopped)
    int getState();
}
