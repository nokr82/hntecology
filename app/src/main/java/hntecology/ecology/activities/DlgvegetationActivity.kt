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
import hntecology.ecology.adapter.AreaItem1Adapte
import hntecology.ecology.adapter.DlgVegeAdapter
import hntecology.ecology.adapter.DlgVegeAdapter2
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeModel
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlgcommon.*
import kotlinx.android.synthetic.main.activity_dlgvegetation.*
import java.util.ArrayList

class DlgvegetationActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : ArrayList<Vegetation>
    private lateinit var listdata2 : ArrayList<Vegetation>

    private lateinit var listAdapte1: DlgVegeAdapter;
    private lateinit var listAdapte2: DlgVegeAdapter2;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlgvegetation)

        context = applicationContext;

        val dbManager: DataBaseHelper = DataBaseHelper(this)

        val db = dbManager.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        dlgTitleTV.setText(titleName)

        val dataList:Array<String> = arrayOf("categorycode","category","classcode","sign","correspondingname");

        val data1=  db.query(tableName,dataList,null,null,"CATEGORY",null,null,null);

        listView1 = findViewById(R.id.list_view1)
        listView2 = findViewById(R.id.list_view2)

        listdata1 = ArrayList()
        listdata2 = ArrayList()

        listAdapte1 = DlgVegeAdapter(context, listdata1);
        listAdapte2 = DlgVegeAdapter2(context, listdata2);

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2

        dataList(listdata1,data1);

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte2.clearItem()

            var veData =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)


            val data2 =  db.query(tableName,dataList,"CATEGORYCODE='"+veData.CATEGORYCODE +"'" ,null,null,null,null,null);

            dlg_probars.visibility= View.VISIBLE
            dataList(listdata2,data2);
            dlg_probars.visibility= View.GONE

        })

        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->


            var intent = Intent();
            intent.putExtra("veData",listAdapte2.getItem(position))

            for ( i in 0..(listAdapte1.count-1)){

                var vegetation:Vegetation =listAdapte1.getItem(i);

                if(vegetation.chkSelect){
                    intent.putExtra("vegModelParent",listAdapte1.getItem(i))        ;
                }
            }

            setResult(RESULT_OK, intent);
            finish()

        })

    }

    fun dataList(listdata:ArrayList<Vegetation>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vegetation;

            model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

            listdata.add(model)
        }
    }
}
