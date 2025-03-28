package com.myapp

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate



class MainActivity : ReactActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("javax.net.debug", "ssl,handshake")
        System.setProperty("javax.net.debug", "all")
        // Genera la clave en el Android Keystore si aún no existe
       
       if (shouldInstallCertificate()) {
            KeyChainInstaller.installP12FromSdcard(this, this, "myapp-iot-client.p12")
        }
    }

    override fun getMainComponentName(): String = "myApp"

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    
    private fun shouldInstallCertificate(): Boolean {

        val prefs = getSharedPreferences("myapp_prefs", MODE_PRIVATE)
        val alreadyInstalled = prefs.getBoolean("cert_installed", false)
        
        if (!alreadyInstalled) {
            prefs.edit().putBoolean("cert_installed", true).apply()
            return true
        }
        return false
    }
}
