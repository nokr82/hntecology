package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.BridsClassAdapter
import hntecology.ecology.adapter.BridsClassAdapter2
import hntecology.ecology.adapter.BridsClassAdapter3
import hntecology.ecology.adapter.BridsClassAdapter4
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Region
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlg_brids_class.*

class DlgBridsClassActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView
    private lateinit var listView3: ListView
    private lateinit var listView4: ListView

    private lateinit var listdata1 : ArrayList<Region>
    private lateinit var listdata2 : ArrayList<Region>
    private lateinit var listdata3 : ArrayList<Region>
    private lateinit var vegelistdate2 : ArrayList<Vegetation>

    private lateinit var copyvegelistdate2 : ArrayList<Vegetation>

    private lateinit var listAdapte1: BridsClassAdapter;
    private lateinit var listAdapte2: BridsClassAdapter2;
    private lateinit var listAdapte3: BridsClassAdapter3;
    private lateinit var vegelistAdapte2 : BridsClassAdapter4

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_brids_class)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        brids_dlgTitle.setText(titleName)

        val dataList:Array<String> = arrayOf("MAINCATEGORY","MIDDLECATEGORY","SMALLCATEGORY");

        val data1=  db!!.query(tableName,dataList,null,null,"MAINCATEGORY",null,null,null);

        listView1 = findViewById(R.id.brids_list_view1)
        listView2 = findViewById(R.id.brids_list_view2)
        listView3 = findViewById(R.id.brids_list_view3)
        listView4 = findViewById(R.id.brids_list_view4)

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()
        vegelistdate2 = ArrayList()
        copyvegelistdate2 = ArrayList()

        listAdapte1 = BridsClassAdapter(context, listdata1);
        listAdapte2 = BridsClassAdapter2(context, listdata2);
        listAdapte3 = BridsClassAdapter3(context, listdata3);
        vegelistAdapte2 = BridsClassAdapter4(context,vegelistdate2)

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3
        listView4.adapter = vegelistAdapte2

        dataList(listdata1,data1)

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte2.clearItem()
            vegelistAdapte2.clearItem()

            var veData =  listAdapte1.getItem(position)

            if(veData.MAINCATEGORY.equals("산림 지역 (R 42, G 75, B 45)")){
                brids_list_view2.visibility = View.GONE
                linearLL.visibility = View.VISIBLE

                val dataList:Array<String> = arrayOf("categorycode","category","classcode","sign","correspondingname");

                val vegedata=  db!!.query("Vegetation",dataList,null,null,null,null,"SIGN",null);

                listAdapte1.setItemSelect(position)

                class_probar.visibility= View.VISIBLE
                vegeList(vegelistdate2,vegedata)
                class_probar.visibility= View.GONE

                copyvegelistdate2.addAll(vegelistdate2)

                vegelistAdapte2.notifyDataSetChanged()

            }

            else {
                brids_list_view2.visibility = View.VISIBLE
                linearLL.visibility = View.GONE

                listAdapte1.setItemSelect(position)

                val data2 =  db!!.query(tableName,dataList,"MAINCATEGORY='"+veData.MAINCATEGORY +"'" ,null,"MIDDLECATEGORY",null,null,null);

                class_probar.visibility= View.VISIBLE
                dataList(listdata2,data2);
                class_probar.visibility= View.GONE

                listAdapte2.notifyDataSetChanged()
            }

        })

        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte3.clearItem()

            var veData =  listAdapte2.getItem(position)

            listAdapte2.setItemSelect(position)

            val data2 =  db!!.query(tableName,dataList,"MIDDLECATEGORY='"+veData.MIDDLECATEGORY +"'" ,null,null,null,null,null);

            class_probar.visibility= View.VISIBLE
            dataList(listdata3,data2);
            class_probar.visibility= View.GONE

            listAdapte3.notifyDataSetChanged()

        })


        listView3.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            val region : Region = listAdapte3.getItem(position)

            if(region.SMALLCATEGORY.equals("기타 교통·통신시설") || region.SMALLCATEGORY.equals("기타 공공시설") || region.SMALLCATEGORY.equals("기타재배지")
            || region.SMALLCATEGORY.equals("기타초지") || region.SMALLCATEGORY.equals("기타나지")){
                var intent = Intent();

                intent.putExtra("Other",1000)

                setResult(RESULT_OK, intent);
                finish()

            }else {
                var intent = Intent();

                intent.putExtra("Region",region)

                setResult(RESULT_OK, intent);
                finish()
            }


        })

        listView4.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val vegetation:Vegetation = vegelistAdapte2.getItem(position)

            var intent = Intent();

            intent.putExtra("Vegetation",vegetation)

            setResult(RESULT_OK, intent);
            finish()
        })

        searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                val text = searchET.text.toString()
                search(text)
            }
        });

    }

    fun dataList(listdata: java.util.ArrayList<Region>, data: Cursor) {

        while (data.moveToNext()){

            var model : Region;

            model = Region(data.getString(0),data.getString(1),data.getString(2),false);

            listdata.add(model)
        }
    }

    fun vegeList(listdata:ArrayList<Vegetation>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vegetation;

            model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

            listdata.add(model)
        }
    }

    fun search(charText: String){
        for(i in 0.. vegelistdate2.size-1){
            vegelistdate2.get(i).chkSelect = false
        }

        vegelistdate2.clear()

        if(charText.length == 0){

            vegelistdate2.addAll(copyvegelistdate2)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copyvegelistdate2.size-1){

                val name =  Utils.getString(copyvegelistdate2.get(i).CORRESPONDINGNAME);

                names.add(name)

            }

            for(i in 0..names.size-1){

                if(names.get(i).toLowerCase().contains(charText)){
                    vegelistdate2.add(copyvegelistdate2.get(i))
                }

            }

        }

        vegelistAdapte2.notifyDataSetChanged()

    }
}
