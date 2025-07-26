package dev.samoylenko.packageurl

import io.konform.validation.Validation
import io.konform.validation.constraints.pattern
import io.konform.validation.ifPresent
import io.konform.validation.onEach

/**
 * Original specification: https://github.com/package-url/purl-spec/blob/main/PURL-SPECIFICATION.rst
 *
 * Implementation specific validations
 */
object PackageUrlBasicValidation {

    fun strictValidation() = Validation<PackageUrlBasic> {
        PackageUrlBasic::type {
            pattern("^[a-z0-9.-]+$") hint "'{value}' <- The package type MUST be composed only of ASCII letters and numbers, period '.', and dash '-'. Must be lowercase."
            run(mustStartWithAsciiLetter)
        }

        PackageUrlBasic::qualifiers {
            ifPresent {
                onEach {
                    Map.Entry<String, String>::key {
                        run(qualifierIllegalCharacters)
                        run(mustStartWithAsciiLetter)
                        pattern("^[a-z0-9.-_]+$") hint "'{value}' <- Qualifier keys MUST be composed only of lowercase ASCII letters and numbers, period '.', dash '-' and underscore '_'."
                    }
                }
            }
        }
    }

    val mustStartWithAsciiLetter = Validation<String> {
        pattern("^[A-Za-z].*") hint "'{value}' <- MUST start with an ASCII letter."
    }

    val qualifierIllegalCharacters = Validation<String> {
        val restrictedChars = listOf('?', '&')

        constrain("'{value}' <- Must not contain any of: ${restrictedChars.joinToString(", ")}") {
            restrictedChars.none { restricted -> it.contains(restricted) }
        }
    }
}
