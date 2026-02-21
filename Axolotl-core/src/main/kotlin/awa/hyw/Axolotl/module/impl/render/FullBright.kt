package awa.hyw.Axolotl.module.impl.render

import awa.hyw.Axolotl.module.Module

object FullBright : Module("FullBright", Category.RENDER) {
    val gamma by setting("gamma", mc.options.gamma().get(), 0.1..15.0, 0.1)
}

