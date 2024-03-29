package app.search.stages

import app.search.controllers.SearchController
import app.search.sectionlistview.SectionListView
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.reflections.Reflections
import system.ResourceUtils
import system.applications.Application
import system.applications.ApplicationManager
import system.image.ImageResolver
import system.search.SearchEngine
import system.search.annotations.FilterableResult
import system.search.results.Result
import system.search.results.ResultCategory
import utils.OSValidator
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * The Dokey Search stage.
 */
class SearchStage @Throws(IOException::class)
constructor(private val resourceBundle: ResourceBundle, private val searchEngine: SearchEngine,
            private val imageResolver: ImageResolver) : Stage() {

    private val controller: SearchController

    // This list view will hold the results of the search
    private val listView : SectionListView

    // Here are registered only the types of filter the user can select.
    // For example, the terminal filter cannot be selected by the user.
    private val filterableResults : MutableSet<KClass<out Result>> = mutableSetOf()

    // If this is set, only show the results of this category
    private var resultFilter: ResultCategory? = null

    // The current results for the given query
    private var currentResults: MutableList<Result>? = null

    // This subject is used to debounce the display of results, to avoid flickering
    private val resultSubject = PublishSubject.create<SortedMap<ResultCategory, MutableList<Result>>>()

    // Current search query
    private var currentQuery: String? = null

    // The application that was active before the bar was shown
    private var activeApplication: Application? = null

    init {
        val fxmlLoader = FXMLLoader(ResourceUtils.getResource("/layouts/search_dialog.fxml")!!.toURI().toURL())
        fxmlLoader.resources = resourceBundle
        val root = fxmlLoader.load<Parent>()
        val scene = Scene(root)
        scene.stylesheets.add(ResourceUtils.getResource("/css/search_dialog.css")!!.toURI().toString())
        this.title = "Dokey Search"  // DON'T CHANGE, used on window in the focus mechanism.
        this.scene = scene
        this.isAlwaysOnTop = true
        initStyle(StageStyle.TRANSPARENT)
        scene.fill = Color.TRANSPARENT
        this.icons.add(Image(SearchStage::class.java.getResourceAsStream("/assets/icon.png")))

        controller = fxmlLoader.getController<Any>() as SearchController

        // Setup the bar width
        controller.rootNode.prefWidth = DIALOG_WIDTH

        // Setup the listview
        listView = SectionListView(imageResolver) {
            this.hide()  // Hide the window when a user clicks on a result
        }

        controller.rootNode.children.add(listView)
        listView.isManaged = false

        // Hide the filter box
        controller.filterBox.isManaged = true
        controller.filterBox.isVisible = true

        // Setup the text field search callbacks
        controller.queryTextField.textProperty().addListener { _, _, searchQuery ->
            currentQuery = searchQuery

            // Reset the current result set
            currentResults?.clear()
            elaborateResults()

            if (!searchQuery.isBlank()) {
                searchEngine.requestQuery(searchQuery.trim(), activeApplication) { query, results ->
                    // Make sure the result is relative to the current query
                    if (currentQuery == query) {
                        currentResults!!.addAll(results)
                        elaborateResults()
                    }
                }
            }
        }

        // If someone deselects the textfield, re select it
        controller.queryTextField.focusedProperty().addListener { _, _, isFocused ->
            if (!isFocused) {
                Platform.runLater { controller.queryTextField.requestFocus() }
            }
        }

        // Setup keyboard events
        controller.queryTextField.setOnKeyPressed { event ->
            val currentlySelected = listView.getSelectedIndex()
            if (event.code == KeyCode.DOWN) {  // Select next element in the list
                if (currentlySelected < listView.getTotalItems() - 1) {
                    listView.selectIndex(listView.getSelectedIndex() + 1)
                } else {  // Select the first
                    if (listView.getTotalItems() > 0) {
                        listView.selectIndex(0)
                    }
                }

                event.consume()
            } else if (event.code == KeyCode.UP) {  // Select previous element in the list
                if (currentlySelected > 0) {
                    listView.selectIndex(listView.getSelectedIndex() - 1)
                } else {  // Select the last one
                    if (listView.getTotalItems() > 0) {
                        listView.selectIndex(listView.getTotalItems() - 1)
                    }
                }

                event.consume()
            }
            else if (event.code == KeyCode.ENTER) {  // Execute action and close the stage
                listView.executeCurrentResult()
                this.hide()
            } else if (event.code == KeyCode.ESCAPE) { // Close the search stage or remove filter
                if (resultFilter != null) {  // REMOVE FILTER
                    resultFilter = null
                    elaborateResults()
                } else {  // CLOSE
                    closeAndRefocus()
                }
            } else if (event.code == KeyCode.ALT) { // Filter
                val currentResult = listView.getSelectedResult()

                if (currentResult != null) {
                    if (resultFilter == null && filterableResults.contains(currentResult::class)) {
                        resultFilter = currentResult.category
                    }

                    elaborateResults()
                }
            } else if (event.code == KeyCode.TAB) {  // Select next category
                listView.selectNextSection()

                event.consume()
            }
        }
        // Detect if window lose focus
        focusedProperty().addListener { _, _, isFocused ->
            if (isFocused == false)
                closeAndRefocus()
        }

        registerFilterableResults()

        // Setup the debouncing mechanism to avoid flickering when displaying the results
        resultSubject.debounce(50, TimeUnit.MILLISECONDS)
                .subscribe(object : Observer<SortedMap<ResultCategory, MutableList<Result>>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(results: SortedMap<ResultCategory, MutableList<Result>>) {
                // Update the list view
                Platform.runLater {
                    // Render filter label
                    if (resultFilter != null) {
                        // Get the filter text
                        try {
                            controller.filterLabel.text = resultFilter?.name
                            controller.filterBox.isManaged = true
                            controller.filterBox.isVisible = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {  // hide the filter label
                        controller.filterBox.isManaged = false
                        controller.filterBox.isVisible = false
                    }

                    // Render results
                    val emptySearch = (currentQuery == null) || (currentQuery != null && currentQuery!!.isBlank())
                    if (results.isNotEmpty() || emptySearch) {
                        listView.setResults(results)

                        // Select the first item
                        if (results.size > 0) {
                            listView.selectIndex(0)
                        }

                        // Set the height based on the list view
                        listView.adaptHeight()

                        // Show the list view and refresh stage size to fit all contents
                        listView.isManaged = true
                        sizeToScene()
                    }
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }

    /**
     * Actions that must be done before showing the bar to initialize it to its empty state.
     */
    fun preInitialize(activeApplication: Application?) {
        // Reset the result data structures
        currentResults = mutableListOf()

        resultFilter = null

        // Reset the query text field to an empty string
        controller.queryTextField.text = ""

        this.activeApplication = activeApplication

        elaborateResults()
    }

    /**
     * Actions that must be done after showing the bar to initialize it.
     */
    fun postInitialize() {
        // Reset the height of the list box so that it contains no elements
        listView.prefHeight = 0.0

        // Resize the scene to fit the listview
        sizeToScene()

        // Center the bar in the screen
        positionBarOnScreen()
    }

    /**
     * Position the bar in the correct position of the screen
     */
    private fun positionBarOnScreen() {
        // Calculate the correct coordinates for the center of the screen
        val primScreenBounds = Screen.getPrimary().visualBounds
        x = (primScreenBounds.width - width) / 2
        y = (primScreenBounds.height - height) / 6 * 1
    }

    /**
     * Elaborate the currentResults by grouping them into categories, ordering them
     * based on the priority and trimming to the correct number of results per category.
     */
    private fun elaborateResults() {
        if (currentResults == null)
            return

        if (currentResults!!.isEmpty()) {
            // Signal the empty list
            resultSubject.onNext(TreeMap<ResultCategory, MutableList<Result>>())
        }

        // Add all the current results to the initial set of results
        var initialResults = mutableListOf<Result>()
        initialResults.addAll(currentResults!!)

        // The maximum number of results for each agent, this will be recalculated later
        var maxNumberOfResultsForAgent = MAX_RESULTS

        if (resultFilter != null) {
            // Filter the initial results based on the filtered category
            initialResults = initialResults.filter { it.category == resultFilter }.toMutableList()
        }else{
            // Calculate the number of maximum number of results for each agent dynamically

            // Get the total number of categories to calculate the number of results for each category
            val totalNumberOfCategories = initialResults.map { it.category.name }.distinct().count()

            // Clip the number of category to the MAX_DISPLAYED_CATEGORIES
            val numberOfCategories = if (totalNumberOfCategories > MAX_DISPLAYED_CATEGORIES)
                MAX_DISPLAYED_CATEGORIES
            else
                totalNumberOfCategories

            // Calculate the maximum number of results based on the number of categories
            maxNumberOfResultsForAgent = if (numberOfCategories > 0 )
                MAX_RESULTS / numberOfCategories
            else
                MAX_RESULTS
        }

        // This sorted map will hold the prioritized results
        val finalResults : SortedMap<ResultCategory, MutableList<Result>> = TreeMap<ResultCategory, MutableList<Result>>()

        var totalResultCount = 0

        for (result in initialResults) {
            // Stop the process if the maximum number of results is reached
            if (totalResultCount > MAX_RESULTS) {
                break
            }

            // Initialize the list for the given category
            if (finalResults[result.category] == null) {
                finalResults[result.category] = mutableListOf()
            }else{
                // Clip the number of results for the given agent if exceeded
                if (finalResults[result.category]!!.size > maxNumberOfResultsForAgent) {
                    continue
                }
            }

            finalResults[result.category]!!.add(result)
            totalResultCount++
        }

        // Signal the new results
        resultSubject.onNext(finalResults)
    }

    /**
     * Register the user filters.
     */
    private fun registerFilterableResults() {
        // Load all the filterable result
        val reflections = Reflections("system.search.results")
        val resultClasses = reflections.getTypesAnnotatedWith(FilterableResult::class.java)
        resultClasses.forEach { resultClass ->
            filterableResults.add((resultClass as Class<out Result>).kotlin)
        }
    }

    /**
     * In some systems, after the window has focused, the bar should focus back to the original
     * application ( like in MacOS ), because the OS does not do that automatically.
     */
    private fun closeAndRefocus() {
        if (OSValidator.isMac()) {
            Platform.runLater { activeApplication?.open() }
        }
        close()
    }

    companion object {
        val MAX_RESULTS = 9 // Maximum number of results
        val MAX_DISPLAYED_CATEGORIES = 4  // Maximum number of categories shown at once

        val DIALOG_WIDTH = 700.0  // The width of the BAR
    }
}