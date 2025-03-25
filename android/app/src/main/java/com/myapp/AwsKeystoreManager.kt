package com.myapp
import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.myapp.R
import java.security.KeyStore
import java.io.File
import java.io.FileInputStream

object AwsKeystoreManager {
    // Definiciones de constantes
    private const val TAG = "AwsKeystoreManager"
    private const val KEYSTORE_NAME = "iotkeystore"
    private const val KEYSTORE_PASSWORD = "iotpasswd"
    private const val CERTIFICATE_ID = "my_iot_cert"

    fun loadOrCreateKeystore(context: Context): String {
        try {
            val keystorePath = context.filesDir.absolutePath
            // Se usa el nombre real sin extensión, ya que AWSIotKeystoreHelper lo guarda así
            val fullKeystorePath = "$keystorePath/$KEYSTORE_NAME"
            Log.i(TAG, "Path: $fullKeystorePath")

            val file = File(fullKeystorePath)

            // Usamos AWSIotKeystoreHelper.isKeystorePresent para verificar si existe
            val exists = AWSIotKeystoreHelper.isKeystorePresent(keystorePath, KEYSTORE_NAME)
            if (!exists || !file.exists()) {
                Log.i(TAG, "Keystore no existe, creando uno nuevo...")

                // Carga los certificados desde res/raw
                val certificatePem = loadPemFromRaw(context, R.raw.myapp_v1_certificate_crt)
                val privateKeyPem = loadPemFromRaw(context, R.raw.myapp_v1_private_key)

                // Guarda (certificate + privateKey) en el keystore
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
                    CERTIFICATE_ID,
                    certificatePem,
                    privateKeyPem,
                    keystorePath,
                    KEYSTORE_NAME,
                    KEYSTORE_PASSWORD
                )

                Log.i(TAG, "Keystore creado exitosamente!")
            } else {
                Log.i(TAG, "Keystore ya existente, no se necesita crearlo de nuevo.")

                // Carga el keystore para listar sus alias y confirmar su contenido
                /*val keyStore = KeyStore.getInstance("BKS")
                val fis = FileInputStream(file)
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray())
                fis.close()

                val aliases = keyStore.aliases()
                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    Log.d(TAG, "Alias en keystore: $alias")
                }*/
            }
            return fullKeystorePath
        } catch (e: Exception) {
            Log.e(TAG, "Error creando keystore", e)
            return "Error Creando Keystore"
        }
    }

    // Método para cargar un certificado en formato PEM desde la carpeta res/raw
    private fun loadPemFromRaw(context: Context, rawResId: Int): String {
        val inputStream = context.resources.openRawResource(rawResId)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer)
    }
}
