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

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=480F;
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

            100 -> {

                listdata1.add("자연형")
                listdata1.add("직강화")
                listdata1.add("복합형")
                listdata1.add("댐/보/교각")
                listdata1.add("하천공사")

            }

            500 -> {
                listdata1.add("5")
                listdata1.add("10")
                listdata1.add("15")
                listdata1.add("20")
                listdata1.add("25")
                listdata1.add("30")
                listdata1.add("35")
                listdata1.add("40")
                listdata1.add("45")
                listdata1.add("50")
                listdata1.add("55")
                listdata1.add("60")
                listdata1.add("65")
                listdata1.add("70")
                listdata1.add("75")
                listdata1.add("80")
                listdata1.add("85")
                listdata1.add("90")
                listdata1.add("95")
                listdata1.add("100")
            }

            1000 -> {
                insertTime()
            }

            1001 -> {
                insertTime()
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

    fun insertTime(){
        listdata1.add("00")
        listdata1.add("01")
        listdata1.add("02")
        listdata1.add("03")
        listdata1.add("04")
        listdata1.add("05")
        listdata1.add("06")
        listdata1.add("07")
        listdata1.add("08")
        listdata1.add("09")
        listdata1.add("10")
        listdata1.add("11")
        listdata1.add("12")
        listdata1.add("13")
        listdata1.add("14")
        listdata1.add("15")
        listdata1.add("16")
        listdata1.add("17")
        listdata1.add("18")
        listdata1.add("19")
        listdata1.add("20")
        listdata1.add("21")
        listdata1.add("22")
        listdata1.add("23")
    }
}





