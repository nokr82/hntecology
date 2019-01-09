package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.model.GpsSet
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : Activity() {

    private lateinit var context: Context

    //Android 6.0 : Access to mac address from WifiManager forbidden
    private val marshmallowMacAddress = "02:00:00:00:00:00"
    private val fileAddressMac = "/sys/class/net/wlan0/address"

    val SET_PRJNAME = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        this.context = this

        window.setGravity(Gravity.CENTER);
        window.setLayout(Utils.dpToPx(600f).toInt(), Utils.dpToPx(440f).toInt());

        val dbManager: DataBaseHelper = DataBaseHelper(this)
        val db = dbManager.createDataBase();

        val dataList: Array<String> = arrayOf("*")
//        val data = db!!.query("settings", dataList, null, null, null, null, "id desc", "1")
//
//        while (data.moveToNext()) {
//
//            var gpsset: GpsSet = GpsSet(data.getInt(0), data.getDouble(1), data.getDouble(2),data.getString(3), data.getInt(4))
//
//            val prjname = Utils.getString(gpsset.prjname)
//            prjnameTV.setText(prjname)
//
//            break
//        }

        if (PrefUtils.getStringPreference(context, "prjname") != null){
            prjnameTV.setText(PrefUtils.getStringPreference(context, "prjname"))
        }

        doneTV.setOnClickListener {

            val getName: String = nickNameET.text.toString();
            if (getName == "" || getName == null) {

                Toast.makeText(this, "이름을 입력해 주세요", Toast.LENGTH_LONG).show()

                return@setOnClickListener

            }

            val prjname:String = prjnameTV.text.toString()



            if (prjname == "" || prjname == null) {
                Toast.makeText(this, "프로젝트 이름을 선택해 주세요", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            val intent: Intent = Intent(this, MainActivity::class.java);

            PrefUtils.setPreference(this, "name", getName);
            PrefUtils.setPreference(this,"prjname",prjname)

            startActivity(intent)
            finish()

        }

        changeprjTV.setOnClickListener {
            val intent = Intent(this, DlgProjectActivity::class.java)
//            startActivity(intent)
            startActivityForResult(intent, SET_PRJNAME);
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        var number: Number

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_PRJNAME -> {
                    if (data!!.getStringExtra("name") != null){
                        val name = data!!.getStringExtra("name")
                        prjnameTV.setText(name)
                    }
                }
            }
        }
    }


}
