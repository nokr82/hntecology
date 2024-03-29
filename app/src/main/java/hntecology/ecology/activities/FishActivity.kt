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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import au.com.objectix.jgridshift.Util
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
import kotlinx.android.synthetic.main.activity_fish.*
import kotlinx.android.synthetic.main.activity_fish.btnPIC_FOLDER
import kotlinx.android.synthetic.main.activity_fish.btn_add
import kotlinx.android.synthetic.main.activity_fish.confmodTV
import kotlinx.android.synthetic.main.activity_fish.coordedET
import kotlinx.android.synthetic.main.activity_fish.coordemET
import kotlinx.android.synthetic.main.activity_fish.coordesET
import kotlinx.android.synthetic.main.activity_fish.coordndET
import kotlinx.android.synthetic.main.activity_fish.coordnmET
import kotlinx.android.synthetic.main.activity_fish.coordnsET
import kotlinx.android.synthetic.main.activity_fish.prjnameET
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FishActivity : Activity(), OnLocationUpdatedListener {

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

    var page: Int? = null

    var dataArray: ArrayList<Fish_attribute> = ArrayList<Fish_attribute>()

    var lat: String = ""
    var log: String = ""

    var basechkdata = false

    val SET_MAMMAL = 1
    val SET_UNSPEC = 2

    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101

    var cameraPath: String? = null

    //    private var addPicturesLL: LinearLayout? = null
    private val imgSeq = 0

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null
    var values: java.util.ArrayList<Int> = java.util.ArrayList()
    var values_name = ""

    var markerid: String? = null

    var dbmanager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    val SET_COLL = 1000
    val SET_COLL2 = 1001

    var imageUri: Uri? = null

    var invtm = ""

    var prjname = ""

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fish)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        //this.setFinishOnTouchOutside(true);

        userName = PrefUtils.getStringPreference(context, "name");
        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        var today = Utils.todayStr();
        val time = Utils.timeStr()
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)


        fishinvtmTV.setText(time)

        fishinvtmTV.setOnClickListener {
            timedlg()
        }

        /*    var todays = today.split("-")

            var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

            for (i in 1 until todays.size){
                texttoday += todays.get(i)
            }
            fishnumTV.setText(texttoday + "1")*/

