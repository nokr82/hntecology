package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import hntecology.ecology.adapter.DlgMammalAdapter
import hntecology.ecology.adapter.DlgMammalAdapter2
import hntecology.ecology.adapter.DlgReptiliaAdapter
import hntecology.ecology.base.Jaso
import hntecology.ecology.model.Endangered
import hntecology.ecology.model.EndangeredSelect
import hntecology.ecology.model.Mammal
import kotlinx.android.synthetic.main.dlg_mammal.*
import org.json.JSONObject
import kotlin.collections.ArrayList

class DlgMammalActivity : Activity() {

    private lateinit var context:Context;

    private var copyadapterData :ArrayList<Mammal> = ArrayList<Mammal>()

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : java.util.ArrayList<Mammal>
    private lateinit var listdata2 : java.util.ArrayList<EndangeredSelect>

    private lateinit var listAdapte1: DlgMammalAdapter;
    private lateinit var listAdapter2: DlgMammalAdapter2

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F
    var END:String = ""
    var SPEC:String = ""

    var chkData = false

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_mammal)

        context = this;

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);


        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        dbManager = DataBaseHelper(context);
        db = dbManager!!.createDataBase();

        val dataList:Array<String> = arrayOf("no","taxon","zoological","name_kr","author","year","Phylum_name","Phylum_name_kr","Class_name","Class_name_kr","Order_name","Order_name_kr","Family_name"
                ,"Family_name_kr","Genus_name","Genus_name_kr","Species_name","Species_name_kr","Subspecies_name","Subspecies_name_kr");

        val data = db!!.query(tableName, dataList, null, null, null, null, "name_kr", null);

        listView1 = findViewById(R.id.listLV)
        listView2 = findViewById(R.id.listLV2)

        listdata1 = java.util.ArrayList()
        listdata2 = java.util.ArrayList()

        listAdapte1 = DlgMammalAdapter(context,listdata1)
        listAdapter2 = DlgMammalAdapter2(context,listdata2)

        dataReptiliaList(listdata1,data)

        val item = EndangeredSelect("A","동일 지역(격자)에서 사용하고 있는 보금자리가 발견되고 실체가 1회 이상 확인됨",false)
        val item2 = EndangeredSelect("B","동일 지역(격자)에서 배설물(오래된 것, 신선한 것 2개 이상)이 2회 이상 발견됨",false)
        val item3 = EndangeredSelect("C","동일 지역(격자)에서 발자국이 2회 이상 발견됨",false)
        val item4 = EndangeredSelect("D","동일 지역(격자)에서 실체가 2회 이상 발견됨",false)
        val item5 = EndangeredSelect("E","하천에서 어린 새끼와 어미가 함께 활동하는 모습이 관찰되는 지역(번식활동이 이루어지는 지역)",false)
        val item6 = EndangeredSelect("F","장기적 사용 흔적(실체, 최근 이용 배설물 다수)이 있는 보금자리가 존재할 경우(하천경계부에서 20m 이내)",false)
        val item7 = EndangeredSelect("G","산림이 우수한 지역의 5m 이내에 변색된 배설물(서식흔적)과 신선한 배설물이 3곳 이상에서 관찰됨",false)
        val item8 = EndangeredSelect("H","실체가 1회 이상 확인되고, 주변에 다수의 배설물이 산재한 지역",false)


        if (intent.getStringExtra("SPEC") != null){
            SPEC = intent.getStringExtra("SPEC")
            for (i in 0 until listdata1.size){
                if (listdata1.get(i).name_kr == SPEC){
                    listdata1.get(i).chkSelect = true
                    listView1.setSelection(i)
                    val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");
                    val EndangeredData = db!!.query("ENDANGERED", dataEndangeredList, "TITLE = '$SPEC'", null, null, null, null, null);

                    while (EndangeredData.moveToNext()) {

                        var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                        chkData = true

                    }

                    if (chkData){
                        selectTV.visibility = View.VISIBLE
                        listdata2.add(item)
                        listdata2.add(item2)
                        listdata2.add(item3)
                        listdata2.add(item4)
                        listdata2.add(item5)
                        listdata2.add(item6)
                        listdata2.add(item7)
                        listdata2.add(item8)
                    }else {
                        if(listdata2 != null) {
                            listdata2.clear()
                        }
                    }

                    if (intent.getStringExtra("END") != null){
                        END = intent.getStringExtra("END")
                        println("end ------$END")
                        var split = END.split(" ")
                        for (i in 0 until split.size){
                            for (j in 0 until listdata2.size){
                                if (split.get(i) == listdata2.get(j).SIGN){
                                    listdata2.get(j).is_checked = true
                                }
                            }
                        }
                    }

                }
            }
        }

        copyadapterData.addAll(listdata1)

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapter2


        selectLL.setOnClickListener {
            var name:String = ""
            var family_name:String = ""
            var zoological:String = ""
            var code:ArrayList<String> = ArrayList<String>()

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
                        code.add(listdata2.get(i).SIGN!!)
                    }
                }
            }

            if (name != "" && name != null) {

                intent.putExtra("name", name)
                intent.putExtra("family_name", family_name)
                intent.putExtra("zoological", zoological)

                if (code != null) {
                    intent.putExtra("code", code)
                }
                setResult(RESULT_OK, intent);

                finish()

                data.close()
            } else {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("빈값을 입력하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            intent.putExtra("name", name)
                            intent.putExtra("family_name", family_name)
                            intent.putExtra("zoological", zoological)

                            if (code != null) {
                                intent.putExtra("code", code)
                            }
                            setResult(RESULT_OK, intent);

                            finish()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

        }

        listLV.setOnItemClickListener { parent, view, position, id ->

            if(listdata2 != null){
                for(i in 0..listdata2.size-1){
                    listdata2.get(i).is_checked = false
                }
            }

            chkData = false

            listAdapter2.clearItem()

            selectTV.visibility = View.INVISIBLE

            for(i in 0..listdata1.size-1){
                listdata1.get(i).chkSelect = false
            }

            var data = listdata1.get(position)
            var name = data.name_kr
            var family_name = data.Family_name_kr
            var zoological = data.zoological

            if(data.chkSelect == false){
                data.chkSelect = true
                listAdapte1.notifyDataSetChanged()
            }else {
                data.chkSelect = false
                listAdapte1.notifyDataSetChanged()
            }

            val dataEndangeredList:Array<String> = arrayOf("ID","TITLE","SCIENTIFICNAME","CLASS","DANGERCLASS","CONTRYCLASS");
            val EndangeredData = db!!.query("ENDANGERED", dataEndangeredList, "TITLE = '$name'", null, null, null, null, null);

            while (EndangeredData.moveToNext()) {

                var endangered = Endangered(EndangeredData.getString(0),EndangeredData.getString(1),EndangeredData.getString(2),EndangeredData.getString(3),EndangeredData.getString(4),EndangeredData.getString(5))

                chkData = true

            }

            if (chkData){
                selectTV.visibility = View.VISIBLE
                    listdata2.add(item)
                    listdata2.add(item2)
                    listdata2.add(item3)
                    listdata2.add(item4)
                    listdata2.add(item5)
                    listdata2.add(item6)
                    listdata2.add(item7)
                    listdata2.add(item8)
            }else {
                if(listdata2 != null) {
                    listdata2.clear()
                }
            }

            EndangeredData.close()

        }

        listView2.setOnItemClickListener { adapterView, view, position, l ->

            val data = listdata2.get(position)

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

    fun dataReptiliaList(listdata: java.util.ArrayList<Mammal>, data: Cursor) {

        while (data.moveToNext()){

            var model : Mammal;

            model = Mammal(data.getInt(0),data.getString(1),data.getString(2),data.getString(3),data.getString(4),data.getString(5),data.getString(6),data.getString(7),data.getString(8),data.getString(9),data.getString(10)
                    ,data.getString(11),data.getString(12),data.getString(13),data.getString(14),data.getString(15),data.getString(16),data.getString(17),data.getString(18),data.getString(19),false);

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





