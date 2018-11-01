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
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Insect_attribute
import hntecology.ecology.model.Mammal_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_birds.*
import kotlinx.android.synthetic.main.activity_fish.*
import kotlinx.android.synthetic.main.activity_insect.*

class InsectActivity : Activity() , OnLocationUpdatedListener{

    lateinit var context: Context;

    var userName = "";

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var latitude = 0.0f;
    var longitude = 0.0f;

    private var progressDialog: ProgressDialog? = null

    var chkdata: Boolean = false;


    var keyId: String? = null;

    var page:Int? = null

    var dataArray:ArrayList<Insect_attribute> = ArrayList<Insect_attribute>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insect)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        userName = PrefUtils.getStringPreference(context, "name");
        insectusernameET.setText(userName)
        insectinvdtET.setText(Utils.todayStr().toString())
        insecttimeET.setText(Utils.timeStr().toString())

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        initGPS()

        var intent: Intent = getIntent();

        if (intent.getSerializableExtra("id") != null) {
            keyId = intent.getStringExtra("id")

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("insectAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27))

                insectinvregionET.setText(insect_attribute.INV_REGION)

                insectinvdtET.setText(insect_attribute.INV_DT)

                insectusernameET.setText(insect_attribute.INV_PERSON)

                insectweatherET.setText(insect_attribute.WEATHER)
                insectwindET.setText(insect_attribute.WIND)
                insectwinddireET.setText(insect_attribute.WIND_DIRE)
                insecttemperaturET.setText(insect_attribute.TEMPERATUR.toString())
                insectetcET.setText(insect_attribute.ETC)

                insectnumET.setText(insect_attribute.NUM.toString())

                insecttimeET.setText(insect_attribute.INV_TM)

                insectspecnmET.setText(insect_attribute.SPEC_NM)

//                insect_attribute.FAMI_NM view 검토
//                insect_attribute.SCIEN_NM view 검토

                insectindicntET.setText(insect_attribute.INDI_CNT.toString())

                if(insect_attribute.OBS_ST_ETC != null && !insect_attribute.OBS_ST_ETC.equals("")){
                    insectobsstatTV.setText(insect_attribute.OBS_ST_ETC)
                }else {
                    insectobsstatTV.setText(insect_attribute.OBS_STAT)
                }

                if(insect_attribute.USER_TA_ETC != null && !insect_attribute.USER_TA_ETC.equals("")){
                    insectusetarTV.setText(insect_attribute.USER_TA_ETC)
                }else {
                    insectusetarTV.setText(insect_attribute.USE_TAR)
                }

                if(insect_attribute.MJ_ACT_ETC != null && !insect_attribute.MJ_ACT_ETC.equals("")){
                    insectmjactTV.setText(insect_attribute.MJ_ACT_ETC)
                }else {
                    insectmjactTV.setText(insect_attribute.MJ_ACT)
                }

                insectunusnoteET.setText(insect_attribute.UNUS_NOTE)

                dataArray.add(insect_attribute)

            }

            page = dataArray.size

            insectpageTV.setText(page.toString() + " / " + dataArray.size.toString())

        }

        insectleftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("insectAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27))

                dataArray.add(insect_attribute)
            }

            if(page == dataArray.size && page!! > 1) {
                page = page!! - 1
                insectpageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)
            }else if (page!! < dataArray.size && page!! > 1){
                page = page!! - 1
                insectpageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()


                resetPage(page!!)
            }


        }

        insectrightLL.setOnClickListener {
            clear()

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("insectAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27))

                dataArray.add(insect_attribute)
            }

            var insect_attribute: Insect_attribute = Insect_attribute(null,null,null,null,null,null,null,null,null,null
            ,null,null,null,null,null,null,null,null,null,null,null,null,null
            ,null,null,null,null,null)

            insect_attribute.id = keyId + page.toString()

            insect_attribute.GROP_ID = keyId

            insect_attribute.PRJ_NAME = ""

            insect_attribute.INV_REGION = insectinvregionET.text.toString()
            insect_attribute.INV_DT = insectinvdtET.text.toString()
            insect_attribute.INV_PERSON = userName

            insect_attribute.WEATHER = insectweatherET.text.toString()
            insect_attribute.WIND = insectwindET.text.toString()
            insect_attribute.WIND_DIRE = insectwinddireET.text.toString()

            if(insecttemperaturET.text.isNotEmpty()){
                insect_attribute.TEMPERATUR = insecttemperaturET.text.toString().toFloat()
            }

            println("ETC : ${insect_attribute.ETC}")
            insect_attribute.ETC = insectetcET.text.toString()

            if(insectnumET.text.isNotEmpty()){
                insect_attribute.NUM = insectnumET.text.toString().toInt()
            }

            insect_attribute.INV_TM = insectinvdtET.text.toString()
            insect_attribute.SPEC_NM = insectspecnmET.text.toString()

//            insect_attribute.FAMI_NM view 확인
//            insect_attribute.SCIEN_NM view 확인

            if(insectindicntET.text.isNotEmpty()){
                insect_attribute.INDI_CNT = insectindicntET.text.toString().toInt()
            }

            insect_attribute.OBS_STAT = insectobsstatTV.text.toString()

            if(!insectobsstatET.text.equals("") && insectobsstatET.text != null){
                insect_attribute.OBS_ST_ETC = insectobsstatET.text.toString()
            }

            insect_attribute.USE_TAR = insectusetarTV.text.toString()
            if(!insectusetarET.text.equals("") && insectusetarET.text != null){
                insect_attribute.USER_TA_ETC = insectusetarET.text.toString()
            }

            insect_attribute.MJ_ACT = insectmjactTV.text.toString()
            if(!insectmjactET.text.equals("") && insectmjactET.text != null){
                insect_attribute.MJ_ACT_ETC = insectmjactET.text.toString()
            }

//            insect_attribute.INV_MEAN = insectinvmeanET.text.toString() view 확인 필요

            insect_attribute.UNUS_NOTE = insectunusnoteET.text.toString()

            if(page == dataArray.size){
                dbManager.insertinsect_attribute(insect_attribute)
                page = page!! + 1
            }


            val data2 = db.query("insectAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var insect_attribute: Insect_attribute = Insect_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
                        data2.getString(8), data2.getFloat(9), data2.getString(10), data2.getInt(11), data2.getString(12), data2.getString(13), data2.getString(14)
                        , data2.getString(15), data2.getInt(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                        , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27))

                dataArray.add(insect_attribute)

            }

            if(page !! < dataArray.size){
                page = page!! + 1
            }

            insectpageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)



        }

        insectSaveBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->


                        var insect_attribute: Insect_attribute = Insect_attribute(null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null)

                        insect_attribute.id = keyId + page.toString()

                        insect_attribute.GROP_ID = keyId

                        insect_attribute.PRJ_NAME = ""

                        insect_attribute.INV_REGION = insectinvregionET.text.toString()

                        if(insectinvdtET.text == null){
                            insect_attribute.INV_DT = Utils.todayStr()
                        }else {
                            insect_attribute.INV_DT = insectinvdtET.text.toString()
                        }

                        if(insectusernameET.text == null){
                            insect_attribute.INV_PERSON = userName
                        }else {
                            insect_attribute.INV_PERSON = insectusernameET.text.toString()
                        }

                        insect_attribute.WEATHER = insectweatherET.text.toString()
                        insect_attribute.WIND = insectwindET.text.toString()
                        insect_attribute.WIND_DIRE = insectwinddireET.text.toString()

                        if(insecttemperaturET.text.isNotEmpty()){
                            insect_attribute.TEMPERATUR = insecttemperaturET.text.toString().toFloat()
                        }

                        insect_attribute.ETC = insectetcET.text.toString()
                        println("ETC : ${insect_attribute.ETC}")

                        if(insectnumET.text.isNotEmpty()){
                            insect_attribute.NUM = insectnumET.text.toString().toInt()
                        }

                        insect_attribute.INV_TM = Utils.timeStr()

                        insect_attribute.SPEC_NM = insectspecnmET.text.toString()

