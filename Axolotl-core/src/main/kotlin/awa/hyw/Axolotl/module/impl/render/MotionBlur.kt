package awa.hyw.Axolotl.module.impl.render

import org.lwjgl.opengl.GL45.*
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.Render3DEvent
import awa.hyw.Axolotl.graphics.GlHelper
import awa.hyw.Axolotl.graphics.buffer.VertexBufferObjects
import awa.hyw.Axolotl.graphics.buffer.drawArrays
import awa.hyw.Axolotl.graphics.shader.impl.MotionBlurShader
import awa.hyw.Axolotl.module.Module

object MotionBlur : Module(
    name = "MotionBlur",
    category = Category.RENDER,
) {
    val strength by setting("strength", 3.0f, 0.0f..10.0f)

    @EventTarget(priority = Int.MAX_VALUE)
    private fun onRender3D(e: Render3DEvent) {
        GlHelper.depth = false

        GlHelper.bindTexture(0, MotionBlurShader.currentTex.id)
        GlHelper.bindTexture(1, MotionBlurShader.prevTex.id)

        VertexBufferObjects.MotionBlur.drawArrays(GL_TRIANGLE_STRIP) {
            vertex(1f, 0f)
            vertex(0f, 0f)
            vertex(1f, 1f)
            vertex(0f, 1f)
        }

        GlHelper.bindTexture(0, 0)
        GlHelper.bindTexture(1, 0)

        glCopyTextureSubImage2D(MotionBlurShader.prevTex.id, 0, 0, 0, 0, 0,
            mc.mainRenderTarget.width, mc.mainRenderTarget.height
        )
    }

}


