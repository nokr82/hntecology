package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgBirdsAdapter2
import hntecology.ecology.adapter.DlgStandardAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.RootActivity
import hntecology.ecology.base.Utils
import hntecology.ecology.model.EndangeredSelect
import kotlinx.android.synthetic.main.activity_dlg_standard.*

class DlgStandardActivity : Activity() {
    private lateinit var context: Context;

    private lateinit var listdata1: java.util.ArrayList<EndangeredSelect>

    private lateinit var listAdapter1: DlgBirdsAdapter2

    var chkData = false

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    var SPEC:String = ""
    var END:String = ""

    var type: String = ""
    var DlgHeight: Float = 620F

    var mam_name = ""
    private lateinit var listView1: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_standard)

        context = this;

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        dbManager = DataBaseHelper(context);
        db = dbManager!!.createDataBase();

        type = intent.getStringExtra("type")
        DlgHeight = intent.getFloatExtra("DlgHeight", 430F);


        Log.d("타타타타입",type)

        listdata1 = java.util.ArrayList()

        listView1 = findViewById(R.id.listLV)

        listAdapter1 = DlgBirdsAdapter2(context, listdata1);

        listView1.adapter = listAdapter1

        if (type == "mammal"){
            mam_name= intent.getStringExtra("mam_name")
            addList2()
        }else{
            addList()
        }

        selectLL.setOnClickListener {
            var content: String = ""
            var code: ArrayList<String> = ArrayList<String>()
            var r_code: ArrayList<String> = ArrayList<String>()
            if (listdata1 != null) {
                for (i in 0..listdata1.size - 1) {
                    if (listdata1.get(i).is_checked == true) {
                        code.add(listdata1.get(i).SIGN!!+":"+listdata1.get(i).CONTENT!!)
                        r_code.add(listdata1.get(i).SIGN!!)
                    }
                }
            }

            if (code != null) {
                intent.putExtra("code", code)
                intent.putExtra("r_code", r_code)
            }

            if (code.size > 0) {

                intent.putExtra("code", code)
                setResult(RESULT_OK, intent);

                finish()

            } else {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("빈값을 입력하시겠습니까 ?").setCancelable(false)
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            intent.putExtra("content", content)

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

        listView1.setOnItemClickListener { adapterView, view, position, l ->

            val data = listdata1.get(position)

            if (data.is_checked == false) {
                data.is_checked = true
                listAdapter1.notifyDataSetChanged()
            } else {
                data.is_checked = false
                listAdapter1.notifyDataSetChanged()
            }

        }



    }

    fun addList2(){

        if (listdata1 != null){
            listdata1.clear()
        }

        if (mam_name=="수달"){
            val item = EndangeredSelect("A", "동일 지역(격자)에서 사용하고 있는 보금자리가 발견되고 실체가 1회 이상 확인됨", false)
            val item2 = EndangeredSelect("B", "동일 지역(격자)에서 배설물(오래된 것, 신선한 것 2개 이상)이 2회 이상 발 견됨 ", false)
            val item3 = EndangeredSelect("C", "동일 지역(격자)에서 발자국이 2회 이상 발견됨", false)
            val item4 = EndangeredSelect("D", "동일 지역(격자)에서 실체가 2회 이상 발견됨", false)
            val item5 = EndangeredSelect("E", "하천에서 어린 새끼와 어미가 함께 활동하는 모습이 관찰되는 지역(번식활 동이 이루어지는 지역)", false)
            val item6 = EndangeredSelect("F", "장기적 사용 흔적(실체, 최근 이용 배설물 다수)이 있는 보금자리가 존재할 경우(하천경계부에서 20m 이내)", false)
            listdata1.add(item)
            listdata1.add(item2)
            listdata1.add(item3)
            listdata1.add(item4)
            listdata1.add(item5)
            listdata1.add(item6)

            listAdapter1.notifyDataSetChanged()
        }else if (mam_name=="담비"||mam_name=="검은담비"){
            val item = EndangeredSelect("A", "동일 지역(격자)에서 사용하고 있는 보금자리가 발견되고 실체가 1회 이상 확인됨", false)
            val item2 = EndangeredSelect("B", "동일 지역(격자)에서 배설물(오래된 것, 신선한 것 2개 이상)이 2회 이상 발 견됨 ", false)
            val item3 = EndangeredSelect("C", "동일 지역(격자)에서 발자국이 2회 이상 발견됨", false)
            val item4 = EndangeredSelect("D", "동일 지역(격자)에서 실체가 2회 이상 발견됨", false)
            listdata1.add(item)
            listdata1.add(item2)
            listdata1.add(item3)
            listdata1.add(item4)
        }else if (mam_name=="삵"){
            val item = EndangeredSelect("A", "동일 지역(격자)에서 사용하고 있는 보금자리가 발견되고 실체가 1회 이상 확인됨", false)
            val item2 = EndangeredSelect("B", "동일 지역(격자)에서 배설물(오래된 것, 신선한 것 2개 이상)이 2회 이상 발 견됨 ", false)
            val item3 = EndangeredSelect("C", "동일 지역(격자)에서 발자국이 2회 이상 발견됨", false)
            val item4 = EndangeredSelect("D", "동일 지역(격자)에서 실체가 2회 이상 발견됨", false)
            val item7 = EndangeredSelect("G", "산림이 우수한 지역의 5m 이내에 변색된 배설물(서식흔적)과 신선한 배설물 이 3곳 이상에서 관찰됨", false)
            listdata1.add(item)
            listdata1.add(item2)
            listdata1.add(item3)
            listdata1.add(item4)
            listdata1.add(item7)
        }else if (mam_name=="하늘다람쥐"){
            val item = EndangeredSelect("A", "동일 지역(격자)에서 사용하고 있는 보금자리가 발견되고 실체가 1회 이상 확인됨", false)
            val item2 = EndangeredSelect("B", "동일 지역(격자)에서 배설물(오래된 것, 신선한 것 2개 이상)이 2회 이상 발 견됨 ", false)
            val item3 = EndangeredSelect("C", "동일 지역(격자)에서 발자국이 2회 이상 발견됨", false)
            val item4 = EndangeredSelect("D", "동일 지역(격자)에서 실체가 2회 이상 발견됨", false)
            val item8 = EndangeredSelect("H", "실체가 1회 이상 확인되고, 주변에 다수의 배설물이 산재한 지역", false)
            listdata1.add(item)
            listdata1.add(item2)
            listdata1.add(item3)
            listdata1.add(item4)
            listdata1.add(item8)
        }





    }

    fun addList(){

        if (listdata1 != null){
            listdata1.clear()
        }

        val item = EndangeredSelect("a", "성조가 둥지 또는 둥지가 있을 것으로 예상되는 장소를 3회 이상 출입하는 것을 관찰", false)
        val item2 = EndangeredSelect("b", "성조가 포란 또는 새끼를 품고 있는 것을 관찰", false)
        val item3 = EndangeredSelect("c", "성조가 새끼의 배설물을 운반하고 있는 것을 관찰", false)
        val item4 = EndangeredSelect("d", "성조가 새끼에게 먹이를 운반 또는 경계하는 것을 관찰", false)
        val item5 = EndangeredSelect("e", "의상행동을 관찰", false)
        val item6 = EndangeredSelect("f", "교미행동을 관찰(겨울철새/통과철새는 제외)", false)
        val item7 = EndangeredSelect("g", "당해 또는 2년 이내에 이소한 것으로 추정되는 둥지를 관찰", false)
        val item8 = EndangeredSelect("h", "둥지 트는 행동을 관찰(둥지로 이용코자 땅 파는 행동 포함)", false)
        val item9 = EndangeredSelect("i", "성조가 둥지를 틀 때 쓰이는 재료를 운반하는 것을 관찰", false)
        val item10 = EndangeredSelect("j", "알이 있는 둥지를 관찰", false)
        val item11 = EndangeredSelect("k", "성조가 앉아 있는 둥지 근처에서 그 종의 알 껍질을 관찰", false)
        val item12 = EndangeredSelect("l", "새끼가 들어 있는 둥지를 관찰", false)
        val item13 = EndangeredSelect("m", "둥지 근처에서 거의 이동하지 못하는 새끼를 관찰", false)
        val item14 = EndangeredSelect("n", "새끼의 소리를 들음", false)

        listdata1.add(item)
        listdata1.add(item2)
        listdata1.add(item3)
        listdata1.add(item4)
        listdata1.add(item5)
        listdata1.add(item6)
        listdata1.add(item7)
        listdata1.add(item8)
        listdata1.add(item9)
        listdata1.add(item10)
        listdata1.add(item11)
        listdata1.add(item12)
        listdata1.add(item13)
        listdata1.add(item14)

        listAdapter1.notifyDataSetChanged()
    }
}
