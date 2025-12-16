import './src/main/resources/styles.css'
import 'scalajs:main.js'
import "@ui5/webcomponents/dist/Assets.js";
import "@ui5/webcomponents-fiori/dist/Assets.js";
import "@ui5/webcomponents-icons/dist/Assets.js";

// Import SheetJS
import * as XLSX from 'xlsx';

// Make XLSX available globally for Scala.js
window.XLSX = XLSX;

main();

