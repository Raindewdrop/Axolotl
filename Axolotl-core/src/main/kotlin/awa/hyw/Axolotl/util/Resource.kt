package awa.hyw.Axolotl.util

import awa.hyw.Axolotl.Axolotl

class Resource(path: String) {
    val byteArr: ByteArray = javaClass.getResourceAsStream("${Axolotl.ASSETS_DIRECTORY}/${path}")?.use { it.readBytes() }
        ?: throw IllegalArgumentException("Resource not found: ${Axolotl.ASSETS_DIRECTORY}/${path}")
    val data: String get() = String(byteArr, Charsets.UTF_8)
}


