package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
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


    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var DlgHeight: Float = 430F
    var dbManager: DataBaseHelper? = null
    var chkData = false
    var polygonid = ""
    var grop_id = ""
    var table_name = ""
    var geom = ""
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
            println("---------점스스스 $geom")
        }






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
                while (data.moveToNext()) {

                    var model: Birds_attribute;

                    model = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                            , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
                            , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
                            , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
                    )

                    listdata3.add(model)

                }
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
                while (data.moveToNext()) {

                    var model: Reptilia_attribute;

                    model = Reptilia_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                            , data.getString(15), data.getString(16), data.getInt(17), data.getInt(18), data.getInt(19), data.getString(20), data.getString(21), data.getString(22)
                            , data.getString(23), data.getString(24), data.getString(25), data.getInt(26), data.getInt(27), data.getInt(28), data.getFloat(29), data.getFloat(30), data.getString(31), data.getString(32), data.getString(33)
                            , data.getInt(34), data.getInt(35), data.getFloat(36), data.getInt(37), data.getInt(38), data.getFloat(39)
                            , data.getFloat(40), data.getString(41), data.getString(42)
                    )

                    listdata_rep3.add(model)

                }
                dbManager!!.delete_grop_reptilia_attribute(attribute, grop_id)
                for (i in 0..listdata_rep3.size - 1) {
                    Log.d("버드데이터", listdata_rep3[i].SPEC_NM)
                    addrepter(attribute, listdata_rep3[i].SPEC_NM.toString()
                            , listdata_rep3[i].FAMI_NM.toString(), listdata_rep3[i].SCIEN_NM.toString()
                            , listdata_rep3[i].INV_REGION.toString())
                }
                last_finish()
            }
        }
        else if (table_name == "mammalAttribute") {
            dataList_mal(listdata_mal1, data1)
            dataList_mal(listdata_mal2, data2)
            listmamalAdapter = DlgPointModiMamalAdapter(context, listdata_mal1, listdata_mal2);

            listView1.adapter = listmamalAdapter

            var attribute = null_mal_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listmamalAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                while (data.moveToNext()) {

                    var model: Mammal_attribute;

                    model = Mammal_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                            data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                            , data.getString(15), data.getString(16), data.getString(17), data.getInt(18), data.getString(19), data.getString(20), data.getString(21)
                            , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26), data.getString(27), data.getString(28), data.getString(29), data.getString(30)
                            , data.getInt(31), data.getInt(32), data.getFloat(33), data.getInt(34), data.getInt(35), data.getFloat(36), data.getString(37), data.getString(38), data.getString(39))


                    listdata_mal3.add(model)

                }
                dbManager!!.delete_grop_mamal_attribute(attribute, grop_id)
                for (i in 0..listdata_mal3.size - 1) {
                    Log.d("버드데이터", listdata_mal3[i].SPEC_NM)
                    addmamal(attribute, listdata_mal3[i].SPEC_NM.toString()
                            , listdata_mal3[i].FAMI_NM.toString(), listdata_mal3[i].SCIEN_NM.toString()
                            , listdata_mal3[i].INV_REGION.toString())
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
                while (data.moveToNext()) {

                    var model: Fish_attribute;

                    model = Fish_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                            data.getString(8), data.getString(9), data.getFloat(10), data.getString(11), data.getString(12), data.getString(13), data.getString(14), data.getString(15),
                            data.getFloat(16), data.getFloat(17), data.getString(18), data.getString(19), data.getInt(20), data.getInt(21), data.getInt(22), data.getInt(23), data.getString(24), data.getString(25),
                            data.getInt(26), data.getInt(27), data.getInt(28), data.getInt(29), data.getInt(30), data.getString(31), data.getInt(32), data.getString(33), data.getString(34), data.getString(35),
                            data.getInt(36), data.getString(37), data.getString(38), data.getString(39), data.getString(40), data.getString(41), data.getString(42), data.getInt(43), data.getInt(44), data.getFloat(45)
                            , data.getInt(46), data.getInt(47), data.getFloat(48), data.getString(49), data.getString(50), data.getString(51), data.getString(52))

                    listdata_fish3.add(model)

                }
                dbManager!!.delete_grop_fish_attribute(attribute, grop_id)
                for (i in 0..listdata_fish3.size - 1) {
                    Log.d("버드데이터", listdata_fish3[i].SPEC_NM)
                    addfish(attribute, listdata_fish3[i].SPEC_NM.toString()
                            , listdata_fish3[i].FAMI_NM.toString(), listdata_fish3[i].SCIEN_NM.toString()
                            , listdata_fish3[i].INV_REGION.toString())
                }
                last_finish()
            }
        }
        else if (table_name == "insectAttribute") {
            dataList_insect(listdata_insect1, data1)
            dataList_insect(listdata_insect2, data2)
            listinsectAdapter = DlgPointModiInsectAdapter(context, listdata_insect1, listdata_insect2);

            listView1.adapter = listinsectAdapter

            var attribute = null_insect_attribute()
            listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                var all_data = listinsectAdapter.getItem(position)
                val data = db.query(table_name, dataList2, "GROP_ID='${all_data.GROP_ID}'", null, null, null, "id asc", null);
                while (data.moveToNext()) {

                    var model: Insect_attribute;

                    model = Insect_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4)
                            , data.getString(5), data.getString(6), data.getString(7), data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11)
                            , data.getString(12), data.getString(13), data.getString(14), data.getString(15), data.getInt(16), data.getString(17)
                            , data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                            , data.getFloat(22), data.getFloat(23), data.getString(24), data.getString(25), data.getString(26)
                            , data.getInt(27), data.getInt(28), data.getFloat(29), data.getInt(30), data.getInt(31), data.getFloat(32)
                            , data.getString(33), data.getString(34))

                    listdata_insect3.add(model)

                }
                dbManager!!.delete_grop_insect_attribute(attribute, grop_id)
                for (i in 0..listdata_insect3.size - 1) {
                    Log.d("버드데이터", listdata_insect3[i].SPEC_NM)
                    addinsect(attribute, listdata_insect3[i].SPEC_NM.toString()
                            , listdata_insect3[i].FAMI_NM.toString(), listdata_insect3[i].SCIEN_NM.toString()
                            , listdata_insect3[i].INV_REGION.toString())
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
        attribute.INV_REGION = region
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

        /*attribute.WEATHER = spec
        attribute.WIND = fami
        attribute.WIND_DIRE = scien
        attribute.TEMPERATUR = spec
        attribute.ETC = fami
        attribute.NUM = scien
        attribute.ENDANGERED = spec
        attribute.INDI_CNT = fami
        attribute.OBS_STAT = scien
        attribute.USE_TAR = spec
        attribute.USE_LAYER = fami
        attribute.MJ_ACT = scien
        attribute.MJ_ACT_PR = scien
        attribute.GPS_LAT = spec
        attribute.GPS_LON = fami
        attribute.GPSLAT_DEG = scien
        attribute.GPSLAT_MIN = scien
        attribute.GPSLAT_SEC = spec
        attribute.GPSLON_DEG = fami
        attribute.GPSLON_MIN = scien
        attribute.GPSLON_SEC = scien*/

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

            listdata.add(model)

        }
    }

    fun addrepter(attribute: Reptilia_attribute, spec: String, fami: String, scien: String, region: String) {
        Log.d("인서트비오톱33", geom.toString());
        attribute.GROP_ID = grop_id
        attribute.INV_REGION = region
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

        /*  attribute.WEATHER = spec
          attribute.WIND = fami
          attribute.WIND_DIRE =scien
          attribute.TEMPERATUR = spec
          attribute.ETC = fami
          attribute.NUM =scien
          attribute.ENDANGERED = spec
          attribute.INDI_CNT = fami
          attribute.OBS_STAT =scien
          attribute.USE_TAR = spec
          attribute.USE_LAYER = fami
          attribute.MJ_ACT =scien
          attribute.MJ_ACT_PR =scien
          attribute.GPS_LAT = spec
          attribute.GPS_LON = fami
          attribute.GPSLAT_DEG =scien
          attribute.GPSLAT_MIN =scien
          attribute.GPSLAT_SEC = spec
          attribute.GPSLON_DEG = fami
          attribute.GPSLON_MIN =scien
          attribute.GPSLON_SEC =scien*/

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        attribute.CURRENT_TM = getTime.substring(2, 14)
        attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertreptilia_attribute(attribute);
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

            listdata.add(model)

        }
    }

    fun addmamal(attribute: Mammal_attribute, spec: String, fami: String, scien: String, region: String) {
        Log.d("인서트비오톱33", geom.toString());
        attribute.GROP_ID = grop_id
        attribute.INV_REGION = region
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

        /*  attribute.WEATHER = spec
          attribute.WIND = fami
          attribute.WIND_DIRE =scien
          attribute.TEMPERATUR = spec
          attribute.ETC = fami
          attribute.NUM =scien
          attribute.ENDANGERED = spec
          attribute.INDI_CNT = fami
          attribute.OBS_STAT =scien
          attribute.USE_TAR = spec
          attribute.USE_LAYER = fami
          attribute.MJ_ACT =scien
          attribute.MJ_ACT_PR =scien
          attribute.GPS_LAT = spec
          attribute.GPS_LON = fami
          attribute.GPSLAT_DEG =scien
          attribute.GPSLAT_MIN =scien
          attribute.GPSLAT_SEC = spec
          attribute.GPSLON_DEG = fami
          attribute.GPSLON_MIN =scien
          attribute.GPSLON_SEC =scien*/

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        attribute.CURRENT_TM = getTime.substring(2, 14)
        attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertmammal_attribute(attribute);
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



            listdata.add(model)

        }
    }

    fun addfish(attribute: Fish_attribute, spec: String, fami: String, scien: String, region: String) {
        Log.d("인서트비오톱33", geom.toString());
        attribute.GROP_ID = grop_id
        attribute.INV_REGION = region
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

        /*  attribute.WEATHER = spec
          attribute.WIND = fami
          attribute.WIND_DIRE =scien
          attribute.TEMPERATUR = spec
          attribute.ETC = fami
          attribute.NUM =scien
          attribute.ENDANGERED = spec
          attribute.INDI_CNT = fami
          attribute.OBS_STAT =scien
          attribute.USE_TAR = spec
          attribute.USE_LAYER = fami
          attribute.MJ_ACT =scien
          attribute.MJ_ACT_PR =scien
          attribute.GPS_LAT = spec
          attribute.GPS_LON = fami
          attribute.GPSLAT_DEG =scien
          attribute.GPSLAT_MIN =scien
          attribute.GPSLAT_SEC = spec
          attribute.GPSLON_DEG = fami
          attribute.GPSLON_MIN =scien
          attribute.GPSLON_SEC =scien*/

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        attribute.CURRENT_TM = getTime.substring(2, 14)
        attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertfish_attribute(attribute);
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

            listdata.add(model)

        }
    }

    fun addinsect(attribute: Insect_attribute, spec: String, fami: String, scien: String, region: String) {
        Log.d("인서트비오톱33", geom.toString());
        attribute.GROP_ID = grop_id
        attribute.INV_REGION = region
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

        /*  attribute.WEATHER = spec
          attribute.WIND = fami
          attribute.WIND_DIRE =scien
          attribute.TEMPERATUR = spec
          attribute.ETC = fami
          attribute.NUM =scien
          attribute.ENDANGERED = spec
          attribute.INDI_CNT = fami
          attribute.OBS_STAT =scien
          attribute.USE_TAR = spec
          attribute.USE_LAYER = fami
          attribute.MJ_ACT =scien
          attribute.MJ_ACT_PR =scien
          attribute.GPS_LAT = spec
          attribute.GPS_LON = fami
          attribute.GPSLAT_DEG =scien
          attribute.GPSLAT_MIN =scien
          attribute.GPSLAT_SEC = spec
          attribute.GPSLON_DEG = fami
          attribute.GPSLON_MIN =scien
          attribute.GPSLON_SEC =scien*/

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        attribute.CURRENT_TM = getTime.substring(2, 14)
        attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.insertinsect_attribute(attribute);
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
            listdata.add(model)

        }
    }


}
