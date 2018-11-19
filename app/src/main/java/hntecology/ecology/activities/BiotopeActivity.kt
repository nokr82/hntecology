package hntecology.ecology.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
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

    //gps 관련
    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    val SET_DATA1 = 1;
    val SET_DATA2 = 2
    val SET_DATA3 = 3
    val SET_DATA4 = 4
    val SET_DATA5 = 5
    val SET_DATA6 = 6

    val BIOTOPE_BASE = 3000

    var keyId: String? = null;
    var pk: String? = null
    var gropid: ArrayList<Long>? = null
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

    var cameraPath:String? = null

    var finishFlag:Boolean = true
    //gps 다시 시작.

    //위치정보 객체
    var lm: LocationManager? = null
    //위치정보 장치 이름
    var provider: String? = null

    var page:Int? = null

    var dataArray:ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

    private var vegetationData:ArrayList<Vegetation> = ArrayList<Vegetation>()

    private var latlngs: ArrayList<LatLng> = ArrayList<LatLng>()

    var GPS_LAT : String = ""
    var GPS_LON : String = ""

    var lat:String = ""
    var log:String = ""



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biotope)

        this.context = this

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);
        buildGoogleApiClient();

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        addPicturesLL = findViewById(R.id.addPicturesLL)

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);
        //etinvesDatetimeTV 바뀜.
        //etinvesDatetimeTV.text = getTime()

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        var intent: Intent = getIntent();

        val num = dbManager.biotopesNextNum()

        tvINV_IndexTV.setText(num.toString())

        val userName = PrefUtils.getStringPreference(context, "name");
        tvINV_PERSONTV.setText(userName)

        etINV_DTTV.setText(Utils.todayStr())

        etINV_TMTV.setText(Utils.timeStr())

        if(intent.getStringExtra("latitude")!= null){
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            etGPS_LATTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            etGPS_LONTV.setText(log)
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

            tvINV_PERSONTV.setText(base.INV_PERSON)
            etINV_DTTV.setText(base.INV_DT)
            etINV_TMTV.setText(base.INV_TM)

            etGPS_LATTV.setText(base.GPS_LAT)
            etGPS_LONTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,tvINV_PERSONTV.text.toString(),etINV_DTTV.text.toString(),etINV_TMTV.text.toString())

            dbManager.insertbase(base)

        }


        etTRE_SPECET.setOnClickListener {
            val intent = Intent(this, DlgvegetationActivity::class.java)
            intent.putExtra("title", "교목층")
            intent.putExtra("table", "Vegetation")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA2);
        }

        etSTRE_SPECET.setOnClickListener {
            val intent = Intent(this, DlgvegetationActivity::class.java)
            intent.putExtra("title", "아교목층")
            intent.putExtra("table", "Vegetation")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA3);
        }

        etSHR_SPECET.setOnClickListener {
            val intent = Intent(this, DlgvegetationActivity::class.java)
            intent.putExtra("title", "관목층")
            intent.putExtra("table", "Vegetation")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA4);
        }

        etHER_SPECET.setOnClickListener {
            val intent = Intent(this, DlgvegetationActivity::class.java)
            intent.putExtra("title", "초본층")
            intent.putExtra("table", "Vegetation")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA5);
        }

        if (intent.getStringExtra("id") != null) {

                tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
                etINV_DTTV.setText(getTime());
                etINV_TMTV.setText(createId())


            val dataList: Array<String> = arrayOf("*");

            var data= db.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            val data2 = db.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data2.moveToNext()) {

                chkdata = true
                var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                        data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                        , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                        , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                        , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                        , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                        , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                        , data2.getDouble(50), data2.getString(51), data2.getString(52),data2.getString(53))


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


                if(biotope_attribute.PIC_FOLDER == "null" || biotope_attribute.PIC_FOLDER == "" || biotope_attribute.PIC_FOLDER ==null){

                    tvPIC_FOLDERTV.visibility = View.GONE;

                }else{

                    val file =   File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/biotope/"+biotope_attribute.PIC_FOLDER)

                    val fileList  = file.listFiles()

                    for(i in 0.. fileList.size-1){

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


                val id = biotope_attribute.id

                if(biotope_attribute.TEMP_YN.equals("N")){
                    dbManager.deletebiotope_attribute(biotope_attribute,id)
                }

                if(biotope_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(biotope_attribute)
                }
            }

            leftLL.setOnClickListener {
                while (data2.moveToNext()) {

                    if(dataArray != null){
                        dataArray.clear()
                    }

                    chkdata = true
                    var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                            data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                            , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                            , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                            , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                            , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                            , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                            , data2.getDouble(50), data2.getString(51), data2.getString(52),data2.getString(53))

                    dataArray.add(biotope_attribute)
                }

                if(page == dataArray.size && page!! > 1 ){
                    page = page!!-1
                    pageTV.text = page.toString() + " / " + dataArray.size

                    clear()

                    resetPage(page!!)

                    println("page : $page")

                }else if (page!! < dataArray.size && page!! > 1){

                    page = page!!-1
                    pageTV.text = page.toString() + " / " + dataArray.size

                    clear()

                    resetPage(page!!)

                }



            }

            rightLL.setOnClickListener {
                clear()
                getGps()
                while (data2.moveToNext()) {


                    tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
                    etINV_DTTV.setText(getTime());
                    etINV_TMTV.setText(createId())


                    if(dataArray != null){
                        dataArray.clear()
                    }

                    chkdata = true
                    var biotope_attribute: Biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                            data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                            , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                            , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                            , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                            , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                            , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                            , data2.getDouble(50), data2.getString(51), data2.getString(52),data2.getString(53))

                    dataArray.add(biotope_attribute)
                }


                val biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                        , null, null, null, null, null, null, null, null
                        , null, null, null, null, null, null, null, null
                        , null, null, null, null, null, null, null, null
                        , null, null, null, null, null, null, null
                        , null, null, null, null, null, null, null, null
                        , null, null, null, null, null, null,null,null)

                biotope_attribute.id = keyId + page.toString()

                println("id-====right${biotope_attribute.id}")

                biotope_attribute.GROP_ID = keyId

                biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();

                println("etINV_REGIONET : " )

                biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
