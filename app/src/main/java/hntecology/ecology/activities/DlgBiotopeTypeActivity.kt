package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgBiotopeTypeAdapter1
import hntecology.ecology.adapter.DlgBiotopeTypeAdapter2
import hntecology.ecology.adapter.DlgBiotopeTypeAdapter3
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeType
import kotlinx.android.synthetic.main.activity_dlg_biotope_type.*

class DlgBiotopeTypeActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView
    private lateinit var listView3: ListView

    private lateinit var listdata1 : ArrayList<BiotopeType>
    private lateinit var listdata2 : ArrayList<BiotopeType>
    private lateinit var listdata3 : ArrayList<BiotopeType>

    private lateinit var listAdapte1: DlgBiotopeTypeAdapter1;
    private lateinit var listAdapte2: DlgBiotopeTypeAdapter2;
    private lateinit var listAdapte3: DlgBiotopeTypeAdapter3;

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_biotope_type)

        context = applicationContext;

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        type_dlgTitle.setText(titleName)

        val dataList:Array<String> = arrayOf("DIVISION","MAINCATEGORY","CONTENT");

        val data1=  db.query(tableName,dataList,null,null,"DIVISION",null,null,null);

        listView1 = findViewById(R.id.type_list_view1)
        listView2 = findViewById(R.id.type_list_view2)
        listView3 = findViewById(R.id.type_list_view3)

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()

        listAdapte1 = DlgBiotopeTypeAdapter1(context, listdata1);
        listAdapte2 = DlgBiotopeTypeAdapter2(context, listdata2);
        listAdapte3 = DlgBiotopeTypeAdapter3(context, listdata3);

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3

        dataList(listdata1,data1)

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, position, l ->
            listAdapte2.clearItem()

            var veData =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)

            val data2 =  db.query(tableName,dataList,"DIVISION='"+veData.DIVISION +"'" ,null,"MAINCATEGORY",null,null,null);

            type_probar.visibility= View.VISIBLE
            dataList(listdata2,data2);
            type_probar.visibility= View.GONE

            listAdapte2.notifyDataSetChanged()

        })

        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte3.clearItem()

            var veData =  listAdapte2.getItem(position)

            listAdapte2.setItemSelect(position)

            val data3 =  db.query(tableName,dataList,"MAINCATEGORY='"+veData.MAINCATEGORY +"'" ,null,null,null,null,null);

            type_probar.visibility= View.VISIBLE
            dataList(listdata3,data3);
            type_probar.visibility= View.GONE

            listAdapte3.notifyDataSetChanged()
        })

        listView3.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            val biotopeType : BiotopeType = listAdapte3.getItem(position)

                intent.putExtra("biotopeType",biotopeType)

                setResult(RESULT_OK, intent);
                finish()

        })




    }


    fun dataList(listdata: java.util.ArrayList<BiotopeType>, data: Cursor) {

        while (data.moveToNext()){

            var model : BiotopeType;

            model = BiotopeType(data.getString(0),data.getString(1),data.getString(2),false);

            listdata.add(model)
        }
    }
}
