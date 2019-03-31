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
    public static void search_map(String keyword, int page, int size, JsonHttpResponseHandler handler) {
        Map<String, String> headers = new HashMap<>();
//        headers.put("Authorization", "KakaoAK 9928b24dd82518aeab9ebc55fa5989b3");
        headers.put("Authorization", "KakaoAK 46f1df579c075f861d2e6762ed85f3db");
        RequestParams params = new RequestParams();
        params.put("query", keyword);
        params.put("page", page);
        params.put("size", size);

//        HttpClient.get("http://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword + "&page=" + page + "&size=" + size, headers, params, handler);
        HttpClient.get("https://dapi.kakao.com/v2/local/search/address.json", headers, params, handler);
    }

    // 좌표로 주소검색
    public static void coord2addr(RequestParams params, JsonHttpResponseHandler handler) {
        HttpClient.post("/main/api_coord2addr.json", params, handler);
    }

}
