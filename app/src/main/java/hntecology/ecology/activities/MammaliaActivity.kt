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
import hntecology.ecology.model.Mammal_attribute
import hntecology.ecology.model.Reptilia_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_birds.*
import kotlinx.android.synthetic.main.activity_mammalia.*

class MammaliaActivity : Activity(), OnLocationUpdatedListener {

    lateinit var context: Context;

    var chkdata: Boolean = false;

    var userName = "";

    var keyId: String? = null;

    var page:Int? = null

    var dataArray:ArrayList<Mammal_attribute> = ArrayList<Mammal_attribute>()

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var latitude = 0.0f;
    var longitude = 0.0f;

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mammalia)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        maminvdtTV.text = Utils.todayStr()

        userName = PrefUtils.getStringPreference(context, "name");

        maminvpersonTV.setText(userName)

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        initGPS()

        var intent: Intent = getIntent();

        if (intent.getSerializableExtra("id") != null) {
            keyId = intent.getStringExtra("id")

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                        , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26))

                maminvregionET.setText(mammal_attribute.INV_REGION)
                maminvdtTV.setText(mammal_attribute.INV_DT)

                mamweatherET.setText(mammal_attribute.WEATHER)
                mamwindET.setText(mammal_attribute.WIND)
                mamwinddireET.setText(mammal_attribute.WIND_DIRE)

                mamtemperatureET.setText(mammal_attribute.TEMPERATUR.toString())
                mametcET.setText(mammal_attribute.ETC)

                mamspecnmET.setText(mammal_attribute.SPEC_NM)

//                faminmTV.setText      view 검토 후
//                scien_nm.setText      view 검토 후

                mamobstyET.setText(mammal_attribute.OBS_TY)

                mamindicntET.setText(mammal_attribute.INDI_CNT.toString())

                mamobptcharET.setText(mammal_attribute.OB_PT_CHAR)

                mamunusnoteET.setText(mammal_attribute.UNUS_NOTE)

                mamunspecET.setText(mammal_attribute.UN_SPEC)
                mamunspecreET.setText(mammal_attribute.UN_SPEC_RE)

                mamtreasyET.setText(mammal_attribute.TR_EASY)
                mamtreasyreET.setText(mammal_attribute.TR_EASY_RE)

                dataArray.add(mammal_attribute)
            }

            page = dataArray.size

            mampageTV.setText(page.toString() + " / " + dataArray.size.toString())

        }

        mamleftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                        , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26))

                dataArray.add(mammal_attribute)
            }

            if(page == dataArray.size && page!! > 1){
                page = page!! - 1
                mampageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)

            }else if (page!! < dataArray.size && page!! > 1){
                page = page!! - 1
                mampageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)

            }
        }

        mamrightLL.setOnClickListener {
            clear()

            val dataList: Array<String> = arrayOf("*");

            val data= db.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                        , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26))

                dataArray.add(mammal_attribute)
            }

            var mammal_attribute:Mammal_attribute = Mammal_attribute(null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null)

            mammal_attribute.id = keyId + page.toString()

            mammal_attribute.GROP_ID = keyId

            mammal_attribute.PRJ_NAME = ""

            mammal_attribute.INV_REGION = maminvregionET.text.toString()
            mammal_attribute.INV_DT = maminvdtTV.text.toString()
            mammal_attribute.INV_PERSON = maminvpersonTV.text.toString()

            mammal_attribute.WEATHER = mamweatherET.text.toString()
            mammal_attribute.WIND = mamwindET.text.toString()
            mammal_attribute.WIND_DIRE = mamwinddireET.text.toString()

            if(mamtemperatureET.text.isNotEmpty()) {
                mammal_attribute.TEMPERATUR = mamtemperatureET.text.toString().toFloat()
            }

            mammal_attribute.ETC = mametcET.text.toString()

            mammal_attribute.NUM = 0

            mammal_attribute.INV_TM = Utils.timeStr()

            mammal_attribute.SPEC_NM = mamspecnmET.text.toString()

//            mammal_attribute.FAMI_NM 화면에 view 검토
//            mammal_attribute.SCIEN_NM 화면에 view 검토

            mammal_attribute.OBS_TY = mamobstyET.text.toString()
