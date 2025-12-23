package pme123.geak4s.services

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.{Try, Success, Failure}
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import pme123.geak4s.domain.*
import pme123.geak4s.domain.project.*
import pme123.geak4s.state.AppState

/** Service for Excel file operations using SheetJS */
object ExcelService:
  
  private val XLSX = js.Dynamic.global.XLSX
  
  /** Read Excel file from file input */
  def readExcelFile(file: dom.File, onSuccess: (GeakProject, String) => Unit, onError: String => Unit): Unit =
    val reader = new dom.FileReader()
    
    reader.onload = (e: dom.Event) =>
      try
        val data = reader.result.asInstanceOf[js.typedarray.ArrayBuffer]
        val workbook = XLSX.read(data, js.Dynamic.literal(
          `type` = "array"
        ))
        
        // Parse workbook to GeakProject
        val project = parseWorkbook(workbook)
        onSuccess(project, file.name)
      catch
        case ex: Exception =>
          onError(s"Error reading Excel file: ${ex.getMessage}")
    
    reader.onerror = (e: dom.Event) =>
      onError("Failed to read file")
    
    reader.readAsArrayBuffer(file)
  
  /** Parse workbook to GeakProject */
  private def parseWorkbook(workbook: js.Dynamic): GeakProject =
    try
      // Start with empty project
      var project = GeakProject.empty

      // Parse Project sheet if it exists
      if hasSheet(workbook, "Projekt") then
        project = parseProjectSheet(workbook, project)

      // Parse Building Usage sheet if it exists
      if hasSheet(workbook, "Gebäudenutzungen") then
        project = parseBuildingUsageSheet(workbook, project)

      // Parse Envelope sheets if they exist
      if hasSheet(workbook, "Dächer und Decken") then
        project = parseRoofsCeilingsSheet(workbook, project)

      if hasSheet(workbook, "Wände") then
        project = parseWallsSheet(workbook, project)

      if hasSheet(workbook, "Fenster und Türen") then
        project = parseWindowsDoorsSheet(workbook, project)

      // Parse HVAC sheets if they exist
      if hasSheet(workbook, "Wärmeerzeuger") then
        project = parseHeatProducersSheet(workbook, project)

      // Parse Energy sheet if it exists
      if hasSheet(workbook, "Elektrizitätsprod") then
        project = parseEnergySheet(workbook, project)

      project
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing workbook: ${ex.getMessage}")
        GeakProject.empty

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

  /** Parse Project sheet */
  private def parseProjectSheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Projekt")

      // Debug: Print first 50 rows
      dom.console.log("=== PARSING PROJECT SHEET ===")
      for row <- 1 to 50 do
        val rowData = scala.collection.mutable.ArrayBuffer[String]()
        for col <- List("A", "B", "C", "D", "E", "F") do
          getCellValue(sheet, s"$col$row").foreach { value =>
            rowData += s"$col$row=$value"
          }
        if rowData.nonEmpty then
          dom.console.log(s"Row $row: ${rowData.mkString(" | ")}")

      // Parse project basic info
      // Row 2: A2=Projektbezeichnung | B2=Name | D2=Template R6.8 generiert am: | E2=Date
      val projectName = getCellValue(sheet, "B2").getOrElse("")
      val templateVersion = "R6.8" // Fixed from D2 text
      val generatedDate = getCellValue(sheet, "E2").getOrElse("")

      dom.console.log(s"Project Name: $projectName")
      dom.console.log(s"Template Version: $templateVersion")
      dom.console.log(s"Generated Date: $generatedDate")

      // Parse client info
      // Row 5: A5=Anrede | B5=Value
      // Row 6: A6=Name 1 | B6=Value
      // Row 7: A7=Name 2 | B7=Value
      // Row 8: A8=Adresse | B8=Value (Street + HouseNumber)
      // Row 9: A9=Postfach | B9=Value
      // Row 10: A10=PLZ | B10=Value
      // Row 11: A11=Ort | B11=Value
      // Row 12: A12=Land | B12=Value
      // Row 13: A13=E-Mail | B13=Value
      // Row 14: A14=Telefon 1 | B14=Value
      // Row 15: A15=Telefon 2 | B15=Value

      val clientAddressStr = getCellValue(sheet, "B8").getOrElse("")
      val clientAddressParts = clientAddressStr.split(" ")
      val clientAddress = Address(
        street = if clientAddressParts.length > 1 then Some(clientAddressParts.dropRight(1).mkString(" ")) else Some(clientAddressStr),
        houseNumber = if clientAddressParts.length > 1 then clientAddressParts.lastOption else None,
        zipCode = getCellValue(sheet, "B10"),
        city = getCellValue(sheet, "B11"),
        country = getCellValue(sheet, "B12")
      )

      dom.console.log(s"Client Address: $clientAddress")

      val client = Client(
        salutation = getCellValue(sheet, "B5").map(Anrede.valueOf).getOrElse(Anrede.Herr),
        name1 = getCellValue(sheet, "B6"),
        name2 = getCellValue(sheet, "B7"),
        address = clientAddress,
        poBox = getCellValue(sheet, "B9"),
        email = getCellValue(sheet, "B13"),
        phone1 = getCellValue(sheet, "B14"),
        phone2 = getCellValue(sheet, "B15")
      )

      dom.console.log(s"Client: $client")

      // Parse building location
      // Row 18: A18=PLZ | B18=Value
      // Row 19: A19=Ort | B19=Value
      // Row 20: A20=Gemeinde | B20=Value
      // Row 21: A21=Strasse | B21=Value
      // Row 22: A22=Hausnummer | B22=Value
      // Row 23: A23=Gebäudebezeichnung | B23=Value
      // Row 26: A26=Parzellen-Nummer | B26=Value

      val buildingAddress = Address(
        street = getCellValue(sheet, "B21"),
        houseNumber = getCellValue(sheet, "B22"),
        zipCode = getCellValue(sheet, "B18"),
        city = getCellValue(sheet, "B19"),
        country = Some("Schweiz")
      )

      val buildingLocation = BuildingLocation(
        address = buildingAddress,
        municipality = getCellValue(sheet, "B20"),
        buildingName = getCellValue(sheet, "B23"),
        parcelNumber = getCellValue(sheet, "B26")
      )

      dom.console.log(s"Building Location: $buildingLocation")

      // Parse building data
      // Row 24: A24=Baujahr | B24=Value
      // Row 25: A25=Jahr der letzten Gesamtsanierung | B25=Value
      // Row 27: A27=Klimastation | B27=Value
      // Row 28: A28=Bestbekannte Werte Klimastation | B28=Value
      // Row 29: A29=Höhe ü. M. | B29=Value
      // Row 30: A30=Energiebezugsfläche [m²] | B30=Value
      // Row 31: A31=Lichte Raumhöhe [m] | B31=Value
      // Row 32: A32=Anzahl der Vollgeschosse | B32=Value
      // Row 33: A33=Gebäudebreite [m] | B33=Value
      // Row 34: A34=Bauweise Gebäude | B34=Value
      // Row 35: A35=Grundrisstyp | B35=Value

      val buildingData = BuildingData(
        constructionYear = getCellValue(sheet, "B24").flatMap(_.toIntOption),
        lastRenovationYear = getCellValue(sheet, "B25").flatMap(_.toIntOption),
        weatherStation = getCellValue(sheet, "B27"),
        weatherStationValues = getCellValue(sheet, "B28"),
        altitude = getCellValue(sheet, "B29").flatMap(_.toDoubleOption),
        energyReferenceArea = getCellValue(sheet, "B30").flatMap(_.toDoubleOption),
        clearRoomHeight = getCellValue(sheet, "B31").flatMap(_.toDoubleOption),
        numberOfFloors = getCellValue(sheet, "B32").flatMap(_.toIntOption),
        buildingWidth = getCellValue(sheet, "B33").flatMap(_.toDoubleOption),
        constructionType = getCellValue(sheet, "B34"),
        groundPlanType = getCellValue(sheet, "B35")
      )

      dom.console.log(s"Building Data: $buildingData")

      // Parse descriptions
      // Row 38: A38=Beschreibung des Gebäudes | B38=Value
      // Row 41: A41=Beschreibung der Gebäudehülle | B41=Value
      // Row 44: A44=Beschreibung Gebäudetechnik | B44=Value

      val descriptions = Descriptions(
        buildingDescription = getCellValue(sheet, "B38"),
        envelopeDescription = getCellValue(sheet, "B41"),
        hvacDescription = getCellValue(sheet, "B44")
      )

      dom.console.log(s"Descriptions: $descriptions")

      // Parse EGID/EDID entries
      // Row 5: D5=EGID | E5=EDID | F5=Adresse | G5=PLZ | H5=Ort (header)
      // Row 6+: D6=EGID | E6=EDID | F6=Adresse | G6=PLZ | H6=Ort (data)

      val egidEntries = scala.collection.mutable.ArrayBuffer[EgidEdidEntry]()
      var egidRow = 6 // Start from row 6 (after header in row 5)
      var continueEgid = true

      while continueEgid && egidRow < 20 do // Max ~15 entries
        val egid = getCellValue(sheet, s"D$egidRow")
        if egid.isDefined && egid.get.nonEmpty then
          // Parse address (F column contains "Street HouseNumber")
          val addressStr = getCellValue(sheet, s"F$egidRow").getOrElse("")
          val addressParts = addressStr.split(" ")
          val address = Address(
            street = if addressParts.length > 1 then Some(addressParts.dropRight(1).mkString(" ")) else Some(addressStr),
            houseNumber = if addressParts.length > 1 then addressParts.lastOption else None,
            zipCode = getCellValue(sheet, s"G$egidRow"),
            city = getCellValue(sheet, s"H$egidRow"),
            country = Some("Schweiz")
          )

          val entry = EgidEdidEntry(
            egid = egid,
            edid = getCellValue(sheet, s"E$egidRow"),
            address = address
          )
          egidEntries += entry
          egidRow += 1
        else
          continueEgid = false

      dom.console.log(s"EGID Entries: ${egidEntries.size} entries")

      project.copy(
        project = project.project.copy(
          projectName = projectName,
          templateVersion = templateVersion,
          generatedDate = generatedDate,
          client = client,
          buildingLocation = buildingLocation,
          buildingData = buildingData,
          descriptions = descriptions,
          egidEdidGroup = EgidEdidGroup(egidEntries.toList)
        )
      )
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Project sheet: ${ex.getMessage}")
        ex.printStackTrace()
        project

  /** Parse Building Usage sheet */
  private def parseBuildingUsageSheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Gebäudenutzungen")
      val usages = scala.collection.mutable.ArrayBuffer[building.BuildingUsage]()

      var row = 2 // Start from row 2 (after header)
      var continue = true

      while continue do
        val usageType = getCellValue(sheet, s"A$row")
        if usageType.isDefined then
          val usage = building.BuildingUsage(
            usageType = usageType.get,
            usageSubType = getCellValue(sheet, s"B$row"),
            area = getCellValue(sheet, s"C$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            areaPercentage = getCellValue(sheet, s"D$row").flatMap(_.toDoubleOption),
            constructionYear = getCellValue(sheet, s"E$row").flatMap(_.toIntOption)
          )
          usages += usage
          row += 1
        else
          continue = false

      project.copy(buildingUsages = usages.toList)
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Building Usage sheet: ${ex.getMessage}")
        project

  /** Parse Roofs & Ceilings sheet */
  private def parseRoofsCeilingsSheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Dächer und Decken")
      val items = scala.collection.mutable.ArrayBuffer[envelope.RoofCeiling]()

      var row = 2
      var continue = true

      while continue do
        val code = getCellValue(sheet, s"A$row")
        if code.isDefined then
          val item = envelope.RoofCeiling(
            code = code.get,
            description = getCellValue(sheet, s"B$row").getOrElse(""),
            roofType = getCellValue(sheet, s"C$row").getOrElse(""),
            orientation = None,
            renovationYear = getCellValue(sheet, s"F$row").flatMap(_.toIntOption),
            area = getCellValue(sheet, s"D$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            uValue = getCellValue(sheet, s"E$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            bFactor = 1.0,
            quantity = 1,
            floorHeating = false,
            neighborRoomTemp = None,
            generalCondition = None,
            priority = None,
            possibleImprovements = None,
            maintenanceCost = None,
            investment = None,
            calculationBase = None,
            usefulLife = None
          )
          items += item
          row += 1
        else
          continue = false

      project.copy(roofsCeilings = items.toList)
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Roofs & Ceilings sheet: ${ex.getMessage}")
        project

  /** Parse Walls sheet */
  private def parseWallsSheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Wände")
      val items = scala.collection.mutable.ArrayBuffer[envelope.Wall]()

      var row = 2
      var continue = true

      while continue do
        val code = getCellValue(sheet, s"A$row")
        if code.isDefined then
          val item = envelope.Wall(
            code = code.get,
            description = getCellValue(sheet, s"B$row").getOrElse(""),
            wallType = getCellValue(sheet, s"C$row").getOrElse(""),
            orientation = None,
            renovationYear = getCellValue(sheet, s"F$row").flatMap(_.toIntOption),
            area = getCellValue(sheet, s"D$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            uValue = getCellValue(sheet, s"E$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            bFactor = 1.0,
            quantity = 1,
            wallHeating = false,
            neighborRoomTemp = None,
            generalCondition = None,
            priority = None,
            possibleImprovements = None,
            maintenanceCost = None,
            investment = None,
            calculationBase = None,
            usefulLife = None
          )
          items += item
          row += 1
        else
          continue = false

      project.copy(walls = items.toList)
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Walls sheet: ${ex.getMessage}")
        project

  /** Parse Windows & Doors sheet */
  private def parseWindowsDoorsSheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Fenster und Türen")
      val items = scala.collection.mutable.ArrayBuffer[envelope.WindowDoor]()

      var row = 2
      var continue = true

      while continue do
        val code = getCellValue(sheet, s"A$row")
        if code.isDefined then
          val item = envelope.WindowDoor(
            code = code.get,
            description = getCellValue(sheet, s"B$row").getOrElse(""),
            windowType = getCellValue(sheet, s"C$row").getOrElse(""),
            orientation = None,
            renovationYear = None,
            area = getCellValue(sheet, s"D$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            uValue = getCellValue(sheet, s"E$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            gValue = getCellValue(sheet, s"F$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            bFactor = 1.0,
            glassRatio = 0.7,
            shading = 0.85,
            quantity = 1,
            installedIn = None,
            generalCondition = None,
            priority = None,
            possibleImprovements = None,
            maintenanceCost = None,
            investment = None,
            calculationBase = None,
            usefulLife = None
          )
          items += item
          row += 1
        else
          continue = false

      project.copy(windowsDoors = items.toList)
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Windows & Doors sheet: ${ex.getMessage}")
        project

  /** Parse Heat Producers sheet */
  private def parseHeatProducersSheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Wärmeerzeuger")
      val items = scala.collection.mutable.ArrayBuffer[hvac.HeatProducer]()

      var row = 2
      var continue = true

      while continue do
        val code = getCellValue(sheet, s"A$row")
        if code.isDefined then
          val item = hvac.HeatProducer(
            code = code.get,
            description = getCellValue(sheet, s"B$row").getOrElse(""),
            energySource = getCellValue(sheet, s"C$row").getOrElse(""),
            efficiencyHeating = getCellValue(sheet, s"D$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            efficiencyHotWater = getCellValue(sheet, s"E$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            oversizing = 1.0,
            producerType = "",
            suppliedDistributionSystems = List.empty,
            heatEmissionType = "",
            constructionYear = None,
            condition = None,
            location = "",
            maintenanceCost = 0.0,
            investment = None,
            calculationBase = None,
            usefulLife = None
          )
          items += item
          row += 1
        else
          continue = false

      project.copy(heatProducers = items.toList)
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Heat Producers sheet: ${ex.getMessage}")
        project

  /** Parse Energy Production sheet */
  private def parseEnergySheet(workbook: js.Dynamic, project: GeakProject): GeakProject =
    try
      val sheet = workbook.Sheets.selectDynamic("Elektrizitätsprod")
      val items = scala.collection.mutable.ArrayBuffer[energy.ElectricityProducer]()

      var row = 2
      var continue = true

      while continue do
        val code = getCellValue(sheet, s"A$row")
        if code.isDefined then
          val item = energy.ElectricityProducer(
            code = code.get,
            producerType = getCellValue(sheet, s"B$row").getOrElse(""),
            connectedHeatProducer = None,
            description = getCellValue(sheet, s"C$row").getOrElse(""),
            commissioningYear = None,
            annualProduction = getCellValue(sheet, s"D$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            gridFeedIn = getCellValue(sheet, s"E$row").flatMap(_.toDoubleOption).getOrElse(0.0),
            maintenanceCost = 0.0,
            selfConsumptionCalculatedExternally = false,
            investment = None,
            calculationBase = None,
            usefulLife = None,
            generalCondition = None,
            priority = None,
            possibleImprovements = None
          )
          items += item
          row += 1
        else
          continue = false

      project.copy(electricityProducers = items.toList)
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Energy Production sheet: ${ex.getMessage}")
        project

  /** Export GeakProject to Excel file */
  def exportToExcel(project: GeakProject, fileName: String = ""): Unit =
    try
      // Generate filename from project name if not provided
      val exportFileName = if fileName.isEmpty then
        val projectName = if project.project.projectName.isEmpty then "geak_export" else project.project.projectName
        projectName.replace(" ", "-") + ".xlsx"
      else
        fileName

      // Load the template file from public folder
      dom.fetch("/geak_newproject.xlsx")
        .toFuture
        .flatMap { response =>
          if response.ok then
            response.arrayBuffer().toFuture
          else
            throw new Exception(s"Failed to load template: ${response.statusText}")
        }
        .foreach { arrayBuffer =>
          try
            dom.console.log(s"Template loaded, size: ${arrayBuffer.byteLength} bytes")

            // Read the XLSX template with minimal options for XLS conversion
            val workbook = XLSX.read(arrayBuffer, js.Dynamic.literal(
              `type` = "array"
            ))

            dom.console.log(s"Workbook loaded, sheets: ${workbook.SheetNames.asInstanceOf[js.Array[String]].mkString(", ")}")

            // TEMPORARILY DISABLED: All updates for testing
            // updateProjectSheet(workbook, project.project)
            // updateBuildingUsageSheet(workbook, project)
            // updateEnvelopeSheets(workbook, project)
            // updateHvacSheets(workbook, project)
            // updateEnergySheet(workbook, project)

            dom.console.log("About to write file in XLSX format...")

            // Use writeFile for simple and reliable XLSX export
            XLSX.writeFile(workbook, exportFileName)

            dom.console.log("✅ File written successfully!")
          catch
            case ex: Exception =>
              dom.console.error(s"Error processing template: ${ex.getMessage}")
              ex.printStackTrace()
        }
    catch
      case ex: Exception =>
        dom.console.error(s"Error exporting Excel: ${ex.getMessage}")
  
  /** Set cell value in sheet using SheetJS utils */
  private def setCellValue(sheet: js.Dynamic, cell: String, value: String): Unit =
    try
      // Parse cell reference (e.g., "B2" -> col: 1, row: 1)
      val col = cell.charAt(0).toInt - 'A'.toInt
      val row = cell.substring(1).toInt - 1

      // Use SheetJS utils to add cell data
      val data = js.Array(js.Array(value))
      XLSX.utils.sheet_add_aoa(sheet, data, js.Dynamic.literal(
        origin = js.Dynamic.literal(r = row, c = col)
      ))
    catch
      case ex: Exception =>
        dom.console.warn(s"Could not set cell $cell: ${ex.getMessage}")

  /** Update project information sheet */
  private def updateProjectSheet(workbook: js.Dynamic, project: Project): Unit =
    try
      val sheet = workbook.Sheets.selectDynamic("Projekt")

      dom.console.log("=== UPDATING PROJECT SHEET ===")
      dom.console.log(s"Sheet range before: ${sheet.selectDynamic("!ref")}")

      // Update project basic info
      // Row 2: B2=Projektbezeichnung, E2=Date
      setCellValue(sheet, "B2", project.projectName)
      setCellValue(sheet, "E2", project.generatedDate)

      dom.console.log(s"Project Name: ${project.projectName}")
      dom.console.log(s"Generated Date: ${project.generatedDate}")

      // Update client info
      // Row 5: B5=Anrede
      // Row 6: B6=Name 1
      // Row 7: B7=Name 2
      // Row 8: B8=Adresse (Street + HouseNumber)
      // Row 9: B9=Postfach
      // Row 10: B10=PLZ
      // Row 11: B11=Ort
      // Row 12: B12=Land
      // Row 13: B13=E-Mail
      // Row 14: B14=Telefon 1
      // Row 15: B15=Telefon 2

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
      // Row 18: B18=PLZ
      // Row 19: B19=Ort
      // Row 20: B20=Gemeinde
      // Row 21: B21=Strasse
      // Row 22: B22=Hausnummer
      // Row 23: B23=Gebäudebezeichnung
      // Row 26: B26=Parzellen-Nummer

      setCellValue(sheet, "B18", project.buildingLocation.address.zipCode.getOrElse(""))
      setCellValue(sheet, "B19", project.buildingLocation.address.city.getOrElse(""))
      setCellValue(sheet, "B20", project.buildingLocation.municipality.getOrElse(""))
      setCellValue(sheet, "B21", project.buildingLocation.address.street.getOrElse(""))
      setCellValue(sheet, "B22", project.buildingLocation.address.houseNumber.getOrElse(""))
      setCellValue(sheet, "B23", project.buildingLocation.buildingName.getOrElse(""))
      setCellValue(sheet, "B26", project.buildingLocation.parcelNumber.getOrElse(""))

      // Update building data
      // Row 24: B24=Baujahr
      // Row 25: B25=Jahr der letzten Gesamtsanierung
      // Row 27: B27=Klimastation
      // Row 28: B28=Bestbekannte Werte Klimastation
      // Row 29: B29=Höhe ü. M.
      // Row 30: B30=Energiebezugsfläche [m²]
      // Row 31: B31=Lichte Raumhöhe [m]
      // Row 32: B32=Anzahl der Vollgeschosse
      // Row 33: B33=Gebäudebreite [m]
      // Row 34: B34=Bauweise Gebäude
      // Row 35: B35=Grundrisstyp

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
      // Row 38: B38=Beschreibung des Gebäudes
      // Row 41: B41=Beschreibung der Gebäudehülle
      // Row 44: B44=Beschreibung Gebäudetechnik

      setCellValue(sheet, "B38", project.descriptions.buildingDescription.getOrElse(""))
      setCellValue(sheet, "B41", project.descriptions.envelopeDescription.getOrElse(""))
      setCellValue(sheet, "B44", project.descriptions.hvacDescription.getOrElse(""))

      // Update EGID/EDID entries
      // Row 5: D5=EGID | E5=EDID | F5=Adresse (existing headers, don't overwrite)
      // We need to add G5=PLZ | H5=Ort headers if they don't exist

      dom.console.log(s"EGID Entries to export: ${project.egidEdidGroup.entries.size}")

      // Check if G5 and H5 headers exist
      val g5Value = getCellValue(sheet, "G5")
      val h5Value = getCellValue(sheet, "H5")

      dom.console.log(s"Current G5: $g5Value, H5: $h5Value")

      // Only add headers if they don't exist
      if g5Value.isEmpty || g5Value.get.isEmpty then
        setCellValue(sheet, "G5", "PLZ")
        dom.console.log("Added PLZ header to G5")

      if h5Value.isEmpty || h5Value.get.isEmpty then
        setCellValue(sheet, "H5", "Ort")
        dom.console.log("Added Ort header to H5")

      // Write EGID/EDID data rows (starting from row 6)
      // Combine all address parts into a single string for column F
      var egidRow = 6
      project.egidEdidGroup.entries.foreach { entry =>
        // Combine street, house number, ZIP, and city into one address string
        val addressParts = Seq(
          entry.address.street,
          entry.address.houseNumber,
          entry.address.zipCode.map(z => s", $z"),
          entry.address.city
        ).flatten
        val fullAddressStr = addressParts.mkString(" ")

        setCellValue(sheet, s"D$egidRow", entry.egid.getOrElse(""))
        setCellValue(sheet, s"E$egidRow", entry.edid.getOrElse(""))
        setCellValue(sheet, s"F$egidRow", fullAddressStr)

        // TEMPORARILY DISABLED: Write to G and H columns
        // setCellValue(sheet, s"G$egidRow", entry.address.zipCode.getOrElse(""))
        // setCellValue(sheet, s"H$egidRow", entry.address.city.getOrElse(""))

        dom.console.log(s"Row $egidRow - EGID: ${entry.egid.getOrElse("")}, EDID: ${entry.edid.getOrElse("")}, Full Address: $fullAddressStr")

        egidRow += 1
      }

      dom.console.log(s"Sheet range after: ${sheet.selectDynamic("!ref")}")
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
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating Energy sheet: ${ex.getMessage}")

end ExcelService

