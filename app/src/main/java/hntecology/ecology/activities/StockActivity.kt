package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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
import hntecology.ecology.model.StockMapSelect
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_stock.*
import java.io.File
import java.io.IOException
import java.util.*

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

    var FRTP_CD_CODE = ""
    var SET_FRTPCD_CODE = ""
    var STORUNST_CD_CODE = ""
    var FROR_CD_CODE = ""
    var DMCLS_CD_CODE = ""
    var AGCLS_CD_CODE = ""
    var DNST_CD_CODE = ""

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var landuse:String? = null

    var dataArray:ArrayList<StockMap> = ArrayList<StockMap>()

    var frtpdata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()
    var frtpcddata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()
    var storunstdata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()
    var frordata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()
    var dmclsdata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()
    var agclsdata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()
    var dnstdata : java.util.ArrayList<StockMapSelect> = ArrayList<StockMapSelect>()

    var prjname = ""

    var geom = ""

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        this.context = this;

        addSelectItem()

        invdtTV.setText(Utils.todayStr())
        invtmTV.setText(Utils.timeStr())

        userName = PrefUtils.getStringPreference(context, "name");

        invpersonTV.setText(userName)

        window.setGravity(Gravity.RIGHT);
        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        dbManager = DataBaseHelper(context);
        db = dbManager!!.createDataBase();

        var today = Utils.todayStr();

        var todays = today.split("-")

        var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

        for (i in 1 until todays.size){
            texttoday += todays.get(i)
        }

        numTV.setText(texttoday + "1")
        var intent: Intent = getIntent();

        invdtTV.setOnClickListener {
            datedlg()
        }
        invtmTV.setOnClickListener {
          timedlg()
        }

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

        if (intent.getStringExtra("geom") != null){
            geom = intent.getStringExtra("geom")
            println("-----------stockgeom $geom")
        }

        if (intent.getIntExtra("landuse",0) != null){
            val num = intent.getIntExtra("landuse",0)
            println("landuse ------ $num")
            landuse = num.toString()
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

//                    invregionTV.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")
        keyId = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

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

//                    invregionTV.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, invpersonTV.text.toString(), invdtTV.text.toString(), invtmTV.text.toString())

            dbManager!!.insertbase(base)

        }

        if (intent.getSerializableExtra("stokedata") != null){

            var stockMap = intent.getSerializableExtra("stokedata") as StockMap

            println("-----------------------------------------------------------------------------------------")

            val dbManager: DataBaseHelper = DataBaseHelper(this)

            val db = dbManager.createDataBase()

            val dataList: Array<String> = arrayOf("*");

//            frtpcdTV.setText(stockMap.FRTP_CD)
//            koftrTV.setText(stockMap.KOFTR_GROUP_CD)
//            storunstTV.setText(stockMap.STORUNST_CD)
//            frorcdTV.setText(stockMap.FROR_CD)
//            dmclscdTV.setText(stockMap.DMCLS_CD)
//            agclsTV.setText(stockMap.AGCLS_CD)
//            dnstTV.setText(stockMap.DNST_CD)

            for (i in 0 until frtpdata.size){
                if (stockMap.FRTP_CD == frtpdata.get(i).code){
                    frtpcdTV.text = frtpdata.get(i).Title
                }
            }
            FROR_CD_CODE = stockMap.FRTP_CD.toString()
//                frtpcdTV.text = stockMap.FRTP_CD

//            for (i in 0 until frtpcddata.size){
//                if (stockMap.KOFTR_GROUP_CD == frtpcddata.get(i).code){
//                    koftrTV.text = frtpcddata.get(i).Title
//                }
//            }
//            SET_FRTPCD_CODE = stockMap.KOFTR_GROUP_CD.toString()
//                koftrTV.text = stockMap.KOFTR_GROUP_CD
            koftrTV.text = stockMap.KOFTR_GROUP_CD

            val codedata = db!!.query("Dropsygroup", dataList, "CODE = '${stockMap.KOFTR_GROUP_CD}'", null, null, null, "", null)
            while (codedata.moveToNext()) {
                val item = StockMapSelect(codedata.getString(1),codedata.getString(2), codedata.getString(3),false)
                koftrTV.setText(item.Title)
            }

            for (i in 0 until storunstdata.size){
                if (stockMap.STORUNST_CD == storunstdata.get(i).code){
                    storunstTV.text = storunstdata.get(i).Title
                }
            }
            STORUNST_CD_CODE = stockMap.STORUNST_CD.toString()
//                storunstTV.text = stockMap.STORUNST_CD

            for (i in 0 until frordata.size){
                if (stockMap.FROR_CD == frordata.get(i).code){
                    frorcdTV.text = frordata.get(i).Title
                }
            }

            FROR_CD_CODE = stockMap.FROR_CD.toString()
//                frorcdTV.text = stockMap.FROR_CD

            for (i in 0 until dmclsdata.size){
                if (stockMap.DMCLS_CD == dmclsdata.get(i).code){
                    dmclscdTV.text = dmclsdata.get(i).Title
                }
            }
            DMCLS_CD_CODE = stockMap.DMCLS_CD.toString()
//                dmclscdTV.text = stockMap.DMCLS_CD

            for (i in 0 until agclsdata.size){
                if (stockMap.AGCLS_CD == agclsdata.get(i).code){
                    agclsTV.text = agclsdata.get(i).Title
                }
            }
            AGCLS_CD_CODE = stockMap.AGCLS_CD.toString()
//                agclsTV.text = stockMap.AGCLS_CD

            for (i in 0 until dnstdata.size){
                if (stockMap.DNST_CD == dnstdata.get(i).code){
                    dnstTV.text = dnstdata.get(i).Title
                }
            }
            DNST_CD_CODE = stockMap.DNST_CD.toString()
//                dnstTV.text = stockMap.DNST_CD

            heightET.setText(stockMap.HEIGHT)
            map_lableET.setText(stockMap.MAP_LABEL)
//            map_lable2ET.setText(stockMap.MAP_LABEL2)
            etcpcmttET.setText(stockMap.ETC_PCMTT)
            confmodTV.setText(stockMap.CONF_MOD)
            landuse = stockMap.LANDUSE

            println("landuse------$landuse")



        }

        if (intent.getStringExtra("id") != null) {

            deleteBT.visibility = View.VISIBLE

            var stockMap: StockMap = StockMap(null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    null,null,null)

            val dataList: Array<String> = arrayOf("*");

            var data = db!!.query("StockMap", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {

                var stockMap = export_attribute(data)
                invtmTV.text =  stockMap.INV_TM
                prjnameET.setText(stockMap.PRJ_NAME)
                if (stockMap.PRJ_NAME != "" || stockMap.PRJ_NAME != null){
//                    prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
                }
                numTV.text = stockMap.NUM.toString()
                for (i in 0 until frtpdata.size){
                    if (stockMap.FRTP_CD == frtpdata.get(i).code){
                        frtpcdTV.text = frtpdata.get(i).Title
                    }
                }

                invregionTV.setText(stockMap.INV_REGION)
                INV_REGION = stockMap.INV_REGION.toString()

                FROR_CD_CODE = stockMap.FRTP_CD.toString()
//                frtpcdTV.text = stockMap.FRTP_CD

                for (i in 0 until frtpcddata.size){
                    if (stockMap.KOFTR_GROUP_CD == frtpcddata.get(i).code){
                        koftrTV.text = frtpcddata.get(i).Title
                    }
                }
//                SET_FRTPCD_CODE = stockMap.KOFTR_GROUP_CD.toString()
                koftrTV.text = stockMap.KOFTR_GROUP_CD

                val codedata = db!!.query("Dropsygroup", dataList, "CODE = '${stockMap.KOFTR_GROUP_CD}'", null, null, null, "", null)
                while (codedata.moveToNext()) {
                    val item = StockMapSelect(codedata.getString(1),codedata.getString(2), codedata.getString(3),false)
                    koftrTV.setText(item.Title)
                }

                for (i in 0 until storunstdata.size){
                    if (stockMap.STORUNST_CD == storunstdata.get(i).code){
                        storunstTV.text = storunstdata.get(i).Title
                    }
                }
                STORUNST_CD_CODE = stockMap.STORUNST_CD.toString()
//                storunstTV.text = stockMap.STORUNST_CD

                for (i in 0 until frordata.size){
                    if (stockMap.FROR_CD == frordata.get(i).code){
                        frorcdTV.text = frordata.get(i).Title
                    }
                }
                FROR_CD_CODE = stockMap.FROR_CD.toString()
//                frorcdTV.text = stockMap.FROR_CD

                for (i in 0 until dmclsdata.size){
                    if (stockMap.DMCLS_CD == dmclsdata.get(i).code){
                        dmclscdTV.text = dmclsdata.get(i).Title
                    }
                }
                DMCLS_CD_CODE = stockMap.DMCLS_CD.toString()
//                dmclscdTV.text = stockMap.DMCLS_CD

                for (i in 0 until agclsdata.size){
                    if (stockMap.AGCLS_CD == agclsdata.get(i).code){
                        agclsTV.text = agclsdata.get(i).Title
                    }
                }
                AGCLS_CD_CODE = stockMap.AGCLS_CD.toString()
//                agclsTV.text = stockMap.AGCLS_CD

                for (i in 0 until dnstdata.size){
                    if (stockMap.DNST_CD == dnstdata.get(i).code){
                        dnstTV.text = dnstdata.get(i).Title
                    }
                }
                DNST_CD_CODE = stockMap.DNST_CD.toString()
//                dnstTV.text = stockMap.DNST_CD

                heightET.setText(stockMap.HEIGHT)
                map_lableET.setText(stockMap.MAP_LABEL)
//                map_lable2ET.setText(stockMap.MAP_LABEL2)
                etcpcmttET.setText(stockMap.ETC_PCMTT)
                gpslatTV.setText(stockMap.GPS_LAT.toString())
                gpslonTV.setText(stockMap.GPS_LON.toString())
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

        resetBT.setOnClickListener {
            koftrLL.visibility = View.GONE
            koftrTV.visibility = View.VISIBLE
            koftrTV.setText("")
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

                                var stockMap = export_attribute(data)

                                dataArray.add(stockMap)

                            }

                            if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
                                var intent = Intent()
                                intent.putExtra("polygonid", polygonid)
                                setResult(RESULT_OK, intent);
                            }

                            data.close()

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
                                null,null,null)

                        stockMap.GROP_ID = keyId

                        val prj = prjnameET.text.toString()
                        if (prj == prjname){
                            stockMap.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            stockMap.PRJ_NAME = prjnameET.text.toString()
                        }

//                        stockMap.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            stockMap.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            stockMap.PRJ_NAME = prjname
//                        }

//                        stockMap.INV_REGION = invregionTV.text.toString()
                        if (invregionTV.length() > 0){
                            stockMap.INV_REGION = invregionTV.text.toString();
                        } else {
                            stockMap.INV_REGION = INV_REGION
                        }
                        stockMap.INV_PERSON = invpersonTV.text.toString()
                        stockMap.INV_DT = invdtTV.text.toString()
                        stockMap.INV_TM = Utils.timeStr()
                        stockMap.NUM = numTV.text.toString().toInt()
                        stockMap.FRTP_CD = FRTP_CD_CODE
//                        stockMap.KOFTR_GROUP_CD = SET_FRTPCD_CODE
                        stockMap.KOFTR_GROUP_CD = koftrTV.text.toString()
                        if (koftrET.text.toString() != "" && koftrET.text.toString() != null){
                            stockMap.KOFTR_GROUP_CD = koftrET.text.toString()
                        }
                        stockMap.STORUNST_CD = STORUNST_CD_CODE
                        stockMap.FROR_CD = FROR_CD_CODE
                        stockMap.DMCLS_CD = DMCLS_CD_CODE
                        stockMap.AGCLS_CD = AGCLS_CD_CODE
                        stockMap.DNST_CD = DNST_CD_CODE
                        stockMap.HEIGHT = heightET.text.toString()
                        stockMap.LDMARK_STNDA_CD = ""
                        stockMap.MAP_LABEL = map_lableET.text.toString()
//                        stockMap.MAP_LABEL2 = map_lable2ET.text.toString()
                        stockMap.ETC_PCMTT = etcpcmttET.text.toString()
                        stockMap.GPS_LAT = gpslatTV.text.toString().toFloat()
                        stockMap.GPS_LON = gpslonTV.text.toString().toFloat()
                        stockMap.CONF_MOD = "N"
                        println("stockmap.LANDUSE----${stockMap.LANDUSE}")
                        stockMap.LANDUSE = landuse
                        stockMap.GEOM = geom
                        if (stockMap.LANDUSE != null || stockMap.LANDUSE != ""){

                        } else {

                        }

                        if (chkdata) {

                            if(pk != null){

                                val CONF_MOD = confmodTV.text.toString()

                                if(CONF_MOD == "C" || CONF_MOD == "N"){
                                    stockMap.CONF_MOD = "M"
                                }


                                stockMap.MAP_LABEL2 = map_lableET.text.toString()

                                dbManager!!.updatestockmap(stockMap,pk)
                                dbManager!!.updatecommonstockmap(stockMap,keyId)
                            }

                        } else {

                            dbManager!!.insertstockmap(stockMap);

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
                                    null,null,null)

                            if (pk != null) {

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("StockMap", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var stockMap = export_attribute(data)
                                        dataArray.add(stockMap)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager!!.deletestockmap(pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager!!.deletestockmap(pk)

                                        var intent = Intent()

                                        intent.putExtra("polygonid", polygonid)

                                        setResult(RESULT_OK, intent);
                                        finish()
                                    }

                                    data.close()
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
                        .setPositiveButton("확인") { dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data= db!!.query("StockMap", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true


                                }

                                if (chkdata == true) {
                                    Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    intent.putExtra("polygonid", polygonid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }

                                data.close()

                            }

                            if (intent.getStringExtra("id") == null) {
                                intent.putExtra("polygonid", polygonid)

                                setResult(RESULT_OK, intent);
                                finish()
                            }


                        }
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }
        }

        addBT.setOnClickListener {
            var stockMap: StockMap = StockMap(null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    null,null,null)

            stockMap.GROP_ID = keyId

            val prj = prjnameET.text.toString()
            if (prj == prjname){
                stockMap.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                stockMap.PRJ_NAME = prjnameET.text.toString()
            }

//            stockMap.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                stockMap.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                stockMap.PRJ_NAME = prjname
//            }

//            stockMap.INV_REGION = invregionTV.text.toString()
            if (invregionTV.length() > 0){
                stockMap.INV_REGION = invregionTV.text.toString();
            } else {
                stockMap.INV_REGION = INV_REGION
            }
            stockMap.INV_PERSON = invpersonTV.text.toString()
            stockMap.INV_DT =  invdtTV.text.toString()
            stockMap.INV_TM = Utils.timeStr()
            stockMap.NUM = numTV.text.toString().toInt()
            stockMap.FRTP_CD = FRTP_CD_CODE
//            stockMap.KOFTR_GROUP_CD = SET_FRTPCD_CODE
            stockMap.KOFTR_GROUP_CD = koftrTV.text.toString()
            if (koftrET.text.toString() != "" && koftrET.text.toString() != null){
                stockMap.KOFTR_GROUP_CD = koftrET.text.toString()
            }
            stockMap.STORUNST_CD = STORUNST_CD_CODE
            stockMap.FROR_CD = FROR_CD_CODE
            stockMap.DMCLS_CD = DMCLS_CD_CODE
            stockMap.AGCLS_CD = AGCLS_CD_CODE
            stockMap.DNST_CD = DNST_CD_CODE
            stockMap.HEIGHT = heightET.text.toString()
            stockMap.LDMARK_STNDA_CD = ""
            stockMap.MAP_LABEL = map_lableET.text.toString()
//            stockMap.MAP_LABEL2 = map_lable2ET.text.toString()
            stockMap.ETC_PCMTT = etcpcmttET.text.toString()
            stockMap.GPS_LAT = gpslatTV.text.toString().toFloat()
            stockMap.GPS_LON = gpslonTV.text.toString().toFloat()
            stockMap.CONF_MOD = "N"
            stockMap.GEOM = lat.toString() + " " + log.toString()
            if (stockMap.LANDUSE != null || stockMap.LANDUSE != ""){

            } else {
                stockMap.LANDUSE = landuse
            }

            if (chkdata) {

                if(pk != null){

                    val CONF_MOD = confmodTV.text.toString()

                    if(CONF_MOD == "C" || CONF_MOD == "N"){
                        stockMap.CONF_MOD = "M"
                    }


                    stockMap.MAP_LABEL2 = map_lableET.text.toString()

                    dbManager!!.updatestockmap(stockMap,pk)
                    dbManager!!.updatecommonstockmap(stockMap,keyId)
                }

            } else {

                dbManager!!.insertstockmap(stockMap);

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

        var num = numTV.text.toString()
        if (num.length > 7){
            var textnum = num.substring(num.length - 2, num.length)
            var splitnum = num.substring(0, num.length - 2)
            var plusnum = textnum.toInt() + 1
            numTV.setText(splitnum.toString() + plusnum.toString())
        } else {
            var textnum = num.substring(num.length - 1, num.length)
            var splitnum = num.substring(0, num.length - 1)
            var plusnum = textnum.toInt() + 1
            numTV.setText(splitnum.toString() + plusnum.toString())
        }

        koftrET.setText("")
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

        FRTP_CD_CODE = ""
        SET_FRTPCD_CODE = ""
        STORUNST_CD_CODE = ""
        FROR_CD_CODE = ""
        DMCLS_CD_CODE = ""
        AGCLS_CD_CODE = ""
        DNST_CD_CODE = ""


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var div = ""

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                FRTP_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        FRTP_CD_CODE = code

//                        frtpcdTV.setText(code)
                        frtpcdTV.setText(title)
//                        setCode()
                    }
                }

                SET_FRTPCD -> {
                    if (data!!.getStringExtra("name") != null) {
                        val name = data!!.getStringExtra("name")
                        koftrTV.setText(name)
                        if (name == "SP(미동정)"){
                            koftrLL.visibility = View.VISIBLE
                            koftrTV.visibility = View.GONE
                        }
                    }

                        if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        SET_FRTPCD_CODE = code

                        if (code == "기타") {
                            koftrLL.visibility = View.VISIBLE
                            koftrTV.visibility = View.GONE
                        } else {
//                            koftrTV.setText(code)
                            koftrTV.setText(title)
                        }
//                        setCode()
                    }

                    if (data!!.getStringExtra("division") != null){
                        val division = data!!.getStringExtra("division")

                        div = division
//                        codeModify(div)
                    }
                }

                STORUNST_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        STORUNST_CD_CODE = code

