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


class DlgLayersActivity : Activity() {

    private lateinit var context:Context;

    private var adapterData :ArrayList<LayerModel> = ArrayList<LayerModel>()

    private lateinit var apdater: DlgLayerAdapter;

    private lateinit var db: SQLiteDatabase

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
        listView.setOnItemClickListener { parent, view, position, id ->
            val data = adapterData.get(position)

            var intent = Intent();
            intent.putExtra("file_name", data.file_name);
            intent.putExtra("layer_name", data.layer_name);

            setResult(RESULT_OK, intent);
            finish()
        }


        loadData()

    }

    fun loadData() {

        // select
        val dataList:Array<String> = arrayOf("file_name", "layer_name");

        //대분류
        val data =  db.query("layers", dataList,null,null,null,null,"id",null);
        while (data.moveToNext()) {
            val layerModel = LayerModel(data.getString(0), data.getString(1));
            adapterData.add(layerModel)
        }

        apdater.notifyDataSetChanged()
    }
}




