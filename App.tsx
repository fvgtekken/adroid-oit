/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import { NativeModules } from 'react-native';
const { CertificateModule } = NativeModules;

import React, { useEffect } from 'react';
import type { PropsWithChildren } from 'react';
import { ScrollView, StatusBar, StyleSheet, Text, useColorScheme, View } from 'react-native';

import { Colors, DebugInstructions, Header, LearnMoreLinks, ReloadInstructions } from 'react-native/Libraries/NewAppScreen';

type SectionProps = PropsWithChildren<{
  title: string;
}>;

function Section({ children, title }: SectionProps): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? Colors.white : Colors.black,
          },
        ]}
      >
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? Colors.light : Colors.dark,
          },
        ]}
      >
        {children}
      </Text>
    </View>
  );
}

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    async function testInsertCertificates() {
      try {
        // Insertamos u obtenemos los certificados
        const result = await CertificateModule.fetchCredentials();
        console.log('Resultado certificado KeyStore', result);

        const resultCsrPath = await CertificateModule.generateCsr();
        console.log('📄 CSR exportado en:', resultCsrPath);

        // exportamos la clave publica para importar en amazon
        //const resultExport = await CertificateModule.exportCertificate();
        //console.log('Exported at path:', resultExport);

        // Obtenemos el finger Print de la clave Publica para saber que estamos
        // lidiando con el mismo certificado que vamos a importar en aws
        //const fingerprint = await CertificateModule.getCertificateFingerprint();
        //console.log('🔐 Fingerprint desde KeyStore:', fingerprint);
      } catch (error) {
        console.error('Error: ', error);
      }
    }
    testInsertCertificates();
  }, []);

  /*
   * To keep the template simple and small we're adding padding to prevent view
   * from rendering under the System UI.
   * For bigger apps the reccomendation is to use `react-native-safe-area-context`:
   * https://github.com/AppAndFlow/react-native-safe-area-context
   *
   * You can read more about it here:
   * https://github.com/react-native-community/discussions-and-proposals/discussions/827
   */
  const safePadding = '5%';

  return (
    <View style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} backgroundColor={backgroundStyle.backgroundColor} />
      <ScrollView style={backgroundStyle}>
        <View style={{ paddingRight: safePadding }}>
          <Header />
        </View>
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
            paddingHorizontal: safePadding,
            paddingBottom: safePadding,
          }}
        >
          <Section title='Step One'>
            Edit <Text style={styles.highlight}>App.tsx</Text> to change this screen and then come back to see your edits.
          </Section>
          <Section title='See Your Changes'>
            <ReloadInstructions />
          </Section>
          <Section title='Debug'>
            <DebugInstructions />
          </Section>
          <Section title='Learn More'>Read the docs to discover what to do next:</Section>
          <LearnMoreLinks />
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
