package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgRobAdapter1
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeClass
import hntecology.ecology.model.common
import kotlinx.android.synthetic.main.activity_dlg_rob.*

class DlgRobActivity : Activity() {

    private lateinit var context: Context;

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView
    private lateinit var listView3: ListView

    private lateinit var listdata1 : ArrayList<common>
    private lateinit var listdata2 : ArrayList<common>
    private lateinit var listdata3 : ArrayList<common>

    private lateinit var listAdapte1: DlgRobAdapter1;
    private lateinit var listAdapte2: DlgRobAdapter1;
    private lateinit var listAdapte3: DlgRobAdapter1;

    var click1 = ""
    var click2 = ""
    var click3 = ""

    var cate = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_rob)

        context = applicationContext;

        window.setLayout(Utils.dpToPx(800f).toInt(), Utils.dpToPx(255f).toInt());
        this.setFinishOnTouchOutside(true);




        listView1 = findViewById(R.id.list_view1)
        listView2 = findViewById(R.id.list_view2)
        listView3 = findViewById(R.id.list_view3)

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()

        itemAdd()

        listAdapte1 = DlgRobAdapter1(context, listdata1);
        listAdapte2 = DlgRobAdapter1(context, listdata2);
        listAdapte3 = DlgRobAdapter1(context, listdata3);

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3

        var biotopeClass: BiotopeClass
        if (intent.getSerializableExtra("biotopeClass") != null) {
            biotopeClass = intent.getSerializableExtra("biotopeClass") as BiotopeClass
            cate = biotopeClass.smallcategory.toString()
            println("=========33333==$cate")
        }



        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            var veData =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)
            click1 = veData.code.toString()
        })

        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            var veData =  listAdapte2.getItem(position)

            listAdapte2.setItemSelect(position)
            click2 = veData.code.toString()
        })


        listView3.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            var veData =  listAdapte3.getItem(position)

            listAdapte3.setItemSelect(position)
            click3 = veData.code.toString()
        })

        selectLL.setOnClickListener {

            if (click1 == "" || click2 == "" || click3 == ""){
                Toast.makeText(this, "교목층 코드를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }

            var intent = Intent()
            intent.putExtra("code1",click1)
            intent.putExtra("code2",click2)
            intent.putExtra("code3",click3)
            intent.putExtra("result_cate",cate)
            setResult(RESULT_OK,intent)
            finish()
        }

    }

    fun itemAdd(){
        val item1 = common("아교목층 미발달",0,0,0f,false)
        val item2 = common("아교목층 외래종",1,0,0f,false)
        val item3 = common("아교목층 자생종",2,0,0f,false)

        listdata1.add(item1)
        listdata1.add(item2)
        listdata1.add(item3)

        val item4 = common("관목층 미발달",0,0,0f,false)
        val item5 = common("관목층 외래종",1,0,0f,false)
        val item6 = common("관목층 자생종",2,0,0f,false)

        listdata2.add(item4)
        listdata2.add(item5)
        listdata2.add(item6)

        val item7 = common("15cm 이하",0,0,0f,false)
        val item8 = common("15~25cm",1,0,0f,false)
        val item9 = common("25cm 이상",2,0,0f,false)

        listdata3.add(item7)
        listdata3.add(item8)
        listdata3.add(item9)
    }
}