//        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        fishinvdtET.setText(Utils.todayStr())
        fishinvpersonET.setText(userName)

        fishinvdtET.setOnClickListener {
            datedlg()
        }

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        val SET_DATA1 = 1;

        dbmanager = DataBaseHelper(context);
        db = dbmanager!!.createDataBase();
        var c = dbmanager!!.pkNum("fishAttribute")
        fishnumTV.text = c.toString()

        var intent: Intent = getIntent();

        if (intent.getStringExtra("markerid") != null) {
            markerid = intent.getStringExtra("markerid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            fishgpslatTV.setText(lat)
            println("-----lat.......$lat")
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            fishgpslonTV.setText(log)
            println("-----lat.......$lat")
        }

        keyId = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        if (intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
//                    fishinvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
            convert(lat.toDouble())
            logconvert(log.toDouble())
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            fishinvpersonET.setText(base.INV_PERSON)
            fishinvdtET.setText(base.INV_DT)

            val time = Utils.timeStr()

            fishgpslatTV.setText(base.GPS_LAT)
            fishgpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    fishinvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, fishinvpersonET.text.toString(), fishinvdtET.text.toString(), Utils.timeStr())

            dbmanager!!.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {

            btn_fishDelete.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("fishAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var fish_attribute = export_attribute(data)

                fishinvregionET.setText(fish_attribute.INV_REGION)
                INV_REGION = fish_attribute.INV_REGION.toString()
                fishinvdtET.setText(fish_attribute.INV_DT)
                fishinvpersonET.setText(fish_attribute.INV_PERSON)
                fishinvtmTV.setText(fish_attribute.INV_TM)
                var timesplit = fish_attribute.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)

                prjnameET.setText(fish_attribute.PRJ_NAME)

                fishweatherET.setText(fish_attribute.WEATHER)
                fishwindET.setText(fish_attribute.WIND)
                fishwinddireET.setText(fish_attribute.WIND_DIRE)
                fishtemperaturET.setText(fish_attribute.TEMPERATUR.toString())

                fishetcET.setText(fish_attribute.ETC)

                fishmidrageET.setText(fish_attribute.MID_RAGE)

                fishcodenumET.setText(fish_attribute.CODE_NUM)

                val fishrivernum = fish_attribute.RIVER_NUM.toString()

                if ("" != fishrivernum && fishrivernum != null && fishrivernum.length > 0 && fishrivernum != "null") {
                    fishrivernumET.setText(fishrivernum + "차")
                }


                /*   var net_cnt = 0
                   var net_min = 0
                   net_cnt =   fish_attribute.NET_CNT!!.toInt()
                   net_min =   fish_attribute.NET_MIN!!.toInt()
                   if (net_cnt!=0&&net_min!=0){
                       if (net_cnt > net_min){
                           fishnetcntET.setText(net_cnt.toString()+"회")
                       }else{
                           fishnetminet.setText(net_min.toString()+"분")
                       }
                   }*/

                fishstreamwET.setText(fish_attribute.STREAM_W.toString())
                fishwaterwET.setText(fish_attribute.WATER_W.toString())
                fishwaterdET.setText(fish_attribute.WATER_D.toString())
                fishwatercurET.setText(fish_attribute.WATER_CUR.toString())

                boulderET.setText(fish_attribute.BOULDER.toString())
                cobbleET.setText(fish_attribute.COBBLE.toString())
                pebbleET.setText(fish_attribute.PEBBLE.toString())
                gravelET.setText(fish_attribute.GRAVEL.toString())
                sendET.setText(fish_attribute.SEND.toString())

//                rivstrTV.setText(fish_attribute.RIV_STR)
//                rivstrdetET.setText(fish_attribute.RIV_STR_IN)
//
//                if(fish_attribute.RIV_STR_IN == null || fish_attribute.RIV_STR_IN.equals("")){
//                    rivstrdetET.setText("")
//                    detailLL.visibility = View.GONE
//                }
//
//                if(fish_attribute.RIV_STR_IN != null && !fish_attribute.RIV_STR_IN.equals("")){
//                    detailLL.visibility = View.VISIBLE
//                }

                formTV.setText(fish_attribute.RIV_FORM)

                fishnumTV.setText(fish_attribute.NUM.toString())

                fishspecnmET.setText(fish_attribute.SPEC_NM)
                fishfaminmET.setText(fish_attribute.FAMI_NM)
                fishsciennmET.setText(fish_attribute.SCIEN_NM)

                fishindicntET.setText(fish_attribute.INDI_CNT.toString())

                fishunidentET.setText(fish_attribute.UNIDENT)
                if (fish_attribute.UNIDENT == null) {
                    fishunidentET.setText("")
                }

                fishtivfmchET.setText(fish_attribute.RIV_FM_CH)

                fishunfishchET.setText(fish_attribute.UN_FISH_CH)

                fishgpslonTV.setText(fish_attribute.GPS_LON.toString())
                fishgpslatTV.setText(fish_attribute.GPS_LAT.toString())

                colltoolTV.setText(fish_attribute.COLL_TOOL.toString())
//                colltool2TV.setText(fish_attribute.COLL_TOOL2.toString())

                coordndET.setText(fish_attribute.GPSLAT_DEG.toString())
                coordnmET.setText(fish_attribute.GPSLAT_MIN.toString())
                coordnsET.setText(fish_attribute.GPSLAT_SEC.toString())
                coordedET.setText(fish_attribute.GPSLON_DEG.toString())
                coordemET.setText(fish_attribute.GPSLON_MIN.toString())
                coordesET.setText(fish_attribute.GPSLON_SEC.toString())

                val id = fish_attribute.id

                if (fish_attribute.TEMP_YN.equals("N")) {
                    dbmanager!!.deletefish_attribute(fish_attribute, id)
                }

                if (fish_attribute.TEMP_YN.equals("Y")) {
                    dataArray.add(fish_attribute)
                }

                confmodTV.setText(fish_attribute.CONF_MOD)

                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
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
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator, add_images[add_images.size - 1]))) {
                                //                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/fish/images/" + fish_attribute.NUM.toString() +"_"+fish_attribute.INV_TM +"_" + (j+1) + ".png")) {
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

            }

            data.close()

        }

        hideBT.setOnClickListener {
            detailLL.visibility = View.GONE
        }

        fishleftLL.setOnClickListener {


            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var fish_attribute = export_attribute(data)
                dataArray.add(fish_attribute)

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

            data.close()

        }

        fishrightLL.setOnClickListener {

            clear()

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var fish_attribute = export_attribute(data)
                dataArray.add(fish_attribute)

            }

            var fish_attribute = null_attribute()


            fish_attribute.GROP_ID = keyId

            fish_attribute.PRJ_NAME = prjnameET.text.toString()

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
                fish_attribute.RIVER_NUM = fishrivernumET.text.toString()
            }

            fish_attribute.RIVER_NM = fishrivernmET.text.toString()

            /*     if ( fishnetcntET.text.toString().replace("회","").toInt() > fishnetminet.text.toString().replace("분","").toInt()){
                     fish_attribute.NET_CNT =  fishnetcntET.text.toString().toInt()
                 }else{
                     fish_attribute.NET_MIN =  fishnetminet.text.toString().toInt()
                 }*/
            if (fishnetcntET.text.equals("")) {
                fishnetcntET.setText("0")
            }
            if (fishnetminet.text.equals("")) {
                fishnetminet.setText("0")
            }

            if (fishnetcntET.text.toString().replace("회", "").toInt() > fishnetminet.text.toString().replace("분", "").toInt()) {
                fish_attribute.COLL_TIME = fishnetcntET.text.toString()
            } else {
                fish_attribute.COLL_TIME = fishnetminet.text.toString()
            }


