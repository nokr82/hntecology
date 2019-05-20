package hntecology.ecology.activities

import android.Manifest
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.FileFilter
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_birds_ex.*
import kotlinx.android.synthetic.main.activity_birds_ex.gpslatTV
import kotlinx.android.synthetic.main.activity_birds_ex.gpslonTV
import kotlinx.android.synthetic.main.activity_flora2.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Math.abs
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BirdsActivity : Activity(), OnLocationUpdatedListener {


    lateinit var context: Context;

    val SET_DATA1 = 1;
    val SET_DATA2 = 2;
    val SET_DATA3 = 3;
    val SET_BIRDS = 4;
    val SET_DATA4 = 5;
    val SET_STANDARD = 6;

    var userName = "";

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var chkdata: Boolean = false;

    var latitude = 0.0f;
    var longitude = 0.0f;

    var lat: String = ""
    var log: String = ""

    private var progressDialog: ProgressDialog? = null

    var type = "write";

    var keyId: String? = null;

    var pk: String? = null

    var page: Int? = null

    var dataArray: ArrayList<Birds_attribute> = ArrayList<Birds_attribute>()

    var basechkdata = false

    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101

    var cameraPath: String? = null

    private var addPicturesLL: LinearLayout? = null
    private val imgSeq = 0

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    var markerid: String? = null

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var imageUri: Uri? = null

    var invtm = ""

    var albumcount = 1
    val SET_FLORA = 2005
    val SET_FLORA2 = 2006
    val SET_INPUT = 2007

    var prjname = ""

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birds_ex)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
//        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        var today = Utils.todayStr();
        var time = Utils.timeStr();

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        invDtTV.text = today;
        timeTV.text = time;

        var todays = today.split("-")
        var timesplit = time.split(":")

        var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

        for (i in 1 until todays.size) {
            texttoday += todays.get(i)
        }

        invtm = timesplit.get(0) + timesplit.get(1)

        invDtTV.setOnClickListener {
            datedlg()
        }

        timeTV.setOnClickListener {
            timedlg()
        }

        userName = PrefUtils.getStringPreference(context, "name");
        invPersonTV.text = userName;

        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        dbManager = DataBaseHelper(this)

        val datalist_s: Array<String> = arrayOf("*");
//        var c = db!!.rawQuery("SELECT max(NUM) FROM birdsAttribute", null);
//        var data_s= db!!.query("birdsAttribute", datalist_s, null, null, null, null, "", null)

