Este proyecto integra React Native con código nativo Android para instalar certificados .p12 mediante la API KeyChain, y luego realizar una conexión mTLS segura con AWS IoT Core para obtener credenciales temporales.

Archivos Kotlin y su propósito :

MainActivity.kt
-Es la actividad principal de Android.
-Verifica si el certificado ya fue instalado.
-Si no está instalado, lanza el instalador del .p12 usando KeyChainInstaller.
-Escucha el resultado con onActivityResult para marcar el certificado como instalado en SharedPreferences.

KeyChainInstaller.kt
-Se encarga de lanzar la instalación del certificado .p12.
-Lee el archivo .p12 desde la sandbox de la app (context.filesDir).
-Usa KeyChain.createInstallIntent() para lanzar la ventana de instalación del certificado en Android.

CertificateModule.kt
-Módulo nativo expuesto a React Native como CertificateModule.
-Implementa fetchCredentialsWithSignedCert(), el cual:
-Usa un alias fijo o detectado para acceder al certificado instalado en el sistema.
-Crea un SSLContext usando KeyChainSslContextBuilder.
-Se conecta a AWS IoT Core y devuelve las credenciales a React Native.

KeyChainSslContextBuilder.kt
-Construye el SSLContext necesario para mTLS.
-Obtiene la clave privada y cadena de certificados desde KeyChain (KeyChain.getPrivateKey y -getCertificateChain).
-Carga el certificado raíz de Amazon (AmazonRootCA1.pem) desde assets.
-Crea el SSLContext combinando los KeyManagers y TrustManagers.

AwsIotCredentialsFetcher.kt
-Realiza la conexión HTTPS con mTLS hacia AWS IoT Core.
-Usa el SSLContext para conectarse al endpoint de AWS con el rol, thingName y certificado.
-Devuelve un JSON con las credenciales temporales (accessKeyId, secretAccessKey, sessionToken).

CertificatePackage.kt
-Registra el módulo nativo.
-Registra CertificateModule para que esté disponible desde el código JavaScript.

🛠️ Flujo de ejecución resumido
La app detecta si el certificado ya fue instalado.
Si no lo fue, lo instala mediante la API de KeyChain.
Desde React Native, se invoca el método fetchCredentialsWithSignedCert.
Se construye un SSLContext con el alias del certificado instalado.
Se realiza una conexión segura mTLS con AWS IoT Core.
AWS valida el certificado y devuelve credenciales temporales (por política del rol configurado).
Las credenciales son devueltas al frontend para su uso en la app.

✅ Este enfoque es seguro porque la clave privada no está embebida en el código ni se maneja manualmente: queda protegida en el sistema operativo Android mediante el KeyChain.

Pasos para instalar el certificado empaquetado p12
adb push myapp-iot-client.p12 /sdcard/
adb shell
run-as com.myapp
cp /sdcard/myapp-iot-client.p12 files/

🔴 Eliminar certificado Manualmente desde configuración del emulador/dispositivo
🔁 Borrar flags locales adb shell pm clear com.myapp o .clear() en SharedPreferences
📥 Copiar .p12 al emulador adb push myapp-iot-client.p12 /sdcard/
📦 Moverlo al sandbox adb shell run-as com.myapp cp /sdcard/myapp-iot-client.p12 files/
🚀 Abrir app App detecta que no hay certificado y lanza KeyChainInstaller
🔐 Instalar certificado Aceptás el prompt, se instala como myapp-iot-client
🧪 Alias App selecciona alias automáticamente o te deja elegir
✅ mTLS Se conecta a AWS IoT y devuelve credencial

Borrado manual del certificado instalado

Abrí Configuración en el emulador o dispositivo Android.
Andá a:
Seguridad > Cifrado y credenciales > Credenciales de usuario
(en algunos dispositivos aparece como Credenciales instaladas)
Tocá el certificado llamado myapp-iot-client.
Tocá Eliminar.

🚀 Comando para empaquetar los certificados que se crearon en aws.

openssl pkcs12 -export \
 -in aws-certificate.pem.crt \
 -inkey aws-private.pem.key \
 -certfile AmazonRootCA1.pem \
 -name myapp-iot-client \
 -out myapp-iot-client.p12 \
 -legacy \
 -passout pass:myapp-password

Para antes de compilar dentro de la carpeta android en tu proyecto:
./gradlew clean

Para compilar los archivos kt:
npx react-native run-android
