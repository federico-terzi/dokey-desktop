package system.search.agents

import org.xml.sax.*
import org.xml.sax.helpers.XMLReaderFactory
import system.applications.Application
import system.context.SearchContext
import system.search.annotations.RegisterAgent
import system.search.results.GoogleSearchResult
import system.search.results.Result
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList



@RegisterAgent(priority = 30)
class GoogleSearchAgent(context: SearchContext) : AbstractAgent(context) {
    override fun validate(query: String): Boolean = true

    override fun getResults(query: String, activeApplication: Application?): List<out Result> {
        val actualSearchResult = GoogleSearchResult(context, query)
        val results = mutableListOf<GoogleSearchResult>(actualSearchResult)
        val googleResults = getSuggestions(query).forEach {
            results.add(GoogleSearchResult(context, it))
        }
        return results
    }

    companion object {
        /**
         * Request the Search Suggestions from Google.
         * @param query the query to search.
         * @return a list of suggestions for the given query.
         */
        private fun getSuggestions(query: String): List<String> {
            val result = ArrayList<String>(10)

            // Request the suggestions and parse the resulting XML
            try {
                val url = "http://suggestqueries.google.com/complete/search?output=toolbar&q=" + URLEncoder.encode(query, "UTF-8")
                val myReader = XMLReaderFactory.createXMLReader()
                myReader.setContentHandler(object : ContentHandler {
                    override fun setDocumentLocator(locator: Locator) {

                    }

                    @Throws(SAXException::class)
                    override fun startDocument() {

                    }

                    @Throws(SAXException::class)
                    override fun endDocument() {

                    }

                    @Throws(SAXException::class)
                    override fun startPrefixMapping(prefix: String, uri: String) {

                    }

                    @Throws(SAXException::class)
                    override fun endPrefixMapping(prefix: String) {

                    }

                    @Throws(SAXException::class)
                    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
                        if (localName == "suggestion") {
                            val res = atts.getValue("data")
                            // Filter out the results equal to query
                            if (res != query)
                                result.add(res)
                        }
                    }

                    @Throws(SAXException::class)
                    override fun endElement(uri: String, localName: String, qName: String) {

                    }

                    @Throws(SAXException::class)
                    override fun characters(ch: CharArray, start: Int, length: Int) {

                    }

                    @Throws(SAXException::class)
                    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {

                    }

                    @Throws(SAXException::class)
                    override fun processingInstruction(target: String, data: String) {

                    }

                    @Throws(SAXException::class)
                    override fun skippedEntity(name: String) {

                    }
                })
                val inputSource = InputSource(InputStreamReader(URL(url).openStream(), "ISO-8859-1"))
                inputSource.setEncoding("ISO-8859-1")
                myReader.parse(inputSource)
            } catch (e: Exception) {
            }

            return result
        }
    }
}