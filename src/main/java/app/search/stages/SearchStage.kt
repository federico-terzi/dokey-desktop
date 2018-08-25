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
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.reflections.Reflections
import system.ResourceUtils
import system.applications.Application
import system.image.ImageResolver
import system.search.SearchEngine
import system.search.annotations.FilterableResult
import system.search.annotations.RegisterAgent
import system.search.results.Result
import system.search.results.ResultCategory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class SearchStage @Throws(IOException::class)
constructor(private val resourceBundle: ResourceBundle, private val searchEngine: SearchEngine,
            private val imageResolver: ImageResolver) : Stage() {

    private val controller: SearchController

    private val listView : SectionListView

    // Similar to the resultPriorityList, here are registered only the types of filter
    // the user can select. For example, the terminal filter cannot be selected by the user.
    private val userFilters : MutableList<FilterEntry> = mutableListOf<FilterEntry>()

    // If this is set, only show the results of this category
    private var resultFilter: ResultCategory? = null

    // The current results for the given query
    private var currentResults: MutableList<Result>? = null

    // This subject is used to debounce the display of results, to avoid flickering
    private val resultSubject = PublishSubject.create<SortedMap<ResultCategory, MutableList<Result>>>()

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
        listView = SectionListView(DIALOG_WIDTH, imageResolver)
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
            val currentlySelected = listView.selectionModel.selectedIndex
//            if (event.code == KeyCode.DOWN) {  // Select next element in the list
//                if (currentlySelected < controller.resultListView.items.size - 1) {
//                    controller.resultListView.selectionModel.selectIndex(currentlySelected + 1)
//                } else {  // Select the first
//                    if (controller.resultListView.items.size > 0) {
//                        controller.resultListView.selectionModel.selectIndex(0)
//                    }
//                }
//
//                event.consume()
//            } else if (event.code == KeyCode.UP) {  // Select previous element in the list
//                if (currentlySelected > 0) {
//                    controller.resultListView.selectionModel.selectIndex(currentlySelected - 1)
//                } else {  // Select the last one
//                    if (controller.resultListView.items.size > 0) {
//                        controller.resultListView.selectionModel.selectIndex(controller.resultListView.items.size - 1)
//                    }
//                }
//
//                event.consume()
//            } else if (event.code == KeyCode.ENTER) {  // Execute action and close the stage
//                val result = controller.resultListView.selectionModel.selectedItem as Result
//                result.executeAction()
//            } else if (event.code == KeyCode.ESCAPE) { // Close the search stage or remove filter
//                if (resultFilter != null) {  // REMOVE FILTER
//                    resultFilter = null
//                    elaborateResults()
//                } else {  // CLOSE
//                    close()
//                }
//            } else if (event.code == KeyCode.TAB) {  // Filter results
//                if (controller.resultListView.selectionModel.selectedItem == null ||
//                        (controller.resultListView.selectionModel.selectedItem as Result).javaClass.kotlin == resultFilter?.resultClass) {
//                    var index = 0
//                    if (resultFilter != null) {
//                        val nextIndex = userFilters.indexOf(resultFilter!!) + 1
//                        if (nextIndex != -1) {
//                            if (nextIndex >= userFilters.size) {
//                                index = 0
//                            } else {
//                                index = nextIndex
//                            }
//                        }
//                    }
//                    resultFilter = userFilters[index]
//                    elaborateResults()
//                } else {
//                    if (controller.resultListView.selectionModel.selectedItem != null) {
//                        val selectedClass = (controller.resultListView.selectionModel.selectedItem.javaClass as Class<out Result>).kotlin
//                        // Make sure the selected item is filterable from the user
//                        // for example, the terminal cannot be filtered
//                        val filterEntry = userFilters.find {it.resultClass == selectedClass}
//                        if (filterEntry != null) {
//                            resultFilter = filterEntry
//                            elaborateResults()
//                        }
//                    }
//                }
//
//                event.consume()
//            }
        }
        // Detect if window lose focus
        focusedProperty().addListener { _, _, isFocused ->
            if (isFocused == false)
                close()
        }

        // Result list view click listener, execute the corresponding action
//        controller.resultListView.setOnMouseClicked { event ->
//            val result = controller.resultListView.selectionModel.selectedItem as Result
//            result.executeAction()
//        }

        registerUserFilterList()

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
        y = (primScreenBounds.height - height) / 4 * 1
    }

    private fun elaborateResults() {
        if (currentResults == null)
            return

        if (currentResults!!.isEmpty()) {
            // Signal the empty list
            resultSubject.onNext(TreeMap<ResultCategory, MutableList<Result>>())
        }


        val initialResults = mutableListOf<Result>()

        if (resultFilter != null) {
            initialResults.addAll(currentResults!!.filter { it.category == resultFilter })
        }else{
            initialResults.addAll(currentResults!!)
        }

        // If the results has more than one category, limit the results for each one
        var limitResultsForCategory = currentResults!!.size > 1 && resultFilter == null

        val finalResults : SortedMap<ResultCategory, MutableList<Result>> = TreeMap<ResultCategory, MutableList<Result>>()

        var totalResultCount = 0

        for (result in initialResults) {
            if (totalResultCount > MAX_RESULTS) {
                break
            }

            if (finalResults[result.category] == null) {
                finalResults[result.category] = mutableListOf()
            }else{
                if (finalResults[result.category]!!.size >= MAX_RESULTS_FOR_AGENT) {
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
    private fun registerUserFilterList() {
        // Load all the filterable result
        val reflections = Reflections("system.search.results")
        val resultClasses = reflections.getTypesAnnotatedWith(FilterableResult::class.java)
        resultClasses.forEach { resultClass ->
            val annotation = resultClass.getAnnotation(FilterableResult::class.java) as FilterableResult
            userFilters.add(FilterEntry((resultClass as Class<out Result>).kotlin, resourceBundle.getString(annotation.filterName)))
        }
    }

    data class FilterEntry(val resultClass : KClass<out Result>, val labelText: String)

    companion object {
        val MAX_RESULTS = 9 // Maximum number of results
        val MAX_RESULTS_FOR_AGENT = 3 // Maximum number of results for each agent

        val DIALOG_WIDTH = 700.0
    }
}

/**
 * Workaround used to solve the select "Overload resolution ambiguity" problem.
 */
fun <T> MultipleSelectionModel<T>.selectIndex(index : Int) {
    this.select(index)
}