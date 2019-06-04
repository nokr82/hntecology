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
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import au.com.objectix.jgridshift.Util
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.activities.MainActivity.Companion.REQUEST_ACCESS_COARSE_LOCATION
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.FileFilter
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_flora2.*
import kotlinx.android.synthetic.main.activity_flora2.*
import kotlinx.android.synthetic.main.activity_flora2.addPicturesLL
import kotlinx.android.synthetic.main.activity_flora2.btnPIC_FOLDER
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*

class Flora2Activity : Activity() {

    lateinit var context: Context;

    var markerid: String? = null
    var chkdata: Boolean = false;
    var basechkdata = false
    var keyId: String? = null;
    var pk: String? = null
    var userName = "";

    var latitude = 0.0f;
    var longitude = 0.0f;


    var cameraPath:String? = null
    var imageUri: Uri? = null

    private val imgSeq = 0

    var invtm = ""

    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    var lat: String = ""
    var log: String = ""

    var dataArray: ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()

    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101

    val SET_DATA2 = 2
    val SET_DATA1 = 1;
    val SET_DATA3 = 3
    val SET_DATA4 = 4
    val SET_DATA5 = 5
    val SET_DATA6 = 6

    val SET_INPUT = 2007
    val SET_INPUT2 = 2008


    var trepage = 1
    var strepage = 1
    var shrpage = 1
    var herpage = 1

    var TreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var StreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var ShrDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()
    var HerDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()
    var t_name = ""
    var dataPk = -1

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var INV_REGION = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flora2)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

