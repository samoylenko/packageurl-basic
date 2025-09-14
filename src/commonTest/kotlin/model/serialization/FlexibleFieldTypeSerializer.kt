package model.serialization

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import model.InputParams

object FlexibleFieldTypeSerializer : JsonTransformingSerializer<InputParams>(InputParams.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element as? JsonObject ?: JsonObject(mapOf("purl" to element))
}
