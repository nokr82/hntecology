package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgLayerAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.LayerModel
import kotlinx.android.synthetic.main.dlg_layers.*
import org.json.JSONObject


class DlgLayersActivity : Activity() {

    private lateinit var context:Context;

    private var adapterData :ArrayList<LayerModel> = ArrayList<LayerModel>()

    private lateinit var apdater: DlgLayerAdapter;

    private lateinit var db: SQLiteDatabase

    private var data :ArrayList<LayerModel> = ArrayList<LayerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_layers)

        context = this

        val dataBaseHelper = DataBaseHelper(this);
        db = dataBaseHelper.createDataBase()

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(430f).toInt());
        this.setFinishOnTouchOutside(true);

        apdater = DlgLayerAdapter(context, R.layout.item_layer, adapterData)
        listView.adapter = apdater


//        listView.setOnItemClickListener { parent, view, position, id ->
//            val data = adapterData.get(position)
//
//            var intent = Intent();
//            intent.putExtra("file_name", data.file_name);
//            intent.putExtra("layer_name", data.layer_name);
//
//            setResult(RESULT_OK, intent);
//            finish()
//        }

        dlgClick.setOnClickListener {
            for(i in 0 ..adapterData.size-1){
                var checkData = adapterData.get(i)
                var checked = checkData.is_checked

                println("checked : " + checked)

                if(checked) {
                    data.add(adapterData.get(i))
                }

            }

            var intent = Intent();
            intent.putExtra("data", data);
            setResult(RESULT_OK, intent);
            finish()
        }

        loadData()

    }

    fun loadData() {

        // select
        val dataList:Array<String> = arrayOf("file_name", "layer_name","min_scale","max_scale","type");

        //대분류
        val data =  db.query("layers", dataList,null,null,null,null,"id",null);
        while (data.moveToNext()) {
            val layerModel = LayerModel(data.getString(0), data.getString(1), data.getInt(2),data.getInt(3),data.getString(4),false);

            val zoom = intent.getFloatExtra("zoom", 0.0F)

            if(zoom > layerModel.min_scale && zoom < layerModel.max_scale) {
                adapterData.add(layerModel)
                println("file_name ${layerModel.file_name}")
            }

        }

        apdater.notifyDataSetChanged()

    }

}