package com.miinfc.domain.amiibo

object RawExperimentalWritePolicy {
    val pagesToWrite: IntRange = 4..129
    val forbiddenPages: Set<Int> = (0..3).toSet() + (130..134).toSet()
    const val appliesPermanentLocks = false
    const val guaranteesSwitchCompatibility = false
}
