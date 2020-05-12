package com.example.multistreamsearchview

import android.app.UiModeManager.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.multistreamsearchview.databinding.ActivityMainBinding
import com.multistream.multistreamsearchview.data_source.DataSource
import com.multistream.multistreamsearchview.search_view.SearchViewLayout
import com.multistream.multistreamsearchview.filter.FilterSelection
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.CHANNELS
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.GAMES
import com.multistream.multistreamsearchview.search_view.SearchViewLayout.Companion.STREAMS

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val sourceDownloader = DataSource.Builder()
            .setIconDrawable(R.drawable.twitch_icon)
            .setName("Twitch")
            .build(SearchViewLayout.SearchData::class.java) {
                getData(TWITCH, GAMES)
            }
        val sourceDownloader3 = DataSource.Builder()
            .setIconDrawable(R.drawable.recent_icon)
            .setName("All")
            .build(SearchViewLayout.SearchData::class.java) {
                getData(TWITCH, GAMES)
            }

        val sourceDownloader2 = DataSource.Builder()
            .setIconDrawable(R.drawable.mixer_icon)
            .setName("Mixer")
            .build(SearchViewLayout.SearchData::class.java) {
                getData2(
                    TWITCH,
                    GAMES
                )
            }
        binding.searchLayout.addSourceDownloader(sourceDownloader3)

        binding.searchLayout.addSourceDownloader(sourceDownloader)

        binding.searchLayout.addSourceDownloader(sourceDownloader2)
        val platform = FilterSelection.Builder()
            .setFilterSelectionName("Twitch")
            .build(SearchViewLayout.SearchData::class.java) { list -> list.filter { it.platform == TWITCH } }
        val platform2 = FilterSelection.Builder()
            .setFilterSelectionName("Mixer")
            .build(SearchViewLayout.SearchData::class.java) { list -> list.filter { it.platform == MIXER } }
        val listOfSelectionData = listOf(platform, platform2)
        val category = FilterSelection.Builder()
            .setFilterSelectionName("Games")
            .build(SearchViewLayout.SearchData::class.java) { list -> list.filter { it.category == GAMES } }
        val category2 = FilterSelection.Builder()
            .setFilterSelectionName("Channels")
            .build(SearchViewLayout.SearchData::class.java) { list -> list.filter { it.category == CHANNELS } }
        val category3 = FilterSelection.Builder()
            .setFilterSelectionName("Streams")
            .build(SearchViewLayout.SearchData::class.java) { list -> list.filter { it.category == STREAMS } }
        val listOfSelectionData1 = listOf(category, category2, category3)
        binding.searchLayout.addFilter("Choose category", listOfSelectionData1, false, true)
        binding.searchLayout.initSearchView()
    }

    companion object {
        val TWITCH = 0
        val MIXER = 1
        val YOUTUBE = 2
    }

    fun getData(category: Int, platform: Int): List<SearchViewLayout.SearchData> {
        return listOf(
            SearchViewLayout.SearchData(
                "FORTNITE",
                "https://static-cdn.jtvnw.net/ttv-boxart/Fortnite-285x380.jpg",
                GAMES,
                R.string.games_category,
                TWITCH,
                R.drawable.twitch_icon
            ),
            SearchViewLayout.SearchData(
                "Andy",
                "https://static-cdn.jtvnw.net/previews-ttv/live_user_thekairi78-440x248.jpg",
                STREAMS,
                R.string.streams_category,
                TWITCH,
                R.drawable.twitch_icon
            ),
            SearchViewLayout.SearchData(
                "Call of Duty: Modern Warfare",
                "https://static-cdn.jtvnw.net/ttv-boxart/Call%20of%20Duty:%20Modern%20Warfare-285x380.jpg",
                GAMES,
                R.string.games_category,
                TWITCH,
                R.drawable.twitch_icon
            ),
            SearchViewLayout.SearchData(
                "League of legends",
                "https://static-cdn.jtvnw.net/ttv-boxart/League%20of%20Legends-285x380.jpg",
                CHANNELS,
                R.string.channels_category,
                TWITCH,
                R.drawable.twitch_icon
            )
        )
    }

    fun getData2(category: Int, platform: Int): List<SearchViewLayout.SearchData> {
        return listOf(
            SearchViewLayout.SearchData(
                "Valorant",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                GAMES,
                R.string.games_category,
                MIXER,
                R.drawable.mixer_icon
            ),
            SearchViewLayout.SearchData(
                "Fortnite",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                GAMES,
                R.string.games_category,
                MIXER,
                R.drawable.mixer_icon
            ),
            SearchViewLayout.SearchData(
                "Drd",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/cdnthe3rd-profile_image-8246eb11f7f4b215-70x70.jpeg",
                CHANNELS,
                R.string.channels_category,
                MIXER,
                R.drawable.mixer_icon
            ),
            SearchViewLayout.SearchData(
                "Shr",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/bobross-profile_image-0b9dd167a9bb16b5-70x70.jpeg",
                CHANNELS,
                R.string.channels_category,
                MIXER,
                R.drawable.mixer_icon
            )
        )
    }
}
