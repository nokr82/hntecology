package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextWatcher
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Vegetation
import java.util.ArrayList
import android.text.Editable
import hntecology.ecology.adapter.DlgVegetationAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Jaso
import kotlinx.android.synthetic.main.activity_dlg_stock_map.*


class DlgVegetationActivity : Activity() {

        private lateinit var context: Context;


        var tableName:String = ""
        var titleName:String=""
        var DlgHeight:Float=430F

        var chkData = false
        private var copyadapterData :ArrayList<Vegetation> = ArrayList<Vegetation>()
        private lateinit var listView1: ListView
        private lateinit var listdata1 : java.util.ArrayList<Vegetation>
        private lateinit var listAdapter1: DlgVegetationAdapter;

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_dlg_stock_map)

            context = this;

            val dataBaseHelper = DataBaseHelper(context);
            val db = dataBaseHelper.createDataBase();



            window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
            this.setFinishOnTouchOutside(true);

            listView1 = findViewById(R.id.listLV)
            listdata1 = java.util.ArrayList()
            listAdapter1 = DlgVegetationAdapter(context,listdata1)
            listView1.adapter = listAdapter1
            val dataList:Array<String> = arrayOf("*");
            val data = db!!.query("Vegetation", dataList, null, null, null, null, "CORRESPONDINGNAME", null);
            setDataList(listdata1,data);
            copyadapterData.addAll(listdata1)

            listView1.setOnItemClickListener { parent, view, position, id ->

                var data = listdata1.get(position)
                var name = data.CORRESPONDINGNAME
                var code = data.SIGN
                var cate = data.CATEGORY
                if (cate == "식재림"){
                    name += cate
                }


                val intent = Intent();
                intent.putExtra("name",name)
                intent.putExtra("code",code.toString())
                setResult(RESULT_OK, intent);
                finish()

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

        fun setDataList(listdata: java.util.ArrayList<Vegetation>, data: Cursor){

            while (data.moveToNext()){

                var model: Vegetation;

                model = Vegetation(data.getInt(0), data.getString(1), data.getInt(2), data.getString(3), data.getString(4), false)


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

                    val name =  Utils.getString(copyadapterData.get(i).CORRESPONDINGNAME, copyadapterData.get(i).CORRESPONDINGNAME);

                    names.add(name)

                }

                for(i in 0..names.size-1){

                    if (Jaso.startsWith(names.get(i), charText)
                            || names.get(i).toLowerCase().contains(charText)) {
                        listdata1.add(copyadapterData.get(i))
                    }

                }

            }

            listAdapter1.notifyDataSetChanged()

        }

}
