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
import android.provider.Settings
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
import hntecology.ecology.model.Base
import hntecology.ecology.model.Flora_Attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_flora.*
import kotlinx.android.synthetic.main.activity_flora.*
import kotlinx.android.synthetic.main.activity_flora.btnPIC_FOLDER
import kotlinx.android.synthetic.main.activity_flora.confmodTV
import kotlinx.android.synthetic.main.activity_flora.coordedET
import kotlinx.android.synthetic.main.activity_flora.coordemET
import kotlinx.android.synthetic.main.activity_flora.coordesET
import kotlinx.android.synthetic.main.activity_flora.coordndET
import kotlinx.android.synthetic.main.activity_flora.coordnmET
import kotlinx.android.synthetic.main.activity_flora.coordnsET
import kotlinx.android.synthetic.main.activity_flora.fishpageTV
import kotlinx.android.synthetic.main.activity_flora.prjnameET
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class FloraActivity : Activity(), OnLocationUpdatedListener {

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

    var page: Int? = null

    val SET_FLORA = 1

    var dataArray: ArrayList<Flora_Attribute> = ArrayList<Flora_Attribute>()

    var lat: String = ""
    var log: String = ""

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

    val SET_INPUT = 2007

    var prjname = ""

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flora)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

        progressDialog = ProgressDialog(context)

