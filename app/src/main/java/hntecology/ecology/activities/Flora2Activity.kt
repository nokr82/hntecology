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
import android.widget.TimePicker
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

    var HerDatas: ArrayList<TreeData3> = ArrayList<TreeData3>()
    var TreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var StreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var ShrDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()
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
        prjnameET.setText(PrefUtils.getStringPreference(context, "prjname"))

        florainvtmET.setText(Utils.timeStr())
        florainvtmET.setOnClickListener {
            timedlg()
        }


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
                Log.d("군집구조불러오기","부른다")
                invregionTV.setText(manyFloraAttribute.INV_REGION)
                INV_REGION = manyFloraAttribute.INV_REGION.toString()
                prjnameET.setText(manyFloraAttribute.PRJ_NAME)
                invdtTV.setText(manyFloraAttribute.INV_DT)
                florainvtmET.setText(manyFloraAttribute.INV_TM)
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
                val M_TRE_DBH = manyFloraAttribute.M_TRE_DBH
                val X_TRE_DBH = manyFloraAttribute.X_TRE_DBH
                val M_TRE_TOIL = manyFloraAttribute.M_TRE_TOIL
                val X_TRE_TOIL = manyFloraAttribute.X_TRE_TOIL
                val M_TRE_UDR = manyFloraAttribute.M_TRE_UDR
                val X_TRE_UDR = manyFloraAttribute.X_TRE_UDR
                val M_TRE_WT = manyFloraAttribute.M_TRE_WT
                val X_TRE_WT = manyFloraAttribute.X_TRE_WT


                println("TRE_NUM $TRE_NUM TRE_SCIEN $TRE_SCIEN TRE_DBH $TRE_DBH TRE_TOIL $TRE_TOIL TRE_UNDER $TRE_UNDER")

                if (TRE_SPEC!!.length > 0 || TRE_DBH!! != 0.0f || TRE_TOIL!! != 0.0f || TRE_WATERWIDTH != 0.0f) {
                    val data = TreeData1(PK, TRE_NUM, TRE_SPEC, TRE_FAMI, TRE_SCIEN, M_TRE_DBH, TRE_DBH, X_TRE_DBH, M_TRE_TOIL, TRE_TOIL, X_TRE_TOIL,
                            M_TRE_UDR, TRE_UNDER, X_TRE_UDR, M_TRE_WT, TRE_WATERWIDTH, X_TRE_WT, TRE_TYPE)
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
                    str_minET.setText(manyFloraAttribute.M_TRE_DBH.toString())
                    str_maxET.setText(manyFloraAttribute.X_TRE_DBH.toString())
                    st_br_minET.setText(manyFloraAttribute.M_TRE_TOIL.toString())
                    st_br_maxET.setText(manyFloraAttribute.X_TRE_TOIL.toString())
                    tr_cov_minET.setText(manyFloraAttribute.M_TRE_UDR.toString())
                    tr_cov_maxET.setText(manyFloraAttribute.X_TRE_UDR.toString())
                    tr_wt_minET.setText(manyFloraAttribute.M_TRE_WT.toString())
                    tr_wt_maxET.setText(manyFloraAttribute.X_TRE_WT.toString())
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
                val M_STR_DBH = manyFloraAttribute.M_STR_DBH
                val X_STR_DBH = manyFloraAttribute.X_STR_DBH
                val M_STR_TOIL = manyFloraAttribute.M_STR_TOIL
                val X_STR_TOIL = manyFloraAttribute.X_STR_TOIL
                val M_STR_UDR = manyFloraAttribute.M_STR_UDR
                val X_STR_UDR = manyFloraAttribute.X_STR_UDR
                val M_STR_WT = manyFloraAttribute.M_STR_WT
                val X_STR_WT = manyFloraAttribute.X_STR_WT
                println("STRE_NUM $STRE_NUM STRE_SPEC $STRE_FAMI STRE_FAMI $STRE_FAMI STRE_SCIEN $STRE_SCIEN STRE_H $STRE_DBH")

                if (STRE_SPEC!!.length > 0 || STRE_DBH!! != 0.0f || STRE_TOIL!! != 0.0f || STRE_WATER != 0.0f) {
                    val data = TreeData1(PK, STRE_NUM, STRE_SPEC, STRE_FAMI, STRE_SCIEN, M_STR_DBH, STRE_DBH, X_STR_DBH, M_STR_TOIL, STRE_TOIL, X_STR_TOIL
                            , M_STR_UDR, STRE_UNDER, X_STR_UDR
                           , M_STR_WT, STRE_WATER, X_STR_WT, STRE_TYPE)
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
                    st_min_hetET.setText(manyFloraAttribute.M_STR_DBH.toString())
                    st_max_hetET.setText(manyFloraAttribute.X_STR_DBH.toString())
                    st_br_min_ET.setText(manyFloraAttribute.M_STR_TOIL.toString())
                    st_br_max_ET.setText(manyFloraAttribute.X_STR_TOIL.toString())
                    st_cov_minET.setText(manyFloraAttribute.M_STR_UDR.toString())
                    st_cov_maxET.setText(manyFloraAttribute.X_STR_UDR.toString())
                    st_wt_minET.setText(manyFloraAttribute.M_STR_WT.toString())
                    st_wt_maxET.setText(manyFloraAttribute.X_STR_WT.toString())

                }

                //관목층

                val SHR_NUM = manyFloraAttribute.SHR_NUM
                val SHR_SPEC = manyFloraAttribute.SHR_SPEC
                val SHR_FAMI = manyFloraAttribute.SHR_FAMI
                val SHR_SCIEN = manyFloraAttribute.SHR_SCIEN
                val SHR_TOIL = manyFloraAttribute.SHR_TOIL
                val SHR_WATERWIDTH = manyFloraAttribute.SHR_WATER
                val SHR_UNDER = manyFloraAttribute.SHR_UNDER
                val M_SHR_WT = manyFloraAttribute.M_SHR_WT
                val X_SHR_WT = manyFloraAttribute.X_SHR_WT
                val M_SHR_TOIL = manyFloraAttribute.M_SHR_TOIL
                val X_SHR_TOIL = manyFloraAttribute.X_SHR_TOIL
                val M_SHR_UDR = manyFloraAttribute.M_SHR_UDR
                val X_SHR_UDR = manyFloraAttribute.X_SHR_UDR

                println("SHR_NUM $SHR_NUM SHR_SPEC $SHR_SPEC SHR_FAMI $SHR_FAMI SHR_SCIEN $SHR_SCIEN SHR_TOIL $SHR_TOIL")

                if (SHR_SPEC!!.length > 0 || SHR_TOIL!! != 0.0f || SHR_WATERWIDTH != 0.0f) {
                    shrpage = manyFloraAttribute.SHR_NUM!!
                    val data = TreeData2(PK, SHR_NUM, SHR_SPEC, SHR_FAMI, SHR_SCIEN, M_SHR_TOIL.toString(), SHR_TOIL.toString(), X_SHR_TOIL.toString()
                            , M_SHR_WT.toString(), SHR_WATERWIDTH.toString(), X_SHR_WT.toString()
                            , M_SHR_UDR.toString(), SHR_UNDER.toString(), X_SHR_UDR.toString())
                    ShrDatas.add(data)
                    shrnumTV.setText(manyFloraAttribute.SHR_NUM.toString())
                    etSHR_SPECET.setText(manyFloraAttribute.SHR_SPEC)
                    etSHR_FAMIET.setText(manyFloraAttribute.SHR_FAMI)
                    etSHR_SCIENET.setText(manyFloraAttribute.SHR_SCIEN)
                    etSHR_HET.setText(manyFloraAttribute.SHR_TOIL.toString())
                    etSTR_COVEET.setText(manyFloraAttribute.SHR_WATER.toString())
                    etSHR_UNDER.setText(manyFloraAttribute.SHR_UNDER.toString())
                    sh_het_minET.setText(manyFloraAttribute.M_SHR_TOIL.toString())
                    sh_het_maxET.setText(manyFloraAttribute.X_SHR_TOIL.toString())
                    sh_cov_minET.setText(manyFloraAttribute.M_SHR_WT.toString())
                    sh_cov_maxET.setText(manyFloraAttribute.X_SHR_WT.toString())
                    sh_under_minET.setText(manyFloraAttribute.M_SHR_UDR.toString())
                    sh_under_maxET.setText(manyFloraAttribute.X_SHR_UDR.toString())
                }

                val HER_NUM = manyFloraAttribute.HER_NUM
                val HER_SPEC = manyFloraAttribute.HER_SPEC
                val HER_FAMI = manyFloraAttribute.HER_FAMI
                val HER_SCIEN = manyFloraAttribute.HER_SCIEN
                val HER_DOMIN = manyFloraAttribute.HER_DOMIN
                val HER_GUNDO = manyFloraAttribute.HER_GUNDO
                val HER_HEIGHT = manyFloraAttribute.HER_HEIGHT
                val M_HER_HET = manyFloraAttribute.M_HER_HET
                val X_HER_HET = manyFloraAttribute.X_HER_HET
                println("HER_NUM $HER_NUM HER_SPEC $HER_SPEC HER_FAMI $HER_FAMI HER_SCIEN $HER_SCIEN HER_DOMIN $HER_DOMIN")

                if (HER_SPEC!!.length > 0 || HER_DOMIN!! != "" || HER_GUNDO != "") {
                    herpage = manyFloraAttribute.HER_NUM!!
                    val data = TreeData3(PK, HER_NUM, HER_SPEC, HER_FAMI, HER_SCIEN,M_HER_HET.toString(), HER_HEIGHT.toString(),X_HER_HET.toString(), HER_GUNDO.toString(), HER_DOMIN.toString())
                    HerDatas.add(data)
                    hernumTV.setText(manyFloraAttribute.HER_NUM.toString())
                    etHER_SPECET.setText(manyFloraAttribute.HER_SPEC)
                    etHER_FAMIET.setText(manyFloraAttribute.HER_FAMI)
                    etHER_SCIENET.setText(manyFloraAttribute.HER_SCIEN)

                    val etc = manyFloraAttribute.HER_DOMIN.toString()
                    if (etc != "" && etc != null && etc.count() > 0 && etc != "null") {
                        etHER_HET.setText(etc)
                    }

                    val cove = manyFloraAttribute.HER_GUNDO.toString()
                    if (cove.count() > 0 && cove != "" && cove != "null" && cove != null) {
                        etHER_COVEET.setText(cove)
                    }

                    etHER_HEIGHT.setText(manyFloraAttribute.HER_HEIGHT.toString())
                    herminET.setText(manyFloraAttribute.M_HER_HET.toString())
                    hermaxET.setText(manyFloraAttribute.X_HER_HET.toString())
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
//            invdtTV.setText(base.INV_DT)

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
                        herminET.setText(HerDatas.get(i).M_H.toString())
                        hermaxET.setText(HerDatas.get(i).X_H.toString())
                        etHER_HEIGHT.setText(HerDatas.get(i).H.toString())

                        val cove = HerDatas.get(i).COVE.toString()
                        if (cove != "" && cove != null && cove.count() > 0 && cove != "null") {
                            etHER_COVEET.setText(cove)
                        }

                        val etc = HerDatas.get(i).ETC.toString()
                        if (etc != "" && etc != null && etc.count() > 0 && etc != "null") {
                            etHER_HET.setText(etc)
                        }


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
                            herminET.setText(HerDatas.get(i).M_H.toString())
                            etHER_HEIGHT.setText(HerDatas.get(i).H.toString())
                            hermaxET.setText(HerDatas.get(i).X_H.toString())

                            val cove = HerDatas.get(i).COVE.toString()
                            if (cove != "" && cove != null && cove != "null" && cove.count() > 0) {
                                etHER_COVEET.setText(cove)
                            }

                            val etc = HerDatas.get(i).ETC.toString()
                            if (etc != "" && etc != null && etc.count() > 0 && etc != "null") {
                                etHER_HET.setText(etc)
                            }

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
                            herminET.setText(HerDatas.get(i).M_H.toString())
                            etHER_HEIGHT.setText(HerDatas.get(i).H.toString())
                            hermaxET.setText(HerDatas.get(i).X_H.toString())

                            val cove = HerDatas.get(i).COVE.toString()
                            if (cove.count() > 0 && cove != "null" && cove != null && cove != "") {
                                etHER_COVEET.setText(cove)
                            }

                            val etc = HerDatas.get(i).ETC.toString()
                            if (etc != "" && etc != null && etc.count() > 0 && etc != "null") {
                                etHER_HET.setText(etc)
                            }

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


                var m_dbh: Float = 0.0F

                if (str_minET.text.isNotEmpty()) {
                    m_dbh = str_minET.text.toString().toFloat()
                }
                var dbh: Float = 0.0F

                if (etTRE_HET.text.isNotEmpty()) {
                    dbh = etTRE_HET.text.toString().toFloat()
                }
                var x_dbh: Float = 0.0F

                if (str_maxET.text.isNotEmpty()) {
                    x_dbh = str_maxET.text.toString().toFloat()
                }

                var m_toil: Float = 0.0F

                if (st_br_minET.text.isNotEmpty()) {
                    m_toil = st_br_minET.text.toString().toFloat()
                }

                var toil = 0.0F

                if (etTRE_BREAET.text.isNotEmpty()) {
                    toil = etTRE_BREAET.text.toString().toFloat()
                }
                var x_toil: Float = 0.0F

                if (st_br_maxET.text.isNotEmpty()) {
                    x_toil = st_br_maxET.text.toString().toFloat()
                }
                var m_under = 0.0F

                if (tr_cov_minET.text.isNotEmpty()) {
                    m_under = tr_cov_minET.text.toString().toFloat()
                }
                var under = 0.0F
                if (etTRE_COVEET.text.isNotEmpty()) {
                    under = etTRE_COVEET.text.toString().toFloat()
                }
                var x_under = 0.0F

                if (tr_cov_maxET.text.isNotEmpty()) {
                    x_under = tr_cov_maxET.text.toString().toFloat()
                }
//        var under = etSTRE_COVEET.text.toString()
                var m_waterwidth = 0.0F
                if (tr_wt_minET.text.isNotEmpty()) {
                    m_waterwidth = tr_wt_minET.text.toString().toFloat()
                }
                var waterwidth = 0.0F
                if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                    waterwidth = etTRE_WATERWIDTH.text.toString().toFloat()
                }
                var x_waterwidth = 0.0F
                if (tr_wt_maxET.text.isNotEmpty()) {
                    x_waterwidth = tr_wt_maxET.text.toString().toFloat()
                }
                var type =  Utils.getString(etTRE_TYPE)


                var tredata = TreeData1(-1, trepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                        ,m_waterwidth, waterwidth,x_waterwidth, type)

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
                            TreDatas.get(i).UNDER = etTRE_COVEET.text.toString().toFloat()
                        }

                        if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                            TreDatas.get(i).WATERWIDTH = etTRE_WATERWIDTH.text.toString().toFloat()
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
                        st_min_hetET.setText(data.M_DBH.toString())
                        st_max_hetET.setText(data.X_DBH.toString())
                        st_br_min_ET.setText(data.M_TOIL.toString())
                        st_br_max_ET.setText(data.X_TOIL.toString())
                        st_cov_minET.setText(data.M_UNDER.toString())
                        st_cov_maxET.setText(data.X_UNDER.toString())
                        st_wt_minET.setText(data.M_WATER.toString())
                        st_wt_maxET.setText(data.X_WATER.toString())
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
                var m_dbh: Float = 0.0F

                if (st_min_hetET.text.isNotEmpty()) {
                    m_dbh = st_min_hetET.text.toString().toFloat()
                }
                var dbh: Float = 0.0F

                if (etSTRE_HET.text.isNotEmpty()) {
                    dbh = etSTRE_HET.text.toString().toFloat()
                }
                var x_dbh: Float = 0.0F

                if (st_max_hetET.text.isNotEmpty()) {
                    x_dbh = st_max_hetET.text.toString().toFloat()
                }

                var m_toil: Float = 0.0F

                if (st_max_hetET.text.isNotEmpty()) {
                    m_toil = st_br_minET.text.toString().toFloat()
                }

                var toil = 0.0F

                if (etSTRE_BREAET.text.isNotEmpty()) {
                    toil = etSTRE_BREAET.text.toString().toFloat()
                }
                var x_toil: Float = 0.0F

                if (st_br_maxET.text.isNotEmpty()) {
                    x_toil = st_br_maxET.text.toString().toFloat()
                }
                var m_under = 0.0F

                if (st_cov_minET.text.isNotEmpty()) {
                    m_under = st_cov_minET.text.toString().toFloat()
                }
                var under = 0.0F

                if (etSTRE_COVEET.text.isNotEmpty()) {
                    under = etSTRE_COVEET.text.toString().toFloat()
                }
                var x_under = 0.0F

                if (st_cov_minET.text.isNotEmpty()) {
                    x_under = st_cov_maxET.text.toString().toFloat()
                }
