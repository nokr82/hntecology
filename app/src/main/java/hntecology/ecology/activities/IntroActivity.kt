package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import hntecology.ecology.R
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import java.io.File

class IntroActivity : Activity() {

    lateinit var context: Context

    protected var _splashTime = 500 // time to display the splash screen in ms
    private val _active = true
    lateinit var splashThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        this.context = this

        val dataBaseHelper = DataBaseHelper(this)
        dataBaseHelper.deleteDataBase()

        splashThread = object : Thread() {
            override fun run() {
                try {
                    var waited = 0
                    while (waited < _splashTime && _active) {
                        Thread.sleep(100)
                        waited += 100
                    }
                } catch (e: InterruptedException) {
                    // do nothing
                } finally {
                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            home_hint();
                        }
                    });
                    */

                    copyAllData()

                }
            }
        }
        splashThread.start()

    }

    private fun stopIntro() {

//        PrefUtils.clear(context)

//        val name = PrefUtils.getStringPreference(context, "name")
//
//        if (null == name || "" == name || name.length < 1) {
            toLogin()
//        } else {
//            val intent = Intent(context, MainActivity::class.java)
//                            Intent intent = new Intent(context, SelectLocationActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//        }

    }

    private fun toLogin() {
        PrefUtils.clear(context)

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun copyAllData() {
        val sourceDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "data")
        val targetDirectory = File(applicationInfo.dataDir)

        println("sourceDirectory : $sourceDirectory")

        val sourceDirectoryFiles = sourceDirectory.listFiles()

        println("sourceDirectoryFiles : $sourceDirectoryFiles")

        if(sourceDirectoryFiles == null){
            stopIntro()
            return
        }

        for (sourceDirectoryFile in sourceDirectoryFiles) {
            val targetDirectoryFile = File("$targetDirectory${File.separator}${sourceDirectoryFile.name}")

            println(targetDirectoryFile.absolutePath)

            if(!targetDirectoryFile.exists()) {
                sourceDirectoryFile.copyTo(targetDirectoryFile, true)
            }
        }

        stopIntro()

    }
}
