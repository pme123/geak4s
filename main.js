import './src/main/resources/styles.css'
import 'scalajs:main.js'

// Import SheetJS
import * as XLSX from 'xlsx';

// Make XLSX available globally for Scala.js
window.XLSX = XLSX;

main();

