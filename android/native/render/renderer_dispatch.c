// Renderer Dispatch — select GL translation layer by MC version + device tier
#include "renderer_dispatch.h"
#include <dlfcn.h>
#include <string.h>
#include <stdlib.h>

ll_render_mode_t ll_render_detect(const char* mc_version, int device_tier) {
    if (!mc_version) return LL_RENDER_LWJGL2_GL4ES;

    // extract major.minor from version string
    int major = 0, minor = 0;
    if (mc_version[0] >= '0' && mc_version[0] <= '9') {
        if (strchr(mc_version, '.') - mc_version > 2) {
            // new format: YY.D.H (e.g. "26.2")
            major = atoi(mc_version);
            const char* dot = strchr(mc_version, '.');
            if (dot) minor = atoi(dot + 1);
        } else {
            // old format: 1.x.x
            major = atoi(mc_version);
            const char* dot = strchr(mc_version, '.');
            if (dot) {
                minor = atoi(dot + 1);
                major = minor; // for 1.x.x, use minor as effective major
            }
        }
    }

    LL_LOGI("MC version parsed: string=%s, major=%d, minor=%d, tier=%d",
            mc_version, major, minor, device_tier);

    // Tier detection (device tier not from strings)
    if (major <= 12) {
        // MC <= 1.12: LWJGL2 + GL4ES
        return LL_RENDER_LWJGL2_GL4ES;
    } else if (major >= 13 && major <= 16) {
        // MC 1.13-1.16: LWJGL3 + MobileGlues (GLES 3.1)
        return LL_RENDER_LWJGL3_MOBILEGLUES;
    } else {
        // MC >= 1.17: LWJGL3 + Zink (Vulkan) if high tier, else MobileGlues
        if (device_tier >= 2) {
            return LL_RENDER_LWJGL3_ZINK;
        } else {
            return LL_RENDER_LWJGL3_MOBILEGLUES;
        }
    }
}

ll_renderer_handle_t ll_render_load(ll_render_mode_t mode) {
    const char* lib_name = NULL;
    switch (mode) {
        case LL_RENDER_LWJGL2_GL4ES:       lib_name = "libgl4es.so";     break;
        case LL_RENDER_LWJGL3_MOBILEGLUES: lib_name = "libmobileglues.so"; break;
        case LL_RENDER_LWJGL3_ZINK:        lib_name = "libOSMesa.so";    break;
        default: return NULL;
    }

    void* handle = dlopen(lib_name, RTLD_NOW | RTLD_GLOBAL);
    if (!handle) {
        LL_LOGE("Failed to load renderer %s: %s", lib_name, dlerror());
        return NULL;
    }
    LL_LOGI("Renderer loaded: %s", lib_name);
    return handle;
}

bool ll_render_init(ll_renderer_handle_t renderer, ll_egl_context_t* ctx) {
    if (!renderer) return false;

    typedef bool (*init_fn)(void*);
    init_fn init = (init_fn)dlsym(renderer, "renderer_init");
    if (!init) {
        LL_LOGE("renderer_init not found in renderer DLL");
        return false;
    }
    return init(ctx);
}

bool ll_render_swap(ll_renderer_handle_t renderer, ll_egl_context_t* ctx) {
    if (!renderer) return ll_surface_swap(ctx);

    typedef bool (*swap_fn)(void*);
    swap_fn swap = (swap_fn)dlsym(renderer, "renderer_swap");
    if (swap) return swap(ctx);
    return ll_surface_swap(ctx);
}

void ll_render_shutdown(ll_renderer_handle_t renderer) {
    if (!renderer) return;

    typedef void (*shutdown_fn)(void);
    shutdown_fn shutdown = (shutdown_fn)dlsym(renderer, "renderer_shutdown");
    if (shutdown) shutdown();
    dlclose(renderer);
}

bool ll_render_has_azdo(void) {
    const char* glExt = (const char*)glGetString(GL_EXTENSIONS);
    if (!glExt) return false;
    return strstr(glExt, "multi_draw_indirect") != NULL ||
           strstr(glExt, "GL_EXT_multi_draw_indirect") != NULL;
}
