package hntecology.ecology.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import hntecology.ecology.R
import hntecology.ecology.action.AddressAction
import hntecology.ecology.adapter.AddressAdapter
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_search_address.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SearchAddressActivity : Activity() {

    private lateinit var webWV: WebView
    private var context: Context? = null
    private lateinit var progressDialog: ProgressDialog

    private var adapterData :ArrayList<JSONObject> = ArrayList<JSONObject>()

    private lateinit var addressAdapter: AddressAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_address)

        this.context = this
        progressDialog = ProgressDialog(context)

        addressAdapter = AddressAdapter(this, R.layout.item_address, adapterData)
        listLV.setAdapter(addressAdapter)
        listLV.setOnItemClickListener { parent, view, position, id ->

            val item: JSONObject =  adapterData.get(position)
            val point: JSONObject =  item.getJSONObject("point")

            val x = Utils.getDouble(point, "x")
            val y = Utils.getDouble(point, "y")

            println("x : $x, y : $y")

            intent.putExtra("x", x)
            intent.putExtra("y", y)
            setResult(RESULT_OK, intent);

            finish()
        }

        /*
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
        */

        findBtn.setOnClickListener {
            val address = Utils.getString(addressET)
            if(address.isEmpty()) {
                Utils.alert(this, "주소를 입력해주세요.")
                return@setOnClickListener
            }
            findLocation(address)
        }

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

    private fun findLocation(address: String) {
        val params = RequestParams()
        params.put("address", address)

        println(params)

        AddressAction.search_map(address, 1, 30, object : JsonHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog.dismiss()
                }

                println(response)

                adapterData.clear()

                try {
                    if (response!!.getJSONObject("response") != null) {
                        val res = response.getJSONObject("response")
                        val result = res.getJSONObject("result")
                        val items = result.getJSONArray("items")

                        for(idx in 0 until items.length()) {
                            adapterData.add(items.getJSONObject(idx))
                        }

                        addressAdapter.notifyDataSetChanged()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                super.onSuccess(statusCode, headers, response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>?, responseString: String?) {

                // System.out.println(responseString);
            }

            private fun error() {
                if (progressDialog != null) {
                    Utils.alert(context, "조회중 장애가 발생하였습니다.")
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, responseString: String?, throwable: Throwable) {
                if (progressDialog != null) {
                    progressDialog.dismiss()
                }

                //                System.out.println(responseString);

                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONObject?) {
                if (progressDialog != null) {
                    progressDialog.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>?, throwable: Throwable, errorResponse: JSONArray?) {
                if (progressDialog != null) {
                    progressDialog.dismiss()
                }
                throwable.printStackTrace()
                error()
            }

            override fun onStart() {
                // show dialog
                if (progressDialog != null) {

                    progressDialog.show()
                }
            }

            override fun onFinish() {
                if (progressDialog != null) {
                    progressDialog.dismiss()
                }
            }
        })
    }


}
