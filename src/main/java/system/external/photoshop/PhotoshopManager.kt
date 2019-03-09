package system.external.photoshop

class PhotoshopManager(val engine: PhotoshopEngine) {
    val sliders = mutableMapOf<PhotoshopSliderAction, String>(
            PhotoshopSliderAction.LAYER_OPACITY to "app.activeDocument.activeLayer.opacity=arguments[0];",
            PhotoshopSliderAction.BRUSH_SIZE to "var idsetd = charIDToTypeID( \"setd\" );var desc12 = new ActionDescriptor();var idnull = charIDToTypeID( \"null\" );var ref7 = new ActionReference();var idBrsh = charIDToTypeID( \"Brsh\" );var idOrdn = charIDToTypeID( \"Ordn\" );var idTrgt = charIDToTypeID( \"Trgt\" );ref7.putEnumerated( idBrsh, idOrdn, idTrgt );desc12.putReference( idnull, ref7 );var idT = charIDToTypeID( \"T   \" );var desc13 = new ActionDescriptor();var idmasterDiameter = stringIDToTypeID( \"masterDiameter\" );var idPxl = charIDToTypeID( \"#Pxl\" );desc13.putUnitDouble( idmasterDiameter, idPxl, arguments[0] );var idBrsh = charIDToTypeID( \"Brsh\" );desc12.putObject( idT, idBrsh, desc13 );executeAction( idsetd, desc12, DialogModes.ALL );"
    )

    fun moveSlider(sliderId: PhotoshopSliderAction, value: Double) {
        if (sliders.contains(sliderId)) {
            engine.executeJavascript(sliders[sliderId]!!, arrayOf(value))
        }
    }
}