//            biotope_attribute.INVES_DATETIME        =   etinvesDatetimeTV.text.toString()

                biotope_attribute.INV_DT = etINV_DTTV.text.toString();
                biotope_attribute.INV_TM = etINV_TMTV.text.toString();

                if (tvINV_IndexTV.text.isNotEmpty()) {

                    biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()
                }

                biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()

                if (etLU_TY_RATEET.text.isNotEmpty()) {

                    biotope_attribute.LU_TY_RATE = Utils.getString(etLU_TY_RATEET).toFloat();
                }
                if (etSTAND_HET.text.isNotEmpty()) {

                    biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
                }


                biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
                biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()

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


//            biotope_attribute.PIC_FOLDER        =   etPIC_FOLDERET.text.toString()
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

                biotope_attribute.PIC_FOLDER = tvPIC_FOLDERTV.text.toString()

                if (chkdata) {

//                        if(images!!.size > 0 && biotope_attribute.PIC_FOLDER == null){
//
//                            biotope_attribute.PIC_FOLDER = getAttrubuteKey()
//                        }
//
//                        dbManager.updatebiotope_attribute(biotope_attribute)

                } else {

                }

                if (images!!.size > 0){

                    biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                }

                if (etGPS_LATTV.text.toString() !="" && etGPS_LONTV.text.toString() !=""){
                    biotope_attribute.GPS_LAT = lat.toDouble()
                    biotope_attribute.GPS_LON = log.toDouble()
                }

                biotope_attribute.TEMP_YN = "N"

                if(page == dataArray.size) {
                    dbManager.insertbiotope_attribute(biotope_attribute);
                    page = page!! + 1
                    println("biotope_attribute ====== ${biotope_attribute.id}")
                }

                val data2 = db.query("biotopeAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                if(dataArray != null){
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
                            , data2.getDouble(50), data2.getString(51), data2.getString(52),data2.getString(53))


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



                    if(biotope_attribute.PIC_FOLDER == "null" || biotope_attribute.PIC_FOLDER == "" || biotope_attribute.PIC_FOLDER ==null){

                        tvPIC_FOLDERTV.visibility = View.GONE;

                    }else {

                        val file = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/biotope/" + biotope_attribute.PIC_FOLDER)

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


                    dataArray.add(biotope_attribute)
                }

                if(page!! < dataArray.size) {
                    page = page!! + 1
                }

                    pageTV.text = page.toString() + " / " + dataArray.size

                resetPage(page!!)

                println("page : $page")

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
            intent.putExtra("table","biotopeClass")
            intent.putExtra("DlgHeight", 600f);

            startActivityForResult(intent, SET_DATA6);

        }

        btn_biotopCancle1.setOnClickListener {
            finish()
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
                                , null, null, null, null, null, null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

                        biotope_attribute.GROP_ID = keyId

                        biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();
                        biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
//            biotope_attribute.INVES_DATETIME        =   etinvesDatetimeTV.text.toString()

                        if(etINV_DTTV.text == null){
                            biotope_attribute.INV_DT = Utils.todayStr()
                        }else {
                            biotope_attribute.INV_DT = etINV_DTTV.text.toString();
                        }

                        if(etINV_TMTV.text == null){
                            biotope_attribute.INV_TM = Utils.timeStr()
                        }else {
                            biotope_attribute.INV_TM = etINV_TMTV.text.toString();
                        }

                        if (tvINV_IndexTV.text.isNotEmpty()) {

                            biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()
                        }

                        biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()

                        if (etLU_TY_RATEET.text.isNotEmpty()) {

                            biotope_attribute.LU_TY_RATE = Utils.getString(etLU_TY_RATEET).toFloat();
                        }
                        if (etSTAND_HET.text.isNotEmpty()) {

                            biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
                        }


                        biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
                        biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()

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


//            biotope_attribute.PIC_FOLDER        =   etPIC_FOLDERET.text.toString()
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

                        biotope_attribute.PIC_FOLDER = tvPIC_FOLDERTV.text.toString()

                        if(etGPS_LATTV.text.toString() !="" && etGPS_LONTV.text.toString() !=""){

                            biotope_attribute.GPS_LAT = lat.toDouble()
                            biotope_attribute.GPS_LON = log.toDouble()

                        }

                        biotope_attribute.TEMP_YN = "Y"

                        if (chkdata) {

                            if(images!!.size > 0 && biotope_attribute.PIC_FOLDER == null){

                                biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                            }

                            if(pk != null){
                                dbManager.updatebiotope_attribute(biotope_attribute,pk)
                            }



                        } else {

                            if(images!!.size > 0){

                                biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                            }

                            dbManager.insertbiotope_attribute(biotope_attribute);

                        }

                        var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                        sdPath += "/biotope"

                        val biotope = File(sdPath)
                        biotope.mkdir();
//                        sdPath +="/imgs"
                        sdPath +="/"+biotope_attribute.PIC_FOLDER

                        val file = File(sdPath)
                        file.mkdir();
                        //이미 있다면 삭제. 후 생성
                        setDirEmpty(sdPath)

                        sdPath+="/"

                        var pathArray:ArrayList<String> = ArrayList<String>()

                        for(i   in 0..images!!.size-1){

                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "/biotope/imgs" + File.separator
                            val outputsDir = File(outPath)

                            if (outputsDir.exists()) {
                                println("Exit : $outPath")

                                val files = outputsDir.listFiles()
                                if (files != null) {
                                    for (i in files.indices) {
                                        println("f : " + files[i])
                                    }
                                }

                            } else {
                                val made = outputsDir.mkdirs()

                                println("made : $made")
                            }

                            saveVitmapToFile(images!!.get(i),outPath+i+".png")

                        }

                        intent.putExtra("bio_attri", biotope_attribute);

                        setResult(RESULT_OK, intent);
                        finishFlag = false

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
                        var intent = Intent();
                        val biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null,null,null)

                        if(pk != null){
                            dbManager.deletebiotope_attribute(biotope_attribute,pk)

                            intent.putExtra("bio_attri", biotope_attribute);

                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

                            sdPath +="/biotope/"+biotope_attribute.PIC_FOLDER

                            //이미 있다면 삭제. 후 생성
                            setDirEmpty(sdPath)

                            setResult(RESULT_OK, intent);

                            finish()
                        }else {
                            Toast.makeText(context, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
                        }

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

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
                    , null, null, null, null, null, null,null,null)

            keyId = intent.getStringExtra("GROP_ID")

            biotope_attribute.GROP_ID = keyId

            biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();
            biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
