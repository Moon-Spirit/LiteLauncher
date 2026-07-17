package java.lang.module;

import java.util.Collections;
import java.util.Set;

public final class ModuleDescriptor {
    private final String name;

    ModuleDescriptor(String name) { this.name = name; }
    public String name() { return name; }

    public static Builder newModule(String name) { return new Builder(name); }
    public Set<Requires> requires() { return Collections.emptySet(); }
    public Set<Exports> exports() { return Collections.emptySet(); }

    public static final class Builder {
        private final String name;
        Builder(String name) { this.name = name; }
        public ModuleDescriptor build() { return new ModuleDescriptor(name); }
    }

    public static final class Requires { }
    public static final class Exports { }
}
