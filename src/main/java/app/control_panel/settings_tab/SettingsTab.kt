package app.control_panel.settings_tab

import app.control_panel.ControlPanelTab
import app.ui.control.CollapseExpandButton
import app.ui.control.RoundedButton
import app.ui.control.ToggleButton
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import system.applications.ApplicationManager
import system.image.ImageResolver

class SettingsTab(val imageResolver: ImageResolver, val applicationManager: ApplicationManager) : ControlPanelTab() {
    private val scrollPane = ScrollPane()
    private val normalSettingsPane = VBox()
    private val advancedSettingsPane = VBox()
    private val showAdvancedBtn = CollapseExpandButton(imageResolver, "Show advanced", "Hide advanced") // TODO: i18n

    private val autolaunchBtn = ToggleButton()
    private val enableDokeySearchBtn = ToggleButton()
    private val supportBtn = RoundedButton("Contact us")  // TODO: i18n
    private val creditsBtn = RoundedButton("View credits")  // TODO: i18n

    private val clearCache = RoundedButton("Clear cache")  // TODO: i18n

    init {
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        normalSettingsPane.alignment = Pos.CENTER
        advancedSettingsPane.alignment = Pos.CENTER

        // Setup settings UI
        val autolaunchEntry = SettingEntry("Autolaunch", "Start Dokey at System Startup", autolaunchBtn)  // TODO: i18n
        val enableDokeySearchEntry = SettingEntry("Dokey Search", "Enable Dokey search bar", enableDokeySearchBtn)  // TODO: i18n
        val supportEntry = SettingEntry("Support", "Contact us for any question", supportBtn)  // TODO: i18n
        val creditsEntry = SettingEntry("Credits", "Dokey uses Open Source software", creditsBtn)  // TODO: i18n

        normalSettingsPane.children.addAll(autolaunchEntry, enableDokeySearchEntry, supportEntry, creditsEntry)
        normalSettingsPane.children.add(showAdvancedBtn)

        // Setup advanced settings UI
        val clearCacheEntry = SettingEntry("Clear Cache", "NOTE: Dokey must be restarted afterwards", clearCache)  // TODO: i18n

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
        creditsBtn.setOnAction {
            applicationManager.openWebLink("https://dokey.io/credits.html")
        }
    }

    override fun onFocus() {
        // Load all the settings
    }

    override fun onGlobalKeyPress(event: KeyEvent) {

    }

}