//        var under = etSTRE_COVEET.text.toString()
                var m_waterwidth = 0.0F
                if (st_wt_minET.text.isNotEmpty()) {
                    m_waterwidth = st_wt_minET.text.toString().toFloat()
                }
                var waterwidth = 0.0F
                if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                    waterwidth = etSTRE_WATERWIDTH.text.toString().toFloat()
                }
                var x_waterwidth = 0.0F
                if (st_wt_maxET.text.isNotEmpty()) {
                    x_waterwidth = st_wt_maxET.text.toString().toFloat()
                }

                var type = Utils.getString(etSTRE_TYPE)

                var stredata = TreeData1(-1, strepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                        ,m_waterwidth, waterwidth,x_waterwidth, type)

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
                            StreDatas.get(i).UNDER = etSTRE_COVEET.text.toString().toFloat()
                        }

                        if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                            StreDatas.get(i).WATERWIDTH = etSTRE_WATERWIDTH.text.toString().toFloat()
                        }
                        if (st_min_hetET.text.isNotEmpty()) {
                            StreDatas.get(i).M_DBH = st_min_hetET.text.toString().toFloat()
                        }
                        if (st_br_min_ET.text.isNotEmpty()) {
                            StreDatas.get(i).M_TOIL = st_br_min_ET.text.toString().toFloat()
                        }
                        if (st_br_max_ET.text.isNotEmpty()) {
                            StreDatas.get(i).X_TOIL = st_br_max_ET.text.toString().toFloat()
                        }
                        if (st_cov_minET.text.isNotEmpty()) {
                            StreDatas.get(i).M_UNDER = st_cov_minET.text.toString().toFloat()
                        }
                        if (st_cov_maxET.text.isNotEmpty()) {
                            StreDatas.get(i).X_UNDER = st_cov_maxET.text.toString().toFloat()
                        }
                        if (st_wt_minET.text.isNotEmpty()) {
                            StreDatas.get(i).M_WATER = st_wt_minET.text.toString().toFloat()
                        }
                        if (st_wt_maxET.text.isNotEmpty()) {
                            StreDatas.get(i).X_WATER = st_wt_maxET.text.toString().toFloat()
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
                        st_min_hetET.setText(data.M_DBH.toString())
                        st_max_hetET.setText(data.X_DBH.toString())
                        st_br_min_ET.setText(data.M_TOIL.toString())
                        st_br_max_ET.setText(data.X_TOIL.toString())
                        st_cov_minET.setText(data.M_UNDER.toString())
                        st_cov_maxET.setText(data.X_UNDER.toString())
                        st_wt_minET.setText(data.M_WATER.toString())
                        st_wt_maxET.setText(data.X_WATER.toString())
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
                var m_h: Float = 0.0F

                if (sh_het_minET.text.isNotEmpty()) {
                    m_h = sh_het_minET.text.toString().toFloat()
                }
                var x_h: Float = 0.0F

                if (sh_het_maxET.text.isNotEmpty()) {
                    x_h = sh_het_maxET.text.toString().toFloat()
                }

                var cove = 0.0F

                if (etSTR_COVEET.text.isNotEmpty()) {
                    cove = etSTR_COVEET.text.toString().toFloat()
                }
                var m_cove = 0.0F

                if (sh_cov_minET.text.isNotEmpty()) {
                    m_cove = sh_cov_minET.text.toString().toFloat()
                }
                var x_cove = 0.0F

                if (sh_cov_maxET.text.isNotEmpty()) {
                    x_cove = sh_cov_maxET.text.toString().toFloat()
                }

                var m_etc = 0.0F
                if (sh_under_minET.text.isNotEmpty()) {
                    m_etc = sh_under_minET.text.toString().toFloat()
                }
                var x_etc = 0.0F
                if (sh_under_maxET.text.isNotEmpty()) {
                    x_etc = sh_under_maxET.text.toString().toFloat()
                }
                var etc = 0.0F
                if (etSHR_UNDER.text.isNotEmpty()) {
                    etc = etSHR_UNDER.text.toString().toFloat()
                }

                var shrdata = TreeData2(-1, shrpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), m_cove.toString(), cove.toString(), x_cove.toString(), m_etc.toString(), etc.toString(), x_etc.toString())

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
                        ShrDatas.get(i).M_H = sh_het_minET.text.toString()
                        ShrDatas.get(i).X_H = sh_het_maxET.text.toString()
                        ShrDatas.get(i).M_COVE = sh_cov_minET.text.toString()
                        ShrDatas.get(i).X_COVE = sh_cov_maxET.text.toString()
                        ShrDatas.get(i).M_ETC = sh_under_minET.text.toString()
                        ShrDatas.get(i).X_ETC = sh_under_maxET.text.toString()




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
                        sh_het_minET.setText(data.M_H.toString())
                        sh_het_maxET.setText(data.X_H.toString())
                        sh_cov_minET.setText(data.M_COVE.toString())
                        sh_cov_maxET.setText(data.X_COVE.toString())
                        sh_under_minET.setText(data.M_ETC.toString())
                        sh_under_maxET.setText(data.X_ETC.toString())
                        val size = shrrightpageTV.text.toString().toInt()

                        shrpageTV.setText(shrpage.toString())
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
                var m_h: Float = 0.0F

                if (herminET.text.isNotEmpty()) {
                    m_h = herminET.text.toString().toFloat()
                }
                var x_h: Float = 0.0F

                if (hermaxET.text.isNotEmpty()) {
                    x_h = hermaxET.text.toString().toFloat()
                }
                var h =  0.0f
                if (etHER_HEIGHT.text.isNotEmpty()) {
                    h = etHER_HEIGHT.text.toString().toFloat()
                }
                var etc = ""
                if (etHER_HET.text.isNotEmpty()) {
                    etc = etHER_HET.text.toString()
                }

                var cove =""

                if (etHER_COVEET.text.isNotEmpty()) {
                    cove = etHER_COVEET.text.toString()
                }



                var herdata = TreeData3(-1, herpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), cove.toString(), etc.toString())

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
                        HerDatas.get(i).H = etHER_HEIGHT.text.toString()
                        HerDatas.get(i).COVE = etHER_COVEET.text.toString()
                        HerDatas.get(i).ETC = etHER_HET.text.toString()
                        HerDatas.get(i).M_H = herminET.text.toString()
                        HerDatas.get(i).X_H = hermaxET.text.toString()
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
                        herminET.setText(data.M_H.toString())

                        val etc = data.ETC.toString()
                        if (etc != "" && etc != null && etc.count() > 0 && etc != "null") {
                            etHER_HET.setText(etc)
                        }

                        hermaxET.setText(data.X_H.toString())

                        val cove = data.COVE.toString()
                        if (cove != "" && cove != "null" && cove != null && cove.count() > 0) {
                            etHER_COVEET.setText(cove)
                        }

                        etHER_HEIGHT.setText(data.H.toString())

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

                            manyFloraAttribute.PRJ_NAME = prjnameET.text.toString();

                            if (invpersonTV.text == null || invpersonTV.text.equals("")) {
                                manyFloraAttribute.INV_PERSON = userName
                            } else {
                                manyFloraAttribute.INV_PERSON = invpersonTV.text.toString()
                            }

                            manyFloraAttribute.INV_TM = florainvtmET.text.toString()


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
                            if (etTRE_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_UNDER = etTRE_COVEET.text.toString().toFloat()
                            }

                            if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_WATER = etTRE_WATERWIDTH.text.toString().toFloat()
                            }
                            if (str_minET.text.isNotEmpty()) {
                                manyFloraAttribute.M_TRE_DBH = str_minET.text.toString().toFloat()
                            }
                            if (str_maxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_TRE_DBH = str_maxET.text.toString().toFloat()
                            }
                            if (st_br_minET.text.isNotEmpty()) {
                                manyFloraAttribute.M_TRE_TOIL = st_br_minET.text.toString().toFloat()
                            }
                            if (st_br_maxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_TRE_TOIL = st_br_maxET.text.toString().toFloat()
                            }
                            if (tr_cov_minET.text.isNotEmpty()) {
                                manyFloraAttribute.M_TRE_UDR = tr_cov_minET.text.toString().toFloat()
                            }
                            if (tr_cov_maxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_TRE_UDR = tr_cov_maxET.text.toString().toFloat()
                            }
                            if (tr_wt_minET.text.isNotEmpty()) {
                                manyFloraAttribute.M_TRE_WT = tr_wt_minET.text.toString().toFloat()
                            }
                            if (tr_wt_maxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_TRE_WT = tr_wt_maxET.text.toString().toFloat()
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


                            if (etSTRE_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_UNDER = etSTRE_COVEET.text.toString().toFloat()
                            }
//                            if (etSTRE_COVEET.text.isNotEmpty()) {
//                                manyFloraAttribute.STRE_UNDER = etSTRE_COVEET.text.toString().toFloat()
//                            }

                            if (st_min_hetET.text.isNotEmpty()) {
                                manyFloraAttribute.M_STR_DBH = st_min_hetET.text.toString().toFloat()
                            }
                            if (st_max_hetET.text.isNotEmpty()) {
                                manyFloraAttribute.X_STR_DBH = st_max_hetET.text.toString().toFloat()
                            }
                            if (st_br_min_ET.text.isNotEmpty()) {
                                manyFloraAttribute.M_STR_TOIL = st_br_min_ET.text.toString().toFloat()
                            }
                            if (st_br_max_ET.text.isNotEmpty()) {
                                manyFloraAttribute.X_STR_TOIL = st_br_max_ET.text.toString().toFloat()
                            }
                            if (st_cov_minET.text.isNotEmpty()) {
                                manyFloraAttribute.M_STR_UDR = st_cov_minET.text.toString().toFloat()
                            }
                            if (st_cov_maxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_STR_UDR = st_cov_maxET.text.toString().toFloat()
                            }
                            if (st_wt_minET.text.isNotEmpty()) {
                                manyFloraAttribute.M_STR_WT = st_wt_minET.text.toString().toFloat()
                            }
                            if (st_wt_maxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_STR_WT = st_wt_maxET.text.toString().toFloat()
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
                            if (etSHR_UNDER.text.toString().isNotEmpty()) {
                                manyFloraAttribute.SHR_UNDER = etSHR_UNDER.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.SHR_UNDER =  0.0f
                            }

                            if (sh_het_minET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.M_SHR_TOIL = sh_het_minET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.M_SHR_TOIL =  0.0f
                            }
                            if (sh_het_maxET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.X_SHR_TOIL = sh_het_maxET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.X_SHR_TOIL =  0.0f
                            }
                            if (sh_cov_minET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.M_SHR_WT = sh_cov_minET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.M_SHR_WT =  0.0f
                            }
                            if (sh_cov_maxET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.X_SHR_WT = sh_cov_maxET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.X_SHR_WT =  0.0f
                            }
                            if (sh_under_minET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.M_SHR_UDR = sh_under_minET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.M_SHR_UDR =  0.0f
                            }
                            if (sh_under_maxET.text.toString().isNotEmpty()) {
                                manyFloraAttribute.X_SHR_UDR = sh_under_maxET.text.toString().toFloat()
                            }else{
                                manyFloraAttribute.X_SHR_UDR =  0.0f
                            }

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
                                manyFloraAttribute.HER_DOMIN = etHER_HET.text.toString()
                            }

                            if (etHER_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.HER_GUNDO = etHER_COVEET.text.toString()
                            }

                            if (etHER_HEIGHT.text.isNotEmpty()) {
                                manyFloraAttribute.HER_HEIGHT = etHER_HEIGHT.text.toString().toFloat()
                            }

                            if (herminET.text.isNotEmpty()) {
                                manyFloraAttribute.M_HER_HET = herminET.text.toString().toFloat()
                            }
                            if (hermaxET.text.isNotEmpty()) {
                                manyFloraAttribute.X_HER_HET = hermaxET.text.toString().toFloat()
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

                            var m_dbh: Float = 0.0F

                            if (str_minET.text.isNotEmpty()) {
                                m_dbh = str_minET.text.toString().toFloat()
                            }
                            var dbh: Float = 0.0F

                            if (etTRE_HET.text.isNotEmpty()) {
                                dbh = etTRE_HET.text.toString().toFloat()
                            }
                            var x_dbh: Float = 0.0F

                            if (str_maxET.text.isNotEmpty()) {
                                x_dbh = str_maxET.text.toString().toFloat()
                            }

                            var m_toil: Float = 0.0F

                            if (st_br_minET.text.isNotEmpty()) {
                                m_toil = st_br_minET.text.toString().toFloat()
                            }

                            var toil = 0.0F

                            if (etTRE_BREAET.text.isNotEmpty()) {
                                toil = etTRE_BREAET.text.toString().toFloat()
                            }
                            var x_toil: Float = 0.0F

                            if (st_br_maxET.text.isNotEmpty()) {
                                x_toil = st_br_maxET.text.toString().toFloat()
                            }
                            var m_under = 0.0F

                            if (tr_cov_minET.text.isNotEmpty()) {
                                m_under = tr_cov_minET.text.toString().toFloat()
                            }
                            var under = 0.0F
                            if (etTRE_COVEET.text.isNotEmpty()) {
                                under = etTRE_COVEET.text.toString().toFloat()
                            }
                            var x_under = 0.0F

                            if (tr_cov_maxET.text.isNotEmpty()) {
                                x_under = tr_cov_maxET.text.toString().toFloat()
                            }
//        var under = etSTRE_COVEET.text.toString()
                            var m_waterwidth = 0.0F
                            if (tr_wt_minET.text.isNotEmpty()) {
                                m_waterwidth = tr_wt_minET.text.toString().toFloat()
                            }
                            var waterwidth = 0.0F
                            if (etTRE_WATERWIDTH.text.isNotEmpty()) {
                                waterwidth = etTRE_WATERWIDTH.text.toString().toFloat()
                            }
                            var x_waterwidth = 0.0F
                            if (tr_wt_maxET.text.isNotEmpty()) {
                                x_waterwidth = tr_wt_maxET.text.toString().toFloat()
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

                                var tredata = TreeData1(-1, trepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                                        ,m_waterwidth, waterwidth,x_waterwidth, type)

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
                                        TreDatas.get(i).M_DBH = m_dbh
                                        TreDatas.get(i).X_DBH = x_dbh
                                        TreDatas.get(i).M_TOIL = m_toil
                                        TreDatas.get(i).X_TOIL = x_toil
                                        TreDatas.get(i).M_UNDER = m_under
                                        TreDatas.get(i).X_UNDER = x_under
                                        TreDatas.get(i).M_WATER = m_waterwidth
                                        TreDatas.get(i).X_WATER = x_waterwidth
                                    }
                                }
                            }

                            var streChk = false

                            for (i in 0 until StreDatas.size) {
                                if (StreDatas.get(i).PAGE == strepage) {
                                    streChk = true
                                    println("strechk -- true")
                                }
                            }

                            if (streChk == false) {

                                var spec = etSTRE_SPECET.text.toString()
                                if (etSTRE_SPECtmp.length() > 0) {
                                    spec = etSTRE_SPECtmp.text.toString()
                                }

                                val fami = etSTRE_FAMIET.text.toString()
                                val scien = etSTRE_SCIENET.text.toString()
                                var m_dbh: Float = 0.0F

                                if (st_min_hetET.text.isNotEmpty()) {
                                    m_dbh = st_min_hetET.text.toString().toFloat()
                                }
                                var dbh: Float = 0.0F

                                if (etSTRE_HET.text.isNotEmpty()) {
                                    dbh = etSTRE_HET.text.toString().toFloat()
                                }
                                var x_dbh: Float = 0.0F

                                if (st_max_hetET.text.isNotEmpty()) {
                                    x_dbh = st_max_hetET.text.toString().toFloat()
                                }

                                var m_toil: Float = 0.0F

                                if (st_max_hetET.text.isNotEmpty()) {
                                    m_toil = st_br_minET.text.toString().toFloat()
                                }

                                var toil = 0.0F

                                if (etSTRE_BREAET.text.isNotEmpty()) {
                                    toil = etSTRE_BREAET.text.toString().toFloat()
                                }
                                var x_toil: Float = 0.0F

                                if (st_br_maxET.text.isNotEmpty()) {
                                    x_toil = st_br_maxET.text.toString().toFloat()
                                }
                                var m_under = 0.0F

                                if (st_cov_minET.text.isNotEmpty()) {
                                    m_under = st_cov_minET.text.toString().toFloat()
                                }
                                var under = 0.0F

                                if (etSTRE_COVEET.text.isNotEmpty()) {
                                    under = etSTRE_COVEET.text.toString().toFloat()
                                }
                                var x_under = 0.0F

                                if (st_cov_minET.text.isNotEmpty()) {
                                    x_under = st_cov_maxET.text.toString().toFloat()
                                }
                                var m_waterwidth = 0.0F
                                if (st_wt_minET.text.isNotEmpty()) {
                                    m_waterwidth = st_wt_minET.text.toString().toFloat()
                                }
                                var waterwidth = 0.0F
                                if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                                    waterwidth = etSTRE_WATERWIDTH.text.toString().toFloat()
                                }
                                var x_waterwidth = 0.0F
                                if (st_wt_maxET.text.isNotEmpty()) {
                                    x_waterwidth = st_wt_maxET.text.toString().toFloat()
                                }

                                var type = Utils.getString(etSTRE_TYPE)


                                var stredata = TreeData1(-1, strepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                                        ,m_waterwidth, waterwidth,x_waterwidth, type)
                                StreDatas.add(stredata)

                            } else {
                                var spec = etSTRE_SPECET.text.toString()
                                if (etSTRE_SPECtmp.length() > 0) {
                                    spec = etSTRE_SPECtmp.text.toString()
                                }

                                val fami = etSTRE_FAMIET.text.toString()
                                val scien = etSTRE_SCIENET.text.toString()
                                var m_dbh: Float = 0.0F

                                if (st_min_hetET.text.isNotEmpty()) {
                                    m_dbh = st_min_hetET.text.toString().toFloat()
                                }
                                var dbh: Float = 0.0F

                                if (etSTRE_HET.text.isNotEmpty()) {
                                    dbh = etSTRE_HET.text.toString().toFloat()
                                }
                                var x_dbh: Float = 0.0F

                                if (st_max_hetET.text.isNotEmpty()) {
                                    x_dbh = st_max_hetET.text.toString().toFloat()
                                }

                                var m_toil: Float = 0.0F

                                if (st_max_hetET.text.isNotEmpty()) {
                                    m_toil = st_br_minET.text.toString().toFloat()
                                }

                                var toil = 0.0F

                                if (etSTRE_BREAET.text.isNotEmpty()) {
                                    toil = etSTRE_BREAET.text.toString().toFloat()
                                }
                                var x_toil: Float = 0.0F

                                if (st_br_maxET.text.isNotEmpty()) {
                                    x_toil = st_br_maxET.text.toString().toFloat()
                                }
                                var m_under = 0.0F

                                if (st_cov_minET.text.isNotEmpty()) {
                                    m_under = st_cov_minET.text.toString().toFloat()
                                }
                                var under = 0.0F

                                if (etSTRE_COVEET.text.isNotEmpty()) {
                                    under = etSTRE_COVEET.text.toString().toFloat()
                                }
                                var x_under = 0.0F

                                if (st_cov_minET.text.isNotEmpty()) {
                                    x_under = st_cov_maxET.text.toString().toFloat()
                                }
                                var m_waterwidth = 0.0F
                                if (st_wt_minET.text.isNotEmpty()) {
                                    m_waterwidth = st_wt_minET.text.toString().toFloat()
                                }
                                var waterwidth = 0.0F
                                if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
                                    waterwidth = etSTRE_WATERWIDTH.text.toString().toFloat()
                                }
                                var x_waterwidth = 0.0F
                                if (st_wt_maxET.text.isNotEmpty()) {
                                    x_waterwidth = st_wt_maxET.text.toString().toFloat()
                                }
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
                                        StreDatas.get(i).M_DBH = m_dbh
                                        StreDatas.get(i).X_DBH = x_dbh
                                        StreDatas.get(i).M_TOIL = m_toil
                                        StreDatas.get(i).X_TOIL = x_toil
                                        StreDatas.get(i).M_UNDER = m_under
                                        StreDatas.get(i).X_UNDER = x_under
                                        StreDatas.get(i).M_WATER = m_waterwidth
                                        StreDatas.get(i).X_WATER = x_waterwidth

                                    }
                                }
                            }

                            var shrChk = false

                            for (i in 0 until ShrDatas.size) {
                                if (ShrDatas.get(i).PAGE == shrpage) {
                                    shrChk = true
                                }
                            }

                            if (shrChk == false) {
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
                                var m_h: Float = 0.0F

                                if (sh_het_minET.text.isNotEmpty()) {
                                    m_h = sh_het_minET.text.toString().toFloat()
                                }
                                var x_h: Float = 0.0F

                                if (sh_het_maxET.text.isNotEmpty()) {
                                    x_h = sh_het_maxET.text.toString().toFloat()
                                }

                                var cove = 0.0F

                                if (etSTR_COVEET.text.isNotEmpty()) {
                                    cove = etSTR_COVEET.text.toString().toFloat()
                                }
                                var m_cove = 0.0F

                                if (sh_cov_minET.text.isNotEmpty()) {
                                    m_cove = sh_cov_minET.text.toString().toFloat()
                                }
                                var x_cove = 0.0F

                                if (sh_cov_maxET.text.isNotEmpty()) {
                                    x_cove = sh_cov_maxET.text.toString().toFloat()
                                }

                                var m_etc = 0.0F
                                if (sh_under_minET.text.isNotEmpty()) {
                                    m_etc = sh_under_minET.text.toString().toFloat()
                                }
                                var x_etc = 0.0F
                                if (sh_under_maxET.text.isNotEmpty()) {
                                    x_etc = sh_under_maxET.text.toString().toFloat()
                                }
                                var etc = 0.0F
                                if (etSHR_UNDER.text.isNotEmpty()) {
                                    etc = etSHR_UNDER.text.toString().toFloat()
                                }

                                var shrdata = TreeData2(-1, shrpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), m_cove.toString(), cove.toString(), x_cove.toString(), m_etc.toString(), etc.toString(), x_etc.toString())

                                ShrDatas.add(shrdata)

                            } else {
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
                                var m_h: Float = 0.0F

                                if (sh_het_minET.text.isNotEmpty()) {
                                    m_h = sh_het_minET.text.toString().toFloat()
                                }
                                var x_h: Float = 0.0F

                                if (sh_het_maxET.text.isNotEmpty()) {
                                    x_h = sh_het_maxET.text.toString().toFloat()
                                }

                                var cove = 0.0F

                                if (etSTR_COVEET.text.isNotEmpty()) {
                                    cove = etSTR_COVEET.text.toString().toFloat()
                                }
                                var m_cove = 0.0F

                                if (sh_cov_minET.text.isNotEmpty()) {
                                    m_cove = sh_cov_minET.text.toString().toFloat()
                                }
                                var x_cove = 0.0F

                                if (sh_cov_maxET.text.isNotEmpty()) {
                                    x_cove = sh_cov_maxET.text.toString().toFloat()
                                }

                                var m_etc = 0.0F
                                if (sh_under_minET.text.isNotEmpty()) {
                                    m_etc = sh_under_minET.text.toString().toFloat()
                                }
                                var x_etc = 0.0F
                                if (sh_under_maxET.text.isNotEmpty()) {
                                    x_etc = sh_under_maxET.text.toString().toFloat()
                                }
                                var etc = 0.0F
                                if (etSHR_UNDER.text.isNotEmpty()) {
                                    etc = etSHR_UNDER.text.toString().toFloat()
                                }
                                for (i in 0 until ShrDatas.size) {
                                    if (ShrDatas.get(i).PAGE == shrpage) {
                                        ShrDatas.get(i).SPEC = spec
                                        ShrDatas.get(i).FAMI = fami
                                        ShrDatas.get(i).SCIEN = scien
                                        ShrDatas.get(i).H = h.toString()
                                        ShrDatas.get(i).COVE = cove.toString()
                                        ShrDatas.get(i).ETC = etc.toString()
                                        ShrDatas.get(i).M_H = m_h.toString()
                                        ShrDatas.get(i).X_H = x_h.toString()
                                        ShrDatas.get(i).M_COVE = m_cove.toString()
                                        ShrDatas.get(i).X_COVE = x_cove.toString()
                                        ShrDatas.get(i).M_ETC = m_etc.toString()
                                        ShrDatas.get(i).X_ETC = x_etc.toString()
                                    }
                                }
                            }

                            var herChk = false



                            for (i in 0 until HerDatas.size) {
                                if (HerDatas.get(i).PAGE == herpage) {
                                    herChk = true
                                }
                            }

                            if (herChk == false) {
                                var spec = etHER_SPECET.text.toString()
                                if (etHER_SPECtmp.length() > 0) {
                                    spec = etHER_SPECtmp.text.toString()
                                }
                                val fami = etHER_FAMIET.text.toString()
                                val scien = etHER_SCIENET.text.toString()
                                var m_h: Float = 0.0F

                                if (herminET.text.isNotEmpty()) {
                                    m_h = herminET.text.toString().toFloat()
                                }
                                var x_h: Float = 0.0F

                                if (hermaxET.text.isNotEmpty()) {
                                    x_h = hermaxET.text.toString().toFloat()
                                }
                                var h =  0.0f
                                if (etHER_HEIGHT.text.isNotEmpty()) {
                                    h = etHER_HEIGHT.text.toString().toFloat()
                                }
                                var etc = ""
                                if (etHER_HET.text.isNotEmpty()) {
                                    etc = etHER_HET.text.toString()
                                }

                                var cove = ""

                                if (etHER_COVEET.text.isNotEmpty()) {
                                    cove = etHER_COVEET.text.toString()
                                }

                                var herdata = TreeData3(-1, herpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), cove.toString(), etc.toString())

                                HerDatas.add(herdata)
                            } else {
                                var spec = etHER_SPECET.text.toString()
                                if (etHER_SPECtmp.length() > 0) {
                                    spec = etHER_SPECtmp.text.toString()
                                }
                                val fami = etHER_FAMIET.text.toString()
                                val scien = etHER_SCIENET.text.toString()
                                var m_h: Float = 0.0F

                                if (herminET.text.isNotEmpty()) {
                                    m_h = herminET.text.toString().toFloat()
                                }
                                var x_h: Float = 0.0F

                                if (hermaxET.text.isNotEmpty()) {
                                    x_h = hermaxET.text.toString().toFloat()
                                }
                                var h =  0.0f
                                if (etHER_HEIGHT.text.isNotEmpty()) {
                                    h = etHER_HEIGHT.text.toString().toFloat()
                                }
                                var etc = ""
                                if (etHER_HET.text.isNotEmpty()) {
                                    etc = etHER_HET.text.toString()
                                }

                                var cove = ""

                                if (etHER_COVEET.text.isNotEmpty()) {
                                    cove = etHER_COVEET.text.toString()
                                }


                                for (i in 0 until HerDatas.size) {
                                    if (HerDatas.get(i).PAGE == herpage) {
                                        HerDatas.get(i).SPEC = spec
                                        HerDatas.get(i).FAMI = fami
                                        HerDatas.get(i).SCIEN = scien
                                        HerDatas.get(i).M_H = m_h.toString()
                                        HerDatas.get(i).H = h.toString()
                                        HerDatas.get(i).X_H = x_h.toString()
                                        HerDatas.get(i).COVE = cove.toString()
                                        HerDatas.get(i).ETC = etc.toString()
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
                                manyFloraAttribute.PRJ_NAME = prjnameET.text.toString();

                                manyFloraAttribute.INV_DT = invdtTV.text.toString()

                                if (invpersonTV.text == null || invpersonTV.text.equals("")) {
                                    manyFloraAttribute.INV_PERSON = userName
                                } else {
                                    manyFloraAttribute.INV_PERSON = invpersonTV.text.toString()
                                }

                                manyFloraAttribute.INV_TM =  florainvtmET.text.toString()

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
                                        manyFloraAttribute.TRE_UNDER = 0.0f
                                        manyFloraAttribute.TRE_WATER = 0.0f
                                        manyFloraAttribute.TRE_TYPE = ""
                                        manyFloraAttribute.M_TRE_DBH = 0.0f
                                        manyFloraAttribute.X_TRE_DBH = 0.0f
                                        manyFloraAttribute.M_TRE_TOIL = 0.0f
                                        manyFloraAttribute.X_TRE_TOIL = 0.0f
                                        manyFloraAttribute.M_TRE_UDR = 0.0f
                                        manyFloraAttribute.X_TRE_UDR =  0.0f
                                        manyFloraAttribute.M_TRE_WT = 0.0f
                                        manyFloraAttribute.X_TRE_WT = 0.0f
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
                                        manyFloraAttribute.M_TRE_DBH = TreDatas.get(i).M_DBH
                                        manyFloraAttribute.X_TRE_DBH = TreDatas.get(i).X_DBH
                                        manyFloraAttribute.M_TRE_TOIL =  TreDatas.get(i).M_TOIL
                                        manyFloraAttribute.X_TRE_TOIL = TreDatas.get(i).X_TOIL
                                        manyFloraAttribute.M_TRE_UDR = TreDatas.get(i).M_UNDER
                                        manyFloraAttribute.X_TRE_UDR = TreDatas.get(i).X_UNDER
                                        manyFloraAttribute.M_TRE_WT = TreDatas.get(i).M_WATER
                                        manyFloraAttribute.X_TRE_WT =  TreDatas.get(i).X_WATER

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
                                        manyFloraAttribute.STRE_UNDER =  0.0f
                                        manyFloraAttribute.STRE_WATER = 0.0f
                                        manyFloraAttribute.STRE_TYPE = ""
                                        manyFloraAttribute.M_STR_DBH = 0.0f
                                        manyFloraAttribute.X_STR_DBH = 0.0f
                                        manyFloraAttribute.M_STR_TOIL = 0.0f
                                        manyFloraAttribute.X_STR_TOIL = 0.0f
                                        manyFloraAttribute.M_STR_UDR = 0.0f
                                        manyFloraAttribute.X_STR_UDR =  0.0f
                                        manyFloraAttribute.M_STR_WT = 0.0f
                                        manyFloraAttribute.X_STR_WT = 0.0f

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
                                        manyFloraAttribute.M_STR_DBH = StreDatas.get(i).M_DBH
                                        manyFloraAttribute.X_STR_DBH = StreDatas.get(i).X_DBH
                                        manyFloraAttribute.M_STR_TOIL =  StreDatas.get(i).M_TOIL
                                        manyFloraAttribute.X_STR_TOIL = StreDatas.get(i).X_TOIL
                                        manyFloraAttribute.M_STR_UDR = StreDatas.get(i).M_UNDER
                                        manyFloraAttribute.X_STR_UDR = StreDatas.get(i).X_UNDER
                                        manyFloraAttribute.M_STR_WT = StreDatas.get(i).M_WATER
                                        manyFloraAttribute.X_STR_WT =  StreDatas.get(i).X_WATER
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
                                        manyFloraAttribute.SHR_UNDER = 0.0f
                                        manyFloraAttribute.M_SHR_TOIL = 0.0f
                                        manyFloraAttribute.X_SHR_TOIL = 0.0f
                                        manyFloraAttribute.M_SHR_WT = 0.0f
                                        manyFloraAttribute.X_SHR_WT = 0.0f
                                        manyFloraAttribute.M_SHR_UDR = 0.0f
                                        manyFloraAttribute.X_SHR_UDR = 0.0f


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
                                        if (ShrDatas.get(i).M_H != null) {
                                            manyFloraAttribute.M_SHR_TOIL = ShrDatas.get(i).M_H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.M_SHR_TOIL = 0.0f
                                        }
                                        if (ShrDatas.get(i).X_H != null) {
                                            manyFloraAttribute.X_SHR_TOIL = ShrDatas.get(i).X_H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.X_SHR_TOIL = 0.0f
                                        }
                                        if (ShrDatas.get(i).M_COVE != null) {
                                            manyFloraAttribute.M_SHR_WT = ShrDatas.get(i).M_COVE!!.toFloat()
                                        } else {
                                            manyFloraAttribute.M_SHR_WT = 0.0f
                                        }
                                        if (ShrDatas.get(i).X_COVE != null) {
                                            manyFloraAttribute.X_SHR_WT = ShrDatas.get(i).X_COVE!!.toFloat()
                                        } else {
                                            manyFloraAttribute.X_SHR_WT = 0.0f
                                        }
                                        if (ShrDatas.get(i).M_ETC != null) {
                                            manyFloraAttribute.M_SHR_UDR = ShrDatas.get(i).M_ETC!!.toFloat()
                                        } else {
                                            manyFloraAttribute.M_SHR_UDR = 0.0f
                                        }
                                        if (ShrDatas.get(i).X_ETC != null) {
                                            manyFloraAttribute.X_SHR_UDR = ShrDatas.get(i).X_ETC!!.toFloat()
                                        } else {
                                            manyFloraAttribute.X_SHR_UDR = 0.0f
                                        }
                                        if (ShrDatas.get(i).ETC != null) {
                                            manyFloraAttribute.SHR_UNDER = ShrDatas.get(i).ETC!!.toFloat()
                                        } else {
                                            manyFloraAttribute.SHR_UNDER = 0.0f
                                        }

                                    }
                                }

                                if (HerDatas != null && HerDataSize > 0) {
                                    if (i > HerDataSize - 1) {
                                        manyFloraAttribute.HER_NUM = 1
                                        manyFloraAttribute.HER_SPEC = ""
                                        manyFloraAttribute.HER_FAMI = ""
                                        manyFloraAttribute.HER_SCIEN = ""
                                        manyFloraAttribute.HER_DOMIN = ""
                                        manyFloraAttribute.HER_GUNDO= ""
                                        manyFloraAttribute.HER_HEIGHT = 0.0f
                                        manyFloraAttribute.M_HER_HET =  0.0f
                                        manyFloraAttribute.X_HER_HET = 0.0f

                                    }
                                    if (i <= HerDataSize - 1) {
                                        manyFloraAttribute.HER_NUM = HerDatas.get(i).PAGE
                                        manyFloraAttribute.HER_SPEC = HerDatas.get(i).SPEC
                                        manyFloraAttribute.HER_FAMI = HerDatas.get(i).FAMI
                                        manyFloraAttribute.HER_SCIEN = HerDatas.get(i).SCIEN
                                        if (HerDatas.get(i).ETC != null) {
                                            manyFloraAttribute.HER_DOMIN = HerDatas.get(i).ETC
                                        }

                                        if (HerDatas.get(i).COVE != null) {
                                            manyFloraAttribute.HER_GUNDO = HerDatas.get(i).COVE
                                        }

                                        if (HerDatas.get(i).H != null && HerDatas.get(i).H != "") {
                                            manyFloraAttribute.HER_HEIGHT = HerDatas.get(i).H!!.toFloat()
                                        }
                                        if (HerDatas.get(i).M_H != null&& HerDatas.get(i).M_H != "null") {
                                            manyFloraAttribute.M_HER_HET = HerDatas.get(i).M_H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.M_HER_HET = 0.0f
                                        }
                                        if (HerDatas.get(i).X_H != null && HerDatas.get(i).X_H != "null") {
                                            manyFloraAttribute.X_HER_HET = HerDatas.get(i).X_H!!.toFloat()
                                        } else {
                                            manyFloraAttribute.X_HER_HET = 0.0f
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

        if (intent.getStringExtra("id") == null) {

            var intent = Intent()

            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

        }

        data.close()

        finish()
    }
    fun AddHerFlora() {

        var spec = etHER_SPECET.text.toString()
        if (etHER_SPECtmp.length() > 0) {
            spec = etHER_SPECtmp.text.toString()
        }
        val fami = etHER_FAMIET.text.toString()
        val scien = etHER_SCIENET.text.toString()
        var m_h: Float = 0.0F

        if (herminET.text.isNotEmpty()) {
            m_h = herminET.text.toString().toFloat()
        }
        var x_h: Float = 0.0F

        if (hermaxET.text.isNotEmpty()) {
            x_h = hermaxET.text.toString().toFloat()
        }


        var cove = ""

        if (etHER_COVEET.text.isNotEmpty()) {
            cove = etHER_COVEET.text.toString()
        }

        var h =  0.0f
        if (etHER_HEIGHT.text.isNotEmpty()) {
            h = etHER_HEIGHT.text.toString().toFloat()
        }
        var etc = ""
        if (etHER_HET.text.isNotEmpty()) {
            etc = etHER_HET.text.toString()
        }

        var chkData = false

        var equlas = false

        var division = false

        for (i in 0 until HerDatas.size) {
            if (HerDatas.get(i).PAGE == herpage) {
                division = true
            }
        }

        if (spec == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {
            val maxsize = herrightpageTV.text.toString().toInt()

            if (herpage == maxsize) {

                if (division == false) {
                    if (herpage > 1) {

                        var herdata = TreeData3(-1, herpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), cove.toString(), etc.toString())

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
                        herminET.setText("")
                        hermaxET.setText("")
                    }

                    if (herpage == 1) {

                        var herdata = TreeData3(-1, herpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), cove.toString(), etc.toString())

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
                        herminET.setText("")
                        hermaxET.setText("")
                    }
                } else {
                    if (herpage > 1) {

                        for (i in 0 until HerDatas.size) {
                            if (HerDatas.get(i).PAGE == herpage) {
                                HerDatas.get(i).SPEC = spec
                                HerDatas.get(i).FAMI = fami
                                HerDatas.get(i).SCIEN = scien
                                HerDatas.get(i).M_H = m_h.toString()
                                HerDatas.get(i).H = h.toString()
                                HerDatas.get(i).X_H = x_h.toString()
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
                        herminET.setText("")
                        hermaxET.setText("")
                    }

                    if (herpage == 1) {

                        for (i in 0 until HerDatas.size) {
                            if (HerDatas.get(i).PAGE == herpage) {
                                HerDatas.get(i).SPEC = spec
                                HerDatas.get(i).FAMI = fami
                                HerDatas.get(i).SCIEN = scien
                                HerDatas.get(i).M_H = m_h.toString()
                                HerDatas.get(i).H = h.toString()
                                HerDatas.get(i).X_H = x_h.toString()
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
                        herminET.setText("")
                        hermaxET.setText("")
                    }
                }
            }
            if (herpage < maxsize) {
                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE == herpage) {
                        HerDatas.get(i).SPEC = spec
                        HerDatas.get(i).FAMI = fami
                        HerDatas.get(i).SCIEN = scien
                        HerDatas.get(i).M_H = m_h.toString()
                        HerDatas.get(i).H = h.toString()
                        HerDatas.get(i).X_H = x_h.toString()
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
                        herminET.setText(data.M_H.toString())

                        val etc = data.ETC.toString()
                        if (etc != "" && etc != null && etc.count() > 0 && etc != "null") {
                            etHER_HET.setText(etc)
                        }

                        hermaxET.setText(data.X_H.toString())

                        var cove = data.COVE.toString();
                        if (cove.count() > 0 && cove != "" && cove != null && cove != "null") {
                            etHER_COVEET.setText(cove)
                        }

                        etHER_HEIGHT.setText(data.H.toString())
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
                    herminET.setText("")
                    hermaxET.setText("")
                }
            }

            deleteBT.visibility = View.GONE
        }

        etHER_SPECET.visibility = View.VISIBLE
        etHER_SPECLL.visibility = View.GONE
        etHER_SPECtmp.setText("")

    }

    fun AddTreFlora() {

        var spec = etTRE_SPECET.text.toString()
        if (etTRE_SPECtmp.length() > 0) {
            spec = etTRE_SPECtmp.text.toString()
        }

        val fami = etTRE_FAMIET.text.toString()
        val scien = etTRE_SCIENET.text.toString()


        var m_dbh: Float = 0.0F

        if (str_minET.text.isNotEmpty()) {
            m_dbh = str_minET.text.toString().toFloat()
        }
        var dbh: Float = 0.0F

        if (etTRE_HET.text.isNotEmpty()) {
            dbh = etTRE_HET.text.toString().toFloat()
        }
        var x_dbh: Float = 0.0F

        if (str_maxET.text.isNotEmpty()) {
            x_dbh = str_maxET.text.toString().toFloat()
        }

        var m_toil: Float = 0.0F

        if (st_br_minET.text.isNotEmpty()) {
            m_toil = st_br_minET.text.toString().toFloat()
        }

        var toil = 0.0F

        if (etTRE_BREAET.text.isNotEmpty()) {
            toil = etTRE_BREAET.text.toString().toFloat()
        }
        var x_toil: Float = 0.0F

        if (st_br_maxET.text.isNotEmpty()) {
            x_toil = st_br_maxET.text.toString().toFloat()
        }
        var m_under = 0.0F

        if (tr_cov_minET.text.isNotEmpty()) {
            m_under = tr_cov_minET.text.toString().toFloat()
        }
        var under = 0.0F
        if (etTRE_COVEET.text.isNotEmpty()) {
            under = etTRE_COVEET.text.toString().toFloat()
        }
        var x_under = 0.0F

        if (tr_cov_maxET.text.isNotEmpty()) {
            x_under = tr_cov_maxET.text.toString().toFloat()
        }
//        var under = etSTRE_COVEET.text.toString()
        var m_waterwidth = 0.0F
        if (tr_wt_minET.text.isNotEmpty()) {
            m_waterwidth = tr_wt_minET.text.toString().toFloat()
        }
        var waterwidth = 0.0F
        if (etTRE_WATERWIDTH.text.isNotEmpty()) {
            waterwidth = etTRE_WATERWIDTH.text.toString().toFloat()
        }
        var x_waterwidth = 0.0F
        if (tr_wt_maxET.text.isNotEmpty()) {
            x_waterwidth = tr_wt_maxET.text.toString().toFloat()
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

        if (spec == "" && dbh == 0.0F && toil == 0.0F && under ==0.0F && waterwidth == 0.0F && type == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {

            if (trepage == maxsize) {

                if (division == false) {

                    if (trepage > 1) {

                        var tredata = TreeData1(-1, trepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                                ,m_waterwidth, waterwidth,x_waterwidth, type)
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
                        str_minET.setText("")
                        str_maxET.setText("")
                        st_br_minET.setText("")
                        st_br_maxET.setText("")
                        tr_cov_minET.setText("")
                        tr_cov_maxET.setText("")
                        tr_wt_minET.setText("")
                        tr_wt_maxET.setText("")
                    }

                    if (trepage == 1) {

                        var tredata = TreeData1(-1, trepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                                ,m_waterwidth, waterwidth,x_waterwidth, type)

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
                        str_minET.setText("")
                        str_maxET.setText("")
                        st_br_minET.setText("")
                        st_br_maxET.setText("")
                        tr_cov_minET.setText("")
                        tr_cov_maxET.setText("")
                        tr_wt_minET.setText("")
                        tr_wt_maxET.setText("")
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
                                TreDatas.get(i).M_DBH = m_dbh
                                TreDatas.get(i).X_DBH = x_dbh
                                TreDatas.get(i).M_TOIL = m_toil
                                TreDatas.get(i).X_TOIL = x_toil
                                TreDatas.get(i).M_UNDER = m_under
                                TreDatas.get(i).X_UNDER = x_under
                                TreDatas.get(i).M_WATER = m_waterwidth
                                TreDatas.get(i).X_WATER = x_waterwidth


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
                        str_minET.setText("")
                        str_maxET.setText("")
                        st_br_minET.setText("")
                        st_br_maxET.setText("")
                        tr_cov_minET.setText("")
                        tr_cov_maxET.setText("")
                        tr_wt_minET.setText("")
                        tr_wt_maxET.setText("")
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
                                TreDatas.get(i).M_DBH = m_dbh
                                TreDatas.get(i).X_DBH = x_dbh
                                TreDatas.get(i).M_TOIL = m_toil
                                TreDatas.get(i).X_TOIL = x_toil
                                TreDatas.get(i).M_UNDER = m_under
                                TreDatas.get(i).X_UNDER = x_under
                                TreDatas.get(i).M_WATER = m_waterwidth
                                TreDatas.get(i).X_WATER = x_waterwidth
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
                        str_minET.setText("")
                        str_maxET.setText("")
                        st_br_minET.setText("")
                        st_br_maxET.setText("")
                        tr_cov_minET.setText("")
                        tr_cov_maxET.setText("")
                        tr_wt_minET.setText("")
                        tr_wt_maxET.setText("")

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
                        TreDatas.get(i).M_DBH = m_dbh
                        TreDatas.get(i).X_DBH = x_dbh
                        TreDatas.get(i).M_TOIL = m_toil
                        TreDatas.get(i).X_TOIL = x_toil
                        TreDatas.get(i).M_UNDER = m_under
                        TreDatas.get(i).X_UNDER = x_under
                        TreDatas.get(i).M_WATER = m_waterwidth
                        TreDatas.get(i).X_WATER = x_waterwidth
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
                        str_minET.setText(data.M_DBH.toString())
                        str_maxET.setText(data.X_DBH.toString())
                        st_br_minET.setText(data.M_TOIL.toString())
                        st_br_maxET.setText(data.X_TOIL.toString())
                        tr_cov_minET.setText(data.M_UNDER.toString())
                        tr_cov_maxET.setText(data.X_UNDER.toString())
                        tr_wt_minET.setText(data.M_WATER.toString())
                        tr_wt_maxET.setText(data.X_WATER.toString())
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
                    str_minET.setText("")
                    str_maxET.setText("")
                    st_br_minET.setText("")
                    st_br_maxET.setText("")
                    tr_cov_minET.setText("")
                    tr_cov_maxET.setText("")
                    tr_wt_minET.setText("")
                    tr_wt_maxET.setText("")
                }
            }

            deleteBT.visibility = View.GONE
            etTRE_SPECET.visibility = View.VISIBLE
            etTRE_SPECLL.visibility = View.GONE
            etTRE_SPECtmp.setText("")
        }
//
    }

    fun AddStreFlora() {

        var spec = etSTRE_SPECET.text.toString()
        if (etSTRE_SPECtmp.length() > 0) {
            spec = etSTRE_SPECtmp.text.toString()
        }

        val fami = etSTRE_FAMIET.text.toString()
        val scien = etSTRE_SCIENET.text.toString()
        var m_dbh: Float = 0.0F

        if (st_min_hetET.text.isNotEmpty()) {
            m_dbh = st_min_hetET.text.toString().toFloat()
        }
        var dbh: Float = 0.0F

        if (etSTRE_HET.text.isNotEmpty()) {
            dbh = etSTRE_HET.text.toString().toFloat()
        }
        var x_dbh: Float = 0.0F

        if (st_max_hetET.text.isNotEmpty()) {
            x_dbh = st_max_hetET.text.toString().toFloat()
        }

        var m_toil: Float = 0.0F

        if (st_max_hetET.text.isNotEmpty()) {
            m_toil = st_br_minET.text.toString().toFloat()
        }

        var toil = 0.0F

        if (etSTRE_BREAET.text.isNotEmpty()) {
            toil = etSTRE_BREAET.text.toString().toFloat()
        }
        var x_toil: Float = 0.0F

        if (st_br_maxET.text.isNotEmpty()) {
            x_toil = st_br_maxET.text.toString().toFloat()
        }
        var m_under = 0.0F

        if (st_cov_minET.text.isNotEmpty()) {
            m_under = st_cov_minET.text.toString().toFloat()
        }
        var under = 0.0F

        if (etSTRE_COVEET.text.isNotEmpty()) {
            under = etSTRE_COVEET.text.toString().toFloat()
        }
        var x_under = 0.0F

        if (st_cov_minET.text.isNotEmpty()) {
            x_under = st_cov_maxET.text.toString().toFloat()
        }
//        var under = etSTRE_COVEET.text.toString()
        var m_waterwidth = 0.0F
        if (st_wt_minET.text.isNotEmpty()) {
            m_waterwidth = st_wt_minET.text.toString().toFloat()
        }
        var waterwidth = 0.0F
        if (etSTRE_WATERWIDTH.text.isNotEmpty()) {
            waterwidth = etSTRE_WATERWIDTH.text.toString().toFloat()
        }
        var x_waterwidth = 0.0F
        if (st_wt_maxET.text.isNotEmpty()) {
            x_waterwidth = st_wt_maxET.text.toString().toFloat()
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

        if (spec == "" && dbh == 0.0F && toil == 0.0F && under == 0.0F && waterwidth == 0.0F && type == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {
            if (strepage == maxsize) {

                if (division == false) {

                    if (strepage > 1) {

                        var stredata = TreeData1(-1, strepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                                ,m_waterwidth, waterwidth,x_waterwidth, type)

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
                        st_min_hetET.setText("")
                        st_max_hetET.setText("")
                        st_br_min_ET.setText("")
                        st_br_max_ET.setText("")
                        st_cov_minET.setText("")
                        st_cov_maxET.setText("")
                        st_wt_minET.setText("")
                        st_wt_maxET.setText("")
                    }

                    if (strepage == 1) {

                        var stredata = TreeData1(-1, strepage, spec, fami, scien,m_dbh, dbh,x_dbh,m_toil, toil,x_toil,m_under, under,x_under
                                ,m_waterwidth, waterwidth,x_waterwidth, type)
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
                        st_min_hetET.setText("")
                        st_max_hetET.setText("")
                        st_br_min_ET.setText("")
                        st_br_max_ET.setText("")
                        st_cov_minET.setText("")
                        st_cov_maxET.setText("")
                        st_wt_minET.setText("")
                        st_wt_maxET.setText("")

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
                        st_min_hetET.setText("")
                        st_max_hetET.setText("")
                        st_br_min_ET.setText("")
                        st_br_max_ET.setText("")
                        st_cov_minET.setText("")
                        st_cov_maxET.setText("")
                        st_wt_minET.setText("")
                        st_wt_maxET.setText("")
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
                                StreDatas.get(i).M_DBH = m_dbh
                                StreDatas.get(i).X_DBH = x_dbh
                                StreDatas.get(i).M_TOIL = m_toil
                                StreDatas.get(i).X_TOIL = x_toil
                                StreDatas.get(i).M_UNDER = m_under
                                StreDatas.get(i).X_UNDER = x_under
                                StreDatas.get(i).M_WATER = m_waterwidth
                                StreDatas.get(i).X_WATER = x_waterwidth
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
                        st_min_hetET.setText("")
                        st_max_hetET.setText("")
                        st_br_min_ET.setText("")
                        st_br_max_ET.setText("")
                        st_cov_minET.setText("")
                        st_cov_maxET.setText("")
                        st_wt_minET.setText("")
                        st_wt_maxET.setText("")
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
                        StreDatas.get(i).M_DBH = m_dbh
                        StreDatas.get(i).X_DBH = x_dbh
                        StreDatas.get(i).M_TOIL = m_toil
                        StreDatas.get(i).X_TOIL = x_toil
                        StreDatas.get(i).M_UNDER = m_under
                        StreDatas.get(i).X_UNDER = x_under
                        StreDatas.get(i).M_WATER = m_waterwidth
                        StreDatas.get(i).X_WATER = x_waterwidth
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
                        st_min_hetET.setText(data.M_DBH.toString())
                        st_max_hetET.setText(data.X_DBH.toString())
                        st_br_min_ET.setText(data.M_TOIL.toString())
                        st_br_max_ET.setText(data.X_TOIL.toString())
                        st_cov_minET.setText(data.M_UNDER.toString())
                        st_cov_maxET.setText(data.X_UNDER.toString())
                        st_wt_minET.setText(data.M_WATER.toString())
                        st_wt_maxET.setText(data.X_WATER.toString())

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
                    st_min_hetET.setText("")
                    st_max_hetET.setText("")
                    st_br_min_ET.setText("")
                    st_br_max_ET.setText("")
                    st_cov_minET.setText("")
                    st_cov_maxET.setText("")
                    st_wt_minET.setText("")
                    st_wt_maxET.setText("")
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
        var m_h: Float = 0.0F

        if (sh_het_minET.text.isNotEmpty()) {
            m_h = sh_het_minET.text.toString().toFloat()
        }
        var x_h: Float = 0.0F

        if (sh_het_maxET.text.isNotEmpty()) {
            x_h = sh_het_maxET.text.toString().toFloat()
        }

        var cove = 0.0F

        if (etSTR_COVEET.text.isNotEmpty()) {
            cove = etSTR_COVEET.text.toString().toFloat()
        }
        var m_cove = 0.0F

        if (sh_cov_minET.text.isNotEmpty()) {
            m_cove = sh_cov_minET.text.toString().toFloat()
        }
        var x_cove = 0.0F

        if (sh_cov_maxET.text.isNotEmpty()) {
            x_cove = sh_cov_maxET.text.toString().toFloat()
        }

        var m_etc = 0.0F
        if (sh_under_minET.text.isNotEmpty()) {
            m_etc = sh_under_minET.text.toString().toFloat()
        }
        var x_etc = 0.0F
        if (sh_under_maxET.text.isNotEmpty()) {
            x_etc = sh_under_maxET.text.toString().toFloat()
        }
        var etc = 0.0F
        if (etSHR_UNDER.text.isNotEmpty()) {
            etc = etSHR_UNDER.text.toString().toFloat()
        }

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

        if (spec == "" && h == 0.0F && cove == 0.0F && etc == 0.0F) {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {
            if (shrpage == maxsize) {

                if (division == false) {

                    if (shrpage > 1) {

                        var shrdata = TreeData2(-1, shrpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), m_cove.toString(), cove.toString(), x_cove.toString(), m_etc.toString(), etc.toString(), x_etc.toString())

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

                        sh_het_minET.setText("")
                        sh_het_maxET.setText("")
                        sh_cov_minET.setText("")
                        sh_cov_maxET.setText("")
                        sh_under_minET.setText("")
                        sh_under_maxET.setText("")
                    }

                    if (shrpage == 1) {

                        var shrdata = TreeData2(-1, shrpage, spec, fami, scien,m_h.toString(), h.toString(),x_h.toString(), m_cove.toString(), cove.toString(), x_cove.toString(), m_etc.toString(), etc.toString(), x_etc.toString())

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
                        sh_het_minET.setText("")
                        sh_het_maxET.setText("")
                        sh_cov_minET.setText("")
                        sh_cov_maxET.setText("")
                        sh_under_minET.setText("")
                        sh_under_maxET.setText("")
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
                                ShrDatas.get(i).M_H = m_h.toString()
                                ShrDatas.get(i).X_H = x_h.toString()
                                ShrDatas.get(i).M_COVE = m_cove.toString()
                                ShrDatas.get(i).X_COVE = x_cove.toString()
                                ShrDatas.get(i).M_ETC = m_etc.toString()
                                ShrDatas.get(i).X_ETC = x_etc.toString()
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
                        sh_het_minET.setText("")
                        sh_het_maxET.setText("")
                        sh_cov_minET.setText("")
                        sh_cov_maxET.setText("")
                        sh_under_minET.setText("")
                        sh_under_maxET.setText("")
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
                                ShrDatas.get(i).M_H = m_h.toString()
                                ShrDatas.get(i).X_H = x_h.toString()
                                ShrDatas.get(i).M_COVE = m_cove.toString()
                                ShrDatas.get(i).X_COVE = x_cove.toString()
                                ShrDatas.get(i).M_ETC = m_etc.toString()
                                ShrDatas.get(i).X_ETC = x_etc.toString()

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
                        sh_het_minET.setText("")
                        sh_het_maxET.setText("")
                        sh_cov_minET.setText("")
                        sh_cov_maxET.setText("")
                        sh_under_minET.setText("")
                        sh_under_maxET.setText("")
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
                        ShrDatas.get(i).M_H = m_h.toString()
                        ShrDatas.get(i).X_H = x_h.toString()
                        ShrDatas.get(i).M_COVE = m_cove.toString()
                        ShrDatas.get(i).X_COVE = x_cove.toString()
                        ShrDatas.get(i).M_ETC = m_etc.toString()
                        ShrDatas.get(i).X_ETC = x_etc.toString()
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

                        sh_het_minET.setText(data.M_H.toString())
                        sh_het_maxET.setText(data.X_H.toString())
                        sh_cov_minET.setText(data.M_COVE.toString())
                        sh_cov_maxET.setText(data.X_COVE.toString())
                        sh_under_minET.setText(data.M_ETC.toString())
                        sh_under_maxET.setText(data.X_ETC.toString())
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
                    sh_het_minET.setText("")
                    sh_het_maxET.setText("")
                    sh_cov_minET.setText("")
                    sh_cov_maxET.setText("")
                    sh_under_minET.setText("")
                    sh_under_maxET.setText("")
                }
            }
            deleteBT.visibility = View.GONE
            etSHR_SPECET.visibility = View.VISIBLE
            etSHR_SPECLL.visibility = View.GONE
            etSHR_SPECtmp.setText("")
        }

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



    fun null_many_attribute(): ManyFloraAttribute {
        val manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null,null, null,null, null, null,null, null,null, null, null,null, null,null
                , null, null,null, null,null, null, null,null, null,null,null,null,null,null,null)
        return manyFloraAttribute
    }

    fun ps_many_attribute(data: Cursor): ManyFloraAttribute {
        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getFloat(13), data.getString(14)
                , data.getInt(15), data.getString(16), data.getString(17), data.getString(18), data.getFloat(19), data.getFloat(20), data.getFloat(21), data.getFloat(22)
                , data.getString(23), data.getInt(24), data.getString(25), data.getString(26), data.getString(27), data.getFloat(28), data.getFloat(29), data.getFloat(30), data.getInt(31), data.getString(32)
                , data.getString(33), data.getString(34), data.getString(35), data.getString(36), data.getFloat(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41)
                , data.getString(42), data.getString(43),data.getString(44),data.getString(45),data.getString(46)
                , data.getFloat(47), data.getFloat(48), data.getFloat(49), data.getFloat(50), data.getFloat(51), data.getFloat(52), data.getFloat(53)
                , data.getFloat(54), data.getFloat(55), data.getFloat(56), data.getFloat(57), data.getFloat(58), data.getFloat(59), data.getFloat(60)
                , data.getFloat(61), data.getFloat(62), data.getFloat(63), data.getFloat(64), data.getFloat(65), data.getFloat(66), data.getFloat(67)
                , data.getFloat(68), data.getFloat(69), data.getFloat(70))

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
            florainvtmET.text = msg

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
            florainvtmET.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        dialog.show()
        */
    }
}


