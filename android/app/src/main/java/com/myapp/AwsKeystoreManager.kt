package com.myapp

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.myapp.R
import java.io.InputStream

object AwsKeystoreManager {

    private const val TAG = "AwsKeystoreManager"
    private const val KEYSTORE_NAME = "iotkeystore"
    private const val KEYSTORE_PASSWORD = "iotpasswd"
    private const val CERTIFICATE_ID = "my_iot_cert"

    fun loadOrCreateKeystore(context: Context) : String {
        try {
            val keystorePath = context.filesDir.absolutePath

            // Verifica si ya existe un keystore guardado
            val exists = AWSIotKeystoreHelper.isKeystorePresent(keystorePath, KEYSTORE_NAME)
            if (!exists) {
                Log.i(TAG, "Keystore no existe, creando uno nuevo...")

                // Carga los certificados de res/raw
                val certificatePem = loadPemFromRaw(context, R.raw.myapp_v1_certificate_crt)
                val privateKeyPem = loadPemFromRaw(context, R.raw.myapp_v1_private_key)
                // (Opcional) la CA Root se usa para validar el servidor, pero AWSIotKeystoreHelper
                // no la incluye por defecto en el keystore si usas un truststore distinto.

                // Guarda (certificate + privateKey) en un keystore BKS
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                    CERTIFICATE_ID,        // alias
                    certificatePem,
                    privateKeyPem,
                    keystorePath,
                    KEYSTORE_NAME,
                    KEYSTORE_PASSWORD
                )

                Log.i(TAG, "Keystore creado exitosamente!")
            } else {
                Log.i(TAG, "Keystore ya existente, no se necesita crearlo de nuevo.")
            }

            // Retornar la ruta (puedes retornar cualquier string que necesites)
             Log.i(TAG, "Devolviendo el path in JSON For!")
             return "$keystorePath/$KEYSTORE_NAME.bks"

        } catch (e: Exception) {
            Log.e(TAG, "Error creando keystore", e)
            return "Error Creando Keystore "
        }
    }

    private fun loadPemFromRaw(context: Context, rawResId: Int): String {
        val inputStream = context.resources.openRawResource(rawResId)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer)
    }
}
