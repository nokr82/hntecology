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
import hntecology.ecology.model.Flora_Attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_flora.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class FloraActivity : Activity() , OnLocationUpdatedListener{

    lateinit var context: Context;

    private var progressDialog: ProgressDialog? = null

    var userName = "";

    val REQUEST_FINE_LOCATION = 50
    val REQUEST_ACCESS_COARSE_LOCATION = 51

    var latitude = 0.0f;
    var longitude = 0.0f;

    var chkdata: Boolean = false;

    var keyId: String? = null;

    var pk: String? = null

    var page:Int? = null

    val SET_FLORA = 1

    var dataArray:ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()

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
        setContentView(R.layout.activity_flora)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

        progressDialog = ProgressDialog(context)

        this.setFinishOnTouchOutside(true);

        florainvdvET.setText(Utils.todayStr())
        florainvtmET.setText(Utils.timeStr())

        userName = PrefUtils.getStringPreference(context, "name");

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        florainvperson.setText(userName)

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
            floragpslatTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            floragpslonTV.setText(log)
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

            florainvperson.setText(base.INV_PERSON)
            florainvdvET.setText(base.INV_DT)
            floranumET.setText(base.INV_TM)

            floragpslatTV.setText(base.GPS_LAT)
            floragpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,florainvperson.text.toString(),florainvdvET.text.toString(),floranumET.text.toString())

            dbManager.insertbase(base)

        }





        if (intent.getStringExtra("id") != null) {

            floradeleteBT.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("floraAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                        , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

                florainvregionET.setText(flora_Attribute.INV_REGION)
                florainvdvET.setText(flora_Attribute.INV_DT)
                florainvperson.setText(flora_Attribute.INV_PERSON)

                floraweatherTV.setText(flora_Attribute.WEATHER)
                florawindTV.setText(flora_Attribute.WIND)
                florawinddireTV.setText(flora_Attribute.WIND_DIRE)
                floratemperaturTV.setText(flora_Attribute.TEMPERATUR.toString())

                floraetcET.setText(flora_Attribute.ETC)

                florainvtmET.setText(flora_Attribute.INV_TM)

                floranumET.setText(flora_Attribute.id)

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

                val id = flora_Attribute.id

                if(flora_Attribute.TEMP_YN.equals("N")){
                    dbManager.deleteflora_attribute(flora_Attribute,id)
                }

                if(flora_Attribute.TEMP_YN.equals("Y")){
                    dataArray.add(flora_Attribute)
                }

                confmodTV.setText(flora_Attribute.CONF_MOD)

                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + flora_Attribute.INV_DT + "." + flora_Attribute.INV_TM + "/imges")
                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/")
                var tmpfileList = tmpfiles.listFiles()

                if (fileList != null) {
                    for (i in 0..fileList.size - 1) {
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/"
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
                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/flora/imges" ,   pk +"_" + (i+1) + ".png")

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

                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/flora/imges/" + pk + "_" + (j + 1).toString() + ".png")) {
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
                        , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

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
                        , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

                dataArray.add(flora_Attribute)
            }

            var flora_Attribute: Flora_Attribute = Flora_Attribute(null,null,null,null,null,null,null,null,null,null
            ,null,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null)

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
                flora_Attribute.GPS_LAT = 0F
            }

            if(floragpslatTV.text.isNotEmpty()){
                flora_Attribute.GPS_LON = 0F
            }

            flora_Attribute.TEMP_YN = "N"

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
                        , data2.getFloat(22), data2.getFloat(23),data2.getString(24),data.getString(25))

                dataArray.add(flora_Attribute)
            }

            if(page!! < dataArray.size){
                page = page!! + 1
            }

            fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)

        }

        florasaveBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var flora_Attribute: Flora_Attribute = Flora_Attribute(null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

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
                            flora_Attribute.GPS_LAT = lat.toFloat()
                        }

                        if(floragpslonTV.text.isNotEmpty()){
                            flora_Attribute.GPS_LON = log.toFloat()
                        }

                        flora_Attribute.TEMP_YN = "Y"

                        flora_Attribute.CONF_MOD = "N"

                        if (chkdata) {

                            if(pk != null){

                                val CONF_MOD = confmodTV.text.toString()

                                if(CONF_MOD == "C" || CONF_MOD == "N"){
                                    flora_Attribute.CONF_MOD = "M"
                                }


                                dbManager.updateflora_attribute(flora_Attribute,pk)
                            }

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/")
                            val pathdir = path.listFiles()

                            if(pathdir != null) {
                                for (i in 0..pathdir.size-1) {

                                    for(j in 0..pathdir.size-1) {

                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/flora/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                            pathdir.get(i).canonicalFile.delete()

                                            println("delete ===============")

                                        }
                                    }

                                }
                            }

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/"
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

                            dbManager.insertflora_attribute(flora_Attribute);

                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            sdPath += "/ecology/tmps/" + flora_Attribute.INV_DT +"."+ flora_Attribute.INV_TM + "/imges"
                            val flora = File(sdPath)
                            flora.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                            val file = File(sdPath)
                            file.mkdir();
                            //이미 있다면 삭제. 후 생성
                            setDirEmpty(sdPath)

                            sdPath+="/"

                            var pathArray:ArrayList<String> = ArrayList<String>()

                            for(i   in 0..images!!.size-1){

                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + flora_Attribute.INV_DT +"."+ flora_Attribute.INV_TM + "/imges/"
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

                        var intent = Intent()

                        intent.putExtra("export", 70);

                        setResult(RESULT_OK, intent);

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        floracancleBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->


                        val dataList: Array<String> = arrayOf("*");

                        val data= db.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null){
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                                    , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

                            dataArray.add(flora_Attribute)
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

        floradeleteBT.setOnClickListener {
            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var flora_Attribute: Flora_Attribute = Flora_Attribute(null, null, null, null, null, null, null, null, null, null
                                    , null, null, null, null, null, null, null, null, null, null, null, null
                                    , null, null, null,null)

                            if (pk != null) {

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/flora/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                }

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db.query("floraAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                                , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                                                , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

                                        dataArray.add(flora_Attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager.deleteflora_attribute(flora_Attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager.deleteflora_attribute(flora_Attribute, pk)

                                        intent.putExtra("markerid", markerid)

                                        setResult(RESULT_OK, intent);
                                        finish()
                                    }
                                }


                            } else {
                                Toast.makeText(context, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
                            }

                            finish()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

            if (pk == null) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db.query("floraAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                            , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                                            , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))
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

        floraaddBT.setOnClickListener {
            var flora_Attribute: Flora_Attribute = Flora_Attribute(null,null,null,null,null,null,null,null,null,null
                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                    ,null,null,null)

            keyId = intent.getStringExtra("GROP_ID")

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
                flora_Attribute.GPS_LAT = lat.toFloat()
            }

            if(floragpslonTV.text.isNotEmpty()){
                flora_Attribute.GPS_LON = log.toFloat()
            }

            flora_Attribute.TEMP_YN = "Y"

            flora_Attribute.CONF_MOD = "N"

            if (chkdata) {

                if(pk != null){

                    val CONF_MOD = confmodTV.text.toString()

                    if(CONF_MOD == "C" || CONF_MOD == "N"){
                        flora_Attribute.CONF_MOD = "M"
                    }

                    dbManager.updateflora_attribute(flora_Attribute,pk)
                }

                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/")
                val pathdir = path.listFiles()

                if(pathdir != null) {
                    for (i in 0..pathdir.size-1) {

                        for(j in 0..pathdir.size-1) {

                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/flora/imges/" + pk + "_" + (j + 1).toString() + ".png")) {

                                pathdir.get(i).canonicalFile.delete()

                                println("delete ===============")

                            }
                        }

                    }
                }

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "flora/imges/"
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

                dbManager.insertflora_attribute(flora_Attribute);

                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                sdPath += "/ecology/tmps/" + flora_Attribute.INV_DT +"."+ flora_Attribute.INV_TM + "/imges"
                val flora = File(sdPath)
                flora.mkdir();
//                          sdPath +="/imgs"
//                          sdPath +="/"+biotope_attribute.PIC_FOLDER

                val file = File(sdPath)
                file.mkdir();
                //이미 있다면 삭제. 후 생성
                setDirEmpty(sdPath)

                sdPath+="/"

                var pathArray:ArrayList<String> = ArrayList<String>()

                for(i   in 0..images!!.size-1){

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "tmps/" + flora_Attribute.INV_DT +"."+ flora_Attribute.INV_TM + "/imges/"
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

            floradeleteBT.visibility = View.GONE

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

    fun startDlgFlora(){
        val intent = Intent(context, DlgFloraActivity::class.java)
        startActivityForResult(intent, SET_FLORA);
    }

    fun clear(){

        florainvdvET.setText(Utils.todayStr())

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

        confmodTV.setText("")

        addPicturesLL!!.removeAllViews()

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
                    , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

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

            var str = latitude.toString() + " / " + longitude.toString()

            floragpslatTV.setText(lat)
            floragpslonTV.setText(log)

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
