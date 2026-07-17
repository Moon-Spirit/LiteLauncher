// Surface Bridge — ANativeWindow to EGL context
#include "surface_bridge.h"
#include <string.h>

static float g_fps = 0.0f;
static int g_dropped_total = 0;

bool ll_surface_create(ANativeWindow* window, ll_egl_context_t* ctx, ll_render_mode_t mode) {
    (void)mode;
    memset(ctx, 0, sizeof(*ctx));

    ctx->display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (ctx->display == EGL_NO_DISPLAY) {
        LL_LOGE("eglGetDisplay failed");
        return false;
    }

    EGLint major, minor;
    if (!eglInitialize(ctx->display, &major, &minor)) {
        LL_LOGE("eglInitialize failed: 0x%x", eglGetError());
        return false;
    }
    LL_LOGI("EGL version %d.%d", major, minor);

    const EGLint attribs[] = {
        EGL_SURFACE_TYPE,    EGL_WINDOW_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_RED_SIZE,        8,
        EGL_GREEN_SIZE,      8,
        EGL_BLUE_SIZE,       8,
        EGL_ALPHA_SIZE,      8,
        EGL_DEPTH_SIZE,      24,
        EGL_STENCIL_SIZE,    8,
        EGL_NONE
    };
    EGLint num_configs;
    if (!eglChooseConfig(ctx->display, attribs, &ctx->config, 1, &num_configs) || num_configs == 0) {
        LL_LOGE("eglChooseConfig failed: 0x%x", eglGetError());
        return false;
    }

    EGLint ctx_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE };
    ctx->context = eglCreateContext(ctx->display, ctx->config, EGL_NO_CONTEXT, ctx_attribs);
    if (ctx->context == EGL_NO_CONTEXT) {
        ctx_attribs[1] = 2;
        ctx->context = eglCreateContext(ctx->display, ctx->config, EGL_NO_CONTEXT, ctx_attribs);
        if (ctx->context == EGL_NO_CONTEXT) {
            LL_LOGE("eglCreateContext failed: 0x%x", eglGetError());
            return false;
        }
        LL_LOGI("Falling back to GLES 2.0 context");
    } else {
        LL_LOGI("Using GLES 3.x context");
    }

    ctx->surface = eglCreateWindowSurface(ctx->display, ctx->config, window, NULL);
    if (ctx->surface == EGL_NO_SURFACE) {
        LL_LOGE("eglCreateWindowSurface failed: 0x%x", eglGetError());
        eglDestroyContext(ctx->display, ctx->context);
        return false;
    }

    if (!eglMakeCurrent(ctx->display, ctx->surface, ctx->surface, ctx->context)) {
        LL_LOGE("eglMakeCurrent failed: 0x%x", eglGetError());
        return false;
    }

    ctx->width  = ANativeWindow_getWidth(window);
    ctx->height = ANativeWindow_getHeight(window);
    ctx->refresh_rate = 60;

    LL_LOGI("EGL context ready: %dx%d @ %dHz", ctx->width, ctx->height, ctx->refresh_rate);
    return true;
}

bool ll_surface_swap(ll_egl_context_t* ctx) {
    if (!ctx || ctx->surface == EGL_NO_SURFACE) return false;
    if (!eglSwapBuffers(ctx->display, ctx->surface)) {
        g_dropped_total++;
        return false;
    }
    return true;
}

void ll_surface_destroy(ll_egl_context_t* ctx) {
    if (!ctx) return;
    if (ctx->display != EGL_NO_DISPLAY) {
        eglMakeCurrent(ctx->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (ctx->surface  != EGL_NO_SURFACE) eglDestroySurface(ctx->display, ctx->surface);
        if (ctx->context  != EGL_NO_CONTEXT) eglDestroyContext(ctx->display, ctx->context);
        eglTerminate(ctx->display);
    }
    memset(ctx, 0, sizeof(*ctx));
}

bool ll_surface_has_extension(const char* name) {
    const char* exts = (const char*)eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS);
    if (!exts || !name) return false;
    return strstr(exts, name) != NULL;
}

float ll_surface_get_fps(void) { return g_fps; }

void ll_surface_adaptive_vsync(ll_egl_context_t* ctx) {
    static int samples     = 0;
    static float sum_time  = 0;
    static int slow_count  = 0;

    samples++;
    if (samples >= 60) {
        g_fps = 60.0f / (sum_time > 0 ? sum_time : 1.0f);
        if (g_fps < 30.0f) slow_count++;
        else slow_count = 0;

        if (slow_count > 5 && ctx->refresh_rate > 60) {
            LL_LOGI("Adaptive: capping to 30fps (FPS=%.1f)", g_fps);
            ctx->refresh_rate = 30;
        }
        samples = 0; sum_time = 0;
    }
    (void)ctx;
}
