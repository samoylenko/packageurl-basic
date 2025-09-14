package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TestType {
    @SerialName("parse")
    PARSE,

    @SerialName("build")
    BUILD,

    @SerialName("roundtrip")
    ROUNDTRIP,
}
