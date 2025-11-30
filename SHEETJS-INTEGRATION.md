# SheetJS Integration Guide

This document explains how SheetJS (xlsx) is integrated into the Geak4s project using simple JavaScript interop.

## Overview

The project uses:
- **SheetJS (xlsx) 0.20.3** - JavaScript library for Excel file manipulation (security-patched version)
- **Scala.js Dynamic** - Direct JavaScript interop without facades
- **Vite** - For bundling and serving the SheetJS library

## Security Note

We use SheetJS 0.20.3 from the official CDN, which includes fixes for:
- **GHSA-4r6h-8v6p-xvw6**: Prototype Pollution vulnerability
- **GHSA-5pgg-2g8v-p4x9**: Regular Expression Denial of Service (ReDoS)

The older npm versions (0.18.x) have known security vulnerabilities and should not be used in production.

## Why This Approach?

We use direct JavaScript interop instead of ScalablyTyped because:
1. **Vite Compatibility** - Works seamlessly with Vite's ES module system
2. **Simplicity** - No complex build configuration needed
3. **Fast Compilation** - No facade generation overhead
4. **Flexibility** - Easy to use any JavaScript library

## Configuration

### 1. NPM Dependencies (package.json)

```json
{
  "dependencies": {
    "xlsx": "https://cdn.sheetjs.com/xlsx-0.20.3/xlsx-0.20.3.tgz"
  }
}
```

**Note**: We install from the official SheetJS CDN to get the latest security-patched version. The npm registry versions have known vulnerabilities.

### 2. JavaScript Setup (main.js)

```javascript
import * as XLSX from 'xlsx';

// Make XLSX available globally for Scala.js
window.XLSX = XLSX;
```

### 3. Scala.js Code

```scala
import scala.scalajs.js.Dynamic.{global => g}

// Access XLSX from global scope
val XLSX = g.XLSX
```

## Usage Examples

### Import the Library

```scala
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import org.scalajs.dom
```

### Read an Excel File

```scala
def readExcelFile(file: dom.File): Unit = {
  val reader = new dom.FileReader()
  
  reader.onload = (e: dom.Event) => {
    val XLSX = g.XLSX
    val data = reader.result.asInstanceOf[js.typedarray.ArrayBuffer]
    
    // Read workbook
    val options = js.Dynamic.literal("type" -> "array")
    val workbook = XLSX.read(data, options)
    
    // Get first sheet
    val sheetNames = workbook.SheetNames.asInstanceOf[js.Array[String]]
    val sheetName = sheetNames(0)
    val worksheet = workbook.Sheets.selectDynamic(sheetName)
    
    // Convert to JSON
    val jsonData = XLSX.utils.sheet_to_json(worksheet)
      .asInstanceOf[js.Array[js.Dynamic]]
    
    // Process the data
    jsonData.foreach { row =>
      println(s"Name: ${row.Name}, Age: ${row.Age}")
    }
  }
  
  reader.readAsArrayBuffer(file)
}
```

### Write an Excel File

```scala
def exportToExcel(data: List[Person]): Unit = {
  val XLSX = g.XLSX
  
  // Convert Scala data to JS array
  val jsData = js.Array(
    data.map { person =>
      js.Dynamic.literal(
        "Name" -> person.name,
        "Age" -> person.age,
        "Email" -> person.email
      )
    }*
  )
  
  // Create worksheet
  val worksheet = XLSX.utils.json_to_sheet(jsData)
  
  // Create workbook
  val workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, "Data")
  
  // Download file
  XLSX.writeFile(workbook, "export.xlsx")
}
```

## Working with Dynamic Types

### Accessing Properties

```scala
// Direct property access
val value = row.Name

// Dynamic property access (when property name is a variable)
val propertyName = "Name"
val value = row.selectDynamic(propertyName)

// Check if property exists
if (!js.isUndefined(row.Name)) {
  println(row.Name)
}
```

### Type Conversions

```scala
// Convert to String
val name = row.Name.toString

// Convert to Int
val age = row.Age.toString.toIntOption.getOrElse(0)

// Convert to Array
val array = row.items.asInstanceOf[js.Array[js.Dynamic]]

// Convert to Dictionary
val dict = row.asInstanceOf[js.Dictionary[js.Any]]
```

## Supported File Formats

