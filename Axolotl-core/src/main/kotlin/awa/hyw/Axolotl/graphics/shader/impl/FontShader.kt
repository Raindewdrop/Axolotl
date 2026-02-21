package awa.hyw.Axolotl.graphics.shader.impl

import org.lwjgl.opengl.ARBBindlessTexture.glProgramUniformHandleui64ARB
import org.lwjgl.opengl.GL45
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.graphics.RenderSystem
import awa.hyw.Axolotl.graphics.matrix.MatrixStack
import awa.hyw.Axolotl.graphics.shader.Shader

// Only for sparse font mode
object FontShader: Shader(
    "${Axolotl.ASSETS_DIRECTORY}/shader/general/FontRenderer.vert",
    "${Axolotl.ASSETS_DIRECTORY}/shader/general/FontRenderer.frag",
    shouldBeCompiled = { RenderSystem.gpuType != RenderSystem.GPUType.INTEL && RenderSystem.gpuType != RenderSystem.GPUType.OTHER }
) {

    private val matrixLocation = GL45.glGetUniformLocation(id, "u_MVPMatrix")
    private val samplerLocation = GL45.glGetUniformLocation(id, "u_Texture")

    override fun default() {
        matrix4f(matrixLocation, MatrixStack.peek().mvpMatrix)
        textureUnit?.let { glProgramUniformHandleui64ARB(id, samplerLocation, it) }
    }

    var textureUnit: Long? = 0L
}

