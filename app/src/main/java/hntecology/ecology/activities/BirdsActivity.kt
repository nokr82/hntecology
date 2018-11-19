package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.OpenAlertDialog
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_birds.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class BirdsActivity : Activity(), OnLocationUpdatedListener {

    lateinit var openAlertDialog: OpenAlertDialog;

    private lateinit var googleMap: GoogleMap

    lateinit var context: Context;

    val SET_DATA1 = 1;
    val SET_DATA2 = 2;
    val SET_DATA3 = 3;
    val SET_BIRDS = 4;
    val SET_DATA4 = 5;

    var userName = "";

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var chkdata: Boolean = false;

    var latitude = 0.0f;
    var longitude = 0.0f;

    var lat:String = ""
    var log:String = ""

    private var progressDialog: ProgressDialog? = null

    var type = "write";

    var keyId: String? = null;

    var pk : String? = null

    var page:Int? = null

    var dataArray:ArrayList<Birds_attribute> = ArrayList<Birds_attribute>()

    var basechkdata = false


    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101

    var cameraPath:String? = null

    private var addPicturesLL: LinearLayout? = null
    private val imgSeq = 0

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birds)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        var today = Utils.todayStr();
        var time = Utils.timeStr();

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        invDtTV.text = today;
        timeTV.text = time;

        userName = PrefUtils.getStringPreference(context, "name");
        invPersonTV.text = userName;

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        var intent: Intent = getIntent();

        val num = dbManager.birdsNextNum();
        numTV.text = num.toString()

        if(intent.getStringExtra("latitude")!= null){
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            gpslatTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            gpslonTV.setText(log)
        }

        keyId = intent.getStringExtra("GROP_ID")

        if(intent.getStringExtra("id") != null){
            pk = intent.getStringExtra("id")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata= db.query("Base", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while(basedata.moveToNext()){

            basechkdata = true

            var base : Base = Base(basedata.getInt(0) , basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5) , basedata.getString(6),basedata.getString(7))

            println("keyid ==== $keyId")
            println("base ==== ${base.GROP_ID}")

            invPersonTV.setText(base.INV_PERSON)
            invDtTV.setText(base.INV_DT)
            timeTV.setText(base.INV_TM)

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,invPersonTV.text.toString(),invDtTV.text.toString(),timeTV.text.toString())

            dbManager.insertbase(base)

        }

                if (intent.getSerializableExtra("GROP_ID") != null) {
                    keyId = intent.getStringExtra("GROP_ID")

                    val dataList: Array<String> = arrayOf("*");

                    val data = db.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)


                    if (dataArray != null) {
                        dataArray.clear()
                    }

                    while (data.moveToNext()) {

                        chkdata = true

                        var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                                , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25), data.getString(26))

                        dataArray.add(birds_attribute)

                    }

                    println("dataArrayList ${dataArray.size}")
                }

        if (intent.getStringExtra("id") != null) {

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("birdsAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {

                chkdata = true

                var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25), data.getString(26))


                                invPersonTV.setText(birds_attribute.INV_PERSON)

                invRegionET.setText(birds_attribute.INV_REGION)

                btn1.setText(birds_attribute.WEATHER)       //날씨
                btn2.setText(birds_attribute.WIND)          //바람
                btn3.setText(birds_attribute.WIND_DIRE)     //풍향

                temperatureET.setText(birds_attribute.TEMPERATUR.toString())       //기온
                etcET.setText(birds_attribute.ETC)

                birdsTV.setText(birds_attribute.SPEC_NM)
                familyNameTV.setText(birds_attribute.FAMI_NM)
                zoologicalTV.setText(birds_attribute.SCIEN_NM)
                indicntET.setText(birds_attribute.INDI_CNT.toString())

                obsstatTV.setText(birds_attribute.OBS_STAT)
                useTarTV.setText(birds_attribute.USE_TAR)
                useTarSpET.setText(birds_attribute.USE_TAR_SP)
                if(birds_attribute.USE_TAR_SP == null || birds_attribute.USE_TAR_SP.equals("")){
                    useTarSpET.setText("")
                    useTarSpLL.visibility = View.GONE
                }

                if(birds_attribute.USE_TAR_SP != null && !birds_attribute.USE_TAR_SP.equals("")){
                    useTarSpLL.visibility = View.VISIBLE
                }

                useLayerTV.setText(birds_attribute.USE_LAYER)
                mjActTV.setText(birds_attribute.MJ_ACT)
                mjActPrET.setText(birds_attribute.MJ_ACT_PR)
                if(birds_attribute.MJ_ACT_PR == null || birds_attribute.MJ_ACT_PR.equals("")){
                    mjActPrET.setText("")
                    mjActPrLL.visibility = View.GONE
                }

                if(birds_attribute.MJ_ACT_PR != null && !birds_attribute.MJ_ACT_PR.equals("")){
                    mjActPrLL.visibility = View.VISIBLE
                }

                if(birds_attribute.TEMP_YN.equals("N")){
                    dbManager.deletebirds_attribute(birds_attribute,pk)
                }

                if(birds_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(birds_attribute)
                }

            }

        }

