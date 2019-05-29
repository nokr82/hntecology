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
import android.text.Editable
import android.text.TextWatcher
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
import hntecology.ecology.model.Insect_attribute
import hntecology.ecology.model.Reptilia_attribute
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationAccuracy
import io.nlopez.smartlocation.location.config.LocationParams
import io.nlopez.smartlocation.location.providers.LocationManagerProvider
import kotlinx.android.synthetic.main.activity_biotope_ex.*
import kotlinx.android.synthetic.main.activity_reptilia_ex.*
import kotlinx.android.synthetic.main.activity_reptilia_ex.btnPIC_FOLDER
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Math.abs
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.*

class ReptiliaActivity : Activity(), OnLocationUpdatedListener {

    val SET_WEATHER = 1;
    val SET_WIND = 2;
    val SET_WIND_DIRE = 3;
    val SET_REPTILIA = 4

    lateinit var context: Context;

    var chkdata: Boolean = false;

    var userName = "";

    var keyId: String? = null;

    var pk: String? = null

    var page: Int? = null

    val REQUEST_FINE_LOCATION = 50
    val REQUEST_ACCESS_COARSE_LOCATION = 51

    var dataArray: ArrayList<Reptilia_attribute> = ArrayList<Reptilia_attribute>()

    private var progressDialog: ProgressDialog? = null

    var latitude = 0.0f;
    var longitude = 0.0f;

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

    var prjname = ""

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reptilia_ex)

        window.setGravity(Gravity.RIGHT);
//        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        this.context = this;
        // 조사 일자
        createdDateTV.text = Utils.todayStr()
        invtmTV.text = Utils.timeStr()

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        userName = PrefUtils.getStringPreference(context, "name");

        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        invpersonET.setText(userName)

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();


        var c = dbManager!!.pkNum("reptiliaAttribute")
        numET.text = c.toString()

        createdDateTV.setOnClickListener {
            datedlg()
        }
        invtmTV.setOnClickListener {
            timedlg()
        }


        /*   var today = Utils.todayStr();

           var todays = today.split("-")

           var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

           for (i in 1 until todays.size){
               texttoday += todays.get(i)
           }*/

