package hntecology.ecology.activities

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import hntecology.ecology.R
import hntecology.ecology.base.Utils

class MammaliaActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mammalia)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);


        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
