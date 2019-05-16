package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Waypoint
import kotlinx.android.synthetic.main.activity_fish.*
import kotlinx.android.synthetic.main.activity_way_point.*
import kotlinx.android.synthetic.main.activity_way_point.btnPIC_FOLDER
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class WayPointActivity : Activity() {

    lateinit var context: Context;

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var lat:String = ""
    var log:String = ""
    var pk: String? = null
    var userName = "";
    var keyId = ""

    var invtm = ""

    var chkdata = false

    var cameraPath:String? = null

    private var addPicturesLL: LinearLayout? = null
    private val imgSeq = 0

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    var dataArray:ArrayList<Waypoint> = ArrayList<Waypoint>()

    var markerid : String? = null


    val REQUEST_FINE_LOCATION = 50
    val REQUEST_ACCESS_COARSE_LOCATION = 51
    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101
    var imageUri: Uri? = null

    var prjname = ""

    var INV_REGION = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_way_point)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        userName = PrefUtils.getStringPreference(context, "name");

        addPicturesLL = findViewById(R.id.addPicturesLL)

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        invpersonTV.setText(userName)
        invdtTV.setText(Utils.todayStr())
        prjnameTV.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        invdtTV.setOnClickListener {
            datedlg()
        }
        invtmTV.setOnClickListener {
            timedlg()
        }

        var intent: Intent = getIntent();

        var today = Utils.todayStr();

        var todays = today.split("-")

        var time = Utils.timeStr()
        invtmTV.setText(time)
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

       /* var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

        for (i in 1 until todays.size){
            texttoday += todays.get(i)
        }*/