/*
   */


        db = dbManager!!.createDataBase();

        var c = dbManager!!.pkNum("birdsAttribute")
        Log.d("데이터", c.toString())
        numTV.text = c.toString()


        var intent: Intent = getIntent();

        if (intent.getStringExtra("markerid") != null) {
            markerid = intent.getStringExtra("markerid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")

            gpslatTV.setText(lat)
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            gpslonTV.setText(log)
        }

        keyId = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        if (intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            var geocoder: Geocoder = Geocoder(context);

            var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

            if (list.size > 0) {

//                invRegionET.setText(list.get(0).getAddressLine(0));
                INV_REGION = list.get(0).getAddressLine(0)
            }
            convert(lat.toDouble())
            logconvert(log.toDouble())

        }


        useTarSpET.setOnClickListener {
            val intent = Intent(context, DlgFloraActivity::class.java)
            if (useTarSpET.text != null && useTarSpET.text != "") {
                var SPEC = useTarSpET.text.toString()
                intent.putExtra("SPEC", SPEC)
            }
            startActivityForResult(intent, SET_FLORA);
        }


        val dataList: Array<String> = arrayOf("*");

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            invPersonTV.setText(base.INV_PERSON)
            invDtTV.setText(base.INV_DT)
            timeTV.setText(base.INV_TM)

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            var geocoder: Geocoder = Geocoder(context);

            var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

            if (list.size > 0) {
//                invRegionET.setText(list.get(0).getAddressLine(0));

                INV_REGION = list.get(0).getAddressLine(0)
            }

            basedata.close()

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, invPersonTV.text.toString(), invDtTV.text.toString(), timeTV.text.toString())

            dbManager!!.insertbase(base)

        }

        if (intent.getSerializableExtra("GROP_ID") != null) {
            keyId = intent.getStringExtra("GROP_ID")

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var birds_attribute = ps_birds_attribute(data)

                dataArray.add(birds_attribute)

            }

            data.close()
        }

        if (intent.getStringExtra("id") != null) {

            delBtn.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("birdsAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {

                chkdata = true

                var birds_attribute = ps_birds_attribute(data)
                invPersonTV.setText(birds_attribute.INV_PERSON)

                invRegionET.setText(birds_attribute.INV_REGION)
                INV_REGION = birds_attribute.INV_REGION.toString()

                btn1.setText(birds_attribute.WEATHER)       //날씨
                btn2.setText(birds_attribute.WIND)          //바람
                btn3.setText(birds_attribute.WIND_DIRE)     //풍향


                temperatureET.setText(birds_attribute.TEMPERATUR.toString())       //기온

                etcET.setText(birds_attribute.ETC)

                numTV.setText(birds_attribute.NUM.toString())

                prjnameET.setText(birds_attribute.PRJ_NAME)

                coordndET.setText(birds_attribute.GPSLAT_DEG.toString())
                coordnmET.setText(birds_attribute.GPSLAT_MIN.toString())
                coordnsET.setText(birds_attribute.GPSLAT_SEC.toString())
                coordedET.setText(birds_attribute.GPSLON_DEG.toString())
                coordemET.setText(birds_attribute.GPSLON_MIN.toString())
                coordesET.setText(birds_attribute.GPSLON_SEC.toString())

                birdsTV.setText(birds_attribute.SPEC_NM)
                familyNameTV.setText(birds_attribute.FAMI_NM)
                zoologicalTV.setText(birds_attribute.SCIEN_NM)
                endangeredTV.setText(birds_attribute.ENDANGERED)
                indicntET.setText(birds_attribute.INDI_CNT.toString())

                timeTV.setText(birds_attribute.INV_TM)
                var timesplit = birds_attribute.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)

                obsstatTV.setText(birds_attribute.OBS_STAT)
                useTarTV.setText(birds_attribute.USE_TAR)
//                useTarSpET.setText(birds_attribute.USE_TAR_SP)
                /*if(birds_attribute.USE_TAR_SP == null || birds_attribute.USE_TAR_SP.equals("null")){
                    useTarSpET.setText("")
                    useTarSpLL.visibility = View.GONE
                }else{
                    useTarSpLL.visibility = View.VISIBLE
                }*/



                useLayerTV.setText(birds_attribute.USE_LAYER)
                mjActTV.setText(birds_attribute.MJ_ACT)
//                mjActPrET.setText(birds_attribute.MJ_ACT_PR)
                /* if(birds_attribute.MJ_ACT_PR == null || birds_attribute.MJ_ACT_PR.equals("")){
                     mjActPrET.setText("")
                     mjActPrLL.visibility = View.GONE
                 }*/

                /*      if(birds_attribute.MJ_ACT_PR != null && !birds_attribute.MJ_ACT_PR.equals("")){
                          mjActPrLL.visibility = View.VISIBLE
                      }*/

                if (birds_attribute.TEMP_YN.equals("N")) {
                    dbManager!!.deletebirds_attribute(birds_attribute, pk)
                }

                if (birds_attribute.TEMP_YN.equals("Y")) {
                    dataArray.add(birds_attribute)
                }

                confmodTV.setText(birds_attribute.CONF_MOD)
                standardTV.setText(birds_attribute.STANDARD)

//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + birds_attribute.INV_DT + "." + birds_attribute.INV_TM + "."+birds_attribute.NUM+ "/imges")
//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/" + birds_attribute.INV_DT + "_" + birds_attribute.INV_TM + "_"+birds_attribute.NUM)

//                val fileList = file.listFiles()
                val pk = birds_attribute.id
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
                var tmpfileList = tmpfiles.listFiles()

//                if (fileList != null) {
//                    for (i in 0..fileList.size - 1) {
//                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/imges/"
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
//                        val num = birds_attribute.NUM.toString()
//                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/imges" ,   num + "_" + birds_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                        if(tmpfile.exists()){
//                            tmpfile.renameTo(tmpfile2)
//                        }
//
//                        tmpfileList = tmpfiles.listFiles()
//
//
//                    }
//                }

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

                        for (j in 0..tmpfileList.size - 1) {
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "birds/images" + File.separator + keyId, (j + 1).toString()))) {
//                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/" +birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM +"_" + (j+1) + ".png")) {
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

                data.close()

//                if (file.isDirectory){
//                    val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + birds_attribute.INV_DT + "." + birds_attribute.INV_TM + "."+birds_attribute.NUM)
//                    path.deleteRecursively()
//                }

            }


        }

        resetBT.setOnClickListener {
            obsstatTV.visibility = View.VISIBLE
            obsstatLL.visibility = View.GONE
            obsstatTV.setText("")
        }

        birdsresetBT.setOnClickListener {
            birdsET.setText("")
            birdsLL.visibility = View.GONE
            birdsTV.visibility = View.VISIBLE
        }

        standardTV.setOnClickListener {
            val intent = Intent(context, DlgStandardActivity::class.java)
            intent.putExtra("type", "birds")

            startActivityForResult(intent, SET_STANDARD);
        }

        cancelBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dbManager: DataBaseHelper = DataBaseHelper(this)

                        val db = dbManager.createDataBase()

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var birds_attribute = ps_birds_attribute(data)

                            dataArray.add(birds_attribute)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
                            val pathdir = path.listFiles()

                            if (pathdir != null) {

                                val deletedir = path.listFiles()

                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }

                            }
                        }
                        data.close()

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        birdssaveBtn.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var birds_attribute = null_birds_attribute()
                        keyId = intent.getStringExtra("GROP_ID")

                        birds_attribute.GROP_ID = keyId

                        val prj = prjnameET.text.toString()
                        if (prj == prjname) {
                            birds_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            birds_attribute.PRJ_NAME = prjnameET.text.toString()
                        }

