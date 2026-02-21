package awa.hyw.Axolotl.module.impl.render

import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.Render3DEvent
import awa.hyw.Axolotl.event.impl.Render2DEvent
import awa.hyw.Axolotl.graphics.texture.ImageFileUtils
import awa.hyw.Axolotl.graphics.texture.Texture
import awa.hyw.Axolotl.module.Module
import awa.hyw.Axolotl.util.Resource
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel

import awa.hyw.Axolotl.graphics.font.FontRenderers
import awa.hyw.Axolotl.graphics.utils.RenderUtils2D
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.matrix.MatrixStack
import awa.hyw.Axolotl.graphics.utils.RenderUtils3D
import awa.hyw.Axolotl.util.math.vectors.Vec3f
import org.joml.Matrix4f
import org.joml.Vector4f
import org.joml.Vector3f
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import awa.hyw.Axolotl.graphics.GlHelper
import awa.hyw.Axolotl.module.impl.misc.HealthBypass

object ESP : Module("ESP", Category.RENDER) {
    private val players by setting("Players", true)
    private val animals by setting("Animals", true)
    private val mobs by setting("Mobs", true)
    private val invisibles by setting("Invisibles", true)
    private val armorStands by setting("ArmorStands", true)

    private val showImage by setting("Picture", true)
    private val showNametag by setting("Nametag", false)
    private val showBox2D by setting("Box", false)
    private val showHealthBar by setting("Health Bar", false)

    private var iconTexture: Texture? = null

    private var lastProjectionMatrix = Matrix4f()
    private var lastModelViewMatrix = Matrix4f()

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (showImage && iconTexture == null) {
             try {
                iconTexture = ImageFileUtils.loadTextureFromResource(Resource("image/icon.png"))
                if (iconTexture != null) {
                    org.lwjgl.opengl.GL45.glTextureParameteri(iconTexture!!.id, org.lwjgl.opengl.GL45.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL45.GL_NEAREST)
                    org.lwjgl.opengl.GL45.glTextureParameteri(iconTexture!!.id, org.lwjgl.opengl.GL45.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL45.GL_NEAREST)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (mc.level == null) return
        val poseStack = event.poseStack ?: return
        val camera = mc.gameRenderer.mainCamera
        val partialTicks = event.partialTicks

        RenderSystem.depthMask(false)
        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        
        MatrixStack.push()
        val projection = Matrix4f(RenderSystem.getProjectionMatrix())
        val modelView = Matrix4f(poseStack.last().pose())
        MatrixStack.updateMvpMatrix(projection.mul(modelView))

        // Capture matrices for Render2D
        lastProjectionMatrix = Matrix4f(projection)
        lastModelViewMatrix = Matrix4f(modelView)

        val entities = (mc.level as ClientLevel).entitiesForRendering().filter { shouldRender(it) }

        // Pass 1: Batch Render Images
        if (showImage && iconTexture != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader)
            RenderSystem.setShaderTexture(0, iconTexture!!.id)
            val tessellator = Tesselator.getInstance()
            val buffer = tessellator.builder
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
            
            entities.forEach { entity ->
                val interpX = lerp(partialTicks.toDouble(), entity.xo, entity.x)
                val interpY = lerp(partialTicks.toDouble(), entity.yo, entity.y)
                val interpZ = lerp(partialTicks.toDouble(), entity.zo, entity.z)

                poseStack.pushPose()
                poseStack.translate(interpX - camera.position.x, interpY - camera.position.y + entity.bbHeight - 0.3, interpZ - camera.position.z)
                poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot))
                poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot))
                
                val scale = 1.0f 
                poseStack.scale(scale, scale, scale)

                val matrix = poseStack.last().pose()
                
