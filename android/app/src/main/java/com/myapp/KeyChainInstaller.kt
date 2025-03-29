import android.app.Activity
import android.content.Context
import android.content.Intent
import android.security.KeyChain
import android.security.KeyChain.EXTRA_PKCS12
import android.util.Log
import java.io.File

object KeyChainInstaller {
    private const val TAG = "KeyChainInstaller"
    private const val REQUEST_CODE = 1001  // mismo que en MainActivity

    fun installP12FromAppFiles(context: Context, activity: Activity, fileName: String) {
        try {
            val p12File = File(context.filesDir, fileName)

            if (!p12File.exists()) {
                Log.e(TAG, "❌ El archivo $fileName no se encontró en filesDir")
                return
            }

            val p12Bytes = p12File.readBytes()

            val intent = KeyChain.createInstallIntent().apply {
                putExtra(EXTRA_PKCS12, p12Bytes)
            }

            activity.startActivityForResult(intent, REQUEST_CODE)

            Log.i(TAG, "📦 Lanzado KeyChain install intent para $fileName desde sandbox")

        } catch (e: Exception) {
            Log.e(TAG, "🚨 Error al intentar instalar el certificado con KeyChain", e)
        }
    }
}
