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
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import au.com.objectix.jgridshift.Util
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_fish.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class FishActivity : Activity() , OnLocationUpdatedListener {

    lateinit var context: Context;

    var userName = "";

    val REQUEST_FINE_LOCATION = 50
    val REQUEST_ACCESS_COARSE_LOCATION = 51

    var latitude = 0.0f;
    var longitude = 0.0f;

    private var progressDialog: ProgressDialog? = null

    val SET_FISH = 80
    val SET_FISHDLG = 81

    val SET_DATA1 = 1

    var chkdata: Boolean = false;

    var keyId: String? = null;

    var pk: String? = null

    var page:Int? = null

    var dataArray:ArrayList<Fish_attribute> = ArrayList<Fish_attribute>()

    var lat:String = ""
    var log:String = ""

    var basechkdata = false

    val SET_MAMMAL = 1
    val SET_UNSPEC = 2

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

    var markerid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fish)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        userName = PrefUtils.getStringPreference(context, "name");

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        fishinvdtET.setText(Utils.todayStr())
        fishinvpersonET.setText(userName)

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        val SET_DATA1 = 1;

        val dbmanager = DataBaseHelper(context);
        val db = dbmanager.createDataBase();

        var intent: Intent = getIntent();

        if(intent.getStringExtra("markerid") != null){
            markerid = intent.getStringExtra("markerid")
        }

        if(intent.getStringExtra("latitude")!= null){
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            fishgpslatTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            fishgpslonTV.setText(log)
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

            fishinvpersonET.setText(base.INV_PERSON)
            fishinvdtET.setText(base.INV_DT)

            val time = Utils.timeStr()

            fishgpslatTV.setText(base.GPS_LAT)
            fishgpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,fishinvpersonET.text.toString(),fishinvdtET.text.toString(),Utils.timeStr())

            dbmanager.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {

            btn_fishDelete.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("fishAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                        data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                        data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))


                fishinvregionET.setText(fish_attribute.INV_REGION)
                fishinvdtET.setText(fish_attribute.INV_DT)
                fishinvpersonET.setText(fish_attribute.INV_PERSON)

                fishweatherET.setText(fish_attribute.WEATHER)
                fishwindET.setText(fish_attribute.WIND)
                fishwinddireET.setText(fish_attribute.WIND_DIRE)
                fishtemperaturET.setText(fish_attribute.TEMPERATUR.toString())

                fishetcET.setText(fish_attribute.ETC)

                fishmidrageET.setText(fish_attribute.MID_RAGE)

                fishcodenumET.setText(fish_attribute.CODE_NUM)

                fishrivernumET.setText(fish_attribute.RIVER_NUM.toString())
                fishrivernmET.setText(fish_attribute.RIVER_NM)

                fishnetcntET.setText(fish_attribute.NET_CNT.toString())
                fishnetminet.setText(fish_attribute.NET_MIN.toString())

                fishcolltoolET.setText(fish_attribute.COLL_TOOL)

                fishstreamwET.setText(fish_attribute.STREAM_W.toString())
                fishwaterwET.setText(fish_attribute.WATER_W.toString())
                fishwaterdET.setText(fish_attribute.WATER_D.toString())
                fishwatercurET.setText(fish_attribute.WATER_CUR.toString())

                rivstrTV.setText(fish_attribute.RIV_STR)
                rivstrdetET.setText(fish_attribute.RIV_STR_IN)

                if(fish_attribute.RIV_STR_IN == null || fish_attribute.RIV_STR_IN.equals("")){
                    rivstrdetET.setText("")
                    detailLL.visibility = View.GONE
                }

                if(fish_attribute.RIV_STR_IN != null && !fish_attribute.RIV_STR_IN.equals("")){
                    detailLL.visibility = View.VISIBLE
                }

                formTV.setText(fish_attribute.RIV_FORM)

                fishnumTV.setText(fish_attribute.id)

                fishspecnmET.setText(fish_attribute.SPEC_NM)
                fishfaminmET.setText(fish_attribute.FAMI_NM)
                fishsciennmET.setText(fish_attribute.SCIEN_NM)

                fishindicntET.setText(fish_attribute.INDI_CNT.toString())

                fishunidentET.setText(fish_attribute.UNIDENT)
                if(fish_attribute.UNIDENT == null){
                    fishunidentET.setText("")
                }

                fishtivfmchET.setText(fish_attribute.RIV_FM_CH)

                fishunfishchET.setText(fish_attribute.UN_FISH_CH)

                fishgpslonTV.setText(fish_attribute.GPS_LON.toString())
                fishgpslatTV.setText(fish_attribute.GPS_LAT.toString())


                val id = fish_attribute.id

                if(fish_attribute.TEMP_YN.equals("N")){
                    dbmanager.deletefish_attribute(fish_attribute,id)
                }

                if(fish_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(fish_attribute)
                }

                confmodTV.setText(fish_attribute.CONF_MOD)

                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + fish_attribute.INV_DT + "." + fish_attribute.INV_TM + "/imges")
                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/")
                var tmpfileList = tmpfiles.listFiles()

                if (fileList != null) {
                    for (i in 0..fileList.size - 1) {
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/"
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

                        val tmpfile = fileList.get(i)
                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/fish/imges" ,   pk +"_" + (i+1) + ".png")

                        if(tmpfile.exists()){
                            tmpfile.renameTo(tmpfile2)
                        }

                        tmpfileList = tmpfiles.listFiles()

                    }
                }

                if(tmpfileList != null){
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

                        for(j in 0..tmpfileList.size - 1) {

                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/fish/imges/" + pk + "_" + (j + 1).toString() + ".png")) {
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

            }

        }

        fishleftLL.setOnClickListener {


            val dataList: Array<String> = arrayOf("*");

            val data = db.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                        data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                        data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))

                dataArray.add(fish_attribute)

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

        fishrightLL.setOnClickListener {

            clear()

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                        data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                        data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))

                dataArray.add(fish_attribute)

            }

            var fish_attribute: Fish_attribute = Fish_attribute(null, null, null, null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null, null, null, null, null,null,null,null)


            fish_attribute.GROP_ID = keyId

            fish_attribute.PRJ_NAME = ""

            fish_attribute.INV_REGION = fishinvregionET.text.toString()
            fish_attribute.INV_DT = fishinvdtET.text.toString()

            fish_attribute.INV_PERSON = fishinvpersonET.text.toString()

            fish_attribute.WEATHER = fishweatherET.text.toString()
            fish_attribute.WIND = fishwindET.text.toString()
            fish_attribute.WIND_DIRE = fishwinddireET.text.toString()

            if (fishtemperaturET.text.isNotEmpty()) {
                fish_attribute.TEMPERATUR = fishtemperaturET.text.toString().toFloat()
            }

            fish_attribute.ETC = fishetcET.text.toString()

            fish_attribute.MID_RAGE = fishmidrageET.text.toString()

            fish_attribute.CODE_NUM = fishcodenumET.text.toString()

            if (fishrivernumET.text.isNotEmpty()) {
                fish_attribute.RIVER_NUM = fishrivernumET.text.toString().toInt()
            }

            fish_attribute.RIVER_NM = fishrivernmET.text.toString()

            if (fishnetcntET.text.isNotEmpty()) {
                fish_attribute.NET_CNT = fishnetcntET.text.toString().toInt()
            }

            if (fishnetminet.text.isNotEmpty()) {
                fish_attribute.NET_MIN = fishnetminet.text.toString().toInt()
            }

            fish_attribute.COLL_TOOL = fishcolltoolET.text.toString()

            if (fishstreamwET.text.isNotEmpty()) {
                fish_attribute.STREAM_W = fishstreamwET.text.toString().toInt()
            }

            if (fishwaterwET.text.isNotEmpty()) {
                fish_attribute.WATER_W = fishwaterwET.text.toString().toInt()
            }

            if (fishwaterdET.text.isNotEmpty()) {
                fish_attribute.WATER_D = fishwaterdET.text.toString().toInt()
            }

            if (fishwatercurET.text.isNotEmpty()) {
                fish_attribute.WATER_CUR = fishwatercurET.text.toString().toInt()
            }

            fish_attribute.RIV_STR = rivstrTV.text.toString()
            fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

            fish_attribute.RIV_FORM = formTV.text.toString()

            if (fishnumTV.text.isNotEmpty()) {
                fish_attribute.NUM = fishnumTV.text.toString().toInt()
            }

            fish_attribute.SPEC_NM = fishspecnmET.text.toString()
            fish_attribute.FAMI_NM = fishfaminmET.text.toString()
            fish_attribute.SCIEN_NM = fishsciennmET.text.toString()

            if (fishindicntET.text.isNotEmpty()) {
                fish_attribute.INDI_CNT = fishindicntET.text.toString().toInt()
            }

            fish_attribute.UNIDENT = fishunidentET.text.toString()
            fish_attribute.RIV_FM_CH = fishtivfmchET.text.toString()
            fish_attribute.UN_FISH_CH = fishunfishchET.text.toString()

            fish_attribute.SPEC_NM = fishspecnmET.text.toString()

            fish_attribute.TEMP_YN = "N"

            fish_attribute.GPS_LAT = lat.toFloat()
            fish_attribute.GPS_LON = log.toFloat()


            if (page == dataArray.size) {
                dbmanager.insertfish_attribute(fish_attribute)
                page = page!! + 1
            }

            val data2 = db.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var fish_attribute: Fish_attribute = Fish_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
                        data2.getString(8),data2.getString(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getInt(14), data2.getString(15), data2.getInt(16), data2.getInt(17), data2.getString(18),
                        data2.getFloat(19), data2.getFloat(20), data2.getString(21), data2.getInt(22), data2.getInt(23), data2.getInt(24), data2.getInt(25), data2.getString(26), data2.getString(27), data2.getString(28),
                        data2.getInt(29) ,data2.getString(30), data2.getString(31), data2.getString(32), data2.getInt(33), data2.getString(33), data2.getString(35), data2.getString(36),data2.getString(37),data.getString(38))

                dataArray.add(fish_attribute)

            }

            if (page!! < dataArray.size) {
                page = page!! + 1
            }

            fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)

        }

        fishmidrageET.setOnClickListener {

            val intent = Intent(this, DlgcomAcitivity::class.java)
            intent.putExtra("title", "증권군역 / 코드번호")
            intent.putExtra("table", "common")
            intent.putExtra("DlgHeight", 450f);
//            startActivity(intent)
            startActivityForResult(intent, SET_DATA1);

        }

        btn_fishSave1.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        var fish_attribute: Fish_attribute = Fish_attribute(null, null, null, null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null, null, null, null, null, null
                                , null, null, null, null, null, null, null, null, null, null,null, null, null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

                        fish_attribute.GROP_ID = keyId

                        fish_attribute.PRJ_NAME = ""

                        fish_attribute.INV_REGION = fishinvregionET.text.toString()

                        fish_attribute.INV_DT = Utils.todayStr()

                        fish_attribute.INV_TM = Utils.timeStr()

                        fish_attribute.INV_PERSON = fishinvpersonET.text.toString()

                        fish_attribute.WEATHER = fishweatherET.text.toString()
                        fish_attribute.WIND = fishwindET.text.toString()
                        fish_attribute.WIND_DIRE = fishwinddireET.text.toString()

                        if (fishtemperaturET.text.isNotEmpty()) {
                            fish_attribute.TEMPERATUR = fishtemperaturET.text.toString().toFloat()
                        }

                        fish_attribute.ETC = fishetcET.text.toString()

                        fish_attribute.MID_RAGE = fishmidrageET.text.toString()

                        fish_attribute.CODE_NUM = fishcodenumET.text.toString()

                        if (fishrivernumET.text.isNotEmpty()) {
                            fish_attribute.RIVER_NUM = fishrivernumET.text.toString().toInt()
                        }

                        fish_attribute.RIVER_NM = fishrivernmET.text.toString()

                        if (fishnetcntET.text.isNotEmpty()) {
                            fish_attribute.NET_CNT = fishnetcntET.text.toString().toInt()
                        }

                        if (fishnetminet.text.isNotEmpty()) {
                            fish_attribute.NET_MIN = fishnetminet.text.toString().toInt()
                        }

                        if (fishgpslatTV.text.isNotEmpty()) {
                            fish_attribute.GPS_LAT = fishgpslatTV.text.toString().toFloat()
                        }

                        fish_attribute.COLL_TOOL = fishcolltoolET.text.toString()

                        if (fishstreamwET.text.isNotEmpty()) {
                            fish_attribute.STREAM_W = fishstreamwET.text.toString().toInt()
                        }

                        if (fishwaterwET.text.isNotEmpty()) {
                            fish_attribute.WATER_W = fishwaterwET.text.toString().toInt()
                        }

                        if (fishwaterdET.text.isNotEmpty()) {
                            fish_attribute.WATER_D = fishwaterdET.text.toString().toInt()
                        }

                        if (fishwatercurET.text.isNotEmpty()) {
                            fish_attribute.WATER_CUR = fishwatercurET.text.toString().toInt()
                        }

                        fish_attribute.RIV_STR = rivstrTV.text.toString()
                        fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

                        fish_attribute.RIV_FORM = formTV.text.toString()

                        if (fishnumTV.text.isNotEmpty()) {
                            fish_attribute.NUM = fishnumTV.text.toString().toInt()
                        }

                        fish_attribute.SPEC_NM = fishspecnmET.text.toString()
                        fish_attribute.FAMI_NM = fishfaminmET.text.toString()
                        fish_attribute.SCIEN_NM = fishsciennmET.text.toString()

                        if (fishindicntET.text.isNotEmpty()) {
                            fish_attribute.INDI_CNT = fishindicntET.text.toString().toInt()
                        }

                        fish_attribute.UNIDENT = fishunidentET.text.toString()
                        fish_attribute.RIV_FM_CH = fishtivfmchET.text.toString()
                        fish_attribute.UN_FISH_CH = fishunfishchET.text.toString()

                        fish_attribute.SPEC_NM = fishspecnmET.text.toString()

                        fish_attribute.TEMP_YN = "Y"

                        fish_attribute.GPS_LAT = lat.toFloat()
                        fish_attribute.GPS_LON = log.toFloat()

                        fish_attribute.CONF_MOD = "N"

                        if (chkdata) {

                            if(pk != null){

                                val CONF_MOD = confmodTV.text.toString()

                                if(CONF_MOD == "C"  || CONF_MOD == "N"){
                                    fish_attribute.CONF_MOD = "M"
                                }

                                dbmanager.updatefish_attribute(fish_attribute,pk)
                            }

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/")
                            val pathdir = path.listFiles()

                            if(pathdir != null) {
                                for (i in 0..pathdir.size-1) {

                                    for(j in 0..pathdir.size-1) {

                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/fish/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                            pathdir.get(i).canonicalFile.delete()

                                            println("delete ===============")

                                        }
                                    }

                                }
                            }

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/"
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

                                saveVitmapToFile(images!!.get(i),outPath+pk+"_"+(i+1)+".png")

                            }

                        } else {

                            dbmanager.insertfish_attribute(fish_attribute);

                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            sdPath += "/ecology/tmps/" + fish_attribute.INV_DT +"."+ fish_attribute.INV_TM + "/imges"
                            val fish = File(sdPath)
                            fish.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                            val file = File(sdPath)
                            file.mkdir();
                            //이미 있다면 삭제. 후 생성
                            setDirEmpty(sdPath)

                            sdPath+="/"

                            var pathArray:ArrayList<String> = ArrayList<String>()

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + fish_attribute.INV_DT +"."+ fish_attribute.INV_TM + "/imges/"
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

                        }

                        dialog.cancel()

                        var intent = Intent()

                        intent.putExtra("export", 70);

                        setResult(RESULT_OK, intent);

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn_fishCancle1.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                                    data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                                    data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))

                            dataArray.add(fish_attribute)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") != null){

                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                        }

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        btn_fishDelete.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var fish_attribute: Fish_attribute = Fish_attribute(null, null, null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null, null, null, null, null, null, null,null)


                            if (pk != null) {

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/fish/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                }

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db.query("fishAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                                data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                                                data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                                                data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))

                                        dataArray.add(fish_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbmanager.deletefish_attribute(fish_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbmanager.deletefish_attribute(fish_attribute, pk)

                                        var intent = Intent()

                                        intent.putExtra("markerid", markerid)

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

                                val data= db.query("fishAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                            data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                                            data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                                            data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))
                                }

                                if (chkdata == true) {
                                    Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    intent.putExtra("markerid", markerid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }

                            }

                            if (intent.getStringExtra("id") == null) {
                                intent.putExtra("markerid", markerid)

                                setResult(RESULT_OK, intent);
                                finish()
                            }


                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }

        }



        fishspecnmET.setOnClickListener {
            startDlgFish()
        }

        fishfaminmET.setOnClickListener {
            startDlgFish()
        }

        fishsciennmET.setOnClickListener {
            startDlgFish()
        }

        fishweatherET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("맑음");
            listItems.add("흐림");
            listItems.add("안개");
            listItems.add("비");

            alert(listItems, "날씨", fishweatherET, "weather");
        }

        fishwindET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("강");
            listItems.add("중");
            listItems.add("약");
            listItems.add("무");

            alert(listItems, "바람", fishwindET, "wind");
        }

        fishwinddireET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("N");
            listItems.add("NE");
            listItems.add("E");
            listItems.add("SE");
            listItems.add("S");
            listItems.add("SW");
            listItems.add("W");
            listItems.add("NW");

            alert(listItems, "풍향", fishwinddireET, "winddire");
        }


        formTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "형태")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 100);

            startActivityForResult(intent, SET_FISH);

        }


        rivstrTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("Boulder( > 256mm )");
            listItems.add("Cobble( 64 ~ 256mm )");
            listItems.add("Pebble( 16 ~ 64mm )");
            listItems.add("Gravel( 2 ~ 16mm )");
            listItems.add("Sand( < 2mm )");

            alert(listItems, "하상 구조 선택", rivstrTV, "rivstr");

        }

        btn_add.setOnClickListener {
            var fish_attribute: Fish_attribute = Fish_attribute(null, null, null, null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null, null, null, null, null, null
                    , null, null, null, null, null, null, null, null,null, null, null, null, null,null,null)

            keyId = intent.getStringExtra("GROP_ID")

            fish_attribute.GROP_ID = keyId

            fish_attribute.PRJ_NAME = ""

            fish_attribute.INV_REGION = fishinvregionET.text.toString()
            fish_attribute.INV_DT = Utils.todayStr()

            println("inv_dt ===============${fish_attribute.INV_DT}")
            fish_attribute.INV_TM = Utils.timeStr()

            fish_attribute.INV_PERSON = fishinvpersonET.text.toString()

            fish_attribute.WEATHER = fishweatherET.text.toString()
            fish_attribute.WIND = fishwindET.text.toString()
            fish_attribute.WIND_DIRE = fishwinddireET.text.toString()

            if (fishtemperaturET.text.isNotEmpty()) {
                fish_attribute.TEMPERATUR = fishtemperaturET.text.toString().toFloat()
            }

            fish_attribute.ETC = fishetcET.text.toString()

            fish_attribute.MID_RAGE = fishmidrageET.text.toString()

            fish_attribute.CODE_NUM = fishcodenumET.text.toString()

            if (fishrivernumET.text.isNotEmpty()) {
                fish_attribute.RIVER_NUM = fishrivernumET.text.toString().toInt()
            }

            fish_attribute.RIVER_NM = fishrivernmET.text.toString()

            if (fishnetcntET.text.isNotEmpty()) {
                fish_attribute.NET_CNT = fishnetcntET.text.toString().toInt()
            }

            if (fishnetminet.text.isNotEmpty()) {
                fish_attribute.NET_MIN = fishnetminet.text.toString().toInt()
            }

            if (fishgpslatTV.text.isNotEmpty()) {
                fish_attribute.GPS_LAT = fishgpslatTV.text.toString().toFloat()
            }

            fish_attribute.COLL_TOOL = fishcolltoolET.text.toString()

            if (fishstreamwET.text.isNotEmpty()) {
                fish_attribute.STREAM_W = fishstreamwET.text.toString().toInt()
            }

            if (fishwaterwET.text.isNotEmpty()) {
                fish_attribute.WATER_W = fishwaterwET.text.toString().toInt()
            }

            if (fishwaterdET.text.isNotEmpty()) {
                fish_attribute.WATER_D = fishwaterdET.text.toString().toInt()
            }

            if (fishwatercurET.text.isNotEmpty()) {
                fish_attribute.WATER_CUR = fishwatercurET.text.toString().toInt()
            }

            fish_attribute.RIV_STR = rivstrTV.text.toString()
            fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

            fish_attribute.RIV_FORM = formTV.text.toString()

            if (fishnumTV.text.isNotEmpty()) {
                fish_attribute.NUM = fishnumTV.text.toString().toInt()
            }

            fish_attribute.SPEC_NM = fishspecnmET.text.toString()
            fish_attribute.FAMI_NM = fishfaminmET.text.toString()
            fish_attribute.SCIEN_NM = fishsciennmET.text.toString()

            if (fishindicntET.text.isNotEmpty()) {
                fish_attribute.INDI_CNT = fishindicntET.text.toString().toInt()
            }

            fish_attribute.UNIDENT = fishunidentET.text.toString()
            fish_attribute.RIV_FM_CH = fishtivfmchET.text.toString()
            fish_attribute.UN_FISH_CH = fishunfishchET.text.toString()

            fish_attribute.SPEC_NM = fishspecnmET.text.toString()

            fish_attribute.TEMP_YN = "Y"

            fish_attribute.GPS_LAT = lat.toFloat()
            fish_attribute.GPS_LON = log.toFloat()

            fish_attribute.CONF_MOD = "N"

            if (chkdata) {

                if(pk != null){

                    val CONF_MOD = confmodTV.text.toString()

                    if(CONF_MOD == "C"  || CONF_MOD == "N"){
                        fish_attribute.CONF_MOD = "M"
                    }

                    dbmanager.updatefish_attribute(fish_attribute,pk)
                }

                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/")
                val pathdir = path.listFiles()

                if(pathdir != null) {
                    for (i in 0..pathdir.size-1) {

                        for(j in 0..pathdir.size-1) {

                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/fish/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                pathdir.get(i).canonicalFile.delete()

                                println("delete ===============")

                            }
                        }

                    }
                }

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "fish/imges/"
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

                    saveVitmapToFile(images!!.get(i),outPath+pk+"_"+(i+1)+".png")

                }

            } else {

                dbmanager.insertfish_attribute(fish_attribute);

                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                sdPath += "/ecology/tmps/" + fish_attribute.INV_DT +"."+ fish_attribute.INV_TM + "/imges"
                val fish = File(sdPath)
                fish.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                val file = File(sdPath)
                file.mkdir();
                //이미 있다면 삭제. 후 생성
                setDirEmpty(sdPath)

                sdPath+="/"

                var pathArray:ArrayList<String> = ArrayList<String>()

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + fish_attribute.INV_DT +"."+ fish_attribute.INV_TM + "/imges/"
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

            }

            if(intent.getStringExtra("set") != null){
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            btn_fishDelete.visibility = View.GONE

            var intent = Intent()
            intent.putExtra("export",70)
            setResult(RESULT_OK, intent)

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

    fun resetPage(page : Int){
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data= db.query("fishAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true



            var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                    data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                    data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))

            dataArray.add(fish_attribute)

            fishinvregionET.setText(fish_attribute.INV_REGION)
            if(fishinvregionET.text == null){
                fishinvregionET.setText("")
            }

            fishinvdtET.setText(fish_attribute.INV_DT)
            if(fishinvdtET.text == null){
                fishinvdtET.setText(Utils.todayStr())
            }

            fishinvpersonET.setText(fish_attribute.INV_PERSON)
            if(fishinvpersonET.text == null){
                fishinvpersonET.setText("")
            }

            fishweatherET.setText(fish_attribute.WEATHER)
            if(fishinvpersonET.text == null){
                fishinvpersonET.setText("")
            }

            fishwindET.setText(fish_attribute.WIND)
            if(fishwindET.text == null){
                fishwindET.setText("")
            }

            fishwinddireET.setText(fish_attribute.WIND_DIRE)
            if(fishwinddireET.text == null){
                fishwinddireET.setText("")
            }

            fishtemperaturET.setText(fish_attribute.TEMPERATUR.toString())
            if(fishtemperaturET.text == null){
                fishtemperaturET.setText("")
            }

            fishetcET.setText(fish_attribute.ETC)
            if(fishetcET.text == null){
                fishetcET.setText("")
            }

            fishmidrageET.setText(fish_attribute.MID_RAGE)
            if(fishmidrageET.text == null){
                fishmidrageET.setText("")
            }

            fishcodenumET.setText(fish_attribute.CODE_NUM)
            if(fishcodenumET.text == null){
                fishcodenumET.setText("")
            }

            fishrivernumET.setText(fish_attribute.RIVER_NUM.toString())
            if(fishrivernumET.text == null){
                fishrivernumET.setText("")
            }

            fishgpslatTV.setText(fish_attribute.GPS_LAT.toString())
            if(fishgpslatTV.text == null){
                initGPS()
            }

            fishgpslonTV.setText(fish_attribute.GPS_LON.toString())

            fishrivernmET.setText(fish_attribute.RIVER_NM)
            if(fishrivernmET.text == null){
                fishrivernmET.setText("")
            }

            fishnetcntET.setText(fish_attribute.NET_CNT.toString())
            if(fishnetcntET.text == null){
                fishnetcntET.setText("")
            }

            fishnetminet.setText(fish_attribute.NET_MIN.toString())
            if(fishnetminet.text == null){
                fishnetminet.setText("")
            }

            fishcolltoolET.setText(fish_attribute.COLL_TOOL)
            if(fishcolltoolET.text == null){
                fishcolltoolET.setText("")
            }

            fishstreamwET.setText(fish_attribute.STREAM_W.toString())
            if(fishcolltoolET.text == null){
                fishcolltoolET.setText("")
            }

            fishwaterwET.setText(fish_attribute.WATER_W.toString())
            if(fishwaterwET.text == null){
                fishwaterwET.setText("")
            }

            fishwaterdET.setText(fish_attribute.WATER_D.toString())
            if(fishwaterdET.text == null){
                fishwaterdET.setText("")
            }

            fishwatercurET.setText(fish_attribute.WATER_CUR.toString())
            if(fishwatercurET.text == null){
                fishwatercurET.setText("")
            }

            rivstrTV.setText(fish_attribute.RIV_STR)
            if(rivstrTV.text == null){
                rivstrTV.setText("")
                detailLL.visibility = View.GONE
            }else if(rivstrTV.text == "") {
                detailLL.visibility = View.GONE
            }

            rivstrdetET.setText(fish_attribute.RIV_STR_IN)
            if(rivstrdetET.text == null){
                rivstrdetET.setText("")
                detailLL.visibility = View.GONE
            }

            if(fish_attribute.RIV_STR_IN != null && !fish_attribute.RIV_STR_IN.equals("")){
                detailLL.visibility = View.VISIBLE
            }

            formTV.setText(fish_attribute.RIV_FORM)
            if(formTV.text == null){
                formTV.setText("")
            }

            fishnumTV.setText(fish_attribute.NUM.toString())
            if(fishnumTV.text == null){
                fishnumTV.setText("")
            }

            fishspecnmET.setText(fish_attribute.SPEC_NM)
            if(fishspecnmET.text == null){
                fishspecnmET.setText("")
            }

            fishfaminmET.setText(fish_attribute.FAMI_NM)
            if(fishfaminmET.text == null){
                fishfaminmET.setText("")
            }

            fishsciennmET.setText(fish_attribute.SCIEN_NM)
            if(fishsciennmET.text == null){
                fishsciennmET.setText("")
            }

            fishindicntET.setText(fish_attribute.INDI_CNT.toString())
            if(fishindicntET.text == null){
                fishindicntET.setText("")
            }

            fishunidentET.setText(fish_attribute.UNIDENT)
            if(fishunidentET.text == null){
                fishunidentET.setText("")
            }

            fishtivfmchET.setText(fish_attribute.RIV_FM_CH)
            if(fishtivfmchET.text == null){
                fishtivfmchET.setText("")
            }

            fishunfishchET.setText(fish_attribute.UN_FISH_CH)
            if(fishunfishchET.text == null){
                fishunfishchET.setText("")
            }

        }
    }

    fun clear(){

        fishinvregionET.setText("")

        fishweatherET.setText("")
        fishwindET.setText("")
        fishwinddireET.setText("")
        fishtemperaturET.setText("")

        fishetcET.setText("")

        fishmidrageET.setText("")

        fishcodenumET.setText("")

        fishrivernumET.setText("")
        fishrivernmET.setText("")

        fishnetcntET.setText("")

        fishcolltoolET.setText("")

        fishstreamwET.setText("")
        fishwaterwET.setText("")
        fishwaterdET.setText("")
        fishwatercurET.setText("")

        rivstrTV.setText("")
        rivstrdetET.setText("")

        formTV.setText("")


        fishspecnmET.setText("")
        fishfaminmET.setText("")
        fishsciennmET.setText("")

        fishindicntET.setText("")

        fishunidentET.setText("")

        fishtivfmchET.setText("")

        fishunfishchET.setText("")

        confmodTV.setText("")

        addPicturesLL!!.removeAllViews()

    }

    fun startDlgFish(){
        val intent = Intent(context, DlgFishActivity::class.java)
        startActivityForResult(intent, SET_FISHDLG);
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
            if ("rivstr" == type) {
                detailLL.visibility = View.VISIBLE
            }else {
                detailLL.visibility = View.GONE
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


            fishgpslatTV.setText(latitude.toString())
            fishgpslonTV.setText(longitude.toString())


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

        var common : common

        var commonDivision: CommonDivision

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_FISH -> {

                    formTV.setText(data!!.getStringExtra("selectDlg"))

                };


                SET_FISHDLG -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    fishspecnmET.text = name
                    fishfaminmET.text = family_name
                    fishsciennmET.text = zoological

                }

                SET_DATA1 -> {


                    common =data!!.getSerializableExtra("common") as common

                    commonDivision=data!!.getSerializableExtra("CommonDivision") as CommonDivision

                    fishmidrageET.setText(common.title)

                    fishcodenumET.setText(commonDivision.title + " / " + commonDivision.code)

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

    fun getAttrubuteKey(): String {

        val time = System.currentTimeMillis()
//        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT
    }


}
