package com.myapp

import android.util.Log
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CertificateModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "CertificateModule"

  // Ejemplo: método para "fetchCredentials"
  @ReactMethod
  fun fetchCredentials(
    //iotEndpoint: String,
    //roleAlias: String,
    //thingName: String,
    promise: Promise
  ) {
    // Se recomienda no bloquear el main thread, así que lo lanzamos en corrutina
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // 1. Cargar/crear keystore si no existe
       val path =  AwsKeystoreManager.loadOrCreateKeystore(reactContext)
        
        
        val resultMap = Arguments.createMap()
        resultMap.putString("keystorePath", path)

     
        promise.resolve(resultMap)


        //*******************************************************//
        // 2. Hacer peticion mTLS para obtener el token AWS

        //val iotEndpoint:String = "2gk5twytvp3ah.credentials.iot.sa-east-1.amazonaws.com";
        //val roleAlias:String = "myapp-iot-role"
        //val thingName:String = "myapp-v1"

        /*val credsJson = AwsIotCredentialsFetcher.getCredentialsJson(
          reactContext, iotEndpoint, roleAlias, thingName
        )*/

        // 2. Resolver la promesa con el JSON (string)
        //promise.resolve(credsJson)
      
      } catch(e: Exception) {
        Log.e("CertificateModule", "Error fetching credentials", e)
        promise.reject("AWS_IOT_ERROR", e)
      }
    }
  }
}
