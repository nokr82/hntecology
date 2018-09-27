package hntecology.ecology.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setGravity(Gravity.CENTER);
        window.setLayout(Utils.dpToPx(600f).toInt(), Utils.dpToPx(400f).toInt());


        doneTV.setOnClickListener {

            val getName:String =  nickNameET.text.toString();
            if(getName == "" || getName == null){

                Toast.makeText(this,"이름을 입력해 주세요",Toast.LENGTH_LONG).show()

            } else {

                val intent: Intent = Intent(this, MainActivity::class.java);

                PrefUtils.setPreference(this, "name", getName);

                startActivity(intent)
                finish()
            }


        }
    }
}
