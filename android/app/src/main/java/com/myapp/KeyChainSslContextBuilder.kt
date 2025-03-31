import android.content.Context
import android.security.KeyChain
import android.security.KeyChainException
import android.util.Log
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.cert.CertificateFactory


object KeyChainSslContextBuilder {
    private const val TAG = "KeyChainSslBuilder"

    fun buildSSLContext(context: Context, alias: String): SSLContext? {
        return try {
            val privateKey = KeyChain.getPrivateKey(context, alias)
            
            // Obtiene la cadena de certificados (incluye el certificado X.509 de la app y certificados intermedios si existen)
            // Esta cadena se usará junto con la clave privada para autenticarse en la conexión mTLS.
            val certChain = KeyChain.getCertificateChain(context, alias)

            if (privateKey == null || certChain == null) {
                Log.e(TAG, "❌ No se pudo obtener clave privada o cadena de certificados para alias: $alias")
                return null
            }

            // KeyStore en memoria con la clave privada y certificado del usuario, usado para mTLS.
            val keyStore = KeyStore.getInstance("PKCS12").apply {
                load(null, null)
                setKeyEntry(alias, privateKey, null, certChain)
            }

            // Inicializamos el KeyManagerFactory con el KeyStore que contiene la clave privada y el certificado.
            // Esto permite que el SSLContext use esta identidad para autenticación mTLS.

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            // Pasamos null porque este KeyStore en memoria no tiene contraseña.
            keyManagerFactory.init(keyStore, null)

            // Cargamos AmazonRootCA1.pem desde assets
            val trustStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)

            context.assets.open("AmazonRootCA1.pem").use { caInput ->
                val caCert = CertificateFactory.getInstance("X.509").generateCertificate(caInput)
                setCertificateEntry("amazon_root_ca", caCert)
                }
            }


            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(trustStore)


            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, SecureRandom())

            Log.i(TAG, "✅ SSLContext construido exitosamente desde alias: $alias")
            sslContext
        } catch (e: KeyChainException) {
            Log.e(TAG, "🚨 Error KeyChainException: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "🚨 Error general al construir SSLContext", e)
            null
        }
    }
}
