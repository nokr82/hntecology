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


class DlgLayersActivity : Activity() {

    private lateinit var context: Context;

    private var adapterData: ArrayList<LayerModel> = ArrayList<LayerModel>()
    private var adapterData2: ArrayList<LayerModel> = ArrayList<LayerModel>()
    private lateinit var apdater: DlgLayerAdapter;
    private lateinit var apdater2: DlgLayerAdapter;
    private lateinit var db: SQLiteDatabase

    private var data: ArrayList<LayerModel> = ArrayList<LayerModel>()

    var type = 1
    private var grop_id: ArrayList<String> = ArrayList<String>()

    var check = false
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
        apdater2 = DlgLayerAdapter(context, R.layout.item_layer, adapterData2)
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

            if (type == 1) {
                for (j in 0..adapterData.size - 1) {
                    var data = adapterData.get(j)
                    data.is_checked = !data.is_checked
                }
                apdater.notifyDataSetChanged()
            } else {
                for (j in 0..adapterData2.size - 1) {
                    var data = adapterData2.get(j)
                    data.is_checked = !data.is_checked
                }
                apdater2.notifyDataSetChanged()
            }

        }

        lsmdTV.setOnClickListener {
            check = true
            type = 2
            listView.adapter = apdater2
            lsmdTV.setTextColor(Color.BLUE)
            bioTV.setTextColor(Color.BLACK)
            loadData(type)
        }
        bioTV.setOnClickListener {
            check = true
            type = 1
            listView.adapter = apdater
            lsmdTV.setTextColor(Color.BLACK)
            bioTV.setTextColor(Color.BLUE)
            loadData(type)
        }


        listView.setOnItemClickListener { adapterView, view, position, l ->
            if (type == 1) {
                var data = adapterData.get(position)

                if (!data.is_checked) {
                    data.is_checked = true
                    apdater.notifyDataSetChanged()
                } else {
                    data.is_checked = false
                    apdater.notifyDataSetChanged()
                }
            } else {
                var data = adapterData2.get(position)

                if (!data.is_checked) {
                    data.is_checked = true
                    apdater2.notifyDataSetChanged()
                } else {
                    data.is_checked = false
                    apdater2.notifyDataSetChanged()
                }
            }

        }

        dlgClick.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            } else {
                done()
            }

        }
        lsmdTV.setTextColor(Color.BLACK)
        bioTV.setTextColor(Color.BLUE)
        loadData(type)

    }


    fun loadData(type: Int) {

        // select
        val dataList: Array<String> = arrayOf("file_name", "layer_name", "min_scale", "max_scale", "type", "added", "grop_id");

        //대분류
        val data = db.query("layers", dataList, null, null, "file_name", null, "id", null);
        Log.d("첵", check.toString())

        if (!check) {
            adapterData.clear()
            adapterData2.clear()

            while (data.moveToNext()) {

                val layerModel = LayerModel(data.getString(0), data.getString(1), data.getInt(2), data.getInt(3), data.getString(4), data.getString(5), data.getString(6), false);

                Log.d("레이어목록", layerModel.toString())
                val zoom = intent.getFloatExtra("zoom", 0.0F)
                if (zoom > layerModel.min_scale && zoom < layerModel.max_scale) {
                    if (layerModel.type != "lsmd") {
                        adapterData.add(layerModel)
                    }
                    if (layerModel.type == "lsmd") {
                        adapterData2.add(layerModel)
                    }
                }
            }

        }

        if (intent.getSerializableExtra("layerFileName") != null) {
            var filename: ArrayList<String> = intent.getSerializableExtra("layerFileName") as ArrayList<String>
            val gropid: ArrayList<String> = intent.getSerializableExtra("layerGropId") as ArrayList<String>
            val type: ArrayList<String> = intent.getSerializableExtra("layerType") as ArrayList<String>
            Log.d("그뤕", gropid.toString())
            Log.d("그뤕", filename.toString())
            for (i in 0..gropid.size - 1) {
                for (j in 0..adapterData.size - 1) {
                    if (gropid.get(i) == adapterData.get(j).grop_id) {
                        if (type.get(i) == adapterData.get(j).type) {
                            adapterData.get(j).is_checked = true
                        }
                    }
                }
                for (j in 0..adapterData2.size - 1) {
                    if (gropid.get(i) == adapterData2.get(j).grop_id) {
                        if (type.get(i) == adapterData2.get(j).type) {
                            adapterData2.get(j).is_checked = true
                        }
                    }
                }
            }
        }

        Log.d("데이터", adapterData2.size.toString())
        data.close()
        Log.d("데이터", type.toString())

        apdater.notifyDataSetChanged()
        apdater2.notifyDataSetChanged()

    }

    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (android.Manifest.permission.WRITE_EXTERNAL_STORAGE == perm) {
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            } else if (android.Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
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
        for (i in 0..adapterData2.size - 1) {
            var checkData = adapterData2.get(i)
            var checked = checkData.is_checked

            // println("checked : " + checked)

            // println("checkData : ${checkData.type}")

            if (checked) {
                data.add(adapterData2.get(i))
                // println("-------added ${adapterData.get(i).added}")
            }

        }
        /* data.sortWith(object : Comparator<LayerModel> {
             override fun compare(p1: LayerModel, p2: LayerModel): Int {
                 if (p1.type == "lsmd" && p2.type == "lsmd") {
                     return 0
                 } else if (p1.type == "lsmd" && p2.type != "lsmd") {
                     return 1
                 }
                 return -1
             }
         })*/

        for (d in data) {
            println("checkData : ${d.type}")
        }

        var intent = Intent();
        intent.putExtra("data", data);
        setResult(RESULT_OK, intent);
        finish()
    }

}