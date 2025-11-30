import "@ui5/webcomponents/dist/Assets.js";
import "@ui5/webcomponents-fiori/dist/Assets.js";
import "@ui5/webcomponents-icons/dist/Assets.js";

// Import SheetJS
import * as XLSX from 'xlsx';

// Make XLSX available globally for Scala.js
window.XLSX = XLSX;

import { main } from "./target/scala-3.6.2/geak4s-fastopt/main.js";

main();

