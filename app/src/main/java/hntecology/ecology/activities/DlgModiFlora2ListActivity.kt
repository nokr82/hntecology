package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgModiFlora2Adapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.ManyFloraAttribute
import kotlinx.android.synthetic.main.activity_dlg_modi_list.*
import java.io.IOException
import java.util.*


class DlgModiFlora2ListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1: ArrayList<ManyFloraAttribute>
    private lateinit var listdata2: ArrayList<ManyFloraAttribute>
    private lateinit var listdata3: ArrayList<ManyFloraAttribute>

    private lateinit var listAdapte1: DlgModiFlora2Adapter;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var DlgHeight: Float = 430F
    var dbManager: DataBaseHelper? = null
    var chkData = false
    var polygonid = ""
    var grop_id = ""
    var geom = ""
    var lat: String = ""
    var log: String = ""
    var u_name = ""

    var r_region = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_modi_list)

        context = applicationContext;

        dbManager = DataBaseHelper(this)
        u_name = PrefUtils.getStringPreference(context, "name");
        val db = dbManager!!.createDataBase();

        val intent = getIntent()

        DlgHeight = intent.getFloatExtra("DlgHeight", 430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);
        
        titleTV.text ="식물군집구조 선택복사"
        donumTV.visibility = View.GONE

        grop_id = intent.getStringExtra("GROP_ID")

        if (intent.getStringExtra("geom") != null) {
            geom = intent.getStringExtra("geom")
            var geom_sp = geom.split(" ")
            if (geom_sp.size > 1) {
                lat = geom_sp[0]
                log = geom_sp[1]
                region()
            }

            println("---------점스스스 $geom")
        }

        val dataList: Array<String> = arrayOf("ManyFloraAttribute.*", "min(id) as minId");
        val dataList2: Array<String> = arrayOf("*");
        val data1 = db.query("ManyFloraAttribute", dataList, null, null, "GROP_ID", null, "minId asc", null);
        val data2 = db.query("ManyFloraAttribute", dataList2, null, null, null, null, "id asc", null);


        listView1 = findViewById(R.id.modiLV)

        listdata3 = ArrayList()
        listdata2 = ArrayList()
        listdata1 = ArrayList()

        dataList(listdata1, data1)
        dataList(listdata2, data2)
        listAdapte1 = DlgModiFlora2Adapter(context, listdata1, listdata2);

        listView1.adapter = listAdapte1


        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var attribute = null_manyFloraAttribute()
            var bio_data = listAdapte1.getItem(position)
            val data = db.query("ManyFloraAttribute", dataList2, "GROP_ID='${bio_data.GROP_ID}'", null, null, null, "id asc", null);
             dataList(listdata3,data)
            dbManager!!.deleteAllManyFloraAttribute(grop_id)
            for (i in 0..listdata3.size - 1) {
                Log.d("버드데이터", listdata3[i].DOMIN)
                addbiotope(attribute ,listdata3[i].TRE_NUM!!.toInt() , listdata3[i].TRE_SPEC.toString(), listdata3[i].TRE_FAMI.toString() , listdata3[i].TRE_SCIEN.toString()
                ,listdata3[i]. TRE_DBH!!.toFloat(),listdata3[i]. TRE_TOIL!!.toFloat() ,listdata3[i]. TRE_UNDER!!.toFloat() ,listdata3[i].TRE_WATER!!.toFloat() ,listdata3[i].TRE_TYPE.toString()
                ,listdata3[i]. STRE_NUM!!.toInt(),listdata3[i].STRE_SPEC.toString() ,listdata3[i]. STRE_FAMI.toString(),listdata3[i]. STRE_SCIEN.toString() ,listdata3[i]. STRE_DBH!!.toFloat()
                ,listdata3[i]. STRE_TOIL!!.toFloat(),listdata3[i]. STRE_UNDER!!.toFloat(),listdata3[i]. STRE_WATER!!.toFloat(),listdata3[i].STRE_TYPE.toString() ,listdata3[i]. SHR_NUM!!.toInt()
                ,listdata3[i]. SHR_SPEC.toString() ,listdata3[i]. SHR_FAMI.toString() ,listdata3[i]. SHR_SCIEN.toString() ,listdata3[i]. SHR_TOIL!!.toFloat() ,listdata3[i].SHR_WATER!!.toFloat()
                ,listdata3[i].SHR_UNDER!!.toFloat(),listdata3[i]. HER_NUM!!.toInt(),listdata3[i].HER_SPEC.toString() ,listdata3[i].  HER_FAMI.toString() ,listdata3[i]. HER_SCIEN.toString()
                ,listdata3[i]. HER_DOMIN.toString() ,listdata3[i]. HER_GUNDO.toString()  ,listdata3[i]. HER_HEIGHT!!.toFloat(),listdata3[i]. GPS_LAT!!.toFloat(),listdata3[i]. GPS_LON!!.toFloat()
                ,listdata3[i]. TEMP_YN.toString(),listdata3[i]. CONF_MOD.toString(),listdata3[i].GEOM.toString(),listdata3[i].DOMIN.toString() ,listdata3[i]. MAC_ADDR.toString()
                ,listdata3[i]. CURRENT_TM.toString(),listdata3[i].PRJ_NAME.toString()   ,listdata3[i]. M_TRE_DBH!!.toFloat()   ,listdata3[i]. X_TRE_DBH!!.toFloat()   ,listdata3[i]. M_TRE_TOIL!!.toFloat()
                ,listdata3[i]. X_TRE_TOIL!!.toFloat(),listdata3[i]. M_TRE_UDR!!.toFloat()   ,listdata3[i]. X_TRE_UDR!!.toFloat(),listdata3[i]. M_TRE_WT!!.toFloat()   ,listdata3[i]. X_TRE_WT!!.toFloat()
                ,listdata3[i]. M_STR_DBH!!.toFloat(),listdata3[i]. X_STR_DBH!!.toFloat()   ,listdata3[i]. M_STR_TOIL!!.toFloat()   ,listdata3[i]. X_STR_TOIL!!.toFloat()   ,listdata3[i]. M_STR_UDR!!.toFloat()
                ,listdata3[i]. X_STR_UDR!!.toFloat(),listdata3[i]. M_STR_WT!!.toFloat()   ,listdata3[i]. X_STR_WT!!.toFloat()   ,listdata3[i]. M_SHR_TOIL!!.toFloat()   ,listdata3[i]. X_SHR_TOIL!!.toFloat()
                ,listdata3[i]. M_SHR_WT!!.toFloat(),listdata3[i]. X_SHR_WT!!.toFloat(),listdata3[i]. M_SHR_UDR!!.toFloat()   ,listdata3[i]. X_SHR_UDR!!.toFloat() ,listdata3[i].M_HER_HET!!.toFloat()
                ,listdata3[i]. X_HER_HET!!.toFloat())
            }
            last_finish()

        }

    }

    fun addbiotope(manyFloraAttribute: ManyFloraAttribute, TRE_NUM: Int? , TRE_SPEC: String?, TRE_FAMI: String? , TRE_SCIEN: String?
                   , TRE_DBH: Float?, TRE_TOIL: Float? , TRE_UNDER: Float? ,TRE_WATER: Float? ,TRE_TYPE: String?
                   , STRE_NUM: Int?,STRE_SPEC: String? , STRE_FAMI: String?, STRE_SCIEN: String? , STRE_DBH: Float?
                   , STRE_TOIL: Float?, STRE_UNDER: Float?, STRE_WATER: Float?,STRE_TYPE: String? , SHR_NUM: Int?
                   , SHR_SPEC: String? , SHR_FAMI: String? , SHR_SCIEN: String? , SHR_TOIL: Float? ,SHR_WATER: Float?
                   ,SHR_UNDER: Float?, HER_NUM: Int?,HER_SPEC: String? ,  HER_FAMI: String? , HER_SCIEN: String?
                   , HER_DOMIN: String? , HER_GUNDO: String?  , HER_HEIGHT: Float?, GPS_LAT: Float?, GPS_LON: Float?
                   , TEMP_YN: String?, CONF_MOD: String?,GEOM:String?,DOMIN:String? , MAC_ADDR: String?
                   , CURRENT_TM: String?,PRJ_NAME:String?   , M_TRE_DBH: Float?   , X_TRE_DBH: Float?   , M_TRE_TOIL: Float?
                   , X_TRE_TOIL: Float?   , M_TRE_UDR: Float?   , X_TRE_UDR: Float?, M_TRE_WT: Float?   , X_TRE_WT: Float?
                   , M_STR_DBH: Float?   , X_STR_DBH: Float?   , M_STR_TOIL: Float?   , X_STR_TOIL: Float?   , M_STR_UDR: Float?
                   , X_STR_UDR: Float?   , M_STR_WT: Float?   , X_STR_WT: Float?   , M_SHR_TOIL: Float?   , X_SHR_TOIL: Float?
                   , M_SHR_WT: Float?   , X_SHR_WT: Float?, M_SHR_UDR: Float?   , X_SHR_UDR: Float? ,M_HER_HET: Float?
                   , X_HER_HET: Float?) {

        manyFloraAttribute.INV_REGION = r_region
        manyFloraAttribute.GROP_ID = grop_id
        manyFloraAttribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        manyFloraAttribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
        manyFloraAttribute.INV_DT = Utils.todayStr()
        manyFloraAttribute.INV_TM = Utils.timeStr()
        manyFloraAttribute.DOMIN = DOMIN


        manyFloraAttribute.TRE_SPEC = TRE_SPEC
        manyFloraAttribute.TRE_FAMI = TRE_FAMI
        manyFloraAttribute.TRE_SCIEN = TRE_SCIEN
            manyFloraAttribute.TRE_DBH = TRE_DBH

            manyFloraAttribute.TRE_TOIL = TRE_TOIL
            manyFloraAttribute.TRE_UNDER = TRE_UNDER

            manyFloraAttribute.TRE_WATER = TRE_WATER
            manyFloraAttribute.M_TRE_DBH = M_TRE_DBH
            manyFloraAttribute.X_TRE_DBH = X_TRE_DBH
            manyFloraAttribute.M_TRE_TOIL = M_TRE_TOIL
            manyFloraAttribute.X_TRE_TOIL = X_TRE_TOIL
            manyFloraAttribute.M_TRE_UDR = M_TRE_UDR
            manyFloraAttribute.X_TRE_UDR = X_TRE_UDR
            manyFloraAttribute.M_TRE_WT = M_TRE_WT
            manyFloraAttribute.X_TRE_WT = X_TRE_WT

        manyFloraAttribute.TRE_TYPE = TRE_TYPE




        manyFloraAttribute.STRE_SPEC = STRE_SPEC
        manyFloraAttribute.STRE_FAMI = STRE_FAMI
        manyFloraAttribute.STRE_SCIEN = STRE_SCIEN

            manyFloraAttribute.STRE_DBH = STRE_DBH

            manyFloraAttribute.STRE_TOIL = STRE_TOIL


            manyFloraAttribute.STRE_UNDER = STRE_UNDER

            manyFloraAttribute.M_STR_DBH = M_STR_DBH
            manyFloraAttribute.X_STR_DBH = X_STR_DBH
            manyFloraAttribute.M_STR_TOIL = M_STR_TOIL
            manyFloraAttribute.X_STR_TOIL = X_STR_TOIL
            manyFloraAttribute.M_STR_UDR = M_STR_UDR
            manyFloraAttribute.X_STR_UDR = X_STR_UDR
            manyFloraAttribute.M_STR_WT = M_STR_WT
            manyFloraAttribute.X_STR_WT = X_STR_WT


        manyFloraAttribute.STRE_TYPE = STRE_TYPE



            manyFloraAttribute.SHR_NUM = SHR_NUM

        manyFloraAttribute.SHR_SPEC = SHR_SPEC
        manyFloraAttribute.SHR_FAMI = SHR_FAMI
        manyFloraAttribute.SHR_SCIEN = SHR_SCIEN

            manyFloraAttribute.SHR_TOIL = SHR_TOIL


            manyFloraAttribute.SHR_WATER = SHR_WATER

            manyFloraAttribute.SHR_UNDER = SHR_UNDER


            manyFloraAttribute.M_SHR_TOIL = M_SHR_TOIL

            manyFloraAttribute.X_SHR_TOIL = X_SHR_TOIL

            manyFloraAttribute.M_SHR_WT = M_SHR_WT


            manyFloraAttribute.X_SHR_WT = X_SHR_WT


            manyFloraAttribute.M_SHR_UDR = M_SHR_UDR

            manyFloraAttribute.X_SHR_UDR = X_SHR_UDR



        manyFloraAttribute.HER_SPEC = HER_SPEC

        manyFloraAttribute.HER_FAMI = HER_FAMI
        manyFloraAttribute.HER_SCIEN = HER_SCIEN

            manyFloraAttribute.HER_DOMIN = HER_DOMIN

            manyFloraAttribute.HER_GUNDO = HER_GUNDO

            manyFloraAttribute.HER_HEIGHT = HER_HEIGHT

            manyFloraAttribute.M_HER_HET = M_HER_HET
            manyFloraAttribute.X_HER_HET = X_HER_HET

            manyFloraAttribute.GPS_LAT = lat.toFloat()
            manyFloraAttribute.GPS_LON = log.toFloat()

        manyFloraAttribute.TEMP_YN = "Y"
        manyFloraAttribute.CONF_MOD = "M"

        manyFloraAttribute.GEOM = geom


        manyFloraAttribute.TRE_NUM= TRE_NUM
        manyFloraAttribute.STRE_NUM= STRE_NUM
        manyFloraAttribute.SHR_NUM= SHR_NUM
        manyFloraAttribute.HER_NUM= HER_NUM

        dbManager!!.insertmanyflora_attribute(manyFloraAttribute);
    }

    fun last_finish() {
        var intent = Intent()
        Log.d("점스", geom)
        intent.putExtra("export", 70);
        intent.putExtra("geom", geom);
        setResult(RESULT_OK, intent);
        Toast.makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun null_manyFloraAttribute(): ManyFloraAttribute {
        val manyFloraAttribute: ManyFloraAttribute = ManyFloraAttribute(null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null,null, null,null, null, null,null, null,null, null, null,null, null,null
                , null, null,null, null,null, null, null,null, null,null,null,null,null,null,null)
        return manyFloraAttribute
    }

    fun dataList(listdata: ArrayList<ManyFloraAttribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: ManyFloraAttribute;

            model = ManyFloraAttribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getInt(6), data.getString(7),
                    data.getString(8), data.getString(9), data.getFloat(10), data.getFloat(11), data.getFloat(12), data.getFloat(13), data.getString(14)
                    , data.getInt(15), data.getString(16), data.getString(17), data.getString(18), data.getFloat(19), data.getFloat(20), data.getFloat(21), data.getFloat(22)
                    , data.getString(23), data.getInt(24), data.getString(25), data.getString(26), data.getString(27), data.getFloat(28), data.getFloat(29), data.getFloat(30), data.getInt(31), data.getString(32)
                    , data.getString(33), data.getString(34), data.getString(35), data.getString(36), data.getFloat(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41)
                    , data.getString(42), data.getString(43),data.getString(44),data.getString(45),data.getString(46)
                    , data.getFloat(47), data.getFloat(48), data.getFloat(49), data.getFloat(50), data.getFloat(51), data.getFloat(52), data.getFloat(53)
                    , data.getFloat(54), data.getFloat(55), data.getFloat(56), data.getFloat(57), data.getFloat(58), data.getFloat(59), data.getFloat(60)
                    , data.getFloat(61), data.getFloat(62), data.getFloat(63), data.getFloat(64), data.getFloat(65), data.getFloat(66), data.getFloat(67)
                    , data.getFloat(68), data.getFloat(69), data.getFloat(70))
            if (model.GROP_ID!=grop_id&&model.INV_PERSON==u_name){
                if (model.DOMIN!="null"||model.TRE_SPEC!=""||model.STRE_SPEC!=""||model.HER_SPEC!=""||model.SHR_SPEC!=""){
                    listdata.add(model)
                }
            }


        }
    }


    fun region() {
        var geocoder: Geocoder = Geocoder(context);
        var list: List<Address> = geocoder.getFromLocation(log.toDouble(), lat.toDouble(), 1);

        if (list.size > 0) {
            r_region = list.get(0).getAddressLine(0)

        }
    }
}
