package app.search.stages

import app.search.controllers.SearchController
import app.search.listcells.ResultListCell
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Callback
import org.reflections.Reflections
import system.ResourceUtils
import system.image.ImageResolver
import system.search.SearchEngine
import system.search.annotations.FilterableResult
import system.search.annotations.RegisterAgent
import system.search.results.Result
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class SearchStage @Throws(IOException::class)
constructor(private val resourceBundle: ResourceBundle, private val searchEngine: SearchEngine,
            private val imageResolver: ImageResolver) : Stage() {

    private val controller: SearchController

    /**
     * This map is used to store image caches
     */
    private val imageCacheMap = ConcurrentHashMap<String, Image>()

    // In this list are registered the priority of the results in the search bar
    // The first elements are displayed first
    private val resultPriorityList = mutableListOf<KClass<out Result>>()

    // Similar to the resultPriorityList, here are registered only the types of filter
    // the user can select. For example, the terminal filter cannot be selected by the user.
    private val userFilters : MutableList<KClass<out Result>> = mutableListOf<KClass<out Result>>()

    // If this is set, only show the results of this category
    private var resultFilter: KClass<out Result>? = null

    // The result list, when modified the listview updates
    private val observableResults = FXCollections.observableArrayList<Result>()

    // The current results for the given query
    private var currentResults: MutableMap<KClass<out Result>, List<Result>>? = null

    // This subject is used to debounce the display of results, to avoid flickering
    private val resultSubject = PublishSubject.create<List<Result>>()

    private var currentQuery: String? = null

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
        controller.resultListView.isManaged = false

        // Hide the filter box
        controller.filterBox.isManaged = true
        controller.filterBox.isVisible = true

        // Setup the list cells
        val fallback = ImageResolver.getImage(SearchStage::class.java.getResourceAsStream("/assets/photo.png"), 32)
        controller.resultListView.cellFactory = Callback<ListView<Any>, ListCell<Any>> { ResultListCell(resourceBundle, fallback, imageResolver) as ListCell<Any> }

        // Setup the text field search callbacks
        controller.queryTextField.textProperty().addListener { _, _, searchQuery ->
            currentQuery = searchQuery

            // Reset the current result set
            currentResults?.clear()
            elaborateResults()

            if (!searchQuery.isBlank()) {
                searchEngine.requestQuery(searchQuery.trim()) { query, category, results ->
                    // Make sure the result is relative to the current query
                    if (currentQuery == query) {
                        currentResults!![category] = results
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
            val currentlySelected = controller.resultListView.selectionModel.selectedIndex
            if (event.code == KeyCode.DOWN) {  // Select next element in the list
                if (currentlySelected < controller.resultListView.items.size - 1) {
                    controller.resultListView.selectionModel.selectIndex(currentlySelected + 1)
                } else {  // Select the first
                    if (controller.resultListView.items.size > 0) {
                        controller.resultListView.selectionModel.selectIndex(0)
                    }
                }

                event.consume()
            } else if (event.code == KeyCode.UP) {  // Select previous element in the list
                if (currentlySelected > 0) {
                    controller.resultListView.selectionModel.selectIndex(currentlySelected - 1)
                } else {  // Select the last one
                    if (controller.resultListView.items.size > 0) {
                        controller.resultListView.selectionModel.selectIndex(controller.resultListView.items.size - 1)
                    }
                }

                event.consume()
            } else if (event.code == KeyCode.ENTER) {  // Execute action and close the stage
                val result = controller.resultListView.selectionModel.selectedItem as Result
                result.executeAction()
            } else if (event.code == KeyCode.ESCAPE) { // Close the search stage or remove filter
                if (resultFilter != null) {  // REMOVE FILTER
                    resultFilter = null
                    elaborateResults()
                } else {  // CLOSE
                    close()
                }
            } else if (event.code == KeyCode.TAB) {  // Filter results
                if (controller.resultListView.selectionModel.selectedItem == null || (controller.resultListView.selectionModel.selectedItem as Result).javaClass == resultFilter) {
                    var index = 0
                    if (resultFilter != null) {
                        val nextIndex = userFilters.indexOf(resultFilter!!) + 1
                        if (nextIndex != -1) {
                            if (nextIndex >= userFilters.size) {
                                index = 0
                            } else {
                                index = nextIndex
                            }
                        }
                    }
                    resultFilter = userFilters[index]
                    elaborateResults()
                } else {
                    if (controller.resultListView.selectionModel.selectedItem != null) {
                        val selectedClass = controller.resultListView.selectionModel.selectedItem.javaClass as KClass<out Result>
                        // Make sure the selected item is filterable from the user
                        // for example, the terminal cannot be filtered
                        if (userFilters.contains(selectedClass)) {
                            resultFilter = selectedClass
                            elaborateResults()
                        }
                    }
                }

                event.consume()
            }
        }
        // Detect if window lose focus
        focusedProperty().addListener { observable, oldValue, newValue ->
            if (newValue == false)
                close()
        }

        // Result list view click listener, execute the corresponding action
        controller.resultListView.setOnMouseClicked { event ->
            val result = controller.resultListView.selectionModel.selectedItem as Result
            result.executeAction()
        }

        registerResultPriorityList()
        registerUserFilterList()

        controller.resultListView.setItems(observableResults)

        // Setup the debouncing mechanism to avoid flickering when displaying the results
        resultSubject.debounce(50, TimeUnit.MILLISECONDS).subscribe(object : Observer<List<Result>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(priorityResults: List<Result>) {
                // Update the list view
                Platform.runLater {
                    observableResults.setAll(priorityResults)

                    // Select the first item
                    if (priorityResults.size > 0) {
                        controller.resultListView.selectionModel.selectIndex(0)
                    }

                    // Set the height based on the list view
                    controller.resultListView.prefHeight = (priorityResults.size * ResultListCell.ROW_HEIGHT).toDouble()

                    // Show the list view and refresh stage size to fit all contents
                    controller.resultListView.isManaged = true
                    sizeToScene()
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })

        preInitialize()
    }

    /**
     * Actions that must be done before showing the bar to initialize it to its empty state.
     */
    fun preInitialize() {
        // Reset the result data structures
        currentResults = HashMap()
        observableResults.clear()

        resultFilter = null

        // Reset the query text field to an empty string
        controller.queryTextField.text = ""

        elaborateResults()
    }

    /**
     * Actions that must be done after showing the bar to initialize it.
     */
    fun postInitialize() {
        // Reset the height of the list box so that it contains no elements
        controller.resultListView.prefHeight = 0.0

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
//        // Render filter label
//        Platform.runLater {
//            // Show the filter label
//            if (resultFilter != null) {
//                // Get the filter text
//                try {
//                    val labelID = resultFilter!!.getDeclaredField("SEARCH_FILDER_RESOURCE_ID").get(null) as String
//                    val filterText = resourceBundle.getString(labelID)
//                    if (filterText != null) {
//                        controller.filterLabel.text = filterText
//                        controller.filterBox.isManaged = true
//                        controller.filterBox.isVisible = true
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//            } else {  // hide the filter label
//                controller.filterBox.isManaged = false
//                controller.filterBox.isVisible = false
//            }
//        }

        if (currentResults == null)
            return

        if (currentResults!!.isEmpty()) {
            // Signal the empty list
            resultSubject.onNext(listOf<Result>())
        }

        val priorityResults = ArrayList<Result>(20)

        // If the results has more than one category, limit the results for each one
        val limitResultsForCategory = currentResults!!.keys.size > 1

        // If there is a result filter, only show those results
        if (resultFilter != null) {
            if (currentResults!!.containsKey(resultFilter!!)) {
                for (result in currentResults!![resultFilter!!]!!) {
                    if (priorityResults.size < MAX_RESULTS) {
                        priorityResults.add(result)
                    }
                }
            }
        } else {  // No filter, show all
            // Get the result for each result category
            for (resultClass in resultPriorityList) {
                if (currentResults!!.containsKey(resultClass)) {
                    var currentResult = 0

                    for (result in currentResults!![resultClass]!!) {
                        if ((currentResult < MAX_RESULTS_FOR_AGENT || !limitResultsForCategory) && priorityResults.size < MAX_RESULTS) {
                            priorityResults.add(result)
                            currentResult++
                        } else {
                            break
                        }
                    }
                }
            }
        }

        // Signal the new results
        resultSubject.onNext(priorityResults)
    }

    /**
     * Register the result type priority in the list
     */
    private fun registerResultPriorityList() {
        // Load all the priorities using reflection
        val reflections = Reflections("system.search.agents")
        val agentsClasses = reflections.getTypesAnnotatedWith(RegisterAgent::class.java)
        val unorderedAgents = mutableListOf<Pair<KClass<out Result>, Int>>()
        agentsClasses.forEach { agentClass ->
            val agentAnnotation = agentClass.getAnnotation(RegisterAgent::class.java) as RegisterAgent
            unorderedAgents.add(Pair<KClass<out Result>, Int>(agentAnnotation.resultClass, agentAnnotation.priority))
        }
        // Reorder the agents based on priority and add them to the list
        unorderedAgents.sortBy { it.second }
        unorderedAgents.forEach { resultPriorityList.add(it.first) }
    }

    /**
     * Register the user filters.
     */
    private fun registerUserFilterList() {
        // Load all the filterable result
        val reflections = Reflections("system.search.results")
        val resultClasses = reflections.getTypesAnnotatedWith(FilterableResult::class.java)
        resultClasses.forEach { resultClass ->
            userFilters.add((resultClass as Class<out Result>).kotlin)
        }
    }

    companion object {
        val MAX_RESULTS = 6 // Maximum number of results
        val MAX_RESULTS_FOR_AGENT = 3 // Maximum number of results for each agent
    }
}

/**
 * Workaround used to solve the select "Overload resolution ambiguity" problem.
 */
fun <T> MultipleSelectionModel<T>.selectIndex(index : Int) {
    this.select(index)
}