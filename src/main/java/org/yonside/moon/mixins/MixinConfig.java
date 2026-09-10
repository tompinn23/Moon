package org.yonside.moon.mixins;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.yonside.moon.config.CoreConfig;
import org.yonside.moon.config.Feature;

import java.util.List;
import java.util.Set;

public class MixinConfig implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE = "org.yonside.moon.mixins.";

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String relativeClassName = mixinClassName.substring(MIXIN_PACKAGE.length());
        String packageName = relativeClassName.substring(0, relativeClassName.lastIndexOf('.'));
        if(packageName.isEmpty()) {
            return true;
        }
        // if there is a feature check if its enabled, if this mixin/package does not map to a feature run it.
        return Feature.from(packageName).map(CoreConfig::isEnabled).orElse(true);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
