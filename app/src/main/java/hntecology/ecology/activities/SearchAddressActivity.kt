package hntecology.ecology.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import hntecology.ecology.R
import hntecology.ecology.base.Utils

class SearchAddressActivity : Activity() {

    private lateinit var webWV: WebView
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_address)

        var intent: Intent = getIntent()

        // var url = getIntent().getStringExtra("url")
        // val url = "http://www.juso.go.kr/addrlink/addrCoordUrl.do?confmKey=U01TX0FVVEgyMDE4MTEyOTE1NTkzODEwODMzODg=&returnUrl=http://devstories.com&resultType=4"
        // val url = "file:///android_asset/juso.html"
        val url = "http://devstories.com/juso/";

        webWV = findViewById(R.id.webviewWV)

        webWV.settings.javaScriptEnabled = true
        // webWV.loadUrl(url)
        // webWV.webViewClient = InterceptingWebViewClient(this, webWV)
        webWV.webViewClient = WebViewClientClass()
        webWV.webChromeClient = WebChromeClientClass()
        webWV.isVerticalScrollBarEnabled = false
        webWV.isScrollbarFadingEnabled = true
        webWV.settings.databaseEnabled = true
        webWV.settings.domStorageEnabled = true

        webWV.loadUrl(url);
    }

    private class WebChromeClientClass : WebChromeClient() {

    }


    private inner class WebViewClientClass : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {

            val url = request.url.toString()

            if (url.startsWith("http://devstories.com/GET_DETAIL")) {
                val x = Utils.getParameter(url, "x")
                val y = Utils.getParameter(url, "y")

                intent.putExtra("x", x)
                intent.putExtra("y", y)
                setResult(RESULT_OK, intent);

                finish()

                return false
            }

            return true


        }

    }

}
