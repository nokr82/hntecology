package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ListView
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgReptiliaAdapter
import hntecology.ecology.adapter.DlgReptiliaAdapter2
import hntecology.ecology.base.Jaso
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.EndangeredSelect
import hntecology.ecology.model.Reptilia
import kotlinx.android.synthetic.main.dlg_reptilia.*
import org.json.JSONObject
import kotlin.collections.ArrayList

class DlgReptiliaActivity : Activity() {

    private lateinit var context:Context;

    private var copyadapterData :ArrayList<Reptilia> = ArrayList<Reptilia>()

    private lateinit var apdater: DlgReptiliaAdapter;

    private lateinit var listView1: ListView

    private lateinit var listdata1 : java.util.ArrayList<Reptilia>

    private lateinit var listAdapte1: DlgReptiliaAdapter;

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var SPEC = ""

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_reptilia)

        context = this;

        dbManager = DataBaseHelper(context);
        db = dbManager!!.createDataBase();

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()


        val dataList:Array<String> = arrayOf("no","taxon","zoological","name_kr","author","year","Phylum_name","Phylum_name_kr","Class_name","Class_name_kr","Order_name","Order_name_kr","Family_name"
                ,"Family_name_kr","Genus_name","Genus_name_kr","Species_name","Species_name_kr");

        val data = db!!.query(tableName, dataList, null, null, null, null, "name_kr", null);

        listView1 = findViewById(R.id.listLV)

        listdata1 = java.util.ArrayList()

        listAdapte1 = DlgReptiliaAdapter(context,listdata1)

        dataReptiliaList(listdata1,data);

        if (intent.getStringExtra("SPEC") != null){
            SPEC = intent.getStringExtra("SPEC")

            for (i in 0 until listdata1.size){
                if (listdata1.get(i).name_kr == SPEC){
                    listdata1.get(i).chkSelect = true
                    listView1.setSelection(i)
                }
            }
        }

        copyadapterData.addAll(listdata1)

        listView1.adapter = listAdapte1

        listView1.setOnItemClickListener { parent, view, position, id ->

            chkData = false

            var data = listdata1.get(position)

            var name = data.name_kr
            val family_name = data.Family_name_kr
            val zoological = data.zoological

            val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

            val EndangeredData = db!!.query("ENDANGERED", dataEndangeredList, "TITLE = '$name'", null, null, null, null, null);

            while (EndangeredData.moveToNext()) {

                var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                chkData = true

            }
            EndangeredData.close()


            if(chkData){
                probar.visibility = View.VISIBLE

                intent.putExtra("name", name + "(멸종 위기)")
                intent.putExtra("family_name", family_name)
                intent.putExtra("zoological", zoological)
                setResult(RESULT_OK, intent);
                finish()

                probar.visibility = View.GONE
            }else {
                intent.putExtra("name", name)
                intent.putExtra("family_name", family_name)
                intent.putExtra("zoological", zoological)
                setResult(RESULT_OK, intent);
                finish()
            }

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

    fun dataReptiliaList(listdata: java.util.ArrayList<Reptilia>, data: Cursor) {

        while (data.moveToNext()){

            var model : Reptilia;

            model = Reptilia(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getString(8),data.getString(9),data.getString(10)
                    ,data.getString(11),data.getString(12),data.getString(13),data.getString(14),data.getString(15),data.getString(16),data.getString(17),false);

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

        listAdapte1.notifyDataSetChanged()

    }

}





