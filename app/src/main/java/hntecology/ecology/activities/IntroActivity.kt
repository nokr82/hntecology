package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import hntecology.ecology.R
import hntecology.ecology.base.*
import hntecology.ecology.model.GpsSet
import java.io.File
import java.nio.charset.Charset

class IntroActivity : Activity() {

    lateinit var context: Context

    protected var _splashTime = 500 // time to display the splash screen in ms
    private val _active = true
    lateinit var splashThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        this.context = this

        // val dataBaseHelper = DataBaseHelper(this)
        // dataBaseHelper.deleteDataBase()

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

                    runOnUiThread {
                        stopIntro()
                    }

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
            // toLogin()
//        } else {
//            val intent = Intent(context, MainActivity::class.java)
//                            Intent intent = new Intent(context, SelectLocationActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            loadPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.WRITE_EXTERNAL_STORAGE)
        } else {
            checkLicense()
        }

    }

    private fun toLogin() {
//        PrefUtils.clear(context)
        PrefUtils.removePreference(context, "name")

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun checkLicense() {
        /*
        val intent = Intent(context, SearchAddressActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        return
        */

        val dataPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology"
        val sourceDirectory = File(dataPath)
        if(!sourceDirectory.exists()) {
            Utils.alert(context, "비정상적인 접근입니다.[0]", object:AlertListener {
                override fun before(): Boolean {
                    return true
                }

                override fun after() {
                    finish()
                }
            })
        }

        val licenseFile = File(dataPath + File.separator + "license.dat")
        if(!licenseFile.exists()) {
            Utils.alert(context, "비정상적인 접근입니다.[1]", object:AlertListener {
                override fun before(): Boolean {
                    return true
                }

                override fun after() {
                    finish()
                }
            })

            return
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val macAddress = EncUtils.recupAdresseMAC(wifiManager)
        val secretKey = EncUtils.generateKey(wifiManager, macAddress);

        val bytes = licenseFile.readBytes()
        val decryptedBytes = EncUtils.decrypt(bytes, secretKey)
        val decrypted = String(decryptedBytes, Charset.forName("UTF-8"))

        println("decrypted : $decrypted")

        val decryptedSplts = decrypted.split(":")
        if(decryptedSplts.isEmpty()) {
            Utils.alert(context, "비정상적인 접근입니다.[2]", object:AlertListener {
                override fun before(): Boolean {
                    return true
                }

                override fun after() {
                    finish()
                }
            })

            return
        }

        val  phrase = decryptedSplts.last()

        println("phrase : $phrase")

        var idx = 0
        var mac = ""
        for(sp in decryptedSplts) {
            mac += sp

            if(idx < decryptedSplts.size - 2) {
                mac += ":"
            } else {
                break
            }

            idx++
        }

        println("mac : $mac")

        if(macAddress != mac) {
            Utils.alert(context, "비정상적인 접근입니다.[3]", object:AlertListener {
                override fun before(): Boolean {
                    return true
                }

                override fun after() {
                    finish()
                }
            })

            return
        }

        copyAllData(phrase)
    }


    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (android.Manifest.permission.WRITE_EXTERNAL_STORAGE == perm) {
                loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
            } else if (android.Manifest.permission.READ_EXTERNAL_STORAGE == perm) {
                checkLicense()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        println("requestCode f $requestCode")

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
                    Utils.alert(context, "권한을 승인해야 사용할 수 있습니다.", object: AlertListener {
                        override fun before(): Boolean {
                            return true
                        }

                        override fun after() {
                            loadPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.READ_EXTERNAL_STORAGE)
                        }
                    })

                } else {
                    checkLicense()
                }

            }
        }

    }


    private fun copyAllData(phrase: String) {
        val sourceDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ecology" + File.separator + "source")
        val targetDirectory = File(applicationInfo.dataDir)

        println("sourceDirectory : $sourceDirectory")

        val sourceDirectoryFiles = sourceDirectory.listFiles()

        println("sourceDirectoryFiles : $sourceDirectoryFiles")

        if(sourceDirectoryFiles == null) {
            Utils.alert(context, "비정상적인 접근입니다.[4]", object:AlertListener {
                override fun before(): Boolean {
                    return true
                }

                override fun after() {
                    finish()
                }
            })
            return
        }


        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager



        var macAddress = EncUtils.recupAdresseMAC(wifiManager)

        val secretKey = EncUtils.generateKey(wifiManager, "$macAddress:$phrase");

        PrefUtils.setPreference(this, "mac_addr", macAddress!!.replace(":",""));

        var canGo = true;

        for (sourceDirectoryFile in sourceDirectoryFiles) {
            var fileName = sourceDirectoryFile.name
            fileName = fileName.replace("_enc", "")

            val targetDirectoryFile = File("$targetDirectory${File.separator}${fileName}")

            println(targetDirectoryFile.absolutePath)
            try {
                val decryptedBytes = EncUtils.decrypt(sourceDirectoryFile.readBytes(), secretKey)
                targetDirectoryFile.writeBytes(decryptedBytes)
            } catch (e : Exception) {
                e.printStackTrace()

                canGo = false

                break
            }

        }

        if(canGo) {
            // check database
            checkDatabase()
        } else {
            Utils.alert(context, "비정상적인 접근입니다.[5]", object:AlertListener {
                override fun before(): Boolean {
                    return true
                }

                override fun after() {
                    finish()
                }
            })
        }
    }

    private fun checkDatabase() {
        val dbManager: DataBaseHelper = DataBaseHelper(this)
        if(!dbManager.checkDataBase()) {

            dbManager.createDataBase()

            toLogin()

        } else {

            // 기존 DB정보와 비교

            var currentDBVersion = -1
            var newDBVersion = -1

            val db = dbManager.createDataBase();

            val dataList: Array<String> = arrayOf("*")
            val data = db!!.query("settings", dataList, null, null, null, null, "id desc", "1")

            while (data.moveToNext()) {

                var gpsset: GpsSet = GpsSet(data.getInt(0), data.getDouble(1), data.getDouble(2),data.getString(3), data.getInt(4))
                if(gpsset.dbVersion != null) {
                    currentDBVersion= gpsset.dbVersion!!.toInt()
                }

                break
            }

            // 신규 db
            val myPath = "${applicationInfo.dataDir}${File.separator}ecology.db"

            println("myPath : $myPath")

            val newDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)

            println("newDB : $newDB")

            val newData = newDB!!.query("settings", dataList, null, null, null, null, "id desc", "1")

            println("newData : $newData")

            while (newData.moveToFirst()) {

                var gpsset: GpsSet = GpsSet(newData.getInt(0), newData.getDouble(1), newData.getDouble(2), newData.getString(3), newData.getInt(4))

                println("gpsset : ${gpsset.id}")
                println("gpsset : ${gpsset.latitude}")
                println("gpsset : ${gpsset.prjname}")

                if(gpsset.dbVersion != null) {
                    newDBVersion= gpsset.dbVersion!!.toInt()
                }

                break
            }



            println("currentDBVersion : $currentDBVersion, newDBVersion : $newDBVersion")

//            if(currentDBVersion < newDBVersion) {
                dbManager.deleteDataBase()
                dbManager.createDataBase()
//            }

            toLogin()
        }


    }

}
