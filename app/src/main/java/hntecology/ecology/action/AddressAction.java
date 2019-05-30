package hntecology.ecology.action;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.HashMap;
import java.util.Map;

import hntecology.ecology.base.HttpClient;

/**
 * Created by dev2 on 2018-01-26.
 */

public class AddressAction {

    // 지도 검색 (정확도)
    public static void search_map(String keyword, int page, int size,String select, JsonHttpResponseHandler handler) {
        Map<String, String> headers = new HashMap<>();
        RequestParams params = new RequestParams();
        params.put("query", keyword);
        params.put("request", "search");
        params.put("size", "30");
        params.put("category",select);
        params.put("type", "address");
        params.put("page", page);
        params.put("key", "366D7FAA-4B63-3ACA-8C05-AAD64D7DD580");

//        HttpClient.get("http://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword + "&page=" + page + "&size=" + size, headers, params, handler);
        HttpClient.get("http://api.vworld.kr/req/search", headers, params, handler);


    }

    // 좌표로 주소검색
    public static void coord2addr(RequestParams params, JsonHttpResponseHandler handler) {
        HttpClient.post("/main/api_coord2addr.json", params, handler);
    }

}
