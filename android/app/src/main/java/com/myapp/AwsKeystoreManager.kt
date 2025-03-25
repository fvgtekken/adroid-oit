import android.content.Context
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

object AwsKeystoreManager {
    private const val TAG = "AwsKeystoreManager"
    private const val KEYSTORE_NAME = "myapp-v1-certificate.p12"
    private const val KEYSTORE_PASSWORD = "iotpasswd"
    private const val CERTIFICATE_ID = "my_iot_cert"

    fun loadOrCreateKeystore(context: Context): String {
        try {
            val keystorePath = context.filesDir.absolutePath
            val fullKeystorePath = "$keystorePath/$KEYSTORE_NAME"
            Log.i(TAG, "Path: $fullKeystorePath")

            val file = File(fullKeystorePath)

            if (!file.exists()) {
                Log.e(TAG, "El archivo $KEYSTORE_NAME no existe. Crea el archivo .p12 manualmente usando OpenSSL.")
                throw Exception("El archivo $KEYSTORE_NAME no existe")
            }

            // Añade BouncyCastle al proveedor de seguridad
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
                Log.i(TAG, "BouncyCastle añadido como proveedor de seguridad")
            } else {
                Log.i(TAG, "BouncyCastle ya está registrado")
            }

            // Cargar el archivo .p12
            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(file).use { fis ->
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray())
            }

            Log.i(TAG, "Keystore cargado correctamente desde .p12")

            // Confirmar que el alias está disponible
            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                Log.i(TAG, "Alias encontrado en keystore: $alias")
            }

            return fullKeystorePath
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar el keystore", e)
            throw e
        }
    }
}
