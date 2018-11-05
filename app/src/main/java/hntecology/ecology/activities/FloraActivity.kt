package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import au.com.objectix.jgridshift.Util
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Flora_Attribute
import hntecology.ecology.model.Insect_attribute
import hntecology.ecology.model.Mammal_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_birds.*
import kotlinx.android.synthetic.main.activity_flora.*
import kotlinx.android.synthetic.main.activity_insect.*

class FloraActivity : Activity() , OnLocationUpdatedListener{

    lateinit var context: Context;

    private var progressDialog: ProgressDialog? = null

    var userName = "";

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var latitude = 0.0f;
    var longitude = 0.0f;

    var chkdata: Boolean = false;

    var keyId: String? = null;

    var page:Int? = null

    val SET_FLORA = 1

    var dataArray:ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flora)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

        progressDialog = ProgressDialog(context)

        this.setFinishOnTouchOutside(true);

        florainvdvET.setText(Utils.todayStr())
        florainvtmET.setText(Utils.timeStr())


        userName = PrefUtils.getStringPreference(context, "name");

        florainvperson.setText(userName)

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        initGPS()

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        var intent: Intent = getIntent();

        if (intent.getSerializableExtra("id") != null) {
            keyId = intent.getStringExtra("id")

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                        , data.getFloat(22), data.getFloat(23))

                florainvregionET.setText(flora_Attribute.INV_REGION)
                florainvdvET.setText(flora_Attribute.INV_DT)
                florainvperson.setText(flora_Attribute.INV_PERSON)

                floraweatherTV.setText(flora_Attribute.WEATHER)
                florawindTV.setText(flora_Attribute.WIND)
                florawinddireTV.setText(flora_Attribute.WIND_DIRE)
                floratemperaturTV.setText(flora_Attribute.TEMPERATUR.toString())

                floraetcET.setText(flora_Attribute.ETC)

                florainvtmET.setText(flora_Attribute.INV_TM)

                floranumET.setText(flora_Attribute.NUM.toString())

                floraspecnmET.setText(flora_Attribute.SPEC_NM)
                florafaminmTV.setText(flora_Attribute.FAMI_NM)
                florasciennmTV.setText(flora_Attribute.SCIEN_NM)

                florafloreyynTV.setText(flora_Attribute.FLORE_YN)
                floraplantynTV.setText(flora_Attribute.PLANT_YN)

                if(flora_Attribute.HAB_ETC != null && !flora_Attribute.HAB_ETC.equals("")){
                    florahabstatTV.setText(flora_Attribute.HAB_ETC)
                }else {
                    florahabstatTV.setText(flora_Attribute.HAB_STAT)
                }

                floracolincnt.setText(flora_Attribute.COL_IN_CNT.toString())

                florathrecauET.setText(flora_Attribute.THRE_CAU)

                floragpslatTV.setText(flora_Attribute.GPS_LAT.toString())
                floragpslonTV.setText(flora_Attribute.GPS_LON.toString())

                dataArray.add(flora_Attribute)
            }

            page = dataArray.size

            fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

        }

        floraleftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                        , data.getFloat(22), data.getFloat(23))

                dataArray.add(flora_Attribute)
            }

            if(page == dataArray.size && page!! > 1){
                page = page!! - 1
                fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)

            }else if (page!! < dataArray.size && page!! > 1){
                page = page!! - 1
                fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)

            }

        }

        florarightLL.setOnClickListener {


            clear()

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                        , data.getFloat(22), data.getFloat(23))

                dataArray.add(flora_Attribute)
            }

            var flora_Attribute: Flora_Attribute = Flora_Attribute(null,null,null,null,null,null,null,null,null,null
            ,null,null,null,null,null,null,null,null,null,null,null,null,null,null)

            flora_Attribute.id = keyId + page.toString()
            flora_Attribute.GROP_ID = keyId

            flora_Attribute.PRJ_NAME = ""
            flora_Attribute.INV_REGION = florainvregionET.text.toString()

            flora_Attribute.INV_DT = Utils.todayStr()
            flora_Attribute.INV_PERSON = florainvperson.text.toString()

            flora_Attribute.WEATHER = floraweatherTV.text.toString()
            flora_Attribute.WIND = florawindTV.text.toString()
            flora_Attribute.WIND_DIRE = florawinddireTV.text.toString()

            if(floranumET.text.isNotEmpty()) {
                flora_Attribute.NUM = floranumET.text.toString().toInt()
            }

            if(floratemperaturTV.text.isNotEmpty()){
                flora_Attribute.TEMPERATUR = floratemperaturTV.text.toString().toFloat()
            }

            flora_Attribute.ETC = floraetcET.text.toString()

            flora_Attribute.INV_TM = florainvtmET.text.toString()

            flora_Attribute.SPEC_NM = floraspecnmET.text.toString()
            flora_Attribute.FAMI_NM = florafaminmTV.text.toString()
            flora_Attribute.SCIEN_NM = florasciennmTV.text.toString()

            flora_Attribute.FLORE_YN = florafloreyynTV.text.toString()
            flora_Attribute.PLANT_YN = floraplantynTV.text.toString()


            flora_Attribute.HAB_STAT = florahabstatTV.text.toString()
            flora_Attribute.HAB_ETC = florahabstatET.text.toString()

            if(floracolincnt.text.isNotEmpty()){
                flora_Attribute.COL_IN_CNT = floracolincnt.text.toString().toInt()
            }

            flora_Attribute.THRE_CAU = florathrecauET.text.toString()

            if(floragpslatTV.text.isNotEmpty()){
                flora_Attribute.GPS_LAT = floragpslatTV.text.toString().toFloat()
            }

            if(floragpslatTV.text.isNotEmpty()){
                flora_Attribute.GPS_LON = floragpslonTV.text.toString().toFloat()
            }

            if(page == dataArray.size){
                dbManager.insertflora_attribute(flora_Attribute)
                page = page!! + 1
            }

            if (dataArray != null){
                dataArray.clear()
            }

            val data2= db.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            while (data2.moveToNext()) {

                chkdata = true

                var flora_Attribute: Flora_Attribute = Flora_Attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
                        data2.getString(8), data2.getFloat(9), data2.getString(10), data2.getInt(11), data2.getString(12), data2.getString(13), data2.getString(14)
                        , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getInt(20), data2.getString(21)
                        , data2.getFloat(22), data2.getFloat(23))

                dataArray.add(flora_Attribute)
            }

            if(page!! < dataArray.size){
                page = page!! + 1
            }

            fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)

        }

        btn_biotopSave1.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var flora_Attribute: Flora_Attribute = Flora_Attribute(null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null,null)

                        flora_Attribute.id = keyId + page.toString()

                        flora_Attribute.GROP_ID = keyId

                        flora_Attribute.PRJ_NAME = ""

                        flora_Attribute.INV_REGION = florainvregionET.text.toString()

                        flora_Attribute.INV_DT = Utils.todayStr()

                        if(floranumET.text.isNotEmpty()) {
                            flora_Attribute.NUM = floranumET.text.toString().toInt()
                        }

                        if(florainvperson.text == null || florainvperson.text.equals("")){
                            flora_Attribute.INV_PERSON = userName
                        }else {
                            flora_Attribute.INV_PERSON = florainvperson.text.toString()
                        }

                        flora_Attribute.WEATHER = floraweatherTV.text.toString()
                        flora_Attribute.WIND = florawindTV.text.toString()
                        flora_Attribute.WIND_DIRE = florawinddireTV.text.toString()

                        if(floratemperaturTV.text.isNotEmpty()) {
                            flora_Attribute.TEMPERATUR = floratemperaturTV.text.toString().toFloat()
                        }

                        flora_Attribute.INV_TM = Utils.timeStr()

                        flora_Attribute.ETC = floraetcET.text.toString()

                        if(floranumET.text.isNotEmpty()) {
                            flora_Attribute.NUM = floranumET.text.toString().toInt()
                        }

                        flora_Attribute.SPEC_NM = floraspecnmET.text.toString()
                        flora_Attribute.FAMI_NM = florafaminmTV.text.toString()
                        flora_Attribute.SCIEN_NM = florasciennmTV.text.toString()

                        flora_Attribute.FLORE_YN = florafloreyynTV.text.toString()
                        flora_Attribute.PLANT_YN = floraplantynTV.text.toString()

                        flora_Attribute.HAB_STAT = florahabstatTV.text.toString()
                        flora_Attribute.HAB_ETC = florahabstatET.text.toString()

                        if(floracolincnt.text.isNotEmpty()){
                            flora_Attribute.COL_IN_CNT = floracolincnt.text.toString().toInt()
                        }

                        flora_Attribute.THRE_CAU = florathrecauET.text.toString()

                        if(floragpslatTV.text.isNotEmpty()){
                            flora_Attribute.GPS_LAT = floragpslatTV.text.toString().toFloat()
                        }

                        if(floragpslonTV.text.isNotEmpty()){
                            flora_Attribute.GPS_LON = floragpslonTV.text.toString().toFloat()
                        }

                        if(chkdata){
                            val tmppage = page!! - 1
                            val pk = keyId + tmppage.toString()
                            dbManager.updateflora_attribute(flora_Attribute,pk)
                        }else {
                            dbManager.insertflora_attribute(flora_Attribute)
                        }

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn_biotopCancle1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        btn_biotopDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var flora_Attribute: Flora_Attribute = Flora_Attribute(null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null,null)

                        val tmppage = page!! - 1
                        val pk = keyId + tmppage.toString()

                        dbManager.deleteflora_attribute(flora_Attribute,pk)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        florafloreyynTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("Y");
            listItems.add("N");

            alert(listItems, "개화 여부 선택", florafloreyynTV, "floreyyn");

        }

        floraplantynTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("Y");
            listItems.add("N");

            alert(listItems, "식재 여부 선택", floraplantynTV, "plantyn");

        }

        floraweatherTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("맑음");
            listItems.add("흐림");
            listItems.add("안개");
            listItems.add("비");

            alert(listItems, "날씨", floraweatherTV, "weather");

        }

        florawindTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("강");
            listItems.add("중");
            listItems.add("약");
            listItems.add("무");

            alert(listItems, "바람", florawindTV, "wind");

        }

        florawinddireTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("N");
            listItems.add("NE");
            listItems.add("E");
            listItems.add("SE");
            listItems.add("S");
            listItems.add("SW");
            listItems.add("W");
            listItems.add("NW");

            alert(listItems, "풍향", florawinddireTV, "winddire");

        }

        florahabstatTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("산림");
            listItems.add("공원");
            listItems.add("초지");
            listItems.add("하천");
            listItems.add("저수지");
            listItems.add("습지");
            listItems.add("기타");

            alert(listItems, "생육지 현황", florahabstatTV, "habstat");

        }

        floraspecnmET.setOnClickListener {
            startDlgFlora()
        }



    }

    fun startDlgFlora(){
        val intent = Intent(context, DlgFloraActivity::class.java)
        startActivityForResult(intent, SET_FLORA);
    }

    fun clear(){

        florainvregionET.setText("")
        florainvdvET.setText("")

        floraweatherTV.setText("")
        florawindTV.setText("")
        florawinddireTV.setText("")
        floratemperaturTV.setText("")

        floraetcET.setText("")

        florainvtmET.setText("")

        floraspecnmET.setText("")
        florafaminmTV.setText("")
        florasciennmTV.setText("")

        florafloreyynTV.setText("")
        floraplantynTV.setText("")

        florahabstatTV.setText("")
        florahabstatET.setText("")
        floracolincnt.setText("")

        florathrecauET.setText("")

        floragpslatTV.setText("")
        floragpslonTV.setText("")

    }

    fun resetPage(page : Int){
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data= db.query("floraAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23))

            dataArray.add(flora_Attribute)

            florainvregionET.setText(flora_Attribute.INV_REGION)
            if (florainvregionET.text == null){
                florainvregionET.setText("")
            }

            florainvdvET.setText(flora_Attribute.INV_DT)
            if (florainvdvET.text == null){
                florainvdvET.setText("")
            }

            florainvperson.setText(flora_Attribute.INV_PERSON)
            if (florainvperson.text == null){
                florainvperson.setText("")
            }

            floraweatherTV.setText(flora_Attribute.WEATHER)
            if (floraweatherTV.text == null){
                floraweatherTV.setText("")
            }

            florawindTV.setText(flora_Attribute.WIND)
            if (florawindTV.text == null){
                florawindTV.setText("")
            }

            florawinddireTV.setText(flora_Attribute.WIND_DIRE)
            if (florawinddireTV.text == null){
                florawinddireTV.setText("")
            }

            floratemperaturTV.setText(flora_Attribute.TEMPERATUR.toString())
            if (floratemperaturTV.text == null){
                floratemperaturTV.setText("")
            }

            floraetcET.setText(flora_Attribute.ETC)
            if (floraetcET.text == null){
                floraetcET.setText("")
            }

            floranumET.setText(flora_Attribute.NUM.toString())
            if (floranumET.text == null){
                floranumET.setText("")
            }

            florainvtmET.setText(flora_Attribute.INV_TM)
            if (florainvtmET.text == null){
                florainvtmET.setText("")
            }

            floraspecnmET.setText(flora_Attribute.SPEC_NM)
            if (floraspecnmET.text == null){
                floraspecnmET.setText("")
            }

            florafaminmTV.setText(flora_Attribute.FAMI_NM)
            if (florafaminmTV.text == null){
                florafaminmTV.setText("")
            }

            florasciennmTV.setText(flora_Attribute.SCIEN_NM)
            if (florasciennmTV.text == null){
                florasciennmTV.setText("")
            }

            florafloreyynTV.setText(flora_Attribute.FLORE_YN)
            if (florafloreyynTV.text == null){
                florafloreyynTV.setText("")
            }

            floraplantynTV.setText(flora_Attribute.PLANT_YN)
            if (floraplantynTV.text == null){
                floraplantynTV.setText("")
            }

            florahabstatTV.setText(flora_Attribute.HAB_STAT)
            if (florahabstatTV.text == null){
                florahabstatTV.setText("")
            }

            if (flora_Attribute.HAB_STAT == null){
                florahabstatTV.setText("")
            }

            if(flora_Attribute.HAB_ETC != null && !flora_Attribute.HAB_ETC.equals("")){
                florahabstatTV.setText(flora_Attribute.HAB_ETC)
            }

            if (flora_Attribute.HAB_ETC == null){
                florahabstatTV.setText("")
            }






            floracolincnt.setText(flora_Attribute.COL_IN_CNT.toString())
            if (floracolincnt.text == null){
                floracolincnt.setText("")
            }

            florathrecauET.setText(flora_Attribute.THRE_CAU)
            if (florathrecauET.text == null){
                florathrecauET.setText("")
            }

            floragpslatTV.setText(flora_Attribute.GPS_LAT.toString())
            if (floragpslatTV.text == null){
                floragpslatTV.setText("")
            }

            floragpslonTV.setText(flora_Attribute.GPS_LON.toString())
            if (floragpslonTV.text == null){
                floragpslonTV.setText("")
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

            if ("habstat" == type){
                if (selectItem == "기타") {
                    florahabstatET.visibility = View.VISIBLE
                    florahabstatTV.visibility = View.GONE
                }
            }



        })

        builder.show();
    }

    private fun initGPS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION)
        } else {
            checkGPs()
        }
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            } else if (Manifest.permission.ACCESS_COARSE_LOCATION == perm) {
                checkGPs()
            }
        }
    }

    private fun checkGPs() {
        if (Utils.availableLocationService(context)) {
            startLocation()
        } else {
            gpsCheckAlert.sendEmptyMessage(0)
        }
    }

    internal var gpsCheckAlert: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val mainGpsSearchCount = PrefUtils.getIntPreference(context, "mainGpsSearchCount", 0)

            if (mainGpsSearchCount == 0) {
                latitude = -1.0f
                longitude = -1.0f

                val builder = AlertDialog.Builder(context)
                builder.setTitle("확인")
                builder.setMessage("위치 서비스 이용이 제한되어 있습니다.\n설정에서 위치 서비스 이용을 허용해주세요.")
                builder.setCancelable(true)
                builder.setNegativeButton("취소") { dialog, id ->
                    dialog.cancel()

                    latitude = 37.5203175f
                    longitude = 126.9107831f

                }
                builder.setPositiveButton("설정") { dialog, id ->
                    dialog.cancel()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                val alert = builder.create()
                alert.show()
            }
        }
    }

    private fun startLocation() {
        if (progressDialog != null) {
            // show dialog
            //progressDialog.setMessage("현재 위치 확인 중...");
            progressDialog!!.show()
        }

        val smartLocation = SmartLocation.Builder(context).logging(true).build()
        val locationControl = smartLocation.location(LocationManagerProvider()).oneFix()

        if (SmartLocation.with(context).location(LocationManagerProvider()).state().isGpsAvailable()) {
            val locationParams = LocationParams.Builder().setAccuracy(LocationAccuracy.MEDIUM).build()
            locationControl.config(locationParams)
        } else if (SmartLocation.with(context).location(LocationManagerProvider()).state().isNetworkAvailable()) {
            val locationParams = LocationParams.Builder().setAccuracy(LocationAccuracy.LOW).build()
            locationControl.config(locationParams)
        }
        smartLocation.location().oneFix().start(this)
    }


    override fun onLocationUpdated(p0: Location?) {

        stopLocation()

        if (p0 != null) {

            latitude = p0.getLatitude().toFloat()
            longitude = p0.getLongitude().toFloat()

            var str = latitude.toString() + " / " + longitude.toString()

            floragpslatTV.setText(latitude.toString())
            floragpslonTV.setText(longitude.toString())

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_FLORA -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    floraspecnmET.text = name
                    florafaminmTV.text = family_name
                    florasciennmTV.text = zoological

                };
            }
        }
    }
}
