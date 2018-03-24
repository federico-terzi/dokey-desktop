package system.search.agents;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.GoogleSearchResult;
import utils.ImageResolver;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GoogleSearchAgent extends AbstractAgent {
    private ApplicationManager applicationManager;

    public GoogleSearchAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, ApplicationManager applicationManager) {
        super(searchEngine, resourceBundle, GoogleSearchResult.class);

        this.applicationManager = applicationManager;

        this.defaultImage = ImageResolver.getInstance().getImage(GoogleSearchAgent.class.getResourceAsStream("/assets/google.png"), 32);
    }

    @Override
    public boolean validate(String query) {
        return true;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        GoogleSearchResult result = new GoogleSearchResult(searchEngine, resourceBundle, query, defaultImage);
        ArrayList<GoogleSearchResult> results = new ArrayList<>(11);
        results.add(result);

        // Get the google suggestions
        for (String suggestion : getSuggestions(query)) {
            GoogleSearchResult current = new GoogleSearchResult(searchEngine, resourceBundle, suggestion, defaultImage);
            results.add(current);
        }

        return results;
    }

    /**
     * Request the Search Suggestions from Google.
     * @param query the query to search.
     * @return a list of suggestions for the given query.
     */
    private static List<String> getSuggestions(String query) {
        List<String> result = new ArrayList<>(10);

        // Request the suggestions and parse the resulting XML
        try {
            String url = "http://suggestqueries.google.com/complete/search?output=toolbar&q="+ URLEncoder.encode(query, "UTF-8");
            XMLReader myReader = XMLReaderFactory.createXMLReader();
            myReader.setContentHandler(new ContentHandler() {
                @Override
                public void setDocumentLocator(Locator locator) {

                }

                @Override
                public void startDocument() throws SAXException {

                }

                @Override
                public void endDocument() throws SAXException {

                }

                @Override
                public void startPrefixMapping(String prefix, String uri) throws SAXException {

                }

                @Override
                public void endPrefixMapping(String prefix) throws SAXException {

                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    if (localName.equals("suggestion")) {
                        String res = atts.getValue("data");
                        // Filter out the results equal to query
                        if (!res.equals(query))
                            result.add(res);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {

                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {

                }

                @Override
                public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

                }

                @Override
                public void processingInstruction(String target, String data) throws SAXException {

                }

                @Override
                public void skippedEntity(String name) throws SAXException {

                }
            });
            myReader.parse(new InputSource(new URL(url).openStream()));
        } catch (Exception e) {}

        return result;
    }
}
