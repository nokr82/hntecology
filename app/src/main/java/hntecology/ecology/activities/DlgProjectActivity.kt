package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgProjectAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.Utils
import hntecology.ecology.model.Projects
import kotlinx.android.synthetic.main.activity_dlg_project.*

class DlgProjectActivity : Activity() {

    private lateinit var context: Context;

    var dbManager: DataBaseHelper? = null

    private var db: SQLiteDatabase? = null

    private lateinit var listView1: ListView

    private lateinit var listdata1: ArrayList<Projects>

    private lateinit var listAdapter : DlgProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dlg_project)

        context = this

        dbManager = DataBaseHelper(this)

        db = dbManager!!.createDataBase();

        window.setLayout(Utils.dpToPx(500F).toInt(), Utils.dpToPx(430F).toInt());
        this.setFinishOnTouchOutside(true)

        listView1 = findViewById(R.id.listLV)
        listdata1 =  ArrayList()
        listAdapter = DlgProjectAdapter(context,listdata1)

        val dataList: Array<String> = arrayOf("*");

        val data = db!!.query("Projects", dataList, null, null, null, null, null, null);

        setDataList(listdata1,data)

        listView1.adapter = listAdapter

        listView1.setOnItemClickListener { parent, view, position, id ->
            var data = listdata1.get(position)
            val intent = Intent();
            intent.putExtra("name",data.prj_name)
            setResult(Activity.RESULT_OK, intent);
            finish()
        }

        closeLL.setOnClickListener {
            finish()
        }

    }

    fun setDataList(listdata: java.util.ArrayList<Projects>,data: Cursor){

        while (data.moveToNext()){

            var model: Projects;

            model = Projects(data.getInt(0), data.getDouble(1),data.getDouble(2) ,data.getString(3), data.getInt(4));

            listdata.add(model)
        }

    }
}
