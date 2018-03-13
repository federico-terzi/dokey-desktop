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

    public GoogleSearchResult(SearchEngine searchEngine, ResourceBundle resourceBundle, String query) {
        super(searchEngine, resourceBundle);
        this.query = query;
        this.isIcon = false;
    }

    @Override
    public String getTitle() {
        return query;
    }

    @Override
    public String getDescription() {
        return "Search \""+query+"\" on Google";  // TODO: i18n
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                Image appImage = ImageResolver.getInstance().getImage(GoogleSearchResult.class.getResourceAsStream("/assets/google.png"), 32);
                listener.onImageAvailable(appImage, null);
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
