package dev.gaphunter.railsmassassignmentcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.railsmassassignmentcompanion.detect.PermitBangScanner
import dev.gaphunter.railsmassassignmentcompanion.review.ReviewPrompt

/**
 * Flags `params.permit!` -- see [PermitBangScanner] for the full
 * reasoning. Runs via [checkFile] (whole-file text scan), same
 * discipline as the catalog's other Ruby plugins -- see
 * `build.gradle.kts` for why no Ruby-language PSI dependency is
 * taken.
 */
class PermitBangInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
        private val RUBY_FILE_NAME = Regex("""^[^.]+\.rb$""", RegexOption.IGNORE_CASE)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (!RUBY_FILE_NAME.matches(virtualFile.name)) return null

        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = PermitBangScanner.scan(text)
        if (hits.isEmpty()) return null

        val document = file.viewProvider.document ?: return null
        val problems = mutableListOf<ProblemDescriptor>()

        for (hit in hits) {
            if (hit.lineNumber - 1 !in 0 until document.lineCount) continue
            val lineStartOffset = document.getLineStartOffset(hit.lineNumber - 1)
            val absoluteStart = lineStartOffset + hit.columnStart
            val absoluteEnd = lineStartOffset + hit.columnEnd
            val anchor = leafElementAt(file, absoluteStart) ?: continue
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(
                (absoluteStart - anchorStart).coerceAtLeast(0),
                (absoluteEnd - anchorStart).coerceAtMost(anchor.textLength),
            )
            if (relativeRange.startOffset >= relativeRange.endOffset) continue

            problems += manager.createProblemDescriptor(
                anchor,
                relativeRange,
                "permit! allows ALL current and future model attributes to be mass-assigned -- Rails' own docs " +
                    "call for \"extreme care\" here, since it defeats strong parameters entirely. Whitelist the " +
                    "specific attributes with permit(:attr1, :attr2, ...) instead",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )

            ReviewPrompt.recordHit(file.project, "${virtualFile.path}:${hit.lineNumber}")
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) {
            element = element.firstChild
        }
        return element
    }
}
