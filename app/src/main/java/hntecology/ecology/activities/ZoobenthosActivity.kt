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
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.FileFilter
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_zoobenthos.*
import kotlinx.android.synthetic.main.activity_zoobenthos.*
import kotlinx.android.synthetic.main.activity_zoobenthos.btnPIC_FOLDER
import kotlinx.android.synthetic.main.activity_zoobenthos.confmodTV
import kotlinx.android.synthetic.main.activity_zoobenthos.coordedET
import kotlinx.android.synthetic.main.activity_zoobenthos.coordemET
import kotlinx.android.synthetic.main.activity_zoobenthos.coordesET
import kotlinx.android.synthetic.main.activity_zoobenthos.coordndET
import kotlinx.android.synthetic.main.activity_zoobenthos.coordnmET
import kotlinx.android.synthetic.main.activity_zoobenthos.coordnsET
import kotlinx.android.synthetic.main.activity_zoobenthos.gravelET
import kotlinx.android.synthetic.main.activity_zoobenthos.prjnameET
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.round

class ZoobenthosActivity : Activity() {

    val SET_HABTYETC = 1
    val SET_BANK_L = 2
    val SET_BANK_R = 3
    val SET_BAS_L = 4
    val SET_BAS_R = 5
    val SET_DIST_CAU = 6
    val SET_ZOOBENTHOS = 7

    val SET_INPUT = 2007
    val SET_INPUT2 = 1080
    val SET_INPUT3 = 1081
    val SET_INPUT4 = 1082
    val SET_INPUT5 = 1083
    val SET_INPUT6 = 1084


    lateinit var context: Context;

    var userName = "";

    var chkdata: Boolean = false;

    var latitude = 0.0f;
    var longitude = 0.0f;

    var lat: String = ""
    var log: String = ""

    var keyId: String? = null;

    var pk: String? = null

    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    val REQUEST_FINE_LOCATION = 100
    val REQUEST_ACCESS_COARSE_LOCATION = 101

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

    private var progressDialog: ProgressDialog? = null

    var dataArray: ArrayList<Zoobenthos_Attribute> = ArrayList<Zoobenthos_Attribute>()

    var basechkdata = false

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var imageUri: Uri? = null

    var invtm = ""

