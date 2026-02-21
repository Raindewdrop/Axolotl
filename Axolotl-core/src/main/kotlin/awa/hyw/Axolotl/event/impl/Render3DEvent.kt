package awa.hyw.Axolotl.event.impl

import com.mojang.blaze3d.vertex.PoseStack
import awa.hyw.Axolotl.event.Event

class Render3DEvent(val partialTicks: Float, val poseStack: PoseStack? = null): Event()