//        numTV.setText(texttoday + "1")

        var c = dbManager!!.pkNum("Waypoint")
        numTV.text = c.toString()

        if(intent.getStringExtra("markerid") != null){
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

        if (intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
//                    invregionTV.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        val dataList: Array<String> = arrayOf("*");

        if (intent.getStringExtra("id") != null) {
            waypointdeleteBT.visibility = View.VISIBLE
            pk = intent.getStringExtra("id")
            val data = db!!.query("Waypoint", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var waypoint:Waypoint = Waypoint(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getFloat(8),data.getFloat(9),data.getString(10),data.getString(11))

                invregionTV.setText(waypoint.INV_REGION)
                INV_REGION = waypoint.INV_REGION.toString()
                invdtTV.setText(waypoint.INV_DT)
                invtmTV.setText(waypoint.INV_TM)
                var timesplit = waypoint.INV_TM!!.split(":")
                invtm = timesplit.get(0) + timesplit.get(1)
                numTV.setText(waypoint.NUM)
                invpersonTV.setText(waypoint.INV_PERSON)
                prjnameTV.setText(waypoint.PRJ_NAME)
                gpslatTV.setText(waypoint.GPS_LAT.toString())
                gpslonTV.setText(waypoint.GPS_LON.toString())
                memoTV.setText(waypoint.MEMO)

                val tmpfiles =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                var tmpfileList = tmpfiles.listFiles()

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


                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator + waypoint.NUM.toString() + "_" + invtm +"_" + (j+1) + ".png")) {
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

        waypointsaveBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var waypoint: Waypoint = Waypoint(null,null,null,null,null,null,null,null,null,null,null,null)
                        keyId = intent.getStringExtra("GROP_ID")
                        waypoint.GROP_ID = keyId
//                        waypoint.INV_REGION = invregionTV.text.toString()
                        if (invregionTV.length() > 0){
                            waypoint.INV_REGION = invregionTV.text.toString();
                        } else {
                            waypoint.INV_REGION = INV_REGION
                        }
                        waypoint.INV_DT = invdtTV.text.toString()
                        waypoint.INV_TM = invtmTV.text.toString()
                        waypoint.NUM = numTV.text.toString()
                        waypoint.INV_PERSON = invpersonTV.text.toString()

                        val prj = prjnameTV.text.toString()
                        if (prj == prjname){
                            waypoint.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
                        } else {
                            waypoint.PRJ_NAME = prjnameTV.text.toString()
                        }

//                        waypoint.PRJ_NAME = prjnameTV.text.toString()
//                        if (prjnameTV.length() > 0){
//                            waypoint.PRJ_NAME = prjnameTV.text.toString()
//                        } else {
//                            waypoint.PRJ_NAME = prjname
//                        }

                        waypoint.GPS_LAT = gpslatTV.text.toString().toFloat()
                        waypoint.GPS_LON = gpslonTV.text.toString().toFloat()
                        waypoint.MEMO = memoTV.text.toString()
                        waypoint.GEOM = log.toString() + " " + lat.toString()

                        if (chkdata) {
                            if(pk != null){
                                dbManager!!.updatewaypoint(waypoint,pk)
                            }
                        } else {
                            dbManager!!.insertWayPoint(waypoint)
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
        waypointcancleBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dataList: Array<String> = arrayOf("*");

                        val data = db!!.query("Waypoint", dataList, "id = '$pk'", null, null, null, "", null)

                        while (data.moveToNext()) {

                            var waypoint:Waypoint = Waypoint(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getFloat(8),data.getFloat(9),data.getString(10),data.getString(11))

                            dataArray.add(waypoint)
                        }

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null ){

                            var intent = Intent()
                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                            val pathdir = path.listFiles()

                            if (pathdir != null) {

                                val deletedir = path.listFiles()
                                println("deletedir.size ${deletedir.size}")
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                                    path.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
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

        waypointdeleteBT.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var waypoint: Waypoint = Waypoint(null,null,null,null,null,null,null,null,null,null,null,null)

                        if (pk != null) {

                            val data = db!!.query("Waypoint", dataList, "id = '$pk'", null, null, null, "", null)
                            while (data.moveToNext()) {
                                var waypoint:Waypoint = Waypoint(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getFloat(8),data.getFloat(9),data.getString(10),data.getString(11))
                            }
                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                for (i in 0..pathdir.size - 1) {

                                    for (j in 0..pathdir.size - 1) {

                                        if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator + waypoint.NUM.toString() + "_" + waypoint.INV_TM +"_" + (j+1) + ".png")) {
                                            pathdir.get(i).canonicalFile.delete()
                                        }
                                    }

                                }
                                val deletedir = path.listFiles()
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                                    path.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory){
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                                    path.deleteRecursively()
                                }
                            }
                            if (intent.getStringExtra("GROP_ID") != null) {
                                val GROP_ID = intent.getStringExtra("GROP_ID")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("Waypoint", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {
                                    chkdata = true
                                    var waypoint:Waypoint = Waypoint(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getFloat(8),data.getFloat(9),data.getString(10),data.getString(11))
                                    dataArray.add(waypoint)
                                }

                                var intent = Intent()

                                if (dataArray.size > 1) {
                                    dbManager!!.deletewaypoint(pk!!)

                                    intent.putExtra("reset", 100)

                                    setResult(RESULT_OK, intent);
                                    finish()

                                }

                                if (dataArray.size == 1) {
                                    dbManager!!.deletewaypoint(pk!!)

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

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            } else if (Manifest.permission.ACCESS_COARSE_LOCATION == perm) {
//                checkGPs()
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

                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "insect/images"+ File.separator +keyId+ File.separator
                    addPicturesLL!!.removeAllViews()
                    images!!.clear()
                    val tag = v.tag as Int
                    images_path!!.removeAt(tag)

                    val num = numTV.text.toString()
                    var file = File(outPath+num + "_" + invtmTV+"_"+(tag+1)+".png")
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
                            val num = numTV.text.toString()
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
    override fun onBackPressed() {

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val dataList: Array<String> = arrayOf("*");

        val data = db.query("Waypoint", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var waypoint:Waypoint = Waypoint(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getFloat(8),data.getFloat(9),data.getString(10),data.getString(11))

            dataArray.add(waypoint)
        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null ){

            var intent = Intent()
            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
            val pathdir = path.listFiles()

            if (pathdir != null) {
                val deletedir = path.listFiles()
                if (path.isDirectory){
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                    deletepath.deleteRecursively()
                }
            } else {
                if (path.isDirectory){
                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator)
                    deletepath.deleteRecursively()
                }

            }

        }

        data.close()

        finish()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

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
                            time = invtmTV.text.toString()
                            var timesplit = time.split(":")
                            invtm = timesplit.get(0) + timesplit.get(1)
                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator
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
                            saveVitmapToFile(images!!.get(i),outPath+num + "_" + invtm+"_"+(i+1)+".png")

                        }
                        images!!.clear()
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

                        val num = numTV.text.toString()
                        var time = ""
                        time = invtmTV.text.toString()
                        var timesplit = time.split(":")
                        invtm = timesplit.get(0) + timesplit.get(1)
                        val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "waypoint/images"+ File.separator +keyId+ File.separator
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
                        saveVitmapToFile(images!!.get(i),outPath+num + "_" + invtm+"_"+(i+1)+".png")

                    }
                    images!!.clear()
                }
            }
        }
    }

    fun timedlg() {
        val cal = Calendar.getInstance()
        val dialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
            var hour_s = hour.toString()
            var min_s = min.toString()
            if (min_s.length!=2){
                min_s = "0"+min_s
            }
            if (hour_s.length!=2){
                hour_s = "0"+hour_s
            }
            val msg = String.format("%s : %s", hour_s, min_s)
            invtmTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
    }


    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt()-1, days[2].toInt()).show()
    }
    private val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%d-%d", year, monthOfYear+1, dayOfMonth)
        invdtTV.text = msg
    }

}