    var prjname = ""

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoobenthos)

        this.context = this;
        progressDialog = ProgressDialog(context)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        var today = Utils.todayStr();
        var time = Utils.timeStr()
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

        invdtTV.setText(today)
        timeTV.setText(time)

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        userName = PrefUtils.getStringPreference(context, "name");
        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        /*     var todays = today.split("-")

             var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

             for (i in 1 until todays.size){
                 texttoday += todays.get(i)
             }

             numTV.setText(texttoday + "1")*/

        invpersonTV.setText(userName)

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();
        var c = dbManager!!.pkNum("ZoobenthosAttribute")
        numTV.text = c.toString()

        invdtTV.setOnClickListener {
            datedlg()
        }
        timeTV.setOnClickListener {
            timedlg()
        }


        var intent: Intent = getIntent();

        if (intent.getStringExtra("markerid") != null) {
            markerid = intent.getStringExtra("markerid")
            println("markerid ---zoobenthos $markerid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            gpslatTV.setText(lat)
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            println("==============$log")
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
                System.out.println("list : " + list);

//                invregionTV.setText(list.get(0).getAddressLine(0));
                INV_REGION = list.get(0).getAddressLine(0)
            }

            val tmplat = lat.toFloat().toInt()
            val minute = ((lat.toFloat() - tmplat) * 60)
            val second = (((lat.toFloat() - tmplat) * 60) - minute) * 60

            val strlat = Location.convert(lat.toDouble(), Location.FORMAT_DEGREES)

            println("strlat $strlat")

//            DegreeToDMS(lat.toDouble())
//            DegreeToDMS(log.toDouble())

            println("lat $lat log $log")


            convert(lat.toDouble())
            logconvert(log.toDouble())


        }

        val dataList: Array<String> = arrayOf("*");

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            println("keyid ==== $keyId")
            println("base ==== ${base.GROP_ID}")

            invpersonTV.setText(base.INV_PERSON)
            invdtTV.setText(base.INV_DT)

            val time = Utils.timeStr()

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!
            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    zoobenthosinvregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, invpersonTV.text.toString(), invdtTV.text.toString(), Utils.timeStr())

            dbManager!!.insertbase(base)

        }

        habresetBT.setOnClickListener {
            habtyetcRL.visibility = View.GONE
            habtyetcTV.visibility = View.VISIBLE
            habtyetcTV.setText("")
        }

        bankresetBT.setOnClickListener {
            banklRL.visibility = View.GONE
            banklTV.visibility = View.VISIBLE
            banklTV.setText("")
        }

        krresetBT.setOnClickListener {
            bankrRL.visibility = View.GONE
            bankrTV.visibility = View.VISIBLE
            bankrTV.setText("")
        }

        baslresetBT.setOnClickListener {
            baslRL.visibility = View.GONE
            baslTV.visibility = View.VISIBLE
            baslTV.setText("")
        }

        basrresetBT.setOnClickListener {
            basrRL.visibility = View.GONE
            basrTV.visibility = View.VISIBLE
            basrTV.setText("")
        }

        distresetBT.setOnClickListener {
            distcauRL.visibility = View.GONE
            distcauTV.visibility = View.VISIBLE
            distcauTV.setText("")
        }

        specnmreset.setOnClickListener {
            specnmLL.visibility = View.GONE
            specnmtmp.setText("")
            specnmTV.visibility = View.VISIBLE
        }


        weatherTV.setOnClickListener {

            var listItems: java.util.ArrayList<String> = java.util.ArrayList();
            listItems.add("맑음");
            listItems.add("흐림");
            listItems.add("안개");
            listItems.add("비");

            alert(listItems, "날씨", weatherTV, "weather");

        }

        invmeanTV.setOnClickListener {

            var listItems: java.util.ArrayList<String> = java.util.ArrayList();
            listItems.add("정량");
            listItems.add("정성");

            alert(listItems, "조사방법", invmeanTV, "invmean");

        }

        habtyetcTV.setOnClickListener {

            val intent = Intent(context, DlgZoobenthosActivity::class.java)
            intent.putExtra("title", "서식지 유형")
            intent.putExtra("type", "HAB_TY_ETC")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_HABTYETC);

        }

        banklTV.setOnClickListener {

            val intent = Intent(context, DlgZoobenthosActivity::class.java)
            intent.putExtra("title", "제방형태 좌안")
            intent.putExtra("type", "BANK_L")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_BANK_L);

        }

        bankrTV.setOnClickListener {

            val intent = Intent(context, DlgZoobenthosActivity::class.java)
            intent.putExtra("title", "제방형태 우안")
            intent.putExtra("type", "BANK_R")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_BANK_R);

        }

        baslTV.setOnClickListener {

            val intent = Intent(context, DlgZoobenthosActivity::class.java)
            intent.putExtra("title", "유역토지이용 좌안")
            intent.putExtra("type", "BAS_L")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_BAS_L);

        }

        basrTV.setOnClickListener {

            val intent = Intent(context, DlgZoobenthosActivity::class.java)
            intent.putExtra("title", "유역토지이용 우안")
            intent.putExtra("type", "BAS_R")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_BAS_R);

        }

        distcauTV.setOnClickListener {

            val intent = Intent(context, DlgZoobenthosActivity::class.java)
            intent.putExtra("title", "교란요인")
            intent.putExtra("type", "DIST_CAU")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DIST_CAU);

        }

        turbidityTV.setOnClickListener {

            var listItems: java.util.ArrayList<String> = java.util.ArrayList();
            listItems.add("매우 탁함");
            listItems.add("탁함");
            listItems.add("맑음");
            listItems.add("맑으나 얕음");

            alert(listItems, "조사방법", turbidityTV, "turbidity");

        }

        specnmTV.setOnClickListener {
            val intent = Intent(context, DlgZoobenActivity::class.java)
            if (specnmTV.text != null && specnmTV.text != "") {
                var spec = specnmTV.text.toString()
                intent.putExtra("SPEC", spec)
            }
            startActivityForResult(intent, SET_ZOOBENTHOS);
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

        if (intent.getSerializableExtra("GROP_ID") != null) {
            keyId = intent.getStringExtra("GROP_ID")

            println("zooben$keyId")

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("ZoobenthosAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var zoo: Zoobenthos_Attribute = export_attribute(data)

                dataArray.add(zoo)

            }
        }

        if (intent.getStringExtra("id") != null) {

            pk = intent.getStringExtra("id")

            println("pk$pk")

            deleteTV.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("ZoobenthosAttribute", dataList, "id = '$pk'", null, null, null, "", null)
            while (data.moveToNext()) {
                chkdata = true

                var zoo: Zoobenthos_Attribute = export_attribute(data)

                prjnameET.setText(zoo.PRJ_NAME)
                invregionTV.setText(zoo.INV_REGION)
                INV_REGION = zoo.INV_REGION.toString()
                mapsysnmET.setText(zoo.MAP_SYS_NM)
                numTV.setText(zoo.NUM)
                timeTV.setText(zoo.INV_TM)
                var timesplit = zoo.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)

                invmeanTV.setText(zoo.INV_MEAN)
                coordndET.setText(zoo.GPSLAT_DEG.toString())
                coordnmET.setText(zoo.GPSLAT_MIN.toString())
                coordnsET.setText(zoo.GPSLAT_SEC.toString())
                coordedET.setText(zoo.GPSLON_DEG.toString())
                coordemET.setText(zoo.GPSLON_MIN.toString())
                coordesET.setText(zoo.GPSLON_SEC.toString())


                cntET.setText(zoo.ZOO_CNT.toString())
                invdtTV.setText(zoo.INV_DT)
                weatherTV.setText(zoo.WEATHER)
                invtoolET.setText(zoo.INV_TOOL)
                addistnmET.setText(zoo.AD_DIST_NM)

                var rivs = zoo.RIV_W.toString().split("~")
                if (rivs.size > 1) {
                    rivw1ET.setText(rivs[0])
                    rivw2ET.setText(rivs[1])
                }
                var run_rivs = zoo.RUN_RIV_W.toString().split("~")
                if (run_rivs.size > 1) {
                    runrivw1ET.setText(run_rivs[0])
                    runrivw2ET.setText(run_rivs[1])
                }

                waterdeptET.setText(zoo.WATER_DEPT.toString())
                habtyetcTV.setText(zoo.HAB_TY)
                /*      if (zoo.HAB_TY_ETC != null && zoo.HAB_TY_ETC != "") {
                          habtyetcTV.setText(zoo.HAB_TY_ETC)
                      }*/
                filtareaET.setText(zoo.FILT_AREA)
                temperaturET.setText(zoo.TEMPERATUR.toString())
                watertemET.setText(zoo.WATER_TEM.toString())
                turbidityTV.setText(zoo.TURBIDITY)
                mudET.setText(zoo.MUD.toString())
                sandET.setText(zoo.SAND.toString())
                corsandET.setText(zoo.COR_SAND.toString())
                gravelET.setText(zoo.GRAVEL.toString())
                stonesET.setText(zoo.STONE_S.toString())
                stonebET.setText(zoo.STONE_B.toString())
                concreteET.setText(zoo.CONCRETE.toString())
                bedroceET.setText(zoo.BED_ROCK.toString())
                banklTV.setText(zoo.BANK_L)
                /*      if (zoo.BANK_L_ETC != null && zoo.BANK_L_ETC != "") {
                          banklTV.setText(zoo.BANK_L_ETC)
                      }*/
                bankrTV.setText(zoo.BANK_R)
                /*  if (zoo.BANK_R_ETC != null && zoo.BANK_R_ETC != "") {
                      bankrTV.setText(zoo.BANK_R_ETC)
                  }*/
                baslTV.setText(zoo.BAS_L)
                /*   if (zoo.BAS_L_ETC != null && zoo.BAS_L_ETC != "") {
                       baslTV.setText(zoo.BAS_L_ETC)
                   }*/
                basrTV.setText(zoo.BAS_R)
                /*       if (zoo.BAS_R_ETC != null && zoo.BAS_R_ETC != "") {
                           basrTV.setText(zoo.BAS_R_ETC)
                       }*/
                distcauTV.setText(zoo.DIST_CAU)
                /*   if (zoo.DIST_ETC != null && zoo.DIST_ETC != "") {
                       distcauTV.setText(zoo.DIST_ETC)
                   }*/
                unusnoteET.setText(zoo.UNUS_NOTE)
                specnmTV.setText(zoo.SPEC_NM)
                faminmTV.setText(zoo.FAMI_NM)
                sciennmTV.setText(zoo.SCIEN_NM)
                confmodTV.setText(zoo.CONF_MOD)
                gpslatTV.setText(zoo.GPS_LAT.toString())
                gpslonTV.setText(zoo.GPS_LON.toString())

//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + zoo.INV_DT + "." + zoo.INV_TM + "/images")
//                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
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
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator, add_images[add_images.size - 1]))) {
                                //                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/zoobenthos/images/" + zoobenthos_attribute.NUM.toString() +"_"+zoobenthos_attribute.INV_TM +"_" + (j+1) + ".png")) {
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
        }

        saveBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var zoobenthos_Attribute: Zoobenthos_Attribute = null_attribute()

                        keyId = intent.getStringExtra("GROP_ID")

                        zoobenthos_Attribute.GROP_ID = keyId
                        zoobenthos_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
                        zoobenthos_Attribute.CURRENT_TM = Utils.current_tm()
                        val prj = prjnameET.text.toString()
                        if (prj == prjname) {
                            zoobenthos_Attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            zoobenthos_Attribute.PRJ_NAME = prjnameET.text.toString()
                        }

