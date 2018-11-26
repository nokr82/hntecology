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
import hntecology.ecology.adapter.DlgBirdsAdapter
import hntecology.ecology.adapter.DlgBirdsAdapter2
import hntecology.ecology.model.Birds
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.EndangeredSelect
import kotlinx.android.synthetic.main.activity_dlgvegetation.*
import kotlinx.android.synthetic.main.dlg_birds.*
import org.json.JSONObject
import kotlin.collections.ArrayList

class DlgBirdsActivity : Activity() {

    private lateinit var context:Context;

    private var copyadapterData:ArrayList<Birds> = ArrayList<Birds>()

    private var endangerdDatas : ArrayList<Endangered> = ArrayList<Endangered>()

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : java.util.ArrayList<Birds>
    private lateinit var listdata2 : java.util.ArrayList<EndangeredSelect>

    private lateinit var listAdapte1: DlgBirdsAdapter;
    private lateinit var listAdapter2 : DlgBirdsAdapter2


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_birds)

        context = this;

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val item = EndangeredSelect("a","성조가 둥지 또는 둥지가 있을 것으로 예상되는 장소를 3회 이상 출입하는 것을 관찰")
        val item2 = EndangeredSelect("b","성조가 포란 또는 새끼를 품고 있는 것을 관찰")
        val item3 = EndangeredSelect("c","성조가 새끼의 배설물을 운반하고 있는 것을 관찰")
        val item4 = EndangeredSelect("d","성조가 새끼에게 먹이를 운반 또는 경계하는 것을 관찰")
        val item5 = EndangeredSelect("e","의상행동을 관찰")
        val item6 = EndangeredSelect("f","교미행동을 관찰(겨울철새/통과철새는 제외)")
        val item7 = EndangeredSelect("g","당해 또는 2년 이내에 이소한 것으로 추정되는 둥지를 관찰")
        val item8 = EndangeredSelect("h","둥지 트는 행동을 관찰(둥지로 이용코자 땅 파는 행동 포함)")
        val item9 = EndangeredSelect("i","성조가 둥지를 틀 때 쓰이는 재료를 운반하는 것을 관찰")
        val item10 = EndangeredSelect("j","알이 있는 둥지를 관찰")
        val item11 = EndangeredSelect("k","성조가 앉아 있는 둥지 근처에서 그 종의 알 껍질을 관찰")
        val item12 = EndangeredSelect("l","새끼가 들어 있는 둥지를 관찰")
        val item13 = EndangeredSelect("m","둥지 근처에서 거의 이동하지 못하는 새끼를 관찰")
        val item14 = EndangeredSelect("n","새끼의 소리를 들음")



        val enDataList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");


        listView1 = findViewById(R.id.listLV)
        listView2 = findViewById(R.id.list2LV)


        listdata1 = java.util.ArrayList()
        listdata2 = java.util.ArrayList()

        listAdapte1 = DlgBirdsAdapter(context, listdata1);
        listAdapter2 = DlgBirdsAdapter2(context, listdata2)

        val dataList:Array<String> = arrayOf("no","taxon","zoological","name_kr","author","year","Phylum_name","Phylum_name_kr","Class_name","Class_name_kr","Order_name","Order_name_kr","Family_name"
                ,"Family_name_kr","Genus_name","Genus_name_kr","Species_name","Species_name_kr");
        val data = db.query(tableName, dataList, null, null, null, null, "name_kr", null);


        copyadapterData.addAll(listdata1)

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapter2

        dataBirdsList(listdata1,data)

        val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");
        val EndangeredData = db.query("ENDANGERED", dataEndangeredList, null, null, null, null, "TITLE", null);

        dataEndangerdList(endangerdDatas,EndangeredData)

        closeLL.setOnClickListener {
            finish()
        }

        listView1.setOnItemClickListener { parent, view, position, id ->

            listAdapter2.clearItem()

            listAdapte1.setItemSelect(position)

            var data = listdata1.get(position)
            var name = data.name_kr
            var family_name = data.Family_name
            var zoological = data.zoological

            val EndangeredData = db.query("ENDANGERED", dataEndangeredList, "name", null, null, null, null, null);


                    dlg_probars.visibility = View.VISIBLE
                    listdata2.add(item)
                    listdata2.add(item2)
                    listdata2.add(item3)
                    listdata2.add(item4)
                    listdata2.add(item5)
                    listdata2.add(item6)
                    listdata2.add(item7)
                    listdata2.add(item8)
                    listdata2.add(item9)
                    listdata2.add(item10)
                    listdata2.add(item11)
                    listdata2.add(item12)
                    listdata2.add(item13)
                    listdata2.add(item14)
                    dlg_probars.visibility = View.GONE
                    val intent = Intent();
                    intent.putExtra("name", name)
                    intent.putExtra("family_name", family_name)
                    intent.putExtra("zoological", zoological)
                    setResult(RESULT_OK, intent);
                    finish()

        }

        listView2.setOnItemClickListener { adapterView, view, i, l ->

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

    fun dataBirdsList(listdata: java.util.ArrayList<Birds>, data: Cursor) {

        while (data.moveToNext()){

            var model : Birds;

            model = Birds(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getString(8),data.getString(9),data.getString(10)
            ,data.getString(11),data.getString(12),data.getString(13),data.getString(14),data.getString(15),data.getString(16),data.getString(17),false);

            listdata.add(model)
        }
    }

    fun dataEndangerdList(listdata: java.util.ArrayList<Endangered>, data: Cursor) {

        while (data.moveToNext()){

            var model : Endangered;

            model = Endangered(data.getString(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5));

            listdata.add(model)
        }
    }



}





