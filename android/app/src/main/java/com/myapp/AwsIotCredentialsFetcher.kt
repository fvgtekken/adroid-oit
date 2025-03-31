package com.myapp

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext


object AwsIotCredentialsFetcher {

    @Throws(Exception::class)
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
