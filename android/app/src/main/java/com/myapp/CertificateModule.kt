package com.myapp

import android.util.Log
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.myapp.getSSLContext  // Importa la función getSSLContext desde SSLContextUtil.kt

class CertificateModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "CertificateModule"
   

  // React Fetch Credentials
  /***************************************************************************/ 
  @ReactMethod
   fun fetchCredentials(promise: Promise) {
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val context = reactContext

            // 1. Crear clave si no existe
            val created = SecureKeyManager.ensureKeyExists(context)
            if (!created) {
                throw Exception("Error creando clave en AndroidKeyStore")
            }

            // 2. Obtener certificado generado por el sistema
            val cert = SecureKeyManager.getCertificate()
            if (cert != null) {
                Log.i("CertificateModule", "✅ Certificado generado:")
                Log.i("CertificateModule", cert.toString())
                promise.resolve(cert.toString())
            } else {
                throw Exception("No se pudo obtener el certificado")
            }

        } catch (e: Exception) {
            Log.e("CertificateModule", "Error en KeyStore", e)
            promise.reject("ANDROID_KEYSTORE_ERROR", e)
        }
    }
  }


  // React export public certificate
  /***************************************************************************/ 
    @ReactMethod
    fun exportCertificate(promise: Promise) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val certPath = SecureKeyManager.exportCertificateToPemFile(reactContext)
                if (certPath != null) {
                    promise.resolve(certPath) // 👍 Pasamos ruta a JS
                } else {
                    throw Exception("No se pudo exportar el certificado.")
                }
            } catch (e: Exception) {
                promise.reject("EXPORT_ERROR", e)
            }
        }
    }



  // React Fetch  get fingerprint
  /***************************************************************************/ 
    @ReactMethod
    fun getCertificateFingerprint(promise: Promise) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fingerprint = SecureKeyManager.getCertificateFingerprint(reactContext)
                promise.resolve(fingerprint)
            } catch (e: Exception) {
                promise.reject("FINGERPRINT_ERROR", e)
            }
        }
    }


    // React generateCsr
    /***************************************************************************/ 
    @ReactMethod
    fun generateCsr(promise: Promise) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val path = SecureKeyManager.generateCsr(reactContext)
                promise.resolve(path)
            } catch (e: Exception) {
                promise.reject("CSR_ERROR", e)
            }
        }
    }

}