//                        storunstTV.setText(code)
                        storunstTV.setText(title)
//                        setCode()
                    }
                }

                FROR_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        FROR_CD_CODE = code

//                        frorcdTV.setText(code)
                        frorcdTV.setText(title)
//                        setCode()
                    }
                }

                DMCLS_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        DMCLS_CD_CODE = code

//                        dmclscdTV.setText(code)
                        dmclscdTV.setText(title)
//                        setCode()
                    }
                }

                AGCLS_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        AGCLS_CD_CODE = code

//                        agclsTV.setText(code)
                        agclsTV.setText(title)
//                        setCode()
                    }
                }

                DNST_CD -> {
                    if (data!!.getStringExtra("CODE") != null){
                        val code = data!!.getStringExtra("CODE")
                        val title = data!!.getStringExtra("Title")

                        DNST_CD_CODE = code

//                        dnstTV.setText(code)
                        dnstTV.setText(title)
//                        setCode()
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

                        val dataList: Array<String> = arrayOf("*");

                        val data = db!!.query("StockMap", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var stockMap = export_attribute(data)

                            dataArray.add(stockMap)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
                            var intent = Intent()
                            intent.putExtra("polygonid", polygonid)
                            setResult(RESULT_OK, intent);
                        }

                        data.close()

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
        val koftr = SET_FRTPCD_CODE

        if (koftr != "" && koftr != null) {
            if (div != "" && div != null) {
                content += div
                content += koftr
                val frorcd = FROR_CD_CODE

                if (frorcd != "" && frorcd != null) {
                    content += frorcd
                }

                val dmclscd = DMCLS_CD_CODE

                if (dmclscd != "" && dmclscd != null) {
                    content += dmclscd
                }

                val agcls = AGCLS_CD_CODE

                if (agcls != "" && agcls != null) {
                    content += agcls
                }

                val dnst = DNST_CD_CODE

                if (dnst != "" && dnst != null) {
                    content += dnst
                }

                map_lableET.setText(content)
            }
        }
    }

    fun setCode(){
        val koftr = koftrTV.text.toString()
        val dataList: Array<String> = arrayOf("*");
        var div = ""

        if (SET_FRTPCD_CODE != "" && SET_FRTPCD_CODE != null) {
            var data = db!!.query("Vegetation", dataList, "SIGN = '$SET_FRTPCD_CODE'", null, null, null, "", null)
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
            data.close()
        }
    }

    fun addSelectItem(){
        val selectitem1 = StockMapSelect("입목지"," : 산림의 정의에 따른 구분","1",false)
        val selectitem2 = StockMapSelect("무립목지"," : 미립목지, 제지","2",false)
        val selectitem3 = StockMapSelect("비산림"," : 산림이외의 지역과 산림으로 둘러싸여 있는 초지, 경작지, 하천 과수원, 기타","0",false)

        val selectitem4 = StockMapSelect("침엽수림"," : 침엽수의 수관 점유면적이 75% 이상","1",false)
        val selectitem5 = StockMapSelect("활엽수림"," : 활엽수의 수관 점유면적이 75% 이상","2",false)
        val selectitem6 = StockMapSelect("혼효림"," : 침활이 25% 이상, 75% 미만인 임분","3",false)
        val selectitem7 = StockMapSelect("죽림"," : 대나무림","4",false)
        val selectitem8 = StockMapSelect("무립목지/비산림"," : 산림의 구분이 무립목지/비산림인 경우","0",false)

        val selectitem9 = StockMapSelect("인공림"," : 조림이나 파종 등에 의해 인위적으로 형성된 산림","1",false)
        val selectitem10 = StockMapSelect("천연림"," : 인간의 간섭을 받지 않고 자연적으로 형성된 산림","2",false)
        val selectitem11 = StockMapSelect("무립목지/비산림"," : 산림의 구분이 무립목지/비산림인 경우","0",false)

        val selectitem12 = StockMapSelect("치수"," : 흉고직경 6cm 미만 입목의 수관점유면적 비율이 51% 이상","0",false)
        val selectitem13 = StockMapSelect("소경목"," : 흉고직경 6cm 이상 18cm 미만 입목의 수관점유면적 비율이 51% 이상","1",false)
        val selectitem14 = StockMapSelect("중경목"," : 흉고직경 18cm 이상 30cm 미만 입목의 수관점유면적 비율이 51% 이상","2",false)
        val selectitem15 = StockMapSelect("중경목"," : 흉고직경 30cm 이상 입목의 수관점유면적 비율이 51% 이상","3",false)

        val selectitem16 = StockMapSelect("1영급"," : 1~10 년생의 수관점유 비율이 50% 이상","1",false)
        val selectitem17 = StockMapSelect("2영급"," : 11~20 년생의 수관점유 비율이 50% 이상","2",false)
        val selectitem18 = StockMapSelect("3영급"," : 21~30 년생의 수관점유 비율이 50% 이상","3",false)
        val selectitem19 = StockMapSelect("4영급"," : 31~40 년생의 수관점유 비율이 50% 이상","4",false)
        val selectitem20 = StockMapSelect("5영급"," : 41~50 년생의 수관점유 비율이 50% 이상","5",false)
        val selectitem21 = StockMapSelect("6영급"," : 51~60 년생의 수관점유 비율이 50% 이상(25,000 임상도에서는 51 년생 입목의 수관점유비율이 50% 이상인 임분의 의미로 사용)","6",false)
        val selectitem22 = StockMapSelect("7영급"," : 61~70 년생의 수관점유 비율이 50% 이상","7",false)
        val selectitem23 = StockMapSelect("8영급"," : 71~80 년생의 수관점유 비율이 50% 이상","8",false)
        val selectitem24 = StockMapSelect("9영급"," : 81 년생의 수관점유 비율이 50% 이상","9",false)

        val selectitem25 = StockMapSelect("소"," : 교목의 수관점유 면적이 50% 이하인 임분","A",false)
        val selectitem26 = StockMapSelect("2영급"," : 교목의 수관점유 면적이 51%~70% 이하인 임분","B",false)
        val selectitem27 = StockMapSelect("3영급"," : 교목의 수관점유 면적이 71% 이상인 임분","C",false)


        frtpdata.add(selectitem4)
        frtpdata.add(selectitem5)
        frtpdata.add(selectitem6)
        frtpdata.add(selectitem7)
        frtpdata.add(selectitem8)

        frtpcddata.add(selectitem4)
        frtpcddata.add(selectitem5)
        frtpcddata.add(selectitem6)
        frtpcddata.add(selectitem7)
        frtpcddata.add(selectitem8)

        storunstdata.add(selectitem1)
        storunstdata.add(selectitem2)
        storunstdata.add(selectitem3)

        frordata.add(selectitem9)
        frordata.add(selectitem10)
        frordata.add(selectitem11)

        dmclsdata.add(selectitem12)
        dmclsdata.add(selectitem13)
        dmclsdata.add(selectitem14)
        dmclsdata.add(selectitem15)


        agclsdata.add(selectitem16)
        agclsdata.add(selectitem17)
        agclsdata.add(selectitem18)
        agclsdata.add(selectitem19)
        agclsdata.add(selectitem20)
        agclsdata.add(selectitem21)
        agclsdata.add(selectitem22)
        agclsdata.add(selectitem23)
        agclsdata.add(selectitem24)

        dnstdata.add(selectitem25)
        dnstdata.add(selectitem26)
        dnstdata.add(selectitem27)
    }

    fun timedlg() {
        val cal = Calendar.getInstance()
        val dialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
            var hour_s = hour.toString()
            var min_s = min.toString()
            if (min_s.length!=2){
                min_s = "0"+min_s
            }
            if (hour_s.length!=2){
                hour_s = "0"+hour_s
            }
            val msg = String.format("%s : %s", hour_s, min_s)
            invtmTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }

    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt()-1, days[2].toInt()).show()
    }
    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%d-%d", year, monthOfYear+1, dayOfMonth)
        invdtTV.text = msg
    }


    fun null_attribute(): StockMap {
        var reptilia_attribute: StockMap = StockMap(null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null)
        return reptilia_attribute
    }

    fun export_attribute(data: Cursor): StockMap {
        var stockMap: StockMap = StockMap(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                data.getString(8), data.getString(9), data.getString(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getFloat(20), data.getFloat(21)
                , data.getString(22),data.getString(23), data.getString(24))
        return stockMap
    }



}
