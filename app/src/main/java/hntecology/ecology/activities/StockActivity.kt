package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Base
import hntecology.ecology.model.StockMap
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_stock.*
import java.io.File
import java.io.IOException
import java.util.ArrayList

class StockActivity : Activity() {

    lateinit var context: Context;

    var userName = "";

    var pk: String? = null
    var keyId: String? = null;
    var chkdata: Boolean = false;
    var basechkdata: Boolean = false

    var GPS_LAT: String = ""
    var GPS_LON: String = ""

    var lat: String = ""
    var log: String = ""

    var polygonid: String? = null

    val FRTP_CD = 100
    val SET_FRTPCD = 101
    val STORUNST_CD = 102
    val FROR_CD = 103
    val DMCLS_CD = 104
    val AGCLS_CD = 105
    val DNST_CD = 106

    var dataArray:ArrayList<StockMap> = ArrayList<StockMap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        this.context = this;

        invdtTV.setText(Utils.todayStr())
        invtmTV.setText(Utils.timeStr())

        userName = PrefUtils.getStringPreference(context, "name");

        invpersonTV.setText(userName)

        window.setGravity(Gravity.RIGHT);
        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        val dbManager = DataBaseHelper(context);
        val db = dbManager.createDataBase();

        val num = dbManager.stockmapNextNum()
        numTV.setText(num.toString())

        var intent: Intent = getIntent();

        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")

            println("polygonid ---------$polygonid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            println("==============stock$lat")
            gpslatTV.setText(lat)
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            println("==============stock$log")
            gpslonTV.setText(log)
        }

        if(intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null){
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")
            println("==============stock$lat")
            println("==============stock$log")
            try {
                var geocoder: Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

                    invregionTV.setText(list.get(0).getAddressLine(0));
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        keyId = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata = db.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            invpersonTV.setText(base.INV_PERSON)
            invdtTV.setText(base.INV_DT)
            invtmTV.setText(base.INV_TM)

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

                    invregionTV.setText(list.get(0).getAddressLine(0));
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, invpersonTV.text.toString(), invdtTV.text.toString(), invtmTV.text.toString())

            dbManager.insertbase(base)

        }

        if (intent.getSerializableExtra("stokedata") != null){

            var stockMap = intent.getSerializableExtra("stokedata") as StockMap

            println("-----------------------------------------------------------------------------------------")

            val dbManager: DataBaseHelper = DataBaseHelper(this)

            val db = dbManager.createDataBase()

            val dataList: Array<String> = arrayOf("*");

            frtpcdTV.setText(stockMap.FRTP_CD)
            koftrTV.setText(stockMap.KOFTR_GROUP_CD)
            storunstTV.setText(stockMap.STORUNST_CD)
            frorcdTV.setText(stockMap.FROR_CD)
            dmclscdTV.setText(stockMap.DMCLS_CD)
            agclsTV.setText(stockMap.AGCLS_CD)
            dnstTV.setText(stockMap.DNST_CD)
            heightET.setText(stockMap.HEIGHT)
            map_lableET.setText(stockMap.MAP_LABEL)
//            map_lable2ET.setText(stockMap.MAP_LABEL2)
            etcpcmttET.setText(stockMap.ETC_PCMTT)
            confmodTV.setText(stockMap.CONF_MOD)

        }

        if (intent.getStringExtra("id") != null) {

            deleteBT.visibility = View.VISIBLE

            var stockMap: StockMap = StockMap(null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    null)

            val dataList: Array<String> = arrayOf("*");

            var data = db.query("StockMap", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {

                var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                        data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                        , data.getString(22))
                invtmTV.text =  stockMap.INV_TM
                numTV.text = stockMap.NUM.toString()
                frtpcdTV.text = stockMap.FRTP_CD
                koftrTV.text = stockMap.KOFTR_GROUP_CD
                storunstTV.text = stockMap.STORUNST_CD
                frorcdTV.text = stockMap.FROR_CD
                dmclscdTV.text = stockMap.DMCLS_CD
                agclsTV.text = stockMap.AGCLS_CD
                dnstTV.text = stockMap.DNST_CD
                heightET.setText(stockMap.HEIGHT)
                map_lableET.setText(stockMap.MAP_LABEL)
//                map_lable2ET.setText(stockMap.MAP_LABEL2)
                etcpcmttET.setText(stockMap.ETC_PCMTT)
                gpslatTV.setText(stockMap.GPS_LAT.toString())
                gpslonTV.setText(stockMap.GPS_LAT.toString())
                confmodTV.setText(stockMap.CONF_MOD)

            }

        }

