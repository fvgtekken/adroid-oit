package com.myapp

import android.content.Context
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.File
import java.io.FileOutputStream

class CertificateModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "CertificateModule"

    @ReactMethod
    fun copyCertificates(promise: Promise) {
        try {
            val context: Context = reactApplicationContext
            // Directorio destino: almacenamiento interno privado, en la carpeta "cert"
            val certDir = File(context.filesDir, "cert")
            if (!certDir.exists()) {
                certDir.mkdirs()
            }
            
            // Nombres de los archivos en assets
            val privateKeyAssetName = "myapp-v1-private.pem.key"
            val certificateAssetName = "myapp-v1-certificate.pem.crt"
            val caAssetName = "AmazonRootCA1.pem"

            // Archivos destino en el almacenamiento interno
            val privateKeyFile = File(certDir, privateKeyAssetName)
            val certificateFile = File(certDir, certificateAssetName)
            val caFile = File(certDir, caAssetName)

            // Copia cada archivo desde assets al directorio interno si no existe
            if (!privateKeyFile.exists()) {
                context.assets.open(privateKeyAssetName).use { input ->
                    FileOutputStream(privateKeyFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (!certificateFile.exists()) {
                context.assets.open(certificateAssetName).use { input ->
                    FileOutputStream(certificateFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (!caFile.exists()) {
                context.assets.open(caAssetName).use { input ->
                    FileOutputStream(caFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // Devuelve las rutas absolutas de los archivos copiados
            val resultMap = Arguments.createMap()
            
            resultMap.putString("privateKeyPath", privateKeyFile.absolutePath)
            resultMap.putString("certPath", certificateFile.absolutePath)
            resultMap.putString("caPath", caFile.absolutePath)
            promise.resolve(resultMap)

        } catch (e: Exception) {
            promise.reject("COPY_ERROR", e)
        }
    }
}
