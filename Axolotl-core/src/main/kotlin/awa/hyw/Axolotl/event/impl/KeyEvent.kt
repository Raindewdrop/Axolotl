package awa.hyw.Axolotl.event.impl

import awa.hyw.Axolotl.event.CancellableEvent

class KeyEvent(val keyCode: Int, val scanCode: Int, val action: Int, val modifiers: Int): CancellableEvent()
