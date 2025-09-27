package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.serialization.FlexibleFieldTypeSerializer

@Serializable
data class TestSuiteRecord(
    val description: String,
    @SerialName("test_group") val testGroup: String,
    @SerialName("test_type") val testType: TestType,
    @SerialName("expected_output") val expectedOutput: String?,
    @SerialName("expected_failure") val expectedFailure: Boolean,
    @SerialName("expected_failure_reason") val expectedFailureReason: String,

    @Serializable(with = FlexibleFieldTypeSerializer::class)
    val input: InputParams,
)
