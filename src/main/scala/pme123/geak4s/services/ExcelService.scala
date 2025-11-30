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

      // Parse project basic info (rows 2-4)
      val projectName = getCellValue(sheet, "B2").getOrElse("")
      val templateVersion = getCellValue(sheet, "B3").getOrElse("R6.8")
      val generatedDate = getCellValue(sheet, "B4").getOrElse("")

      // Parse client address (B10 contains "Street HouseNumber")
      val clientAddressStr = getCellValue(sheet, "B10").getOrElse("")
      val clientAddressParts = clientAddressStr.split(" ")
      val clientAddress = Address(
        street = if clientAddressParts.length > 1 then Some(clientAddressParts.dropRight(1).mkString(" ")) else Some(clientAddressStr),
        houseNumber = if clientAddressParts.length > 1 then clientAddressParts.lastOption else None,
        zipCode = getCellValue(sheet, "B12"),
        city = getCellValue(sheet, "B13"),
        country = getCellValue(sheet, "B14")
      )

      val client = Client(
        salutation = getCellValue(sheet, "B7"),
        name1 = getCellValue(sheet, "B8"),
        name2 = getCellValue(sheet, "B9"),
        address = clientAddress,
        poBox = getCellValue(sheet, "B11"),
        email = getCellValue(sheet, "B15"),
        phone1 = getCellValue(sheet, "B16"),
        phone2 = getCellValue(sheet, "B17")
      )

      // Parse building location (rows 19-22)
      val buildingAddressStr = getCellValue(sheet, "B19").getOrElse("")
      val buildingAddressParts = buildingAddressStr.split(" ")
      val buildingAddress = Address(
        street = if buildingAddressParts.length > 1 then Some(buildingAddressParts.dropRight(1).mkString(" ")) else Some(buildingAddressStr),
        houseNumber = if buildingAddressParts.length > 1 then buildingAddressParts.lastOption else None,
        zipCode = getCellValue(sheet, "B20"),
        city = getCellValue(sheet, "B21"),
        country = Some("Schweiz")
      )

      val buildingLocation = BuildingLocation(
        address = buildingAddress,
        municipality = getCellValue(sheet, "B22"),
        buildingName = getCellValue(sheet, "B23"),
        parcelNumber = getCellValue(sheet, "B24")
      )

      // Parse building data (rows 26-36)
      val buildingData = BuildingData(
        constructionYear = getCellValue(sheet, "B26").flatMap(_.toIntOption),
        lastRenovationYear = getCellValue(sheet, "B27").flatMap(_.toIntOption),
        weatherStation = getCellValue(sheet, "B28"),
        weatherStationValues = getCellValue(sheet, "B29"),
        altitude = getCellValue(sheet, "B30").flatMap(_.toDoubleOption),
        energyReferenceArea = getCellValue(sheet, "B31").flatMap(_.toDoubleOption),
        clearRoomHeight = getCellValue(sheet, "B32").flatMap(_.toDoubleOption),
        numberOfFloors = getCellValue(sheet, "B33").flatMap(_.toIntOption),
        buildingWidth = getCellValue(sheet, "B34").flatMap(_.toDoubleOption),
        constructionType = getCellValue(sheet, "B35"),
        groundPlanType = getCellValue(sheet, "B36")
      )

      // Parse descriptions (rows 38-40)
      val descriptions = Descriptions(
        buildingDescription = getCellValue(sheet, "B38"),
        envelopeDescription = getCellValue(sheet, "B39"),
        hvacDescription = getCellValue(sheet, "B40")
      )

      // Parse EGID/EDID entries (starting from row 42)
      val egidEntries = scala.collection.mutable.ArrayBuffer[EgidEdidEntry]()
      var egidRow = 42
      var continueEgid = true

      while continueEgid && egidRow < 52 do // Max 10 entries
        val egid = getCellValue(sheet, s"B$egidRow")
        if egid.isDefined && egid.get.nonEmpty then
          val entry = EgidEdidEntry(
            egid = egid,
            edid = getCellValue(sheet, s"C$egidRow"),
            address = getCellValue(sheet, s"D$egidRow"),
            zipCode = getCellValue(sheet, s"E$egidRow"),
            city = getCellValue(sheet, s"F$egidRow")
          )
          egidEntries += entry
          egidRow += 1
        else
          continueEgid = false

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
            // Read the template workbook
            val workbook = XLSX.read(arrayBuffer, js.Dynamic.literal(
              `type` = "array"
            ))

            // Update sheets with project data
            updateProjectSheet(workbook, project.project)
            updateBuildingUsageSheet(workbook, project)
            updateEnvelopeSheets(workbook, project)
            updateHvacSheets(workbook, project)
            updateEnergySheet(workbook, project)

            // Write file
            XLSX.writeFile(workbook, exportFileName)
          catch
            case ex: Exception =>
              dom.console.error(s"Error processing template: ${ex.getMessage}")
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

      // Update project basic info (rows 2-4)
      setCellValue(sheet, "B2", project.projectName)
      setCellValue(sheet, "B3", project.templateVersion)
      setCellValue(sheet, "B4", project.generatedDate)

      // Update client info (rows 7-17)
      setCellValue(sheet, "B7", project.client.salutation.getOrElse(""))
      setCellValue(sheet, "B8", project.client.name1.getOrElse(""))
      setCellValue(sheet, "B9", project.client.name2.getOrElse(""))

      // Update client address
      val clientAddressStr = Seq(
        project.client.address.street,
        project.client.address.houseNumber
      ).flatten.mkString(" ")
      setCellValue(sheet, "B10", clientAddressStr)

      setCellValue(sheet, "B11", project.client.poBox.getOrElse(""))
      setCellValue(sheet, "B12", project.client.address.zipCode.getOrElse(""))
      setCellValue(sheet, "B13", project.client.address.city.getOrElse(""))
      setCellValue(sheet, "B14", project.client.address.country.getOrElse(""))
      setCellValue(sheet, "B15", project.client.email.getOrElse(""))
      setCellValue(sheet, "B16", project.client.phone1.getOrElse(""))
      setCellValue(sheet, "B17", project.client.phone2.getOrElse(""))

      // Update building location (rows 19-24)
      val buildingAddressStr = Seq(
        project.buildingLocation.address.street,
        project.buildingLocation.address.houseNumber
      ).flatten.mkString(" ")
      setCellValue(sheet, "B19", buildingAddressStr)
      setCellValue(sheet, "B20", project.buildingLocation.address.zipCode.getOrElse(""))
      setCellValue(sheet, "B21", project.buildingLocation.address.city.getOrElse(""))
      setCellValue(sheet, "B22", project.buildingLocation.municipality.getOrElse(""))
      setCellValue(sheet, "B23", project.buildingLocation.buildingName.getOrElse(""))
      setCellValue(sheet, "B24", project.buildingLocation.parcelNumber.getOrElse(""))

      // Update building data (rows 26-36)
      setCellValue(sheet, "B26", project.buildingData.constructionYear.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B27", project.buildingData.lastRenovationYear.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B28", project.buildingData.weatherStation.getOrElse(""))
      setCellValue(sheet, "B29", project.buildingData.weatherStationValues.getOrElse(""))
      setCellValue(sheet, "B30", project.buildingData.altitude.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B31", project.buildingData.energyReferenceArea.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B32", project.buildingData.clearRoomHeight.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B33", project.buildingData.numberOfFloors.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B34", project.buildingData.buildingWidth.map(_.toString).getOrElse(""))
      setCellValue(sheet, "B35", project.buildingData.constructionType.getOrElse(""))
      setCellValue(sheet, "B36", project.buildingData.groundPlanType.getOrElse(""))

      // Update descriptions (rows 38-40)
      setCellValue(sheet, "B38", project.descriptions.buildingDescription.getOrElse(""))
      setCellValue(sheet, "B39", project.descriptions.envelopeDescription.getOrElse(""))
      setCellValue(sheet, "B40", project.descriptions.hvacDescription.getOrElse(""))

      // Update EGID/EDID entries (starting from row 42)
      var egidRow = 42
      project.egidEdidGroup.entries.foreach { entry =>
        setCellValue(sheet, s"B$egidRow", entry.egid.getOrElse(""))
        setCellValue(sheet, s"C$egidRow", entry.edid.getOrElse(""))
        setCellValue(sheet, s"D$egidRow", entry.address.getOrElse(""))
        setCellValue(sheet, s"E$egidRow", entry.zipCode.getOrElse(""))
        setCellValue(sheet, s"F$egidRow", entry.city.getOrElse(""))
        egidRow += 1
      }
    catch
      case ex: Exception =>
        dom.console.error(s"Error updating Project sheet: ${ex.getMessage}")
  
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

