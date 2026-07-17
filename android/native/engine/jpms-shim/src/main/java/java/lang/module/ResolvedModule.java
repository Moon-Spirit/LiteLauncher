package java.lang.module;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class ResolvedModule {
    public String name() { return ""; }
    public Set<ModuleDescriptor> reads() { return Collections.emptySet(); }
    public Configuration configuration() { return Configuration.empty(); }
}
