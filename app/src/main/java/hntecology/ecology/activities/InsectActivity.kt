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
import hntecology.ecology.model.Insect_attribute
import hntecology.ecology.model.Region
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_insect.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class InsectActivity : Activity() , OnLocationUpdatedListener{

    lateinit var context: Context;

    var userName = "";

    val REQUEST_FINE_LOCATION = 50
    val REQUEST_ACCESS_COARSE_LOCATION = 51

    val SET_DATA = 1

    var latitude = 0.0f;
    var longitude = 0.0f;

    private var progressDialog: ProgressDialog? = null

    var chkdata: Boolean = false;

    val SET_INSECT = 4;

    var keyId: String? = null;

    var pk: String? = null

    var page:Int? = null

    var dataArray:ArrayList<Insect_attribute> = ArrayList<Insect_attribute>()

    var lat:String = ""
    var log:String = ""

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

    var markerid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insect)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        userName = PrefUtils.getStringPreference(context, "name");

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        insectusernameET.setText(userName)
        insectinvdtET.setText(Utils.todayStr().toString())
        insecttimeET.setText(Utils.timeStr().toString())

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        var intent: Intent = getIntent();

        if(intent.getStringExtra("markerid") != null){
            markerid = intent.getStringExtra("markerid")
        }

        if(intent.getStringExtra("latitude")!= null){
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            insectgpslatTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            insectgpslonTV.setText(log)
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

            insectusernameET.setText(base.INV_PERSON)
            insectinvdtET.setText(base.INV_DT)
            insectnumET.setText(base.INV_TM)

            insectgpslatTV.setText(base.GPS_LAT)
            insectgpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,insectusernameET.text.toString(),insectinvdtET.text.toString(),insectnumET.text.toString())

            dbManager.insertbase(base)

        }




        if (intent.getStringExtra("id") != null) {

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("insectAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28))

                insectinvregionET.setText(insect_attribute.INV_REGION)

                insectinvdtET.setText(insect_attribute.INV_DT)

                insectusernameET.setText(insect_attribute.INV_PERSON)

                insectweatherET.setText(insect_attribute.WEATHER)
                insectwindET.setText(insect_attribute.WIND)
                insectwinddireET.setText(insect_attribute.WIND_DIRE)
                insecttemperaturET.setText(insect_attribute.TEMPERATUR.toString())
                insectetcET.setText(insect_attribute.ETC)

                insectnumET.setText(insect_attribute.id)

                insecttimeET.setText(insect_attribute.INV_TM)

                insectspecnmET.setText(insect_attribute.SPEC_NM)
                insectfaminmET.setText(insect_attribute.FAMI_NM)
                insectsciennmET.setText(insect_attribute.SCIEN_NM)

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

                if(insect_attribute.INV_MN_ETC != null && !insect_attribute.INV_MN_ETC.equals("")){
                    insectinvmeanTV.setText(insect_attribute.INV_MN_ETC)
                }else {
                    insectinvmeanTV.setText(insect_attribute.INV_MEAN)
                }

                insectunusnoteET.setText(insect_attribute.UNUS_NOTE)

                val id = insect_attribute.id

                if(insect_attribute.TEMP_YN.equals("N")){
                    dbManager.deleteinsect_attribute(insect_attribute,id)
                }

                if(insect_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(insect_attribute)
                }

                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + insect_attribute.INV_DT + "." + insect_attribute.INV_TM + "/imges")
                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/")
                var tmpfileList = tmpfiles.listFiles()

                if (fileList != null) {
                    for (i in 0..fileList.size - 1) {
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/"
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
                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/insect/imges" ,   pk +"_" + (i+1) + ".png")

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

                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/insect/imges/" + pk + "_" + (j + 1).toString() + ".png")) {
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
                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28))

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
                        , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28))

                dataArray.add(insect_attribute)
            }

            var insect_attribute: Insect_attribute = Insect_attribute(null,null,null,null,null,null,null,null,null,null
            ,null,null,null,null,null,null,null,null,null,null,null,null,null
            ,null,null,null,null,null,null)

            insect_attribute.id = keyId + page.toString()

            insect_attribute.GROP_ID = keyId

            insect_attribute.PRJ_NAME = ""

            insect_attribute.INV_REGION = insectinvregionET.text.toString()
            insect_attribute.INV_DT = Utils.todayStr()
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

            insect_attribute.INV_TM = Utils.todayStr()

            insect_attribute.SPEC_NM = insectspecnmET.text.toString()
            insect_attribute.FAMI_NM = insectfaminmET.text.toString()
            insect_attribute.SCIEN_NM = insectsciennmET.text.toString()

            if(insectindicntET.text.isNotEmpty()){
                insect_attribute.INDI_CNT = insectindicntET.text.toString().toInt()
            }

            insect_attribute.OBS_STAT = insectobsstatTV.text.toString()

            if(!insectobsstatET.text.equals("") && insectobsstatET.text != null){
                insect_attribute.OBS_ST_ETC = insectobsstatET.text.toString()
            }

            insect_attribute.USE_TAR = insectusetarTV.text.toString()
            insect_attribute.USER_TA_ETC = insectusetarET.text.toString()

            insect_attribute.MJ_ACT = insectmjactTV.text.toString()
            insect_attribute.MJ_ACT_ETC = insectmjactET.text.toString()

            insect_attribute.INV_MEAN = insectinvmeanTV.text.toString()
            insect_attribute.INV_MN_ETC = insectinvmeanTV.text.toString()

            insect_attribute.UNUS_NOTE = insectunusnoteET.text.toString()

            insect_attribute.GPS_LAT = lat.toFloat()
            insect_attribute.GPS_LON = log.toFloat()

            insect_attribute.TEMP_YN = "N"



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
                        , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27),data2.getString(28))

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
                                ,null,null,null,null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

                        insect_attribute.GROP_ID = keyId

                        insect_attribute.PRJ_NAME = ""

                        insect_attribute.INV_REGION = insectinvregionET.text.toString()

                        insect_attribute.INV_DT = Utils.todayStr()

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

                        if(insectnumET.text.isNotEmpty()){
                            insect_attribute.NUM = insectnumET.text.toString().toInt()
                        }

                        insect_attribute.INV_TM = Utils.timeStr()

                        insect_attribute.SPEC_NM = insectspecnmET.text.toString()
                        insect_attribute.FAMI_NM = insectfaminmET.text.toString()
                        insect_attribute.SCIEN_NM = insectsciennmET.text.toString()

                        if(insectindicntET.text.isNotEmpty()){
                            insect_attribute.INDI_CNT = insectindicntET.text.toString().toInt()
                        }

                        insect_attribute.OBS_STAT = insectobsstatTV.text.toString()
                        insect_attribute.OBS_ST_ETC = insectobsstatET.text.toString()

                        insect_attribute.USE_TAR = insectusetarTV.text.toString()
                        insect_attribute.USER_TA_ETC = insectusetarET.text.toString()

                        insect_attribute.MJ_ACT = insectmjactTV.text.toString()
                        insect_attribute.MJ_ACT_ETC = insectmjactET.text.toString()

                        insect_attribute.INV_MEAN = insectinvmeanTV.text.toString()
                        insect_attribute.INV_MN_ETC = insectinvmeanET.text.toString()

                        insect_attribute.UNUS_NOTE = insectunusnoteET.text.toString()

                        insect_attribute.GPS_LAT = lat.toFloat()
                        insect_attribute.GPS_LON = log.toFloat()

                        insect_attribute.TEMP_YN = "Y"

                        if (chkdata) {

                            if(pk != null){
                                dbManager.updateinsect_attribute(insect_attribute,pk)
                            }

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/")
                            val pathdir = path.listFiles()

                            if(pathdir != null) {
                                for (i in 0..pathdir.size-1) {

                                    for(j in 0..pathdir.size-1) {

                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/insect/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                            pathdir.get(i).canonicalFile.delete()

                                            println("delete ===============")

                                        }
                                    }

                                }
                            }

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/"
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

                            dbManager.insertinsect_attribute(insect_attribute);

                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            sdPath += "/ecology/tmps/" + insect_attribute.INV_DT +"."+ insect_attribute.INV_TM + "/imges"
                            val insect = File(sdPath)
                            insect.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                            val file = File(sdPath)
                            file.mkdir();
                            //이미 있다면 삭제. 후 생성
                            setDirEmpty(sdPath)

                            sdPath+="/"

                            var pathArray:ArrayList<String> = ArrayList<String>()

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + insect_attribute.INV_DT +"."+ insect_attribute.INV_TM + "/imges/"
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

            if(pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var insect_attribute: Insect_attribute = Insect_attribute(null, null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null)


                            if (pk != null) {

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/insect/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                }
                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db.query("insectAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true



                                        var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                                , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                                                , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28))

                                        dataArray.add(insect_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager.deleteinsect_attribute(insect_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager.deleteinsect_attribute(insect_attribute, pk)

                                        intent.putExtra("markerpk", markerid)

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

                                val data = db.query("insectAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var insect_attribute: Insect_attribute = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                            , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                                            , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28))
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

        insectobsstatTV.setOnClickListener {


            val intent = Intent(this, DlgInsectClassActivity::class.java)
            intent.putExtra("title", "토지이용유형 분류")
            intent.putExtra("table", "Region")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA);

        }

        insectusetarTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("인공물");
            listItems.add("식물명");
            listItems.add("흙");
            listItems.add("물");

            alert(listItems, "이용 대상 선택", insectusetarTV, "usetar");
        }

        insectmjactTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("먹이");
            listItems.add("알낳기");
            listItems.add("짝짓기");
            listItems.add("멀리 이동");
            listItems.add("가까이 이동");
            listItems.add("물먹기");
            listItems.add("휴식");
            listItems.add("기타");

            alert(listItems, "주요 행위", insectmjactTV, "mjact");
        }

        insectweatherET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("맑음");
            listItems.add("흐림");
            listItems.add("안개");
            listItems.add("비");

            alert(listItems, "날씨", insectweatherET, "weather");
        }

        insectwindET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();

            listItems.add("강");
            listItems.add("중");
            listItems.add("약");
            listItems.add("무");

            alert(listItems, "바람", insectwindET, "wind");
        }

        insectwinddireET.setOnClickListener {


            var listItems: ArrayList<String> = ArrayList();

            listItems.add("N");
            listItems.add("NE");
            listItems.add("E");
            listItems.add("SE");
            listItems.add("S");
            listItems.add("SW");
            listItems.add("W");
            listItems.add("NW");

            alert(listItems, "풍향", insectwinddireET, "winddire");

        }

        insectinvmeanTV.setOnClickListener {


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


            alert(listItems, "조사방법선택", insectinvmeanTV, "invmean");


        }

        insectspecnmET.setOnClickListener {
            startDlgInsect()
        }

        insectfaminmET.setOnClickListener {
            startDlgInsect()
        }

        insectsciennmET.setOnClickListener {
            startDlgInsect()
        }

        insectaddBT.setOnClickListener {
            var insect_attribute: Insect_attribute = Insect_attribute(null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null)

            keyId = intent.getStringExtra("GROP_ID")

            insect_attribute.GROP_ID = keyId

            insect_attribute.PRJ_NAME = ""

            insect_attribute.INV_REGION = insectinvregionET.text.toString()

            insect_attribute.INV_DT = Utils.todayStr()

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

            if(insectnumET.text.isNotEmpty()){
                insect_attribute.NUM = insectnumET.text.toString().toInt()
            }

            insect_attribute.INV_TM = Utils.timeStr()

            insect_attribute.SPEC_NM = insectspecnmET.text.toString()
            insect_attribute.FAMI_NM = insectfaminmET.text.toString()
            insect_attribute.SCIEN_NM = insectsciennmET.text.toString()

            if(insectindicntET.text.isNotEmpty()){
                insect_attribute.INDI_CNT = insectindicntET.text.toString().toInt()
            }

            insect_attribute.OBS_STAT = insectobsstatTV.text.toString()
            insect_attribute.OBS_ST_ETC = insectobsstatET.text.toString()

            insect_attribute.USE_TAR = insectusetarTV.text.toString()
            insect_attribute.USER_TA_ETC = insectusetarET.text.toString()

            insect_attribute.MJ_ACT = insectmjactTV.text.toString()
            insect_attribute.MJ_ACT_ETC = insectmjactET.text.toString()

            insect_attribute.INV_MEAN = insectinvmeanTV.text.toString()
            insect_attribute.INV_MN_ETC = insectinvmeanET.text.toString()

            insect_attribute.UNUS_NOTE = insectunusnoteET.text.toString()

            insect_attribute.GPS_LAT = lat.toFloat()
            insect_attribute.GPS_LON = log.toFloat()

            insect_attribute.TEMP_YN = "Y"

            if (chkdata) {

                if(pk != null){
                    dbManager.updateinsect_attribute(insect_attribute,pk)
                }

                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/")
                val pathdir = path.listFiles()

                if(pathdir != null) {
                    for (i in 0..pathdir.size-1) {

                        for(j in 0..pathdir.size-1) {

                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/insect/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                pathdir.get(i).canonicalFile.delete()

                                println("delete ===============")

                            }
                        }

                    }
                }

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "insect/imges/"
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

                dbManager.insertinsect_attribute(insect_attribute);

                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                sdPath += "/ecology/tmps/" + insect_attribute.INV_DT +"."+ insect_attribute.INV_TM + "/imges"
                val insect = File(sdPath)
                insect.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                val file = File(sdPath)
                file.mkdir();
                //이미 있다면 삭제. 후 생성
                setDirEmpty(sdPath)

                sdPath+="/"

                var pathArray:ArrayList<String> = ArrayList<String>()

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + insect_attribute.INV_DT +"."+ insect_attribute.INV_TM + "/imges/"
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

            if(intent.getStringExtra("id") != null){
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

    fun startDlgInsect(){
        val intent = Intent(context, DlgInsectActivity::class.java)
        startActivityForResult(intent, SET_INSECT);
    }


    fun clear(){

        insectinvdtET.setText(Utils.todayStr())

        insectweatherET.setText("")
        insectwindET.setText("")
        insectwinddireET.setText("")
        insecttemperaturET.setText("")
        insectetcET.setText("")

        insectnumET.setText("")

        insecttimeET.setText("")

        insectspecnmET.setText("")
        insectfaminmET.setText("")
        insectsciennmET.setText("")

        insectindicntET.setText("")

        insectobsstatTV.setText("")

        insectusetarTV.setText("")

        insectinvmeanET.setText("")
        insectinvmeanTV.setText("")

        insectmjactTV.setText("")

        insectunusnoteET.setText("")

        addPicturesLL!!.removeAllViews()

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
                    , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28))

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

            insectfaminmET.setText(insect_attribute.FAMI_NM)
            if(insectfaminmET.text == null){
                insectfaminmET.setText("")
            }

            insectsciennmET.setText(insect_attribute.SCIEN_NM)
            if(insectsciennmET.text == null){
                insectsciennmET.setText("")
            }

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

            insectinvmeanTV.setText(insect_attribute.INV_MEAN)
            if(insectinvmeanTV.text == null){
                insectinvmeanTV.setText("")
            }

            if(insect_attribute.INV_MN_ETC != null && !insect_attribute.INV_MN_ETC.equals("")){
                insectinvmeanTV.setText(insect_attribute.INV_MN_ETC)
                insectinvmeanET.visibility = View.GONE
            }

            insectunusnoteET.setText(insect_attribute.UNUS_NOTE)
            if(insectunusnoteET.text == null){
                insectunusnoteET.setText("")
            }

            insectgpslatTV.setText(insect_attribute.GPS_LAT.toString())
            insectgpslonTV.setText(insect_attribute.GPS_LON.toString())

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
            }else if("invmean" == type){
                if(selectItem == "기타"){
                    insectinvmeanET.visibility = View.VISIBLE
                    insectinvmeanTV.visibility = View.GONE
                }else {

                    insectinvmeanET.visibility = View.GONE
                    insectinvmeanTV.visibility = View.VISIBLE
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


            insectgpslatTV.setText(lat)
            insectgpslonTV.setText(log)


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

        var region: Region

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {


                SET_INSECT -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    insectspecnmET.text = name
                    insectfaminmET.text = family_name
                    insectsciennmET.text = zoological

                };

                SET_DATA -> {

                    if(data!!.getSerializableExtra("Region") != null) {
                        region = data!!.getSerializableExtra("Region") as Region
                        insectobsstatET.visibility = View.GONE
                        insectobsstatTV.visibility = View.VISIBLE
                        insectobsstatTV.setText(region.SMALLCATEGORY)
                    }

                    if(data!!.getIntExtra("Other",0) != null){
                        val count = data!!.getIntExtra("Other",0)

                        if(count == 1000){
                            insectobsstatET.visibility = View.VISIBLE
                            insectobsstatTV.visibility = View.GONE
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
