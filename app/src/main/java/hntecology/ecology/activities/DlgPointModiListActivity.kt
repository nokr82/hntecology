package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient.log
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgModiAdapter
import hntecology.ecology.adapter.DlgPointModiAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_dlg_modi_list.*
import hntecology.ecology.model.Biotope_attribute
import hntecology.ecology.model.Birds_attribute
import kotlinx.android.synthetic.main.activity_biotope_ex.*
import java.text.SimpleDateFormat
import java.util.*


class DlgModiPointListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1 : ArrayList<Birds_attribute>
    private lateinit var listdata2 : ArrayList<Birds_attribute>



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
        setContentView(R.layout.activity_dlg_modi_list)

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

            println("폴리아이디 ---------$polygonid")
        }
        if (intent.getStringExtra("table_name") != null) {
            table_name = intent.getStringExtra("table_name")

            println("폴리아이디 ---------$table_name")
        }
        if (intent.getStringExtra("geom") != null) {
            geom = intent.getStringExtra("geom")
            println("---------점스스스 $geom")
        }

//        val dataList: Array<String> = arrayOf("biotopeAttribute.*", "min(id) as minId");
        val dataList: Array<String> = arrayOf("*");
//        val data1=  db.query("biotopeAttribute",dataList,null,null,null,null,"id asc",null);
//        val data2=  db.query("biotopeAttribute",dataList,null,null,"GROP_ID",null,"id asc",null);
        val data1=  db.query(table_name,dataList,null,null,"GROP_ID",null,"id asc",null);
        val data2=  db.query(table_name,dataList,null,null,null,null,"id asc",null);


        listView1 = findViewById(R.id.modiLV)

        listdata2 = ArrayList()
        listdata1= ArrayList()

        dataList(listdata1,data1)
        dataList(listdata2,data2)
        listAdapte1 = DlgPointModiAdapter(context,listdata1, listdata2);

        listView1.adapter = listAdapte1

        var attribute = null_birds_attribute()

    /*    listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var data =  listAdapte1.getItem(position)
            Log.d("인서트비오톱22",data.toString())
            var domin = data.DOMIN
            var tre_spec = data.TRE_SPEC
            var stre_spec = data.STRE_SPEC
            var shr_spec = data.SHR_SPEC
            var her_spec = data.HER_SPEC
            var tre_fami = data.TRE_FAMI
            var tre_scien = data.TRE_SCIEN
            var stre_fami = data.STRE_FAMI
            var stre_scien = data.STRE_SCIEN
            var shr_fami = data.SHR_FAMI
            var shr_scien = data.SHR_SCIEN
            var her_fami = data.HER_FAMI
            var her_scien = data.HER_SCIEN
            var region = data.INV_REGION
            Log.d("인서트비오톱22",her_fami.toString())
            Log.d("인서트비오톱22",her_spec.toString())
            addbiotope(attribute,domin.toString(),region.toString(),geom,tre_spec.toString(),stre_spec.toString(),shr_spec.toString(),her_spec.toString(),tre_fami.toString()
                    ,tre_scien.toString(),stre_fami.toString(),stre_scien.toString(),shr_fami.toString(),shr_scien.toString(),her_fami.toString(),her_scien.toString())

        }*/

    }
    fun addbiotope(attribute: Birds_attribute,domin:String,region:String,geom:String,tre_spec:String,stre_spec:String,shr_spec:String
                   ,her_spec:String,tre_fami:String,tre_scien:String,stre_fami:String,stre_scien:String,shr_fami:String
                   ,shr_scien:String,her_fami:String,her_scien:String) {
        Log.d("인서트비오톱33","추가");
        attribute.GROP_ID = grop_id
        attribute.INV_REGION = region
        attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
        attribute.INV_DT = Utils.todayStr()
        attribute.INV_TM = Utils.timeStr()
        attribute.TEMP_YN = "Y"
        attribute.CONF_MOD = "M"
        attribute.GEOM = geom

        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        attribute.CURRENT_TM = getTime.substring(2, 14)
        attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        dbManager!!.deletegrop_biotope(grop_id)
        dbManager!!.insertbirds_attribute(attribute);
        var intent = Intent()
        intent.putExtra("export", 70);
        intent.putExtra("geom", geom);
        setResult(RESULT_OK, intent);
        Toast.makeText(context,"복사되었습니다.",Toast.LENGTH_SHORT).show()
        finish()
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
