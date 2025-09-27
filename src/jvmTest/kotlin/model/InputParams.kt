package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class InputParams(
    val purl: String? = null,
    val type: String? = null,
    val namespace: String? = null,
    val name: String? = null,
    val version: String? = null,
    val qualifiers: JsonObject? = null,
    val subpath: String? = null,
)
