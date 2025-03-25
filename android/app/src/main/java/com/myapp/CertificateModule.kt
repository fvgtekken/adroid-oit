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

  @ReactMethod
  fun fetchCredentials(promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // 1. Cargar/crear keystore si no existe
        val path = AwsKeystoreManager.loadOrCreateKeystore(reactContext)
        val resultMap = Arguments.createMap()
        resultMap.putString("keystorePath", path)

        // 2. Llamada a getSSLContext para obtener el SSLContext configurado
        val sslContext = getSSLContext(reactContext)
        if (sslContext == null) {
          throw Exception("Error initializing SSLContext")
        } else {
          Log.i("CertificateModule", "SSLContext initialized successfully")
        }

        // 3. Procede a la petición mTLS para obtener el token AWS
        val iotEndpoint: String = "https://c2gk5twytvp3ah.credentials.iot.sa-east-1.amazonaws.com"
        val roleAlias: String = "myapp-iot-role"
        val thingName: String = "myapp-v1"

        // Supón que AwsIotCredentialsFetcher.getCredentialsJson utiliza el SSLContext configurado
        val credsJson = AwsIotCredentialsFetcher.getCredentialsJson(
          reactContext, iotEndpoint, roleAlias, thingName
        )

        promise.resolve(credsJson)
      
      } catch(e: Exception) {
        Log.e("CertificateModule", "Error fetching credentials", e)
        promise.reject("AWS_IOT_ERROR", e)
      }
    }
  }
}
