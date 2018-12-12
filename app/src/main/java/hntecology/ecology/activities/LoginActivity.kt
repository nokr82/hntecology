package hntecology.ecology.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.setGravity(Gravity.CENTER);
        window.setLayout(Utils.dpToPx(600f).toInt(), Utils.dpToPx(400f).toInt());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.WRITE_EXTERNAL_STORAGE)
        } else {
            val dbManager: DataBaseHelper = DataBaseHelper(this)

            val db = dbManager.createDataBase();
        }


        doneTV.setOnClickListener {

            val getName: String = nickNameET.text.toString();
            if (getName == "" || getName == null) {

                Toast.makeText(this, "이름을 입력해 주세요", Toast.LENGTH_LONG).show()

            } else {

                val intent: Intent = Intent(this, MainActivity::class.java);

                PrefUtils.setPreference(this, "name", getName);

                startActivity(intent)
                finish()
            }

        }
    }


    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (android.Manifest.permission.ACCESS_FINE_LOCATION == perm) {
                loadPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION, MainActivity.REQUEST_ACCESS_COARSE_LOCATION)
            } else if (android.Manifest.permission.ACCESS_COARSE_LOCATION == perm) {

            } else if (android.Manifest.permission.WRITE_EXTERNAL_STORAGE == perm) {
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            } else if (android.Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                val dbManager: DataBaseHelper = DataBaseHelper(this)

                val db = dbManager.createDataBase();
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MainActivity.WRITE_EXTERNAL_STORAGE -> {
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            }
            MainActivity.READ_EXTERNAL_STORAGE -> {
                val dbManager: DataBaseHelper = DataBaseHelper(this)
                val db = dbManager.createDataBase();
            }
        }

    }
}
