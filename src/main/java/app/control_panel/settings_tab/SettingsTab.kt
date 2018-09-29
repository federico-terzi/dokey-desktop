package app.control_panel.settings_tab

import app.control_panel.ControlPanelTab
import app.ui.control.CollapseExpandButton
import app.ui.control.RoundedButton
import app.ui.control.ToggleButton
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import system.SettingsManager
import system.applications.ApplicationManager
import system.image.ImageResolver
import system.startup.StartupManager
import java.util.*
import javafx.scene.control.ButtonType
import system.storage.StorageManager


class SettingsTab(val imageResolver: ImageResolver, val applicationManager: ApplicationManager,
                  val settingsManager: SettingsManager, val startupManager: StartupManager,
                  val resourceBundle: ResourceBundle, val storageManager: StorageManager) : ControlPanelTab() {
    private val scrollPane = ScrollPane()
    private val normalSettingsPane = VBox()
    private val advancedSettingsPane = VBox()
    private val showAdvancedBtn = CollapseExpandButton(imageResolver, "Show advanced", "Hide advanced") // TODO: i18n

    private val autolaunchBtn = ToggleButton()
    private val enableDokeySearchBtn = ToggleButton()
    private val showDeletedCommandsBtn = ToggleButton()
    private val supportBtn = RoundedButton("Contact us")  // TODO: i18n
    private val creditsBtn = RoundedButton("View credits")  // TODO: i18n

    private val clearCacheBtn = RoundedButton("Clear cache")  // TODO: i18n

    init {
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        normalSettingsPane.alignment = Pos.CENTER
        advancedSettingsPane.alignment = Pos.CENTER

        // Setup settings UI
        val autolaunchEntry = SettingEntry("Autolaunch", "Start Dokey at System Startup", autolaunchBtn)  // TODO: i18n
        val enableDokeySearchEntry = SettingEntry("Dokey Search", "Enable Dokey search bar", enableDokeySearchBtn)  // TODO: i18n
        val showDeletedCommandsEntry = SettingEntry("Show Deleted Commands", "Show the deleted commands in the tab", showDeletedCommandsBtn)  // TODO: i18n
        val supportEntry = SettingEntry("Support", "Contact us for any question", supportBtn)  // TODO: i18n
        val creditsEntry = SettingEntry("Credits", "Dokey uses Open Source software", creditsBtn)  // TODO: i18n

        normalSettingsPane.children.addAll(autolaunchEntry, enableDokeySearchEntry, showDeletedCommandsEntry, supportEntry, creditsEntry)
        normalSettingsPane.children.add(showAdvancedBtn)

        // Setup advanced settings UI
        val clearCacheEntry = SettingEntry("Clear Cache", "NOTE: Dokey must be restarted afterwards", clearCacheBtn)  // TODO: i18n

        advancedSettingsPane.children.addAll(clearCacheEntry)


        advancedSettingsPane.isManaged = false
        advancedSettingsPane.isVisible = false

        showAdvancedBtn.onCollapse = {
            advancedSettingsPane.isManaged = false
            advancedSettingsPane.isVisible = false
        }
        showAdvancedBtn.onExpand = {
            advancedSettingsPane.isManaged = true
            advancedSettingsPane.isVisible = true
        }

        children.addAll(normalSettingsPane, advancedSettingsPane)

        // Action listeners

        autolaunchBtn.onToggle = {checked ->
            Thread {
                val result = if (checked) {
                    startupManager.enableAutomaticStartup()
                }else{
                    startupManager.disableAutomaticStartup()
                }

                // Update the checkbox
                Platform.runLater { autolaunchBtn.checked = checked }

                if (!result) {  // An error occurred
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = resourceBundle.getString("error")
                    alert.headerText = resourceBundle.getString("cannot_start_automatically")

                    val alertResult = alert.showAndWait()
                }
            }.start()
        }

        enableDokeySearchBtn.onToggle = { checked ->
            settingsManager.dokeySearchEnabled = checked
        }
        showDeletedCommandsBtn.onToggle = { checked ->
            settingsManager.showDeletedCommands = checked
        }

        creditsBtn.setOnAction {
            applicationManager.openWebLink("https://dokey.io/credits.html")
        }

        clearCacheBtn.setOnAction {
            val result = storageManager.clearCache()
            if (result) {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = resourceBundle.getString("cache_deleted")
                alert.headerText = resourceBundle.getString("cache_deleted_msg")

                val alertResult = alert.showAndWait()
                System.exit(0)
            } else {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Error deleting cache!"
                alert.headerText = "Unfortunately, the cache couldn't be deleted!"

                val alertResult = alert.showAndWait()
            }
        }
    }

    override fun onFocus() {
        // Load all the settings
        enableDokeySearchBtn.checked = settingsManager.dokeySearchEnabled
        showDeletedCommandsBtn.checked = settingsManager.showDeletedCommands
        autolaunchBtn.checked = startupManager.isAutomaticStartupEnabled
    }
}