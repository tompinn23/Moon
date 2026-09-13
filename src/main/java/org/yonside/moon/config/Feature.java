package org.yonside.moon.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Feature {

    VOIDMINER("voidminer"),
    AE2_DOWNTIER("ae2downtier"),;

    private static final Map<String, Feature> MIXIN_SUBPACKAGES;

    static {
        MIXIN_SUBPACKAGES = new HashMap<>();
        for (Feature feature : Feature.values()) {
            MIXIN_SUBPACKAGES.put(feature.mixinPackage, feature);
        }
    }

    private final String mixinPackage;

    Feature(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }

    public static Optional<Feature> from(String mixinPackage) {
        return Optional.ofNullable(MIXIN_SUBPACKAGES.get(mixinPackage));
    }
}
