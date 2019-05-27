package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
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
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
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
import hntecology.ecology.model.Base
import hntecology.ecology.model.BiotopeType
import hntecology.ecology.model.Insect_attribute
import hntecology.ecology.model.Mammal_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_mammalia_ex.*
import kotlinx.android.synthetic.main.activity_mammalia_ex.confmodTV
import kotlinx.android.synthetic.main.activity_mammalia_ex.prjnameET
import kotlinx.android.synthetic.main.activity_mammalia_ex.resetBT
import kotlinx.android.synthetic.main.activity_stock.*
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
    val SET_STANDARD = 500

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

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var imageUri:Uri? = null

    var invtm = ""

    var prjname = ""

    var INV_REGION = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mammalia_ex)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
//        this.setFinishOnTouchOutside(true);

        maminvdtTV.text = Utils.todayStr()

        var time = Utils.timeStr()
        mammaltimeTV.setText(time)
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

        userName = PrefUtils.getStringPreference(context, "name");
        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")



        maminvdtTV.setOnClickListener {
            datedlg()
        }
  /*      var today = Utils.todayStr();

        var todays = today.split("-")

        var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

        for (i in 1 until todays.size){
            texttoday += todays.get(i)
        }

        mammalnumTV.setText(texttoday + "1")*/



        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        maminvpersonTV.setText(userName)

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        var c = dbManager!!.pkNum("mammalAttribute")
        mammalnumTV.text = c.toString()

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

        if(intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null){
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            try {
                var geocoder:Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

//                    maminvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e:IOException) {
                e.printStackTrace();
            }
            convert(lat.toDouble())
            logconvert(log.toDouble())
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata= db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

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

            try {
                var geocoder:Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

//                    maminvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e:IOException) {
                e.printStackTrace();
            }

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,maminvpersonTV.text.toString(),maminvdtTV.text.toString(),mammaltimeTV.text.toString())

            dbManager!!.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {

            btn_mammalDelete.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("mammalAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var mammal_attribute: Mammal_attribute = export_attribute(data)

                maminvregionET.setText(mammal_attribute.INV_REGION)
                INV_REGION = mammal_attribute.INV_REGION.toString()
                maminvdtTV.setText(mammal_attribute.INV_DT)

                mamweatherET.setText(mammal_attribute.WEATHER)
                mamwindET.setText(mammal_attribute.WIND)
                mamwinddireET.setText(mammal_attribute.WIND_DIRE)

                mamtemperatureET.setText(mammal_attribute.TEMPERATUR.toString())
                mametcET.setText(mammal_attribute.ETC)

                prjnameET.setText(mammal_attribute.PRJ_NAME)

                mammaltimeTV.setText(mammal_attribute.INV_TM)
                var timesplit = mammal_attribute.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)

                mammalnumTV.setText(mammal_attribute.NUM.toString())

                mamspecnmET.setText(mammal_attribute.SPEC_NM)
                mamfaminmTV.setText(mammal_attribute.FAMI_NM)
                mamsciennmTV.setText(mammal_attribute.SCIEN_NM)
                endangeredTV.setText(mammal_attribute.ENDANGERED)

                mammalobstyTV.setText(mammal_attribute.OBS_TY)
              /*  if(mammal_attribute.OBS_TY_ETC != null){
                    mammalobstyTV.setText(mammal_attribute.OBS_TY_ETC)
                }*/

                mamindicntET.setText(mammal_attribute.INDI_CNT.toString())

                mamobptcharET.setText(mammal_attribute.OB_PT_CHAR)

                mamunusnoteET.setText(mammal_attribute.UNUS_NOTE)

                mamunspecET.setText(mammal_attribute.UN_SPEC)
                mamunspecreET.setText(mammal_attribute.UN_SPEC_RE)

                mamtreasyET.setText(mammal_attribute.TR_EASY)
                mamtreasyreET.setText(mammal_attribute.TR_EASY_RE)

                standardTV.setText(mammal_attribute.STANDARD)

                coordndET.setText(mammal_attribute.GPSLAT_DEG.toString())
                coordnmET.setText(mammal_attribute.GPSLAT_MIN.toString())
                coordnsET.setText(mammal_attribute.GPSLAT_SEC.toString())
                coordedET.setText(mammal_attribute.GPSLON_DEG.toString())
                coordemET.setText(mammal_attribute.GPSLON_MIN.toString())
                coordesET.setText(mammal_attribute.GPSLON_SEC.toString())



                val id = mammal_attribute.id

                if(mammal_attribute.TEMP_YN.equals("N")){
                    dbManager!!.deletemammal_attribute(mammal_attribute,id)
                }

                if(mammal_attribute.TEMP_YN.equals("Y")){
                    dataArray.add(mammal_attribute)
                }

                confmodTV.setText(mammal_attribute.CONF_MOD)
//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/mammalia/images/" + mammal_attribute.INV_DT + "_" + mammal_attribute.INV_TM + "_"+mammal_attribute.NUM)

//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM +"."+mammal_attribute.NUM+ "/images")
//                val fileList = file.listFiles()
//                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/mammalia/images/")
                val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
                var tmpfileList = tmpfiles.listFiles()

//                if (fileList != null) {
//                    for (i in 0..fileList.size - 1) {
//                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/mammalia/images/"
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
//                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/mammalia/images" ,   mammal_attribute.NUM.toString() +"_"+mammal_attribute.INV_TM  +"_" + (i+1) + ".png")
//
//                        if(tmpfile.exists()){
//                            tmpfile.renameTo(tmpfile2)
//                        }
//
//                        tmpfileList = tmpfiles.listFiles()
//
//                    }
//                }

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

//                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/mammalia/images/" + mammal_attribute.NUM.toString() +"_"+mammal_attribute.INV_TM  +"_" + (j+1) + ".png")) {
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator,(j+1).toString()))) {

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
//                if (file.isDirectory){
//                    val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM + "."+mammal_attribute.NUM)
//                    path.deleteRecursively()
//                }
            }
            data.close()

        }

        resetBT.setOnClickListener {
            mammalobstyRL.visibility = View.GONE
            mammalobstyTV.visibility = View.VISIBLE
            mammalobstyTV.setText("")
        }

        mamleftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data= db!!.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var mammal_attribute: Mammal_attribute = export_attribute(data)

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
            data.close()
        }

        mamrightLL.setOnClickListener {
            clear()

            val dataList: Array<String> = arrayOf("*");

            val data= db!!.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var mammal_attribute: Mammal_attribute =export_attribute(data)

                dataArray.add(mammal_attribute)
            }

            var mammal_attribute:Mammal_attribute =null_attribute()

            mammal_attribute.id = keyId + page.toString()

            mammal_attribute.GROP_ID = keyId
            mammal_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr").replace(":","")
            mammal_attribute.CURRENT_TM = Utils.current_tm()

            mammal_attribute.PRJ_NAME = prjnameET.text.toString()

            mammal_attribute.INV_REGION = maminvregionET.text.toString()

            mammal_attribute.INV_DT =  maminvdtTV.text.toString()

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

            mammal_attribute.SPEC_NM = mamspecnmET.text.toString()
            mammal_attribute.FAMI_NM = mamfaminmTV.text.toString()
            mammal_attribute.SCIEN_NM = mamsciennmTV.text.toString()

            if (mammalobstyET.text.toString().count()>0){
                mammal_attribute.OBS_TY = mammalobstyET.text.toString()
            }else{
                mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
            }