//        this.setFinishOnTouchOutside(true);



        florainvdvET.setText(Utils.todayStr())
        var time = Utils.timeStr()
        florainvtmET.setText(time)
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

        florainvtmET.setOnClickListener {
            timedlg()
        }
        userName = PrefUtils.getStringPreference(context, "name");

        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        florainvdvET.setOnClickListener {
            datedlg()
        }

        /*      var today = Utils.todayStr();

              var todays = today.split("-")

              var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

              for (i in 1 until todays.size){
                  texttoday += todays.get(i)
              }

              floranumET.setText(texttoday + "1")*/

        florainvperson.setText(userName)

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        var c = dbManager!!.pkNum("floraAttribute")
        floranumET.text = c.toString()


        var intent: Intent = getIntent();

        if (intent.getStringExtra("markerid") != null) {
            markerid = intent.getStringExtra("markerid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            floragpslatTV.setText(lat)
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            println("==============$log")
            floragpslonTV.setText(log)
        }

        if (intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    florainvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
            convert(lat.toDouble())
            logconvert(log.toDouble())
        }

        keyId = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            florainvperson.setText(base.INV_PERSON)
            florainvdvET.setText(base.INV_DT)

            floragpslatTV.setText(base.GPS_LAT)
            floragpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    florainvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, florainvperson.text.toString(), florainvdvET.text.toString(), floranumET.text.toString())

            dbManager!!.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {

            floradeleteBT.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("floraAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var flora_Attribute = ex_attribute(data)


                florainvregionET.setText(flora_Attribute.INV_REGION)
                INV_REGION = flora_Attribute.INV_REGION.toString()
                florainvdvET.setText(flora_Attribute.INV_DT)
                florainvperson.setText(flora_Attribute.INV_PERSON)
                prjnameET.setText(flora_Attribute.PRJ_NAME)

                floraweatherTV.setText(flora_Attribute.WEATHER)
                florawindTV.setText(flora_Attribute.WIND)
                florawinddireTV.setText(flora_Attribute.WIND_DIRE)
                floratemperaturTV.setText(flora_Attribute.TEMPERATUR.toString())

                coordndET.setText(flora_Attribute.GPSLAT_DEG.toString())
                coordnmET.setText(flora_Attribute.GPSLAT_MIN.toString())
                coordnsET.setText(flora_Attribute.GPSLAT_SEC.toString())
                coordedET.setText(flora_Attribute.GPSLON_DEG.toString())
                coordemET.setText(flora_Attribute.GPSLON_MIN.toString())
                coordesET.setText(flora_Attribute.GPSLON_SEC.toString())

                floraetcET.setText(flora_Attribute.ETC)

                florainvtmET.setText(flora_Attribute.INV_TM)

                floranumET.setText(flora_Attribute.NUM.toString())
                var timesplit = flora_Attribute.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)

                floraspecnmET.setText(flora_Attribute.SPEC_NM)
                florafaminmTV.setText(flora_Attribute.FAMI_NM)
                florasciennmTV.setText(flora_Attribute.SCIEN_NM)

                florafloreyynTV.setText(flora_Attribute.FLORE_YN)
                floraplantynTV.setText(flora_Attribute.PLANT_YN)

                if (flora_Attribute.HAB_ETC != null && !flora_Attribute.HAB_ETC.equals("")) {
                    florahabstatTV.setText(flora_Attribute.HAB_ETC)
                } else {
                    florahabstatTV.setText(flora_Attribute.HAB_STAT)
                }

                floracolincnt.setText(flora_Attribute.COL_IN_CNT.toString())

                florathrecauET.setText(flora_Attribute.THRE_CAU)

                floragpslatTV.setText(flora_Attribute.GPS_LAT.toString())
                floragpslonTV.setText(flora_Attribute.GPS_LON.toString())

                val id = flora_Attribute.id

                if (flora_Attribute.TEMP_YN.equals("N")) {
                    dbManager!!.deleteflora_attribute(flora_Attribute, id)
                }

                if (flora_Attribute.TEMP_YN.equals("Y")) {
                    dataArray.add(flora_Attribute)
                }

                confmodTV.setText(flora_Attribute.CONF_MOD)

//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + flora_Attribute.INV_DT + "." + flora_Attribute.INV_TM +"."+flora_Attribute.NUM+ "/images")
//                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
                var tmpfileList = tmpfiles.listFiles()


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
                        Log.d("바바33", images_path.toString())
                        for (j in 0..tmpfileList.size - 1) {


                            var add_images = tmpfileList.get(j).path.split("/")
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator, add_images[add_images.size - 1]))) {
                                //                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/flora/images/" + flora_attribute.NUM.toString() +"_"+flora_attribute.INV_TM +"_" + (j+1) + ".png")) {
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
                    Log.d("바바33", images_path.toString())
                }

                data.close()


            }

        }

        resetBT.setOnClickListener {
            florahabstatRL.visibility = View.GONE
            florahabstatTV.visibility = View.VISIBLE
            florahabstatTV.setText("")
        }

        floraleftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var flora_Attribute = ex_attribute(data)

                dataArray.add(flora_Attribute)
            }

            if (page == dataArray.size && page!! > 1) {
                page = page!! - 1
                fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)

            } else if (page!! < dataArray.size && page!! > 1) {
                page = page!! - 1
                fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

                clear()

                resetPage(page!!)

            }

        }

        florarightLL.setOnClickListener {


            clear()

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var flora_Attribute = ex_attribute(data)

                dataArray.add(flora_Attribute)
            }

            var flora_Attribute = null_attribute()

            flora_Attribute.id = keyId + page.toString()
            flora_Attribute.GROP_ID = keyId

            flora_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

            flora_Attribute.CURRENT_TM = Utils.current_tm()
            flora_Attribute.PRJ_NAME = flora_Attribute.PRJ_NAME
            flora_Attribute.INV_REGION = florainvregionET.text.toString()

            flora_Attribute.INV_DT = florainvdvET.text.toString()
            flora_Attribute.INV_PERSON = florainvperson.text.toString()

            flora_Attribute.WEATHER = floraweatherTV.text.toString()
            flora_Attribute.WIND = florawindTV.text.toString()
            flora_Attribute.WIND_DIRE = florawinddireTV.text.toString()

            if (floranumET.text.isNotEmpty()) {
                flora_Attribute.NUM = floranumET.text.toString().toInt()
            }

            if (floratemperaturTV.text.isNotEmpty()) {
                flora_Attribute.TEMPERATUR = floratemperaturTV.text.toString().toFloat()
            }
            if (coordndET.text.isNotEmpty()) {
                flora_Attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                flora_Attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                flora_Attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                flora_Attribute.GPSLAT_DEG = coordedET.text.toString().toInt()
            }
            if (coordedET.text.isNotEmpty()) {
                flora_Attribute.GPSLON_MIN = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                flora_Attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                flora_Attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            flora_Attribute.ETC = floraetcET.text.toString()

            flora_Attribute.INV_TM = florainvtmET.text.toString()

            flora_Attribute.SPEC_NM = floraspecnmET.text.toString()
            if (floraspecnmtmp.length() > 0) {
                flora_Attribute.SPEC_NM = floraspecnmtmp.text.toString()
            }
            flora_Attribute.FAMI_NM = florafaminmTV.text.toString()
            flora_Attribute.SCIEN_NM = florasciennmTV.text.toString()

            flora_Attribute.FLORE_YN = florafloreyynTV.text.toString()
            flora_Attribute.PLANT_YN = floraplantynTV.text.toString()


            flora_Attribute.HAB_STAT = florahabstatTV.text.toString()
            flora_Attribute.HAB_ETC = florahabstatET.text.toString()

            if (floracolincnt.text.isNotEmpty()) {
                flora_Attribute.COL_IN_CNT = floracolincnt.text.toString().toInt()
            }

            flora_Attribute.THRE_CAU = florathrecauET.text.toString()

            if (floragpslatTV.text.isNotEmpty()) {
                flora_Attribute.GPS_LAT = 0F
            }

            if (floragpslatTV.text.isNotEmpty()) {
                flora_Attribute.GPS_LON = 0F
            }

            flora_Attribute.TEMP_YN = "N"

            if (page == dataArray.size) {
                dbManager!!.insertflora_attribute(flora_Attribute)
                page = page!! + 1
            }

            if (dataArray != null) {
                dataArray.clear()
            }

            val data2 = db!!.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            while (data2.moveToNext()) {

                chkdata = true

                var flora_Attribute: Flora_Attribute = Flora_Attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getString(7),
                        data2.getString(8), data2.getFloat(9), data2.getString(10), data2.getInt(11), data2.getString(12), data2.getString(13), data2.getString(14)
                        , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getInt(20), data2.getString(21)
                        , data2.getFloat(22), data2.getFloat(23), data2.getString(24), data.getString(25), data.getString(26)
                        , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31), data.getFloat(32),data.getString(33),data.getString(34))

                dataArray.add(flora_Attribute)
            }

            if (page!! < dataArray.size) {
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

                        var flora_Attribute = null_attribute()

                        keyId = intent.getStringExtra("GROP_ID")
                        flora_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

                        flora_Attribute.CURRENT_TM = Utils.current_tm()
                        flora_Attribute.GROP_ID = keyId

                        val prj = prjnameET.text.toString()
                        if (prj == prjname) {
                            flora_Attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            flora_Attribute.PRJ_NAME = prjnameET.text.toString()
                        }

//                        flora_Attribute.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            flora_Attribute.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            flora_Attribute.PRJ_NAME = prjname
//                        }

//                        flora_Attribute.INV_REGION = florainvregionET.text.toString()
                        if (florainvregionET.length() > 0) {
                            flora_Attribute.INV_REGION = florainvregionET.text.toString();
                        } else {
                            flora_Attribute.INV_REGION = INV_REGION
                        }

                        flora_Attribute.INV_DT = florainvdvET.text.toString()

                        if (florainvperson.text == null || florainvperson.text.equals("")) {
                            flora_Attribute.INV_PERSON = userName
                        } else {
                            flora_Attribute.INV_PERSON = florainvperson.text.toString()
                        }

                        flora_Attribute.WEATHER = floraweatherTV.text.toString()
                        flora_Attribute.WIND = florawindTV.text.toString()
                        flora_Attribute.WIND_DIRE = florawinddireTV.text.toString()
                        if (coordndET.text.isNotEmpty()) {
                            flora_Attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                        }
                        if (coordnmET.text.isNotEmpty()) {
                            flora_Attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                        }
                        if (coordnsET.text.isNotEmpty()) {
                            flora_Attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            flora_Attribute.GPSLAT_DEG = coordedET.text.toString().toInt()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            flora_Attribute.GPSLON_MIN = coordedET.text.toString().toInt()
                        }
                        if (coordemET.text.isNotEmpty()) {
                            flora_Attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                        }
                        if (coordesET.text.isNotEmpty()) {
                            flora_Attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                        }
                        if (floratemperaturTV.text.isNotEmpty()) {
                            flora_Attribute.TEMPERATUR = floratemperaturTV.text.toString().toFloat()
                        }

                        flora_Attribute.INV_TM = florainvtmET.text.toString()

                        flora_Attribute.ETC = floraetcET.text.toString()

                        if (floranumET.text.isNotEmpty()) {
                            flora_Attribute.NUM = floranumET.text.toString().toInt()
                        }

                        flora_Attribute.SPEC_NM = floraspecnmET.text.toString()
                        if (floraspecnmtmp.length() > 0) {
                            flora_Attribute.SPEC_NM = floraspecnmtmp.text.toString()
                        }
                        flora_Attribute.FAMI_NM = florafaminmTV.text.toString()
                        flora_Attribute.SCIEN_NM = florasciennmTV.text.toString()

                        flora_Attribute.FLORE_YN = florafloreyynTV.text.toString()
                        flora_Attribute.PLANT_YN = floraplantynTV.text.toString()

                        flora_Attribute.HAB_STAT = florahabstatTV.text.toString()
                        flora_Attribute.HAB_ETC = florahabstatET.text.toString()

                        if (floracolincnt.text.isNotEmpty()) {
                            flora_Attribute.COL_IN_CNT = floracolincnt.text.toString().toInt()
                        }

                        flora_Attribute.THRE_CAU = florathrecauET.text.toString()

                        if (floragpslatTV.text.isNotEmpty()) {
                            flora_Attribute.GPS_LAT = lat.toFloat()
                        }

                        if (floragpslonTV.text.isNotEmpty()) {
                            flora_Attribute.GPS_LON = log.toFloat()
                        }

                        flora_Attribute.TEMP_YN = "Y"

                        flora_Attribute.CONF_MOD = "N"

                        flora_Attribute.GEOM = log.toString() + " " + lat.toString()

                        if (chkdata) {

                            if (pk != null) {

                                val CONF_MOD = confmodTV.text.toString()

                                if (CONF_MOD == "C" || CONF_MOD == "N") {
                                    flora_Attribute.CONF_MOD = "M"
                                }

                                dbManager!!.updateflora_attribute(flora_Attribute, pk)
                                dbManager!!.updatecommonflora(flora_Attribute, keyId)
                            }

//                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images"+ File.separator +keyId+ File.separator)
////                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "/data/flora/images/")
//                            val pathdir = path.listFiles()
//
//                            if(pathdir != null) {
//                                for (i in 0..pathdir.size-1) {
//
//                                    for(j in 0..pathdir.size-1) {
//
//                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "flora/images"+ File.separator +keyId+ File.separator + flora_Attribute.NUM.toString() + "_" + flora_Attribute.INV_TM +"_" + (j+1) + ".png")) {
////                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/flora/images/" + flora_Attribute.NUM.toString() +"_" + flora_Attribute.INV_TM +"_"+(j+1) + ".png")) {
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
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/flora/images/"
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
//                                saveVitmapToFile(images!!.get(i),outPath+flora_Attribute.NUM.toString() +"_" + flora_Attribute.INV_TM +"_"+(i+1) + ".png")
//
//                            }
//
//                            val deletedir = path.listFiles()
//                            if (deletedir.size == 0){
//                                if (path.isDirectory){
//                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images"+ File.separator +keyId+ File.separator)
//                                    deletepath.deleteRecursively()
//                                }
//                            }

                        } else {

                            dbManager!!.insertflora_attribute(flora_Attribute);

//                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            sdPath += "/ecology/tmps/" + flora_Attribute.INV_DT +"."+ flora_Attribute.INV_TM +"."+flora_Attribute.NUM+ "/images"
//                            val flora = File(sdPath)
//                            flora.mkdir();
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
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/flora/images/"
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
//                                saveVitmapToFile(images!!.get(i),outPath+flora_Attribute.NUM + "_" + flora_Attribute.INV_TM+"_"+(i+1)+".png")
//
//                            }

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

                        val data = db!!.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var flora_Attribute = ex_attribute(data)

                            dataArray.add(flora_Attribute)
                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

                            var intent = Intent()

                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/flora/images/")
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                val deletedir = path.listFiles()
                                println("deletedir.size ${deletedir.size}")
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory) {
                                    val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    path.deleteRecursively()
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

        floradeleteBT.setOnClickListener {
            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var flora_Attribute = null_attribute()

                            if (pk != null) {

                                val data = db!!.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                                while (data.moveToNext()) {

                                    flora_Attribute = ex_attribute(data)

                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/flora/images/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator + flora_Attribute.NUM.toString() + "_" + flora_Attribute.INV_TM + "_" + (j + 1) + ".png")) {
//                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/flora/images/" + flora_Attribute.NUM.toString() +"_" + flora_Attribute.INV_TM +"_"+(j+1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        path.deleteRecursively()
                                    }
                                }

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("floraAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var flora_Attribute = ex_attribute(data)

                                        dataArray.add(flora_Attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager!!.deleteflora_attribute(flora_Attribute, pk)

                                        var intent = Intent()

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager!!.deleteflora_attribute(flora_Attribute, pk)

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

                            finish()

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

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("floraAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true


                                    if (chkdata == true) {
                                        Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        var intent = Intent()

                                        intent.putExtra("markerid", markerid)

                                        setResult(RESULT_OK, intent);
                                        finish()
                                    }

                                }

                                if (intent.getStringExtra("id") == null) {
                                    var intent = Intent()

                                    intent.putExtra("markerid", markerid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }

                            }
                        })
                                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                            val alert = builder.create()
                            alert.show()

                        }

            florafloreyynTV.setOnClickListener {

                var listItems: ArrayList<String> = ArrayList();
                listItems.add("1");
                listItems.add("0");

                alert(listItems, "개화 여부 선택", florafloreyynTV, "floreyyn");

            }

            floraplantynTV.setOnClickListener {

                var listItems: ArrayList<String> = ArrayList();
                listItems.add("1");
                listItems.add("0");

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

            floraspecnmreset.setOnClickListener {
                floraspecnmLL.visibility = View.GONE
                floraspecnmET.visibility = View.VISIBLE
                floraspecnmtmp.setText("")
            }

            floraaddBT.setOnClickListener {
                var flora_Attribute = null_attribute()

                keyId = intent.getStringExtra("GROP_ID")

                flora_Attribute.GROP_ID = keyId
                flora_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
                flora_Attribute.CURRENT_TM = Utils.current_tm()
                val prj = prjnameET.text.toString()
                if (prj == prjname) {
                    flora_Attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                } else {
                    flora_Attribute.PRJ_NAME = prjnameET.text.toString()
                }

//            flora_Attribute.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                flora_Attribute.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                flora_Attribute.PRJ_NAME = prjname
//            }

//            flora_Attribute.INV_REGION = florainvregionET.text.toString()
                if (florainvregionET.length() > 0) {
                    flora_Attribute.INV_REGION = florainvregionET.text.toString();
                } else {
                    flora_Attribute.INV_REGION = INV_REGION
                }

                flora_Attribute.INV_DT =florainvdvET.text.toString()

                if (florainvperson.text == null || florainvperson.text.equals("")) {
                    flora_Attribute.INV_PERSON = userName
                } else {
                    flora_Attribute.INV_PERSON = florainvperson.text.toString()
                }

                flora_Attribute.WEATHER = floraweatherTV.text.toString()
                flora_Attribute.WIND = florawindTV.text.toString()
                flora_Attribute.WIND_DIRE = florawinddireTV.text.toString()
                if (coordndET.text.isNotEmpty()) {
                    flora_Attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                }
                if (coordnmET.text.isNotEmpty()) {
                    flora_Attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                }
                if (coordnsET.text.isNotEmpty()) {
                    flora_Attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                }
                if (coordedET.text.isNotEmpty()) {
                    flora_Attribute.GPSLAT_DEG = coordedET.text.toString().toInt()
                }
                if (coordedET.text.isNotEmpty()) {
                    flora_Attribute.GPSLON_MIN = coordedET.text.toString().toInt()
                }
                if (coordemET.text.isNotEmpty()) {
                    flora_Attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                }
                if (coordesET.text.isNotEmpty()) {
                    flora_Attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                }
                if (floratemperaturTV.text.isNotEmpty()) {
                    flora_Attribute.TEMPERATUR = floratemperaturTV.text.toString().toFloat()
                }

                flora_Attribute.INV_TM = florainvtmET.text.toString()

                flora_Attribute.ETC = floraetcET.text.toString()

                if (floranumET.text.isNotEmpty()) {
                    flora_Attribute.NUM = floranumET.text.toString().toInt()
                }

                flora_Attribute.SPEC_NM = floraspecnmET.text.toString()
                if (floraspecnmtmp.length() > 0) {
                    flora_Attribute.SPEC_NM = floraspecnmtmp.text.toString()
                }
                flora_Attribute.FAMI_NM = florafaminmTV.text.toString()
                flora_Attribute.SCIEN_NM = florasciennmTV.text.toString()

                flora_Attribute.FLORE_YN = florafloreyynTV.text.toString()
                flora_Attribute.PLANT_YN = floraplantynTV.text.toString()

                flora_Attribute.HAB_STAT = florahabstatTV.text.toString()
                flora_Attribute.HAB_ETC = florahabstatET.text.toString()

                if (floracolincnt.text.isNotEmpty()) {
                    flora_Attribute.COL_IN_CNT = floracolincnt.text.toString().toInt()
                }

                flora_Attribute.THRE_CAU = florathrecauET.text.toString()

                if (floragpslatTV.text.isNotEmpty()) {
                    flora_Attribute.GPS_LAT = lat.toFloat()
                }

                if (floragpslonTV.text.isNotEmpty()) {
                    flora_Attribute.GPS_LON = log.toFloat()
                }

                flora_Attribute.TEMP_YN = "Y"

                flora_Attribute.CONF_MOD = "N"

                flora_Attribute.GEOM = log.toString() + " " + lat.toString()

                if (chkdata) {

                    if (pk != null) {

                        val CONF_MOD = confmodTV.text.toString()

                        if (CONF_MOD == "C" || CONF_MOD == "N") {
                            flora_Attribute.CONF_MOD = "M"
                        }

                        dbManager!!.updateflora_attribute(flora_Attribute, pk)
                        dbManager!!.updatecommonflora(flora_Attribute, keyId)
                    }


                } else {

                    dbManager!!.insertflora_attribute(flora_Attribute);


                }

                if (intent.getStringExtra("set") != null) {
                    intent.putExtra("reset", 100)

                    setResult(RESULT_OK, intent);
                }

                floradeleteBT.visibility = View.GONE
                dbManager!!.updatecommonflora(flora_Attribute, keyId)
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
    }

    fun null_attribute(): Flora_Attribute {
        var flora_Attribute: Flora_Attribute = Flora_Attribute(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null)
        return flora_Attribute
    }

    fun ex_attribute(data: Cursor): Flora_Attribute {
        var flora_Attribute: Flora_Attribute = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26)
                , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31), data.getFloat(32), data.getString(33), data.getString(34))
        return flora_Attribute
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

    fun startDlgFlora() {
        val intent = Intent(context, DlgFloraActivity::class.java)
        if (floraspecnmET.text != null && floraspecnmET.text != "") {
            var SPEC = floraspecnmET.text.toString()
            intent.putExtra("SPEC", SPEC)
        }
        startActivityForResult(intent, SET_FLORA);
    }

    fun clear() {

        var num = floranumET.text.toString()
/*
        if (num.length > 7){
            var textnum = num.substring(num.length - 2, num.length)
            var splitnum = num.substring(0, num.length - 2)
            var plusnum = textnum.toInt() + 1
            floranumET.setText(splitnum.toString() + plusnum.toString())
        } else {
            var textnum = num.substring(num.length - 1, num.length)
            var splitnum = num.substring(0, num.length - 1)
            var plusnum = textnum.toInt() + 1
            floranumET.setText(splitnum.toString() + plusnum.toString())
        }*/
        var c = dbManager!!.pkNum("floraAttribute")
        floranumET.text = c.toString()

        floraspecnmET.setText("")
        florafaminmTV.setText("")
        florasciennmTV.setText("")

        floraspecnmtmp.setText("")


        confmodTV.setText("")

        addPicturesLL!!.removeAllViews()

    }

    fun resetPage(page: Int) {
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data = db.query("floraAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var flora_Attribute = ex_attribute(data)

            dataArray.add(flora_Attribute)

            florainvregionET.setText(flora_Attribute.INV_REGION)
            if (florainvregionET.text == null) {
                florainvregionET.setText("")
            }

            florainvdvET.setText(flora_Attribute.INV_DT)
            if (florainvdvET.text == null) {
                florainvdvET.setText("")
            }

            florainvperson.setText(flora_Attribute.INV_PERSON)
            if (florainvperson.text == null) {
                florainvperson.setText("")
            }

            floraweatherTV.setText(flora_Attribute.WEATHER)
            if (floraweatherTV.text == null) {
                floraweatherTV.setText("")
            }
            coordndET.setText(flora_Attribute.GPSLAT_DEG.toString())
            coordnmET.setText(flora_Attribute.GPSLAT_MIN.toString())
            coordnsET.setText(flora_Attribute.GPSLAT_SEC.toString())
            coordedET.setText(flora_Attribute.GPSLON_DEG.toString())
            coordemET.setText(flora_Attribute.GPSLON_MIN.toString())
            coordesET.setText(flora_Attribute.GPSLON_SEC.toString())

            florawindTV.setText(flora_Attribute.WIND)
            if (florawindTV.text == null) {
                florawindTV.setText("")
            }

            florawinddireTV.setText(flora_Attribute.WIND_DIRE)
            if (florawinddireTV.text == null) {
                florawinddireTV.setText("")
            }

            floratemperaturTV.setText(flora_Attribute.TEMPERATUR.toString())
            if (floratemperaturTV.text == null) {
                floratemperaturTV.setText("")
            }

            floraetcET.setText(flora_Attribute.ETC)
            if (floraetcET.text == null) {
                floraetcET.setText("")
            }

            floranumET.setText(flora_Attribute.NUM.toString())
            if (floranumET.text == null) {
                floranumET.setText("")
            }

            florainvtmET.setText(flora_Attribute.INV_TM)
            if (florainvtmET.text == null) {
                florainvtmET.setText("")
            }

            floraspecnmET.setText(flora_Attribute.SPEC_NM)
            if (floraspecnmET.text == null) {
                floraspecnmET.setText("")
            }

            florafaminmTV.setText(flora_Attribute.FAMI_NM)
            if (florafaminmTV.text == null) {
                florafaminmTV.setText("")
            }

            florasciennmTV.setText(flora_Attribute.SCIEN_NM)
            if (florasciennmTV.text == null) {
                florasciennmTV.setText("")
            }

            florafloreyynTV.setText(flora_Attribute.FLORE_YN)
            if (florafloreyynTV.text == null) {
                florafloreyynTV.setText("")
            }

            floraplantynTV.setText(flora_Attribute.PLANT_YN)
            if (floraplantynTV.text == null) {
                floraplantynTV.setText("")
            }

            florahabstatTV.setText(flora_Attribute.HAB_STAT)
            if (florahabstatTV.text == null) {
                florahabstatTV.setText("")
            }

            if (flora_Attribute.HAB_STAT == null) {
                florahabstatTV.setText("")
            }

            if (flora_Attribute.HAB_ETC != null && !flora_Attribute.HAB_ETC.equals("")) {
                florahabstatTV.setText(flora_Attribute.HAB_ETC)
            }

            if (flora_Attribute.HAB_ETC == null) {
                florahabstatTV.setText("")
            }

            floracolincnt.setText(flora_Attribute.COL_IN_CNT.toString())
            if (floracolincnt.text == null) {
                floracolincnt.setText("")
            }

            florathrecauET.setText(flora_Attribute.THRE_CAU)
            if (florathrecauET.text == null) {
                florathrecauET.setText("")
            }

            floragpslatTV.setText(flora_Attribute.GPS_LAT.toString())
            if (floragpslatTV.text == null) {
                floragpslatTV.setText("")
            }

            floragpslonTV.setText(flora_Attribute.GPS_LON.toString())
            if (floragpslonTV.text == null) {
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

            if ("habstat" == type) {
                if (selectItem == "기타") {
                    val intent = Intent(context, DlgInputActivity::class.java)
                    startActivityForResult(intent, SET_INPUT)
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
                SET_INPUT -> {
                    var name = data!!.getStringExtra("name");
                    florahabstatTV.text = name
                }
                SET_FLORA -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    floraspecnmET.text = name
                    if (name == "SP(미동정)") {
                        floraspecnmLL.visibility = View.VISIBLE
                        floraspecnmET.visibility = View.GONE
                    }
                    florafaminmTV.text = family_name
                    florasciennmTV.text = zoological

                };

                FROM_CAMERA -> {

                    if (resultCode == -1) {
                        val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId
                        addPicturesLL!!.removeAllViews()
                        val realPathFromURI = cameraPath!!
                        images_path!!.add(cameraPath!!)
                        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$realPathFromURI")))
                        try {

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

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        FileFilter.removeDir(outPath2)
                        images_path!!.clear()
                        val child = addPicturesLL!!.getChildCount()
                        for (i in 0 until child) {

                            println("test : $i")

                            val v = addPicturesLL!!.getChildAt(i)

                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator
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

                FROM_ALBUM -> {
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator
                    val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId

                    addPicturesLL!!.removeAllViews()
//                    images_path!!.clear()

                    val result = data!!.getStringArrayExtra("result")
                    Log.d("이미지패스", images_path.toString())
                    for (i in result.indices) {
                        val str = result[i]
                        images_path!!.add(str);
                    }
                    Log.d("이미지패스2", images_path.toString())
                    Log.d("이미지패스3", images_path!!.size.toString())
                    images!!.clear()
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
                    FileFilter.removeDir(outPath2)

                    val child = addPicturesLL!!.getChildCount()

                    images_path!!.clear()
                    println("test : $images")
                    for (i in 0 until child) {

                        println("test : $i")
/*
//                        val v = addPicturesLL!!.getChildAt(i)

//                        val num = floranumTV.text.toString()
                        var time = ""
                        time = florainvtmTV.text.toString()
                        var timesplit = time.split(":")
                        invtm = timesplit.get(0) + timesplit.get(1)


                        val outputsDir = File(outPath)

                        if (outputsDir.exists()) {

                            val files = outputsDir.listFiles()
                            if (files != null) {
                                for (i in files.indices) {
                                    println("파이즐"+files[i].toString())
                                    images_path!!.add(files[i].toString())
                                }
                            }

                        } else {
                            val made = outputsDir.mkdirs()

                        }
                        */

                        val outputsDir = File(outPath)
                        if (!outputsDir.exists()) {
                            outputsDir.mkdirs()
                        }

                        val date = Date()
                        val sdf = SimpleDateFormat("yyyyMMdd-HHmmSS")

                        val getTime = sdf.format(date)
                        var gettimes = getTime.split("-")

                        println("test : $images")
                        saveVitmapToFile(images!!.get(i), outPath + getTime.substring(2, 8) + "_" + gettimes[1] + "_" + (i + 1) + ".png")

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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator
                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    var del_images: ArrayList<String> = ArrayList();
                    try {
                        images!!.clear()
                        del_images = images_path!![tag].split("/") as ArrayList<String>
                        images_path!!.removeAt(tag)

//                    val num = floranumTV.text.toString()
                        var path = FileFilter.delete_img(outPath, del_images[del_images.size - 1])
                        Log.d("경로", path.toString())
                        var file = File(path)
                        file.delete()

                    } catch (e: IndexOutOfBoundsException) {

                    }

                    /* for (k in images_url!!.indices) {
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
                     }*/
                    for (j in images_path!!.indices) {
                        val paths = images_path!!.get(j).split("/")
                        val file_name = paths.get(paths.size - 1)
                        val getPk = file_name.split("_")
                        if (getPk.size > 2) {
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
                            val num = floranumET.text.toString()
                            val invtm = florainvtmET.text.toString()


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

    fun saveVitmapToFile(bitmap: Bitmap, filePath: String) {
        Log.d("파일", filePath.toString())
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

        images_path!!.add(filePath)

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

    override fun onBackPressed() {

        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query("floraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var flora_Attribute = ex_attribute(data)

            dataArray.add(flora_Attribute)
        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

            var intent = Intent()

            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                println("deletedir.size ${deletedir.size}")
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }

            }

        }

        data.close()

        finish()
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


    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt() - 1, days[2].toInt()).show()
    }

    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth)
        var msgs  = msg.split("-")
        var month = ""
        var day = ""
        if (msgs[1].length<2){
            month = "0"+msgs[1]
        }else{
            month = msgs[1]
        }
        if (msgs[2].length<2){
            day = "0"+msgs[2]
        }else{
            day = msgs[2]
        }
        florainvdvET.text = msgs[0]+"-"+month+"-"+day

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
            val msg = String.format("%s:%s", hour_s, min_s)
            florainvtmET.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }
}