//        if (intent.getSerializableExtra("GROP_ID") != null) {
//            keyId = intent.getStringExtra("GROP_ID")
//
//            val dataList: Array<String> = arrayOf("*");
//
//            println("keyId : $keyId")
//
//
//
//            val data= db.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)
//
//            while (data.moveToNext()) {
//                chkdata = true
//                var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
//                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
//                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
//                        , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25) , data.getString(26))
//
//                val id = birds_attribute.id
//
//                invPersonTV.setText(birds_attribute.INV_PERSON)
//
//                invRegionET.setText(birds_attribute.INV_REGION)
//
//                btn1.setText(birds_attribute.WEATHER)       //날씨
//                btn2.setText(birds_attribute.WIND)          //바람
//                btn3.setText(birds_attribute.WIND_DIRE)     //풍향
//
//                temperatureET.setText(birds_attribute.TEMPERATUR.toString())       //기온
//                etcET.setText(birds_attribute.ETC)
//
//                birdsTV.setText(birds_attribute.SPEC_NM)
//                familyNameTV.setText(birds_attribute.FAMI_NM)
//                zoologicalTV.setText(birds_attribute.SCIEN_NM)
//                indicntET.setText(birds_attribute.INDI_CNT.toString())
//
//                obsstatTV.setText(birds_attribute.OBS_STAT)
//                useTarTV.setText(birds_attribute.USE_TAR)
//                useTarSpET.setText(birds_attribute.USE_TAR_SP)
//                if(birds_attribute.USE_TAR_SP == null || birds_attribute.USE_TAR_SP.equals("")){
//                    useTarSpET.setText("")
//                    useTarSpLL.visibility = View.GONE
//                }
//
//                if(birds_attribute.USE_TAR_SP != null && !birds_attribute.USE_TAR_SP.equals("")){
//                    useTarSpLL.visibility = View.VISIBLE
//                }
//
//                useLayerTV.setText(birds_attribute.USE_LAYER)
//                mjActTV.setText(birds_attribute.MJ_ACT)
//                mjActPrET.setText(birds_attribute.MJ_ACT_PR)
//                if(birds_attribute.MJ_ACT_PR == null || birds_attribute.MJ_ACT_PR.equals("")){
//                    mjActPrET.setText("")
//                    mjActPrLL.visibility = View.GONE
//                }
//
//                if(birds_attribute.MJ_ACT_PR != null && !birds_attribute.MJ_ACT_PR.equals("")){
//                    mjActPrLL.visibility = View.VISIBLE
//                }
//
//                if(birds_attribute.TEMP_YN.equals("N")){
//                    dbManager.deletebirds_attribute(birds_attribute,id)
//                }
//
//                if(birds_attribute.TEMP_YN.equals("Y")){
//                    dataArray.add(birds_attribute)
//                }
//
//
//            }
//
//            page = dataArray.size
//
//            birdspageTV.text = page.toString() + " / " + dataArray.size
//
//        }
//
//        birdsleftLL.setOnClickListener {
//
//            val dataList: Array<String> = arrayOf("*");
//
//            val data= db.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)
//
//
//            if (dataArray != null){
//                dataArray.clear()
//            }
//
//            while (data.moveToNext()) {
//
//                chkdata = true
//
//                var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
//                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
//                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
//                        , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25),data.getString(26))
//
//                dataArray.add(birds_attribute)
//
//            }
//
//            if(page == dataArray.size && page!! > 1 ){
//                page = page!! - 1
//                birdspageTV.text = page.toString() + " / " + dataArray.size
//
//                clear()
//
//                resetPage(page!!)
//
//            }else if (page!! < dataArray.size && page!! > 1){
//                page = page!! - 1
//                birdspageTV.text = page.toString() + " / " + dataArray.size
//
//                clear()
//
//                resetPage(page!!)
//            }
//
//        }
//
//        birdsrightLL.setOnClickListener {
//            clear()
//
//            val dataList: Array<String> = arrayOf("*");
//
//            val data= db.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)
//
//            if (dataArray != null){
//                dataArray.clear()
//            }
//
//            while (data.moveToNext()) {
//
//                chkdata = true
//
//                var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
//                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
//                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
//                        , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25), data.getString(26))
//
//                dataArray.add(birds_attribute)
//
//            }
//
//            var birds_attribute: Birds_attribute = Birds_attribute(null,null,null,null,null,null,null,null,null,null,
//                    null,null,null,null,null,null,null,null,null,null,null,null,
//                    null,null,null,null,null)
//
//            birds_attribute.id = keyId + page.toString()
//
//            birds_attribute.GROP_ID = keyId
//
//            birds_attribute.INV_REGION = invRegionET.text.toString()
//            birds_attribute.INV_DT = Utils.todayStr()
//
//            birds_attribute.INV_PERSON = invPersonTV.text.toString()
//            birds_attribute.WEATHER = weatherTV.text.toString()
//
//            birds_attribute.WEATHER = btn1.text.toString()
//            birds_attribute.WIND = btn2.text.toString()
//            birds_attribute.WIND_DIRE = btn3.text.toString()
//
//            if(temperatureET.text.isNotEmpty()){
//                Utils.getString(temperatureET).toFloat();
//            }
//
//            birds_attribute.ETC = etcET.text.toString()
//
//            birds_attribute.NUM = numTV.text.toString().toInt()
//
//            birds_attribute.INV_TM = Utils.timeStr()
//
//            birds_attribute.SPEC_NM = birdsTV.text.toString()
//
//            birds_attribute.FAMI_NM = familyNameTV.text.toString()
//            birds_attribute.SCIEN_NM = zoologicalTV.text.toString()
//
//            if (indicntET.text.isNotEmpty()) {
//                birds_attribute.INDI_CNT = indicntET.text.toString().toInt()
//            }
//
//            birds_attribute.OBS_STAT = obsstatTV.text.toString()
//            birds_attribute.USE_TAR = useTarTV.text.toString()
//            birds_attribute.USE_TAR_SP = useTarSpET.text.toString()
//            birds_attribute.USE_LAYER = useLayerTV.text.toString()
//
//            birds_attribute.MJ_ACT = mjActTV.text.toString()
//            birds_attribute.MJ_ACT_PR = mjActPrET.text.toString()
//
//            birds_attribute.GPS_LAT = 0F
//            birds_attribute.GPS_LON = 0F
//
//            birds_attribute.TEMP_YN = "N"
//
//            if(page == dataArray.size){
//
//                dbManager.insertbirds_attribute(birds_attribute)
//                page = page!! + 1
//
//            }
//
//
//            val data2= db.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)
//
//            if (dataArray != null){
//                dataArray.clear()
//            }
//
//
//            while (data2.moveToNext()) {
//
//                chkdata = true
//
//
//                var birds_attribute: Birds_attribute = Birds_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
//                        data2.getString(8), data2.getFloat(9), data2.getString(10), data2.getInt(11), data2.getString(12), data2.getString(13), data2.getString(14)
//                        , data2.getString(15), data2.getInt(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
//                        , data2.getString(22), data2.getString(23), data2.getFloat(24), data2.getFloat(25), data2.getString(26))
//
//                println("data2 ======= $data2")
//
//                dataArray.add(birds_attribute)
//
//            }
//
//            if(page!! < dataArray.size){
//                page = page!! + 1
//            }
//
//
//            birdspageTV.setText(page.toString() + " / " + dataArray.size)
//
//            resetPage(page!!)
//
//        }


        cancelBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener {

                        dialog, id -> dialog.cancel()
                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        birdssaveBtn.setOnClickListener{

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var birds_attribute: Birds_attribute = Birds_attribute(null,null,null,null,null,null,null,null,null,null,
                                null,null,null,null,null,null,null,null,null,null,null,null,
                                null,null,null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

                        birds_attribute.GROP_ID = keyId

                        birds_attribute.PRJ_NAME = ""

                        birds_attribute.INV_REGION = invRegionET.text.toString()


                        birds_attribute.INV_DT = Utils.todayStr()


                        if(invPersonTV.text == null){
                            birds_attribute.INV_PERSON = userName
                        }else {
                            birds_attribute.INV_PERSON = invPersonTV.text.toString()
                        }

                        birds_attribute.WEATHER = btn1.text.toString()

                        birds_attribute.WIND = btn2.text.toString()

                        birds_attribute.WIND_DIRE = btn3.text.toString()

                        if(temperatureET.text.isNotEmpty()){
                            Utils.getString(temperatureET).toFloat();
                        }

                        birds_attribute.ETC = etcET.text.toString()

                        birds_attribute.NUM = numTV.text.toString().toInt()

                        birds_attribute.INV_TM = Utils.timeStr()

                        birds_attribute.SPEC_NM = birdsTV.text.toString()

                        birds_attribute.FAMI_NM = familyNameTV.text.toString()

                        birds_attribute.SCIEN_NM = zoologicalTV.text.toString()

                        if (indicntET.text.isNotEmpty()) {
                            birds_attribute.INDI_CNT = indicntET.text.toString().toInt()
                        }

                        birds_attribute.OBS_STAT = obsstatTV.text.toString()

                        birds_attribute.USE_TAR = useTarTV.text.toString()
                        birds_attribute.USE_TAR_SP = useTarSpET.text.toString()

                        birds_attribute.USE_LAYER = useLayerTV.text.toString()

                        birds_attribute.MJ_ACT = mjActTV.text.toString()
                        birds_attribute.MJ_ACT_PR = mjActPrET.text.toString()


                        if (gpslatTV.text.isNotEmpty()) {
                            birds_attribute.GPS_LAT = lat.toFloat()
                        }

                        if (gpslonTV.text.isNotEmpty()) {
                            birds_attribute.GPS_LON = log.toFloat()
                        }




                        if(chkdata){

                            birds_attribute.TEMP_YN = "Y"

                            if(pk != null){
                                dbManager.updatebirds_attribute(birds_attribute,pk)
                            }

                        }else {

                            birds_attribute.TEMP_YN = "Y"

                            dbManager.insertbirds_attribute(birds_attribute)

                        }

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()



        }

        delBtn.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var birds_attribute: Birds_attribute = Birds_attribute(null,null,null,null,null,null,null,null,null,null,
                                null,null,null,null,null,null,null,null,null,null,null,null,
                                null,null,null,null,null)

                        if(pk != null) {
                            dbManager.deletebirds_attribute(birds_attribute,pk)
                            finish()
                        }else {
                            Toast.makeText(context, "잘못된 접근입니다..", Toast.LENGTH_SHORT).show()
                        }



                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn1.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "날씨")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 1);

            startActivityForResult(intent, SET_DATA1);

        }

        btn2.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "바람")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 2);

            startActivityForResult(intent, SET_DATA2);

        }

        btn3.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "풍향")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 3);

            startActivityForResult(intent, SET_DATA3);

        }

        birdsTV.setOnClickListener {
            startDlgBirds()
        }

        familyNameTV.setOnClickListener {
            startDlgBirds()
        }

        zoologicalTV.setOnClickListener {
            startDlgBirds()
        }

        // 이용 층위
        useLayerTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("교목층");
            listItems.add("아교목층");
            listItems.add("관목층");
            listItems.add("초본층");
            listItems.add("수면");
            listItems.add("수변");
            listItems.add("취소");

            alert(listItems, "이용 층위 선택", useLayerTV, "useLayer");

        }

        // 주요 행위
        mjActTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("채이");
            listItems.add("휴식");
            listItems.add("Song");
            listItems.add("Call");
            listItems.add("Flying");
            listItems.add("Pass");
            listItems.add("목욕");
            listItems.add("물먹기");
            listItems.add("번식 및 번식행동");
            listItems.add("취소");

            alert(listItems, "주요 행위 선택", mjActTV, "mjAct");

        }

        // 이용 대상
        useTarTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("수종명 기록");
            listItems.add("흙");
            listItems.add("물");
            listItems.add("인공물");
            listItems.add("취소");

            alert(listItems, "이용 대상 선택", useTarTV, "useTar");

        }

        nextTV.setOnClickListener {

            var birds_attribute: Birds_attribute = Birds_attribute(null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    null,null,null,null,null)

            keyId = intent.getStringExtra("GROP_ID")

            birds_attribute.GROP_ID = keyId

            birds_attribute.PRJ_NAME = ""

            birds_attribute.INV_REGION = invRegionET.text.toString()


            birds_attribute.INV_DT = Utils.todayStr()


            if(invPersonTV.text == null){
                birds_attribute.INV_PERSON = userName
            }else {
                birds_attribute.INV_PERSON = invPersonTV.text.toString()
            }

            birds_attribute.WEATHER = btn1.text.toString()

            birds_attribute.WIND = btn2.text.toString()

            birds_attribute.WIND_DIRE = btn3.text.toString()

            if(temperatureET.text.isNotEmpty()){
                Utils.getString(temperatureET).toFloat();
            }

            birds_attribute.ETC = etcET.text.toString()

            birds_attribute.NUM = numTV.text.toString().toInt()

            birds_attribute.INV_TM = Utils.timeStr()

            birds_attribute.SPEC_NM = birdsTV.text.toString()

            birds_attribute.FAMI_NM = familyNameTV.text.toString()

            birds_attribute.SCIEN_NM = zoologicalTV.text.toString()

            if (indicntET.text.isNotEmpty()) {
                birds_attribute.INDI_CNT = indicntET.text.toString().toInt()
            }

            birds_attribute.OBS_STAT = obsstatTV.text.toString()

            birds_attribute.USE_TAR = useTarTV.text.toString()
            birds_attribute.USE_TAR_SP = useTarSpET.text.toString()

            birds_attribute.USE_LAYER = useLayerTV.text.toString()

            birds_attribute.MJ_ACT = mjActTV.text.toString()
            birds_attribute.MJ_ACT_PR = mjActPrET.text.toString()


            if (gpslatTV.text.isNotEmpty()) {
                birds_attribute.GPS_LAT = lat.toFloat()
            }

            if (gpslonTV.text.isNotEmpty()) {
                birds_attribute.GPS_LON = log.toFloat()
            }




            if(chkdata){

                birds_attribute.TEMP_YN = "Y"


                dbManager.updatebirds_attribute(birds_attribute,pk)

            }else {

                birds_attribute.TEMP_YN = "Y"

                dbManager.insertbirds_attribute(birds_attribute)

            }

            clear()
            chkdata = false
            pk = null

        }

        obsstatTV.setOnClickListener {
            val intent = Intent(this, DlgBridsClassActivity::class.java)
            intent.putExtra("title", "토지이용유형 분류")
            intent.putExtra("table", "Region")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA4);
        }

        btnPIC_FOLDER.setOnClickListener {

            var ListItems: List<String>
            ListItems = ArrayList();
            ListItems.add("카메라");
            ListItems.add("사진");
            ListItems.add("취소");

            val items = Array<CharSequence>(ListItems.size, { i -> ListItems.get(i) })

            var builder: AlertDialog.Builder = AlertDialog.Builder(this);
            builder.setTitle("선택해 주세요");

            builder.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->

                when (i) {
                    //카메라
                    0 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            loadPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)
                        } else {
                            takePhoto()
                        }

                    }
                    //갤러리
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            loadPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                        } else {
                            imageFromGallery();
                        }
                    }
                }

            })
            builder.show();

        }

