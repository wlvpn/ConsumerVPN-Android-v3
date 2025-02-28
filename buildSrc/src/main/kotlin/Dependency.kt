data class Dependency(
    val group: String = "",
    val module: String = "",
    val version: String = ""
) {
    val mergedId = "$group:$module:$version"
}