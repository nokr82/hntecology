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
import android.widget.ListView
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgFishAdapter
import hntecology.ecology.adapter.DlgReptiliaAdapter
import hntecology.ecology.base.Jaso
import hntecology.ecology.model.Birds
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.Fishs
import kotlinx.android.synthetic.main.dig_fish.*
import org.json.JSONObject
import kotlin.collections.ArrayList

class DlgFishActivity : Activity() {

    private lateinit var context:Context;

    private var adapterData :ArrayList<Fishs> = ArrayList<Fishs>()

    private var copyadapterData :ArrayList<Fishs> = ArrayList<Fishs>()

    private lateinit var apdater: DlgFishAdapter;

    var DlgHeight:Float=430F;

    var chkData = false

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    private lateinit var listView1: ListView

    private lateinit var listdata1: java.util.ArrayList<Fishs>

    private lateinit var listAdapter1: DlgFishAdapter;

    var SPEC:String = ""

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dig_fish)

        context = this;

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        dbManager = DataBaseHelper(context);
        db = dbManager!!.createDataBase();
        listView1 = findViewById(R.id.listfLV)
        listdata1 = java.util.ArrayList()
        listAdapter1 = DlgFishAdapter(context, listdata1);
        listView1.adapter = listAdapter1

        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query("fish", dataList, null, null, null, null, "name_kr", null);
        setDataList(listdata1,data);

        copyadapterData.addAll(listdata1)

        if (intent.getStringExtra("SPEC") != null){
            SPEC = intent.getStringExtra("SPEC")
            for (i in 0 until listdata1.size){
                if (listdata1.get(i).name_kr == SPEC){
                    listdata1.get(i).chkSelect = true
                    listView1.setSelection(i)
                }
            }
        }

        closeLL.setOnClickListener {
            finish()
        }

        listfLV.setOnItemClickListener { parent, view, position, id ->

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
                intent.putExtra("family_name",family_name)
                intent.putExtra("zoological",zoological)
                setResult(RESULT_OK, intent);
                finish()
            }else {
                val intent = Intent();
                intent.putExtra("name",name)
                intent.putExtra("family_name",family_name)
                intent.putExtra("zoological",zoological)
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

    fun setDataList(listdata: java.util.ArrayList<Fishs>,data: Cursor){

        while (data.moveToNext()){

            var model: Fishs;

            model = Fishs(data.getInt(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5), data.getString(6), data.getString(7), data.getString(8), data.getString(9), data.getString(10)
                    , data.getString(11), data.getString(12), data.getString(13), data.getString(14), data.getString(15), data.getString(16), data.getString(17),  data.getString(18), data.getString(19),false);


            listdata.add(model)
        }

    }

    fun search(charText: String) {
        listdata1.clear()

        if (charText.length == 0) {

            listdata1.addAll(copyadapterData)

        } else {

            var names: ArrayList<String> = ArrayList<String>()

            for (i in 0..copyadapterData.size - 1) {

                val name =  Utils.getString(copyadapterData.get(i).name_kr, copyadapterData.get(i).name_kr);

                names.add(name)

                println(names.get(i) + "-------------------------")

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