//                        birds_attribute.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            birds_attribute.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            birds_attribute.PRJ_NAME = prjname
//                        }

//                        birds_attribute.INV_REGION = invRegionET.text.toString()
                        if (invRegionET.length() > 0) {
                            birds_attribute.INV_REGION = invRegionET.text.toString();
                        } else {
                            birds_attribute.INV_REGION = INV_REGION
                        }

                        birds_attribute.INV_DT = invDtTV.text.toString()

                        birds_attribute.NUM = numTV.text.toString().toInt()

                        if (invPersonTV.text == null) {
                            birds_attribute.INV_PERSON = userName
                        } else {
                            birds_attribute.INV_PERSON = invPersonTV.text.toString()
                        }

                        birds_attribute.WEATHER = btn1.text.toString()

                        birds_attribute.WIND = btn2.text.toString()

                        birds_attribute.WIND_DIRE = btn3.text.toString()

                        if (temperatureET.text.isNotEmpty()) {
                            birds_attribute.TEMPERATUR = Utils.getString(temperatureET).toFloat();
                        }

                        birds_attribute.ETC = etcET.text.toString()

                        birds_attribute.INV_TM = timeTV.text.toString()

                        birds_attribute.SPEC_NM = birdsTV.text.toString()
                        var birdsET = birdsET.text.toString()
                        if (birdsET != "" && birdsET != null) {
                            birds_attribute.SPEC_NM = birdsET
                        }

                        birds_attribute.FAMI_NM = familyNameTV.text.toString()

                        birds_attribute.SCIEN_NM = zoologicalTV.text.toString()

                        birds_attribute.ENDANGERED = endangeredTV.text.toString()

                        if (indicntET.text.isNotEmpty()) {
                            birds_attribute.INDI_CNT = indicntET.text.toString().toInt()
                        }

                        birds_attribute.OBS_STAT = obsstatTV.text.toString()

                        birds_attribute.USE_TAR = useTarTV.text.toString()
                        if (useTarSpET.text != "") {
//                            birds_attribute.USE_TAR_SP = useTarSpET.text.toString()
                            birds_attribute.USE_TAR = useTarSpET.text.toString()
                        }

                        birds_attribute.USE_LAYER = useLayerTV.text.toString()

                        birds_attribute.MJ_ACT = mjActTV.text.toString()
