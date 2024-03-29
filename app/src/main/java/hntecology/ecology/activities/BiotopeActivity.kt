package hntecology.ecology.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.location.LocationManager
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
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.joooonho.SelectableRoundedImageView
import com.nostra13.universalimageloader.core.ImageLoader
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.FileFilter
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_biotope_ex.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class BiotopeActivity : Activity(), com.google.android.gms.location.LocationListener {

    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null

    var geom = ""
    private val SET_DATA = 1000
    val SET_DATA1 = 1;
    val SET_DATA2 = 2
    val SET_DATA3 = 3
    val SET_DATA4 = 4
    val SET_DATA5 = 5
    val SET_DATA6 = 6
    val SET_RATE = 7
    val SET_INPUT = 2007
    val SET_INPUT2 = 2008

    val SET_INPUT3 = 2009
    val SET_INPUT4 = 2010
    val SET_INPUT5 = 2011
    val SET_INPUT6 = 2012


    var t_name = ""
    var t_name2 = ""

    val SET_DOMIN = 133;
    val SET_DOMIN2 = 134;

    val BIOTOPE_BASE = 3000
    var keyId: String? = null;
    var pk: String? = null
    var chkdata: Boolean = false;
    var basechkdata: Boolean = false

    private val REQUEST_PERMISSION_CAMERA = 3
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2
    private var absolutePath: String? = null
    private var imageUri: Uri? = null
    private val FROM_CAMERA = 100
    private val FROM_ALBUM = 101
    private var context: Context? = null

    var images_path: ArrayList<String>? = null
    var images: ArrayList<Bitmap>? = null
    var images_url: ArrayList<String>? = null
    var images_url_remove: ArrayList<String>? = null
    var images_id: ArrayList<Int>? = null

    private var addPicturesLL: LinearLayout? = null
    private val imgSeq = 0

    var cameraPath: String? = null

    var finishFlag: Boolean = true
    //gps 다시 시작.

    //위치정보 객체
    var lm: LocationManager? = null
    //위치정보 장치 이름
    var provider: String? = null

    var page: Int? = null

    var dataArray: ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

    var GPS_LAT: String = ""
    var GPS_LON: String = ""

    var lat: String = ""
    var log: String = ""

    var polygonid: String? = null

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var landuse: String? = null

    var biotope: String? = null

    var invtm = ""

    var prjname = ""

    var INV_REGION = ""

    var lc_type = ""
    var modi_type = "N"
    var it_index = 0


    var t_trepage = 1
    var strepage = 1
    var shrpage = 1
    var herpage = 1

    var TreDatas: ArrayList<BioTreeData> = ArrayList<BioTreeData>()
    var StreDatas: ArrayList<BioTreeData3> = ArrayList<BioTreeData3>()
    var ShrDatas: ArrayList<BioTreeData2> = ArrayList<BioTreeData2>()
    var HerDatas: ArrayList<BioTreeData4> = ArrayList<BioTreeData4>()


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biotope_ex)

        this.context = this
        window.setGravity(Gravity.RIGHT);
        buildGoogleApiClient();

        images_path = ArrayList();
        images = ArrayList()
        images_url = ArrayList()

        addPicturesLL = findViewById(R.id.addPicturesLL)

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);


        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase()

        var intent: Intent = getIntent();

        var today = Utils.todayStr();
        var todays = today.split("-")

        var texttoday = todays.get(0).substring(todays.get(0).length - 2, todays.get(0).length)

        if (intent.getStringExtra("ufid") != null) {
            ufidTV.text = intent.getStringExtra("ufid")
        }


        for (i in 1 until todays.size) {
            texttoday += todays.get(i)
        }




        trecloseLL.setOnClickListener {

            etTRE_SPECET.visibility = View.VISIBLE

            if (t_trepage == 1) {
                for (i in 0 until TreDatas.size) {
                    if (TreDatas.get(i).PAGE == t_trepage) {
//                        val data = TreDatas.get(i)
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
                    if (TreDatas.get(i).PAGE == t_trepage) {
                        etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                        etTRE_FAMIET.setText(TreDatas.get(i).SPEC2)
                        etTRE_SCIENET.setText(TreDatas.get(i).SPEC3)
                        minET.setText(TreDatas.get(i).NS.toString())
                        etTRE_HET.setText(TreDatas.get(i).S.toString())
                        maxET.setText(TreDatas.get(i).MS.toString())
                        min2ET.setText(TreDatas.get(i).NS2.toString())
                        etTRE_BREAET.setText(TreDatas.get(i).S2.toString())
                        max2ET.setText(TreDatas.get(i).MS2.toString())
                        etTRE_COVEET.setText(TreDatas.get(i).PER.toString())
                        division = true
                    }
                }
                if (division == false) {
                    etTRE_SPECET.setText("")
                    etTRE_FAMIET.setText("")
                    etTRE_SCIENET.setText("")
                    minET.setText("")
                    etTRE_HET.setText("")
                    maxET.setText("")
                    min2ET.setText("")
                    etTRE_BREAET.setText("")
                    max2ET.setText("")
                    etTRE_COVEET.setText("")

                }

                val page = t_trepage
                var size = trerightpageTV.text.toString().toInt()

                trepageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    trerightpageTV.setText(size.toString())
                }
            }

            if (t_trepage > 1) {
                if (t_trepage == 2) {
                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == t_trepage) {
                            val data = TreDatas.get(i)
                            TreDatas.removeAt(i)
                            break
                        }
                    }

                    t_trepage = t_trepage - 1

                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == t_trepage) {
                            etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                            etTRE_FAMIET.setText(TreDatas.get(i).SPEC2)
                            etTRE_SCIENET.setText(TreDatas.get(i).SPEC3)
                            minET.setText(TreDatas.get(i).NS.toString())
                            etTRE_HET.setText(TreDatas.get(i).S.toString())
                            maxET.setText(TreDatas.get(i).MS.toString())
                            min2ET.setText(TreDatas.get(i).NS2.toString())
                            etTRE_BREAET.setText(TreDatas.get(i).S2.toString())
                            max2ET.setText(TreDatas.get(i).MS2.toString())
                            etTRE_COVEET.setText(TreDatas.get(i).PER.toString())
                        }

                        if (TreDatas.get(i).PAGE!! > 1) {
                            TreDatas.get(i).PAGE = TreDatas.get(i).PAGE!! - 1
                        }
                    }

                } else if (t_trepage > 2) {
                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == t_trepage) {
//                            val data = TreDatas.get(i)
                            TreDatas.removeAt(i)
                            break
                        }
                    }

                    t_trepage = t_trepage - 1

                    for (i in 0 until TreDatas.size) {
                        if (TreDatas.get(i).PAGE == t_trepage) {
                            etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                            etTRE_FAMIET.setText(TreDatas.get(i).SPEC2)
                            etTRE_SCIENET.setText(TreDatas.get(i).SPEC3)
                            minET.setText(TreDatas.get(i).NS.toString())
                            etTRE_HET.setText(TreDatas.get(i).S.toString())
                            maxET.setText(TreDatas.get(i).MS.toString())
                            min2ET.setText(TreDatas.get(i).NS2.toString())
                            etTRE_BREAET.setText(TreDatas.get(i).S2.toString())
                            max2ET.setText(TreDatas.get(i).MS2.toString())
                            etTRE_COVEET.setText(TreDatas.get(i).PER.toString())
                        }

                        if (TreDatas.get(i).PAGE!! > t_trepage) {
                            TreDatas.get(i).PAGE = TreDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = t_trepage
                val size = trerightpageTV.text.toString().toInt() - 1

                trepageTV.setText(page.toString())
                trerightpageTV.setText(size.toString())
            }

            println("delete-------------------------${TreDatas.size}")

        }

        strecloseLL.setOnClickListener {

            etSTRE_SPECET.visibility = View.VISIBLE

            if (strepage == 1) {
                for (i in 0 until StreDatas.size) {
                    if (StreDatas.get(i).PAGE == t_trepage) {
                        val data = StreDatas.get(i)
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
                        etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                        etSTRE_FAMIET.setText(StreDatas.get(i).SPEC2)
                        etSTRE_SCIENET.setText(StreDatas.get(i).SPEC3)
                        min3ET.setText(StreDatas.get(i).NS.toString())
                        etSTRE_HET.setText(StreDatas.get(i).S.toString())
                        max3ET.setText(StreDatas.get(i).MS.toString())
                        min4ET.setText(StreDatas.get(i).NS2.toString())
                        etSTRE_BREAET.setText(StreDatas.get(i).S2.toString())
                        max4ET.setText(StreDatas.get(i).MS2.toString())
                        etSTRE_COVEET.setText(StreDatas.get(i).PER.toString())
                        division = true
                    }
                }
                if (division == false) {
                    etSTRE_SPECET.setText("")
                    etSTRE_FAMIET.setText("")
                    etSTRE_SCIENET.setText("")
                    min3ET.setText("")
                    etSTRE_HET.setText("")
                    max3ET.setText("")
                    min4ET.setText("")
                    etSTRE_BREAET.setText("")
                    max4ET.setText("")
                    etSTRE_COVEET.setText("")

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
                            val data = StreDatas.get(i)
                            StreDatas.removeAt(i)
                            break
                        }
                    }

                    strepage = strepage - 1

                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == strepage) {
                            etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                            etSTRE_FAMIET.setText(StreDatas.get(i).SPEC2)
                            etSTRE_SCIENET.setText(StreDatas.get(i).SPEC3)
                            min3ET.setText(StreDatas.get(i).NS.toString())
                            etSTRE_HET.setText(StreDatas.get(i).S.toString())
                            max3ET.setText(StreDatas.get(i).MS.toString())
                            min4ET.setText(StreDatas.get(i).NS2.toString())
                            etSTRE_BREAET.setText(StreDatas.get(i).S2.toString())
                            max4ET.setText(StreDatas.get(i).MS2.toString())
                            etSTRE_COVEET.setText(StreDatas.get(i).PER.toString())
                        }

                        if (StreDatas.get(i).PAGE!! > 1) {
                            StreDatas.get(i).PAGE = StreDatas.get(i).PAGE!! - 1
                        }
                    }

                } else if (strepage > 2) {
                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == strepage) {
                            val data = StreDatas.get(i)
                            StreDatas.removeAt(i)
                            break
                        }
                    }

                    strepage = strepage - 1

                    for (i in 0 until StreDatas.size) {
                        if (StreDatas.get(i).PAGE == t_trepage) {
                            etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                            etSTRE_FAMIET.setText(StreDatas.get(i).SPEC2)
                            etSTRE_SCIENET.setText(StreDatas.get(i).SPEC3)
                            min3ET.setText(StreDatas.get(i).NS.toString())
                            etSTRE_HET.setText(StreDatas.get(i).S.toString())
                            max3ET.setText(StreDatas.get(i).MS.toString())
                            min4ET.setText(StreDatas.get(i).NS2.toString())
                            etSTRE_BREAET.setText(StreDatas.get(i).S2.toString())
                            max4ET.setText(StreDatas.get(i).MS2.toString())
                            etSTRE_COVEET.setText(StreDatas.get(i).PER.toString())
                        }

                        if (StreDatas.get(i).PAGE!! > strepage) {
                            StreDatas.get(i).PAGE = StreDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = strepage
                val size = strerightpageTV.text.toString().toInt() - 1

                strepageTV.setText(page.toString())
                strerightpageTV.setText(size.toString())
            }

            println("delete-------------------------${StreDatas.size}")

        }
        shrecloseLL.setOnClickListener {

            etSHR_SPECET.visibility = View.VISIBLE

            if (shrpage == 1) {
                for (i in 0 until ShrDatas.size) {
                    if (ShrDatas.get(i).PAGE == shrpage) {
                        val data = ShrDatas.get(i)
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
                        etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                        etSHR_FAMIET.setText(ShrDatas.get(i).SPEC2)
                        etSHR_SCIENET.setText(ShrDatas.get(i).SPEC3)
                        min5ET.setText(ShrDatas.get(i).NS.toString())
                        etSHR_HET.setText(ShrDatas.get(i).S.toString())
                        max5ET.setText(ShrDatas.get(i).MS.toString())
                        etSTR_COVEET.setText(ShrDatas.get(i).PER.toString())
                        division = true
                    }
                }
                if (division == false) {
                    clear_shr()

                }

                val page = shrpage
                var size = shrerightpageTV.text.toString().toInt()

                shrepageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    shrerightpageTV.setText(size.toString())
                }
            }

            if (shrpage > 1) {
                if (shrpage == 2) {
                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            val data = ShrDatas.get(i)
                            ShrDatas.removeAt(i)
                            break
                        }
                    }

                    shrpage = shrpage - 1

                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                            etSHR_FAMIET.setText(ShrDatas.get(i).SPEC2)
                            etSHR_SCIENET.setText(ShrDatas.get(i).SPEC3)
                            min5ET.setText(ShrDatas.get(i).NS.toString())
                            etSHR_HET.setText(ShrDatas.get(i).S.toString())
                            max5ET.setText(ShrDatas.get(i).MS.toString())
                            etSTR_COVEET.setText(ShrDatas.get(i).PER.toString())
                        }

                        if (ShrDatas.get(i).PAGE!! > 1) {
                            ShrDatas.get(i).PAGE = ShrDatas.get(i).PAGE!! - 1
                        }
                    }

                } else if (shrpage > 2) {
                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            val data = ShrDatas.get(i)
                            ShrDatas.removeAt(i)
                            break
                        }
                    }

                    shrpage = shrpage - 1

                    for (i in 0 until ShrDatas.size) {
                        if (ShrDatas.get(i).PAGE == shrpage) {
                            etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                            etSHR_FAMIET.setText(ShrDatas.get(i).SPEC2)
                            etSHR_SCIENET.setText(ShrDatas.get(i).SPEC3)
                            min5ET.setText(ShrDatas.get(i).NS.toString())
                            etSHR_HET.setText(ShrDatas.get(i).S.toString())
                            max5ET.setText(ShrDatas.get(i).MS.toString())
                            etSTR_COVEET.setText(ShrDatas.get(i).PER.toString())
                        }

                        if (ShrDatas.get(i).PAGE!! > shrpage) {
                            ShrDatas.get(i).PAGE = ShrDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = shrpage
                val size = shrerightpageTV.text.toString().toInt() - 1

                shrepageTV.setText(page.toString())
                shrerightpageTV.setText(size.toString())
            }

            println("delete-------------------------${ShrDatas.size}")

        }
        hercloseLL.setOnClickListener {

            etHER_SPECET.visibility = View.VISIBLE

            if (herpage == 1) {
                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE == herpage) {
                        val data = HerDatas.get(i)
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
                        etHER_SPECET.setText(HerDatas.get(i).SPEC)
                        etHER_FAMIET.setText(HerDatas.get(i).SPEC2)
                        etHER_SCIENET.setText(HerDatas.get(i).SPEC3)
                        min6ET.setText(HerDatas.get(i).NS.toString())
                        etHER_HET.setText(HerDatas.get(i).S.toString())
                        max6ET.setText(HerDatas.get(i).MS.toString())
                        etHER_COVEET.setText(HerDatas.get(i).PER.toString())
                        division = true
                    }
                }
                if (division == false) {
                    clear_her()

                }

                val page = herpage
                var size = herrightpageTV.text.toString().toInt()

                herleftpageTV.setText(page.toString())

                if (size > 1) {
                    size = size - 1
                    herrightpageTV.setText(size.toString())
                }
            }

            if (herpage > 1) {
                if (herpage == 2) {
                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == shrpage) {
                            val data = HerDatas.get(i)
                            HerDatas.removeAt(i)
                            break
                        }
                    }

                    herpage = herpage - 1

                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            etHER_SPECET.setText(HerDatas.get(i).SPEC)
                            etHER_FAMIET.setText(HerDatas.get(i).SPEC2)
                            etHER_SCIENET.setText(HerDatas.get(i).SPEC3)
                            min6ET.setText(HerDatas.get(i).NS.toString())
                            etHER_HET.setText(HerDatas.get(i).S.toString())
                            max6ET.setText(HerDatas.get(i).MS.toString())
                            etHER_COVEET.setText(HerDatas.get(i).PER.toString())
                        }

                        if (HerDatas.get(i).PAGE!! > 1) {
                            HerDatas.get(i).PAGE = HerDatas.get(i).PAGE!! - 1
                        }
                    }

                } else if (herpage > 2) {
                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            val data = HerDatas.get(i)
                            HerDatas.removeAt(i)
                            break
                        }
                    }

                    herpage = herpage - 1

                    for (i in 0 until HerDatas.size) {
                        if (HerDatas.get(i).PAGE == herpage) {
                            etHER_SPECET.setText(HerDatas.get(i).SPEC)
                            etHER_FAMIET.setText(HerDatas.get(i).SPEC2)
                            etHER_SCIENET.setText(HerDatas.get(i).SPEC3)
                            min6ET.setText(HerDatas.get(i).NS.toString())
                            etHER_HET.setText(HerDatas.get(i).S.toString())
                            max6ET.setText(HerDatas.get(i).MS.toString())
                            etHER_COVEET.setText(HerDatas.get(i).PER.toString())
                        }

                        if (HerDatas.get(i).PAGE!! > herpage) {
                            HerDatas.get(i).PAGE = HerDatas.get(i).PAGE!! - 1
                        }
                    }
                }

                val page = herpage
                val size = herrightpageTV.text.toString().toInt() - 1

                herleftpageTV.setText(page.toString())
                herrightpageTV.setText(size.toString())
            }

            println("delete-------------------------${HerDatas.size}")

        }












        tvINV_IndexTV.setText(texttoday + "1")


        val userName = PrefUtils.getStringPreference(context, "name");
        tvINV_PERSONTV.setText(userName)

        prjnameTV.setText(PrefUtils.getStringPreference(context, "prjname"))
        prjname = PrefUtils.getStringPreference(context, "prjname")

        etINV_DTTV.setText(Utils.todayStr())


        var time = Utils.timeStr()
        var timesplit = time.split(":")
        invtm = timesplit.get(0) + timesplit.get(1)

        etINV_TMTV.setText(time)


        it_index = intent.getIntExtra("position", 0)

        Log.d("인덱스", it_index.toString())

        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")

            println("polygonid ---------$polygonid")
        }

        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")

            println("==============$lat")
            etGPS_LATTV.setText(lat)
        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            println("==============$log")
            etGPS_LONTV.setText(log)
        }

        if (intent.getStringExtra("geom") != null) {
            geom = intent.getStringExtra("geom")
            println("---------biotopegeom $geom")
        }



        if (intent.getIntExtra("landuse", 0) != null) {
            val color = intent.getIntExtra("landuse", 0)

            landuse = color.toString()
            println("landuse : $landuse")
        }

        val dataList: Array<String> = arrayOf("*");

        if (intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")
            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.size > 0) {
                    System.out.println("list : " + list);

//                    etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        keyId = intent.getStringExtra("GROP_ID")
        println("keyid ---------------biotope $keyId")

        if (intent.getStringExtra("id") != null) {
            pk = intent.getStringExtra("id")
        }

        var basedata = db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while (basedata.moveToNext()) {

            basechkdata = true

            var base: Base = Base(basedata.getInt(0), basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5), basedata.getString(6), basedata.getString(7))

            tvINV_PERSONTV.setText(base.INV_PERSON)
            etINV_DTTV.setText(base.INV_DT)
            etINV_TMTV.setText(base.INV_TM)

            etGPS_LATTV.setText(base.GPS_LAT)
            etGPS_LONTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

        }

        if (basechkdata) {

        } else {

            val base: Base = Base(null, keyId, "", lat, log, tvINV_PERSONTV.text.toString(), etINV_DTTV.text.toString(), etINV_TMTV.text.toString())

            dbManager!!.insertbase(base)

        }


        if (intent.getStringExtra("id") != null) {


            btn_biotopDelete.visibility = View.GONE

            tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
            etINV_DTTV.setText(getTime());
            etINV_TMTV.setText(createId())

            val dataList: Array<String> = arrayOf("*");

            var data = db!!.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            val data2 = db!!.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data2.moveToNext()) {

                chkdata = true

                val biotope_attribute = ps_biotope_attribute(data2)
                etGPS_LATTV.setText(biotope_attribute.GPS_LAT.toString())
                etGPS_LONTV.setText(biotope_attribute.GPS_LON.toString())

                if (biotope_attribute.GPS_LON != 0.0 && biotope_attribute.GPS_LAT != 0.0) {
                    lat = biotope_attribute.GPS_LAT.toString()
                    log = biotope_attribute.GPS_LON.toString()
                    try {
                        var geocoder: Geocoder = Geocoder(context);

                        var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                        if (list.size > 0) {
                            System.out.println("list : " + list);

                            INV_REGION = list.get(0).getAddressLine(0)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }

                if (biotope_attribute.INV_REGION != "" && biotope_attribute.INV_REGION != null) {
                    etINV_REGIONET.setText(biotope_attribute.INV_REGION);                   // 조사지
                    INV_REGION = biotope_attribute.INV_REGION.toString()
                } else {

                }

                if (biotope_attribute.INV_PERSON != "" || biotope_attribute.INV_PERSON != null) {
                    tvINV_PERSONTV.text = biotope_attribute.INV_PERSON                    // 조사자
                } else {
                    tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))
                }

                if (biotope_attribute.INV_DT != "" && biotope_attribute.INV_DT != null) {
                    etINV_DTTV.setText(biotope_attribute.INV_DT)
                } else {
                    etINV_DTTV.setText(Utils.todayStr())
                }

                if (biotope_attribute.INV_TM != "" && biotope_attribute.INV_TM != null) {
                    etINV_TMTV.setText(biotope_attribute.INV_TM)
                } else {
                    etINV_TMTV.setText(Utils.timeStr())
                }

                if (etINV_DTTV.text == null || etINV_DTTV.text == "") {
                    etINV_DTTV.setText(Utils.todayStr())
                }


                var timesplit = biotope_attribute.INV_TM!!.split(":")
                if (timesplit.size > 1) {
                    invtm = timesplit.get(0) + timesplit.get(1)
                }

                tvINV_IndexTV.setText(biotope_attribute.INV_INDEX.toString())

                if (biotope_attribute.PRJ_NAME != null && biotope_attribute.PRJ_NAME != "") {
                    prjnameTV.setText(biotope_attribute.PRJ_NAME)
                } else {
                    prjnameTV.setText(PrefUtils.getStringPreference(context, "prjname"))
                }

                if (biotope_attribute.LU_GR_NUM == "null") {
                    TVLU_GR_NumTV.setText("")
                } else {
                    TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
                }

                etLU_TY_RATEET.setText(biotope_attribute.LU_TY_RATE.toString())
                etSTAND_HET.setText(biotope_attribute.STAND_H.toString())

                if (biotope_attribute.LU_GR_NUM != null && biotope_attribute.LU_GR_NUM != "") {

                    var text = biotope_attribute.LU_GR_NUM!!.split("(")

                    println("text ------ $text")

                    val data = db!!.query("biotopeM", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                    while (data.moveToNext()) {
                        TVLU_GR_NumTV.setText(data.getString(1) + "(" + data.getString(0) + ")")
                        if (TVLU_GR_NumTV.text == "null") {
                            TVLU_GR_NumTV.setText("")
                        }
                        ETLU_GR_NumET.setText(data.getString(1) + "(" + data.getString(0) + ")")
                        if (ETLU_GR_NumET.text == null) {
                            ETLU_GR_NumET.setText("")
                        }

                    }
                }

                if (biotope_attribute.LC_GR_NUM != null && biotope_attribute.LC_GR_NUM != "") {

                    var text = biotope_attribute.LC_GR_NUM!!.split("(")

                    val data = db!!.query("biotopeS", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                    while (data.moveToNext()) {

                        TVLC_GR_NUMTV.setText(data.getString(1) + "(" + data.getString(0) + ")")
                        if (TVLC_GR_NUMTV.text == null) {
                            TVLC_GR_NUMTV.setText("")
                        }
                        ETlcmGR_NumET.setText(data.getString(1) + "(" + data.getString(0) + ")")
                        if (ETlcmGR_NumET.text == null) {
                            ETlcmGR_NumET.setText("")
                        }
                    }

                }

                load_biotope(biotope_attribute)


                val tmpfiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
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
                            if (images_path!!.get(i).equals(FileFilter.img(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator, add_images[add_images.size - 1]))) {
                                //                            if (images_path!!.get(i).equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" + biotope_attribute.NUM.toString() +"_"+biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {
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


            if (data.count < 1) {
                tvINV_PERSONTV.setText(PrefUtils.getStringPreference(this, "name"))                    // 조사자
                etINV_DTTV.setText(getTime());
                etINV_TMTV.setText(createId())
                tvPIC_FOLDERTV.visibility = View.GONE;
            }

        }
        if (intent.getStringExtra("GROP_ID") != null) {

            var AllDatas: ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

            val grop_id = intent.getStringExtra("GROP_ID")


            val dataList: Array<String> = arrayOf("*");

            val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "id asc", null)

            while (data2.moveToNext()) {

                val biotope_attribute = ps_biotope_attribute(data2)
                // 교목층

                etINV_REGIONET.setText(biotope_attribute.INV_REGION)
                INV_REGION = biotope_attribute.INV_REGION.toString()
                etINV_DTTV.setText(biotope_attribute.INV_DT)
                tvINV_PERSONTV.setText(biotope_attribute.INV_PERSON)

                var dataPk = biotope_attribute.id!!.toInt()
                val TRE_NUM = biotope_attribute.TRE_NUM
                var SPEC = biotope_attribute.TRE_SPEC
                var SPEC2 = biotope_attribute.TRE_FAMI
                var SPEC3 = biotope_attribute.TRE_SCIEN
                var NS = biotope_attribute.TRE_H_N
                var S = biotope_attribute.TRE_H
                var MS = biotope_attribute.TRE_H_X
                var NS2 = biotope_attribute.TRE_BREA_N
                var S2 = biotope_attribute.TRE_BREA
                var MS2 = biotope_attribute.TRE_BREA_X
                var PER = biotope_attribute.TRE_COVE


                if (SPEC!!.length > 0 && SPEC2 != "" && SPEC3 != "null") {
                    val data = BioTreeData(dataPk, TRE_NUM, SPEC, SPEC2, SPEC3, NS, S, MS, NS2, S2, MS2, PER)
                    TreDatas.add(data)
                    println("TRE_NUM ADD ${data.PAGE}")
                    t_trepage = biotope_attribute.TRE_NUM!!
                    println("스스 ${t_trepage}")

                    if (SPEC != "null") {
                        etTRE_SPECET.setText(SPEC)
                        etTRE_FAMIET.setText(SPEC2)
                        etTRE_SCIENET.setText(SPEC3)
                    }

                    minET.setText(biotope_attribute.TRE_H_N.toString())
                    etTRE_HET.setText(biotope_attribute.TRE_H.toString())
                    maxET.setText(biotope_attribute.TRE_H_X.toString())
                    min2ET.setText(biotope_attribute.TRE_BREA_N.toString())
                    etTRE_BREAET.setText(biotope_attribute.TRE_BREA.toString())
                    max2ET.setText(biotope_attribute.TRE_BREA_X.toString())
                    etTRE_COVEET.setText(biotope_attribute.TRE_COVE.toString())
                }
                // 아교목층
                val STRE_NUM = biotope_attribute.STRE_NUM
                val STRE_SPEC = biotope_attribute.STRE_SPEC
                val STRE_FAMI = biotope_attribute.STRE_FAMI
                val STRE_SCIEN = biotope_attribute.STRE_SCIEN
                val STRE_H_N = biotope_attribute.STRE_H_N
                val STRE_H_X = biotope_attribute.STRE_H_X
                val STRE_H = biotope_attribute.STRE_H
                val STRE_BRT_N = biotope_attribute.STRE_BRT_N
                val STRE_BRT_X = biotope_attribute.STRE_BRT_X
                val STRE_BRT = biotope_attribute.STRE_BRT
                var PER2 = biotope_attribute.STRE_COVE
                println("STRE_NUM $STRE_NUM STRE_SPEC $STRE_SPEC STRE_FAMI $STRE_FAMI STRE_SCIEN $STRE_SCIEN")
                if (STRE_SPEC!!.length > 0 && STRE_SPEC != "" && STRE_SPEC != "null") {
                    val data = BioTreeData3(dataPk, STRE_NUM, STRE_SPEC, STRE_FAMI, STRE_SCIEN, STRE_H_N, STRE_H, STRE_H_X, STRE_BRT_N, STRE_BRT, STRE_BRT_X, PER2)
                    StreDatas.add(data)
                    strepage = biotope_attribute.STRE_NUM!!
                    if (biotope_attribute.STRE_SPEC.toString() != "null") {
                        etSTRE_SPECET.setText(biotope_attribute.STRE_SPEC.toString())
                        etSTRE_FAMIET.setText(biotope_attribute.STRE_FAMI.toString())
                        etSTRE_SCIENET.setText(biotope_attribute.STRE_SCIEN.toString())
                    }

                    min3ET.setText(biotope_attribute.STRE_H_N.toString())
                    etSTRE_HET.setText(biotope_attribute.STRE_H.toString())
                    max3ET.setText(biotope_attribute.STRE_H_X.toString())
                    min4ET.setText(biotope_attribute.STRE_BRT_N.toString())
                    etSTRE_BREAET.setText(biotope_attribute.STRE_BRT.toString())
                    max4ET.setText(biotope_attribute.STRE_BRT_X.toString())
                    etSTRE_COVEET.setText(biotope_attribute.STRE_COVE.toString())

                }

                val SHR_NUM = biotope_attribute.SHR_NUM
                val SHR_SPEC = biotope_attribute.SHR_SPEC
                val SHR_FAMI = biotope_attribute.SHR_FAMI
                val SHR_SCIEN = biotope_attribute.SHR_SCIEN
                val SHR_HET_N = biotope_attribute.SHR_HET_N
                val SHR_HET_X = biotope_attribute.SHR_HET_X
                val SHR_H = biotope_attribute.SHR_H
                var PER3 = biotope_attribute.STR_COVE

                if (SHR_SPEC!!.length > 0 && SHR_SPEC != "" && SHR_SPEC != "null") {
                    val data = BioTreeData2(dataPk, SHR_NUM, SHR_SPEC, SHR_FAMI, SHR_SCIEN, SHR_HET_N, SHR_H, SHR_HET_X, PER3)
                    ShrDatas.add(data)
                    shrpage = biotope_attribute.SHR_NUM!!
                    if (biotope_attribute.SHR_SPEC.toString() != "null") {
                        etSHR_SPECET.setText(biotope_attribute.SHR_SPEC)
                        etSHR_FAMIET.setText(biotope_attribute.SHR_FAMI)
                        etSHR_SCIENET.setText(biotope_attribute.SHR_SCIEN)
                    }

                    min5ET.setText(biotope_attribute.SHR_HET_N.toString())
                    etSHR_HET.setText(biotope_attribute.SHR_H.toString())
                    max5ET.setText(biotope_attribute.SHR_HET_X.toString())
                    etSTR_COVEET.setText(biotope_attribute.STR_COVE.toString())

                }


                val HER_NUM = biotope_attribute.HER_NUM
                val HER_SPEC = biotope_attribute.HER_SPEC
                val HER_FAMI = biotope_attribute.HER_FAMI
                val HER_SCIEN = biotope_attribute.HER_SCIEN
                val HER_HET_N = biotope_attribute.HER_HET_N
                val HER_HET_X = biotope_attribute.HER_HET_X
                val HER_H = biotope_attribute.HER_H
                var PER4 = biotope_attribute.HER_COVE

                if (HER_SPEC!!.length > 0 && HER_SPEC != "" && HER_SPEC != "null") {
                    val data = BioTreeData4(dataPk, HER_NUM, HER_SPEC, HER_FAMI, HER_SCIEN, HER_HET_N, HER_H, HER_HET_X, PER4)
                    HerDatas.add(data)
                    herpage = biotope_attribute.HER_NUM!!
                    if (biotope_attribute.HER_SPEC.toString() != "null") {
                        etHER_SPECET.setText(biotope_attribute.HER_SPEC)
                        etHER_FAMIET.setText(biotope_attribute.HER_FAMI)
                        etHER_SCIENET.setText(biotope_attribute.HER_SCIEN)
                    }
                    min6ET.setText(biotope_attribute.HER_HET_N.toString())
                    etHER_HET.setText(biotope_attribute.HER_H.toString())
                    max6ET.setText(biotope_attribute.HER_HET_X.toString())
                    etHER_COVEET.setText(biotope_attribute.HER_COVE.toString())

                }

                AllDatas.add(biotope_attribute)

            }

            var treDataSize = TreDatas.size + 1
            var streDataSize = StreDatas.size + 1
            var shrDataSize = ShrDatas.size + 1
            var herDataSize = HerDatas.size + 1

            if (TreDatas.size > 0) {
                t_trepage = TreDatas.size
            }
            if (StreDatas.size > 0) {
                strepage = StreDatas.size
            }
            if (ShrDatas.size > 0) {
                shrpage = ShrDatas.size
            }
            if (HerDatas.size > 0) {
                herpage = HerDatas.size
            }


            trepageTV.setText(t_trepage.toString())
            trerightpageTV.setText(t_trepage.toString())

            strepageTV.setText(strepage.toString())
            strerightpageTV.setText(strepage.toString())

            shrepageTV.setText(shrpage.toString())
            shrerightpageTV.setText(shrpage.toString())

            herleftpageTV.setText(herpage.toString())
            herrightpageTV.setText(herpage.toString())

            for (i in 0 until TreDatas.size) {
                println("TER_NUM ${TreDatas.get(i).PAGE} TER_SPEC ${TreDatas.get(i).SPEC}")
            }

            for (i in 0..StreDatas.size - 1) {
                println("STER_NUM ${StreDatas.get(i).PAGE} STER_SPEC ${StreDatas.get(i).SPEC}")
            }

            for (i in 0..ShrDatas.size - 1) {
                println("SHR_NUM ${ShrDatas.get(i).PAGE} SHR_SPEC ${ShrDatas.get(i).SPEC}")
            }
            for (i in 0..HerDatas.size - 1) {
                println("HER_NUM ${HerDatas.get(i).PAGE} HER_SPEC ${HerDatas.get(i).SPEC}")
            }

            data2.close()
        }




        if (intent.getSerializableExtra("biotopedata") != null) {

            var biotope_attribute = intent.getSerializableExtra("biotopedata") as Biotope_attribute

            println("biotope_attribute ${biotope_attribute.GPS_LON}")
            println("biotope_attribute ${biotope_attribute.LU_TY_RATE}")
            println("biotope_attribute ${biotope_attribute.STAND_H}")
            println("biotope_attribute ${biotope_attribute.IMPERV}")
            println("biotope_attribute ${biotope_attribute.LC_TY}")
            println("biotope_attribute333333333333333 ${biotope_attribute.LC_GR_NUM}")
            println("biotope_attribute4444444444444 ${biotope_attribute.TY_MARK}")
            val dbManager: DataBaseHelper = DataBaseHelper(this)


            if (biotope_attribute.TY_MARK != null &&biotope_attribute.TY_MARK !=""){
                val data = db!!.query("biotopeClass", dataList, "SIGN = '" + biotope_attribute.TY_MARK + "'", null, null, null, "", null);

                println("---------------33333333-------$data")
                while (data.moveToNext()) {
                    TVTY_MARKTV.setText(data.getString(3) + "(" + data.getString(2) + ")")
                    if (TVTY_MARKTV.text == null) {
                        TVTY_MARKTV.setText("")
                    }
                }
            }



            if (biotope_attribute.GPS_LON != 0.0 && biotope_attribute.GPS_LAT != 0.0) {
                lat = biotope_attribute.GPS_LAT.toString()
                log = biotope_attribute.GPS_LON.toString()
                try {
                    var geocoder: Geocoder = Geocoder(context);

                    var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                    if (list.size > 0) {
                        System.out.println("list : " + list);

//                        etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                        INV_REGION = list.get(0).getAddressLine(0)
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }

            val db = dbManager.createDataBase()

            val dataList: Array<String> = arrayOf("*");

            etGPS_LATTV.setText(biotope_attribute.GPS_LAT.toString())
            etGPS_LONTV.setText(biotope_attribute.GPS_LON.toString())

            if (intent.getStringExtra("latitude") != null) {
                lat = intent.getStringExtra("latitude")

                println("==============$lat")
                etGPS_LATTV.setText(lat)
            }

            if (intent.getStringExtra("longitude") != null) {
                log = intent.getStringExtra("longitude")
                println("==============$log")
                etGPS_LONTV.setText(log)
            }

            if (intent.getStringExtra("EMD_NM") != null) {
                val EMD_NM = intent.getStringExtra("EMD_NM")

                if (EMD_NM != "" && EMD_NM != null) {
                    // etINV_REGIONET.setText(EMD_NM);
                }
            }

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if (list.isNotEmpty()) {
                    System.out.println("list : " + list);

                    etINV_REGIONET.setText(list.get(0).getAddressLine(0));
                    INV_REGION = list.get(0).getAddressLine(0)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

            etINV_DTTV.setText(biotope_attribute.INV_DT)
            etINV_TMTV.setText(biotope_attribute.INV_TM)

            if (etINV_DTTV.text == null || etINV_DTTV.text == "") {
                etINV_DTTV.setText(Utils.todayStr())
            }

            if (etINV_TMTV.text == null || etINV_TMTV.text == "") {
                etINV_TMTV.setText(Utils.timeStr())
            }
            tvINV_IndexTV.setText(biotope_attribute.INV_INDEX.toString())

            TVLU_GR_NumTV.setText(biotope_attribute.LU_GR_NUM)
            if (TVLU_GR_NumTV.text == "null") {
                TVLU_GR_NumTV.setText("")
            }

            etLU_TY_RATEET.setText(biotope_attribute.LU_TY_RATE.toString())
            etSTAND_HET.setText(biotope_attribute.STAND_H.toString())
            TVLC_GR_NUMTV.setText(biotope_attribute.LC_GR_NUM)
            if (TVLC_GR_NUMTV.text == null) {
                TVLC_GR_NUMTV.setText("")
            }

            if (biotope_attribute.LU_GR_NUM != null && biotope_attribute.LU_GR_NUM != "") {


                var text = biotope_attribute.LU_GR_NUM!!.split("(")

                println("text ------ $text")

                val data = db!!.query("biotopeM", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                while (data.moveToNext()) {
                    TVLU_GR_NumTV.setText(data.getString(1) + "(" + data.getString(0) + ")")
                    if (TVLU_GR_NumTV.text == "null") {
                        TVLU_GR_NumTV.setText("")
                    }
                    ETLU_GR_NumET.setText(data.getString(1) + "(" + data.getString(0) + ")")
                    if (ETLU_GR_NumET.text == null) {
                        ETLU_GR_NumET.setText("")
                    }

                }
            }

            if (biotope_attribute.LC_GR_NUM != null && biotope_attribute.LC_GR_NUM != "") {


                var text = biotope_attribute.LC_GR_NUM!!.split("(")

                val data = db!!.query("biotopeS", dataList, "code = '" + text.get(0) + "'", null, null, null, "", null);

                while (data.moveToNext()) {

                    TVLC_GR_NUMTV.setText(data.getString(1) + "(" + data.getString(0) + ")")
                    if (TVLC_GR_NUMTV.text == null) {
                        TVLC_GR_NUMTV.setText("")
                    }
                    ETlcmGR_NumET.setText(data.getString(1) + "(" + data.getString(0) + ")")
                    if (ETlcmGR_NumET.text == null) {
                        ETlcmGR_NumET.setText("")
                    }
                }
            }

            checkTV.setText(biotope_attribute.CHECK.toString())


        }

        click()

    }

    fun click() {
        val userName = PrefUtils.getStringPreference(context, "name");
        dominTV.setOnClickListener {
            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "우점")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DOMIN);
        }

        ausTV.setOnClickListener {
            if (dominTV.text.equals("")) {
                Toast.makeText(context, "우점을 먼저 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DlgVascularActivity::class.java)
            intent.putExtra("title", "아우점")
            intent.putExtra("table", "vascular_plant")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DOMIN2);
        }



        pRL.setOnClickListener {
            set_menu()
            lcCB.setImageResource(R.mipmap.box_check_on)
            lc_type = "P"
        }

        iRL.setOnClickListener {
            set_menu()
            lc2CB.setImageResource(R.mipmap.box_check_on)
            lc_type = "I"
        }

        gRL.setOnClickListener {
            set_menu()
            lc3CB.setImageResource(R.mipmap.box_check_on)
            lc_type = "G"
        }

        wRL.setOnClickListener {
            set_menu()
            lc4CB.setImageResource(R.mipmap.box_check_on)
            lc_type = "W"
        }

        etINV_TMTV.setOnClickListener {
            timedlg()
        }
        etINV_DTTV.setOnClickListener {
            datedlg()
        }


        bioBT.setOnClickListener {
            val intent = Intent(this, DlgBiotopeTypeActivity::class.java)
            intent.putExtra("title", "비오톱유형 분류")
            intent.putExtra("table", "biotopeType")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA);
        }

        //토지이용현황 분류 버튼  높이 450f
        btn_Dlg1.setOnClickListener {

            val intent = Intent(this, DlgCommonActivity::class.java)
            intent.putExtra("title", "토지이용유형 분류기준")
            intent.putExtra("table", "biotopeM")
            intent.putExtra("DlgHeight", 450f);
//            startActivity(intent)
            startActivityForResult(intent, SET_DATA1);

        }
        //토지피복현황 분류 버튼 사이즈 높이 600f 줄 것.
        btn_Dlg2.setOnClickListener {

            val intent = Intent(this, DlgCommonActivity::class.java)
            intent.putExtra("title", "토지피복현황 분류기준")
            intent.putExtra("table", "biotopeS")
            intent.putExtra("DlgHeight", 600f);
            startActivityForResult(intent, SET_DATA1);

        }

        //현존식생현황 분류 버튼
        btn_Dlg3.setOnClickListener {

            val intent = Intent(this, DlgBiotopeClassActivity::class.java)
            intent.putExtra("title", "현존식생현황 분류기준")
            intent.putExtra("table", "biotopeClass")
            intent.putExtra("DlgHeight", 600f);

            startActivityForResult(intent, SET_DATA6);

        }

        etTRE_SPECETreset.setOnClickListener {
            etTRE_SPECETLL.visibility = View.GONE
            etTRE_SPECETtmp.setText("")
            etTRE_SPECET.visibility = View.VISIBLE
        }

        etSTRE_SPECETreset.setOnClickListener {
            etSTRE_SPECETLL.visibility = View.GONE
            etSTRE_SPECETtmp.setText("")
            etSTRE_SPECET.visibility = View.VISIBLE
        }

        etSHR_SPECETreset.setOnClickListener {
            etSHR_SPECETLL.visibility = View.GONE
            etSHR_SPECETtmp.setText("")
            etSHR_SPECET.visibility = View.VISIBLE
        }

        etHER_SPECETreset.setOnClickListener {
            etHER_SPECETLL.visibility = View.GONE
            etHER_SPECETtmp.setText("")
            etHER_SPECET.visibility = View.VISIBLE
        }

        btn_biotopCancle1.setOnClickListener {

            if (intent.getSerializableExtra("biotopedata") == null) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            val dbManager: DataBaseHelper = DataBaseHelper(this)

                            val db = dbManager.createDataBase()

                            val dataList: Array<String> = arrayOf("*");

                            val data2 = db.query("biotopeAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                            var dataArray: ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

                            while (data2.moveToNext()) {

                                val biotope_attribute = ps_biotope_attribute(data2)

                                dataArray.add(biotope_attribute)

                            }

                            if (dataArray.size == 0) {
                                var intent = Intent()
                                intent.putExtra("polygonid", polygonid)

                                setResult(RESULT_OK, intent);

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
                                val pathdir = path.listFiles()

                                if (pathdir != null) {

                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
                                        path.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
                                        path.deleteRecursively()
                                    }
                                }
                            }

                            finish()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            } else {
                finish()
            }
        }

        //sqlite 저장.
        btn_biotopSave1.setOnClickListener {

            if (TreDatas.size > 0 || StreDatas.size > 0 || ShrDatas.size > 0 || HerDatas.size > 0) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            var biotope_attribute = null_biotope_attribute()
                            dbManager!!.deletegrop_biotope(keyId)

//                            addbiotope2(biotope_attribute)

                            var treChk = false

                            for (i in 0 until TreDatas.size) {
                                println("끔찍-------------------- " + TreDatas.size.toString())

                                if (TreDatas.get(i).PAGE == t_trepage) {
                                    println("끔찍-------------------- $t_trepage")
                                    treChk = true
                                }
                            }
                            println("끔찍-------------------- $treChk")
                            if (treChk == false) {
                                var spec = etTRE_SPECET.text.toString()
                                println("끔찍-------------------- $spec")
                                val fami = etTRE_FAMIET.text.toString()
                                val scien = etTRE_SCIENET.text.toString()

                                var NS: Float = 0.0F

                                if (minET.text.isNotEmpty()) {
                                    NS = minET.text.toString().toFloat()
                                }
                                var S: Float = 0.0F

                                if (etTRE_HET.text.isNotEmpty()) {
                                    S = etTRE_HET.text.toString().toFloat()
                                }
                                var MS: Float = 0.0F

                                if (maxET.text.isNotEmpty()) {
                                    MS = maxET.text.toString().toFloat()
                                }

                                var NS2: Float = 0.0F

                                if (min2ET.text.isNotEmpty()) {
                                    NS2 = min2ET.text.toString().toFloat()
                                }

                                var S2: Float = 0.0F

                                if (etTRE_BREAET.text.isNotEmpty()) {
                                    S2 = etTRE_BREAET.text.toString().toFloat()
                                }
                                var MS2: Float = 0.0F
                                if (max2ET.text.isNotEmpty()) {
                                    MS2 = max2ET.text.toString().toFloat()
                                }

                                var PER: Float = 0.0F
                                if (etTRE_COVEET.text.isNotEmpty()) {
                                    PER = etTRE_COVEET.text.toString().toFloat()
                                }
                                var tredata = BioTreeData(-1, t_trepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                                TreDatas.add(tredata)
                            } else {
                                for (i in 0 until TreDatas.size) {
                                    println("페이지" + TreDatas.get(i).PAGE + TreDatas.get(i).SPEC)
                                    if (TreDatas.get(i).PAGE == t_trepage) {
                                        var spec = etTRE_SPECET.text.toString()
                                        val fami = etTRE_FAMIET.text.toString()
                                        val scien = etTRE_SCIENET.text.toString()
                                        var NS: Float = 0.0F
                                        if (minET.text.isNotEmpty()) {
                                            NS = minET.text.toString().toFloat()
                                        }
                                        var S: Float = 0.0F

                                        if (etTRE_HET.text.isNotEmpty()) {
                                            S = etTRE_HET.text.toString().toFloat()
                                        }
                                        var MS: Float = 0.0F

                                        if (maxET.text.isNotEmpty()) {
                                            MS = maxET.text.toString().toFloat()
                                        }

                                        var NS2: Float = 0.0F

                                        if (min2ET.text.isNotEmpty()) {
                                            NS2 = min2ET.text.toString().toFloat()
                                        }

                                        var S2: Float = 0.0F

                                        if (etTRE_BREAET.text.isNotEmpty()) {
                                            S2 = etTRE_BREAET.text.toString().toFloat()
                                        }
                                        var MS2: Float = 0.0F
                                        if (max2ET.text.isNotEmpty()) {
                                            MS2 = max2ET.text.toString().toFloat()
                                        }

                                        var PER: Float = 0.0F
                                        if (etTRE_COVEET.text.isNotEmpty()) {
                                            PER = etTRE_COVEET.text.toString().toFloat()
                                        }
                                        TreDatas.get(i).SPEC = spec
                                        TreDatas.get(i).SPEC2 = fami
                                        TreDatas.get(i).SPEC3 = scien
                                        TreDatas.get(i).NS = NS
                                        TreDatas.get(i).S = S
                                        TreDatas.get(i).MS = MS
                                        TreDatas.get(i).NS2 = NS2
                                        TreDatas.get(i).S2 = S2
                                        TreDatas.get(i).MS2 = MS2
                                        TreDatas.get(i).PER = PER
                                        Log.d("찌즈", spec.toString())
                                    }
                                }
                            }


                            println("delete-------------------- $keyId")
//                            println("삭제5 $pk =============================================")
//                            dbManager!!.deleteallbiotope_attribute(biotope_attribute, keyId + it_index)

                            var streChk = false

                            for (i in 0 until StreDatas.size) {
                                println("끔찍2-------------------- " + StreDatas.size.toString())
                                if (StreDatas.get(i).PAGE == strepage) {
                                    println("끔찍2-------------------- " + strepage)
                                    streChk = true
                                }
                                println("끔찍2-------------------- " + streChk)
                            }

                            if (streChk == false) {

                                var spec = etSTRE_SPECET.text.toString()

                                val fami = etSTRE_FAMIET.text.toString()
                                val scien = etSTRE_SCIENET.text.toString()

                                var NS: Float = 0.0F

                                if (min3ET.text.isNotEmpty()) {
                                    NS = min3ET.text.toString().toFloat()
                                }
                                var S: Float = 0.0F

                                if (etSTRE_HET.text.isNotEmpty()) {
                                    S = etSTRE_HET.text.toString().toFloat()
                                }
                                var MS: Float = 0.0F

                                if (max3ET.text.isNotEmpty()) {
                                    MS = max3ET.text.toString().toFloat()
                                }

                                var NS2: Float = 0.0F

                                if (min4ET.text.isNotEmpty()) {
                                    NS2 = min4ET.text.toString().toFloat()
                                }

                                var S2: Float = 0.0F

                                if (etSTRE_BREAET.text.isNotEmpty()) {
                                    S2 = etSTRE_BREAET.text.toString().toFloat()
                                }
                                var MS2: Float = 0.0F
                                if (max4ET.text.isNotEmpty()) {
                                    MS2 = max4ET.text.toString().toFloat()
                                }

                                var PER: Float = 0.0F
                                if (etSTRE_COVEET.text.isNotEmpty()) {
                                    PER = etSTRE_COVEET.text.toString().toFloat()
                                }


                                var stredata = BioTreeData3(-1, strepageTV.text.toString().toInt(), spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                                StreDatas.add(stredata)
                            } else {
                                for (i in 0 until StreDatas.size) {

                                    if (StreDatas.get(i).PAGE == strepage) {
                                        var spec = etSTRE_SPECET.text.toString()

                                        val fami = etSTRE_FAMIET.text.toString()
                                        val scien = etSTRE_SCIENET.text.toString()

                                        var NS: Float = 0.0F

                                        if (min3ET.text.isNotEmpty()) {
                                            NS = min3ET.text.toString().toFloat()
                                        }
                                        var S: Float = 0.0F

                                        if (etSTRE_HET.text.isNotEmpty()) {
                                            S = etSTRE_HET.text.toString().toFloat()
                                        }
                                        var MS: Float = 0.0F

                                        if (max3ET.text.isNotEmpty()) {
                                            MS = max3ET.text.toString().toFloat()
                                        }

                                        var NS2: Float = 0.0F

                                        if (min4ET.text.isNotEmpty()) {
                                            NS2 = min4ET.text.toString().toFloat()
                                        }

                                        var S2: Float = 0.0F

                                        if (etSTRE_BREAET.text.isNotEmpty()) {
                                            S2 = etSTRE_BREAET.text.toString().toFloat()
                                        }
                                        var MS2: Float = 0.0F
                                        if (max4ET.text.isNotEmpty()) {
                                            MS2 = max4ET.text.toString().toFloat()
                                        }

                                        var PER: Float = 0.0F
                                        if (etSTRE_COVEET.text.isNotEmpty()) {
                                            PER = etSTRE_COVEET.text.toString().toFloat()
                                        }
                                        StreDatas.get(i).SPEC = spec
                                        StreDatas.get(i).SPEC2 = fami
                                        StreDatas.get(i).SPEC3 = scien
                                        StreDatas.get(i).NS = NS
                                        StreDatas.get(i).S = S
                                        StreDatas.get(i).MS = MS
                                        StreDatas.get(i).NS2 = NS2
                                        StreDatas.get(i).S2 = S2
                                        StreDatas.get(i).MS2 = MS2
                                        StreDatas.get(i).PER = PER
                                        Log.d("찌즈", spec.toString())
                                    }
                                }
                            }

                            var shrchk = false

                            for (i in 0 until ShrDatas.size) {
                                if (ShrDatas.get(i).PAGE == shrpage) {
                                    shrchk = true
                                }
                            }

                            if (shrchk == false) {

                                var spec = etSHR_SPECET.text.toString()

                                val fami = etSHR_FAMIET.text.toString()
                                val scien = etSHR_SCIENET.text.toString()

                                var NS: Float = 0.0F

                                if (min5ET.text.isNotEmpty()) {
                                    NS = min5ET.text.toString().toFloat()
                                }
                                var S: Float = 0.0F

                                if (etSHR_HET.text.isNotEmpty()) {
                                    S = etSHR_HET.text.toString().toFloat()
                                }
                                var MS: Float = 0.0F

                                if (max5ET.text.isNotEmpty()) {
                                    MS = max5ET.text.toString().toFloat()
                                }

                                var PER: Float = 0.0F
                                if (etSTR_COVEET.text.isNotEmpty()) {
                                    PER = etSTR_COVEET.text.toString().toFloat()
                                }

                                var shrdata = BioTreeData2(-1, shrepageTV.text.toString().toInt(), spec, fami, scien, NS, S, MS, PER)

                                ShrDatas.add(shrdata)
                            } else {
                                for (i in 0 until ShrDatas.size) {
                                    var spec = etSHR_SPECET.text.toString()

                                    val fami = etSHR_FAMIET.text.toString()
                                    val scien = etSHR_SCIENET.text.toString()

                                    var NS: Float = 0.0F

                                    if (min5ET.text.isNotEmpty()) {
                                        NS = min5ET.text.toString().toFloat()
                                    }
                                    var S: Float = 0.0F

                                    if (etSHR_HET.text.isNotEmpty()) {
                                        S = etSHR_HET.text.toString().toFloat()
                                    }
                                    var MS: Float = 0.0F

                                    if (max5ET.text.isNotEmpty()) {
                                        MS = max5ET.text.toString().toFloat()
                                    }

                                    var PER: Float = 0.0F
                                    if (etSTR_COVEET.text.isNotEmpty()) {
                                        PER = etSTR_COVEET.text.toString().toFloat()
                                    }

                                    if (ShrDatas.get(i).PAGE == strepage) {
                                        ShrDatas.get(i).SPEC = spec
                                        ShrDatas.get(i).SPEC2 = fami
                                        ShrDatas.get(i).SPEC3 = scien
                                        ShrDatas.get(i).NS = NS
                                        ShrDatas.get(i).S = S
                                        ShrDatas.get(i).MS = MS
                                        ShrDatas.get(i).PER = PER
                                        Log.d("찌즈", spec.toString())
                                    }
                                }
                            }

                            var herchk = false

                            for (i in 0 until HerDatas.size) {
                                if (HerDatas.get(i).PAGE == herpage) {
                                    herchk = true
                                }
                            }

                            if (herchk == false) {

                                var spec = etHER_SPECET.text.toString()

                                val fami = etHER_FAMIET.text.toString()
                                val scien = etHER_SCIENET.text.toString()

                                var NS: Float = 0.0F

                                if (min6ET.text.isNotEmpty()) {
                                    NS = min6ET.text.toString().toFloat()
                                }
                                var S: Float = 0.0F

                                if (etHER_HET.text.isNotEmpty()) {
                                    S = etHER_HET.text.toString().toFloat()
                                }
                                var MS: Float = 0.0F

                                if (max6ET.text.isNotEmpty()) {
                                    MS = max6ET.text.toString().toFloat()
                                }

                                var PER: Float = 0.0F
                                if (etHER_COVEET.text.isNotEmpty()) {
                                    PER = etHER_COVEET.text.toString().toFloat()
                                }

                                var herdata = BioTreeData4(-1, herleftpageTV.text.toString().toInt(), spec, fami, scien, NS, S, MS, PER)

                                HerDatas.add(herdata)
                            } else {
                                for (i in 0 until HerDatas.size) {

                                    if (HerDatas.get(i).PAGE == herpage) {
                                        var spec = etHER_SPECET.text.toString()

                                        val fami = etHER_FAMIET.text.toString()
                                        val scien = etHER_SCIENET.text.toString()

                                        var NS: Float = 0.0F

                                        if (min6ET.text.isNotEmpty()) {
                                            NS = min6ET.text.toString().toFloat()
                                        }
                                        var S: Float = 0.0F

                                        if (etHER_HET.text.isNotEmpty()) {
                                            S = etHER_HET.text.toString().toFloat()
                                        }
                                        var MS: Float = 0.0F

                                        if (max6ET.text.isNotEmpty()) {
                                            MS = max6ET.text.toString().toFloat()
                                        }

                                        var PER: Float = 0.0F
                                        if (etHER_COVEET.text.isNotEmpty()) {
                                            PER = etHER_COVEET.text.toString().toFloat()
                                        }
                                        HerDatas.get(i).SPEC = spec
                                        HerDatas.get(i).SPEC2 = fami
                                        HerDatas.get(i).SPEC3 = scien
                                        HerDatas.get(i).NS = NS
                                        HerDatas.get(i).S = S
                                        HerDatas.get(i).MS = MS
                                        HerDatas.get(i).PER = PER
                                        Log.d("찌즈", spec.toString())
                                    }
                                }
                            }




                            finishFlag = false
                            biotope_attribute.GEOM = geom

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
                                var biotope_attribute = null_biotope_attribute()
                                keyId = intent.getStringExtra("GROP_ID")

                                println("insertkeyid $keyId")


                                addbiotope2(biotope_attribute)


                                if (TreDatas != null && TreDataSize > 0) {
                                    if (i > TreDataSize) {
                                        biotope_attribute.TRE_NUM = 0
                                        biotope_attribute.TRE_SPEC = ""
                                        biotope_attribute.TRE_FAMI = ""
                                        biotope_attribute.TRE_SCIEN = ""
                                        biotope_attribute.TRE_H_N = 0.0f
                                        biotope_attribute.TRE_H = 0.0f
                                        biotope_attribute.TRE_H_X = 0.0f
                                        biotope_attribute.TRE_BREA_N = 0.0f
                                        biotope_attribute.TRE_BREA = 0.0f
                                        biotope_attribute.TRE_BREA_X = 0.0f
                                        biotope_attribute.TRE_COVE = 0.0f
                                    }

                                    if (i < TreDataSize) {
                                        biotope_attribute.TRE_NUM = TreDatas.get(i).PAGE
                                        biotope_attribute.TRE_SPEC = TreDatas.get(i).SPEC
                                        biotope_attribute.TRE_FAMI = TreDatas.get(i).SPEC2
                                        biotope_attribute.TRE_SCIEN = TreDatas.get(i).SPEC3
                                        biotope_attribute.TRE_H_N = TreDatas.get(i).NS
                                        biotope_attribute.TRE_H = TreDatas.get(i).S
                                        biotope_attribute.TRE_H_X = TreDatas.get(i).MS
                                        biotope_attribute.TRE_BREA_N = TreDatas.get(i).NS2
                                        biotope_attribute.TRE_BREA = TreDatas.get(i).S2
                                        biotope_attribute.TRE_BREA_X = TreDatas.get(i).MS2
                                        biotope_attribute.TRE_COVE = TreDatas.get(i).PER


                                    }
                                }

                                if (StreDatas != null && StreDataSize > 0) {
                                    if (i > StreDataSize) {
                                        biotope_attribute.STRE_NUM = 1
                                        biotope_attribute.STRE_SPEC = ""
                                        biotope_attribute.STRE_FAMI = ""
                                        biotope_attribute.STRE_SCIEN = ""
                                        biotope_attribute.STRE_H_N = 0.0f
                                        biotope_attribute.STRE_H = 0.0f
                                        biotope_attribute.STRE_H_X = 0.0f
                                        biotope_attribute.STRE_BRT_N = 0.0f
                                        biotope_attribute.STRE_BRT = 0.0f
                                        biotope_attribute.STRE_BRT_X = 0.0f
                                        biotope_attribute.STRE_COVE = 0.0f
                                    }

                                    if (i < StreDataSize) {
                                        biotope_attribute.STRE_NUM = StreDatas.get(i).PAGE
                                        biotope_attribute.STRE_SPEC = StreDatas.get(i).SPEC
                                        biotope_attribute.STRE_FAMI = StreDatas.get(i).SPEC2
                                        biotope_attribute.STRE_SCIEN = StreDatas.get(i).SPEC3
                                        biotope_attribute.STRE_H_N = StreDatas.get(i).NS
                                        biotope_attribute.STRE_H = StreDatas.get(i).S
                                        biotope_attribute.STRE_H_X = StreDatas.get(i).MS
                                        biotope_attribute.STRE_BRT_N = StreDatas.get(i).NS2
                                        biotope_attribute.STRE_BRT = StreDatas.get(i).S2
                                        biotope_attribute.STRE_BRT_X = StreDatas.get(i).MS2
                                        biotope_attribute.STRE_COVE = StreDatas.get(i).PER
                                    }
                                }

                                if (ShrDatas != null && ShrDataSize > 0) {
                                    if (i > ShrDataSize) {
                                        biotope_attribute.SHR_NUM = 1
                                        biotope_attribute.SHR_SPEC = ""
                                        biotope_attribute.SHR_FAMI = ""
                                        biotope_attribute.SHR_SCIEN = ""
                                        biotope_attribute.SHR_HET_N = 0.0f
                                        biotope_attribute.SHR_H = 0.0f
                                        biotope_attribute.SHR_HET_X = 0.0f
                                        biotope_attribute.STR_COVE = 0.0f
                                    }

                                    if (i < ShrDataSize) {
                                        biotope_attribute.SHR_NUM = ShrDatas.get(i).PAGE
                                        biotope_attribute.SHR_SPEC = ShrDatas.get(i).SPEC
                                        biotope_attribute.SHR_FAMI = ShrDatas.get(i).SPEC2
                                        biotope_attribute.SHR_SCIEN = ShrDatas.get(i).SPEC3
                                        biotope_attribute.SHR_HET_N = ShrDatas.get(i).NS
                                        biotope_attribute.SHR_H = ShrDatas.get(i).S
                                        biotope_attribute.SHR_HET_X = ShrDatas.get(i).MS
                                        biotope_attribute.STR_COVE = ShrDatas.get(i).PER
                                    }
                                }

                                if (HerDatas != null && HerDataSize > 0) {
                                    if (i > HerDataSize) {
                                        biotope_attribute.HER_NUM = 1
                                        biotope_attribute.HER_SPEC = ""
                                        biotope_attribute.HER_FAMI = ""
                                        biotope_attribute.HER_SCIEN = ""
                                        biotope_attribute.HER_HET_N = 0.0f
                                        biotope_attribute.HER_H = 0.0f
                                        biotope_attribute.HER_HET_X = 0.0f
                                        biotope_attribute.HER_COVE = 0.0f
                                    }

                                    if (i < HerDataSize) {
                                        biotope_attribute.HER_NUM = HerDatas.get(i).PAGE
                                        biotope_attribute.HER_SPEC = HerDatas.get(i).SPEC
                                        biotope_attribute.HER_FAMI = HerDatas.get(i).SPEC2
                                        biotope_attribute.HER_SCIEN = HerDatas.get(i).SPEC3
                                        biotope_attribute.HER_HET_N = HerDatas.get(i).NS
                                        biotope_attribute.HER_H = HerDatas.get(i).S
                                        biotope_attribute.HER_HET_X = HerDatas.get(i).MS
                                        biotope_attribute.HER_COVE = HerDatas.get(i).PER
                                    }
                                }


                                biotope_attribute.GEOM = geom

                                dbManager!!.insertbiotope_attribute(biotope_attribute);
                                println("insert-------22222")
                            }
                            var intent = Intent()
                            intent.putExtra("export", 70);
                            intent.putExtra("domin", t_name)
                            intent.putExtra("geom", geom)
                            setResult(RESULT_OK, intent);
                            finish()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            } else {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            Log.d("수정클라스", "")
                            println("삭제3 $pk =============================================")
                            var biotope_attribute = null_biotope_attribute()
//                            dbManager!!.deleteallbiotope_attribute(biotope_attribute, keyId + it_index)
                            biotope_attribute.TRE_NUM = 1
                            biotope_attribute.STRE_NUM = 1
                            biotope_attribute.SHR_NUM = 1
                            biotope_attribute.HER_NUM = 1
                            addbiotope(biotope_attribute)

                            finishFlag = false

                            var intent = Intent()

                            intent.putExtra("export", 70);
                            setResult(RESULT_OK, intent);

                            finish()

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }
        }
        btn_biotopAdd.setOnClickListener {
            var biotope_attribute = null_biotope_attribute()
            TreDatas.clear()
            StreDatas.clear()
            ShrDatas.clear()
            HerDatas.clear()
            it_index += 1
            addbiotope(biotope_attribute)


        }
        trerightTV.setOnClickListener {
            AddTreFlora()
        }
        treleftTV.setOnClickListener {
            var division = false
            for (i in 0 until TreDatas.size) {
                if (t_trepage == TreDatas.get(i).PAGE) {
                    division = true
                }
            }

            if (division == false) {
                var spec = etTRE_SPECET.text.toString()

                val fami = etTRE_FAMIET.text.toString()
                val scien = etTRE_SCIENET.text.toString()

                var NS: Float = 0.0F

                if (minET.text.isNotEmpty()) {
                    NS = minET.text.toString().toFloat()
                }
                var S: Float = 0.0F

                if (etTRE_HET.text.isNotEmpty()) {
                    S = etTRE_HET.text.toString().toFloat()
                }
                var MS: Float = 0.0F

                if (maxET.text.isNotEmpty()) {
                    MS = maxET.text.toString().toFloat()
                }

                var NS2: Float = 0.0F

                if (min2ET.text.isNotEmpty()) {
                    NS2 = min2ET.text.toString().toFloat()
                }

                var S2: Float = 0.0F

                if (etTRE_BREAET.text.isNotEmpty()) {
                    S2 = etTRE_BREAET.text.toString().toFloat()
                }
                var MS2: Float = 0.0F
                if (max2ET.text.isNotEmpty()) {
                    MS2 = max2ET.text.toString().toFloat()
                }

                var PER: Float = 0.0F
                if (etTRE_COVEET.text.isNotEmpty()) {
                    PER = etTRE_COVEET.text.toString().toFloat()
                }

                var tredata = BioTreeData(-1, t_trepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                TreDatas.add(tredata)
            }

            if (t_trepage > 1) {

                etTRE_SPECET.visibility = View.VISIBLE

                for (i in 0 until TreDatas.size) {
                    if (t_trepage == TreDatas.get(i).PAGE) {
                        TreDatas.get(i).SPEC = etTRE_SPECET.text.toString()

                        TreDatas.get(i).SPEC2 = etTRE_FAMIET.text.toString()
                        TreDatas.get(i).SPEC3 = etTRE_SCIENET.text.toString()

                        if (minET.text.isNotEmpty()) {
                            TreDatas.get(i).NS = minET.text.toString().toFloat()
                        }

                        if (etTRE_HET.text.isNotEmpty()) {
                            TreDatas.get(i).S = etTRE_HET.text.toString().toFloat()
                        }

                        if (maxET.text.isNotEmpty()) {
                            TreDatas.get(i).MS = maxET.text.toString().toFloat()
                        }


                        if (min2ET.text.isNotEmpty()) {
                            TreDatas.get(i).NS2 = min2ET.text.toString().toFloat()
                        }


                        if (etTRE_BREAET.text.isNotEmpty()) {
                            TreDatas.get(i).S2 = etTRE_BREAET.text.toString().toFloat()
                        }

                        if (max2ET.text.isNotEmpty()) {
                            TreDatas.get(i).MS2 = max2ET.text.toString().toFloat()
                        }

                        if (etTRE_COVEET.text.isNotEmpty()) {
                            TreDatas.get(i).PER = etTRE_COVEET.text.toString().toFloat()
                        }

                    }
                }

                t_trepage = t_trepage - 1
                for (i in 0..TreDatas.size - 1) {
                    if (t_trepage == TreDatas.get(i).PAGE) {
                        val data = TreDatas.get(i)

                        etTRE_SPECET.setText(data.SPEC)
                        etTRE_FAMIET.setText(data.SPEC2)
                        etTRE_SCIENET.setText(data.SPEC3)
                        minET.setText(data.NS.toString())
                        etTRE_HET.setText(data.S.toString())
                        maxET.setText(data.MS.toString())
                        min2ET.setText(data.NS2.toString())
                        etTRE_BREAET.setText(data.S2.toString())
                        max2ET.setText(data.MS2.toString())
                        etTRE_COVEET.setText(data.PER.toString())
                        val size = trerightpageTV.text.toString().toInt()

                        trepageTV.setText(t_trepage.toString())
                        trerightpageTV.setText(size.toString())
                    }
                }
            }
        }
        strerightTV.setOnClickListener {
            AddStreFlora()
        }
        streleftTV.setOnClickListener {
            var division = false
            for (i in 0 until StreDatas.size) {
                if (strepage == StreDatas.get(i).PAGE) {
                    division = true
                }
            }

            if (division == false) {
                var spec = etSTRE_SPECET.text.toString()

                val fami = etSTRE_FAMIET.text.toString()
                val scien = etSTRE_SCIENET.text.toString()

                var NS: Float = 0.0F

                if (min3ET.text.isNotEmpty()) {
                    NS = min3ET.text.toString().toFloat()
                }
                var S: Float = 0.0F

                if (etSTRE_HET.text.isNotEmpty()) {
                    S = etSTRE_HET.text.toString().toFloat()
                }
                var MS: Float = 0.0F

                if (max3ET.text.isNotEmpty()) {
                    MS = max3ET.text.toString().toFloat()
                }

                var NS2: Float = 0.0F

                if (min4ET.text.isNotEmpty()) {
                    NS2 = min4ET.text.toString().toFloat()
                }

                var S2: Float = 0.0F

                if (etSTRE_BREAET.text.isNotEmpty()) {
                    S2 = etSTRE_BREAET.text.toString().toFloat()
                }
                var MS2: Float = 0.0F
                if (max4ET.text.isNotEmpty()) {
                    MS2 = max4ET.text.toString().toFloat()
                }

                var PER: Float = 0.0F
                if (etSTRE_COVEET.text.isNotEmpty()) {
                    PER = etSTRE_COVEET.text.toString().toFloat()
                }

                var stredata = BioTreeData3(-1, strepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                StreDatas.add(stredata)
            }

            if (strepage > 1) {

                etSTRE_SPECET.visibility = View.VISIBLE

                for (i in 0 until StreDatas.size) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        StreDatas.get(i).SPEC = etSTRE_SPECET.text.toString()

                        StreDatas.get(i).SPEC2 = etSTRE_FAMIET.text.toString()
                        StreDatas.get(i).SPEC3 = etSTRE_SCIENET.text.toString()

                        if (min3ET.text.isNotEmpty()) {
                            StreDatas.get(i).NS = min3ET.text.toString().toFloat()
                        }

                        if (etSTRE_HET.text.isNotEmpty()) {
                            StreDatas.get(i).S = etSTRE_HET.text.toString().toFloat()
                        }

                        if (max3ET.text.isNotEmpty()) {
                            StreDatas.get(i).MS = max3ET.text.toString().toFloat()
                        }


                        if (min4ET.text.isNotEmpty()) {
                            StreDatas.get(i).NS2 = min4ET.text.toString().toFloat()
                        }


                        if (etSTRE_BREAET.text.isNotEmpty()) {
                            StreDatas.get(i).S2 = etSTRE_BREAET.text.toString().toFloat()
                        }

                        if (max4ET.text.isNotEmpty()) {
                            StreDatas.get(i).MS2 = max4ET.text.toString().toFloat()
                        }

                        if (etSTRE_COVEET.text.isNotEmpty()) {
                            StreDatas.get(i).PER = etSTRE_COVEET.text.toString().toFloat()
                        }

                    }
                }

                strepage = strepage - 1
                for (i in 0..StreDatas.size - 1) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        val data = StreDatas.get(i)

                        etSTRE_SPECET.setText(data.SPEC)
                        etSTRE_FAMIET.setText(data.SPEC2)
                        etSTRE_SCIENET.setText(data.SPEC3)
                        min3ET.setText(data.NS.toString())
                        etSTRE_HET.setText(data.S.toString())
                        max3ET.setText(data.MS.toString())
                        min4ET.setText(data.NS2.toString())
                        etSTRE_BREAET.setText(data.S2.toString())
                        max4ET.setText(data.MS2.toString())
                        etSTRE_COVEET.setText(data.PER.toString())
                        val size = strerightpageTV.text.toString().toInt()

                        strepageTV.setText(strepage.toString())
                        strerightpageTV.setText(size.toString())
                    }
                }
            }
        }
        herrightTV.setOnClickListener {
            AddHerFlora()
        }
        herleftTV.setOnClickListener {
            var division = false
            for (i in 0 until HerDatas.size) {
                if (herpage == HerDatas.get(i).PAGE) {
                    division = true
                }
            }

            Log.d("아니왜",herpage.toString())
            Log.d("아니왜333",division.toString())
            if (division == false) {

                var spec = etHER_SPECET.text.toString()

                val fami = etHER_FAMIET.text.toString()
                val scien = etHER_SCIENET.text.toString()

                var NS: Float = 0.0F

                if (min6ET.text.isNotEmpty()) {
                    NS = min6ET.text.toString().toFloat()
                }
                var S: Float = 0.0F

                if (etHER_HET.text.isNotEmpty()) {
                    S = etHER_HET.text.toString().toFloat()
                }
                var MS: Float = 0.0F

                if (max6ET.text.isNotEmpty()) {
                    MS = max6ET.text.toString().toFloat()
                }

                var PER: Float = 0.0F
                if (etHER_COVEET.text.isNotEmpty()) {
                    PER = etHER_COVEET.text.toString().toFloat()
                }

                var herdata = BioTreeData4(-1, herpage, spec, fami, scien, NS, S, MS, PER)

                HerDatas.add(herdata)
            }

            if (herpage > 1) {

                etHER_SPECET.visibility = View.VISIBLE

                for (i in 0 until HerDatas.size) {
                    if (herpage == HerDatas.get(i).PAGE) {
                        HerDatas.get(i).SPEC = etHER_SPECET.text.toString()

                        HerDatas.get(i).SPEC2 = etHER_FAMIET.text.toString()
                        HerDatas.get(i).SPEC3 = etHER_SCIENET.text.toString()

                        if (min6ET.text.isNotEmpty()) {
                            HerDatas.get(i).NS = min6ET.text.toString().toFloat()
                        }

                        if (etHER_HET.text.isNotEmpty()) {
                            HerDatas.get(i).S = etHER_HET.text.toString().toFloat()
                        }

                        if (max6ET.text.isNotEmpty()) {
                            HerDatas.get(i).MS = max6ET.text.toString().toFloat()
                        }
                        if (etHER_COVEET.text.isNotEmpty()) {
                            HerDatas.get(i).PER = etHER_COVEET.text.toString().toFloat()
                        }


                    }
                }

                herpage = herpage - 1
                for (i in 0..HerDatas.size - 1) {
                    if (herpage == HerDatas.get(i).PAGE) {
                        val data = HerDatas.get(i)

                        etHER_SPECET.setText(data.SPEC)
                        etHER_FAMIET.setText(data.SPEC2)
                        etHER_SCIENET.setText(data.SPEC3)
                        min6ET.setText(data.NS.toString())
                        etHER_HET.setText(data.S.toString())
                        max6ET.setText(data.MS.toString())
                        etHER_COVEET.setText(data.PER.toString())
                        val size = herrightpageTV.text.toString().toInt()

                        herleftpageTV.setText(herpage.toString())
                        herrightpageTV.setText(size.toString())
                    }
                }
            }
        }
        shrerightTV.setOnClickListener {
            Log.d("클릭", "클릭")
            AddShrFlora()
        }
        shreleftTV.setOnClickListener {
            var division = false
            for (i in 0 until ShrDatas.size) {
                if (shrpage == ShrDatas.get(i).PAGE) {
                    division = true
                }
            }
            Log.d("아니왜444",herpage.toString())
            Log.d("아니왜444",division.toString())
            if (division == false) {
                var spec = etSHR_SPECET.text.toString()

                val fami = etSHR_FAMIET.text.toString()
                val scien = etSHR_SCIENET.text.toString()

                var NS: Float = 0.0F

                if (min5ET.text.isNotEmpty()) {
                    NS = min5ET.text.toString().toFloat()
                }
                var S: Float = 0.0F

                if (etSHR_HET.text.isNotEmpty()) {
                    S = etSHR_HET.text.toString().toFloat()
                }
                var MS: Float = 0.0F

                if (max5ET.text.isNotEmpty()) {
                    MS = max5ET.text.toString().toFloat()
                }

                var PER: Float = 0.0F
                if (etSTR_COVEET.text.isNotEmpty()) {
                    PER = etSTR_COVEET.text.toString().toFloat()
                }

                var shrdata = BioTreeData2(-1, shrpage, spec, fami, scien, NS, S, MS, PER)

                ShrDatas.add(shrdata)
            }

            if (shrpage > 1) {

                etSHR_SPECET.visibility = View.VISIBLE

                for (i in 0 until ShrDatas.size) {
                    if (shrpage == ShrDatas.get(i).PAGE) {
                        ShrDatas.get(i).SPEC = etSHR_SPECET.text.toString()

                        ShrDatas.get(i).SPEC2 = etSHR_FAMIET.text.toString()
                        ShrDatas.get(i).SPEC3 = etSHR_SCIENET.text.toString()

                        if (min5ET.text.isNotEmpty()) {
                            ShrDatas.get(i).NS = min5ET.text.toString().toFloat()
                        }

                        if (etSHR_HET.text.isNotEmpty()) {
                            ShrDatas.get(i).S = etSHR_HET.text.toString().toFloat()
                        }

                        if (max5ET.text.isNotEmpty()) {
                            ShrDatas.get(i).MS = max5ET.text.toString().toFloat()
                        }
                        if (etSTR_COVEET.text.isNotEmpty()) {
                            ShrDatas.get(i).PER = etSTR_COVEET.text.toString().toFloat()
                        }


                    }
                }

                shrpage = shrpage - 1
                for (i in 0..ShrDatas.size - 1) {
                    if (shrpage == ShrDatas.get(i).PAGE) {
                        val data = ShrDatas.get(i)

                        etSHR_SPECET.setText(data.SPEC)
                        etSHR_FAMIET.setText(data.SPEC2)
                        etSHR_SCIENET.setText(data.SPEC3)
                        min5ET.setText(data.NS.toString())
                        etSHR_HET.setText(data.S.toString())
                        max5ET.setText(data.MS.toString())
                        etSTR_COVEET.setText(data.PER.toString())
                        val size = shrerightpageTV.text.toString().toInt()

                        shrepageTV.setText(shrpage.toString())
                        shrerightpageTV.setText(size.toString())
                    }
                }
            }
        }



        btn_biotopDelete.setOnClickListener {

            if (pk != null) {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()
                            var biotope_attribute = null_biotope_attribute()
                            val dataList: Array<String> = arrayOf("*");

                            if (pk != null) {
                                val data2 = db!!.query("biotopeAttribute", dataList, "id = '$pk'", null, null, null, "", null)

                                while (data2.moveToNext()) {
                                    val biotope_attribute = ps_biotope_attribute(data2)
                                }

                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/biotope/images/")
                                val pathdir = path.listFiles()

                                if (pathdir != null) {
                                    for (i in 0..pathdir.size - 1) {

                                        for (j in 0..pathdir.size - 1) {

                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator + biotope_attribute.INV_INDEX.toString() + "_" + invtm + "_" + (j + 1) + ".png")) {
//                                            if (pathdir.get(i).path.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/data/biotope/images/" +  biotope_attribute.INV_INDEX + "_" + biotope_attribute.INV_TM +"_" + (j+1) + ".png")) {

                                                pathdir.get(i).canonicalFile.delete()

                                                println("delete ===============")

                                            }
                                        }

                                    }

                                    val deletedir = path.listFiles()
                                    println("deletedir.size ${deletedir.size}")
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
                                        path.deleteRecursively()
                                    }
                                } else {
                                    if (path.isDirectory) {
                                        val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
                                        path.deleteRecursively()
                                    }
                                }


                                if (intent.getStringExtra("GROP_ID") != null) {
                                    val GROP_ID = intent.getStringExtra("GROP_ID")

                                    println("GROP_ID---------------$GROP_ID")

                                    val dataList: Array<String> = arrayOf("*");

                                    val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "id desc", null)

                                    if (dataArray != null) {
                                        dataArray.clear()
                                    }

                                    while (data2.moveToNext()) {

                                        chkdata = true

                                        val biotope_attribute = ps_biotope_attribute(data2)
                                        dataArray.add(biotope_attribute)

                                        var dataPk = biotope_attribute.id!!.toInt()
                                        val TRE_NUM = biotope_attribute.TRE_NUM
                                        var SPEC = biotope_attribute.TRE_SPEC
                                        var SPEC2 = biotope_attribute.TRE_FAMI
                                        var SPEC3 = biotope_attribute.TRE_SCIEN
                                        var NS = biotope_attribute.TRE_H_N
                                        var S = biotope_attribute.TRE_H
                                        var MS = biotope_attribute.TRE_H_X
                                        var NS2 = biotope_attribute.TRE_BREA_N
                                        var S2 = biotope_attribute.TRE_BREA
                                        var MS2 = biotope_attribute.TRE_BREA_X
                                        var PER = biotope_attribute.TRE_COVE


                                        if (SPEC!!.length > 0) {
                                            val data = BioTreeData(dataPk, TRE_NUM, SPEC, SPEC2, SPEC3, NS, S, MS, NS2, S2, MS2, PER)
                                            TreDatas.add(data)
                                            println("TRE_NUM ADD ${data.PAGE}")
                                            t_trepage = biotope_attribute.TRE_NUM!!
                                            etTRE_SPECET.setText(biotope_attribute.TRE_SPEC)
                                            etTRE_FAMIET.setText(biotope_attribute.TRE_FAMI)
                                            etTRE_SCIENET.setText(biotope_attribute.TRE_SCIEN)
                                            minET.setText(biotope_attribute.TRE_H_N.toString())
                                            etTRE_HET.setText(biotope_attribute.TRE_H.toString())
                                            maxET.setText(biotope_attribute.TRE_H_X.toString())
                                            min2ET.setText(biotope_attribute.TRE_BREA_N.toString())
                                            etTRE_BREAET.setText(biotope_attribute.TRE_BREA.toString())
                                            max2ET.setText(biotope_attribute.TRE_BREA_X.toString())
                                            etTRE_COVEET.setText(biotope_attribute.TRE_COVE.toString())
                                        }


                                    }

                                    var treDataSize = TreDatas.size + 1

                                    if (TreDatas.size > 0) {
                                        treDataSize = TreDatas.size
                                        t_trepage = TreDatas.size
                                    }

                                    trepageTV.setText(t_trepage.toString())
                                    trerightpageTV.setText(treDataSize.toString())

                                    var intent = Intent()

                                    if (dataArray.size > 1) {

                                        println("삭제 $pk =============================================")

                                        dbManager!!.deleteallbiotope_attribute(biotope_attribute, keyId + it_index.toString())

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager!!.deletebiotope_attribute(biotope_attribute, pk)

                                        var intent = Intent()

                                        intent.putExtra("polygonid", polygonid)

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

                            dialog.cancel()

                            val biotope_attribute = null_biotope_attribute()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")
                                val GROP_ID = intent.getStringExtra("GROP_ID")

                                val dataList: Array<String> = arrayOf("*");

                                val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

                                if (dataArray != null) {
                                    dataArray.clear()
                                }

                                while (data2.moveToNext()) {

                                    chkdata = true

                                    val biotope_attribute = ps_biotope_attribute(data2)
                                    dataArray.add(biotope_attribute)

                                }

                                var intent = Intent()

                                if (dataArray.size > 1) {
                                    println("삭제2 $pk =============================================")
                                    dbManager!!.deleteallbiotope_attribute(biotope_attribute, keyId + it_index.toString())

                                    intent.putExtra("reset", 100)

                                    setResult(RESULT_OK, intent);
                                    finish()

                                }

                                if (dataArray.size == 1) {

                                    var intent = Intent()

                                    intent.putExtra("polygonid", polygonid)

                                    dbManager!!.deletebiotope_attribute(biotope_attribute, pk)

                                    setResult(RESULT_OK, intent);
                                    finish()

                                }

                            }

                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()

            }
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
        etLU_TY_RATEET.setOnClickListener {
            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "토지이용유형(%)")
            intent.putExtra("DlgHeight", 600f);
            intent.putExtra("selectDlg", 500);

            startActivityForResult(intent, SET_RATE);
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

    }


    fun AddTreFlora() {

        var spec = etTRE_SPECET.text.toString()


        val fami = etTRE_FAMIET.text.toString()
        val scien = etTRE_SCIENET.text.toString()

        var NS: Float = 0.0F

        if (minET.text.isNotEmpty()) {
            NS = minET.text.toString().toFloat()
        }
        var S: Float = 0.0F

        if (etTRE_HET.text.isNotEmpty()) {
            S = etTRE_HET.text.toString().toFloat()
        }
        var MS: Float = 0.0F

        if (maxET.text.isNotEmpty()) {
            MS = maxET.text.toString().toFloat()
        }

        var NS2: Float = 0.0F

        if (min2ET.text.isNotEmpty()) {
            NS2 = min2ET.text.toString().toFloat()
        }

        var S2: Float = 0.0F

        if (etTRE_BREAET.text.isNotEmpty()) {
            S2 = etTRE_BREAET.text.toString().toFloat()
        }
        var MS2: Float = 0.0F
        if (max2ET.text.isNotEmpty()) {
            MS2 = max2ET.text.toString().toFloat()
        }

        var PER: Float = 0.0F
        if (etTRE_COVEET.text.isNotEmpty()) {
            PER = etTRE_COVEET.text.toString().toFloat()
        }


        var chkData = false

        var equlas = false

        println("trepage $t_trepage")

        val maxsize = trerightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until TreDatas.size) {
            if (TreDatas.get(i).PAGE == t_trepage) {
                division = true
            }
        }

        if (spec == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {

            if (t_trepage == maxsize) {

                if (division == false) {

                    if (t_trepage > 1) {

                        var tredata = BioTreeData(-1, t_trepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                        TreDatas.add(tredata)

                        t_trepage = t_trepage + 1

                        val page = t_trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        clear_tre()
                    }

                    if (t_trepage == 1) {

                        var tredata = BioTreeData(-1, t_trepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                        TreDatas.add(tredata)

                        t_trepage = t_trepage + 1

                        val page = t_trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        clear_tre()

                    }
                } else {

                    if (t_trepage > 1) {

                        for (i in 0 until TreDatas.size) {
                            if (TreDatas.get(i).PAGE == t_trepage) {
                                TreDatas.get(i).SPEC = spec
                                TreDatas.get(i).SPEC2 = fami
                                TreDatas.get(i).SPEC3 = scien
                                TreDatas.get(i).NS = NS
                                TreDatas.get(i).S = S
                                TreDatas.get(i).MS = MS
                                TreDatas.get(i).NS2 = NS2
                                TreDatas.get(i).S2 = S2
                                TreDatas.get(i).MS2 = MS2
                                TreDatas.get(i).PER = PER
                            }
                        }

                        t_trepage = t_trepage + 1

                        val page = t_trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        clear_tre()
                    }

                    if (t_trepage == 1) {

                        for (i in 0 until TreDatas.size) {
                            if (TreDatas.get(i).PAGE == t_trepage) {
                                TreDatas.get(i).SPEC = spec
                                TreDatas.get(i).SPEC2 = fami
                                TreDatas.get(i).SPEC3 = scien
                                TreDatas.get(i).NS = NS
                                TreDatas.get(i).S = S
                                TreDatas.get(i).MS = MS
                                TreDatas.get(i).NS2 = NS2
                                TreDatas.get(i).S2 = S2
                                TreDatas.get(i).MS2 = MS2
                                TreDatas.get(i).PER = PER
                            }
                        }

                        t_trepage = t_trepage + 1

                        val page = t_trepage
                        val size = trerightpageTV.text.toString().toInt() + 1

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                        clear_tre()

                    }
                }
            }
            if (t_trepage < maxsize) {

                for (i in 0 until TreDatas.size) {
                    if (TreDatas.get(i).PAGE == t_trepage) {
                        TreDatas.get(i).SPEC = spec
                        TreDatas.get(i).SPEC2 = fami
                        TreDatas.get(i).SPEC3 = scien
                        TreDatas.get(i).NS = NS
                        TreDatas.get(i).S = S
                        TreDatas.get(i).MS = MS
                        TreDatas.get(i).NS2 = NS2
                        TreDatas.get(i).S2 = S2
                        TreDatas.get(i).MS2 = MS2
                        TreDatas.get(i).PER = PER
                    }
                }

                t_trepage = t_trepage + 1
                var chk = false
                for (i in 0..TreDatas.size - 1) {
                    if (t_trepage == TreDatas.get(i).PAGE) {
                        chk = true
                        val data = TreDatas.get(i)
                        etTRE_SPECET.setText(data.SPEC)
                        etTRE_FAMIET.setText(data.SPEC2)
                        etTRE_SCIENET.setText(data.SPEC3)
                        minET.setText(data.NS.toString())
                        etTRE_HET.setText(data.S.toString())
                        maxET.setText(data.MS.toString())
                        min2ET.setText(data.NS2.toString())
                        etTRE_BREAET.setText(data.S2.toString())
                        max2ET.setText(data.MS2.toString())
                        etTRE_COVEET.setText(data.PER.toString())

                        val page = t_trepage
                        val size = trerightpageTV.text.toString().toInt()

                        trepageTV.setText(page.toString())
                        trerightpageTV.setText(size.toString())
                    }
                }

                if (chk == false) {
                    val page = t_trepage
                    val size = trerightpageTV.text.toString().toInt() + 1

                    trepageTV.setText(page.toString())
                    trerightpageTV.setText(size.toString())
                    clear_tre()
                }
            }

            etTRE_SPECET.visibility = View.VISIBLE
        }

    }

    fun AddStreFlora() {

        var spec = etSTRE_SPECET.text.toString()

        val fami = etSTRE_FAMIET.text.toString()
        val scien = etSTRE_SCIENET.text.toString()

        var NS: Float = 0.0F

        if (min3ET.text.isNotEmpty()) {
            NS = min3ET.text.toString().toFloat()
        }
        var S: Float = 0.0F

        if (etSTRE_HET.text.isNotEmpty()) {
            S = etSTRE_HET.text.toString().toFloat()
        }
        var MS: Float = 0.0F

        if (max3ET.text.isNotEmpty()) {
            MS = max3ET.text.toString().toFloat()
        }

        var NS2: Float = 0.0F

        if (min4ET.text.isNotEmpty()) {
            NS2 = min4ET.text.toString().toFloat()
        }

        var S2: Float = 0.0F

        if (etSTRE_BREAET.text.isNotEmpty()) {
            S2 = etSTRE_BREAET.text.toString().toFloat()
        }
        var MS2: Float = 0.0F
        if (max4ET.text.isNotEmpty()) {
            MS2 = max4ET.text.toString().toFloat()
        }

        var PER: Float = 0.0F
        if (etSTRE_COVEET.text.isNotEmpty()) {
            PER = etSTRE_COVEET.text.toString().toFloat()
        }

        println("trepage $t_trepage")

        val maxsize = strerightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until StreDatas.size) {
            if (StreDatas.get(i).PAGE == strepage) {
                division = true
            }
        }

        if (spec == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {

            if (strepage == maxsize) {

                if (division == false) {

                    if (strepage > 1) {

                        var stredata = BioTreeData3(-1, strepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                        StreDatas.add(stredata)

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        clear_stre()
                    }

                    if (strepage == 1) {

                        var stredata = BioTreeData3(-1, strepage, spec, fami, scien, NS, S, MS, NS2, S2, MS2, PER)

                        StreDatas.add(stredata)

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        clear_stre()

                    }
                } else {

                    if (strepage > 1) {

                        for (i in 0 until StreDatas.size) {
                            if (StreDatas.get(i).PAGE == strepage) {
                                StreDatas.get(i).SPEC = spec
                                StreDatas.get(i).SPEC2 = fami
                                StreDatas.get(i).SPEC3 = scien
                                StreDatas.get(i).NS = NS
                                StreDatas.get(i).S = S
                                StreDatas.get(i).MS = MS
                                StreDatas.get(i).NS2 = NS2
                                StreDatas.get(i).S2 = S2
                                StreDatas.get(i).MS2 = MS2
                                StreDatas.get(i).PER = PER
                            }
                        }

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        clear_stre()
                    }

                    if (strepage == 1) {

                        for (i in 0 until StreDatas.size) {
                            if (StreDatas.get(i).PAGE == strepage) {
                                StreDatas.get(i).SPEC = spec
                                StreDatas.get(i).SPEC2 = fami
                                StreDatas.get(i).SPEC3 = scien
                                StreDatas.get(i).NS = NS
                                StreDatas.get(i).S = S
                                StreDatas.get(i).MS = MS
                                StreDatas.get(i).NS2 = NS2
                                StreDatas.get(i).S2 = S2
                                StreDatas.get(i).MS2 = MS2
                                StreDatas.get(i).PER = PER
                            }
                        }

                        strepage = strepage + 1

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt() + 1

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                        clear_stre()

                    }
                }
            }
            if (strepage < maxsize) {

                for (i in 0 until StreDatas.size) {
                    if (StreDatas.get(i).PAGE == strepage) {
                        StreDatas.get(i).SPEC = spec
                        StreDatas.get(i).SPEC2 = fami
                        StreDatas.get(i).SPEC3 = scien
                        StreDatas.get(i).NS = NS
                        StreDatas.get(i).S = S
                        StreDatas.get(i).MS = MS
                        StreDatas.get(i).NS2 = NS2
                        StreDatas.get(i).S2 = S2
                        StreDatas.get(i).MS2 = MS2
                        StreDatas.get(i).PER = PER
                    }
                }

                strepage = strepage + 1
                var chk = false
                for (i in 0..StreDatas.size - 1) {
                    if (strepage == StreDatas.get(i).PAGE) {
                        chk = true
                        val data = StreDatas.get(i)
                        etSTRE_SPECET.setText(data.SPEC)
                        etSTRE_FAMIET.setText(data.SPEC2)
                        etSTRE_SCIENET.setText(data.SPEC3)
                        min3ET.setText(data.NS.toString())
                        etSTRE_HET.setText(data.S.toString())
                        max3ET.setText(data.MS.toString())
                        min4ET.setText(data.NS2.toString())
                        etSTRE_BREAET.setText(data.S2.toString())
                        max4ET.setText(data.MS2.toString())
                        etSTRE_COVEET.setText(data.PER.toString())

                        val page = strepage
                        val size = strerightpageTV.text.toString().toInt()

                        strepageTV.setText(page.toString())
                        strerightpageTV.setText(size.toString())
                    }
                }

                if (chk == false) {
                    val page = strepage
                    val size = strerightpageTV.text.toString().toInt() + 1

                    strepageTV.setText(page.toString())
                    strerightpageTV.setText(size.toString())
                    clear_stre()
                }
            }

            etSTRE_SPECET.visibility = View.VISIBLE
        }

    }

    fun AddHerFlora() {

        var spec = etHER_SPECET.text.toString()

        val fami = etHER_FAMIET.text.toString()
        val scien = etHER_SCIENET.text.toString()

        Log.d("짜증", spec.toString())
        var NS: Float = 0.0F

        if (min6ET.text.isNotEmpty()) {
            NS = min6ET.text.toString().toFloat()
        }
        var S: Float = 0.0F

        if (etHER_HET.text.isNotEmpty()) {
            S = etHER_HET.text.toString().toFloat()
        }
        var MS: Float = 0.0F

        if (max6ET.text.isNotEmpty()) {
            MS = max6ET.text.toString().toFloat()
        }

        var PER: Float = 0.0F
        if (etHER_COVEET.text.isNotEmpty()) {
            PER = etHER_COVEET.text.toString().toFloat()
        }

        println("herpage $herpage")

        val maxsize = herrightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until HerDatas.size) {
            if (HerDatas.get(i).PAGE == herpage) {
                division = true
            }
        }

        if (spec == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {

            if (herpage == maxsize) {

                if (division == false) {

                    if (herpage > 1) {

                        var herdata = BioTreeData4(-1, herpage, spec, fami, scien, NS, S, MS, PER)

                        HerDatas.add(herdata)

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herleftpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        clear_her()
                    }

                    if (herpage == 1) {

                        var herdata = BioTreeData4(-1, herpage, spec, fami, scien, NS, S, MS, PER)

                        HerDatas.add(herdata)

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herleftpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        clear_her()

                    }
                } else {

                    if (herpage > 1) {

                        for (i in 0 until HerDatas.size) {
                            if (HerDatas.get(i).PAGE == herpage) {
                                HerDatas.get(i).SPEC = spec
                                HerDatas.get(i).SPEC2 = fami
                                HerDatas.get(i).SPEC3 = scien
                                HerDatas.get(i).NS = NS
                                HerDatas.get(i).S = S
                                HerDatas.get(i).MS = MS
                                HerDatas.get(i).PER = PER
                            }
                        }

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herleftpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        clear_her()
                    }

                    if (herpage == 1) {

                        for (i in 0 until HerDatas.size) {
                            if (HerDatas.get(i).PAGE == herpage) {
                                HerDatas.get(i).SPEC = spec
                                HerDatas.get(i).SPEC2 = fami
                                HerDatas.get(i).SPEC3 = scien
                                HerDatas.get(i).NS = NS
                                HerDatas.get(i).S = S
                                HerDatas.get(i).MS = MS
                                HerDatas.get(i).PER = PER
                            }
                        }

                        herpage = herpage + 1

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt() + 1

                        herleftpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                        clear_her()

                    }
                }
            }
            if (herpage < maxsize) {

                for (i in 0 until HerDatas.size) {
                    if (HerDatas.get(i).PAGE == herpage) {
                        HerDatas.get(i).SPEC = spec
                        HerDatas.get(i).SPEC2 = fami
                        HerDatas.get(i).SPEC3 = scien
                        HerDatas.get(i).NS = NS
                        HerDatas.get(i).S = S
                        HerDatas.get(i).MS = MS
                        HerDatas.get(i).PER = PER
                    }
                }

                herpage = herpage + 1
                var chk = false
                for (i in 0..HerDatas.size - 1) {
                    if (herpage == HerDatas.get(i).PAGE) {
                        chk = true
                        val data = HerDatas.get(i)
                        etHER_SPECET.setText(data.SPEC)
                        etHER_FAMIET.setText(data.SPEC2)
                        etHER_SCIENET.setText(data.SPEC3)
                        min6ET.setText(data.NS.toString())
                        etHER_HET.setText(data.S.toString())
                        max6ET.setText(data.MS.toString())
                        etHER_COVEET.setText(data.PER.toString())

                        val page = herpage
                        val size = herrightpageTV.text.toString().toInt()

                        herleftpageTV.setText(page.toString())
                        herrightpageTV.setText(size.toString())
                    }
                }

                if (chk == false) {
                    val page = herpage
                    val size = herrightpageTV.text.toString().toInt() + 1

                    herleftpageTV.setText(page.toString())
                    herrightpageTV.setText(size.toString())
                    clear_her()
                }
            }

            etHER_SPECET.visibility = View.VISIBLE
        }

    }

    fun AddShrFlora() {

        var spec = etSHR_SPECET.text.toString()

        val fami = etSHR_FAMIET.text.toString()
        val scien = etSHR_SCIENET.text.toString()

        Log.d("짜증", spec.toString())
        var NS: Float = 0.0F

        if (min5ET.text.isNotEmpty()) {
            NS = min5ET.text.toString().toFloat()
        }
        var S: Float = 0.0F

        if (etSHR_HET.text.isNotEmpty()) {
            S = etSHR_HET.text.toString().toFloat()
        }
        var MS: Float = 0.0F

        if (max5ET.text.isNotEmpty()) {
            MS = max5ET.text.toString().toFloat()
        }

        var PER: Float = 0.0F
        if (etSTR_COVEET.text.isNotEmpty()) {
            PER = etSTR_COVEET.text.toString().toFloat()
        }

        println("shrpage $shrpage")

        val maxsize = shrerightpageTV.text.toString().toInt()

        var division = false

        for (i in 0 until ShrDatas.size) {
            if (ShrDatas.get(i).PAGE == shrpage) {
                division = true
            }
        }

        if (spec == "") {
            Toast.makeText(context, "빈칸은 입력하실수 없습니다..", Toast.LENGTH_SHORT).show()
        } else {

            if (shrpage == maxsize) {

                if (division == false) {

                    if (shrpage > 1) {

                        var shrdata = BioTreeData2(-1, shrpage, spec, fami, scien, NS, S, MS, PER)

                        ShrDatas.add(shrdata)

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrerightpageTV.text.toString().toInt() + 1

                        shrepageTV.setText(page.toString())
                        shrerightpageTV.setText(size.toString())
                        clear_shr()
                    }

                    if (shrpage == 1) {

                        var shrdata = BioTreeData2(-1, shrpage, spec, fami, scien, NS, S, MS, PER)

                        ShrDatas.add(shrdata)

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrerightpageTV.text.toString().toInt() + 1

                        shrepageTV.setText(page.toString())
                        shrerightpageTV.setText(size.toString())
                        clear_shr()

                    }
                } else {

                    if (shrpage > 1) {

                        for (i in 0 until ShrDatas.size) {
                            if (ShrDatas.get(i).PAGE == shrpage) {
                                ShrDatas.get(i).SPEC = spec
                                ShrDatas.get(i).SPEC2 = fami
                                ShrDatas.get(i).SPEC3 = scien
                                ShrDatas.get(i).NS = NS
                                ShrDatas.get(i).S = S
                                ShrDatas.get(i).MS = MS
                                ShrDatas.get(i).PER = PER
                            }
                        }

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrerightpageTV.text.toString().toInt() + 1

                        shrepageTV.setText(page.toString())
                        shrerightpageTV.setText(size.toString())
                        clear_shr()
                    }

                    if (shrpage == 1) {

                        for (i in 0 until ShrDatas.size) {
                            if (ShrDatas.get(i).PAGE == shrpage) {
                                ShrDatas.get(i).SPEC = spec
                                ShrDatas.get(i).SPEC2 = fami
                                ShrDatas.get(i).SPEC3 = scien
                                ShrDatas.get(i).NS = NS
                                ShrDatas.get(i).S = S
                                ShrDatas.get(i).MS = MS
                                ShrDatas.get(i).PER = PER
                            }
                        }

                        shrpage = shrpage + 1

                        val page = shrpage
                        val size = shrerightpageTV.text.toString().toInt() + 1

                        shrepageTV.setText(page.toString())
                        shrerightpageTV.setText(size.toString())
                        clear_shr()

                    }
                }
            }
            if (shrpage < maxsize) {

                for (i in 0 until ShrDatas.size) {
                    if (ShrDatas.get(i).PAGE == shrpage) {
                        ShrDatas.get(i).SPEC = spec
                        ShrDatas.get(i).SPEC2 = fami
                        ShrDatas.get(i).SPEC3 = scien
                        ShrDatas.get(i).NS = NS
                        ShrDatas.get(i).S = S
                        ShrDatas.get(i).MS = MS
                        ShrDatas.get(i).PER = PER
                    }
                }

                shrpage = shrpage + 1
                var chk = false
                for (i in 0..ShrDatas.size - 1) {
                    if (shrpage == ShrDatas.get(i).PAGE) {
                        chk = true
                        val data = ShrDatas.get(i)
                        etSHR_SPECET.setText(data.SPEC)
                        etSHR_FAMIET.setText(data.SPEC2)
                        etSHR_SCIENET.setText(data.SPEC3)
                        min5ET.setText(data.NS.toString())
                        etSHR_HET.setText(data.S.toString())
                        max5ET.setText(data.MS.toString())
                        etSTR_COVEET.setText(data.PER.toString())

                        val page = shrpage
                        val size = shrerightpageTV.text.toString().toInt()

                        shrepageTV.setText(page.toString())
                        shrerightpageTV.setText(size.toString())
                    }
                }

                if (chk == false) {
                    val page = shrpage
                    val size = shrerightpageTV.text.toString().toInt() + 1

                    shrepageTV.setText(page.toString())
                    shrerightpageTV.setText(size.toString())
                    clear_shr()
                }
            }

            etSHR_SPECET.visibility = View.VISIBLE
        }

    }


    fun clear_tre() {
        etTRE_SPECET.setText("")
        etTRE_FAMIET.setText("")
        etTRE_SCIENET.setText("")
        minET.setText("")
        etTRE_HET.setText("")
        maxET.setText("")
        min2ET.setText("")
        etTRE_BREAET.setText("")
        max2ET.setText("")
        etTRE_COVEET.setText("")
    }

    fun clear_stre() {
        etSTRE_SPECET.setText("")
        etSTRE_FAMIET.setText("")
        etSTRE_SCIENET.setText("")
        min3ET.setText("")
        etSTRE_HET.setText("")
        max3ET.setText("")
        min4ET.setText("")
        etSTRE_BREAET.setText("")
        max4ET.setText("")
        etSTRE_COVEET.setText("")
    }

    fun clear_shr() {
        etSHR_SPECET.setText("")
        etSHR_FAMIET.setText("")
        etSHR_SCIENET.setText("")
        min5ET.setText("")
        etSHR_HET.setText("")
        max5ET.setText("")
        etSTR_COVEET.setText("")
    }

    fun clear_her() {
        etHER_SPECET.setText("")
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        min6ET.setText("")
        etHER_HET.setText("")
        max6ET.setText("")
        etHER_COVEET.setText("")
    }

    fun null_biotope_attribute(): Biotope_attribute {
        val biotope_attribute: Biotope_attribute = Biotope_attribute(null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
        )
        return biotope_attribute
    }

    fun ps_biotope_attribute(data2: Cursor): Biotope_attribute {
        val biotope_attribute = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53), data2.getString(54), data2.getString(55), data2.getString(56), data2.getString(57)
                , data2.getFloat(58), data2.getFloat(59), data2.getFloat(60), data2.getFloat(61), data2.getFloat(62), data2.getFloat(63)
                , data2.getFloat(64), data2.getFloat(65), data2.getFloat(66), data2.getFloat(67), data2.getFloat(68), data2.getFloat(69), data2.getString(70), data2.getFloat(71)
                , data2.getString(72), data2.getString(73), data2.getString(74), data2.getInt(75), data2.getInt(76), data2.getInt(77), data2.getInt(78)
        )
        return biotope_attribute
    }

    fun addbiotope(biotope_attribute: Biotope_attribute) {
        keyId = intent.getStringExtra("GROP_ID")

        biotope_attribute.GROP_ID = keyId

        Log.d("추가인덱스", it_index.toString())


        val prj = prjnameTV.text.toString()
        if (prj == prjname) {
            biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        } else {
            biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
        }
        biotope_attribute.NEED_CONF = ""

        if (etINV_REGIONET.length() > 0) {
            biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();
        } else {
            biotope_attribute.INV_REGION = INV_REGION
        }
        biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");

        if (etINV_DTTV.text == null) {
            biotope_attribute.INV_DT = Utils.todayStr()
        } else {
            biotope_attribute.INV_DT = etINV_DTTV.text.toString();
        }

        if (etINV_TMTV.text == null) {
            biotope_attribute.INV_TM = Utils.timeStr()
        } else {
            biotope_attribute.INV_TM = etINV_TMTV.text.toString();
        }

        if (tvINV_IndexTV.text.isNotEmpty()) {
            biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()
        }

        if (etLU_TY_RATEET.text.isNotEmpty()) {
            biotope_attribute.LU_TY_RATE = etLU_TY_RATEET.text.toString().toFloat()
        }
        if (etSTAND_HET.text.isNotEmpty()) {

            biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
        }

        biotope_attribute.LU_GR_NUM = TVLU_GR_NumTV.text.toString()
//        if (ETLU_GR_NumET.text.toString() != null && ETLU_GR_NumET.text.toString() != "") {
//            biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()
//        }

        biotope_attribute.LC_GR_NUM = TVLC_GR_NUMTV.text.toString()
//        if (ETlcmGR_NumET.text != null) {
//            biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
//        }
        println("으아!!!!!!!!!!!!!!!${biotope_attribute.LC_GR_NUM}")
        biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()
        if (ETTY_MARKET.text.toString() != null && ETTY_MARKET.text.toString() != "") {
            biotope_attribute.TY_MARK = ETTY_MARKET.text.toString()
        }


        biotope_attribute.BIO_TYPE = bioTV.text.toString()
        if (impET.text.isNotEmpty()) {
            biotope_attribute.IMPERV = impET.text.toString().toFloat()
        }

        if (etGV_RATEET.text.isNotEmpty()) {

            biotope_attribute.GV_RATE = Utils.getString(etGV_RATEET).toFloat();
        }
        biotope_attribute.GV_STRUCT = etGV_STRUCTET.text.toString()
        biotope_attribute.DIS_RET = etDIS_RETET.text.toString()
        biotope_attribute.RESTOR_POT = etRESTOR_POTET.text.toString()
        biotope_attribute.COMP_INTA = etCOMP_INTAET.text.toString()
        biotope_attribute.VP_INTA = etVP_INTAET.text.toString()
        biotope_attribute.IMP_FORM = etIMP_FORMET.text.toString()
        biotope_attribute.BREA_DIA = etBREA_DIAET.text.toString()
        biotope_attribute.FIN_EST = etFIN_ESTET.text.toString()
        biotope_attribute.TRE_SPEC = etTRE_SPECET.text.toString()


        biotope_attribute.TRE_FAMI = etTRE_FAMIET.text.toString()
        biotope_attribute.TRE_SCIEN = etTRE_SCIENET.text.toString()

        if (etTRE_HET.text.isNotEmpty()) {
            biotope_attribute.TRE_H = Utils.getString(etTRE_HET).toFloat();
        }



        if (etTRE_BREAET.text.isNotEmpty()) {

            biotope_attribute.TRE_BREA = Utils.getString(etTRE_BREAET).toFloat();
        }
        if (etTRE_COVEET.text.isNotEmpty()) {

            biotope_attribute.TRE_COVE = Utils.getString(etTRE_COVEET).toFloat();
        }

        biotope_attribute.STRE_SPEC = etSTRE_SPECET.text.toString()
        biotope_attribute.STRE_FAMI = etSTRE_FAMIET.text.toString()
        biotope_attribute.STRE_SCIEN = etSTRE_SCIENET.text.toString()

        if (etSTRE_HET.text.isNotEmpty()) {

            biotope_attribute.STRE_H = Utils.getString(etSTRE_HET).toFloat();
        }

        if (etSTRE_BREAET.text.isNotEmpty()) {

            biotope_attribute.STRE_BRT = Utils.getString(etSTRE_BREAET).toFloat();
        }

        if (etSTRE_COVEET.text.isNotEmpty()) {

            biotope_attribute.STRE_COVE = Utils.getString(etSTRE_COVEET).toFloat();
        }

        biotope_attribute.SHR_SPEC = etSHR_SPECET.text.toString()

        biotope_attribute.SHR_FAMI = etSHR_FAMIET.text.toString()
        biotope_attribute.SHR_SCIEN = etSHR_SCIENET.text.toString()

        if (etSHR_HET.text.isNotEmpty()) {

            biotope_attribute.SHR_H = Utils.getString(etSHR_HET).toFloat();
        }

        if (etSTR_COVEET.text.isNotEmpty()) {

            biotope_attribute.STR_COVE = Utils.getString(etSTR_COVEET).toFloat();
        }

        biotope_attribute.HER_SPEC = etHER_SPECET.text.toString()

        biotope_attribute.HER_FAMI = etHER_FAMIET.text.toString()
        biotope_attribute.HER_SCIEN = etHER_SCIENET.text.toString()

        if (etHER_HET.text.isNotEmpty()) {

            biotope_attribute.HER_H = Utils.getString(etHER_HET).toFloat();
        }

        if (etHER_COVEET.text.isNotEmpty()) {

            biotope_attribute.HER_COVE = Utils.getString(etHER_COVEET).toFloat();
        }

        biotope_attribute.WILD_ANI = etWILD_ANIET.text.toString()
        biotope_attribute.BIOTOP_POT = etBIOTOP_POTET.text.toString()
        biotope_attribute.UNUS_NOTE = etUNUS_NOTEET.text.toString()
        biotope_attribute.LC_TY = lc_type


        if (etGPS_LATTV.text.toString() != "" && etGPS_LONTV.text.toString() != "") {

            biotope_attribute.GPS_LAT = lat.toDouble()
            biotope_attribute.GPS_LON = log.toDouble()

        }

        biotope_attribute.TEMP_YN = "Y"
        val CONF_MOD = tvCONF_MOD.text.toString()

        if (CONF_MOD == "N" || CONF_MOD == "C"|| CONF_MOD == "M") {
            biotope_attribute.CONF_MOD = "M"
        }else{
            biotope_attribute.CONF_MOD = "N"
        }
        biotope_attribute.LANDUSE = landuse

        if (biotope != null) {
            biotope_attribute.LANDUSE = biotope
        }
        biotope_attribute.BIO_TYPE = bioTV.text.toString()
        if (impET.text.isNotEmpty()) {
            biotope_attribute.IMPERV = impET.text.toString().toFloat()
        }
        biotope_attribute.GEOM = geom
        biotope_attribute.UFID = ufidTV.text.toString()
        biotope_attribute.CHECK = checkTV.text.toString()

        biotope_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

        biotope_attribute.CURRENT_TM = Utils.current_tm()

        if (minET.text.isNotEmpty()) {
            biotope_attribute.TRE_H_N = Utils.getString(minET).toFloat();
        }

        if (maxET.text.isNotEmpty()) {
            biotope_attribute.TRE_H_X = Utils.getString(maxET).toFloat();
        }
        if (min2ET.text.isNotEmpty()) {
            biotope_attribute.TRE_BREA_N = Utils.getString(min2ET).toFloat();
        }

        if (max2ET.text.isNotEmpty()) {
            biotope_attribute.TRE_BREA_X = Utils.getString(max2ET).toFloat();
        }

        if (min3ET.text.isNotEmpty()) {
            biotope_attribute.STRE_H_N = Utils.getString(min3ET).toFloat();
        }

        if (max3ET.text.isNotEmpty()) {
            biotope_attribute.STRE_H_X = Utils.getString(max3ET).toFloat();
        }

        if (min4ET.text.isNotEmpty()) {
            biotope_attribute.STRE_BRT_N = Utils.getString(min4ET).toFloat();
        }

        if (max4ET.text.isNotEmpty()) {
            biotope_attribute.STRE_BRT_X = Utils.getString(max4ET).toFloat();
        }

        if (min5ET.text.isNotEmpty()) {
            biotope_attribute.SHR_HET_N = Utils.getString(min5ET).toFloat();
        }

        if (max5ET.text.isNotEmpty()) {
            biotope_attribute.SHR_HET_X = Utils.getString(max5ET).toFloat();
        }

        if (min6ET.text.isNotEmpty()) {
            biotope_attribute.HER_HET_N = Utils.getString(min6ET).toFloat();
        }

        if (max6ET.text.isNotEmpty()) {
            biotope_attribute.HER_HET_X = Utils.getString(max6ET).toFloat();
        }

        Log.d("구실", t_name)
        t_name += t_name2
        var names = t_name.split("-")
        if (names.size > 1 ) {
            if (!t_name.contains("군락")) {
                biotope_attribute.DOMIN =names[0]+"-"+names[1] + "군락"
            }else{
                biotope_attribute.DOMIN =names[0]+"-"+names[1]
            }

        } else if (!t_name.contains("군락")) {
            if (t_name.length > 0) {
                biotope_attribute.DOMIN = names[0] + "군락"
            }else{
                biotope_attribute.DOMIN =""
            }
        } else {
            if (t_name.length > 0) {
                biotope_attribute.DOMIN =names[0]
            }else{
                biotope_attribute.DOMIN =""
            }
        }

        if (chkdata) {

            if (images!!.size > 0 && biotope_attribute.PIC_FOLDER == null) {

                biotope_attribute.PIC_FOLDER = keyId + it_index.toString()
            }else{
                biotope_attribute.PIC_FOLDER =""
            }

            if (pk != null) {

                val CONF_MOD = tvCONF_MOD.text.toString()

                if (CONF_MOD == "N" || CONF_MOD == "C"|| CONF_MOD == "M") {
                    biotope_attribute.CONF_MOD = "M"
                }

                dbManager!!.updatebiotope_attribute(biotope_attribute, pk)
                dbManager!!.updatecommonbiotope(biotope_attribute, keyId)
            }


        } else {

            if (images!!.size > 0) {
                biotope_attribute.PIC_FOLDER = keyId + it_index.toString()
            }else{
                biotope_attribute.PIC_FOLDER =""
            }

            dbManager!!.insertbiotope_attribute(biotope_attribute);


        }

        finishFlag = false

        if (intent.getStringExtra("set") != null) {
            var intent = Intent()
            intent.putExtra("reset", 100)

            setResult(RESULT_OK, intent);
        }

        var intent = Intent()
        intent.putExtra("export", 70)
        setResult(RESULT_OK, intent)

        btn_biotopDelete.visibility = View.GONE

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

    fun addbiotope2(biotope_attribute: Biotope_attribute) {
        keyId = intent.getStringExtra("GROP_ID")
        biotope_attribute.GROP_ID = keyId

        val prj = prjnameTV.text.toString()
        if (prj == prjname) {
            biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        } else {
            biotope_attribute.PRJ_NAME = prjnameTV.text.toString()
        }
        biotope_attribute.NEED_CONF = ""

        if (etINV_REGIONET.length() > 0) {
            biotope_attribute.INV_REGION = etINV_REGIONET.text.toString();
        } else {
            biotope_attribute.INV_REGION = INV_REGION
        }
        biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");

        if (etINV_DTTV.text == null) {
            biotope_attribute.INV_DT = Utils.todayStr()
        } else {
            biotope_attribute.INV_DT = etINV_DTTV.text.toString();
        }

        if (etINV_TMTV.text == null) {
            biotope_attribute.INV_TM = Utils.timeStr()
        } else {
            biotope_attribute.INV_TM = etINV_TMTV.text.toString();
        }

        if (tvINV_IndexTV.text.isNotEmpty()) {
            biotope_attribute.INV_INDEX = tvINV_IndexTV.text.toString().toInt()
        }

        if (etLU_TY_RATEET.text.isNotEmpty()) {

            biotope_attribute.LU_TY_RATE = etLU_TY_RATEET.text.toString().toFloat()
        }
        if (etSTAND_HET.text.isNotEmpty()) {

            biotope_attribute.STAND_H = Utils.getString(etSTAND_HET).toFloat();
        }

        biotope_attribute.LU_GR_NUM = TVLU_GR_NumTV.text.toString()
//        if (ETLU_GR_NumET.text.toString() != null && ETLU_GR_NumET.text.toString() != "") {
//            biotope_attribute.LU_GR_NUM = ETLU_GR_NumET.text.toString()
//        }

        biotope_attribute.LC_GR_NUM = TVLC_GR_NUMTV.text.toString()
//        if (ETlcmGR_NumET.text != null) {
//            biotope_attribute.LC_GR_NUM = ETlcmGR_NumET.text.toString()
//        }
        println("으악!!!!!!!!!!!!!!!${biotope_attribute.LC_GR_NUM}")
        biotope_attribute.TY_MARK = TVTY_MARKTV.text.toString()
        if (ETTY_MARKET.text.toString() != null && ETTY_MARKET.text.toString() != "") {
            biotope_attribute.TY_MARK = ETTY_MARKET.text.toString()
        }


        biotope_attribute.BIO_TYPE = bioTV.text.toString()
        if (impET.text.isNotEmpty()) {
            biotope_attribute.IMPERV = impET.text.toString().toFloat()
        }

        if (etGV_RATEET.text.isNotEmpty()) {

            biotope_attribute.GV_RATE = Utils.getString(etGV_RATEET).toFloat();
        }
        biotope_attribute.GV_STRUCT = etGV_STRUCTET.text.toString()
        biotope_attribute.DIS_RET = etDIS_RETET.text.toString()
        biotope_attribute.RESTOR_POT = etRESTOR_POTET.text.toString()
        biotope_attribute.COMP_INTA = etCOMP_INTAET.text.toString()
        biotope_attribute.VP_INTA = etVP_INTAET.text.toString()
        biotope_attribute.IMP_FORM = etIMP_FORMET.text.toString()
        biotope_attribute.BREA_DIA = etBREA_DIAET.text.toString()
        biotope_attribute.FIN_EST = etFIN_ESTET.text.toString()
        biotope_attribute.TRE_SPEC = etTRE_SPECET.text.toString()



        biotope_attribute.WILD_ANI = etWILD_ANIET.text.toString()
        biotope_attribute.BIOTOP_POT = etBIOTOP_POTET.text.toString()
        biotope_attribute.UNUS_NOTE = etUNUS_NOTEET.text.toString()
        biotope_attribute.LC_TY = lc_type


        if (etGPS_LATTV.text.toString() != "" && etGPS_LONTV.text.toString() != "") {

            biotope_attribute.GPS_LAT = lat.toDouble()
            biotope_attribute.GPS_LON = log.toDouble()

        }

        biotope_attribute.TEMP_YN = "Y"
        val CONF_MOD = tvCONF_MOD.text.toString()

        if (CONF_MOD == "N" || CONF_MOD == "C"|| CONF_MOD == "M") {
            biotope_attribute.CONF_MOD = "M"
        }else{
            biotope_attribute.CONF_MOD = "N"
        }

        biotope_attribute.LANDUSE = landuse

        if (biotope != null) {
            biotope_attribute.LANDUSE = biotope
        }
        biotope_attribute.BIO_TYPE = bioTV.text.toString()
        if (impET.text.isNotEmpty()) {
            biotope_attribute.IMPERV = impET.text.toString().toFloat()
        }
        biotope_attribute.GEOM = geom
        biotope_attribute.UFID = ufidTV.text.toString()
        biotope_attribute.CHECK = checkTV.text.toString()

        biotope_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

        biotope_attribute.CURRENT_TM = Utils.current_tm()

        t_name += t_name2
        var names = t_name.split("-")
        if (names.size > 1 ) {
            if (!t_name.contains("군락")) {
                biotope_attribute.DOMIN =names[0]+"-"+names[1] + "군락"
            }else{
                biotope_attribute.DOMIN =names[0]+"-"+names[1]
            }

        } else if (!t_name.contains("군락")) {
            if (t_name.length > 0) {
                biotope_attribute.DOMIN = names[0] + "군락"
            }else{
                biotope_attribute.DOMIN =""
            }
        } else {
            if (t_name.length > 0) {
                biotope_attribute.DOMIN =names[0]
            }else{
                biotope_attribute.DOMIN =""
            }
        }


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

//        clear()
        chkdata = false
        pk = null
    }


    fun load_biotope(biotope_attribute: Biotope_attribute) {

        if (biotope_attribute.TY_MARK.toString() != "null") {
            TVTY_MARKTV.setText(biotope_attribute.TY_MARK)
        }
        if (biotope_attribute.BIO_TYPE != "null") {
            bioTV.setText(biotope_attribute.BIO_TYPE)
        }
        if (biotope_attribute.IMPERV.toString() != "null") {
            impET.setText(biotope_attribute.IMPERV.toString())
        }
        if (biotope_attribute.GV_RATE.toString() != "null") {
            etGV_RATEET.setText(biotope_attribute.GV_RATE.toString())
        }
        if (biotope_attribute.GV_STRUCT.toString() != "null") {
            etGV_STRUCTET.setText(biotope_attribute.GV_STRUCT)
        }
        if (biotope_attribute.DIS_RET.toString() != "null") {
            etDIS_RETET.setText(biotope_attribute.DIS_RET)
        }
        if (biotope_attribute.RESTOR_POT.toString() != "null") {
            etRESTOR_POTET.setText(biotope_attribute.RESTOR_POT)
        }
        if (biotope_attribute.COMP_INTA.toString() != "null") {
            etCOMP_INTAET.setText(biotope_attribute.COMP_INTA)
        }
        if (biotope_attribute.VP_INTA.toString() != "null") {
            etVP_INTAET.setText(biotope_attribute.VP_INTA)
        }
        if (biotope_attribute.BREA_DIA.toString() != "null") {
            etBREA_DIAET.setText(biotope_attribute.BREA_DIA)
        }
        if (biotope_attribute.FIN_EST.toString() != "null") {
            etFIN_ESTET.setText(biotope_attribute.FIN_EST)
        }
        if (biotope_attribute.TRE_SPEC.toString() != "null") {
            etTRE_SPECET.setText(biotope_attribute.TRE_SPEC)
            etTRE_FAMIET.setText(biotope_attribute.TRE_FAMI)
            etTRE_SCIENET.setText(biotope_attribute.TRE_SCIEN.toString())
        }
        etTRE_HET.setText(biotope_attribute.TRE_H.toString())
        minET.setText(biotope_attribute.TRE_H_N.toString())
        maxET.setText(biotope_attribute.TRE_H_X.toString())
        min2ET.setText(biotope_attribute.TRE_BREA_N.toString())
        max2ET.setText(biotope_attribute.TRE_BREA_X.toString())
        min3ET.setText(biotope_attribute.STRE_H_N.toString())
        max3ET.setText(biotope_attribute.STRE_H_X.toString())
        min4ET.setText(biotope_attribute.STRE_BRT_N.toString())
        max4ET.setText(biotope_attribute.STRE_BRT_X.toString())
        min5ET.setText(biotope_attribute.SHR_HET_N.toString())
        max5ET.setText(biotope_attribute.SHR_HET_X.toString())
        min6ET.setText(biotope_attribute.HER_HET_N.toString())
        max6ET.setText(biotope_attribute.HER_HET_X.toString())
        etTRE_BREAET.setText(biotope_attribute.TRE_BREA.toString())
        etTRE_COVEET.setText(biotope_attribute.TRE_COVE.toString())
        if (biotope_attribute.STRE_SPEC.toString() != "null") {
            etSTRE_SPECET.setText(biotope_attribute.STRE_SPEC.toString())
            etSTRE_FAMIET.setText(biotope_attribute.STRE_FAMI.toString())
            etSTRE_SCIENET.setText(biotope_attribute.STRE_SCIEN.toString())
        }

        etSTRE_HET.setText(biotope_attribute.STRE_H.toString())
        etSTRE_BREAET.setText(biotope_attribute.STRE_BRT.toString())
        etSTRE_COVEET.setText(biotope_attribute.STRE_COVE.toString())

        if (biotope_attribute.SHR_SPEC.toString() != "null") {
            etSHR_SPECET.setText(biotope_attribute.SHR_SPEC)
            etSHR_FAMIET.setText(biotope_attribute.SHR_FAMI.toString())
            etSHR_SCIENET.setText(biotope_attribute.SHR_SCIEN.toString())
        }

        etSHR_HET.setText(biotope_attribute.SHR_H.toString())
        etSTR_COVEET.setText(biotope_attribute.STR_COVE.toString())
        if (biotope_attribute.HER_SPEC.toString() != "null") {
            etHER_SPECET.setText(biotope_attribute.HER_SPEC.toString())
            etHER_FAMIET.setText(biotope_attribute.HER_FAMI.toString())
            etHER_SCIENET.setText(biotope_attribute.HER_SCIEN.toString())
        }


        etHER_HET.setText(biotope_attribute.HER_H.toString())
        etHER_COVEET.setText(biotope_attribute.HER_COVE.toString())
        etPIC_FOLDERET.setText(biotope_attribute.PIC_FOLDER.toString())
        etWILD_ANIET.setText(biotope_attribute.WILD_ANI.toString())
        etBIOTOP_POTET.setText(biotope_attribute.BIOTOP_POT.toString())
        etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())
        tvPIC_FOLDERTV.setText(biotope_attribute.PIC_FOLDER)
        etUNUS_NOTEET.setText(biotope_attribute.UNUS_NOTE.toString())
        if (biotope_attribute.UFID.toString() != "null") {
            ufidTV.setText(biotope_attribute.UFID)
        }
        if (biotope_attribute.CHECK.toString() != "null") {
            checkTV.setText(biotope_attribute.CHECK)
        }

        if (biotope_attribute.DOMIN.toString() != "null") {
            var t_names = biotope_attribute.DOMIN.toString().split("-")
            if (t_names.size > 1) {
                t_name = t_names[0]
                t_name2 = "-" + t_names[1]
            } else {
                t_name = t_names[0].replace("군락", "")
            }
        }

        if (biotope_attribute.DOMIN.toString() != "null") {
            var domins = biotope_attribute.DOMIN!!.split("-")
            if (domins.size > 1) {
                dominTV.text = domins[0]
                ausTV.text = domins[1]
            } else {
                dominTV.text = domins[0]
            }
        }

        etIMP_FORMET.setText(biotope_attribute.IMP_FORM.toString())
        landuse = biotope_attribute.LANDUSE
        geom = biotope_attribute.GEOM.toString()


        lc_type = biotope_attribute.LC_TY.toString()
        if (lc_type == "P") {
            set_menu()
            lcCB.setImageResource(R.mipmap.box_check_on)
        } else if (lc_type == "I") {
            set_menu()
            lc2CB.setImageResource(R.mipmap.box_check_on)
        } else if (lc_type == "G") {
            set_menu()
            lc3CB.setImageResource(R.mipmap.box_check_on)
        } else if (lc_type == "W") {
            set_menu()
            lc4CB.setImageResource(R.mipmap.box_check_on)
        }


        val id = biotope_attribute.id

        if (biotope_attribute.TEMP_YN.equals("N")) {
            dbManager!!.deletebiotope_attribute(biotope_attribute, id)
        }

        if (biotope_attribute.TEMP_YN.equals("Y")) {
            dataArray.add(biotope_attribute)
        }

        tvCONF_MOD.setText(biotope_attribute.CONF_MOD)
    }

    fun getTime(): String {

        val date = Date()
        val fullTime = SimpleDateFormat("yyyy-MM-dd")

        return fullTime.format(date).toString()
    }

    fun createId(): String {

        val date = Date()
//        val fullTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val fullTime = SimpleDateFormat("HH:mm")

        return fullTime.format(date).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var biotopeModel: BiotopeModel

        var biotopeClass: BiotopeClass

        var vegetation: Vegetation

        var biotopeType: BiotopeType

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                SET_DATA -> {

                    if (data!!.getSerializableExtra("biotopeType") != null) {
                        biotopeType = data!!.getSerializableExtra("biotopeType") as BiotopeType
                        bioTV.setText(biotopeType.CONTENT)
                    }

                }
                SET_DOMIN -> {

                    if (data!!.getStringExtra("name") == "SP(미동정)") {
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT)
                    } else {
                        t_name = data!!.getStringExtra("name")
                        dominTV.setText(t_name)
                    }

                }

                SET_DOMIN2 -> {
                    if (data!!.getStringExtra("name") == "SP(미동정)") {
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT2)
                    } else {
                        t_name2 = "-" + data!!.getStringExtra("name");
//                        var names = t_name.split("-")
                        ausTV.setText(t_name2.replace("-", "") + "군락")
                    }
                }


                SET_INPUT -> {
                    var name = data!!.getStringExtra("name");
                    dominTV.text = name
                }
                SET_INPUT2 -> {
                    var name = data!!.getStringExtra("name");
                    t_name2 = "-" + name
                    ausTV.text = name + "군락"
                }


                SET_INPUT3 -> {
                    var name = data!!.getStringExtra("name");
                    etTRE_SPECET.text = name
                }
                SET_INPUT4 -> {
                    var name = data!!.getStringExtra("name");
                    etSTRE_SPECET.text = name
                }
                SET_INPUT5 -> {
                    var name = data!!.getStringExtra("name");
                    etSHR_SPECET.text = name
                }
                SET_INPUT6 -> {
                    var name = data!!.getStringExtra("name");
                    etHER_SPECET.text = name
                }

                SET_DATA1 -> {

                    biotopeModel = data!!.getSerializableExtra("bioModel") as BiotopeModel


                    //토지이용현황
                    if (biotopeModel.codeType == "biotopeM") {

                        TVLU_GR_NumTV.setText(biotopeModel.code + "(" + biotopeModel.name + ")")
                        if (TVLU_GR_NumTV.text == "null") {
                            TVLU_GR_NumTV.setText("")
                        }
                        ETLU_GR_NumET.setText(biotopeModel.code + "(" + biotopeModel.name + ")")
                        if (ETLU_GR_NumET.text == null) {
                            ETLU_GR_NumET.setText("")
                        }
                        //토지피복현황
                    } else if (biotopeModel.codeType == "biotopeS") {
                        ETlcmGR_NumET.setText(biotopeModel.code)
                        if (ETlcmGR_NumET.text == null) {
                            ETlcmGR_NumET.setText("")
                        }

                        var bioModelParent: BiotopeModel
                        bioModelParent = data!!.getSerializableExtra("bioModelParent") as BiotopeModel


                        //불투수
                        if (bioModelParent.code == "A") {

                            TVLC_GR_NUMTV.setText(biotopeModel.code + "(" + biotopeModel.name + ")")
                        } else if (bioModelParent.code == "B") {

                            TVLC_GR_NUMTV.setText(biotopeModel.code + "(" + biotopeModel.name + ")")
                            //녹지
                        } else if (bioModelParent.code == "C") {
                            TVLC_GR_NUMTV.setText(biotopeModel.code + "(" + biotopeModel.name + ")")
                            //수공간
                        } else if (bioModelParent.code == "D") {
                            TVLC_GR_NUMTV.setText(biotopeModel.code + "(" + biotopeModel.name + ")")
                        }

                        //현존식생현황  아직 테이블 명 코드 미정
                    } else if (biotopeModel.codeType == "biotopeS") {

                        TVTY_MARKTV.setText(biotopeModel.name)
                        TVTY_MARKTV.setText(biotopeModel.code)
                    }

                }

                SET_DATA2 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etTRE_SPECET.setText(name)
                    if (name == "SP(미동정)") {
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT3)
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
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT4)
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
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT5)
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
                        val intent = Intent(context, DlgInputActivity::class.java)
                        startActivityForResult(intent, SET_INPUT6)
                    }
                    etHER_FAMIET.setText(family_name)
                    etHER_SCIENET.setText(zoological)

                }

                SET_DATA6 -> {

                    if (data!!.getSerializableExtra("biotopeClass") != null) {
                        biotopeClass = data!!.getSerializableExtra("biotopeClass") as BiotopeClass
                        if (biotopeClass.smallcategory.equals("조림지") || biotopeClass.smallcategory.equals("이차림") || biotopeClass.smallcategory.equals("자연림")){
                            TVTY_MARKTV.setText(biotopeClass.smallcategory)
                        }else{
                            TVTY_MARKTV.setText(biotopeClass.sign+"("+biotopeClass.smallcategory+")")
                        }
                        println("biotopeSize ${biotopeClass.sign}")
                    }

                    if (data!!.getSerializableExtra("Vegetation") != null) {
                        vegetation = data!!.getSerializableExtra("Vegetation") as Vegetation
                        if (data!!.getSerializableExtra("Number") != null) {
                            var number = data!!.getStringExtra("Number")
                            val category = vegetation.CATEGORY
                            if (category == "기타") {
                                TVTY_MARKTV.setText("")
                                ETTY_MARKET.setText("")
                                TVTY_MARKTV.visibility = View.GONE
                                ETTY_MARKET.visibility = View.VISIBLE
                            } else {
                                ETTY_MARKET.setText("")
                                ETTY_MARKET.visibility = View.GONE
                                TVTY_MARKTV.visibility = View.VISIBLE
                                TVTY_MARKTV.setText(vegetation.SIGN)
                                if (number != "") {
                                    TVTY_MARKTV.setText(vegetation.SIGN + number)
                                }
                            }
                        }

                    }

                    if (data!!.getStringExtra("etc") != null) {
                        TVTY_MARKTV.setText("")
                        ETTY_MARKET.setText("")
                        TVTY_MARKTV.visibility = View.GONE
                        ETTY_MARKET.visibility = View.VISIBLE
                    }

                }

                SET_RATE -> {
                    etLU_TY_RATEET.setText(data!!.getStringExtra("selectDlg"))
                }

                BIOTOPE_BASE -> {

                }

                FROM_CAMERA -> {

                    if (resultCode == -1) {
                        val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId
                        addPicturesLL!!.removeAllViews()
                        val realPathFromURI = cameraPath!!
                        images_path!!.add(cameraPath!!)
                        context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$realPathFromURI")))
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

                            val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator
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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator
                    val outPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId

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

//                        val num = biotopenumTV.text.toString()
                        var time = ""
                        time = biotopeinvtmTV.text.toString()
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

    fun getAttrubuteKey(): String {

        val time = System.currentTimeMillis()
        val dayTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val strDT = dayTime.format(Date(time))

        return strDT
    }

    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
    }

    @SuppressLint("MissingPermission", "SetTextI18n")

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient!!.connect()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
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
                imageUri = FileProvider.getUriForFile(context, context!!.packageName + ".provider", photo)

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, FROM_CAMERA)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }


    private fun imageFromGallery() {

        val intent1 = Intent(context, WriteAlbumActivity::class.java)
        startActivityForResult(intent1, FROM_ALBUM)

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
                    val outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator
                    addPicturesLL!!.removeAllViews()
                    val tag = v.tag as Int
                    var del_images: ArrayList<String> = ArrayList();
                    try {
                        images!!.clear()
                        del_images = images_path!![tag].split("/") as ArrayList<String>
                        images_path!!.removeAt(tag)

//                    val num = biotopenumTV.text.toString()
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
                            println("getPk {$getPk}")
                            val pathPk2 = getPk.get(1)
                            val num = tvINV_IndexTV.text.toString()
                            val invtm = etINV_TMTV.text.toString()

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

    fun clear() {

        etINV_DTTV.setText(Utils.todayStr());
        etINV_TMTV.setText(createId())

        var num = tvINV_IndexTV.text.toString()

        if (num.length > 7) {
            var textnum = num.substring(num.length - 2, num.length)
            var splitnum = num.substring(0, num.length - 2)
            var plusnum = textnum.toInt() + 1
            tvINV_IndexTV.setText(splitnum.toString() + plusnum.toString())
        } else {
            var textnum = num.substring(num.length - 1, num.length)
            var splitnum = num.substring(0, num.length - 1)
            var plusnum = textnum.toInt() + 1
            tvINV_IndexTV.setText(splitnum.toString() + plusnum.toString())
        }

        etINV_TMTV.setText(Utils.timeStr())

        trerightpageTV.text = "1"
        trepageTV.text = "1"
        strerightpageTV.text = "1"
        strepageTV.text = "1"

        dominTV.setText("")
        ausTV.setText("")

        bioTV.setText("")
        TVLU_GR_NumTV.setText("")
        etLU_TY_RATEET.setText("")
        etSTAND_HET.setText("")
        TVLC_GR_NUMTV.setText("")
        ETLU_GR_NumET.setText("")
        ETlcmGR_NumET.setText("")

        etTRE_SPECETtmp.setText("")
        etSTRE_SPECETtmp.setText("")
        etSHR_SPECETtmp.setText("")
        etHER_SPECETtmp.setText("")

        TVTY_MARKTV.setText("")


        etGV_RATEET.setText("")
        etGV_STRUCTET.setText("")
        etDIS_RETET.setText("")
        etRESTOR_POTET.setText("")
        etCOMP_INTAET.setText("")
        etVP_INTAET.setText("")
        etBREA_DIAET.setText("")
        etFIN_ESTET.setText("")
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
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        etHER_HET.setText("")
        etHER_COVEET.setText("")
        etPIC_FOLDERET.setText("")
        etWILD_ANIET.setText("")
        etBIOTOP_POTET.setText("")
        etUNUS_NOTEET.setText("")
        tvPIC_FOLDERTV.setText("")
        etUNUS_NOTEET.setText("")

        etIMP_FORMET.setText("")

        addPicturesLL!!.removeAllViews()

    }

    fun set_menu() {
        lcCB.setImageResource(R.mipmap.box_check_off)
        lc2CB.setImageResource(R.mipmap.box_check_off)
        lc3CB.setImageResource(R.mipmap.box_check_off)
        lc4CB.setImageResource(R.mipmap.box_check_off)
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
            etINV_TMTV.text = msg

        })
        dialog.show()

        /*
        val cal = Calendar.getInstance()
        val timeDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->
            var hour_s = hour.toString()
            var min_s = min.toString()
            if (min_s.length != 2) {
                min_s = "0" + min_s
            }
            if (hour_s.length != 2) {
                hour_s = "0" + hour_s
            }
            val msg = String.format("%s:%s", hour_s, min_s)
            etINV_TMTV.text = msg
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
        timeDialog.show()
        */
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
        etINV_DTTV.text = msgs[0] + "-" + month + "-" + day
    }

    override fun onBackPressed() {

        if (intent.getSerializableExtra("biotopedata") == null) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("작성을 취소하시겠습니까?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        val dataList: Array<String> = arrayOf("*");

                        val data2 = db!!.query("biotopeAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        var dataArray: ArrayList<Biotope_attribute> = ArrayList<Biotope_attribute>()

                        while (data2.moveToNext()) {

                            val biotope_attribute = ps_biotope_attribute(data2)


                            dataArray.add(biotope_attribute)

                        }

                        if (dataArray.size == 0) {
                            var intent = Intent()
                            intent.putExtra("polygonid", polygonid)
                            setResult(RESULT_OK, intent);

                            val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
//                                val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data/birds/images/")
                            val pathdir = path.listFiles()

                            if (pathdir != null) {
                                val deletedir = path.listFiles()
                                println("deletedir.size ${deletedir.size}")
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
//                                     val path:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ecology/tmps/" + biotope_attribute.INV_DT + "." + biotope_attribute.INV_TM + "."+biotope_attribute.INV_INDEX)
                                    deletepath.deleteRecursively()
                                }
                            } else {
                                if (path.isDirectory) {
                                    val deletepath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology/data" + File.separator + "biotope/images" + File.separator + keyId + File.separator)
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
        } else {
            finish()
        }

    }


}

