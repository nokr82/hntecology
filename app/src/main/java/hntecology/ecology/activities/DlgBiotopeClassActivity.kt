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
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.adapter.*
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
    private lateinit var listView5: ListView
    private lateinit var listView6: ListView

    private lateinit var listdata1 : ArrayList<BiotopeClass>
    private lateinit var listdata2 : ArrayList<BiotopeClass>
    private lateinit var listdata3 : ArrayList<BiotopeClass>
    private lateinit var listdata4 : ArrayList<Number>
    private lateinit var listdata5 : ArrayList<Vegetation>
    private lateinit var listdata6 : ArrayList<Number>

    private lateinit var listAdapte1: BiotopeClassAdapter;
    private lateinit var listAdapte2: BiotopeClassAdapter2;
    private lateinit var listAdapte3: BiotopeClassAdapter3;
    private lateinit var listAdapte4: BiotopeClassAdapter5;
    private lateinit var listAdapte5: BiotopeClassAdapter4;
    private lateinit var listAdapte6: BiotopeClassAdapter5;

    private lateinit var copylistdata3 : ArrayList<Vegetation>

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var list3position = -1
    var list4position:Int = 0
    var list5position = -1

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var GETCODE = 1000

    var code1 = ""
    var code2 = ""
    var code3 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_biotope_class_acitivity)

        context = applicationContext;

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(1000F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        class_dlgTitle.setText(titleName)

        val dataList:Array<String> = arrayOf("CATEGORY","MIDDLECATEGORY","SMALLCATEGORY","SIGN");

        val data1=  db!!.query(tableName,dataList,null,null,"CATEGORY",null,null,null);

        listView1 = findViewById(R.id.class_list_view1)
        listView2 = findViewById(R.id.class_list_view2)
        listView3 = findViewById(R.id.class_list_view3)
        listView4 = findViewById(R.id.class_list_view4)
        listView5 = findViewById(R.id.class_list_view5)
        listView6 = findViewById(R.id.class_list_view6)

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()
        listdata4 = ArrayList()
        listdata5 = ArrayList()
        listdata6 = ArrayList()

        copylistdata3 = ArrayList()

        listAdapte1 = BiotopeClassAdapter(context, listdata1);
        listAdapte2 = BiotopeClassAdapter2(context, listdata2);
        listAdapte3 = BiotopeClassAdapter3(context, listdata3);
        listAdapte4 = BiotopeClassAdapter5(context,listdata4)
        listAdapte5 = BiotopeClassAdapter4(context, listdata5);
        listAdapte6 = BiotopeClassAdapter5(context,listdata6)

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3
        listView4.adapter = listAdapte4
        listView5.adapter = listAdapte5
        listView6.adapter = listAdapte6

        dataList(listdata1,data1);

        selectLL.setOnClickListener {
            println("----list3 : $list3position")
            println("----list5 : $list5position")
            if (list3position > -1){
                if (list5position > -1){
                    var vegetation = listAdapte5.getItem(list5position)
                    var category = vegetation.CATEGORY
                    var intent = Intent()
                    if (category == "기타"){
                        intent.putExtra("etc",category)

                        setResult(RESULT_OK, intent);
                        finish()
                    } else {
                        var number = ""
                        if (code1 != ""){
                            number += code1
                        }

                        if (code2 != ""){
                            number += code2
                        }

                        if (code3 != ""){
                            number += code3
                        }

                        intent.putExtra("Vegetation",vegetation)
                        intent.putExtra("Number",number)

                        setResult(RESULT_OK, intent);
                        finish()
                    }
                } else {
                    var biotopeClass:BiotopeClass = listAdapte3.getItem(list3position)

                    intent.putExtra("biotopeClass",biotopeClass)

                    setResult(RESULT_OK, intent);
                    finish()
                }
            } else {
                Toast.makeText(this, "분류기준을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
        }

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte2.clearItem()
            listAdapte3.clearItem()
            listAdapte4.clearItem()
            listAdapte5.clearItem()
            listAdapte6.clearItem()

            code1 = ""
            code2 = ""
            code3 = ""

            list3position = -1
            list5position = -1

            var veData =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)

            val data2 =  db!!.query(tableName,dataList,"CATEGORY='"+veData.CATEGORY +"'" ,null,"MIDDLECATEGORY",null,null,null);

            class_probar.visibility= View.VISIBLE
            dataList(listdata2,data2);
            class_probar.visibility= View.GONE

            data2.close()

        })

        listView2.setOnItemClickListener (AdapterView.OnItemClickListener{ adapterView, view, position, l ->

            listAdapte3.clearItem()
            listAdapte4.clearItem()
            listAdapte5.clearItem()
            listAdapte6.clearItem()

            println("position ------- $position")

            var veData:BiotopeClass = listAdapte2.getItem(position)

            listAdapte2.setItemSelect(position)

            val data =  db!!.query(tableName,dataList,"MIDDLECATEGORY='"+veData.middlecategory +"'" ,null,null,null,null,null);

            class_probar.visibility= View.VISIBLE
            dataList(listdata3,data)
            class_probar.visibility= View.GONE

            data.close()

        })

        listView3.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte4.clearItem()
            listAdapte5.clearItem()
            listAdapte6.clearItem()
            list3position = position

            var biotopeClass:BiotopeClass = listAdapte3.getItem(position)

            if(biotopeClass.smallcategory.equals("조림지") || biotopeClass.smallcategory.equals("이차림") || biotopeClass.smallcategory.equals("자연림")
                    || biotopeClass.smallcategory.equals("하천림")){

                val dataList:Array<String> = arrayOf("categorycode","category","classcode","sign","correspondingname");


                listAdapte3.setItemSelect(position)

                listAdapte3.notifyDataSetChanged()

                val item2 = Number("식재림",false)
                val item = Number("자연림",false)
                listAdapte4.addItem(item2)
                listAdapte4.addItem(item)
            }else {
                listAdapte3.setItemSelect(position)
//                var intent = Intent();
//
//                intent.putExtra("biotopeClass",biotopeClass)
//
//                setResult(RESULT_OK, intent);
//                finish()
            }

        })

        listView4.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte5.clearItem()
            listAdapte6.clearItem()

            list4position = position

            if (listdata5 != null){
                listdata5.clear()
            }

            val data = listAdapte4.getItem(position)
            val name = data.COUNT

            val dataList:Array<String> = arrayOf("*");
            var numdata=  db!!.query("Vegetation",dataList,null,null,null,null,"CORRESPONDINGNAME",null);

            class_probar.visibility= View.VISIBLE
            class_probar.visibility= View.GONE

            val search = "식재림"

            copylistdata3.addAll(listdata5)
            if (name == "식재림"){
                println("nick------biotope -- $name")
                numdata=  db!!.query("Vegetation",dataList,"CATEGORY == '$search'",null,null,null,"CORRESPONDINGNAME",null);
            } else {
                println("nick------biotope -- $name")
                numdata=  db!!.query("Vegetation",dataList,"CATEGORY != '$search'",null,null,null,"CORRESPONDINGNAME",null);
            }

            listAdapte4.setItemSelect(position)

            class_probar.visibility= View.VISIBLE
            data3List(listdata5,numdata)
            class_probar.visibility= View.GONE

            listAdapte4.notifyDataSetChanged()

            numdata.close()

        })

        listView5.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            listAdapte6.clearItem()

            list4position = position
            list5position = position

            val vegetation : Vegetation = listAdapte5.getItem(position)
            val cate = vegetation.CATEGORY

            code1 = ""
            code2 = ""
            code3 = ""

            if (cate == "기타"){
//                var intent = Intent();
//
//                intent.putExtra("etc",cate)
//
//                setResult(RESULT_OK, intent);
//                finish()
                listAdapte5.setItemSelect(position)
            } else {

                val dataList:Array<String> = arrayOf("COUNT");

                val numdata=  db!!.query("Number",dataList,null,null,null,null,"COUNT",null);

                listAdapte5.setItemSelect(position)

                class_probar.visibility= View.VISIBLE
                data4List(listdata6,numdata)
                class_probar.visibility= View.GONE

                listAdapte5.notifyDataSetChanged()

                numdata.close()

                val intent = Intent(this, DlgRobActivity::class.java)

                startActivityForResult(intent, GETCODE);

            }


        })

        listView6.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            val vegetation : Vegetation = listAdapte5.getItem(list4position)
            val number:Number = listAdapte6.getItem(position)

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

            model = BiotopeClass(data.getString(0),data.getString(1),data.getString(2),data.getString(3),false);

            listdata.add(model)
        }
    }

    fun data3List(listdata:ArrayList<Vegetation>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vegetation;

            model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

            listdata.add(model)
        }

        var additem = Vegetation(1,"기타",1,"기타","기타",false)
        listdata.add(additem)
    }

    fun data4List(listdata:ArrayList<Number>, data: Cursor) {

        while (data.moveToNext()){

            var model : Number;

            model = Number(data.getString(0),false);

            listdata.add(model)
        }
    }

    fun search(charText: String){
        for(i in 0.. listdata5.size-1){
            listdata5.get(i).chkSelect = false
        }

        listdata5.clear()

        if(charText.length == 0){

            listdata5.addAll(copylistdata3)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copylistdata3.size-1){

                val name =  Utils.getString(copylistdata3.get(i).CORRESPONDINGNAME);

                names.add(name)

            }

            for(i in 0..names.size-1){

                if(names.get(i).toLowerCase().contains(charText)){
                    listdata5.add(copylistdata3.get(i))
                }

            }

        }

        listAdapte5.notifyDataSetChanged()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GETCODE -> {
                    if (data!!.getStringExtra("code1") != null){
                        code1 = data!!.getStringExtra("code1")
                    }

                    if (data!!.getStringExtra("code2") != null){
                        code2 = data!!.getStringExtra("code2")
                    }

                    if (data!!.getStringExtra("code3") != null){
                        code3 = data!!.getStringExtra("code3")
                    }
                }
            }
        }
    }

}
