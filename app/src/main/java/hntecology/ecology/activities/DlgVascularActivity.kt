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
import hntecology.ecology.model.Vascular_plant


class DlgVascularActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView

    private lateinit var listdata1 : ArrayList<Vascular_plant>

    private lateinit var copylistdata1 : ArrayList<Vascular_plant>

    private lateinit var listAdapte1: DlgPlantAdapter;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

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

        val dataList:Array<String> = arrayOf("no","taxon","zoological","name_kr","author","year","Phylum_name","Phylum_name_kr","Class_name","Class_name_kr","Order_name","Order_name_kr"
                ,"Family_name","Family_name_kr","Genus_name","Genus_name_kr","Species_name","Species_name_kr","Subspecies_subsp","author","Subspecies_name","Subspecies_name_kr","Variety_var"
                ,"Variety_name","Variety_name_kr","Forma_f","Forma_name","Forma_name_kr");

        val data1=  db.query(tableName,dataList,null,null,"name_kr",null,null,null);

        listView1 = findViewById(R.id.list_view1)

        listdata1 = ArrayList()

        copylistdata1 = ArrayList()

        listAdapte1 = DlgPlantAdapter(context, listdata1);

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

        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            var data =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)

            var name = data.name_kr
            var family_name = data.Family_name_kr
            var zoological = data.zoological

            val intent = Intent();
            intent.putExtra("name",name)
            intent.putExtra("family_name",family_name)
            intent.putExtra("zoological",zoological)
            setResult(RESULT_OK, intent);
            finish()

        })

    }

    fun dataList(listdata:ArrayList<Vascular_plant>, data: Cursor) {

        while (data.moveToNext()){

            var model : Vascular_plant;

            model = Vascular_plant(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getString(8),data.getString(9),data.getString(10),
                    data.getString(11),data.getString(12),data.getString(13),data.getString(14),data.getString(15),data.getString(16),data.getString(17),data.getString(18),data.getString(19),data.getString(20),data.getString(21),
                    data.getString(22),data.getString(23),data.getString(24),data.getString(25),data.getString(26),false);

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

                val name =  Utils.getString(copylistdata1.get(i).name_kr, copylistdata1.get(i).name_kr);

                names.add(name)

            }

            for (i in 0..copylistdata1.size-1){

                if(copylistdata1.get(i).name_kr!!.toLowerCase().contains(charText)){
                    listdata1.add(copylistdata1.get(i))
                }

            }

        }

        listAdapte1.notifyDataSetChanged()

    }

}
