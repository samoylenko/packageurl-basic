package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestSuiteRecord(
    val description: String,
    val purl: String?,
    @SerialName("canonical_purl") val canonicalPurl: String?,
    val type: String?,
    val name: String?,
    val version: String?,
    val namespace: String?,
    val qualifiers: Map<String, String>?,
    val subpath: String?,
    @SerialName("is_invalid") val isInvalid: Boolean,
    @SerialName("ignore_reason") val ignoreReason: String? = null
)
