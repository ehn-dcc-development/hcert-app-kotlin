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
            runOnUiThread {
                findViewById<TextView>(R.id.textview_first).text = ""
                fillLayout(
                    findViewById<LinearLayout>(R.id.container_data),
                    vaccinationData,
                    verificationResult
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
        data: VaccinationData,
        verificationResult: VerificationResult
    ) {
        container.removeAllViews()
        container.addView(TextView(this).also {
            it.text = "Successfully decoded the contents of the scanned code."
            it.setTextColor(resources.getColor(R.color.success, theme))
        })
        addTextView(container, "COSE verified", verificationResult.coseVerified.toString())
        addTextView(container, "ZLIB decoded", verificationResult.zlibDecoded.toString())
        addTextView(container, "CBOR decoded", verificationResult.cborDecoded.toString())
        addTextView(container, "Base45 decoded", verificationResult.base45Decoded.toString())
        addTextView(container, "ValSuite prefix", verificationResult.valSuitePrefix)
        data.sub?.let { sub -> fillSubject(container, sub) }
        data.rec?.let { it.filterNotNull().forEach { rec -> fillRecovery(container, rec) } }
        data.tst?.let { it.filterNotNull().forEach { tst -> fillTest(container, tst) } }
        data.cert?.let { cert -> fillCertificate(container, cert) }
        data.vac?.let { it.filterNotNull().forEach { vac -> fillVac(container, vac) } }
    }

    private fun fillSubject(container: LinearLayout, sub: Person) {
        addTextView(container, "Subject", "")
        addTextView(container, "  Name", sub.n)
        addTextView(container, "  Given Name", sub.gn)
        addTextView(container, "  Family Name", sub.fn)
        addTextView(container, "  Date of Birth", sub.dob)
        addTextView(container, "  Gender", sub.gen)
        sub.id?.let { idList ->
            idList.forEach { entry ->
                entry?.let { id ->
                    addTextView(container, "  Identifier", "")
                    addTextView(container, "    Type", id.t)
                    addTextView(container, "    id", id.i)
                }
            }
        }
    }

    private fun fillRecovery(container: LinearLayout, rec: PastInfection) {
        addTextView(container, "Recovery statement", "")
        addTextView(container, "  Disease", rec.dis)
        addTextView(container, "  Date", rec.dat)
        addTextView(container, "  Country", rec.cou)
    }

    private fun fillTest(container: LinearLayout, tst: Test) {
        addTextView(container, "Test", "")
        addTextView(container, "  Disease", tst.dis)
        addTextView(container, "  Type", tst.typ)
        addTextView(container, "  Name", tst.tna)
        addTextView(container, "  Manufacturer", tst.tma)
        addTextView(container, "  Sample origin", tst.ori)
        addTextView(container, "  Date", tst.dat)
        addTextView(container, "  Date of sample", tst.dts)
        addTextView(container, "  Date of result", tst.dtr)
        addTextView(container, "  Result", tst.res)
        addTextView(container, "  Facility", tst.fac)
        addTextView(container, "  Country", tst.cou)
    }

    private fun fillVac(container: LinearLayout, vac: Vaccination) {
        addTextView(container, "Vaccination", "")
        addTextView(container, "  Disease", vac.dis)
        addTextView(container, "  Vaccine", vac.des)
        addTextView(container, "  Product", vac.nam)
        addTextView(container, "  VAP", vac.vap)
        addTextView(container, "  MEP", vac.mep)
        addTextView(container, "  Authorisation Holder", vac.aut)
        addTextView(container, "  Dose sequence", vac.seq?.toString())
        addTextView(container, "  Total number of doses", vac.tot?.toString())
        addTextView(container, "  Batch", vac.lot)
        addTextView(container, "  Date", vac.dat)
        addTextView(container, "  Administering centre", vac.adm)
        addTextView(container, "  Country", vac.cou)
    }

    private fun fillCertificate(container: LinearLayout, cert: DocumentMetadata) {
        addTextView(container, "Certificate Metadata", "")
        addTextView(container, "  Issuer", cert.`is`)
        addTextView(container, "  Identifier", cert.id)
        addTextView(container, "  Valid from", cert.vf)
        addTextView(container, "  Valid until", cert.vu)
        addTextView(container, "  Country", cert.co)
        addTextView(container, "  Version", cert.vr)
    }

    private fun addTextView(container: LinearLayout, key: String, value: String?) {
        value?.let { notnull ->
            container.addView(TextView(this).also {
                it.text = "$key: $notnull"
            })
        }
    }

    private fun getChain() = CborProcessingChain(
        CborService(),
        LenientCoseService(VerificationCryptoService("https://dev.a-sit.at/certservice/cert")),
        LenientValSuiteService(),
        CompressorService(),
        Base45Service()
    )

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