//            mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()

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
                dbManager!!.insertmammal_attribute(mammal_attribute)
                page = page!! + 1
            }

            val data2= db!!.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null){
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var mammal_attribute: Mammal_attribute = export_attribute(data)

                dataArray.add(mammal_attribute)

            }

            if(page!! < dataArray.size){
                page = page!! + 1
            }

            mampageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)

            data.close()


        }

        btn_mammalSave1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        var mammal_attribute:Mammal_attribute =null_attribute()

                        keyId = intent.getStringExtra("GROP_ID")

                        mammal_attribute.GROP_ID = keyId
                        mammal_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr").replace(":","")
                        mammal_attribute.CURRENT_TM = Utils.current_tm()

                        val prj = prjnameET.text.toString()
                        if (prj == prjname){
                            mammal_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            mammal_attribute.PRJ_NAME = prjnameET.text.toString()
                        }

//                        mammal_attribute.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            mammal_attribute.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            mammal_attribute.PRJ_NAME = prjname
//                        }

//                        mammal_attribute.INV_REGION = maminvregionET.text.toString()
                        if (maminvregionET.length() > 0){
                            mammal_attribute.INV_REGION = maminvregionET.text.toString();
                        } else {
                            mammal_attribute.INV_REGION = INV_REGION
                        }
                        mammal_attribute.INV_DT =  maminvdtTV.text.toString()

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
                        if (coordndET.text.isNotEmpty()) {
                            mammal_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                        }
                        if (coordnmET.text.isNotEmpty()) {
                            mammal_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                        }
                        if (coordnsET.text.isNotEmpty()) {
                            mammal_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            mammal_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
                        }
                        if (coordemET.text.isNotEmpty()) {
                            mammal_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                        }
                        if (coordesET.text.isNotEmpty()) {
                            mammal_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                        }
                        mammal_attribute.MJ_ACT_PR = standardTV.text.toString()

                        mammal_attribute.INV_TM = mammaltimeTV.text.toString()

                        mammal_attribute.SPEC_NM = mamspecnmET.text.toString()
                        if (mamspecnmtmp.length() >  0){
                            mammal_attribute.SPEC_NM = mamspecnmtmp.text.toString()
                        }
                        mammal_attribute.FAMI_NM = mamfaminmTV.text.toString()
                        mammal_attribute.SCIEN_NM = mamsciennmTV.text.toString()
                        mammal_attribute.ENDANGERED = endangeredTV.text.toString()


                        mammal_attribute.OBS_TY = mammalobstyTV.text.toString()

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

                        mammal_attribute.STANDARD = standardTV.text.toString()

                        mammal_attribute.TR_EASY = mamtreasyET.text.toString()
                        mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()

                        mammal_attribute.TEMP_YN = "Y"

                        mammal_attribute.CONF_MOD = "N"

                        mammal_attribute.GEOM = log.toString() + " " + lat.toString()

                        if (chkdata) {

                            if(pk != null){

                                val CONF_MOD = confmodTV.text.toString()

                                if(CONF_MOD == "C" || CONF_MOD == "N"){
                                    mammal_attribute.CONF_MOD = "M"
                                }

                                dbManager!!.updatemammal_attribute(mammal_attribute,pk)
                                dbManager!!.updatecommonmammal(mammal_attribute,keyId)
                            }

//                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
////                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/mammalia/images/")
//                            val pathdir = path.listFiles()
//
//                            if(pathdir != null) {
//                                for (i in 0..pathdir.size-1) {
//
//                                    for(j in 0..pathdir.size-1) {
//
//                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator + mammal_attribute.NUM.toString() + "_" + invtm +"_" + (j+1) + ".png")) {
////                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/mammalia/images/" + mammal_attribute.NUM.toString() +"_"+mammal_attribute.INV_TM  +"_" + (j+1) + ".png")) {
//
//                                            pathdir.get(i).canonicalFile.delete()
//
//                                            println("delete ===============")
//
//                                        }
//                                    }
//
//                                }
//                            }
//
//                            for(i   in 0..images!!.size-1){
//
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/mammalia/images/"
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
//                                saveVitmapToFile(images!!.get(i),outPath+mammal_attribute.NUM.toString() +"_"+mammal_attribute.INV_TM  +"_" + (i+1) + ".png")
//
//                            }
//
//                            val deletedir = path.listFiles()
//                            if (deletedir.size == 0){
//                                if (path.isDirectory){
//                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                    deletepath.deleteRecursively()
//                                }
//                            }

                        } else {

                            dbManager!!.insertmammal_attribute(mammal_attribute);

//                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            sdPath += "/ecology/data/mammalia/images/" + mammal_attribute.INV_DT +"."+ mammal_attribute.INV_TM +"."+mammal_attribute.NUM
//                            val mammalia = File(sdPath)
//                            mammalia.mkdir();
////                          sdPath +="/imgs"
////                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                            val file = File(sdPath)
//                            file.mkdir();
//                            //이미 있다면 삭제. 후 생성
////                            setDirEmpty(sdPath)

//                            sdPath+="/"

//                            var pathArray:ArrayList<String> = ArrayList<String>()
//
//                            for(i   in 0..images!!.size-1){
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "/ecology/data/mammalia/images/"
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
//                                saveVitmapToFile(images!!.get(i),outPath+mammal_attribute.NUM + "_" + mammal_attribute.INV_TM+"_"+(i+1)+".png")
//
//                            }

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


        btn_mammalDelete.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

                            dialog.cancel()

                            var mammal_attribute: Mammal_attribute =null_attribute()

                            if (pk != null) {

                                val data= db!!.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                                while (data.moveToNext()) {

                                    Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                                            , data.getString(15), data.getString(16), data.getString(17),data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                                            , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27),data.getString(28),data.getString(29),data.getString(30)
                                            ,data.getInt(31), data.getInt(32),data.getFloat(33),data.getInt(34),data.getInt(35),data.getFloat(36),data.getString(37),data.getString(38),data.getString(39))
                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator + mammal_attribute.NUM.toString() + "_" + mammal_attribute.INV_TM +"_" + (j+1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }

                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory){
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM + "."+mammal_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory){
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM + "."+mammal_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                }

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("mammalAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var mammal_attribute: Mammal_attribute = export_attribute(data)
                                        dataArray.add(mammal_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager!!.deletemammal_attribute(mammal_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager!!.deletemammal_attribute(mammal_attribute, pk)

                                        var intent = Intent()

                                        intent.putExtra("markerid", markerid)

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

            if(pk == null){

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("mammalAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true


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

                        val data= db!!.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null){
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var mammal_attribute: Mammal_attribute = export_attribute(data)

                            dataArray.add(mammal_attribute)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null){

                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
                            val pathdir = path.listFiles()

                            if (pathdir != null) {

                                val deletedir = path.listFiles()
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM + "."+mammal_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + mammal_attribute.INV_DT + "." + mammal_attribute.INV_TM + "."+mammal_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            }

                        }

                        finish()
                        data.close()

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

        mamspecnmreset.setOnClickListener {
            mamspecnmLL.visibility = View.GONE
            mamspecnmET.visibility = View.VISIBLE
            mamspecnmtmp.setText("")
        }

        standardTV.setOnClickListener {
            val intent = Intent(context, DlgStandardActivity::class.java)
            intent.putExtra("type", "mammal")

            startActivityForResult(intent, SET_STANDARD);
        }

        btn_add.setOnClickListener {

            var mammal_attribute:Mammal_attribute =null_attribute()

            keyId = intent.getStringExtra("GROP_ID")

            mammal_attribute.GROP_ID = keyId
            mammal_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
            mammal_attribute.CURRENT_TM = Utils.current_tm()

            val prj = prjnameET.text.toString()
            if (prj == prjname){
                mammal_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                mammal_attribute.PRJ_NAME = prjnameET.text.toString()
            }

//            mammal_attribute.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                mammal_attribute.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                mammal_attribute.PRJ_NAME = prjname
//            }


//            mammal_attribute.INV_REGION = maminvregionET.text.toString()
            if (maminvregionET.length() > 0){
                mammal_attribute.INV_REGION = maminvregionET.text.toString();
            } else {
                mammal_attribute.INV_REGION = INV_REGION
            }
            mammal_attribute.INV_DT = maminvdtTV.text.toString()

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

            mammal_attribute.INV_TM = mammaltimeTV.text.toString()

            mammal_attribute.SPEC_NM = mamspecnmET.text.toString()
            if (mamspecnmtmp.length() >  0){
                mammal_attribute.SPEC_NM = mamspecnmtmp.text.toString()
            }
            mammal_attribute.FAMI_NM = mamfaminmTV.text.toString()
            mammal_attribute.SCIEN_NM = mamsciennmTV.text.toString()
            mammal_attribute.ENDANGERED = endangeredTV.text.toString()

            if(mammalobstyTV.text != null && !mammalobstyET.text.equals("")){
                mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
//                mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()
            }else {
                mammal_attribute.OBS_TY = mammalobstyTV.text.toString()
//                mammal_attribute.OBS_TY_ETC = mammalobstyET.text.toString()
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

            mammal_attribute.STANDARD = standardTV.text.toString()
            if(mammalnumTV.text.isNotEmpty()) {
                mammal_attribute.NUM = mammalnumTV.text.toString().toInt()
            }
            if (coordndET.text.isNotEmpty()) {
                mammal_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                mammal_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                mammal_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                mammal_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                mammal_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                mammal_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            mammal_attribute.MJ_ACT_PR = standardTV.text.toString()
            mammal_attribute.TR_EASY = mamtreasyET.text.toString()
            mammal_attribute.TR_EASY_RE = mamtreasyreET.text.toString()

            mammal_attribute.TEMP_YN = "Y"

            mammal_attribute.CONF_MOD = "N"

            mammal_attribute.GEOM = log.toString() + " " + lat.toString()

            if (chkdata) {

                if(pk != null){

                    val CONF_MOD = confmodTV.text.toString()

                    if(CONF_MOD == "C" || CONF_MOD == "N"){
                        mammal_attribute.CONF_MOD = "M"
                    }

                    dbManager!!.updatemammal_attribute(mammal_attribute,pk)
                    dbManager!!.updatecommonmammal(mammal_attribute,keyId)
                }

//                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
////                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/mammalia/images/")
//                val pathdir = path.listFiles()
//
//                if(pathdir != null) {
//                    for (i in 0..pathdir.size-1) {
//
//                        for(j in 0..pathdir.size-1) {
//
//                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator + mammal_attribute.NUM.toString() + "_" + mammal_attribute.INV_TM +"_" + (j+1) + ".png")) {
////                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/mammalia/images/" +mammal_attribute.NUM.toString() +"_"+mammal_attribute.INV_TM  +"_" + (j+1) + ".png")) {
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
//                for(i   in 0..images!!.size-1){
//
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
////                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/mammalia/images/"
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
//                    saveVitmapToFile(images!!.get(i),outPath+mammal_attribute.NUM.toString() +"_"+mammal_attribute.INV_TM  +"_" + (i+1) + ".png")
//
//                }

            } else {

                dbManager!!.insertmammal_attribute(mammal_attribute);

//                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                sdPath += "/ecology/tmps/" + mammal_attribute.INV_DT +"."+ mammal_attribute.INV_TM + "."+mammal_attribute.NUM+ "/images"
//                val mammalia = File(sdPath)
//                mammalia.mkdir();
////                          sdPath +="/imgs"
////                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                val file = File(sdPath)
//                file.mkdir();
//                //이미 있다면 삭제. 후 생성
//                setDirEmpty(sdPath)
//
//                sdPath+="/"

//                var pathArray:ArrayList<String> = ArrayList<String>()
//
//                for(i   in 0..images!!.size-1){
//
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
////                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images/"
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
//                    saveVitmapToFile(images!!.get(i),outPath+mammal_attribute.NUM + "_" + mammal_attribute.INV_TM+"_"+(i+1)+".png")
//
//                }

            }

            if(intent.getStringExtra("set") != null){
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            var intent = Intent()
            intent.putExtra("export",70)
            setResult(RESULT_OK, intent)

            btn_mammalDelete.visibility = View.GONE

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

    fun clear(){
      /*  var num = mammalnumTV.text.toString()
        if (num.length > 7){
            var textnum = num.substring(num.length - 2, num.length)
            var splitnum = num.substring(0, num.length - 2)
            var plusnum = textnum.toInt() + 1
            mammalnumTV.setText(splitnum.toString() + plusnum.toString())
        } else {
            var textnum = num.substring(num.length - 1, num.length)
            var splitnum = num.substring(0, num.length - 1)
            var plusnum = textnum.toInt() + 1
            mammalnumTV.setText(splitnum.toString() + plusnum.toString())
        }*/
        var c = dbManager!!.pkNum("mammalAttribute")
        mammalnumTV.text = c.toString()

        mammaltimeTV.setText(Utils.timeStr())
        mamspecnmET.setText("")
        mamfaminmTV.setText("")
        mamsciennmTV.setText("")
        endangeredTV.setText("")

        mamspecnmtmp.setText("")

        mammalobstyTV.setText("")

        mamindicntET.setText("")

//        mamobptcharET.setText("")

//        mamunusnoteET.setText("")

        mamunspecET.setText("")
        mamunspecreET.setText("")

        mamtreasyET.setText("")
        mamtreasyreET.setText("")

        confmodTV.setText("")

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

            var mammal_attribute: Mammal_attribute = export_attribute(data)

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
                    mammalobstyTV.visibility = View.GONE
                    mammalobstyRL.visibility = View.VISIBLE
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
        intent.putExtra("title", "포유류 선택")
        intent.putExtra("table", "mammal")
        intent.putExtra("DlgHeight", 600f);
        if (mamspecnmET.text != null && mamspecnmET.text != ""){
            val spec = mamspecnmET.text.toString()
            intent.putExtra("SPEC",spec)
        }

        if (endangeredTV.text != null && endangeredTV.text != ""){
            val end = endangeredTV.text.toString()
            intent.putExtra("END",end)
        }
        startActivityForResult(intent, SET_MAMMAL);
    }

    fun startDlgM(){
        val intent = Intent(context, DlgMammalActivity::class.java)
        intent.putExtra("title", "확인되지 않는 종 선택")
        intent.putExtra("table", "mammal")
        intent.putExtra("DlgHeight", 600f);
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

                    if (data!!.getStringExtra("ENDANGERED") != null){
                        name += "(멸종위기)"
                    }

//                    var code:ArrayList<String> = ArrayList<String>()
//
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

                    mamspecnmET.text = name
                    if (name == "SP(미동정)"){
                        mamspecnmLL.visibility = View.VISIBLE
                        mamspecnmET.visibility = View.GONE
                    }
                    mamfaminmTV.text = family_name
                    mamsciennmTV.text = zoological

                };

                SET_UNSPEC -> {
                    var name = data!!.getStringExtra("name");

                    mamunspecET.text = name
                }

                SET_STANDARD -> {
                    var code:ArrayList<String> = ArrayList<String>()

                    if(data!!.getSerializableExtra("code") != null) {
                        code = data!!.getSerializableExtra("code") as ArrayList<String>
                        var codeText:String = ""

                        for (i in 0..code.size-1){
                            codeText += code.get(i) + " "
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

                            val num = mammalnumTV.text.toString()
                            var time = ""
                            time = mammaltimeTV.text.toString()
                            var timesplit = time.split(":")
                            invtm = timesplit.get(0) + timesplit.get(1)
                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
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

                            saveVitmapToFile(images!!.get(i), outPath +getTime.substring(2,8)+"_"+gettimes[1] + "_" + (i + 1) + ".png")

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

                        val num = mammalnumTV.text.toString()
                        var time = ""
                        time = mammaltimeTV.text.toString()
                        var timesplit = time.split(":")
                        invtm = timesplit.get(0) + timesplit.get(1)
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
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

                        saveVitmapToFile(images!!.get(i), outPath +getTime.substring(2,8)+"_"+gettimes[1] + "_" + (i + 1) + ".png")

                    }
                    images!!.clear()
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
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator
                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)
                    val num = mammalnumTV.text.toString()
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

                        println("getpk ----- $getPk")
                        if (getPk.size > 2) {
                            val pathPk = getPk.get(0)

                            val pathPk2 = getPk.get(1)
                            val num = mammalnumTV.text.toString()

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
                        val pathPk = getPk.get(0)

                        val pathPk2 = getPk.get(1)
                        val num = mammalnumTV.text.toString()
                        val invtm = mammaltimeTV.text.toString()

                        if (pathPk == num && pathPk2 == invtm){
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




    fun convert(d: Double): String {

        var long_d = Math.abs(d)

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
        i = Math.round(long_d.toDouble()).toInt()

        coordnsET.setText(long_d.toFloat().toString())

        s = s + i.toString() + '"';

        println("")

        println("i $i")

        println("s ::::::::::::::::::::::::::::::::::::::::::::::::::::::: " + s)

        return s
    }

    fun logconvert(d: Double): String {

        var long_d = Math.abs(d)

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
        i = Math.round(long_d.toDouble()).toInt()

        coordesET.setText(long_d.toFloat().toString())

        s = s + i.toString() + '"';

        println("")

        println("i $i")



        println("s ::::::::::::::::::::::::::::::::::::::::::::::::::::::: " + s)

        return s
    }


    fun getAttrubuteKey(): String {

        val time = System.currentTimeMillis()
//        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT
    }

    override fun onBackPressed() {

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val dataList: Array<String> = arrayOf("*");

        val data= db.query("mammalAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var mammal_attribute: Mammal_attribute = export_attribute(data)

            dataArray.add(mammal_attribute)

        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null){

            var intent = Intent()
            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                println("deletedir.size ${deletedir.size}")
                if (path.isDirectory){
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory){
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "mammalia/images"+ File.separator +keyId+ File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }

            }

        }

        data.close()

        finish()

    }

    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt()-1, days[2].toInt()).show()
    }
    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%d-%d", year, monthOfYear+1, dayOfMonth)
        maminvdtTV.text = msg
    }
    fun null_attribute(): Mammal_attribute {
        var mammal_attribute:Mammal_attribute =Mammal_attribute(null,null,null,null,null
                ,null,null,null,null,null,null,null,null,null
                ,null,null,null,null,null,null,null,null
                ,null,null,null,null,null,null,null,null
                ,null,null,null,null,null,null,null,null
                ,null,null
        )
        return mammal_attribute
    }

    fun export_attribute(data: Cursor): Mammal_attribute {
        var mammal_attribute: Mammal_attribute = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getString(17),data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26),data.getString(27),data.getString(28),data.getString(29),data.getString(30)
                ,data.getInt(31), data.getInt(32),data.getFloat(33),data.getInt(34),data.getInt(35),data.getFloat(36),data.getString(37), data.getString(38), data.getString(39))


        return mammal_attribute
    }


}
