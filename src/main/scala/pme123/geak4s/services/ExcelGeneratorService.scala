package pme123.geak4s.services

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import pme123.geak4s.domain.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.domain.building.*
import pme123.geak4s.domain.envelope.*
import pme123.geak4s.domain.hvac.*
import pme123.geak4s.domain.energy.*

/** Service for generating Excel files using template and exporting as XLS */
object ExcelGeneratorService:

  private val XLSX = js.Dynamic.global.XLSX

  /** Create a brand new Excel workbook using template (preserves metadata and named ranges) */
  def createNewWorkbook(project: GeakProject, fileName: String = ""): Unit =
    try
      val exportFileName = if fileName.isEmpty then
        val projectName = if project.project.projectName.isEmpty then "geak_export" else project.project.projectName
        projectName.replace(" ", "-") + ".xls"
      else
        fileName

      dom.console.log("🔄 Creating new Excel workbook from template...")
      dom.console.log(s"📁 Export filename: $exportFileName")
      dom.console.log("📥 Loading XLSX template (preserves metadata and named ranges)...")

      // Load the XLSX template file from public folder
      val future = dom.fetch("/geak_newproject.xlsx")
        .toFuture
        .flatMap { response =>
          dom.console.log(s"📡 Fetch response status: ${response.status} ${response.statusText}")
          if response.ok then
            dom.console.log("✅ Template fetch successful, reading array buffer...")
            response.arrayBuffer().toFuture
          else
            val errorMsg = s"Failed to load template: ${response.status} ${response.statusText}"
            dom.console.error(s"❌ $errorMsg")
            Future.failed(new Exception(errorMsg))
        }
        .map { arrayBuffer =>
          try
            dom.console.log(s"✅ XLSX Template loaded, size: ${arrayBuffer.byteLength} bytes")

            // Read the XLSX template (this preserves all metadata, named ranges, and DropDownData)
            dom.console.log("📖 Reading XLSX workbook...")
            val workbook = XLSX.read(arrayBuffer, js.Dynamic.literal(
              `type` = "array",
              cellStyles = true
            ))

            dom.console.log(s"✅ Workbook loaded with all metadata and named ranges")
            dom.console.log(s"   Sheets: ${workbook.SheetNames.asInstanceOf[js.Array[String]].mkString(", ")}")

            // Update all data sheets with project data
            dom.console.log("📝 Updating sheets with project data...")
            updateProjectSheet(workbook, project.project)
            updateBuildingUsageSheet(workbook, project)
            updateEnvelopeSheets(workbook, project)
            updateHvacSheets(workbook, project)
            updateEnergySheet(workbook, project)

            dom.console.log("💾 Writing file in XLS (BIFF8) format...")

            // Write as XLS format (BIFF8)
            XLSX.writeFile(workbook, exportFileName, js.Dynamic.literal(
              bookType = "biff8",
              cellStyles = false  // Disable cell styles for better XLS compatibility
            ))

            dom.console.log("✅✅✅ Excel XLS file created successfully with all metadata!")
          catch
            case ex: Exception =>
              dom.console.error(s"❌ Error processing template: ${ex.getMessage}")
              dom.console.error("Stack trace:")
              ex.printStackTrace()
              throw ex
        }

      future.failed.foreach { error =>
        dom.console.error(s"❌ Future failed: ${error.getMessage}")
        error.printStackTrace()
      }
    catch
      case ex: Exception =>
        dom.console.error(s"❌ Error creating Excel file: ${ex.getMessage}")
        dom.console.error("Stack trace:")
        ex.printStackTrace()

  /** Generate Excel file from GeakProject data using template */
  def generateExcel(project: GeakProject, fileName: String = ""): Unit =
    try
      // Generate filename from project name if not provided
      val exportFileName = if fileName.isEmpty then
        val projectName = if project.project.projectName.isEmpty then "geak_export" else project.project.projectName
        projectName.replace(" ", "-") + ".xls"
      else
        fileName

      dom.console.log("🔄 Starting Excel XLS generation...")
      dom.console.log(s"📁 Export filename: $exportFileName")
      dom.console.log("📥 Loading XLSX template from public folder (will convert to XLS)...")

      // Load the XLSX template file from public folder (XLS files are often encrypted)
      val future = dom.fetch("/geak_newproject.xlsx")
        .toFuture
        .flatMap { response =>
          dom.console.log(s"📡 Fetch response status: ${response.status} ${response.statusText}")
          if response.ok then
            dom.console.log("✅ Template fetch successful, reading array buffer...")
            response.arrayBuffer().toFuture
          else
            val errorMsg = s"Failed to load template: ${response.status} ${response.statusText}"
            dom.console.error(s"❌ $errorMsg")
            Future.failed(new Exception(errorMsg))
        }
        .map { arrayBuffer =>
          try
            dom.console.log(s"✅ XLSX Template loaded, size: ${arrayBuffer.byteLength} bytes")

            // Read the XLSX template
            dom.console.log("📖 Reading XLSX workbook...")
            val workbook = XLSX.read(arrayBuffer, js.Dynamic.literal(
              `type` = "array",
              cellStyles = true
            ))

            dom.console.log(s"✅ Workbook loaded, sheets: ${workbook.SheetNames.asInstanceOf[js.Array[String]].mkString(", ")}")

            // Update all sheets with project data
            dom.console.log("📝 Updating sheets with project data...")
            updateProjectSheet(workbook, project.project)
            updateBuildingUsageSheet(workbook, project)
            updateEnvelopeSheets(workbook, project)
            updateHvacSheets(workbook, project)
            updateEnergySheet(workbook, project)

            dom.console.log("💾 About to write file in XLS format...")

            // Write as XLS format (BIFF8) - using biff8 explicitly
            XLSX.writeFile(workbook, exportFileName, js.Dynamic.literal(
              bookType = "biff8",
              cellStyles = false  // Disable cell styles for better XLS compatibility
            ))

            dom.console.log("✅✅✅ Excel XLS file generated successfully!")
          catch
            case ex: Exception =>
              dom.console.error(s"❌ Error processing template: ${ex.getMessage}")
              dom.console.error("Stack trace:")
              ex.printStackTrace()
              throw ex
        }

      future.failed.foreach { error =>
        dom.console.error(s"❌ Future failed: ${error.getMessage}")
        error.printStackTrace()
      }
    catch
      case ex: Exception =>
        dom.console.error(s"❌ Error generating Excel file: ${ex.getMessage}")
        dom.console.error("Stack trace:")
        ex.printStackTrace()

  /** Check if workbook has a sheet with given name */
  private def hasSheet(workbook: js.Dynamic, sheetName: String): Boolean =
    val sheetNames = workbook.SheetNames.asInstanceOf[js.Array[String]]
    sheetNames.contains(sheetName)

  /** Get cell value as string */
  private def getCellValue(sheet: js.Dynamic, cell: String): Option[String] =
    try
      val cellObj = sheet.selectDynamic(cell)
      if cellObj != null && !js.isUndefined(cellObj) then
        val value = cellObj.v.toString.trim
        if value.nonEmpty then Some(value) else None
      else None
    catch
      case _: Exception => None

  /** Set cell value in sheet using SheetJS utils */
  private def setCellValue(sheet: js.Dynamic, cell: String, value: String): Unit =
    try
      // Ensure cell object exists
      val cellObj = if js.isUndefined(sheet.selectDynamic(cell)) || sheet.selectDynamic(cell) == null then
        js.Dynamic.literal()
      else
        sheet.selectDynamic(cell)

      // Set cell properties for XLS export
      cellObj.updateDynamic("v")(value)      // raw value
      cellObj.updateDynamic("w")(value)      // formatted value
      cellObj.updateDynamic("t")("s")        // string type

      // Assign the cell back to the sheet
      sheet.updateDynamic(cell)(cellObj)

    catch
      case ex: Exception =>
        dom.console.error(s"Could not set cell $cell to '$value': ${ex.getMessage}")
        ex.printStackTrace()

  /** Set numeric cell value in sheet */
  private def setCellNumeric(sheet: js.Dynamic, cell: String, value: Double): Unit =
    try
      val cellObj = if js.isUndefined(sheet.selectDynamic(cell)) || sheet.selectDynamic(cell) == null then
        js.Dynamic.literal()
      else
        sheet.selectDynamic(cell)

      cellObj.updateDynamic("v")(value)
      cellObj.updateDynamic("w")(value.toString)
      cellObj.updateDynamic("t")("n")  // number type

      sheet.updateDynamic(cell)(cellObj)
    catch
      case ex: Exception =>
        dom.console.error(s"Could not set numeric cell $cell to $value: ${ex.getMessage}")

  /** Set formula in cell */
  private def setCellFormula(sheet: js.Dynamic, cell: String, formula: String): Unit =
    try
      val cellObj = if js.isUndefined(sheet.selectDynamic(cell)) || sheet.selectDynamic(cell) == null then
        js.Dynamic.literal()
      else
        sheet.selectDynamic(cell)

      cellObj.updateDynamic("f")(formula)
      cellObj.updateDynamic("t")("f")  // formula type

      sheet.updateDynamic(cell)(cellObj)
    catch
      case ex: Exception =>
        dom.console.error(s"Could not set formula in cell $cell: ${ex.getMessage}")

  /** Update project information sheet */
  private def updateProjectSheet(workbook: js.Dynamic, project: Project): Unit =
    try
      val sheet = workbook.Sheets.selectDynamic("Projekt")

      dom.console.log("=== UPDATING PROJECT SHEET ===")

      // Update project basic info
      // Row 2: B2=Projektbezeichnung, E2=Date
      setCellValue(sheet, "B2", project.projectName)
      setCellValue(sheet, "E2", project.generatedDate)

      // Update client info
      setCellValue(sheet, "B5", project.client.salutation.toString)
      setCellValue(sheet, "B6", project.client.name1.getOrElse(""))
      setCellValue(sheet, "B7", project.client.name2.getOrElse(""))

      val clientAddressStr = Seq(
        project.client.address.street,
        project.client.address.houseNumber
      ).flatten.mkString(" ")
      setCellValue(sheet, "B8", clientAddressStr)

      setCellValue(sheet, "B9", project.client.poBox.getOrElse(""))
      setCellValue(sheet, "B10", project.client.address.zipCode.getOrElse(""))
      setCellValue(sheet, "B11", project.client.address.city.getOrElse(""))
      setCellValue(sheet, "B12", project.client.address.country.getOrElse(""))
      setCellValue(sheet, "B13", project.client.email.getOrElse(""))
      setCellValue(sheet, "B14", project.client.phone1.getOrElse(""))
      setCellValue(sheet, "B15", project.client.phone2.getOrElse(""))

      // Update building location
      setCellValue(sheet, "B18", project.buildingLocation.address.zipCode.getOrElse(""))
      setCellValue(sheet, "B19", project.buildingLocation.address.city.getOrElse(""))
      setCellValue(sheet, "B20", project.buildingLocation.municipality.getOrElse(""))
      setCellValue(sheet, "B21", project.buildingLocation.address.street.getOrElse(""))
      setCellValue(sheet, "B22", project.buildingLocation.address.houseNumber.getOrElse(""))
      setCellValue(sheet, "B23", project.buildingLocation.buildingName.getOrElse(""))
      setCellValue(sheet, "B26", project.buildingLocation.parcelNumber.getOrElse(""))

      // Update building data
      setCellValue(sheet, "B24", project.buildingData.constructionYear.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B25", project.buildingData.lastRenovationYear.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B27", project.buildingData.weatherStation.getOrElse(""))
      setCellValue(sheet, "B28", project.buildingData.weatherStationValues.getOrElse(""))
      setCellValue(sheet, "B29", project.buildingData.altitude.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B30", project.buildingData.energyReferenceArea.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B31", project.buildingData.clearRoomHeight.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B32", project.buildingData.numberOfFloors.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B33", project.buildingData.buildingWidth.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B34", project.buildingData.constructionType.getOrElse(""))
      setCellValue(sheet, "B35", project.buildingData.groundPlanType.getOrElse(""))

      // Update descriptions
      setCellValue(sheet, "B38", project.descriptions.buildingDescription.getOrElse(""))
      setCellValue(sheet, "B41", project.descriptions.envelopeDescription.getOrElse(""))
      setCellValue(sheet, "B44", project.descriptions.hvacDescription.getOrElse(""))

      // Update EGID/EDID entries (starting from row 6)
      var egidRow = 6
      project.egidEdidGroup.entries.foreach { entry =>
        val addressParts = Seq(
          entry.address.street,
          entry.address.houseNumber
        ).flatten
        val addressStr = addressParts.mkString(" ")

        setCellValue(sheet, s"D$egidRow", entry.egid.getOrElse(""))
        setCellValue(sheet, s"E$egidRow", entry.edid.getOrElse(""))
        setCellValue(sheet, s"F$egidRow", addressStr)
        setCellValue(sheet, s"G$egidRow", entry.address.zipCode.getOrElse(""))
        setCellValue(sheet, s"H$egidRow", entry.address.city.getOrElse(""))

        egidRow += 1
      }

      dom.console.log("✅ Project sheet updated")
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating Project sheet: ${ex.getMessage}")
        ex.printStackTrace()

  /** Update building usage sheet */
  private def updateBuildingUsageSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      if hasSheet(workbook, "Gebäudenutzungen") && project.buildingUsages.nonEmpty then
        val sheet = workbook.Sheets.selectDynamic("Gebäudenutzungen")

        var row = 2 // Start from row 2 (after header)
        project.buildingUsages.foreach { usage =>
          setCellValue(sheet, s"A$row", usage.usageType)
          setCellValue(sheet, s"B$row", usage.usageSubType.getOrElse(""))
          setCellValue(sheet, s"C$row", usage.area.toString)
          setCellValue(sheet, s"D$row", usage.areaPercentage.map(_.toString).getOrElse(""))
          setCellValue(sheet, s"E$row", usage.constructionYear.map(_.toString).getOrElse(""))
          row += 1
        }

        dom.console.log("✅ Building usage sheet updated")
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating Building Usage sheet: ${ex.getMessage}")


  /** Update envelope sheets */
  private def updateEnvelopeSheets(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      // Roofs & Ceilings
      if hasSheet(workbook, "Dächer und Decken") && project.roofsCeilings.nonEmpty then
        val sheet = workbook.Sheets.selectDynamic("Dächer und Decken")
        var row = 2
        project.roofsCeilings.foreach { roof =>
          setCellValue(sheet, s"A$row", roof.code)
          setCellValue(sheet, s"B$row", roof.description)
          setCellValue(sheet, s"C$row", roof.roofType)
          setCellValue(sheet, s"D$row", roof.area.toString)
          setCellValue(sheet, s"E$row", roof.uValue.toString)
          setCellValue(sheet, s"F$row", roof.renovationYear.map(_.toString).getOrElse(""))
          row += 1
        }
        dom.console.log("✅ Roofs & Ceilings sheet updated")

      // Walls
      if hasSheet(workbook, "Wände") && project.walls.nonEmpty then
        val sheet = workbook.Sheets.selectDynamic("Wände")
        var row = 2
        project.walls.foreach { wall =>
          setCellValue(sheet, s"A$row", wall.code)
          setCellValue(sheet, s"B$row", wall.description)
          setCellValue(sheet, s"C$row", wall.wallType)
          setCellValue(sheet, s"D$row", wall.area.toString)
          setCellValue(sheet, s"E$row", wall.uValue.toString)
          setCellValue(sheet, s"F$row", wall.renovationYear.map(_.toString).getOrElse(""))
          row += 1
        }
        dom.console.log("✅ Walls sheet updated")

      // Windows & Doors
      if hasSheet(workbook, "Fenster und Türen") && project.windowsDoors.nonEmpty then
        val sheet = workbook.Sheets.selectDynamic("Fenster und Türen")
        var row = 2
        project.windowsDoors.foreach { window =>
          setCellValue(sheet, s"A$row", window.code)
          setCellValue(sheet, s"B$row", window.description)
          setCellValue(sheet, s"C$row", window.windowType)
          setCellValue(sheet, s"D$row", window.area.toString)
          setCellValue(sheet, s"E$row", window.uValue.toString)
          setCellValue(sheet, s"F$row", window.gValue.toString)
          row += 1
        }
        dom.console.log("✅ Windows & Doors sheet updated")
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating Envelope sheets: ${ex.getMessage}")

  /** Update HVAC sheets */
  private def updateHvacSheets(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      // Heat Producers
      if hasSheet(workbook, "Wärmeerzeuger") && project.heatProducers.nonEmpty then
        val sheet = workbook.Sheets.selectDynamic("Wärmeerzeuger")
        var row = 2
        project.heatProducers.foreach { producer =>
          setCellValue(sheet, s"A$row", producer.code)
          setCellValue(sheet, s"B$row", producer.description)
          setCellValue(sheet, s"C$row", producer.energySource)
          setCellValue(sheet, s"D$row", producer.efficiencyHeating.toString)
          setCellValue(sheet, s"E$row", producer.efficiencyHotWater.toString)
          row += 1
        }
        dom.console.log("✅ Heat Producers sheet updated")
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating HVAC sheets: ${ex.getMessage}")

  /** Update energy production sheet */
  private def updateEnergySheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      if hasSheet(workbook, "Elektrizitätsprod") && project.electricityProducers.nonEmpty then
        val sheet = workbook.Sheets.selectDynamic("Elektrizitätsprod")
        var row = 2
        project.electricityProducers.foreach { producer =>
          setCellValue(sheet, s"A$row", producer.code)
          setCellValue(sheet, s"B$row", producer.producerType)
          setCellValue(sheet, s"C$row", producer.description)
          setCellValue(sheet, s"D$row", producer.annualProduction.toString)
          setCellValue(sheet, s"E$row", producer.gridFeedIn.toString)
          row += 1
        }
        dom.console.log("✅ Electricity Producers sheet updated")
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating Energy sheet: ${ex.getMessage}")

  // ========== Methods for creating sheets from scratch ==========

  /** Create project sheet from scratch - matching exact template structure */
  private def createProjectSheet(workbook: js.Dynamic, project: Project): Unit =
    try
      dom.console.log("Creating Project sheet with exact template structure...")

      // Create worksheet data matching the exact template structure
      val data = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Projekt", "", "", "", "", "", ""),
        // Row 2: Project name and date
        js.Array("Projektbezeichnung", project.projectName, "", "Template R6.8 generiert am:", project.generatedDate),
        // Row 3: Empty
        js.Array("", ""),
        // Row 4: Section headers
        js.Array("Auftraggeber", "", "", "EGID_EDID-Gruppe", "", "", ""),
        // Row 5: Field labels and EGID headers
        js.Array("Anrede", project.client.salutation.toString, "", "EGID", "EDID", "Adresse", "PLZ/Ort"),
        // Row 6: Name 1
        js.Array("Name 1", project.client.name1.getOrElse("")),
        // Row 7: Name 2
        js.Array("Name 2", project.client.name2.getOrElse("")),
        // Row 8: Address
        js.Array("Adresse", s"${project.client.address.street.getOrElse("")} ${project.client.address.houseNumber.getOrElse("")}".trim),
        // Row 9: Postfach
        js.Array("Postfach", project.client.poBox.getOrElse("")),
        // Row 10: PLZ
        js.Array("PLZ", project.client.address.zipCode.getOrElse("")),
        // Row 11: Ort
        js.Array("Ort", project.client.address.city.getOrElse("")),
        // Row 12: Land
        js.Array("Land", project.client.address.country.getOrElse("")),
        // Row 13: E-Mail
        js.Array("E-Mail", project.client.email.getOrElse("")),
        // Row 14: Telefon 1
        js.Array("Telefon 1", project.client.phone1.getOrElse("")),
        // Row 15: Telefon 2
        js.Array("Telefon 2", project.client.phone2.getOrElse("")),
        // Row 16: Empty
        js.Array("", ""),
        // Row 17: Building section header
        js.Array("Gebäude", ""),
        // Row 18: PLZ
        js.Array("PLZ", project.buildingLocation.address.zipCode.getOrElse("")),
        // Row 19: Ort
        js.Array("Ort", project.buildingLocation.address.city.getOrElse("")),
        // Row 20: Gemeinde
        js.Array("Gemeinde", project.buildingLocation.municipality.getOrElse("")),
        // Row 21: Strasse
        js.Array("Strasse", project.buildingLocation.address.street.getOrElse("")),
        // Row 22: Hausnummer
        js.Array("Hausnummer", project.buildingLocation.address.houseNumber.getOrElse("")),
        // Row 23: Gebäudebezeichnung
        js.Array("Gebäudebezeichnung", project.buildingLocation.buildingName.getOrElse("")),
        // Row 24: Baujahr
        js.Array("Baujahr", project.buildingData.constructionYear.map(_.toString).getOrElse("")),
        // Row 25: Jahr der letzten Gesamtsanierung
        js.Array("Jahr der letzten Gesamtsanierung", project.buildingData.lastRenovationYear.map(_.toString).getOrElse("")),
        // Row 26: Parzellen-Nummer
        js.Array("Parzellen-Nummer", project.buildingLocation.parcelNumber.getOrElse("")),
        // Row 27: Klimastation
        js.Array("Klimastation", project.buildingData.weatherStation.getOrElse("")),
        // Row 28: Bestbekannte Werte Klimastation
        js.Array("Bestbekannte Werte Klimastation", project.buildingData.weatherStationValues.getOrElse("")),
        // Row 29: Höhe ü. M.
        js.Array("Höhe ü. M.", project.buildingData.altitude.map(_.toString).getOrElse("")),
        // Row 30: Energiebezugsfläche
        js.Array("Energiebezugsfläche [m²]", project.buildingData.energyReferenceArea.map(_.toString).getOrElse("")),
        // Row 31: Lichte Raumhöhe
        js.Array("Lichte Raumhöhe [m]", project.buildingData.clearRoomHeight.map(_.toString).getOrElse("")),
        // Row 32: Anzahl der Vollgeschosse
        js.Array("Anzahl der Vollgeschosse", project.buildingData.numberOfFloors.map(_.toString).getOrElse("")),
        // Row 33: Gebäudebreite
        js.Array("Gebäudebreite [m]", project.buildingData.buildingWidth.map(_.toString).getOrElse("")),
        // Row 34: Bauweise Gebäude
        js.Array("Bauweise Gebäude", project.buildingData.constructionType.getOrElse("")),
        // Row 35: Grundrisstyp
        js.Array("Grundrisstyp", project.buildingData.groundPlanType.getOrElse("")),
        // Row 36: Empty
        js.Array(""),
        // Row 37: Descriptions header
        js.Array("Beschreibungen im Ist-Zustand", ""),
        // Row 38: Prioritization
        js.Array("Prioritization:", project.descriptions.buildingDescription.getOrElse("")),
        // Row 39: Empty
        js.Array(""),
        // Row 40: Gebäudehülle header
        js.Array("Gebäudehülle", ""),
        // Row 41: Envelope description
        js.Array("Beschreibung der Gebäudehülle", project.descriptions.envelopeDescription.getOrElse("")),
        // Row 42: Empty
        js.Array(""),
        // Row 43: HVAC header
        js.Array("Gebäudetechnik", ""),
        // Row 44: HVAC description
        js.Array("Beschreibung Gebäudetechnik", project.descriptions.hvacDescription.getOrElse(""))
      )

      // Create worksheet from array of arrays
      val worksheet = XLSX.utils.aoa_to_sheet(data)

      // Add worksheet to workbook
      XLSX.utils.book_append_sheet(workbook, worksheet, "Projekt")

      dom.console.log("✅ Project sheet created with exact structure")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Project sheet: ${ex.getMessage}")
        ex.printStackTrace()

  /** Create building usage sheet from scratch - matching exact template structure */
  private def createBuildingUsageSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Building Usage sheet with exact template structure...")

      // Calculate total area
      val totalArea = project.buildingUsages.map(_.area).sum

      // Create worksheet data matching the exact template structure
      val data = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Gebäudenutzungen", "", "", "", "", "", "", "", "", ""),
        // Row 2: Empty
        js.Array(""),
        // Row 3: Empty
        js.Array("", "", "", ""),
        // Row 4: Usage column headers
        js.Array("", "Nutzung 1", "Nutzung 2", "Nutzung 3"),
        // Row 5: Nutzungsart
        js.Array("Nutzungsart",
          if project.buildingUsages.length > 0 then project.buildingUsages(0).usageType else "",
          if project.buildingUsages.length > 1 then project.buildingUsages(1).usageType else "",
          if project.buildingUsages.length > 2 then project.buildingUsages(2).usageType else ""
        ),
        // Row 6: Nutzungsart II
        js.Array("Nutzungsart II",
          if project.buildingUsages.length > 0 then project.buildingUsages(0).usageSubType.getOrElse("") else "",
          if project.buildingUsages.length > 1 then project.buildingUsages(1).usageSubType.getOrElse("") else "",
          if project.buildingUsages.length > 2 then project.buildingUsages(2).usageSubType.getOrElse("") else "",
          "Gesamt"
        ),
        // Row 7: Fläche [m²]
        js.Array("Fläche [m²]",
          if project.buildingUsages.length > 0 then project.buildingUsages(0).area else "",
          if project.buildingUsages.length > 1 then project.buildingUsages(1).area else "",
          if project.buildingUsages.length > 2 then project.buildingUsages(2).area else "",
          totalArea
        ),
        // Row 8: Fläche [%]
        js.Array("Fläche [%]",
          if project.buildingUsages.length > 0 then project.buildingUsages(0).areaPercentage.getOrElse("") else "",
          if project.buildingUsages.length > 1 then project.buildingUsages(1).areaPercentage.getOrElse("") else "",
          if project.buildingUsages.length > 2 then project.buildingUsages(2).areaPercentage.getOrElse("") else "",
          ""
        ),
        // Row 9: Baujahr
        js.Array("Baujahr",
          if project.buildingUsages.length > 0 then project.buildingUsages(0).constructionYear.getOrElse("") else "",
          if project.buildingUsages.length > 1 then project.buildingUsages(1).constructionYear.getOrElse("") else "",
          if project.buildingUsages.length > 2 then project.buildingUsages(2).constructionYear.getOrElse("") else "",
          ""
        ),
        // Row 10: Empty
        js.Array("", "", "", ""),
        // Row 11: Standard-Nutzungsdaten überschreiben
        js.Array("Standard-Nutzungsdaten überschreiben", "", "", "")
      )

      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Gebäudenutzungen")

      dom.console.log("✅ Building Usage sheet created with exact structure")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Building Usage sheet: ${ex.getMessage}")

  /** Create envelope sheets from scratch - matching exact template structure */
  private def createEnvelopeSheets(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      // Roofs & Ceilings - matching exact template structure
      dom.console.log("Creating Roofs & Ceilings sheet with exact template structure...")
      val roofsData = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Dächer und Decken", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 2: Empty
        js.Array("", "", "", "", "", "", "", "", ""),
        // Row 3: Dachtyp
        js.Array("Dachtyp", "", "", "", "", "", "", "", ""),
        // Row 4: Section headers
        js.Array("", "", "Dächer / Decken gegen aussen", "", "", "", "", "Übrige Decken", "", "", ""),
        // Row 5: GeneralCondition
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", "", "", ""),
        // Row 6: Priorität
        js.Array("Priorität", "", "", "", "", "", "", "", "", "", ""),
        // Row 7: Beschreibung
        js.Array("Beschreibung", "", "", "", "", "", "", "", "", "", ""),
        // Row 8: Mögliche Verbesserungen
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", "", "", ""),
        // Row 9: Empty
        js.Array("", "", "", "", "", "", "", "", ""),
        // Row 10: Column headers
        js.Array("Kürzel", "Bezeichnung", "Typ", "Ausrichtung", "U-Wert [W/(m²K)]", "Fläche [m²]",
                "Temp. Nachbarraum", "b-Faktor [—]", "Anzahl [—]", "Bauteilheizung",
                "Temperaturzuschlag Nachbarraum", "WS3ExpertCodes:", "VL Nachbarzone",
                "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      // Add roof data rows (starting from row 11)
      project.roofsCeilings.foreach { roof =>
        roofsData.push(js.Array(
          roof.code,
          roof.description,
          roof.roofType,
          "", // Ausrichtung
          roof.uValue,
          roof.area,
          "", // Temp. Nachbarraum
          "", // b-Faktor
          "", // Anzahl
          "", // Bauteilheizung
          "", // Temperaturzuschlag
          "", // WS3ExpertCodes
          "", // VL Nachbarzone
          "", // Werterhalt
          "", // Investition
          "", // Berechnungsgrundlage
          roof.renovationYear.getOrElse("")
        ))
      }

      val roofsWorksheet = XLSX.utils.aoa_to_sheet(roofsData)
      XLSX.utils.book_append_sheet(workbook, roofsWorksheet, "Dächer und Decken")
      dom.console.log("✅ Roofs & Ceilings sheet created with exact structure")

      // Walls - similar structure
      dom.console.log("Creating Walls sheet with exact template structure...")
      val wallsData = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Wände", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 2-9: Similar structure to roofs
        js.Array("", "", "", "", "", "", "", "", ""),
        js.Array("Wandtyp", "", "", "", "", "", "", "", ""),
        js.Array("", "", "Aussenwände", "", "", "", "", "Innenwände", "", "", ""),
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Priorität", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Beschreibung", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", "", "", ""),
        js.Array("", "", "", "", "", "", "", "", ""),
        // Row 10: Column headers
        js.Array("Kürzel", "Bezeichnung", "Typ", "Ausrichtung", "U-Wert [W/(m²K)]", "Fläche [m²]",
                "Temp. Nachbarraum", "b-Faktor [—]", "Anzahl [—]", "Bauteilheizung",
                "Temperaturzuschlag Nachbarraum", "WS3ExpertCodes:", "VL Nachbarzone",
                "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      project.walls.foreach { wall =>
        wallsData.push(js.Array(
          wall.code,
          wall.description,
          wall.wallType,
          "", // Ausrichtung
          wall.uValue,
          wall.area,
          "", "", "", "", "", "", "", "", "", "",
          wall.renovationYear.getOrElse("")
        ))
      }

      val wallsWorksheet = XLSX.utils.aoa_to_sheet(wallsData)
      XLSX.utils.book_append_sheet(workbook, wallsWorksheet, "Wände")
      dom.console.log("✅ Walls sheet created with exact structure")

      // Windows & Doors
      dom.console.log("Creating Windows & Doors sheet with exact template structure...")
      val windowsData = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Fenster und Türen", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 2-9: Similar structure
        js.Array("", "", "", "", "", "", "", "", ""),
        js.Array("Fenstertyp", "", "", "", "", "", "", "", ""),
        js.Array("", "", "Fenster", "", "", "", "", "Türen", "", "", ""),
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Priorität", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Beschreibung", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", "", "", ""),
        js.Array("", "", "", "", "", "", "", "", ""),
        // Row 10: Column headers
        js.Array("Kürzel", "Bezeichnung", "Typ", "Ausrichtung", "U-Wert [W/(m²K)]", "Fläche [m²]",
                "g-Wert [—]", "Anzahl [—]", "Rahmenanteil [%]", "Verschattung",
                "WS3ExpertCodes:", "VL Nachbarzone", "Werterhalt", "Investition",
                "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      project.windowsDoors.foreach { window =>
        windowsData.push(js.Array(
          window.code,
          window.description,
          window.windowType,
          "", // Ausrichtung
          window.uValue,
          window.area,
          window.gValue,
          "", "", "", "", "", "", "", "", ""
        ))
      }

      val windowsWorksheet = XLSX.utils.aoa_to_sheet(windowsData)
      XLSX.utils.book_append_sheet(workbook, windowsWorksheet, "Fenster und Türen")
      dom.console.log("✅ Windows & Doors sheet created with exact structure")

      // Böden (Floors)
      dom.console.log("Creating Floors sheet with exact template structure...")
      val floorsData = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Böden", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 2-9: Similar structure to roofs
        js.Array("", "", "", "", "", "", "", "", ""),
        js.Array("Bodentyp", "", "", "", "", "", "", "", ""),
        js.Array("", "", "Böden gegen Erdreich", "", "", "", "", "Böden gegen unbeheizt", "", "", ""),
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Priorität", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Beschreibung", "", "", "", "", "", "", "", "", "", ""),
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", "", "", ""),
        js.Array("", "", "", "", "", "", "", "", ""),
        // Row 10: Column headers
        js.Array("Kürzel", "Bezeichnung", "Typ", "Ausrichtung", "U-Wert [W/(m²K)]", "Fläche [m²]",
                "Temp. Nachbarraum", "b-Faktor [—]", "Anzahl [—]", "Bauteilheizung",
                "Temperaturzuschlag Nachbarraum", "WS3ExpertCodes:", "VL Nachbarzone",
                "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      // Add floor data if available
      project.floors.foreach { floor =>
        floorsData.push(js.Array(
          floor.code,
          floor.description,
          floor.floorType,
          "", // Ausrichtung
          floor.uValue,
          floor.area,
          "", "", "", "", "", "", "", "", "", "",
          floor.renovationYear.getOrElse("")
        ))
      }

      val floorsWorksheet = XLSX.utils.aoa_to_sheet(floorsData)
      XLSX.utils.book_append_sheet(workbook, floorsWorksheet, "Böden")
      dom.console.log("✅ Floors sheet created with exact structure")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Envelope sheets: ${ex.getMessage}")

  /** Create HVAC sheets from scratch - matching exact template structure */
  private def createHvacSheets(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Heat Producers sheet with exact template structure...")
      val data = js.Array[js.Array[Any]](
        // Row 1: Title
        js.Array("Wärmeerzeuger (max. 5)", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 2: Empty
        js.Array(""),
        // Row 3: GeneralCondition
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 4: Priorität
        js.Array("Priorität", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 5: Beschreibung
        js.Array("Beschreibung", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 6: Mögliche Verbesserungen
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", "", "", "", "", "", ""),
        // Row 7: Empty
        js.Array(""),
        // Row 8: Column headers
        js.Array("Kürzel", "Bezeichnung", "Typ", "Energieträger", "Versorgte Verteilsysteme",
                "Aufstellort", "Nutzungsgrad Heizung [%]", "Nutzungsgrad Warmwasser [%]",
                "Anteil Heizung [%]", "Anteil Warmwasser [%]", "Werterhalt", "Investition",
                "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      // Add heat producer data rows
      project.heatProducers.foreach { producer =>
        data.push(js.Array(
          producer.code,
          producer.description,
          "", // Typ
          producer.energySource,
          "", // Versorgte Verteilsysteme
          "", // Aufstellort
          producer.efficiencyHeating,
          producer.efficiencyHotWater,
          "", // Anteil Heizung
          "", // Anteil Warmwasser
          "", // Werterhalt
          "", // Investition
          "", // Berechnungsgrundlage
          ""  // Nutzungsdauer
        ))
      }

      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Wärmeerzeuger")
      dom.console.log("✅ Heat Producers sheet created with exact structure")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating HVAC sheets: ${ex.getMessage}")

  /** Create thermal bridges sheet */
  private def createThermalBridgesSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Thermal Bridges sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Wärmebrücken", "", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Typ", "Länge [m]", "Psi-Wert [W/(mK)]", "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Wärmebrücken")
      dom.console.log("✅ Thermal Bridges sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Thermal Bridges sheet: ${ex.getMessage}")

  /** Create storage sheet */
  private def createStorageSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Storage sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Speicher", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Typ", "Volumen [l]", "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Speicher")
      dom.console.log("✅ Storage sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Storage sheet: ${ex.getMessage}")

  /** Create heating area sheet */
  private def createHeatingAreaSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Heating Area sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Versorgter Bereich Heizung", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Fläche [m²]", "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Versorgter Bereich Heizung")
      dom.console.log("✅ Heating Area sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Heating Area sheet: ${ex.getMessage}")

  /** Create hot water area sheet */
  private def createHotWaterAreaSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Hot Water Area sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Versorgter Bereich Warmwasser", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Fläche [m²]", "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Versorgter Bereich Warmwasser")
      dom.console.log("✅ Hot Water Area sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Hot Water Area sheet: ${ex.getMessage}")

  /** Create hot water consumption sheet */
  private def createHotWaterConsumptionSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Hot Water Consumption sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Verbrauchsdaten HWW", "", "", "", ""),
        js.Array(""),
        js.Array("Jahr", "Verbrauch [kWh]", "Kommentar", "", "")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Verbrauchsdaten HWW")
      dom.console.log("✅ Hot Water Consumption sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Hot Water Consumption sheet: ${ex.getMessage}")

  /** Create devices sheet */
  private def createDevicesSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Devices sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Geräte und Installationen", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Leistung [W]", "Anzahl", "Werterhalt", "Investition", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Geräte und Installationen")
      dom.console.log("✅ Devices sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Devices sheet: ${ex.getMessage}")

  /** Create small devices sheet */
  private def createSmallDevicesSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Small Devices sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Kleingeräte und Elektronik", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Leistung [W]", "Anzahl", "Werterhalt", "Investition", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Kleingeräte und Elektronik")
      dom.console.log("✅ Small Devices sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Small Devices sheet: ${ex.getMessage}")

  /** Create lighting sheet */
  private def createLightingSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Lighting sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Beleuchtung", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Leistung [W]", "Anzahl", "Werterhalt", "Investition", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Beleuchtung")
      dom.console.log("✅ Lighting sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Lighting sheet: ${ex.getMessage}")

  /** Create operating equipment sheet */
  private def createOperatingEquipmentSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Operating Equipment sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Betriebseinrichtungen und Gerät", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Leistung [W]", "Anzahl", "Werterhalt", "Investition", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Betriebseinrichtungen und Gerät")
      dom.console.log("✅ Operating Equipment sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Operating Equipment sheet: ${ex.getMessage}")

  /** Create energy production sheets (both with and without PVopti) */
  private def createEnergySheets(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      // Sheet 1: Elektrizitätsprod. ohne PVopti
      dom.console.log("Creating Electricity Producers (ohne PVopti) sheet...")
      val data1 = js.Array[js.Array[Any]](
        js.Array("Elektrizitätsprod. ohne PVopti", "", "", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", ""),
        js.Array("Priorität", "", "", "", "", "", "", "", ""),
        js.Array("Beschreibung", "", "", "", "", "", "", "", ""),
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Typ", "Beschreibung", "Jahresproduktion [kWh]", "Netzeinspeisung [kWh]",
                "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      project.electricityProducers.foreach { producer =>
        data1.push(js.Array(
          producer.code, producer.producerType, producer.description,
          producer.annualProduction, producer.gridFeedIn,
          "", "", "", ""
        ))
      }

      val worksheet1 = XLSX.utils.aoa_to_sheet(data1)
      XLSX.utils.book_append_sheet(workbook, worksheet1, "Elektrizitätsprod. ohne PVopti")
      dom.console.log("✅ Electricity Producers (ohne PVopti) sheet created")

      // Sheet 2: Elektrizitätsprod. mit PVopti
      dom.console.log("Creating Electricity Producers (mit PVopti) sheet...")
      val data2 = js.Array[js.Array[Any]](
        js.Array("Elektrizitätsprod. mit PVopti", "", "", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("GeneralCondition:", "", "", "", "", "", "", "", ""),
        js.Array("Priorität", "", "", "", "", "", "", "", ""),
        js.Array("Beschreibung", "", "", "", "", "", "", "", ""),
        js.Array("Mögliche Verbesserungen", "", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Typ", "Beschreibung", "Jahresproduktion [kWh]", "Netzeinspeisung [kWh]",
                "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )

      val worksheet2 = XLSX.utils.aoa_to_sheet(data2)
      XLSX.utils.book_append_sheet(workbook, worksheet2, "Elektrizitätsprod. mit PVopti")
      dom.console.log("✅ Electricity Producers (mit PVopti) sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Energy sheets: ${ex.getMessage}")

  /** Create other consumers sheet */
  private def createOtherConsumersSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Other Consumers sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Weitere Verbraucher", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Typ", "Beschreibung", "Verbrauch [kWh]", "Werterhalt", "Investition", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Weitere Verbraucher")
      dom.console.log("✅ Other Consumers sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Other Consumers sheet: ${ex.getMessage}")

  /** Create electricity consumption sheet */
  private def createElectricityConsumptionSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Electricity Consumption sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Verbrauchsdaten Elektrizität", "", "", "", ""),
        js.Array(""),
        js.Array("Jahr", "Verbrauch [kWh]", "Kommentar", "", "")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Verbrauchsdaten Elektrizität")
      dom.console.log("✅ Electricity Consumption sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Electricity Consumption sheet: ${ex.getMessage}")

  /** Create ventilation sheet */
  private def createVentilationSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    try
      dom.console.log("Creating Ventilation sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("Lüftung", "", "", "", "", "", "", ""),
        js.Array(""),
        js.Array("Kürzel", "Bezeichnung", "Typ", "Volumenstrom [m³/h]", "Werterhalt", "Investition", "Berechnungsgrundlage", "Nutzungsdauer [Jahre]")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Lüftung")
      dom.console.log("✅ Ventilation sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating Ventilation sheet: ${ex.getMessage}")

  /** Create dropdown data sheet */
  private def createDropDownDataSheet(workbook: js.Dynamic): Unit =
    try
      dom.console.log("Creating DropDownData sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("DropDownData", "", "", ""),
        js.Array(""),
        js.Array("Category", "Value", "Description", "")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "DropDownData")
      dom.console.log("✅ DropDownData sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating DropDownData sheet: ${ex.getMessage}")

  /** Create wall opening parents sheet */
  private def createWallOpeningParentsSheet(workbook: js.Dynamic): Unit =
    try
      dom.console.log("Creating WallOpeningParents sheet...")
      val data = js.Array[js.Array[Any]](
        js.Array("WallOpeningParents", "", "", ""),
        js.Array(""),
        js.Array("Parent ID", "Child ID", "Relationship", "")
      )
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "WallOpeningParents")
      dom.console.log("✅ WallOpeningParents sheet created")
    catch
      case ex: Exception =>
        dom.console.error(s"Error creating WallOpeningParents sheet: ${ex.getMessage}")


end ExcelGeneratorService
