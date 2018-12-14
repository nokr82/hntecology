package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgStokeMapAdapter1
import hntecology.ecology.adapter.DlgStokeMapAdapter2
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Jaso
import hntecology.ecology.base.Utils
import hntecology.ecology.model.StockMapSelect
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlg_stock_map.*

class DlgStockMapActivity : Activity() {

    private lateinit var context: Context;

    private var copyadapterData :ArrayList<Vegetation> = ArrayList<Vegetation>()

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : java.util.ArrayList<StockMapSelect>
    private lateinit var listdata2 : java.util.ArrayList<Vegetation>

    private lateinit var listAdapter1: DlgStokeMapAdapter1;
    private lateinit var listAdapter2: DlgStokeMapAdapter2;

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

        val selectitem1 = StockMapSelect("자연림","N",false)
        val selectitem2 = StockMapSelect("식재림","A",false)
        val selectitem3 = StockMapSelect("기타","기타",false)

        listView1 = findViewById(R.id.listLV)
        listView2 = findViewById(R.id.listLV2)

        listdata1 = java.util.ArrayList()
        listdata2 = java.util.ArrayList()

        listAdapter1 = DlgStokeMapAdapter1(context,listdata1)
        listAdapter2 = DlgStokeMapAdapter2(context,listdata2)

        listdata1.add(selectitem1)
        listdata1.add(selectitem2)
        listdata1.add(selectitem3)

        listView1.adapter = listAdapter1
        listView2.adapter = listAdapter2


        listView1.setOnItemClickListener { adapterView, view, position, l ->

            if(listdata2 != null){
                listdata2.clear()
            }

            listAdapter1.setItemSelect(position)

            for(i in 0..listdata1.size-1){
                listdata1.get(i).chkSelect = false
            }

            listAdapter2.clearItem()

            var data = listdata1.get(position)

            if(data.chkSelect == false){
                data.chkSelect = true
                listAdapter1.notifyDataSetChanged()
            }else {
                data.chkSelect = false
                listAdapter1.notifyDataSetChanged()
            }

            if (data.Title == "기타"){

                intent.putExtra("CODE", data.code)

                setResult(RESULT_OK, intent);

                finish()


            } else if(data.Title == "자연림"){

                val dataList:Array<String> = arrayOf("CATEGORYCODE","CATEGORY","CLASSCODE","SIGN","CORRESPONDINGNAME");

                val title = "식재림"

                val data = db.query("Vegetation", dataList, "CATEGORY != '$title'", null, null, null, "CORRESPONDINGNAME", null);

                dataVegetationList(listdata2,data)

                if(copyadapterData != null){
                    copyadapterData.clear()
                }

                copyadapterData.addAll(listdata2)

                listAdapter2.notifyDataSetChanged()

                data.close()

            } else if(data.Title == "식재림"){
                val dataList:Array<String> = arrayOf("CATEGORYCODE","CATEGORY","CLASSCODE","SIGN","CORRESPONDINGNAME");

                val title = "식재림"

                val data = db.query("Vegetation", dataList, "CATEGORY = '$title'", null, null, null, "CORRESPONDINGNAME", null);

                dataVegetationList(listdata2,data)

                copyadapterData.addAll(listdata2)

                listAdapter2.notifyDataSetChanged()

                data.close()

            }

        }

        listView2.setOnItemClickListener { adapterView, view, position, l ->

            var data = listdata2.get(position)

            if (data.CATEGORY == "식재림"){
                intent.putExtra("division", "A")
            } else {
                intent.putExtra("division", "N")
            }

            intent.putExtra("CODE", data.SIGN)

            setResult(RESULT_OK, intent);

            finish()

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

    fun dataVegetationList(listdata: java.util.ArrayList<Vegetation>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vegetation;

            model = Vegetation(data.getInt(0),data.getString(1),data.getInt(2),data.getString(3),data.getString(4),false);

            listdata.add(model)

        }
    }

    fun search(charText: String){
        listdata2.clear()

        if(charText.length == 0){

            listdata2.addAll(copyadapterData)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copyadapterData.size-1){

                val name =  Utils.getString(copyadapterData.get(i).CORRESPONDINGNAME, copyadapterData.get(i).CORRESPONDINGNAME);

                names.add(name)

            }

            for(i in 0..names.size-1){

                if (Jaso.startsWith(names.get(i), charText)
                        || names.get(i).toLowerCase().contains(charText)) {
                    listdata2.add(copyadapterData.get(i))
                }

            }

        }

        listAdapter2.notifyDataSetChanged()

    }




}
