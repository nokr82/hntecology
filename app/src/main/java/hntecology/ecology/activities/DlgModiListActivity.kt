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
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_dlg_modi_list.*
import hntecology.ecology.model.Biotope_attribute
import kotlinx.android.synthetic.main.activity_biotope_ex.*
import java.text.SimpleDateFormat
import java.util.*


class DlgModiListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1 : ArrayList<Biotope_attribute>
    private lateinit var listdata2 : ArrayList<Biotope_attribute>
    private lateinit var listAdapte1: DlgModiAdapter;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var DlgHeight:Float=430F
    var dbManager: DataBaseHelper? = null
    var chkData = false
    var polygonid = ""
    var it_index  = -1
    var grop_id = ""
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
        if (intent.getStringExtra("geom") != null) {
            geom = intent.getStringExtra("geom")
            println("---------점스스스 $geom")
        }

        val dataList: Array<String> = arrayOf("biotopeAttribute.*", "min(id) as minId");

//        val data1=  db.query("biotopeAttribute",dataList,null,null,"IT_GROP_ID",null,"id asc",null);
        val data2=  db.query("biotopeAttribute",dataList,null,null,"GROP_ID",null,"minId asc",null);


        listView1 = findViewById(R.id.modiLV)

        listdata2 = ArrayList()
//        listdata1= ArrayList()
        listAdapte1 = DlgModiAdapter(context, listdata2);

        listView1.adapter = listAdapte1

//        dataList(listdata1,data1);
        dataList(listdata2,data2);
        var biotope_attribute = null_biotope_attribute()

        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
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
            Log.d("인서트비오톱22",her_fami.toString())
            Log.d("인서트비오톱22",her_spec.toString())
            addbiotope(biotope_attribute,domin.toString(),geom,tre_spec.toString(),stre_spec.toString(),shr_spec.toString(),her_spec.toString(),tre_fami.toString()
                    ,tre_scien.toString(),stre_fami.toString(),stre_scien.toString(),shr_fami.toString(),shr_scien.toString(),her_fami.toString(),her_scien.toString())

        }

    }
    fun addbiotope(biotope_attribute: Biotope_attribute,domin:String,geom:String,tre_spec:String,stre_spec:String,shr_spec:String
                   ,her_spec:String,tre_fami:String,tre_scien:String,stre_fami:String,stre_scien:String,shr_fami:String
                   ,shr_scien:String,her_fami:String,her_scien:String) {
        Log.d("인서트비오톱33","추가");
        biotope_attribute.GROP_ID = grop_id
        biotope_attribute.PRJ_NAME = PrefUtils.getStringPreference(context, "prjname")
        biotope_attribute.INV_PERSON = PrefUtils.getStringPreference(this, "name");
        biotope_attribute.INV_DT = Utils.todayStr()
        biotope_attribute.INV_TM = Utils.timeStr()
        biotope_attribute.TEMP_YN = "Y"
        biotope_attribute.CONF_MOD = "N"
        biotope_attribute.GEOM = geom

        biotope_attribute.STRE_SPEC = stre_spec
        biotope_attribute.STRE_FAMI = stre_fami
        biotope_attribute.STRE_SCIEN = stre_scien
        biotope_attribute.TRE_SPEC = tre_spec
        biotope_attribute.TRE_FAMI = tre_fami
        biotope_attribute.TRE_SCIEN = tre_scien
        biotope_attribute.SHR_SPEC = shr_spec
        biotope_attribute.SHR_FAMI = shr_fami
        biotope_attribute.SHR_SCIEN = shr_scien
        biotope_attribute.HER_SPEC = her_spec
        biotope_attribute.HER_FAMI = her_fami
        biotope_attribute.HER_SCIEN = her_scien
        biotope_attribute.MAC_ADDR = PrefUtils.getStringPreference(context, "mac_addr")
        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMddHHmmSS")
        val getTime = sdf.format(date)
        biotope_attribute.CURRENT_TM = getTime.substring(2, 14)
        biotope_attribute.DOMIN = domin
        dbManager!!.deletegrop_biotope(grop_id)
        dbManager!!.insertbiotope_attribute(biotope_attribute);
        var intent = Intent()
        intent.putExtra("export", 70);
        setResult(RESULT_OK, intent);
        Toast.makeText(context,"복사되었습니다.",Toast.LENGTH_SHORT).show()
        finish()
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
                , null, null, null, null, null, null, null, null
        )
        return biotope_attribute
    }

    fun dataList(listdata:ArrayList<Biotope_attribute>, data2: Cursor) {

        while (data2.moveToNext()){

            var model : Biotope_attribute;

            model = Biotope_attribute(data2.getString(0), data2.getString(1), data2.getString(2), data2.getString(3), data2.getString(4), data2.getString(5), data2.getString(6), data2.getInt(7),
                    data2.getString(8), data2.getFloat(9), data2.getFloat(10), data2.getString(11), data2.getString(12), data2.getString(13), data2.getFloat(14)
                    , data2.getString(15), data2.getString(16), data2.getString(17), data2.getString(18), data2.getString(19), data2.getString(20), data2.getString(21)
                    , data2.getString(22), data2.getString(23), data2.getString(24), data2.getString(25), data2.getFloat(26), data2.getFloat(27), data2.getFloat(28)
                    , data2.getString(29), data2.getString(30), data2.getString(31), data2.getFloat(32), data2.getFloat(33), data2.getFloat(34), data2.getString(35)
                    , data2.getString(36), data2.getString(37), data2.getFloat(38), data2.getFloat(39), data2.getString(40), data2.getString(41), data2.getString(42)
                    , data2.getFloat(43), data2.getFloat(44), data2.getString(45), data2.getString(46), data2.getString(47), data2.getString(48), data2.getDouble(49)
                    , data2.getDouble(50), data2.getString(51), data2.getString(52), data2.getString(53), data2.getString(54), data2.getString(55), data2.getString(56), data2.getString(57)
                    , data2.getFloat(58), data2.getFloat(59), data2.getFloat(60), data2.getFloat(61), data2.getFloat(62), data2.getFloat(63)
                    , data2.getFloat(64), data2.getFloat(65), data2.getFloat(66), data2.getFloat(67), data2.getFloat(68), data2.getFloat(69), data2.getString(70), data2.getFloat(71)
                    , data2.getString(72), data2.getString(73), data2.getString(74), data2.getString(75), data2.getInt(76), data2.getInt(77), data2.getInt(78), data2.getInt(79)
            )

            listdata.add(model)

        }
    }


}
