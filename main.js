// IMPORTANT: Import env-config FIRST to set up environment variables
// before Scala.js code runs
import './env-config.js';

import './src/main/resources/styles.css'

// Import SheetJS
import * as XLSX from 'xlsx';

// Make libraries available globally for Scala.js
window.XLSX = XLSX;

// Load Google API libraries dynamically
// We use the new Google Identity Services (GIS) instead of gapi-script
function loadGoogleAPIs() {
  return new Promise((resolve) => {
    let gapiLoaded = false;
    let gisLoaded = false;

    const checkBothLoaded = () => {
      if (gapiLoaded && gisLoaded) {
        console.log('✅ All Google API libraries loaded');
        resolve();
      }
    };

    // Load Google API Client Library
    const gapiScript = document.createElement('script');
    gapiScript.src = 'https://apis.google.com/js/api.js';
    gapiScript.async = true;
    gapiScript.defer = true;
    gapiScript.onload = () => {
      console.log('✅ Google API Client Library loaded');
      gapiLoaded = true;
      checkBothLoaded();
    };
    gapiScript.onerror = () => {
      console.error('❌ Failed to load Google API Client Library');
      gapiLoaded = true; // Continue anyway
      checkBothLoaded();
    };
    document.head.appendChild(gapiScript);

    // Load Google Identity Services Library
    const gisScript = document.createElement('script');
    gisScript.src = 'https://accounts.google.com/gsi/client';
    gisScript.async = true;
    gisScript.defer = true;
    gisScript.onload = () => {
      console.log('✅ Google Identity Services loaded');
      gisLoaded = true;
      checkBothLoaded();
    };
    gisScript.onerror = () => {
      console.error('❌ Failed to load Google Identity Services');
      gisLoaded = true; // Continue anyway
      checkBothLoaded();
    };
    document.head.appendChild(gisScript);
  });
}

// Load Google APIs first, then import Scala.js
loadGoogleAPIs().then(() => {
  console.log('🚀 Starting Scala.js application...');
  // Import Scala.js main module (this will call main() automatically due to scalaJSUseMainModuleInitializer)
  import('scalajs:main.js').then(module => {
    console.log('✅ Scala.js module loaded');
    // Don't call main() explicitly - it's already called automatically
  }).catch(error => {
    console.error('❌ Failed to load Scala.js module:', error);
  });
});

