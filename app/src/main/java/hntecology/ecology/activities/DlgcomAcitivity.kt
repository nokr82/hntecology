package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgComAdapter
import hntecology.ecology.adapter.DlgComdiviAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.CommonDivision
import hntecology.ecology.model.common
import kotlinx.android.synthetic.main.activity_dlgcom_acitivity.*
import java.util.ArrayList

class DlgcomAcitivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : ArrayList<common>
    private lateinit var listdata2 : ArrayList<CommonDivision>

    private lateinit var listAdapte1: DlgComAdapter;
    private lateinit var listAdapte2: DlgComdiviAdapter;

    var dbManager: DataBaseHelper? = null
    private var db: SQLiteDatabase? = null

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlgcom_acitivity)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        com_dlgTitleTV.setText(titleName)

        val dataList:Array<String> = arrayOf("title","code","commons","area");

        val data1=  db!!.query(tableName,dataList,null,null,"code",null,null,null);

        listView1 = findViewById(R.id.com_list_view1)
        listView2 = findViewById(R.id.com_list_view2)

        listdata1 = ArrayList()
        listdata2 = ArrayList()

        listAdapte1 = DlgComAdapter(context, listdata1);
        listAdapte2 = DlgComdiviAdapter(context, listdata2);

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte2.clearItem()

            var veData =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)

            val datadiviList:Array<String> = arrayOf("code","title","division","area","round","upstreamdrea","rivername","rating");

            val data2 =  db!!.query("CommonDivision",datadiviList,"code like '%" + veData.code + "%'" ,null,null,null,null,null);

            com_dlg_probars.visibility= View.VISIBLE
            datadiviList(listdata2,data2);
            com_dlg_probars.visibility= View.GONE

        })

        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->


            var intent = Intent();
            intent.putExtra("CommonDivision",listAdapte2.getItem(position))

            for ( i in 0..(listAdapte1.count-1)){

                var common:common =listAdapte1.getItem(i);

                if(common.chkSelect){
                    intent.putExtra("common",listAdapte1.getItem(i))        ;
                }
            }

            setResult(RESULT_OK, intent);
            finish()

        })



        dataList(listdata1,data1);
    }

    fun dataList(listdata:ArrayList<common>, data: Cursor) {

        while (data.moveToNext()){

            var model : common;

            model = common(data.getString(0),data.getInt(1),data.getInt(2),data.getFloat(3),false);

            listdata.add(model)
        }
    }

    fun datadiviList(listdata:ArrayList<CommonDivision>, data: Cursor) {

        while (data.moveToNext()){

            var model : CommonDivision;

            model = CommonDivision(data.getInt(0),data.getString(1),data.getString(2),data.getFloat(3),data.getFloat(4),data.getFloat(5),data.getString(6),data.getString(7),false);

            listdata.add(model)
        }
    }
}
