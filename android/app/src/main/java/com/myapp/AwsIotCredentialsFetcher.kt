import com.myapp.R
import android.content.Context
import android.content.res.Resources
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory


object AwsIotCredentialsFetcher {

    @Throws(Exception::class)
    fun getCredentialsJson(
        context: Context,
        iotEndpoint: String,
        roleAlias: String,
        thingName: String
    ): String {
        // 1) Cargar keystore desde el archivo .p12
        val keystorePath = AwsKeystoreManager.loadOrCreateKeystore(context)
        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(keystorePath).use { fis ->
            keyStore.load(fis, "iotpasswd".toCharArray())
        }

        // 2) Configurar KeyManagerFactory con tu certificate & key
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, "iotpasswd".toCharArray())

        // 3) Cargar AmazonRootCA1.pem desde res/raw
        val caInput = context.resources.openRawResource(R.raw.amazonrootca1)
        val cf = CertificateFactory.getInstance("X.509")
        val caCert = cf.generateCertificate(caInput)

        // 4) Crear un KeyStore vacío y agregar la CA de Amazon
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(null, null)
        trustStore.setCertificateEntry("AmazonRootCA1", caCert)

        // 5) Inicializar el TrustManagerFactory con ese trustStore
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(trustStore)

        // 6) Crear el SSLContext con KeyManagerFactory + TrustManagerFactory
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(kmf.keyManagers, tmf.trustManagers, null)

        // 7) Construir URL y hacer la petición HTTPS
        val urlStr = "$iotEndpoint/role-aliases/$roleAlias/credentials"
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpsURLConnection
        conn.sslSocketFactory = sslContext.socketFactory
        conn.requestMethod = "GET"
        conn.setRequestProperty("x-amzn-iot-thingname", thingName)
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        // 8) Recoger la respuesta
        val responseCode = conn.responseCode
        return if (responseCode == 200) {
            val inputStream = conn.inputStream
            readAll(inputStream)
        } else {
            val err = conn.errorStream
            val errorPayload = if (err != null) readAll(err) else ""
            throw Exception("Error HTTP: $responseCode: $errorPayload")
        }
    }



    fun getCredentialsJsonWithSslContext(
        sslContext: SSLContext,
        iotEndpoint: String,
        roleAlias: String,
        thingName: String
    ): String {
        try {
            val urlStr = "$iotEndpoint/role-aliases/$roleAlias/credentials"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpsURLConnection

            conn.sslSocketFactory = sslContext.socketFactory
            conn.requestMethod = "GET"
            conn.setRequestProperty("x-amzn-iot-thingname", thingName)
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val responseCode = conn.responseCode
            return if (responseCode == 200) {
                val inputStream = conn.inputStream
                readAll(inputStream)
            } else {
                val err = conn.errorStream
                val errorPayload = if (err != null) readAll(err) else ""
                throw Exception("Error HTTP $responseCode: $errorPayload")
            }

        } catch (e: Exception) {
            Log.e("AwsIotFetcher", "❌ Error en getCredentialsJsonWithSslContext", e)
            throw e
        }
    }


    @Throws(IOException::class)
    private fun readAll(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        return sb.toString()
    }
}
