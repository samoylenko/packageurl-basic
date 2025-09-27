package model

import kotlinx.serialization.Serializable

@Serializable
data class TestSuite(
    @Suppress("kotlin:S117", "PropertyName")
    val `$schema`: String = "https://packageurl.org/schemas/purl-test.schema-1.0.json",
    val tests: Collection<TestSuiteRecord>,
)
