package hntecology.ecology.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import hntecology.ecology.base.Utils
import hntecology.ecology.R
import kotlinx.android.synthetic.main.dig_input.*

class DlgInputActivity : Activity() {

    private lateinit var context:Context;
    var DlgHeight:Float=430F;
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dig_input)
        context = this;
        window.setLayout(Utils.dpToPx(800F).toInt(), Utils.dpToPx(DlgHeight).toInt());
        this.setFinishOnTouchOutside(true);

        okTV.setOnClickListener {
            var name = Utils.getString(inputET)
            val intent = Intent()
            intent.putExtra("name",name)
            setResult(RESULT_OK, intent);
            finish()
        }


    }


}





