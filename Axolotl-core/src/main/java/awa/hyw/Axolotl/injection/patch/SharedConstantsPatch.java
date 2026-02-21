package awa.hyw.Axolotl.injection.patch;

import net.minecraft.SharedConstants;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;

/**
 * Skidded from <a href="https://github.com/astei/lazydfu">LazyDFU</a>
 */
@Patch(SharedConstants.class)
public class SharedConstantsPatch {
    @Inject(method = "enableDataFixerOptimizations", desc = "()V")
    public static void enableDataFixerOptimizations(CallbackInfo ci) {
        ci.cancelled = true;
    }
}


