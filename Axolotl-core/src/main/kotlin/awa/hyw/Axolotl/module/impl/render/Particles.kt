package awa.hyw.Axolotl.module.impl.render

import awa.hyw.Axolotl.module.Module

object Particles : Module("Particles", Category.RENDER) {
    val showFirstPerson by setting("show-first-person", false)
    val blockBreaking by setting("block-breaking", true)
}

