package com.myapp

import android.content.Context
import java.security.KeyStore
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

object AwsIotCredentialsFetcher {

    @Throws(Exception::class)
    fun getCredentialsJson(
        context: Context,
        iotEndpoint: String,
        roleAlias: String,
        thingName: String
    ): String {
        // 1) Cargar keystore con nuestro cert y key
        val keystorePath = context.filesDir.absolutePath
        val clientKeyStore: KeyStore = AWSIotKeystoreHelper.getIotKeystore(
            "my_iot_cert",
            keystorePath,
            "iotkeystore",
            "iotpasswd"
        )

        // 2) KeyManagerFactory y TrustManagerFactory
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        // Asigna la contraseña a una variable para desambiguar la sobrecarga
        val keyPassword: CharArray = "iotpasswd".toCharArray()
        kmf.init(clientKeyStore, keyPassword)

        // CA trust store (si no confías en el truststore del sistema)
        // Por simplicidad, aquí se usa el truststore por defecto del sistema.
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)

        // 3) Configurar SSLContext con mTLS
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(kmf.keyManagers, tmf.trustManagers, null)

        // 4) Construir la URL
        val urlStr = "$iotEndpoint/role-aliases/$roleAlias/credentials"
        val url = URL(urlStr)

        // 5) Hacer la petición HTTPS
        val conn = url.openConnection() as HttpsURLConnection
        conn.sslSocketFactory = sslContext.socketFactory
        conn.requestMethod = "GET"
        conn.setRequestProperty("x-amzn-iot-thingname", thingName)
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        // 6) Recoger la respuesta
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
