package hntecology.ecology.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import hntecology.ecology.model.Number
import kotlinx.android.synthetic.main.activity_biotope.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class BiotopeActivity : Activity(),com.google.android.gms.location.LocationListener {

    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    var geom = ""

    val SET_DATA1 = 1;
    val SET_DATA2 = 2
    val SET_DATA3 = 3
    val SET_DATA4 = 4
    val SET_DATA5 = 5
    val SET_DATA6 = 6
    val SET_RATE = 7

    val BIOTOPE_BASE = 3000
    var keyId: String? = null;
    var pk: String? = null
    var chkdata: Boolean = false;
    var basechkdata: Boolean = false

    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2
    private var absolutePath: String? = null
    private var imageUri: Uri? = null
    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101

    private var context: Context? = null

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

//    Map<String, Array()>

    private var addPicturesLL: LinearLayout? = null
    private val imgSeq = 0

    var cameraPath: String? = null

    var finishFlag: Boolean = true
    //gps 다시 시작.

    //위치정보 객체
    var lm: LocationManager? = null
    //위치정보 장치 이름
    var provider: String? = null

    var page: Int? = null

    var dataArray: ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

    var GPS_LAT: String = ""
    var GPS_LON: String = ""

    var lat: String = ""
    var log: String = ""

    var polygonid: String? = null

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var landuse:String? = null

    var biotope:String? = null

    var invtm = ""

    var prjname = ""

    var INV_REGION = ""

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biotope)

        this.context = this

        window.setGravity(Gravity.RIGHT);
//        this.setFinishOnTouchOutside(true);
        buildGoogleApiClient();

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        addPicturesLL = findViewById(R.id.addPicturesLL)

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);
        //etinvesDatetimeTV 바뀜.
        //etinvesDatetimeTV.text = getTime()

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase()

        var intent: Intent = getIntent();

        var today = Utils.todayStr();
        var todays = today.split("-")

        var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

        for (i in 1 until todays.size){
            texttoday += todays.get(i)
        }

        tvINV_IndexTV.setText(texttoday + "1")

        val userName = PrefUtils.getStringPreference(context, "name");
        tvINV_PERSONTV.setText(userName)

        prjnameTV.setText( PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        etINV_DTTV.setText(Utils.todayStr())

        var time = Utils.timeStr()
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

        etINV_TMTV.setText(time)

        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")

            println("polygonid ---------$polygonid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            etGPS_LATTV.setText(lat)
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            println("==============$log")
            etGPS_LONTV.setText(log)
        }

        if (intent.getStringExtra("geom") != null){
            geom = intent.getStringExtra("geom")
            println("---------biotopegeom $geom")
        }

        if (intent.getIntExtra("landuse" ,0)!= null){
            val color = intent.getIntExtra("landuse",0)

            landuse = color.toString()
            println("landuse : $landuse")
        }

        val dataList: Array<String> = arrayOf("*");

        if(intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null){
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")
            try {
                var geocoder:Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

//                    etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e:IOException) {
                e.printStackTrace();
            }

        }

        keyId = intent.getStringExtra("GROP_ID")
        println("keyid ---------------biotope $keyId")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            tvINV_PERSONTV.setText(base.INV_PERSON)
            etINV_DTTV.setText(base.INV_DT)
            etINV_TMTV.setText(base.INV_TM)

            etGPS_LATTV.setText(base.GPS_LAT)
            etGPS_LONTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, tvINV_PERSONTV.text.toString(), etINV_DTTV.text.toString(), etINV_TMTV.text.toString())

            dbManager!!.insertbase(base)

        }

        etLU_TY_RATEET.setOnClickListener {
            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "토지이용유형(%)")
            intent.putExtra("DlgHeight", 600f);
            intent.putExtra("selectDlg", 500);

            startActivityForResult(intent, SET_RATE);
        }

        etTRE_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "교목층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA2);
        }

        etSTRE_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "아교목층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA3);
        }

        etSHR_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "관목층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA4);
        }

        etHER_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "초본층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA5);
        }

        if (intent.getStringExtra("id") != null) {

            btn_biotopDelete.visibility = View.VISIBLE

            tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
            etINV_DTTV.setText(getTime());
            etINV_TMTV.setText(createId())

            val dataList: Array<String> = arrayOf("*");

            var data = db!!.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            val data2 = db!!.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data2.moveToNext()) {

                chkdata = true
                var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                        data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                        , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                        , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                        , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                        , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                        , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                        , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53),data2.getString(54),data2.getString(55),data2.getString(56),data2.getString(57))

//                etinvesRegionET.text        = biotope_attribute.INVES_REGION

                etGPS_LATTV.setText(biotope_attribute.GPS_LAT.toString())
                etGPS_LONTV.setText(biotope_attribute.GPS_LON.toString())

                if (biotope_attribute.GPS_LON != 0.0 && biotope_attribute.GPS_LAT != 0.0) {
                    lat = biotope_attribute.GPS_LAT.toString()
                    log = biotope_attribute.GPS_LON.toString()
                    try {
                        var geocoder: Geocoder = Geocoder(context);

                        var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                        if (list.size > 0) {
                            System.out.println("list : " + list);

//                            etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                            INV_REGION = list.get(0).getAddressLine(0)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }

                if (biotope_attribute.INV_REGION != "" && biotope_attribute.INV_REGION != null) {
                    etINV_REGIONET.setText(biotope_attribute.INV_REGION);                   // 조사지
                    INV_REGION = biotope_attribute.INV_REGION.toString()
                } else {

                }

                if (biotope_attribute.INV_PERSON != "" && biotope_attribute.INV_PERSON != null) {
                    tvINV_PERSONTV.setText(biotope_attribute.INV_PERSON)                    // 조사자
                } else {
                    tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))
                }

                if (biotope_attribute.INV_DT != "" && biotope_attribute.INV_DT != null) {
                    etINV_DTTV.setText(biotope_attribute.INV_DT)
                } else {
                    etINV_DTTV.setText(Utils.todayStr())
                }

                if (biotope_attribute.INV_TM != "" && biotope_attribute.INV_TM != null) {
                    etINV_TMTV.setText(biotope_attribute.INV_TM)
                } else {
                    etINV_TMTV.setText(Utils.timeStr())
                }

                if (etINV_DTTV.text == null || etINV_DTTV.text == ""){
                    etINV_DTTV.setText(Utils.todayStr())
                }

                if (etINV_TMTV.text == null || etINV_TMTV.text == ""){
                    etINV_TMTV.setText(Utils.timeStr())
                }

                var timesplit = biotope_attribute.INV_TM!!.split(":")
                if (timesplit.size > 1) {
                    invtm = timesplit.get(0) + timesplit.get(1)
                }

                tvINV_IndexTV.setText(biotope_attribute.INV_INDEX.toString())

                if (biotope_attribute.PRJ_NAME != null && biotope_attribute.PRJ_NAME != "") {
                    prjnameTV.setText(biotope_attribute.PRJ_NAME)
                } else {
                    prjnameTV.setText( PrefUtils.getStringPreference(context, "prjname"))
                }

                if (biotope_attribute.LU_GR_NUM == null){
                    TVLU_GR_NumTV.setText("")
                } else {
                    TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
                }

                etLU_TY_RATEET.setText(biotope_attribute.LU_TY_RATE.toString())
                etSTAND_HET.setText(biotope_attribute.STAND_H.toString())
//                TVLC_GR_NUMTV.setText(biotope_attribute.LC_GR_NUM)

                if (biotope_attribute.LU_GR_NUM != null && biotope_attribute.LU_GR_NUM != "") {

//                    TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
////                    if (TVLU_GR_NumTV.text == null) {
////                        TVLU_GR_NumTV.setText("")
////                    }
////                    ETLU_GR_NumET.setText(biotope_attribute.LU_GR_NUM)
////                    if (ETLU_GR_NumET.text == null) {
////                        ETLU_GR_NumET.setText("")
////                    }

                    var text = biotope_attribute.LU_GR_NUM!!.split("(")

                    println("text ------ $text")

                    val data = db!!.query("biotopeM", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                    while (data.moveToNext()) {
                        TVLU_GR_NumTV.setText(data.getString(1) + "("+data.getString(0)+")")
                        if (TVLU_GR_NumTV.text == null) {
                            TVLU_GR_NumTV.setText("")
                        }
                        ETLU_GR_NumET.setText(data.getString(1) + "("+data.getString(0)+")")
                        if (ETLU_GR_NumET.text == null) {
                            ETLU_GR_NumET.setText("")
                        }

                    }
                }

                if (biotope_attribute.LC_GR_NUM != null&& biotope_attribute.LC_GR_NUM != "") {

//                    TVLC_GR_NUMTV.setText(biotope_attribute.LC_GR_NUM)
//                    if (TVLC_GR_NUMTV.text == null) {
//                        TVLC_GR_NUMTV.setText("")
//                    }
//                    ETlcmGR_NumET.setText(biotope_attribute.LC_GR_NUM)
//                    if (ETlcmGR_NumET.text == null) {
//                        ETlcmGR_NumET.setText("")
//                    }

                    var text = biotope_attribute.LC_GR_NUM!!.split("(")

//                    TVLC_GR_NUMTV.setText(data.getString(1))
                    val data = db!!.query("biotopeS", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                    while (data.moveToNext()) {

                        TVLC_GR_NUMTV.setText(data.getString(1) + "("+data.getString(0)+")")
                        if (TVLC_GR_NUMTV.text == null) {
                            TVLC_GR_NUMTV.setText("")
                        }
                        ETlcmGR_NumET.setText(data.getString(1)  + "("+data.getString(0)+")")
                        if (ETlcmGR_NumET.text == null) {
                            ETlcmGR_NumET.setText("")
                        }

                        var num = biotope_attribute.LC_GR_NUM
//                var textnum = num
                        var textnum = ""
                        if (num != null && num.length > 1) {
                            textnum = num!!.substring(0, 1)
                        }

                        //투수
                        if (textnum == "B") {
                            etlcmTypepET.setText(data.getString(1)  + "("+data.getString(0)+")")
//                        etlcmTypepET.setText(biotope_attribute.LC_GR_NUM)
                            //불투수
                        } else if (textnum == "A") {
                            etlcmTypeiET.setText(data.getString(1)  + "("+data.getString(0)+")")
//                        etlcmTypeiET.setText(biotope_attribute.LC_GR_NUM)
                            //녹지
                        } else if (textnum == "C") {
                            etlcmTypegET.setText(data.getString(1)  + "("+data.getString(0)+")")
//                        etlcmTypegET.setText(biotope_attribute.LC_GR_NUM)
                            //수공간
                        } else if (textnum == "D") {
//                        etlcmTypewET.setText(biotope_attribute.LC_GR_NUM)
                            etlcmTypewET.setText(data.getString(1)  + "("+data.getString(0)+")")
                        }
                    }

                }

                TVTY_MARKTV.setText(biotope_attribute.TY_MARK)
                etGV_RATEET.setText(biotope_attribute.GV_RATE.toString())
                etGV_STRUCTET.setText(biotope_attribute.GV_STRUCT)
                etDIS_RETET.setText(biotope_attribute.DIS_RET)
                etRESTOR_POTET.setText(biotope_attribute.RESTOR_POT)
                etCOMP_INTAET.setText(biotope_attribute.COMP_INTA)
                etVP_INTAET.setText(biotope_attribute.VP_INTA)
                etBREA_DIAET.setText(biotope_attribute.BREA_DIA)
                etFIN_ESTET.setText(biotope_attribute.FIN_EST)
                etTRE_SPECET.setText(biotope_attribute.TRE_SPEC)
                etTRE_FAMIET.setText(biotope_attribute.TRE_FAMI)
                etTRE_SCIENET.setText(biotope_attribute.TRE_SCIEN.toString())
                etTRE_HET.setText(biotope_attribute.TRE_H.toString())
                etTRE_BREAET.setText(biotope_attribute.TRE_BREA.toString())
                etTRE_COVEET.setText(biotope_attribute.TRE_COVE.toString())
                etSTRE_SPECET.setText(biotope_attribute.STRE_SPEC.toString())
                etSTRE_FAMIET.setText(biotope_attribute.STRE_FAMI.toString())
                etSTRE_SCIENET.setText(biotope_attribute.STRE_SCIEN.toString())
                etSTRE_HET.setText(biotope_attribute.STRE_H.toString())
                etSTRE_BREAET.setText(biotope_attribute.STRE_BREA.toString())
                etSTRE_COVEET.setText(biotope_attribute.STRE_COVE.toString())
                etSHR_SPECET.setText(biotope_attribute.SHR_SPEC)
                etSHR_FAMIET.setText(biotope_attribute.SHR_FAMI.toString())
                etSHR_SCIENET.setText(biotope_attribute.SHR_SCIEN.toString())
                etSHR_HET.setText(biotope_attribute.SHR_H.toString())
                etSTR_COVEET.setText(biotope_attribute.STR_COVE.toString())
                etHER_SPECET.setText(biotope_attribute.HER_SPEC.toString())
                etHER_FAMIET.setText(biotope_attribute.HER_FAMI.toString())
                etHER_SCIENET.setText(biotope_attribute.HER_SCIEN.toString())
                etHER_HET.setText(biotope_attribute.HER_H.toString())
                etHER_COVEET.setText(biotope_attribute.HER_COVE.toString())
                etPIC_FOLDERET.setText(biotope_attribute.PIC_FOLDER.toString())
                etWILD_ANIET.setText(biotope_attribute.WILD_ANI.toString())
                etBIOTOP_POTET.setText(biotope_attribute.BIOTOP_POT.toString())
                etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())
                tvPIC_FOLDERTV.setText(biotope_attribute.PIC_FOLDER)
                etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())

                etIMP_FORMET.setText(biotope_attribute.IMP_FORM.toString())
                ufidTV.setText(biotope_attribute.UFID)
                checkTV.setText(biotope_attribute.CHECK)

                geom = biotope_attribute.GEOM.toString()

                landuse = biotope_attribute.LANDUSE

//                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM +"." + biotope_attribute.INV_INDEX + "/imges")
//                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
//                    val fileList = file.listFiles()
                    val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                    val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images/")
                    var tmpfileList = tmpfiles.listFiles()

//                    if (fileList != null) {
//                        for (i in 0..fileList.size - 1) {
//                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/imges/"
//                            val outputsDir = File(outPath)
//
//                            if (outputsDir.exists()) {
//                                println("Exit : $outPath")
//
//                                val files = outputsDir.listFiles()
//                                if (files != null) {
//                                    for (i in files.indices) {
//                                        println("f : " + files[i])
//                                    }
//                                }
//
//                            } else {
//                                val made = outputsDir.mkdirs()
//
//                                println("made : $made")
//                            }
//
//                            val tmpfile = fileList.get(i)
//                            println("filelist.get(i) ${fileList.get(i)}")
//                            val num = biotope_attribute.INV_INDEX.toString()
//                            println("-------------$num")
//                            val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/imges", num + "_" + biotope_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                            if (tmpfile.exists()) {
//                                tmpfile.renameTo(tmpfile2)
//                            }
//
//                            tmpfileList = tmpfiles.listFiles()
//
//                        }
//                    }

                    if (tmpfileList != null) {
                        for (i in 0..tmpfileList.size - 1) {

                            val options = BitmapFactory.Options()
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
                            }

                            images_path!!.add(tmpfileList.get(i).path)
                            println("images_path --- ${images_path!!.size}")

                            for (j in 0..tmpfileList.size - 1) {
                                println("invtm--------$invtm")
                                if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator + biotope_attribute.INV_INDEX.toString() + "_" + invtm +"_" + (j+1) + ".png")) {
//                                if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" + biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {
                                    val bitmap = BitmapFactory.decodeFile(tmpfileList.get(i).path, options)
                                    val v = View.inflate(context, R.layout.item_add_image, null)
                                    val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                                    val delIV = v.findViewById<View>(R.id.delIV) as ImageView
                                    imageIV.setImageBitmap(bitmap)
                                    delIV.setTag(i)
                                    images!!.add(bitmap)
                                    if (imgSeq == 0) {
                                        addPicturesLL!!.addView(v)
                                    }
                                }
                            }
                        }
                    }
