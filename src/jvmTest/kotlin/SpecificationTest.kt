import dev.samoylenko.packageurl.PackageUrlBasic
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import model.TestSuite
import model.TestType
import kotlin.test.*

/**
 * Uses test data from the purl specification repository (MIT License, Copyright (c) the purl authors):
 * - https://raw.githubusercontent.com/package-url/purl-spec/18fd3e395dda53c00bc8b11fe481666dc7b3807a/tests/spec/specification-test.json
 */
@Ignore // TODO: enable when the new test suite is available
class SpecificationTest {

    companion object {
        const val URL_TEST_SPEC =
            "https://raw.githubusercontent.com/package-url/purl-spec/1c45ea44cd808971f2388b88dc8350f8f186d004/tests/spec/specification-test.json"

        val logger = KotlinLogging.logger {}
        val ignoredTests = mapOf<Int, String>(
            // TODO: Update when tests are available
            0 to "Not Applicable: impossible scenario in this Kotlin implementation",
            1 to "Not Applicable: impossible scenario in this Kotlin implementation",
        )
    }

    @Test
    fun runTestSuite() = runTest {
        val testSuite: TestSuite = HttpClient().use { Json.decodeFromString(it.get(URL_TEST_SPEC).body()) }

        testSuite.tests.forEachIndexed { index, testRecord ->

            logger.info {
                buildString {
                    append("#${index.toString().padStart(3, '0')} '${testRecord.description}'")
                    if (index in ignoredTests.keys) append(" (Ignored: ${ignoredTests[index]})")
                }
            }

            if (index in ignoredTests) return@forEachIndexed

            when (testRecord.testType) {
                TestType.ROUNDTRIP -> TODO("No generic roundtrip examples available yet")

                TestType.BUILD -> {
                    val input = testRecord.input
                    require(input.type != null && (input.name != null)) { "Test data issue" }
                    val isFailure = runCatching {
                        PackageUrlBasic.build(input.type, input.name) {
                            input.version?.let { version(it) }
                            input.subpath?.let { subpath(it) }
                            // TODO: add qualifiers when the corresponding test data is available
                        }
                    }.isFailure

                    assertEquals(testRecord.expectedFailure, isFailure, testRecord.expectedFailureReason)
                }

                TestType.PARSE -> {
                    require(testRecord.input.purl != null) { "Test data issue" }
                    val isFailure = runCatching { PackageUrlBasic.parse(testRecord.input.purl) }.isFailure
                    assertEquals(testRecord.expectedFailure, isFailure, testRecord.expectedFailureReason)
                }
            }
        }
    }
}
