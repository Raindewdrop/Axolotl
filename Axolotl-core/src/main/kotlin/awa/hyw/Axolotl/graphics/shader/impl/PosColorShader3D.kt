package awa.hyw.Axolotl.graphics.shader.impl

import org.lwjgl.opengl.GL20.glGetUniformLocation
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.graphics.matrix.MatrixStack
import awa.hyw.Axolotl.graphics.shader.Shader

object PosColorShader3D: Shader(
    vertShaderPath = "${Axolotl.ASSETS_DIRECTORY}/shader/general/PosColor3D.vert",
    fragShaderPath = "${Axolotl.ASSETS_DIRECTORY}/shader/general/PosColor.frag"
) {

    private val matrixLocation = glGetUniformLocation(id, "MVPMatrix")

    override fun default() {
        matrix4f(matrixLocation, MatrixStack.peek().mvpMatrix)
    }

}

