package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgZoobenthosAdapter
import hntecology.ecology.base.Utils
import hntecology.ecology.model.ZoobenthosSelect
import kotlinx.android.synthetic.main.activity_dlg_zoobenthos.*
import java.util.ArrayList

class DlgZoobenthosActivity : Activity() {

    private lateinit var context: Context;

    var type: String = ""
    var titleName: String = ""
    var DlgHeight: Float = 430F

    private lateinit var listView1: ListView

    private lateinit var listdata1: java.util.ArrayList<ZoobenthosSelect>

    private lateinit var listAdapter: DlgZoobenthosAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_zoobenthos)

        context = this;

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        type = intent.getStringExtra("type");
        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight", 430F);

        titleTV.setText(titleName)

        val habitem = ZoobenthosSelect("상류",false)
        val habitem2 = ZoobenthosSelect("중류",false)
        val habitem3 = ZoobenthosSelect("하류",false)
        val habitem4 = ZoobenthosSelect("지류1",false)
        val habitem5 = ZoobenthosSelect("지류2",false)
        val habitem6 = ZoobenthosSelect("연못",false)
        val habitem7 = ZoobenthosSelect("둠벙",false)
        val habitem8 = ZoobenthosSelect("배후습지",false)
        val habitem9 = ZoobenthosSelect("소택지",false)

        val bankdata = ZoobenthosSelect("자연제방",false)
        val bankdata2 = ZoobenthosSelect("콘크리트",false)
        val bankdata3 = ZoobenthosSelect("석축",false)
        val bankdata4 = ZoobenthosSelect("돌망태",false)
        val bankdata5 = ZoobenthosSelect("흙",false)

        val basdata = ZoobenthosSelect("자연식생",false)
        val basdata2 = ZoobenthosSelect("농경지",false)
        val basdata3 = ZoobenthosSelect("거주지",false)
        val basdata4 = ZoobenthosSelect("상업지역",false)
        val basdata5 = ZoobenthosSelect("공업지역",false)
        val basdata6 = ZoobenthosSelect("유원지",false)
        val basdata7 = ZoobenthosSelect("도로",false)

        val distdata = ZoobenthosSelect("산업폐수",false)
        val distdata2 = ZoobenthosSelect("농.축산폐수",false)
        val distdata3 = ZoobenthosSelect("공사",false)
        val distdata4 = ZoobenthosSelect("행락",false)
        val distdata5 = ZoobenthosSelect("준설",false)
        val distdata6 = ZoobenthosSelect("선박",false)
        val distdata7 = ZoobenthosSelect("어로",false)
        val distdata8 = ZoobenthosSelect("홍수",false)
        val distdata9 = ZoobenthosSelect("매립",false)
        val distdata10 = ZoobenthosSelect("인공수변",false)
        val distdata11 = ZoobenthosSelect("생활하수",false)
        val distdata12 = ZoobenthosSelect("없음",false)

        val alladd = ZoobenthosSelect("기타",false)

        listdata1 = ArrayList()

        if(type == "HAB_TY_ETC"){
            listdata1.add(habitem)
            listdata1.add(habitem2)
            listdata1.add(habitem3)
            listdata1.add(habitem4)
            listdata1.add(habitem5)
            listdata1.add(habitem6)
            listdata1.add(habitem7)
            listdata1.add(habitem8)
            listdata1.add(habitem9)
            listdata1.add(alladd)
        }

        if(type == "BANK_L" || type == "BANK_R"){
            listdata1.add(bankdata)
            listdata1.add(bankdata2)
            listdata1.add(bankdata3)
            listdata1.add(bankdata4)
            listdata1.add(bankdata5)
            listdata1.add(alladd)
        }

        if(type == "BAS_L" || type == "BAS_R"){
            listdata1.add(basdata)
            listdata1.add(basdata2)
            listdata1.add(basdata3)
            listdata1.add(basdata4)
            listdata1.add(basdata5)
            listdata1.add(basdata6)
            listdata1.add(basdata7)
            listdata1.add(alladd)
        }

        if(type == "DIST_CAU"){
            listdata1.add(distdata)
            listdata1.add(distdata2)
            listdata1.add(distdata3)
            listdata1.add(distdata4)
            listdata1.add(distdata5)
            listdata1.add(distdata6)
            listdata1.add(distdata7)
            listdata1.add(distdata8)
            listdata1.add(distdata9)
            listdata1.add(distdata10)
            listdata1.add(distdata11)
            listdata1.add(distdata12)
            listdata1.add(alladd)
        }

        listView1 = findViewById(R.id.list_view1)

        listAdapter = DlgZoobenthosAdapter(context, R.layout.item_layer ,listdata1);

        listView1.adapter = listAdapter

        selectLL.setOnClickListener {

            var data:ArrayList<ZoobenthosSelect> = ArrayList<ZoobenthosSelect>()

            for(i in 0..listdata1.size-1){
                if(listdata1.get(i).chkSelect == true){
                    data.add(listdata1.get(i))
                }
            }

            var intent = Intent();
            intent.putExtra("data", data)
            setResult(RESULT_OK, intent);

            finish()
        }


        listView1.setOnItemClickListener { adapterView, view, position, l ->

            val data = listAdapter.getItem(position)

            if(data.title == "기타") {
                var intent = Intent();
                intent.putExtra("title", listAdapter.getItem(position).title)
                setResult(RESULT_OK, intent);
                finish()
            }

            if(data.chkSelect == false){
                data.chkSelect = true
                listAdapter.notifyDataSetChanged()
            }else {
                data.chkSelect = false
                listAdapter.notifyDataSetChanged()
            }

        }

    }
}
