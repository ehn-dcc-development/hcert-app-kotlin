package ehn.techiop.hcert.android.chain

class CborVerificationChain(
    private val cborService: CborService,
    private val valSuiteService: ValSuiteService,
    private val compressorService: CompressorService,
    private val base45Service: Base45Service
) {

    fun verify(input: String): VaccinationData {
        val plainInput = valSuiteService.decode(input)
        val compressedCose = base45Service.decode(plainInput)
        val cose = compressorService.decode(compressedCose)
        val cbor = cborService.verify(cose)
        return cborService.decode(cbor)
    }

}