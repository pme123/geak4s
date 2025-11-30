# Quick Start Guide

Get your Geak4s application running in 3 simple steps!

## Step 1: Install Dependencies

```bash
npm install
```

## Step 2: Start Development (2 Terminals)

### Terminal 1 - Scala.js Compiler
```bash
sbt ~fastLinkJS
```
⏳ Wait for "Compilation completed" message

### Terminal 2 - Vite Dev Server
```bash
npm run dev
```

## Step 3: Open Browser

Navigate to: **http://localhost:5173**

---

## 🎉 That's it!

You should now see the application with two demos:
- **Hello World** - Interactive input field with reactive updates
- **Excel Demo** - Import/export Excel files with SheetJS

## Common Commands

| Command | Description |
|---------|-------------|
| `sbt ~fastLinkJS` | Watch mode compilation (development) |
| `sbt fullLinkJS` | Optimized compilation (production) |
| `npm run dev` | Start dev server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `sbt clean` | Clean build artifacts |

## Need Help?

Check the full [README.md](README.md) for detailed documentation.