SheetJS supports many formats:
- **Excel**: .xlsx, .xlsm, .xlsb, .xls
- **CSV**: .csv, .txt
- **OpenDocument**: .ods, .fods
- **And many more**: See [SheetJS documentation](https://docs.sheetjs.com/)

## Advanced Features

### Custom Parsing Options

```scala
val options = js.Dynamic.literal(
  "type" -> "array",
  "raw" -> true,
  "dateNF" -> "yyyy-mm-dd"
)

val workbook = XLSX.read(data, options)
```

### Working with Multiple Sheets

```scala
val XLSX = g.XLSX
val workbook = XLSX.read(data, options)

// Get all sheet names
val sheetNames = workbook.SheetNames.asInstanceOf[js.Array[String]]

// Iterate through all sheets
sheetNames.foreach { sheetName =>
  val worksheet = workbook.Sheets.selectDynamic(sheetName)
  val jsonData = XLSX.utils.sheet_to_json(worksheet)
  println(s"Sheet: $sheetName")
}
```

### Cell-Level Access

```scala
val worksheet = workbook.Sheets.selectDynamic(sheetName)

// Access specific cell
val cellA1 = worksheet.selectDynamic("A1")
println(s"Cell A1: $cellA1")

// Access cell value
val cellValue = cellA1.v
println(s"Value: $cellValue")
```

### Writing with Formatting

```scala
val XLSX = g.XLSX

// Create worksheet
val worksheet = XLSX.utils.json_to_sheet(jsData)

// Set column widths
val cols = js.Array(
  js.Dynamic.literal("wch" -> 20),  // Column A width
  js.Dynamic.literal("wch" -> 10),  // Column B width
  js.Dynamic.literal("wch" -> 30)   // Column C width
)
worksheet.updateDynamic("!cols")(cols)

// Create workbook and export
val workbook = XLSX.utils.book_new()
XLSX.utils.book_append_sheet(workbook, worksheet, "Data")
XLSX.writeFile(workbook, "export.xlsx")
```

## Error Handling

Always wrap XLSX operations in try-catch blocks:

```scala
try {
  val XLSX = g.XLSX
  val workbook = XLSX.read(data, options)
  // ... process data
} catch {
  case e: Exception =>
    println(s"Error: ${e.getMessage}")
    dom.console.error("Excel error:", e)
}
```

## Troubleshooting

### XLSX is undefined

**Problem**: `TypeError: Cannot read property 'read' of undefined`

**Solution**: 
1. Check that `npm install` was run
2. Verify that `main.js` imports and exposes XLSX
3. Check browser console: `window.XLSX` should be defined

### Type Errors

**Problem**: Scala compiler errors with dynamic types

**Solution**: Use explicit type casts:
```scala
val array = value.asInstanceOf[js.Array[js.Dynamic]]
val dict = value.asInstanceOf[js.Dictionary[js.Any]]
```

### Property Access Errors

**Problem**: `TypeError: Cannot read property 'X' of undefined`

**Solution**: Check if property exists first:
```scala
if (!js.isUndefined(row.Name)) {
  val name = row.Name.toString
} else {
  val name = "Unknown"
}
```

## Adding More JavaScript Libraries

To add other JavaScript libraries with the same approach:

1. **Install via NPM**:
```bash
npm install library-name
```

2. **Import in main.js**:
```javascript
import * as LibraryName from 'library-name';
window.LibraryName = LibraryName;
```

3. **Use in Scala**:
```scala
val lib = g.LibraryName
lib.someMethod()
```

## Performance Considerations

- **Runtime**: SheetJS is fast for typical spreadsheets (< 10,000 rows)
- **Large files**: For very large files (> 100MB), consider:
  - Server-side processing
  - Streaming APIs
  - Web Workers for background processing
- **Memory**: Large Excel files are loaded entirely into memory

## Demo Component

See `src/main/scala/com/example/geak4s/ExcelDemoView.scala` for a complete working example that demonstrates:
- File upload and reading
- Data display in a table
- Excel file export
- Sample data generation
- Error handling

## Resources

- **SheetJS Documentation**: https://docs.sheetjs.com/
- **Scala.js Documentation**: https://www.scala-js.org/
- **Scala.js Dynamic**: https://www.scala-js.org/doc/interoperability/facade-types.html

## License

- **SheetJS Community Edition**: Apache 2.0 License
- **This project**: MIT License

