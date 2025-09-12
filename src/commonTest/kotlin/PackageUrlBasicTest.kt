import data.TEST_SUITE_DATA_MODIFIED_RAW
import model.TestSuiteRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import dev.samoylenko.packageurl.PackageUrlBasic
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import kotlin.test.*

/**
 * See https://github.com/package-url/purl-spec/blob/3f9cd76e102edd4636ca441d71d3cd33c8442b81/PURL-SPECIFICATION.rst#tests
 *
 * Test data from: https://github.com/package-url/purl-spec/blob/3bcba88a651ee9a681620391b869d26d0a6d6dc2/test-suite-data.json
 * (MIT License, Copyright (c) the purl authors)
 *
 */
class PackageUrlBasicTest {

    companion object {
        val logger = KotlinLogging.logger {}
    }

    @Test
    fun percentEncoderImplementationChecks() {

        listOf("..", ".").forEach {
            assertEquals(it, UrlEncoderUtil.encode(it))
            assertEquals(it, UrlEncoderUtil.decode(it))
        }
    }

    @Test
    fun smokeTest() {
        """
            pkg:bitbucket/birkenfeld/pygments-main@244fd47e07d1014f0aed9c
            pkg:deb/debian/curl@7.50.3-1?arch=i386&distro=jessie
            pkg:gem/ruby-advisory-db-check@0.12.4
            pkg:github/package-url/purl-spec@244fd47e07d1004f0aed9c
            pkg:golang/google.golang.org/genproto#googleapis/api/annotations
            pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1?packaging=sources
            pkg:npm/foobar@12.3.1
            pkg:nuget/EnterpriseLibrary.Common@6.0.1304
            pkg:pypi/django@1.11.1
            pkg:rpm/fedora/curl@7.50.3-1.fc25?arch=i386&distro=fedora-25
        """.trimIndent().lines().forEach {
            logger.info { it }
            PackageUrlBasic.parse(it) // assertDoesNotFail
        }
    }

    @Test
    fun mustIgnoreDoubleSlash() {
        assertEquals(
            PackageUrlBasic.parse("pkg:gem/ruby-advisory-db-check@0.12.4"),
            PackageUrlBasic.parse("pkg://gem/ruby-advisory-db-check@0.12.4")
        )
    }

    @Test
    fun mustSkipPeriodPaths() {
        assertEquals(
            PackageUrlBasic.parse("pkg:gem/ruby-advisory-db-check@0.12.4#some/path"),
            PackageUrlBasic.parse("pkg://gem/ruby-advisory-db-check@0.12.4#.././some/./../path/./.././")
        )
    }

    @Test
    fun userPassedNamespaceWithName() {
        assertEquals(
            PackageUrlBasic.build("generic", "name") { namespace("namespace/and") },
            PackageUrlBasic.build("generic", "namespace/and/name")
        )
    }

    @Test
    fun runTestSuite() {

        val testData: Collection<TestSuiteRecord> =
            Json.decodeFromString<Collection<TestSuiteRecord>>(TEST_SUITE_DATA_MODIFIED_RAW)
                .filter { it.ignoreReason == null }

        testData.forEach {

            logger.info { it.description }

            when {
                it.isInvalid -> {

                    if (it.purl != null) assertFailsWith<IllegalArgumentException> { PackageUrlBasic.parse(it.purl) }
                    else if (it.type != null && it.name != null) assertFailsWith<IllegalArgumentException> {
                        PackageUrlBasic.build(type = it.type, name = it.name) {
                            namespace(it.namespace)
                            version(it.version)
                            qualifiers(it.qualifiers)
                            subpath(it.subpath)
                        }
                    } else throw Error("Unexpected scenario with invalid item")
                }

                else -> {
                    assertNotNull(it.canonicalPurl, "Test suite data issue")
                    assertNotNull(it.name, "Test suite data issue")
                    assertNotNull(it.purl, "Test suite data issue")
                    assertNotNull(it.type, "Test suite data issue")

                    val parsedCanonicalPurl = PackageUrlBasic.parse(it.canonicalPurl)
                    assertEquals(
                        it.canonicalPurl,
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
                        it.canonicalPurl.lowercase(),
                        createdFromParsedCanonicalComponents.toString().lowercase(),
                        "parsing the test canonical purl then re-building a purl from these parsed components should return the test canonical purl"
                    )

                    val parsedTestPurl = PackageUrlBasic.parse(it.purl)

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
                        PackageUrlBasic.build(type = it.type, name = it.name) {
                            version(it.version)
                            namespace(it.namespace)
                            qualifiers(it.qualifiers)
                            subpath(it.subpath)
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
