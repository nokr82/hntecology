package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_dlgcommon.*
import java.util.*
import hntecology.ecology.R
import hntecology.ecology.adapter.ComDlgAdapter


class DlgCommonSubActivity : Activity() {

    private lateinit var context:Context;


    private lateinit var listView1: ListView


    private lateinit var listdata1 :ArrayList<String>

    private lateinit var listAdapte1: ComDlgAdapter;

    val dataBaseHelper = DataBaseHelper(this);
    val db = dataBaseHelper.createDataBase()
    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F;
    var selectDlg:Int = 1

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlgcommon_sub)

        context = applicationContext;

        val intent = getIntent()


        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);
        selectDlg = intent.getIntExtra("selectDlg",1)

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        dlgTitle.setText(titleName)

        listView1 = findViewById(R.id.recipe_list_view1)

        //대분류 중분류 소분류

        listdata1 = ArrayList()


        when (selectDlg) {

            1 -> {

                listdata1.add("맑음")
                listdata1.add("흐림")
                listdata1.add("안개")
                listdata1.add("비")
            }

            2 -> {

                listdata1.add("강")
                listdata1.add("중")
                listdata1.add("약")
                listdata1.add("무")
            }

            3 -> {


                listdata1.add("N")
                listdata1.add("NE")
                listdata1.add("E")
                listdata1.add("SE")
                listdata1.add("S")
                listdata1.add("SW")
                listdata1.add("W")
                listdata1.add("NW")
            }
        }

        //대분류 데이터 셋팅


        listAdapte1 = ComDlgAdapter(context, listdata1);

        listView1.adapter = listAdapte1

        listAdapte1.notifyDataSetChanged()

        //대분류
        listView1.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->


            var intent = Intent();

            intent.putExtra("selectDlg",listAdapte1.getItem(position))
            setResult(RESULT_OK, intent);
            finish()
        })


    }

}





