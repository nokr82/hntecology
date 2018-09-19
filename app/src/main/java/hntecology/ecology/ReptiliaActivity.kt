package hntecology.ecology

import android.os.Bundle
import android.app.Activity
import android.view.Gravity
import android.view.WindowManager
import hntecology.ecology.base.Utils

import kotlinx.android.synthetic.main.activity_reptilia.*

class ReptiliaActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reptilia)

        window.setGravity(Gravity.RIGHT);
        this.setFinishOnTouchOutside(true);


        window.setLayout(Utils.dpToPx(700f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT);
    }

}
