package hntecology.ecology.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import hntecology.ecology.R
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_search_address.*

class SearchAddressActivity : Activity() {

    private lateinit var webWV: WebView
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_address)

        var intent: Intent = getIntent()

        val url = intent.getStringExtra("url")


        webWV = findViewById(R.id.webviewWV)

        webWV.settings.javaScriptEnabled = true
        webWV.loadUrl(url)
        webWV.webViewClient = WebViewClientClass()
        webWV.isVerticalScrollBarEnabled = false
        webWV.isScrollbarFadingEnabled = true
        webWV.settings.databaseEnabled = true
        webWV.settings.domStorageEnabled = true
        
    }


    private inner class WebViewClientClass : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {

            println("url-------------$request")

            println("url-------------${request.url}")

            /*
            if (url.startsWith("http://postcode.map.daum.net/search?region_name=")) {
                val type = Utils.getParameter(url, "type")
                println("type -------$type")
                if(type == "postcode_click"){
                    val addr = Utils.getParameter(url, "addr")

                    val addrArr = addr.split("|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (addrArr.size > 2) {
                        val address = addrArr[2]
                        println("address$address")
                    }
                    view.loadUrl(url)
                    return true

                }
            }else {
                return false
            }
            */

//            intent.putExtra("url",url)
//            setResult(RESULT_OK, intent);
//
//            finish()

            return false

        }

    }

}
