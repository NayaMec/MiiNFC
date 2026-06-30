package com.miinfc.data.nfc

import android.content.Context
import com.miinfc.domain.model.NfcAvailabilityStatus
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class NfcAvailabilityCheckerTest {
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `returns enabled when supported adapter is enabled`() {
        val checker = checkerFor(hardwareState = true)

        assertEquals(
            NfcAvailabilityStatus.AVAILABLE_ENABLED,
            checker.checkAvailability(context),
        )
    }

    @Test
    fun `returns disabled when supported adapter is disabled`() {
        val checker = checkerFor(hardwareState = false)

        assertEquals(
            NfcAvailabilityStatus.AVAILABLE_DISABLED,
            checker.checkAvailability(context),
        )
    }

    @Test
    fun `returns not supported when adapter does not exist`() {
        val checker = checkerFor(hardwareState = null)

        assertEquals(
            NfcAvailabilityStatus.NOT_SUPPORTED,
            checker.checkAvailability(context),
        )
    }

    private fun checkerFor(hardwareState: Boolean?): NfcAvailabilityChecker =
        NfcAvailabilityChecker(
            hardwareGateway = NfcHardwareGateway { hardwareState },
        )
}
