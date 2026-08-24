package dev.gaphunter.railsmassassignmentcompanion.detect

import dev.gaphunter.railsmassassignmentcompanion.model.PermitBangHit

/**
 * Plain-text scan for `params.permit!` (or `.permit!` on any
 * `_params` receiver, e.g. `user_params.permit!`) -- Rails' own
 * official documentation states: "Extreme care should be taken when
 * using permit!, as it will allow all current and future model
 * attributes to be mass-assigned". This is the exact anti-pattern
 * strong parameters exist to prevent: it disables the whitelist
 * entirely, permitting whatever attributes an attacker sends in the
 * request body, including ones the developer never intended to be
 * settable (the well-known real-world case: a `role`/`admin` boolean
 * flipped via mass assignment). Confirmed real gap: none of
 * RuboCop's 7 Security cops cover Rails strong parameters/mass
 * assignment.
 *
 * **v0.1 scope, stated honestly:** plain-text regex matching, not
 * real Ruby/Rails PSI -- doesn't resolve whether the receiver is
 * actually an `ActionController::Parameters` instance, so an
 * unrelated custom `.permit!` method on some other object is a
 * possible (rare) false positive.
 */
object PermitBangScanner {

    private val PERMIT_BANG = Regex("""(\bparams|\w*_params)\s*\.\s*permit!""")

    fun scan(text: String): List<PermitBangHit> {
        val hits = mutableListOf<PermitBangHit>()
        text.lines().forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trimStart()
            if (trimmed.startsWith("#")) return@forEachIndexed

            val match = PERMIT_BANG.find(rawLine) ?: return@forEachIndexed
            hits += PermitBangHit(index + 1, match.range.first, match.range.last + 1)
        }
        return hits
    }
}
