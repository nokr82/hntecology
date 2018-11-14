package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgBirdsAdapter
import kotlinx.android.synthetic.main.activity_dlgvegetation.*
import kotlinx.android.synthetic.main.dlg_birds.*
import org.json.JSONObject
import kotlin.collections.ArrayList

class DlgBirdsActivity : Activity() {

    private lateinit var context:Context;

    private var adapterData :ArrayList<JSONObject> = ArrayList<JSONObject>()

    private var copyadapterData:ArrayList<JSONObject> = ArrayList<JSONObject>()

    private lateinit var apdater: DlgBirdsAdapter;

    var DlgHeight:Float=430F;

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_birds)

        context = this;

        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        val intent = getIntent()

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        val dataList:Array<String> = arrayOf("name_kr","Family_name_kr","zoological");

        val data = db.query("birds", dataList, null, null, null, null, null, null);
        setDataList(data);

        copyadapterData.addAll(adapterData)

        apdater = DlgBirdsAdapter(context, R.layout.item_birds, adapterData)
        listLV.adapter = apdater

        apdater.notifyDataSetChanged()

        closeLL.setOnClickListener {
            finish()
        }

        listLV.setOnItemClickListener { parent, view, position, id ->

            var data = adapterData.get(position)
            var name = Utils.getString(data, "name_kr");
            var family_name = Utils.getString(data, "family_name_kr");
            var zoological = Utils.getString(data, "zoological");

            val intent = Intent();
            intent.putExtra("name",name)
            intent.putExtra("family_name",family_name)
            intent.putExtra("zoological",zoological)
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

    fun setDataList(data: Cursor){

        while (data.moveToNext()){

            var dataObj : JSONObject = JSONObject();
            dataObj.put("name_kr", data.getString(0))
            dataObj.put("family_name_kr", data.getString(1))
            dataObj.put("zoological", data.getString(2))

            adapterData.add(dataObj)
        }

    }


    fun search(charText: String){
        adapterData.clear()

        if(charText.length == 0){

            adapterData.addAll(copyadapterData)

        }else {

            var names:ArrayList<String> = ArrayList<String>()

            for (i in 0..copyadapterData.size-1){

                val name =  Utils.getString(copyadapterData.get(i), "name_kr");

                names.add(name)

            }

            for(i in 0..names.size-1){

                if(names.get(i).toLowerCase().contains(charText)){
                    adapterData.add(copyadapterData.get(i))
                }

            }

        }

        apdater.notifyDataSetChanged()

    }
}





