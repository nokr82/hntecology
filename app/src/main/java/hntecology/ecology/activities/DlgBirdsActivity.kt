package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.*
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_dlgcommon.*
import java.util.*
import hntecology.ecology.R
import hntecology.ecology.adapter.ComDlgAdapter
import hntecology.ecology.adapter.DlgBirdsAdapter
import hntecology.ecology.model.BiotopeModel
import kotlinx.android.synthetic.main.dlg_birds.*
import org.json.JSONObject
import kotlin.collections.ArrayList


class DlgBirdsActivity : Activity() {

    private lateinit var context:Context;

    private var adapterData :ArrayList<JSONObject> = ArrayList<JSONObject>()

    private lateinit var apdater: DlgBirdsAdapter;

    var tableName:String = ""
    var titleName:String=""
    var DlgHeight:Float=430F;
    var selectDlg:Int = 1

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_birds)

        context = this;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        val intent = getIntent()

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val dataList:Array<String> = arrayOf("name_kr","Family_name_kr","zoological");

        val data = db.query("birds", dataList, null, null, null, null, null, null);
        setDataList(data);

        apdater = DlgBirdsAdapter(context, R.layout.item_birds, adapterData)
        listLV.adapter = apdater

        apdater.notifyDataSetChanged()

        closeLL.setOnClickListener {
            finish()
        }

        listLV.setOnItemClickListener { parent, view, position, id ->

            println("position : " + position);

//            intent.putExtra("selectDlg",apdater.getItem(position))
//            setResult(RESULT_OK, intent);
//            finish()
        }

    }

    fun setDataList(data: Cursor){

        while (data.moveToNext()){

            var dataObj : JSONObject = JSONObject();
            dataObj.put("name_kr", data.getString(0))
            dataObj.put("family_name_kr", data.getString(1))
            dataObj.put("zoological", data.getString(2))

            adapterData.add(dataObj)
        }

    }

}