//            mammal_attribute.OBS_TY_ETC 수동입력 view 검토

            if(mamindicntET.text.isNotEmpty()) {
                mammal_attribute.INDI_CNT = mamindicntET.text.toString().toInt()
            }

            mammal_attribute.OB_PT_CHAR = mamobptcharET.text.toString()
            mammal_attribute.UNUS_NOTE = mamunusnoteET.text.toString()

            if(mamgpslonTV.text.toString() != "" && mamgpslatTV.text.toString() != ""){
                mammal_attribute.GPS_LAT = mamgpslatTV.text.toString().toFloat()
                mammal_attribute.GPS_LON = mamgpslonTV.text.toString().toFloat()
            }

            mammal_attribute.UN_SPEC = mamunspecET.text.toString()
            mammal_attribute.UN_SPEC_RE = mamunspecreET.text.toString()

            mammal_attribute.TR_EASY = mamtreasyET.text.toString()
            mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()

            if(page == dataArray.size){
                dbManager.insertmammal_attribute(mammal_attribute)
                page = page!! + 1
            }

            val data2= db.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var mammal_attribute: Mammal_attribute = Mammal_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
                        data2.getString(8), data2.getFloat(9), data2.getString(10), data2.getInt(11), data2.getString(12), data2.getString(13), data2.getString(14)
                        , data2.getString(15), data2.getString(16), data2.getString(17), data2.getInt(18), data2.getString(19), data2.getString(20), data2.getFloat(21)
                        , data2.getFloat(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getString(26))

                dataArray.add(mammal_attribute)

            }

            if(page!! < dataArray.size){
                page = page!! + 1
            }

            mampageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)


        }

        btn_biotopSave1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        var mammal_attribute:Mammal_attribute = Mammal_attribute(null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null)

                        val id = keyId + page.toString()

                        mammal_attribute.id = keyId + page.toString()

                        mammal_attribute.GROP_ID = keyId

                        mammal_attribute.PRJ_NAME = ""

                        mammal_attribute.INV_REGION = maminvregionET.text.toString()
                        mammal_attribute.INV_DT = maminvdtTV.text.toString()
                        mammal_attribute.INV_PERSON = maminvpersonTV.text.toString()

                        mammal_attribute.WEATHER = mamweatherET.text.toString()
                        mammal_attribute.WIND = mamwindET.text.toString()
                        mammal_attribute.WIND_DIRE = mamwinddireET.text.toString()

                        if(mamtemperatureET.text.isNotEmpty()) {
                            mammal_attribute.TEMPERATUR = mamtemperatureET.text.toString().toFloat()
                        }

                        mammal_attribute.ETC = mametcET.text.toString()

                        mammal_attribute.NUM = 0

                        mammal_attribute.INV_TM = Utils.timeStr()

                        mammal_attribute.SPEC_NM = mamspecnmET.text.toString()

//            mammal_attribute.FAMI_NM 화면에 view 검토
//            mammal_attribute.SCIEN_NM 화면에 view 검토

                        mammal_attribute.OBS_TY = mamobstyET.text.toString()
