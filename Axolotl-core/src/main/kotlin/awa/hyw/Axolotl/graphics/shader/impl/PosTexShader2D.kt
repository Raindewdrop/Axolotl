package awa.hyw.Axolotl.graphics.shader.impl

import org.lwjgl.opengl.GL45
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.graphics.matrix.MatrixStack
import awa.hyw.Axolotl.graphics.shader.Shader

object PosTexShader2D: Shader(
    "${Axolotl.ASSETS_DIRECTORY}/shader/general/PosTex2D.vert",
    "${Axolotl.ASSETS_DIRECTORY}/shader/general/PosTex2D.frag",
) {

    private val matrixLocation = GL45.glGetUniformLocation(id, "MVPMatrix")
    private val samplerLocation = GL45.glGetUniformLocation(id, "u_Texture")

    override fun default() {
        matrix4f(matrixLocation, MatrixStack.peek().mvpMatrix)
        int1(samplerLocation, 0)
    }
}

