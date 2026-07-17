package java.lang.module;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class ModuleFinder {
    public static ModuleFinder ofSystem() { return new ModuleFinder(); }
    public Optional<ModuleDescriptor> find(String name) { return Optional.empty(); }
    public Set<ModuleDescriptor> findAll() { return Collections.emptySet(); }
}
