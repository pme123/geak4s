package pme123.geak4s.views

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import pme123.geak4s.state.AppState
import pme123.geak4s.services.ExcelService

/** Welcome screen with options to start new or import existing project */
object WelcomeView:
  
  def apply(): HtmlElement =
    val fileInputRef = Var[Option[dom.html.Input]](None)
    val errorMessage = Var[Option[String]](None)
    val isLoading = Var(false)
    
    div(
      className := "welcome-view",
      
      // Hero section
      div(
        className := "welcome-hero",
        Icon(_.name := IconName.`building`),
        Title(
          _.level := TitleLevel.H1,
          "GEAK Expert Tool"
        ),
        Label(
          _.wrappingType := WrappingType.Normal,
          "Gebäudeenergieausweis der Kantone - Professional Project Management"
        )
      ),
      
      // Action cards
      div(
        className := "welcome-actions",

        // New Project Card
        Card(
          _.slots.header := CardHeader(
            _.titleText := "New Project",
            _.subtitleText := "Start a fresh GEAK assessment",
            _.slots.avatar := Icon(_.name := IconName.`add-document`)
          ),
          div(
            className := "card-content",
            Label(
              _.wrappingType := WrappingType.Normal,
              "Create a new GEAK project from scratch. You'll be guided through all required sections."
            ),
            div(
              className := "card-actions",
              Button(
                _.design := ButtonDesign.Emphasized,
                _.icon := IconName.`create`,
                _.events.onClick.mapTo(()) --> Observer[Unit] { _ =>
                  AppState.createNewProject()
                },
                "Create New Project"
              )
            )
          )
        ),

        // Example Project Card
        Card(
          _.slots.header := CardHeader(
            _.titleText := "Project with Example Data",
            _.subtitleText := "Explore with pre-filled data",
            _.slots.avatar := Icon(_.name := IconName.`example`)
          ),
          div(
            className := "card-content",
            Label(
              _.wrappingType := WrappingType.Normal,
              "Start with a complete example project including sample building data, envelope components, HVAC systems, and energy producers."
            ),
            div(
              className := "card-actions",
              Button(
                _.design := ButtonDesign.Emphasized,
                _.icon := IconName.`lightbulb`,
                _.events.onClick.mapTo(()) --> Observer[Unit] { _ =>
                  AppState.createExampleProject()
                },
                "Load Example Project"
              )
            )
          )
        ),
        
        // Import Project Card
        Card(
          _.slots.header := CardHeader(
            _.titleText := "Import Project",
            _.subtitleText := "Load existing GEAK Excel file",
            _.slots.avatar := Icon(_.name := IconName.`excel-attachment`)
          ),
          div(
            className := "card-content",
            Label(
              _.wrappingType := WrappingType.Normal,
              "Import an existing GEAK project from an Excel file. All data will be loaded and ready to edit."
            ),
            
            // Error message
            child.maybe <-- errorMessage.signal.map(_.map { msg =>
              MessageStrip(
                _.design := MessageStripDesign.Negative,
                _.hideCloseButton := true,
                msg
              )
            }),
            
            // Loading indicator
            child.maybe <-- isLoading.signal.map {
              case true => Some(BusyIndicator(_.active := true, _.size := BusyIndicatorSize.Medium))
              case false => None
            },
            
            div(
              className := "card-actions",
              Button(
                _.design := ButtonDesign.Emphasized,
                _.icon := IconName.`upload`,
                _.disabled <-- isLoading.signal,
                _.events.onClick.mapTo(()) --> Observer[Unit] { _ =>
                  fileInputRef.now().foreach(_.click())
                },
                "Import Excel File"
              ),
              // Hidden file input
              input(
                tpe := "file",
                accept := ".xlsx,.xls",
                display := "none",
                onMountCallback { ctx =>
                  fileInputRef.set(Some(ctx.thisNode.ref))
                },
                onChange --> Observer[dom.Event] { e =>
                  val input = e.target.asInstanceOf[dom.html.Input]
                  val files = input.files
                  
                  if files != null && files.length > 0 then
                    val file = files(0)
                    errorMessage.set(None)
                    isLoading.set(true)
                    AppState.setLoading(file.name)
                    
                    ExcelService.readExcelFile(
                      file,
                      onSuccess = (project, fileName) =>
                        isLoading.set(false)
                        AppState.loadProject(project, fileName)
                      ,
                      onError = msg =>
                        isLoading.set(false)
                        errorMessage.set(Some(msg))
                        AppState.setError(msg)
                    )
                    
                    // Reset input
                    input.value = ""
                }
              )
            )
          )
        )
      ),
      
      // Info section
      div(
        className := "welcome-info",
        Title(
          _.level := TitleLevel.H3,
          "Features"
        ),
        div(
          className := "feature-list",
          featureItem(IconName.`edit`, "Guided Data Entry", "Step-by-step process for all GEAK sections"),
          featureItem(IconName.`validate`, "Data Validation", "Automatic validation of all inputs"),
          featureItem(IconName.`excel-attachment`, "Excel Export", "Export to standard GEAK Excel format"),
          featureItem(IconName.`save`, "Auto-Save", "Your work is automatically saved in the browser")
        )
      )
    )
  
  private def featureItem(icon: IconName, title: String, description: String): HtmlElement =
    div(
      className := "feature-item",
      Icon(_.name := icon),
      div(
        className := "feature-text",
        Title(_.level := TitleLevel.H5, title),
        Label(_.wrappingType := WrappingType.Normal, description)
      )
    )

end WelcomeView