//                    if (file.isDirectory){
//                        val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
//                        path.deleteRecursively()
//                    }


                val id = biotope_attribute.id

                if (biotope_attribute.TEMP_YN.equals("N")) {
                    dbManager!!.deletebiotope_attribute(biotope_attribute, id)
                }

                if (biotope_attribute.TEMP_YN.equals("Y")) {
                    dataArray.add(biotope_attribute)
                }

                tvCONF_MOD.setText(biotope_attribute.CONF_MOD)
            }


            if (data.count < 1) {
                tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
                etINV_DTTV.setText(getTime());
                etINV_TMTV.setText(createId())
                tvPIC_FOLDERTV.visibility = View.GONE;
            }

        }

//        //gps 다시 시작
//        /**위치정보 객체를 생성한다.*/
//      lm =  getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//      /** 현재 사용가능한 위치 정보 장치 검색*/
//      //위치정보 하드웨어 목록
//      var c =  Criteria();
//      //최적의 하드웨어 이름을 리턴받는다.
//      provider = lm!!.getBestProvider(c, true);
//
//      // 최적의 값이 없거나, 해당 장치가 사용가능한 상태가 아니라면,
//      //모든 장치 리스트에서 사용가능한 항목 얻기
//      if(provider == null || !lm!!.isProviderEnabled(provider)){
//       // 모든 장치 목록
//        var list = lm!!.getAllProviders();
//
//       for(i in 0..list.size-1){
//        //장치 이름 하나 얻기
//        var temp = list.get(i);
//
//        //사용 가능 여부 검사
//        if(lm!!.isProviderEnabled(temp)){
//         provider = temp;
//         break;
//        }
//       }
//      }// (end if)위치정보 검색 끝
//
//        /**마지막으로  조회했던 위치 얻기*/
//        val location = lm!!.getLastKnownLocation(provider)
//
//        if (location == null) {
//            Toast.makeText(this, "사용가능한 위치 정보 제공자가 없습니다.", Toast.LENGTH_SHORT).show()
//        } else {
//            //최종 위치에서 부터 이어서 GPS 시작...
//            onLocationChanged(location)
//
//        }

        //토지이용현황 분류 버튼  높이 450f
        btn_Dlg1.setOnClickListener {

            val intent = Intent(this, DlgCommonActivity::class.java)
            intent.putExtra("title", "토지이용유형 분류기준")
            intent.putExtra("table", "biotopeM")
            intent.putExtra("DlgHeight", 450f);
//            startActivity(intent)
            startActivityForResult(intent, SET_DATA1);

        }
        //토지피복현황 분류 버튼 사이즈 높이 600f 줄 것.
        btn_Dlg2.setOnClickListener {

            val intent = Intent(this, DlgCommonActivity::class.java)
            intent.putExtra("title", "토지피복현황 분류기준")
            intent.putExtra("table", "biotopeS")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA1);

        }

        //현존식생현황 분류 버튼
        btn_Dlg3.setOnClickListener {

            val intent = Intent(this, DlgBiotopeClassActivity::class.java)
            intent.putExtra("title", "현존식생현황 분류기준")
            intent.putExtra("table", "biotopeClass")
            intent.putExtra("DlgHeight", 600f);

            startActivityForResult(intent, SET_DATA6);

        }

        etTRE_SPECETreset.setOnClickListener {
            etTRE_SPECETLL.visibility = View.GONE
            etTRE_SPECETtmp.setText("")
            etTRE_SPECET.visibility = View.VISIBLE
        }

        etSTRE_SPECETreset.setOnClickListener {
            etSTRE_SPECETLL.visibility = View.GONE
            etSTRE_SPECETtmp.setText("")
            etSTRE_SPECET.visibility = View.VISIBLE
        }

        etSHR_SPECETreset.setOnClickListener {
            etSHR_SPECETLL.visibility = View.GONE
            etSHR_SPECETtmp.setText("")
            etSHR_SPECET.visibility = View.VISIBLE
        }

        etHER_SPECETreset.setOnClickListener {
            etHER_SPECETLL.visibility = View.GONE
            etHER_SPECETtmp.setText("")
            etHER_SPECET.visibility = View.VISIBLE
        }

        btn_biotopCancle1.setOnClickListener {

            if (intent.getSerializableExtra("biotopedata") == null){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            val dbManager: DataBaseHelper = DataBaseHelper(this)

                            val db = dbManager.createDataBase()

                            val dataList: Array<String> = arrayOf("*");

                            val data2 = db.query("biotopeAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                            var dataArray:ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

                            while (data2.moveToNext()) {

                                var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                                        data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                                        , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                                        , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                                        , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                                        , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                                        , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                                        , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53),data2.getString(54),data2.getString(55),data2.getString(56),data2.getString(57))

                                dataArray.add(biotope_attribute)

                            }

                            if (dataArray.size == 0 ){
                                var intent = Intent()
                                intent.putExtra("polygonid", polygonid)
                                setResult(RESULT_OK, intent);

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {

                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory){
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        path.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory){
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        path.deleteRecursively()
                                    }
                                }
                            }

                            finish()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }else {
                finish()
            }
        }

        //sqlite 저장.
        btn_biotopSave1.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null,null,null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

                        biotope_attribute.GROP_ID = keyId

                        val prj = prjnameTV.text.toString()
                        if (prj == prjname){
                            biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
                        }
