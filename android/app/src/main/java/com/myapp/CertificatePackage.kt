package com.myapp

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

// Este paquete nativo registra el módulo CertificateModule para que pueda ser accedido desde JavaScript.
// No incluye componentes de UI personalizados, por eso createViewManagers devuelve una lista vacía.
class CertificatePackage : ReactPackage {
  
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(CertificateModule(reactContext))
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList()
  }
}
