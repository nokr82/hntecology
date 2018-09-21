package hntecology.ecology

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import hntecology.ecology.base.Utils

class InsectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insect)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);


        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