//                        zoobenthos_Attribute.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            zoobenthos_Attribute.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            zoobenthos_Attribute.PRJ_NAME = prjname
//                        }

//                        zoobenthos_Attribute.INV_REGION = invregionTV.text.toString()
                        if (invregionTV.length() > 0) {
                            zoobenthos_Attribute.INV_REGION = invregionTV.text.toString();
                        } else {
                            zoobenthos_Attribute.INV_REGION = INV_REGION
                        }
                        zoobenthos_Attribute.INV_MEAN = invmeanTV.text.toString()
                        zoobenthos_Attribute.INV_PERSON = invpersonTV.text.toString()
                        zoobenthos_Attribute.MAP_SYS_NM = mapsysnmET.text.toString()

                        if (coordndET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                        }
                        if (coordnmET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                        }
                        if (coordnsET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GPSLON_DEG = coordedET.text.toString().toInt()
                        }
                        if (coordemET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                        }
                        if (coordesET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                        }
                        if (cntET.text.isNotEmpty()) {
                            zoobenthos_Attribute.ZOO_CNT = cntET.text.toString().toInt()
                        }
                        zoobenthos_Attribute.INV_DT = invdtTV.text.toString()
                        zoobenthos_Attribute.NUM = numTV.text.toString()
                        zoobenthos_Attribute.INV_TM = timeTV.text.toString()
                        zoobenthos_Attribute.WEATHER = weatherTV.text.toString()
                        zoobenthos_Attribute.INV_TOOL = invtoolET.text.toString()
                        zoobenthos_Attribute.AD_DIST_NM = addistnmET.text.toString()
                        if (rivw1ET.text.isNotEmpty() && rivw2ET.text.isNotEmpty()) {
                            zoobenthos_Attribute.RIV_W = rivw1ET.text.toString() + "~" + rivw2ET.text.toString()
                        }
                        if (rivw2ET.text.isNotEmpty()) {
                            zoobenthos_Attribute.RIV_W2 = rivw2ET.text.toString().toInt()
                        }
                        if (runrivw1ET.text.isNotEmpty() && runrivw2ET.text.isNotEmpty()) {
                            zoobenthos_Attribute.RUN_RIV_W = runrivw1ET.text.toString() + "~" + runrivw2ET.text.toString()
                        }
                        if (runrivw2ET.text.isNotEmpty()) {
                            zoobenthos_Attribute.RUN_RIV_W2 = runrivw2ET.text.toString().toInt()
                        }
                        if (waterdeptET.text.isNotEmpty()) {
                            zoobenthos_Attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
                        }
                        zoobenthos_Attribute.HAB_TY = habtyetcTV.text.toString()