//            biotope_attribute.INVES_DATETIME        =   etinvesDatetimeTV.text.toString()

            if(etINV_DTTV.text == null){
                biotope_attribute.INV_DT = Utils.todayStr()
            }else {
                biotope_attribute.INV_DT = etINV_DTTV.text.toString();
            }

            if(etINV_TMTV.text == null){
                biotope_attribute.INV_TM = Utils.timeStr()
            }else {
                biotope_attribute.INV_TM = etINV_TMTV.text.toString();
            }

            if (tvINV_IndexTV.text.isNotEmpty()) {

                biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()
            }

            biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()

            if (etLU_TY_RATEET.text.isNotEmpty()) {

                biotope_attribute.LU_TY_RATE = Utils.getString(etLU_TY_RATEET).toFloat();
            }
            if (etSTAND_HET.text.isNotEmpty()) {

                biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
            }


            biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
            biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()

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

//            biotope_attribute.PIC_FOLDER        =   etPIC_FOLDERET.text.toString()
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

            biotope_attribute.PIC_FOLDER = tvPIC_FOLDERTV.text.toString()

            if(etGPS_LATTV.text.toString() !="" && etGPS_LONTV.text.toString() !=""){

                biotope_attribute.GPS_LAT = lat.toDouble()
                biotope_attribute.GPS_LON = log.toDouble()

            }

            biotope_attribute.TEMP_YN = "Y"

            if (chkdata) {

                if(images!!.size > 0 && biotope_attribute.PIC_FOLDER == null){

                    biotope_attribute.PIC_FOLDER = getAttrubuteKey()

                }

                if(pk != null){
                    dbManager.updatebiotope_attribute(biotope_attribute,pk)
                }

            } else {

                if(images!!.size > 0){

                    biotope_attribute.PIC_FOLDER = getAttrubuteKey()
                }

                dbManager.insertbiotope_attribute(biotope_attribute);

            }

            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            sdPath += "/biotope"

            val biotope = File(sdPath)
            biotope.mkdir();
            sdPath +="/"+biotope_attribute.PIC_FOLDER

            val file = File(sdPath)
            file.mkdir();
            //이미 있다면 삭제. 후 생성
            setDirEmpty(sdPath)

            sdPath+="/"


            var pathArray:ArrayList<String> = ArrayList<String>()

            for(i   in 0..images!!.size-1){

                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "/biotope/imgs" + File.separator
                val outputsDir = File(outPath)

                if (outputsDir.exists()) {
                    println("Exit : $outPath")

                    val files = outputsDir.listFiles()
                    if (files != null) {
                        for (i in files.indices) {
                            println("f : " + files[i])
                        }
                    }

                } else {
                    val made = outputsDir.mkdirs()

                    println("made : $made")
                }

                saveVitmapToFile(images!!.get(i),sdPath+i+".png")

            }

            intent.putExtra("bio_attri", biotope_attribute);

            setResult(RESULT_OK, intent);
            finishFlag = false

            clear()
            chkdata = false
            pk = null

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

        var vegetationData : Vegetation

        var biotopeClass: BiotopeClass

        var vegetation: Vegetation

        var number: Number

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_DATA1 -> {

                    biotopeModel = data!!.getSerializableExtra("bioModel") as BiotopeModel

                    //토지이용현황
                    if (biotopeModel.codeType == "biotopeM") {

                        TVLU_GR_NumTV.setText(biotopeModel.name)
                        ETLU_GR_NumET.setText(biotopeModel.code)
                        //토지피복현황
                    } else if (biotopeModel.codeType == "biotopeS") {

                        TVLC_GR_NUMTV.setText(biotopeModel.name)
                        ETlcmGR_NumET.setText(biotopeModel.code)


                        var bioModelParent: BiotopeModel
                        bioModelParent = data!!.getSerializableExtra("bioModelParent") as BiotopeModel

                        //불투수 투수
                        etlcmTypeiET.setText("");
                        etlcmTypepET.setText("")
                        etlcmTypegET.setText("")
                        etlcmTypewET.setText("")

                        //불투수
                        if (bioModelParent.code == "A") {

                            etlcmTypeiET.setText("I")
                            //투수
                        } else if (bioModelParent.code == "B") {

                            etlcmTypepET.setText("P")
                            //녹지
                        } else if (bioModelParent.code == "C") {

                            etlcmTypegET.setText("G")
                            //수공간
                        } else if (bioModelParent.code == "D") {

                            etlcmTypewET.setText("W")
                        }

                        //현존식생현황  아직 테이블 명 코드 미정
                    } else if (biotopeModel.codeType == "biotopeS") {

                        TVTY_MARKTV.setText(biotopeModel.name)
                        TVTY_MARKTV.setText(biotopeModel.code)
                    }

                }

                SET_DATA2 -> {

                    vegetationData =data!!.getSerializableExtra("veData") as Vegetation

                    etTRE_SPECET.setText(vegetationData.CATEGORY)
                    etTRE_FAMIET.setText(vegetationData.SIGN)
                    etTRE_SCIENET.setText(vegetationData.CORRESPONDINGNAME)

                }

                SET_DATA3 -> {

                    vegetationData =data!!.getSerializableExtra("veData") as Vegetation

                    etSTRE_SPECET.setText(vegetationData.CATEGORY)
                    etSTRE_FAMIET.setText(vegetationData.SIGN)
                    etSTRE_SCIENET.setText(vegetationData.CORRESPONDINGNAME)

                }

                SET_DATA4 -> {

                    vegetationData =data!!.getSerializableExtra("veData") as Vegetation

                    etSHR_SPECET.setText(vegetationData.CATEGORY)
                    etSHR_FAMIET.setText(vegetationData.SIGN)
                    etSHR_SCIENET.setText(vegetationData.CORRESPONDINGNAME)

                }

                SET_DATA5 -> {

                    vegetationData =data!!.getSerializableExtra("veData") as Vegetation

                    etHER_SPECET.setText(vegetationData.CATEGORY)
                    etHER_FAMIET.setText(vegetationData.SIGN)
                    etHER_SCIENET.setText(vegetationData.CORRESPONDINGNAME)

                }

                SET_DATA6 -> {

                    if(data!!.getSerializableExtra("biotopeClass") != null) {
                        biotopeClass = data!!.getSerializableExtra("biotopeClass") as BiotopeClass

                        println("biotopeSize ${biotopeClass.sign}")

                        TVTY_MARKTV.setText(biotopeClass.sign)

                    }

                    if(data!!.getSerializableExtra("Vegetation") != null) {
                        vegetation = data!!.getSerializableExtra("Vegetation") as Vegetation
                        if(data!!.getSerializableExtra("Number") != null) {
                            number = data!!.getSerializableExtra("Number") as Number
                            TVTY_MARKTV.setText(vegetation.SIGN + number.COUNT)
                        }

                    }


                }

                BIOTOPE_BASE ->{


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
        var out:OutputStream? =null
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



    fun clear(){

        etINV_DTTV.setText(Utils.todayStr());
        etINV_TMTV.setText(createId())
        tvINV_IndexTV.setText("")

        TVLU_GR_NumTV.setText("")
        etLU_TY_RATEET.setText("")
        etSTAND_HET.setText("")
        TVLC_GR_NUMTV.setText("")
        ETLU_GR_NumET.setText("")
        ETlcmGR_NumET.setText("")

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
    }

    fun resetPage(page : Int){


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
                    , data.getDouble(50), data.getString(51), data.getString(52),data.getString(53))

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



            if(biotope_attribute.PIC_FOLDER == "null" || biotope_attribute.PIC_FOLDER == "" || biotope_attribute.PIC_FOLDER ==null){

                tvPIC_FOLDERTV.visibility = View.GONE;

            }else{

                val file =   File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/biotope/"+biotope_attribute.PIC_FOLDER)

                val fileList  = file.listFiles()

                for(i in 0.. fileList.size-1){

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
}

