package com.example.geak4s

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.Dynamic.{global => g}

object ExcelDemoView:

  case class Person(name: String, age: Int, email: String)

  def apply(): HtmlElement =
    val dataVar = Var(List.empty[Person])
    val statusVar = Var("Ready to import or export Excel files")
    val fileInputRef = Var(Option.empty[dom.html.Input])

    div(
      className := "excel-demo-container",
      Card(
        _.slots.header := CardHeader(
          _.titleText := "Excel Import/Export Demo",
          _.subtitleText := "Using SheetJS (xlsx) with ScalablyTyped"
        ),
        div(
          className := "card-content",
          
          // Status message
          div(
            className := "status-message",
            MessageStrip(
              _.design := MessageStripDesign.Information,
              text <-- statusVar.signal
            )
          ),

          // Import section
          div(
            className := "section",
            Title(_.size := TitleLevel.H5, "Import Excel File"),
            div(
              className := "import-controls",
              input(
                tpe := "file",
                accept := ".xlsx, .xls, .csv",
                onMountCallback(ctx => fileInputRef.set(Some(ctx.thisNode.ref))),
                onChange --> { _ =>
                  fileInputRef.now().foreach { inputElement =>
                    Option(inputElement.files).flatMap(files => Option(files.item(0))).foreach { file =>
                      readExcelFile(file, dataVar, statusVar)
                    }
                  }
                }
              ),
              Button(
                _.design := ButtonDesign.Emphasized,
                _.events.onClick --> { _ =>
                  fileInputRef.now().foreach(_.click())
                },
                "Choose File"
              )
            )
          ),

          // Data display section
          div(
            className := "section",
            Title(_.size := TitleLevel.H5, "Data Preview"),
            child <-- dataVar.signal.map { data =>
              if data.isEmpty then
                div(
                  className := "empty-state",
                  p("No data loaded. Import an Excel file or generate sample data.")
                )
              else
                renderDataTable(data)
            }
          ),

          // Export section
          div(
            className := "section",
            Title(_.size := TitleLevel.H5, "Export Options"),
            div(
              className := "export-controls",
              Button(
                _.design := ButtonDesign.Positive,
                _.disabled <-- dataVar.signal.map(_.isEmpty),
                _.events.onClick --> { _ =>
                  exportToExcel(dataVar.now(), statusVar)
                },
                "Export to Excel"
              ),
              Button(
                _.design := ButtonDesign.Default,
                _.events.onClick --> { _ =>
                  generateSampleData(dataVar, statusVar)
                },
                "Generate Sample Data"
              ),
              Button(
                _.design := ButtonDesign.Negative,
                _.disabled <-- dataVar.signal.map(_.isEmpty),
                _.events.onClick --> { _ =>
                  dataVar.set(List.empty)
                  statusVar.set("Data cleared")
                },
                "Clear Data"
              )
            )
          )
        )
      )
    )

  private def renderDataTable(data: List[Person]): HtmlElement =
    div(
      className := "data-table",
      table(
        thead(
          tr(
            th("Name"),
            th("Age"),
            th("Email")
          )
        ),
        tbody(
          data.map { person =>
            tr(
              td(person.name),
              td(person.age.toString),
              td(person.email)
            )
          }
        )
      )
    )

  private def readExcelFile(
    file: dom.File,
    dataVar: Var[List[Person]],
    statusVar: Var[String]
  ): Unit =
    statusVar.set(s"Reading file: ${file.name}...")

    val reader = new dom.FileReader()

    reader.onload = (e: dom.Event) => {
      try {
        val XLSX = g.XLSX
        val data = reader.result.asInstanceOf[js.typedarray.ArrayBuffer]

        // Read workbook
        val options = js.Dynamic.literal(
          "type" -> "array"
        )
        val workbook = XLSX.read(data, options)

        // Get the first sheet
        val sheetNames = workbook.SheetNames.asInstanceOf[js.Array[String]]
        val sheetName = sheetNames(0)
        val worksheet = workbook.Sheets.selectDynamic(sheetName)

        // Convert to JSON
        val jsonData = XLSX.utils.sheet_to_json(worksheet).asInstanceOf[js.Array[js.Dynamic]]

        // Parse the data
        val persons = jsonData.map { row =>
          val name = if (js.isUndefined(row.Name)) row.name.toString else row.Name.toString
          val ageValue = if (js.isUndefined(row.Age)) row.age else row.Age
          val age = ageValue.toString.toIntOption.getOrElse(0)
          val email = if (js.isUndefined(row.Email)) row.email.toString else row.Email.toString

          Person(name, age, email)
        }.toList

        dataVar.set(persons)
        statusVar.set(s"Successfully imported ${persons.length} rows from ${file.name}")
      } catch {
        case e: Exception =>
          statusVar.set(s"Error reading file: ${e.getMessage}")
          dom.console.error("Error reading Excel file:", e)
      }
    }

    reader.onerror = (e: dom.Event) => {
      statusVar.set("Error reading file")
      dom.console.error("FileReader error:", e)
    }

    reader.readAsArrayBuffer(file)

  private def exportToExcel(data: List[Person], statusVar: Var[String]): Unit =
    try {
      statusVar.set("Generating Excel file...")

      val XLSX = g.XLSX

      // Convert data to JS array
      val jsData = js.Array(
        data.map { person =>
          js.Dynamic.literal(
            "Name" -> person.name,
            "Age" -> person.age,
            "Email" -> person.email
          )
        }*
      )

      // Create worksheet from data
      val worksheet = XLSX.utils.json_to_sheet(jsData)

      // Create workbook
      val workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, "Data")

      // Generate Excel file and trigger download
      XLSX.writeFile(workbook, "export.xlsx")

      statusVar.set(s"Successfully exported ${data.length} rows to export.xlsx")
    } catch {
      case e: Exception =>
        statusVar.set(s"Error exporting file: ${e.getMessage}")
        dom.console.error("Error exporting Excel file:", e)
    }

  private def generateSampleData(dataVar: Var[List[Person]], statusVar: Var[String]): Unit =
    val sampleData = List(
      Person("Alice Johnson", 28, "alice@example.com"),
      Person("Bob Smith", 35, "bob@example.com"),
      Person("Charlie Brown", 42, "charlie@example.com"),
      Person("Diana Prince", 31, "diana@example.com"),
      Person("Eve Wilson", 26, "eve@example.com"),
      Person("Frank Miller", 39, "frank@example.com"),
      Person("Grace Lee", 33, "grace@example.com"),
      Person("Henry Davis", 45, "henry@example.com")
    )
    
    dataVar.set(sampleData)
    statusVar.set(s"Generated ${sampleData.length} sample records")

end ExcelDemoView