        frtpcdTV.setOnClickListener {
            val intent = Intent(context, DlgStockSelectActivity::class.java)
            intent.putExtra("title", "임상존재코드 선택")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, FRTP_CD);
        }

        koftrTV.setOnClickListener {
            val intent = Intent(context, DlgStockMapActivity::class.java)
            intent.putExtra("title", "수종그룹코드 선택")
            intent.putExtra("table", "Vegetation")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_FRTPCD);
        }

        storunstTV.setOnClickListener {
            val intent = Intent(context, DlgStockSelectActivity::class.java)
            intent.putExtra("title", "입목존재코드 선택")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, STORUNST_CD);
        }

        frorcdTV.setOnClickListener {
            val intent = Intent(context, DlgStockSelectActivity::class.java)
            intent.putExtra("title", "임종코드 선택")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, FROR_CD);
        }


        dmclscdTV.setOnClickListener {
            val intent = Intent(context, DlgStockSelectActivity::class.java)
            intent.putExtra("title", "경급코드 선택")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, DMCLS_CD);
        }

        agclsTV.setOnClickListener {
            val intent = Intent(context, DlgStockSelectActivity::class.java)
            intent.putExtra("title", "영급코드 선택")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, AGCLS_CD);
        }

        dnstTV.setOnClickListener {
            val intent = Intent(context, DlgStockSelectActivity::class.java)
            intent.putExtra("title", "밀도 선택")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, DNST_CD);
        }

        cancleBT.setOnClickListener {
            if (intent.getSerializableExtra("stokedata") == null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            val dbManager: DataBaseHelper = DataBaseHelper(this)

                            val db = dbManager.createDataBase()

                            val dataList: Array<String> = arrayOf("*");

                            val data = db.query("StockMap", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                            if (dataArray != null) {
                                dataArray.clear()
                            }

                            while (data.moveToNext()) {

                                var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                                        data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                                        , data.getString(22))

                                dataArray.add(stockMap)

                            }

                            if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
                                var intent = Intent()
                                intent.putExtra("polygonid", polygonid)
                                setResult(RESULT_OK, intent);
                            }

                            finish()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            } else {
                finish()
            }
        }

        saveBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var stockMap: StockMap = StockMap(null,null,null,null,null,null,null,null,null,null,
                                null,null,null,null,null,null,null,null,null,null,null,null,
                                null)

                        stockMap.GROP_ID = keyId
                        stockMap.PRJ_NAME = ""
                        stockMap.INV_REGION = invregionTV.text.toString()
                        stockMap.INV_PERSON = invpersonTV.text.toString()
                        stockMap.INV_DT = Utils.todayStr()
                        stockMap.INV_TM = Utils.timeStr()
                        stockMap.NUM = numTV.text.toString().toInt()
                        stockMap.FRTP_CD = frtpcdTV.text.toString()
                        stockMap.KOFTR_GROUP_CD = koftrTV.text.toString()
                        if (koftrET.text.toString() != "" && koftrET.text.toString() != null){
                            stockMap.KOFTR_GROUP_CD = koftrET.text.toString()
                        }
                        stockMap.STORUNST_CD = storunstTV.text.toString()
                        stockMap.FROR_CD = frorcdTV.text.toString()
                        stockMap.DMCLS_CD = dmclscdTV.text.toString()
                        stockMap.AGCLS_CD = agclsTV.text.toString()
                        stockMap.DNST_CD = dnstTV.text.toString()
                        stockMap.HEIGHT = heightET.text.toString()
                        stockMap.LDMARK_STNDA_CD = ""
                        stockMap.MAP_LABEL = map_lableET.text.toString()
