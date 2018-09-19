package hntecology.ecology

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import hntecology.ecology.R
import hntecology.ecology.base.OpenAlertDialog
import hntecology.ecology.base.Utils
import hntecology.ecology.model.BiotopeModel
import kotlinx.android.synthetic.main.activity_biotope.*
import kotlinx.android.synthetic.main.activity_birds.*

class BirdsActivity : Activity() {


    lateinit var openAlertDialog:OpenAlertDialog ;

    val SET_DATA1 = 1;
    val SET_DATA2 = 2;
    val SET_DATA3 = 3;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_birds)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);


        btn1.setOnClickListener {

            val intent = Intent(this,DlgCommonSubActivity::class.java)
            intent.putExtra("title","날씨")
            intent.putExtra("DlgHeight",290f);
            intent.putExtra("selectDlg",1);

            startActivityForResult(intent, SET_DATA1);

        }

        btn2.setOnClickListener {

            val intent = Intent(this,DlgCommonSubActivity::class.java)
            intent.putExtra("title","바람")
            intent.putExtra("DlgHeight",290f);
            intent.putExtra("selectDlg",2);

            startActivityForResult(intent, SET_DATA2);

        }

        btn3.setOnClickListener {

            val intent = Intent(this,DlgCommonSubActivity::class.java)
            intent.putExtra("title","풍향")
            intent.putExtra("DlgHeight",500f);
            intent.putExtra("selectDlg",3);

            startActivityForResult(intent, SET_DATA3);

        }


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
            }
        }
    }
}
