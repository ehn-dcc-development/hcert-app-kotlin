package ehn.techiop.hcert.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import ehn.techiop.hcert.kotlin.chain.*
import ehn.techiop.hcert.kotlin.chain.impl.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = IntentIntegrator(this).also {
                it.setOrientationLocked(false)
                it.setDesiredBarcodeFormats(
                    BarcodeFormat.AZTEC.name,
                    BarcodeFormat.QR_CODE.name
                )
            }.createScanIntent()
            startActivityForResult(intent, IntentIntegrator.REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let { intentResult ->
            intentResult.contents?.let {
                findViewById<TextView>(R.id.textview_first).text = "Validating ..."
                findViewById<LinearLayout>(R.id.container_data).removeAllViews()
                thread {
                    verifyOnBackgroundThread(it)
                }
            }
        }
    }

    private fun verifyOnBackgroundThread(qrCodeContent: String) {
        val verificationResult = VerificationResult()
        try {
            val vaccinationData = getChain().verify(qrCodeContent, verificationResult)
            val verificationDecision = DecisionService().decide(verificationResult)
            runOnUiThread {
                findViewById<TextView>(R.id.textview_first).text = ""
                fillLayout(
                    findViewById<LinearLayout>(R.id.container_data),
                    Data.fromSchema(vaccinationData),
                    verificationResult,
                    verificationDecision
                )
            }
        } catch (e: Throwable) {
            runOnUiThread {
                findViewById<TextView>(R.id.textview_first).text = ""
                findViewById<LinearLayout>(R.id.container_data).addView(TextView(this).also {
                    it.text = "Error on validation: ${e.message}"
                    it.setTextColor(resources.getColor(R.color.error, theme))
                })
            }
        }
    }

    private fun fillLayout(
        container: LinearLayout,
        data: GreenCertificate,
        verificationResult: VerificationResult,
        verificationDecision: VerificationDecision
    ) {
        container.removeAllViews()

        when (verificationDecision) {
            VerificationDecision.GOOD -> container.addView(TextView(this).also {
                it.text = "Successfully decoded the contents of the scanned code."
                it.setTextColor(resources.getColor(R.color.success, theme))
            })
            VerificationDecision.WARNING -> container.addView(TextView(this).also {
                it.text = "Decoded the contents of the scanned code with warnings."
                it.setTextColor(resources.getColor(R.color.warning, theme))
            })
            else -> container.addView(TextView(this).also {
                it.text = "Decoded the contents of the scanned code with errors."
                it.setTextColor(resources.getColor(R.color.error, theme))
            })
        }
        addTextView(container, "  Context", verificationResult.contextIdentifier ?: "NONE")
        addTextView(container, "  Base45 decoded", verificationResult.base45Decoded.toString())
        addTextView(container, "  ZLIB decoded", verificationResult.zlibDecoded.toString())
        addTextView(container, "  COSE verified", verificationResult.coseVerified.toString())
        addTextView(container, "  CBOR decoded", verificationResult.cborDecoded.toString())
        addTextView(container, "  Issuer", verificationResult.issuer)
        addTextView(container, "  Issued At", verificationResult.issuedAt?.toString())
        addTextView(container, "  Expiration", verificationResult.expirationTime?.toString())
        addTextView(container, "Data decoded", "")
        addTextView(container, "  Version", data.schemaVersion)
        addTextView(container, "  ID", data.identifier)
        data.subject?.let { sub -> fillSubject(container, sub) }
        data.recoveryStatements?.let {
            it.filterNotNull().forEach { rec -> fillRecovery(container, rec) }
        }
        data.tests?.let { it.filterNotNull().forEach { tst -> fillTest(container, tst) } }
        data.vaccinations?.let { it.filterNotNull().forEach { vac -> fillVac(container, vac) } }
    }

    private fun fillSubject(container: LinearLayout, sub: Person) {
        addTextView(container, "Subject", "")
        addTextView(container, "  Given Name", sub.givenName)
        addTextView(container, "  Given Name Transliterated", sub.givenNameTransliterated)
        addTextView(container, "  Family Name", sub.familyName)
        addTextView(container, "  Family Name Transliterated", sub.familyNameTransliterated)
        addTextView(container, "  Date of Birth", sub.dateOfBirth?.toString())
        addTextView(container, "  Gender", sub.gender)
        sub.identifiers?.let { idList ->
            idList.forEach { entry ->
                entry?.let { id ->
                    addTextView(container, "  Identifier", "")
                    addTextView(container, "    Type", id.type)
                    addTextView(container, "    Id", id.id)
                    addTextView(container, "    Country", id.country)
                }
            }
        }
    }

    private fun fillRecovery(container: LinearLayout, rec: RecoveryStatement) {
        addTextView(container, "Recovery statement", "")
        addTextView(container, "  Disease", rec.disease)
        addTextView(container, "  Date", rec.date?.toString())
        addTextView(container, "  Country", rec.country)
    }

    private fun fillTest(container: LinearLayout, tst: Test) {
        addTextView(container, "Test", "")
        addTextView(container, "  Disease", tst.disease)
        addTextView(container, "  Type", tst.type)
        addTextView(container, "  Name", tst.name)
        addTextView(container, "  Manufacturer", tst.manufacturer)
        addTextView(container, "  Sample origin", tst.sampleOrigin)
        addTextView(container, "  Date of sample", tst.dateTimeSample?.toString())
        addTextView(container, "  Date of result", tst.dateTimeResult?.toString())
        addTextView(container, "  Result", tst.result)
        addTextView(container, "  Facility", tst.testFacility)
        addTextView(container, "  Country", tst.country)
    }

    private fun fillVac(container: LinearLayout, vac: Vaccination) {
        addTextView(container, "Vaccination", "")
        addTextView(container, "  Disease", vac.disease)
        addTextView(container, "  Vaccine", vac.vaccine)
        addTextView(container, "  Product", vac.medicinalProduct)
        addTextView(container, "  Authorisation Holder", vac.authorizationHolder)
        addTextView(container, "  Dose sequence", vac.doseSequence?.toString())
        addTextView(container, "  Total number of doses", vac.doseTotalNumber?.toString())
        addTextView(container, "  Batch", vac.lotNumber)
        addTextView(container, "  Date", vac.date?.toString())
        addTextView(container, "  Administering centre", vac.administeringCentre)
        addTextView(container, "  Country", vac.country)
    }

    private fun addTextView(container: LinearLayout, key: String, value: String?) {
        value?.let { notnull ->
            container.addView(TextView(this).also {
                it.text = "$key: $notnull"
            })
        }
    }

    private fun getChain(): CborProcessingChain {
        val repository = RemoteCachedCertificateRepository("https://dev.a-sit.at/certservice/cert")
        val cryptoService = VerificationCryptoService(repository)
        return CborProcessingChain(
            DefaultCborService(),
            DefaultCoseService(cryptoService),
            DefaultContextIdentifierService(),
            DefaultCompressorService(),
            DefaultBase45Service()
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
