package hntecology.ecology.activities

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.base.Utils

import kotlinx.android.synthetic.main.dlg_gps.*

class Dlg_gps : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_gps)

        window.setGravity(Gravity.CENTER);
        this.setFinishOnTouchOutside(true);
        window.setLayout(Utils.dpToPx(600f).toInt(), Utils.dpToPx(500f).toInt());

        //확인
        longitude_ok.setOnClickListener {

            val longitude:String = longitudeET.text.toString();
            val latitude:String = latitudeET.text.toString();
            if(longitude  == "" || latitude == ""){

                Toast.makeText(this,"위도 경도를 입력해 주세요",Toast.LENGTH_LONG).show()
            }
            var intent = Intent();
            intent.putExtra("longitude",longitude.toDouble());
            intent.putExtra("latitude",latitude.toDouble());

            setResult(RESULT_OK, intent);
            finish()

        }

        //취소
        longitude_cancle.setOnClickListener {

            finish()
        }
    }

}
