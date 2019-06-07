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
import hntecology.ecology.adapter.DlgPointModiAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Birds_attribute
import kotlinx.android.synthetic.main.activity_dlg_modi_list.*
import kotlinx.android.synthetic.main.activity_dlg_point_modi_list.*
import java.text.SimpleDateFormat
import java.util.*


class DlgPointModiListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1 : ArrayList<Birds_attribute>
    private lateinit var listdata2 : ArrayList<Birds_attribute>


    private lateinit var listdata3 : ArrayList<Birds_attribute>


    private lateinit var listAdapte1: DlgPointModiAdapter;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var DlgHeight:Float=430F
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

        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        grop_id  = intent.getStringExtra("GROP_ID")
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
        listdata1= ArrayList()

        if (table_name=="birdsAttribute"){

        }
        val dataList: Array<String> = arrayOf("$table_name.*", "min(id) as minId");
        val dataList2: Array<String> = arrayOf("*");
        val data1=  db.query(table_name,dataList,null,null,"GROP_ID",null,"minId asc",null);
        val data2=  db.query(table_name,dataList2,null,null,null,null,"id asc",null);


        dataList(listdata1,data1)
        dataList(listdata2,data2)
        listAdapte1 = DlgPointModiAdapter(context,listdata1, listdata2);

        listView1.adapter = listAdapte1

        var attribute = null_birds_attribute()
        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var birds_data =  listAdapte1.getItem(position)
            val dataList2: Array<String> = arrayOf("*");
            val data=  db.query(table_name,dataList2,"GROP_ID='${birds_data.GROP_ID}'",null,null,null,"id asc",null);
            while (data.moveToNext()){

                var model : Birds_attribute;

                model = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                        data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                        , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
                        , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
                        , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
                )

                listdata3.add(model)

            }
            dbManager!!.delete_grop_birds_attribute(attribute,grop_id)
            for (i in 0..listdata3.size - 1) {
                Log.d("버드데이터",listdata3[i].SPEC_NM)
                addbirds(attribute,listdata3[i].SPEC_NM.toString(),listdata3[i].FAMI_NM.toString(),listdata3[i].SCIEN_NM.toString(),listdata3[i].INV_REGION.toString())
            }
            var intent = Intent()
            Log.d("점스",geom)
            intent.putExtra("export", 70);
            intent.putExtra("geom", geom);
            setResult(RESULT_OK, intent);
            Toast.makeText(context,"복사되었습니다.",Toast.LENGTH_SHORT).show()
            finish()
        }

    }
    fun addbirds(attribute: Birds_attribute,spec:String,fami:String,scien:String,region:String) {
        Log.d("인서트비오톱33",geom.toString());
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
        attribute.SCIEN_NM =scien

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
        dbManager!!.insertbirds_attribute(attribute);
    }
    fun null_birds_attribute(): Birds_attribute {
        var birds_attribute: Birds_attribute = Birds_attribute(null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null)

        return birds_attribute
    }

    fun dataList(listdata:ArrayList<Birds_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Birds_attribute;

            model = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
                    , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
                    , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
            )

            listdata.add(model)

        }
    }




    fun ps_birds_attribute(data: Cursor): Birds_attribute {
        var birds_attribute: Birds_attribute = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                , data.getString(15), data.getString(16), data.getInt(17), data.getString(18), data.getString(19), data.getString(20)
                , data.getString(21), data.getString(22), data.getFloat(23), data.getFloat(24), data.getString(25), data.getString(26), data.getString(27)
                , data.getInt(28), data.getInt(29), data.getFloat(30), data.getInt(31), data.getInt(32), data.getFloat(33), data.getString(34), data.getString(35)
        )
        return birds_attribute
    }
}
