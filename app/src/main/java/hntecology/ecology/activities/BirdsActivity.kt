package hntecology.ecology.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import hntecology.ecology.R
import hntecology.ecology.adapter.DlgBirdsAdapter
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.OpenAlertDialog
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_birds.*

class BirdsActivity : Activity() {

    lateinit var openAlertDialog:OpenAlertDialog ;

    lateinit var context:Context;

    val SET_DATA1 = 1;
    val SET_DATA2 = 2;
    val SET_DATA3 = 3;
    val SET_BIRDS = 4;

    var userName = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birds)

        this.context = this;

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);

        var today = Utils.todayStr();
        var time = Utils.timeStr();

        invDtTV.text = today;
        timeTV.text = time;

        userName = PrefUtils.getStringPreference(context,"name");
        invPersonTV.text = userName;

        val dataBaseHelper = DataBaseHelper(context);
        val db = dataBaseHelper.createDataBase();

        val num = dataBaseHelper.birdsNextNum();
        numTV.text = num.toString()

        btn1.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title","날씨")
            intent.putExtra("DlgHeight",290f);
            intent.putExtra("selectDlg",1);

            startActivityForResult(intent, SET_DATA1);

        }

        btn2.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title","바람")
            intent.putExtra("DlgHeight",290f);
            intent.putExtra("selectDlg",2);

            startActivityForResult(intent, SET_DATA2);

        }

        btn3.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title","풍향")
            intent.putExtra("DlgHeight",500f);
            intent.putExtra("selectDlg",3);

            startActivityForResult(intent, SET_DATA3);

        }

        birdsTV.setOnClickListener {

            val intent = Intent(context, DlgBirdsActivity::class.java)
            startActivityForResult(intent, SET_BIRDS);

        }

        // 이용 층위
        useLayerTV.setOnClickListener {

            var listItems: ArrayList<String> = ArrayList();
            listItems.add("교목층");
            listItems.add("아교목층");
            listItems.add("관목층");
            listItems.add("초본층");
            listItems.add("수면");
            listItems.add("수변");
            listItems.add("취소");

            alert(listItems, "이용 층위 선택", useLayerTV);

        }

    }

    fun alert(ListItems: ArrayList<String>, title:String, textView: TextView) {

        val items = Array<CharSequence>(ListItems.size, { i -> ListItems.get(i) })

        var size = ListItems.size

        var builder: AlertDialog.Builder = AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setItems(items, DialogInterface.OnClickListener { dialogInterface, i ->

            if(ListItems.get(i) != "취소") {
                textView.text = ListItems.get(i)
            }

        })

        builder.show();
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_DATA1 -> {

                    btn1.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_DATA2 -> {

                    btn2.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_DATA3 -> {

                    btn3.setText(data!!.getStringExtra("selectDlg"))

                };
                SET_BIRDS -> {

                    var name = data!!.getStringExtra("name");
                    var family_name = data!!.getStringExtra("family_name");
                    var zoological = data!!.getStringExtra("zoological");

                    birdsTV.text = name
                    familyNameTV.text = family_name
                    zoologicalTV.text = zoological

                };
            }
        }
    }
}
