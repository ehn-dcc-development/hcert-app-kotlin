package ehn.techiop.hcert.android

import android.annotation.SuppressLint
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
import ehn.techiop.hcert.kotlin.chain.Chain
import ehn.techiop.hcert.kotlin.chain.DecisionService
import ehn.techiop.hcert.kotlin.chain.VerificationDecision
import ehn.techiop.hcert.kotlin.chain.VerificationResult
import ehn.techiop.hcert.kotlin.chain.impl.PrefilledCertificateRepository
import ehn.techiop.hcert.kotlin.chain.impl.TrustListCertificateRepository
import ehn.techiop.hcert.kotlin.data.*
import ehn.techiop.hcert.kotlin.trust.TrustListDecodeService
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fabTrustList).setOnClickListener {
            thread {
                downloadTrustList()
            }
        }
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

    private fun downloadTrustList() {
        try {
            val content = loadTrustListCached(true)
            val trustList = TrustListDecodeService(loadTrustListAnchor()).decode(content)
            runOnUiThread {
                addTextView(
                    findViewById(R.id.container_data),
                    "Loaded trust list, contains ${trustList.certificates.size} entries\n" +
                            "Valid from ${trustList.validFrom} until ${trustList.validUntil}"
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            runOnUiThread {
                addTextView(
                    findViewById(R.id.container_data),
                    "Error on download: ${e.message}"
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let { intentResult ->
            intentResult.contents?.let {
                addTextView(findViewById(R.id.container_data), "Validating ...")
                findViewById<LinearLayout>(R.id.container_data).removeAllViews()
                thread {
                    verifyOnBackgroundThread(it)
                }
            }
        }
    }

    private fun verifyOnBackgroundThread(qrCodeContent: String) {
        try {
            val verificationResult = VerificationResult()
            val vaccinationData = getChain().decode(qrCodeContent, verificationResult)
            val verificationDecision = DecisionService().decide(verificationResult)
            val data = GreenCertificate.fromEuSchema(vaccinationData)
            runOnUiThread {
                fillLayout(
                    findViewById(R.id.container_data),
                    data,
                    verificationResult,
                    verificationDecision
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            runOnUiThread {
                findViewById<LinearLayout>(R.id.container_data).addView(TextView(this).also {
                    it.text = "Error on validation: ${e.message}"
                    it.setTextColor(resources.getColor(R.color.error, theme))
                })
            }
        }
    }

    private fun fillLayout(
        container: LinearLayout,
        data: GreenCertificate?,
        it: VerificationResult,
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
        addTextView(container, "  Context", it.contextIdentifier ?: "NONE")
        addTextView(container, "  Base45 decoded", it.base45Decoded.toString())
        addTextView(container, "  ZLIB decoded", it.zlibDecoded.toString())
        addTextView(container, "  COSE verified", it.coseVerified.toString())
        addTextView(container, "  CBOR decoded", it.cborDecoded.toString())
        addTextView(container, "  Issuer", it.issuer)
        addTextView(container, "  Issued At", it.issuedAt?.toString())
        addTextView(container, "  Expiration", it.expirationTime?.toString())
        addTextView(container, "  Cert. valid from", it.certificateValidFrom?.toString())
        addTextView(container, "  Cert. valid until", it.certificateValidUntil?.toString())
        addTextView(container, "  Cert. valid content", it.certificateValidContent.toString())
        addTextView(container, "  Content", it.content.toString())
        if (data == null) {
            addTextView(container, "No data decoded")
            return
        }
        addTextView(container, "Data decoded:")
        addTextView(container, "  Version", data.schemaVersion)
        addTextView(container, "  DateOfBirth", data.dateOfBirth.toString())
        fillSubject(container, data.subject)
        data.recoveryStatements?.let {
            it.filterNotNull().forEach { rec -> fillRecovery(container, rec) }
        }
        data.tests?.let { it.filterNotNull().forEach { tst -> fillTest(container, tst) } }
        data.vaccinations?.let { it.filterNotNull().forEach { vac -> fillVac(container, vac) } }
    }

    private fun fillSubject(container: LinearLayout, it: Person) {
        addTextView(container, "Person:")
        addTextView(container, "  Given Name", it.givenName)
        addTextView(container, "  Given Name Transliterated", it.givenNameTransliterated)
        addTextView(container, "  Family Name", it.familyName)
        addTextView(container, "  Family Name Transliterated", it.familyNameTransliterated)
    }

    private fun fillRecovery(container: LinearLayout, it: RecoveryStatement) {
        addTextView(container, "Recovery statement:")
        addTextView(container, "  Target", it.target.value)
        addTextView(
            container,
            "  Date first pos. result",
            it.dateOfFirstPositiveTestResult.toString()
        )
        addTextView(container, "  Cert. valid from", it.certificateValidFrom.toString())
        addTextView(container, "  Cert. valid until", it.certificateValidUntil.toString())
        addTextView(container, "  Country", it.country)
        addTextView(container, "  Cert. Issuer", it.certificateIssuer)
        addTextView(container, "  Cert. Id", it.certificateIdentifier)
    }

    private fun fillTest(container: LinearLayout, it: Test) {
        addTextView(container, "Test:")
        addTextView(container, "  Target", it.target.value)
        addTextView(container, "  Type", it.type)
        addTextView(container, "  Name (NAA)", it.nameNaa)
        addTextView(container, "  Name (RAT)", it.nameRat)
        addTextView(container, "  Date of sample", it.dateTimeSample.toString())
        addTextView(container, "  Date of result", it.dateTimeResult.toString())
        addTextView(container, "  Result", it.resultPositive.toString())
        addTextView(container, "  Facility", it.testFacility)
        addTextView(container, "  Country", it.country)
        addTextView(container, "  Cert. Issuer", it.certificateIssuer)
        addTextView(container, "  Cert. Id", it.certificateIdentifier)
    }

    private fun fillVac(container: LinearLayout, it: Vaccination) {
        addTextView(container, "Vaccination:")
        addTextView(container, "  Target", it.target.value)
        addTextView(container, "  Vaccine", it.vaccine.value)
        addTextView(container, "  Product", it.medicinalProduct.value)
        addTextView(container, "  Authorisation Holder", it.authorizationHolder.value)
        addTextView(container, "  Dose Number", it.doseNumber.toString())
        addTextView(container, "  Total number of doses", it.doseTotalNumber.toString())
        addTextView(container, "  Date", it.date.toString())
        addTextView(container, "  Country", it.country)
        addTextView(container, "  Cert. Issuer", it.certificateIssuer)
        addTextView(container, "  Cert. Id", it.certificateIdentifier)
    }

    private fun addTextView(container: LinearLayout, key: String) {
        container.addView(TextView(this).also {
            it.text = key
        })
    }

    private fun addTextView(container: LinearLayout, key: String, value: String?) {
        value?.let { notnull ->
            container.addView(TextView(this).also {
                it.text = "$key: $notnull"
            })
        }
    }

    private fun getChain(): Chain {
        val trustAnchor = loadTrustListAnchor()
        val trustListEncoded = loadTrustListCached()
        val repository = TrustListCertificateRepository(trustListEncoded, trustAnchor)
        return Chain.buildVerificationChain(repository)
    }

    private fun loadTrustListCached(forceDownload: Boolean = false): ByteArray {
        val file = File(applicationContext.filesDir, "trust_list.bin")
        if (file.exists() && !forceDownload) {
            return file.readBytes()
        }
        val content = loadTrustListFromWeb()
        FileOutputStream(file, false).use {
            it.write(content)
        }
        return content
    }

    private fun loadTrustListAnchor(): PrefilledCertificateRepository {
        val trustAnchorResource = resources.openRawResource(R.raw.trust_list_anchor)
        val trustAnchorCertPem = trustAnchorResource.readBytes().decodeToString()
        return PrefilledCertificateRepository(trustAnchorCertPem)
    }

    private fun loadTrustListFromWeb(): ByteArray {
        val url = "https://dgc.a-sit.at/ehn/cert/list"
        val request = Request.Builder().get().url(url).build()
        val response = OkHttpClient.Builder().build().newCall(request).execute()
        response.body?.let {
            return it.bytes()
        }
        throw IllegalArgumentException("Could not load trust list from $url")
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