//        initGPS();

    }

    fun startDlgBirds(){
        val intent = Intent(context, DlgBirdsActivity::class.java)
        startActivityForResult(intent, SET_BIRDS);
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

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            // File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)


            // File photo = new File(dir, System.currentTimeMillis() + ".jpg");

            try {
                val photo = File.createTempFile(
                        System.currentTimeMillis().toString(), /* prefix */
                        ".jpg", /* suffix */
                        storageDir      /* directory */
                )

/*                absolutePath = photo.absolutePath
                imageUri = Uri.fromFile(photo)
                //imageUri = Uri.fromFile(photo);
                imageUri = FileProvider.getUriForFile(context, context!!.getApplicationContext().getPackageName() + ".provider", photo)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, FROM_CAMERA)*/

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {

                    startActivityForResult(takePictureIntent, FROM_CAMERA)
                    cameraPath = photo.absolutePath;
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun imageFromGallery() {

        val intent1 = Intent(context, WriteAlbumActivity::class.java)
//        startActivity(intent1);
        startActivityForResult(intent1, FROM_ALBUM)

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
            } else if (Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                imageFromGallery()
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE == perm) {
                loadPermissions(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA)
            } else if (Manifest.permission.CAMERA == perm) {
                takePhoto()
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
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var region: Region

        var vegetation: Vegetation

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_DATA1 -> {

                    btn1.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_DATA2 -> {

                    btn2.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_DATA3 -> {

                    btn3.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_BIRDS -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    birdsTV.text = name
                    familyNameTV.text = family_name
                    zoologicalTV.text = zoological

                };

                SET_DATA4 -> {

                    if(data!!.getSerializableExtra("Region") != null) {
                        region = data!!.getSerializableExtra("Region") as Region
                        obsstatET.visibility = View.GONE
                        obsstatTV.visibility = View.VISIBLE
                        obsstatTV.setText(region.SMALLCATEGORY)
                    }

                    if(data!!.getSerializableExtra("Vegetation") != null) {
                        vegetation = data!!.getSerializableExtra("Vegetation") as Vegetation
                        obsstatET.visibility = View.GONE
                        obsstatTV.visibility = View.VISIBLE
                        obsstatTV.setText(vegetation.CORRESPONDINGNAME)

                    }

                    if(data!!.getIntExtra("Other",0) != null){
                        val count = data!!.getIntExtra("Other",0)

                        if(count == 1000){
                        obsstatET.visibility = View.VISIBLE
                        obsstatTV.visibility = View.GONE
                        }
                    }

                }

                FROM_CAMERA -> {

                    if (resultCode == -1) {

                        /*  val options = BitmapFactory.Options()
                          options.inJustDecodeBounds = true

                          options.inJustDecodeBounds = false
                          options.inSampleSize = 1
                          if (options.outWidth > 96) {
                              val ws = options.outWidth / 96 + 1
                              if (ws > options.inSampleSize) {
                                  options.inSampleSize = ws
                              }
                          }
                          if (options.outHeight > 96) {
                              val hs = options.outHeight / 96 + 1
                              if (hs > options.inSampleSize) {
                                  options.inSampleSize = hs
                              }
                          }*/

                        var extras: Bundle = data!!.getExtras();
                        val bitmap = extras.get("data") as Bitmap

                        val v = View.inflate(context, R.layout.item_add_image, null)
                        val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
                        imageIV.setImageBitmap(bitmap)
                        delIV.setTag(images!!.size)

                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(v)
                        }

                    }
                }

                FROM_ALBUM -> {
                    val result = data!!.getStringArrayExtra("result")
                    for (i in result.indices) {
                        val str = result[i]
                        images_path!!.add(str);
                        val add_file = Utils.getImage(context!!.getContentResolver(), str)
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }

                        }
                        reset(str, i)

                    }
                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        println("test : $i")

                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                    }
                }
            }
        }
    }

    fun setDirEmpty( dirName:String){

        var path = Environment.getExternalStorageDirectory().toString() + dirName;

        val dir:File    =  File(path);
        var childFileList = dir.listFiles()

        if(dir.exists()){
            for(childFile:File in childFileList){

                if(childFile.isDirectory()){

                    setDirEmpty(childFile.absolutePath); //하위디렉토리

                } else{

                    childFile.delete(); // 하위파일
                }

            }
            dir.delete();
        }
    }

    fun saveVitmapToFile(bitmap:Bitmap, filePath:String){

        var file = File(filePath)
        var out: OutputStream? =null
        try {
            file.createNewFile()
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);

        }catch (e:Exception){

            e.printStackTrace()
        }finally {

            out!!.close()
        }

    }

    fun reset(str: String, i: Int) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(str, options)
        options.inJustDecodeBounds = false
        options.inSampleSize = 1
        if (options.outWidth > 96) {
            val ws = options.outWidth / 96 + 1
            if (ws > options.inSampleSize) {
                options.inSampleSize = ws
            }
        }
        if (options.outHeight > 96) {
            val hs = options.outHeight / 96 + 1
            if (hs > options.inSampleSize) {
                options.inSampleSize = hs
            }
        }
        val bitmap = BitmapFactory.decodeFile(str, options)
        val v = View.inflate(context, R.layout.item_add_image, null)
        val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
        imageIV.setImageBitmap(bitmap)
        delIV.tag = i

        if (imgSeq == 0) {
            addPicturesLL!!.addView(v)
        }
    }

    fun clickMethod(v: View) {
        addPicturesLL!!.removeAllViews()
        images!!.clear()
        val tag = v.tag as Int
        images_path!!.removeAt(tag)

        for (k in images_url!!.indices) {
            val vv = View.inflate(context, R.layout.item_add_image, null)
            val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
            val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
            delIV.visibility = View.GONE
            val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
            del2IV.visibility = View.VISIBLE
            del2IV.tag = k
            ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
            if (imgSeq == 0) {
                addPicturesLL!!.addView(vv)
            }
        }
        for (j in images_path!!.indices) {
            val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
            if (images!!.size == 0) {
                images!!.add(add_file)
            } else {
                try {
                    images!!.set(images!!.size, add_file)
                } catch (e: IndexOutOfBoundsException) {
                    images!!.add(add_file)
                }

            }
            reset(images_path!!.get(j), j)
        }
    }

    fun clickMethod2(v: View) {
        addPicturesLL!!.removeAllViews()
        val tag = v.tag as Int
        images_url!!.removeAt(tag)
        images_url_remove!!.add(images_id!!.get(tag).toString())
        images_id!!.removeAt(tag)

        for (k in images_url!!.indices) {
            val vv = View.inflate(context, R.layout.item_add_image, null)
            val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
            val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
            delIV.visibility = View.GONE
            val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
            del2IV.visibility = View.VISIBLE
            del2IV.tag = k
            ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
            if (imgSeq == 0) {
                addPicturesLL!!.addView(vv)
            }
        }
        for (j in images_path!!.indices) {
            val add_file = Utils.getImage(context!!.getContentResolver(), images_path!!.get(j))
            if (images!!.size == 0) {
                images!!.add(add_file)
            } else {
                try {
                    images!!.set(images!!.size, add_file)
                } catch (e: IndexOutOfBoundsException) {
                    images!!.add(add_file)
                }

            }
            reset(images_path!!.get(j), j)
        }


    }

    override fun onLocationUpdated(p0: Location?) {

        stopLocation()

        if (p0 != null) {
            latitude = p0.getLatitude().toFloat()
            longitude = p0.getLongitude().toFloat()

            gpslatTV.setText(lat)
            gpslonTV.setText(log)

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }



    fun clear(){

        invDtTV.setText(Utils.todayStr())

        btn1.setText("")       //날씨
        btn2.setText("")          //바람
        btn3.setText("")     //풍향

        temperatureET.setText("")       //기온
        etcET.setText("")

        birdsTV.setText("")
        familyNameTV.setText("")
        zoologicalTV.setText("")
        indicntET.setText("")

        obsstatTV.setText("")
        useTarTV.setText("")
        useTarSpET.setText("")
        useLayerTV.setText("")
        mjActTV.setText("")
        mjActPrET.setText("")

    }

    fun resetPage(page : Int){

        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

         pk = intent.getStringExtra("id")

        val data = db.query("birdsAttribute", dataList, "id = '${pk}'", null, null, null, "", null);

        while (data.moveToNext()) {

            var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25), data.getString(26))

            birds_attribute.GROP_ID = keyId

            invRegionET.setText(birds_attribute.INV_REGION)
            if(invRegionET.text == null){
                invRegionET.setText("")
            }

            birds_attribute.PRJ_NAME = ""

            invDtTV.setText(birds_attribute.INV_DT)
            if(invDtTV.text == null){
                invDtTV.setText("")
            }

            invPersonTV.setText( birds_attribute.INV_PERSON)
            if(invPersonTV.text == null){
                invPersonTV.setText("")
            }

            btn1.setText(birds_attribute.WEATHER)
            if(btn1.text == null){
                btn1.setText("0.0")
            }

            btn2.setText(birds_attribute.WIND)
            if(btn2.text == null){
                btn2.setText("")
            }

            btn3.setText(birds_attribute.WIND_DIRE)
            if(btn3.text == null){
                btn3.setText("")
            }

            temperatureET.setText(birds_attribute.TEMPERATUR.toString())
            if(temperatureET.text == null){
                temperatureET.setText("0.0")
            }


            etcET.setText(birds_attribute.ETC)
            if(etcET.text == null){
                etcET.setText("")
            }

            numTV.setText(birds_attribute.NUM.toString())
            if(numTV.text == null){
                numTV.setText("")
            }

            timeTV.setText(birds_attribute.INV_TM)
            if(timeTV.text == null){
                timeTV.setText("")
            }

            birdsTV.setText(birds_attribute.SPEC_NM)
            if(birdsTV.text == null){
                birdsTV.setText("")
            }

            familyNameTV.setText(birds_attribute.FAMI_NM)
            if(familyNameTV.text == null){
                familyNameTV.setText("")
            }

            zoologicalTV.setText(birds_attribute.SCIEN_NM)
            if(zoologicalTV.text == null){
                zoologicalTV.setText("")
            }

            indicntET.setText(birds_attribute.INDI_CNT.toString())
            if(indicntET.text == null){
                indicntET.setText("0")
            }

            obsstatTV.setText(birds_attribute.OBS_STAT)
            if(obsstatTV.text == null){
                obsstatTV.setText("")
            }

            useTarTV.setText(birds_attribute.USE_TAR)
            if(useTarTV.text == null){
                useTarTV.setText("")
                useTarSpLL.visibility = View.GONE
            }else if(useTarTV.text == "") {
                useTarSpLL.visibility = View.GONE
            }



            useTarSpET.setText(birds_attribute.USE_TAR_SP)
            if(useTarSpET.text == null){
                useTarSpET.setText("")
                useTarSpLL.visibility = View.GONE
            }

            if(birds_attribute.USE_TAR_SP != null && !birds_attribute.USE_TAR_SP.equals("")){
                useTarSpLL.visibility = View.VISIBLE
            }

            useLayerTV.setText(birds_attribute.USE_LAYER)
            if(useLayerTV.text == null){
                useLayerTV.setText("")
            }

            mjActTV.setText(birds_attribute.MJ_ACT)
            if(mjActTV.text == null){
                mjActTV.setText("")
                mjActPrLL.visibility = View.GONE
            }else if(mjActTV.text == "") {
                mjActPrLL.visibility = View.GONE
            }

            mjActPrET.setText(birds_attribute.MJ_ACT_PR)
            if(mjActPrET.text == null){
                mjActPrET.setText("")
                mjActPrLL.visibility = View.GONE
            }

            if(birds_attribute.MJ_ACT_PR != null && !birds_attribute.MJ_ACT_PR.equals("")){
                mjActPrLL.visibility = View.VISIBLE
            }

            gpslatTV.setText(lat)
            gpslonTV.setText(log)

        }

    }


}
