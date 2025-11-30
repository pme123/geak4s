package pme123.geak4s.domain

import scala.scalajs.js

/** JavaScript interop helpers for reading from Excel */
object JSHelpers:
  
  extension (row: js.Dynamic)
    
    /** Get optional string value from dynamic object */
    def getStringOpt(key: String): Option[String] =
      if js.isUndefined(row.selectDynamic(key)) || 
         row.selectDynamic(key) == null ||
         row.selectDynamic(key).toString.trim.isEmpty 
      then None
      else Some(row.selectDynamic(key).toString)
    
    /** Get string value with default */
    def getString(key: String, default: String = ""): String =
      getStringOpt(key).getOrElse(default)
    
    /** Get optional integer value */
    def getIntOpt(key: String): Option[Int] =
      getStringOpt(key).flatMap(_.toIntOption)
    
    /** Get integer value with default */
    def getInt(key: String, default: Int = 0): Int =
      getIntOpt(key).getOrElse(default)
    
    /** Get optional double value */
    def getDoubleOpt(key: String): Option[Double] =
      getStringOpt(key).flatMap(_.toDoubleOption)
    
    /** Get double value with default */
    def getDouble(key: String, default: Double = 0.0): Double =
      getDoubleOpt(key).getOrElse(default)
    
    /** Get boolean value */
    def getBoolean(key: String, default: Boolean = false): Boolean =
      getStringOpt(key) match
        case Some("ja") | Some("Ja") | Some("true") | Some("1") => true
        case Some("nein") | Some("Nein") | Some("false") | Some("0") => false
        case _ => default
    
    /** Get list of values from comma-separated string */
    def getList(key: String): List[String] =
      getStringOpt(key) match
        case Some(value) => value.split(",").map(_.trim).toList
        case None => List.empty
    
    /** Get map from key-value pairs (e.g., "WE-1:80,WE-2:20") */
    def getMap(key: String): Map[String, Double] =
      getStringOpt(key) match
        case Some(value) =>
          value.split(",").flatMap { pair =>
            pair.split(":").toList match
              case k :: v :: Nil => v.toDoubleOption.map(k.trim -> _)
              case _ => None
          }.toMap
        case None => Map.empty
  
  end extension
  
  /** Convert Excel date number to string */
  def excelDateToString(excelDate: Double): String =
    // Excel dates are days since 1900-01-01
    // This is a simplified conversion
    val date = new js.Date((excelDate - 25569) * 86400 * 1000)
    s"${date.getFullYear()}-${(date.getMonth() + 1).toString.padTo(2, '0')}-${date.getDate().toString.padTo(2, '0')}"
  
  /** Safe array access */
  def getArrayElement[T](arr: js.Array[T], index: Int): Option[T] =
    if index >= 0 && index < arr.length then Some(arr(index))
    else None
  
  /** Convert JS array to Scala list */
  def jsArrayToList[T](arr: js.Array[T]): List[T] =
    arr.toList
  
  /** Convert JS object to Map */
  def jsObjectToMap(obj: js.Dynamic): Map[String, String] =
    js.Object.keys(obj.asInstanceOf[js.Object]).map { key =>
      key -> obj.selectDynamic(key).toString
    }.toMap

end JSHelpers

