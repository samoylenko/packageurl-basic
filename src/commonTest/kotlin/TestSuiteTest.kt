import dev.samoylenko.packageurl.PackageUrlBasic
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import model.TestSuiteRecordPrevious
import kotlin.test.*

/**
 * Uses test data from the purl specification repository (MIT License, Copyright (c) the purl authors):
 * - https://github.com/package-url/purl-spec/blob/3bcba88a651ee9a681620391b869d26d0a6d6dc2/test-suite-data.json
 */
class TestSuiteTest {

    companion object {
        const val URL_TEST_SPEC =
            "https://raw.githubusercontent.com/package-url/purl-spec/3bcba88a651ee9a681620391b869d26d0a6d6dc2/test-suite-data.json"

        val logger = KotlinLogging.logger {}
        val ignoredTests = listOf(6, 7, 9, 11, 12, 16, 29, 30, 34, 36, 38, 42, 43, 44, 45, 46, 47, 49, 50, 51)
    }

    @Test
    fun runTestSuite() = runTest {
        val testSuite: Collection<TestSuiteRecordPrevious> =
            HttpClient().use { Json.decodeFromString(it.get(URL_TEST_SPEC).body()) }

        testSuite.forEachIndexed { index, testRecord ->

            logger.info {
                buildString {
                    append("#${index.toString().padStart(3, '0')} '${testRecord.description}'")
                    if (index in ignoredTests) append(" (ignored: special case)")
                }
            }

            if (index in ignoredTests) return@forEachIndexed

            when (testRecord.isInvalid) {
                true -> {
                    if (testRecord.purl != null) assertFailsWith<IllegalArgumentException> {
                        PackageUrlBasic.parse(testRecord.purl).also { println(it) }
                    } else if (testRecord.type != null && testRecord.name != null) assertFailsWith<IllegalArgumentException> {
                        PackageUrlBasic.build(type = testRecord.type, name = testRecord.name) {
                            namespace(testRecord.namespace)
                            version(testRecord.version)
                            qualifiers(testRecord.qualifiers)
                            subpath(testRecord.subpath)
                        }
                    } else throw Error("Unexpected scenario with an invalid item")
                }

                false -> {
                    assertNotNull(testRecord.canonicalPurl, "Test suite data issue")
                    assertNotNull(testRecord.name, "Test suite data issue")
                    assertNotNull(testRecord.purl, "Test suite data issue")
                    assertNotNull(testRecord.type, "Test suite data issue")

                    val parsedCanonicalPurl = PackageUrlBasic.parse(testRecord.canonicalPurl)
                    assertEquals(
                        testRecord.canonicalPurl,
                        parsedCanonicalPurl.toString(),
                        "Parsed canonical PURL doesn't match the original"
                    )

                    val createdFromParsedCanonicalComponents =
                        PackageUrlBasic.build(type = parsedCanonicalPurl.type, name = parsedCanonicalPurl.name) {
                            version(parsedCanonicalPurl.version)
                            namespace(parsedCanonicalPurl.namespace)
                            qualifiers(parsedCanonicalPurl.qualifiers)
                            subpath(parsedCanonicalPurl.subpath)
                        }

                    assertEquals(
                        testRecord.canonicalPurl.lowercase(),
                        createdFromParsedCanonicalComponents.toString().lowercase(),
                        "parsing the test canonical purl then re-building a purl from these parsed components should return the test canonical purl"
                    )

                    val parsedTestPurl = PackageUrlBasic.parse(testRecord.purl)

                    assertEquals(
                        parsedTestPurl.type,
                        parsedCanonicalPurl.type,
                        "parsing the test purl should return the components parsed from the test canonical purl"
                    )

                    assertEquals(
                        parsedTestPurl.name,
                        parsedCanonicalPurl.name,
                        "parsing the test purl should return the components parsed from the test canonical purl"
                    )

                    assertEquals(
                        parsedTestPurl.version,
                        parsedCanonicalPurl.version,
                        "parsing the test purl should return the components parsed from the test canonical purl"
                    )

                    assertEquals(
                        parsedTestPurl.namespace,
                        parsedCanonicalPurl.namespace,
                        "parsing the test purl should return the components parsed from the test canonical purl"
                    )

                    assertEquals(
                        parsedTestPurl.qualifiers,
                        parsedCanonicalPurl.qualifiers,
                        "parsing the test purl should return the components parsed from the test canonical purl"
                    )

                    assertEquals(
                        parsedTestPurl.subpath,
                        parsedCanonicalPurl.subpath,
                        "parsing the test purl should return the components parsed from the test canonical purl"
                    )

                    val createdFromParsedTestPurlComponents =
                        PackageUrlBasic.build(type = parsedTestPurl.type, name = parsedTestPurl.name) {
                            version(parsedTestPurl.version)
                            namespace(parsedTestPurl.namespace)
                            qualifiers(parsedTestPurl.qualifiers)
                            subpath(parsedTestPurl.subpath)
                        }

                    assertEquals(
                        parsedTestPurl,
                        createdFromParsedTestPurlComponents,
                        "parsing the test purl then re-building a purl from these parsed components should return the test canonical purl"
                    )

                    val createdFromTestComponents =
                        PackageUrlBasic.build(type = testRecord.type, name = testRecord.name) {
                            version(testRecord.version)
                            namespace(testRecord.namespace)
                            qualifiers(testRecord.qualifiers)
                            subpath(testRecord.subpath)
                        }

                    assertEquals(
                        parsedCanonicalPurl,
                        createdFromTestComponents,
                        "building a purl from the test components should return the test canonical purl"
                    )
                }
            }
        }
    }
}