                buffer.vertex(matrix, -0.5f, -0.5f, 0f).uv(0f, 1f).endVertex()
                buffer.vertex(matrix, -0.5f, 0.5f, 0f).uv(0f, 0f).endVertex()
                buffer.vertex(matrix, 0.5f, 0.5f, 0f).uv(1f, 0f).endVertex()
                buffer.vertex(matrix, 0.5f, -0.5f, 0f).uv(1f, 1f).endVertex()
                
                poseStack.popPose()
            }
            
            tessellator.end()
        }

        // Pass 2: Render Nametags
        if (showNametag) {
            entities.forEach { entity ->
                val interpX = lerp(partialTicks.toDouble(), entity.xo, entity.x)
                val interpY = lerp(partialTicks.toDouble(), entity.yo, entity.y)
                val interpZ = lerp(partialTicks.toDouble(), entity.zo, entity.z)

                poseStack.pushPose()
                poseStack.translate(interpX - camera.position.x, interpY - camera.position.y + entity.bbHeight - 0.3, interpZ - camera.position.z)
                poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot))
                poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot))

                val scale = 0.025f
                poseStack.translate(0.0, 0.6, 0.0)
                poseStack.scale(-scale, -scale, scale) 
                
                MatrixStack.push()
                val nametagProjection = Matrix4f(RenderSystem.getProjectionMatrix())
                val nametagModelView = Matrix4f(poseStack.last().pose())
                MatrixStack.updateMvpMatrix(nametagProjection.mul(nametagModelView))

                val name = entity.displayName.string
                val width = FontRenderers.getStringWidth(name)
                val height = FontRenderers.getHeight()
                val x = -width / 2f
                val y = -height / 2f

                RenderUtils2D.drawRectFilled(x - 2, y - 1, width + 4, height + 2, ColorRGB(0f, 0f, 0f, 0.5f))
                FontRenderers.drawString(name, x, y, ColorRGB.WHITE)
                
                MatrixStack.pop()
                poseStack.popPose()
            }
        }

        MatrixStack.pop()

        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
    }

    private fun shouldRender(entity: net.minecraft.world.entity.Entity): Boolean {
        if (entity !is LivingEntity) return false
        if (entity == mc.player) return false
        if (entity is Player && !players) return false
        if (entity is Animal && !animals) return false
        if (entity is Monster && !mobs) return false
        if (entity.isInvisible && !invisibles) return false
        if (entity is ArmorStand && !armorStands) return false
        return true
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (!showBox2D) return
        val mc = Minecraft.getInstance()
        val camera = mc.gameRenderer.mainCamera
        val mvp = lastProjectionMatrix 
        
        val viewport = IntArray(4)
        org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_VIEWPORT, viewport)
        val displayWidth = mc.window.width
        val displayHeight = mc.window.height
        val guiScale = mc.window.guiScale

        (mc.level as ClientLevel).entitiesForRendering().forEach { entity ->
            if (entity is LivingEntity && entity != mc.player) {
                if (entity is Player && !players) return@forEach
                if (entity is Animal && !animals) return@forEach
                if (entity is Monster && !mobs) return@forEach
                if (entity.isInvisible && !invisibles) return@forEach

                val interpX = lerp(event.partialTick.toDouble(), entity.xo, entity.x) - camera.position.x
                val interpY = lerp(event.partialTick.toDouble(), entity.yo, entity.y) - camera.position.y
                val interpZ = lerp(event.partialTick.toDouble(), entity.zo, entity.z) - camera.position.z

                val bb = entity.boundingBox
                val minX = interpX - (entity.x - bb.minX)
                val minY = interpY
                val minZ = interpZ - (entity.z - bb.minZ)
                val maxX = interpX + (bb.maxX - entity.x)
                val maxY = interpY + (bb.maxY - entity.y)
                val maxZ = interpZ + (bb.maxZ - entity.z)

                // Project 8 corners
                val corners = arrayOf(
                    Vector3f(minX.toFloat(), minY.toFloat(), minZ.toFloat()),
                    Vector3f(minX.toFloat(), maxY.toFloat(), minZ.toFloat()),
                    Vector3f(maxX.toFloat(), minY.toFloat(), minZ.toFloat()),
                    Vector3f(maxX.toFloat(), maxY.toFloat(), minZ.toFloat()),
                    Vector3f(minX.toFloat(), minY.toFloat(), maxZ.toFloat()),
                    Vector3f(minX.toFloat(), maxY.toFloat(), maxZ.toFloat()),
                    Vector3f(maxX.toFloat(), minY.toFloat(), maxZ.toFloat()),
                    Vector3f(maxX.toFloat(), maxY.toFloat(), maxZ.toFloat())
                )

                var minScreenX = Float.MAX_VALUE
                var maxScreenX = -Float.MAX_VALUE
                var minScreenY = Float.MAX_VALUE
                var maxScreenY = -Float.MAX_VALUE
                
                // Check if any point is on screen (behind camera check needed)
                // In clip space, w < 0 means behind camera (usually).
                // Or z < -w or z > w.
                
                var anyVisible = false
                
                for (vec in corners) {
                    val target = Vector4f(vec.x, vec.y, vec.z, 1.0f)
                    target.mul(mvp)
                    
                    if (target.w() > 0.0) { // In front of camera
                        anyVisible = true
                        val ndcX = target.x() / target.w()
                        val ndcY = target.y() / target.w()
                        
                        // Convert NDC to Screen
                        // Viewport: [0, 0, width, height]
                        // x = (ndcX + 1) * 0.5 * w + x
                        // y = (ndcY + 1) * 0.5 * h + y
                        
                        // However, we are in GUI coordinates (scaled).
                        // Viewport is in pixels.
                        // We need to convert to GUI scale.
                        
                        val screenX = (ndcX + 1) * 0.5f * displayWidth
                        val screenY = (1 - (ndcY + 1) * 0.5f) * displayHeight // Flip Y for screen coords
                        
                        val guiX = screenX / guiScale.toFloat()
                        val guiY = screenY / guiScale.toFloat()
                        
                        if (guiX < minScreenX) minScreenX = guiX
                        if (guiX > maxScreenX) maxScreenX = guiX
                        if (guiY < minScreenY) minScreenY = guiY
                        if (guiY > maxScreenY) maxScreenY = guiY
                    }
                }
                
                if (anyVisible && minScreenX < maxScreenX && minScreenY < maxScreenY) {
                    // Draw Box
                    val width = maxScreenX - minScreenX
                    val height = maxScreenY - minScreenY
                    
                    // Black outline (width 3, offset 1)
                    org.lwjgl.opengl.GL45.glLineWidth(3.0f)
                    RenderUtils2D.drawRectOutline(minScreenX, minScreenY, width, height, ColorRGB.BLACK)
                    
                    // White inner box (width 1)
                    org.lwjgl.opengl.GL45.glLineWidth(1.0f)
                    RenderUtils2D.drawRectOutline(minScreenX, minScreenY, width, height, ColorRGB.WHITE)

                    if (showHealthBar) {
                        val hp = HealthBypass.getHealth(entity)
                        val maxHp = HealthBypass.getMaxHealth(entity)
                        val hpPercentage = (hp / maxHp).coerceIn(0f, 1f)

                        val barHeight = height
                        val barWidth = 1.0f
                        val barX = minScreenX - barWidth - 2.0f
                        val barY = minScreenY

                        // Draw background (Black)
                        RenderUtils2D.drawRectFilled(barX, barY, barWidth, barHeight, ColorRGB.BLACK)

                        // Draw foreground (Green)
                        val currentHeight = barHeight * hpPercentage
                        val currentY = barY + (barHeight - currentHeight)

                        RenderUtils2D.drawRectFilled(barX, currentY, barWidth, currentHeight, ColorRGB.GREEN)
                    }
                }
            }
        }
    }

    private fun lerp(delta: Double, start: Double, end: Double): Double {
        return start + (end - start) * delta
    }
}
