package hntecology.ecology.base

import android.net.wifi.WifiManager
import android.util.Log
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

object EncUtils {

    //Android 6.0 : Access to mac address from WifiManager forbidden
    private val marshmallowMacAddress = "02:00:00:00:00:00"
    private val fileAddressMac = "/sys/class/net/wlan0/address"

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
    fun generateKey(wifiManager:WifiManager, macAddress:String?): SecretKey {

        println("macAddress : $macAddress")

        if(macAddress == null) {
            return SecretKeySpec("".toByteArray(), "AES")
        }


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

        return SecretKeySpec(macAddressWithPadding.toByteArray(), "AES")
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

    fun encrypt(message: ByteArray, secret: SecretKey): ByteArray {
        /* Encrypt the message. */
        var cipher: Cipher? = null
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher!!.init(Cipher.ENCRYPT_MODE, secret)
        return cipher!!.doFinal(message)
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
    fun decrypt(cipherText: ByteArray, secret: SecretKey): ByteArray {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        var cipher: Cipher? = null
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher!!.init(Cipher.DECRYPT_MODE, secret)
        return cipher!!.doFinal(cipherText)
    }

}