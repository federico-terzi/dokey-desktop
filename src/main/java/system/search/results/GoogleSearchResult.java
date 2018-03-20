package system.search.results;

import javafx.scene.image.Image;
import system.search.SearchEngine;
import utils.ImageResolver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ResourceBundle;

public class GoogleSearchResult extends AbstractResult {
    private String query;

    public GoogleSearchResult(SearchEngine searchEngine, ResourceBundle resourceBundle, String query, Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
        this.query = query;
        this.isIcon = false;
    }

    @Override
    public String getTitle() {
        return query;
    }

    @Override
    public String getDescription() {
        return resourceBundle.getString("search") + " \""+query+"\" "+resourceBundle.getString("on_google");
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                listener.onImageAvailable(defaultImage, null);
            }).start();
        }
    }

    @Override
    public void executeAction() {
        try {
            // Search the requested query on google
            searchEngine.getApplicationManager().openWebLink("https://www.google.it/search?q="+ URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
