package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
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
    private lateinit var listView2: ListView

    private lateinit var listdata1 : java.util.ArrayList<Reptilia>
    private lateinit var listdata2 : java.util.ArrayList<EndangeredSelect>

    private lateinit var listAdapte1: DlgReptiliaAdapter;
    private lateinit var listAdapter2: DlgReptiliaAdapter2

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_reptilia)

        context = this;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        val dataList:Array<String> = arrayOf("no","taxon","zoological","name_kr","author","year","Phylum_name","Phylum_name_kr","Class_name","Class_name_kr","Order_name","Order_name_kr","Family_name"
                ,"Family_name_kr","Genus_name","Genus_name_kr","Species_name","Species_name_kr");

        val data = db.query(tableName, dataList, null, null, null, null, "name_kr", null);

        listView1 = findViewById(R.id.listLV)
        listView2 = findViewById(R.id.listLV2)

        listdata1 = java.util.ArrayList()
        listdata2 = java.util.ArrayList()

        listAdapte1 = DlgReptiliaAdapter(context,listdata1)
        listAdapter2 = DlgReptiliaAdapter2(context,listdata2)

        dataReptiliaList(listdata1,data);

        copyadapterData.addAll(listdata1)

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapter2

        val item = EndangeredSelect("Y","Y",false)
        val item2 = EndangeredSelect("N","N",false)

        selectLL.setOnClickListener {
            var name:String = ""
            var family_name:String = ""
            var zoological:String = ""
            var code:String = ""

            for(i in 0..listdata1.size-1){
                if (listdata1.get(i).chkSelect == true){
                    name = listdata1.get(i).name_kr!!
                    family_name = listdata1.get(i).Family_name_kr!!
                    zoological = listdata1.get(i).zoological!!
                }
            }

            if(listdata2 != null){
                for(i in 0..listdata2.size-1){
                    if(listdata2.get(i).is_checked == true){
                        code = listdata2.get(i).SIGN!!
                    }
                }
            }

            intent.putExtra("name", name)
            intent.putExtra("family_name", family_name)
            intent.putExtra("zoological", zoological)

            if(code != null){
                intent.putExtra("code", code)
            }
            setResult(RESULT_OK, intent);

            finish()

        }

        listView1.setOnItemClickListener { parent, view, position, id ->
            if(listdata2 != null){
                for(i in 0..listdata2.size-1){
                    listdata2.get(i).is_checked = false
                }
            }

            chkData = false

            listAdapter2.clearItem()

            selectTV.visibility = View.INVISIBLE

            listAdapte1.setItemSelect(position)

            for(i in 0..listdata1.size-1){
                listdata1.get(i).chkSelect = false
            }

            var data = listdata1.get(position)

            if(data.chkSelect == false){
                data.chkSelect = true
                listAdapte1.notifyDataSetChanged()
            }else {
                data.chkSelect = false
                listAdapte1.notifyDataSetChanged()
            }

            var name = data.name_kr

            val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");

            val EndangeredData = db.query("ENDANGERED", dataEndangeredList, "TITLE = '$name'", null, null, null, null, null);

            while (EndangeredData.moveToNext()) {

                var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                chkData = true

            }

            if(chkData){
                selectTV.visibility = View.VISIBLE
                probar.visibility = View.VISIBLE
                listdata2.add(item)
                listdata2.add(item2)
                probar.visibility = View.GONE
            }else {
                if(listdata2 != null) {
                    listdata2.clear()
                }
            }

        }

        listView2.setOnItemClickListener { adapterView, view, position, l ->

            val data = listdata2.get(position)

            listAdapter2.setItemSelect(position)

            for(i in 0..listdata2.size-1){
                listdata2.get(i).is_checked = false
            }

            if(data.is_checked == false){
                data.is_checked = true
                listAdapter2.notifyDataSetChanged()
            }else {
                data.is_checked = false
                listAdapter2.notifyDataSetChanged()
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

                if(names.get(i).toLowerCase().contains(charText)){
                    listdata1.add(copyadapterData.get(i))
                }

            }

        }

        listAdapte1.notifyDataSetChanged()

    }

}





