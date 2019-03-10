package system.external.photoshop

class PhotoshopManager(val engine: PhotoshopEngine) {
    val sliders = mutableMapOf<PhotoshopSliderAction, String>(
            PhotoshopSliderAction.LAYER_OPACITY to "app.activeDocument.activeLayer.opacity=arguments[0];",
            PhotoshopSliderAction.BRUSH_SIZE to "var idsetd = charIDToTypeID( \"setd\" );var desc12 = new ActionDescriptor();var idnull = charIDToTypeID( \"null\" );var ref7 = new ActionReference();var idBrsh = charIDToTypeID( \"Brsh\" );var idOrdn = charIDToTypeID( \"Ordn\" );var idTrgt = charIDToTypeID( \"Trgt\" );ref7.putEnumerated( idBrsh, idOrdn, idTrgt );desc12.putReference( idnull, ref7 );var idT = charIDToTypeID( \"T   \" );var desc13 = new ActionDescriptor();var idmasterDiameter = stringIDToTypeID( \"masterDiameter\" );var idPxl = charIDToTypeID( \"#Pxl\" );desc13.putUnitDouble( idmasterDiameter, idPxl, arguments[0] );var idBrsh = charIDToTypeID( \"Brsh\" );desc12.putObject( idT, idBrsh, desc13 );executeAction( idsetd, desc12, DialogModes.ALL );",
            PhotoshopSliderAction.LAYER_SELECTOR to "var layers = [];function addLayers(layerSet) {for (var i = 0; i < layerSet.length; i++) {if (layerSet[i].typename == \"LayerSet\") {addLayers(layerSet[i].layers);} else {layers.push(layerSet[i]);}}}addLayers(app.activeDocument.layers);app.activeDocument.activeLayer=layers[parseInt((1-(arguments[0]/100))*(layers.length-1))];"
    )

    val commands = mutableMapOf<PhotoshopCommandAction, String>(
            PhotoshopCommandAction.NEXT_LAYER to "var layers = [];function addLayers(layerSet) {for (var i =0; i<layerSet.length; i++) {if ( layerSet[i].typename == \"LayerSet\" ) {addLayers(layerSet[i].layers);}else{layers.push(layerSet[i]);}}}addLayers(app.activeDocument.layers);for (var i =0; i<layers.length; i++) {if (layers[i] == app.activeDocument.activeLayer) {if (i < layers.length -1) {app.activeDocument.activeLayer = layers[i+1];}break;}}",
            PhotoshopCommandAction.PREV_LAYER to "var layers = [];function addLayers(layerSet) {for (var i =0; i<layerSet.length; i++) {if ( layerSet[i].typename == \"LayerSet\" ) {addLayers(layerSet[i].layers);}else{layers.push(layerSet[i]);}}}addLayers(app.activeDocument.layers);for (var i =0; i<layers.length; i++) {if (layers[i] == app.activeDocument.activeLayer) {if (i > 0) {app.activeDocument.activeLayer = layers[i-1];}break;}}"
    )

    fun moveSlider(sliderId: PhotoshopSliderAction, value: Double) {
        if (sliders.contains(sliderId)) {
            engine.executeJavascript(sliders[sliderId]!!, arrayOf(value))
        }
    }

    fun executeCommand(command: PhotoshopCommandAction) {
        if (commands.contains(command)) {
            engine.executeJavascript(commands[command]!!, arrayOf())
        }
    }
}