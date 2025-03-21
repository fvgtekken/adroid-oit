import RNFetchBlob from 'react-native-fetch-blob';

const {config} = RNFetchBlob;

let options = {
  url: 'https://your.url/endpoint',
  trusty: true, // for self-signed certificates
  ciphers: ['ECDHE-RSA-AES128-GCM-SHA256'], // Accepted cipher list
  timeout: 5000, // request will time out in 5 seconds
  sslPinning: {
    certs: ['miCert'], // the name of the certificate file placed in `res/raw`
  },
  headers: {
    'Content-Type': 'text/plain',
  },
};

config(options)
  .fetch('GET', 'https://10.0.2.2:3000')
  .then(response => response.text())
  .then(data => {
    console.log('Respuesta del servidor:', data);
  })
  .catch(error => {
    console.error('Error en la conexión HTTPS:', error);
  });
