package hntecology.ecology.action

import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import hntecology.ecology.base.HttpClient

class ReverseAction {

    // 지도 검색 (정확도)
    fun search_map(url: String, handler: JsonHttpResponseHandler) {
        val params = RequestParams()

        HttpClient.get(url, params, handler)
    }

}