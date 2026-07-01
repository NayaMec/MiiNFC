package com.miinfc.domain.amiibo

import org.junit.Assert.*
import org.junit.Test

class RawExperimentalWritePolicyTest {
    @Test fun `raw writer excludes UID config password PACK and lock pages`() {
        assertTrue(RawExperimentalWritePolicy.pagesToWrite.all { it in 4..129 })
        assertTrue(RawExperimentalWritePolicy.forbiddenPages.containsAll((0..3) + (130..134)))
        assertFalse(RawExperimentalWritePolicy.appliesPermanentLocks)
        assertFalse(RawExperimentalWritePolicy.guaranteesSwitchCompatibility)
    }
}
