package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgFloraAdapter
import hntecology.ecology.adapter.DlgStokeMapAdapter1
import hntecology.ecology.adapter.DlgStokeMapAdapter2
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Jaso
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.Floras
import hntecology.ecology.model.StockMapSelect
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlg_stock_map.*

class DlgStockMapActivity : Activity() {

    private lateinit var context: Context;

    private var copyadapterData :ArrayList<Floras> = ArrayList<Floras>()

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false

    private lateinit var listView1: ListView
    private lateinit var listdata1 : java.util.ArrayList<Floras>
    private lateinit var listAdapter1: DlgFloraAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_stock_map)

        context = this;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        listView1 = findViewById(R.id.listLV)
        listdata1 = java.util.ArrayList()
        listAdapter1 = DlgFloraAdapter(context,listdata1)
        listView1.adapter = listAdapter1
        val dataList:Array<String> = arrayOf("*");
        val data = db!!.query("vascular_plant", dataList, null, null, null, null, "name_kr", null);
        setDataList(listdata1,data);
        copyadapterData.addAll(listdata1)

        listView1.setOnItemClickListener { parent, view, position, id ->

            var data = listdata1.get(position)
            var name = data.name_kr
            var family_name = data.Family_name
            var zoological = data.zoological

            val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

            val EndangeredData = db!!.query("ENDANGERED", dataEndangeredList, "TITLE = '$name'", null, null, null, null, null);

            while (EndangeredData.moveToNext()) {

                var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                chkData = true

            }

            if(chkData){
                val intent = Intent();
                intent.putExtra("name",name + "(멸종 위기)")
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

    fun setDataList(listdata: java.util.ArrayList<Floras>,data: Cursor){

        while (data.moveToNext()){

            var model: Floras;

            model = Floras(data.getInt(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7), data.getString(8), data.getString(9), data.getString(10)
                    , data.getString(11), data.getString(12), data.getString(13), data.getString(14), data.getString(15), data.getString(16), data.getString(17),  data.getString(18), data.getString(19),data.getString(20),
                    data.getString(21),data.getString(22),data.getString(23),data.getString(24),data.getString(25),data.getString(26),false);


            listdata.add(model)
        }

    }

    fun search(charText: String){
        listdata1.clear()

        if(charText.length == 0){

            listdata1.addAll(copyadapterData)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copyadapterData.size-1){

                val name =  Utils.getString(copyadapterData.get(i).name_kr, copyadapterData.get(i).name_kr);

                names.add(name)

            }

            for(i in 0..names.size-1){

                if (Jaso.startsWith(names.get(i), charText)
                        || names.get(i).toLowerCase().contains(charText)) {
                    listdata1.add(copyadapterData.get(i))
                }

            }

        }

        listAdapter1.notifyDataSetChanged()

    }

}
