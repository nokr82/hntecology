package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.BiotopeClassAdapter
import hntecology.ecology.adapter.BiotopeClassAdapter2
import hntecology.ecology.adapter.BiotopeClassAdapter3
import hntecology.ecology.adapter.BiotopeClassAdapter4
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeClass
import hntecology.ecology.model.Number
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlg_biotope_class_acitivity.*

class DlgBiotopeClassActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView
    private lateinit var listView3: ListView
    private lateinit var listView4: ListView

    private lateinit var listdata1 : ArrayList<BiotopeClass>
    private lateinit var listdata2 : ArrayList<BiotopeClass>
    private lateinit var listdata3 : ArrayList<Vegetation>
    private lateinit var listdata4 : ArrayList<Number>

    private lateinit var listAdapte1: BiotopeClassAdapter;
    private lateinit var listAdapte2: BiotopeClassAdapter2;
    private lateinit var listAdapte3: BiotopeClassAdapter3;
    private lateinit var listAdapte4: BiotopeClassAdapter4;

    private lateinit var copylistdata3 : ArrayList<Vegetation>

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var list3position:Int = 0

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
        listView4 = findViewById(R.id.class_list_view4)

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()
        listdata4 = ArrayList()

        copylistdata3 = ArrayList()

        listAdapte1 = BiotopeClassAdapter(context, listdata1);
        listAdapte2 = BiotopeClassAdapter2(context, listdata2);
        listAdapte3 = BiotopeClassAdapter3(context, listdata3);
        listAdapte4 = BiotopeClassAdapter4(context,listdata4)

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3
        listView4.adapter = listAdapte4

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

            var biotopeClass:BiotopeClass = listAdapte2.getItem(position)

            if(biotopeClass.smallcategory.equals("조림지") || biotopeClass.smallcategory.equals("이차림") || biotopeClass.smallcategory.equals("자연림")
                    || biotopeClass.smallcategory.equals("하천림")){

                val dataList:Array<String> = arrayOf("categorycode","category","classcode","sign","correspondingname");

                val vegedata=  db.query("Vegetation",dataList,null,null,null,null,"SIGN",null);

                listAdapte2.setItemSelect(position)

                class_probar.visibility= View.VISIBLE
                data3List(listdata3,vegedata)
                class_probar.visibility= View.GONE

                copylistdata3.addAll(listdata3)

                listAdapte3.notifyDataSetChanged()

            }else {

                var intent = Intent();

                intent.putExtra("biotopeClass",biotopeClass)

                setResult(RESULT_OK, intent);
                finish()

            }

        })

        listView3.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            list3position = position

            val dataList:Array<String> = arrayOf("COUNT");

            val numdata=  db.query("Number",dataList,null,null,null,null,"COUNT",null);

            listAdapte3.setItemSelect(position)

            class_probar.visibility= View.VISIBLE
            data4List(listdata4,numdata)
            class_probar.visibility= View.GONE

            listAdapte4.notifyDataSetChanged()

        })

        listView4.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            val vegetation : Vegetation = listAdapte3.getItem(list3position)
            val number:Number = listAdapte4.getItem(position)

            var intent = Intent();

            intent.putExtra("Vegetation",vegetation)
            intent.putExtra("Number",number)

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

    fun data4List(listdata:ArrayList<Number>, data: Cursor) {

        while (data.moveToNext()){

            var model : Number;

            model = Number(data.getString(0),false);

            listdata.add(model)
        }
    }

    fun search(charText: String){
        for(i in 0.. listdata3.size-1){
            listdata3.get(i).chkSelect = false
        }

        listdata3.clear()

        if(charText.length == 0){

            listdata3.addAll(copylistdata3)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copylistdata3.size-1){

                val name =  Utils.getString(copylistdata3.get(i).CORRESPONDINGNAME);

                names.add(name)

            }

            for(i in 0..names.size-1){

                if(names.get(i).toLowerCase().contains(charText)){
                    listdata3.add(copylistdata3.get(i))
                }

            }

        }

        listAdapte3.notifyDataSetChanged()

    }


}