package com.cloudstream.plugins

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.network.get
import org.jsoup.Jsoup

@CloudstreamPlugin
class StreamedSuPlugin : Plugin() {
    override fun load() {
        // Load streamed.su as a new provider
        PluginManager.addProvider(StreamedSuProvider())
    }
}

class StreamedSuProvider : Plugin() {
    override val name: String = "StreamedSu"
    override val mainUrl: String = "https://streamed.su"
    override val apiVersion: Int = 3

    override suspend fun search(query: String): List<SearchResult> {
        // Not required, as this plugin scrapes sports streams directly
        return emptyList()
    }

    override suspend fun loadMainPage(): List<HomePageList> {
        val document = get(mainUrl).document
        val sportCategories = mutableListOf<HomePageList>()

        // Scrape sports categories
        val categories = document.select("div.sports-category") // Adjust CSS selector based on site structure
        for (category in categories) {
            val title = category.select("h3").text()
            val links = category.select("a")
            val homePageList = HomePageList(
                title, 
                links.map { link -> 
                    HomePageListItem(
                        title = link.text(), 
                        link = fixUrl(link.attr("href"))
                    )
                }
            )
            sportCategories.add(homePageList)
        }
        return sportCategories
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = get(url).document
        val streams = document.select("a.stream-link") // Adjust CSS selector to get stream links
        return streams.map { stream ->
            Stream(
                name = stream.text(),
                url = stream.attr("href"),
                quality = "Auto"
            )
        }
    }
}