//            mammal_attribute.OBS_TY_ETC 수동입력 view 검토

                        if(mamindicntET.text.isNotEmpty()) {
                            mammal_attribute.INDI_CNT = mamindicntET.text.toString().toInt()
                        }

                        mammal_attribute.OB_PT_CHAR = mamobptcharET.text.toString()
                        mammal_attribute.UNUS_NOTE = mamunusnoteET.text.toString()

                        if(mamgpslonTV.text.toString() != "" && mamgpslatTV.text.toString() != ""){
                            mammal_attribute.GPS_LAT = mamgpslatTV.text.toString().toFloat()
                            mammal_attribute.GPS_LON = mamgpslonTV.text.toString().toFloat()
                        }

                        mammal_attribute.UN_SPEC = mamunspecET.text.toString()
                        mammal_attribute.UN_SPEC_RE = mamunspecreET.text.toString()

                        mammal_attribute.TR_EASY = mamtreasyET.text.toString()
                        mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()

                        if(chkdata){

                            val tmppage = page!! - 1
                            val pk = keyId + tmppage.toString()
                            dbManager.updatemammal_attribute(mammal_attribute,pk)

                        }else {

                            dbManager.insertmammal_attribute(mammal_attribute)

                        }

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

                        var mammal_attribute:Mammal_attribute = Mammal_attribute(null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null)

                        val tmppage = page!! - 1 !!
                        val id = keyId + tmppage.toString()

                        dbManager.deletemammal_attribute(mammal_attribute,id)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        btn_biotopCancle1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        }



    fun clear(){
        maminvregionET.setText("")
        maminvdtTV.setText("")

        mamweatherET.setText("")
        mamwindET.setText("")
        mamwinddireET.setText("")

        mamtemperatureET.setText("")
        mametcET.setText("")

        mamspecnmET.setText("")

//                faminmTV.setText      view 검토 후
//                scien_nm.setText      view 검토 후

        mamobstyET.setText("")

        mamindicntET.setText("")

        mamobptcharET.setText("")

        mamunusnoteET.setText("")

        mamunspecET.setText("")
        mamunspecreET.setText("")

        mamtreasyET.setText("")
        mamtreasyreET.setText("")
    }


    fun resetPage(page : Int){
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data= db.query("mammalAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                    , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26))

            dataArray.add(mammal_attribute)

            maminvregionET.setText(mammal_attribute.INV_REGION)
            if(maminvregionET.text == null){
                maminvregionET.setText("")
            }

            maminvdtTV.setText(mammal_attribute.INV_DT)
            if(maminvdtTV.text == null){
                maminvdtTV.setText("")
            }

            mamweatherET.setText(mammal_attribute.WEATHER)
            if(mamweatherET.text == null){
                mamweatherET.setText("")
            }

            mamwindET.setText(mammal_attribute.WIND)
            if(mamwindET.text == null){
                mamwindET.setText("")
            }

            mamwinddireET.setText(mammal_attribute.WIND_DIRE)
            if(mamwinddireET.text == null){
                mamwinddireET.setText("")
            }

            mamtemperatureET.setText(mammal_attribute.TEMPERATUR.toString())
            if(mamtemperatureET.text == null){
                mamtemperatureET.setText("")
            }

            mametcET.setText(mammal_attribute.ETC)
            if(mametcET.text == null){
                mametcET.setText("")
            }

            mamspecnmET.setText(mammal_attribute.SPEC_NM)
            if(mamspecnmET.text == null){
                mamspecnmET.setText("")
            }

//                faminmTV.setText      view 검토 후
//                scien_nm.setText      view 검토 후

            mamobstyET.setText(mammal_attribute.OBS_TY)
            if(mamobstyET.text == null){
                mamobstyET.setText("")
            }

            mamindicntET.setText(mammal_attribute.INDI_CNT.toString())
            if(mamindicntET.text == null){
                mamindicntET.setText("")
            }

            mamobptcharET.setText(mammal_attribute.OB_PT_CHAR)
            if(mamobptcharET.text == null){
                mamobptcharET.setText("")
            }

            mamunusnoteET.setText(mammal_attribute.UNUS_NOTE)
            if(mamunusnoteET.text == null){
                mamunusnoteET.setText("")
            }

            mamgpslatTV.setText(mammal_attribute.GPS_LAT.toString())
            if(mamgpslatTV.text == null){
                mamgpslatTV.setText("")
            }

            mamgpslonTV.setText(mammal_attribute.GPS_LON.toString())
            if(mamgpslonTV.text == null){
                mamgpslonTV.setText("")
            }

            mamunspecET.setText(mammal_attribute.UN_SPEC)
            if(mamunspecET.text == null){
                mamunspecET.setText("")
            }

            mamunspecreET.setText(mammal_attribute.UN_SPEC_RE)
            if(mamunspecreET.text == null){
                mamunspecreET.setText("")
            }

            mamtreasyET.setText(mammal_attribute.TR_EASY)
            if(mamtreasyET.text == null){
                mamtreasyET.setText("")
            }

            mamtreasyreET.setText(mammal_attribute.TR_EASY_RE)
            if(mamtreasyreET.text == null){
                mamtreasyreET.setText("")
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
            if ("mjAct" == type) {
                if (selectItem == "번식 및 번식행동") {
                    mjActPrLL.visibility = View.VISIBLE
                } else {
                    mjActPrLL.visibility = View.GONE
                    mjActPrET.setText("");
                }
            } else if ("useTar" == type) {
                if (selectItem == "수종명 기록") {
                    useTarSpLL.visibility = View.VISIBLE
                } else {
                    useTarSpLL.visibility = View.GONE
                    useTarSpET.setText("");
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


            mamgpslatTV.setText(latitude.toString())
            mamgpslonTV.setText(longitude.toString())

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }


}