//                        biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
//                        if (prjnameTV.length() > 0){
//                            biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
//                        } else {
//                            biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
//                        }

                        if (etINV_REGIONET.length() > 0){
                            biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();
                        } else {
                            biotope_attribute.INV_REGION = INV_REGION
                        }



                        biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
//            biotope_attribute.INVES_DATETIME        =   etinvesDatetimeTV.text.toString()

                        if (etINV_DTTV.text == null) {
                            biotope_attribute.INV_DT = Utils.todayStr()
                        } else {
                            biotope_attribute.INV_DT = etINV_DTTV.text.toString();
                        }

                        if (etINV_TMTV.text == null) {
                            biotope_attribute.INV_TM = Utils.timeStr()
                        } else {
                            biotope_attribute.INV_TM = etINV_TMTV.text.toString();
                        }

                        biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()

                        biotope_attribute.LU_GR_NUM = TVLU_GR_NumTV.text.toString()

                        if (ETLU_GR_NumET.text.toString() != null && ETLU_GR_NumET.text.toString() != "") {
                            biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()
                        }

                        if (etLU_TY_RATEET.text.isNotEmpty()) {

//                            biotope_attribute.LU_TY_RATE = Utils.getString(etLU_TY_RATEET).toFloat();
                            biotope_attribute.LU_TY_RATE = etLU_TY_RATEET.text.toString().toFloat()
                        }
                        if (etSTAND_HET.text.isNotEmpty()) {

                            biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
                        }

                        biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
                        biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()

                        if (ETTY_MARKET.text.toString() != null && ETTY_MARKET.text.toString() != "") {
                            biotope_attribute.TY_MARK = ETTY_MARKET.text.toString()
                        }

                        if (etGV_RATEET.text.isNotEmpty()) {

                            biotope_attribute.GV_RATE = Utils.getString(etGV_RATEET).toFloat();
                        }

                        biotope_attribute.GV_STRUCT = etGV_STRUCTET.text.toString()
                        biotope_attribute.DIS_RET = etDIS_RETET.text.toString()
                        biotope_attribute.RESTOR_POT = etRESTOR_POTET.text.toString()
                        biotope_attribute.COMP_INTA = etCOMP_INTAET.text.toString()
                        biotope_attribute.VP_INTA = etVP_INTAET.text.toString()
                        biotope_attribute.IMP_FORM = etIMP_FORMET.text.toString()
                        biotope_attribute.BREA_DIA = etBREA_DIAET.text.toString()
                        biotope_attribute.FIN_EST = etFIN_ESTET.text.toString()
                        biotope_attribute.TRE_SPEC = etTRE_SPECET.text.toString()
                        if (etTRE_SPECETtmp.text.toString().length > 0){
                        biotope_attribute.TRE_SPEC = etTRE_SPECETtmp.text.toString()
                    }

                        biotope_attribute.TRE_FAMI = etTRE_FAMIET.text.toString()
                        biotope_attribute.TRE_SCIEN = etTRE_SCIENET.text.toString()

                        if (etTRE_HET.text.isNotEmpty()) {

                            biotope_attribute.TRE_H = Utils.getString(etTRE_HET).toFloat();

                        }
                        if (etTRE_BREAET.text.isNotEmpty()) {

                            biotope_attribute.TRE_BREA = Utils.getString(etTRE_BREAET).toFloat();
                        }
                        if (etTRE_COVEET.text.isNotEmpty()) {

                            biotope_attribute.TRE_COVE = Utils.getString(etTRE_COVEET).toFloat();
                        }

                        biotope_attribute.STRE_SPEC = etSTRE_SPECET.text.toString()
                        biotope_attribute.STRE_FAMI = etSTRE_FAMIET.text.toString()
                        biotope_attribute.STRE_SCIEN = etSTRE_SCIENET.text.toString()

                        if (etSTRE_HET.text.isNotEmpty()) {

                            biotope_attribute.STRE_H = Utils.getString(etSTRE_HET).toFloat();
                        }

                        if (etSTRE_BREAET.text.isNotEmpty()) {

                            biotope_attribute.STRE_BREA = Utils.getString(etSTRE_BREAET).toFloat();
                        }

                        if (etSTRE_COVEET.text.isNotEmpty()) {

                            biotope_attribute.STRE_COVE = Utils.getString(etSTRE_COVEET).toFloat();
                        }

                        biotope_attribute.SHR_SPEC = etSHR_SPECET.text.toString()

                        biotope_attribute.SHR_FAMI = etSHR_FAMIET.text.toString()
                        biotope_attribute.SHR_SCIEN = etSHR_SCIENET.text.toString()

                        if (etSHR_HET.text.isNotEmpty()) {

                            biotope_attribute.SHR_H = Utils.getString(etSHR_HET).toFloat();
                        }

                        if (etSTR_COVEET.text.isNotEmpty()) {

                            biotope_attribute.STR_COVE = Utils.getString(etSTR_COVEET).toFloat();
                        }

                        biotope_attribute.HER_SPEC = etHER_SPECET.text.toString()

                        biotope_attribute.HER_FAMI = etHER_FAMIET.text.toString()
                        biotope_attribute.HER_SCIEN = etHER_SCIENET.text.toString()

                        if (etHER_HET.text.isNotEmpty()) {

                            biotope_attribute.HER_H = Utils.getString(etHER_HET).toFloat();
                        }

                        if (etHER_COVEET.text.isNotEmpty()) {

                            biotope_attribute.HER_COVE = Utils.getString(etHER_COVEET).toFloat();
                        }

                        biotope_attribute.WILD_ANI = etWILD_ANIET.text.toString()
                        biotope_attribute.BIOTOP_POT = etBIOTOP_POTET.text.toString()
                        biotope_attribute.UNUS_NOTE = etUNUS_NOTEET.text.toString()

                        //투수
                        if (etlcmTypepET.text.toString() != "") {

                            biotope_attribute.LC_TY = etlcmTypepET.text.toString()
                            //불투수
                        } else if (etlcmTypeiET.text.toString() != "") {

                            biotope_attribute.LC_TY = etlcmTypeiET.text.toString()
                            //녹지
                        } else if (etlcmTypegET.text.toString() != "") {

                            biotope_attribute.LC_TY = etlcmTypegET.text.toString()
                            //수공간
                        } else if (etlcmTypewET.text.toString() != "") {

                            biotope_attribute.LC_TY = etlcmTypewET.text.toString()
                        }

                        if (etGPS_LATTV.text.toString() != "" && etGPS_LONTV.text.toString() != "") {

                            biotope_attribute.GPS_LAT = lat.toDouble()
                            biotope_attribute.GPS_LON = log.toDouble()

                        }

                        biotope_attribute.TEMP_YN = "Y"

                        biotope_attribute.CONF_MOD = "N"
                        biotope_attribute.LANDUSE = landuse

                        biotope_attribute.GEOM = geom

                        if (biotope != null){
                            biotope_attribute.LANDUSE = biotope
                        }

                        biotope_attribute.UFID = ufidTV.text.toString()
                        biotope_attribute.CHECK = checkTV.text.toString()


                        if (intent.getSerializableExtra("biotopedata") != null){
                            biotope_attribute.LANDUSE = landuse
                        }

                        println("landuse ------ $landuse")

                        if (chkdata) {

                            if (images!!.size > 0 && biotope_attribute.PIC_FOLDER == null) {

                                biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                            }


                            if (pk != null) {

                                val CONF_MOD = tvCONF_MOD.text.toString()

                                if (CONF_MOD == "N" || CONF_MOD == "C") {
                                    biotope_attribute.CONF_MOD = "M"
                                }

                                dbManager!!.updatebiotope_attribute(biotope_attribute, pk)
                                dbManager!!.updatecommonbiotope(biotope_attribute,keyId)
                            }
//                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
////                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images/")
//                            val pathdir = path.listFiles()
//
//                            if (pathdir != null) {
//                                for (i in 0..pathdir.size - 1) {
//
//                                    for (j in 0..pathdir.size - 1) {
//                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator + biotope_attribute.INV_INDEX.toString() + "_" + biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {
//
////                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" + biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {
//
//                                            pathdir.get(i).canonicalFile.delete()
//
//
//                                        }
//                                    }
//
//                                }
//                            }
//
//                            for (i in 0..images!!.size - 1) {
//
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
//                                val outputsDir = File(outPath)
//
//                                if (outputsDir.exists()) {
//                                    println("Exit : $outPath")
//
//                                    val files = outputsDir.listFiles()
//                                    if (files != null) {
//                                        for (i in files.indices) {
//                                            println("f : " + files[i])
//                                        }
//                                    }
//
//                                } else {
//                                    val made = outputsDir.mkdirs()
//
//                                    println("made : $made")
//                                }
//
//                                saveVitmapToFile(images!!.get(i), outPath + biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                            }
//
//                            val deletedir = path.listFiles()
//                            if (deletedir.size == 0){
//                                if (path.isDirectory){
//                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                    deletepath.deleteRecursively()
//                                }
//                            }

                        } else {

                        if (images!!.size > 0) {
                            biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                        }

                            dbManager!!.insertbiotope_attribute(biotope_attribute);

//                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            sdPath += "/ecology/data/biotope/images" + biotope_attribute.INV_DT + "_" + biotope_attribute.INV_TM + "_" + biotope_attribute.INV_INDEX
//                            val biotope = File(sdPath)
//                            biotope.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

//                            val file = File(sdPath)
//                            file.mkdir();
                            //이미 있다면 삭제. 후 생성
//                            setDirEmpty(sdPath)

//                            sdPath += "/"

                            var pathArray: ArrayList<String> = ArrayList<String>()

//                            for (i in 0..images!!.size - 1) {
//
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/"
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/" + biotope_attribute.INV_DT + "_" + biotope_attribute.INV_TM + "_" + biotope_attribute.INV_INDEX
//                                val outputsDir = File(outPath)
//
//                                if (outputsDir.exists()) {
//                                    println("Exit : $outPath")
//
//                                    val files = outputsDir.listFiles()
//                                    if (files != null) {
//                                        for (i in files.indices) {
//                                            println("f : " + files[i])
//                                        }
//                                    }
//
//                                } else {
//                                    val made = outputsDir.mkdirs()
//
//                                    println("made : $made")
//                                }
//
//                                saveVitmapToFile(images!!.get(i), outPath+biotope_attribute.INV_INDEX+"_"+biotope_attribute.INV_TM+"_" + (i+1) + ".png")
//
//                            }

                        }

