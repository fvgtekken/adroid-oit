import android.app.Activity
import android.security.KeyChain
import android.security.KeyChainAliasCallback

object KeyChainLoader {
    fun chooseAlias(activity: Activity, callback: (String?) -> Unit) {
        KeyChain.choosePrivateKeyAlias(
            activity,
            object : KeyChainAliasCallback {
                override fun alias(alias: String?) {
                    callback(alias)
                }
            },
            null, // keyTypes: any
            null, // issuers: any
            null, // uri: any
            -1,   // port: any
            null  // alias preseleccionado
        )
    }
}
