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
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Base
import hntecology.ecology.model.BiotopeType
import hntecology.ecology.model.Mammal_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_mammalia.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class MammaliaActivity : Activity(), OnLocationUpdatedListener {

    lateinit var context: Context;

    var chkdata: Boolean = false;

    var userName = "";

    var keyId: String? = null;

    var page:Int? = null

    var pk:String? = null

    var dataArray:ArrayList<Mammal_attribute> = ArrayList<Mammal_attribute>()

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

    var latitude = 0.0f;
    var longitude = 0.0f;

    val SET_MAMMAL = 1
    val SET_UNSPEC = 2

    val SET_DATA = 1000

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

    private var progressDialog: ProgressDialog? = null

    var lat:String = ""
    var log:String = ""

    var basechkdata = false

    var markerid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mammalia)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        maminvdtTV.text = Utils.todayStr()
        mammaltimeTV.text = Utils.timeStr()

        userName = PrefUtils.getStringPreference(context, "name");

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        maminvpersonTV.setText(userName)

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        var intent: Intent = getIntent();

        if(intent.getStringExtra("markerid") != null){
            markerid = intent.getStringExtra("markerid")
        }

        if(intent.getStringExtra("latitude")!= null){
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            mamgpslatTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            mamgpslonTV.setText(log)
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

            maminvpersonTV.setText(base.INV_PERSON)
            maminvdtTV.setText(base.INV_DT)
            mammaltimeTV.setText(base.INV_TM)

            mamgpslatTV.setText(base.GPS_LAT)
            mamgpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,maminvpersonTV.text.toString(),maminvdtTV.text.toString(),mammaltimeTV.text.toString())

            dbManager.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("mammalAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                        , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27))

                maminvregionET.setText(mammal_attribute.INV_REGION)
                maminvdtTV.setText(mammal_attribute.INV_DT)

                mamweatherET.setText(mammal_attribute.WEATHER)
                mamwindET.setText(mammal_attribute.WIND)
                mamwinddireET.setText(mammal_attribute.WIND_DIRE)

                mamtemperatureET.setText(mammal_attribute.TEMPERATUR.toString())
                mametcET.setText(mammal_attribute.ETC)

                mammaltimeTV.setText(mammal_attribute.INV_TM)
                mammalnumTV.setText(mammal_attribute.id)

                mamspecnmET.setText(mammal_attribute.SPEC_NM)
                mamfaminmTV.setText(mammal_attribute.FAMI_NM)
                mamsciennmTV.setText(mammal_attribute.SCIEN_NM)

                mammalobstyTV.setText(mammal_attribute.OBS_TY)
                if(mammal_attribute.OBS_TY_ETC != null){
                    mammalobstyTV.setText(mammal_attribute.OBS_TY_ETC)
                }

                mamindicntET.setText(mammal_attribute.INDI_CNT.toString())

                mamobptcharET.setText(mammal_attribute.OB_PT_CHAR)

                mamunusnoteET.setText(mammal_attribute.UNUS_NOTE)

                mamunspecET.setText(mammal_attribute.UN_SPEC)
                mamunspecreET.setText(mammal_attribute.UN_SPEC_RE)

                mamtreasyET.setText(mammal_attribute.TR_EASY)
                mamtreasyreET.setText(mammal_attribute.TR_EASY_RE)

                val id = mammal_attribute.id

                if(mammal_attribute.TEMP_YN.equals("N")){
                    dbManager.deletemammal_attribute(mammal_attribute,id)
                }

                if(mammal_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(mammal_attribute)
                }

                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM + "/imges")
                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/")
                var tmpfileList = tmpfiles.listFiles()

                if (fileList != null) {
                    for (i in 0..fileList.size - 1) {
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/"
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
                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/mammalia/imges" ,   pk +"_" + (i+1) + ".png")

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

                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/mammalia/imges/" + pk + "_" + (j + 1).toString() + ".png")) {
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
                        , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27))

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
                        , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27))

                dataArray.add(mammal_attribute)
            }

            var mammal_attribute:Mammal_attribute = Mammal_attribute(null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null)

            mammal_attribute.id = keyId + page.toString()

            mammal_attribute.GROP_ID = keyId

            mammal_attribute.PRJ_NAME = ""

            mammal_attribute.INV_REGION = maminvregionET.text.toString()

            mammal_attribute.INV_DT = Utils.todayStr()


            if(maminvpersonTV.text == null){
                mammal_attribute.INV_PERSON = userName
            }else {
                mammal_attribute.INV_PERSON = maminvpersonTV.text.toString()
            }

            mammal_attribute.WEATHER = mamweatherET.text.toString()
            mammal_attribute.WIND = mamwindET.text.toString()
            mammal_attribute.WIND_DIRE = mamwinddireET.text.toString()

            if(mamtemperatureET.text.isNotEmpty()) {
                mammal_attribute.TEMPERATUR = mamtemperatureET.text.toString().toFloat()
            }

            mammal_attribute.INV_TM = mammaltimeTV.text.toString()

            if(mammalnumTV.text.isNotEmpty()) {
                mammal_attribute.NUM = mammalnumTV.text.toString().toInt()
            }

            mammal_attribute.ETC = mametcET.text.toString()

            mammal_attribute.NUM = 0

            mammal_attribute.INV_TM = Utils.timeStr()

            mammal_attribute.SPEC_NM = mamspecnmET.text.toString()
            mammal_attribute.FAMI_NM = mamfaminmTV.text.toString()
            mammal_attribute.SCIEN_NM = mamsciennmTV.text.toString()

            mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
            mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()

            if(mamindicntET.text.isNotEmpty()) {
                mammal_attribute.INDI_CNT = mamindicntET.text.toString().toInt()
            }

            mammal_attribute.OB_PT_CHAR = mamobptcharET.text.toString()
            mammal_attribute.UNUS_NOTE = mamunusnoteET.text.toString()

            if(mamgpslonTV.text.toString() != "" && mamgpslatTV.text.toString() != ""){
                mammal_attribute.GPS_LAT = lat.toFloat()
                mammal_attribute.GPS_LON = log.toFloat()
            }

            mammal_attribute.UN_SPEC = mamunspecET.text.toString()
            mammal_attribute.UN_SPEC_RE = mamunspecreET.text.toString()

            mammal_attribute.TR_EASY = mamtreasyET.text.toString()
            mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()


            mammal_attribute.TEMP_YN = "N"

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
                        , data2.getFloat(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getString(26),data2.getString(27))

                dataArray.add(mammal_attribute)

            }

            if(page!! < dataArray.size){
                page = page!! + 1
            }

            mampageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)


        }

        btn_mammalSave1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        var mammal_attribute:Mammal_attribute = Mammal_attribute(null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

                        mammal_attribute.GROP_ID = keyId

                        mammal_attribute.PRJ_NAME = ""

                        mammal_attribute.INV_REGION = maminvregionET.text.toString()
                        mammal_attribute.INV_DT = Utils.todayStr()

                        if(maminvpersonTV.text == null){
                            mammal_attribute.INV_PERSON = userName
                        }else {
                            mammal_attribute.INV_PERSON = maminvpersonTV.text.toString()
                        }

                        mammal_attribute.WEATHER = mamweatherET.text.toString()
                        mammal_attribute.WIND = mamwindET.text.toString()
                        mammal_attribute.WIND_DIRE = mamwinddireET.text.toString()

                        if(mamtemperatureET.text.isNotEmpty()) {
                            mammal_attribute.TEMPERATUR = mamtemperatureET.text.toString().toFloat()
                        }

                        mammal_attribute.ETC = mametcET.text.toString()

                        if(mammalnumTV.text.isNotEmpty()) {
                            mammal_attribute.NUM = mammalnumTV.text.toString().toInt()
                        }

                        mammal_attribute.INV_TM = Utils.timeStr()

                        mammal_attribute.SPEC_NM = mamspecnmET.text.toString()
                        mammal_attribute.FAMI_NM = mamfaminmTV.text.toString()
                        mammal_attribute.SCIEN_NM = mamsciennmTV.text.toString()

                        if(mammalobstyTV.text != null && !mammalobstyET.text.equals("")){
                            mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
                            mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()
                        }else {
                            mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
                            mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()
                        }

                        if(mamindicntET.text.isNotEmpty()) {
                            mammal_attribute.INDI_CNT = mamindicntET.text.toString().toInt()
                        }

                        mammal_attribute.OB_PT_CHAR = mamobptcharET.text.toString()
                        mammal_attribute.UNUS_NOTE = mamunusnoteET.text.toString()

                        if(mamgpslonTV.text.toString() != "" && mamgpslatTV.text.toString() != ""){
                            mammal_attribute.GPS_LAT = lat.toFloat()
                            mammal_attribute.GPS_LON = log.toFloat()
                        }

                        mammal_attribute.UN_SPEC = mamunspecET.text.toString()
                        mammal_attribute.UN_SPEC_RE = mamunspecreET.text.toString()

                        mammal_attribute.TR_EASY = mamtreasyET.text.toString()
                        mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()

                        mammal_attribute.TEMP_YN = "Y"

                        if (chkdata) {

                            if(pk != null){
                                dbManager.updatemammal_attribute(mammal_attribute,pk)
                            }

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/")
                            val pathdir = path.listFiles()

                            if(pathdir != null) {
                                for (i in 0..pathdir.size-1) {

                                    for(j in 0..pathdir.size-1) {

                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/mammalia/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                            pathdir.get(i).canonicalFile.delete()

                                            println("delete ===============")

                                        }
                                    }

                                }
                            }

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/"
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

                            dbManager.insertmammal_attribute(mammal_attribute);

                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            sdPath += "/ecology/tmps/" + mammal_attribute.INV_DT +"."+ mammal_attribute.INV_TM + "/imges"
                            val mammalia = File(sdPath)
                            mammalia.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                            val file = File(sdPath)
                            file.mkdir();
                            //이미 있다면 삭제. 후 생성
                            setDirEmpty(sdPath)

                            sdPath+="/"

                            var pathArray:ArrayList<String> = ArrayList<String>()

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + mammal_attribute.INV_DT +"."+ mammal_attribute.INV_TM + "/imges/"
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


                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }


        btn_mammalDelete.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

                            dialog.cancel()

                            var mammal_attribute: Mammal_attribute = Mammal_attribute(null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null)

                            if (pk != null) {

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/mammalia/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                }

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db.query("mammalAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                                , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                                                , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26), data.getString(27))

                                        dataArray.add(mammal_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager.deletemammal_attribute(mammal_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager.deletemammal_attribute(mammal_attribute, pk)

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
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db.query("mammalAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                            , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                                            , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26), data.getString(27))
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

        mamspecnmET.setOnClickListener {
            startDlgMammal()
        }

        mamfaminmTV.setOnClickListener {
            startDlgMammal()
        }

        mamsciennmTV.setOnClickListener {
            startDlgMammal()
        }

        mamunspecET.setOnClickListener {
            startDlgM()
        }

        mamobptcharET.setOnClickListener {

            val intent = Intent(this, DlgBiotopeTypeActivity::class.java)
            intent.putExtra("title", "비오톱유형 분류")
            intent.putExtra("table", "biotopeType")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA);

        }


        btn_mammalCancle1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->


                        val dataList: Array<String> = arrayOf("*");

                        val data= db.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null){
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getFloat(21)
                                    , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27))

                            dataArray.add(mammal_attribute)

                        }

                        if (dataArray.size == 0 ){

                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                        }

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }

        mammalobstyTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("목견");
            listItems.add("울음소리");
            listItems.add("먹이흔적");
            listItems.add("발자국");
            listItems.add("배설물");
            listItems.add("털");
            listItems.add("사체");
            listItems.add("기타");

            alert(listItems, "관찰 유형 선택", mammalobstyTV, "obsty");

        }

        mamweatherET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("맑음");
            listItems.add("흐림");
            listItems.add("안개");
            listItems.add("비");

            alert(listItems, "날씨", mamweatherET, "weather");
        }

        mamwindET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("강");
            listItems.add("중");
            listItems.add("약");
            listItems.add("무");

            alert(listItems, "바람", mamwindET, "wind");
        }

        mamwinddireET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("N");
            listItems.add("NE");
            listItems.add("E");
            listItems.add("SE");
            listItems.add("S");
            listItems.add("SW");
            listItems.add("W");
            listItems.add("NW");

            alert(listItems, "풍향", mamwinddireET, "winddire");
        }

        mamtreasyET.setOnClickListener {
            var listItems: ArrayList<String> = ArrayList();

            listItems.add("쉬움");
            listItems.add("보통");
            listItems.add("어려움");

            alert(listItems, "풍향", mamtreasyET, "treasy");
        }

        btn_add.setOnClickListener {

            var mammal_attribute:Mammal_attribute = Mammal_attribute(null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null)


            keyId = intent.getStringExtra("GROP_ID")

            mammal_attribute.GROP_ID = keyId

            mammal_attribute.PRJ_NAME = ""

            mammal_attribute.INV_REGION = maminvregionET.text.toString()
            mammal_attribute.INV_DT = Utils.todayStr()

            if(maminvpersonTV.text == null){
                mammal_attribute.INV_PERSON = userName
            }else {
                mammal_attribute.INV_PERSON = maminvpersonTV.text.toString()
            }

            mammal_attribute.WEATHER = mamweatherET.text.toString()
            mammal_attribute.WIND = mamwindET.text.toString()
            mammal_attribute.WIND_DIRE = mamwinddireET.text.toString()

            if(mamtemperatureET.text.isNotEmpty()) {
                mammal_attribute.TEMPERATUR = mamtemperatureET.text.toString().toFloat()
            }

            mammal_attribute.ETC = mametcET.text.toString()

            if(mammalnumTV.text.isNotEmpty()) {
                mammal_attribute.NUM = mammalnumTV.text.toString().toInt()
            }

            mammal_attribute.INV_TM = Utils.timeStr()

            mammal_attribute.SPEC_NM = mamspecnmET.text.toString()
            mammal_attribute.FAMI_NM = mamfaminmTV.text.toString()
            mammal_attribute.SCIEN_NM = mamsciennmTV.text.toString()

            if(mammalobstyTV.text != null && !mammalobstyET.text.equals("")){
                mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
                mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()
            }else {
                mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
                mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()
            }

            if(mamindicntET.text.isNotEmpty()) {
                mammal_attribute.INDI_CNT = mamindicntET.text.toString().toInt()
            }

            mammal_attribute.OB_PT_CHAR = mamobptcharET.text.toString()
            mammal_attribute.UNUS_NOTE = mamunusnoteET.text.toString()

            if(mamgpslonTV.text.toString() != "" && mamgpslatTV.text.toString() != ""){
                mammal_attribute.GPS_LAT = lat.toFloat()
                mammal_attribute.GPS_LON = log.toFloat()
            }

            mammal_attribute.UN_SPEC = mamunspecET.text.toString()
            mammal_attribute.UN_SPEC_RE = mamunspecreET.text.toString()

            mammal_attribute.TR_EASY = mamtreasyET.text.toString()
            mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()

            mammal_attribute.TEMP_YN = "Y"

            if (chkdata) {

                if(pk != null){
                    dbManager.updatemammal_attribute(mammal_attribute,pk)
                }

                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/")
                val pathdir = path.listFiles()

                if(pathdir != null) {
                    for (i in 0..pathdir.size-1) {

                        for(j in 0..pathdir.size-1) {

                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/mammalia/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                pathdir.get(i).canonicalFile.delete()

                                println("delete ===============")

                            }
                        }

                    }
                }

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "mammalia/imges/"
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

                dbManager.insertmammal_attribute(mammal_attribute);

                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                sdPath += "/ecology/tmps/" + mammal_attribute.INV_DT +"."+ mammal_attribute.INV_TM + "/imges"
                val mammalia = File(sdPath)
                mammalia.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                val file = File(sdPath)
                file.mkdir();
                //이미 있다면 삭제. 후 생성
                setDirEmpty(sdPath)

                sdPath+="/"

                var pathArray:ArrayList<String> = ArrayList<String>()

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + mammal_attribute.INV_DT +"."+ mammal_attribute.INV_TM + "/imges/"
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



    fun clear(){

        mammalnumTV.setText("")
        maminvdtTV.setText(Utils.todayStr())

        mamweatherET.setText("")
        mamwindET.setText("")
        mamwinddireET.setText("")

        mamtemperatureET.setText("")
        mametcET.setText("")

        mamspecnmET.setText("")
        mamfaminmTV.setText("")
        mamsciennmTV.setText("")


        mammalobstyTV.setText("")

        mamindicntET.setText("")

        mamobptcharET.setText("")

        mamunusnoteET.setText("")

        mamunspecET.setText("")
        mamunspecreET.setText("")

        mamtreasyET.setText("")
        mamtreasyreET.setText("")

        addPicturesLL!!.removeAllViews()

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
                    , data.getFloat(22), data.getString(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27))

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

            mamfaminmTV.setText(mammal_attribute.FAMI_NM)
            if(mamfaminmTV.text == null){
                mamfaminmTV.setText("")
            }

            mamsciennmTV.setText(mammal_attribute.SCIEN_NM)
            if(mamsciennmTV.text == null){
                mamsciennmTV.setText("")
            }

            mammalobstyTV.setText(mammal_attribute.OBS_TY)
            if(mammalobstyTV.text == null){
                mammalobstyTV.setText("")
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
            if ("obsty" == type) {
                if (selectItem == "기타") {
                    mammalobstyET.visibility = View.VISIBLE
                    mammalobstyTV.visibility = View.GONE
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


            mamgpslatTV.setText(lat)
            mamgpslonTV.setText(log)

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
    }

    fun startDlgMammal(){
        val intent = Intent(context, DlgMammalActivity::class.java)
        startActivityForResult(intent, SET_MAMMAL);
    }

    fun startDlgM(){
        val intent = Intent(context, DlgMammalActivity::class.java)
        startActivityForResult(intent, SET_UNSPEC);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var biotopeType : BiotopeType

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_DATA -> {

                    if(data!!.getSerializableExtra("biotopeType") != null){
                        biotopeType = data!!.getSerializableExtra("biotopeType") as BiotopeType

                        mamobptcharET.setText(biotopeType.CONTENT)
                    }

                }

                SET_MAMMAL -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    mamspecnmET.text = name
                    mamfaminmTV.text = family_name
                    mamsciennmTV.text = zoological

                };

                SET_UNSPEC -> {
                    var name = data!!.getStringExtra("name");

                    mamunspecET.text = name
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