//                        stockMap.MAP_LABEL2 = map_lable2ET.text.toString()
                        stockMap.ETC_PCMTT = etcpcmttET.text.toString()
                        stockMap.GPS_LAT = gpslatTV.text.toString().toFloat()
                        stockMap.GPS_LAT = gpslonTV.text.toString().toFloat()
                        stockMap.CONF_MOD = "N"

                        if (chkdata) {

                            if(pk != null){

                                val CONF_MOD = confmodTV.text.toString()

                                if(CONF_MOD == "C" || CONF_MOD == "N"){
                                    stockMap.CONF_MOD = "M"
                                }


                                stockMap.MAP_LABEL2 = map_lableET.text.toString()

                                dbManager.updatestockmap(stockMap,pk)
                            }

                        } else {

                            dbManager.insertstockmap(stockMap);

                        }

                        var intent = Intent()
                        intent.putExtra("export",70)
                        setResult(RESULT_OK, intent)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        deleteBT.setOnClickListener {
            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var stockMap: StockMap = StockMap(null,null,null,null,null,null,null,null,null,null,
                                    null,null,null,null,null,null,null,null,null,null,null,null,
                                    null)

                            if (pk != null) {

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db.query("StockMap", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                                                data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                                                , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                                                , data.getString(22))

                                        dataArray.add(stockMap)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager.deletestockmap(pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager.deletestockmap(pk)

                                        var intent = Intent()

                                        intent.putExtra("polygonid", polygonid)

                                        setResult(RESULT_OK, intent);
                                        finish()
                                    }
                                }

                            } else {
                                Toast.makeText(context, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
                            }


                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }

            if(pk == null){

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", { dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data= db.query("StockMap", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                                            data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                                            , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                                            , data.getString(22))
                                }

                                if (chkdata == true) {
                                    Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    intent.putExtra("polygonid", polygonid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }

                            }

                            if (intent.getStringExtra("id") == null) {
                                intent.putExtra("polygonid", polygonid)

                                setResult(RESULT_OK, intent);
                                finish()
                            }


                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }
        }

        addBT.setOnClickListener {
            var stockMap: StockMap = StockMap(null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    null)

            stockMap.GROP_ID = keyId
            stockMap.PRJ_NAME = ""
            stockMap.INV_REGION = invregionTV.text.toString()
            stockMap.INV_PERSON = invpersonTV.text.toString()
            stockMap.INV_DT = Utils.todayStr()
            stockMap.INV_TM = Utils.timeStr()
            stockMap.NUM = numTV.text.toString().toInt()
            stockMap.FRTP_CD = frtpcdTV.text.toString()
            stockMap.KOFTR_GROUP_CD = koftrTV.text.toString()
            if (koftrET.text.toString() != "" && koftrET.text.toString() != null){
                stockMap.KOFTR_GROUP_CD = koftrET.text.toString()
            }
            stockMap.STORUNST_CD = storunstTV.text.toString()
            stockMap.FROR_CD = frorcdTV.text.toString()
            stockMap.DMCLS_CD = dmclscdTV.text.toString()
            stockMap.AGCLS_CD = agclsTV.text.toString()
            stockMap.DNST_CD = dnstTV.text.toString()
            stockMap.HEIGHT = heightET.text.toString()
            stockMap.LDMARK_STNDA_CD = ""
            stockMap.MAP_LABEL = map_lableET.text.toString()
//            stockMap.MAP_LABEL2 = map_lable2ET.text.toString()
            stockMap.ETC_PCMTT = etcpcmttET.text.toString()
            stockMap.GPS_LAT = gpslatTV.text.toString().toFloat()
            stockMap.GPS_LAT = gpslonTV.text.toString().toFloat()
            stockMap.CONF_MOD = "N"

            if (chkdata) {

                if(pk != null){

                    val CONF_MOD = confmodTV.text.toString()

                    if(CONF_MOD == "C" || CONF_MOD == "N"){
                        stockMap.CONF_MOD = "M"
                    }


                    stockMap.MAP_LABEL2 = map_lableET.text.toString()

                    dbManager.updatestockmap(stockMap,pk)
                }

            } else {

                dbManager.insertstockmap(stockMap);

            }

            if(intent.getStringExtra("set") != null){
                var intent = Intent()
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            deleteBT.visibility = View.GONE

            var intent = Intent()
            intent.putExtra("export",70)
            setResult(RESULT_OK, intent)

            clear()
            chkdata = false
            pk = null
        }

    }

    fun clear(){
        frtpcdTV.text = ""
        koftrTV.text = ""
        storunstTV.text = ""
        frorcdTV.text = ""
        dmclscdTV.text = ""
        agclsTV.text = ""
        dnstTV.text = ""
        heightET.setText("")
        map_lableET.setText("")
//        map_lable2ET.setText("")
        etcpcmttET.setText("")
        confmodTV.setText("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var div = ""

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                FRTP_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        frtpcdTV.setText(code)
                        setCode()
                    }
                }

                SET_FRTPCD -> {

                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        if (code == "기타") {
                            koftrET.visibility = View.VISIBLE
                            koftrTV.visibility = View.GONE
                        } else {
                            koftrTV.setText(code)
                        }
                        setCode()
                    }

                    if (data!!.getStringExtra("division") != null){
                        val division = data!!.getStringExtra("division")

                        div = division
                        codeModify(div)
                    }
                }

                STORUNST_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        storunstTV.setText(code)
                        setCode()
                    }
                }

                FROR_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        frorcdTV.setText(code)
                        setCode()
                    }
                }

                DMCLS_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        dmclscdTV.setText(code)
                        setCode()
                    }
                }

                AGCLS_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        agclsTV.setText(code)
                        setCode()
                    }
                }

                DNST_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")

                        dnstTV.setText(code)
                        setCode()
                    }
                }
            }
        }
    }

    fun alert(ListItems: ArrayList<String>, title: String, textView: TextView, type: String) {

        val items = Array<CharSequence>(ListItems.size, { i -> ListItems.get(i) })

        var size = ListItems.size

        var builder: AlertDialog.Builder = AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->


            var selectItem = ListItems.get(i);

            if (selectItem != "취소") {
                textView.text = selectItem
            }
        })

        builder.show();
    }

    override fun onBackPressed() {
        if (intent.getSerializableExtra("stokedata") == null) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dbManager: DataBaseHelper = DataBaseHelper(this)

                        val db = dbManager.createDataBase()

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("StockMap", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                                    data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                                    , data.getString(22))

                            dataArray.add(stockMap)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
                            var intent = Intent()
                            intent.putExtra("polygonid", polygonid)
                            setResult(RESULT_OK, intent);
                        }

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        } else {
            finish()
        }
    }

    fun codeModify(div : String?) {

        var content = ""
        val koftr = koftrTV.text.toString()

        if (koftr != "" && koftr != null) {
            if (div != "" && div != null) {
                content += div
                content += koftr
                val frorcd = frorcdTV.text.toString()

                if (frorcd != "" && frorcd != null) {
                    content += frorcd
                }

                val dmclscd = dmclscdTV.text.toString()

                if (dmclscd != "" && dmclscd != null) {
                    content += dmclscd
                }

                val agcls = agclsTV.text.toString()

                if (agcls != "" && agcls != null) {
                    content += agcls
                }

                val dnst = dnstTV.text.toString()

                if (dnst != "" && dnst != null) {
                    content += dnst
                }

                map_lableET.setText(content)
            }
        }
    }

    fun setCode(){
        val koftr = koftrTV.text.toString()
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();
        val dataList: Array<String> = arrayOf("*");
        var div = ""

        if (koftr != "" && koftr != null) {
            var data = db.query("Vegetation", dataList, "SIGN = '$koftr'", null, null, null, "", null)
            while (data.moveToNext()){

                var model : Vegetation;

                model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

                if (model.CATEGORY == "식재림"){
                    div= "A"
                } else {
                    div = "Y"
                }
            }

            codeModify(div)
        }
    }

}
