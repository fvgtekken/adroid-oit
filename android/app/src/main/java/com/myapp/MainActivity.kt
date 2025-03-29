package com.myapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

    private val INSTALL_CERT_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("javax.net.debug", "ssl,handshake")

        if (shouldInstallCertificate()) {
            KeyChainInstaller.installP12FromAppFiles(this, this, "myapp-iot-client.p12")

        }
    }

    override fun getMainComponentName(): String = "myApp"

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    private fun shouldInstallCertificate(): Boolean {
        val prefs = getSharedPreferences("myapp_prefs", MODE_PRIVATE)
        val alreadyInstalled = prefs.getBoolean("cert_installed", false)

        return !alreadyInstalled
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == INSTALL_CERT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("MainActivity", "✅ Certificado instalado correctamente")
                getSharedPreferences("myapp_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("cert_installed", true)
                    .apply()
            } else {
                Log.w("MainActivity", "⚠️ Instalación de certificado cancelada")
            }
        }
    }


    
   /*override fun onResume() {
       super.onResume()
        KeyChainLoader.chooseAlias(this) { alias ->
            if (alias != null) {
                Log.i("MainActivity", "🎉 ¡Alias detectado! Vamos bien: $alias")
                // 👉 Acá ya podrías usar el alias para construir el SSLContext
            } else {
                Log.e("MainActivity", "😢 No se encontró alias para el certificado.")
            }
        }
    }*/


    
}
