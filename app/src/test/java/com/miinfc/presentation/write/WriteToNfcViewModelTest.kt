package com.miinfc.presentation.write

import com.miinfc.domain.amiibo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WriteToNfcViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `transitions missing engine to ready and prepared`() = runTest {
        var engine: AmiiboCryptoEngine = SafeStubAmiiboCryptoEngine()
        val viewModel = WriteToNfcViewModel(AmiiboCryptoEngineProvider { engine })
        val key = ImportedKeyFile("id", "key.bin", "/private/key", 160)
        val source = AmiiboSourceFile("id", "amiibo.bin", AmiiboSourceFormat.RAW_BIN, ByteArray(540))

        viewModel.setInputs(key, source, ByteArray(7))
        assertEquals(AmiiboCompatibilityStatus.MISSING_CRYPTO_ENGINE, viewModel.state.value.status)

        engine = FakeAmiiboCryptoEngine()
        viewModel.refreshEngine()
        assertEquals(AmiiboCompatibilityStatus.READY_TO_PREPARE, viewModel.state.value.status)

        viewModel.prepareForTargetUid()
        advanceUntilIdle()
        assertEquals(AmiiboCompatibilityStatus.PREPARED_FOR_TARGET_UID, viewModel.state.value.status)
    }
}