//                        zoobenthos_Attribute.HAB_TY_ETC = habtyetcET.text.toString()
                        zoobenthos_Attribute.FILT_AREA = filtareaET.text.toString()
                        if (temperaturET.text.isNotEmpty()) {
                            zoobenthos_Attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
                        }
                        if (watertemET.text.isNotEmpty()) {
                            zoobenthos_Attribute.WATER_TEM = watertemET.text.toString().toFloat()
                        }
                        zoobenthos_Attribute.TURBIDITY = turbidityTV.text.toString()
                        if (mudET.text.isNotEmpty()) {
                            zoobenthos_Attribute.MUD = mudET.text.toString().toFloat()
                        }
                        if (sandET.text.isNotEmpty()) {
                            zoobenthos_Attribute.SAND = sandET.text.toString().toFloat()
                        }
                        if (corsandET.text.isNotEmpty()) {
                            zoobenthos_Attribute.COR_SAND = corsandET.text.toString().toFloat()
                        }
                        if (gravelET.text.isNotEmpty()) {
                            zoobenthos_Attribute.GRAVEL = gravelET.text.toString().toFloat()
                        }
                        if (stonesET.text.isNotEmpty()) {
                            zoobenthos_Attribute.STONE_S = stonesET.text.toString().toFloat()
                        }
                        if (stonebET.text.isNotEmpty()) {
                            zoobenthos_Attribute.STONE_B = stonebET.text.toString().toFloat()
                        }
                        if (concreteET.text.isNotEmpty()) {
                            zoobenthos_Attribute.CONCRETE = concreteET.text.toString().toFloat()
                        }
                        if (bedroceET.text.isNotEmpty()) {
                            zoobenthos_Attribute.BED_ROCK = bedroceET.text.toString().toFloat()
                        }
                        zoobenthos_Attribute.BANK_L = banklTV.text.toString()
//                        zoobenthos_Attribute.BANK_L_ETC = banklET.text.toString()
                        zoobenthos_Attribute.BANK_R = bankrTV.text.toString()
//                        zoobenthos_Attribute.BANK_R_ETC = bankrET.text.toString()
                        zoobenthos_Attribute.BAS_L = baslTV.text.toString()
//                        zoobenthos_Attribute.BAS_L_ETC = baslET.text.toString()
                        zoobenthos_Attribute.BAS_R = basrTV.text.toString()
//                        zoobenthos_Attribute.BAS_R_ETC = basrET.text.toString()
                        zoobenthos_Attribute.DIST_CAU = distcauTV.text.toString()
