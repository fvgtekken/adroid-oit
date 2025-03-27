// SecureKeyManager.kt
package com.myapp

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.x500.X500Principal

// Para generar CSR con BouncyCastle
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.security.cert.CertificateEncodingException
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.ContentSigner


object SecureKeyManager {
    private const val KEY_ALIAS = "my_secure_iot_key"
    private const val TAG = "SecureKeyManager"

   // Checks if certificates exists
    /*************************************************/ 
    /*************************************************/ 

    fun ensureKeyExists(context: Context): Boolean {

        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (keyStore.containsAlias(KEY_ALIAS)) {
                Log.i(TAG, "La clave ya existe: $KEY_ALIAS")
                true
            } else {
                Log.i(TAG, "La clave no existe. Generando...")

                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 1) // La clave será válida 1 año

                val spec = KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(X500Principal("CN=$KEY_ALIAS"))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()

                val kpGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
                kpGenerator.initialize(spec)
                kpGenerator.generateKeyPair()

                Log.i(TAG, "Clave generada correctamente: $KEY_ALIAS")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generando clave en AndroidKeyStore", e)
            false
        }
    }

    fun getCertificate(): java.security.cert.Certificate? {

        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.getCertificate(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener certificado del KeyStore", e)
            null
        }
    }

    fun getKeyAlias(): String = KEY_ALIAS

    
    
    // Export cerificate pem
    /*************************************************/ 
    /*************************************************/ 
    fun exportCertificateToPemFile(context: Context): String?{

        return try {
            val cert = getCertificate()
            if (cert == null) {
                Log.e(TAG, "No se encontró el certificado para exportar.")
                return null
            }

            // Convertir certificado a Base64 en formato PEM
           val base64 = Base64.encodeToString(cert.encoded, Base64.NO_WRAP)

            val pem = "-----BEGIN CERTIFICATE-----\n" +
                    base64.chunked(64).joinToString("\n") +
                    "\n-----END CERTIFICATE-----\n"

            // Guardar en external files dir (accesible con ADB pull)
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir == null) {
                Log.e(TAG, "No se pudo acceder al directorio externo.")
                return null
            }

            val certFile = File(externalDir, "myapp-iot-cert.pem")
            FileOutputStream(certFile).use { it.write(pem.toByteArray()) }

            Log.i(TAG, "✅ Certificado exportado exitosamente a: ${certFile.absolutePath}")
            return certFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error al exportar certificado", e)
            return null
        }
    }



    //  Get FingerPrint Public Key
    /*************************************************/ 
    /*************************************************/ 
    fun getCertificateFingerprint(context: Context): String {

        val alias = "my_secure_iot_key"
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val cert = keyStore.getCertificate(alias)
        ?: throw Exception("No se encontró el certificado para el alias: $alias")

        val sha256 = MessageDigest.getInstance("SHA-256").digest(cert.encoded)

        // Formateamos como lo hace openssl (en pares hex separados por :)
        return sha256.joinToString(":") { byte -> "%02X".format(byte) }

    }

    // Generate CSR Certificate
    /*************************************************/ 
    /*************************************************/ 
    fun generateCsr(context: Context): String {
        try {
            Security.addProvider(BouncyCastleProvider())

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
                ?: throw Exception("🔐 Private key not found in AndroidKeyStore")

            val privateKey = privateKeyEntry.privateKey
            val publicKey = privateKeyEntry.certificate.publicKey

            val x500Name = X500Name("CN=$KEY_ALIAS")

           
            val pubKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
            val builder = PKCS10CertificationRequestBuilder(x500Name, pubKeyInfo)
            
            val signer: ContentSigner = JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("AndroidKeyStoreBCWorkaround")
                .build(privateKey)

            val csr: PKCS10CertificationRequest = builder.build(signer)

            val pem = buildString {
                append("-----BEGIN CERTIFICATE REQUEST-----\n")
                append(
                    Base64.encodeToString(csr.encoded, Base64.NO_WRAP)
                        .chunked(64)
                        .joinToString("\n")
                )
                append("\n-----END CERTIFICATE REQUEST-----\n")
            }

            val outFile = File(context.getExternalFilesDir(null), "myapp-iot.csr")
            FileOutputStream(outFile).use { it.write(pem.toByteArray()) }

            Log.i(TAG, "📄 CSR generado exitosamente en: ${outFile.absolutePath}")
            return outFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al generar CSR", e)
            throw e
        }
    }



}