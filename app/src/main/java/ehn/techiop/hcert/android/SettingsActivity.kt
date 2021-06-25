package ehn.techiop.hcert.android

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<EditTextPreference>(KEY_TRUST_LIST_CONTENT)?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            findPreference<EditTextPreference>(KEY_TRUST_LIST_SIGNATURE)?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            findPreference<Preference>(KEY_DEFAULTS_ASIT)?.setOnPreferenceClickListener {
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                    .putString(KEY_TRUST_LIST_CONTENT, "https://dgc.a-sit.at/ehn/cert/listv2")
                    .putString(KEY_TRUST_LIST_SIGNATURE, "https://dgc.a-sit.at/ehn/cert/sigv2")
                    .putString(
                        KEY_TRUST_LIST_ROOT, "-----BEGIN CERTIFICATE-----\n" +
                                "MIIBJTCBy6ADAgECAgUAwvEVkzAKBggqhkjOPQQDAjAQMQ4wDAYDVQQDDAVFQy1N\n" +
                                "ZTAeFw0yMTA0MjMxMTI3NDhaFw0yMTA1MjMxMTI3NDhaMBAxDjAMBgNVBAMMBUVD\n" +
                                "LU1lMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/OV5UfYrtE140ztF9jOgnux1\n" +
                                "oyNO8Bss4377E/kDhp9EzFZdsgaztfT+wvA29b7rSb2EsHJrr8aQdn3/1ynte6MS\n" +
                                "MBAwDgYDVR0PAQH/BAQDAgWgMAoGCCqGSM49BAMCA0kAMEYCIQC51XwstjIBH10S\n" +
                                "N701EnxWGK3gIgPaUgBN+ljZAs76zQIhAODq4TJ2qAPpFc1FIUOvvlycGJ6QVxNX\n" +
                                "EkhRcgdlVfUb\n" +
                                "-----END CERTIFICATE-----"
                    )
                    .apply()
                onCreatePreferences(savedInstanceState, rootKey)
                true
            }
            findPreference<Preference>(KEY_DEFAULTS_QS)?.setOnPreferenceClickListener {
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                    .putString(KEY_TRUST_LIST_CONTENT, "https://dgc-trusttest.qr.gv.at/trustlist")
                    .putString(KEY_TRUST_LIST_SIGNATURE, "https://dgc-trusttest.qr.gv.at/trustlistsig")
                    .putString(
                        KEY_TRUST_LIST_ROOT, "-----BEGIN CERTIFICATE-----\n" +
                                "MIIB6zCCAZGgAwIBAgIKAXmEuohlRbR2qzAKBggqhkjOPQQDAjBQMQswCQYDVQQG\n" +
                                "EwJBVDEPMA0GA1UECgwGQk1TR1BLMQowCAYDVQQLDAFRMQwwCgYDVQQFEwMwMDEx\n" +
                                "FjAUBgNVBAMMDUFUIERHQyBDU0NBIDEwHhcNMjEwNTE5MTMwNDQ3WhcNMjIwNjE5\n" +
                                "MTMwNDQ3WjBRMQswCQYDVQQGEwJBVDEPMA0GA1UECgwGQk1TR1BLMQowCAYDVQQL\n" +
                                "DAFRMQ8wDQYDVQQFEwYwMDEwMDExFDASBgNVBAMMC0FUIERHQyBUTCAxMFkwEwYH\n" +
                                "KoZIzj0CAQYIKoZIzj0DAQcDQgAE29KpT1eIKsy5Jx3J0xpPLW+fEBF7ma9943/j\n" +
                                "4Z+o1TytLVok9cWjsdasWCS/zcRyAh7HBL+oyMWdFBOWENCQ76NSMFAwDgYDVR0P\n" +
                                "AQH/BAQDAgeAMB0GA1UdDgQWBBQYmsL5sXTdMCyW4UtP5BMxq+UAVzAfBgNVHSME\n" +
                                "GDAWgBR2sKi2xkUpGC1Cr5ehwL0hniIsJzAKBggqhkjOPQQDAgNIADBFAiBse17k\n" +
                                "F5F43q9mRGettRDLprASrxsDO9XxUUp3ObjcWQIhALfUWnserGEPiD7Pa25tg9lj\n" +
                                "wkrqDrMdZHZ39qb+Jf/E\n" +
                                "-----END CERTIFICATE-----"
                    )
                    .apply()
                onCreatePreferences(savedInstanceState, rootKey)
                true
            }
            findPreference<Preference>(KEY_DEFAULTS_PROD)?.setOnPreferenceClickListener {
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                    .putString(KEY_TRUST_LIST_CONTENT, "https://dgc-trust.qr.gv.at/trustlist")
                    .putString(KEY_TRUST_LIST_SIGNATURE, "https://dgc-trust.qr.gv.at/trustlistsig")
                    .putString(
                        KEY_TRUST_LIST_ROOT, "-----BEGIN CERTIFICATE-----\n" +
                                "MIIB1DCCAXmgAwIBAgIKAXnM+Z3eG2QgVzAKBggqhkjOPQQDAjBEMQswCQYDVQQG\n" +
                                "EwJBVDEPMA0GA1UECgwGQk1TR1BLMQwwCgYDVQQFEwMwMDExFjAUBgNVBAMMDUFU\n" +
                                "IERHQyBDU0NBIDEwHhcNMjEwNjAyMTM0NjIxWhcNMjIwNzAyMTM0NjIxWjBFMQsw\n" +
                                "CQYDVQQGEwJBVDEPMA0GA1UECgwGQk1TR1BLMQ8wDQYDVQQFEwYwMDEwMDExFDAS\n" +
                                "BgNVBAMMC0FUIERHQyBUTCAxMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEl2tm\n" +
                                "d16CBHXwcBN0r1Uy+CmNW/b2V0BNP85y5N3JZeo/8l9ey/jIe5mol9fFcGTk9bCk\n" +
                                "8zphVo0SreHa5aWrQKNSMFAwDgYDVR0PAQH/BAQDAgeAMB0GA1UdDgQWBBRTwp6d\n" +
                                "cDGcPUB6IwdDja/a3ncM0TAfBgNVHSMEGDAWgBQfIqwcZRYptMGYs2Nvv90Jnbt7\n" +
                                "ezAKBggqhkjOPQQDAgNJADBGAiEAlR0x3CRuQV/zwHTd2R9WNqZMabXv5XqwHt72\n" +
                                "qtgnjRgCIQCZHIHbCvlgg5uL8ZJQzAxLavqF2w6uUxYVrvYDj2Cqjw==\n" +
                                "-----END CERTIFICATE-----"
                    )
                    .apply()
                onCreatePreferences(savedInstanceState, rootKey)
                true
            }
        }
    }

    companion object {
        val KEY_TRUST_LIST_CONTENT = "trust_list_content"
        val KEY_TRUST_LIST_SIGNATURE = "trust_list_signature"
        val KEY_TRUST_LIST_ROOT = "trust_list_root"
        val KEY_DEFAULTS_ASIT = "defaults_asit"
        val KEY_DEFAULTS_QS = "defaults_qs"
        val KEY_DEFAULTS_PROD = "defaults_prod"
    }

}