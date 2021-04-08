package ehn.techiop.hcert.android.chain

import android.util.Base64

fun ByteArray.asBase64() = Base64.encodeToString(this, Base64.DEFAULT)

fun ByteArray.asBase64Url() = Base64.encodeToString(this, Base64.URL_SAFE)

fun String.fromBase64() = Base64.decode(this, Base64.DEFAULT)

