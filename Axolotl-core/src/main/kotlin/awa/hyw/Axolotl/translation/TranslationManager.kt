package awa.hyw.Axolotl.translation

object TranslationManager {
    private val translations = HashMap<String, String>()

    fun getTranslation(key: String, default: String = key): String {
        return translations.getOrDefault(key, default)
    }

    fun addTranslation(key: String, value: String) {
        translations[key] = value
    }
}
