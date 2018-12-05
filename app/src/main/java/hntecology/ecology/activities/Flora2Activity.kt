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
import hntecology.ecology.model.Base
import hntecology.ecology.model.Flora_Attribute
import hntecology.ecology.model.ManyFloraAttribute
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

    var TreDatas: ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()
    var StrDatas: ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()
    var ShrDatas: ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()
    var HerDatas: ArrayList<ManyFloraAttribute> = ArrayList<ManyFloraAttribute>()

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

            val grop_id = intent.getStringExtra("GROP_ID")

            val dataList: Array<String> = arrayOf("*");

            val data = db.query("ManyFloraAttribute", dataList, "GROP_ID = '$grop_id'", null, null, null, "", null)

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

        }

        trerightTV.setOnClickListener {
            AddTreFlora()
//            TreClear()
        }

        streleftTV.setOnClickListener {

        }

        strerightTV.setOnClickListener {
            AddStreFlora()
//            Stre_Clear()
        }

        shrleftTV.setOnClickListener {

        }

        shrrightTV.setOnClickListener {
            AddShrFlora()
//            Shr_Clear()
        }

        herleftTV.setOnClickListener {

        }

        herrightTV.setOnClickListener {
            AddHerFlora()
//            Her_Clear()
        }

        saveBT.setOnClickListener {
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

        floraaddBT.setOnClickListener {
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

            if(intent.getStringExtra("set") != null){
                intent.putExtra("reset", 100)

                setResult(RESULT_OK, intent);
            }

            deleteBT.visibility = View.GONE

            var intent = Intent()

            intent.putExtra("export", 70);

            setResult(RESULT_OK, intent);

            clear()
            chkdata = false
            pk = null
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
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

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

        if (chkdata) {

            if(pk != null){

                val CONF_MOD = manyFloraAttribute.CONF_MOD

                if(CONF_MOD == "C" || CONF_MOD == "N"){
                    manyFloraAttribute.CONF_MOD = "M"
                }

            }

        } else {

        }

        if(intent.getStringExtra("set") != null){
            intent.putExtra("reset", 100)

            setResult(RESULT_OK, intent);
        }

        TreDatas.add(manyFloraAttribute)

        deleteBT.visibility = View.GONE

        chkdata = false
        pk = null
    }

    fun AddStreFlora(){
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

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

        if (chkdata) {

            if(pk != null){

                val CONF_MOD = manyFloraAttribute.CONF_MOD

                if(CONF_MOD == "C" || CONF_MOD == "N"){
                    manyFloraAttribute.CONF_MOD = "M"
                }

            }

        } else {

        }

        if(intent.getStringExtra("set") != null){
            intent.putExtra("reset", 100)

            setResult(RESULT_OK, intent);
        }

        StrDatas.add(manyFloraAttribute)

        deleteBT.visibility = View.GONE

        chkdata = false
        pk = null
    }

    fun AddShrFlora(){
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

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

        if (chkdata) {

            if(pk != null){

                val CONF_MOD = manyFloraAttribute.CONF_MOD

                if(CONF_MOD == "C" || CONF_MOD == "N"){
                    manyFloraAttribute.CONF_MOD = "M"
                }

            }

        } else {

        }

        if(intent.getStringExtra("set") != null){
            intent.putExtra("reset", 100)

            setResult(RESULT_OK, intent);
        }

        ShrDatas.add(manyFloraAttribute)

        deleteBT.visibility = View.GONE

        chkdata = false
        pk = null
    }

    fun AddHerFlora(){
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase()

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

        if (chkdata) {

            if(pk != null){

                val CONF_MOD = manyFloraAttribute.CONF_MOD

                if(CONF_MOD == "C" || CONF_MOD == "N"){
                    manyFloraAttribute.CONF_MOD = "M"
                }

            }

        } else {

        }

        if(intent.getStringExtra("set") != null){
            intent.putExtra("reset", 100)

            setResult(RESULT_OK, intent);
        }

        HerDatas.add(manyFloraAttribute)

        deleteBT.visibility = View.GONE

        chkdata = false
        pk = null
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


