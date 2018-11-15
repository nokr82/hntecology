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
import hntecology.ecology.R
import hntecology.ecology.adapter.DataBirdsAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Birds_attribute
import kotlinx.android.synthetic.main.activity_dlg_data_list.*

class DlgDataListActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var birdsData : ArrayList<Birds_attribute>

    private lateinit var birdsAadapter: DataBirdsAdapter;

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F
    var GROP_ID:String = ""

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

        birdsData = ArrayList()

        birdsAadapter = DataBirdsAdapter(context,birdsData)

        println("tableName ====== $tableName")

        if(tableName.equals("birdsAttribute")){

            val dataList:Array<String> = arrayOf("id","GROP_ID","PRJ_NAME","INV_REGION","INV_DT","INV_PERSON","WEATHER","WIND","INV_DT","WIND_DIRE","TEMPERATUR","ETC","NUM","INV_TM"
            ,"SPEC_NM" ,"FAMI_NM" ,"SCIEN_NM" ,"INDI_CNT" ,"OBS_STAT" ,"OBS_ST_ETC" ,"USE_TAR" ,"USE_TAR_SP" ,"USE_LAYER" ,"MJ_ACT" ,"MJ_ACT_PR" ,"GPS_LAT" ,"GPS_LON" ,"TEMP_YN");

            val birdsdata=  db.query(tableName,dataList,"GROP_ID='"+ GROP_ID +"'",null,null,null,null,null);

            birdsdataList(birdsData,birdsdata)

            listView1.adapter = birdsAadapter

        }

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            if(tableName.equals("birdsAttribute")){

                val birdsdata = birdsAadapter.getItem(position)

                val intent = Intent(this, BirdsActivity::class.java)

                intent!!.putExtra("id", birdsdata.id.toString())
                intent!!.putExtra("GROP_ID",birdsdata.GROP_ID)

                startActivityForResult(intent, BIRDS)

                finish()
            }

        })

        closeLL.setOnClickListener {
            finish()
        }

    }

    fun birdsdataList(listdata: java.util.ArrayList<Birds_attribute>, data: Cursor) {

        while (data.moveToNext()){

            var model : Birds_attribute;

            model = Birds_attribute(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7),
                    data.getString(8), data.getFloat(9), data.getString(10), data.getInt(11), data.getString(12), data.getString(13), data.getString(14)
                    , data.getString(15), data.getInt(16), data.getString(17), data.getString(18), data.getString(19), data.getString(20), data.getString(21)
                    , data.getString(22), data.getString(23), data.getFloat(24), data.getFloat(25), data.getString(26))

            listdata.add(model)
        }
    }
}
