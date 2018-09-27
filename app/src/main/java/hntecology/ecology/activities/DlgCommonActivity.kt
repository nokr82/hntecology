package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.*
import hntecology.ecology.adapter.AreaItem1Adapte
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeModel
import kotlinx.android.synthetic.main.activity_dlgcommon.*
import java.util.*
import hntecology.ecology.R


class DlgCommonActivity : Activity() {

    private lateinit var context:Context;


    private lateinit var listView1: ListView
    private lateinit var listView2: ListView
    private lateinit var listView3: ListView


    private lateinit var listdata1 :ArrayList<BiotopeModel>
    private lateinit var listdata2 :ArrayList<BiotopeModel>
    private lateinit var listdata3 :ArrayList<BiotopeModel>

    private lateinit var listAdapte1: AreaItem1Adapte;
    private lateinit var listAdapte2: AreaItem1Adapte;
    private lateinit var listAdapte3: AreaItem1Adapte;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F;

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlgcommon)

        context = applicationContext;

        val intent = getIntent()

        tableName = intent.getStringExtra("table");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        dlgTitle.setText(titleName)

        //select
        val dataList:Array<String> = arrayOf("name","code","rgb_code");
        //대분류
        val data1 =  db.query(tableName,dataList,"g_code='A'",null,null,null,"code",null);
        //대분류 중분류 소분류
        listView1 = findViewById(R.id.recipe_list_view1)
        listView2 = findViewById(R.id.recipe_list_view2)
        listView3 = findViewById(R.id.recipe_list_view3)

        //대분류 중분류 소분류

        listdata1 = ArrayList()
        listdata2 = ArrayList()
        listdata3 = ArrayList()

        //대분류 데이터 셋팅
        dataList(listdata1,data1);




        //대분류 중분류 소분류
        listAdapte1 = AreaItem1Adapte(context, listdata1);
        listAdapte2 = AreaItem1Adapte(context, listdata2);
        listAdapte3 = AreaItem1Adapte(context, listdata3);

        listView1.adapter = listAdapte1
        listView2.adapter = listAdapte2
        listView3.adapter = listAdapte3

        //대분류
        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            //중분류 소분류 리스트 클리어
            // listAdapte2.clearItem()
            // listAdapte3.clearItem()

            var biomModel =  listAdapte1.getItem(position)

            listAdapte1.setItemSelect(position)

            /*
            val data2 =  db.query(tableName,dataList,"g_code='B' and code like '%"+biomModel.code+"%'",null,null,null,"code",null);

            dlg_probar.visibility= View.VISIBLE
            dataList(listdata2,data2);
            dlg_probar.visibility= View.GONE
            */


        })

        //중분류
        listView2.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            listAdapte3.clearItem()

            val biomModel =  listAdapte2.getItem(position)

            listAdapte2.setItemSelect(position)

            val data3 =  db.query(tableName,dataList,"g_code='C' and code like '%"+biomModel.code+"%'",null,null,null,"code",null);
            dataList(listdata3,data3);
        })

        //소분류
        listView3.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->

            var intent = Intent();
            intent.putExtra("bioModel",listAdapte3.getItem(position))


            for ( i in 0..(listAdapte1.count-1)){

                var biotopeModel:BiotopeModel =listAdapte1.getItem(i);

                if(biotopeModel.chkSelect){
                    intent.putExtra("bioModelParent",listAdapte1.getItem(i))        ;
                }
            }

            setResult(RESULT_OK, intent);
            finish()
        })


    }

    //dataset 셋팅
    fun dataList(listdata:ArrayList<BiotopeModel>,data:Cursor) {

        while (data.moveToNext()){

            var model :BiotopeModel ;

            model = BiotopeModel(data.getString(0),data.getString(1),data.getString(2),tableName,false);

            listdata.add(model)
        }
    }
}




