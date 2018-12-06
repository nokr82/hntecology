package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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

    var trepage = 0
    var strepage = 0
    var shrpage = 0
    var herpage = 0

    var TreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var StreDatas: ArrayList<TreeData1> = ArrayList<TreeData1>()
    var ShrDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()
    var HerDatas: ArrayList<TreeData2> = ArrayList<TreeData2>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flora2)

        this.context = this;

        window.setGravity(Gravity.RIGHT);

        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

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

        if(intent.getStringExtra("GROP_ID") != null){

            var AllDatas:ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()

            val grop_id = intent.getStringExtra("GROP_ID")

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$grop_id'", null, null, null, "", null)

            while(data.moveToNext()){

                var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                        data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                        , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                        , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                        ,data.getFloat(33),data.getString(34),data.getString(35))

                AllDatas.add(manyFloraAttribute)
            }

            if(AllDatas != null){
                for (i in 0..AllDatas.size -1){
                    trepage = AllDatas.get(i).TRE_NUM!!.toInt()
                    strepage = AllDatas.get(i).STRE_NUM!!.toInt()
                    shrpage = AllDatas.get(i).SHR_NUM!!.toInt()
                    herpage = AllDatas.get(i).HER_NUM!!.toInt()
                }

                for (i in 0..trepage - 1){
                    val data = TreeData1(AllDatas.get(i).TRE_NUM,AllDatas.get(i).TRE_SPEC,AllDatas.get(i).TRE_FAMI,AllDatas.get(i).TRE_SCIEN,AllDatas.get(i).TRE_H,AllDatas.get(i).TRE_BREA,AllDatas.get(i).TRE_COVE)
                    TreDatas.add(data)
                }

                for (i in 0..strepage - 1){
                    val data = TreeData1(AllDatas.get(i).STRE_NUM,AllDatas.get(i).STRE_SPEC,AllDatas.get(i).STRE_FAMI,AllDatas.get(i).STRE_SCIEN,AllDatas.get(i).STRE_H,AllDatas.get(i).STRE_BREA,AllDatas.get(i).STRE_COVE)
                    StreDatas.add(data)
                }

                for (i in 0..shrpage - 1){
                    val data = TreeData2(AllDatas.get(i).SHR_NUM,AllDatas.get(i).SHR_SPEC,AllDatas.get(i).SHR_FAMI,AllDatas.get(i).SHR_SCIEN,AllDatas.get(i).SHR_H,AllDatas.get(i).STRE_COVE)
                    ShrDatas.add(data)
                }

                for (i in 0..herpage - 1){
                    val data = TreeData2(AllDatas.get(i).HER_NUM,AllDatas.get(i).HER_SPEC,AllDatas.get(i).HER_FAMI,AllDatas.get(i).HER_SCIEN,AllDatas.get(i).HER_H,AllDatas.get(i).HER_COVE)
                    HerDatas.add(data)
                }

            }

            trepageTV.setText(trepage.toString() + " / "+ trepage.toString())
            strepageTV.setText(strepage.toString() + " / "+ strepage.toString())
            shrpageTV.setText(shrpage.toString() + " / "+ strepage.toString())
            herpageTV.setText(herpage.toString() + " / "+ strepage.toString())

        }

        if(intent.getStringExtra("id") != null){
            pk = intent.getStringExtra("id")
        }

        val dataList: Array<String> = arrayOf("*");

        var basedata= db.query("Base", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

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

            dbManager.insertbase(base)

        }

        if(intent.getStringExtra("id") != null){
            pk = intent.getStringExtra("id")
        }

        if (intent.getStringExtra("id") != null) {

            deleteBT.visibility = View.VISIBLE

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("ManyFloraAttribute", dataList, "id = '$pk'", null, null, null, "", null)

            while (data.moveToNext()) {
                chkdata = true
                var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                        data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getInt(13), data.getString(14)
                        , data.getString(15),data.getString(16), data.getFloat(17), data.getFloat(18), data.getFloat(19), data.getInt(20), data.getString(21), data.getString(22)
                        , data.getString(23), data.getFloat(24), data.getFloat(25), data.getInt(26), data.getString(27), data.getString(28),data.getString(29),data.getFloat(30),data.getFloat(31),data.getFloat(32)
                        ,data.getFloat(33),data.getString(34),data.getString(35))

                invregionTV.setText(manyFloraAttribute.INV_REGION)
                invdtTV.setText(manyFloraAttribute.INV_DT)
                invpersonTV.setText(manyFloraAttribute.INV_PERSON)
                trenumTV.setText(manyFloraAttribute.TRE_NUM.toString())
                etTRE_SPECET.setText(manyFloraAttribute.TRE_SPEC)
                etTRE_FAMIET.setText(manyFloraAttribute.TRE_FAMI)
                etTRE_SCIENET.setText(manyFloraAttribute.TRE_SCIEN)
                etTRE_HET.setText(manyFloraAttribute.TRE_H.toString())
                etTRE_BREAET.setText(manyFloraAttribute.TRE_BREA.toString())
                etTRE_COVEET.setText(manyFloraAttribute.TRE_COVE.toString())
                strenumTV.setText(manyFloraAttribute.STRE_NUM.toString())
                etSTRE_SPECET.setText(manyFloraAttribute.STRE_SPEC)
                etSTRE_FAMIET.setText(manyFloraAttribute.STRE_FAMI)
                etSTRE_SCIENET.setText(manyFloraAttribute.STRE_SCIEN)
                etSTRE_HET.setText(manyFloraAttribute.STRE_H.toString())
                etSTRE_BREAET.setText(manyFloraAttribute.STRE_BREA.toString())
                etSTRE_COVEET.setText(manyFloraAttribute.STRE_COVE.toString())
                shrnumTV.setText(manyFloraAttribute.SHR_NUM.toString())
                etSHR_SPECET.setText(manyFloraAttribute.SHR_SPEC)
                etSHR_FAMIET.setText(manyFloraAttribute.SHR_FAMI)
                etSHR_SCIENET.setText(manyFloraAttribute.SHR_SCIEN)
                etSHR_HET.setText(manyFloraAttribute.SHR_H.toString())
                etSTR_COVEET.setText(manyFloraAttribute.STRE_COVE.toString())
                hernumTV.setText(manyFloraAttribute.HER_NUM.toString())
                etHER_SPECET.setText(manyFloraAttribute.HER_SPEC)
                etHER_FAMIET.setText(manyFloraAttribute.HER_FAMI)
                etHER_SCIENET.setText(manyFloraAttribute.HER_SCIEN)
                etHER_HET.setText(manyFloraAttribute.HER_H.toString())
                etHER_COVEET.setText(manyFloraAttribute.HER_COVE.toString())
                gpslatTV.setText(manyFloraAttribute.GPS_LAT.toString())
                gpslonTV.setText(manyFloraAttribute.GPS_LON.toString())

                val tredata = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "TRE_NUM", null)
                val stredata = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "STRE_NUM", null)
                val shrdata = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "SHR_NUM", null)
                val herdata = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "HER_NUM", null)

                val TRE_NUM = manyFloraAttribute.TRE_NUM
                val STRE_NUM = manyFloraAttribute.STRE_NUM
                val SHR_NUM = manyFloraAttribute.SHR_NUM
                val HER_NUM = manyFloraAttribute.HER_NUM

                if(TRE_NUM!! > 0) {
                    while (tredata.moveToNext()) {
                        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(tredata.getString(0), tredata.getString(1), tredata.getString(2), tredata.getString(3), tredata.getString(4), tredata.getString(5), tredata.getInt(6), tredata.getString(7),
                                tredata.getString(8), tredata.getString(9), tredata.getFloat(10), tredata.getFloat(11), tredata.getFloat(12), tredata.getInt(13), tredata.getString(14)
                                , tredata.getString(15), tredata.getString(16), tredata.getFloat(17), tredata.getFloat(18), tredata.getFloat(19), tredata.getInt(20), tredata.getString(21), tredata.getString(22)
                                , tredata.getString(23), tredata.getFloat(24), tredata.getFloat(25), tredata.getInt(26), tredata.getString(27), tredata.getString(28), tredata.getString(29), tredata.getFloat(30), tredata.getFloat(31), tredata.getFloat(32)
                                , tredata.getFloat(33), tredata.getString(34), tredata.getString(35))

                        val tredata = TreeData1(manyFloraAttribute.TRE_NUM, manyFloraAttribute.TRE_SPEC,manyFloraAttribute.TRE_FAMI,manyFloraAttribute.TRE_SCIEN,manyFloraAttribute.TRE_H,manyFloraAttribute.TRE_BREA
                        ,manyFloraAttribute.TRE_COVE)
                        TreDatas.add(tredata)


                    }
                    trepage = TreDatas.size
                    trenumTV.setText(trepage.toString() + " / " + TreDatas.size.toString())
                }

                if(STRE_NUM!! > 0 ) {
                    while (stredata.moveToNext()) {
                        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(stredata.getString(0), stredata.getString(1), stredata.getString(2), stredata.getString(3), stredata.getString(4), stredata.getString(5), stredata.getInt(6), stredata.getString(7),
                                stredata.getString(8), stredata.getString(9), stredata.getFloat(10), stredata.getFloat(11), stredata.getFloat(12), stredata.getInt(13), stredata.getString(14)
                                , stredata.getString(15), stredata.getString(16), stredata.getFloat(17), stredata.getFloat(18), stredata.getFloat(19), stredata.getInt(20), stredata.getString(21), stredata.getString(22)
                                , stredata.getString(23), stredata.getFloat(24), stredata.getFloat(25), stredata.getInt(26), stredata.getString(27), stredata.getString(28), stredata.getString(29), stredata.getFloat(30), stredata.getFloat(31), stredata.getFloat(32)
                                , stredata.getFloat(33), stredata.getString(34), stredata.getString(35))

                        val stredata = TreeData1(manyFloraAttribute.STRE_NUM, manyFloraAttribute.STRE_SPEC,manyFloraAttribute.STRE_FAMI,manyFloraAttribute.STRE_SCIEN,manyFloraAttribute.STRE_H,manyFloraAttribute.STRE_BREA
                                ,manyFloraAttribute.STRE_COVE)
                        StreDatas.add(stredata)
                    }

                    strepage = StreDatas.size
                    strenumTV.setText(strepage.toString() + " / " + StreDatas.size.toString())
                }

                if(SHR_NUM!! > 0) {
                    while (shrdata.moveToNext()) {
                        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(shrdata.getString(0), shrdata.getString(1), shrdata.getString(2), shrdata.getString(3), shrdata.getString(4), shrdata.getString(5), shrdata.getInt(6), shrdata.getString(7),
                                shrdata.getString(8), shrdata.getString(9), shrdata.getFloat(10), shrdata.getFloat(11), shrdata.getFloat(12), shrdata.getInt(13), shrdata.getString(14)
                                , shrdata.getString(15), shrdata.getString(16), shrdata.getFloat(17), shrdata.getFloat(18), shrdata.getFloat(19), shrdata.getInt(20), shrdata.getString(21), shrdata.getString(22)
                                , shrdata.getString(23), shrdata.getFloat(24), shrdata.getFloat(25), shrdata.getInt(26), shrdata.getString(27), shrdata.getString(28), shrdata.getString(29), shrdata.getFloat(30), shrdata.getFloat(31), shrdata.getFloat(32)
                                , shrdata.getFloat(33), shrdata.getString(34), shrdata.getString(35))

                        val shrdata = TreeData2(manyFloraAttribute.SHR_NUM, manyFloraAttribute.SHR_SPEC,manyFloraAttribute.SHR_FAMI,manyFloraAttribute.SHR_SCIEN,manyFloraAttribute.SHR_H,manyFloraAttribute.SHR_COVE)
                        ShrDatas.add(shrdata)
                    }

                    shrpage = ShrDatas.size
                    shrnumTV.setText(shrpage.toString() + " / " + ShrDatas.size.toString())
                }

                if(HER_NUM!! > 0 ) {
                    while (herdata.moveToNext()) {
                        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(herdata.getString(0), herdata.getString(1), herdata.getString(2), herdata.getString(3), herdata.getString(4), herdata.getString(5), herdata.getInt(6), herdata.getString(7),
                                herdata.getString(8), herdata.getString(9), herdata.getFloat(10), herdata.getFloat(11), herdata.getFloat(12), herdata.getInt(13), herdata.getString(14)
                                , herdata.getString(15), herdata.getString(16), herdata.getFloat(17), herdata.getFloat(18), herdata.getFloat(19), herdata.getInt(20), herdata.getString(21), herdata.getString(22)
                                , herdata.getString(23), herdata.getFloat(24), herdata.getFloat(25), herdata.getInt(26), herdata.getString(27), herdata.getString(28), herdata.getString(29), herdata.getFloat(30), herdata.getFloat(31), herdata.getFloat(32)
                                , herdata.getFloat(33), herdata.getString(34), herdata.getString(35))

                        val herdata = TreeData2(manyFloraAttribute.HER_NUM, manyFloraAttribute.HER_SPEC,manyFloraAttribute.HER_FAMI,manyFloraAttribute.HER_SCIEN,manyFloraAttribute.HER_H,manyFloraAttribute.HER_COVE)
                        HerDatas.add(herdata)
                    }

                    herpage = HerDatas.size
                    shrnumTV.setText(herpage.toString() + " / " + HerDatas.size.toString())
                }


            }

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

        treleftTV.setOnClickListener {
            println("trepage ===== $trepage tredatas.size ${TreDatas.size}")
            if(trepage == TreDatas.size && trepage > 1){
                    trepage = trepage-1
                    val data = TreDatas.get(trepage - 1)

                    trenumTV.setText(trepage.toString())
                    etTRE_SPECET.setText(data.SPEC)
                    etTRE_FAMIET.setText(data.FAMI)
                    etTRE_SCIENET.setText(data.SCIEN)
                    etTRE_HET.setText(data.H.toString())
                    etTRE_BREAET.setText(data.BREA.toString())
                    etTRE_COVEET.setText(data.COVE.toString())

                    trepageTV.setText(trepage.toString() + " / " + TreDatas.size.toString())

            }else if (trepage < TreDatas.size && trepage > 1){
                trepage = trepage - 1

                val data = TreDatas.get(trepage - 1)

                trenumTV.setText(trepage.toString())
                etTRE_SPECET.setText(data.SPEC)
                etTRE_FAMIET.setText(data.FAMI)
                etTRE_SCIENET.setText(data.SCIEN)
                etTRE_HET.setText(data.H.toString())
                etTRE_BREAET.setText(data.BREA.toString())
                etTRE_COVEET.setText(data.COVE.toString())
                trepageTV.setText(trepage.toString() + " / " + TreDatas.size.toString())
            }
        }

        trerightTV.setOnClickListener {
            AddTreFlora()

//            TreClear()
        }

        streleftTV.setOnClickListener {
            if(strepage == StreDatas.size && strepage > 1){
                strepage = strepage-1
                val data = StreDatas.get(strepage - 1)

                strenumTV.setText(strepage.toString())
                etSTRE_SPECET.setText(data.SPEC)
                etSTRE_FAMIET.setText(data.FAMI)
                etSTRE_SCIENET.setText(data.SCIEN)
                etSTRE_HET.setText(data.H.toString())
                etSTRE_BREAET.setText(data.BREA.toString())
                etSTRE_COVEET.setText(data.COVE.toString())

                strepageTV.setText(strepage.toString() + " / " + StreDatas.size.toString())

            }else if (strepage < StreDatas.size && strepage > 1){
                strepage = strepage - 1

                val data = StreDatas.get(strepage - 1)

                strenumTV.setText(strepage.toString())
                etSTRE_SPECET.setText(data.SPEC)
                etSTRE_FAMIET.setText(data.FAMI)
                etSTRE_SCIENET.setText(data.SCIEN)
                etSTRE_HET.setText(data.H.toString())
                etSTRE_BREAET.setText(data.BREA.toString())
                etSTRE_COVEET.setText(data.COVE.toString())
                strepageTV.setText(strepage.toString() + " / " + StreDatas.size.toString())
            }
        }

        strerightTV.setOnClickListener {
            AddStreFlora()
//            Stre_Clear()
        }

        shrleftTV.setOnClickListener {
            println("trepage ===== $shrpage tredatas.size ${ShrDatas.size}")
            if(shrpage == ShrDatas.size && shrpage > 1){
                shrpage = shrpage-1
                val data = ShrDatas.get(shrpage - 1)

                shrnumTV.setText(shrpage.toString())
                etSHR_SPECET.setText(data.SPEC)
                etSHR_FAMIET.setText(data.FAMI)
                etSHR_SCIENET.setText(data.SCIEN)
                etSHR_HET.setText(data.H.toString())
                etSTR_COVEET.setText(data.COVE.toString())

                shrpageTV.setText(shrpage.toString() + " / " + ShrDatas.size.toString())

            }else if (shrpage < ShrDatas.size && shrpage > 1){
                shrpage = shrpage - 1

                val data = ShrDatas.get(shrpage - 1)

                shrnumTV.setText(shrpage.toString())
                etSHR_SPECET.setText(data.SPEC)
                etSHR_FAMIET.setText(data.FAMI)
                etSHR_SCIENET.setText(data.SCIEN)
                etSHR_HET.setText(data.H.toString())
                etSTR_COVEET.setText(data.COVE.toString())
                shrpageTV.setText(shrpage.toString() + " / " + ShrDatas.size.toString())
            }
        }

        shrrightTV.setOnClickListener {
            AddShrFlora()
//            Shr_Clear()
        }

        herleftTV.setOnClickListener {
            if(herpage == HerDatas.size && herpage > 1){
                herpage = herpage-1
                val data = HerDatas.get(herpage - 1)

                hernumTV.setText(herpage.toString())
                etHER_SPECET.setText(data.SPEC)
                etHER_FAMIET.setText(data.FAMI)
                etHER_SCIENET.setText(data.SCIEN)
                etHER_HET.setText(data.H.toString())
                etHER_COVEET.setText(data.COVE.toString())

                herpageTV.setText(herpage.toString() + " / " + HerDatas.size.toString())

            }else if (herpage < HerDatas.size && herpage > 1){
                herpage = herpage - 1

                val data = HerDatas.get(herpage - 1)

                hernumTV.setText(herpage.toString())
                etHER_SPECET.setText(data.SPEC)
                etHER_FAMIET.setText(data.FAMI)
                etHER_SCIENET.setText(data.SCIEN)
                etHER_HET.setText(data.H.toString())
                etHER_COVEET.setText(data.COVE.toString())
                herpageTV.setText(herpage.toString() + " / " + HerDatas.size.toString())
            }
        }

        herrightTV.setOnClickListener {
            AddHerFlora()
//            Her_Clear()
        }

        saveBT.setOnClickListener {
            if(TreDatas == null && StreDatas == null && ShrDatas == null && HerDatas == null){
            val builder = AlertDialog.Builder(context)
            builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()

                        var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                ,null,null,null,null,null,null,null,null,null,null,null,null,null)

                        keyId = intent.getStringExtra("GROP_ID")

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

                                dbManager.updatemanyflora_attribute(manyFloraAttribute,pk)
                            }

                        } else {

                            dbManager.insertmanyflora_attribute(manyFloraAttribute);

                        }

                        var intent = Intent()

                        intent.putExtra("export", 70);

                        setResult(RESULT_OK, intent);

                        finish()

                    })

                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
                    }else {

                val builder = AlertDialog.Builder(context)
                builder.setMessage("저장하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->

                            dialog.cancel()

                            if(dataArray != null){
                                dataArray.clear()
                            }

                            var MaxLength = 0
                            var TreDataSize  = TreDatas.size
                            var StreDataSize = StreDatas.size
                            var ShrDataSize = ShrDatas.size
                            var HerDataSize = HerDatas.size


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

                            println("MaxLength ------ ${MaxLength.toString()}")

                            for(i in 0..MaxLength-1){
                                var manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null,null,null,null,null,null,null,null,null,null
                                        ,null,null,null,null,null,null,null,null,null,null,null,null,null
                                        ,null,null,null,null,null,null,null,null,null,null,null,null,null)

                                keyId = intent.getStringExtra("GROP_ID")

                                manyFloraAttribute.GROP_ID = keyId

                                manyFloraAttribute.INV_REGION = invregionTV.text.toString()

                                manyFloraAttribute.INV_DT = Utils.todayStr()

                                if(invpersonTV.text == null || invpersonTV.text.equals("")){
                                    manyFloraAttribute.INV_PERSON = userName
                                }else {
                                    manyFloraAttribute.INV_PERSON = invpersonTV.text.toString()
                                }

                                manyFloraAttribute.INV_TM = Utils.timeStr()

                                if(MaxLength - 1 > TreDataSize - 1){
                                    manyFloraAttribute.TRE_NUM = TreDataSize -1
                                    manyFloraAttribute.TRE_SPEC = TreDatas.get(TreDataSize -1).SPEC
                                    manyFloraAttribute.TRE_FAMI = TreDatas.get(TreDataSize -1).FAMI
                                    manyFloraAttribute.TRE_SCIEN = TreDatas.get(TreDataSize -1).SCIEN
                                    manyFloraAttribute.TRE_H = TreDatas.get(TreDataSize -1).H
                                    manyFloraAttribute.TRE_BREA = TreDatas.get(TreDataSize -1).BREA
                                    manyFloraAttribute.TRE_COVE = TreDatas.get(TreDataSize -1).COVE
                                }else {
                                    manyFloraAttribute.TRE_NUM = TreDatas.get(i).PAGE
                                    manyFloraAttribute.TRE_SPEC = TreDatas.get(i).SPEC
                                    manyFloraAttribute.TRE_FAMI = TreDatas.get(i).FAMI
                                    manyFloraAttribute.TRE_SCIEN = TreDatas.get(i).SCIEN
                                    manyFloraAttribute.TRE_H = TreDatas.get(i).H
                                    manyFloraAttribute.TRE_BREA = TreDatas.get(i).BREA
                                    manyFloraAttribute.TRE_COVE = TreDatas.get(i).COVE
                                }

                                if (MaxLength - 1 > StreDataSize - 1){
                                    manyFloraAttribute.STRE_NUM = StreDataSize - 1
                                    manyFloraAttribute.STRE_SPEC = StreDatas.get(StreDataSize - 1).SPEC
                                    manyFloraAttribute.STRE_FAMI = StreDatas.get(StreDataSize - 1).FAMI
                                    manyFloraAttribute.STRE_SCIEN = StreDatas.get(StreDataSize - 1).SCIEN
                                    manyFloraAttribute.STRE_H = StreDatas.get(StreDataSize - 1).H
                                    manyFloraAttribute.STRE_BREA = StreDatas.get(StreDataSize - 1).BREA
                                    manyFloraAttribute.STRE_COVE = StreDatas.get(StreDataSize - 1).COVE
                                } else {
                                    manyFloraAttribute.STRE_NUM = StreDatas.get(i).PAGE
                                    manyFloraAttribute.STRE_SPEC = StreDatas.get(i).SPEC
                                    manyFloraAttribute.STRE_FAMI = StreDatas.get(i).FAMI
                                    manyFloraAttribute.STRE_SCIEN = StreDatas.get(i).SCIEN
                                    manyFloraAttribute.STRE_H = StreDatas.get(i).H
                                    manyFloraAttribute.STRE_BREA = StreDatas.get(i).BREA
                                    manyFloraAttribute.STRE_COVE = StreDatas.get(i).COVE
                                }

                                if (MaxLength - 1 > ShrDataSize - 1){
                                    manyFloraAttribute.SHR_NUM = ShrDataSize - 1
                                    manyFloraAttribute.STRE_SPEC = ShrDatas.get(ShrDataSize - 1).SPEC
                                    manyFloraAttribute.STRE_FAMI = ShrDatas.get(ShrDataSize - 1).FAMI
                                    manyFloraAttribute.STRE_SCIEN = ShrDatas.get(ShrDataSize - 1).SCIEN
                                    manyFloraAttribute.STRE_H = ShrDatas.get(ShrDataSize - 1).H
                                    manyFloraAttribute.STRE_COVE = ShrDatas.get(ShrDataSize - 1).COVE
                                } else {
                                    manyFloraAttribute.STRE_NUM = ShrDatas.get(i).PAGE
                                    manyFloraAttribute.STRE_SPEC = ShrDatas.get(i).SPEC
                                    manyFloraAttribute.STRE_FAMI = ShrDatas.get(i).FAMI
                                    manyFloraAttribute.STRE_SCIEN = ShrDatas.get(i).SCIEN
                                    manyFloraAttribute.STRE_H = ShrDatas.get(i).H
                                    manyFloraAttribute.STRE_COVE = ShrDatas.get(i).COVE
                                }

                                if (MaxLength - 1 > HerDataSize - 1){
                                    manyFloraAttribute.HER_NUM = HerDataSize - 1
                                    manyFloraAttribute.HER_SPEC = HerDatas.get(HerDataSize - 1).SPEC
                                    manyFloraAttribute.HER_FAMI = HerDatas.get(HerDataSize - 1).FAMI
                                    manyFloraAttribute.HER_SCIEN = HerDatas.get(HerDataSize - 1).SCIEN
                                    manyFloraAttribute.HER_H = HerDatas.get(HerDataSize - 1).H
                                    manyFloraAttribute.HER_COVE = HerDatas.get(HerDataSize - 1).COVE
                                } else {
                                    manyFloraAttribute.HER_NUM = HerDatas.get(i).PAGE
                                    manyFloraAttribute.HER_SPEC = HerDatas.get(i).SPEC
                                    manyFloraAttribute.HER_FAMI = HerDatas.get(i).FAMI
                                    manyFloraAttribute.HER_SCIEN = HerDatas.get(i).SCIEN
                                    manyFloraAttribute.HER_H = HerDatas.get(i).H
                                    manyFloraAttribute.HER_COVE = HerDatas.get(i).COVE
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

                                        dbManager.updatemanyflora_attribute(manyFloraAttribute,pk)
                                    }

                                } else {

                                    dbManager.insertmanyflora_attribute(manyFloraAttribute);

                                }

                                dataArray.add(manyFloraAttribute)
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
        }

        cancleBT.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("취소하시겠습니까 ?").setCancelable(false)
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->


                        val dataList: Array<String> = arrayOf("*");

                        val data= db.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

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

                                    val data = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$GROP_ID'", null, null, null, "", null)

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

                                    var intent = Intent()

                                    if (dataArray.size > 1) {
                                        dbManager.deletemanyflora_attribute(manyFloraAttribute, pk)

                                        var intent = Intent()

                                        intent.putExtra("reset", 100)

                                        setResult(RESULT_OK, intent);
                                        finish()

                                    }

                                    if (dataArray.size == 1) {
                                        dbManager.deletemanyflora_attribute(manyFloraAttribute, pk)

                                        var intent = Intent()

                                        intent.putExtra("markerid", markerid)

                                        setResult(RESULT_OK, intent);
                                        finish()
                                    }
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
                        .setPositiveButton("확인",  DialogInterface.OnClickListener{ dialog, id ->

                            dialog.cancel()

                            if (intent.getStringExtra("id") != null) {
                                val id = intent.getStringExtra("id")

                                val dataList: Array<String> = arrayOf("*");

                                val data = db.query("ManyFloraAttribute", dataList, "id = '$id'", null, null, null, "", null)

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

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase()

        val data= db.query("ManyFloraAttribute", dataList, "GROP_ID = '$keyId'", null, null, null, "", null)

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

        finish()
    }

    fun AddTreFlora(){

        if(trepage == TreDatas.size){
            trepage = trepage + 1

            val spec = etTRE_SPECET.text.toString()
            val fami = etTRE_FAMIET.text.toString()
            val scien = etTRE_SCIENET.text.toString()

            var h:Float = 0.0F

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

            var tredata = TreeData1(trepage,spec,fami,scien,h,brea,cove)

            TreDatas.add(tredata)

            trepageTV.setText(trepage.toString() + " / " + TreDatas.size)
            trenumTV.setText(trepage.toString())
            etTRE_SPECET.setText("")
            etTRE_FAMIET.setText("")
            etTRE_SCIENET.setText("")
            etTRE_HET.setText("")
            etTRE_BREAET.setText("")
            etTRE_COVEET.setText("")

        }

        if(trepage < TreDatas.size){

            val data = TreDatas.get(trepage - 1)
            trepage = trepage + 1
            trenumTV.setText(trepage.toString())
            etTRE_SPECET.setText(data.SPEC)
            etTRE_FAMIET.setText(data.FAMI)
            etTRE_SCIENET.setText(data.SCIEN)
            etTRE_HET.setText(data.H.toString())
            etTRE_BREAET.setText(data.BREA.toString())
            etTRE_COVEET.setText(data.COVE.toString())
            trepageTV.setText(trepage.toString() + " / " + TreDatas.size)

        }

        deleteBT.visibility = View.GONE

    }

    fun AddStreFlora(){

        if(strepage == StreDatas.size){
            strepage = strepage + 1

            val spec = etSTRE_SPECET.text.toString()
            val fami = etSTRE_FAMIET.text.toString()
            val scien = etSTRE_SCIENET.text.toString()

            var h:Float = 0.0F

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

            var tredata = TreeData1(strepage,spec,fami,scien,h,brea,cove)

            StreDatas.add(tredata)

            strepageTV.setText(strepage.toString() + " / " + StreDatas.size)
            strenumTV.setText(strepage.toString())
            etSTRE_SPECET.setText("")
            etSTRE_FAMIET.setText("")
            etSTRE_SCIENET.setText("")
            etSTRE_HET.setText("")
            etSTRE_BREAET.setText("")
            etSTRE_COVEET.setText("")

        }

        if(strepage < StreDatas.size){

            val data = StreDatas.get(strepage - 1)
            strepage = strepage + 1
            strenumTV.setText(strepage.toString())
            etSTRE_SPECET.setText(data.SPEC)
            etSTRE_FAMIET.setText(data.FAMI)
            etSTRE_SCIENET.setText(data.SCIEN)
            etSTRE_HET.setText(data.H.toString())
            etSTRE_BREAET.setText(data.BREA.toString())
            etSTRE_COVEET.setText(data.COVE.toString())
            strepageTV.setText(strepage.toString() + " / " + StreDatas.size)

        }

        deleteBT.visibility = View.GONE
    }

    fun AddShrFlora(){
        if(shrpage == ShrDatas.size){
            shrpage = shrpage + 1

            val spec = etSHR_SPECET.text.toString()
            val fami = etSHR_FAMIET.text.toString()
            val scien = etSHR_SCIENET.text.toString()

            var h:Float = 0.0F

            if (etSHR_HET.text.isNotEmpty()) {
                h = etSHR_HET.text.toString().toFloat()
            }

            var cove = 0.0F

            if (etSTR_COVEET.text.isNotEmpty()) {
                cove = etSTR_COVEET.text.toString().toFloat()
            }

            var shrdata = TreeData2(shrpage,spec,fami,scien,h,cove)

            ShrDatas.add(shrdata)

            shrpageTV.setText(shrpage.toString() + " / " + ShrDatas.size)
            shrnumTV.setText(shrpage.toString())
            etSHR_SPECET.setText("")
            etSHR_FAMIET.setText("")
            etSHR_SCIENET.setText("")
            etSHR_HET.setText("")
            etSTR_COVEET.setText("")

        }

        if(shrpage < ShrDatas.size){

            val data = ShrDatas.get(shrpage - 1)
            shrpage = shrpage + 1
            shrnumTV.setText(shrpage.toString())
            etSHR_SPECET.setText(data.SPEC)
            etSHR_FAMIET.setText(data.FAMI)
            etSHR_SCIENET.setText(data.SCIEN)
            etSHR_HET.setText(data.H.toString())
            etSTR_COVEET.setText(data.COVE.toString())
            shrpageTV.setText(shrpage.toString() + " / " + ShrDatas.size)

        }

        deleteBT.visibility = View.GONE
    }

    fun AddHerFlora(){
        if(herpage == HerDatas.size){
            herpage = herpage + 1

            val spec = etHER_SPECET.text.toString()
            val fami = etHER_FAMIET.text.toString()
            val scien = etHER_SCIENET.text.toString()

            var h:Float = 0.0F

            if (etHER_HET.text.isNotEmpty()) {
                h = etHER_HET.text.toString().toFloat()
            }

            var cove = 0.0F

            if (etHER_COVEET.text.isNotEmpty()) {
                cove = etHER_COVEET.text.toString().toFloat()
            }

            var tredata = TreeData2(herpage,spec,fami,scien,h,cove)

            HerDatas.add(tredata)

            herpageTV.setText(herpage.toString() + " / " + HerDatas.size)
            hernumTV.setText(herpage.toString())
            etHER_SPECET.setText("")
            etHER_FAMIET.setText("")
            etHER_SCIENET.setText("")
            etHER_HET.setText("")
            etHER_COVEET.setText("")

        }

        if(herpage < HerDatas.size){

            val data = HerDatas.get(herpage - 1)
            herpage = herpage + 1
            hernumTV.setText(herpage.toString())
            etHER_SPECET.setText(data.SPEC)
            etHER_FAMIET.setText(data.FAMI)
            etHER_SCIENET.setText(data.SCIEN)
            etHER_HET.setText(data.H.toString())
            etHER_COVEET.setText(data.COVE.toString())
            herpageTV.setText(herpage.toString() + " / " + HerDatas.size)

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

}


