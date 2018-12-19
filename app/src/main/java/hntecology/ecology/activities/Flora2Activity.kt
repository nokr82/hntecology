package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.joooonho.SelectableRoundedImageView
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_flora2.*
import java.io.File
import java.io.IOException
import java.util.ArrayList

class Flora2Activity : Activity() {

    lateinit var context: Context;

    var markerid : String? = null
    var chkdata: Boolean = false;
    var basechkdata = false
    var keyId: String? = null;
    var pk: String? = null
    var userName = "";

    var latitude = 0.0f;
    var longitude = 0.0f;

    var lat:String = ""
    var log:String = ""

    var dataArray:ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()

    val SET_DATA1 = 1;
    val SET_DATA2 = 2
    val SET_DATA3 = 3
    val SET_DATA4 = 4
    val SET_DATA5 = 5
    val SET_DATA6 = 6

    var trepage = 1
    var strepage = 1
    var shrpage =  1
    var herpage = 1

    var TreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var StreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var ShrDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()
    var HerDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()

    var dataPk = -1

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flora2)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

//        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        userName = PrefUtils.getStringPreference(context, "name");
        invpersonTV.setText(userName)
        invdtTV.setText(Utils.todayStr())

//        val TRENUM = dbManager.manyfloratrenumNext()
//        val STRENUM = dbManager.manyflorastrenumNext()
//        val SHRNUM = dbManager.manyflorashrnumNext()
//        val HERNUM = dbManager.manyflorahernumNext()

