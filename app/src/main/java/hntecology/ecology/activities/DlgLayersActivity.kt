package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgLayerAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.LayerModel
import kotlinx.android.synthetic.main.dlg_layers.*
import kotlinx.android.synthetic.main.dlg_layers.view.*


class DlgLayersActivity : Activity() {

    private lateinit var context:Context;

    private var adapterData :ArrayList<LayerModel> = ArrayList<LayerModel>()

    private lateinit var apdater: DlgLayerAdapter;

    private lateinit var db: SQLiteDatabase

    private var data :ArrayList<LayerModel> = ArrayList<LayerModel>()

    private var grop_id:ArrayList<String> = ArrayList<String>()

    val READ_EXTERNAL_STORAGE = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_layers)

        context = this

        val dataBaseHelper = DataBaseHelper(this);
        db = dataBaseHelper.createDataBase()

        window.setLayout(Utils.dpToPx(1000f).toInt(), Utils.dpToPx(430f).toInt());
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
        allclickTV.setOnClickListener {

            for(j in 0..adapterData.size-1){
                var data  = adapterData.get(j)
                data.is_checked = !data.is_checked
            }
            apdater.notifyDataSetChanged()
        }

        lsmdTV.setOnClickListener {
            lsmdTV.setTextColor(Color.BLUE)
            bioTV.setTextColor(Color.BLACK)
            loadData()
        }
        bioTV.setOnClickListener {
            lsmdTV.setTextColor(Color.BLACK)
            bioTV.setTextColor(Color.BLUE)
            loadData()
        }

        bioTV.callOnClick()

        listView.setOnItemClickListener { adapterView, view, position, l ->
            var data  = adapterData.get(position)

            if(!data.is_checked){
                data.is_checked = true
                apdater.notifyDataSetChanged()
            } else {
                data.is_checked = false
                apdater.notifyDataSetChanged()
            }
        }

        dlgClick.setOnClickListener {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            } else {
                done()
            }

        }



    }



    fun loadData() {

        // select
        val dataList:Array<String> = arrayOf("file_name", "layer_name","min_scale","max_scale","type","added","grop_id");

        //대분류
        val data =  db.query("layers", dataList,null,null,null,null,"id",null);
        adapterData.clear()
        while (data.moveToNext()) {
            val layerModel = LayerModel(data.getString(0), data.getString(1), data.getInt(2),data.getInt(3),data.getString(4),data.getString(5),data.getString(6),false);

            val zoom = intent.getFloatExtra("zoom", 0.0F)

            if(zoom > layerModel.min_scale && zoom < layerModel.max_scale) {
                adapterData.add(layerModel)

                // println("file_name ${layerModel.file_name}")
            }
            Log.d("데이터", adapterData.size.toString())
            if(intent.getSerializableExtra("layerFileName") != null){
                var filename: ArrayList<String> = intent.getSerializableExtra("layerFileName") as ArrayList<String>
                val gropid:ArrayList<String> = intent.getSerializableExtra("layerGropId") as ArrayList<String>

                for(i in 0..gropid.size-1){
                    for(j in 0..adapterData.size-1){
                        if(gropid.get(i) == adapterData.get(j).grop_id){
                            adapterData.get(j).is_checked = true
                        }
                    }
                }
            }

        }

        data.close()

        apdater.notifyDataSetChanged()

    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if(android.Manifest.permission.WRITE_EXTERNAL_STORAGE == perm){
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            } else if(android.Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                done()
            }
        }
    }

    private fun done() {
        if (data != null) {
            data.clear()
        }

        for (i in 0..adapterData.size - 1) {
            var checkData = adapterData.get(i)
            var checked = checkData.is_checked

            // println("checked : " + checked)

            // println("checkData : ${checkData.type}")

            if (checked) {
                data.add(adapterData.get(i))
                // println("-------added ${adapterData.get(i).added}")
            }

        }

        data.sortWith(object: Comparator<LayerModel>{
            override fun compare(p1: LayerModel, p2: LayerModel): Int {
                if(p1.type == "lsmd" && p2.type == "lsmd") {
                    return 0
                } else if(p1.type == "lsmd" && p2.type != "lsmd") {
                    return 1
                }
                return -1
            }
        })

        for (d in data) {
            println("checkData : ${d.type}")
        }

        var intent = Intent();
        intent.putExtra("data", data);
        setResult(RESULT_OK, intent);
        finish()
    }

}