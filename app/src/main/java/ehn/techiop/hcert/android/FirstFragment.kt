package ehn.techiop.hcert.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.container_data).addView(TextView(context).also {
            it.text = "This is ${BuildConfig.APPLICATION_ID}, Version ${BuildConfig.VERSION_NAME}"
        })
        view.findViewById<LinearLayout>(R.id.container_data).addView(TextView(context).also {
            it.text =
                "Press the camera button in the right-hand lower corner to scan a QR Code"
        })
        view.findViewById<LinearLayout>(R.id.container_data).addView(TextView(context).also {
            it.text =
                "Press the download button to load the trust list (URL can be configured in the settings dialog)"
        })

    }
}