//        numET.setText(texttoday + "1")
        var time = Utils.timeStr();
        invtmTV.text = time;
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

        var intent: Intent = getIntent();




        if (intent.getStringExtra("markerid") != null) {
            markerid = intent.getStringExtra("markerid")
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

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    invregionET.setText(list.get(0).getAddressLine(0));
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

        memory()



        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            println("keyid ==== $keyId")
            println("base ==== ${base.GROP_ID}")

            invpersonET.setText(base.INV_PERSON)
            createdDateTV.setText(base.INV_DT)
            invtmTV.setText(base.INV_TM)

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    invregionET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, invpersonET.text.toString(), createdDateTV.text.toString(), invtmTV.text.toString())

            dbManager!!.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {

            btn_reptiliaDelete.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("reptiliaAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var reptilia_attribute: Reptilia_attribute = export_attribute(data)

                invregionET.setText(reptilia_attribute.INV_REGION)
                INV_REGION = reptilia_attribute.INV_REGION.toString()
                createdDateTV.setText(reptilia_attribute.INV_DT)

                weatherTV.setText(reptilia_attribute.WEATHER)
                windTV.setText(reptilia_attribute.WIND)
                windDireTV.setText(reptilia_attribute.WIND_DIRE)
                prjnameET.setText(reptilia_attribute.PRJ_NAME)

                temperaturET.setText(reptilia_attribute.TEMPERATUR.toString())
                etcET.setText(reptilia_attribute.ETC)

                numET.setText(reptilia_attribute.NUM.toString())
                invtmTV.setText(reptilia_attribute.INV_TM)

                var timesplit = reptilia_attribute.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)

                specnmET.setText(reptilia_attribute.SPEC_NM)
                famiET.setText(reptilia_attribute.FAMI_NM)
                scienET.setText(reptilia_attribute.SCIEN_NM)

                incntaduET.setText(reptilia_attribute.IN_CNT_ADU.toString())
                incntlarET.setText(reptilia_attribute.IN_CNT_LAR.toString())
                incnteggET.setText(reptilia_attribute.IN_CNT_EGG.toString())

                habriveerET.setText(reptilia_attribute.HAB_RIVEER)
                habedgeET.setText(reptilia_attribute.HAB_EDGE)

                waterinET.setText(reptilia_attribute.WATER_IN)
                wateroutET.setText(reptilia_attribute.WATER_OUT)

                habareawET.setText(reptilia_attribute.HAB_AREA_W.toString())
                habareahET.setText(reptilia_attribute.HAB_AREA_H.toString())

                watercontET.setText(reptilia_attribute.WATER_CONT)
                waterqualET.setText(reptilia_attribute.WATER_QUAL)
                waterdeptET.setText(reptilia_attribute.WATER_DEPT.toString())

                gpslatTV.setText(reptilia_attribute.GPS_LAT.toString())
                gpslonTV.setText(reptilia_attribute.GPS_LON.toString())

                coordndET.setText(reptilia_attribute.GPSLAT_DEG.toString())
                coordnmET.setText(reptilia_attribute.GPSLAT_MIN.toString())
                coordnsET.setText(reptilia_attribute.GPSLAT_SEC.toString())
                coordedET.setText(reptilia_attribute.GPSLON_DEG.toString())
                coordemET.setText(reptilia_attribute.GPSLON_MIN.toString())
                coordesET.setText(reptilia_attribute.GPSLON_SEC.toString())

                memoryTV.setText(reptilia_attribute.HAB_AREA.toString())

                val id = reptilia_attribute.id

                if (reptilia_attribute.TEMP_YN.equals("N")) {
                    dbManager!!.deletereptilia_attribute(reptilia_attribute, id)
                }

                if (reptilia_attribute.TEMP_YN.equals("Y")) {
                    dataArray.add(reptilia_attribute)
                }

                confmodTV.setText(reptilia_attribute.CONF_MOD)

//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + reptilia_attribute.INV_DT + "." + reptilia_attribute.INV_TM + "." + reptilia_attribute.NUM + "/images")
//                val fileList = file.listFiles()
                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/")
                var tmpfileList = tmpfiles.listFiles()

//                if (fileList != null) {
//                    for (i in 0..fileList.size - 1) {
//                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/"
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
//                        val num = reptilia_attribute.NUM.toString()
//
//                        val tmpfile2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/reptilia/images" ,   num + "_" + reptilia_attribute.INV_TM +"_" + (i+1) + ".png")
//
//                        if(tmpfile.exists()){
//                            tmpfile.renameTo(tmpfile2)
//                        }
//
//                        tmpfileList = tmpfiles.listFiles()
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
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator, (j + 1).toString()))) {
//                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/reptilia/images/" +reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM +"_" + (j+1) + ".png")) {
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
//                    val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + reptilia_attribute.INV_DT + "." + reptilia_attribute.INV_TM + "."+reptilia_attribute.NUM)
//                    path.deleteRecursively()
//                }
            }


        }

        reptilialeftLL.setOnClickListener {

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var reptilia_attribute: Reptilia_attribute = export_attribute(data)

                dataArray.add(reptilia_attribute)

            }

            if (page == dataArray.size && page!! > 1) {
                page = page!! - 1
                reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

                clear()

                resetPage(page!!)
            } else if (page!! < dataArray.size && page!! > 1) {
                page = page!! - 1
                reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

                clear()

                resetPage(page!!)
            }

        }


        reptiliarightLL.setOnClickListener {
            clear()

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data.moveToNext()) {

                chkdata = true

                var reptilia_attribute: Reptilia_attribute = export_attribute(data)

                dataArray.add(reptilia_attribute)

            }

            var reptilia_attribute: Reptilia_attribute = null_attribute()

            reptilia_attribute.id = keyId + page.toString()

            reptilia_attribute.GROP_ID = keyId
            reptilia_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
            reptilia_attribute.CURRENT_TM = Utils.current_tm()

            reptilia_attribute.PRJ_NAME = prjnameET.text.toString()

            reptilia_attribute.INV_REGION = invregionET.text.toString()
            reptilia_attribute.INV_DT = createdDateTV.text.toString()
            reptilia_attribute.INV_PERSON = invpersonET.text.toString()

            reptilia_attribute.WEATHER = weatherTV.text.toString()
            reptilia_attribute.WIND = windTV.text.toString()
            reptilia_attribute.WIND_DIRE = windDireTV.text.toString()

            if (temperaturET.text.isNotEmpty()) {
                reptilia_attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
            }

            reptilia_attribute.ETC = etcET.text.toString()

            if (numET.text.isNotEmpty()) {
                reptilia_attribute.NUM = numET.text.toString().toInt()
            }

            reptilia_attribute.INV_TM = invtmTV.text.toString()

            reptilia_attribute.SPEC_NM = specnmET.text.toString()
            reptilia_attribute.FAMI_NM = famiET.text.toString()
            reptilia_attribute.SCIEN_NM = scienET.text.toString()

            if (incntaduET.text.isNotEmpty()) {
                reptilia_attribute.IN_CNT_ADU = incntaduET.text.toString().toInt()
            }
            if (incntlarET.text.isNotEmpty()) {
                reptilia_attribute.IN_CNT_LAR = incntlarET.text.toString().toInt()
            }
            if (incnteggET.text.isNotEmpty()) {
                reptilia_attribute.IN_CNT_EGG = incnteggET.text.toString().toInt()
            }

            reptilia_attribute.HAB_RIVEER = habriveerET.text.toString()
            reptilia_attribute.HAB_EDGE = habedgeET.text.toString()

            reptilia_attribute.WATER_IN = waterinET.text.toString()
            reptilia_attribute.WATER_OUT = wateroutET.text.toString()

            reptilia_attribute.WATER_CONT = watercontET.text.toString()
            reptilia_attribute.WATER_QUAL = waterqualET.text.toString()

            if (waterdeptET.text.isNotEmpty()) {
                reptilia_attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
            }

            if (habareawET.text.isNotEmpty()) {
                reptilia_attribute.HAB_AREA_W = habareawET.text.toString().toInt()
            }

            if (habareahET.text.isNotEmpty()) {
                reptilia_attribute.HAB_AREA_H = habareahET.text.toString().toInt()
            }


            if (gpslatTV.text.isNotEmpty()) {
                reptilia_attribute.GPS_LAT = lat.toFloat()
            }

            if (gpslonTV.text.isNotEmpty()) {
                reptilia_attribute.GPS_LON = log.toFloat()
            }
            if (coordndET.text.isNotEmpty()) {
                reptilia_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                reptilia_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                reptilia_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                reptilia_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                reptilia_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                reptilia_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            if (memoryTV.text.isNotEmpty()) {
                reptilia_attribute.HAB_AREA = memoryTV.text.toString().toFloat()
            }
            reptilia_attribute.TEMP_YN = "N"

            if (page == dataArray.size) {
                dbManager!!.insertreptilia_attribute(reptilia_attribute)
                page = page!! + 1
            }

            val data2 = db!!.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

            if (dataArray != null) {
                dataArray.clear()
            }

            while (data2.moveToNext()) {

                chkdata = true

                var reptilia_attribute: Reptilia_attribute = export_attribute(data)

                dataArray.add(reptilia_attribute)

            }

            if (page!! < dataArray.size) {
                page = page!! + 1
            }

            reptiliapageTV.setText(page.toString() + " / " + dataArray.size)

            resetPage(page!!)

        }

        specnmET.setOnClickListener {
            startDlgReptilia()
        }

        famiET.setOnClickListener {
            startDlgReptilia()
        }

        scienET.setOnClickListener {
            startDlgReptilia()
        }

        weatherTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "날씨")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 1);

            startActivityForResult(intent, SET_WEATHER);
        }

        windTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "바람")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 2);

            startActivityForResult(intent, SET_WIND);
        }

        windDireTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "풍향")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 3);

            startActivityForResult(intent, SET_WIND_DIRE);
        }

        waterqualET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("좋음");
            listItems.add("보통");
            listItems.add("나쁨");

            alert(listItems, "수질", waterqualET, "waterqual");
        }

        watercontET.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("100m이상 연속");
            listItems.add("100m이상 연속지 중 단절된 웅덩이");
            listItems.add("단절된 웅덩이");

            alert(listItems, "수계", watercontET, "watercont");
        }

        reptiliaresetBT.setOnClickListener {
            specLL.visibility = View.GONE
            specnmET.visibility = View.VISIBLE
            specET.setText("")
        }

        btn_reptiliaDelete.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var reptilia_attribute: Reptilia_attribute = null_attribute()

                            if (pk != null) {

                                val data = db!!.query("reptiliaAttribute", dataList, "id = '$pk'", null, null, null, "", null)

                                while (data.moveToNext()) {

                                    reptilia_attribute = export_attribute(data)

                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {
                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator + reptilia_attribute.NUM.toString() + "_" + invtm + "_" + (j + 1) + ".png")) {
//                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/reptilia/images/" + reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM +"_" + (j+1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }
                                    val deletedir = path.listFiles()
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + reptilia_attribute.INV_DT + "." + reptilia_attribute.INV_TM + "."+reptilia_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + reptilia_attribute.INV_DT + "." + reptilia_attribute.INV_TM + "."+reptilia_attribute.INV_INDEX)
                                        deletepath.deleteRecursively()
                                    }
                                }


                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("reptiliaAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var reptilia_attribute: Reptilia_attribute = export_attribute(data)

                                        dataArray.add(reptilia_attribute)

                                    }

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager!!.deletereptilia_attribute(reptilia_attribute, pk)

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager!!.deletereptilia_attribute(reptilia_attribute, pk)

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
                        .setPositiveButton("확인", { dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("reptiliaAttribute", dataList, "id = '$id'", null, null, null, "", null)

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

        btn_reptiliaCancle1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dbManager: DataBaseHelper = DataBaseHelper(this)

                        val db = dbManager.createDataBase();

                        val dataList: Array<String> = arrayOf("*");

                        val data = db.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var reptilia_attribute: Reptilia_attribute = export_attribute(data)

                            dataArray.add(reptilia_attribute)

                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/")
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                val deletedir = path.listFiles()
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + reptilia_attribute.INV_DT + "." + reptilia_attribute.INV_TM + "."+reptilia_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + reptilia_attribute.INV_DT + "." + reptilia_attribute.INV_TM + "."+reptilia_attribute.INV_INDEX)
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

        btn_reptiliaSave1.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var reptilia_attribute: Reptilia_attribute = null_attribute()

                        keyId = intent.getStringExtra("GROP_ID")

                        reptilia_attribute.GROP_ID = keyId
                        reptilia_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
                        reptilia_attribute.CURRENT_TM = Utils.current_tm()
                        val prj = prjnameET.text.toString()
                        if (prj == prjname) {
                            reptilia_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            reptilia_attribute.PRJ_NAME = prjnameET.text.toString()
                        }

//                        reptilia_attribute.PRJ_NAME = prjnameET.text.toString()
//                        if (prjnameET.length() > 0){
//                            reptilia_attribute.PRJ_NAME = prjnameET.text.toString()
//                        } else {
//                            reptilia_attribute.PRJ_NAME = prjname
//                        }

//                        reptilia_attribute.INV_REGION = invregionET.text.toString()
                        if (invregionET.length() > 0) {
                            reptilia_attribute.INV_REGION = invregionET.text.toString();
                        } else {
                            reptilia_attribute.INV_REGION = INV_REGION
                        }

                        reptilia_attribute.INV_DT = createdDateTV.text.toString()
                        reptilia_attribute.INV_PERSON = invpersonET.text.toString()

                        reptilia_attribute.WEATHER = weatherTV.text.toString()
                        reptilia_attribute.WIND = windTV.text.toString()
                        reptilia_attribute.WIND_DIRE = windDireTV.text.toString()

                        if (temperaturET.text.isNotEmpty()) {
                            reptilia_attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
                        }

                        reptilia_attribute.ETC = etcET.text.toString()

                        if (numET.text.isNotEmpty()) {
                            reptilia_attribute.NUM = numET.text.toString().toInt()
                        }

                        reptilia_attribute.INV_TM = invtmTV.text.toString()

                        reptilia_attribute.SPEC_NM = specnmET.text.toString()
                        var specet = specET.text.toString()
                        if (specet != null && specet != "") {
                            reptilia_attribute.SPEC_NM = specet
                        }
                        reptilia_attribute.FAMI_NM = famiET.text.toString()
                        reptilia_attribute.SCIEN_NM = scienET.text.toString()

                        if (incntaduET.text.isNotEmpty()) {
                            reptilia_attribute.IN_CNT_ADU = incntaduET.text.toString().toInt()
                        }
                        if (incntlarET.text.isNotEmpty()) {
                            reptilia_attribute.IN_CNT_LAR = incntlarET.text.toString().toInt()
                        }
                        if (incnteggET.text.isNotEmpty()) {
                            reptilia_attribute.IN_CNT_EGG = incnteggET.text.toString().toInt()
                        }

                        reptilia_attribute.HAB_RIVEER = habriveerET.text.toString()
                        reptilia_attribute.HAB_EDGE = habedgeET.text.toString()

                        reptilia_attribute.WATER_IN = waterinET.text.toString()
                        reptilia_attribute.WATER_OUT = wateroutET.text.toString()

                        reptilia_attribute.WATER_CONT = watercontET.text.toString()
                        reptilia_attribute.WATER_QUAL = waterqualET.text.toString()

                        if (waterdeptET.text.isNotEmpty()) {
                            reptilia_attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
                        }

                        if (habareawET.text.isNotEmpty()) {
                            reptilia_attribute.HAB_AREA_W = habareawET.text.toString().toInt()
                        }

                        if (habareahET.text.isNotEmpty()) {
                            reptilia_attribute.HAB_AREA_H = habareahET.text.toString().toInt()
                        }


                        if (gpslatTV.text.isNotEmpty()) {
                            reptilia_attribute.GPS_LAT = lat.toFloat()
                        }

                        if (gpslonTV.text.isNotEmpty()) {
                            reptilia_attribute.GPS_LON = log.toFloat()
                        }
                        if (coordndET.text.isNotEmpty()) {
                            reptilia_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
                        }
                        if (coordnmET.text.isNotEmpty()) {
                            reptilia_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
                        }
                        if (coordnsET.text.isNotEmpty()) {
                            reptilia_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
                        }
                        if (coordedET.text.isNotEmpty()) {
                            reptilia_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
                        }
                        if (coordemET.text.isNotEmpty()) {
                            reptilia_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
                        }
                        if (coordesET.text.isNotEmpty()) {
                            reptilia_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
                        }
                        if (memoryTV.text.isNotEmpty()) {
                            reptilia_attribute.HAB_AREA = memoryTV.text.toString().toFloat()
                        }
                        reptilia_attribute.TEMP_YN = "Y"

                        reptilia_attribute.CONF_MOD = "N"

                        reptilia_attribute.GEOM = log.toString() + " " + lat.toString()

                        if (chkdata) {

                            if (pk != null) {

                                val CONF_MOD = confmodTV.text.toString()

                                if (CONF_MOD == "C" || CONF_MOD == "N") {
                                    reptilia_attribute.CONF_MOD = "M"
                                }

                                dbManager!!.updatereptilia_attribute(reptilia_attribute, pk)
                                dbManager!!.updatecommonreptilia(reptilia_attribute, keyId)
                            }

//                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator)
////                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/")
//                            val pathdir = path.listFiles()
//
//                            if(pathdir != null) {
//                                for (i in 0..pathdir.size-1) {
//
//                                    for(j in 0..pathdir.size-1) {
//
//                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator + reptilia_attribute.NUM.toString() + "_" + reptilia_attribute.INV_TM +"_" + (j+1) + ".png")) {
////                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/reptilia/images/" + reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM +"_" + (j+1) + ".png")) {
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
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/"
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
//                                saveVitmapToFile(images!!.get(i),outPath+reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM +"_" + (i+1) + ".png")

//                            }
//
//                            val deletedir = path.listFiles()
//                            if (deletedir.size == 0){
//                                if (path.isDirectory){
//                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator)
//                                    deletepath.deleteRecursively()
//                                }
//                            }

                        } else {

                            dbManager!!.insertreptilia_attribute(reptilia_attribute);

//                            var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            sdPath += "/ecology/tmps/" + reptilia_attribute.INV_DT +"."+ reptilia_attribute.INV_TM + "." + reptilia_attribute.NUM + "/images"
//                            val reptilia = File(sdPath)
//                            reptilia.mkdir();
////                          sdPath +="/imgs"
////                          sdPath +="/"+biotope_attribute.PIC_FOLDER
//
//                            val file = File(sdPath)
//                            file.mkdir();
//                            //이미 있다면 삭제. 후 생성
////                            setDirEmpty(sdPath)
//
//                            sdPath+="/"

//                            var pathArray:ArrayList<String> = ArrayList<String>()
//
//                            for(i   in 0..images!!.size-1){
//                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator
////                                val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/reptilia/images/"
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
//                                saveVitmapToFile(images!!.get(i),outPath+reptilia_attribute.NUM+"_"+reptilia_attribute.INV_TM+"_"+(i+1)+".png")
//
//                            }

                        }

                        var intent = Intent()

                        intent.putExtra("export", 70)

                        setResult(RESULT_OK, intent);

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()


        }

        btn_add.setOnClickListener {
            var reptilia_attribute: Reptilia_attribute = null_attribute()

            keyId = intent.getStringExtra("GROP_ID")

            reptilia_attribute.GROP_ID = keyId
            reptilia_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
            reptilia_attribute.CURRENT_TM = Utils.current_tm()
            val prj = prjnameET.text.toString()
            if (prj == prjname) {
                reptilia_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
            } else {
                reptilia_attribute.PRJ_NAME = prjnameET.text.toString()
            }

//            reptilia_attribute.PRJ_NAME = prjnameET.text.toString()
//            if (prjnameET.length() > 0){
//                reptilia_attribute.PRJ_NAME = prjnameET.text.toString()
//            } else {
//                reptilia_attribute.PRJ_NAME = prjname
//            }


//            reptilia_attribute.INV_REGION = invregionET.text.toString()
            if (invregionET.length() > 0) {
                reptilia_attribute.INV_REGION = invregionET.text.toString();
            } else {
                reptilia_attribute.INV_REGION = INV_REGION
            }
            reptilia_attribute.INV_DT = createdDateTV.text.toString()
            reptilia_attribute.INV_PERSON = invpersonET.text.toString()

            reptilia_attribute.WEATHER = weatherTV.text.toString()
            reptilia_attribute.WIND = windTV.text.toString()
            reptilia_attribute.WIND_DIRE = windDireTV.text.toString()

            if (temperaturET.text.isNotEmpty()) {
                reptilia_attribute.TEMPERATUR = temperaturET.text.toString().toFloat()
            }

            reptilia_attribute.ETC = etcET.text.toString()

            if (numET.text.isNotEmpty()) {
                reptilia_attribute.NUM = numET.text.toString().toInt()
            }

            reptilia_attribute.INV_TM = invtmTV.text.toString()

            reptilia_attribute.SPEC_NM = specnmET.text.toString()
            var specet = specET.text.toString()
            if (specet != null && specet != "") {
                reptilia_attribute.SPEC_NM = specet
            }
            reptilia_attribute.FAMI_NM = famiET.text.toString()
            reptilia_attribute.SCIEN_NM = scienET.text.toString()

            if (incntaduET.text.isNotEmpty()) {
                reptilia_attribute.IN_CNT_ADU = incntaduET.text.toString().toInt()
            }
            if (incntlarET.text.isNotEmpty()) {
                reptilia_attribute.IN_CNT_LAR = incntlarET.text.toString().toInt()
            }
            if (incnteggET.text.isNotEmpty()) {
                reptilia_attribute.IN_CNT_EGG = incnteggET.text.toString().toInt()
            }

            reptilia_attribute.HAB_RIVEER = habriveerET.text.toString()
            reptilia_attribute.HAB_EDGE = habedgeET.text.toString()

            reptilia_attribute.WATER_IN = waterinET.text.toString()
            reptilia_attribute.WATER_OUT = wateroutET.text.toString()

            reptilia_attribute.WATER_CONT = watercontET.text.toString()
            reptilia_attribute.WATER_QUAL = waterqualET.text.toString()

            if (waterdeptET.text.isNotEmpty()) {
                reptilia_attribute.WATER_DEPT = waterdeptET.text.toString().toInt()
            }

            if (habareawET.text.isNotEmpty()) {
                reptilia_attribute.HAB_AREA_W = habareawET.text.toString().toInt()
            }

            if (habareahET.text.isNotEmpty()) {
                reptilia_attribute.HAB_AREA_H = habareahET.text.toString().toInt()
            }


            if (gpslatTV.text.isNotEmpty()) {
                reptilia_attribute.GPS_LAT = lat.toFloat()
            }

            if (gpslonTV.text.isNotEmpty()) {
                reptilia_attribute.GPS_LON = log.toFloat()
            }
            if (coordndET.text.isNotEmpty()) {
                reptilia_attribute.GPSLAT_DEG = coordndET.text.toString().toInt()
            }
            if (coordnmET.text.isNotEmpty()) {
                reptilia_attribute.GPSLAT_MIN = coordnmET.text.toString().toInt()
            }
            if (coordnsET.text.isNotEmpty()) {
                reptilia_attribute.GPSLAT_SEC = coordnsET.text.toString().toFloat()
            }
            if (coordedET.text.isNotEmpty()) {
                reptilia_attribute.GPSLON_DEG = coordedET.text.toString().toInt()
            }
            if (coordemET.text.isNotEmpty()) {
                reptilia_attribute.GPSLON_MIN = coordemET.text.toString().toInt()
            }
            if (coordesET.text.isNotEmpty()) {
                reptilia_attribute.GPSLON_SEC = coordesET.text.toString().toFloat()
            }
            if (memoryTV.text.isNotEmpty()) {
                reptilia_attribute.HAB_AREA = memoryTV.text.toString().toFloat()
            }
            reptilia_attribute.TEMP_YN = "Y"

            reptilia_attribute.CONF_MOD = "N"

            reptilia_attribute.GEOM = log.toString() + " " + lat.toString()


            if (chkdata) {

                if (pk != null) {

                    val CONF_MOD = confmodTV.text.toString()

                    if (CONF_MOD == "C" || CONF_MOD == "N") {
                        reptilia_attribute.CONF_MOD = "M"
                    }

                    dbManager!!.updatereptilia_attribute(reptilia_attribute, pk)
                    dbManager!!.updatecommonreptilia(reptilia_attribute, keyId)
                }

//                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator)
////                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/")
//                val pathdir = path.listFiles()
//
//                if(pathdir != null) {
//                    for (i in 0..pathdir.size-1) {
//
//                        for(j in 0..pathdir.size-1) {
//
//                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator + reptilia_attribute.NUM.toString() + "_" + reptilia_attribute.INV_TM +"_" + (j+1) + ".png")) {
////                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/reptilia/images/" + reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM +"_" + (j+1) + ".png")) {
//
//                                pathdir.get(i).canonicalFile.delete()
//
//                                println("delete ===============")
//
//                            }
//                        }
//
//                    }
//
//                }
//
//                for(i   in 0..images!!.size-1){
//
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator
////                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/"
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
//                    saveVitmapToFile(images!!.get(i),outPath+reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM +"_" + (i+1) + ".png")

//                }

            } else {

                dbManager!!.insertreptilia_attribute(reptilia_attribute);

//                var sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//                sdPath += "/ecology/data/birds/images" + reptilia_attribute.INV_DT +"_"+ reptilia_attribute.INV_TM + "_" + reptilia_attribute.NUM
//                val reptilia = File(sdPath)
//                reptilia.mkdir();
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
//                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images"+ File.separator +keyId+ File.separator
////                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/reptilia/images/"
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
//                    saveVitmapToFile(images!!.get(i),outPath+reptilia_attribute.NUM + "_" + reptilia_attribute.INV_TM+"_"+(i+1)+".png")
//
//                }

            }

            if (intent.getStringExtra("set") != null) {
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            btn_reptiliaDelete.visibility = View.GONE

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

    private fun imageFromGallery() {

        val intent1 = Intent(context, WriteAlbumActivity::class.java)
//        startActivity(intent1);
        startActivityForResult(intent1, FROM_ALBUM)

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

    fun startDlgReptilia() {
        val intent = Intent(context, DlgReptiliaActivity::class.java)
        intent.putExtra("title", "양서ㆍ파충류 선택")
        intent.putExtra("table", "Amphibian")
        intent.putExtra("DlgHeight", 600f);

        if (specnmET.text != null && specnmET.text != "") {
            val SPEC = specnmET.text.toString()
            intent.putExtra("SPEC", SPEC)
        }
        startActivityForResult(intent, SET_REPTILIA);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_WEATHER -> {

                    weatherTV.text = data!!.getStringExtra("selectDlg")

                }
                SET_WIND -> {

                    windTV.text = data!!.getStringExtra("selectDlg")

                }
                SET_WIND_DIRE -> {

                    windDireTV.text = data!!.getStringExtra("selectDlg")

                }

                SET_REPTILIA -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    if (name == "SP(미동정)") {
                        specnmET.visibility = View.GONE
                        specLL.visibility = View.VISIBLE
                    }

                    specnmET.text = name
                    famiET.text = family_name
                    scienET.text = zoological

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

                            val num = numET.text.toString()
                            var time = ""
                            time = invtmTV.text.toString()
                            var timesplit = time.split(":")
                            invtm = timesplit.get(0) + timesplit.get(1)
                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator
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
                            println("images_size ${images!!.size}")
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
                    val child = addPicturesLL!!.getChildCount()
                    for (i in 0 until child) {

                        println("test : $i")

                        val v = addPicturesLL!!.getChildAt(i)

                        val num = numET.text.toString()
                        var time = ""
                        time = invtmTV.text.toString()
                        var timesplit = time.split(":")
                        invtm = timesplit.get(0) + timesplit.get(1)
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator
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

            if ("waterqual" == type) {

            }


        })

        builder.show();
    }

    fun clickMethod(v: View) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("삭제하시겠습니까 ? ").setCancelable(false)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator
                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)
                    val num = numET.text.toString()
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
                        ImageLoader.getInstance().displayImage(images_url!!.get(k), imageIV, Utils.UILoptions)
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
                            val num = numET.text.toString()

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
                            val num = numET.text.toString()
                            val invtm = invtmTV.text.toString()


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
        var c = dbManager!!.pkNum("reptiliaAttribute")
        numET.text = c.toString()
        /* var num = numET.text.toString()
         if (num.length > 7){
             var textnum = num.substring(num.length - 2, num.length)
             var splitnum = num.substring(0, num.length - 2)
             var plusnum = textnum.toInt() + 1
             numET.setText(splitnum.toString() + plusnum.toString())
         } else {
             var textnum = num.substring(num.length - 1, num.length)
             var splitnum = num.substring(0, num.length - 1)
             var plusnum = textnum.toInt() + 1
             numET.setText(splitnum.toString() + plusnum.toString())
         }*/

        invtmTV.setText(Utils.timeStr())
        specnmET.setText("")
        famiET.setText("")
        scienET.setText("")

        incntaduET.setText("")
        incntlarET.setText("")
        incnteggET.setText("")

//        habriveerET.setText("")
//        habedgeET.setText("")

//        waterinET.setText("")
//        wateroutET.setText("")
//        waterdeptET.setText("")

//        habareawET.setText("")
//        habareahET.setText("")

//        watercontET.setText("")
//        waterqualET.setText("")

        confmodTV.setText("")

        addPicturesLL!!.removeAllViews()

    }

    fun resetPage(page: Int) {
        val dataList: Array<String> = arrayOf("*");

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val id = intent.getStringExtra("id")

        val data = db.query("reptiliaAttribute", dataList, "id = '$id'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            chkdata = true

            var reptilia_attribute: Reptilia_attribute = export_attribute(data)

            dataArray.add(reptilia_attribute)

            invregionET.setText(reptilia_attribute.INV_REGION)
            if (invregionET.text == null) {
                invregionET.setText("")
            }

            createdDateTV.setText(reptilia_attribute.INV_DT)
            if (createdDateTV.text == null) {
                createdDateTV.setText("")
            }

            weatherTV.setText(reptilia_attribute.WEATHER)
            if (weatherTV.text == null) {
                weatherTV.setText("")
            }

            windTV.setText(reptilia_attribute.WIND)
            if (windTV.text == null) {
                windTV.setText("")
            }

            windDireTV.setText(reptilia_attribute.WIND_DIRE)
            if (windDireTV.text == null) {
                windDireTV.setText("")
            }

            temperaturET.setText(reptilia_attribute.TEMPERATUR.toString())
            if (temperaturET.text == null) {
                temperaturET.setText("")
            }

            etcET.setText(reptilia_attribute.ETC)
            if (etcET.text == null) {
                etcET.setText("")
            }

            numET.setText(reptilia_attribute.NUM.toString())
            if (numET.text == null) {
                numET.setText("")
            }

            invtmTV.setText(reptilia_attribute.INV_TM)
            if (invtmTV.text == null) {
                invtmTV.setText("")
            }

            specnmET.setText(reptilia_attribute.SPEC_NM)
            if (specnmET.text == null) {
                specnmET.setText("")
            }

            famiET.setText(reptilia_attribute.FAMI_NM)
            if (famiET.text == null) {
                famiET.setText("")
            }

            scienET.setText(reptilia_attribute.SCIEN_NM)
            if (scienET.text == null) {
                scienET.setText("")
            }

            incntaduET.setText(reptilia_attribute.IN_CNT_ADU.toString())
            if (incntaduET.text == null) {
                incntaduET.setText("")
            }
            incntlarET.setText(reptilia_attribute.IN_CNT_LAR.toString())
            if (incntlarET.text == null) {
                incntlarET.setText("")
            }
            incnteggET.setText(reptilia_attribute.IN_CNT_EGG.toString())
            if (incnteggET.text == null) {
                incnteggET.setText("")
            }

            habriveerET.setText(reptilia_attribute.HAB_RIVEER)
            if (habriveerET.text == null) {
                habriveerET.setText("")
            }
            habedgeET.setText(reptilia_attribute.HAB_EDGE)
            if (habedgeET.text == null) {
                habedgeET.setText("")
            }

            waterinET.setText(reptilia_attribute.WATER_IN)
            if (waterinET.text == null) {
                waterinET.setText("")
            }

            wateroutET.setText(reptilia_attribute.WATER_OUT)
            if (wateroutET.text == null) {
                wateroutET.setText("")
            }

            watercontET.setText(reptilia_attribute.WATER_CONT)
            if (watercontET.text == null) {
                watercontET.setText("")
            }

            waterqualET.setText(reptilia_attribute.WATER_QUAL)
            if (waterqualET.text == null) {
                waterqualET.setText("")
            }

            waterdeptET.setText(reptilia_attribute.WATER_DEPT.toString())
            if (waterdeptET.text == null) {
                waterdeptET.setText("")
            }

            habareawET.setText(reptilia_attribute.HAB_AREA_W.toString())
            if (habareawET.text == null) {
                habareawET.setText("0")
            }
            habareahET.setText(reptilia_attribute.HAB_AREA_H.toString())
            if (habareahET.text == null) {
                habareahET.setText("0")
            }

            gpslatTV.setText(reptilia_attribute.GPS_LAT.toString())
            gpslonTV.setText(reptilia_attribute.GPS_LON.toString())

            coordndET.setText(reptilia_attribute.GPSLAT_DEG.toString())
            coordnmET.setText(reptilia_attribute.GPSLAT_MIN.toString())
            coordnsET.setText(reptilia_attribute.GPSLAT_SEC.toString())
            coordedET.setText(reptilia_attribute.GPSLON_DEG.toString())
            coordemET.setText(reptilia_attribute.GPSLON_MIN.toString())
            coordesET.setText(reptilia_attribute.GPSLON_SEC.toString())
            memoryTV.setText(reptilia_attribute.HAB_AREA.toString())

        }


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

    override fun onLocationUpdated(p0: Location?) {

        stopLocation()

        if (p0 != null) {

            latitude = p0.getLatitude().toFloat()
            longitude = p0.getLongitude().toFloat()

            gpslatTV.setText(latitude.toString())
            gpslonTV.setText(longitude.toString())

            if (progressDialog != null) {
                progressDialog!!.dismiss()
            }
        }

    }


    private fun stopLocation() {
        SmartLocation.with(context).location().stop()
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

        val data = db!!.query("reptiliaAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var reptilia_attribute: Reptilia_attribute = export_attribute(data)

            dataArray.add(reptilia_attribute)

        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

            var intent = Intent()
            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                println("deletedir.size ${deletedir.size}")
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory) {
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "reptilia/images" + File.separator + keyId + File.separator)
//                                      val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                    deletepath.deleteRecursively()
                }

            }

        }

        data.close()

        finish()

    }

    fun memory() {


        habareawET.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var width = Utils.getString(habareawET)
                var height = Utils.getString(habareahET)
                if (width != "" && height != "") {
                    memoryTV.text = ((width.toFloat() * height.toFloat()).toString())
                }

            }
        })
        habareahET.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var width = Utils.getString(habareawET)
                var height = Utils.getString(habareahET)
                if (width != "" && height != "") {
                    memoryTV.text = ((width.toFloat() * height.toFloat()).toString())
                }
            }
        })
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
        createdDateTV.text = msgs[0]+"-"+month+"-"+day
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
            invtmTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }


    fun null_attribute(): Reptilia_attribute {
        var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
                , null, null, null, null, null, null
                , null, null, null, null, null, null, null,null,null)
        return reptilia_attribute
    }

    fun export_attribute(data: Cursor): Reptilia_attribute {
        var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getInt(17), data.getInt(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                , data.getString(23), data.getString(24), data.getString(25), data.getInt(26), data.getInt(27), data.getInt(28), data.getFloat(29), data.getFloat(30), data.getString(31), data.getString(32), data.getString(33)
                , data.getInt(34), data.getInt(35), data.getFloat(36), data.getInt(37), data.getInt(38), data.getFloat(39)
                , data.getFloat(40), data.getString(41), data.getString(42)
        )
        return reptilia_attribute
    }


}