//            if (fishnetcntET.text.isNotEmpty()) {
//                fish_attribute.NET_CNT = fishnetcntET.text.toString().toInt()
//            }
//
//            if (fishnetminet.text.isNotEmpty()) {
//                fish_attribute.NET_MIN = fishnetminet.text.toString().toInt()
//            }


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

//            fish_attribute.RIV_STR = rivstrTV.text.toString()
//            fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

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
                dbmanager!!.insertfish_attribute(fish_attribute)
                page = page!! + 1
            }

            val data2 = db!!.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var fish_attribute = export_attribute(data)
                dataArray.add(fish_attribute)

            }

            if (page!! < dataArray.size) {
                page = page!! + 1
            }

            fishpageTV.setText(page.toString() + " / " + dataArray.size.toString())

            resetPage(page!!)

            data.close()

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
            if (fishspecnmET.text==""){
                Toast.makeText(context, "종명을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        var fish_attribute = null_attribute()
                        keyId = intent.getStringExtra("GROP_ID")

                        fish_attribute.GROP_ID = keyId
                        fish_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

                        fish_attribute.CURRENT_TM = Utils.current_tm()
                        val prj = prjnameET.text.toString()
                        if (prj == prjname) {
                            fish_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            fish_attribute.PRJ_NAME = prjnameET.text.toString()
                        }
                        fish_attribute.RIVER_BED = values_name
//                        fish_attribute.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            fish_attribute.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            fish_attribute.PRJ_NAME = prjname
//                        }

//                        fish_attribute.INV_REGION = fishinvregionET.text.toString()
                        if (fishinvregionET.length() > 0) {
                            fish_attribute.INV_REGION = fishinvregionET.text.toString();
                        } else {
                            fish_attribute.INV_REGION = INV_REGION
                        }
                        river_bed()

                        fish_attribute.INV_DT = fishinvdtET.text.toString()

                        fish_attribute.INV_TM = fishinvtmTV.text.toString()

                        fish_attribute.INV_PERSON = fishinvpersonET.text.toString()

                        fish_attribute.WEATHER = fishweatherET.text.toString()
                        fish_attribute.WIND = fishwindET.text.toString()
                        fish_attribute.WIND_DIRE = fishwinddireET.text.toString()
                        if (coordndET.text.isNotEmpty()) {
                            fish_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                        }
                        if (coordnmET.text.isNotEmpty()) {
                            fish_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                        }
                        if (coordnsET.text.isNotEmpty()) {
                            fish_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            fish_attribute.GPSLAT_DEG = coordedET.text.toString().toInt()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            fish_attribute.GPSLON_MIN = coordedET.text.toString().toInt()
                        }
                        if (coordemET.text.isNotEmpty()) {
                            fish_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                        }
                        if (coordesET.text.isNotEmpty()) {
                            fish_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                        }
                        if (fishtemperaturET.text.isNotEmpty()) {
                            fish_attribute.TEMPERATUR = fishtemperaturET.text.toString().toFloat()
                        }

                        fish_attribute.ETC = fishetcET.text.toString()

                        fish_attribute.MID_RAGE = fishmidrageET.text.toString()
                        fish_attribute.CODE_NUM = fishcodenumET.text.toString()

                        fish_attribute.COLL_TOOL = colltoolTV.text.toString()
//                        fish_attribute.COLL_TOOL2 = colltool2TV.text.toString()


                        if (fishrivernumET.text.isNotEmpty()) {
                            fish_attribute.RIVER_NUM = fishrivernumET.text.toString()
                        }

                        fish_attribute.RIVER_NM = fishrivernmET.text.toString()

                        /* if(fishnetcntET.text.isNotEmpty()&&fishnetminet.text.isNotEmpty()){
                             if ( fishnetcntET.text.toString().replace("회","").toInt() > fishnetminet.text.toString().replace("분","").toInt()){
                                 fish_attribute.NET_CNT =  fishnetcntET.text.toString().toInt()
                             }else{
                                 fish_attribute.NET_MIN =  fishnetminet.text.toString().toInt()
                             }
                         }*/
                        if (fishnetcntET.text.equals("")) {
                            fishnetcntET.setText("0")
                        }
                        if (fishnetminet.text.equals("")) {
                            fishnetminet.setText("0")
                        }

                        if (fishnetcntET.text.toString().replace("회", "").toInt() > fishnetminet.text.toString().replace("분", "").toInt()) {
                            fish_attribute.COLL_TIME = fishnetcntET.text.toString()
                        } else {
                            fish_attribute.COLL_TIME = fishnetminet.text.toString()
                        }

//                        if (fishnetcntET.text.isNotEmpty()) {
//                            fish_attribute.NET_CNT = fishnetcntET.text.toString().toInt()
//                        }
//
//                        if (fishnetminet.text.isNotEmpty()) {
//                            fish_attribute.NET_MIN = fishnetminet.text.toString().toInt()
//                        }

                        if (fishgpslatTV.text.isNotEmpty()) {
                            fish_attribute.GPS_LAT = fishgpslatTV.text.toString().toFloat()
                        }

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

                        if (boulderET.text.isNotEmpty()) {
                            fish_attribute.BOULDER = boulderET.text.toString().toInt()
                        }

                        if (cobbleET.text.isNotEmpty()) {
                            fish_attribute.COBBLE = cobbleET.text.toString().toInt()
                        }

                        if (pebbleET.text.isNotEmpty()) {
                            fish_attribute.PEBBLE = pebbleET.text.toString().toInt()
                        }

                        if (gravelET.text.isNotEmpty()) {
                            fish_attribute.GRAVEL = gravelET.text.toString().toInt()
                        }

                        if (sendET.text.isNotEmpty()) {
                            fish_attribute.SEND = sendET.text.toString().toInt()
                        }


//                        fish_attribute.RIV_STR = rivstrTV.text.toString()
//                        fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

                        fish_attribute.RIV_FORM = formTV.text.toString()

                        if (fishnumTV.text.isNotEmpty()) {
                            fish_attribute.NUM = fishnumTV.text.toString().toInt()
                        }

                        fish_attribute.SPEC_NM = fishspecnmET.text.toString()
                        if (fishspecnmtmp.length() > 0) {
                            fish_attribute.SPEC_NM = fishspecnmtmp.text.toString()
                        }
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

                        println("------lat.$lat")
                        println("------log.$log")

                        fish_attribute.CONF_MOD = "N"

                        fish_attribute.GEOM = log.toString() + " " + lat.toString()

                        if (chkdata) {

                            if (pk != null) {

                                val CONF_MOD = confmodTV.text.toString()

                                if (CONF_MOD == "C" || CONF_MOD == "N") {
                                    fish_attribute.CONF_MOD = "M"
                                }

                                dbmanager!!.updatefish_attribute(fish_attribute, pk)
                                dbmanager!!.updatecommonfish(fish_attribute, keyId)
                            }


                        } else {

                            dbmanager!!.insertfish_attribute(fish_attribute);

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

                        val data = db!!.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var fish_attribute = export_attribute(data)
                            dataArray.add(fish_attribute)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                val deletedir = path.listFiles()
                                println("deletedir.size ${deletedir.size}")
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX) deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
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

        btn_fishDelete.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var fish_attribute = null_attribute()

                            if (pk != null) {

                                val data = db!!.query("fishAttribute", dataList, "id = '$pk'", null, null, null, "", null)

                                while (data.moveToNext()) {
                                    chkdata = true
                                    fish_attribute = export_attribute(data)
                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator + fish_attribute.NUM.toString() + "_" + fish_attribute.INV_TM + "_" + (j + 1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                }

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("fishAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var fish_attribute = export_attribute(data)

                                        dataArray.add(fish_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbmanager!!.deletefish_attribute(fish_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbmanager!!.deletefish_attribute(fish_attribute, pk)

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

            if (pk == null) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인") { dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("fishAttribute", dataList, "id = '$id'", null, null, null, "", null)

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

                                data.close()

                            }

                            if (intent.getStringExtra("id") == null) {
                                intent.putExtra("markerid", markerid)

                                setResult(RESULT_OK, intent);
                                finish()
                            }


                        }
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
            intent.putExtra("DlgHeight", 350f);
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

        fishspecnmreset.setOnClickListener {
            fishspecnmtmp.setText("")
            fishspecnmLL.visibility = View.GONE
            fishspecnmET.visibility = View.VISIBLE
        }

        btn_add.setOnClickListener {
            if (fishspecnmET.text==""){
                Toast.makeText(context, "종명을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            river_bed()

            var fish_attribute = null_attribute()

            keyId = intent.getStringExtra("GROP_ID")

            fish_attribute.GROP_ID = keyId

            val prj = prjnameET.text.toString()
            if (prj == prjname) {
                fish_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                fish_attribute.PRJ_NAME = prjnameET.text.toString()
            }
            fish_attribute.RIVER_BED = values_name
//            fish_attribute.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                fish_attribute.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                fish_attribute.PRJ_NAME = prjname
//            }

//            fish_attribute.INV_REGION = fishinvregionET.text.toString()
            if (fishinvregionET.length() > 0) {
                fish_attribute.INV_REGION = fishinvregionET.text.toString();
            } else {
                fish_attribute.INV_REGION = INV_REGION
            }

            fish_attribute.INV_DT = fishinvdtET.text.toString()

            fish_attribute.INV_TM = fishinvtmTV.text.toString()

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
                fish_attribute.RIVER_NUM = fishrivernumET.text.toString()
            }

            fish_attribute.RIVER_NM = fishrivernmET.text.toString()
            if (fishnetcntET.text.isNotEmpty() && fishnetminet.text.isNotEmpty()) {
                /* if ( fishnetcntET.text.toString().replace("회","").toInt() > fishnetminet.text.toString().replace("분","").toInt()){
                     fish_attribute.NET_CNT =  fishnetcntET.text.toString().toInt()
                 }else{
                     fish_attribute.NET_MIN =  fishnetminet.text.toString().toInt()
                 }*/
                if (fishnetcntET.text.equals("")) {
                    fishnetcntET.setText("0")
                }
                if (fishnetminet.text.equals("")) {
                    fishnetminet.setText("0")
                }

                if (fishnetcntET.text.toString().replace("회", "").toInt() > fishnetminet.text.toString().replace("분", "").toInt()) {
                    fish_attribute.COLL_TIME = fishnetcntET.text.toString()
                } else {
                    fish_attribute.COLL_TIME = fishnetminet.text.toString()
                }
            }

//            if (fishnetcntET.text.isNotEmpty()) {
//                fish_attribute.NET_CNT = fishnetcntET.text.toString().toInt()
//            }
//
//            if (fishnetminet.text.isNotEmpty()) {
//                fish_attribute.NET_MIN = fishnetminet.text.toString().toInt()
//            }

            if (fishgpslatTV.text.isNotEmpty()) {
                fish_attribute.GPS_LAT = fishgpslatTV.text.toString().toFloat()
            }

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

            if (boulderET.text.isNotEmpty()) {
                fish_attribute.BOULDER = boulderET.text.toString().toInt()
            }

            if (cobbleET.text.isNotEmpty()) {
                fish_attribute.COBBLE = cobbleET.text.toString().toInt()
            }

            if (pebbleET.text.isNotEmpty()) {
                fish_attribute.PEBBLE = pebbleET.text.toString().toInt()
            }

            if (gravelET.text.isNotEmpty()) {
                fish_attribute.GRAVEL = gravelET.text.toString().toInt()
            }

            if (sendET.text.isNotEmpty()) {
                fish_attribute.SEND = sendET.text.toString().toInt()
            }

//            fish_attribute.RIV_STR = rivstrTV.text.toString()
//            fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

            fish_attribute.RIV_FORM = formTV.text.toString()

            if (fishnumTV.text.isNotEmpty()) {
                fish_attribute.NUM = fishnumTV.text.toString().toInt()
            }



            if (coordndET.text.isNotEmpty()) {
                fish_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                fish_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                fish_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                fish_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                fish_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                fish_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            if (fishtemperaturET.text.isNotEmpty()) {
                fish_attribute.TEMPERATUR = fishtemperaturET.text.toString().toFloat()
            }

            fish_attribute.SPEC_NM = fishspecnmET.text.toString()
            if (fishspecnmtmp.length() > 0) {
                fish_attribute.SPEC_NM = fishspecnmtmp.text.toString()
            }
            fish_attribute.FAMI_NM = fishfaminmET.text.toString()
            fish_attribute.SCIEN_NM = fishsciennmET.text.toString()

            if (fishindicntET.text.isNotEmpty()) {
                fish_attribute.INDI_CNT = fishindicntET.text.toString().toInt()
            }

            fish_attribute.UNIDENT = fishunidentET.text.toString()
            fish_attribute.RIV_FM_CH = fishtivfmchET.text.toString()
            fish_attribute.UN_FISH_CH = fishunfishchET.text.toString()

            fish_attribute.SPEC_NM = fishspecnmET.text.toString()
            fish_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
            fish_attribute.CURRENT_TM = Utils.current_tm()

            fish_attribute.TEMP_YN = "Y"

            fish_attribute.GPS_LAT = lat.toFloat()
            fish_attribute.GPS_LON = log.toFloat()

            fish_attribute.CONF_MOD = "N"

            fish_attribute.COLL_TOOL = colltoolTV.text.toString()
//            fish_attribute.COLL_TOOL2 = colltool2TV.text.toString()

            fish_attribute.GEOM = log.toString() + " " + lat.toString()

            if (chkdata) {

                if (pk != null) {

                    val CONF_MOD = confmodTV.text.toString()

                    if (CONF_MOD == "C" || CONF_MOD == "N") {
                        fish_attribute.CONF_MOD = "M"
                    }

                    dbmanager!!.updatefish_attribute(fish_attribute, pk)
                }


            } else {

                dbmanager!!.insertfish_attribute(fish_attribute);

            }
            dbmanager!!.updatecommonfish(fish_attribute, keyId)

            if (intent.getStringExtra("set") != null) {
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            btn_fishDelete.visibility = View.GONE

            var intent = Intent()
            intent.putExtra("export", 70)
            setResult(RESULT_OK, intent)

          /*  if (images_path != null) {
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
            }*/

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



        fishnetcntET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var cnt = fishnetcntET.text.toString().replace("회", "").toInt()
                var min = fishnetminet.text.toString().replace("분", "")
                if (min == "") {
                    min = "0"
                }

                if (cnt < min.toInt()) {
                    colltoolTV.text = "족대"
                } else {
                    colltoolTV.text = "투망"
                }
            }
        });
        fishnetminet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var cnt = fishnetcntET.text.toString().replace("회", "")
                var min = fishnetminet.text.toString().replace("분", "").toInt()
                if (cnt == "") {
                    cnt = "0"
                }
                if (cnt.toInt() < min) {
                    colltoolTV.text = "족대"
                } else {
                    colltoolTV.text = "투망"
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })


        /* colltoolTV.setOnClickListener {
             var listItems: ArrayList<String> = ArrayList();
             listItems.add("투망 (망목 9 x 9 mm)");
             listItems.add("족대 (망목 4 x 4 mm)");

             alert(listItems, "투망,족대", colltoolTV, "colltool");

         }*/
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

    fun resetPage(page: Int) {
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val tmppages = page - 1

        val id = keyId + tmppages.toString()

        val data = db.query("fishAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var fish_attribute = export_attribute(data)
            dataArray.add(fish_attribute)

            fishinvregionET.setText(fish_attribute.INV_REGION)
            if (fishinvregionET.text == null) {
                fishinvregionET.setText("")
            }

            fishinvdtET.setText(fish_attribute.INV_DT)
            if (fishinvdtET.text == null) {
                fishinvdtET.setText(Utils.todayStr())
            }

            fishinvpersonET.setText(fish_attribute.INV_PERSON)
            if (fishinvpersonET.text == null) {
                fishinvpersonET.setText("")
            }

            fishweatherET.setText(fish_attribute.WEATHER)
            if (fishinvpersonET.text == null) {
                fishinvpersonET.setText("")
            }

            fishwindET.setText(fish_attribute.WIND)
            if (fishwindET.text == null) {
                fishwindET.setText("")
            }

            fishwinddireET.setText(fish_attribute.WIND_DIRE)
            if (fishwinddireET.text == null) {
                fishwinddireET.setText("")
            }

            fishtemperaturET.setText(fish_attribute.TEMPERATUR.toString())
            if (fishtemperaturET.text == null) {
                fishtemperaturET.setText("")
            }

            fishetcET.setText(fish_attribute.ETC)
            if (fishetcET.text == null) {
                fishetcET.setText("")
            }

            fishmidrageET.setText(fish_attribute.MID_RAGE)
            if (fishmidrageET.text == null) {
                fishmidrageET.setText("")
            }

            fishcodenumET.setText(fish_attribute.CODE_NUM)
            if (fishcodenumET.text == null) {
                fishcodenumET.setText("")
            }

            var fishrivernum = fish_attribute.RIVER_NUM.toString();

            if (null != fishrivernum && fishrivernum != "" && fishrivernum.count() > 0 && fishrivernum != "null") {
                fishrivernumET.setText(fishrivernum + "차")
            }


            fishgpslatTV.setText(fish_attribute.GPS_LAT.toString())
            if (fishgpslatTV.text == null) {
                initGPS()
            }

            fishgpslonTV.setText(fish_attribute.GPS_LON.toString())

            fishrivernmET.setText(fish_attribute.RIVER_NM)
            if (fishrivernmET.text == null) {
                fishrivernmET.setText("")
            }

            /* if (fish_attribute.NET_CNT!!.toInt() > fish_attribute.NET_MIN!!.toInt()){
                 fishnetcntET.setText(fish_attribute.NET_CNT.toString()+"회")
             }else{
                 fishnetminet.setText(fish_attribute.NET_MIN.toString()+"분")
             }*/
//            fishnetcntET.setText(fish_attribute.NET_CNT.toString())
//            if(fishnetcntET.text == null){
//                fishnetcntET.setText("")
//            }
//
//            fishnetminet.setText(fish_attribute.NET_MIN.toString())
//            if(fishnetminet.text == null){
//                fishnetminet.setText("")
//            }

            fishwaterwET.setText(fish_attribute.WATER_W.toString())
            if (fishwaterwET.text == null) {
                fishwaterwET.setText("")
            }

            fishwaterdET.setText(fish_attribute.WATER_D.toString())
            if (fishwaterdET.text == null) {
                fishwaterdET.setText("")
            }

            fishwatercurET.setText(fish_attribute.WATER_CUR.toString())
            if (fishwatercurET.text == null) {
                fishwatercurET.setText("")
            }
            coordndET.setText(fish_attribute.GPSLAT_DEG.toString())
            coordnmET.setText(fish_attribute.GPSLAT_MIN.toString())
            coordnsET.setText(fish_attribute.GPSLAT_SEC.toString())
            coordedET.setText(fish_attribute.GPSLON_DEG.toString())
            coordemET.setText(fish_attribute.GPSLON_MIN.toString())
            coordesET.setText(fish_attribute.GPSLON_SEC.toString())
//            rivstrTV.setText(fish_attribute.RIV_STR)
//            if(rivstrTV.text == null){
//                rivstrTV.setText("")
//                detailLL.visibility = View.GONE
//            }else if(rivstrTV.text == "") {
//                detailLL.visibility = View.GONE
//            }
//
//            rivstrdetET.setText(fish_attribute.RIV_STR_IN)
//            if(rivstrdetET.text == null){
//                rivstrdetET.setText("")
//                detailLL.visibility = View.GONE
//            }

            if (fish_attribute.RIV_STR_IN != null && !fish_attribute.RIV_STR_IN.equals("")) {
                detailLL.visibility = View.VISIBLE
            }

            formTV.setText(fish_attribute.RIV_FORM)
            if (formTV.text == null) {
                formTV.setText("")
            }

            fishnumTV.setText(fish_attribute.NUM.toString())
            if (fishnumTV.text == null) {
                fishnumTV.setText("")
            }

            fishspecnmET.setText(fish_attribute.SPEC_NM)
            if (fishspecnmET.text == null) {
                fishspecnmET.setText("")
            }

            fishfaminmET.setText(fish_attribute.FAMI_NM)
            if (fishfaminmET.text == null) {
                fishfaminmET.setText("")
            }

            fishsciennmET.setText(fish_attribute.SCIEN_NM)
            if (fishsciennmET.text == null) {
                fishsciennmET.setText("")
            }

            fishindicntET.setText(fish_attribute.INDI_CNT.toString())
            if (fishindicntET.text == null) {
                fishindicntET.setText("")
            }

            fishunidentET.setText(fish_attribute.UNIDENT)
            if (fishunidentET.text == null) {
                fishunidentET.setText("")
            }

            fishtivfmchET.setText(fish_attribute.RIV_FM_CH)
            if (fishtivfmchET.text == null) {
                fishtivfmchET.setText("")
            }

            fishunfishchET.setText(fish_attribute.UN_FISH_CH)
            if (fishunfishchET.text == null) {
                fishunfishchET.setText("")
            }

        }
    }

    fun clear() {

//        var num = fishnumTV.text.toString()
        /*     if (num.length > 7){
                 var textnum = num.substring(num.length - 2, num.length)
                 var splitnum = num.substring(0, num.length - 2)
                 var plusnum = textnum.toInt() + 1
                 fishnumTV.setText(splitnum.toString() + plusnum.toString())
             } else {
                 var textnum = num.substring(num.length - 1, num.length)
                 var splitnum = num.substring(0, num.length - 1)
                 var plusnum = textnum.toInt() + 1
                 fishnumTV.setText(splitnum.toString() + plusnum.toString())
             }*/
        var c = dbmanager!!.pkNum("fishAttribute")
        fishnumTV.text = c.toString()



//        fishinvtmTV.setText(Utils.timeStr())
        fishspecnmET.setText("")
        fishfaminmET.setText("")
        fishsciennmET.setText("")

        fishspecnmtmp.setText("")

        fishindicntET.setText("")

        fishunidentET.setText("")

        fishtivfmchET.setText("")

        fishunfishchET.setText("")

        confmodTV.setText("")

//        addPicturesLL!!.removeAllViews()

    }

    fun startDlgFish() {
        val intent = Intent(context, DlgFishActivity::class.java)
        if (fishspecnmET.text != null && fishspecnmET.text != "") {
            val spec = fishspecnmET.text.toString()
            intent.putExtra("SPEC", spec)
        }
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
//            if ("rivstr" == type) {
//                detailLL.visibility = View.VISIBLE
//            }else {
//                detailLL.visibility = View.GONE
//            }
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

        var common: common

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
                    if (name == "SP(미동정)") {
                        fishspecnmET.visibility = View.GONE
                        fishspecnmLL.visibility = View.VISIBLE
                    }
                    fishfaminmET.text = family_name
                    fishsciennmET.text = zoological

                }

                SET_DATA1 -> {


                    common = data!!.getSerializableExtra("common") as common

                    commonDivision = data!!.getSerializableExtra("CommonDivision") as CommonDivision

                    fishmidrageET.setText(common.title + "/" + commonDivision.title + " ( " + commonDivision.code + ")")

                    fishcodenumET.setText(commonDivision.title + " ( " + commonDivision.code + ")")

                }

                FROM_CAMERA -> {

                    if (resultCode == -1) {
                        val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId
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

                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator
                    val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId

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

//                        val num = fishnumTV.text.toString()
                        var time = ""
                        time = fishinvtmTV.text.toString()
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

                SET_COLL -> {
                    var title = data!!.getStringExtra("num");
                    var title2 = data!!.getStringExtra("num2");
                    colltoolTV.setText(title + "x" + title2)
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator
                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    var del_images: ArrayList<String> = ArrayList();
                    try {
                        images!!.clear()
                        del_images = images_path!![tag].split("/") as ArrayList<String>
                        images_path!!.removeAt(tag)

//                    val num = fishnumTV.text.toString()
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
                    Log.d("바바23", images_url.toString())
                    images_url!!.removeAt(tag)
                    Log.d("바바23", images_url.toString())
                    images_url_remove!!.add(images_id!!.get(tag).toString())
                    images_id!!.removeAt(tag)
                    Log.d("바바2", tag.toString())
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
//                            val pathPk = getPk.get(0)
//                            val pathPk2 = getPk.get(1)
//                            val num = fishnumTV.text.toString()
//                            val invtm = fishinvtmTV.text.toString()

//                            if (pathPk == num && pathPk2 == invtm) {
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
//                            }
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

    fun river_bed() {
        if (boulderET.text.isNotEmpty()) {
            values.add(boulderET.text.toString().toInt())
        }
        if (cobbleET.text.isNotEmpty()) {
            values.add(Utils.getString(cobbleET).toInt())
        }
        if (pebbleET.text.isNotEmpty()) {
            values.add(Utils.getString(pebbleET).toInt())
        }
        if (gravelET.text.isNotEmpty()) {
            values.add(Utils.getString(gravelET).toInt())
        }
        if (sendET.text.isNotEmpty()) {
            values.add(Utils.getString(sendET).toInt())
        }
        if (!(values.size <= 0 && values == null)) {
            var max = Collections.max<Int>(values) // 100

            if (max == Utils.getString(boulderET).toInt()) {
                values_name = "Boulder"
            } else if (max == Utils.getString(cobbleET).toInt()) {
                values_name = "Cobble"
            } else if (max == Utils.getString(pebbleET).toInt()) {
                values_name = "Pebble"
            } else if (max == Utils.getString(gravelET).toInt()) {
                values_name = "Gravel"
            } else if (max == Utils.getString(sendET).toInt()) {
                values_name = "Sand"
            } else {
                values_name = ""
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

    override fun onBackPressed() {

        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query("fishAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var fish_attribute = export_attribute(data)

            dataArray.add(fish_attribute)

        }

        if ( intent.getStringExtra("id") == null) {

            var intent = Intent()
            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                println("deletedir.size ${deletedir.size}")
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "fish/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }

            }

        }

        finish()

        data.close()

    }

    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt() - 1, days[2].toInt()).show()
    }

    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth)
        var msgs = msg.split("-")
        var month = ""
        var day = ""
        if (msgs[1].length < 2) {
            month = "0" + msgs[1]
        } else {
            month = msgs[1]
        }
        if (msgs[2].length < 2) {
            day = "0" + msgs[2]
        } else {
            day = msgs[2]
        }
        fishinvdtET.text = msgs[0] + "-" + month + "-" + day
    }

    fun null_attribute(): Fish_attribute {
        var fish_attribute: Fish_attribute = Fish_attribute(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null)
        return fish_attribute
    }

    fun export_attribute(data: Cursor): Fish_attribute {
        var fish_attribute: Fish_attribute = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                data.getString(8), data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14), data.getString(15),
                data.getFloat(16), data.getFloat(17), data.getString(18), data.getString(19), data.getInt(20), data.getInt(21), data.getInt(22), data.getInt(23), data.getString(24), data.getString(25),
                data.getInt(26), data.getInt(27), data.getInt(28), data.getInt(29), data.getInt(30), data.getString(31), data.getInt(32), data.getString(33), data.getString(34), data.getString(35),
                data.getInt(36), data.getString(37), data.getString(38), data.getString(39), data.getString(40), data.getString(41), data.getString(42), data.getInt(43), data.getInt(44), data.getFloat(45)
                , data.getInt(46), data.getInt(47), data.getFloat(48), data.getString(49), data.getString(50), data.getString(51), data.getString(52))
        return fish_attribute
    }

    fun timedlg() {

        val view = View.inflate(this, R.layout.dlg_timepicker, null)
        val timeTP: TimePicker = view.findViewById(R.id.timeTP)
        timeTP.setIs24HourView(true)
        timeTP.isLongClickable = true
        timeTP.isEnabled = true
        timeTP.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS

        val dialog = AlertDialog.Builder(this)
        dialog.setView(view)
        dialog.setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
        })
        dialog.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
            var hour_s = timeTP.hour.toString()
            var min_s = timeTP.minute.toString()
            if (min_s.length != 2) {
                min_s = "0" + min_s
            }
            if (hour_s.length != 2) {
                hour_s = "0" + hour_s
            }
            val msg = String.format("%s:%s", hour_s, min_s)
            fishinvtmTV.text = msg

        })
        dialog.show()

        /*
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
            fishinvtmTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
        */
    }
}
