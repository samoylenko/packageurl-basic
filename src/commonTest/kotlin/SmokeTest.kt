import dev.samoylenko.packageurl.PackageUrlBasic
import io.github.oshai.kotlinlogging.KotlinLogging
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Uses discarded test data from the purl specification repository (MIT License, Copyright (c) the purl authors):
 * - https://github.com/package-url/purl-spec/blob/3f9cd76e102edd4636ca441d71d3cd33c8442b81/PURL-SPECIFICATION.rst#tests
 * - https://github.com/package-url/purl-spec/blob/3bcba88a651ee9a681620391b869d26d0a6d6dc2/test-suite-data.json
 */
class SmokeTest {

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
}
