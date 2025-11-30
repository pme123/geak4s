# Geak4s

A modern Scala.js application built with Laminar and UI5 Web Components, powered by Vite for fast development and optimized builds.

## 🚀 Features

- **Scala.js 1.17.0** with **Scala 3.6.2** - Modern Scala with the latest features
- **Laminar 17.2.0** - Reactive UI library for elegant and type-safe web development
- **UI5 Web Components 2.1.0** - Professional enterprise-grade UI components
- **SheetJS (xlsx) 0.20.3** - Excel file import/export via JavaScript interop (security-patched version)
- **Vite 6.0** - Lightning-fast development server with HMR (Hot Module Replacement)
- **Modern Architecture** - ES Modules, module splitting, and optimized builds

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java JDK 11+** (for running SBT and Scala.js compiler)
- **Node.js 18+** and **npm** (for Vite and JavaScript dependencies)
- **SBT 1.9.6+** (Scala Build Tool)

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Scala | 3.6.2 | Programming language |
| Scala.js | 1.17.0 | Scala to JavaScript compiler |
| Laminar | 17.2.0 | Reactive UI framework |
| UI5 Web Components | 2.1.0 | UI component library |
| SheetJS (xlsx) | 0.20.3 | Excel file manipulation |
| Vite | 6.0.0 | Build tool and dev server |
| SBT | 1.9.6 | Scala build tool |

## 📦 Installation

1. **Clone the repository** (or navigate to the project directory):
   ```bash
   cd geak4s
   ```

2. **Install JavaScript dependencies**:
   ```bash
   npm install
   ```

3. **Verify SBT installation**:
   ```bash
   sbt --version
   ```

## 🏃 Development Workflow

To run the application in development mode, you need to start **two processes** in separate terminals:

### Terminal 1: Start Scala.js Compiler (Watch Mode)

```bash
sbt ~fastLinkJS
```

This command:
- Compiles Scala code to JavaScript
- Watches for file changes and recompiles automatically
- Outputs to `target/scala-3.6.2/geak4s-fastopt/`

**Note**: Wait for the initial compilation to complete before starting Vite.

### Terminal 2: Start Vite Development Server

```bash
npm run dev
```

This command:
- Starts the Vite development server
- Enables Hot Module Replacement (HMR)
- Serves the application at `http://localhost:5173`

### 🌐 Open in Browser

Once both processes are running, open your browser and navigate to:

```
http://localhost:5173
```

You should see the Hello World demo with an interactive input field.

## 🏗️ Building for Production

To create an optimized production build:

1. **Compile Scala.js with full optimization**:
   ```bash
   sbt fullLinkJS
   ```

2. **Build with Vite**:
   ```bash
   npm run build
   ```

3. **Preview the production build** (optional):
   ```bash
   npm run preview
   ```

The production-ready files will be in the `dist/` directory.

## 📁 Project Structure

```
geak4s/
├── build.sbt                          # SBT build configuration
├── package.json                       # Node.js dependencies and scripts
├── vite.config.js                     # Vite configuration
├── index.html                         # HTML entry point
├── main.js                            # JavaScript entry point
├── README.md                          # This file
├── .gitignore                         # Git ignore rules
├── project/
│   ├── build.properties               # SBT version
│   └── plugins.sbt                    # SBT plugins (Scala.js)
├── src/main/
│   ├── scala/com/example/geak4s/
│   │   ├── Main.scala                 # Application entry point
│   │   └── HelloWorldView.scala       # Hello World component
│   └── resources/
│       └── styles.css                 # Application styles
└── public/                            # Static assets (optional)
```

## 🎨 Key Components

### Main.scala

The application entry point that:
- Initializes the Laminar application
- Renders the main page layout
- Sets up the UI5 Bar header with navigation
- Provides view switching between Hello World and Excel Demo

### HelloWorldView.scala

An interactive demo component featuring:
- UI5 Card with header
- Reactive input field using Laminar's `Var`
- Real-time text transformation (uppercase)
- Modern Scala 3 syntax

### ExcelDemoView.scala

A comprehensive Excel manipulation demo featuring:
- **Import Excel files** - Read .xlsx, .xls, and .csv files
- **Export to Excel** - Generate and download Excel files
- **Data preview** - Display imported data in a table
- **Sample data generation** - Create test data for export
- **SheetJS integration** - Using JavaScript interop via Scala.js Dynamic

### styles.css

