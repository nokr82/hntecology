package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.google.android.gms.maps.model.Polygon
import hntecology.ecology.R
import hntecology.ecology.adapter.*
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_dlg_data_list.*

class DlgDataListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var biotopeData: ArrayList<Biotope_attribute>
    private lateinit var birdsData : ArrayList<Birds_attribute>
    private lateinit var reptiliasData : ArrayList<Reptilia_attribute>
    private lateinit var mammalsData : ArrayList<Mammal_attribute>
    private lateinit var fishsData : ArrayList<Fish_attribute>
    private lateinit var insectData : ArrayList<Insect_attribute>
    private lateinit var florasData : ArrayList<Flora_Attribute>

    private lateinit var biotopeAdaper : DataBiotopeAdapter
    private lateinit var birdsAadapter: DataBirdsAdapter;
    private lateinit var reptiliaAdapter : DataReptiliaAdapter
    private lateinit var mammalAdapter : DataMammalAdapter
    private lateinit var fishAdapter : DataFIshAdapter
    private lateinit var insectAdapter : DataInsectAdapter
    private lateinit var floraAdapter : DataFloraAdapter

    private val MarkerCallBackData = 1004

    val BIOTOPE_DATA = 3000
    val BIRDS_DATA = 3001
    val REPTILIA_DATA = 3002
    val MAMMALIA_DATA = 3003
    val FISH_DATA = 3004
    val INSECT_DATA = 3005
    val FLORA_DATA = 3006

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F
    var GROP_ID:String = ""
    var markerid:String? = null
    var polygonid:String? = null

    val BIOTOPE = 1
    val BIRDS = 2
    val REPTILIA = 3
    val MAMMAL = 4
    val FISH = 5
    val INSECT = 6
    val FLORA = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_data_list)

        context = applicationContext;

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);
        GROP_ID = intent.getStringExtra("GROP_ID")

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        titleTV.setText(titleName)

        listView1 = findViewById(R.id.listLV)

        biotopeData = ArrayList()
        birdsData = ArrayList()
        reptiliasData = ArrayList()
        mammalsData = ArrayList()
        fishsData = ArrayList()
        insectData = ArrayList()
        florasData = ArrayList()

        biotopeAdaper = DataBiotopeAdapter(context,biotopeData)
        birdsAadapter = DataBirdsAdapter(context,birdsData)
        reptiliaAdapter = DataReptiliaAdapter(context,reptiliasData)
        mammalAdapter = DataMammalAdapter(context,mammalsData)
        fishAdapter = DataFIshAdapter(context,fishsData)
        insectAdapter = DataInsectAdapter(context,insectData)
        floraAdapter = DataFloraAdapter(context,florasData)

        if(intent.getStringExtra("markerid") != null){
            markerid = intent.getStringExtra("markerid")
        }

        if(intent.getStringExtra("polygonid") != null){
            polygonid = intent.getStringExtra("polygonid")
        }

        if(tableName.equals("biotopeAttribute")) {

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_PERSON","INV_DT","INV_TM","INV_INDEX","LU_GR_NUM","LU_TY_RATE","STAND_H","LC_GR_NUM","LC_TY","TY_MARK"
                    ,"GV_RATE" ,"GV_STRUCT" ,"DIS_RET" ,"RESTOR_POT" ,"COMP_INTA" ,"VP_INTA" ,"IMP_FORM" ,"BREA_DIA" ,"FIN_EST" ,"TRE_SPEC","TRE_FAMI" ,"TRE_SCIEN","TRE_H","TRE_BREA"
                    ,"TRE_COVE","STRE_SPEC","STRE_FAMI","STRE_SCIEN","STRE_H","STRE_BREA","STRE_COVE","SHR_SPEC","SHR_FAMI","SHR_SCIEN","SHR_H","STR_COVE","HER_SPEC","HER_FAMI"
                    ,"HER_SCIEN","HER_H","HER_COVE","PIC_FOLDER","WILD_ANI","BIOTOP_POT","UNUS_NOTE","GPS_LAT","GPS_LON","NEED_CONF","CONF_MOD","TEMP_YN");

            val biotopsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            biotopesdataList(biotopeData,biotopsdata)

            listView1.adapter = biotopeAdaper

        }

        if(tableName.equals("birdsAttribute")){

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
            ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"INDI_CNT" ,"OBS_STAT" ,"OBS_ST_ETC" ,"USE_TAR" ,"USE_TAR_SP" ,"USE_LAYER" ,"MJ_ACT" ,"MJ_ACT_PR" ,"GPS_LAT" ,"GPS_LON" ,"TEMP_YN", "CONF_MOD");

            val birdsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            birdsdataList(birdsData,birdsdata)

            listView1.adapter = birdsAadapter

        }

        if(tableName.equals("reptiliaAttribute")){

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"IN_CNT_ADU" ,"IN_CNT_LAR" ,"IN_CNT_EGG" ,"HAB_RIVEER" ,"HAB_EDGE" ,"WATER_IN" ,"WATER_OUT" ,"WATER_CONT" ,"WATER_QUAL" ,"WATER_DEPT"
                    ,"HAB_AREA_W","HAB_AREA_H","GPS_LAT","GPS_LON","TEMP_YN","CONF_MOD");

            val reptiliasdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            reptiliasdataList(reptiliasData,reptiliasdata)

            listView1.adapter = reptiliaAdapter

        }

        if(tableName.equals("mammalAttribute")) {

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"OBS_TY" ,"OBS_TY_ETC" ,"INDI_CNT" ,"OB_PT_CHAR" ,"UNUS_NOTE" ,"GPS_LAT" ,"GPS_LON" ,"UN_SPEC" ,"UN_SPEC_RE" ,"TR_EASY"
                    ,"TR_EASY_RE","TEMP_YN","CONF_MOD");

            val mammalsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            mammalsdataList(mammalsData,mammalsdata)

            listView1.adapter = mammalAdapter

        }

        if(tableName.equals("fishAttribute")) {

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_TM","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","MID_RAGE","CODE_NUM"
                    ,"RIVER_NUM" ,"RIVER_NM" ,"NET_CNT" ,"NET_MIN" ,"AD_DIST_NM" ,"GPS_LAT" ,"GPS_LON" ,"COLL_TOOL" ,"STREAM_W" ,"WATER_W" ,"WATER_D" ,"WATER_CUR" ,"RIV_STR"
                    ,"RIV_STR_IN","RIV_FORM","NUM","SPEC_NM","FAMI_NM","SCIEN_NM","INDI_CNT","UNIDENT","RIV_FM_CH","UN_FISH_CH","TEMP_YN","CONF_MOD");

            val fishsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            fishsdataList(fishsData,fishsdata)

            listView1.adapter = fishAdapter

        }

        if(tableName.equals("insectAttribute")) {

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"INDI_CNT" ,"OBS_STAT" ,"OBS_ST_ETC" ,"USE_TAR" ,"USER_TA_ETC" ,"MJ_ACT" ,"MJ_ACT_ETC" ,"INV_MEAN" ,"INV_MN_ETC" ,"UNUS_NOTE"
                    ,"GPS_LAT","GPS_LON","NUM","TEMP_YN","CONF_MOD");

            val insectsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            insectsdataList(insectData,insectsdata)

            listView1.adapter = insectAdapter

        }

        if(tableName.equals("floraAttribute")) {

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"FLORE_YN" ,"PLANT_YN" ,"HAB_STAT" ,"HAB_ETC" ,"COL_IN_CNT" ,"THRE_CAU" ,"GPS_LAT","GPS_LON" ,"TEMP_YN","CONF_MOD");

            val florasdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            florasdataList(florasData,florasdata)

            listView1.adapter = floraAdapter

        }

        listView1.setOnItemClickListener{ parent, view, position, id ->

            if(tableName.equals("biotopeAttribute")){

                val biotopedata = biotopeAdaper.getItem(position)

                val intent = Intent(this, BiotopeActivity::class.java)

                intent!!.putExtra("id", biotopedata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",biotopedata.GROP_ID)

                println("gropid_-------------------------------------------------------------${biotopedata.GROP_ID} ----")
                intent!!.putExtra("polygonid",polygonid)

                startActivityForResult(intent, BIOTOPE_DATA)

            }

            if(tableName.equals("birdsAttribute")){

                val birdsdata = birdsAadapter.getItem(position)

                val intent = Intent(this, BirdsActivity::class.java)

                intent.putExtra("id", birdsdata.id.toString())
                intent.putExtra("set",3)
                intent.putExtra("GROP_ID",birdsdata.GROP_ID)
                intent.putExtra("export", 70)
                intent.putExtra("markerid",markerid)

                println("markerid $markerid")

                startActivityForResult(intent, BIRDS_DATA)

            }

            if(tableName.equals("reptiliaAttribute")){

                val reptiliadata = reptiliaAdapter.getItem(position)

                val intent = Intent(this, ReptiliaActivity::class.java)

                intent!!.putExtra("id", reptiliadata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",reptiliadata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, REPTILIA_DATA)

            }

            if(tableName.equals("mammalAttribute")){

                val mammaldata = mammalAdapter.getItem(position)

                val intent = Intent(this, MammaliaActivity::class.java)

                intent!!.putExtra("id", mammaldata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",mammaldata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, MAMMALIA_DATA)

            }

            if(tableName.equals("fishAttribute")){

                val fishdata = fishAdapter.getItem(position)

                val intent = Intent(this, FishActivity::class.java)

                intent!!.putExtra("id", fishdata.id.toString())
                intent.putExtra("set",3)
                intent.putExtra("export", 70)
                intent!!.putExtra("GROP_ID",fishdata.GROP_ID)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, FISH_DATA)

            }

            if(tableName.equals("insectAttribute")){

                val insecthdata = insectAdapter.getItem(position)

                val intent = Intent(this, InsectActivity::class.java)

                intent!!.putExtra("id", insecthdata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",insecthdata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, INSECT_DATA)

            }

            if(tableName.equals("floraAttribute")){

                val floradata = floraAdapter.getItem(position)

                val intent = Intent(this, FloraActivity::class.java)

                intent!!.putExtra("id", floradata.id.toString())
                intent.putExtra("set",3)
                intent!!.putExtra("GROP_ID",floradata.GROP_ID)
                intent.putExtra("export", 70)
                intent!!.putExtra("markerid",markerid)

                startActivityForResult(intent, FLORA_DATA)

            }

        }

        closeLL.setOnClickListener {
            finish()
        }

    }

    fun biotopesdataList(listdata: java.util.ArrayList<Biotope_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Biotope_attribute;

            model = Biotope_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
            data.getString(8), data.getFloat(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getFloat(14)
            , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
            , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27), data.getFloat(28)
            , data.getString(29), data.getString(30), data.getString(31), data.getFloat(32), data.getFloat(33), data.getFloat(34), data.getString(35)
            , data.getString(36), data.getString(37), data.getFloat(38), data.getFloat(39), data.getString(40), data.getString(41), data.getString(42)
            , data.getFloat(43), data.getFloat(44), data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getDouble(49)
            , data.getDouble(50), data.getString(51), data.getString(52),data.getString(53))

            listdata.add(model)
        }
    }

    fun birdsdataList(listdata: java.util.ArrayList<Birds_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Birds_attribute;

            model = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15),data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21), data.getString(22)
                    , data.getString(23), data.getString(24), data.getFloat(25), data.getFloat(26), data.getString(27), data.getString(28))

            listdata.add(model)
        }

    }

    fun reptiliasdataList(listdata: java.util.ArrayList<Reptilia_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Reptilia_attribute;

            model = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16),data.getInt(17), data.getInt(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                    , data.getString(23), data.getString(24), data.getString(25), data.getInt(26), data.getInt(27), data.getInt(28), data.getFloat(29), data.getFloat(30),data.getString(31),data.getString(32))

            listdata.add(model)
        }
    }

    fun mammalsdataList(listdata: java.util.ArrayList<Mammal_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Mammal_attribute;

            model =  Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18),data.getInt(19), data.getString(20), data.getString(21), data.getFloat(22)
                    , data.getFloat(23), data.getString(24), data.getString(25), data.getString(26), data.getString(27),data.getString(28),data.getString(29))

            listdata.add(model)
        }
    }

    fun fishsdataList(listdata: java.util.ArrayList<Fish_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Fish_attribute;

            model =  Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8),data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getInt(14), data.getString(15), data.getInt(16), data.getInt(17), data.getString(18),
                    data.getFloat(19), data.getFloat(20), data.getString(21), data.getInt(22), data.getInt(23), data.getInt(24), data.getInt(25), data.getString(26), data.getString(27), data.getString(28),
                    data.getInt(29) ,data.getString(30), data.getString(31), data.getString(32), data.getInt(33), data.getString(33), data.getString(35), data.getString(36),data.getString(37),data.getString(38))

            listdata.add(model)

        }
    }

    fun insectsdataList(listdata: java.util.ArrayList<Insect_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Insect_attribute;

            model = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27),data.getString(28),data.getString(29))

            listdata.add(model)
        }
    }

    fun florasdataList(listdata: java.util.ArrayList<Flora_Attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Flora_Attribute;

            model = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23),data.getString(24),data.getString(25))

            listdata.add(model)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                BIOTOPE_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_PERSON","INV_DT","INV_TM","INV_INDEX","LU_GR_NUM","LU_TY_RATE","STAND_H","LC_GR_NUM","LC_TY","TY_MARK"
                                ,"GV_RATE" ,"GV_STRUCT" ,"DIS_RET" ,"RESTOR_POT" ,"COMP_INTA" ,"VP_INTA" ,"IMP_FORM" ,"BREA_DIA" ,"FIN_EST" ,"TRE_SPEC","TRE_FAMI" ,"TRE_SCIEN","TRE_H","TRE_BREA"
                                ,"TRE_COVE","STRE_SPEC","STRE_FAMI","STRE_SCIEN","STRE_H","STRE_BREA","STRE_COVE","SHR_SPEC","SHR_FAMI","SHR_SCIEN","SHR_H","STR_COVE","HER_SPEC","HER_FAMI"
                                ,"HER_SCIEN","HER_H","HER_COVE","PIC_FOLDER","WILD_ANI","BIOTOP_POT","UNUS_NOTE","GPS_LAT","GPS_LON","NEED_CONF","CONF_MOD","TEMP_YN");

                        val biotopsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                        if(biotopeData != null){
                            biotopeData.clear()
                        }

                        biotopesdataList(biotopeData,biotopsdata)

                        listView1.adapter = biotopeAdaper

                        biotopeAdaper.notifyDataSetChanged()

                    }

                    if(data!!.getStringExtra("polygonid") != null){

                        val polygonid = data!!.getStringExtra("polygonid")

                        println("polygonid ----------------------------------$polygonid")

                        var intent = Intent()
                        intent.putExtra("polygonid", polygonid)
                        setResult(RESULT_OK, intent);

                        finish()

                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                BIRDS_DATA -> {
                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"INDI_CNT" ,"OBS_STAT" ,"OBS_ST_ETC" ,"USE_TAR" ,"USE_TAR_SP" ,"USE_LAYER" ,"MJ_ACT" ,"MJ_ACT_PR" ,"GPS_LAT" ,"GPS_LON" ,"TEMP_YN");

                            val birdsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(birdsData != null){
                                birdsData.clear()
                            }

                            birdsdataList(birdsData,birdsdata)

                            listView1.adapter = birdsAadapter

                            birdsAadapter.notifyDataSetChanged()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                REPTILIA_DATA -> {
                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                                val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                                        ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"IN_CNT_ADU" ,"IN_CNT_LAR" ,"IN_CNT_EGG" ,"HAB_RIVEER" ,"HAB_EDGE" ,"WATER_IN" ,"WATER_OUT" ,"WATER_CONT" ,"WATER_QUAL" ,"WATER_DEPT"
                                        ,"HAB_AREA_W","HAB_AREA_H","GPS_LAT","GPS_LON","TEMP_YN");

                                val reptiliasdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                                if(reptiliasData != null){
                                    reptiliasData.clear()
                                }

                                reptiliasdataList(reptiliasData,reptiliasdata)

                                listView1.adapter = reptiliaAdapter

                            reptiliaAdapter.notifyDataSetChanged()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }


                }

                MAMMALIA_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"OBS_TY" ,"OBS_TY_ETC" ,"INDI_CNT" ,"OB_PT_CHAR" ,"UNUS_NOTE" ,"GPS_LAT" ,"GPS_LON" ,"UN_SPEC" ,"UN_SPEC_RE" ,"TR_EASY"
                                    ,"TR_EASY_RE","TEMP_YN");

                            val mammalsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(mammalsData != null){
                                mammalsData.clear()
                            }

                            mammalsdataList(mammalsData,mammalsdata)

                            listView1.adapter = mammalAdapter

                            mammalAdapter.notifyDataSetChanged()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                FISH_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_TM","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","MID_RAGE","CODE_NUM"
                                    ,"RIVER_NUM" ,"RIVER_NM" ,"NET_CNT" ,"NET_MIN" ,"AD_DIST_NM" ,"GPS_LAT" ,"GPS_LON" ,"COLL_TOOL" ,"STREAM_W" ,"WATER_W" ,"WATER_D" ,"WATER_CUR" ,"RIV_STR"
                                    ,"RIV_STR_IN","RIV_FORM","NUM","SPEC_NM","FAMI_NM","SCIEN_NM","INDI_CNT","UNIDENT","RIV_FM_CH","UN_FISH_CH","TEMP_YN");

                            val fishsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(fishsData != null){
                                fishsData.clear()
                            }

                            fishsdataList(fishsData,fishsdata)

                            listView1.adapter = fishAdapter

                            fishAdapter.notifyDataSetChanged()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                INSECT_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"INDI_CNT" ,"OBS_STAT" ,"OBS_ST_ETC" ,"USE_TAR" ,"USER_TA_ETC" ,"MJ_ACT" ,"MJ_ACT_ETC" ,"INV_MEAN" ,"INV_MN_ETC" ,"UNUS_NOTE"
                                    ,"GPS_LAT","GPS_LON","NUM","TEMP_YN");

                            val insectsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(insectData != null){
                                insectData.clear()
                            }

                            insectsdataList(insectData,insectsdata)

                            listView1.adapter = insectAdapter

                            insectAdapter.notifyDataSetChanged()

                        }

                        if(data!!.getIntExtra("export" , 0) != null){
                            var intent = Intent()

                            val export = data!!.getIntExtra("export",0)

                            intent.putExtra("export",export)
                            setResult(RESULT_OK, intent)

                            finish()
                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }

                FLORA_DATA -> {

                    if (data!!.getIntExtra("reset",0) != null) {

                        val reset = data!!.getIntExtra("reset",0)
                        println("listdata reset $reset")
                        if(reset == 100){

                            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
                                    ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"FLORE_YN" ,"PLANT_YN" ,"HAB_STAT" ,"HAB_ETC" ,"COL_IN_CNT" ,"THRE_CAU" ,"GPS_LAT","GPS_LON" ,"TEMP_YN");

                            val florasdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

                            if(florasData != null){
                                florasData.clear()
                            }

                            florasdataList(florasData,florasdata)

                            listView1.adapter = floraAdapter

                            floraAdapter.notifyDataSetChanged()

                        }

                    }

                    if (data!!.getStringExtra("markerid") != null){

                        val markerpk = data!!.getStringExtra("markerid")
                        println("datalist --------markerpk $markerpk")

                        var intent = Intent()
                        intent.putExtra("markerid", markerpk)
                        setResult(RESULT_OK, intent);

                        finish()
                    }

                    if(data!!.getIntExtra("export" , 0) != null){
                        var intent = Intent()

                        val export = data!!.getIntExtra("export",0)

                        intent.putExtra("export",export)
                        setResult(RESULT_OK, intent)

                        finish()
                    }

                }
            }
        }
    }
}
