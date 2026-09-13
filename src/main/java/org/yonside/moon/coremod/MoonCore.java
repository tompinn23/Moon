package org.yonside.moon.coremod;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yonside.moon.config.CoreConfig;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({ "org.yonside.moon.coremod.", "org.yonside.moon.config." })
public class MoonCore implements IFMLLoadingPlugin {

    public static final Logger LOG = LogManager.getLogger("MoonCore");

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "org.yonside.moon.coremod.NHCMScriptTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        File mcLocation = (File) data.get("mcLocation");
        File configDir = new File(mcLocation, "config");
        configDir.mkdir();

        CoreConfig.loadFrom(configDir);
        LOG.info("MoonCore loaded!");
        LOG.info(
            "MoonCore is going to transform the following NHCM scripts: {}",
            CoreConfig.rulesByClass()
                .keySet());
        NHCMScriptTransformer.setRules(CoreConfig.rulesByClass());
    }

    @Override
    public String getAccessTransformerClass() {
        return "";
    }
}
