package com.miinfc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.miinfc.domain.amiibo.*
import com.miinfc.presentation.components.PlaceholderScreen
import com.miinfc.presentation.collection.*
import com.miinfc.presentation.diagnostic.AmiiboDiagnosticScreen
import com.miinfc.presentation.guide.*
import com.miinfc.presentation.home.*
import com.miinfc.presentation.importkey.ImportKeyScreen
import com.miinfc.presentation.importkey.ImportKeyViewModel
import com.miinfc.presentation.importfile.*
import com.miinfc.presentation.navigation.AppRoute
import com.miinfc.presentation.write.*
import com.miinfc.presentation.scan.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MiiNfcNavHost() } }
    }
}

@Composable
private fun MiiNfcNavHost(nav: NavHostController = rememberNavController()) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val homeState by homeViewModel.state.collectAsState()
    NavHost(navController = nav, startDestination = AppRoute.Home.route) {
        composable(AppRoute.Home.route) { HomeScreen(
            uiState = homeState,
            onImportKey = { nav.navigate(AppRoute.ImportKey.route) },
            onKeyGuide = { nav.navigate(AppRoute.KeyGuide.route) },
            onImportBin = { nav.navigate(AppRoute.ImportBin.route) },
            onScanAmiibo = { nav.navigate(AppRoute.Scan.route) },
            onBinGuide = { nav.navigate(AppRoute.BinGuide.route) },
            onAmiiboInfo = { nav.navigate(AppRoute.AmiiboInfo.route) },
            onWrite = { nav.navigate(AppRoute.Write.route) },
            onWriteGuide = { nav.navigate(AppRoute.WriteGuide.route) },
            onCollection = { nav.navigate(AppRoute.Collection.route) },
            onSettings = { nav.navigate(AppRoute.Settings.route) },
        ) }
        composable(AppRoute.ImportKey.route) {
            val vm: ImportKeyViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            ImportKeyScreen(state, vm::import, nav::popBackStack)
        }
        composable(AppRoute.ImportBin.route) {
            val vm: ImportAmiiboFileViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            ImportAmiiboFileScreen(AmiiboImportFileType.BIN, state,
                onFileRead = { name, bytes -> vm.import(AmiiboImportFileType.BIN, name, bytes) },
                onUseAmiibo = nav::popBackStack, onBack = nav::popBackStack)
        }
        composable(AppRoute.Scan.route) {
            val vm: ScanAmiiboViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            ScanAmiiboScreen(state, vm::scan, nav::popBackStack, nav::popBackStack)
        }
        composable(AppRoute.AmiiboInfo.route) { PlaceholderScreen("Informação do Amiibo", homeState.selectedAmiiboName ?: "Nenhum Amiibo selecionado.", nav::popBackStack) }
        composable(AppRoute.Collection.route) {
            val vm: CollectionViewModel = hiltViewModel()
            val items by vm.state.collectAsState()
            CollectionScreen(items,
                onSelect = { id -> vm.select(id, nav::popBackStack) },
                onImportBin = { nav.navigate(AppRoute.ImportBin.route) },
                onBack = nav::popBackStack)
        }
        composable(AppRoute.Settings.route) { PlaceholderScreen("Configurações", "Preferências do MiiNFC.", nav::popBackStack) }
        composable(AppRoute.KeyGuide.route) { KeyGuideScreen({}, nav::popBackStack) }
        composable(AppRoute.BinGuide.route) { BinGuideScreen({}, nav::popBackStack) }
        composable(AppRoute.WriteGuide.route) { NfcWriteGuideScreen(
            onDiagnostic = { nav.navigate(AppRoute.Diagnostic.route) }, onBack = nav::popBackStack,
        ) }
        composable(AppRoute.Write.route) {
            if (homeState.selectedWriteMode == WriteMode.RAW_EXPERIMENTAL) {
                val vm: RawWriteViewModel = hiltViewModel()
                val rawState by vm.state.collectAsState()
                RawExperimentalWriteScreen(rawState, vm::onTag,
                    onDiagnostic = { nav.navigate(AppRoute.Diagnostic.route) }, onBack = nav::popBackStack)
                return@composable
            }
            val writeState = WriteToNfcUiState(
                hasImportedKeyFile = homeState.hasImportedKeyFile,
                hasValidCryptoKey = homeState.hasValidKey,
                hasSelectedAmiibo = homeState.hasSelectedAmiibo,
                selectedAmiiboName = homeState.selectedAmiiboName,
                amiiboFileValid = homeState.hasSelectedAmiibo,
                cryptoEngineAvailable = homeState.cryptoEngineAvailable,
                nfcEnabled = true,
            )
            WriteToNfcScreen(writeState, {}, { nav.navigate(AppRoute.Diagnostic.route) }, {}, nav::popBackStack)
        }
        composable(AppRoute.Diagnostic.route) {
            val vm: com.miinfc.presentation.diagnostic.DiagnosticViewModel = hiltViewModel()
            val rawReport by vm.rawReport.collectAsState()
            AmiiboDiagnosticScreen(emptyDiagnostic(homeState).copy(
                rawPhysicalWriteStatus = when (rawReport.status) {
                com.miinfc.domain.repository.RawPhysicalWriteStatus.COMPLETED -> "Concluída"
                com.miinfc.domain.repository.RawPhysicalWriteStatus.FAILED -> "Falhou"
                else -> "Não realizada"
                },
                targetUidRead = rawReport.targetUidRead,
                targetUidLengthValid = rawReport.targetUidLengthValid,
                isNtag215 = rawReport.ntag215Detected,
                tagWritable = rawReport.tagWritable,
                physicalWriteOk = rawReport.status == com.miinfc.domain.repository.RawPhysicalWriteStatus.COMPLETED,
                verificationOk = rawReport.pagesVerified,
                keyUsedInPreparation = rawReport.keyUsedInPreparation,
                preparedForTargetUid = rawReport.preparedForTargetUid,
                pwdPackConfigApplied = rawReport.pwdPackConfigApplied,
                locksApplied = rawReport.locksApplied,
                switchCompatible = rawReport.switchCompatible,
            ), nav::popBackStack)
        }
    }
}

private fun emptyDiagnostic(state: HomeUiState) = AmiiboDiagnosticReport(
    fileName = state.selectedAmiiboName, fileType = null, fileNormalized = state.hasSelectedAmiibo,
    keyImported = state.hasImportedKeyFile, keyValidated = state.hasValidKey,
    cryptoEngineAvailable = state.cryptoEngineAvailable, engineFunctional = state.cryptoEngineAvailable,
    targetUidRead = false, targetUidLengthValid = false, isNtag215 = false,
    preparedForTargetUid = false, physicalWriteOk = false, finalizationApplied = false,
    verificationOk = false, switchCompatible = false,
    failureReason = if (state.cryptoEngineAvailable) null else "Motor Amiibo não implementado.",
)
