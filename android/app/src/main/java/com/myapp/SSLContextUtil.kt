package com.myapp
import android.content.Context
import android.util.Log
import com.myapp.R   // Asegúrate de importar el R correcto de tu app
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

fun getSSLContext(context: Context): SSLContext? {
    try {
        // 1. Carga del keystore del cliente
        val keystorePath = context.filesDir.absolutePath
        val keystoreFile = File(keystorePath, "iotkeystore") // sin extensión
        val clientKeyStore = KeyStore.getInstance("BKS")
        FileInputStream(keystoreFile).use { fis ->
            clientKeyStore.load(fis, "iotpasswd".toCharArray())
        }
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(clientKeyStore, "iotpasswd".toCharArray())

        // 2. Configura el TrustStore con la CA de Amazon
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(null, null)
        val cf = CertificateFactory.getInstance("X.509")
        context.resources.openRawResource(R.raw.amazonrootca1).use { caInputStream ->
            val caCert = cf.generateCertificate(caInputStream)
            trustStore.setCertificateEntry("AmazonRootCA1", caCert)
        }
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(trustStore)

        // 3. Inicializa el SSLContext forzando TLSv1.2
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(kmf.keyManagers, tmf.trustManagers, null)
        Log.i("SSLContextUtil", "SSLContext inicializado correctamente con TLSv1.2")
        return sslContext
    } catch (e: Exception) {
        Log.e("SSLContextUtil", "Error al inicializar SSLContext", e)
        return null
    }
}
