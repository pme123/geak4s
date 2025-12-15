package pme123.geak4s.services

import org.scalajs.dom
import pme123.geak4s.domain.gis.*
import scala.scalajs.js
import scala.util.{Try, Success, Failure}

/** Parser for eCH-0206 MADD XML responses */
object GisXmlParser:

  /** Parse XML string to MaddResponse */
  def parse(xmlString: String): Try[MaddResponse] = Try {
    val parser = new dom.DOMParser()
    val doc = parser.parseFromString(xmlString, dom.MIMEType.`text/xml`)
    
    // Check for parse errors
    val parseError = doc.querySelector("parsererror")
    if parseError != null then
      throw new Exception(s"XML parsing error: ${parseError.textContent}")
    
    parseMaddResponse(doc)
  }
  
  private def parseMaddResponse(doc: dom.Document): MaddResponse =
    val root = doc.documentElement
    
    val status = parseStatus(root)
    val buildingList = parseBuildingList(root)
    val responseMetadata = parseResponseMetadata(root)
    
    MaddResponse(status, buildingList, responseMetadata)
  
  private def parseStatus(root: dom.Element): Status =
    val statusElem = root.querySelector("status")
    Status(
      code = getTextContent(statusElem, "code"),
      message = getTextContent(statusElem, "message")
    )
  
  private def parseBuildingList(root: dom.Element): List[BuildingItem] =
    val buildingItems = root.querySelectorAll("buildingItem")
    (0 until buildingItems.length).map { i =>
      parseBuildingItem(buildingItems(i).asInstanceOf[dom.Element])
    }.toList
  
  private def parseBuildingItem(elem: dom.Element): BuildingItem =
    BuildingItem(
      egid = getTextContent(elem, "EGID"),
      building = parseBuilding(elem.querySelector("building").asInstanceOf[dom.Element]),
      buildingEntranceList = parseBuildingEntranceList(elem),
      municipality = parseMunicipality(elem.querySelector("municipality").asInstanceOf[dom.Element]),
      realestateIdentificationList = parseRealestateList(elem)
    )
  
  private def parseBuilding(elem: dom.Element): Building =
    Building(
      officialBuildingNo = getTextContentOpt(elem, "officialBuildingNo"),
      coordinates = parseCoordinates(elem.querySelector("coordinates").asInstanceOf[dom.Element]),
      buildingStatus = getTextContentOpt(elem, "buildingStatus"),
      buildingCategory = getTextContentOpt(elem, "buildingCategory"),
      buildingClass = getTextContentOpt(elem, "buildingClass"),
      dateOfConstruction = Option(elem.querySelector("dateOfConstruction")).map(e => parseDateOfConstruction(e.asInstanceOf[dom.Element])),
      surfaceAreaOfBuilding = getTextContentOpt(elem, "surfaceAreaOfBuilding").flatMap(_.toIntOption),
      numberOfFloors = getTextContentOpt(elem, "numberOfFloors").flatMap(_.toIntOption),
      thermotechnicalDeviceForHeating1 = Option(elem.querySelector("thermotechnicalDeviceForHeating1")).map(e => parseThermotechnicalDevice(e.asInstanceOf[dom.Element])),
      thermotechnicalDeviceForWarmWater1 = Option(elem.querySelector("thermotechnicalDeviceForWarmWater1")).map(e => parseThermotechnicalDevice(e.asInstanceOf[dom.Element]))
    )
  
  private def parseCoordinates(elem: dom.Element): Coordinates =
    Coordinates(
      east = getTextContent(elem, "east").toDouble,
      north = getTextContent(elem, "north").toDouble,
      originOfCoordinates = getTextContentOpt(elem, "originOfCoordinates")
    )
  
  private def parseDateOfConstruction(elem: dom.Element): DateOfConstruction =
    DateOfConstruction(
      dateOfConstruction = getTextContentOpt(elem, "dateOfConstruction"),
      periodOfConstruction = getTextContentOpt(elem, "periodOfConstruction")
    )
  
  private def parseThermotechnicalDevice(elem: dom.Element): ThermotechnicalDevice =
    ThermotechnicalDevice(
      heatGenerator = getTextContentOpt(elem, "heatGeneratorHeating").orElse(getTextContentOpt(elem, "heatGeneratorHotWater")),
      energySource = getTextContentOpt(elem, "energySourceHeating"),
      informationSource = getTextContentOpt(elem, "informationSourceHeating"),
      revisionDate = getTextContentOpt(elem, "revisionDate")
    )
  
  private def parseBuildingEntranceList(elem: dom.Element): List[BuildingEntranceItem] =
    val entrances = elem.querySelectorAll("buildingEntranceItem")
    (0 until entrances.length).map { i =>
      parseBuildingEntranceItem(entrances(i).asInstanceOf[dom.Element])
    }.toList
  
  private def parseBuildingEntranceItem(elem: dom.Element): BuildingEntranceItem =
    BuildingEntranceItem(
      edid = getTextContent(elem, "EDID"),
      buildingEntrance = parseBuildingEntrance(elem.querySelector("buildingEntrance").asInstanceOf[dom.Element]),
      dwellingList = parseDwellingList(elem)
    )
  
  private def parseBuildingEntrance(elem: dom.Element): BuildingEntrance =
    BuildingEntrance(
      egaid = getTextContent(elem, "EGAID"),
      buildingEntranceNo = getTextContent(elem, "buildingEntranceNo"),
      coordinates = parseCoordinates(elem.querySelector("coordinates").asInstanceOf[dom.Element]),
      isOfficialAddress = getTextContent(elem, "isOfficialAddress") == "1",
      street = parseStreet(elem.querySelector("street").asInstanceOf[dom.Element]),
      locality = parseLocality(elem.querySelector("locality").asInstanceOf[dom.Element])
    )
  
  private def parseStreet(elem: dom.Element): Street =
    val streetNameElem = elem.querySelector("streetNameItem").asInstanceOf[dom.Element]
    Street(
      esid = getTextContent(elem, "ESID"),
      isOfficialDescription = getTextContent(elem, "isOfficialDescription") == "1",
      streetName = parseStreetName(streetNameElem)
    )
  
  private def parseStreetName(elem: dom.Element): StreetName =
    StreetName(
      language = getTextContent(elem, "language"),
      descriptionLong = getTextContent(elem, "descriptionLong"),
      descriptionShort = getTextContent(elem, "descriptionShort"),
      descriptionIndex = getTextContent(elem, "descriptionIndex")
    )
  
  private def parseLocality(elem: dom.Element): Locality =
    Locality(
      swissZipCode = getTextContent(elem, "swissZipCode"),
      swissZipCodeAddOn = getTextContent(elem, "swissZipCodeAddOn"),
      placeName = getTextContent(elem, "placeName")
    )
  
  private def parseDwellingList(elem: dom.Element): List[DwellingItem] =
    val dwellings = elem.querySelectorAll("dwellingItem")
    (0 until dwellings.length).map { i =>
      parseDwellingItem(dwellings(i).asInstanceOf[dom.Element])
    }.toList

  private def parseDwellingItem(elem: dom.Element): DwellingItem =
    DwellingItem(
      ewid = getTextContent(elem, "EWID"),
      dwelling = parseDwelling(elem.querySelector("dwelling").asInstanceOf[dom.Element])
    )

  private def parseDwelling(elem: dom.Element): Dwelling =
    Dwelling(
      administrativeDwellingNo = getTextContent(elem, "administrativeDwellingNo"),
      yearOfConstruction = getTextContentOpt(elem, "yearOfConstruction").flatMap(_.toIntOption),
      noOfHabitableRooms = getTextContentOpt(elem, "noOfHabitableRooms").flatMap(_.toIntOption),
      floor = getTextContentOpt(elem, "floor"),
      multipleFloor = getTextContentOpt(elem, "multipleFloor").flatMap(_.toIntOption),
      kitchen = getTextContentOpt(elem, "kitchen").flatMap(_.toIntOption),
      surfaceAreaOfDwelling = getTextContentOpt(elem, "surfaceAreaOfDwelling").flatMap(_.toIntOption),
      dwellingStatus = getTextContentOpt(elem, "dwellingStatus")
    )

  private def parseMunicipality(elem: dom.Element): Municipality =
    Municipality(
      municipalityId = getTextContent(elem, "municipalityId"),
      municipalityName = getTextContent(elem, "municipalityName"),
      cantonAbbreviation = getTextContent(elem, "cantonAbbreviation")
    )

  private def parseRealestateList(elem: dom.Element): List[RealestateIdentificationItem] =
    val items = elem.querySelectorAll("realestateIdentificationItem")
    (0 until items.length).map { i =>
      parseRealestateItem(items(i).asInstanceOf[dom.Element])
    }.toList

  private def parseRealestateItem(elem: dom.Element): RealestateIdentificationItem =
    RealestateIdentificationItem(
      egrid = getTextContent(elem, "EGRID"),
      number = getTextContent(elem, "number"),
      subDistrict = getTextContent(elem, "subDistrict")
    )

  private def parseResponseMetadata(root: dom.Element): ResponseMetadata =
    val metadataElem = root.querySelector("responseMetadata")
    ResponseMetadata(
      lastUpdateDate = getTextContent(metadataElem, "lastUpdateDate"),
      exportDate = getTextContent(metadataElem, "exportDate")
    )

  // Helper methods
  private def getTextContent(parent: dom.Element, tagName: String): String =
    val elem = parent.querySelector(tagName)
    if elem != null then elem.textContent.trim else ""

  private def getTextContentOpt(parent: dom.Element, tagName: String): Option[String] =
    val elem = parent.querySelector(tagName)
    if elem != null && elem.textContent.trim.nonEmpty then Some(elem.textContent.trim) else None

end GisXmlParser

