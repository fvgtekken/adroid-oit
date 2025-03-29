package com.myapp

import android.util.Log
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.net.ssl.SSLContext

class CertificateModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "CertificateModule"

  @ReactMethod
  fun fetchCredentialsWithSignedCert(promise: Promise) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // Paso 1: Abrimos selector de alias del certificado instalado vía KeyChain
        KeyChainLoader.chooseAlias(currentActivity!!) { alias ->
          if (alias == null) {
            Log.e("CertificateModule", "🚫 No se seleccionó alias de certificado")
            promise.reject("NO_ALIAS", "No se seleccionó ningún certificado")
            return@chooseAlias
          }

          try {
            Log.i("CertificateModule", "🔐 Alias seleccionado: $alias")

            // Paso 2: Construimos el SSLContext con el alias seleccionado desde KeyChain
            val sslContext = KeyChainSslContextBuilder.buildSSLContext(reactContext, alias)


            if (sslContext == null) {
              throw Exception("❌ No se pudo construir el SSLContext con KeyChain")
            }

            // Paso 3: Conectamos a AWS IoT con mTLS usando ese SSLContext
            val iotEndpoint = "https://c2gk5twytvp3ah.credentials.iot.sa-east-1.amazonaws.com"
            val roleAlias = "myapp-iot-role"
            val thingName = "myapp-v1"

            val credsJson = AwsIotCredentialsFetcher.getCredentialsJsonWithSslContext(
              sslContext,
              iotEndpoint,
              roleAlias,
              thingName
            )

            Log.i("CertificateModule", "✅ Credenciales obtenidas con KeyChain")
            promise.resolve(credsJson)

          } catch (inner: Exception) {
            Log.e("CertificateModule", "🚨 Error con el certificado de KeyChain", inner)
            promise.reject("KEYCHAIN_ERROR", inner)
          }
        }
      } catch (e: Exception) {
        Log.e("CertificateModule", "❌ Error general con certificado firmado", e)
        promise.reject("SIGNED_CERT_ERROR", e)
      }
    }
  }
}