//                        intent.putExtra("bio_attri", biotope_attribute);
//
//                        setResult(RESULT_OK, intent);
                        finishFlag = false

                        var intent = Intent()

                        intent.putExtra("export", 70);

                        setResult(RESULT_OK, intent);

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn_biotopDelete.setOnClickListener {

            if (pk != null) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            var biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null,null,null,null,null)

                            if (pk != null) {
                                val data2 = db!!.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

                                while (data2.moveToNext()) {
                                    biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                                            data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                                            , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                                            , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                                            , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                                            , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                                            , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                                            , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53),data2.getString(54),data2.getString(55),data2.getString(56),data2.getString(57))


                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator + biotope_attribute.INV_INDEX.toString() + "_" + invtm +"_" + (j+1) + ".png")) {
//                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" +  biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }

                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory){
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        path.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory){
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        path.deleteRecursively()
                                    }
                                }


                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    println("GROP_ID---------------$GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data2.moveToNext()) {

                                        chkdata = true

                                        var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                                                data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                                                , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                                                , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                                                , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                                                , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                                                , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                                                , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53), data2.getString(54),data2.getString(55),data2.getString(56),data2.getString(57))

                                        dataArray.add(biotope_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {

                                        println("pk $pk =============================================")

                                        dbManager!!.deletebiotope_attribute(biotope_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager!!.deletebiotope_attribute(biotope_attribute, pk)

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

            if (pk == null) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            val biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null,null,null,null,null)

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")
                                val GROP_ID = intent.getStringExtra("GROP_ID")

                                val dataList: Array<String> = arrayOf("*");

                                val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data2.moveToNext()) {

                                    chkdata = true

                                    var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                                            data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                                            , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                                            , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                                            , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                                            , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                                            , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                                            , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53), data2.getString(54),data2.getString(55),data2.getString(56),data2.getString(57))

                                    dataArray.add(biotope_attribute)

                                }

                                var intent = Intent()

                                if(dataArray.size > 1) {

                                    dbManager!!.deletebiotope_attribute(biotope_attribute,pk)

                                    intent.putExtra("reset", 100)

                                    setResult(RESULT_OK, intent);
                                    finish()

                                }

                                if(dataArray.size == 1){

                                    var intent = Intent()

                                    intent.putExtra("polygonid", polygonid)

                                    dbManager!!.deletebiotope_attribute(biotope_attribute, pk)

                                    setResult(RESULT_OK, intent);
                                    finish()

                                }

                            }

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }
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

        btn_biotopAdd.setOnClickListener {
            var biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null,null,null,null,null)

            keyId = intent.getStringExtra("GROP_ID")

            biotope_attribute.GROP_ID = keyId

            val prj = prjnameTV.text.toString()
            if (prj == prjname){
                biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
            }

//            biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
//            if (prjnameTV.length() > 0){
//                biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
//            } else {
//                biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
//            }

            if (etINV_REGIONET.length() > 0){
                biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();
            } else {
                biotope_attribute.INV_REGION = INV_REGION
            }
            biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
