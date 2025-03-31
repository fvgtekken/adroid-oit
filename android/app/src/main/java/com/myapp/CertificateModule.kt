package com.myapp

import android.util.Log
import android.security.KeyChain
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
      
    // Verifica si ya hay un alias de certificado guardado en SharedPreferences.
    // Si existe, lo reutiliza directamente para evitar mostrar el selector de KeyChain.
    // Si no existe, se asegura de tener una Activity activa  para poder lanzar el prompt de selección.

      try {
        val prefs = reactContext.getSharedPreferences("myapp_prefs", android.content.Context.MODE_PRIVATE)
        val storedAlias = prefs.getString("cert_alias", null)

        if (storedAlias != null) {
          Log.i("CertificateModule", "✅ Alias reutilizado: $storedAlias")
          buildAndFetchWithAlias(storedAlias, promise)
        } else {
          val activity = currentActivity
          if (activity == null) {
            Log.e("CertificateModule", "❌ currentActivity es null")
            promise.reject("NO_ACTIVITY", "No se pudo obtener la actividad actual")
            return@launch
          }

          KeyChain.choosePrivateKeyAlias(
            activity,
            { alias ->
              if (alias == null) {
                Log.e("CertificateModule", "❌ No se seleccionó alias")
                promise.reject("NO_ALIAS", "No se seleccionó ningún certificado")
              } else {
                Log.i("CertificateModule", "🔐 Alias elegido: $alias")
                prefs.edit().putString("cert_alias", alias).apply()
                buildAndFetchWithAlias(alias, promise)
              }
            },
            null, null, null, -1, null
          )
        }
      } catch (e: Exception) {
        Log.e("CertificateModule", "❌ Error general con certificado firmado", e)
        promise.reject("SIGNED_CERT_ERROR", e)
      }
    }
  }

  private fun buildAndFetchWithAlias(alias: String, promise: Promise) {
    try {
      val sslContext: SSLContext? = KeyChainSslContextBuilder.buildSSLContext(reactContext, alias)

      if (sslContext == null) {
        throw Exception("❌ No se pudo construir SSLContext con KeyChain")
      }

      // Esto deberia estar en variables de entorno.
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

    } catch (e: Exception) {
      Log.e("CertificateModule", "🚨 Error en buildAndFetchWithAlias", e)
      promise.reject("FETCH_ERROR", e)
    }
  }
}
