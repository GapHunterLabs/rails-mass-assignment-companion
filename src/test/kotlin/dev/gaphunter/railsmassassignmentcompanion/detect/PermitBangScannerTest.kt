package dev.gaphunter.railsmassassignmentcompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PermitBangScannerTest {

    @Test
    fun `flags params-permit-bang`() {
        val code = """def update_params; params.permit!; end"""
        val hits = PermitBangScanner.scan(code)
        assertEquals(1, hits.size)
    }

    @Test
    fun `flags a custom-named params variable`() {
        val code = """user_params.permit!"""
        val hits = PermitBangScanner.scan(code)
        assertEquals(1, hits.size)
    }

    @Test
    fun `does not flag whitelisted permit with explicit attributes`() {
        val code = """params.permit(:name, :email)"""
        assertTrue(PermitBangScanner.scan(code).isEmpty())
    }

    @Test
    fun `does not flag a commented-out line`() {
        val code = """# params.permit!"""
        assertTrue(PermitBangScanner.scan(code).isEmpty())
    }

    @Test
    fun `does not flag an unrelated bang method`() {
        val code = """user.save!"""
        assertTrue(PermitBangScanner.scan(code).isEmpty())
    }
}
