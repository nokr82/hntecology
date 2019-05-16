package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgPlantAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlgvegetation.*
import java.util.ArrayList
import android.text.Editable
import hntecology.ecology.adapter.DlgVegeAdapter2
import hntecology.ecology.base.Jaso
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.Vascular_plant


class DlgVegetationActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1 : ArrayList<Vegetation>
    private lateinit var listAdapte1: DlgVegeAdapter2;

    private lateinit var copylistdata1 : ArrayList<Vegetation>

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false

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

        val dataList:Array<String> = arrayOf("CATEGORYCODE","CATEGORY","CLASSCODE","SIGN","CORRESPONDINGNAME");

        val data1=  db.query(tableName,dataList,null,null,null,null,null,null);

        listView1 = findViewById(R.id.list_view1)

        listdata1 = ArrayList()

        copylistdata1 = ArrayList()

        listAdapte1 = DlgVegeAdapter2(context, listdata1);

        listView1.adapter = listAdapte1

        dataList(listdata1,data1);
        copylistdata1.addAll(listdata1)

        leftsearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                val text = leftsearch.text.toString()
                leftSearch(text)
            }
        });

        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            var data =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)

            var name = data.CORRESPONDINGNAME

            val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

            val EndangeredData = db.query("ENDANGERED", dataEndangeredList, "TITLE = '$name'", null, null, null, null, null);

            while (EndangeredData.moveToNext()) {

                var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                chkData = true

            }

            if(chkData){
                val intent = Intent();
                intent.putExtra("name",name)
                setResult(RESULT_OK, intent);
                finish()
            }else {
                val intent = Intent();
                intent.putExtra("name",name)
                setResult(RESULT_OK, intent);
                finish()
            }

            EndangeredData.close()

        }

    }

    fun dataList(listdata:ArrayList<Vegetation>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vegetation;

            model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

            listdata.add(model)

        }
    }

    fun leftSearch(charText: String){
        for(i in 0..listdata1.size-1){
            listdata1.get(i).chkSelect = false
        }

        listdata1.clear()

        if(charText.length == 0){

            listdata1.addAll(copylistdata1)


        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copylistdata1.size-1){

                val name =  Utils.getString(copylistdata1.get(i).CORRESPONDINGNAME, copylistdata1.get(i).CORRESPONDINGNAME);

                names.add(name)

            }

            for (i in 0..copylistdata1.size-1){

                if (Jaso.startsWith(copylistdata1.get(i).CORRESPONDINGNAME!!, charText)
                        ||copylistdata1.get(i).CORRESPONDINGNAME!!.toLowerCase().contains(charText)) {
                    listdata1.add(copylistdata1.get(i))
                }

            }

        }

        listAdapte1.notifyDataSetChanged()

    }

}