//        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        userName = PrefUtils.getStringPreference(context, "name");
        invpersonTV.setText(userName)
        invdtTV.setText(Utils.todayStr())

        invdtTV.setOnClickListener {
            datedlg()
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
                            val intent1 = Intent(context, WriteAlbumActivity::class.java)
                            startActivityForResult(intent1, FROM_ALBUM)
                        }
                    }
                }

            })
            builder.show();

        }




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


        if (intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    invregionTV.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }



        keyId = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("GROP_ID") != null) {

            var AllDatas: ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()

            val grop_id = intent.getStringExtra("GROP_ID")

            println("flora2gropid ${intent.getStringExtra("GROP_ID")}")

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$grop_id'", null, null, null, "id", null)

            while (data.moveToNext()) {

                var manyFloraAttribute = ps_many_attribute(data)
                // 교목층
                if (manyFloraAttribute.DOMIN.toString() != "null") {
                    t_name =manyFloraAttribute.DOMIN.toString()
                }
                var domins = t_name.split("-")
                if (domins.size>1){
                    dominTV.text = domins[0]
                    ausTV.text = domins[1]
                }else{
                    dominTV.text = t_name
                }
                invregionTV.setText(manyFloraAttribute.INV_REGION)
                INV_REGION = manyFloraAttribute.INV_REGION.toString()

                invdtTV.setText(manyFloraAttribute.INV_DT)
                invpersonTV.setText(manyFloraAttribute.INV_PERSON)

                dataPk = manyFloraAttribute.id!!.toInt()

                val PK = manyFloraAttribute.id!!.toInt()
                val TRE_NUM = manyFloraAttribute.TRE_NUM
                val TRE_SPEC = manyFloraAttribute.TRE_SPEC
                val TRE_FAMI = manyFloraAttribute.TRE_FAMI
                val TRE_SCIEN = manyFloraAttribute.TRE_SCIEN
                val TRE_DBH = manyFloraAttribute.TRE_DBH
                val TRE_TOIL = manyFloraAttribute.TRE_TOIL
                val TRE_UNDER = manyFloraAttribute.TRE_UNDER
                val TRE_WATERWIDTH = manyFloraAttribute.TRE_WATER
                val TRE_TYPE = manyFloraAttribute.TRE_TYPE

                println("TRE_NUM $TRE_NUM TRE_SCIEN $TRE_SCIEN TRE_DBH $TRE_DBH TRE_TOIL $TRE_TOIL TRE_UNDER $TRE_UNDER")

                if (TRE_SPEC!!.length > 0 || TRE_DBH!! != 0.0f || TRE_TOIL!! != 0.0f || TRE_WATERWIDTH != 0) {
                    val data = TreeData1(PK, TRE_NUM, TRE_SPEC, TRE_FAMI, TRE_SCIEN, TRE_DBH, TRE_TOIL, TRE_UNDER, TRE_WATERWIDTH, TRE_TYPE)
                    TreDatas.add(data)
                    println("TRE_NUM ADD ${data.PAGE}")
                    trepage = manyFloraAttribute.TRE_NUM!!
                    trenumTV.setText(manyFloraAttribute.TRE_NUM.toString())
                    etTRE_SPECET.setText(manyFloraAttribute.TRE_SPEC)
                    etTRE_FAMIET.setText(manyFloraAttribute.TRE_FAMI)
                    etTRE_SCIENET.setText(manyFloraAttribute.TRE_SCIEN)
                    etTRE_HET.setText(manyFloraAttribute.TRE_DBH.toString())
                    etTRE_BREAET.setText(manyFloraAttribute.TRE_TOIL.toString())
                    etTRE_COVEET.setText(manyFloraAttribute.TRE_UNDER.toString())
                    etTRE_WATERWIDTH.setText(manyFloraAttribute.TRE_WATER.toString())
                    etTRE_TYPE.setText(manyFloraAttribute.TRE_TYPE.toString())
                }

                // 아교목층

                val STRE_NUM = manyFloraAttribute.STRE_NUM
                val STRE_SPEC = manyFloraAttribute.STRE_SPEC
                val STRE_FAMI = manyFloraAttribute.STRE_FAMI
                val STRE_SCIEN = manyFloraAttribute.STRE_SCIEN
                val STRE_DBH = manyFloraAttribute.STRE_DBH
                val STRE_TOIL = manyFloraAttribute.STRE_TOIL
                val STRE_UNDER = manyFloraAttribute.STRE_UNDER
                val STRE_WATER = manyFloraAttribute.STRE_WATER
                val STRE_TYPE = manyFloraAttribute.STRE_TYPE

                println("STRE_NUM $STRE_NUM STRE_SPEC $STRE_FAMI STRE_FAMI $STRE_FAMI STRE_SCIEN $STRE_SCIEN STRE_H $STRE_DBH")

                if (STRE_SPEC!!.length > 0 || STRE_DBH!! != 0.0f || STRE_TOIL!! != 0.0f || STRE_WATER != 0) {
                    val data = TreeData1(PK, STRE_NUM, STRE_SPEC, STRE_FAMI, STRE_SCIEN, STRE_DBH, STRE_TOIL, STRE_UNDER, STRE_WATER, STRE_TYPE)
                    StreDatas.add(data)
                    strepage = manyFloraAttribute.STRE_NUM!!
                    strenumTV.setText(manyFloraAttribute.STRE_NUM.toString())
                    etSTRE_SPECET.setText(manyFloraAttribute.STRE_SPEC)
                    etSTRE_FAMIET.setText(manyFloraAttribute.STRE_FAMI)
                    etSTRE_SCIENET.setText(manyFloraAttribute.STRE_SCIEN)
                    etSTRE_HET.setText(manyFloraAttribute.STRE_DBH.toString())
                    etSTRE_BREAET.setText(manyFloraAttribute.STRE_TOIL.toString())
                    etSTRE_COVEET.setText(manyFloraAttribute.STRE_UNDER.toString())
                    etSTRE_WATERWIDTH.setText(manyFloraAttribute.STRE_WATER.toString())
                    etSTRE_TYPE.setText(manyFloraAttribute.STRE_TYPE.toString())

                }

                //관목층

                val SHR_NUM = manyFloraAttribute.SHR_NUM
                val SHR_SPEC = manyFloraAttribute.SHR_SPEC
                val SHR_FAMI = manyFloraAttribute.SHR_FAMI
                val SHR_SCIEN = manyFloraAttribute.SHR_SCIEN
                val SHR_TOIL = manyFloraAttribute.SHR_TOIL
                val SHR_WATERWIDTH = manyFloraAttribute.SHR_WATER
                val SHR_UNDER = manyFloraAttribute.SHR_UNDER

                println("SHR_NUM $SHR_NUM SHR_SPEC $SHR_SPEC SHR_FAMI $SHR_FAMI SHR_SCIEN $SHR_SCIEN SHR_TOIL $SHR_TOIL")

                if (SHR_SPEC!!.length > 0 || SHR_TOIL!! != 0.0f || SHR_WATERWIDTH != 0.0f) {
                    shrpage = manyFloraAttribute.SHR_NUM!!
                    val data = TreeData2(PK, SHR_NUM, SHR_SPEC, SHR_FAMI, SHR_SCIEN, SHR_TOIL.toString(), SHR_WATERWIDTH.toString(), SHR_UNDER)
                    ShrDatas.add(data)
                    shrnumTV.setText(manyFloraAttribute.SHR_NUM.toString())
                    etSHR_SPECET.setText(manyFloraAttribute.SHR_SPEC)
                    etSHR_FAMIET.setText(manyFloraAttribute.SHR_FAMI)
                    etSHR_SCIENET.setText(manyFloraAttribute.SHR_SCIEN)
                    etSHR_HET.setText(manyFloraAttribute.SHR_TOIL.toString())
                    etSTR_COVEET.setText(manyFloraAttribute.SHR_WATER.toString())
                    etSHR_UNDER.setText(manyFloraAttribute.SHR_UNDER.toString())
                }

                val HER_NUM = manyFloraAttribute.HER_NUM
                val HER_SPEC = manyFloraAttribute.HER_SPEC
                val HER_FAMI = manyFloraAttribute.HER_FAMI
                val HER_SCIEN = manyFloraAttribute.HER_SCIEN
                val HER_DOMIN = manyFloraAttribute.HER_DOMIN
                val HER_GUNDO = manyFloraAttribute.HER_GUNDO
                val HER_HEIGHT = manyFloraAttribute.HER_HEIGHT

                println("HER_NUM $HER_NUM HER_SPEC $HER_SPEC HER_FAMI $HER_FAMI HER_SCIEN $HER_SCIEN HER_DOMIN $HER_DOMIN")

                if (HER_SPEC!!.length > 0 || HER_DOMIN!! != 0.0f || HER_GUNDO != 0.0f) {
                    herpage = manyFloraAttribute.HER_NUM!!
                    val data = TreeData2(PK, HER_NUM, HER_SPEC, HER_FAMI, HER_SCIEN, HER_DOMIN.toString(), HER_GUNDO.toString(), HER_HEIGHT.toString())
                    HerDatas.add(data)
                    hernumTV.setText(manyFloraAttribute.HER_NUM.toString())
                    etHER_SPECET.setText(manyFloraAttribute.HER_SPEC)
                    etHER_FAMIET.setText(manyFloraAttribute.HER_FAMI)
                    etHER_SCIENET.setText(manyFloraAttribute.HER_SCIEN)
                    etHER_HET.setText(manyFloraAttribute.HER_DOMIN.toString())
                    etHER_COVEET.setText(manyFloraAttribute.HER_GUNDO.toString())
                    etHER_HEIGHT.setText(manyFloraAttribute.HER_HEIGHT.toString())
                }

                gpslatTV.setText(manyFloraAttribute.GPS_LAT.toString())
                gpslonTV.setText(manyFloraAttribute.GPS_LON.toString())

                AllDatas.add(manyFloraAttribute)

            }

            var treDataSize = TreDatas.size + 1
            var streDataSize = StreDatas.size + 1
            var shrDataSize = ShrDatas.size + 1
            var herDataSize = HerDatas.size + 1

            if (TreDatas.size > 0) {
                treDataSize = TreDatas.size
                trepage = TreDatas.size
            }

            if (StreDatas.size > 0) {
                streDataSize = StreDatas.size
                strepage = StreDatas.size
            }

            if (ShrDatas.size > 0) {
                shrDataSize = ShrDatas.size
                shrpage = ShrDatas.size
            }

            if (HerDatas.size > 0) {
                herDataSize = HerDatas.size
                herpage = HerDatas.size
            }

            trepageTV.setText(trepage.toString())
            trerightpageTV.setText(treDataSize.toString())
            strepageTV.setText(strepage.toString())
            strerightpageTV.setText(streDataSize.toString())
            shrpageTV.setText(shrpage.toString())
            shrrightpageTV.setText(shrDataSize.toString())
            herpageTV.setText(herpage.toString())
            herrightpageTV.setText(herDataSize.toString())

            for (i in 0 until TreDatas.size) {
                println("TER_NUM ${TreDatas.get(i).PAGE} TER_SPEC ${TreDatas.get(i).FAMI}")
            }

            for (i in 0..StreDatas.size - 1) {
                println("STER_NUM ${StreDatas.get(i).PAGE} STER_SPEC ${StreDatas.get(i).FAMI}")
            }

            for (i in 0..ShrDatas.size - 1) {
                println("SHR_NUM ${ShrDatas.get(i).PAGE} SHR_SPEC ${ShrDatas.get(i).FAMI}")
            }

            for (i in 0..HerDatas.size - 1) {
                println("HER_NUM ${HerDatas.get(i).PAGE} HER_SPEC ${HerDatas.get(i).FAMI}")
            }
            val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora2/images" + File.separator + keyId + File.separator)
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
                        if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "flora2/images" + File.separator + keyId + File.separator, add_images[add_images.size - 1]))) {
                            //                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/flora2/images/" + flora2_attribute.NUM.toString() +"_"+flora2_attribute.INV_TM +"_" + (j+1) + ".png")) {
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


            deleteBT.visibility = View.VISIBLE

            data.close()
        }

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            invpersonTV.setText(base.INV_PERSON)
            invdtTV.setText(base.INV_DT)

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    invregionTV.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, invpersonTV.text.toString(), invdtTV.text.toString(), "0")

            dbManager!!.insertbase(base)

        }

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        dominTV.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "우점")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA1);
        }

        ausTV.setOnClickListener {
            if (dominTV.text.equals("")){
                Toast.makeText(context, "우점을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "우점")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA6);
        }

        etTRE_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "교목층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA2);
        }

        etSTRE_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "아교목층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA3);
        }

        etSHR_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "관목층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA4);
        }

        etHER_SPECET.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "초본층")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA5);
        }

        trecloseLL.setOnClickListener {

            etTRE_SPECET.visibility = View.VISIBLE
            etTRE_SPECLL.visibility = View.GONE
            etTRE_SPECtmp.setText("")

            if (trepage == 1) {
                for (i in 0 until TreDatas.size) {
                    if (TreDatas.get(i).PAGE == trepage) {
                        val data = TreDatas.get(i)
                        TreDatas.removeAt(i)
                        break
                    }
                }

                var division = false

                for (i in 0 until TreDatas.size) {
                    if (TreDatas.get(i).PAGE!! > 1) {
                        TreDatas.get(i).PAGE = TreDatas.get(i).PAGE!! - 1
                    }
                }

                for (i in 0 until TreDatas.size) {
                    if (TreDatas.get(i).PAGE == trepage) {
                        trenumTV.setText(trepage.toString())
                        etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                        etTRE_FAMIET.setText(TreDatas.get(i).FAMI)
                        etTRE_SCIENET.setText(TreDatas.get(i).SCIEN)
                        etTRE_HET.setText(TreDatas.get(i).DBH.toString())
                        etTRE_BREAET.setText(TreDatas.get(i).TOIL.toString())
                        etTRE_COVEET.setText(TreDatas.get(i).UNDER.toString())
                        etTRE_WATERWIDTH.setText(TreDatas.get(i).WATERWIDTH.toString())
                        etTRE_TYPE.setText(TreDatas.get(i).TYPE.toString())
                        division = true
                    }
                }
                if (division == false) {
                    etTRE_SPECET.setText("")
                    etTRE_SPECtmp.setText("")
                    etTRE_FAMIET.setText("")
                    etTRE_SCIENET.setText("")
                    etTRE_HET.setText("")
                    etTRE_BREAET.setText("")
                    etTRE_COVEET.setText("")
                    etTRE_WATERWIDTH.setText("")
                    etTRE_TYPE.setText("")
                }

                val page = trepage
                var size = trerightpageTV.text.toString().toInt()

                trepageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    trerightpageTV.setText(size.toString())
                }
            }

            if (trepage > 1) {
                if (trepage == 2) {
                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == trepage) {
                            val data = TreDatas.get(i)
                            TreDatas.removeAt(i)
                            break
                        }
                    }

                    trepage = trepage - 1

                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == trepage) {
                            trenumTV.setText(trepage.toString())
                            etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                            etTRE_FAMIET.setText(TreDatas.get(i).FAMI)
                            etTRE_SCIENET.setText(TreDatas.get(i).SCIEN)
                            etTRE_HET.setText(TreDatas.get(i).DBH.toString())
                            etTRE_BREAET.setText(TreDatas.get(i).TOIL.toString())
                            etTRE_COVEET.setText(TreDatas.get(i).UNDER.toString())
                            etTRE_WATERWIDTH.setText(TreDatas.get(i).WATERWIDTH.toString())
                            etTRE_TYPE.setText(TreDatas.get(i).TYPE.toString())
                        }

                        if (TreDatas.get(i).PAGE!! > 1) {
                            TreDatas.get(i).PAGE = TreDatas.get(i).PAGE!! - 1
                        }
                    }

                } else if (trepage > 2) {
                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == trepage) {
                            val data = TreDatas.get(i)
                            TreDatas.removeAt(i)
                            break
                        }
                    }

                    trepage = trepage - 1

                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == trepage) {
                            trenumTV.setText(trepage.toString())
                            etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                            etTRE_FAMIET.setText(TreDatas.get(i).FAMI)
                            etTRE_SCIENET.setText(TreDatas.get(i).SCIEN)
                            etTRE_HET.setText(TreDatas.get(i).DBH.toString())
                            etTRE_BREAET.setText(TreDatas.get(i).TOIL.toString())
                            etTRE_COVEET.setText(TreDatas.get(i).UNDER.toString())
                            etTRE_WATERWIDTH.setText(TreDatas.get(i).WATERWIDTH.toString())
                            etTRE_TYPE.setText(TreDatas.get(i).TYPE.toString())
                        }

                        if (TreDatas.get(i).PAGE!! > trepage) {
                            TreDatas.get(i).PAGE = TreDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = trepage
                val size = trerightpageTV.text.toString().toInt() - 1

                trepageTV.setText(page.toString())
                trerightpageTV.setText(size.toString())
            }

            println("delete-------------------------${TreDatas.size}")

        }

        strecloseLL.setOnClickListener {
            etSTRE_SPECET.visibility = View.VISIBLE
            etSTRE_SPECLL.visibility = View.GONE
            etSTRE_SPECtmp.setText("")

            if (strepage == 1) {
                for (i in 0 until StreDatas.size) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        StreDatas.removeAt(i)
                        break
                    }
                }

                var division = false

                for (i in 0 until StreDatas.size) {
                    if (StreDatas.get(i).PAGE!! > 1) {
                        StreDatas.get(i).PAGE = StreDatas.get(i).PAGE!! - 1
                    }
                }

                for (i in 0 until StreDatas.size) {
                    if (StreDatas.get(i).PAGE == strepage) {
                        strenumTV.setText(strepage.toString())
                        etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                        etSTRE_FAMIET.setText(StreDatas.get(i).FAMI)
                        etSTRE_SCIENET.setText(StreDatas.get(i).SCIEN)
                        etSTRE_HET.setText(StreDatas.get(i).DBH.toString())
                        etSTRE_BREAET.setText(StreDatas.get(i).TOIL.toString())
                        etSTRE_COVEET.setText(StreDatas.get(i).UNDER.toString())
                        etSTRE_WATERWIDTH.setText(StreDatas.get(i).WATERWIDTH.toString())
                        etSTRE_TYPE.setText(StreDatas.get(i).TYPE.toString())
                        division = true
                    }
                }

                if (division == false) {

                    etSTRE_SPECET.setText("")
                    etSTRE_SPECtmp.setText("")
                    etSTRE_FAMIET.setText("")
                    etSTRE_SCIENET.setText("")
                    etSTRE_HET.setText("")
                    etSTRE_BREAET.setText("")
                    etSTRE_COVEET.setText("")
                    etSTRE_WATERWIDTH.setText("")
                    etSTRE_TYPE.setText("")
                }

                val page = strepage
                var size = strerightpageTV.text.toString().toInt()

                strepageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    strerightpageTV.setText(size.toString())
                }


            }

            if (strepage > 1) {

                if (strepage == 2) {
                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == strepage) {
                            StreDatas.removeAt(i)
                            break
                        }
                    }

                    strepage = strepage - 1

                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == strepage) {
                            strenumTV.setText(strepage.toString())
                            etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                            etSTRE_FAMIET.setText(StreDatas.get(i).FAMI)
                            etSTRE_SCIENET.setText(StreDatas.get(i).SCIEN)
                            etSTRE_HET.setText(StreDatas.get(i).DBH.toString())
                            etSTRE_BREAET.setText(StreDatas.get(i).TOIL.toString())
                            etSTRE_COVEET.setText(StreDatas.get(i).UNDER.toString())
                            etSTRE_WATERWIDTH.setText(StreDatas.get(i).WATERWIDTH.toString())
                            etSTRE_TYPE.setText(StreDatas.get(i).TYPE.toString())
                        }

                        if (StreDatas.get(i).PAGE!! > strepage) {
                            StreDatas.get(i).PAGE = StreDatas.get(i).PAGE!! - 1
                        }
                    }
                } else if (strepage > 2) {
                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == strepage) {
                            StreDatas.removeAt(i)
                            break
                        }
                    }

                    strepage = strepage - 1

                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == strepage) {
                            strenumTV.setText(strepage.toString())
                            etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                            etSTRE_FAMIET.setText(StreDatas.get(i).FAMI)
                            etSTRE_SCIENET.setText(StreDatas.get(i).SCIEN)
                            etSTRE_HET.setText(StreDatas.get(i).DBH.toString())
                            etSTRE_BREAET.setText(StreDatas.get(i).TOIL.toString())
                            etSTRE_COVEET.setText(StreDatas.get(i).UNDER.toString())
                            etSTRE_WATERWIDTH.setText(StreDatas.get(i).WATERWIDTH.toString())
                            etSTRE_TYPE.setText(StreDatas.get(i).TYPE.toString())
                        }

                        if (StreDatas.get(i).PAGE!! > 2) {
                            StreDatas.get(i).PAGE = StreDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = strepage
                val size = strerightpageTV.text.toString().toInt() - 1

                strepageTV.setText(page.toString())
                strerightpageTV.setText(size.toString())
            }

        }

        shrcloseLL.setOnClickListener {

            etSHR_SPECET.visibility = View.VISIBLE
            etSHR_SPECLL.visibility = View.GONE
            etSHR_SPECtmp.setText("")

            if (shrpage == 1) {
                for (i in 0 until ShrDatas.size) {
                    if (ShrDatas.get(i).PAGE == shrpage) {
                        ShrDatas.removeAt(i)
                        break
                    }
                }

                var division = false

                for (i in 0 until ShrDatas.size) {
                    if (ShrDatas.get(i).PAGE!! > 1) {
                        ShrDatas.get(i).PAGE = ShrDatas.get(i).PAGE!! - 1
                    }
                }

                for (i in 0 until ShrDatas.size) {
                    if (ShrDatas.get(i).PAGE == shrpage) {
                        shrnumTV.setText(shrpage.toString())
                        etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                        etSHR_FAMIET.setText(ShrDatas.get(i).FAMI)
                        etSHR_SCIENET.setText(ShrDatas.get(i).SCIEN)
                        etSHR_HET.setText(ShrDatas.get(i).H.toString())
                        etSTR_COVEET.setText(ShrDatas.get(i).COVE.toString())
                        etSHR_UNDER.setText(ShrDatas.get(i).ETC.toString())
                        division = true
                    }
                }

                if (division == false) {
                    etSHR_SPECET.setText("")
                    etSHR_SPECtmp.setText("")
                    etSHR_FAMIET.setText("")
                    etSHR_SCIENET.setText("")
                    etSHR_HET.setText("")
                    etSTR_COVEET.setText("")
                    etSHR_UNDER.setText("")
                }

                val page = shrpage
                var size = shrrightpageTV.text.toString().toInt()

                shrpageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    shrrightpageTV.setText(size.toString())
                }

            }

            if (shrpage > 1) {
                if (shrpage == 2) {
                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            ShrDatas.removeAt(i)
                            break
                        }
                    }

                    shrpage = shrpage - 1

                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            shrnumTV.setText(shrpage.toString())
                            etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                            etSHR_FAMIET.setText(ShrDatas.get(i).FAMI)
                            etSHR_SCIENET.setText(ShrDatas.get(i).SCIEN)
                            etSHR_HET.setText(ShrDatas.get(i).H.toString())
                            etSTR_COVEET.setText(ShrDatas.get(i).COVE.toString())
                            etSHR_UNDER.setText(ShrDatas.get(i).ETC.toString())
                        }

                        if (ShrDatas.get(i).PAGE!! > 1) {
                            ShrDatas.get(i).PAGE = ShrDatas.get(i).PAGE!! - 1
                        }
                    }
                } else if (shrpage > 2) {
                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            ShrDatas.removeAt(i)
                            break
                        }
                    }

                    shrpage = shrpage - 1

                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            shrnumTV.setText(shrpage.toString())
                            etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                            etSHR_FAMIET.setText(ShrDatas.get(i).FAMI)
                            etSHR_SCIENET.setText(ShrDatas.get(i).SCIEN)
                            etSHR_HET.setText(ShrDatas.get(i).H.toString())
                            etSTR_COVEET.setText(ShrDatas.get(i).COVE.toString())
                            etSHR_UNDER.setText(ShrDatas.get(i).ETC.toString())
                        }

                        if (ShrDatas.get(i).PAGE!! > shrpage) {
                            ShrDatas.get(i).PAGE = ShrDatas.get(i).PAGE!! - 1
                        }
                    }
                }


                val page = shrpage
                val size = shrrightpageTV.text.toString().toInt() - 1

                shrpageTV.setText(page.toString())
                shrrightpageTV.setText(size.toString())
            }

        }

        hercloseLL.setOnClickListener {

            etHER_SPECET.visibility = View.VISIBLE
            etHER_SPECLL.visibility = View.GONE
            etHER_SPECtmp.setText("")

            if (herpage == 1) {
                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE == herpage) {
                        HerDatas.removeAt(i)
                        break
                    }
                }

                var division = false

                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE!! > 1) {
                        HerDatas.get(i).PAGE = HerDatas.get(i).PAGE!! - 1
                    }
                }

                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE == herpage) {
                        hernumTV.setText(herpage.toString())
                        etHER_SPECET.setText(HerDatas.get(i).SPEC)
                        etHER_FAMIET.setText(HerDatas.get(i).FAMI)
                        etHER_SCIENET.setText(HerDatas.get(i).SCIEN)
                        etHER_HET.setText(HerDatas.get(i).H.toString())
                        etHER_COVEET.setText(HerDatas.get(i).COVE.toString())
                        etHER_HEIGHT.setText(HerDatas.get(i).ETC.toString())
                        division = true
                    }
                }

                if (division == false) {
                    etHER_SPECET.setText("")
                    etHER_SPECtmp.setText("")
                    etHER_FAMIET.setText("")
                    etHER_SCIENET.setText("")
                    etHER_HET.setText("")
                    etHER_COVEET.setText("")
                    etHER_HEIGHT.setText("")
                }

                val page = herpage
                var size = herrightpageTV.text.toString().toInt()

                herpageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    herrightpageTV.setText(size.toString())
                }
            }

            if (herpage > 1) {
                if (herpage == 2) {
                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            HerDatas.removeAt(i)
                            break
                        }
                    }

                    herpage = herpage - 1

                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            hernumTV.setText(herpage.toString())
                            etHER_SPECET.setText(HerDatas.get(i).SPEC)
                            etHER_FAMIET.setText(HerDatas.get(i).FAMI)
                            etHER_SCIENET.setText(HerDatas.get(i).SCIEN)
                            etHER_HET.setText(HerDatas.get(i).H.toString())
                            etHER_COVEET.setText(HerDatas.get(i).COVE.toString())
                            etHER_HEIGHT.setText(HerDatas.get(i).ETC.toString())
                        }

                        if (HerDatas.get(i).PAGE!! > 1) {
                            HerDatas.get(i).PAGE = HerDatas.get(i).PAGE!! - 1
                        }
                    }
                } else if (herpage > 2) {
                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            HerDatas.removeAt(i)
                            break
                        }
                    }

                    herpage = herpage - 1

                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            hernumTV.setText(herpage.toString())
                            etHER_SPECET.setText(HerDatas.get(i).SPEC)
                            etHER_FAMIET.setText(HerDatas.get(i).FAMI)
                            etHER_SCIENET.setText(HerDatas.get(i).SCIEN)
                            etHER_HET.setText(HerDatas.get(i).H.toString())
                            etHER_COVEET.setText(HerDatas.get(i).COVE.toString())
                            etHER_HEIGHT.setText(HerDatas.get(i).ETC.toString())
                        }

                        if (HerDatas.get(i).PAGE!! > herpage) {
                            HerDatas.get(i).PAGE = HerDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = herpage

                val size = herrightpageTV.text.toString().toInt() - 1

                herpageTV.setText(page.toString())
                herrightpageTV.setText(size.toString())
            }

        }


        treleftTV.setOnClickListener {

            var division = false
            for (i in 0 until TreDatas.size) {
                if (trepage == TreDatas.get(i).PAGE) {
                    division = true
                }
            }

            if (division == false) {
                var spec = etTRE_SPECET.text.toString()
                if (etTRE_SPECtmp.length() > 0) {
                    spec = etTRE_SPECtmp.text.toString()
                }
                val fami = etTRE_FAMIET.text.toString()
                val scien = etTRE_SCIENET.text.toString()

                var dbh: Float = 0.0F

                if (etTRE_HET.text.isNotEmpty()) {
                    dbh = etTRE_HET.text.toString().toFloat()
                }

                var toil = 0.0F

                if (etTRE_BREAET.text.isNotEmpty()) {
                    toil = etTRE_BREAET.text.toString().toFloat()
                }
//                var cove = 0.0F
//
//                if (etTRE_COVEET.text.isNotEmpty()) {
//                    cove = etTRE_COVEET.text.toString().toFloat()
//                }

                var under = etTRE_COVEET.text.toString()
                var waterwidth = 0
                if (etTRE_BREAET.text.isNotEmpty()) {
                    waterwidth = etTRE_WATERWIDTH.text.toString().toInt()
                }
                var type = Utils.getString(etTRE_TYPE)

                var tredata = TreeData1(-1, trepage, spec, fami, scien, dbh, toil, under.toString(), waterwidth, type)

                TreDatas.add(tredata)
            }

            if (trepage > 1) {

                etTRE_SPECET.visibility = View.VISIBLE
                etTRE_SPECLL.visibility = View.GONE
                etTRE_SPECtmp.setText("")

                for (i in 0 until TreDatas.size) {
                    if (trepage == TreDatas.get(i).PAGE) {
                        TreDatas.get(i).SPEC = etTRE_SPECET.text.toString()
                        if (etTRE_SPECtmp.length() > 0) {
                            TreDatas.get(i).SPEC = etTRE_SPECtmp.text.toString()
                        }
                        TreDatas.get(i).FAMI = etTRE_FAMIET.text.toString()
                        TreDatas.get(i).SCIEN = etTRE_SCIENET.text.toString()

                        if (etTRE_HET.text.isNotEmpty()) {
                            TreDatas.get(i).DBH = etTRE_HET.text.toString().toFloat()
                        }

                        if (etTRE_BREAET.text.isNotEmpty()) {
                            TreDatas.get(i).TOIL = etTRE_BREAET.text.toString().toFloat()
                        }

                        if (etTRE_COVEET.text.isNotEmpty()) {
                            TreDatas.get(i).UNDER = etTRE_COVEET.text.toString()
                        }

                        if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                            TreDatas.get(i).WATERWIDTH = etTRE_WATERWIDTH.text.toString().toInt()
                        }
                        TreDatas.get(i).TYPE = etTRE_TYPE.toString()

                    }
                }

                trepage = trepage - 1
                for (i in 0..TreDatas.size - 1) {
                    if (trepage == TreDatas.get(i).PAGE) {
                        val data = TreDatas.get(i)

                        trenumTV.setText(trepage.toString())
                        etTRE_SPECET.setText(data.SPEC)
                        etTRE_FAMIET.setText(data.FAMI)
                        etTRE_SCIENET.setText(data.SCIEN)
                        etTRE_HET.setText(data.DBH.toString())
                        etTRE_BREAET.setText(data.TOIL.toString())
                        etTRE_COVEET.setText(data.UNDER.toString())
                        etTRE_WATERWIDTH.setText(data.WATERWIDTH.toString())
                        etTRE_TYPE.setText(data.TYPE.toString())

                        val size = trerightpageTV.text.toString().toInt()

                        trepageTV.setText(trepage.toString())
                        trerightpageTV.setText(size.toString())
                    }
                }
            }

            deleteBT.visibility = View.GONE
        }

        trerightTV.setOnClickListener {
            AddTreFlora()
//            TreClear()
        }

        streleftTV.setOnClickListener {
            println("leftPage $strepage")
            var division = false
            for (i in 0 until StreDatas.size) {
                if (strepage == StreDatas.get(i).PAGE) {
                    division = true
                }
            }

            if (division == false) {
                var spec = etSTRE_SPECET.text.toString()
                if (etSTRE_SPECtmp.length() > 0) {
                    spec = etSTRE_SPECtmp.text.toString()
                }
                val fami = etSTRE_FAMIET.text.toString()
                val scien = etSTRE_SCIENET.text.toString()

                var dbh: Float = 0.0F

                if (etSTRE_HET.text.isNotEmpty()) {
                    dbh = etSTRE_HET.text.toString().toFloat()
                }

                var toil = 0.0F

                if (etSTRE_BREAET.text.isNotEmpty()) {
                    toil = etSTRE_BREAET.text.toString().toFloat()
                }
//                var under = 0.0F
//
//                if (etSTRE_COVEET.text.isNotEmpty()) {
//                    under = etSTRE_COVEET.text.toString().toFloat()
//                }

                var under = etSTRE_COVEET.text.toString()

                var waterwidth = 0
                if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                    waterwidth = etSTRE_WATERWIDTH.text.toString().toInt()
                }

                var type =  Utils.getString(etSTRE_TYPE)

                var stredata = TreeData1(-1, strepage, spec, fami, scien, dbh, toil, under, waterwidth, type)

                StreDatas.add(stredata)
            }

            if (strepage > 1) {

                etSTRE_SPECET.visibility = View.VISIBLE
                etSTRE_SPECLL.visibility = View.GONE
                etSTRE_SPECtmp.setText("")

                for (i in 0 until StreDatas.size) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        StreDatas.get(i).SPEC = etSTRE_SPECET.text.toString()
                        if (etSTRE_SPECtmp.length() > 0) {
                            StreDatas.get(i).SPEC = etSTRE_SPECtmp.text.toString()
                        }
                        StreDatas.get(i).FAMI = etSTRE_FAMIET.text.toString()
                        StreDatas.get(i).SCIEN = etSTRE_SCIENET.text.toString()

                        if (etSTRE_HET.text.isNotEmpty()) {
                            StreDatas.get(i).DBH = etSTRE_HET.text.toString().toFloat()
                        }

                        if (etSTRE_BREAET.text.isNotEmpty()) {
                            StreDatas.get(i).TOIL = etSTRE_BREAET.text.toString().toFloat()
                        }

                        if (etSTRE_COVEET.text.isNotEmpty()) {
                            StreDatas.get(i).UNDER = etSTRE_COVEET.text.toString()
                        }

                        if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                            StreDatas.get(i).WATERWIDTH = etSTRE_WATERWIDTH.text.toString().toInt()
                        }
                        StreDatas.get(i).TYPE = etSTRE_TYPE.toString()
                    }
                }

                strepage = strepage - 1
                for (i in 0..StreDatas.size - 1) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        val data = StreDatas.get(i)

                        strenumTV.setText(strepage.toString())
                        etSTRE_SPECET.setText(data.SPEC)
                        etSTRE_FAMIET.setText(data.FAMI)
                        etSTRE_SCIENET.setText(data.SCIEN)
                        etSTRE_HET.setText(data.DBH.toString())
                        etSTRE_BREAET.setText(data.TOIL.toString())
                        etSTRE_COVEET.setText(data.UNDER.toString())
                        etSTRE_WATERWIDTH.setText(data.WATERWIDTH.toString())
                        etSTRE_TYPE.setText(data.TYPE.toString())

                        val size = strerightpageTV.text.toString().toInt()

                        strepageTV.setText(strepage.toString())
                        strerightpageTV.setText(size.toString())
                    }
                }
            }

            deleteBT.visibility = View.GONE
        }

        strerightTV.setOnClickListener {
            AddStreFlora()
//            Stre_Clear()
        }

        shrleftTV.setOnClickListener {
            println("leftPage $shrpage")
            var division = false
            for (i in 0 until ShrDatas.size) {
                if (shrpage == ShrDatas.get(i).PAGE) {
                    division = true
                }
            }

            if (division == false) {
                var spec = etSHR_SPECET.text.toString()
                if (etSHR_SPECtmp.length() > 0) {
                    spec = etSHR_SPECtmp.text.toString()
                }
                val fami = etSHR_FAMIET.text.toString()
                val scien = etSHR_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etSHR_HET.text.isNotEmpty()) {
                    h = etSHR_HET.text.toString().toFloat()
                }

                var cove = 0.0F

                if (etSTR_COVEET.text.isNotEmpty()) {
                    cove = etSTR_COVEET.text.toString().toFloat()
                }

                var etc = etSHR_UNDER.text.toString()

                var shrdata = TreeData2(-1, shrpage, spec, fami, scien, h.toString(), cove.toString(), etc)

                ShrDatas.add(shrdata)
            }

            if (shrpage > 1) {

                etSHR_SPECET.visibility = View.VISIBLE
                etSHR_SPECLL.visibility = View.GONE
                etSHR_SPECtmp.setText("")

                for (i in 0 until ShrDatas.size) {
                    if (shrpage == ShrDatas.get(i).PAGE) {
                        ShrDatas.get(i).SPEC = etSHR_SPECET.text.toString()
                        ShrDatas.get(i).FAMI = etSHR_FAMIET.text.toString()
                        ShrDatas.get(i).SCIEN = etSHR_SCIENET.text.toString()
                        ShrDatas.get(i).H = etSHR_HET.text.toString()
                        ShrDatas.get(i).COVE = etSTR_COVEET.text.toString()
                        ShrDatas.get(i).ETC = etSHR_UNDER.text.toString()

//                        if (etSHR_HET.text.isNotEmpty()) {
//                            ShrDatas.get(i).H = etSHR_HET.text.toString().toFloat()
//                        }
//
//                        if (etSTR_COVEET.text.isNotEmpty()) {
//                            ShrDatas.get(i).COVE = etSTR_COVEET.text.toString().toFloat()
//                        }
                    }
                }

                shrpage = shrpage - 1
                for (i in 0..ShrDatas.size - 1) {
                    if (shrpage == ShrDatas.get(i).PAGE) {
                        val data = ShrDatas.get(i)

                        shrnumTV.setText(shrpage.toString())
                        etSHR_SPECET.setText(data.SPEC)
                        etSHR_FAMIET.setText(data.FAMI)
                        etSHR_SCIENET.setText(data.SCIEN)
                        etSHR_HET.setText(data.H.toString())
                        etSTR_COVEET.setText(data.COVE.toString())
                        etSHR_UNDER.setText(data.ETC.toString())

                        val size = shrrightpageTV.text.toString().toInt()

                        shrpageTV.setText(strepage.toString())
                        shrrightpageTV.setText(size.toString())
                    }
                }
            }

            deleteBT.visibility = View.GONE
        }

        shrrightTV.setOnClickListener {
            AddShrFlora()
//            Shr_Clear()
        }

        herleftTV.setOnClickListener {
            println("leftPage $herpage")
            var division = false
            for (i in 0 until HerDatas.size) {
                if (herpage == HerDatas.get(i).PAGE) {
                    division = true
                }
            }

            if (division == false) {
                var spec = etHER_SPECET.text.toString()
                if (etHER_SPECtmp.length() > 0) {
                    spec = etHER_SPECtmp.text.toString()
                }
                val fami = etHER_FAMIET.text.toString()
                val scien = etHER_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etHER_HET.text.isNotEmpty()) {
                    h = etHER_HET.text.toString().toFloat()
                }

                var cove = 0.0F

                if (etHER_COVEET.text.isNotEmpty()) {
                    cove = etHER_COVEET.text.toString().toFloat()
                }

                var etc = 0.0f
                if (etHER_HEIGHT.text.isNotEmpty()) {
                    etc = etHER_HEIGHT.text.toString().toFloat()
                }

                var herdata = TreeData2(-1, herpage, spec, fami, scien, h.toString(), cove.toString(), etc.toString())

                HerDatas.add(herdata)
            }

            if (herpage > 1) {

                etHER_SPECET.visibility = View.VISIBLE
                etHER_SPECLL.visibility = View.GONE
                etHER_SPECtmp.setText("")

                for (i in 0 until HerDatas.size) {
                    if (herpage == HerDatas.get(i).PAGE) {
                        HerDatas.get(i).SPEC = etHER_SPECET.text.toString()
                        HerDatas.get(i).FAMI = etHER_FAMIET.text.toString()
                        HerDatas.get(i).SCIEN = etHER_SCIENET.text.toString()
                        HerDatas.get(i).H = etHER_HET.text.toString()
                        HerDatas.get(i).COVE = etHER_COVEET.text.toString()
                        HerDatas.get(i).ETC = etHER_HEIGHT.text.toString()
//                        if (etHER_HET.text.isNotEmpty()) {
//                            HerDatas.get(i).H = etHER_HET.text.toString().toFloat()
//                        }
//
//                        if (etHER_COVEET.text.isNotEmpty()) {
//                            HerDatas.get(i).COVE = etHER_COVEET.text.toString().toFloat()
//                        }
                    }
                }

                herpage = herpage - 1
                for (i in 0..HerDatas.size - 1) {
                    if (herpage == HerDatas.get(i).PAGE) {
                        val data = HerDatas.get(i)

                        hernumTV.setText(herpage.toString())
                        etHER_SPECET.setText(data.SPEC)
                        etHER_FAMIET.setText(data.FAMI)
                        etHER_SCIENET.setText(data.SCIEN)
                        etHER_HET.setText(data.H.toString())
                        etHER_COVEET.setText(data.COVE.toString())
                        etHER_HEIGHT.setText(data.ETC.toString())

                        val size = herrightpageTV.text.toString().toInt()

                        herpageTV.setText(herpage.toString())
                        herrightpageTV.setText(size.toString())
                    }
                }
            }

            deleteBT.visibility = View.GONE
        }

        herrightTV.setOnClickListener {
            AddHerFlora()
//            Her_Clear()
        }

        saveBT.setOnClickListener {
            if (TreDatas.size == 0 && StreDatas.size == 0 && ShrDatas.size == 0 && HerDatas.size == 0) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            println("delete-------------------- $keyId")
                            dbManager!!.deleteAllManyFloraAttribute(keyId)

                            dialog.cancel()

                            var manyFloraAttribute =  null_many_attribute()

                            keyId = intent.getStringExtra("GROP_ID")

                            println("insertkeyid $keyId")

                            manyFloraAttribute.GROP_ID = keyId
                            manyFloraAttribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

                            manyFloraAttribute.CURRENT_TM = Utils.current_tm()
//                            manyFloraAttribute.INV_REGION = invregionTV.text.toString()


                            var names = t_name.split("-")
                            if (names.size > 1&&!t_name.contains("군락")) {
                                manyFloraAttribute.DOMIN = t_name+ "군락"
                            } else if (!t_name.contains("군락")) {
                                if (t_name.length>0){
                                    manyFloraAttribute.DOMIN = t_name + "군락"
                                }
                            } else {
                                manyFloraAttribute.DOMIN = t_name
                            }

                            if (invregionTV.length() > 0) {
                                manyFloraAttribute.INV_REGION = invregionTV.text.toString();
                            } else {
                                manyFloraAttribute.INV_REGION = INV_REGION
                            }

                            manyFloraAttribute.INV_DT = invdtTV.text.toString()

                            if (invpersonTV.text == null || invpersonTV.text.equals("")) {
                                manyFloraAttribute.INV_PERSON = userName
                            } else {
                                manyFloraAttribute.INV_PERSON = invpersonTV.text.toString()
                            }

                            manyFloraAttribute.INV_TM = Utils.timeStr()


                            if (trenumTV.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_NUM = trenumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.TRE_SPEC = etTRE_SPECET.text.toString()
                            manyFloraAttribute.TRE_FAMI = etTRE_FAMIET.text.toString()
                            manyFloraAttribute.TRE_SCIEN = etTRE_SCIENET.text.toString()

                            if (etTRE_HET.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_DBH = etTRE_HET.text.toString().toFloat()
                            }

                            if (etTRE_BREAET.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_TOIL = etTRE_BREAET.text.toString().toFloat()
                            }

                            manyFloraAttribute.TRE_UNDER = etTRE_COVEET.text.toString()
                            if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_WATER = etTRE_WATERWIDTH.text.toString().toInt()
                            }
                            manyFloraAttribute.TRE_TYPE = etTRE_TYPE.text.toString()
//                            if (etTRE_COVEET.text.isNotEmpty()) {
//                                manyFloraAttribute.TRE_UNDER = etTRE_COVEET.text.toString().toFloat()
//                            }

                            val TRE_SPEC = etTRE_SPECET.text.toString()
                            if (TRE_SPEC != "" && TRE_SPEC != null) {
                                manyFloraAttribute.TRE_NUM = 1
                            }

                            if (strenumTV.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_NUM = strenumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.STRE_SPEC = etSTRE_SPECET.text.toString()
                            manyFloraAttribute.STRE_FAMI = etSTRE_FAMIET.text.toString()
                            manyFloraAttribute.STRE_SCIEN = etSTRE_SCIENET.text.toString()

                            if (etSTRE_HET.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_DBH = etSTRE_HET.text.toString().toFloat()
                            }

                            if (etSTRE_BREAET.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_TOIL = etSTRE_BREAET.text.toString().toFloat()
                            }

                            manyFloraAttribute.STRE_UNDER = etSTRE_COVEET.text.toString()
//                            if (etSTRE_COVEET.text.isNotEmpty()) {
//                                manyFloraAttribute.STRE_UNDER = etSTRE_COVEET.text.toString().toFloat()
//                            }

                            if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_WATER = etSTRE_WATERWIDTH.text.toString().toInt()
                            }
                            manyFloraAttribute.STRE_TYPE = etSTRE_TYPE.text.toString()

                            val STRE_SPEC = etSTRE_SPECET.text.toString()
                            if (STRE_SPEC != "" && STRE_SPEC != null) {
                                manyFloraAttribute.STRE_NUM = 1
                            }

                            if (shrnumTV.text.isNotEmpty()) {
                                manyFloraAttribute.SHR_NUM = shrnumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.SHR_SPEC = etSHR_SPECET.text.toString()
                            if (etSHR_SPECtmp.length() > 0) {
                                manyFloraAttribute.SHR_SPEC = etSHR_SPECtmp.text.toString()
                            }
                            manyFloraAttribute.SHR_FAMI = etSHR_FAMIET.text.toString()
                            manyFloraAttribute.SHR_SCIEN = etSHR_SCIENET.text.toString()

                            if (etSTRE_HET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.SHR_TOIL = etSTRE_HET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.SHR_TOIL = 0.0f
                            }

                            if (etSTR_COVEET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.SHR_WATER = etSTR_COVEET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.SHR_WATER =  0.0f
                            }

                            manyFloraAttribute.SHR_UNDER = etSHR_UNDER.text.toString()

                            val SHR_SPEC = etSHR_SPECET.text.toString()
                            if (SHR_SPEC != "" && SHR_SPEC != null) {
                                manyFloraAttribute.SHR_NUM = 1
                            }

                            if (etSHR_SPECtmp.length() > 0) {
                                manyFloraAttribute.SHR_NUM = etSHR_SPECtmp.text.toString().toInt()
                            }

                            if (hernumTV.text.isNotEmpty()) {
                                manyFloraAttribute.HER_NUM = hernumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.HER_SPEC = etHER_SPECET.text.toString()
                            if (etHER_SPECtmp.length() > 0) {
                                manyFloraAttribute.HER_SPEC = etHER_SPECtmp.text.toString()
                            }
                            manyFloraAttribute.HER_FAMI = etHER_FAMIET.text.toString()
                            manyFloraAttribute.HER_SCIEN = etHER_SCIENET.text.toString()

                            if (etHER_HET.text.isNotEmpty()) {
                                manyFloraAttribute.HER_DOMIN = etHER_HET.text.toString().toFloat()
                            }

                            if (etHER_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.HER_GUNDO = etHER_COVEET.text.toString().toFloat()
                            }

                            if (etHER_HEIGHT.text.isNotEmpty()) {
                                manyFloraAttribute.HER_HEIGHT = etHER_HEIGHT.text.toString().toFloat()
                            }

                            val HER_SPEC = etHER_COVEET.text.toString()
                            if (HER_SPEC != "" && HER_SPEC != null) {
                                manyFloraAttribute.HER_NUM = 1
                            }

                            if (gpslatTV.text.isNotEmpty()) {
                                manyFloraAttribute.GPS_LAT = gpslatTV.text.toString().toFloat()
                            }

                            if (gpslonTV.text.isNotEmpty()) {
                                manyFloraAttribute.GPS_LON = gpslonTV.text.toString().toFloat()
                            }

                            manyFloraAttribute.TEMP_YN = "Y"
                            manyFloraAttribute.CONF_MOD = "N"

                            manyFloraAttribute.GEOM = log.toString() + " " + lat.toString()


                            val CONF_MOD = manyFloraAttribute.CONF_MOD

                            if (CONF_MOD == "C" || CONF_MOD == "N") {
                                manyFloraAttribute.CONF_MOD = "M"
                            }

                            dbManager!!.insertmanyflora_attribute(manyFloraAttribute);


                            var intent = Intent()

                            intent.putExtra("export", 70);

                            setResult(RESULT_OK, intent);

                            finish()

                        })

                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            } else if (TreDatas.size > 0 || StreDatas.size > 0 || ShrDatas.size > 0 || HerDatas.size > 0) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            println("insert---------------------")

                            val spec = etTRE_SPECET.text.toString()
                            val fami = etTRE_FAMIET.text.toString()
                            val scien = etTRE_SCIENET.text.toString()

                            var dbh: Float = 0.0F

                            if (etTRE_HET.text.isNotEmpty()) {
                                dbh = etTRE_HET.text.toString().toFloat()
                            }

                            var toil = 0.0F

                            if (etTRE_BREAET.text.isNotEmpty()) {
                                try {
                                    toil = etTRE_BREAET.text.toString().toFloat()
                                } catch (e: NumberFormatException) {
                                }
                            }

                            var under = etTRE_COVEET.text.toString()
                            var waterwidth = 0
                            if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                                waterwidth = etTRE_WATERWIDTH.text.toString().toInt()
                            }

                            var type =  Utils.getString(etTRE_TYPE)

                            println("delete-------------------- $keyId")
                            dbManager!!.deleteAllManyFloraAttribute(keyId)

                            var treChk = false

                            for (i in 0 until TreDatas.size) {
                                if (TreDatas.get(i).PAGE == trepage) {
                                    treChk = true
                                }
                            }

                            if (treChk == false) {

                                var tredata = TreeData1(-1, trepage, spec, fami, scien, dbh, toil, under.toString(), waterwidth, type)

                                TreDatas.add(tredata)
                            } else {
                                for (i in 0 until TreDatas.size) {
                                    if (TreDatas.get(i).PAGE == trepage) {
                                        TreDatas.get(i).SPEC = spec
                                        TreDatas.get(i).FAMI = fami
                                        TreDatas.get(i).SCIEN = scien
                                        TreDatas.get(i).DBH = dbh
                                        TreDatas.get(i).TOIL = toil
                                        TreDatas.get(i).UNDER = under
                                        TreDatas.get(i).WATERWIDTH = waterwidth
                                        TreDatas.get(i).TYPE = type
                                    }
                                }
                            }

                            var streChk = false

                            val strespec = etSTRE_SPECET.text.toString()
                            val strefami = etSTRE_FAMIET.text.toString()
                            val strescien = etSTRE_SCIENET.text.toString()

                            var streh: Float = 0.0F

                            if (etSTRE_HET.text.isNotEmpty()) {
                                streh = etSTRE_HET.text.toString().toFloat()
                            }

                            var strebrea = 0.0F

                            if (etSTRE_BREAET.text.isNotEmpty()) {
                                strebrea = etSTRE_BREAET.text.toString().toFloat()
                            }
                            var strecove = ""

                            if (etSTRE_COVEET.text.isNotEmpty()) {
                                strecove = etSTRE_COVEET.text.toString()
                            }

                            var strewatherrwidth = 0
                            if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                                strewatherrwidth = etSTRE_WATERWIDTH.text.toString().toInt()
                            }
                            var stretype = etSTRE_TYPE.text.toString()


                            for (i in 0 until StreDatas.size) {
                                if (StreDatas.get(i).PAGE == strepage) {
                                    streChk = true
                                    println("strechk -- true")
                                }
                            }

                            if (streChk == false) {

                                var stredata = TreeData1(-1, strepageTV.text.toString().toInt(), strespec, strefami, strescien, streh, strebrea, strecove.toString(), waterwidth, stretype)

                                StreDatas.add(stredata)

                            } else {
                                for (i in 0 until StreDatas.size) {
                                    if (StreDatas.get(i).PAGE == strepage) {
                                        StreDatas.get(i).SPEC = strespec
                                        StreDatas.get(i).FAMI = strefami
                                        StreDatas.get(i).SCIEN = strescien
                                        StreDatas.get(i).DBH = streh
                                        StreDatas.get(i).TOIL = strebrea
                                        StreDatas.get(i).UNDER = strecove
                                        StreDatas.get(i).WATERWIDTH = waterwidth
                                        StreDatas.get(i).TYPE = type

                                    }
                                }
                            }

                            var shrChk = false

                            var shrspec = etSHR_SPECET.text.toString()
                            if (etSHR_SPECtmp.length() > 0) {
                                shrspec = etSHR_SPECtmp.text.toString()
                            }
                            val shrfami = etSHR_FAMIET.text.toString()
                            val shrscien = etSHR_SCIENET.text.toString()

                            var shrh: Float = 0.0F

                            if (etSHR_HET.text.isNotEmpty()) {
                                shrh = etSHR_HET.text.toString().toFloat()
                            }

                            var shrcove = 0.0F

                            if (etSTR_COVEET.text.isNotEmpty()) {
                                shrcove = etSTR_COVEET.text.toString().toFloat()
                            }

                            var shretc =""
                            if (etSHR_UNDER.text.isNotEmpty()) {
                                shretc = etSHR_UNDER.text.toString()
                            }

                            for (i in 0 until ShrDatas.size) {
                                if (ShrDatas.get(i).PAGE == shrpage) {
                                    shrChk = true
                                }
                            }

                            if (shrChk == false) {

                                var shrdata = TreeData2(-1, shrpageTV.text.toString().toInt(), shrspec, shrfami, shrscien, shrh.toString(), shrcove.toString(), shretc.toString())

                                ShrDatas.add(shrdata)

                            } else {
                                for (i in 0 until ShrDatas.size) {
                                    if (ShrDatas.get(i).PAGE == shrpage) {
                                        ShrDatas.get(i).SPEC = shrspec
                                        ShrDatas.get(i).FAMI = shrfami
                                        ShrDatas.get(i).SCIEN = shrscien
                                        ShrDatas.get(i).H = shrh.toString()
                                        ShrDatas.get(i).COVE = shrcove.toString()
                                        ShrDatas.get(i).ETC = shretc.toString()
                                    }
                                }
                            }

                            var herChk = false

                            val herspec = etHER_SPECET.text.toString()
                            val herfami = etHER_FAMIET.text.toString()
                            val herscien = etHER_SCIENET.text.toString()

                            var herh: Float = 0.0F

                            if (etHER_HET.text.isNotEmpty()) {
                                herh = etHER_HET.text.toString().toFloat()
                            }

                            var hercove = 0.0F

                            if (etHER_COVEET.text.isNotEmpty()) {
                                hercove = etHER_COVEET.text.toString().toFloat()
                            }

                            var heretc = 0.0f
                            if (etHER_HEIGHT.text.isNotEmpty()) {
                                heretc = etHER_HEIGHT.text.toString().toFloat()
                            }

                            for (i in 0 until HerDatas.size) {
                                if (HerDatas.get(i).PAGE == herpage) {
                                    herChk = true
                                }
                            }

                            if (herChk == false) {

                                var herdata = TreeData2(-1, herpageTV.text.toString().toInt(), herspec, herfami, herscien, herh.toString(), hercove.toString(), heretc.toString())

                                HerDatas.add(herdata)
                            } else {
                                for (i in 0 until HerDatas.size) {
                                    if (HerDatas.get(i).PAGE == herpage) {
                                        HerDatas.get(i).SPEC = herspec
                                        HerDatas.get(i).FAMI = herfami
                                        HerDatas.get(i).SCIEN = herscien
                                        HerDatas.get(i).H = herh.toString()
                                        HerDatas.get(i).COVE = hercove.toString()
                                        HerDatas.get(i).ETC = heretc.toString()
                                    }
                                }
                            }

                            var MaxLength = 0
                            var TreDataSize = TreDatas.size
                            var StreDataSize = StreDatas.size
                            var ShrDataSize = ShrDatas.size
                            var HerDataSize = HerDatas.size

                            println("TreDataSize : $TreDataSize StreDataSize $StreDataSize ShrDataSize : $ShrDataSize HerDataSize : $HerDataSize ")

                            MaxLength = TreDatas.size

                            if (MaxLength < StreDatas.size) {
                                MaxLength = StreDatas.size
                            }

                            if (MaxLength < ShrDatas.size) {
                                MaxLength = ShrDatas.size
                            }

                            if (MaxLength < HerDatas.size) {
                                MaxLength = HerDatas.size
                            }

                            for (i in 0..MaxLength - 1) {
                                var manyFloraAttribute =  null_many_attribute()

                                keyId = intent.getStringExtra("GROP_ID")

                                println("insertkeyid $keyId")
                                manyFloraAttribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

                                manyFloraAttribute.CURRENT_TM = Utils.current_tm()
                                manyFloraAttribute.GROP_ID = keyId

                                var names = t_name.split("-")
                                if (names.size > 1&&!t_name.contains("군락")) {
                                        manyFloraAttribute.DOMIN = t_name + "군락"
                                } else if (!t_name.contains("군락")) {
                                    if (t_name.length>0){
                                        manyFloraAttribute.DOMIN = t_name + "군락"
                                    }
                                } else {
                                    manyFloraAttribute.DOMIN = t_name
                                }
//                                    manyFloraAttribute.INV_REGION = invregionTV.text.toString()
                                if (invregionTV.length() > 0) {
                                    manyFloraAttribute.INV_REGION = invregionTV.text.toString();
                                } else {
                                    manyFloraAttribute.INV_REGION = INV_REGION
                                }

                                manyFloraAttribute.INV_DT = invdtTV.text.toString()

                                if (invpersonTV.text == null || invpersonTV.text.equals("")) {
                                    manyFloraAttribute.INV_PERSON = userName
                                } else {
                                    manyFloraAttribute.INV_PERSON = invpersonTV.text.toString()
                                }

                                manyFloraAttribute.INV_TM = Utils.timeStr()

                                manyFloraAttribute.TEMP_YN = "Y"
                                manyFloraAttribute.CONF_MOD = "N"

                                if (TreDatas != null && TreDataSize > 0) {
                                    if (i > TreDataSize - 1) {
                                        manyFloraAttribute.TRE_NUM = 0
                                        manyFloraAttribute.TRE_SPEC = ""
                                        manyFloraAttribute.TRE_FAMI = ""
                                        manyFloraAttribute.TRE_SCIEN = ""
                                        manyFloraAttribute.TRE_DBH = 0.0f
                                        manyFloraAttribute.TRE_TOIL = 0.0f
                                        manyFloraAttribute.TRE_UNDER = ""
                                        manyFloraAttribute.TRE_WATER = 0
                                        manyFloraAttribute.TRE_TYPE = ""
                                    }

                                    if (i <= TreDataSize - 1) {
                                        manyFloraAttribute.TRE_NUM = TreDatas.get(i).PAGE
                                        manyFloraAttribute.TRE_SPEC = TreDatas.get(i).SPEC
                                        manyFloraAttribute.TRE_FAMI = TreDatas.get(i).FAMI
                                        manyFloraAttribute.TRE_SCIEN = TreDatas.get(i).SCIEN
                                        manyFloraAttribute.TRE_DBH = TreDatas.get(i).DBH
                                        manyFloraAttribute.TRE_TOIL = TreDatas.get(i).TOIL
                                        manyFloraAttribute.TRE_UNDER = TreDatas.get(i).UNDER
                                        manyFloraAttribute.TRE_WATER = TreDatas.get(i).WATERWIDTH
                                        manyFloraAttribute.TRE_TYPE = TreDatas.get(i).TYPE
                                    }
                                }

                                if (StreDatas != null && StreDataSize > 0) {
                                    if (i > StreDataSize - 1) {
                                        manyFloraAttribute.STRE_NUM = 1
                                        manyFloraAttribute.STRE_SPEC = ""
                                        manyFloraAttribute.STRE_FAMI = ""
                                        manyFloraAttribute.STRE_SCIEN = ""
                                        manyFloraAttribute.STRE_DBH = 0.0f
                                        manyFloraAttribute.STRE_TOIL = 0.0f
                                        manyFloraAttribute.STRE_UNDER = ""
                                        manyFloraAttribute.STRE_WATER = 0
                                        manyFloraAttribute.STRE_TYPE = ""
                                    }

                                    if (i <= StreDataSize - 1) {
                                        manyFloraAttribute.STRE_NUM = StreDatas.get(i).PAGE
                                        manyFloraAttribute.STRE_SPEC = StreDatas.get(i).SPEC
                                        manyFloraAttribute.STRE_FAMI = StreDatas.get(i).FAMI
                                        manyFloraAttribute.STRE_SCIEN = StreDatas.get(i).SCIEN
                                        manyFloraAttribute.STRE_DBH = StreDatas.get(i).DBH
                                        manyFloraAttribute.STRE_TOIL = StreDatas.get(i).TOIL
                                        manyFloraAttribute.STRE_UNDER = StreDatas.get(i).UNDER
                                        manyFloraAttribute.STRE_WATER = StreDatas.get(i).WATERWIDTH
                                        manyFloraAttribute.STRE_TYPE = StreDatas.get(i).TYPE
                                    }
                                }


                                if (ShrDatas != null && ShrDataSize > 0) {
                                    if (i > ShrDataSize - 1) {
                                        manyFloraAttribute.SHR_NUM = 1
                                        manyFloraAttribute.SHR_SPEC = ""
                                        manyFloraAttribute.SHR_FAMI = ""
                                        manyFloraAttribute.SHR_SCIEN = ""
                                        manyFloraAttribute.SHR_TOIL = 0.0f
                                        manyFloraAttribute.SHR_WATER = 0.0f
                                        manyFloraAttribute.SHR_UNDER = ""

                                    }
                                    if (i <= ShrDataSize - 1) {
                                        manyFloraAttribute.SHR_NUM = ShrDatas.get(i).PAGE
                                        manyFloraAttribute.SHR_SPEC = ShrDatas.get(i).SPEC
                                        manyFloraAttribute.SHR_FAMI = ShrDatas.get(i).FAMI
                                        manyFloraAttribute.SHR_SCIEN = ShrDatas.get(i).SCIEN
                                        if (ShrDatas.get(i).H != null) {
                                            manyFloraAttribute.SHR_TOIL = ShrDatas.get(i).H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.SHR_TOIL = 0.0f
                                        }

                                        if (ShrDatas.get(i).COVE != null) {
                                            manyFloraAttribute.SHR_WATER = ShrDatas.get(i).COVE!!.toFloat()
                                        } else {
                                            manyFloraAttribute.SHR_WATER = 0.0f
                                        }
                                        manyFloraAttribute.SHR_UNDER = ShrDatas.get(i).ETC
                                    }
                                }

                                if (HerDatas != null && HerDataSize > 0) {
                                    if (i > HerDataSize - 1) {
                                        manyFloraAttribute.HER_NUM = 1
                                        manyFloraAttribute.HER_SPEC = ""
                                        manyFloraAttribute.HER_FAMI = ""
                                        manyFloraAttribute.HER_SCIEN = ""
                                        manyFloraAttribute.HER_DOMIN = 0.0f
                                        manyFloraAttribute.HER_GUNDO = 0.0f
                                        manyFloraAttribute.HER_HEIGHT = 0.0f
                                    }
                                    if (i <= HerDataSize - 1) {
                                        manyFloraAttribute.HER_NUM = HerDatas.get(i).PAGE
                                        manyFloraAttribute.HER_SPEC = HerDatas.get(i).SPEC
                                        manyFloraAttribute.HER_FAMI = HerDatas.get(i).FAMI
                                        manyFloraAttribute.HER_SCIEN = HerDatas.get(i).SCIEN
                                        if (HerDatas.get(i).H != null) {
                                            manyFloraAttribute.HER_DOMIN = HerDatas.get(i).H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.HER_DOMIN = 0.0f
                                        }

                                        if (HerDatas.get(i).COVE != null) {
                                            manyFloraAttribute.HER_DOMIN = HerDatas.get(i).H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.HER_DOMIN = 0.0f
                                        }

                                        if (HerDatas.get(i).ETC != null) {
                                            manyFloraAttribute.HER_HEIGHT = HerDatas.get(i).ETC!!.toFloat()
                                        } else {
                                            manyFloraAttribute.HER_HEIGHT = 0.0f
                                        }

                                    }
                                }

                                manyFloraAttribute.GEOM = log.toString() + " " + lat.toString()



                                dbManager!!.insertmanyflora_attribute(manyFloraAttribute);
                                println("insert-------")

                            }


                            var intent = Intent()

                            intent.putExtra("export", 70);

                            setResult(RESULT_OK, intent);

                            finish()

