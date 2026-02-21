package awa.hyw.Axolotl.util

import javazoom.jl.player.Player
import java.io.BufferedInputStream
import kotlin.concurrent.thread
import awa.hyw.Axolotl.Axolotl

object SoundUtil {
    fun playSound(name: String) {
        thread {
            try {
                val stream = Axolotl::class.java.getResourceAsStream("${Axolotl.ASSETS_DIRECTORY}/sounds/$name.mp3")
                if (stream != null) {
                    val buffer = BufferedInputStream(stream)
                    val player = Player(buffer)
                    player.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}