//            biotope_attribute.INVES_DATETIME        =   etinvesDatetimeTV.text.toString()

            if (etINV_DTTV.text == null) {
                biotope_attribute.INV_DT = Utils.todayStr()
            } else {
                biotope_attribute.INV_DT = etINV_DTTV.text.toString();
            }

            if (etINV_TMTV.text == null) {
                biotope_attribute.INV_TM = Utils.timeStr()
            } else {
                biotope_attribute.INV_TM = etINV_TMTV.text.toString();
            }

            if (tvINV_IndexTV.text.isNotEmpty()) {
                biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()
            }

            if (etLU_TY_RATEET.text.isNotEmpty()) {

//                biotope_attribute.LU_TY_RATE = Utils.getString(etLU_TY_RATEET).toFloat();
                biotope_attribute.LU_TY_RATE = etLU_TY_RATEET.text.toString().toFloat()
            }
            if (etSTAND_HET.text.isNotEmpty()) {

                biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
            }

            biotope_attribute.LU_GR_NUM = TVLU_GR_NumTV.text.toString()
            if (ETLU_GR_NumET.text.toString() != null && ETLU_GR_NumET.text.toString() != ""){
                biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()
            }

            biotope_attribute.LC_GR_NUM = TVLC_GR_NUMTV.text.toString()
            if (ETlcmGR_NumET.text != null) {
                biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
            }
            biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()

            if (ETTY_MARKET.text.toString() != null && ETTY_MARKET.text.toString() != "") {
                biotope_attribute.TY_MARK = ETTY_MARKET.text.toString()
            }

            if (etGV_RATEET.text.isNotEmpty()) {

                biotope_attribute.GV_RATE = Utils.getString(etGV_RATEET).toFloat();
            }
            biotope_attribute.GV_STRUCT = etGV_STRUCTET.text.toString()
            biotope_attribute.DIS_RET = etDIS_RETET.text.toString()
            biotope_attribute.RESTOR_POT = etRESTOR_POTET.text.toString()
            biotope_attribute.COMP_INTA = etCOMP_INTAET.text.toString()
            biotope_attribute.VP_INTA = etVP_INTAET.text.toString()
            biotope_attribute.IMP_FORM = etIMP_FORMET.text.toString()
            biotope_attribute.BREA_DIA = etBREA_DIAET.text.toString()
            biotope_attribute.FIN_EST = etFIN_ESTET.text.toString()
            biotope_attribute.TRE_SPEC = etTRE_SPECET.text.toString()
            if (etTRE_SPECETtmp.text.toString().length > 0){
                biotope_attribute.TRE_SPEC = etTRE_SPECETtmp.text.toString()
            }

            biotope_attribute.TRE_FAMI = etTRE_FAMIET.text.toString()
            biotope_attribute.TRE_SCIEN = etTRE_SCIENET.text.toString()

            if (etTRE_HET.text.isNotEmpty()) {

                biotope_attribute.TRE_H = Utils.getString(etTRE_HET).toFloat();

            }
            if (etTRE_BREAET.text.isNotEmpty()) {

                biotope_attribute.TRE_BREA = Utils.getString(etTRE_BREAET).toFloat();
            }
            if (etTRE_COVEET.text.isNotEmpty()) {

                biotope_attribute.TRE_COVE = Utils.getString(etTRE_COVEET).toFloat();
            }

            biotope_attribute.STRE_SPEC = etSTRE_SPECET.text.toString()
            biotope_attribute.STRE_FAMI = etSTRE_FAMIET.text.toString()
            biotope_attribute.STRE_SCIEN = etSTRE_SCIENET.text.toString()

            if (etSTRE_HET.text.isNotEmpty()) {

                biotope_attribute.STRE_H = Utils.getString(etSTRE_HET).toFloat();
            }

            if (etSTRE_BREAET.text.isNotEmpty()) {

                biotope_attribute.STRE_BREA = Utils.getString(etSTRE_BREAET).toFloat();
            }

            if (etSTRE_COVEET.text.isNotEmpty()) {

                biotope_attribute.STRE_COVE = Utils.getString(etSTRE_COVEET).toFloat();
            }

            biotope_attribute.SHR_SPEC = etSHR_SPECET.text.toString()

            biotope_attribute.SHR_FAMI = etSHR_FAMIET.text.toString()
            biotope_attribute.SHR_SCIEN = etSHR_SCIENET.text.toString()

            if (etSHR_HET.text.isNotEmpty()) {

                biotope_attribute.SHR_H = Utils.getString(etSHR_HET).toFloat();
            }

            if (etSTR_COVEET.text.isNotEmpty()) {

                biotope_attribute.STR_COVE = Utils.getString(etSTR_COVEET).toFloat();
            }

            biotope_attribute.HER_SPEC = etHER_SPECET.text.toString()

            biotope_attribute.HER_FAMI = etHER_FAMIET.text.toString()
            biotope_attribute.HER_SCIEN = etHER_SCIENET.text.toString()

            if (etHER_HET.text.isNotEmpty()) {

                biotope_attribute.HER_H = Utils.getString(etHER_HET).toFloat();
            }

            if (etHER_COVEET.text.isNotEmpty()) {

                biotope_attribute.HER_COVE = Utils.getString(etHER_COVEET).toFloat();
            }

            biotope_attribute.WILD_ANI = etWILD_ANIET.text.toString()
            biotope_attribute.BIOTOP_POT = etBIOTOP_POTET.text.toString()
            biotope_attribute.UNUS_NOTE = etUNUS_NOTEET.text.toString()

            //투수
            if (etlcmTypepET.text.toString() != "") {

                biotope_attribute.LC_TY = etlcmTypepET.text.toString()
                //불투수
            } else if (etlcmTypeiET.text.toString() != "") {

                biotope_attribute.LC_TY = etlcmTypeiET.text.toString()
                //녹지
            } else if (etlcmTypegET.text.toString() != "") {

                biotope_attribute.LC_TY = etlcmTypegET.text.toString()
                //수공간
            } else if (etlcmTypewET.text.toString() != "") {

                biotope_attribute.LC_TY = etlcmTypewET.text.toString()
            }

            if (etGPS_LATTV.text.toString() != "" && etGPS_LONTV.text.toString() != "") {

                biotope_attribute.GPS_LAT = lat.toDouble()
                biotope_attribute.GPS_LON = log.toDouble()

            }

            biotope_attribute.TEMP_YN = "Y"

            biotope_attribute.CONF_MOD = "N"
            biotope_attribute.LANDUSE = landuse

            if (biotope != null){
                biotope_attribute.LANDUSE = biotope
            }
            biotope_attribute.GEOM = geom
            biotope_attribute.UFID = ufidTV.text.toString()
            biotope_attribute.CHECK = checkTV.text.toString()

            if (chkdata) {

                if (images!!.size > 0 && biotope_attribute.PIC_FOLDER == null) {

                    biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                }

                if (pk != null) {

                    val CONF_MOD = tvCONF_MOD.text.toString()

                    if (CONF_MOD == "N" || CONF_MOD == "C") {
                        biotope_attribute.CONF_MOD = "M"
                    }

                    dbManager!!.updatebiotope_attribute(biotope_attribute, pk)
                    dbManager!!.updatecommonbiotope(biotope_attribute,keyId)
                }

//                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/")
//                val pathdir = path.listFiles()
//
//                if (pathdir != null) {
//                    for (i in 0..pathdir.size - 1) {
//
//                        for (j in 0..pathdir.size - 1) {
//
//                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator + biotope_attribute.INV_INDEX.toString() + "_" + biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {
//                                if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" + biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (i+1) + ".png")) {
//
//                                pathdir.get(i).canonicalFile.delete()
//
//                                println("delete ===============")
//
//                            }
//                        }
//
//                    }
//                }
//
//                for (i in 0..images!!.size - 1) {
//
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/"
//                    val outputsDir = File(outPath)
//
//                    if (outputsDir.exists()) {
//                        println("Exit : $outPath")
//
//                        val files = outputsDir.listFiles()
//                        if (files != null) {
//                            for (i in files.indices) {
//                                println("f : " + files[i])
//                            }
//                        }
//
//                    } else {
//                        val made = outputsDir.mkdirs()
//
//                        println("made : $made")
//                    }
//
//                    saveVitmapToFile(images!!.get(i), outPath + biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                }

            } else {

                if (images!!.size > 0) {
                    biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                }

                dbManager!!.insertbiotope_attribute(biotope_attribute);

//                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                sdPath += "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "." + biotope_attribute.INV_INDEX + "/images"
//                val biotope = File(sdPath)
//                biotope.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                val file = File(sdPath)
//                file.mkdir();
//                //이미 있다면 삭제. 후 생성
//                setDirEmpty(sdPath)
//
//                sdPath += "/"

//                var pathArray: ArrayList<String> = ArrayList<String>()
//
//                for (i in 0..images!!.size - 1) {
//
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "." + biotope_attribute.INV_INDEX +"/images/"
//                    val outputsDir = File(outPath)
//
//                    if (outputsDir.exists()) {
//                        println("Exit : $outPath")
//
//                        val files = outputsDir.listFiles()
//                        if (files != null) {
//                            for (i in files.indices) {
//                                println("f : " + files[i])
//                            }
//                        }
//
//                    } else {
//                        val made = outputsDir.mkdirs()
//
//                        println("made : $made")
//                    }
//
//                    saveVitmapToFile(images!!.get(i), outPath + i + ".png")
//
//                }

            }

            finishFlag = false

            if (intent.getStringExtra("set") != null) {
                var intent = Intent()
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            var intent = Intent()
            intent.putExtra("export",70)
            setResult(RESULT_OK, intent)

            btn_biotopDelete.visibility = View.GONE

            if (images_path != null){
                images_path!!.clear()
            }

            if (images != null){
                images!!.clear()
            }

            if (images_url != null){
                images_url!!.clear()
            }

            if (images_url_remove != null){
                images_url_remove!!.clear()
            }

            if (images_id != null){
                images_id!!.clear()
            }

            clear()
            chkdata = false
            pk = null

        }

        if (intent.getSerializableExtra("biotopedata") != null){

            var biotope_attribute = intent.getSerializableExtra("biotopedata") as Biotope_attribute

            println("biotope_attribute ${biotope_attribute.GPS_LON}")
            val dbManager: DataBaseHelper = DataBaseHelper(this)

            if (biotope_attribute.GPS_LON != 0.0 && biotope_attribute.GPS_LAT != 0.0) {
                lat = biotope_attribute.GPS_LAT.toString()
                log = biotope_attribute.GPS_LON.toString()
                try {
                    var geocoder: Geocoder = Geocoder(context);

                    var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                    if (list.size > 0) {
                        System.out.println("list : " + list);

//                        etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                        INV_REGION = list.get(0).getAddressLine(0)
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }

            val db = dbManager.createDataBase()

            val dataList: Array<String> = arrayOf("*");

            etGPS_LATTV.setText(biotope_attribute.GPS_LAT.toString())
            etGPS_LONTV.setText(biotope_attribute.GPS_LON.toString())

            if (intent.getStringExtra("latitude") != null) {
                lat = intent.getStringExtra("latitude")

                println("==============$lat")
                etGPS_LATTV.setText(lat)
            }

            if (intent.getStringExtra("longitude") != null) {
                log = intent.getStringExtra("longitude")
                println("==============$log")
                etGPS_LONTV.setText(log)
            }

            if (intent.getStringExtra("EMD_NM") != null) {
                val EMD_NM = intent.getStringExtra("EMD_NM")

                if (EMD_NM != "" && EMD_NM != null) {
                    etINV_REGIONET.setText(EMD_NM);
                }
            }

            try {
                var geocoder:Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

//                    etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e:IOException) {
                e.printStackTrace();
            }

            etINV_DTTV.setText(biotope_attribute.INV_DT)
            etINV_TMTV.setText(biotope_attribute.INV_TM)

            if (etINV_DTTV.text == null || etINV_DTTV.text == ""){
                etINV_DTTV.setText(Utils.todayStr())
            }

            if (etINV_TMTV.text == null || etINV_TMTV.text == ""){
                etINV_TMTV.setText(Utils.timeStr())
            }
            tvINV_IndexTV.setText(biotope_attribute.INV_INDEX.toString())

            TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
            if (TVLU_GR_NumTV.text == null) {
                TVLU_GR_NumTV.setText("")
            }

            etLU_TY_RATEET.setText(biotope_attribute.LU_TY_RATE.toString())
            etSTAND_HET.setText(biotope_attribute.STAND_H.toString())
            TVLC_GR_NUMTV.setText(biotope_attribute.LC_GR_NUM)
            if (TVLC_GR_NUMTV.text == null) {
                TVLC_GR_NUMTV.setText("")
            }

            if (biotope_attribute.LU_GR_NUM != null && biotope_attribute.LU_GR_NUM != "") {

//                    TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
////                    if (TVLU_GR_NumTV.text == null) {
////                        TVLU_GR_NumTV.setText("")
////                    }
////                    ETLU_GR_NumET.setText(biotope_attribute.LU_GR_NUM)
////                    if (ETLU_GR_NumET.text == null) {
////                        ETLU_GR_NumET.setText("")
////                    }

                var text = biotope_attribute.LU_GR_NUM!!.split("(")

                println("text ------ $text")

                val data = db!!.query("biotopeM", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                while (data.moveToNext()) {
                    TVLU_GR_NumTV.setText(data.getString(1) + "("+data.getString(0)+")")
                    if (TVLU_GR_NumTV.text == null) {
                        TVLU_GR_NumTV.setText("")
                    }
                    ETLU_GR_NumET.setText(data.getString(1) + "("+data.getString(0)+")")
                    if (ETLU_GR_NumET.text == null) {
                        ETLU_GR_NumET.setText("")
                    }

                }
            }

            if (biotope_attribute.LC_GR_NUM != null&& biotope_attribute.LC_GR_NUM != "") {

//                    TVLC_GR_NUMTV.setText(biotope_attribute.LC_GR_NUM)
//                    if (TVLC_GR_NUMTV.text == null) {
//                        TVLC_GR_NUMTV.setText("")
//                    }
//                    ETlcmGR_NumET.setText(biotope_attribute.LC_GR_NUM)
//                    if (ETlcmGR_NumET.text == null) {
//                        ETlcmGR_NumET.setText("")
//                    }

                var text = biotope_attribute.LC_GR_NUM!!.split("(")

//                    TVLC_GR_NUMTV.setText(data.getString(1))
                val data = db!!.query("biotopeS", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                while (data.moveToNext()) {

                    TVLC_GR_NUMTV.setText(data.getString(1) + "("+data.getString(0)+")")
                    if (TVLC_GR_NUMTV.text == null) {
                        TVLC_GR_NUMTV.setText("")
                    }
                    ETlcmGR_NumET.setText(data.getString(1)  + "("+data.getString(0)+")")
                    if (ETlcmGR_NumET.text == null) {
                        ETlcmGR_NumET.setText("")
                    }

                    var num = biotope_attribute.LC_GR_NUM
//                var textnum = num
                    var textnum = ""
                    if (num != null && num.length > 1) {
                        textnum = num!!.substring(0, 1)
                    }

                    //투수
                    if (textnum == "B") {
                        etlcmTypepET.setText(data.getString(1)  + "("+data.getString(0)+")")
//                        etlcmTypepET.setText(biotope_attribute.LC_GR_NUM)
                        //불투수
                    } else if (textnum == "A") {
                        etlcmTypeiET.setText(data.getString(1)  + "("+data.getString(0)+")")
//                        etlcmTypeiET.setText(biotope_attribute.LC_GR_NUM)
                        //녹지
                    } else if (textnum == "C") {
                        etlcmTypegET.setText(data.getString(1)  + "("+data.getString(0)+")")
//                        etlcmTypegET.setText(biotope_attribute.LC_GR_NUM)
                        //수공간
                    } else if (textnum == "D") {
//                        etlcmTypewET.setText(biotope_attribute.LC_GR_NUM)
                        etlcmTypewET.setText(data.getString(1)  + "("+data.getString(0)+")")
                    }
                }

            }

            TVTY_MARKTV.setText(biotope_attribute.TY_MARK)
            etGV_RATEET.setText(biotope_attribute.GV_RATE.toString())
            etGV_STRUCTET.setText(biotope_attribute.GV_STRUCT)
            etDIS_RETET.setText(biotope_attribute.DIS_RET)
            etRESTOR_POTET.setText(biotope_attribute.RESTOR_POT)
            etCOMP_INTAET.setText(biotope_attribute.COMP_INTA)
            etVP_INTAET.setText(biotope_attribute.VP_INTA)
            etBREA_DIAET.setText(biotope_attribute.BREA_DIA)
            etFIN_ESTET.setText(biotope_attribute.FIN_EST)
            etTRE_SPECET.setText(biotope_attribute.TRE_SPEC)
            etTRE_FAMIET.setText(biotope_attribute.TRE_FAMI)
            etTRE_SCIENET.setText(biotope_attribute.TRE_SCIEN.toString())
            etTRE_HET.setText(biotope_attribute.TRE_H.toString())
            etTRE_BREAET.setText(biotope_attribute.TRE_BREA.toString())
            etTRE_COVEET.setText(biotope_attribute.TRE_COVE.toString())
            etSTRE_SPECET.setText(biotope_attribute.STRE_SPEC.toString())
            etSTRE_FAMIET.setText(biotope_attribute.STRE_FAMI.toString())
            etSTRE_SCIENET.setText(biotope_attribute.STRE_SCIEN.toString())
            etSTRE_HET.setText(biotope_attribute.STRE_H.toString())
            etSTRE_BREAET.setText(biotope_attribute.STRE_BREA.toString())
            etSTRE_COVEET.setText(biotope_attribute.STRE_COVE.toString())
            etSHR_SPECET.setText(biotope_attribute.SHR_SPEC)
            etSHR_FAMIET.setText(biotope_attribute.SHR_FAMI.toString())
            etSHR_SCIENET.setText(biotope_attribute.SHR_SCIEN.toString())
            etSHR_HET.setText(biotope_attribute.SHR_H.toString())
            etSTR_COVEET.setText(biotope_attribute.STR_COVE.toString())
            etHER_SPECET.setText(biotope_attribute.HER_SPEC.toString())
            etHER_FAMIET.setText(biotope_attribute.HER_FAMI.toString())
            etHER_SCIENET.setText(biotope_attribute.HER_SCIEN.toString())
            etHER_HET.setText(biotope_attribute.HER_H.toString())
            etHER_COVEET.setText(biotope_attribute.HER_COVE.toString())
            etPIC_FOLDERET.setText(biotope_attribute.PIC_FOLDER.toString())
            etWILD_ANIET.setText(biotope_attribute.WILD_ANI.toString())
            etBIOTOP_POTET.setText(biotope_attribute.BIOTOP_POT.toString())
            etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())
            tvPIC_FOLDERTV.setText(biotope_attribute.PIC_FOLDER)
            etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())
            ufidTV.setText(biotope_attribute.UFID)
            checkTV.setText(biotope_attribute.CHECK)

            etIMP_FORMET.setText(biotope_attribute.IMP_FORM.toString())
            landuse = biotope_attribute.LANDUSE
            geom = biotope_attribute.GEOM.toString()


//            if (intent.getStringExtra("biotope") != null){
//                biotope = intent.getStringExtra("biotope")
//
//                println("biotope--------$biotope")
//
//                val data = db!!.query("biotopeM", dataList, "code = '" + biotope + "'", null, null, null, "", null);
//
//                while (data.moveToNext()) {
//                    TVLU_GR_NumTV.setText(data.getString(1))
//                    if (TVLU_GR_NumTV.text == null) {
//                        TVLU_GR_NumTV.setText("")
//                    }
//                    ETLU_GR_NumET.setText(data.getString(1))
//                    if (ETLU_GR_NumET.text == null) {
//                        ETLU_GR_NumET.setText("")
//                    }
//                }
//            }
//
//            if (intent.getStringExtra("biotope") != null) {
//                biotope = intent.getStringExtra("biotope")
//                var chk = false
//
//                val dataSelectList: Array<String> = arrayOf("name", "code");
//                val data = db!!.query("biotopeS", dataList, "code = '" + biotope + "'", null, null, null, "", null);
//
//                while (data.moveToNext()) {
//
//                    TVLC_GR_NUMTV.setText(data.getString(1))
//                    if (TVLC_GR_NUMTV.text == null) {
//                        TVLC_GR_NUMTV.setText("")
//                    }
//                    ETlcmGR_NumET.setText(data.getString(1))
//                    if (ETlcmGR_NumET.text == null) {
//                        ETlcmGR_NumET.setText("")
//                    }
//
//                    chk = true
//
//                }
//
//                landuse = biotope_attribute.LANDUSE
//
//                var num = biotope
//                var textnum = num!!.substring(0, 1)
//
//                //투수
//                if (chk == true) {
//                    if (textnum == "B") {
//
//                        etlcmTypepET.setText(biotope)
//                        //불투수
//                    } else if (textnum == "A") {
//
//                        etlcmTypeiET.setText(biotope)
//                        //녹지
//                    } else if (textnum == "C") {
//
//                        etlcmTypegET.setText(biotope)
//                        //수공간
//                    } else if (textnum == "D") {
//                        etlcmTypewET.setText(biotope)
//                    }
//                }
//            }

//            if (biotope_attribute.PIC_FOLDER == "null" || biotope_attribute.PIC_FOLDER == "" || biotope_attribute.PIC_FOLDER == null) {
//
//                tvPIC_FOLDERTV.visibility = View.GONE;
//
//            } else {
//
//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "." + biotope_attribute.INV_INDEX +"/images")
//                val fileList = file.listFiles()
//                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/")
//                var tmpfileList = tmpfiles.listFiles()
//
//                if (fileList != null) {
//                    for (i in 0..fileList.size - 1) {
//                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/"
//                        val outputsDir = File(outPath)
//
//                        if (outputsDir.exists()) {
//                            println("Exit : $outPath")
//
//                            val files = outputsDir.listFiles()
//                            if (files != null) {
//                                for (i in files.indices) {
//                                    println("f : " + files[i])
//                                }
//                            }
//
//                        } else {
//                            val made = outputsDir.mkdirs()
//
//                            println("made : $made")
//                        }
//
//                        val tmpfile = fileList.get(i)
//                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images", biotope_attribute.INV_INDEX.toString() + "_" + biotope_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                        if (tmpfile.exists()) {
//                            tmpfile.renameTo(tmpfile2)
//                        }
//
//                        tmpfileList = tmpfiles.listFiles()
//
//                    }
//                }
//
//                if (tmpfileList != null) {
//                    for (i in 0..tmpfileList.size - 1) {
//
//                        val options = BitmapFactory.Options()
//                        options.inJustDecodeBounds = true
//                        options.inJustDecodeBounds = false
//                        options.inSampleSize = 1
//                        if (options.outWidth > 96) {
//                            val ws = options.outWidth / 96 + 1
//                            if (ws > options.inSampleSize) {
//                                options.inSampleSize = ws
//                            }
//                        }
//                        if (options.outHeight > 96) {
//                            val hs = options.outHeight / 96 + 1
//                            if (hs > options.inSampleSize) {
//                                options.inSampleSize = hs
//                            }
//                        }
//
//                        images_path!!.add(tmpfileList.get(i).path)
//
//                        for (j in 0..tmpfileList.size - 1) {
//
//                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecolog/datay/biotope/images/" +biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (i+1) + ".png")) {
//                                val bitmap = BitmapFactory.decodeFile(tmpfileList.get(i).path, options)
//                                val v = View.inflate(context, R.layout.item_add_image, null)
//                                val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
//                                val delIV = v.findViewById<View>(R.id.delIV) as ImageView
//                                imageIV.setImageBitmap(bitmap)
//                                delIV.setTag(i)
//                                images!!.add(bitmap)
//                                if (imgSeq == 0) {
//                                    addPicturesLL!!.addView(v)
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }

            val id = biotope_attribute.id

            if (biotope_attribute.TEMP_YN.equals("N")) {
                dbManager.deletebiotope_attribute(biotope_attribute, id)
            }

            if (biotope_attribute.TEMP_YN.equals("Y")) {
                dataArray.add(biotope_attribute)
            }

            tvCONF_MOD.setText(biotope_attribute.CONF_MOD)
        }
    }

    fun getGps() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                getLocation();
            } else {
                //Request Location Permission
                checkLocationPermission()
            }

        } else {
            getLocation();
        }

    }

    fun getTime(): String {

        val date = Date()
        val fullTime = SimpleDateFormat("yyyy-MM-dd")

        return fullTime.format(date).toString()
    }

    fun createId(): String {

        val date = Date()
//        val fullTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val fullTime = SimpleDateFormat("HH:mm")

        return fullTime.format(date).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var biotopeModel: BiotopeModel

        var vegetationData: Vegetation

        var biotopeClass: BiotopeClass

        var vegetation: Vegetation

        var number: Number

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_DATA1 -> {

                    biotopeModel = data!!.getSerializableExtra("bioModel") as BiotopeModel


                    //토지이용현황
                    if (biotopeModel.codeType == "biotopeM") {

//                        TVLU_GR_NumTV.setText(biotopeModel.code)
//                        var substring = biotopeModel.code!!.substring(0, 1)

                       TVLU_GR_NumTV.setText(biotopeModel.code+"("+biotopeModel.name+")")
//                        TVLU_GR_NumTV.setText(substring)
                        if (TVLU_GR_NumTV.text == null) {
                            TVLU_GR_NumTV.setText("")
                        }
                        ETLU_GR_NumET.setText(biotopeModel.code+"("+biotopeModel.name+")")
//                        ETLU_GR_NumET.setText(substring)
                        if (ETLU_GR_NumET.text == null) {
                            ETLU_GR_NumET.setText("")
                        }
                        //토지피복현황
                    } else if (biotopeModel.codeType == "biotopeS") {
//                        var substring = biotopeModel.code!!.substring(0, 1)
                        ETlcmGR_NumET.setText(biotopeModel.code)
//                        ETlcmGR_NumET.setText(substring)
                        if (ETlcmGR_NumET.text == null) {
                            ETlcmGR_NumET.setText("")
                        }

                        var bioModelParent: BiotopeModel
                        bioModelParent = data!!.getSerializableExtra("bioModelParent") as BiotopeModel

                        //불투수 투수
                        etlcmTypeiET.setText("");
                        etlcmTypepET.setText("")
                        etlcmTypegET.setText("")
                        etlcmTypewET.setText("")

                        //불투수
                        if (bioModelParent.code == "A") {

                            etlcmTypeiET.setText(biotopeModel.code+"("+biotopeModel.name+")")
                            TVLC_GR_NUMTV.setText(biotopeModel.code+"("+biotopeModel.name+")")
//                            etlcmTypeiET.setText(substring)
//                            TVLC_GR_NUMTV.setText(substring)
                            //투수
                        } else if (bioModelParent.code == "B") {

                            etlcmTypepET.setText(biotopeModel.code+"("+biotopeModel.name+")")
                            TVLC_GR_NUMTV.setText(biotopeModel.code+"("+biotopeModel.name+")")
//                            etlcmTypepET.setText(substring)
//                            TVLC_GR_NUMTV.setText(substring)
                            //녹지
                        } else if (bioModelParent.code == "C") {
//                            etlcmTypegET.setText(substring)
//                            TVLC_GR_NUMTV.setText(substring)
                            etlcmTypegET.setText(biotopeModel.code+"("+biotopeModel.name+")")
                            TVLC_GR_NUMTV.setText(biotopeModel.code+"("+biotopeModel.name+")")
                            //수공간
                        } else if (bioModelParent.code == "D") {
//                            etlcmTypewET.setText(substring)
//                            TVLC_GR_NUMTV.setText(substring)
                            etlcmTypewET.setText(biotopeModel.code+"("+biotopeModel.name+")")
                            TVLC_GR_NUMTV.setText(biotopeModel.code+"("+biotopeModel.name+")")
                        }

                        //현존식생현황  아직 테이블 명 코드 미정
                    } else if (biotopeModel.codeType == "biotopeS") {

                        TVTY_MARKTV.setText(biotopeModel.name)
                        TVTY_MARKTV.setText(biotopeModel.code)
                    }

                }

                SET_DATA2 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etTRE_SPECET.setText(name)
                    if (name == "SP(미동정)"){
                        etTRE_SPECETLL.visibility = View.VISIBLE
                        etTRE_SPECET.visibility = View.GONE
                    }
                    etTRE_FAMIET.setText(family_name)
                    etTRE_SCIENET.setText(zoological)

                }

                SET_DATA3 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etSTRE_SPECET.setText(name)
                    if (name == "SP(미동정)"){
                        etSTRE_SPECETLL.visibility = View.VISIBLE
                        etSTRE_SPECET.visibility = View.GONE
                    }
                    etSTRE_FAMIET.setText(family_name)
                    etSTRE_SCIENET.setText(zoological)

                }

                SET_DATA4 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etSHR_SPECET.setText(name)
                    if (name == "SP(미동정)"){
                        etSHR_SPECETLL.visibility = View.VISIBLE
                        etSHR_SPECET.visibility = View.GONE
                    }
                    etSHR_FAMIET.setText(family_name)
                    etSHR_SCIENET.setText(zoological)

                }

                SET_DATA5 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etHER_SPECET.setText(name)
                    if (name == "SP(미동정)"){
                        etHER_SPECETLL.visibility = View.VISIBLE
                        etHER_SPECET.visibility = View.GONE
                    }
                    etHER_FAMIET.setText(family_name)
                    etHER_SCIENET.setText(zoological)

                }

                SET_DATA6 -> {

                    if (data!!.getSerializableExtra("biotopeClass") != null) {
                        biotopeClass = data!!.getSerializableExtra("biotopeClass") as BiotopeClass

                        println("biotopeSize ${biotopeClass.sign}")

                        TVTY_MARKTV.setText(biotopeClass.smallcategory)

                    }

                    if (data!!.getSerializableExtra("Vegetation") != null) {
                        vegetation = data!!.getSerializableExtra("Vegetation") as Vegetation
                        if (data!!.getSerializableExtra("Number") != null) {
                            var number = data!!.getStringExtra("Number")
                            val category = vegetation.CATEGORY
                            if (category == "기타"){
                                TVTY_MARKTV.setText("")
                                ETTY_MARKET.setText("")
                                TVTY_MARKTV.visibility = View.GONE
                                ETTY_MARKET.visibility = View.VISIBLE
                            } else {
                                ETTY_MARKET.setText("")
                                ETTY_MARKET.visibility = View.GONE
                                TVTY_MARKTV.visibility = View.VISIBLE
                                TVTY_MARKTV.setText(vegetation.SIGN)
                                if (number != ""){
                                    TVTY_MARKTV.setText(vegetation.SIGN + number)
                                }
                            }
                        }

                    }

                    if (data!!.getStringExtra("etc")!= null){
                        TVTY_MARKTV.setText("")
                        ETTY_MARKET.setText("")
                        TVTY_MARKTV.visibility = View.GONE
                        ETTY_MARKET.visibility = View.VISIBLE
                    }

                }

                SET_RATE -> {
                    etLU_TY_RATEET.setText(data!!.getStringExtra("selectDlg"))
                }

                BIOTOPE_BASE -> {

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
                        addPicturesLL!!.removeAllViews()
                        val realPathFromURI = cameraPath!!
                        images_path!!.add(cameraPath!!)
                        context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$realPathFromURI")))
                        try {
                            val add_file = Utils.getImages(context!!.contentResolver, cameraPath)

                            val v = View.inflate(context, R.layout.item_add_image, null)
                            val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                            val delIV = v.findViewById<View>(R.id.delIV) as ImageView
                            imageIV.setImageBitmap(add_file)
                            delIV.setTag(images!!.size)
                            images!!.add(add_file)

                            if (imgSeq == 0) {
                                addPicturesLL!!.addView(v)
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val child = addPicturesLL!!.getChildCount()
                        for (i in 0 until child) {

                            val v = addPicturesLL!!.getChildAt(i)

                            val num = tvINV_IndexTV.text.toString()
                            var time = ""
                            time = etINV_TMTV.text.toString()
                            var timesplit = time.split(":")
                            invtm = timesplit.get(0) + timesplit.get(1)
                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
                            val outputsDir = File(outPath)

                            if (outputsDir.exists()) {

                                val files = outputsDir.listFiles()
                                if (files != null) {
                                    for (i in files.indices) {
                                    }
                                }

                            } else {
                                val made = outputsDir.mkdirs()

                            }
                            saveVitmapToFile(images!!.get(i),outPath+num + "_" + invtm+"_"+(i+1)+".png")

                        }
                        images!!.clear()

//                        var extras: Bundle = data!!.getExtras();
//                        val bitmap = extras.get("data") as Bitmap
//
//                        val v = View.inflate(context, R.layout.item_add_image, null)
//                        val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
//                        val delIV = v.findViewById<View>(R.id.delIV) as ImageView
//                        imageIV.setImageBitmap(bitmap)
//                        images!!.add(bitmap)
//                        delIV.setTag(images!!.size)
//
//                        if (imgSeq == 0) {
//                            addPicturesLL!!.addView(v)
//                        }

                    }
                }

                FROM_ALBUM -> {
                    addPicturesLL!!.removeAllViews()
                    val result = data!!.getStringArrayExtra("result")
                    for (i in result.indices) {
                        val str = result[i]
                        images_path!!.add(str);
                    }
                    for (i in 0 until images_path!!.size){
                        val add_file = Utils.getImages(context!!.getContentResolver(), images_path!!.get(i))
                        if (images!!.size == 0) {
                            images!!.add(add_file)
                        } else {
                            try {
                                images!!.set(images!!.size, add_file)
                            } catch (e: IndexOutOfBoundsException) {
                                images!!.add(add_file)
                            }

                        }
                        reset(images_path!!.get(i), i)
                    }
                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        val v = addPicturesLL!!.getChildAt(i)

                        val num = tvINV_IndexTV.text.toString()
                        var time = ""
                        time = etINV_TMTV.text.toString()
                        var timesplit = time.split(":")
                        invtm = timesplit.get(0) + timesplit.get(1)
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
                        val outputsDir = File(outPath)

                        if (outputsDir.exists()) {

                            val files = outputsDir.listFiles()
                            if (files != null) {
                                for (i in files.indices) {
                                }
                            }

                        } else {
                            val made = outputsDir.mkdirs()

                        }
                        saveVitmapToFile(images!!.get(i),outPath+num + "_" + invtm+"_"+(i+1)+".png")

                    }
                    images!!.clear()

                }

            }
        }
    }

    fun getAttrubuteKey(): String {

        val time = System.currentTimeMillis()
//        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT
    }

    /*
        *  gps function
        * */

    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

