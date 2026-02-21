package awa.hyw.Axolotl.graphics.font

import awa.hyw.Axolotl.graphics.font.FontRenderers.fontRendererType
import awa.hyw.Axolotl.graphics.font.general.FontChunks
import awa.hyw.Axolotl.graphics.font.general.GlyphChunk
import awa.hyw.Axolotl.graphics.font.sparse.SparseFontGlyph
import java.awt.Font

class FontAdapter(
    font: Font
) {
    val general = FontChunks(font)
    val sparse = SparseFontGlyph(font, FontRenderers.DRAW_FONT_SIZE)

    fun getHeight(): Float = when (fontRendererType) {
        FontRendererType.GENERAL -> {
            general.getHeight()
        }

        FontRendererType.SPARSE -> {
            sparse.height
        }
    }

    fun getCharData(char: Char): CharData? {
        return when (fontRendererType) {
            FontRendererType.GENERAL -> {
                if (canDisplay(char)) general.getChunk(char.code / GlyphChunk.CHUNK_SIZE).charData[char] else null
            }

            FontRendererType.SPARSE -> {
                if (canDisplay(char)) sparse.getCharData(char) else null
            }
        }
    }

    fun canDisplay(char: Char): Boolean = when (fontRendererType) {
        FontRendererType.GENERAL -> {
            general.canDisplay(char)
        }
        FontRendererType.SPARSE -> {
            sparse.canDisplay(char)
        }
    }

}

