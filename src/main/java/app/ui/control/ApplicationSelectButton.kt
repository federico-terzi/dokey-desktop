package app.ui.control

import app.control_panel.dialog.app_select_dialog.ApplicationSelectDialog
import app.ui.stage.BlurrableStage
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import system.applications.Application
import system.applications.ApplicationManager
import system.image.ImageResolver

class ApplicationSelectButton(val parent: BlurrableStage, val imageResolver: ImageResolver,
                              val applicationManager: ApplicationManager, val allowGlobal : Boolean = true) : Button() {
    private val contentBox = HBox()
    private val imageView = ImageView()
    private val nameLabel = Label()
    private val descriptionLabel = Label()

    private var _application : Application? = null
    var application : Application?
        get() = _application
        set(value) {
            _application = value

            if (_application != null) {
                imageResolver.loadInto("app:${_application?.id}", 24, imageView)
                nameLabel.text = _application?.name
                descriptionLabel.text = "Click to change the app..."  // TODO: i18n
            }else{
                if (allowGlobal) {
                    imageResolver.loadInto("asset:world_black", 24, imageView)
                    nameLabel.text = "Global"  // TODO: i18n
                }else{
                    imageResolver.loadInto("asset:launch", 24, imageView)
                    nameLabel.text = "Not selected"  // TODO: i18n
                }

                descriptionLabel.text = "Click to select an app..."  // TODO: i18n
            }
        }

    init {
        styleClass.add("application-select-button")

        nameLabel.styleClass.add("application-select-button-name")
        descriptionLabel.styleClass.add("application-select-button-desc")

        imageView.fitHeight = 24.0
        imageView.fitWidth = 24.0

        application = null

        val vBox = VBox()
        vBox.children.addAll(nameLabel, descriptionLabel)

        contentBox.alignment = Pos.CENTER
        contentBox.spacing = 6.0

        contentBox.children.addAll(imageView, vBox)

        graphic = contentBox

        setOnAction {
            val dialog = ApplicationSelectDialog(parent, imageResolver, applicationManager)
            dialog.onApplicationSelected = { app ->
                application = app
            }
            dialog.showWithAnimation()
        }
    }
}