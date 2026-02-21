package awa.hyw.Axolotl.translation

class TranslationString(
    prefix: String,
    key: String,
    val default: String = key
) {
    val key = TranslationKey(prefix, key)

    val translation: String
        get() = TranslationManager.getTranslation(key.fullKey, default)

    override fun toString(): String {
        return translation
    }
}

class TranslationKey(var prefix: String, var key: String) {
    val fullKey: String
        get() {
            if (prefix.isEmpty()) return key
            return "$prefix.$key"
        }
}
