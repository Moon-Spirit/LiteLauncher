// JPMS Compatibility Shim for Forge/NeoForge >= 1.13 on Android JVM
// Android libcore lacks java.lang.module.* — these stubs prevent NoClassDefFoundError
package java.lang.module;

import java.lang.Module;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class ModuleLayer {
    public static ModuleLayer boot() { return new ModuleLayer(); }
    public static ModuleLayer empty() { return new ModuleLayer(); }
    public Set<Module> modules() { return Collections.emptySet(); }
    public Optional<Module> findModule(String name) { return Optional.empty(); }
    public Configuration configuration() { return Configuration.empty(); }
}