//        if (mLocation == null) {
//            startLocationUpdates();
//        }
        startLocationUpdates();
        if (mLocation != null) {
//            tvLatitude.text =mLocation!!.latitude.toString()
//            tvLongitude.text = mLocation!!.longitude.toString()

            etGPS_LATTV.setText(lat)
            etGPS_LONTV.setText(log)
            //Toast.makeText(this, "성공"+mLocation!!.latitude.toString(), Toast.LENGTH_SHORT).show();
        } else {

            //Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    private fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient!!.connect()
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = android.support.v7.app.AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                imageFromGallery()
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE == perm) {
                loadPermissions(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA)
            } else if (Manifest.permission.CAMERA == perm) {
                takePhoto()
            }
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            try {
                val photo = File.createTempFile(
                        System.currentTimeMillis().toString(), /* prefix */
                        ".jpg", /* suffix */
                        storageDir      /* directory */
                )

                cameraPath = photo.absolutePath
                //imageUri = Uri.fromFile(photo);
                imageUri = FileProvider.getUriForFile(context, context!!.packageName + ".provider", photo)

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, FROM_CAMERA)

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
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator
                    val num = tvINV_IndexTV.text.toString()

                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)
                    var file = File(outPath+num + "_" + invtm+"_"+(tag+1)+".png")
                    file.delete()

                    for (k in images_url!!.indices) {
                        val vv = View.inflate(context, R.layout.item_add_image, null)
                        val imageIV = vv.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                        val delIV = vv.findViewById<View>(R.id.delIV) as ImageView
                        delIV.visibility = View.GONE
                        val del2IV = vv.findViewById<View>(R.id.del2IV) as ImageView
                        del2IV.visibility = View.VISIBLE
                        del2IV.tag = k
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
                        if (imgSeq == 0) {
                            addPicturesLL!!.addView(vv)
                        }
                    }
                    for (j in images_path!!.indices) {

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")

                        if (getPk.size > 2) {
                            val pathPk = getPk.get(0)
                            println("getPk {$getPk}")
                            val pathPk2 = getPk.get(1)
                            val num = tvINV_IndexTV.text.toString()

                            if (pathPk == num && pathPk2 == invtm) {
                                val add_file = Utils.getImages(context!!.getContentResolver(), images_path!!.get(j))
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
                        } else {
                            val add_file = Utils.getImages(context!!.getContentResolver(), images_path!!.get(j))
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

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

    fun clickMethod2(v: View) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

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

                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")

                        if (getPk.size > 1) {
                            val pathPk = getPk.get(0)
                            println("getPk {$getPk}")
                            val pathPk2 = getPk.get(1)
                            val num = tvINV_IndexTV.text.toString()
                            val invtm = etINV_TMTV.text.toString()

                            if (pathPk == num && pathPk2 == invtm) {
                                val add_file = Utils.getImages(context!!.getContentResolver(), images_path!!.get(j))
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
                        } else {
                            val add_file = Utils.getImages(context!!.getContentResolver(), images_path!!.get(j))
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

                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()

    }

    fun setDirEmpty(dirName: String) {

        var path = Environment.getExternalStorageDirectory().toString() + dirName;

        val dir: File = File(path);
        var childFileList = dir.listFiles()

        if (dir.exists()) {
            for (childFile: File in childFileList) {

                if (childFile.isDirectory()) {

                    setDirEmpty(childFile.absolutePath); //하위디렉토리

                } else {

                    childFile.delete(); // 하위파일
                }

            }
            dir.delete();
        }
    }

    fun saveVitmapToFile(bitmap: Bitmap, filePath: String) {

        var file = File(filePath)
        var out: OutputStream? = null
        try {
            file.createNewFile()
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (e: Exception) {

            e.printStackTrace()
        } finally {

            out!!.close()
        }

    }

    fun clear() {

        etINV_DTTV.setText(Utils.todayStr());
        etINV_TMTV.setText(createId())

        var num = tvINV_IndexTV.text.toString()

        if (num.length > 7){
            var textnum = num.substring(num.length - 2, num.length)
            var splitnum = num.substring(0, num.length - 2)
            var plusnum = textnum.toInt() + 1
            tvINV_IndexTV.setText(splitnum.toString() + plusnum.toString())
        } else {
            var textnum = num.substring(num.length - 1, num.length)
            var splitnum = num.substring(0, num.length - 1)
            var plusnum = textnum.toInt() + 1
            tvINV_IndexTV.setText(splitnum.toString() + plusnum.toString())
        }

        etINV_TMTV.setText(Utils.timeStr())

        TVLU_GR_NumTV.setText("")
        etLU_TY_RATEET.setText("")
        etSTAND_HET.setText("")
        TVLC_GR_NUMTV.setText("")
        ETLU_GR_NumET.setText("")
        ETlcmGR_NumET.setText("")

        etTRE_SPECETtmp.setText("")
        etSTRE_SPECETtmp.setText("")
        etSHR_SPECETtmp.setText("")
        etHER_SPECETtmp.setText("")

        TVTY_MARKTV.setText("")

        etlcmTypepET.setText("")
        etlcmTypeiET.setText("")
        etlcmTypegET.setText("")
        etlcmTypewET.setText("")
        etGV_RATEET.setText("")
        etGV_STRUCTET.setText("")
        etDIS_RETET.setText("")
        etRESTOR_POTET.setText("")
        etCOMP_INTAET.setText("")
        etVP_INTAET.setText("")
        etBREA_DIAET.setText("")
        etFIN_ESTET.setText("")
        etTRE_SPECET.setText("")
        etTRE_FAMIET.setText("")
        etTRE_SCIENET.setText("")
        etTRE_HET.setText("")
        etTRE_BREAET.setText("")
        etTRE_COVEET.setText("")
        etSTRE_SPECET.setText("")
        etSTRE_FAMIET.setText("")
        etSTRE_SCIENET.setText("")
        etSTRE_HET.setText("")
        etSTRE_BREAET.setText("")
        etSTRE_COVEET.setText("")
        etSHR_SPECET.setText("")
        etSHR_FAMIET.setText("")
        etSHR_SCIENET.setText("")
        etSHR_HET.setText("")
        etSTR_COVEET.setText("")
        etHER_SPECET.setText("")
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        etHER_HET.setText("")
        etHER_COVEET.setText("")
        etPIC_FOLDERET.setText("")
        etWILD_ANIET.setText("")
        etBIOTOP_POTET.setText("")
        etUNUS_NOTEET.setText("")
        tvPIC_FOLDERTV.setText("")
        etUNUS_NOTEET.setText("")

        etIMP_FORMET.setText("")

        addPicturesLL!!.removeAllViews()

    }

    fun resetPage(page: Int) {


        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = intent.getStringExtra("id")

        val data = db.query("biotopeAttribute", dataList, "id = '${id}'", null, null, null, "", null);

        if (data.count < 1) {
            tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
            etINV_DTTV.setText(getTime());
            etINV_TMTV.setText(createId())
            tvPIC_FOLDERTV.visibility = View.GONE;
        }

        while (data.moveToNext()) {

            chkdata = true
            var biotope_attribute: Biotope_attribute = Biotope_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getString(8), data.getFloat(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getFloat(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27), data.getFloat(28)
                    , data.getString(29), data.getString(30), data.getString(31), data.getFloat(32), data.getFloat(33), data.getFloat(34), data.getString(35)
                    , data.getString(36), data.getString(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41), data.getString(42)
                    , data.getFloat(43), data.getFloat(44), data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getDouble(49)
                    , data.getDouble(50), data.getString(51), data.getString(52), data.getString(53), data.getString(54),data.getString(55), data.getString(56), data.getString(57))

//                etinvesRegionET.text        = biotope_attribute.INVES_REGION

            etGPS_LATTV.setText(biotope_attribute.GPS_LAT.toString())
            etGPS_LONTV.setText(biotope_attribute.GPS_LON.toString())

            etINV_REGIONET.setText(biotope_attribute.INV_REGION);                   // 조사지
            tvINV_PERSONTV.setText(biotope_attribute.INV_PERSON)                    // 조사자

            etINV_DTTV.setText(biotope_attribute.INV_DT)
            etINV_TMTV.setText(biotope_attribute.INV_TM)
            tvINV_IndexTV.setText(biotope_attribute.INV_INDEX.toString())

            TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
            etLU_TY_RATEET.setText(biotope_attribute.LU_TY_RATE.toString())
            etSTAND_HET.setText(biotope_attribute.STAND_H.toString())
            TVLC_GR_NUMTV.setText(biotope_attribute.LC_GR_NUM)

            if (biotope_attribute.LU_GR_NUM != null) {

                val dataSelectList: Array<String> = arrayOf("name");
                val data = db.query("biotopeM", dataList, "code = '" + biotope_attribute.LU_GR_NUM + "'", null, null, null, "", null);

                while (data.moveToNext()) {

                    TVLU_GR_NumTV.setText(data.getString(0))
                    ETLU_GR_NumET.setText(data.getString(1))
                }
            }

            if (biotope_attribute.LC_GR_NUM != null) {

                val dataSelectList: Array<String> = arrayOf("name", "code");
                val data = db.query("biotopeS", dataList, "code = '" + biotope_attribute.LC_GR_NUM + "'", null, null, null, "", null);

                while (data.moveToNext()) {

                    TVLC_GR_NUMTV.setText(data.getString(0))
                    ETlcmGR_NumET.setText(data.getString(1))
                }
            }

            //투수
            if (biotope_attribute.LC_TY == "P") {

                etlcmTypepET.setText(biotope_attribute.LC_TY)
                //불투수
            } else if (biotope_attribute.LC_TY == "I") {

                etlcmTypeiET.setText(biotope_attribute.LC_TY)
                //녹지
            } else if (biotope_attribute.LC_TY == "G") {

                etlcmTypegET.setText(biotope_attribute.LC_TY)
                //수공간
            } else if (biotope_attribute.LC_TY == "W") {

                etlcmTypewET.setText(biotope_attribute.LC_TY)
            }

            TVTY_MARKTV.setText(biotope_attribute.TY_MARK)
            etGV_RATEET.setText(biotope_attribute.GV_RATE.toString())
            etGV_STRUCTET.setText(biotope_attribute.GV_STRUCT)
            etDIS_RETET.setText(biotope_attribute.DIS_RET)
            etRESTOR_POTET.setText(biotope_attribute.RESTOR_POT)
            etCOMP_INTAET.setText(biotope_attribute.COMP_INTA)
            etVP_INTAET.setText(biotope_attribute.VP_INTA)
            etBREA_DIAET.setText(biotope_attribute.BREA_DIA)
            etFIN_ESTET.setText(biotope_attribute.FIN_EST)
            etTRE_SPECET.setText(biotope_attribute.TRE_SPEC)
            etTRE_FAMIET.setText(biotope_attribute.TRE_FAMI)
            etTRE_SCIENET.setText(biotope_attribute.TRE_SCIEN.toString())
            etTRE_HET.setText(biotope_attribute.TRE_H.toString())
            etTRE_BREAET.setText(biotope_attribute.TRE_BREA.toString())
            etTRE_COVEET.setText(biotope_attribute.TRE_COVE.toString())
            etSTRE_SPECET.setText(biotope_attribute.STRE_SPEC.toString())
            etSTRE_FAMIET.setText(biotope_attribute.STRE_FAMI.toString())
            etSTRE_SCIENET.setText(biotope_attribute.STRE_SCIEN.toString())
            etSTRE_HET.setText(biotope_attribute.STRE_H.toString())
            etSTRE_BREAET.setText(biotope_attribute.STRE_BREA.toString())
            etSTRE_COVEET.setText(biotope_attribute.STRE_COVE.toString())
            etSHR_SPECET.setText(biotope_attribute.SHR_SPEC)
            etSHR_FAMIET.setText(biotope_attribute.SHR_FAMI.toString())
            etSHR_SCIENET.setText(biotope_attribute.SHR_SCIEN.toString())
            etSHR_HET.setText(biotope_attribute.SHR_H.toString())
            etSTR_COVEET.setText(biotope_attribute.STR_COVE.toString())
            etHER_SPECET.setText(biotope_attribute.HER_SPEC.toString())
            etHER_FAMIET.setText(biotope_attribute.HER_FAMI.toString())
            etHER_SCIENET.setText(biotope_attribute.HER_SCIEN.toString())
            etHER_HET.setText(biotope_attribute.HER_H.toString())
            etHER_COVEET.setText(biotope_attribute.HER_COVE.toString())
            etPIC_FOLDERET.setText(biotope_attribute.PIC_FOLDER.toString())
            etWILD_ANIET.setText(biotope_attribute.WILD_ANI.toString())
            etBIOTOP_POTET.setText(biotope_attribute.BIOTOP_POT.toString())
            etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())
            tvPIC_FOLDERTV.setText(biotope_attribute.PIC_FOLDER)
            etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())

            etIMP_FORMET.setText(biotope_attribute.IMP_FORM.toString())



            if (biotope_attribute.PIC_FOLDER == "null" || biotope_attribute.PIC_FOLDER == "" || biotope_attribute.PIC_FOLDER == null) {

                tvPIC_FOLDERTV.visibility = View.GONE;

            } else {

                val file = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ecology/biotope/imgs/" + biotope_attribute.PIC_FOLDER)

                val fileList = file.listFiles()

                for (i in 0..fileList.size - 1) {

                    val options = BitmapFactory.Options()
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
                    }
                    images_path!!.add(fileList.get(i).path)
                    val bitmap = BitmapFactory.decodeFile(fileList.get(i).path, options)
                    val v = View.inflate(context, R.layout.item_add_image, null)
                    val imageIV = v.findViewById<View>(R.id.imageIV) as SelectableRoundedImageView
                    val delIV = v.findViewById<View>(R.id.delIV) as ImageView
                    imageIV.setImageBitmap(bitmap)
                    delIV.setTag(i)
                    images!!.add(bitmap)
                    if (imgSeq == 0) {
                        addPicturesLL!!.addView(v)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {

        if (intent.getSerializableExtra("biotopedata") == null){
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dataList: Array<String> = arrayOf("*");

                        val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        var dataArray:ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

                        while (data2.moveToNext()) {

                            var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                                    data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                                    , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                                    , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                                    , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                                    , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                                    , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                                    , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53), data2.getString(54),data2.getString(55), data2.getString(56),data2.getString(57))

                            dataArray.add(biotope_attribute)

                        }

                        if (dataArray.size == 0 ){
                            var intent = Intent()
                            intent.putExtra("polygonid", polygonid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                val deletedir = path.listFiles()
                                println("deletedir.size ${deletedir.size}")
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }

                            }
                        }

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }else {
            finish()
        }

    }



}

