package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgStokeMapAdapter1
import hntecology.ecology.adapter.DlgStokeMapAdapter2
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Jaso
import hntecology.ecology.base.Utils
import hntecology.ecology.model.StockMapSelect
import hntecology.ecology.model.Vegetation
import kotlinx.android.synthetic.main.activity_dlg_coll.*
import kotlinx.android.synthetic.main.activity_dlg_stock_map.*

class DlgCollActivity : Activity() {

    private lateinit var context: Context;

    var titleName:String=""
    var DlgHeight:Float=430F

    var chkData = false

    private lateinit var listView1: ListView
    private lateinit var listView2: ListView

    private lateinit var listdata1 : java.util.ArrayList<StockMapSelect>
    private lateinit var listdata2 : java.util.ArrayList<StockMapSelect>

    private lateinit var listAdapter1: DlgStokeMapAdapter1;
    private lateinit var listAdapter2: DlgStokeMapAdapter1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_coll)

        context = this;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        titleName = intent.getStringExtra("title")
        DlgHeight = intent.getFloatExtra("DlgHeight",430F);

        col_dlgTitleTV.setText(titleName)

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        listView1 = findViewById(R.id.col_list_view1)
        listView2 = findViewById(R.id.col_list_view2)

        listdata1 = java.util.ArrayList()
        listdata2 = java.util.ArrayList()

        listAdapter1 = DlgStokeMapAdapter1(context,listdata1)
        listAdapter2 = DlgStokeMapAdapter1(context,listdata2)

        val item = StockMapSelect("1","","1",false)
        val item2 = StockMapSelect("2","","2",false)
        val item3 = StockMapSelect("3","","3",false)
        val item4 = StockMapSelect("4","","4",false)
        val item5 = StockMapSelect("5","","5",false)
        val item6 = StockMapSelect("6","","6",false)
        val item7 = StockMapSelect("7","","7",false)
        val item8 = StockMapSelect("8","","8",false)
        val item9 = StockMapSelect("9","","9",false)

        if (titleName == "투망"){
            listdata1.add(item)
            listdata1.add(item2)
            listdata1.add(item3)
            listdata1.add(item4)
            listdata1.add(item5)
            listdata1.add(item6)
            listdata1.add(item7)
            listdata1.add(item8)
            listdata1.add(item9)
        } else {
            listdata1.add(item)
            listdata1.add(item2)
            listdata1.add(item3)
            listdata1.add(item4)
        }

        listView1.adapter = listAdapter1
        listView2.adapter = listAdapter2




        listView1.setOnItemClickListener { adapterView, view, position, l ->

            if(listdata2 != null){
                listdata2.clear()
            }

            listAdapter1.setItemSelect(position)

            for(i in 0..listdata1.size-1){
                listdata1.get(i).chkSelect = false
            }

            listAdapter2.clearItem()

            var data = listdata1.get(position)

            if(data.chkSelect == false){
                data.chkSelect = true
                listAdapter1.notifyDataSetChanged()
            }else {
                data.chkSelect = false
                listAdapter1.notifyDataSetChanged()
            }

            val colitem = StockMapSelect("1","","1",false)
            val colitem2 = StockMapSelect("2","","2",false)
            val colitem3 = StockMapSelect("3","","3",false)
            val colitem4 = StockMapSelect("4","","4",false)
            val colitem5 = StockMapSelect("5","","5",false)
            val colitem6 = StockMapSelect("6","","6",false)
            val colitem7 = StockMapSelect("7","","7",false)
            val colitem8 = StockMapSelect("8","","8",false)
            val colitem9 = StockMapSelect("9","","9",false)

            if (titleName == "투망"){
                listdata2.add(colitem)
                listdata2.add(colitem2)
                listdata2.add(colitem3)
                listdata2.add(colitem4)
                listdata2.add(colitem5)
                listdata2.add(colitem6)
                listdata2.add(colitem7)
                listdata2.add(colitem8)
                listdata2.add(colitem9)
            } else {
                listdata2.add(colitem)
                listdata2.add(colitem2)
                listdata2.add(colitem3)
                listdata2.add(colitem4)
            }
            col_dlg_probars.visibility= View.VISIBLE
            listAdapter2.notifyDataSetChanged()
            col_dlg_probars.visibility= View.GONE

        }

        listView2.setOnItemClickListener { adapterView, view, position, l ->

            var data = listdata2.get(position)

            var num = ""

            for (i in 0 until listdata1.size){
                if (listdata1.get(i).chkSelect == true) {
                    num = listdata1.get(i).Title!!
                }
            }

            intent.putExtra("num", num)
            intent.putExtra("num2", data.Title)

            setResult(RESULT_OK, intent);

            finish()

        }

    }

}
