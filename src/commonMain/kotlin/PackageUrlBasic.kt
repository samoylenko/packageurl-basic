package dev.samoylenko.packageurl

import io.github.oshai.kotlinlogging.KotlinLogging
import net.thauvin.erik.urlencoder.UrlEncoderUtil

class PackageUrlBasic private constructor(
    namespace: String? = null,
    type: String,
    name: String,
    version: String? = null,
    qualifiers: Map<String, String>? = null,
    subpath: String? = null,
) {
    companion object {

        const val SCHEME: String = "pkg"

        private val logger = KotlinLogging.logger {}

        inline fun build(type: String, name: String, block: Builder.() -> Unit) =
            Builder(type = type, name = name).apply(block).build()

        fun parse(purl: String, strict: Boolean = true): PackageUrlBasic {

            logger.trace { "purl = '${purl}'" }

            val subpath = purl
                .substringAfterLast('#', "")
                .trim('/')
                .split('/')
                .filterNot { it.isBlank() || it in listOf(".", "..") }
                .joinToString("/") { UrlEncoderUtil.decode(it) }
                .ifBlank { null }

            logger.trace { "subpath = '${subpath}'" }

            var remainder = purl.substringBeforeLast('#')
            require(remainder.isNotBlank()) { "Malformed PackageURL" }

            val qualifiers = remainder
                .substringAfterLast('?', "")
                .split('&')
                .associate {
                    it.substringBefore('=', "").lowercase() to
                            UrlEncoderUtil.decode(it.substringAfter('=', ""))
                }
                .filterNot { it.value.isBlank() || it.key.isBlank() }
                .ifEmpty { null }

            logger.trace { "qualifiers = '${qualifiers}'" }

            remainder = remainder.substringBeforeLast('?')
            require(remainder.isNotBlank()) { "Malformed PackageURL" }

            val scheme = remainder.substringBefore(':', SCHEME)
            logger.trace { "scheme = '${scheme}'" }
            require(scheme == SCHEME) { "Malformed PackageURL: scheme must always be '${SCHEME}'" }

            remainder = remainder.substringAfter(':', "").trim('/')
            require(remainder.isNotBlank()) { "Malformed PackageURL" }

            val type = remainder.substringBefore('/')
            logger.trace { "type = '${type}'" }

            remainder = remainder.substringAfter('/', "")
            require(remainder.isNotBlank()) { "Malformed PackageURL" }

            val version = remainder.substringAfterLast('@', "")
                .let { if (it.isNotBlank()) UrlEncoderUtil.decode(it) else null }
            logger.trace { "version = '${version}'" }

            remainder = remainder.substringBeforeLast('@')
            require(remainder.isNotBlank()) { "Malformed PackageURL" }

            val name = remainder.substringAfterLast('/')
                .let { UrlEncoderUtil.decode(it) }
            logger.trace { "name = '${name}'" }

            remainder = remainder.substringBeforeLast('/', "")

            val namespace =
                if (remainder.isNotBlank()) remainder
                    .split('/')
                    .filterNot { it.isBlank() }
                    .joinToString("/") { UrlEncoderUtil.decode(it) }
                else
                    null
            logger.trace { "namespace = '${namespace}'" }

            val packageUrlBasic = PackageUrlBasic(
                type = type,
                name = name,
                version = version,
                namespace = namespace,
                qualifiers = qualifiers,
                subpath = subpath,
            )

            if (strict) require(packageUrlBasic.isValid) { "'$purl' failed validation" }

            return packageUrlBasic
        }
    }

    @Suppress("unused")
    val scheme = SCHEME

    val type: String = type.lowercase()

    val name: String

    val version: String?

    val namespace: String?

    val qualifiers: Map<String, String>?

    val subpath: String?

    val canonical: String

    val coordinates: String

    val isValid: Boolean

    init {
        this.namespace = namespace
            ?.trim('/')
            ?.split('/')
            ?.joinToString("/") { it.lowercase() }
            ?.ifBlank { null }

        this.name = name.trim('/').lowercase() // Deviate from the specification: name is always lowercase

        this.version = version

        this.qualifiers = qualifiers
            ?.map { it.key.lowercase().trim() to it.value.trim() }
            ?.filterNot { it.first.isBlank() || it.second.isBlank() }
            ?.sortedBy { it.first }
            ?.associate { it.first to it.second }
            ?.ifEmpty { null }

        this.subpath = subpath
            ?.trim()
            ?.lowercase()
            ?.split('/')
            ?.filterNot { it.isBlank() || it in listOf(".", "..") }
            ?.joinToString("/")
            ?.ifEmpty { null }

        this.canonical = canonicalize()

        this.coordinates = canonicalize(coordinatesOnly = true)

        PackageUrlBasicValidation.strictValidation().validate(this).let { validationResult ->
            this.isValid = validationResult.isValid

            if (!validationResult.isValid) logger.warn {
                "The PURL '$canonical' has failed the following validations:\n${validationResult.errors.joinToString("\n") { it.message }}"
            }
        }
    }

    override fun toString(): String = canonical

    private fun canonicalize(coordinatesOnly: Boolean = false): String =
        buildString {
            append(SCHEME)
            append(':')

            append(type)
            append('/')

            namespace?.let {
                append(
                    it.split('/').joinToString("/") { segment -> UrlEncoderUtil.encode(segment) }
                )
                append('/')
            }

            append(UrlEncoderUtil.encode(name))

            version?.let {
                append('@')
                append(UrlEncoderUtil.encode(it))
            }

            if (!coordinatesOnly) {
                qualifiers?.let {
                    append('?')
                    append(it.entries.joinToString("&") { qualifier ->
                        val value = if (qualifier.key.contentEquals("checksums", ignoreCase = true))
                            qualifier.value.split(',').joinToString(",") { checksum -> UrlEncoderUtil.encode(checksum) }
                        else
                            UrlEncoderUtil.encode(qualifier.value)

                        "${qualifier.key}=${value}"
                    })
                }

                subpath?.let {
                    append('#')
                    append(it.split('/').joinToString("/") { segment -> UrlEncoderUtil.encode(segment) })
                }
            }
        }

    class Builder(
        private val type: String,
        private val name: String
    ) {
        private var version: String? = null
        private var namespace: String? = null
        private var qualifiers: Map<String, String>? = null
        private var subpath: String? = null

        fun version(version: String?) = apply { this.version = version }
        fun namespace(namespace: String?) = apply { this.namespace = namespace }
        fun qualifiers(qualifiers: Map<String, String>?) = apply { this.qualifiers = qualifiers }
        fun subpath(subpath: String?) = apply { this.subpath = subpath }

        fun build() = PackageUrlBasic(
            type = type,
            name = name,
            version = version,
            namespace = namespace,
            qualifiers = qualifiers,
            subpath = subpath,
        )
    }
}
