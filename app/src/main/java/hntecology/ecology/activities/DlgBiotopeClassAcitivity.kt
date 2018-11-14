package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.BiotopeClassAdapter
import hntecology.ecology.adapter.BiotopeClassAdapter2
import hntecology.ecology.adapter.BiotopeClassAdapter3
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeClass
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlg_biotope_class_acitivity.*

class DlgBiotopeClassAcitivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView
    private lateinit var listView3: ListView

    private lateinit var listdata1 : ArrayList<BiotopeClass>
    private lateinit var listdata2 : ArrayList<BiotopeClass>
    private lateinit var listdata3 : ArrayList<Vegetation>

    private lateinit var listAdapte1: BiotopeClassAdapter;
    private lateinit var listAdapte2: BiotopeClassAdapter2;
    private lateinit var listAdapte3: BiotopeClassAdapter3;

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_biotope_class_acitivity)

        context = applicationContext;

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        class_dlgTitle.setText(titleName)

        val dataList:Array<String> = arrayOf("MIDDLECATEGORY","SMALLCATEGORY","SIGN");

        val data1=  db.query(tableName,dataList,null,null,"MIDDLECATEGORY",null,null,null);

        listView1 = findViewById(R.id.class_list_view1)
        listView2 = findViewById(R.id.class_list_view2)
        listView3 = findViewById(R.id.class_list_view3)

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()

        listAdapte1 = BiotopeClassAdapter(context, listdata1);
        listAdapte2 = BiotopeClassAdapter2(context, listdata2);
        listAdapte3 = BiotopeClassAdapter3(context, listdata3);


        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3

        dataList(listdata1,data1);



        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte2.clearItem()

            var veData =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)


            val data2 =  db.query(tableName,dataList,"middlecategory='"+veData.middlecategory +"'" ,null,null,null,null,null);

            class_probar.visibility= View.VISIBLE
            dataList(listdata2,data2);
            class_probar.visibility= View.GONE

        })

        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->


            var intent = Intent();
            intent.putExtra("classData",listAdapte2.getItem(position))

            for ( i in 0..(listAdapte1.count-1)){

                var biotopeClass:BiotopeClass =listAdapte1.getItem(i);

                if(biotopeClass.chkSelect){
                    intent.putExtra("biotopeClass",listAdapte1.getItem(i))        ;
                }
            }

            setResult(RESULT_OK, intent);
            finish()

        })


    }

    fun dataList(listdata: java.util.ArrayList<BiotopeClass>, data: Cursor) {

        while (data.moveToNext()){

            var model : BiotopeClass;

            model = BiotopeClass(data.getString(0),data.getString(1),data.getString(2),false);

            listdata.add(model)
        }
    }

    fun data3List(listdata:ArrayList<Vegetation>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vegetation;

            model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

            listdata.add(model)
        }
    }
}
