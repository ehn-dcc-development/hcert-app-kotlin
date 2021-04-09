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
import ehn.techiop.hcert.android.chain.*
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
                    try {
                        val vaccinationData = getChain().verify(it)
                        runOnUiThread {
                            findViewById<TextView>(R.id.textview_first).text = ""
                            fillLayout(
                                findViewById<LinearLayout>(R.id.container_data),
                                vaccinationData
                            )
                        }
                    } catch (e: Throwable) {
                        runOnUiThread {
                            findViewById<LinearLayout>(R.id.container_data).addView(TextView(this).also {
                                it.text = "Error on validation: ${e.message}"
                                it.setTextColor(resources.getColor(R.color.error, theme))
                            })
                        }
                    }
                }
            }
        }
    }

    private fun fillLayout(container: LinearLayout, data: VaccinationData) {
        container.removeAllViews()
        container.addView(TextView(this).also {
            it.text = "Successfully validated the scanned code."
            it.setTextColor(resources.getColor(R.color.success, theme))
        })
        data.sub?.let { sub ->
            fillSubject(container, sub)
        }
        data.rec?.let { rec ->
            fillRecovery(container, rec)
        }
        data.tst?.let { tst ->
            fillTest(container, tst)
        }
        data.vac?.let { vacList ->
            vacList.forEach { entry ->
                entry?.let { vac ->
                    fillVac(container, vac)
                }
            }
        }
    }

    private fun fillSubject(
        container: LinearLayout,
        sub: Person
    ) {
        container.addView(TextView(this).also { it.text = "Subject:" })
        container.addView(TextView(this).also { it.text = "  Name: ${sub.n}" })
        container.addView(TextView(this).also { it.text = "  DoB: ${sub.dob}" })
        sub.id?.let { idList ->
            idList.forEach { entry ->
                entry?.let { id ->
                    container.addView(TextView(this).also { it.text = "  Identifier:" })
                    container.addView(TextView(this).also { it.text = "      ${id.t} = ${id.i}" })
                }
            }
        }
    }

    private fun fillRecovery(
        container: LinearLayout,
        rec: PastInfection
    ) {
        container.addView(TextView(this).also { it.text = "Recovery statement:" })
        container.addView(TextView(this).also { it.text = "  Disease: ${rec.dis}" })
        container.addView(TextView(this).also { it.text = "  Date: ${rec.dat}" })
        container.addView(TextView(this).also { it.text = "  Country: ${rec.cou}" })
    }

    private fun fillTest(
        container: LinearLayout,
        tst: Test
    ) {
        container.addView(TextView(this).also { it.text = "Test:" })
        container.addView(TextView(this).also { it.text = "  Disease: ${tst.dis}" })
        container.addView(TextView(this).also { it.text = "  Type: ${tst.typ}" })
        container.addView(TextView(this).also { it.text = "  Name: ${tst.tna}" })
        container.addView(TextView(this).also { it.text = "  Manufacturer: ${tst.tma}" })
        container.addView(TextView(this).also { it.text = "  Sample origin: ${tst.ori}" })
        container.addView(TextView(this).also { it.text = "  Date: ${tst.dat}" })
        container.addView(TextView(this).also { it.text = "  Result: ${tst.res}" })
        container.addView(TextView(this).also { it.text = "  Facility: ${tst.fac}" })
        container.addView(TextView(this).also { it.text = "  Country: ${tst.cou}" })
    }

    private fun fillVac(
        container: LinearLayout,
        vac: Vaccination
    ) {
        container.addView(TextView(this).also { it.text = "Vaccination:" })
        container.addView(TextView(this).also { it.text = "  Disease: ${vac.dis}" })
        container.addView(TextView(this).also { it.text = "  Vaccine: ${vac.des}" })
        container.addView(TextView(this).also { it.text = "  Product: ${vac.nam}" })
        container.addView(TextView(this).also { it.text = "  Authorisation Holder: ${vac.aut}" })
        container.addView(TextView(this).also { it.text = "  Dose sequence: ${vac.seq}" })
        container.addView(TextView(this).also { it.text = "  Total number of doses: ${vac.tot}" })
        container.addView(TextView(this).also { it.text = "  Batch: ${vac.lot}" })
        container.addView(TextView(this).also { it.text = "  Date: ${vac.dat}" })
        container.addView(TextView(this).also { it.text = "  Administering centre: ${vac.adm}" })
        container.addView(TextView(this).also { it.text = "  Country: ${vac.cou}" })
    }

    private fun getChain() = CborProcessingChain(
        CborService(VerificationCryptoService("https://dev.a-sit.at/certservice/cert")),
        ValSuiteService(),
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