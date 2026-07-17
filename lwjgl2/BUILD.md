// LWJGL2 Android fork 构建配置
/*
构建步骤:
1. git submodule add https://github.com/PojavLauncherTeam/lwjgl2.git lwjgl2
2. cd lwjgl2
3. 修改 Display.java:
   - Display.create → EGL 初始化 (LLSurfaceBridge)
   - Mouse.poll → 读取 input ring buffer
   - Keyboard.poll → 读取 input ring buffer
4. bash build_android.sh
5. 产物: lwjgl2/build/lwjgl.jar + liblwjgl.so
*/
