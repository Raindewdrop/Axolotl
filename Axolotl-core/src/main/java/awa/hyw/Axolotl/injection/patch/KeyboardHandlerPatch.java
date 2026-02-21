package awa.hyw.Axolotl.injection.patch;

import awa.hyw.Axolotl.Axolotl;
import awa.hyw.Axolotl.event.impl.KeyEvent;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;
import net.minecraft.client.KeyboardHandler;

@Patch(KeyboardHandler.class)
public class KeyboardHandlerPatch {
    @Inject(method = "keyPress", desc = "(JIIII)V")
    public static void keyPress(KeyboardHandler instance, long pWindowPointer, int pKey, int pScanCode,
                         int pAction, int pModifiers, CallbackInfo ci) {
        if (pWindowPointer != Axolotl.mc.getWindow().getWindow()) return;
        if (new KeyEvent(pKey, pScanCode, pAction, pModifiers).post().isCancelled()) {
            ci.cancel();
        }
    }
}

