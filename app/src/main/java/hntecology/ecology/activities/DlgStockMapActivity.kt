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
import hntecology.ecology.adapter.DlgKoftrAdapter
import hntecology.ecology.adapter.DlgStokeMapAdapter1
import hntecology.ecology.adapter.DlgStokeMapAdapter2
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Jaso
import hntecology.ecology.base.Utils
import hntecology.ecology.model.*
import kotlinx.android.synthetic.main.activity_dlg_stock_map.*

class DlgStockMapActivity : Activity() {

    private lateinit var context: Context;


    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false
    private var copyadapterData :ArrayList<Koftr_group> = ArrayList<Koftr_group>()
    private lateinit var listView1: ListView
    private lateinit var listdata1 : java.util.ArrayList<Koftr_group>
    private lateinit var listAdapter1: DlgKoftrAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_stock_map)

        context = this;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();



        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        listView1 = findViewById(R.id.listLV)
        listdata1 = java.util.ArrayList()
        listAdapter1 = DlgKoftrAdapter(context,listdata1)
        listView1.adapter = listAdapter1
        val dataList:Array<String> = arrayOf("*");
        val data = db!!.query("KoftrGroup", dataList, null, null, null, null, "code_name", null);
        setDataList(listdata1,data);
        copyadapterData.addAll(listdata1)

        listView1.setOnItemClickListener { parent, view, position, id ->

            var data = listdata1.get(position)
            var name = data.code_name
            var code = data.code

            val intent = Intent();
            intent.putExtra("name",name)
            intent.putExtra("code",code.toString())
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

    fun setDataList(listdata: java.util.ArrayList<Koftr_group>,data: Cursor){

        while (data.moveToNext()){

            var model: Koftr_group;

            model = Koftr_group(data.getInt(0), data.getInt(1), data.getString(2), data.getString(3));


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

                val name =  Utils.getString(copyadapterData.get(i).code_name, copyadapterData.get(i).code_name);

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
