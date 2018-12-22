package hntecology.ecology.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.Log
import hntecology.ecology.R
import hntecology.ecology.base.AlertListener
import hntecology.ecology.base.DataBaseHelper
import hntecology.ecology.base.PrefUtils
import hntecology.ecology.base.Utils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.InvalidParameterSpecException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

class IntroActivity : Activity() {

    lateinit var context: Context

    protected var _splashTime = 500 // time to display the splash screen in ms
    private val _active = true
    lateinit var splashThread: Thread

    //Android 6.0 : Access to mac address from WifiManager forbidden
    private val marshmallowMacAddress = "02:00:00:00:00:00"
    private val fileAddressMac = "/sys/class/net/wlan0/address"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        this.context = this

        val dataBaseHelper = DataBaseHelper(this)
//        dataBaseHelper.deleteDataBase()

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

        var macAddress = recupAdresseMAC(wifiManager)

        println("macAddress : $macAddress")


        val macAddressLength = macAddress!!.length

        var macAddressWithPadding = macAddress

        if(macAddressLength < 16) {
            for (idx in 0 until (16 - macAddressLength)) {
                macAddressWithPadding += "$"
            }
        } else if(macAddressLength < 24) {
            for (idx in 0 until (24 - macAddressLength)) {
                macAddressWithPadding += "$"
            }
        } else if(macAddressLength < 32) {
            for (idx in 0 until (32 - macAddressLength)) {
                macAddressWithPadding += "$"
            }
        }

        println("macAddress.length : ${macAddressWithPadding.length}")

        val secretKey = generateKey(macAddressWithPadding!!);

        val decryptedBytes = licenseFile.readBytes()

        val decrypted = decryptMsg(decryptedBytes, secretKey)

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

        val  dbVersion = decryptedSplts.last()

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

        toLogin()
    }

    fun recupAdresseMAC(wifiMan: WifiManager): String? {
        val wifiInf = wifiMan.connectionInfo

        if (wifiInf.macAddress == marshmallowMacAddress) {
            var ret: String? = null
            try {
                ret = getAdressMacByInterface()
                if (ret != null && ret.isNotEmpty()) {
                    return ret
                } else {
                    ret = getAddressMacByFile(wifiMan)
                    return ret
                }
            } catch (e: IOException) {
                Log.e("MobileAccess", "Erreur lecture propriete Adresse MAC")
            } catch (e: Exception) {
                Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ")
            }

        } else {
            return wifiInf.macAddress
        }
        return marshmallowMacAddress
    }

    private fun getAdressMacByInterface(): String? {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (nif.name.equals("wlan0", ignoreCase = true)) {
                    val macBytes = nif.hardwareAddress ?: return ""

                    val res1 = StringBuilder()
                    for (b in macBytes) {
                        res1.append(String.format("%02X:", b))
                    }

                    if (res1.length > 0) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    return res1.toString()
                }
            }

        } catch (e: Exception) {
            Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ")
        }

        return null
    }

    @Throws(Exception::class)
    private fun getAddressMacByFile(wifiMan: WifiManager): String {
        val ret: String
        val wifiState = wifiMan.wifiState

        wifiMan.isWifiEnabled = true
        val fl = File(fileAddressMac)
        val fin = FileInputStream(fl)
        val builder = StringBuilder()
        while (true) {
            val ch = fin.read()
            if(ch != -1) {
                break
            }
            builder.append(ch.toChar())
        }

        ret = builder.toString()
        fin.close()

        val enabled = WifiManager.WIFI_STATE_ENABLED == wifiState
        wifiMan.isWifiEnabled = enabled
        return ret
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun generateKey(password:String): SecretKey {
        return SecretKeySpec(password.toByteArray(), "AES")
    }

    @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            InvalidKeyException::class,
            InvalidParameterSpecException::class,
            IllegalBlockSizeException::class,
            BadPaddingException::class,
            UnsupportedEncodingException::class
    )
    fun encryptMsg(message: String, secret: SecretKey): ByteArray {
        /* Encrypt the message. */
        var cipher: Cipher? = null
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher!!.init(Cipher.ENCRYPT_MODE, secret)
        return cipher!!.doFinal(message.toByteArray(charset("UTF-8")))
    }

    @Throws(
            NoSuchPaddingException::class,
            NoSuchAlgorithmException::class,
            InvalidParameterSpecException::class,
            InvalidAlgorithmParameterException::class,
            InvalidKeyException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class,
            UnsupportedEncodingException::class
    )
    fun decryptMsg(cipherText: ByteArray, secret: SecretKey): String {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        var cipher: Cipher? = null
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher!!.init(Cipher.DECRYPT_MODE, secret)
        return String(cipher!!.doFinal(cipherText), Charset.forName("UTF-8"))
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


}
