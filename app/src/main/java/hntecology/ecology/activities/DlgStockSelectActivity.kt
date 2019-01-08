package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgStokeMapAdapter1
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.StockMapSelect
import kotlinx.android.synthetic.main.activity_dlg_stock_select.*

class DlgStockSelectActivity : Activity() {

    private lateinit var context: Context;

    var DlgHeight:Float=430F

    private lateinit var listView1: ListView

    private lateinit var listdata1 : java.util.ArrayList<StockMapSelect>

    private lateinit var listAdapter1: DlgStokeMapAdapter1;

    var titleName:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_stock_select)

        context = this;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        titleTV.setText(titleName)

        closeLL.setOnClickListener {
            finish()
        }

        listView1 = findViewById(R.id.listLV)

        listdata1 = java.util.ArrayList()

        listAdapter1 = DlgStokeMapAdapter1(context,listdata1)

        if (titleName == "입목존재코드 선택"){
            val selectitem1 = StockMapSelect("입목지"," : 산림의 정의에 따른 구분","1",false)
            val selectitem2 = StockMapSelect("무립목지"," : 미립목지, 제지","2",false)
            val selectitem3 = StockMapSelect("비산림"," : 산림이외의 지역과 산림으로 둘러싸여 있는 초지, 경작지, 하천 과수원, 기타","0",false)

            listdata1.add(selectitem1)
            listdata1.add(selectitem2)
            listdata1.add(selectitem3)
        }

        if (titleName == "임상존재코드 선택"){
            val selectitem1 = StockMapSelect("침엽수림"," : 침엽수의 수관 점유면적이 75% 이상","1",false)
            val selectitem2 = StockMapSelect("활엽수림"," : 활엽수의 수관 점유면적이 75% 이상","2",false)
            val selectitem3 = StockMapSelect("혼효림"," : 침활이 25% 이상, 75% 미만인 임분","3",false)
            val selectitem4 = StockMapSelect("죽림"," : 대나무림","4",false)
            val selectitem5 = StockMapSelect("무립목지/비산림"," : 산림의 구분이 무립목지/비산림인 경우","0",false)

            listdata1.add(selectitem1)
            listdata1.add(selectitem2)
            listdata1.add(selectitem3)
            listdata1.add(selectitem4)
            listdata1.add(selectitem5)
        }

        if (titleName == "임종코드 선택"){
            val selectitem1 = StockMapSelect("인공림"," : 조림이나 파종 등에 의해 인위적으로 형성된 산림","1",false)
            val selectitem2 = StockMapSelect("천연림"," : 인간의 간섭을 받지 않고 자연적으로 형성된 산림","2",false)
            val selectitem3 = StockMapSelect("무립목지/비산림"," : 산림의 구분이 무립목지/비산림인 경우","0",false)
            listdata1.add(selectitem1)
            listdata1.add(selectitem2)
            listdata1.add(selectitem3)
        }

        if (titleName == "경급코드 선택"){
            val selectitem1 = StockMapSelect("치수"," : 흉고직경 6cm 미만 입목의 수관점유면적 비율이 51% 이상","0",false)
            val selectitem2 = StockMapSelect("소경목"," : 흉고직경 6cm 이상 18cm 미만 입목의 수관점유면적 비율이 51% 이상","1",false)
            val selectitem3 = StockMapSelect("중경목"," : 흉고직경 18cm 이상 30cm 미만 입목의 수관점유면적 비율이 51% 이상","2",false)
            val selectitem4 = StockMapSelect("중경목"," : 흉고직경 30cm 이상 입목의 수관점유면적 비율이 51% 이상","3",false)

            listdata1.add(selectitem1)
            listdata1.add(selectitem2)
            listdata1.add(selectitem3)
            listdata1.add(selectitem4)
        }

        if (titleName == "영급코드 선택"){
            val selectitem1 = StockMapSelect("1영급"," : 1~10 년생의 수관점유 비율이 50% 이상","1",false)
            val selectitem2 = StockMapSelect("2영급"," : 11~20 년생의 수관점유 비율이 50% 이상","2",false)
            val selectitem3 = StockMapSelect("3영급"," : 21~30 년생의 수관점유 비율이 50% 이상","3",false)
            val selectitem4 = StockMapSelect("4영급"," : 31~40 년생의 수관점유 비율이 50% 이상","4",false)
            val selectitem5 = StockMapSelect("5영급"," : 41~50 년생의 수관점유 비율이 50% 이상","5",false)
            val selectitem6 = StockMapSelect("6영급"," : 51~60 년생의 수관점유 비율이 50% 이상(25,000 임상도에서는 51 년생 입목의 수관점유비율이 50% 이상인 임분의 의미로 사용)","6",false)
            val selectitem7 = StockMapSelect("7영급"," : 61~70 년생의 수관점유 비율이 50% 이상","7",false)
            val selectitem8 = StockMapSelect("8영급"," : 71~80 년생의 수관점유 비율이 50% 이상","8",false)
            val selectitem9 = StockMapSelect("9영급"," : 81 년생의 수관점유 비율이 50% 이상","9",false)

            listdata1.add(selectitem1)
            listdata1.add(selectitem2)
            listdata1.add(selectitem3)
            listdata1.add(selectitem4)
            listdata1.add(selectitem5)
            listdata1.add(selectitem6)
            listdata1.add(selectitem7)
            listdata1.add(selectitem8)
            listdata1.add(selectitem9)
        }

        if (titleName == "밀도 선택"){
            val selectitem1 = StockMapSelect("소"," : 교목의 수관점유 면적이 50% 이하인 임분","A",false)
            val selectitem2 = StockMapSelect("2영급"," : 교목의 수관점유 면적이 51%~70% 이하인 임분","B",false)
            val selectitem3 = StockMapSelect("3영급"," : 교목의 수관점유 면적이 71% 이상인 임분","C",false)

            listdata1.add(selectitem1)
            listdata1.add(selectitem2)
            listdata1.add(selectitem3)
        }



        listView1.adapter = listAdapter1

        listView1.setOnItemClickListener { adapterView, view, position, l ->
            var data = listdata1.get(position)

            intent.putExtra("CODE", data.code)
            intent.putExtra("Title",data.Title)

            setResult(RESULT_OK, intent);

            finish()
        }




    }
}
