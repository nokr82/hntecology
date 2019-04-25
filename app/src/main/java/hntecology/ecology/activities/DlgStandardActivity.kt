package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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


        listdata1 = java.util.ArrayList()

        listView1 = findViewById(R.id.listLV)

        listAdapter1 = DlgBirdsAdapter2(context, listdata1);

        listView1.adapter = listAdapter1

        addList()

        selectLL.setOnClickListener {
            var content: String = ""
            var code: ArrayList<String> = ArrayList<String>()

            if (listdata1 != null) {
                for (i in 0..listdata1.size - 1) {
                    if (listdata1.get(i).is_checked == true) {
                        code.add(listdata1.get(i).SIGN!!+":"+listdata1.get(i).CONTENT!!)
                    }
                }
            }

            if (code != null) {
                intent.putExtra("code", code)
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
