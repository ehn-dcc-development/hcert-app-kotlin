package ehn.techiop.hcert.android.chain

import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.qrcode.QRCodeWriter

class TwoDimCodeService(private val size: Int, private val format: BarcodeFormat) {

    private val writer = when (format) {
        BarcodeFormat.QR_CODE -> QRCodeWriter()
        BarcodeFormat.AZTEC -> AztecWriter()
        else -> throw IllegalArgumentException("format")
    }

    /**
     * Generates a 2D code, returns the image itself as Base64 encoded string
     */
    fun encode(data: String): String {
        Log.e("TwoDimCodeService", "Cannot create 2D code")
        return "ERROR"
    }

}