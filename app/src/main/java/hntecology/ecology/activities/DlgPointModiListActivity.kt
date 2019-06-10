package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.adapter.*
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_dlg_point_modi_list.*
import kotlinx.android.synthetic.main.activity_fish.*
import kotlinx.android.synthetic.main.activity_fish.coordedET
import kotlinx.android.synthetic.main.activity_fish.coordemET
import kotlinx.android.synthetic.main.activity_fish.coordesET
import kotlinx.android.synthetic.main.activity_fish.coordndET
import kotlinx.android.synthetic.main.activity_fish.coordnmET
import kotlinx.android.synthetic.main.activity_fish.coordnsET
import kotlinx.android.synthetic.main.activity_fish.prjnameET
import kotlinx.android.synthetic.main.activity_insect.*
import java.text.SimpleDateFormat
import java.util.*


class DlgPointModiListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1: ArrayList<Birds_attribute>
    private lateinit var listdata2: ArrayList<Birds_attribute>
    private lateinit var listdata3: ArrayList<Birds_attribute>
    private lateinit var listAdapte1: DlgPointModiAdapter

    private lateinit var listdata_rep1: ArrayList<Reptilia_attribute>
    private lateinit var listdata_rep2: ArrayList<Reptilia_attribute>
    private lateinit var listdata_rep3: ArrayList<Reptilia_attribute>
    private lateinit var listrepAdapter: DlgPointModiRepterAdapter

    private lateinit var listdata_mal1: ArrayList<Mammal_attribute>
    private lateinit var listdata_mal2: ArrayList<Mammal_attribute>
    private lateinit var listdata_mal3: ArrayList<Mammal_attribute>
    private lateinit var listmamalAdapter: DlgPointModiMamalAdapter

    private lateinit var listdata_fish1: ArrayList<Fish_attribute>
    private lateinit var listdata_fish2: ArrayList<Fish_attribute>
    private lateinit var listdata_fish3: ArrayList<Fish_attribute>
    private lateinit var listfishAdapter: DlgPointModiFishAdapter

    private lateinit var listdata_insect1: ArrayList<Insect_attribute>
    private lateinit var listdata_insect2: ArrayList<Insect_attribute>
    private lateinit var listdata_insect3: ArrayList<Insect_attribute>
    private lateinit var listinsectAdapter: DlgPointModiInsectAdapter


    private lateinit var listdata_Flora1: ArrayList<Flora_Attribute>
    private lateinit var listdata_Flora2: ArrayList<Flora_Attribute>
    private lateinit var listdata_Flora3: ArrayList<Flora_Attribute>
    private lateinit var listFloraAdapter: DlgPointModiFloraAdapter

    private lateinit var listdata_Zoo1: ArrayList<Zoobenthos_Attribute>
    private lateinit var listdata_Zoo2: ArrayList<Zoobenthos_Attribute>
    private lateinit var listdata_Zoo3: ArrayList<Zoobenthos_Attribute>
    private lateinit var listZooAdapter: DlgPointModiZooAdapter


    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var DlgHeight: Float = 430F
    var dbManager: DataBaseHelper? = null
    var chkData = false
    var polygonid = ""
    var grop_id = ""
    var table_name = ""
    var geom = ""

    var lat = ""
    var log = ""

    var GPSLAT_DEG_RE = ""
    var GPSLAT_MIN_RE = ""
    var GPSLAT_SEC_RE = ""
    var GPSLON_DEG_RE = ""
    var GPSLON_MIN_RE = ""
    var GPSLON_SEC_RE = ""
    var r_region = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_point_modi_list)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        val db = dbManager!!.createDataBase();

        val intent = getIntent()

        DlgHeight = intent.getFloatExtra("DlgHeight", 430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        grop_id = intent.getStringExtra("GROP_ID")
        if (intent.getStringExtra("latitude") != null) {
            lat = intent.getStringExtra("latitude")
            convert(lat.toDouble())

        }

        if (intent.getStringExtra("longitude") != null) {
            log = intent.getStringExtra("longitude")
            logconvert(log.toDouble())

        }

        if (intent.getStringExtra("polygonid") != null) {
            polygonid = intent.getStringExtra("polygonid")

        }
        if (intent.getStringExtra("table") != null) {
            table_name = intent.getStringExtra("table")
            println("폴리아이디 ---------$table_name")
        }
        if (intent.getStringExtra("title") != null) {
            titleTV.text = intent.getStringExtra("title")
            println("폴리아이디 ---------$table_name")
        }
        if (intent.getStringExtra("geom") != null) {
            geom = intent.getStringExtra("geom")
            var geom_sp = geom.split(" ")
            if (geom_sp.size > 1) {
                lat = geom_sp[0]
                log = geom_sp[1]
                convert(lat.toDouble())
                logconvert(log.toDouble())
            }

            println("---------점스스스 $geom")
        }


        r_region = ""



        listView1 = findViewById(R.id.modiLV)

        listdata3 = ArrayList()
        listdata2 = ArrayList()
        listdata1 = ArrayList()

        listdata_rep3 = ArrayList()
        listdata_rep2 = ArrayList()
        listdata_rep1 = ArrayList()

        listdata_mal1 = ArrayList()
        listdata_mal2 = ArrayList()
        listdata_mal3 = ArrayList()

        listdata_fish1 = ArrayList()
        listdata_fish2 = ArrayList()
        listdata_fish3 = ArrayList()

        listdata_insect1 = ArrayList()
        listdata_insect2 = ArrayList()
        listdata_insect3 = ArrayList()

        listdata_Flora1 = ArrayList()
        listdata_Flora2 = ArrayList()
        listdata_Flora3 = ArrayList()

        listdata_Zoo1 = ArrayList()
        listdata_Zoo2 = ArrayList()
        listdata_Zoo3 = ArrayList()


        val dataList: Array<String> = arrayOf("$table_name.*", "min(id) as minId");
        val dataList2: Array<String> = arrayOf("*");
        val data1 = db.query(table_name, dataList, null, null, "GROP_ID", null, "minId asc", null);
        val data2 = db.query(table_name, dataList2, null, null, null, null, "id asc", null);
        if (table_name == "birdsAttribute") {
            dataList(listdata1, data1)
            dataList(listdata2, data2)
            listAdapte1 = DlgPointModiAdapter(context, listdata1, listdata2);

            listView1.adapter = listAdapte1

            var attribute = null_birds_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var birds_data = listAdapte1.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${birds_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList(listdata3, data)
                dbManager!!.delete_grop_birds_attribute(attribute, grop_id)
                for (i in 0..listdata3.size - 1) {
                    Log.d("버드데이터", listdata3[i].SPEC_NM)
                    addbirds(attribute, listdata3[i].SPEC_NM.toString()
                            , listdata3[i].FAMI_NM.toString(), listdata3[i].SCIEN_NM.toString()
                            , listdata3[i].INV_REGION.toString(), listdata3[i].WEATHER.toString()
                            , listdata3[i].WIND.toString(), listdata3[i].WIND_DIRE.toString(), listdata3[i].TEMPERATUR.toString()
                            , listdata3[i].ETC.toString(), listdata3[i].NUM.toString(), listdata3[i].ENDANGERED.toString()
                            , listdata3[i].INDI_CNT.toString(), listdata3[i].OBS_STAT.toString(), listdata3[i].USE_TAR.toString(), listdata3[i].USE_LAYER.toString()
                            , listdata3[i].MJ_ACT.toString(), listdata3[i].MJ_ACT_PR.toString(), listdata3[i].GPS_LAT.toString(), listdata3[i].GPS_LON.toString()
                            , listdata3[i].GPSLAT_DEG.toString(), listdata3[i].GPSLAT_MIN.toString(), listdata3[i].GPSLAT_SEC.toString(), listdata3[i].GPSLON_DEG.toString()
                            , listdata3[i].GPSLON_MIN.toString(), listdata3[i].GPSLON_SEC.toString())
                }
                last_finish()
            }
        } else if (table_name == "reptiliaAttribute") {
            dataList_rep(listdata_rep1, data1)
            dataList_rep(listdata_rep2, data2)
            listrepAdapter = DlgPointModiRepterAdapter(context, listdata_rep1, listdata_rep2);

            listView1.adapter = listrepAdapter

            var attribute = null_rep_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listrepAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList_rep(listdata_rep3, data)
                dbManager!!.delete_grop_reptilia_attribute(attribute, grop_id)
                for (i in 0..listdata_rep3.size - 1) {
                    Log.d("버드데이터", listdata_rep3[i].SPEC_NM)
                    addrepter(attribute, listdata_rep3[i].WEATHER.toString(), listdata_rep3[i].WIND.toString(), listdata_rep3[i].WIND_DIRE.toString()
                            , listdata_rep3[i].TEMPERATUR.toString(), listdata_rep3[i].ETC.toString(), listdata_rep3[i].NUM.toString(), listdata_rep3[i].INV_TM.toString(), listdata_rep3[i].SPEC_NM.toString(), listdata_rep3[i].FAMI_NM.toString(), listdata_rep3[i].SCIEN_NM.toString(), listdata_rep3[i].ENDANGERED.toString(), listdata_rep3[i].IN_CNT_ADU.toString(), listdata_rep3[i].IN_CNT_LAR.toString(), listdata_rep3[i].IN_CNT_EGG.toString()
                            , listdata_rep3[i].HAB_RIVEER.toString(), listdata_rep3[i].HAB_EDGE.toString(), listdata_rep3[i].WATER_IN.toString(), listdata_rep3[i].WATER_OUT.toString(), listdata_rep3[i].WATER_CONT.toString(), listdata_rep3[i].WATER_QUAL.toString(), listdata_rep3[i].WATER_DEPT.toString()
                            , listdata_rep3[i].HAB_AREA_W.toString(), listdata_rep3[i].HAB_AREA_H.toString(), listdata_rep3[i].GPS_LAT.toString(), listdata_rep3[i].GPS_LON.toString(), listdata_rep3[i].TEMP_YN.toString(), listdata_rep3[i].CONF_MOD.toString(), listdata_rep3[i].GEOM.toString()
                            , listdata_rep3[i].GPSLAT_DEG.toString(), listdata_rep3[i].GPSLAT_MIN.toString(), listdata_rep3[i].GPSLAT_SEC.toString(), listdata_rep3[i].GPSLON_DEG.toString(), listdata_rep3[i].GPSLON_MIN.toString(), listdata_rep3[i].GPSLON_SEC.toString(), listdata_rep3[i].HAB_AREA.toString()
                            , listdata_rep3[i].MAC_ADDR.toString(), listdata_rep3[i].CURRENT_TM.toString())
                }
                last_finish()
            }
        } else if (table_name == "mammalAttribute") {
            dataList_mal(listdata_mal1, data1)
            dataList_mal(listdata_mal2, data2)
            listmamalAdapter = DlgPointModiMamalAdapter(context, listdata_mal1, listdata_mal2);

            listView1.adapter = listmamalAdapter

            var attribute = null_mal_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listmamalAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList_mal(listdata_mal3, data)
                dbManager!!.delete_grop_mamal_attribute(attribute, grop_id)
                for (i in 0..listdata_mal3.size - 1) {
                    Log.d("버드데이터", listdata_mal3[i].SPEC_NM)
                    addmamal(attribute, listdata_mal3[i].WEATHER.toString(), listdata_mal3[i].WIND.toString(), listdata_mal3[i].WIND_DIRE.toString(), listdata_mal3[i].TEMPERATUR.toString()
                            , listdata_mal3[i].ETC.toString(), listdata_mal3[i].NUM.toString(), listdata_mal3[i].INV_TM.toString(), listdata_mal3[i].SPEC_NM.toString(), listdata_mal3[i].FAMI_NM.toString()
                            , listdata_mal3[i].SCIEN_NM.toString(), listdata_mal3[i].ENDANGERED.toString(), listdata_mal3[i].OBS_TY.toString(), listdata_mal3[i].INDI_CNT.toString()
                            , listdata_mal3[i].OB_PT_CHAR.toString(), listdata_mal3[i].UNUS_NOTE.toString(), listdata_mal3[i].STANDARD.toString(), listdata_mal3[i].GPS_LAT.toString(), listdata_mal3[i].GPS_LON.toString()
                            , listdata_mal3[i].UN_SPEC.toString(), listdata_mal3[i].UN_SPEC_RE.toString(), listdata_mal3[i].TR_EASY.toString(), listdata_mal3[i].TR_EASY_RE.toString(), listdata_mal3[i].TEMP_YN.toString()
                            , listdata_mal3[i].CONF_MOD.toString(), listdata_mal3[i].GEOM.toString(), listdata_mal3[i].GPSLAT_DEG.toString(), listdata_mal3[i].GPSLAT_MIN.toString(), listdata_mal3[i].GPSLAT_SEC.toString()
                            , listdata_mal3[i].GPSLON_DEG.toString(), listdata_mal3[i].GPSLON_MIN.toString(), listdata_mal3[i].GPSLON_SEC.toString(), listdata_mal3[i].MJ_ACT_PR.toString()
                            , listdata_mal3[i].MAC_ADDR.toString(), listdata_mal3[i].CURRENT_TM.toString())
                }
                last_finish()
            }
        }
        else if (table_name == "fishAttribute") {
            dataList_fish(listdata_fish1, data1)
            dataList_fish(listdata_fish2, data2)
            listfishAdapter = DlgPointModiFishAdapter(context, listdata_fish1, listdata_fish2);

            listView1.adapter = listfishAdapter

            var attribute = null_fish_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listfishAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList_fish(listdata_fish3, data)
                dbManager!!.delete_grop_fish_attribute(attribute, grop_id)
                for (i in 0..listdata_fish3.size - 1) {
                    Log.d("버드데이터", listdata_fish3[i].SPEC_NM)
                    addfish(attribute, listdata_fish3[i].WEATHER.toString(), listdata_fish3[i].WIND.toString(), listdata_fish3[i].WIND_DIRE.toString()
                            , listdata_fish3[i].TEMPERATUR.toString(), listdata_fish3[i].ETC.toString(), listdata_fish3[i].MID_RAGE.toString(), listdata_fish3[i].CODE_NUM.toString(), listdata_fish3[i].RIVER_NUM.toString()
                            , listdata_fish3[i].RIVER_NM.toString(), listdata_fish3[i].GPS_LAT.toString()
                            , listdata_fish3[i].GPS_LON.toString(), listdata_fish3[i].COLL_TOOL.toString(), listdata_fish3[i].COLL_TOOL2.toString(), listdata_fish3[i].STREAM_W.toString(), listdata_fish3[i].WATER_W.toString()
                            , listdata_fish3[i].WATER_D.toString(), listdata_fish3[i].WATER_CUR.toString(), listdata_fish3[i].RIV_STR.toString(), listdata_fish3[i].RIV_STR_IN.toString(), listdata_fish3[i].BOULDER.toString()
                            , listdata_fish3[i].COBBLE.toString(), listdata_fish3[i].PEBBLE.toString(), listdata_fish3[i].GRAVEL.toString(), listdata_fish3[i].SEND.toString(), listdata_fish3[i].RIV_FORM.toString()
                            , listdata_fish3[i].NUM.toString(), listdata_fish3[i].SPEC_NM.toString(), listdata_fish3[i].FAMI_NM.toString(), listdata_fish3[i].SCIEN_NM.toString(), listdata_fish3[i].INDI_CNT.toString()
                            , listdata_fish3[i].UNIDENT.toString(), listdata_fish3[i].RIV_FM_CH.toString(), listdata_fish3[i].UN_FISH_CH.toString(), listdata_fish3[i].TEMP_YN.toString(), listdata_fish3[i].CONF_MOD.toString()
                            , listdata_fish3[i].GEOM.toString(), listdata_fish3[i].GPSLAT_DEG.toString(), listdata_fish3[i].GPSLAT_MIN.toString(), listdata_fish3[i].GPSLAT_SEC.toString(), listdata_fish3[i].GPSLON_DEG.toString()
                            , listdata_fish3[i].GPSLON_MIN.toString(), listdata_fish3[i].GPSLON_SEC.toString(), listdata_fish3[i].RIVER_BED.toString(), listdata_fish3[i].COLL_TIME.toString()
                            , listdata_fish3[i].MAC_ADDR.toString(), listdata_fish3[i].CURRENT_TM.toString())
                }
                last_finish()
            }
        } else if (table_name == "insectAttribute") {
            dataList_insect(listdata_insect1, data1)
            dataList_insect(listdata_insect2, data2)
            listinsectAdapter = DlgPointModiInsectAdapter(context, listdata_insect1, listdata_insect2);

            listView1.adapter = listinsectAdapter

            var attribute = null_insect_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listinsectAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList_insect(listdata_insect3, data)
                dbManager!!.delete_grop_insect_attribute(attribute, grop_id)
                for (i in 0..listdata_insect3.size - 1) {
                    Log.d("버드데이터", listdata_insect3[i].SPEC_NM)
                    addinsect(attribute, listdata_insect3[i].WEATHER.toString(), listdata_insect3[i].WIND.toString(), listdata_insect3[i].WIND_DIRE.toString(), listdata_insect3[i].TEMPERATUR.toString()
                            , listdata_insect3[i].ETC.toString(), listdata_insect3[i].NUM.toString(), listdata_insect3[i].INV_TM.toString(), listdata_insect3[i].SPEC_NM.toString(), listdata_insect3[i].FAMI_NM.toString()
                            , listdata_insect3[i].SCIEN_NM.toString(), listdata_insect3[i].INDI_CNT.toString(), listdata_insect3[i].OBS_STAT.toString(), listdata_insect3[i].USE_TAR.toString()
                            , listdata_insect3[i].MJ_ACT.toString(), listdata_insect3[i].INV_MEAN.toString()
                            , listdata_insect3[i].UNUS_NOTE.toString(), listdata_insect3[i].GPS_LAT.toString(), listdata_insect3[i].GPS_LON.toString(), listdata_insect3[i].TEMP_YN.toString(), listdata_insect3[i].CONF_MOD.toString()
                            , listdata_insect3[i].GEOM.toString(), listdata_insect3[i].GPSLAT_DEG.toString(), listdata_insect3[i].GPSLAT_MIN.toString(), listdata_insect3[i].GPSLAT_SEC.toString()
                            , listdata_insect3[i].GPSLON_DEG.toString(), listdata_insect3[i].GPSLON_MIN.toString(), listdata_insect3[i].GPSLON_SEC.toString()
                            , listdata_insect3[i].MAC_ADDR.toString(), listdata_insect3[i].CURRENT_TM.toString())
                }
                last_finish()
            }
        } else if (table_name == "floraAttribute") {
            dataList_flora(listdata_Flora1, data1)
            dataList_flora(listdata_Flora2, data2)
            listFloraAdapter = DlgPointModiFloraAdapter(context, listdata_Flora1, listdata_Flora2);

            listView1.adapter = listFloraAdapter

            var attribute = null_flora_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listFloraAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList_flora(listdata_Flora3, data)
                dbManager!!.delete_grop_flora_attribute(attribute, grop_id)
                for (i in 0..listdata_Flora3.size - 1) {
                    Log.d("버드데이터", listdata_Flora3[i].SPEC_NM)
                    addflora(attribute, listdata_Flora3[i].WEATHER.toString(), listdata_Flora3[i].WIND.toString(), listdata_Flora3[i].WIND_DIRE.toString()
                            , listdata_Flora3[i].TEMPERATUR.toString(), listdata_Flora3[i].ETC.toString(), listdata_Flora3[i].NUM.toString(), listdata_Flora3[i].INV_TM.toString(), listdata_Flora3[i].SPEC_NM.toString(), listdata_Flora3[i].FAMI_NM.toString(), listdata_Flora3[i].SCIEN_NM.toString(), listdata_Flora3[i].FLORE_YN.toString(), listdata_Flora3[i].PLANT_YN.toString(), listdata_Flora3[i].HAB_STAT.toString()
                            , listdata_Flora3[i].HAB_ETC.toString(), listdata_Flora3[i].COL_IN_CNT.toString(), listdata_Flora3[i].THRE_CAU.toString(), listdata_Flora3[i].GPS_LAT.toString(), listdata_Flora3[i].GPS_LON.toString(), listdata_Flora3[i].TEMP_YN.toString(), listdata_Flora3[i].CONF_MOD.toString()
                            , listdata_Flora3[i].GEOM.toString(), listdata_Flora3[i].GPSLAT_DEG.toString(), listdata_Flora3[i].GPSLAT_MIN.toString(), listdata_Flora3[i].GPSLAT_SEC.toString(), listdata_Flora3[i].GPSLON_DEG.toString(), listdata_Flora3[i].GPSLON_MIN.toString(), listdata_Flora3[i].GPSLON_SEC.toString()
                            , listdata_Flora3[i].MAC_ADDR.toString(), listdata_Flora3[i].CURRENT_TM.toString())
                }
                last_finish()
            }
        }
        else if (table_name == "ZoobenthosAttribute") {
            dataList_zoo(listdata_Zoo1, data1)
            dataList_zoo(listdata_Zoo2, data2)
            listZooAdapter = DlgPointModiZooAdapter(context, listdata_Zoo1, listdata_Zoo2);

            listView1.adapter = listZooAdapter

            var attribute = null_zoo_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listZooAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                dataList_zoo(listdata_Zoo3, data)
                dbManager!!.delete_grop_zoo_attribute(attribute, grop_id)
                for (i in 0..listdata_Zoo3.size - 1) {
                    Log.d("버드데이터", listdata_Zoo3[i].SPEC_NM)
                    addzoo(attribute,listdata_Zoo3[i].INV_MEAN.toString(),
                    listdata_Zoo3[i].MAP_SYS_NM.toString(),
                    listdata_Zoo3[i].GPSLAT_DEG.toString(),
                    listdata_Zoo3[i].GPSLAT_MIN.toString(),
                    listdata_Zoo3[i].GPSLAT_SEC.toString(),
                    listdata_Zoo3[i].GPSLON_DEG.toString(),
                    listdata_Zoo3[i].GPSLON_MIN.toString(),
                    listdata_Zoo3[i].GPSLON_SEC.toString(),
                    listdata_Zoo3[i].INV_DT.toString(),
                    listdata_Zoo3[i].NUM.toString(),
                    listdata_Zoo3[i].INV_TM.toString(),
                    listdata_Zoo3[i].WEATHER.toString(),
                    listdata_Zoo3[i].INV_TOOL.toString(),
                    listdata_Zoo3[i].AD_DIST_NM.toString(),
                    listdata_Zoo3[i].RIV_W.toString(),
                    listdata_Zoo3[i].RIV_W2.toString(),
                    listdata_Zoo3[i].RUN_RIV_W.toString(),
                    listdata_Zoo3[i].RUN_RIV_W2.toString(),
                    listdata_Zoo3[i].WATER_DEPT.toString(),
                    listdata_Zoo3[i].HAB_TY.toString(),
                    listdata_Zoo3[i].FILT_AREA.toString(),
                    listdata_Zoo3[i].TEMPERATUR.toString(),
                    listdata_Zoo3[i].WATER_TEM.toString(),
                    listdata_Zoo3[i].TURBIDITY.toString(),
                    listdata_Zoo3[i].MUD.toString(),
                    listdata_Zoo3[i].SAND.toString(),
                    listdata_Zoo3[i].COR_SAND.toString(),
                    listdata_Zoo3[i].GRAVEL.toString(),
                    listdata_Zoo3[i].STONE_S.toString(),
                    listdata_Zoo3[i].STONE_B.toString(),
                    listdata_Zoo3[i].CONCRETE.toString(),
                    listdata_Zoo3[i].BED_ROCK.toString(),
                    listdata_Zoo3[i].BANK_L.toString(),
                    listdata_Zoo3[i].BANK_R.toString(),
                    listdata_Zoo3[i].BAS_L.toString(),
                    listdata_Zoo3[i].BAS_R.toString(),
                    listdata_Zoo3[i].DIST_CAU.toString(),
                    listdata_Zoo3[i].UNUS_NOTE.toString(),
                    listdata_Zoo3[i].GPS_LAT.toString(),
                    listdata_Zoo3[i].GPS_LON.toString(),
                    listdata_Zoo3[i].SPEC_NM.toString(),
                    listdata_Zoo3[i].FAMI_NM.toString(),
                    listdata_Zoo3[i].SCIEN_NM.toString(),
                    listdata_Zoo3[i].TEMP_YN.toString(),
                    listdata_Zoo3[i].CONF_MOD.toString(),
                    listdata_Zoo3[i].GEOM.toString(),
                    listdata_Zoo3[i].ZOO_CNT.toString(),
                    listdata_Zoo3[i].MAC_ADDR.toString(),
                    listdata_Zoo3[i].CURRENT_TM.toString())
                }
                last_finish()
            }
        }
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

    fun addbirds(attribute: Birds_attribute, spec: String, fami: String, scien: String, region: String, WEATHER: String
                 , WIND: String, WIND_DIRE: String, TEMPERATUR: String, ETC: String, NUM: String
                 , ENDANGERED: String, INDI_CNT: String, OBS_STAT: String, USE_TAR: String, USE_LAYER: String
                 , MJ_ACT: String, MJ_ACT_PR: String, GPS_LAT: String, GPS_LON: String, GPSLAT_DEG: String
                 , GPSLAT_MIN: String, GPSLAT_SEC: String, GPSLON_DEG: String, GPSLON_MIN: String, GPSLON_SEC: String) {
        Log.d("인서트비오톱33", geom.toString());
        attribute.GROP_ID = grop_id
        attribute.INV_REGION = r_region
        attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
        attribute.INV_DT = Utils.todayStr()
        attribute.INV_TM = Utils.timeStr()
        attribute.TEMP_YN = "Y"
        attribute.CONF_MOD = "M"
        attribute.GEOM = geom
        attribute.SPEC_NM = spec
        attribute.FAMI_NM = fami
        attribute.SCIEN_NM = scien
        attribute.WEATHER = WEATHER
        attribute.WIND = WIND
        attribute.WIND_DIRE = WIND_DIRE
        attribute.TEMPERATUR = TEMPERATUR.toString().toFloat()
        attribute.ETC = ETC
        attribute.NUM = NUM.toInt()
        attribute.ENDANGERED = ENDANGERED
        attribute.INDI_CNT = INDI_CNT.toInt()
        attribute.OBS_STAT = OBS_STAT
        attribute.USE_TAR = USE_TAR
        attribute.USE_LAYER = USE_LAYER
        attribute.MJ_ACT = MJ_ACT
        attribute.MJ_ACT_PR = MJ_ACT_PR
        attribute.GPS_LAT = lat.toFloat()
        attribute.GPS_LON = log.toFloat()
        attribute.GPSLAT_DEG = GPSLAT_DEG_RE.toInt()
        attribute.GPSLAT_MIN = GPSLAT_MIN_RE.toInt()
        attribute.GPSLAT_SEC = GPSLAT_SEC_RE.toFloat()
        attribute.GPSLON_DEG = GPSLON_DEG_RE.toInt()
        attribute.GPSLON_MIN = GPSLON_MIN_RE.toInt()
        attribute.GPSLON_SEC = GPSLON_SEC_RE.toString().toFloat()

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        attribute.CURRENT_TM = getTime.substring(2, 14)
        attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertbirds_attribute(attribute);
    }

    fun null_birds_attribute(): Birds_attribute {
        var birds_attribute: Birds_attribute = Birds_attribute(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null)

        return birds_attribute
    }

    fun dataList(listdata: ArrayList<Birds_attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Birds_attribute;

            model = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
                    , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
                    , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
            )
            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }

        }
    }

    fun addrepter(reptilia_attribute: Reptilia_attribute, WEATHER: String?, WIND: String?, WIND_DIRE: String?
                  , TEMPERATUR: String?, ETC: String?, NUM: String?, INV_TM: String?, SPEC_NM: String?, FAMI_NM: String?, SCIEN_NM: String?, ENDANGERED: String?, IN_CNT_ADU: String?, IN_CNT_LAR: String?, IN_CNT_EGG: String?
                  , HAB_RIVEER: String?, HAB_EDGE: String?, WATER_IN: String?, WATER_OUT: String?, WATER_CONT: String?, WATER_QUAL: String?, WATER_DEPT: String?
                  , HAB_AREA_W: String?, HAB_AREA_H: String?, GPS_LAT: String?, GPS_LON: String?, TEMP_YN: String?, CONF_MOD: String?, GEOM: String?
                  , GPSLAT_DEG: String?, GPSLAT_MIN: String?, GPSLAT_SEC: String?, GPSLON_DEG: String?, GPSLON_MIN: String?, GPSLON_SEC: String?, HAB_AREA: String?
                  , MAC_ADDR: String?, CURRENT_TM: String?) {

        reptilia_attribute.GROP_ID = grop_id
        reptilia_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        reptilia_attribute.CURRENT_TM = Utils.current_tm()

        reptilia_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")

        reptilia_attribute.INV_REGION = r_region
        reptilia_attribute.INV_DT = Utils.todayStr()
        reptilia_attribute.INV_TM = Utils.timeStr()
        reptilia_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");

        reptilia_attribute.WEATHER = WEATHER
        reptilia_attribute.WIND = WIND
        reptilia_attribute.WIND_DIRE = WIND_DIRE
        reptilia_attribute.TEMPERATUR = TEMPERATUR!!.toFloat()

        reptilia_attribute.ETC = ETC

        reptilia_attribute.NUM = NUM!!.toInt()

        reptilia_attribute.INV_TM = INV_TM

        reptilia_attribute.SPEC_NM = SPEC_NM
        reptilia_attribute.FAMI_NM = FAMI_NM
        reptilia_attribute.SCIEN_NM = SCIEN_NM
        reptilia_attribute.IN_CNT_ADU = IN_CNT_ADU!!.toInt()
        reptilia_attribute.IN_CNT_LAR = IN_CNT_LAR!!.toInt()
        reptilia_attribute.IN_CNT_EGG = IN_CNT_EGG!!.toInt()

        reptilia_attribute.HAB_RIVEER = HAB_RIVEER
        reptilia_attribute.HAB_EDGE = HAB_EDGE

        reptilia_attribute.WATER_IN = WATER_IN
        reptilia_attribute.WATER_OUT = WATER_OUT

        reptilia_attribute.WATER_CONT = WATER_CONT
        reptilia_attribute.WATER_QUAL = WATER_QUAL

        reptilia_attribute.WATER_DEPT = WATER_DEPT!!.toInt()

        reptilia_attribute.HAB_AREA_W = HAB_AREA_W!!.toInt()

        reptilia_attribute.HAB_AREA_H = HAB_AREA_H!!.toInt()


        reptilia_attribute.GEOM = geom
        reptilia_attribute.CONF_MOD = "N"
        reptilia_attribute.GPS_LAT = lat.toFloat()
        reptilia_attribute.GPS_LON = log.toFloat()
        reptilia_attribute.GPSLAT_DEG = GPSLAT_DEG_RE!!.toInt()
        reptilia_attribute.GPSLAT_MIN = GPSLAT_MIN_RE!!.toInt()
        reptilia_attribute.GPSLAT_SEC = GPSLAT_SEC_RE!!.toFloat()
        reptilia_attribute.GPSLON_DEG = GPSLON_DEG_RE!!.toInt()
        reptilia_attribute.GPSLON_MIN = GPSLON_MIN_RE!!.toInt()
        reptilia_attribute.GPSLON_SEC = GPSLON_SEC_RE!!.toFloat()
        reptilia_attribute.HAB_AREA = HAB_AREA!!.toFloat()
        reptilia_attribute.TEMP_YN = "N"
        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        reptilia_attribute.CURRENT_TM = getTime.substring(2, 14)
        reptilia_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertreptilia_attribute(reptilia_attribute);
    }

    fun null_rep_attribute(): Reptilia_attribute {
        var reptilia_attribute: Reptilia_attribute = Reptilia_attribute(null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
                , null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null)
        return reptilia_attribute
    }

    fun dataList_rep(listdata: ArrayList<Reptilia_attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Reptilia_attribute;

            model = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getInt(17), data.getInt(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                    , data.getString(23), data.getString(24), data.getString(25), data.getInt(26), data.getInt(27), data.getInt(28), data.getFloat(29), data.getFloat(30), data.getString(31), data.getString(32), data.getString(33)
                    , data.getInt(34), data.getInt(35), data.getFloat(36), data.getInt(37), data.getInt(38), data.getFloat(39)
                    , data.getFloat(40), data.getString(41), data.getString(42)
            )
            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }
        }
    }

    fun addmamal(mammal_attribute: Mammal_attribute, WEATHER: String?, WIND: String?, WIND_DIRE: String?, TEMPERATUR: String?
                 , ETC: String?, NUM: String?, INV_TM: String?, SPEC_NM: String?, FAMI_NM: String?
                 , SCIEN_NM: String?, ENDANGERED: String?, OBS_TY: String?, INDI_CNT: String?
                 , OB_PT_CHAR: String?, UNUS_NOTE: String?, STANDARD: String?, GPS_LAT: String?, GPS_LON: String?
                 , UN_SPEC: String?, UN_SPEC_RE: String?, TR_EASY: String?, TR_EASY_RE: String?, TEMP_YN: String?
                 , CONF_MOD: String?, GEOM: String?, GPSLAT_DEG: String?, GPSLAT_MIN: String?, GPSLAT_SEC: String?
                 , GPSLON_DEG: String?, GPSLON_MIN: String?, GPSLON_SEC: String?, MJ_ACT_PR: String?
                 , MAC_ADDR: String?, CURRENT_TM: String?) {
        Log.d("인서트비오톱33", geom.toString());
        mammal_attribute.GROP_ID = grop_id
        mammal_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr").replace(":", "")
        mammal_attribute.CURRENT_TM = Utils.current_tm()

        mammal_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")

        mammal_attribute.INV_REGION = r_region

        mammal_attribute.INV_DT = Utils.todayStr()
        mammal_attribute.INV_TM = Utils.timeStr()
        mammal_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");

        mammal_attribute.WEATHER = WEATHER
        mammal_attribute.WIND = WIND
        mammal_attribute.WIND_DIRE = WIND_DIRE
        mammal_attribute.TEMPERATUR = TEMPERATUR!!.toFloat()

        mammal_attribute.ETC = ETC
        mammal_attribute.NUM = NUM!!.toInt()
        mammal_attribute.GPSLAT_DEG = GPSLAT_DEG_RE!!.toInt()
        mammal_attribute.GPSLAT_MIN = GPSLAT_MIN_RE!!.toInt()
        mammal_attribute.GPSLAT_SEC = GPSLAT_SEC_RE!!.toFloat()
        mammal_attribute.GPSLON_DEG = GPSLON_DEG_RE!!.toInt()
        mammal_attribute.GPSLON_MIN = GPSLON_MIN_RE!!.toInt()
        mammal_attribute.GPSLON_SEC = GPSLON_SEC_RE!!.toFloat()
        mammal_attribute.MJ_ACT_PR = MJ_ACT_PR


        mammal_attribute.SPEC_NM = SPEC_NM
        mammal_attribute.FAMI_NM = FAMI_NM
        mammal_attribute.SCIEN_NM = SCIEN_NM
        mammal_attribute.ENDANGERED = ENDANGERED


        mammal_attribute.OBS_TY = OBS_TY

        mammal_attribute.INDI_CNT = INDI_CNT!!.toInt()

        mammal_attribute.OB_PT_CHAR = OB_PT_CHAR
        mammal_attribute.UNUS_NOTE = UNUS_NOTE

        mammal_attribute.GPS_LAT = lat.toFloat()
        mammal_attribute.GPS_LON = log.toFloat()

        mammal_attribute.GPSLAT_DEG = GPSLAT_DEG_RE!!.toInt()
        mammal_attribute.GPSLAT_MIN = GPSLAT_MIN_RE!!.toInt()
        mammal_attribute.GPSLAT_SEC = GPSLAT_SEC_RE!!.toFloat()
        mammal_attribute.GPSLON_DEG = GPSLON_DEG_RE!!.toInt()
        mammal_attribute.GPSLON_MIN = GPSLON_MIN_RE!!.toInt()
        mammal_attribute.GPSLON_SEC = GPSLON_SEC_RE!!.toFloat()
        mammal_attribute.UN_SPEC = UN_SPEC
        mammal_attribute.UN_SPEC_RE = UN_SPEC_RE

        mammal_attribute.STANDARD = STANDARD

        mammal_attribute.TR_EASY = TR_EASY
        mammal_attribute.TR_EASY_RE = TR_EASY_RE

        mammal_attribute.TEMP_YN = "Y"

        mammal_attribute.CONF_MOD = "N"

        mammal_attribute.GEOM = geom

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        mammal_attribute.CURRENT_TM = getTime.substring(2, 14)
        mammal_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertmammal_attribute(mammal_attribute);
    }

    fun null_mal_attribute(): Mammal_attribute {
        var mammal_attribute: Mammal_attribute = Mammal_attribute(null, null, null, null, null
                , null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null
                , null, null
        )
        return mammal_attribute
    }

    fun dataList_mal(listdata: ArrayList<Mammal_attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Mammal_attribute;

            model = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26), data.getString(27), data.getString(28), data.getString(29), data.getString(30)
                    , data.getInt(31), data.getInt(32), data.getFloat(33), data.getInt(34), data.getInt(35), data.getFloat(36), data.getString(37), data.getString(38), data.getString(39))


            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }

        }
    }

    fun addfish(fish_attribute: Fish_attribute, WEATHER: String?, WIND: String?, WIND_DIRE: String?
                , TEMPERATUR: String?, ETC: String?, MID_RAGE: String?, CODE_NUM: String?, RIVER_NUM: String?
                , RIVER_NM: String?, GPS_LAT: String?
                , GPS_LON: String?, COLL_TOOL: String?, COLL_TOOL2: String?, STREAM_W: String?, WATER_W: String?
                , WATER_D: String?, WATER_CUR: String?, RIV_STR: String?, RIV_STR_IN: String?, BOULDER: String?
                , COBBLE: String?, PEBBLE: String?, GRAVEL: String?, SEND: String?, RIV_FORM: String?
                , NUM: String?, SPEC_NM: String?, FAMI_NM: String?, SCIEN_NM: String?, INDI_CNT: String?
                , UNIDENT: String?, RIV_FM_CH: String?, UN_FISH_CH: String?, TEMP_YN: String?, CONF_MOD: String?
                , GEOM: String?, GPSLAT_DEG: String?, GPSLAT_MIN: String?, GPSLAT_SEC: String?, GPSLON_DEG: String?
                , GPSLON_MIN: String?, GPSLON_SEC: String?, RIVER_BED: String?, COLL_TIME: String?
                , MAC_ADDR: String?, CURRENT_TM: String?) {
        Log.d("인서트비오톱33", geom.toString());
        fish_attribute.GROP_ID = grop_id
        fish_attribute.INV_REGION = r_region
        fish_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        fish_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
        fish_attribute.INV_DT = Utils.todayStr()
        fish_attribute.INV_TM = Utils.timeStr()
        fish_attribute.TEMP_YN = "Y"
        fish_attribute.CONF_MOD = "M"
        fish_attribute.GEOM = geom

        fish_attribute.WEATHER = WEATHER
        fish_attribute.WIND = WIND
        fish_attribute.WIND_DIRE = WIND_DIRE

        fish_attribute.TEMPERATUR = TEMPERATUR!!.toFloat()

        fish_attribute.ETC = ETC

        fish_attribute.MID_RAGE = MID_RAGE
        fish_attribute.CODE_NUM = CODE_NUM

        if (fishrivernumET.text.isNotEmpty()) {
            fish_attribute.RIVER_NUM = fishrivernumET.text.toString()
        }

        fish_attribute.RIVER_NM = fishrivernmET.text.toString()


        fish_attribute.COLL_TIME = COLL_TIME




        fish_attribute.STREAM_W = STREAM_W!!.toInt()

        fish_attribute.WATER_W = WATER_W!!.toInt()

        fish_attribute.WATER_D = WATER_D!!.toInt()

        fish_attribute.WATER_CUR = WATER_CUR!!.toInt()

//            fish_attribute.RIV_STR = rivstrTV.text.toString()
//            fish_attribute.RIV_STR_IN = rivstrdetET.text.toString()

        fish_attribute.RIV_FORM = RIV_FORM

        fish_attribute.NUM = NUM.toString().toInt()

        fish_attribute.SPEC_NM = SPEC_NM
        fish_attribute.FAMI_NM = FAMI_NM
        fish_attribute.SCIEN_NM = SCIEN_NM
        fish_attribute.INDI_CNT = INDI_CNT!!.toInt()

        fish_attribute.UNIDENT = UNIDENT
        fish_attribute.RIV_FM_CH = RIV_FM_CH
        fish_attribute.UN_FISH_CH = UN_FISH_CH


        fish_attribute.TEMP_YN = "N"

        fish_attribute.GPS_LAT = lat.toFloat()
        fish_attribute.GPS_LON = log.toFloat()


        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        fish_attribute.CURRENT_TM = getTime.substring(2, 14)
        fish_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertfish_attribute(fish_attribute);
    }

    fun null_fish_attribute(): Fish_attribute {
        var fish_attribute: Fish_attribute = Fish_attribute(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null)
        return fish_attribute
    }

    fun dataList_fish(listdata: ArrayList<Fish_attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Fish_attribute;

            model = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14), data.getString(15),
                    data.getFloat(16), data.getFloat(17), data.getString(18), data.getString(19), data.getInt(20), data.getInt(21), data.getInt(22), data.getInt(23), data.getString(24), data.getString(25),
                    data.getInt(26), data.getInt(27), data.getInt(28), data.getInt(29), data.getInt(30), data.getString(31), data.getInt(32), data.getString(33), data.getString(34), data.getString(35),
                    data.getInt(36), data.getString(37), data.getString(38), data.getString(39), data.getString(40), data.getString(41), data.getString(42), data.getInt(43), data.getInt(44), data.getFloat(45)
                    , data.getInt(46), data.getInt(47), data.getFloat(48), data.getString(49), data.getString(50), data.getString(51), data.getString(52))
            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }
        }
    }

    fun addinsect(insect_attribute: Insect_attribute, WEATHER: String?, WIND: String?, WIND_DIRE: String?, TEMPERATUR: String?
                  , ETC: String?, NUM: String?, INV_TM: String?, SPEC_NM: String?, FAMI_NM: String?
                  , SCIEN_NM: String?, INDI_CNT: String?, OBS_STAT: String?, USE_TAR: String?
                  , MJ_ACT: String?, INV_MEAN: String?
                  , UNUS_NOTE: String?, GPS_LAT: String?, GPS_LON: String?, TEMP_YN: String?, CONF_MOD: String?
                  , GEOM: String?, GPSLAT_DEG: String?, GPSLAT_MIN: String?, GPSLAT_SEC: String?
                  , GPSLON_DEG: String?, GPSLON_MIN: String?, GPSLON_SEC: String?
                  , MAC_ADDR: String?, CURRENT_TM: String?) {
        Log.d("인서트비오톱33", geom.toString());
        insect_attribute.GROP_ID = grop_id
        insect_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        insect_attribute.CURRENT_TM = Utils.current_tm()

        insect_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        insect_attribute.INV_DT = Utils.todayStr()
        insect_attribute.INV_TM = Utils.timeStr()
        insect_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name")

        insect_attribute.WEATHER = WEATHER
        insect_attribute.WIND = WIND
        insect_attribute.WIND_DIRE = WIND_DIRE

        insect_attribute.TEMPERATUR = TEMPERATUR!!.toFloat()

        insect_attribute.ETC = ETC

        insect_attribute.NUM = NUM!!.toInt()


        insect_attribute.SPEC_NM = SPEC_NM

        insect_attribute.FAMI_NM = FAMI_NM
        insect_attribute.SCIEN_NM = SCIEN_NM

        insect_attribute.INDI_CNT = INDI_CNT!!.toInt()

        insect_attribute.OBS_STAT = OBS_STAT

        insect_attribute.USE_TAR = USE_TAR

        insect_attribute.MJ_ACT = MJ_ACT

        insect_attribute.INV_MEAN = INV_MEAN


        insect_attribute.GPSLAT_DEG = GPSLAT_DEG_RE!!.toInt()
        insect_attribute.GPSLAT_MIN = GPSLAT_MIN_RE!!.toInt()
        insect_attribute.GPSLAT_SEC = GPSLAT_SEC_RE!!.toFloat()
        insect_attribute.GPSLON_DEG = GPSLON_DEG_RE!!.toInt()
        insect_attribute.GPSLON_MIN = GPSLON_MIN_RE!!.toInt()
        insect_attribute.GPSLON_SEC = GPSLON_SEC_RE!!.toFloat()

        insect_attribute.UNUS_NOTE = UNUS_NOTE

        insect_attribute.GPS_LAT = lat.toFloat()
        insect_attribute.GPS_LON = log.toFloat()

        insect_attribute.TEMP_YN = "N"

        insect_attribute.CONF_MOD = "N"


        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        insect_attribute.CURRENT_TM = getTime.substring(2, 14)
        insect_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertinsect_attribute(insect_attribute);
    }

    fun null_insect_attribute(): Insect_attribute {
        var insect_attribute: Insect_attribute = Insect_attribute(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null)
        return insect_attribute
    }

    fun dataList_insect(listdata: ArrayList<Insect_attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Insect_attribute;

            model = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4)
                    , data.getString(5), data.getString(6), data.getString(7), data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11)
                    , data.getString(12), data.getString(13), data.getString(14), data.getString(15), data.getInt(16), data.getString(17)
                    , data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26)
                    , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31), data.getFloat(32)
                    , data.getString(33), data.getString(34))
            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }
        }
    }


    fun addflora(flora_Attribute: Flora_Attribute, WEATHER: String?, WIND: String?, WIND_DIRE: String?
                 , TEMPERATUR: String?, ETC: String?, NUM: String?, INV_TM: String?, SPEC_NM: String?, FAMI_NM: String?, SCIEN_NM: String?, FLORE_YN: String?, PLANT_YN: String?, HAB_STAT: String?
                 , HAB_ETC: String?, COL_IN_CNT: String?, THRE_CAU: String?, GPS_LAT: String?, GPS_LON: String?, TEMP_YN: String?, CONF_MOD: String?
                 , GEOM: String?, GPSLAT_DEG: String?, GPSLAT_MIN: String?, GPSLAT_SEC: String?, GPSLON_DEG: String?, GPSLON_MIN: String?, GPSLON_SEC: String?
                 , MAC_ADDR: String?, CURRENT_TM: String?) {
        Log.d("인서트비오톱33", geom.toString());
        flora_Attribute.GROP_ID = grop_id

        flora_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")

        flora_Attribute.CURRENT_TM = Utils.current_tm()
        flora_Attribute.PRJ_NAME = flora_Attribute.PRJ_NAME
        flora_Attribute.INV_REGION = r_region

        flora_Attribute.INV_DT = Utils.todayStr()
        flora_Attribute.INV_TM = Utils.timeStr()
        flora_Attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name")

        flora_Attribute.WEATHER = WEATHER
        flora_Attribute.WIND = WIND
        flora_Attribute.WIND_DIRE = WIND_DIRE

        flora_Attribute.NUM = NUM!!.toInt()
        flora_Attribute.TEMPERATUR = TEMPERATUR!!.toFloat()

        flora_Attribute.GPSLAT_DEG = GPSLAT_DEG_RE!!.toInt()
        flora_Attribute.GPSLAT_MIN = GPSLAT_MIN_RE!!.toInt()
        flora_Attribute.GPSLAT_SEC = GPSLAT_SEC_RE!!.toFloat()
        flora_Attribute.GPSLON_DEG = GPSLON_DEG_RE!!.toInt()
        flora_Attribute.GPSLON_MIN = GPSLON_MIN_RE!!.toInt()
        flora_Attribute.GPSLON_SEC = GPSLON_SEC_RE!!.toFloat()
        flora_Attribute.ETC = ETC


        flora_Attribute.SPEC_NM = SPEC_NM
        flora_Attribute.FAMI_NM = FAMI_NM
        flora_Attribute.SCIEN_NM = SCIEN_NM

        flora_Attribute.FLORE_YN = FLORE_YN
        flora_Attribute.PLANT_YN = PLANT_YN


        flora_Attribute.HAB_STAT = HAB_STAT
        flora_Attribute.HAB_ETC = HAB_ETC


        flora_Attribute.COL_IN_CNT = COL_IN_CNT!!.toInt()

        flora_Attribute.THRE_CAU = THRE_CAU


        flora_Attribute.GPS_LAT = lat.toFloat()
        flora_Attribute.GPS_LON = log.toFloat()


        flora_Attribute.TEMP_YN = "N"


        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        flora_Attribute.CURRENT_TM = getTime.substring(2, 14)
        flora_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertflora_attribute(flora_Attribute);
    }

    fun null_flora_attribute(): Flora_Attribute {
        var flora_Attribute: Flora_Attribute = Flora_Attribute(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null)
        return flora_Attribute
    }

    fun dataList_flora(listdata: ArrayList<Flora_Attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Flora_Attribute;

            model = Flora_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                    , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26)
                    , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31), data.getFloat(32), data.getString(33), data.getString(34))
            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }
        }
    }


    fun addzoo(zoobenthos_Attribute: Zoobenthos_Attribute,
               INV_MEAN: String?,
               MAP_SYS_NM: String?,
               GPSLAT_DEG: String?,
               GPSLAT_MIN: String?,
               GPSLAT_SEC: String?,
               GPSLON_DEG: String?,
               GPSLON_MIN: String?,
               GPSLON_SEC: String?,
               INV_DT: String?,
               NUM: String?,
               INV_TM: String?,
               WEATHER: String?,
               INV_TOOL: String?,
               AD_DIST_NM: String?,
               RIV_W: String?,
               RIV_W2: String?,
               RUN_RIV_W: String?,
               RUN_RIV_W2: String?,
               WATER_DEPT: String?,
               HAB_TY: String?,
               FILT_AREA: String?,
               TEMPERATUR: String?,
               WATER_TEM: String?,
               TURBIDITY: String?,
               MUD: String?,
               SAND: String?,
               COR_SAND: String?,
               GRAVEL: String?,
               STONE_S: String?,
               STONE_B: String?,
               CONCRETE: String?,
               BED_ROCK: String?,
               BANK_L: String?,
               BANK_R: String?,
               BAS_L: String?,
               BAS_R: String?,
               DIST_CAU: String?,
               UNUS_NOTE: String?,
               GPS_LAT: String?,
               GPS_LON: String?, SPEC_NM: String?,
               FAMI_NM: String?,
               SCIEN_NM: String?, TEMP_YN: String?,
               CONF_MOD: String?, GEOM: String?,
               ZOO_CNT: String?,
               MAC_ADDR: String?, CURRENT_TM: String?) {


        zoobenthos_Attribute.GROP_ID = grop_id
        zoobenthos_Attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        zoobenthos_Attribute.CURRENT_TM = Utils.current_tm()
        zoobenthos_Attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")

        zoobenthos_Attribute.INV_REGION = r_region
        zoobenthos_Attribute.INV_MEAN = INV_MEAN
        zoobenthos_Attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name")
        zoobenthos_Attribute.MAP_SYS_NM = MAP_SYS_NM

        zoobenthos_Attribute.GPSLAT_DEG = GPSLAT_DEG_RE!!.toInt()
        zoobenthos_Attribute.GPSLAT_MIN = GPSLAT_MIN_RE!!.toInt()
        zoobenthos_Attribute.GPSLAT_SEC = GPSLAT_SEC_RE!!.toFloat()
        zoobenthos_Attribute.GPSLON_DEG = GPSLON_DEG_RE!!.toInt()
        zoobenthos_Attribute.GPSLON_MIN = GPSLON_MIN_RE!!.toInt()
        zoobenthos_Attribute.GPSLON_SEC = GPSLON_SEC_RE!!.toFloat()
        zoobenthos_Attribute.ZOO_CNT = ZOO_CNT!!.toInt()
        zoobenthos_Attribute.NUM = NUM

        zoobenthos_Attribute.INV_DT = Utils.todayStr()
        zoobenthos_Attribute.INV_TM = Utils.timeStr()
        zoobenthos_Attribute.WEATHER = WEATHER
        zoobenthos_Attribute.INV_TOOL = INV_TOOL
        zoobenthos_Attribute.AD_DIST_NM = AD_DIST_NM
        zoobenthos_Attribute.RIV_W = RIV_W
        zoobenthos_Attribute.RIV_W2 = RIV_W2!!.toInt()
        zoobenthos_Attribute.RUN_RIV_W = RUN_RIV_W
        zoobenthos_Attribute.RUN_RIV_W2 = RUN_RIV_W2!!.toInt()
        zoobenthos_Attribute.WATER_DEPT = WATER_DEPT!!.toInt()
        zoobenthos_Attribute.HAB_TY = HAB_TY
        zoobenthos_Attribute.FILT_AREA = FILT_AREA
        zoobenthos_Attribute.TEMPERATUR = TEMPERATUR!!.toFloat()
        zoobenthos_Attribute.WATER_TEM = WATER_TEM!!.toFloat()
        zoobenthos_Attribute.TURBIDITY = TURBIDITY
        zoobenthos_Attribute.MUD = MUD!!.toFloat()
        zoobenthos_Attribute.SAND = SAND!!.toFloat()
        zoobenthos_Attribute.COR_SAND = COR_SAND!!.toFloat()
        zoobenthos_Attribute.GRAVEL = GRAVEL!!.toFloat()
        zoobenthos_Attribute.STONE_S = STONE_S!!.toFloat()
        zoobenthos_Attribute.STONE_B = STONE_B!!.toFloat()
        zoobenthos_Attribute.CONCRETE = CONCRETE!!.toFloat()
        zoobenthos_Attribute.BED_ROCK = BED_ROCK!!.toFloat()
        zoobenthos_Attribute.BANK_L = BANK_L
        zoobenthos_Attribute.BANK_R = BANK_R
        zoobenthos_Attribute.BAS_L = BAS_L
        zoobenthos_Attribute.BAS_R = BAS_R
        zoobenthos_Attribute.DIST_CAU = DIST_CAU
        zoobenthos_Attribute.UNUS_NOTE = UNUS_NOTE

        zoobenthos_Attribute.TEMP_YN = "Y"

        zoobenthos_Attribute.CONF_MOD = "N"

        zoobenthos_Attribute.GPS_LAT = lat.toFloat()
        zoobenthos_Attribute.GPS_LON = log.toFloat()

        zoobenthos_Attribute.SPEC_NM = SPEC_NM
        zoobenthos_Attribute.FAMI_NM = FAMI_NM
        zoobenthos_Attribute.SCIEN_NM = SCIEN_NM
        zoobenthos_Attribute.GEOM = geom


        dbManager!!.insertzoobenthos(zoobenthos_Attribute);
    }

    fun null_zoo_attribute(): Zoobenthos_Attribute {
        var zoobenthos_Attribute: Zoobenthos_Attribute = Zoobenthos_Attribute(null, null, null, null, null, null, null, null, null, null, null, null, null
                , null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        return zoobenthos_Attribute
    }

    fun dataList_zoo(listdata: ArrayList<Zoobenthos_Attribute>, data: Cursor) {

        while (data.moveToNext()) {

            var model: Zoobenthos_Attribute;

            model = Zoobenthos_Attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getInt(7),
                    data.getInt(8), data.getFloat(9), data.getInt(10), data.getInt(11), data.getFloat(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getString(17), data.getString(18), data.getString(19), data.getInt(20), data.getString(21)
                    , data.getInt(22)
                    , data.getInt(23), data.getString(24), data.getString(25), data.getFloat(26), data.getFloat(27)
                    , data.getString(28), data.getFloat(29), data.getFloat(30), data.getFloat(31), data.getFloat(32)
                    , data.getFloat(33), data.getFloat(34), data.getFloat(35), data.getFloat(36), data.getString(37)
                    , data.getString(38), data.getString(39), data.getString(40)
                    , data.getString(41), data.getString(42), data.getFloat(43), data.getFloat(44)
                    , data.getString(45), data.getString(46), data.getString(47), data.getString(48), data.getString(49)
                    , data.getString(20), data.getInt(51), data.getString(52), data.getString(53))
            if (model.GROP_ID!=grop_id){
                listdata.add(model)
            }



        }
    }


    fun region(): String {
        var geocoder: Geocoder = Geocoder(context);
        var region_ = ""
        var list: List<Address> = geocoder.getFromLocation(lat.toDouble(), log.toDouble(), 1);

        if (list.size > 0) {
            region_ = list.get(0).getAddressLine(0)

        }
        return region_
    }

    fun convert(d: Double): String {

        var long_d = Math.abs(d)

//        var i = d.intValue()
        var i = long_d.toInt()
        var s = i.toString()

        GPSLAT_DEG_RE = s.toString()

        long_d = long_d - i;
        long_d = long_d * 60;
        GPSLAT_MIN_RE = long_d.toInt().toString()
//        i = long_d.intValue();
        i = long_d.toInt();

//        s = s + String.format(i) + '\'';
        s = s + i.toString()

        long_d = long_d - i;
        long_d = long_d * 60;

//        i = long_d.round().intValue();
        i = Math.round(long_d.toDouble()).toInt()

        GPSLAT_SEC_RE = long_d.toFloat().toString()

        s = s + i.toString() + '"';

        return s
    }

    fun logconvert(d: Double): String {

        var long_d = Math.abs(d)

//        var i = d.intValue()
        var i = long_d.toInt()
        var s = i.toString()

        GPSLON_DEG_RE = s.toString()

        long_d = long_d - i;
        long_d = long_d * 60;
        GPSLON_MIN_RE = long_d.toInt().toString()
//        i = long_d.intValue();
        i = long_d.toInt();

//        s = s + String.format(i) + '\'';
        s = s + i.toString()

        long_d = long_d - i;
        long_d = long_d * 60;

//        i = long_d.round().intValue();
        i = Math.round(long_d.toDouble()).toInt()

        GPSLON_SEC_RE = long_d.toFloat().toString()

        s = s + i.toString() + '"';

        return s
    }

}