Comprehensive styling with:
- CSS Custom Properties for theming
- Responsive design for mobile devices
- Dark mode support
- Smooth animations and transitions
- Professional card-based layout
- Excel demo specific styles

## 🔧 Configuration

### Module Splitting

The project uses Scala.js module splitting for optimized loading:

```scala
scalaJSLinkerConfig ~= {
  _.withModuleKind(ModuleKind.ESModule)
    .withModuleSplitStyle(
      ModuleSplitStyle.SmallModulesFor(List("geak4s"))
    )
}
```

### SheetJS Integration

The project uses direct JavaScript interop to access SheetJS functionality:

**In main.js:**
```javascript
import * as XLSX from 'xlsx';
window.XLSX = XLSX;
```

**In Scala:**
```scala
import scala.scalajs.js.Dynamic.{global => g}

val XLSX = g.XLSX
val workbook = XLSX.read(data, options)
val jsonData = XLSX.utils.sheet_to_json(worksheet)
```

This approach is simpler and fully compatible with Vite's ES module system.

### UI5 Web Components

UI5 components are imported in `main.js`:

```javascript
import "@ui5/webcomponents/dist/Assets.js";
import "@ui5/webcomponents-fiori/dist/Assets.js";
import "@ui5/webcomponents-icons/dist/Assets.js";
```

## 📚 Available Libraries

### UI5 Web Components

This project includes the following UI5 Web Components packages:

- **@ui5/webcomponents** - Core components (Button, Input, Card, etc.)
- **@ui5/webcomponents-fiori** - Fiori-specific components (Bar, ShellBar, etc.)
- **@ui5/webcomponents-icons** - SAP icon library
- **@ui5/webcomponents-compat** - Compatibility layer

For full documentation, visit: [UI5 Web Components](https://sap.github.io/ui5-webcomponents/)

### SheetJS (xlsx)

SheetJS is integrated via JavaScript interop for Excel file manipulation:

- **Read Excel files** - Support for .xlsx, .xls, .csv, and more
- **Write Excel files** - Generate Excel files from data
- **Data conversion** - Convert between Excel and JSON formats
- **Simple integration** - Direct JavaScript interop via Scala.js Dynamic
- **Security** - Uses version 0.20.3 with security patches for known vulnerabilities

**Security Note**: We install SheetJS 0.20.3 from the official CDN (`https://cdn.sheetjs.com/xlsx-0.20.3/xlsx-0.20.3.tgz`) instead of npm to avoid known security vulnerabilities in older versions (GHSA-4r6h-8v6p-xvw6, GHSA-5pgg-2g8v-p4x9).

For full documentation, visit: [SheetJS Documentation](https://docs.sheetjs.com/)

## 🎯 Next Steps

Here are some ideas to extend this application:

1. **Add Routing** - Integrate a routing library for multi-page navigation
2. **State Management** - Implement a more complex state management pattern
3. **API Integration** - Connect to a backend API using Fetch or Axios
4. **More Components** - Explore additional UI5 components (Tables, Charts, Dialogs)
5. **Advanced Excel Features** - Add styling, formulas, charts to Excel exports
6. **More NPM Libraries** - Integrate other JavaScript libraries via Vite and JS interop
7. **Testing** - Add unit tests with ScalaTest or uTest
8. **PWA Support** - Convert to a Progressive Web App

## 🐛 Troubleshooting

### Vite can't find the Scala.js output

**Solution**: Make sure `sbt ~fastLinkJS` has completed at least one compilation before starting Vite.



### UI5 components not rendering

**Solution**: Ensure all UI5 assets are imported in `main.js` and the page has fully loaded.

### Excel import/export not working

**Solution**:
1. Make sure the SheetJS library is properly loaded (check browser console for errors)
2. Verify that `window.XLSX` is available in the browser console
3. Ensure you're using a modern browser with FileReader API support

### Port 5173 already in use

**Solution**: Either stop the process using that port or configure Vite to use a different port in `vite.config.js`:

```javascript
export default defineConfig({
  plugins: [scalaJSPlugin()],
  server: {
    port: 3000
  }
});
```

### SBT compilation errors

**Solution**: Make sure you're using Java 11+ and SBT 1.9.6+. Clear the cache if needed:

```bash
sbt clean
```

### NPM dependency issues

**Solution**: Delete `node_modules` and lock files, then reinstall:

```bash
rm -rf node_modules package-lock.json
npm install
```

## 📄 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 👨‍💻 Author

Built with ❤️ using Scala.js, Laminar, and UI5 Web Components.

---

**Happy Coding! 🎉**