//                        birds_attribute.MJ_ACT_PR = mjActPrET.text.toString()
                        birds_attribute.STANDARD = standardTV.text.toString()


                        /*     if (gpslatTV.text.isNotEmpty()) {
                                 Log.d("방위",gpslatTV.text.toString())
                                 birds_attribute.GPS_LAT = gpslatTV.text.toString().toFloat()
                             }*/
                        birds_attribute.GPS_LAT = lat.toFloat()
                        birds_attribute.GPS_LON = log.toFloat()
                        Log.d("방위", lat)
                        Log.d("방위", log)
                        /*    if (gpslonTV.text.isNotEmpty()) {
                                birds_attribute.GPS_LON = gpslonTV.text.toString().toFloat()
                            }
    */
                        birds_attribute.TEMP_YN = "Y"

                        birds_attribute.CONF_MOD = "N"

                        birds_attribute.GEOM = log + " " + lat

                        if (coordndET.text.isNotEmpty()) {
                            birds_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                        }
                        if (coordnmET.text.isNotEmpty()) {
                            birds_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                        }
                        if (coordnsET.text.isNotEmpty()) {
                            birds_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            birds_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
                        }
                        if (coordemET.text.isNotEmpty()) {
                            birds_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                        }
                        if (coordesET.text.isNotEmpty()) {
                            birds_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                        }

                        if (chkdata) {

                            if (pk != null) {

                                val CONF_MOD = confmodTV.text.toString()

                                if (CONF_MOD == "C" || CONF_MOD == "N") {
                                    birds_attribute.CONF_MOD = "M"
                                }

                                dbManager!!.updatebirds_attribute(birds_attribute, pk)
                                dbManager!!.updatecommonbirds(birds_attribute, keyId)
                            }

//                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images"+ File.separator +keyId+ File.separator)
////                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images/")
//                            val pathdir = path.listFiles()
//
//                            if(pathdir != null) {
//                                for (i in 0..pathdir.size-1) {
//
//                                    for(j in 0..pathdir.size-1) {
//                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "birds/images"+ File.separator +keyId+ File.separator + birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM +"_" + (j+1) + ".png")) {
////                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/" + birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM +"_" + (j+1) + ".png")) {
//
//                                            pathdir.get(i).canonicalFile.delete()
//
//                                        }
//                                    }
//
//                                }
//                            }
//
//                            for(i   in 0..images!!.size-1){
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/"
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images"+ File.separator +keyId+ File.separator
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
//                                saveVitmapToFile(images!!.get(i),outPath+birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                            }
//
//                            val deletedir = path.listFiles()
//                            if (deletedir.size == 0){
//                                if (path.isDirectory){
//                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images"+ File.separator +keyId+ File.separator)
//                                    deletepath.deleteRecursively()
//                                }
//                            }

                        } else {

                            dbManager!!.insertbirds_attribute(birds_attribute);

//                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            sdPath += "/ecology/data/birds/images/" + birds_attribute.INV_DT +"_"+ birds_attribute.INV_TM + "_"+birds_attribute.NUM
//                            val birds = File(sdPath)
//                            birds.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                            val file = File(sdPath)
//                            file.mkdir();
//                            //이미 있다면 삭제. 후 생성
//                            setDirEmpty(sdPath)
//
//                            sdPath+="/"

//                            var pathArray:ArrayList<String> = ArrayList<String>()
//
//                            for(i   in 0..images!!.size-1){
//
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/"
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + birds_attribute.INV_DT +"_"+ birds_attribute.INV_TM + "_"+birds_attribute.NUM
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
//                                saveVitmapToFile(images!!.get(i),outPath+birds_attribute.NUM + "_" + birds_attribute.INV_TM+"_"+(i+1)+".png")
//
//                            }

                        }

                        var intent = Intent()
                        intent.putExtra("export", 70)
                        setResult(RESULT_OK, intent)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        delBtn.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            val intent = getIntent()

                            dialog.cancel()

                            var birds_attribute = null_birds_attribute()
                            if (pk != null) {

                                val data = db!!.query("birdsAttribute", dataList, "id = '$pk'", null, null, null, "", null)

                                while (data.moveToNext()) {
                                    birds_attribute = ps_birds_attribute(data)

                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator + birds_attribute.NUM.toString() + "_" + invtm + "_" + (j + 1) + ".png")) {
//                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/" + birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM +"_" + (j+1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                            }
                                        }

                                    }

                                    val deletedir = path.listFiles()
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }

                                }

                                if (intent.getSerializableExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("birdsAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var birds_attribute = ps_birds_attribute(data)
                                        dataArray.add(birds_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {

                                        dbManager!!.deletebirds_attribute(birds_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {

                                        var intent = Intent()

                                        intent.putExtra("markerid", markerid)

                                        dbManager!!.deletebirds_attribute(birds_attribute, pk)

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

            if (pk == null) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            if (intent.getSerializableExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("birdsAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true


                                }

                                if (chkdata == true) {
                                    Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    var intent = Intent()
                                    intent.putExtra("markerid", markerid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }
                                data.close()

                            }

                            if (intent.getSerializableExtra("id") == null) {
                                var intent = Intent()
                                intent.putExtra("markerid", markerid)

                                setResult(RESULT_OK, intent);
                                finish()
                            }


                            dialog.cancel()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }

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

            //            val intent = Intent(context, DlgStandardActivity::class.java)
//            intent.putExtra("type", "birds")
//
//            startActivityForResult(intent, SET_STANDARD);

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
            listItems.add("수종명");
            listItems.add("흙");
            listItems.add("물");
            listItems.add("인공물");
            listItems.add("취소");

            alert(listItems, "이용 대상 선택", useTarTV, "useTar");

        }

        nextTV.setOnClickListener {

            var birds_attribute = null_birds_attribute()
            keyId = intent.getStringExtra("GROP_ID")

            birds_attribute.GROP_ID = keyId

            val prj = prjnameET.text.toString()
            if (prj == prjname) {
                birds_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                birds_attribute.PRJ_NAME = prjnameET.text.toString()
            }

//            birds_attribute.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                birds_attribute.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                birds_attribute.PRJ_NAME = prjname
//            }

//            birds_attribute.INV_REGION = invRegionET.text.toString()
            if (invRegionET.length() > 0) {
                birds_attribute.INV_REGION = invRegionET.text.toString();
            } else {
                birds_attribute.INV_REGION = INV_REGION
            }
            birds_attribute.INV_DT = invDtTV.text.toString()

            birds_attribute.NUM = numTV.text.toString().toInt()

            if (invPersonTV.text == null) {
                birds_attribute.INV_PERSON = userName
            } else {
                birds_attribute.INV_PERSON = invPersonTV.text.toString()
            }

            birds_attribute.WEATHER = btn1.text.toString()

            birds_attribute.WIND = btn2.text.toString()

            birds_attribute.WIND_DIRE = btn3.text.toString()

            if (temperatureET.text.isNotEmpty()) {
                Utils.getString(temperatureET).toFloat();
            }

            birds_attribute.ETC = etcET.text.toString()

            birds_attribute.INV_TM = timeTV.text.toString()

            birds_attribute.SPEC_NM = birdsTV.text.toString()
            var birdsET = birdsET.text.toString()
            if (birdsET != "" && birdsET != null) {
                birds_attribute.SPEC_NM = birdsET
            }

            birds_attribute.FAMI_NM = familyNameTV.text.toString()

            birds_attribute.SCIEN_NM = zoologicalTV.text.toString()

            birds_attribute.ENDANGERED = endangeredTV.text.toString()

            if (indicntET.text.isNotEmpty()) {
                birds_attribute.INDI_CNT = indicntET.text.toString().toInt()
            }

            birds_attribute.OBS_STAT = obsstatTV.text.toString()

            birds_attribute.USE_TAR = useTarTV.text.toString()
            if (useTarSpET.text != "") {
//                            birds_attribute.USE_TAR_SP = useTarSpET.text.toString()
                birds_attribute.USE_TAR = useTarSpET.text.toString()
            }

            birds_attribute.USE_LAYER = useLayerTV.text.toString()

            birds_attribute.MJ_ACT = mjActTV.text.toString()
//            birds_attribute.MJ_ACT_PR = mjActPrET.text.toString()
            birds_attribute.STANDARD = standardTV.text.toString()

            birds_attribute.GPS_LAT = lat.toFloat()
            birds_attribute.GPS_LON = log.toFloat()


            /*   if (gpslatTV.text.isNotEmpty()) {
                   birds_attribute.GPS_LAT = lat.toFloat()
               }

               if (gpslonTV.text.isNotEmpty()) {
                   birds_attribute.GPS_LON = log.toFloat()
               }*/

            birds_attribute.TEMP_YN = "Y"

            birds_attribute.CONF_MOD = "N"

            birds_attribute.GEOM = log.toString() + " " + lat.toString()

            if (coordndET.text.isNotEmpty()) {
                birds_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                birds_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                birds_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                birds_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                birds_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                birds_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            if (chkdata) {

                if (pk != null) {

                    val CONF_MOD = confmodTV.text.toString()

                    if (CONF_MOD == "C" || CONF_MOD == "N") {
                        birds_attribute.CONF_MOD = "M"
                    }

                    dbManager!!.updatebirds_attribute(birds_attribute, pk)
                    dbManager!!.updatecommonbirds(birds_attribute, keyId)
                }
                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images/")
                val pathdir = path.listFiles()

                if (pathdir != null) {
                    for (i in 0..pathdir.size - 1) {

                        for (j in 0..pathdir.size - 1) {

                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator + birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM + "_" + (j + 1) + ".png")) {
//                                if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/birds/images/" + birds_attribute.NUM.toString() + "_" + birds_attribute.INV_TM +"_" + (j+1) + ".png")) {

                                pathdir.get(i).canonicalFile.delete()

                            }
                        }
                    }
                }

                for (i in 0..images!!.size - 1) {

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images/"
                    val outputsDir = File(outPath)

                    if (outputsDir.exists()) {

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
                    val date = Date()
                    val sdf = SimpleDateFormat("yyyyMMdd-HHmmSS")

                    val getTime = sdf.format(date)
                    var gettimes = getTime.split("-")

                    saveVitmapToFile(images!!.get(i), outPath + getTime.substring(2, 8) + "_" + gettimes[1] + "_" + (i + 1) + ".png")


                }

            } else {

                dbManager!!.insertbirds_attribute(birds_attribute);

//                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                sdPath += "/ecology/data/birds/images" + birds_attribute.INV_DT +"_"+ birds_attribute.INV_TM +"_"+birds_attribute.NUM
//                val birds = File(sdPath)
//                birds.mkdir();
////                          sdPath +="/imgs"
////                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                val file = File(sdPath)
//                file.mkdir();
//                //이미 있다면 삭제. 후 생성
//                setDirEmpty(sdPath)
//
//                sdPath+="/"

                var pathArray: ArrayList<String> = ArrayList<String>()

                for (i in 0..images!!.size - 1) {

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images/"
                    val outputsDir = File(outPath)

                    if (outputsDir.exists()) {

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
                    val date = Date()
                    val sdf = SimpleDateFormat("yyyyMMdd-HHmmSS")

                    val getTime = sdf.format(date)
                    var gettimes = getTime.split("-")

                    saveVitmapToFile(images!!.get(i), outPath + getTime.substring(2, 8) + "_" + gettimes[1] + "_" + (i + 1) + ".png")

                }

            }

            if (intent.getStringExtra("set") != null) {
                var intent = Intent()
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            delBtn.visibility = View.GONE

            var intent = Intent()
            intent.putExtra("export", 70)
            setResult(RESULT_OK, intent)

            if (images_path != null) {
                images_path!!.clear()
            }

            if (images != null) {
                images!!.clear()
            }

            if (images_url != null) {
                images_url!!.clear()
            }

            if (images_url_remove != null) {
                images_url_remove!!.clear()
            }

            if (images_id != null) {
                images_id!!.clear()
            }

            clear()
            chkdata = false
            pk = null

        }

        obsstatTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("도시화");
            listItems.add("산림");
            listItems.add("초지");
            listItems.add("논");
            listItems.add("밭");
            listItems.add("수역");
            listItems.add("공중");
            listItems.add("기타");
            listItems.add("취소");

            alert(listItems, "관찰지역현황", obsstatTV, "obsstat");


            /* val intent = Intent(this, DlgBridsClassActivity::class.java)
             intent.putExtra("title", "토지이용유형 분류")
             intent.putExtra("table", "Region")
             intent.putExtra("DlgHeight", 600f);
             startActivityForResult(intent, SET_DATA4);*/
        }

        btnbPIC_FOLDER.setOnClickListener {

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

    fun startDlgBirds() {
        val intent = Intent(context, DlgBirdsActivity::class.java)
        intent.putExtra("title", "조류 종명 선택")
        intent.putExtra("table", "birds")
        intent.putExtra("DlgHeight", 600f);
        if (birdsTV.text != null && birdsTV.text != "") {
            val spec = birdsTV.text.toString()
            intent.putExtra("SPEC", spec)
        }

        if (endangeredTV.text != null && endangeredTV.text != "") {
            val end = endangeredTV.text.toString()
            intent.putExtra("END", end)
        }
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
                if (selectItem == "수종명") {
                    useTarSpLL.visibility = View.VISIBLE
                } else {
                    useTarSpLL.visibility = View.GONE
                    useTarSpET.setText("");
                }
            } else if ("obsstat" == type) {
                if (selectItem == "산림") {
                    val intent = Intent(context, DlgFloraActivity::class.java)
                    if (obsstatTV.text != null && obsstatTV.text != "") {
                        var SPEC = obsstatTV.text.toString()
                        intent.putExtra("SPEC", SPEC)
                    }
                    startActivityForResult(intent, SET_FLORA2)
                } else if (selectItem == "기타") {
                    val intent = Intent(context, DlgInputActivity::class.java)

                    startActivityForResult(intent, SET_INPUT)
                }
            }

        })

        builder.show();
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
                imageUri = FileProvider.getUriForFile(context, context.packageName + ".provider", photo)

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
                SET_FLORA -> {
                    var name = data!!.getStringExtra("name");
                    useTarSpET.text = name
                }

                SET_INPUT -> {
                    var name = data!!.getStringExtra("name");
                    obsstatTV.text = name
                }
                SET_FLORA2 -> {
                    var name = data!!.getStringExtra("name");
                    obsstatTV.text = "산림(" + name + ")"
                }
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

                    if (data!!.getStringExtra("ENDANGERED") != null) {
                        name += "(멸종위기)"
                    }

                    var code: ArrayList<String> = ArrayList<String>()

//                    if(data!!.getSerializableExtra("code") != null){
//                        code = data!!.getSerializableExtra("code") as ArrayList<String>
//                        var codeText:String = ""
//
//                        for (i in 0..code.size-1){
//                            codeText += code.get(i) + " "
//                        }
//
//                        endangeredTV.setText(codeText)
//                        if (codeText != "") {
//                            name += "(멸종위기)"
//                        }
//                    }

                    if (name == "SP(미동정)") {
                        birdsTV.text = name
                        birdsLL.visibility = View.VISIBLE
                        birdsTV.visibility = View.GONE
                    } else {
                        birdsTV.text = name
                    }
                    familyNameTV.text = family_name
                    zoologicalTV.text = zoological

                };

                SET_DATA4 -> {

                    if (data!!.getSerializableExtra("Region") != null) {
                        region = data!!.getSerializableExtra("Region") as Region
                        obsstatET.visibility = View.GONE
                        obsstatTV.visibility = View.VISIBLE
                        obsstatTV.setText(region.SMALLCATEGORY)
                    }

                    if (data!!.getSerializableExtra("Vegetation") != null) {
                        vegetation = data!!.getSerializableExtra("Vegetation") as Vegetation
                        obsstatET.visibility = View.GONE
                        obsstatTV.visibility = View.VISIBLE
                        obsstatTV.setText(vegetation.CORRESPONDINGNAME)

                    }

                    if (data!!.getIntExtra("Other", 0) != null) {
                        val count = data!!.getIntExtra("Other", 0)

                        if (count == 1000) {
                            obsstatLL.visibility = View.VISIBLE
                            obsstatTV.visibility = View.GONE
                        }
                    }

                }

                SET_STANDARD -> {
                    var content = ""
                    var code: ArrayList<String> = ArrayList<String>()

//                    if(data!!.getStringExtra("content") != null) {
//                        content = data!!.getStringExtra("content")
//                        mjActTV.setText(content)
//                    }

                    if (data!!.getSerializableExtra("code") != null) {
                        code = data!!.getSerializableExtra("code") as ArrayList<String>
                        var codeText: String = ""

                        for (i in 0..code.size - 1) {
                            codeText += code.get(i) + "\n"
                        }
                        standardTV.setText(codeText)

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

                        addPicturesLL!!.removeAllViews()
                        val realPathFromURI = cameraPath!!
                        images_path!!.add(cameraPath!!)

                        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$realPathFromURI")))
                        try {
                            val add_file = Utils.getImages(context.contentResolver, cameraPath)

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

                            val num = numTV.text.toString()
                            var time = ""
                            time = timeTV.text.toString()
                            var timesplit = time.split(":")
                            invtm = timesplit.get(0) + timesplit.get(1)
                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator
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
                            val date = Date()
                            val sdf = SimpleDateFormat("yyyyMMdd-HHmmSS")

                            val getTime = sdf.format(date)
                            var gettimes = getTime.split("-")

                            saveVitmapToFile(images!!.get(i), outPath + getTime.substring(2, 8) + "_" + gettimes[1] + "_" + (i + 1) + ".png")

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
                    for (i in 0 until images_path!!.size) {
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

//                    for (i in 0 until images!!.size){
//                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images"+ File.separator +keyId+ File.separator
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
//                        val num = numTV.text.toString()
//                        saveVitmapToFile(images!!.get(i),outPath+num + "_" + invtm+"_"+albumcount+".png")
//                        albumcount++
//
//                    }

                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {
                        val v = addPicturesLL!!.getChildAt(i)

                        val delIV = v.findViewById(R.id.delIV) as ImageView

                        val num = numTV.text.toString()
                        var time = ""
                        time = timeTV.text.toString()
                        var timesplit = time.split(":")
                        invtm = timesplit.get(0) + timesplit.get(1)
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator
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
                        val date = Date()
                        val sdf = SimpleDateFormat("yyyyMMdd-HHmmSS")

                        val getTime = sdf.format(date)
                        var gettimes = getTime.split("-")

                        saveVitmapToFile(images!!.get(i), outPath + getTime.substring(2, 8) + "_" + gettimes[1] + "_" + (i + 1) + ".png")

                    }
                    images!!.clear()
                }
            }
        }
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

    fun getAttrubuteKey(): String {

        val time = System.currentTimeMillis()
//        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT

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
        val bitmap = BitmapFactory.decodeFile(str)
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator
                    val num = numTV.text.toString()
                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)
                    var file = File(outPath + num + "_" + invtm + "_" + (tag + 1) + ".png")
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
//                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
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
                            val pathPk2 = getPk.get(1)
                            val num = numTV.text.toString()
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
                            val pathPk2 = getPk.get(1)
                            val num = numTV.text.toString()
                            val invtm = timeTV.text.toString()
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

    fun clear() {

        var c = dbManager!!.pkNum("birdsAttribute")
        Log.d("데이터", c.toString())
        numTV.text = c.toString()
        /*   var num = numTV.text.toString()
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
           }*/
        timeTV.setText(Utils.timeStr())
        birdsTV.setText("")
        familyNameTV.setText("")
        zoologicalTV.setText("")
        indicntET.setText("")
        endangeredTV.setText("")
        standardTV.setText("")

//        obsstatTV.setText("")
//        useTarTV.setText("")
        useTarSpET.setText("")
//        useLayerTV.setText("")
//        mjActTV.setText("")
//        standardTV.setText("")
        mjActPrET.setText("")
        confmodTV.setText("")

        addPicturesLL!!.removeAllViews()

    }

    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt() - 1, days[2].toInt()).show()
    }

    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth)
        invDtTV.text = msg
    }


    fun resetPage(page: Int) {

        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        pk = intent.getStringExtra("id")

        val data = db.query("birdsAttribute", dataList, "id = '${pk}'", null, null, null, "", null);

        while (data.moveToNext()) {

            var birds_attribute = ps_birds_attribute(data)
            birds_attribute.GROP_ID = keyId

            invRegionET.setText(birds_attribute.INV_REGION)
            if (invRegionET.text == null) {
                invRegionET.setText("")
            }

            birds_attribute.PRJ_NAME = prjnameET.text.toString()

            invDtTV.setText(birds_attribute.INV_DT)
            if (invDtTV.text == null) {
                invDtTV.setText("")
            }

            invPersonTV.setText(birds_attribute.INV_PERSON)
            if (invPersonTV.text == null) {
                invPersonTV.setText("")
            }

            btn1.setText(birds_attribute.WEATHER)
            if (btn1.text == null) {
                btn1.setText("0.0")
            }

            btn2.setText(birds_attribute.WIND)
            if (btn2.text == null) {
                btn2.setText("")
            }

            btn3.setText(birds_attribute.WIND_DIRE)
            if (btn3.text == null) {
                btn3.setText("")
            }

            temperatureET.setText(birds_attribute.TEMPERATUR.toString())
            if (temperatureET.text == null) {
                temperatureET.setText("0.0")
            }


            etcET.setText(birds_attribute.ETC)
            if (etcET.text == null) {
                etcET.setText("")
            }

            numTV.setText(birds_attribute.NUM.toString())
            if (numTV.text == null) {
                numTV.setText("")
            }

            timeTV.setText(birds_attribute.INV_TM)
            if (timeTV.text == null) {
                timeTV.setText("")
            }

            birdsTV.setText(birds_attribute.SPEC_NM)
            if (birdsTV.text == null) {
                birdsTV.setText("")
            }

            familyNameTV.setText(birds_attribute.FAMI_NM)
            if (familyNameTV.text == null) {
                familyNameTV.setText("")
            }

            zoologicalTV.setText(birds_attribute.SCIEN_NM)
            if (zoologicalTV.text == null) {
                zoologicalTV.setText("")
            }

            indicntET.setText(birds_attribute.INDI_CNT.toString())
            if (indicntET.text == null) {
                indicntET.setText("0")
            }

            obsstatTV.setText(birds_attribute.OBS_STAT)
            if (obsstatTV.text == null) {
                obsstatTV.setText("")
            }

            useTarTV.setText(birds_attribute.USE_TAR)
            if (useTarTV.text == null) {
                useTarTV.setText("")
                useTarSpLL.visibility = View.GONE
            } else if (useTarTV.text == "") {
                useTarSpLL.visibility = View.GONE
            }

            /*  useTarSpET.setText(birds_attribute.USE_TAR_SP)
              if(useTarSpET.text == null){
                  useTarSpET.setText("")
                  useTarSpLL.visibility = View.GONE
              }*/

            /*if(birds_attribute.USE_TAR_SP != null && !birds_attribute.USE_TAR_SP.equals("")){
                useTarSpLL.visibility = View.VISIBLE
            }*/

            useLayerTV.setText(birds_attribute.USE_LAYER)
            if (useLayerTV.text == null) {
                useLayerTV.setText("")
            }

            mjActTV.setText(birds_attribute.MJ_ACT)
            if (mjActTV.text == null) {
                mjActTV.setText("")
                mjActPrLL.visibility = View.GONE
            } else if (mjActTV.text == "") {
                mjActPrLL.visibility = View.GONE
            }

//            mjActPrET.setText(birds_attribute.MJ_ACT_PR)
            if (mjActPrET.text == null) {
                mjActPrET.setText("")
                mjActPrLL.visibility = View.GONE
            }

            /* if(birds_attribute.MJ_ACT_PR != null && !birds_attribute.MJ_ACT_PR.equals("")){
                 mjActPrLL.visibility = View.VISIBLE
             }*/

            gpslatTV.setText(lat)
            gpslonTV.setText(log)

            data.close()

        }

    }

    override fun onBackPressed() {
        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query("birdsAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var birds_attribute = ps_birds_attribute(data)

            dataArray.add(birds_attribute)

        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
            var intent = Intent()
            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "birds/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }

            }
        }

        data.close()

        finish()
    }

    fun convert(d: Double): String {

        var long_d = abs(d)

//        var i = d.intValue()
        var i = long_d.toInt()
        var s = i.toString()

        coordndET.setText(s)

        long_d = long_d - i;
        long_d = long_d * 60;
        coordnmET.setText(long_d.toInt().toString())
//        i = long_d.intValue();
        i = long_d.toInt();

//        s = s + String.format(i) + '\'';
        s = s + i.toString()

        long_d = long_d - i;
        long_d = long_d * 60;

//        i = long_d.round().intValue();
        i = round(long_d.toDouble()).toInt()

        coordnsET.setText(long_d.toFloat().toString())

        s = s + i.toString() + '"';

        return s
    }

    fun logconvert(d: Double): String {

        var long_d = abs(d)

//        var i = d.intValue()
        var i = long_d.toInt()
        var s = i.toString()

        coordedET.setText(s)

        long_d = long_d - i;
        long_d = long_d * 60;
        coordemET.setText(long_d.toInt().toString())
//        i = long_d.intValue();
        i = long_d.toInt();

//        s = s + String.format(i) + '\'';
        s = s + i.toString()

        long_d = long_d - i;
        long_d = long_d * 60;

//        i = long_d.round().intValue();
        i = round(long_d.toDouble()).toInt()

        coordesET.setText(long_d.toFloat().toString())

        s = s + i.toString() + '"';

        return s
    }


    fun timedlg() {
        val cal = Calendar.getInstance()
        val dialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
            var hour_s = hour.toString()
            var min_s = min.toString()
            if (min_s.length != 2) {
                min_s = "0" + min_s
            }
            if (hour_s.length != 2) {
                hour_s = "0" + hour_s
            }
            val msg = String.format("%s : %s", hour_s, min_s)
            timeTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }
}

fun null_birds_attribute(): Birds_attribute {
    var birds_attribute: Birds_attribute = Birds_attribute(null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    return birds_attribute
}

fun ps_birds_attribute(data: Cursor): Birds_attribute {
    var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
            , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
            , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
            , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
    )
    return birds_attribute
}