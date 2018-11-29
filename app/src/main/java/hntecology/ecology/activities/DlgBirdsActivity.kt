package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ListView
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgBirdsAdapter
import hntecology.ecology.adapter.DlgBirdsAdapter2
import hntecology.ecology.model.Birds
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.EndangeredSelect
import kotlinx.android.synthetic.main.activity_dlgvegetation.*
import kotlinx.android.synthetic.main.dlg_birds.*
import java.util.*
import kotlin.collections.ArrayList

class DlgBirdsActivity : Activity() {

    private lateinit var context:Context;

    private var copyadapterData:ArrayList<Birds> = ArrayList<Birds>()

    private var endangerdDatas : ArrayList<Endangered> = ArrayList<Endangered>()

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : java.util.ArrayList<Birds>
    private lateinit var listdata2 : java.util.ArrayList<EndangeredSelect>

    private lateinit var listAdapte1: DlgBirdsAdapter;
    private lateinit var listAdapter2 : DlgBirdsAdapter2

    var chkData = false

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_birds)

        context = this;

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val item = EndangeredSelect("a","성조가 둥지 또는 둥지가 있을 것으로 예상되는 장소를 3회 이상 출입하는 것을 관찰",false)
        val item2 = EndangeredSelect("b","성조가 포란 또는 새끼를 품고 있는 것을 관찰",false)
        val item3 = EndangeredSelect("c","성조가 새끼의 배설물을 운반하고 있는 것을 관찰",false)
        val item4 = EndangeredSelect("d","성조가 새끼에게 먹이를 운반 또는 경계하는 것을 관찰",false)
        val item5 = EndangeredSelect("e","의상행동을 관찰",false)
        val item6 = EndangeredSelect("f","교미행동을 관찰(겨울철새/통과철새는 제외)",false)
        val item7 = EndangeredSelect("g","당해 또는 2년 이내에 이소한 것으로 추정되는 둥지를 관찰",false)
        val item8 = EndangeredSelect("h","둥지 트는 행동을 관찰(둥지로 이용코자 땅 파는 행동 포함)",false)
        val item9 = EndangeredSelect("i","성조가 둥지를 틀 때 쓰이는 재료를 운반하는 것을 관찰",false)
        val item10 = EndangeredSelect("j","알이 있는 둥지를 관찰",false)
        val item11 = EndangeredSelect("k","성조가 앉아 있는 둥지 근처에서 그 종의 알 껍질을 관찰",false)
        val item12 = EndangeredSelect("l","새끼가 들어 있는 둥지를 관찰",false)
        val item13 = EndangeredSelect("m","둥지 근처에서 거의 이동하지 못하는 새끼를 관찰",false)
        val item14 = EndangeredSelect("n","새끼의 소리를 들음",false)

        val enDataList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

        listView1 = findViewById(R.id.listLV)
        listView2 = findViewById(R.id.list2LV)

        listdata1 = java.util.ArrayList()
        listdata2 = java.util.ArrayList()

        listAdapte1 = DlgBirdsAdapter(context, listdata1);
        listAdapter2 = DlgBirdsAdapter2(context, listdata2)

        val dataList:Array<String> = arrayOf("no","taxon","zoological","name_kr","author","year","Phylum_name","Phylum_name_kr","Class_name","Class_name_kr","Order_name","Order_name_kr","Family_name"
                ,"Family_name_kr","Genus_name","Genus_name_kr","Species_name","Species_name_kr");

        val data = db.query(tableName, dataList, null, null, null, null, "case when name_kr like '[0-9]%' then 3 when name_kr like '[A-Za-z]%' then 2 else 1 end, name_kr", null);

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapter2

        dataBirdsList(listdata1,data)

        copyadapterData.addAll(listdata1)

        val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

        selectLL.setOnClickListener {
            var name:String = ""
            var family_name:String = ""
            var zoological:String = ""
            var code:ArrayList<String> = ArrayList<String>()

            for(i in 0..listdata1.size-1){
                if (listdata1.get(i).chkSelect == true){
                    name = listdata1.get(i).name_kr!!
                    family_name = listdata1.get(i).Family_name_kr!!
                    zoological = listdata1.get(i).zoological!!
                }
            }

            if(listdata2 != null){
                for(i in 0..listdata2.size-1){
                    if(listdata2.get(i).is_checked == true){
                        code.add(listdata2.get(i).SIGN!!)
                    }
                }
            }

            intent.putExtra("name", name)
            intent.putExtra("family_name", family_name)
            intent.putExtra("zoological", zoological)

            if(code != null){
                intent.putExtra("code", code)
            }
            setResult(RESULT_OK, intent);

            finish()

        }

        listView1.setOnItemClickListener { parent, view, position, id ->
            if(listdata2 != null){
                for(i in 0..listdata2.size-1){
                    listdata2.get(i).is_checked = false
                }
            }

            chkData = false
            listAdapter2.clearItem()

            selectTV.visibility = View.INVISIBLE

            listAdapte1.setItemSelect(position)

            for(i in 0..listdata1.size-1){
                listdata1.get(i).chkSelect = false
            }

            var data = listdata1.get(position)

            if(data.chkSelect == false){
                data.chkSelect = true
                listAdapte1.notifyDataSetChanged()
            }else {
                data.chkSelect = false
                listAdapte1.notifyDataSetChanged()
            }

            var name = data.name_kr

            val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

            val EndangeredData = db.query("ENDANGERED", dataEndangeredList, "TITLE = '$name'", null, null, null, null, null);

            while (EndangeredData.moveToNext()) {

                var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                chkData = true

            }

            if(chkData){
                selectTV.visibility = View.VISIBLE
                dlg_probars.visibility = View.VISIBLE
                listdata2.add(item)
                listdata2.add(item2)
                listdata2.add(item3)
                listdata2.add(item4)
                listdata2.add(item5)
                listdata2.add(item6)
                listdata2.add(item7)
                listdata2.add(item8)
                listdata2.add(item9)
                listdata2.add(item10)
                listdata2.add(item11)
                listdata2.add(item12)
                listdata2.add(item13)
                listdata2.add(item14)
                dlg_probars.visibility = View.GONE
            }else {
                if(listdata2 != null) {
                    listdata2.clear()
                }
            }

        }

        listView2.setOnItemClickListener { adapterView, view, position, l ->

            val data = listdata2.get(position)

            if(data.is_checked == false){
                data.is_checked = true
                listAdapter2.notifyDataSetChanged()
            }else {
                data.is_checked = false
                listAdapter2.notifyDataSetChanged()
            }

        }

        searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                val text = searchET.text.toString()
                search(text)
            }
        });

    }

    fun search(charText: String){
        listdata1.clear()

        if(charText.length == 0){

            listdata1.addAll(copyadapterData)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copyadapterData.size-1){

//                val name =  Utils.getString(copyadapterData.get(i).name_kr.toLowerCase(Locale.getDefault()).contains(charText), copyadapterData.get(i).name_kr);

//                names.add(name)

//                println(names.get(i) + "-------------------------")

            }

//            for(i in 0..names.size-1){
//
//                if(names.get(i).toLowerCase().contains(charText)){
//                    listdata1.add(copyadapterData.get(i))
//                }
//
//            }

        }

        listAdapte1.notifyDataSetChanged()

    }

    fun dataBirdsList(listdata: java.util.ArrayList<Birds>, data: Cursor) {

        while (data.moveToNext()){

            var model : Birds;

            model = Birds(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getString(8),data.getString(9),data.getString(10)
                    ,data.getString(11),data.getString(12),data.getString(13),data.getString(14),data.getString(15),data.getString(16),data.getString(17),false);

            listdata.add(model)
        }
    }

    fun searchSql(searchStr: String): String {
        var sql:String = "Select * FROM birds"
        if (TextUtils.isEmpty(searchStr) == false) {
            sql += " WHERE ";
            sql += ChoSearchQuery.makeQuery(searchStr);
        }

      return sql;
    }




    object ChoSearchQuery {
        val EVENT_CODE_LENGTH = 6

        val DIGIT_BEGIN_UNICODE = 0x30 //0
        val DIGIT_END_UNICODE = 0x3A //9

        val QUERY_DELIM = 39//'
        val LARGE_ALPHA_BEGIN_UNICODE = 0

        val HANGUL_BEGIN_UNICODE = 0xAC00 // 가
        val HANGUL_END_UNICODE = 0xD7A3 // ?
        val HANGUL_CHO_UNIT = 588 //한글 초성글자간 간격
        val HANGUL_JUNG_UNIT = 28 //한글 중성글자간 간격

        val CHO_LIST = charArrayOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
        val CHO_SEARCH_LIST = booleanArrayOf(true, false, true, true, false, true, true, true, false, true, false, true, true, false, true, true, true, true, true)

        /**
         * 문자를 유니코드(10진수)로 변환 후 반환한다.
         * @param ch 문자
         * @return 10진수 유니코드
         */
        fun convertCharToUnicode(ch: Char): Int {
            return ch.toInt()
        }

        /**
         * 10진수를 16진수 문자열로 변환한다.
         * @param decimal 10진수 숫자
         * @return 16진수 문자열
         */
        private fun toHexString(decimal: Int): String {
            val intDec = java.lang.Long.valueOf(decimal.toLong())
            return java.lang.Long.toHexString(intDec)
        }

        /**
         * 유니코드(16진수)를 문자로 변환 후 반환한다.
         * @param hexUnicode Unicode Hex String
         * @return 문자값
         */
        fun convertUnicodeToChar(hexUnicode: String): Char {
            return Integer.parseInt(hexUnicode, 16).toChar()
        }

        /**
         * 유니코드(10진수)를 문자로 변환 후 반환한다.
         * @param unicode
         * @return 문자값
         */
        fun convertUnicodeToChar(unicode: Int): Char {
            return convertUnicodeToChar(toHexString(unicode))
        }

        /**
         * 검색 문자열을 파싱해서 SQL Query 조건 문자열을 만든다.
         * @param strSearch 검색 문자열
         * @return SQL Query 조건 문자열
         */
        fun makeQuery(strSearch: String?): String {
            var strSearch = strSearch
            strSearch = strSearch?.trim { it <= ' ' } ?: "null"

            val retQuery = StringBuilder()

            var nChoPosition: Int
            var nNextChoPosition: Int
            var StartUnicode: Int
            var EndUnicode: Int

            var nQueryIndex = 0
            //            boolean bChosung = false;
            val query = StringBuilder()
            for (nIndex in 0 until strSearch.length) {
                nChoPosition = -1
                nNextChoPosition = -1
                StartUnicode = -1
                EndUnicode = -1

                if (strSearch[nIndex].toInt() == QUERY_DELIM)
                    continue

                if (nQueryIndex != 0) {
                    query.append(" AND ")
                }

                for (nChoIndex in CHO_LIST.indices) {
                    if (strSearch[nIndex] == CHO_LIST[nChoIndex]) {
                        nChoPosition = nChoIndex
                        nNextChoPosition = nChoPosition + 1
                        while (nNextChoPosition < CHO_SEARCH_LIST.size) {
                            if (CHO_SEARCH_LIST[nNextChoPosition])
                                break
                            nNextChoPosition++
                        }
                        break
                    }
                }

                if (nChoPosition >= 0) { //초성이 있을 경우
                    //                    bChosung = true;
                    StartUnicode = HANGUL_BEGIN_UNICODE + nChoPosition * HANGUL_CHO_UNIT
                    EndUnicode = HANGUL_BEGIN_UNICODE + nNextChoPosition * HANGUL_CHO_UNIT
                } else {
                    val Unicode = convertCharToUnicode(strSearch[nIndex])
                    if (Unicode >= HANGUL_BEGIN_UNICODE && Unicode <= HANGUL_END_UNICODE) {
                        val Jong = (Unicode - HANGUL_BEGIN_UNICODE) % HANGUL_CHO_UNIT % HANGUL_JUNG_UNIT

                        if (Jong == 0) {// 초성+중성으로 되어 있는 경우
                            StartUnicode = Unicode
                            EndUnicode = Unicode + HANGUL_JUNG_UNIT
                        } else {
                            StartUnicode = Unicode
                            EndUnicode = Unicode
                        }
                    }
                }

                //Log.d("SearchQuery","query "+strSearch.codePointAt(nIndex));
                if (StartUnicode > 0 && EndUnicode > 0) {
                    if (StartUnicode == EndUnicode)
                        query.append("substr(name," + (nIndex + 1) + ",1)='" + strSearch[nIndex] + "'")
                    else
                        query.append("(substr(name," + (nIndex + 1) + ",1)>='" + convertUnicodeToChar(StartUnicode)
                                + "' AND substr(name," + (nIndex + 1) + ",1)<'" + convertUnicodeToChar(EndUnicode) + "')")
                } else {
                    if (Character.isLowerCase(strSearch[nIndex])) { //영문 소문자
                        query.append("(substr(name," + (nIndex + 1) + ",1)='" + strSearch[nIndex] + "'"
                                + " OR substr(name," + (nIndex + 1) + ",1)='" + Character.toUpperCase(strSearch[nIndex]) + "')")
                    } else if (Character.isUpperCase(strSearch[nIndex])) { //영문 대문자
                        query.append("(substr(name," + (nIndex + 1) + ",1)='" + strSearch[nIndex] + "'"
                                + " OR substr(name," + (nIndex + 1) + ",1)='" + Character.toLowerCase(strSearch[nIndex]) + "')")
                    } else
                    //기타 문자
                        query.append("substr(name," + (nIndex + 1) + ",1)='" + strSearch[nIndex] + "'")
                }

                nQueryIndex++
            }

            if (query.length > 0 && strSearch != null && strSearch.trim { it <= ' ' }.length > 0) {
                retQuery.append("(" + query.toString() + ")")

                if (strSearch.indexOf(" ") != -1) {
                    // 공백 구분 단어에 대해 단어 모두 포함 검색
                    val tokens = strSearch.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    retQuery.append(" OR (")
                    var i = 0
                    val isize = tokens.size
                    while (i < isize) {
                        val token = tokens[i]
                        if (i != 0) {
                            retQuery.append(" AND ")
                        }
                        retQuery.append("name like '%$token%'")
                        i++
                    }
                    retQuery.append(")")
                } else {
                    // LIKE 검색 추가
                    retQuery.append(" OR name like '%$strSearch%'")
                }
            } else {
                retQuery.append(query.toString())
            }
            //        }
            //Log.d("SearchQuery","query "+query.toString());
            return retQuery.toString()
        }

    }

}




