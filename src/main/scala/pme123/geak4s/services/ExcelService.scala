package pme123.geak4s.services

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.{Try, Success, Failure}
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
      val address = getCellValue(sheet, "B10")
      
      // Parse client info (rows 6-17)
      val client = Client(
        salutation = getCellValue(sheet, "B7"),
        name1 = getCellValue(sheet, "B8"),
        name2 = getCellValue(sheet, "B9"),
        street = address.flatMap(_.split(" ").headOption),
        houseNumber = address.flatMap:
          case a if a.split(" ").length > 1 => a.split(" ").lastOption
          case _ => None
        ,
        poBox = getCellValue(sheet, "B11"),
        zipCode = getCellValue(sheet, "B12"),
        city = getCellValue(sheet, "B13"),
        country = getCellValue(sheet, "B14"),
        email = getCellValue(sheet, "B15"),
        phone1 = getCellValue(sheet, "B16"),
        phone2 = getCellValue(sheet, "B17")
      )

      project.copy(
        project = project.project.copy(
          projectName = projectName,
          templateVersion = templateVersion,
          generatedDate = generatedDate,
          client = client
        )
      )
    catch
      case ex: Exception =>
        dom.console.error(s"Error parsing Project sheet: ${ex.getMessage}")
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
  def exportToExcel(project: GeakProject, fileName: String): Unit =
    try
      // Create a new workbook
      val workbook = XLSX.utils.book_new()
      
      // Add sheets
      addProjectSheet(workbook, project.project)
      addBuildingUsageSheet(workbook, project)
      addEnvelopeSheets(workbook, project)
      addHvacSheets(workbook, project)
      addEnergySheet(workbook, project)
      
      // Write file
      XLSX.writeFile(workbook, fileName)
    catch
      case ex: Exception =>
        dom.console.error(s"Error exporting Excel: ${ex.getMessage}")
  
  /** Add project information sheet */
  private def addProjectSheet(workbook: js.Dynamic, project: Project): Unit =
    val data = js.Array(
      js.Array("Field", "Value"),
      js.Array("Project Name", project.projectName),
      js.Array("Template Version", project.templateVersion),
      js.Array("Generated Date", project.generatedDate),
      js.Array("", ""),
      js.Array("Client (Auftraggeber)", ""),
      js.Array("Salutation", project.client.salutation.getOrElse("")),
      js.Array("Name 1", project.client.name1.getOrElse("")),
      js.Array("Name 2", project.client.name2.getOrElse("")),
      js.Array("Address", Seq(project.client.street, project.client.houseNumber).filter(_.nonEmpty).map(_.get).mkString(" ")),
      js.Array("PO Box", project.client.poBox.getOrElse("")),
      js.Array("ZIP", project.client.zipCode.getOrElse("")),
      js.Array("City", project.client.city.getOrElse("")),
      js.Array("Country", project.client.country.getOrElse("")),
      js.Array("Email", project.client.email.getOrElse("")),
      js.Array("Phone 1", project.client.phone1.getOrElse("")),
      js.Array("Phone 2", project.client.phone2.getOrElse(""))
    )

    val worksheet = XLSX.utils.aoa_to_sheet(data)
    XLSX.utils.book_append_sheet(workbook, worksheet, "Projekt")
  
  /** Add building usage sheet */
  private def addBuildingUsageSheet(workbook: js.Dynamic, project: GeakProject): Unit =
    if project.buildingUsages.nonEmpty then
      val headers = js.Array("Usage Type", "Usage Sub-Type", "Area (m²)", "Area %", "Construction Year")
      val rows = project.buildingUsages.map { usage =>
        js.Array(
          usage.usageType,
          usage.usageSubType.getOrElse(""),
          usage.area.toString,
          usage.areaPercentage.map(_.toString).getOrElse(""),
          usage.constructionYear.map(_.toString).getOrElse("")
        )
      }

      val data = js.Array(headers) ++ js.Array(rows*)
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Gebäudenutzungen")
  
  /** Add envelope sheets */
  private def addEnvelopeSheets(workbook: js.Dynamic, project: GeakProject): Unit =
    // Roofs & Ceilings
    if project.roofsCeilings.nonEmpty then
      val headers = js.Array("Code", "Description", "Type", "Area (m²)", "U-Value", "Renovation Year")
      val rows = project.roofsCeilings.map { roof =>
        js.Array(
          roof.code,
          roof.description,
          roof.roofType,
          roof.area.toString,
          roof.uValue.toString,
          roof.renovationYear.map(_.toString).getOrElse("")
        )
      }
      val data = js.Array(headers) ++ js.Array(rows*)
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Dächer und Decken")

    // Walls
    if project.walls.nonEmpty then
      val headers = js.Array("Code", "Description", "Type", "Area (m²)", "U-Value", "Renovation Year")
      val rows = project.walls.map { wall =>
        js.Array(
          wall.code,
          wall.description,
          wall.wallType,
          wall.area.toString,
          wall.uValue.toString,
          wall.renovationYear.map(_.toString).getOrElse("")
        )
      }
      val data = js.Array(headers) ++ js.Array(rows*)
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Wände")

    // Windows & Doors
    if project.windowsDoors.nonEmpty then
      val headers = js.Array("Code", "Description", "Type", "Area (m²)", "U-Value", "g-Value")
      val rows = project.windowsDoors.map { window =>
        js.Array(
          window.code,
          window.description,
          window.windowType,
          window.area.toString,
          window.uValue.toString,
          window.gValue.toString
        )
      }
      val data = js.Array(headers) ++ js.Array(rows*)
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Fenster und Türen")
  
  /** Add HVAC sheets */
  private def addHvacSheets(workbook: js.Dynamic, project: GeakProject): Unit =
    // Heat Producers
    if project.heatProducers.nonEmpty then
      val headers = js.Array("Code", "Description", "Energy Source", "Efficiency Heating", "Efficiency Hot Water")
      val rows = project.heatProducers.map { producer =>
        js.Array(
          producer.code,
          producer.description,
          producer.energySource,
          producer.efficiencyHeating.toString,
          producer.efficiencyHotWater.toString
        )
      }
      val data = js.Array(headers) ++ js.Array(rows*)
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Wärmeerzeuger")

  /** Add energy production sheet */
  private def addEnergySheet(workbook: js.Dynamic, project: GeakProject): Unit =
    if project.electricityProducers.nonEmpty then
      val headers = js.Array("Code", "Type", "Description", "Annual Production (kWh)", "Grid Feed-in (%)")
      val rows = project.electricityProducers.map { producer =>
        js.Array(
          producer.code,
          producer.producerType,
          producer.description,
          producer.annualProduction.toString,
          producer.gridFeedIn.toString
        )
      }
      val data = js.Array(headers) ++ js.Array(rows*)
      val worksheet = XLSX.utils.aoa_to_sheet(data)
      XLSX.utils.book_append_sheet(workbook, worksheet, "Elektrizitätsprod")

end ExcelService