//                                insert()

                        })

                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }
        }

        cancleBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dataList: Array<String> = arrayOf("*");

                        println("canclekeyid $keyId")

                        val data = db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null) {
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var manyFloraAttribute = ps_many_attribute(data)
                            dataArray.add(manyFloraAttribute)
                        }

                        println("cencle dataArrayListSize ${dataArray.size}")

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

                            var intent = Intent()

                            intent.putExtra("markerid", markerid)
                            setResult(RESULT_OK, intent);

                        }

                        data.close()

                        finish()

                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()

        }

        deleteBT.setOnClickListener {

            if (pk != null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            var manyFloraAttribute =  null_many_attribute()


                            if (pk != null) {

                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data = db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data.moveToNext()) {

                                        chkdata = true

                                        var manyFloraAttribute = ps_many_attribute(data)

                                        dataArray.add(manyFloraAttribute)

                                    }

                                    dbManager!!.deletemanyflora_attribute(manyFloraAttribute, GROP_ID)

                                    var intent = Intent()

                                    intent.putExtra("markerid", markerid)

                                    setResult(RESULT_OK, intent);
                                    finish()
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
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db!!.query("ManyFloraAttribute", dataList, "id = '$id'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data.moveToNext()) {

                                    chkdata = true

                                    var manyFloraAttribute = ps_many_attribute(data)

                                }

                                if (chkdata == true) {
                                    Toast.makeText(context, "추가하신 데이터가 있습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    var intent = Intent()

                                    intent.putExtra("markerid", markerid)

                                    setResult(RESULT_OK, intent);
                                    finish()
                                }
                                data.close()

                            }

                            if (intent.getStringExtra("id") == null) {
                                var intent = Intent()

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

        etTRE_SPECreset.setOnClickListener {
            etTRE_SPECtmp.setText("")
            etTRE_SPECLL.visibility = View.GONE
            etTRE_SPECET.visibility = View.VISIBLE
        }

        etSTRE_SPECreset.setOnClickListener {
            etSTRE_SPECtmp.setText("")
            etSTRE_SPECLL.visibility = View.GONE
            etSTRE_SPECET.visibility = View.VISIBLE
        }

        etSHR_SPECreset.setOnClickListener {
            etSHR_SPECtmp.setText("")
            etSHR_SPECLL.visibility = View.GONE
            etSHR_SPECET.visibility = View.VISIBLE
        }

        etHER_SPECreset.setOnClickListener {
            etHER_SPECtmp.setText("")
            etHER_SPECLL.visibility = View.GONE
            etHER_SPECET.visibility = View.VISIBLE
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var number: Number

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_DATA1 -> {
                    if (data!!.getStringExtra("name") == "SP(미동정)") {
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT)
                    } else {
                        t_name = data!!.getStringExtra("name")
                        dominTV.setText(t_name)
                    }

                }
                SET_DATA6 -> {
                    t_name += "-"
                    if (data!!.getStringExtra("name") == "SP(미동정)") {
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT2)
                    } else {
                        t_name += data!!.getStringExtra("name");
                        var names = t_name.split("-")
                        if (names.size > 1) {
                            ausTV.setText(names[1]+"군락")
                        }
                    }
                }
                SET_INPUT -> {
                    var name = data!!.getStringExtra("name");
                    dominTV.text = name
                }
                SET_INPUT2 -> {
                    var name = data!!.getStringExtra("name");
                    t_name += name
                    ausTV.text = name+"군락"
                }
                SET_DATA2 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etTRE_SPECET.setText(name)
                    if (name == "SP(미동정)") {
                        etTRE_SPECET.visibility = View.GONE
                        etTRE_SPECLL.visibility = View.VISIBLE
                    }
                    etTRE_FAMIET.setText(family_name)
                    etTRE_SCIENET.setText(zoological)

                }

                SET_DATA3 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etSTRE_SPECET.setText(name)
                    if (name == "SP(미동정)") {
                        etSTRE_SPECET.visibility = View.GONE
                        etSTRE_SPECLL.visibility = View.VISIBLE
                    }
                    etSTRE_FAMIET.setText(family_name)
                    etSTRE_SCIENET.setText(zoological)

                }

                SET_DATA4 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etSHR_SPECET.setText(name)
                    if (name == "SP(미동정)") {
                        etSHR_SPECET.visibility = View.GONE
                        etSHR_SPECLL.visibility = View.VISIBLE
                    }
                    etSHR_FAMIET.setText(family_name)
                    etSHR_SCIENET.setText(zoological)

                }

                SET_DATA5 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etHER_SPECET.setText(name)
                    if (name == "SP(미동정)") {
                        etHER_SPECET.visibility = View.GONE
                        etHER_SPECLL.visibility = View.VISIBLE
                    }
                    etHER_FAMIET.setText(family_name)
                    etHER_SCIENET.setText(zoological)

                }
                FROM_CAMERA -> {

                    if (resultCode == -1) {
                        val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora2/images" + File.separator + keyId
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

                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora2/images" + File.separator + keyId + File.separator
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora2/images" + File.separator + keyId + File.separator
                    val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora2/images" + File.separator + keyId

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

//                        val num = flora2numTV.text.toString()
                        var time = ""
                        time = flora2invtmTV.text.toString()
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






    fun datedlg() {
        var day = Utils.todayStr()
        var days = day.split("-")
        DatePickerDialog(context, dateSetListener, days[0].toInt(), days[1].toInt()-1, days[2].toInt()).show()
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


    override fun onBackPressed() {
        val dataList: Array<String> = arrayOf("*");

        println("keyid $keyId")

        val data = db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null) {
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var manyFloraAttribute = ps_many_attribute(data)
            dataArray.add(manyFloraAttribute)
        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null) {

            var intent = Intent()

            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

        }

        data.close()

        finish()
    }

    fun AddTreFlora() {

        var spec = etTRE_SPECET.text.toString()
        if (etTRE_SPECtmp.length() > 0) {
            spec = etTRE_SPECtmp.text.toString()
        }

        val fami = etTRE_FAMIET.text.toString()
        val scien = etTRE_SCIENET.text.toString()

        var dbh: Float = 0.0F

        if (etTRE_HET.text.isNotEmpty()) {
            dbh = etTRE_HET.text.toString().toFloat()
        }

        var toil = 0.0F

        if (etTRE_BREAET.text.isNotEmpty()) {
            toil = etTRE_BREAET.text.toString().toFloat()
        }

//        var cove = 0.0F
//
//        if (etTRE_COVEET.text.isNotEmpty()) {
//            cove = etTRE_COVEET.text.toString().toFloat()
//        }

        var under = etTRE_COVEET.text.toString()
        var waterwidth = 0
        if (etTRE_BREAET.text.isNotEmpty()) {
            waterwidth = etTRE_WATERWIDTH.text.toString().toInt()
        }
        var type =  Utils.getString(etTRE_TYPE)

        var chkData = false

        var equlas = false

        println("trepage $trepage")

        val maxsize = trerightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until TreDatas.size) {
            if (TreDatas.get(i).PAGE == trepage) {
                division = true
            }
        }

        if (spec == "" && dbh == 0.0F && toil == 0.0F && under == "" && waterwidth == 0 && type == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {

            if (trepage == maxsize) {

                if (division == false) {

                    if (trepage > 1) {

                        var tredata = TreeData1(-1, trepage, spec, fami, scien, dbh, toil, under.toString(), waterwidth, type)

                        TreDatas.add(tredata)

                        trepage = trepage + 1

                        val page = trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        trenumTV.setText(page.toString())
                        etTRE_SPECET.setText("")
                        etTRE_FAMIET.setText("")
                        etTRE_SCIENET.setText("")
                        etTRE_HET.setText("")
                        etTRE_BREAET.setText("")
                        etTRE_COVEET.setText("")
                        etTRE_WATERWIDTH.setText("")
                        etTRE_TYPE.setText("")
                    }

                    if (trepage == 1) {

                        var tredata = TreeData1(-1, trepage, spec, fami, scien, dbh, toil, under.toString(), waterwidth, type)

                        TreDatas.add(tredata)

                        trepage = trepage + 1

                        val page = trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        trenumTV.setText(page.toString())
                        etTRE_SPECET.setText("")
                        etTRE_FAMIET.setText("")
                        etTRE_SCIENET.setText("")
                        etTRE_HET.setText("")
                        etTRE_BREAET.setText("")
                        etTRE_COVEET.setText("")
                        etTRE_WATERWIDTH.setText("")
                        etTRE_TYPE.setText("")

                    }
                } else {

                    if (trepage > 1) {

                        for (i in 0 until TreDatas.size) {
                            if (TreDatas.get(i).PAGE == trepage) {
                                TreDatas.get(i).SPEC = spec
                                TreDatas.get(i).FAMI = fami
                                TreDatas.get(i).SCIEN = scien
                                TreDatas.get(i).DBH = dbh
                                TreDatas.get(i).TOIL = toil
                                TreDatas.get(i).UNDER = under
                                TreDatas.get(i).WATERWIDTH = waterwidth
                                TreDatas.get(i).TYPE = type
                            }
                        }

                        trepage = trepage + 1

                        val page = trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        trenumTV.setText(page.toString())
                        etTRE_SPECET.setText("")
                        etTRE_FAMIET.setText("")
                        etTRE_SCIENET.setText("")
                        etTRE_HET.setText("")
                        etTRE_BREAET.setText("")
                        etTRE_COVEET.setText("")
                        etTRE_WATERWIDTH.setText("")
                        etTRE_TYPE.setText("")
                    }

                    if (trepage == 1) {

                        for (i in 0 until TreDatas.size) {
                            if (TreDatas.get(i).PAGE == trepage) {
                                TreDatas.get(i).SPEC = spec
                                TreDatas.get(i).FAMI = fami
                                TreDatas.get(i).SCIEN = scien
                                TreDatas.get(i).DBH = dbh
                                TreDatas.get(i).TOIL = toil
                                TreDatas.get(i).UNDER = under
                                TreDatas.get(i).WATERWIDTH = waterwidth
                                TreDatas.get(i).TYPE = type
                            }
                        }

                        trepage = trepage + 1

                        val page = trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        trenumTV.setText(page.toString())
                        etTRE_SPECET.setText("")
                        etTRE_FAMIET.setText("")
                        etTRE_SCIENET.setText("")
                        etTRE_HET.setText("")
                        etTRE_BREAET.setText("")
                        etTRE_COVEET.setText("")
                        etTRE_WATERWIDTH.setText("")
                        etTRE_TYPE.setText("")

                    }
                }
            }
            if (trepage < maxsize) {

                for (i in 0 until TreDatas.size) {
                    if (TreDatas.get(i).PAGE == trepage) {
                        TreDatas.get(i).SPEC = spec
                        TreDatas.get(i).FAMI = fami
                        TreDatas.get(i).SCIEN = scien
                        TreDatas.get(i).DBH = dbh
                        TreDatas.get(i).TOIL = toil
                        TreDatas.get(i).UNDER = under
                        TreDatas.get(i).WATERWIDTH = waterwidth
                        TreDatas.get(i).TYPE = type
                    }
                }

                trepage = trepage + 1
                var chk = false
                for (i in 0..TreDatas.size - 1) {
                    if (trepage == TreDatas.get(i).PAGE) {
                        chk = true
                        val data = TreDatas.get(i)
                        etTRE_SPECET.setText(data.SPEC)
                        etTRE_FAMIET.setText(data.FAMI)
                        etTRE_SCIENET.setText(data.SCIEN)
                        etTRE_HET.setText(data.DBH.toString())
                        etTRE_BREAET.setText(data.TOIL.toString())
                        etTRE_COVEET.setText(data.UNDER.toString())
                        etTRE_WATERWIDTH.setText(data.WATERWIDTH.toString())
                        etTRE_TYPE.setText(data.TYPE.toString())

                        val page = trepage
                        val size = trerightpageTV.text.toString().toInt()

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        trenumTV.setText(page.toString())
                    }
                }

                if (chk == false) {
                    val page = trepage
                    val size = trerightpageTV.text.toString().toInt() + 1

                    trepageTV.setText(page.toString())
                    trerightpageTV.setText(size.toString())
                    trenumTV.setText(page.toString())
                    etTRE_SPECET.setText("")
                    etTRE_FAMIET.setText("")
                    etTRE_SCIENET.setText("")
                    etTRE_HET.setText("")
                    etTRE_BREAET.setText("")
                    etTRE_COVEET.setText("")
                    etTRE_WATERWIDTH.setText("")
                    etTRE_TYPE.setText("")
                }
            }

            deleteBT.visibility = View.GONE
            etTRE_SPECET.visibility = View.VISIBLE
            etTRE_SPECLL.visibility = View.GONE
            etTRE_SPECtmp.setText("")
        }

    }

    fun AddStreFlora() {

        var spec = etSTRE_SPECET.text.toString()
        if (etSTRE_SPECtmp.length() > 0) {
            spec = etSTRE_SPECtmp.text.toString()
        }

        val fami = etSTRE_FAMIET.text.toString()
        val scien = etSTRE_SCIENET.text.toString()

        var dbh: Float = 0.0F

        if (etSTRE_HET.text.isNotEmpty()) {
            dbh = etSTRE_HET.text.toString().toFloat()
        }

        var toil = 0.0F

        if (etSTRE_BREAET.text.isNotEmpty()) {
            toil = etSTRE_BREAET.text.toString().toFloat()
        }
//                var under = 0.0F
//
//                if (etSTRE_COVEET.text.isNotEmpty()) {
//                    under = etSTRE_COVEET.text.toString().toFloat()
//                }

        var under = etSTRE_COVEET.text.toString()

        var waterwidth = 0
        if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
            waterwidth = etSTRE_WATERWIDTH.text.toString().toInt()
        }

        var type = Utils.getString(etSTRE_TYPE)

        var chkData = false

        var equlas = false

        val maxsize = strerightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until StreDatas.size) {
            if (StreDatas.get(i).PAGE == strepage) {
                division = true
            }
        }

        if (spec == "" && dbh == 0.0F && toil == 0.0F && under == "" && waterwidth == 0 && type == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {
            if (strepage == maxsize) {

                if (division == false) {

                    if (strepage > 1) {

                        var stredata = TreeData1(-1, strepage, spec, fami, scien, dbh, toil, under, waterwidth, type)

                        StreDatas.add(stredata)

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        strenumTV.setText(page.toString())
                        etSTRE_SPECLL.visibility = View.GONE
                        etSTRE_SPECET.visibility = View.VISIBLE
                        etSTRE_SPECET.setText("")
                        etSTRE_FAMIET.setText("")
                        etSTRE_SCIENET.setText("")
                        etSTRE_HET.setText("")
                        etSTRE_BREAET.setText("")
                        etSTRE_COVEET.setText("")
                        etSTRE_WATERWIDTH.setText("")
                        etSTRE_TYPE.setText("")
                    }

                    if (strepage == 1) {

                        var stredata = TreeData1(-1, strepage, spec, fami, scien, dbh, toil, under, waterwidth, type)

                        StreDatas.add(stredata)

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        strenumTV.setText(page.toString())
                        etSTRE_SPECLL.visibility = View.GONE
                        etSTRE_SPECET.visibility = View.VISIBLE
                        etSTRE_SPECET.setText("")
                        etSTRE_FAMIET.setText("")
                        etSTRE_SCIENET.setText("")
                        etSTRE_HET.setText("")
                        etSTRE_BREAET.setText("")
                        etSTRE_COVEET.setText("")
                        etSTRE_WATERWIDTH.setText("")
                        etSTRE_TYPE.setText("")

                    }
                } else {
                    if (strepage > 1) {

                        for (i in 0 until StreDatas.size) {
                            if (StreDatas.get(i).PAGE == strepage) {
                                StreDatas.get(i).SPEC = spec
                                StreDatas.get(i).FAMI = fami
                                StreDatas.get(i).SCIEN = scien
                                StreDatas.get(i).DBH = dbh
                                StreDatas.get(i).TOIL = toil
                                StreDatas.get(i).UNDER = under
                                StreDatas.get(i).WATERWIDTH = waterwidth
                                StreDatas.get(i).TYPE = type
                            }
                        }

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        strenumTV.setText(page.toString())
                        etSTRE_SPECLL.visibility = View.GONE
                        etSTRE_SPECET.visibility = View.VISIBLE
                        etSTRE_SPECET.setText("")
                        etSTRE_FAMIET.setText("")
                        etSTRE_SCIENET.setText("")
                        etSTRE_HET.setText("")
                        etSTRE_BREAET.setText("")
                        etSTRE_COVEET.setText("")
                        etSTRE_WATERWIDTH.setText("")
                        etSTRE_TYPE.setText("")
                    }

                    if (strepage == 1) {

                        for (i in 0 until StreDatas.size) {
                            if (StreDatas.get(i).PAGE == strepage) {
                                StreDatas.get(i).SPEC = spec
                                StreDatas.get(i).FAMI = fami
                                StreDatas.get(i).SCIEN = scien
                                StreDatas.get(i).DBH = dbh
                                StreDatas.get(i).TOIL = toil
                                StreDatas.get(i).UNDER = under
                                StreDatas.get(i).WATERWIDTH = waterwidth
                                StreDatas.get(i).TYPE = type
                            }
                        }

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        strenumTV.setText(page.toString())
                        etSTRE_SPECLL.visibility = View.GONE
                        etSTRE_SPECET.visibility = View.VISIBLE
                        etSTRE_SPECET.setText("")
                        etSTRE_FAMIET.setText("")
                        etSTRE_SCIENET.setText("")
                        etSTRE_HET.setText("")
                        etSTRE_BREAET.setText("")
                        etSTRE_COVEET.setText("")
                        etSTRE_WATERWIDTH.setText("")
                        etSTRE_TYPE.setText("")
                    }
                }
            }
            if (strepage < maxsize) {

                for (i in 0 until StreDatas.size) {
                    if (StreDatas.get(i).PAGE == strepage) {
                        StreDatas.get(i).SPEC = spec
                        StreDatas.get(i).FAMI = fami
                        StreDatas.get(i).SCIEN = scien
                        StreDatas.get(i).DBH = dbh
                        StreDatas.get(i).TOIL = toil
                        StreDatas.get(i).UNDER = under
                        StreDatas.get(i).WATERWIDTH = waterwidth
                        StreDatas.get(i).TYPE = type
                    }
                }
                strepage = strepage + 1
                var chk = false
                for (i in 0..StreDatas.size - 1) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        chk = true
                        val data = StreDatas.get(i)
                        etSTRE_SPECET.setText(data.SPEC)
                        etSTRE_FAMIET.setText(data.FAMI)
                        etSTRE_SCIENET.setText(data.SCIEN)
                        etSTRE_HET.setText(data.DBH.toString())
                        etSTRE_BREAET.setText(data.TOIL.toString())
                        etSTRE_COVEET.setText(data.UNDER.toString())
                        etSTRE_WATERWIDTH.setText(data.WATERWIDTH.toString())
                        etSTRE_TYPE.setText(data.TYPE.toString())

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt()

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        strenumTV.setText(page.toString())
                    }
                }

                if (chk == false) {
                    val page = strepage
                    val size = strerightpageTV.text.toString().toInt() + 1

                    strepageTV.setText(page.toString())
                    strerightpageTV.setText(size.toString())
                    strenumTV.setText(page.toString())
                    etSTRE_SPECLL.visibility = View.GONE
                    etSTRE_SPECET.visibility = View.VISIBLE
                    etSTRE_SPECET.setText("")
                    etSTRE_FAMIET.setText("")
                    etSTRE_SCIENET.setText("")
                    etSTRE_HET.setText("")
                    etSTRE_BREAET.setText("")
                    etSTRE_COVEET.setText("")
                    etSTRE_WATERWIDTH.setText("")
                    etSTRE_TYPE.setText("")
                }
            }

            deleteBT.visibility = View.GONE
            etSTRE_SPECET.visibility = View.VISIBLE
            etSTRE_SPECLL.visibility = View.GONE
            etSTRE_SPECtmp.setText("")
        }

    }

    fun AddShrFlora() {
        var spec = etSHR_SPECET.text.toString()
        if (etSHR_SPECtmp.length() > 0) {
            spec = etSHR_SPECtmp.text.toString()
        }
        val fami = etSHR_FAMIET.text.toString()
        val scien = etSHR_SCIENET.text.toString()

        var h: Float = 0.0F

        if (etSHR_HET.text.isNotEmpty()) {
            h = etSHR_HET.text.toString().toFloat()
        }

        var cove = 0.0F

        if (etSTR_COVEET.text.isNotEmpty()) {
            cove = etSTR_COVEET.text.toString().toFloat()
        }

        var etc = etSHR_UNDER.text.toString()

        var chkData = false

        var equlas = false

        println("shrpage $shrpage")

        val maxsize = shrrightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until ShrDatas.size) {
            if (ShrDatas.get(i).PAGE == shrpage) {
                division = true
            }
        }

        if (spec == "" && h == 0.0F && cove == 0.0F && etc == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {
            if (shrpage == maxsize) {

                if (division == false) {

                    if (shrpage > 1) {

                        var shrdata = TreeData2(-1, shrpage, spec, fami, scien, h.toString(), cove.toString(), etc)

                        ShrDatas.add(shrdata)

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrrightpageTV.text.toString().toInt() + 1

                        shrpageTV.setText(page.toString())
                        shrrightpageTV.setText(size.toString())
                        shrnumTV.setText(page.toString())
                        etSHR_SPECLL.visibility = View.GONE
                        etSHR_SPECET.visibility = View.VISIBLE
                        etSHR_SPECET.setText("")
                        etSHR_SPECtmp.setText("")
                        etSHR_FAMIET.setText("")
                        etSHR_SCIENET.setText("")
                        etSHR_HET.setText("")
                        etSTR_COVEET.setText("")
                        etSHR_UNDER.setText("")
                    }

                    if (shrpage == 1) {

                        var shrdata = TreeData2(-1, shrpage, spec, fami, scien, h.toString(), cove.toString(), etc)

                        ShrDatas.add(shrdata)

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrrightpageTV.text.toString().toInt() + 1

                        shrpageTV.setText(page.toString())
                        shrrightpageTV.setText(size.toString())
                        shrnumTV.setText(page.toString())
                        etSHR_SPECLL.visibility = View.GONE
                        etSHR_SPECET.visibility = View.VISIBLE
                        etSHR_SPECET.setText("")
                        etSHR_SPECtmp.setText("")
                        etSHR_FAMIET.setText("")
                        etSHR_SCIENET.setText("")
                        etSHR_HET.setText("")
                        etSTR_COVEET.setText("")
                        etSHR_UNDER.setText("")

                    }
                } else {
                    if (shrpage > 1) {

                        for (i in 0 until ShrDatas.size) {
                            if (ShrDatas.get(i).PAGE == shrpage) {
                                ShrDatas.get(i).SPEC = spec
                                ShrDatas.get(i).FAMI = fami
                                ShrDatas.get(i).SCIEN = scien
                                ShrDatas.get(i).H = h.toString()
                                ShrDatas.get(i).COVE = cove.toString()
                                ShrDatas.get(i).ETC = etc.toString()
                            }
                        }

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrrightpageTV.text.toString().toInt() + 1

                        shrpageTV.setText(page.toString())
                        shrrightpageTV.setText(size.toString())
                        shrnumTV.setText(page.toString())
                        etSHR_SPECLL.visibility = View.GONE
                        etSHR_SPECET.visibility = View.VISIBLE
                        etSHR_SPECET.setText("")
                        etSHR_SPECtmp.setText("")
                        etSHR_FAMIET.setText("")
                        etSHR_SCIENET.setText("")
                        etSHR_HET.setText("")
                        etSTR_COVEET.setText("")
                        etSHR_UNDER.setText("")
                    }

                    if (shrpage == 1) {

                        for (i in 0 until ShrDatas.size) {
                            if (ShrDatas.get(i).PAGE == shrpage) {
                                ShrDatas.get(i).SPEC = spec
                                ShrDatas.get(i).FAMI = fami
                                ShrDatas.get(i).SCIEN = scien
                                ShrDatas.get(i).H = h.toString()
                                ShrDatas.get(i).COVE = cove.toString()
                                ShrDatas.get(i).ETC = etc.toString()
                            }
                        }

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrrightpageTV.text.toString().toInt() + 1

                        shrpageTV.setText(page.toString())
                        shrrightpageTV.setText(size.toString())
                        shrnumTV.setText(page.toString())
                        etSHR_SPECLL.visibility = View.GONE
                        etSHR_SPECET.visibility = View.VISIBLE
                        etSHR_SPECET.setText("")
                        etSHR_SPECtmp.setText("")
                        etSHR_FAMIET.setText("")
                        etSHR_SCIENET.setText("")
                        etSHR_HET.setText("")
                        etSTR_COVEET.setText("")
                        etSHR_UNDER.setText("")

                    }
                }
            }
            if (shrpage < maxsize) {
                for (i in 0 until ShrDatas.size) {
                    if (ShrDatas.get(i).PAGE == shrpage) {
                        ShrDatas.get(i).SPEC = spec
                        ShrDatas.get(i).FAMI = fami
                        ShrDatas.get(i).SCIEN = scien
                        ShrDatas.get(i).H = h.toString()
                        ShrDatas.get(i).COVE = cove.toString()
                        ShrDatas.get(i).ETC = etc.toString()
                    }
                }
                shrpage = shrpage + 1
                var chk = false
                for (i in 0..ShrDatas.size - 1) {
                    if (shrpage == ShrDatas.get(i).PAGE) {
                        chk = true
                        val data = ShrDatas.get(i)
                        etSHR_SPECET.setText(data.SPEC)
                        etSHR_FAMIET.setText(data.FAMI)
                        etSHR_SCIENET.setText(data.SCIEN)
                        etSHR_HET.setText(data.H.toString())
                        etSTR_COVEET.setText(data.COVE.toString())
                        etSHR_UNDER.setText(data.ETC.toString())

                        val page = shrpage
                        val size = shrrightpageTV.text.toString().toInt()

                        shrpageTV.setText(page.toString())
                        shrrightpageTV.setText(size.toString())
                        shrnumTV.setText(page.toString())
                    }
                }

                if (chk == false) {
                    val page = shrpage
                    val size = shrrightpageTV.text.toString().toInt() + 1

                    shrpageTV.setText(page.toString())
                    shrrightpageTV.setText(size.toString())
                    shrnumTV.setText(page.toString())
                    etSHR_SPECLL.visibility = View.GONE
                    etSHR_SPECET.visibility = View.VISIBLE
                    etSHR_SPECET.setText("")
                    etSHR_SPECtmp.setText("")
                    etSHR_FAMIET.setText("")
                    etSHR_SCIENET.setText("")
                    etSHR_HET.setText("")
                    etSTR_COVEET.setText("")
                    etSHR_UNDER.setText("")
                }
            }
            deleteBT.visibility = View.GONE
            etSHR_SPECET.visibility = View.VISIBLE
            etSHR_SPECLL.visibility = View.GONE
            etSHR_SPECtmp.setText("")
        }

    }

    fun AddHerFlora() {

        var spec = etHER_SPECET.text.toString()
        if (etHER_SPECtmp.length() > 0) {
            spec = etHER_SPECtmp.text.toString()
        }
        val fami = etHER_FAMIET.text.toString()
        val scien = etHER_SCIENET.text.toString()

        var h: Float = 0.0F

        if (etHER_HET.text.isNotEmpty()) {
            h = etHER_HET.text.toString().toFloat()
        }

        var cove = 0.0F

        if (etHER_COVEET.text.isNotEmpty()) {
            cove = etHER_COVEET.text.toString().toFloat()
        }

        var etc = 0.0f
        if (etHER_HEIGHT.text.isNotEmpty()) {
            etc = etHER_HEIGHT.text.toString().toFloat()
        }

        var chkData = false

        var equlas = false

        var division = false

        for (i in 0 until HerDatas.size) {
            if (HerDatas.get(i).PAGE == herpage) {
                division = true
            }
        }

        if (spec == "" && h == 0.0F && cove == 0.0F && etc == 0.0f) {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {
            val maxsize = herrightpageTV.text.toString().toInt()

            if (herpage == maxsize) {

                if (division == false) {
                    if (herpage > 1) {

                        var herdata = TreeData2(-1, herpage, spec, fami, scien, h.toString(), cove.toString(), etc.toString())

                        HerDatas.add(herdata)

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        hernumTV.setText(page.toString())
                        etHER_SPECLL.visibility = View.GONE
                        etHER_SPECET.visibility = View.VISIBLE
                        etHER_SPECET.setText("")
                        etHER_SPECtmp.setText("")
                        etHER_FAMIET.setText("")
                        etHER_SCIENET.setText("")
                        etHER_HET.setText("")
                        etHER_COVEET.setText("")
                        etHER_HEIGHT.setText("")
                    }

                    if (herpage == 1) {

                        var herdata = TreeData2(-1, herpage, spec, fami, scien, h.toString(), cove.toString(), etc.toString())

                        HerDatas.add(herdata)

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        hernumTV.setText(page.toString())
                        etHER_SPECLL.visibility = View.GONE
                        etHER_SPECET.visibility = View.VISIBLE
                        etHER_SPECET.setText("")
                        etHER_SPECtmp.setText("")
                        etHER_FAMIET.setText("")
                        etHER_SCIENET.setText("")
                        etHER_HET.setText("")
                        etHER_COVEET.setText("")
                        etHER_HEIGHT.setText("")
                    }
                } else {
                    if (herpage > 1) {

                        for (i in 0 until HerDatas.size) {
                            if (HerDatas.get(i).PAGE == herpage) {
                                HerDatas.get(i).SPEC = spec
                                HerDatas.get(i).FAMI = fami
                                HerDatas.get(i).SCIEN = scien
                                HerDatas.get(i).H = h.toString()
                                HerDatas.get(i).COVE = cove.toString()
                                HerDatas.get(i).ETC = etc.toString()
                            }
                        }

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        hernumTV.setText(page.toString())
                        etHER_SPECLL.visibility = View.GONE
                        etHER_SPECET.visibility = View.VISIBLE
                        etHER_SPECET.setText("")
                        etHER_SPECtmp.setText("")
                        etHER_FAMIET.setText("")
                        etHER_SCIENET.setText("")
                        etHER_HET.setText("")
                        etHER_COVEET.setText("")
                        etHER_HEIGHT.setText("")
                    }

                    if (herpage == 1) {

                        for (i in 0 until HerDatas.size) {
                            if (HerDatas.get(i).PAGE == herpage) {
                                HerDatas.get(i).SPEC = spec
                                HerDatas.get(i).FAMI = fami
                                HerDatas.get(i).SCIEN = scien
                                HerDatas.get(i).H = h.toString()
                                HerDatas.get(i).COVE = cove.toString()
                                HerDatas.get(i).ETC = etc.toString()
                            }
                        }

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        hernumTV.setText(page.toString())
                        etHER_SPECLL.visibility = View.GONE
                        etHER_SPECET.visibility = View.VISIBLE
                        etHER_SPECET.setText("")
                        etHER_SPECtmp.setText("")
                        etHER_FAMIET.setText("")
                        etHER_SCIENET.setText("")
                        etHER_HET.setText("")
                        etHER_COVEET.setText("")
                        etHER_HEIGHT.setText("")
                    }
                }
            }
            if (herpage < maxsize) {
                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE == herpage) {
                        HerDatas.get(i).SPEC = spec
                        HerDatas.get(i).FAMI = fami
                        HerDatas.get(i).SCIEN = scien
                        HerDatas.get(i).H = h.toString()
                        HerDatas.get(i).COVE = cove.toString()
                        HerDatas.get(i).ETC = etc.toString()
                    }
                }
                herpage = herpage + 1
                var chk = false
                for (i in 0..HerDatas.size - 1) {
                    if (herpage == HerDatas.get(i).PAGE) {
                        chk = true
                        val data = HerDatas.get(i)
                        etHER_SPECET.setText(data.SPEC)
                        etHER_FAMIET.setText(data.FAMI)
                        etHER_SCIENET.setText(data.SCIEN)
                        etHER_HET.setText(data.H.toString())
                        etHER_COVEET.setText(data.COVE.toString())
                        etHER_HEIGHT.setText(data.ETC.toString())

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt()

                        herpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        hernumTV.setText(page.toString())
                    }
                }

                if (chk == false) {
                    val page = herpage
                    val size = herrightpageTV.text.toString().toInt() + 1

                    herpageTV.setText(page.toString())
                    herrightpageTV.setText(size.toString())
                    hernumTV.setText(page.toString())
                    etHER_SPECLL.visibility = View.GONE
                    etHER_SPECET.visibility = View.VISIBLE
                    etHER_SPECET.setText("")
                    etHER_SPECtmp.setText("")
                    etHER_FAMIET.setText("")
                    etHER_SCIENET.setText("")
                    etHER_HET.setText("")
                    etHER_COVEET.setText("")
                    etHER_HEIGHT.setText("")
                }
            }

            deleteBT.visibility = View.GONE
        }

        etHER_SPECET.visibility = View.VISIBLE
        etHER_SPECLL.visibility = View.GONE
        etHER_SPECtmp.setText("")

    }

    fun clear() {
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

        val TRENUM = dbManager.manyfloratrenumNext()
        val STRENUM = dbManager.manyflorastrenumNext()
        val SHRNUM = dbManager.manyflorashrnumNext()
        val HERNUM = dbManager.manyflorahernumNext()

        trenumTV.setText(TRENUM.toString())
        strenumTV.setText(STRENUM.toString())
        shrnumTV.setText(SHRNUM.toString())
        hernumTV.setText(HERNUM.toString())

        etTRE_SPECET.setText("")
        etTRE_FAMIET.setText("")
        etTRE_SCIENET.setText("")
        etTRE_HET.setText("")
        etTRE_BREAET.setText("")
        etTRE_COVEET.setText("")
        etSTRE_SPECET.setText("")
        etSTRE_FAMIET.setText("")
        etSTRE_SCIENET.setText("")
        etSTRE_HET.setText("")
        etSTRE_BREAET.setText("")
        etSTRE_COVEET.setText("")
        etSHR_SPECET.setText("")
        etSHR_FAMIET.setText("")
        etSHR_SCIENET.setText("")
        etSHR_HET.setText("")
        etSTR_COVEET.setText("")
        etHER_SPECET.setText("")
        etHER_SPECtmp.setText("")
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        etHER_HET.setText("")
        etHER_COVEET.setText("")

        etSHR_SPECtmp.setText("")
        etHER_SPECtmp.setText("")
        etSTRE_SPECtmp.setText("")
        etTRE_SPECtmp.setText("")

        etTRE_WATERWIDTH.setText("")
        etTRE_TYPE.setText("")
        etSTRE_WATERWIDTH.setText("")
        etSTRE_TYPE.setText("")
        etSHR_UNDER.setText("")
        etHER_HEIGHT.setText("")
    }

    fun TreClear(page: String) {
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()
//        val TRENUM = dbManager.manyfloratrenumNext()
        trenumTV.setText(page)
        etTRE_SPECET.setText("")
        etTRE_FAMIET.setText("")
        etTRE_SCIENET.setText("")
        etTRE_HET.setText("")
        etTRE_BREAET.setText("")
        etTRE_COVEET.setText("")
    }

    fun Stre_Clear(page: String) {
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

//        val STRENUM = dbManager.manyflorastrenumNext()
        strenumTV.setText(page)
        etSTRE_SPECET.setText("")
        etSTRE_FAMIET.setText("")
        etSTRE_SCIENET.setText("")
        etSTRE_HET.setText("")
        etSTRE_BREAET.setText("")
        etSTRE_COVEET.setText("")
    }

    fun Shr_Clear(page: String) {
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

//        val SHRNUM = dbManager.manyflorashrnumNext()
        shrnumTV.setText(page)
        etSTRE_COVEET.setText("")
        etSHR_SPECET.setText("")
        etSHR_FAMIET.setText("")
        etSHR_SCIENET.setText("")
        etSHR_HET.setText("")
        etSTR_COVEET.setText("")
    }

    fun Her_Clear(page: String) {
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()
//        val HERNUM = dbManager.manyflorahernumNext()

        hernumTV.setText(page)
        etHER_SPECET.setText("")
        etHER_SPECtmp.setText("")
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        etHER_HET.setText("")
        etHER_COVEET.setText("")
    }


    fun null_many_attribute(): ManyFloraAttribute {
        val manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null, null, null, null, null, null, null, null, null, null
        , null, null, null, null, null, null, null, null, null, null, null, null, null
        , null, null, null, null, null, null, null, null, null, null, null, null, null
        , null, null, null, null, null, null, null,null, null,null)
        return manyFloraAttribute
    }

    fun ps_many_attribute(data: Cursor): ManyFloraAttribute {
        val manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getString(12), data.getInt(13), data.getString(14)
                , data.getInt(15), data.getString(16), data.getString(17), data.getString(18), data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22)
                , data.getString(23), data.getInt(24), data.getString(25), data.getString(26), data.getString(27), data.getFloat(28), data.getFloat(29), data.getString(30), data.getInt(31), data.getString(32)
                , data.getString(33), data.getString(34), data.getFloat(35), data.getFloat(36), data.getFloat(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41)
                , data.getString(42), data.getString(43), data.getString(44), data.getString(45))

        return manyFloraAttribute
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION)
            } else if (Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                val intent1 = Intent(context, WriteAlbumActivity::class.java)
                startActivityForResult(intent1, FROM_ALBUM)
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "flora2/images" + File.separator + keyId + File.separator
                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    var del_images: ArrayList<String> = ArrayList();
                    try {
                        images!!.clear()
                        del_images = images_path!![tag].split("/") as ArrayList<String>
                        images_path!!.removeAt(tag)

//                    val num = flora2numTV.text.toString()
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

}


