package hntecology.ecology.activities

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.WindowManager
import hntecology.ecology.R
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_reptilia.*

class ReptiliaActivity : Activity() {

    val SET_WEATHER = 1;
    val SET_WIND = 2;
    val SET_WIND_DIRE = 3;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reptilia)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);

        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);


        // 조사 일자
        createdDateTV.text = Utils.todayStr()

        weatherTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "날씨")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 1);

            startActivityForResult(intent, SET_WEATHER);
        }

        windTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "바람")
            intent.putExtra("DlgHeight", 290f);
            intent.putExtra("selectDlg", 2);

            startActivityForResult(intent, SET_WIND);
        }

        windDireTV.setOnClickListener {

            val intent = Intent(this, DlgCommonSubActivity::class.java)
            intent.putExtra("title", "풍향")
            intent.putExtra("DlgHeight", 500f);
            intent.putExtra("selectDlg", 3);

            startActivityForResult(intent, SET_WIND_DIRE);
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SET_WEATHER -> {

                    weatherTV.text = data!!.getStringExtra("selectDlg")

                }
                SET_WIND -> {

                    windTV.text = data!!.getStringExtra("selectDlg")

                }
                SET_WIND_DIRE -> {

                    windDireTV.text = data!!.getStringExtra("selectDlg")

                }
            }
        }
    }

}