//        trenumTV.setText(TRENUM.toString())
//        strenumTV.setText(STRENUM.toString())
//        shrnumTV.setText(SHRNUM.toString())
//        hernumTV.setText(HERNUM.toString())

        var intent: Intent = getIntent();

        if(intent.getStringExtra("markerid") != null){
            markerid = intent.getStringExtra("markerid")
        }

        if(intent.getStringExtra("latitude")!= null){
            lat = intent.getStringExtra("latitude")
            println("==============$lat")
            gpslatTV.setText(lat)
        }

        if(intent.getStringExtra("longitude")!= null){
            log = intent.getStringExtra("longitude")
            println("==============$log")
            gpslonTV.setText(log)
        }

        if(intent.getStringExtra("longitude") != null && intent.getStringExtra("latitude") != null){
            lat = intent.getStringExtra("latitude")
            log = intent.getStringExtra("longitude")

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

                    invregionTV.setText(list.get(0).getAddressLine(0));
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }

        keyId = intent.getStringExtra("GROP_ID")

        if(intent.getStringExtra("GROP_ID") != null){

            var AllDatas:ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()

            val grop_id = intent.getStringExtra("GROP_ID")

            println("flora2gropid ${intent.getStringExtra("GROP_ID")}")

            val dataList: Array<String> = arrayOf("*");

            val data = db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$grop_id'", null, null, null, "id", null)

            while(data.moveToNext()){

                var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                        data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                        , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                        , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                        ,data.getFloat(33),data.getString(34),data.getString(35))

                // 교목층

                invregionTV.setText(manyFloraAttribute.INV_REGION)
                invdtTV.setText(manyFloraAttribute.INV_DT)
                invpersonTV.setText(manyFloraAttribute.INV_PERSON)

                dataPk = manyFloraAttribute.id!!.toInt()

                val PK = manyFloraAttribute.id!!.toInt()
                val TRE_NUM = manyFloraAttribute.TRE_NUM
                val TRE_SPEC =  manyFloraAttribute.TRE_SPEC
                val TRE_FAMI =  manyFloraAttribute.TRE_FAMI
                val TRE_SCIEN =  manyFloraAttribute.TRE_SCIEN
                val TRE_H =  manyFloraAttribute.TRE_H
                val TRE_BREA =  manyFloraAttribute.TRE_BREA
                val TRE_COVE =  manyFloraAttribute.TRE_COVE

                println("TRE_NUM $TRE_NUM TRE_SCIEN $TRE_SCIEN TRE_BREA $TRE_BREA TRE_COVE $TRE_COVE TRE_H $TRE_H")

                if (TRE_SPEC!!.length > 0 || TRE_H!! != 0.0f || TRE_BREA!! != 0.0f || TRE_COVE != 0.0f){
                    val data = TreeData1(PK,TRE_NUM,TRE_SPEC,TRE_FAMI,TRE_SCIEN,TRE_H,TRE_BREA,TRE_COVE)
                    TreDatas.add(data)
                    println("TRE_NUM ${data.PAGE}")
                    trepage = manyFloraAttribute.TRE_NUM!!
                    trenumTV.setText(manyFloraAttribute.TRE_NUM.toString())
                    etTRE_SPECET.setText(manyFloraAttribute.TRE_SPEC)
                    etTRE_FAMIET.setText(manyFloraAttribute.TRE_FAMI)
                    etTRE_SCIENET.setText(manyFloraAttribute.TRE_SCIEN)
                    etTRE_HET.setText(manyFloraAttribute.TRE_H.toString())
                    etTRE_BREAET.setText(manyFloraAttribute.TRE_BREA.toString())
                    etTRE_COVEET.setText(manyFloraAttribute.TRE_COVE.toString())
                }

                // 아교목층

                val STRE_NUM = manyFloraAttribute.STRE_NUM
                val STRE_SPEC = manyFloraAttribute.STRE_SPEC
                val STRE_FAMI = manyFloraAttribute.STRE_FAMI
                val STRE_SCIEN = manyFloraAttribute.STRE_SCIEN
                val STRE_H = manyFloraAttribute.STRE_H
                val STRE_BREA = manyFloraAttribute.STRE_BREA
                val STRE_COVE = manyFloraAttribute.STRE_COVE

                println("STRE_NUM $STRE_NUM STRE_SPEC $STRE_FAMI STRE_FAMI $STRE_FAMI STRE_SCIEN $STRE_SCIEN STRE_H $STRE_H")

                if (STRE_SPEC!!.length > 0 || STRE_H!! != 0.0f || STRE_BREA!! != 0.0f || STRE_COVE != 0.0f){
                    val data = TreeData1(PK,STRE_NUM,STRE_SPEC,STRE_FAMI,STRE_SCIEN,STRE_H,STRE_BREA,STRE_COVE)
                    StreDatas.add(data)
                    strepage = manyFloraAttribute.STRE_NUM!!
                    strenumTV.setText(manyFloraAttribute.STRE_NUM.toString())
                    etSTRE_SPECET.setText(manyFloraAttribute.STRE_SPEC)
                    etSTRE_FAMIET.setText(manyFloraAttribute.STRE_FAMI)
                    etSTRE_SCIENET.setText(manyFloraAttribute.STRE_SCIEN)
                    etSTRE_HET.setText(manyFloraAttribute.STRE_H.toString())
                    etSTRE_BREAET.setText(manyFloraAttribute.STRE_BREA.toString())
                    etSTRE_COVEET.setText(manyFloraAttribute.STRE_COVE.toString())
                    println("STRE_NUM ${data.PAGE}")
                }

                //관목층

                val SHR_NUM = manyFloraAttribute.SHR_NUM
                val SHR_SPEC = manyFloraAttribute.SHR_SPEC
                val SHR_FAMI = manyFloraAttribute.SHR_FAMI
                val SHR_SCIEN = manyFloraAttribute.SHR_SCIEN
                val SHR_H = manyFloraAttribute.SHR_H
                val SHR_COVE = manyFloraAttribute.SHR_COVE

                println("SHR_NUM $SHR_NUM SHR_SPEC $SHR_SPEC SHR_FAMI $SHR_FAMI SHR_SCIEN $SHR_SCIEN SHR_H $SHR_H")

                if (SHR_SPEC!!.length > 0 || SHR_H!! != 0.0f || SHR_COVE != 0.0f){
                    shrpage = manyFloraAttribute.SHR_NUM!!
                    val data = TreeData2(PK,SHR_NUM,SHR_SPEC,SHR_FAMI,SHR_SCIEN,SHR_H,SHR_COVE)
                    ShrDatas.add(data)
                    shrnumTV.setText(manyFloraAttribute.SHR_NUM.toString())
                    etSHR_SPECET.setText(manyFloraAttribute.SHR_SPEC)
                    etSHR_FAMIET.setText(manyFloraAttribute.SHR_FAMI)
                    etSHR_SCIENET.setText(manyFloraAttribute.SHR_SCIEN)
                    etSHR_HET.setText(manyFloraAttribute.SHR_H.toString())
                    etSTR_COVEET.setText(manyFloraAttribute.STRE_COVE.toString())
                    println("SHR_NUM ${data.PAGE}")
                }

                val HER_NUM = manyFloraAttribute.HER_NUM
                val HER_SPEC = manyFloraAttribute.HER_SPEC
                val HER_FAMI = manyFloraAttribute.HER_FAMI
                val HER_SCIEN = manyFloraAttribute.HER_SCIEN
                val HER_H = manyFloraAttribute.HER_H
                val HER_COVE = manyFloraAttribute.HER_COVE

                println("HER_NUM $HER_NUM HER_SPEC $HER_SPEC HER_FAMI $HER_FAMI HER_SCIEN $HER_SCIEN HER_H $HER_H")

                if (HER_SPEC!!.length > 0 || HER_H!! != 0.0f || HER_COVE != 0.0f){
                    herpage = manyFloraAttribute.HER_NUM!!
                    val data = TreeData2(PK,HER_NUM,HER_SPEC,HER_FAMI,HER_SCIEN,HER_H,HER_COVE)
                    HerDatas.add(data)
                    hernumTV.setText(manyFloraAttribute.HER_NUM.toString())
                    etHER_SPECET.setText(manyFloraAttribute.HER_SPEC)
                    etHER_FAMIET.setText(manyFloraAttribute.HER_FAMI)
                    etHER_SCIENET.setText(manyFloraAttribute.HER_SCIEN)
                    etHER_HET.setText(manyFloraAttribute.HER_H.toString())
                    etHER_COVEET.setText(manyFloraAttribute.HER_COVE.toString())
                    println("HER_NUM ${data.PAGE}")
                }

                gpslatTV.setText(manyFloraAttribute.GPS_LAT.toString())
                gpslonTV.setText(manyFloraAttribute.GPS_LON.toString())

                AllDatas.add(manyFloraAttribute)

            }

            var treDataSize = TreDatas.size + 1
            var streDataSize = StreDatas.size + 1
            var shrDataSize = ShrDatas.size + 1
            var herDataSize = HerDatas.size + 1

            if (TreDatas.size > 0){
                treDataSize = TreDatas.size
                trepage = TreDatas.size
            }

            if (StreDatas.size > 0){
                streDataSize = StreDatas.size
                strepage = StreDatas.size
            }

            if (ShrDatas.size > 0){
                shrDataSize = ShrDatas.size
                shrpage = ShrDatas.size
            }

            if (HerDatas.size > 0){
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

            for (i in 0 until TreDatas.size){
                println("TER_NUM ${TreDatas.get(i).PAGE} TER_SPEC ${TreDatas.get(i).FAMI}")
            }

            for (i in 0..StreDatas.size - 1){
                println("STER_NUM ${StreDatas.get(i).PAGE} STER_SPEC ${StreDatas.get(i).FAMI}")
            }

            for (i in 0..ShrDatas.size - 1){
                println("SHR_NUM ${ShrDatas.get(i).PAGE} SHR_SPEC ${ShrDatas.get(i).FAMI}")
            }

            for (i in 0..HerDatas.size - 1){
                println("HER_NUM ${HerDatas.get(i).PAGE} HER_SPEC ${HerDatas.get(i).FAMI}")
            }
            deleteBT.visibility = View.VISIBLE

            data.close()
        }

        if(intent.getStringExtra("id") != null){
            pk = intent.getStringExtra("id")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata= db!!.query("base_info", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        while(basedata.moveToNext()){

            basechkdata = true

            var base : Base = Base(basedata.getInt(0) , basedata.getString(1), basedata.getString(2), basedata.getString(3), basedata.getString(4), basedata.getString(5) , basedata.getString(6),basedata.getString(7))

            invpersonTV.setText(base.INV_PERSON)
            invdtTV.setText(base.INV_DT)

            gpslatTV.setText(base.GPS_LAT)
            gpslonTV.setText(base.GPS_LON)

            lat = base.GPS_LAT!!
            log = base.GPS_LON!!

            try {
                var geocoder: Geocoder = Geocoder(context);

                var list:List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

                if(list.size > 0){
                    System.out.println("list : " + list);

                    invregionTV.setText(list.get(0).getAddressLine(0));
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }

        }

        if(basechkdata){

        }else {

            val base : Base = Base(null,keyId,"",lat,log,invpersonTV.text.toString(),invdtTV.text.toString(),"0")

            dbManager!!.insertbase(base)

        }

        if(intent.getStringExtra("id") != null){
            pk = intent.getStringExtra("id")
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

            if (trepage == 1){
                for (i in 0 until TreDatas.size){
                    if (trepage == TreDatas.get(i).PAGE){
                        val data = TreDatas.get(i)
                        TreDatas.remove(data)
                        break
                    }
                }

                etTRE_SPECET.setText("")
                etTRE_FAMIET.setText("")
                etTRE_SCIENET.setText("")
                etTRE_HET.setText("")
                etTRE_BREAET.setText("")
                etTRE_COVEET.setText("")
            }

            if (trepage > 1 ){
                for (i in 0 until TreDatas.size){
                    if (trepage == TreDatas.get(i).PAGE){
                        val data = TreDatas.get(i)
                        TreDatas.remove(data)
                        break
                    }
                }

                trepage = trepage - 1

                for (i in 0 until TreDatas.size){
                    if (trepage == TreDatas.get(i).PAGE){
                        trenumTV.setText(trepage.toString())
                        etTRE_SPECET.setText(TreDatas.get(i).SPEC)
                        etTRE_FAMIET.setText(TreDatas.get(i).FAMI)
                        etTRE_SCIENET.setText(TreDatas.get(i).SCIEN)
                        etTRE_HET.setText(TreDatas.get(i).H.toString())
                        etTRE_BREAET.setText(TreDatas.get(i).BREA.toString())
                        etTRE_COVEET.setText(TreDatas.get(i).COVE.toString())
                    }

                    if (TreDatas.get(i).PAGE!! > 1){
                        TreDatas.get(i).PAGE = TreDatas.get(i).PAGE!! - 1
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

            if (strepage == 1){
                for (i in 0 until StreDatas.size){
                    if (strepage == StreDatas.get(i).PAGE){
                        StreDatas.remove(StreDatas.get(i))
                    }
                }

                etSTRE_SPECET.setText("")
                etSTRE_FAMIET.setText("")
                etSTRE_SCIENET.setText("")
                etSTRE_HET.setText("")
                etSTRE_BREAET.setText("")
                etSTRE_COVEET.setText("")

            }

            if (strepage > 1 ){
                for (i in 0 until StreDatas.size){
                    if (strepage == StreDatas.get(i).PAGE){
                        StreDatas.remove(StreDatas.get(i))
                    }
                }

                strepage = strepage - 1

                for (i in 0 until StreDatas.size){
                    if (strepage == StreDatas.get(i).PAGE){
                        strenumTV.setText(strepage.toString())
                        etSTRE_SPECET.setText(StreDatas.get(i).SPEC)
                        etSTRE_FAMIET.setText(StreDatas.get(i).FAMI)
                        etSTRE_SCIENET.setText(StreDatas.get(i).SCIEN)
                        etSTRE_HET.setText(StreDatas.get(i).H.toString())
                        etSTRE_BREAET.setText(StreDatas.get(i).BREA.toString())
                        etSTRE_COVEET.setText(StreDatas.get(i).COVE.toString())
                    }

                    if (StreDatas.get(i).PAGE!! > 1){
                        StreDatas.get(i).PAGE = StreDatas.get(i).PAGE!! - 1
                    }
                }

                val page = strepage
                val size = strerightpageTV.text.toString().toInt() - 1

                strepageTV.setText(page.toString())
                strerightpageTV.setText(size.toString())
            }

        }

        shrcloseLL.setOnClickListener {

            if (shrpage == 1){
                for (i in 0 until ShrDatas.size){
                    if (shrpage == ShrDatas.get(i).PAGE){
                        ShrDatas.remove(ShrDatas.get(i))
                    }
                }

                etSHR_SPECET.setText("")
                etSHR_FAMIET.setText("")
                etSHR_SCIENET.setText("")
                etSHR_HET.setText("")
                etSTR_COVEET.setText("")
            }

            if (shrpage > 1 ){
                for (i in 0 until ShrDatas.size){
                    if (shrpage == ShrDatas.get(i).PAGE){
                        ShrDatas.remove(ShrDatas.get(i))
                    }
                }

                shrpage = shrpage - 1

                for (i in 0 until ShrDatas.size){
                    if (shrpage == ShrDatas.get(i).PAGE){
                        shrnumTV.setText(shrpage.toString())
                        etSHR_SPECET.setText(ShrDatas.get(i).SPEC)
                        etSHR_FAMIET.setText(ShrDatas.get(i).FAMI)
                        etSHR_SCIENET.setText(ShrDatas.get(i).SCIEN)
                        etSHR_HET.setText(ShrDatas.get(i).H.toString())
                        etSTR_COVEET.setText(ShrDatas.get(i).COVE.toString())
                    }

                    if (ShrDatas.get(i).PAGE!! > 1){
                        ShrDatas.get(i).PAGE = ShrDatas.get(i).PAGE!! - 1
                    }
                }

                val page = shrpage
                val size = shrrightpageTV.text.toString().toInt() - 1

                shrpageTV.setText(page.toString())
                shrrightpageTV.setText(size.toString())
            }

        }

        hercloseLL.setOnClickListener {

            if (herpage == 1){
                for (i in 0 until HerDatas.size){
                    if (herpage == HerDatas.get(i).PAGE){
                        HerDatas.remove(HerDatas.get(i))
                    }
                }

                etHER_SPECET.setText("")
                etHER_FAMIET.setText("")
                etHER_SCIENET.setText("")
                etHER_HET.setText("")
                etHER_COVEET.setText("")
            }

            if (herpage > 1 ){
                for (i in 0 until HerDatas.size){
                    if (herpage == HerDatas.get(i).PAGE){
                        HerDatas.remove(HerDatas.get(i))
                        break
                    }
                }

                herpage = herpage - 1

                for (i in 0 until HerDatas.size){
                    if (herpage == HerDatas.get(i).PAGE){
                        hernumTV.setText(herpage.toString())
                        etHER_SPECET.setText(HerDatas.get(i).SPEC)
                        etHER_FAMIET.setText(HerDatas.get(i).FAMI)
                        etHER_SCIENET.setText(HerDatas.get(i).SCIEN)
                        etHER_HET.setText(HerDatas.get(i).H.toString())
                        etHER_COVEET.setText(HerDatas.get(i).COVE.toString())
                    }

                    if (HerDatas.get(i).PAGE!! > 1){
                        HerDatas.get(i).PAGE = HerDatas.get(i).PAGE!! - 1
                    }
                }

                val page = herpage

                val size = herrightpageTV.text.toString().toInt() - 1

                herpageTV.setText(page.toString())
                herrightpageTV.setText(size.toString())
            }

        }


        treleftTV.setOnClickListener {

            println("leftPage $trepage")
            var division = false
            for (i in 0 until TreDatas.size){
                if (trepage == TreDatas.get(i).PAGE){
                    division = true
                }
            }

            if (division == false){
                val spec = etTRE_SPECET.text.toString()
                val fami = etTRE_FAMIET.text.toString()
                val scien = etTRE_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etTRE_HET.text.isNotEmpty()) {
                    h = etTRE_HET.text.toString().toFloat()
                }

                var brea = 0.0F

                if (etTRE_BREAET.text.isNotEmpty()) {
                    brea = etTRE_BREAET.text.toString().toFloat()
                }
                var cove = 0.0F

                if (etTRE_COVEET.text.isNotEmpty()) {
                    cove = etTRE_COVEET.text.toString().toFloat()
                }

                var tredata = TreeData1(-1 , trepage, spec, fami, scien, h, brea, cove)

                TreDatas.add(tredata)
            }

            if(trepage > 1){
                for (i in 0 until TreDatas.size){
                    if (trepage == TreDatas.get(i).PAGE){
                        TreDatas.get(i).SPEC = etTRE_SPECET.text.toString()
                        TreDatas.get(i).FAMI = etTRE_FAMIET.text.toString()
                        TreDatas.get(i).SCIEN = etTRE_SCIENET.text.toString()

                        if (etTRE_HET.text.isNotEmpty()) {
                            TreDatas.get(i).H = etTRE_HET.text.toString().toFloat()
                        }

                        if (etTRE_BREAET.text.isNotEmpty()) {
                            TreDatas.get(i).BREA = etTRE_BREAET.text.toString().toFloat()
                        }

                        if (etTRE_COVEET.text.isNotEmpty()) {
                            TreDatas.get(i).COVE = etTRE_COVEET.text.toString().toFloat()
                        }
                    }
                }

                trepage = trepage - 1
                for (i in 0..TreDatas.size-1){
                    if(trepage == TreDatas.get(i).PAGE){
                        val data = TreDatas.get(i)

                        trenumTV.setText(trepage.toString())
                        etTRE_SPECET.setText(data.SPEC)
                        etTRE_FAMIET.setText(data.FAMI)
                        etTRE_SCIENET.setText(data.SCIEN)
                        etTRE_HET.setText(data.H.toString())
                        etTRE_BREAET.setText(data.BREA.toString())
                        etTRE_COVEET.setText(data.COVE.toString())

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
            for (i in 0 until StreDatas.size){
                if (strepage == StreDatas.get(i).PAGE){
                    division = true
                }
            }

            if (division == false){
                val spec = etSTRE_SPECET.text.toString()
                val fami = etSTRE_FAMIET.text.toString()
                val scien = etSTRE_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etSTRE_HET.text.isNotEmpty()) {
                    h = etSTRE_HET.text.toString().toFloat()
                }

                var brea = 0.0F

                if (etSTRE_BREAET.text.isNotEmpty()) {
                    brea = etSTRE_BREAET.text.toString().toFloat()
                }
                var cove = 0.0F

                if (etSTRE_COVEET.text.isNotEmpty()) {
                    cove = etSTRE_COVEET.text.toString().toFloat()
                }

                var stredata = TreeData1(-1 , strepage, spec, fami, scien, h, brea, cove)

                StreDatas.add(stredata)
            }

            if(strepage > 1){
                for (i in 0 until StreDatas.size){
                    if (strepage == StreDatas.get(i).PAGE){
                        StreDatas.get(i).SPEC = etSTRE_SPECET.text.toString()
                        StreDatas.get(i).FAMI = etSTRE_FAMIET.text.toString()
                        StreDatas.get(i).SCIEN = etSTRE_SCIENET.text.toString()

                        if (etSTRE_HET.text.isNotEmpty()) {
                            StreDatas.get(i).H = etSTRE_HET.text.toString().toFloat()
                        }

                        if (etSTRE_BREAET.text.isNotEmpty()) {
                            StreDatas.get(i).BREA = etSTRE_BREAET.text.toString().toFloat()
                        }

                        if (etSTRE_COVEET.text.isNotEmpty()) {
                            StreDatas.get(i).COVE = etSTRE_COVEET.text.toString().toFloat()
                        }
                    }
                }

                strepage = strepage - 1
                for (i in 0..StreDatas.size-1){
                    if(strepage == StreDatas.get(i).PAGE){
                        val data = StreDatas.get(i)

                        strenumTV.setText(strepage.toString())
                        etSTRE_SPECET.setText(data.SPEC)
                        etSTRE_FAMIET.setText(data.FAMI)
                        etSTRE_SCIENET.setText(data.SCIEN)
                        etSTRE_HET.setText(data.H.toString())
                        etSTRE_BREAET.setText(data.BREA.toString())
                        etSTRE_COVEET.setText(data.COVE.toString())

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
            for (i in 0 until ShrDatas.size){
                if (shrpage == ShrDatas.get(i).PAGE){
                    division = true
                }
            }

            if (division == false){
                val spec = etSHR_SPECET.text.toString()
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

                var shrdata = TreeData2(-1 , shrpage, spec, fami, scien, h, cove)

                ShrDatas.add(shrdata)
            }

            if(shrpage > 1){
                for (i in 0 until ShrDatas.size){
                    if (shrpage == ShrDatas.get(i).PAGE){
                        ShrDatas.get(i).SPEC = etSHR_SPECET.text.toString()
                        ShrDatas.get(i).FAMI = etSHR_FAMIET.text.toString()
                        ShrDatas.get(i).SCIEN = etSHR_SCIENET.text.toString()

                        if (etSHR_HET.text.isNotEmpty()) {
                            ShrDatas.get(i).H = etSHR_HET.text.toString().toFloat()
                        }

                        if (etSTR_COVEET.text.isNotEmpty()) {
                            ShrDatas.get(i).COVE = etSTR_COVEET.text.toString().toFloat()
                        }
                    }
                }

                shrpage = shrpage - 1
                for (i in 0..ShrDatas.size-1){
                    if(shrpage == ShrDatas.get(i).PAGE){
                        val data = ShrDatas.get(i)

                        shrnumTV.setText(shrpage.toString())
                        etSHR_SPECET.setText(data.SPEC)
                        etSHR_FAMIET.setText(data.FAMI)
                        etSHR_SCIENET.setText(data.SCIEN)
                        etSHR_HET.setText(data.H.toString())
                        etSTR_COVEET.setText(data.COVE.toString())

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
            for (i in 0 until HerDatas.size){
                if (herpage == HerDatas.get(i).PAGE){
                    division = true
                }
            }

            if (division == false){
                val spec = etHER_SPECET.text.toString()
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

                var herdata = TreeData2(-1 , herpage, spec, fami, scien, h, cove)

                HerDatas.add(herdata)
            }

            if(herpage > 1){
                for (i in 0 until HerDatas.size){
                    if (herpage == HerDatas.get(i).PAGE){
                        HerDatas.get(i).SPEC = etHER_SPECET.text.toString()
                        HerDatas.get(i).FAMI = etHER_FAMIET.text.toString()
                        HerDatas.get(i).SCIEN = etHER_SCIENET.text.toString()

                        if (etHER_HET.text.isNotEmpty()) {
                            HerDatas.get(i).H = etHER_HET.text.toString().toFloat()
                        }

                        if (etHER_COVEET.text.isNotEmpty()) {
                            HerDatas.get(i).COVE = etHER_COVEET.text.toString().toFloat()
                        }
                    }
                }

                herpage = herpage - 1
                for (i in 0..HerDatas.size-1){
                    if(herpage == HerDatas.get(i).PAGE){
                        val data = HerDatas.get(i)

                        hernumTV.setText(herpage.toString())
                        etHER_SPECET.setText(data.SPEC)
                        etHER_FAMIET.setText(data.FAMI)
                        etHER_SCIENET.setText(data.SCIEN)
                        etHER_HET.setText(data.H.toString())
                        etHER_COVEET.setText(data.COVE.toString())

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
            if(TreDatas.size == 0 && StreDatas.size == 0 && ShrDatas.size == 0 && HerDatas.size == 0){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null,null,null,null,null,null,null,null,null,null
                                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                    ,null,null,null,null,null,null,null,null,null,null,null,null,null)

                            keyId = intent.getStringExtra("GROP_ID")

                            println("insertkeyid $keyId")

                            manyFloraAttribute.GROP_ID = keyId

                            manyFloraAttribute.INV_REGION = invregionTV.text.toString()

                            manyFloraAttribute.INV_DT = Utils.todayStr()

                            if(invpersonTV.text == null || invpersonTV.text.equals("")){
                                manyFloraAttribute.INV_PERSON = userName
                            }else {
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
                                manyFloraAttribute.TRE_H = etTRE_HET.text.toString().toFloat()
                            }

                            if (etTRE_BREAET.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_BREA = etTRE_BREAET.text.toString().toFloat()
                            }

                            if (etTRE_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.TRE_COVE = etTRE_COVEET.text.toString().toFloat()
                            }

                            val TRE_SPEC = etTRE_SPECET.text.toString()
                            if(TRE_SPEC != "" && TRE_SPEC != null){
                                manyFloraAttribute.TRE_NUM = 1
                            }

                            if (strenumTV.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_NUM = strenumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.STRE_SPEC = etSTRE_SPECET.text.toString()
                            manyFloraAttribute.STRE_FAMI = etSTRE_FAMIET.text.toString()
                            manyFloraAttribute.STRE_SCIEN = etSTRE_SCIENET.text.toString()

                            if (etSTRE_HET.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_H = etSTRE_HET.text.toString().toFloat()
                            }

                            if (etSTRE_BREAET.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_BREA = etSTRE_BREAET.text.toString().toFloat()
                            }

                            if (etSTRE_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.STRE_COVE = etSTRE_COVEET.text.toString().toFloat()
                            }

                            val STRE_SPEC = etSTRE_SPECET.text.toString()
                            if(STRE_SPEC != "" && STRE_SPEC != null){
                                manyFloraAttribute.STRE_NUM = 1
                            }

                            if (shrnumTV.text.isNotEmpty()) {
                                manyFloraAttribute.SHR_NUM = shrnumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.SHR_SPEC = etSHR_SPECET.text.toString()
                            manyFloraAttribute.SHR_FAMI = etSHR_FAMIET.text.toString()
                            manyFloraAttribute.SHR_SCIEN = etSHR_SCIENET.text.toString()

                            if (etSTR_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.SHR_H = etSTRE_HET.text.toString().toFloat()
                            }

                            if (etSTR_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.SHR_COVE = etSTR_COVEET.text.toString().toFloat()
                            }

                            val SHR_SPEC = etSHR_SPECET.text.toString()
                            if(SHR_SPEC != "" && SHR_SPEC != null){
                                manyFloraAttribute.SHR_NUM = 1
                            }

                            if (hernumTV.text.isNotEmpty()) {
                                manyFloraAttribute.HER_NUM = hernumTV.text.toString().toInt()
                            }

                            manyFloraAttribute.HER_SPEC = etHER_SPECET.text.toString()
                            manyFloraAttribute.HER_FAMI = etHER_FAMIET.text.toString()
                            manyFloraAttribute.HER_SCIEN = etHER_SCIENET.text.toString()

                            if (etHER_HET.text.isNotEmpty()) {
                                manyFloraAttribute.HER_H = etHER_HET.text.toString().toFloat()
                            }

                            if (etHER_COVEET.text.isNotEmpty()) {
                                manyFloraAttribute.HER_COVE = etHER_COVEET.text.toString().toFloat()
                            }

                            val HER_SPEC = etHER_COVEET.text.toString()
                            if(HER_SPEC != "" && HER_SPEC != null){
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

                            println("============chkdata $chkdata")

                            if (chkdata) {

                                if(pk != null){

                                    val CONF_MOD = manyFloraAttribute.CONF_MOD

                                    if(CONF_MOD == "C" || CONF_MOD == "N"){
                                        manyFloraAttribute.CONF_MOD = "M"
                                    }

                                    dbManager!!.updatemanyflora_attribute(manyFloraAttribute,pk)
                                }

                            } else {

                                dbManager!!.insertmanyflora_attribute(manyFloraAttribute);

                            }

                            var intent = Intent()

                            intent.putExtra("export", 70);

                            setResult(RESULT_OK, intent);

                            finish()

                        })

                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            } else if(TreDatas.size > 0 || StreDatas.size > 0 || ShrDatas.size > 0 || HerDatas.size > 0){

                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            println("delete-------------------- $keyId")
                            dbManager!!.deleteAllManyFloraAttribute(keyId)

                            var treChk = false

                            for (i in 0 until  TreDatas.size){
                                if (TreDatas.get(i).PAGE == trenumTV.text.toString().toInt()){
                                    treChk = true
                                }
                            }

                            if (treChk == false){
                                val spec = etTRE_SPECET.text.toString()
                                val fami = etTRE_FAMIET.text.toString()
                                val scien = etTRE_SCIENET.text.toString()

                                var h: Float = 0.0F

                                if (etTRE_HET.text.isNotEmpty()) {
                                    h = etTRE_HET.text.toString().toFloat()
                                }

                                var brea = 0.0F

                                if (etTRE_BREAET.text.isNotEmpty()) {
                                    brea = etTRE_BREAET.text.toString().toFloat()
                                }
                                var cove = 0.0F

                                if (etTRE_COVEET.text.isNotEmpty()) {
                                    cove = etTRE_COVEET.text.toString().toFloat()
                                }

                                var tredata = TreeData1(-1 , trenumTV.text.toString().toInt(), spec, fami, scien, h, brea, cove)

                                TreDatas.add(tredata)
                            }

                            var streChk = false

                            for (i in 0 until  StreDatas.size){
                                if (StreDatas.get(i).PAGE == strenumTV.text.toString().toInt()){
                                    streChk = true
                                }
                            }

                            if (streChk == false){
                                val spec = etSTRE_SPECET.text.toString()
                                val fami = etSTRE_FAMIET.text.toString()
                                val scien = etSTRE_SCIENET.text.toString()

                                var h: Float = 0.0F

                                if (etSTRE_HET.text.isNotEmpty()) {
                                    h = etSTRE_HET.text.toString().toFloat()
                                }

                                var brea = 0.0F

                                if (etSTRE_BREAET.text.isNotEmpty()) {
                                    brea = etSTRE_BREAET.text.toString().toFloat()
                                }
                                var cove = 0.0F

                                if (etSTRE_COVEET.text.isNotEmpty()) {
                                    cove = etSTRE_COVEET.text.toString().toFloat()
                                }

                                var stredata = TreeData1(-1 , strenumTV.text.toString().toInt(), spec, fami, scien, h, brea, cove)

                                StreDatas.add(stredata)
                            }

                            var shrChk = false

                            for (i in 0 until  ShrDatas.size){
                                if (ShrDatas.get(i).PAGE == shrnumTV.text.toString().toInt()){
                                    shrChk = true
                                }
                            }

                            if (shrChk == false){
                                val spec = etSHR_SPECET.text.toString()
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

                                var shrdata = TreeData2(-1 , shrnumTV.text.toString().toInt(), spec, fami, scien, h, cove)

                                ShrDatas.add(shrdata)
                            }

                            var herChk = false

                            for (i in 0 until  HerDatas.size){
                                if (HerDatas.get(i).PAGE == hernumTV.text.toString().toInt()){
                                    herChk = true
                                }
                            }

                            if (herChk == false){
                                val spec = etHER_SPECET.text.toString()
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

                                var herdata = TreeData2(-1 , hernumTV.text.toString().toInt(), spec, fami, scien, h, cove)

                                HerDatas.add(herdata)
                            }

                            var MaxLength = 0
                            var TreDataSize  = TreDatas.size
                            var StreDataSize = StreDatas.size
                            var ShrDataSize = ShrDatas.size
                            var HerDataSize = HerDatas.size

                            println("TreDataSize : $TreDataSize StreDataSize $StreDataSize ShrDataSize : $ShrDataSize HerDataSize : $HerDataSize ")

                            MaxLength = TreDatas.size

                            if(MaxLength < StreDatas.size){
                                MaxLength = StreDatas.size
                            }

                            if(MaxLength < ShrDatas.size){
                                MaxLength = ShrDatas.size
                            }

                            if(MaxLength < HerDatas.size){
                                MaxLength = HerDatas.size
                            }

                                for(i in 0..MaxLength-1) {
                                    var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null, null, null, null, null, null, null, null, null, null
                                            , null, null, null, null, null, null, null, null, null, null, null, null, null
                                            , null, null, null, null, null, null, null, null, null, null, null, null, null)

                                    keyId = intent.getStringExtra("GROP_ID")

                                    println("insertkeyid $keyId")

                                    manyFloraAttribute.GROP_ID = keyId

                                    manyFloraAttribute.INV_REGION = invregionTV.text.toString()

                                    manyFloraAttribute.INV_DT = Utils.todayStr()

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
                                            manyFloraAttribute.TRE_H = 0.0f
                                            manyFloraAttribute.TRE_BREA = 0.0f
                                            manyFloraAttribute.TRE_COVE = 0.0f
                                        }

                                        if (i <= TreDataSize - 1){
                                            manyFloraAttribute.TRE_NUM = TreDatas.get(i).PAGE
                                            manyFloraAttribute.TRE_SPEC = TreDatas.get(i).SPEC
                                            manyFloraAttribute.TRE_FAMI = TreDatas.get(i).FAMI
                                            manyFloraAttribute.TRE_SCIEN = TreDatas.get(i).SCIEN
                                            manyFloraAttribute.TRE_H = TreDatas.get(i).H
                                            manyFloraAttribute.TRE_BREA = TreDatas.get(i).BREA
                                            manyFloraAttribute.TRE_COVE = TreDatas.get(i).COVE
                                        }
                                    }

                                    if (StreDatas != null  && StreDataSize > 0) {
                                        if (i > StreDataSize - 1 ) {
                                            manyFloraAttribute.STRE_NUM = 1
                                            manyFloraAttribute.STRE_SPEC = ""
                                            manyFloraAttribute.STRE_FAMI = ""
                                            manyFloraAttribute.STRE_SCIEN = ""
                                            manyFloraAttribute.STRE_H = 0.0f
                                            manyFloraAttribute.STRE_BREA = 0.0f
                                            manyFloraAttribute.STRE_COVE = 0.0f
                                        }

                                        if (i <= StreDataSize - 1){
                                            manyFloraAttribute.STRE_NUM = StreDatas.get(i).PAGE
                                            println("insert -----${StreDatas.get(i).PAGE}")
                                            manyFloraAttribute.STRE_SPEC = StreDatas.get(i).SPEC
                                            manyFloraAttribute.STRE_FAMI = StreDatas.get(i).FAMI
                                            manyFloraAttribute.STRE_SCIEN = StreDatas.get(i).SCIEN
                                            manyFloraAttribute.STRE_H = StreDatas.get(i).H
                                            manyFloraAttribute.STRE_BREA = StreDatas.get(i).BREA
                                            manyFloraAttribute.STRE_COVE = StreDatas.get(i).COVE
                                        }
                                    }


                                    if(ShrDatas != null && ShrDataSize > 0 ) {
                                        if (i > ShrDataSize - 1) {
                                            manyFloraAttribute.SHR_NUM = 1
                                            manyFloraAttribute.SHR_SPEC = ""
                                            manyFloraAttribute.SHR_FAMI = ""
                                            manyFloraAttribute.SHR_SCIEN = ""
                                            manyFloraAttribute.SHR_H = 0.0f
                                            manyFloraAttribute.SHR_COVE = 0.0f

                                        }
                                        if (i <= ShrDataSize - 1){
                                            manyFloraAttribute.SHR_NUM = ShrDatas.get(i).PAGE
                                            manyFloraAttribute.SHR_SPEC = ShrDatas.get(i).SPEC
                                            manyFloraAttribute.SHR_FAMI = ShrDatas.get(i).FAMI
                                            manyFloraAttribute.SHR_SCIEN = ShrDatas.get(i).SCIEN
                                            manyFloraAttribute.SHR_H = ShrDatas.get(i).H
                                            manyFloraAttribute.SHR_COVE = ShrDatas.get(i).COVE
                                        }
                                    }

                                    if(HerDatas != null && HerDataSize > 0) {
                                        if (i > HerDataSize - 1) {
                                            manyFloraAttribute.HER_NUM = 1
                                            manyFloraAttribute.HER_SPEC = ""
                                            manyFloraAttribute.HER_FAMI = ""
                                            manyFloraAttribute.HER_SCIEN = ""
                                            manyFloraAttribute.HER_H = 0.0f
                                            manyFloraAttribute.HER_COVE = 0.0f
                                        }
                                        if (i <= HerDataSize - 1){
                                            manyFloraAttribute.HER_NUM = HerDatas.get(i).PAGE
                                            manyFloraAttribute.HER_SPEC = HerDatas.get(i).SPEC
                                            manyFloraAttribute.HER_FAMI = HerDatas.get(i).FAMI
                                            manyFloraAttribute.HER_SCIEN = HerDatas.get(i).SCIEN
                                            manyFloraAttribute.HER_H = HerDatas.get(i).H
                                            manyFloraAttribute.HER_COVE = HerDatas.get(i).COVE
                                        }
                                    }

                                    dbManager!!.insertmanyflora_attribute(manyFloraAttribute);

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

                        val data= db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

                        if (dataArray != null){
                            dataArray.clear()
                        }

                        while (data.moveToNext()) {

                            var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                                    data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                                    , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                                    , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                                    ,data.getFloat(33),data.getString(34),data.getString(35))

                            dataArray.add(manyFloraAttribute)
                        }

                        println("cencle dataArrayListSize ${dataArray.size}")

                        if (dataArray.size == 0 || intent.getStringExtra("id") == null ){

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
                            var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null,null,null,null,null,null,null,null,null,null
                                    ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                    ,null,null,null,null,null,null,null,null,null,null,null,null,null)


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

                                        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                                                data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                                                , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                                                , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                                                ,data.getFloat(33),data.getString(34),data.getString(35))

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
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

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

                                    var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                                            data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                                            , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                                            , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                                            ,data.getFloat(33),data.getString(34),data.getString(35))


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

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var number: Number

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {


                SET_DATA2 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etTRE_SPECET.setText(name)
                    etTRE_FAMIET.setText(family_name)
                    etTRE_SCIENET.setText(zoological)

                }

                SET_DATA3 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etSTRE_SPECET.setText(name)
                    etSTRE_FAMIET.setText(family_name)
                    etSTRE_SCIENET.setText(zoological)

                }

                SET_DATA4 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etSHR_SPECET.setText(name)
                    etSHR_FAMIET.setText(family_name)
                    etSHR_SCIENET.setText(zoological)

                }

                SET_DATA5 -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    etHER_SPECET.setText(name)
                    etHER_FAMIET.setText(family_name)
                    etHER_SCIENET.setText(zoological)

                }

            }
        }
    }

    override fun onBackPressed() {
        val dataList: Array<String> = arrayOf("*");

        println("keyid $keyId")

        val data= db!!.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

        if (dataArray != null){
            dataArray.clear()
        }

        while (data.moveToNext()) {

            var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                    data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                    , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                    , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                    ,data.getFloat(33),data.getString(34),data.getString(35))

            dataArray.add(manyFloraAttribute)
        }

        if (dataArray.size == 0 || intent.getStringExtra("id") == null ){

            var intent = Intent()

            intent.putExtra("markerid", markerid)
            setResult(RESULT_OK, intent);

        }

        data.close()

        finish()
    }

    fun AddTreFlora(){

        var chkData = false

        var equlas = false

        println("trepage $trepage")

        val maxsize = trerightpageTV.text.toString().toInt()

        if(trepage == maxsize){

            if (trepage > 1 ){

                val spec = etTRE_SPECET.text.toString()
                val fami = etTRE_FAMIET.text.toString()
                val scien = etTRE_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etTRE_HET.text.isNotEmpty()) {
                    h = etTRE_HET.text.toString().toFloat()
                }

                var brea = 0.0F

                if (etTRE_BREAET.text.isNotEmpty()) {
                    brea = etTRE_BREAET.text.toString().toFloat()
                }
                var cove = 0.0F

                if (etTRE_COVEET.text.isNotEmpty()) {
                    cove = etTRE_COVEET.text.toString().toFloat()
                }

                var tredata = TreeData1(-1 , trepage, spec, fami, scien, h, brea, cove)

                TreDatas.add(tredata)

                trepage = trepage + 1

                for (i in 0..TreDatas.size - 1) {
                    println("Tredatas ${TreDatas.get(i).PAGE}")
                }

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
            }

            if (trepage == 1 ){
                val spec = etTRE_SPECET.text.toString()
                val fami = etTRE_FAMIET.text.toString()
                val scien = etTRE_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etTRE_HET.text.isNotEmpty()) {
                    h = etTRE_HET.text.toString().toFloat()
                }

                var brea = 0.0F

                if (etTRE_BREAET.text.isNotEmpty()) {
                    brea = etTRE_BREAET.text.toString().toFloat()
                }
                var cove = 0.0F

                if (etTRE_COVEET.text.isNotEmpty()) {
                    cove = etTRE_COVEET.text.toString().toFloat()
                }

                var tredata = TreeData1(-1 ,trepage, spec, fami, scien, h, brea, cove)

                TreDatas.add(tredata)

                trepage = trepage + 1

                for (i in 0..TreDatas.size - 1) {
                    println("Tredatas ${TreDatas.get(i).PAGE}")
                    println("Tredatas ${TreDatas.get(i).SPEC}")
                    println("Tredatas ${TreDatas.get(i).FAMI}")
                    println("Tredatas ${TreDatas.get(i).SCIEN}")
                    println("Tredatas ${TreDatas.get(i).H}")
                    println("Tredatas ${TreDatas.get(i).BREA}")
                    println("Tredatas ${TreDatas.get(i).COVE}")
                }

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

            }
        }
        if (trepage < maxsize){
            trepage = trepage + 1
            var chk = false
            for (i in 0..TreDatas.size-1) {
                if (trepage == TreDatas.get(i).PAGE) {
                    chk = true
                    val data = TreDatas.get(i)
                    etTRE_SPECET.setText(data.SPEC)
                    etTRE_FAMIET.setText(data.FAMI)
                    etTRE_SCIENET.setText(data.SCIEN)
                    etTRE_HET.setText(data.H.toString())
                    etTRE_BREAET.setText(data.BREA.toString())
                    etTRE_COVEET.setText(data.COVE.toString())

                    val page = trepage
                    val size = trerightpageTV.text.toString().toInt()

                    trepageTV.setText(page.toString())
                    trerightpageTV.setText(size.toString())
                    trenumTV.setText(page.toString())
                }
            }

            if (chk == false){
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
            }
        }

        deleteBT.visibility = View.GONE

    }

    fun AddStreFlora(){

        var chkData = false

        var equlas = false

        println("trepage $strepage")

        val maxsize = strerightpageTV.text.toString().toInt()

        if(strepage == maxsize){

            equlas = true
            if (strepage > 1 ){

                val spec = etSTRE_SPECET.text.toString()
                val fami = etSTRE_FAMIET.text.toString()
                val scien = etSTRE_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etSTRE_HET.text.isNotEmpty()) {
                    h = etSTRE_HET.text.toString().toFloat()
                }

                var brea = 0.0F

                if (etSTRE_BREAET.text.isNotEmpty()) {
                    brea = etSTRE_BREAET.text.toString().toFloat()
                }
                var cove = 0.0F

                if (etSTRE_COVEET.text.isNotEmpty()) {
                    cove = etSTRE_COVEET.text.toString().toFloat()
                }

                var stredata = TreeData1(-1 , strepage, spec, fami, scien, h, brea, cove)

                StreDatas.add(stredata)
                println("addstre $strepage")

                strepage = strepage + 1

                for (i in 0..StreDatas.size - 1) {
                    println("Stredatas ${StreDatas.get(i).PAGE}")
                }

                val page = strepage
                val size = strerightpageTV.text.toString().toInt() + 1

                strepageTV.setText(page.toString())
                strerightpageTV.setText(size.toString())
                strenumTV.setText(page.toString())
                etSTRE_SPECET.setText("")
                etSTRE_FAMIET.setText("")
                etSTRE_SCIENET.setText("")
                etSTRE_HET.setText("")
                etSTRE_BREAET.setText("")
                etSTRE_COVEET.setText("")
            }

            if (strepage == 1 ){
                val spec = etSTRE_SPECET.text.toString()
                val fami = etSTRE_FAMIET.text.toString()
                val scien = etSTRE_SCIENET.text.toString()

                var h: Float = 0.0F

                if (etSTRE_HET.text.isNotEmpty()) {
                    h = etSTRE_HET.text.toString().toFloat()
                }

                var brea = 0.0F

                if (etSTRE_BREAET.text.isNotEmpty()) {
                    brea = etSTRE_BREAET.text.toString().toFloat()
                }
                var cove = 0.0F

                if (etSTRE_COVEET.text.isNotEmpty()) {
                    cove = etSTRE_COVEET.text.toString().toFloat()
                }

                var stredata = TreeData1(-1 ,strepage, spec, fami, scien, h, brea, cove)
                println("addstre $strepage")

                StreDatas.add(stredata)

                strepage = strepage + 1

                val page = strepage
                val size = strerightpageTV.text.toString().toInt() + 1

                strepageTV.setText(page.toString())
                strerightpageTV.setText(size.toString())
                strenumTV.setText(page.toString())
                etSTRE_SPECET.setText("")
                etSTRE_FAMIET.setText("")
                etSTRE_SCIENET.setText("")
                etSTRE_HET.setText("")
                etSTRE_BREAET.setText("")
                etSTRE_COVEET.setText("")

            }
        }
        if (strepage < maxsize){
            strepage = strepage + 1
            var chk = false
            for (i in 0..StreDatas.size-1) {
                if (strepage == StreDatas.get(i).PAGE) {
                    chk = true
                    val data = StreDatas.get(i)
                    etSTRE_SPECET.setText(data.SPEC)
                    etSTRE_FAMIET.setText(data.FAMI)
                    etSTRE_SCIENET.setText(data.SCIEN)
                    etSTRE_HET.setText(data.H.toString())
                    etSTRE_BREAET.setText(data.BREA.toString())
                    etSTRE_COVEET.setText(data.COVE.toString())

                    val page = strepage
                    val size = strerightpageTV.text.toString().toInt()

                    strepageTV.setText(page.toString())
                    strerightpageTV.setText(size.toString())
                    strenumTV.setText(page.toString())
                }
            }

            if (chk == false){
                val page = strepage
                val size = strerightpageTV.text.toString().toInt() + 1

                strepageTV.setText(page.toString())
                strerightpageTV.setText(size.toString())
                strenumTV.setText(page.toString())
                etSTRE_SPECET.setText("")
                etSTRE_FAMIET.setText("")
                etSTRE_SCIENET.setText("")
                etSTRE_HET.setText("")
                etSTRE_BREAET.setText("")
                etSTRE_COVEET.setText("")
            }
        }

        deleteBT.visibility = View.GONE
    }

    fun AddShrFlora(){
        var chkData = false

        var equlas = false

        println("shrpage $shrpage")

        val maxsize = shrrightpageTV.text.toString().toInt()

        if(shrpage == maxsize){

            equlas = true
            if (shrpage > 1 ){

                val spec = etSHR_SPECET.text.toString()
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

                var shrdata = TreeData2(-1 , shrpage, spec, fami, scien, h, cove)

                ShrDatas.add(shrdata)

                shrpage = shrpage + 1

                for (i in 0..ShrDatas.size - 1) {
                    println("ShrDatas ${ShrDatas.get(i).PAGE}")
                }

                val page = shrpage
                val size = shrrightpageTV.text.toString().toInt() + 1

                shrpageTV.setText(page.toString())
                shrrightpageTV.setText(size.toString())
                shrnumTV.setText(page.toString())
                etSHR_SPECET.setText("")
                etSHR_FAMIET.setText("")
                etSHR_SCIENET.setText("")
                etSHR_HET.setText("")
                etSTR_COVEET.setText("")
            }

            if (shrpage == 1 ){
                val spec = etSHR_SPECET.text.toString()
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

                var shrdata = TreeData2(-1 ,shrpage, spec, fami, scien, h, cove)

                ShrDatas.add(shrdata)

                shrpage = shrpage + 1

                val page = shrpage
                val size = shrrightpageTV.text.toString().toInt() + 1

                shrpageTV.setText(page.toString())
                shrrightpageTV.setText(size.toString())
                shrnumTV.setText(page.toString())
                etSHR_SPECET.setText("")
                etSHR_FAMIET.setText("")
                etSHR_SCIENET.setText("")
                etSHR_HET.setText("")
                etSTR_COVEET.setText("")

            }
        }
        if (shrpage < maxsize){
            shrpage = shrpage + 1
            var chk = false
            for (i in 0..ShrDatas.size-1) {
                if (shrpage == ShrDatas.get(i).PAGE) {
                    chk = true
                    val data = ShrDatas.get(i)
                    etSHR_SPECET.setText(data.SPEC)
                    etSHR_FAMIET.setText(data.FAMI)
                    etSHR_SCIENET.setText(data.SCIEN)
                    etSHR_HET.setText(data.H.toString())
                    etSTR_COVEET.setText(data.COVE.toString())

                    val page = shrpage
                    val size = shrrightpageTV.text.toString().toInt()

                    shrpageTV.setText(page.toString())
                    shrrightpageTV.setText(size.toString())
                    shrnumTV.setText(page.toString())
                }
            }

            if (chk == false){
                val page = shrpage
                val size = shrrightpageTV.text.toString().toInt() + 1

                shrpageTV.setText(page.toString())
                shrrightpageTV.setText(size.toString())
                shrnumTV.setText(page.toString())
                etSHR_SPECET.setText("")
                etSHR_FAMIET.setText("")
                etSHR_SCIENET.setText("")
                etSHR_HET.setText("")
                etSTR_COVEET.setText("")
            }
        }
        deleteBT.visibility = View.GONE
    }

    fun AddHerFlora(){

        var chkData = false

        var equlas = false

        println("herpage $herpage")

        val maxsize = herrightpageTV.text.toString().toInt()

        if(herpage == maxsize){

            equlas = true
            if (herpage > 1 ){

                val spec = etHER_SPECET.text.toString()
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

                var herdata = TreeData2(-1 , herpage, spec, fami, scien, h, cove)

                HerDatas.add(herdata)

                herpage = herpage + 1

                for (i in 0..HerDatas.size - 1) {
                    println("HerDatas ${HerDatas.get(i).PAGE}")
                }

                val page = herpage
                val size = herrightpageTV.text.toString().toInt() + 1

                herpageTV.setText(page.toString())
                herrightpageTV.setText(size.toString())
                hernumTV.setText(page.toString())
                etHER_SPECET.setText("")
                etHER_FAMIET.setText("")
                etHER_SCIENET.setText("")
                etHER_HET.setText("")
                etHER_COVEET.setText("")
            }

            if (herpage == 1 ){
                val spec = etHER_SPECET.text.toString()
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

                var herdata = TreeData2(-1 ,herpage, spec, fami, scien, h, cove)

                HerDatas.add(herdata)

                herpage = herpage + 1

                val page = herpage
                val size = herrightpageTV.text.toString().toInt() + 1

                herpageTV.setText(page.toString())
                herrightpageTV.setText(size.toString())
                hernumTV.setText(page.toString())
                etHER_SPECET.setText("")
                etHER_FAMIET.setText("")
                etHER_SCIENET.setText("")
                etHER_HET.setText("")
                etHER_COVEET.setText("")

            }
        }
        if (herpage < maxsize){
            herpage = herpage + 1
            var chk = false
            for (i in 0..HerDatas.size-1) {
                if (herpage == HerDatas.get(i).PAGE) {
                    chk = true
                    val data = HerDatas.get(i)
                    etHER_SPECET.setText(data.SPEC)
                    etHER_FAMIET.setText(data.FAMI)
                    etHER_SCIENET.setText(data.SCIEN)
                    etHER_HET.setText(data.H.toString())
                    etHER_COVEET.setText(data.COVE.toString())

                    val page = herpage
                    val size = herrightpageTV.text.toString().toInt()

                    herpageTV.setText(page.toString())
                    herrightpageTV.setText(size.toString())
                    hernumTV.setText(page.toString())
                }
            }

            if (chk == false){
                val page = herpage
                val size = herrightpageTV.text.toString().toInt() + 1

                herpageTV.setText(page.toString())
                herrightpageTV.setText(size.toString())
                hernumTV.setText(page.toString())
                etHER_SPECET.setText("")
                etHER_FAMIET.setText("")
                etHER_SCIENET.setText("")
                etHER_HET.setText("")
                etHER_COVEET.setText("")
            }
        }

        deleteBT.visibility = View.GONE
    }

    fun clear(){
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
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        etHER_HET.setText("")
        etHER_COVEET.setText("")
    }

    fun TreClear(page: String){
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

    fun Stre_Clear(page: String){
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

    fun Shr_Clear(page: String){
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

    fun Her_Clear(page: String){
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()
//        val HERNUM = dbManager.manyflorahernumNext()

        hernumTV.setText(page)
        etHER_SPECET.setText("")
        etHER_FAMIET.setText("")
        etHER_SCIENET.setText("")
        etHER_HET.setText("")
        etHER_COVEET.setText("")
    }

    fun insert(){

        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null,null,null,null,null,null,null,null,null,null
                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                ,null,null,null,null,null,null,null,null,null,null,null,null,null)

        keyId = intent.getStringExtra("GROP_ID")

        println("insertkeyid $keyId")

        manyFloraAttribute.GROP_ID = keyId

        manyFloraAttribute.INV_REGION = invregionTV.text.toString()

        manyFloraAttribute.INV_DT = Utils.todayStr()

        if(invpersonTV.text == null || invpersonTV.text.equals("")){
            manyFloraAttribute.INV_PERSON = userName
        }else {
            manyFloraAttribute.INV_PERSON = invpersonTV.text.toString()
        }

        manyFloraAttribute.INV_TM = Utils.timeStr()

        var treChk = false

        for (i in 0 until  TreDatas.size){
            if (TreDatas.get(i).PAGE == trenumTV.text.toString().toInt()){
              treChk = true
            }
        }

        if (treChk == false){
            if (trenumTV.text.isNotEmpty()) {
                manyFloraAttribute.TRE_NUM = trenumTV.text.toString().toInt()
            }

            manyFloraAttribute.TRE_SPEC = etTRE_SPECET.text.toString()
            manyFloraAttribute.TRE_FAMI = etTRE_FAMIET.text.toString()
            manyFloraAttribute.TRE_SCIEN = etTRE_SCIENET.text.toString()

            if (etTRE_HET.text.isNotEmpty()) {
                manyFloraAttribute.TRE_H = etTRE_HET.text.toString().toFloat()
            }

            if (etTRE_BREAET.text.isNotEmpty()) {
                manyFloraAttribute.TRE_BREA = etTRE_BREAET.text.toString().toFloat()
            }

            if (etTRE_COVEET.text.isNotEmpty()) {
                manyFloraAttribute.TRE_COVE = etTRE_COVEET.text.toString().toFloat()
            }
        }

        var streChk = false

        for (i in 0 until  StreDatas.size){
            if (StreDatas.get(i).PAGE == strenumTV.text.toString().toInt()){
                streChk = true
            }
        }

        if (streChk == false){
            if (strenumTV.text.isNotEmpty()) {
                manyFloraAttribute.STRE_NUM = strenumTV.text.toString().toInt()
            }

            manyFloraAttribute.STRE_SPEC = etSTRE_SPECET.text.toString()
            manyFloraAttribute.STRE_FAMI = etSTRE_FAMIET.text.toString()
            manyFloraAttribute.STRE_SCIEN = etSTRE_SCIENET.text.toString()

            if (etSTRE_HET.text.isNotEmpty()) {
                manyFloraAttribute.STRE_H = etSTRE_HET.text.toString().toFloat()
            }

            if (etSTRE_BREAET.text.isNotEmpty()) {
                manyFloraAttribute.STRE_BREA = etSTRE_BREAET.text.toString().toFloat()
            }

            if (etSTRE_COVEET.text.isNotEmpty()) {
                manyFloraAttribute.STRE_COVE = etSTRE_COVEET.text.toString().toFloat()
            }
        }

        var shrChk = false

        for (i in 0 until  ShrDatas.size){
            if (ShrDatas.get(i).PAGE == shrnumTV.text.toString().toInt()){
                shrChk = true
            }
        }

        if (shrChk == false){
            if (shrnumTV.text.isNotEmpty()) {
                manyFloraAttribute.SHR_NUM = shrnumTV.text.toString().toInt()
            }

            manyFloraAttribute.SHR_SPEC = etSHR_SPECET.text.toString()
            manyFloraAttribute.SHR_FAMI = etSHR_FAMIET.text.toString()
            manyFloraAttribute.SHR_SCIEN = etSHR_SCIENET.text.toString()

            if (etSTR_COVEET.text.isNotEmpty()) {
                manyFloraAttribute.SHR_H = etSTRE_HET.text.toString().toFloat()
            }

            if (etSTR_COVEET.text.isNotEmpty()) {
                manyFloraAttribute.SHR_COVE = etSTR_COVEET.text.toString().toFloat()
            }
        }

        var herChk = false

        for (i in 0 until  HerDatas.size){
            if (HerDatas.get(i).PAGE == hernumTV.text.toString().toInt()){
                herChk = true
            }
        }

        if (herChk == false){
            if (hernumTV.text.isNotEmpty()) {
                manyFloraAttribute.HER_NUM = hernumTV.text.toString().toInt()
            }

            manyFloraAttribute.HER_SPEC = etHER_SPECET.text.toString()
            manyFloraAttribute.HER_FAMI = etHER_FAMIET.text.toString()
            manyFloraAttribute.HER_SCIEN = etHER_SCIENET.text.toString()

            if (etHER_HET.text.isNotEmpty()) {
                manyFloraAttribute.HER_H = etHER_HET.text.toString().toFloat()
            }

            if (etHER_COVEET.text.isNotEmpty()) {
                manyFloraAttribute.HER_COVE = etHER_COVEET.text.toString().toFloat()
            }
        }

        if (gpslatTV.text.isNotEmpty()) {
            manyFloraAttribute.GPS_LAT = gpslatTV.text.toString().toFloat()
        }

        if (gpslonTV.text.isNotEmpty()) {
            manyFloraAttribute.GPS_LON = gpslonTV.text.toString().toFloat()
        }

        manyFloraAttribute.TEMP_YN = "Y"
        manyFloraAttribute.CONF_MOD = "N"

        dbManager!!.insertmanyflora_attribute(manyFloraAttribute);

        var intent = Intent()

        intent.putExtra("export", 70);

        setResult(RESULT_OK, intent);

        finish()
    }

}


