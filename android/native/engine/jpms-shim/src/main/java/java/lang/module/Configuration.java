package java.lang.module;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class Configuration {
    private static final Configuration EMPTY = new Configuration();

    public static Configuration empty() { return EMPTY; }
    public Set<ResolvedModule> modules() { return Collections.emptySet(); }
    public Optional<ResolvedModule> findModule(String name) { return Optional.empty(); }

    public Configuration resolve(ModuleFinder before, java.util.List<Configuration> parents,
                                  ModuleFinder after, java.util.Collection<String> roots) {
        return this;
    }
    public Configuration resolveAndBind(ModuleFinder before, java.util.List<Configuration> parents,
                                         ModuleFinder after, java.util.Collection<String> roots) {
        return this;
    }
}
