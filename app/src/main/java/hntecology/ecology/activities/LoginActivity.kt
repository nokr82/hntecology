package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.Toast
import hntecology.ecology.R
import hntecology.ecology.base.AlertListener
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import hntecology.ecology.base.Utils.alert
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File

class LoginActivity : Activity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        this.context = this

        window.setGravity(Gravity.CENTER);
        window.setLayout(Utils.dpToPx(600f).toInt(), Utils.dpToPx(440f).toInt());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.WRITE_EXTERNAL_STORAGE)
        } else {
            val dbManager: DataBaseHelper = DataBaseHelper(this)
            val db = dbManager.createDataBase();

            copyAllData()
        }

        if (PrefUtils.getStringPreference(context, "prjname") != null){
            val prjname = PrefUtils.getStringPreference(context, "prjname");
            prjnameET.setText(prjname)
        }

        doneTV.setOnClickListener {

            val getName: String = nickNameET.text.toString();
            if (getName == "" || getName == null) {

                Toast.makeText(this, "이름을 입력해 주세요", Toast.LENGTH_LONG).show()

                return@setOnClickListener

            }

            val prjname:String = prjnameET.text.toString()

            if (prjname == "" || prjname == null) {
                Toast.makeText(this, "프로젝트 이름을 입력해 주세요", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }



            val intent: Intent = Intent(this, MainActivity::class.java);

            PrefUtils.setPreference(this, "name", getName);
            PrefUtils.setPreference(this,"prjname",prjname)

            startActivity(intent)
            finish()

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

                copyAllData()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MainActivity.WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Utils.alert(context, "권한을 승인해야 사용할 수 있습니다.", object : AlertListener {
                        override fun before(): Boolean {
                            return true
                        }

                        override fun after() {
                            loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.WRITE_EXTERNAL_STORAGE)
                        }
                    })
                } else {
                    loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
                }


            }
            MainActivity.READ_EXTERNAL_STORAGE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    alert(context, "권한을 승인해야 사용할 수 있습니다.", object: AlertListener {
                        override fun before(): Boolean {
                            return true
                        }

                        override fun after() {
                            loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
                        }
                    })

                } else {
                    val dbManager: DataBaseHelper = DataBaseHelper(this)
                    val db = dbManager.createDataBase();

                    copyAllData()
                }

            }
        }

    }

    private fun copyAllData() {
        val sourceDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data")
        val targetDirectory = File(applicationInfo.dataDir)

        println("sourceDirectory : $sourceDirectory")

        val sourceDirectoryFiles = sourceDirectory.listFiles()

        println("sourceDirectoryFiles : $sourceDirectoryFiles")

        if(sourceDirectoryFiles == null) {
            return
        }

        for (sourceDirectoryFile in sourceDirectoryFiles) {
            val targetDirectoryFile = File("$targetDirectory${File.separator}${sourceDirectoryFile.name}")

            println(targetDirectoryFile.absolutePath)

            if(!targetDirectoryFile.exists()) {
                sourceDirectoryFile.copyTo(targetDirectoryFile, true)
            }
        }

    }
}
