package com.gberguy.ecpatches.core;

import java.io.File;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("EternalConfluenceTweaks")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"com.gberguy.ecpatches.core."})
public final class LoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.gberguy.ecpatches.core.PatchTransformer"};
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
        Object directory = data.get("mcLocation");
        PatchSettings.initialize(directory instanceof File ? (File) directory : new File(System.getProperty("user.dir")));
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
