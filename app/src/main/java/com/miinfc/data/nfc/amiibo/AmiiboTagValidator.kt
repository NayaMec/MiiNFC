package com.miinfc.data.nfc.amiibo

import android.nfc.Tag
import com.miinfc.data.nfc.Ntag215Validator
import com.miinfc.domain.model.*

class AmiiboTagValidator(private val validator: Ntag215Validator) {
    fun validate(tag: Tag, base: NfcTagInfo): Ntag215ValidationResult = validator.inspect(tag, base)
}