//                        zoobenthos_Attribute.DIST_ETC = distcauET.text.toString()
                        zoobenthos_Attribute.UNUS_NOTE = unusnoteET.text.toString()

                        zoobenthos_Attribute.TEMP_YN = "Y"

                        zoobenthos_Attribute.CONF_MOD = "N"

                        zoobenthos_Attribute.GPS_LAT = gpslatTV.text.toString().toFloat()
                        zoobenthos_Attribute.GPS_LON = gpslonTV.text.toString().toFloat()

                        zoobenthos_Attribute.SPEC_NM = specnmTV.text.toString()
                        if (specnmtmp.length() > 0) {
                            zoobenthos_Attribute.SPEC_NM = specnmtmp.text.toString()
                        }
                        zoobenthos_Attribute.FAMI_NM = faminmTV.text.toString()
                        zoobenthos_Attribute.SCIEN_NM = sciennmTV.text.toString()
                        zoobenthos_Attribute.GEOM = gpslatTV.text.toString() + " " + gpslonTV.text.toString()

                        println("zoobenthos_Attribute.GEOM:::::::::::::::::::${zoobenthos_Attribute.GEOM}")
                        println("chkdata :::::::::::::::::::${chkdata}")

                        if (chkdata) {

                            if (pk != null) {

                                val CONF_MOD = confmodTV.text.toString()

                                if (CONF_MOD == "C" || CONF_MOD == "N") {
                                    zoobenthos_Attribute.CONF_MOD = "M"
                                }

                                dbManager!!.updatezoobenthous_attribute(zoobenthos_Attribute, pk)
                                dbManager!!.updatecommonzoobenthos(zoobenthos_Attribute, keyId)
                            }

//                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator)
//                            val pathdir = path.listFiles()
//
//                            if (pathdir != null) {
//                                for (i in 0..pathdir.size - 1) {
//
//                                    for (j in 0..pathdir.size - 1) {
//
//                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator + zoobenthos_Attribute.NUM.toString() + "_" + zoobenthos_Attribute.INV_TM +"_" + (j+1) + ".png")) {
//
//                                            pathdir.get(i).canonicalFile.delete()
//
//                                        }
//                                    }
//
//                                }
//                            }
//
//                            for (i in 0..images!!.size - 1) {
//
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator
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
//                                saveVitmapToFile(images!!.get(i), outPath + zoobenthos_Attribute.NUM.toString() + "_"+zoobenthos_Attribute.INV_TM+"_" + (i + 1) + ".png")
//
//                            }

                        } else {
                            dbManager!!.insertzoobenthos(zoobenthos_Attribute);
                        }
                        dbManager!!.updatecommonzoobenthos(zoobenthos_Attribute, keyId)
                        var intent = Intent()
                        intent.putExtra("export", 70)
                        setResult(RESULT_OK, intent)

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        deleteTV.setOnClickListener {
            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            val intent = getIntent()

                            dialog.cancel()

                            var zoobenthos_Attribute: Zoobenthos_Attribute = null_attribute()

                            if (pk != null) {

                                val data = db!!.query("ZoobenthosAttribute", dataList, "id = '$pk'", null, null, null, "", null)
                                while (data.moveToNext()) {
                                    chkdata = true

                                    zoobenthos_Attribute = export_attribute(data)
                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator + zoobenthos_Attribute.NUM.toString() + "_" + zoobenthos_Attribute.INV_TM + "_" + (j + 1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                }

                                if (intent.getSerializableExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("ZoobenthosAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var zoo: Zoobenthos_Attribute = export_attribute(data)

                                        dataArray.add(zoo)

                                    }

                                    println("dataArrayList.size ${dataArray.size}")

                                    var intent = Intent()

                                    if (dataArray.size > 1) {

                                        dbManager!!.deletezoobenthous_attribute(zoobenthos_Attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {

                                        var intent = Intent()

                                        intent.putExtra("markerid", markerid)

                                        dbManager!!.deletezoobenthous_attribute(zoobenthos_Attribute, pk)

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

                            if (intent.getSerializableExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("ZoobenthosAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var zoo: Zoobenthos_Attribute = export_attribute(data)

                                }

                                if (chkdata == true) {
                                    Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    var intent = Intent()
                                    intent.putExtra("markerid", markerid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }

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

        cancleBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dbManager: DataBaseHelper = DataBaseHelper(this)

                        val db = dbManager.createDataBase()

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("ZoobenthosAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var zoo: Zoobenthos_Attribute = export_attribute(data)


                            dataArray.add(zoo)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {
                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                val deletedir = path.listFiles()
                                println("deletedir.size ${deletedir.size}")
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
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
        }

        nextTV.setOnClickListener {
            var zoobenthos_Attribute: Zoobenthos_Attribute = null_attribute()

            keyId = intent.getStringExtra("GROP_ID")

            zoobenthos_Attribute.GROP_ID = keyId
            zoobenthos_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
            zoobenthos_Attribute.CURRENT_TM = Utils.current_tm()
            val prj = prjnameET.text.toString()
            if (prj == prjname) {
                zoobenthos_Attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                zoobenthos_Attribute.PRJ_NAME = prjnameET.text.toString()
            }

//            zoobenthos_Attribute.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                zoobenthos_Attribute.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                zoobenthos_Attribute.PRJ_NAME = prjname
//            }

//            zoobenthos_Attribute.INV_REGION = invregionTV.text.toString()
            if (invregionTV.length() > 0) {
                zoobenthos_Attribute.INV_REGION = invregionTV.text.toString();
            } else {
                zoobenthos_Attribute.INV_REGION = INV_REGION
            }
            zoobenthos_Attribute.INV_MEAN = invmeanTV.text.toString()
            zoobenthos_Attribute.INV_PERSON = invpersonTV.text.toString()
            zoobenthos_Attribute.MAP_SYS_NM = mapsysnmET.text.toString()
            if (coordndET.text.isNotEmpty()) {
                zoobenthos_Attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                zoobenthos_Attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                zoobenthos_Attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                zoobenthos_Attribute.GPSLON_DEG = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                zoobenthos_Attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                zoobenthos_Attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            if (cntET.text.isNotEmpty()) {
                zoobenthos_Attribute.ZOO_CNT = cntET.text.toString().toInt()
            }
            zoobenthos_Attribute.INV_DT = invdtTV.text.toString()
            zoobenthos_Attribute.NUM = numTV.text.toString()
            zoobenthos_Attribute.INV_TM = timeTV.text.toString()
            zoobenthos_Attribute.WEATHER = weatherTV.text.toString()
            zoobenthos_Attribute.INV_TOOL = invtoolET.text.toString()
            zoobenthos_Attribute.AD_DIST_NM = addistnmET.text.toString()
            if (rivw1ET.text.isNotEmpty() && rivw2ET.text.isNotEmpty()) {
                zoobenthos_Attribute.RIV_W = rivw1ET.text.toString() + "~" + rivw2ET.text.toString()
            }
            if (rivw2ET.text.isNotEmpty()) {
                zoobenthos_Attribute.RIV_W2 = rivw2ET.text.toString().toInt()
            }
            if (runrivw1ET.text.isNotEmpty() && runrivw2ET.text.isNotEmpty()) {
                zoobenthos_Attribute.RUN_RIV_W = runrivw1ET.text.toString() + "~" + runrivw2ET.text.toString()
            }
            if (runrivw2ET.text.isNotEmpty()) {
                zoobenthos_Attribute.RUN_RIV_W2 = runrivw2ET.text.toString().toInt()
            }
            if (waterdeptET.text.isNotEmpty()) {
                zoobenthos_Attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
            }
            zoobenthos_Attribute.HAB_TY = habtyetcTV.text.toString()
//            zoobenthos_Attribute.HAB_TY_ETC = habtyetcET.text.toString()
            zoobenthos_Attribute.FILT_AREA = filtareaET.text.toString()
            if (temperaturET.text.isNotEmpty()) {
                zoobenthos_Attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
            }
            if (watertemET.text.isNotEmpty()) {
                zoobenthos_Attribute.WATER_TEM = watertemET.text.toString().toFloat()
            }
            zoobenthos_Attribute.TURBIDITY = turbidityTV.text.toString()
            if (mudET.text.isNotEmpty()) {
                zoobenthos_Attribute.MUD = mudET.text.toString().toFloat()
            }
            if (sandET.text.isNotEmpty()) {
                zoobenthos_Attribute.SAND = sandET.text.toString().toFloat()
            }
            if (corsandET.text.isNotEmpty()) {
                zoobenthos_Attribute.COR_SAND = corsandET.text.toString().toFloat()
            }
            if (gravelET.text.isNotEmpty()) {
                zoobenthos_Attribute.GRAVEL = gravelET.text.toString().toFloat()
            }
            if (stonesET.text.isNotEmpty()) {
                zoobenthos_Attribute.STONE_S = stonesET.text.toString().toFloat()
            }
            if (stonebET.text.isNotEmpty()) {
                zoobenthos_Attribute.STONE_B = stonebET.text.toString().toFloat()
            }
            if (concreteET.text.isNotEmpty()) {
                zoobenthos_Attribute.CONCRETE = concreteET.text.toString().toFloat()
            }
            if (bedroceET.text.isNotEmpty()) {
                zoobenthos_Attribute.BED_ROCK = bedroceET.text.toString().toFloat()
            }
            zoobenthos_Attribute.BANK_L = banklTV.text.toString()
//            zoobenthos_Attribute.BANK_L_ETC = banklET.text.toString()
            zoobenthos_Attribute.BANK_R = bankrTV.text.toString()
//            zoobenthos_Attribute.BANK_R_ETC = bankrET.text.toString()
            zoobenthos_Attribute.BAS_L = baslTV.text.toString()
//            zoobenthos_Attribute.BAS_L_ETC = baslET.text.toString()
            zoobenthos_Attribute.BAS_R = basrTV.text.toString()
//            zoobenthos_Attribute.BAS_R_ETC = basrET.text.toString()
            zoobenthos_Attribute.DIST_CAU = distcauTV.text.toString()
//            zoobenthos_Attribute.DIST_ETC = distcauET.text.toString()
            zoobenthos_Attribute.UNUS_NOTE = unusnoteET.text.toString()

            zoobenthos_Attribute.TEMP_YN = "Y"

            zoobenthos_Attribute.CONF_MOD = "N"

            zoobenthos_Attribute.GPS_LAT = gpslatTV.text.toString().toFloat()
            zoobenthos_Attribute.GPS_LON = gpslonTV.text.toString().toFloat()

            zoobenthos_Attribute.SPEC_NM = specnmTV.text.toString()
            if (specnmtmp.length() > 0) {
                zoobenthos_Attribute.SPEC_NM = specnmtmp.text.toString()
            }
            zoobenthos_Attribute.FAMI_NM = faminmTV.text.toString()
            zoobenthos_Attribute.SCIEN_NM = sciennmTV.text.toString()
            zoobenthos_Attribute.GEOM = gpslatTV.text.toString() + " " + gpslonTV.text.toString()

            if (chkdata) {

                if (pk != null) {

                    val CONF_MOD = confmodTV.text.toString()

                    if (CONF_MOD == "C" || CONF_MOD == "N") {
                        zoobenthos_Attribute.CONF_MOD = "M"
                    }

                    dbManager!!.updatezoobenthous_attribute(zoobenthos_Attribute, pk)
                    dbManager!!.updatecommonzoobenthos(zoobenthos_Attribute, keyId)
                }

//                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator)
//                val pathdir = path.listFiles()
//
//                if (pathdir != null) {
//                    for (i in 0..pathdir.size - 1) {
//
//                        for (j in 0..pathdir.size - 1) {
//
//                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator + zoobenthos_Attribute.NUM.toString() + "_" + zoobenthos_Attribute.INV_TM +"_" + (j+1) + ".png")) {
//
//                                pathdir.get(i).canonicalFile.delete()
//
//                            }
//                        }
//
//                    }
//                }
//
//                for (i in 0..images!!.size - 1) {
//
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator
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
//                    saveVitmapToFile(images!!.get(i), outPath + zoobenthos_Attribute.NUM.toString() + "_"+zoobenthos_Attribute.INV_TM+"_" + (i + 1) + ".png")
//
//                }

            } else {

                dbManager!!.insertzoobenthos(zoobenthos_Attribute);

//                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                sdPath += "/ecology/tmps/" + zoobenthos_Attribute.INV_DT + "." + zoobenthos_Attribute.INV_TM + "/images"
//                val zoobenthos = File(sdPath)
//                zoobenthos.mkdir();
////                          sdPath +="/imgs"
////                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                val file = File(sdPath)
//                file.mkdir();
//                //이미 있다면 삭제. 후 생성
//                setDirEmpty(sdPath)
//
//                sdPath += "/"
//
//                var pathArray: ArrayList<String> = ArrayList<String>()
//
//                for (i in 0..images!!.size - 1) {
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images"+ File.separator +keyId+ File.separator
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
//                    saveVitmapToFile(images!!.get(i),outPath+zoobenthos_Attribute.NUM + "_" + zoobenthos_Attribute.INV_TM+"_"+(i+1)+".png")
//
//                }

            }
            dbManager!!.updatecommonzoobenthos(zoobenthos_Attribute, keyId)
            if (intent.getStringExtra("set") != null) {
                var intent = Intent()
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            deleteTV.visibility = View.GONE

            var intent = Intent()
            intent.putExtra("export", 70)
            setResult(RESULT_OK, intent)

            /*if (images_path != null) {
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

    fun alert(ListItems: java.util.ArrayList<String>, title: String, textView: TextView, type: String) {

        val items = Array<CharSequence>(ListItems.size, { i -> ListItems.get(i) })

        var size = ListItems.size

        var builder: AlertDialog.Builder = AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->


            var selectItem = ListItems.get(i);

            if (selectItem != "취소") {
                textView.text = selectItem
            }

        })

        builder.show();
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            } else if (Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var region: Region

        var vegetation: Vegetation

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                SET_INPUT -> {
                    var name = data!!.getStringExtra("name")
                    var text = Utils.getString(habtyetcTV)
                    habtyetcTV.text = text + " " + name
                }
                SET_INPUT2 -> {
                    var name = data!!.getStringExtra("name")
                    var text = Utils.getString(banklTV)
                    banklTV.text = text + " " + name
                }
                SET_INPUT3 -> {
                    var name = data!!.getStringExtra("name");
                    var text = Utils.getString(bankrTV)
                    bankrTV.text = name
                }
                SET_INPUT4 -> {
                    var name = data!!.getStringExtra("name")
                    var text = Utils.getString(baslTV)
                    baslTV.text = text + " " + name
                }
                SET_INPUT5 -> {
                    var name = data!!.getStringExtra("name")
                    var text = Utils.getString(distcauTV)
                    distcauTV.text = text + " " + name
                }
                SET_INPUT6 -> {
                    var name = data!!.getStringExtra("name")
                    var text = Utils.getString(basrTV)
                    basrTV.text = text + " " + name
                }
                FROM_CAMERA -> {

                    if (resultCode == -1) {
                        val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId
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

                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator
                    val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId

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

//                        val num = zoobenthosnumTV.text.toString()
                        var time = ""
                        time = zoobenthosinvtmTV.text.toString()
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

                SET_HABTYETC -> {
                    if (data!!.getStringExtra("title") != null) {
                        habtyetcRL.visibility = View.VISIBLE
                        habtyetcTV.visibility = View.GONE
                    }

                    if (data!!.getSerializableExtra("data") != null) {
                        val item = data!!.getSerializableExtra("data") as ArrayList<ZoobenthosSelect>
                        var title = ""

                        if (item != null) {
                            for (i in 0..item.size - 1) {
                                title += item.get(i).title + " "
                                if (item.get(i).title == "기타") {
                                    val intent = Intent(context, DlgInputActivity::class.java)
                                    startActivityForResult(intent, SET_INPUT)
                                }
                            }
                        }

                        habtyetcTV.setText(title)
                    }
                }

                SET_BANK_L -> {
                    if (data!!.getStringExtra("title") != null) {
                        banklRL.visibility = View.VISIBLE
                        banklTV.visibility = View.GONE
                    }

                    if (data!!.getSerializableExtra("data") != null) {
                        val item = data!!.getSerializableExtra("data") as ArrayList<ZoobenthosSelect>
                        var title = ""

                        if (item != null) {
                            for (i in 0..item.size - 1) {
                                title += item.get(i).title + " "
                                if (item.get(i).title == "기타") {
                                    val intent = Intent(context, DlgInputActivity::class.java)

                                    startActivityForResult(intent, SET_INPUT2)
                                }
                            }
                        }

                        banklTV.setText(title)

                    }
                }

                SET_BANK_R -> {
                    if (data!!.getStringExtra("title") != null) {
                        bankrRL.visibility = View.VISIBLE
                        bankrTV.visibility = View.GONE
                    }

                    if (data!!.getSerializableExtra("data") != null) {
                        val item = data!!.getSerializableExtra("data") as ArrayList<ZoobenthosSelect>
                        var title = ""

                        if (item != null) {
                            for (i in 0..item.size - 1) {
                                title += item.get(i).title + " "
                                if (item.get(i).title == "기타") {
                                    val intent = Intent(context, DlgInputActivity::class.java)

                                    startActivityForResult(intent, SET_INPUT3)
                                }
                            }
                        }

                        bankrTV.setText(title)

                    }
                }

                SET_BAS_L -> {
                    if (data!!.getStringExtra("title") != null) {
                        baslRL.visibility = View.VISIBLE
                        baslTV.visibility = View.GONE
                    }

                    if (data!!.getSerializableExtra("data") != null) {
                        val item = data!!.getSerializableExtra("data") as ArrayList<ZoobenthosSelect>
                        var title = ""

                        if (item != null) {
                            for (i in 0..item.size - 1) {
                                title += item.get(i).title + " "
                                if (item.get(i).title == "기타") {
                                    val intent = Intent(context, DlgInputActivity::class.java)
                                    startActivityForResult(intent, SET_INPUT4)
                                }
                            }
                        }

                        baslTV.setText(title)

                    }
                }

                SET_BAS_R -> {
                    if (data!!.getStringExtra("title") != null) {
                        basrRL.visibility = View.VISIBLE
                        basrTV.visibility = View.GONE
                    }

                    if (data!!.getSerializableExtra("data") != null) {
                        val item = data!!.getSerializableExtra("data") as ArrayList<ZoobenthosSelect>
                        var title = ""

                        if (item != null) {
                            for (i in 0..item.size - 1) {
                                title += item.get(i).title + " "
                                if (item.get(i).title == "기타") {
                                    val intent = Intent(context, DlgInputActivity::class.java)
                                    startActivityForResult(intent, SET_INPUT6)
                                }
                            }
                        }

                        basrTV.setText(title)

                    }
                }

                SET_DIST_CAU -> {
                    if (data!!.getStringExtra("title") != null) {
                        distcauRL.visibility = View.VISIBLE
                        distcauTV.visibility = View.GONE
                    }

                    if (data!!.getSerializableExtra("data") != null) {
                        val item = data!!.getSerializableExtra("data") as ArrayList<ZoobenthosSelect>
                        var title = ""

                        if (item != null) {
                            for (i in 0..item.size - 1) {
                                title += item.get(i).title + " "
                                if (item.get(i).title == "기타") {
                                    val intent = Intent(context, DlgInputActivity::class.java)

                                    startActivityForResult(intent, SET_INPUT5)
                                }

                                if (item.get(i).title == "없음") {
                                    title = "없음"
                                }

                            }
                        }

                        distcauTV.setText(title)

                    }
                }

                SET_ZOOBENTHOS -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    specnmTV.setText(name)
                    if (name == "SP(미동정)") {
                        specnmLL.visibility = View.VISIBLE
                        specnmTV.visibility = View.GONE
                    }
                    faminmTV.setText(family_name)
                    sciennmTV.setText(zoological)

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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator
                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    var del_images: ArrayList<String> = ArrayList();
                    try {
                        images!!.clear()
                        del_images = images_path!![tag].split("/") as ArrayList<String>
                        images_path!!.removeAt(tag)

//                    val num = zoobenthosnumTV.text.toString()
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

    fun clear() {

//        var num = numTV.text.toString()
        /*  if (num.length > 7){
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
        var c = dbManager!!.pkNum("ZoobenthosAttribute")
        numTV.text = c.toString()

//        addPicturesLL!!.removeAllViews()

        specnmtmp.setText("")

        specnmTV.setText("")
        faminmTV.setText("")
        sciennmTV.setText("")

    }

    override fun onBackPressed() {

        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query("ZoobenthosAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var zoo: Zoobenthos_Attribute = export_attribute(data)

            dataArray.add(zoo)

        }

        if (intent.getStringExtra("id") == null) {
            var intent = Intent()
            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/zoobenthos/images/")
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                println("deletedir.size ${deletedir.size}")
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "zoobenthos/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }

            }
        }

        data.close()

        finish()
    }

    fun convertLatitud(longitude: Double): String {
        var result = "''"
        if (longitude != null) {
            var direction = "E"
            if (longitude < 0) {
                direction = "W";
            }
            result = convert(longitude) + direction;
        }
        return result;
    }

    fun convertLongitude(longitude: Double): String {
        var result = ""
        if (longitude != null) {
            var direction = 'E'.toString()
            if (longitude < 0) {
                direction = "W"

            }
            result = convert(longitude) + direction;
        }
        return result;
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

        println("")

        println("i $i")

        println("s ::::::::::::::::::::::::::::::::::::::::::::::::::::::: " + s)

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

        println("")

        println("i $i")



        println("s ::::::::::::::::::::::::::::::::::::::::::::::::::::::: " + s)

        return s
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
            timeTV.text = msg

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
            timeTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
        */
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
        invdtTV.text = msgs[0]+"-"+month+"-"+day

    }

    fun null_attribute(): Zoobenthos_Attribute {
        var zoobenthos_Attribute: Zoobenthos_Attribute = Zoobenthos_Attribute(null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        return zoobenthos_Attribute
    }

    fun export_attribute(data: Cursor): Zoobenthos_Attribute {
        var zoo: Zoobenthos_Attribute = Zoobenthos_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                data.getInt(8), data.getFloat(9), data.getInt(10), data.getInt(11), data.getFloat(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                , data.getInt(22)
                , data.getInt(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27)
                , data.getString(28), data.getFloat(29), data.getFloat(30), data.getFloat(31), data.getFloat(32)
                , data.getFloat(33), data.getFloat(34), data.getFloat(35), data.getFloat(36), data.getString(37)
                , data.getString(38), data.getString(39), data.getString(40)
                , data.getString(41), data.getString(42), data.getFloat(43), data.getFloat(44)
                , data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getString(49)
                , data.getString(20), data.getInt(51), data.getString(52), data.getString(53))
        return zoo
    }

}