//            insect_attribute.FAMI_NM view 확인
//            insect_attribute.SCIEN_NM view 확인

                        if(insectindicntET.text.isNotEmpty()){
                            insect_attribute.INDI_CNT = insectindicntET.text.toString().toInt()
                        }

                        insect_attribute.OBS_STAT = insectobsstatTV.text.toString()
                        insect_attribute.OBS_ST_ETC = insectobsstatET.text.toString()

                        insect_attribute.USE_TAR = insectusetarTV.text.toString()
                        insect_attribute.USER_TA_ETC = insectusetarET.text.toString()

                        insect_attribute.MJ_ACT = insectmjactTV.text.toString()
                        insect_attribute.MJ_ACT_ETC = insectmjactET.text.toString()

//            insect_attribute.INV_MEAN = insectinvmeanET.text.toString() view 확인 필요

                        insect_attribute.UNUS_NOTE = insectunusnoteET.text.toString()

                        if(chkdata){

                            val tmppage = page!! - 1
                            val pk = keyId + tmppage.toString()
                            dbManager.updateinsect_attribute(insect_attribute,pk)

                        }else {

                            dbManager.insertinsect_attribute(insect_attribute)

                        }

                        dialog.cancel()

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        insectCancelBT.setOnClickListener {
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



        insectDeleteBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var insect_attribute: Insect_attribute = Insect_attribute(null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null)

                        val tmppage = page!! -1
                        val id = keyId + tmppage.toString()

                        dbManager.deleteinsect_attribute(insect_attribute,id)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        insectobsstatTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("기타");

            alert(listItems, "관할 지역 선택", insectobsstatTV, "obsstat");

        }

        insectusetarTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("인공물");
            listItems.add("식물명");
            listItems.add("흙");
            listItems.add("물");

            alert(listItems, "주요 행위 선택", insectusetarTV, "usetar");
        }

        insectmjactTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("채어잡기");
            listItems.add("쓸어잡기");
            listItems.add("유인");
            listItems.add("비행간섭트랙");
            listItems.add("핏트폴트랩");
            listItems.add("황색수반채집");
            listItems.add("현장확인");
            listItems.add("목견");
            listItems.add("기타");

            alert(listItems, "조사 방법 선택", insectmjactTV, "mjact");
        }


    }

    fun clear(){
        insectinvregionET.setText("")

        insectinvdtET.setText("")

        insectusernameET.setText("")

        insectweatherET.setText("")
        insectwindET.setText("")
        insectwinddireET.setText("")
        insecttemperaturET.setText("")
        insectetcET.setText("")

        insectnumET.setText("")

        insecttimeET.setText("")

        insectspecnmET.setText("")

//                insect_attribute.FAMI_NM view 검토
//                insect_attribute.SCIEN_NM view 검토

        insectindicntET.setText("")

        insectobsstatTV.setText("")

        insectusetarTV.setText("")

        insectmjactTV.setText("")

        insectunusnoteET.setText("")

    }

    fun resetPage(page : Int){

        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data= db.query("insectAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27))

            dataArray.add(insect_attribute)

            insectinvregionET.setText(insect_attribute.INV_REGION)
            if(insectinvregionET.text == null){
                insectinvregionET.setText("")
            }

            insectinvdtET.setText(insect_attribute.INV_DT)
            if(insectinvdtET.text == null){
                insectinvdtET.setText("")
            }

            insectusernameET.setText(insect_attribute.INV_PERSON)
            if(insectusernameET.text == null){
                insectusernameET.setText("")
            }

            insectweatherET.setText(insect_attribute.WEATHER)
            if(insectweatherET.text == null){
                insectweatherET.setText("")
            }

            insectwindET.setText(insect_attribute.WIND)
            if(insectwindET.text == null){
                insectwindET.setText("")
            }

            insectwinddireET.setText(insect_attribute.WIND_DIRE)
            if(insectwinddireET.text == null){
                insectwinddireET.setText("")
            }

            insecttemperaturET.setText(insect_attribute.TEMPERATUR.toString())
            if(insecttemperaturET.text == null){
                insecttemperaturET.setText("")
            }

            insectetcET.setText(insect_attribute.ETC)
            if(insectetcET.text == null){
                insectetcET.setText("")
            }

            insectnumET.setText(insect_attribute.NUM.toString())
            if(insectnumET.text == null){
                insectnumET.setText("")
            }

            insecttimeET.setText(insect_attribute.INV_TM)
            if(insecttimeET.text == null){
                insecttimeET.setText("")
            }

            insectspecnmET.setText(insect_attribute.SPEC_NM)
            if(insectspecnmET.text == null){
                insectspecnmET.setText("")
            }

//                insect_attribute.FAMI_NM view 검토
//                insect_attribute.SCIEN_NM view 검토

            insectindicntET.setText(insect_attribute.INDI_CNT.toString())
            if(insectindicntET.text == null){
                insectindicntET.setText("")
            }

            insectobsstatTV.setText(insect_attribute.OBS_STAT)
            if(insectobsstatTV.text == null){
                insectobsstatTV.setText("")
            }

            if(insect_attribute.OBS_ST_ETC != null && !insect_attribute.OBS_ST_ETC.equals("")){
                insectobsstatTV.setText(insect_attribute.OBS_ST_ETC)
            }else if (insect_attribute.OBS_ST_ETC != null){
                insectobsstatTV.setText("")
            }

            insectusetarTV.setText(insect_attribute.USE_TAR)
            if(insectusetarTV.text == null){
                insectusetarTV.setText("")
            }

            if(insect_attribute.USER_TA_ETC != null && !insect_attribute.USER_TA_ETC.equals("")){
                insectusetarTV.setText(insect_attribute.USER_TA_ETC)
            }

            insectmjactTV.setText(insect_attribute.MJ_ACT)
            if(insectmjactTV.text == null){
                insectmjactTV.setText("")
            }

            if(insect_attribute.MJ_ACT_ETC != null && !insect_attribute.MJ_ACT_ETC.equals("")){
                insectmjactET.setText(insect_attribute.MJ_ACT_ETC)
            }

            insectunusnoteET.setText(insect_attribute.UNUS_NOTE)
            if(insectunusnoteET.text == null){
                insectunusnoteET.setText("")
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

            // 주요행동
            if ("obsstat" == type) {
                if (selectItem == "기타") {
                    insectobsstatET.visibility = View.VISIBLE
                    insectobsstatTV.visibility = View.GONE
                }else {
                    insectobsstatET.visibility = View.GONE
                    insectobsstatTV.visibility = View.VISIBLE
                }
            } else if ("usetar" == type) {
                if (selectItem == "식물명") {
                    insectusetarET.visibility = View.VISIBLE
                    insectusetarTV.visibility = View.GONE
                }else {
                    insectusetarET.visibility = View.GONE
                    insectusetarTV.visibility = View.VISIBLE
                }
            } else if("mjact" == type){
                if (selectItem == "기타") {
                    insectmjactET.visibility = View.VISIBLE
                    insectmjactTV.visibility = View.GONE
                }else {
                    insectmjactET.visibility = View.GONE
                    insectmjactTV.visibility = View.VISIBLE
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


            insectgpslatTV.setText(latitude.toString())
            insectgpslonTV.setText(longitude.toString())


            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }
}
