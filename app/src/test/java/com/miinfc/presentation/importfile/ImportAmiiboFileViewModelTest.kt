package com.miinfc.presentation.importfile

import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.AmiiboLibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ImportAmiiboFileViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun teardown() = Dispatchers.resetMain()

    @Test fun `BIN is normalized saved and selected`() = runTest {
        val repository = FakeLibrary()
        val vm = ImportAmiiboFileViewModel(repository)
        vm.import(AmiiboImportFileType.BIN, "Mipha.bin", ByteArray(540))
        advanceUntilIdle()
        assertTrue(vm.state.value is AmiiboFileImportState.Success)
        assertEquals("Mipha.bin", repository.selectedState.value?.displayName)
    }

    @Test fun `NFC uses Flipper parser and requires NTAG215`() = runTest {
        val repository = FakeLibrary()
        val vm = ImportAmiiboFileViewModel(repository)
        val document = buildString {
            appendLine("Filetype: Flipper NFC device")
            appendLine("Device type: NTAG215")
            appendLine("Pages total: 135")
            repeat(135) { appendLine("Page $it: 00 00 00 00") }
        }.toByteArray()
        vm.import(AmiiboImportFileType.NFC, "Mipha.nfc", document)
        advanceUntilIdle()
        assertTrue(vm.state.value is AmiiboFileImportState.Success)
        assertEquals(AmiiboSourceFormat.FLIPPER_NFC, repository.selectedState.value?.format)
    }

    @Test fun `NFC rejects wrong device type`() = runTest {
        val vm = ImportAmiiboFileViewModel(FakeLibrary())
        vm.import(AmiiboImportFileType.NFC, "tag.nfc", "Filetype: Flipper NFC device\nDevice type: NTAG216\nPages total: 135".toByteArray())
        advanceUntilIdle()
        assertTrue(vm.state.value is AmiiboFileImportState.Error)
    }

    private class FakeLibrary : AmiiboLibraryRepository {
        private val list = MutableStateFlow<List<AmiiboSourceFile>>(emptyList())
        val selectedState = MutableStateFlow<AmiiboSourceFile?>(null)
        override val amiibos: Flow<List<AmiiboSourceFile>> = list
        override val selected: Flow<AmiiboSourceFile?> = selectedState
        override suspend fun save(source: AmiiboSourceFile) { list.value = list.value + source }
        override suspend fun selectAmiibo(id: String) { selectedState.value = list.value.firstOrNull { it.id == id } }
        override suspend fun clearSelectedAmiibo() { selectedState.value = null }
    }
}
