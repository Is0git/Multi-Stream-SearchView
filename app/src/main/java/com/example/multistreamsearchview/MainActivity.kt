package com.example.multistreamsearchview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.multistreamsearchview.databinding.ActivityMainBinding
import com.multistream.multistreamsearchview.DataSource
import com.multistream.multistreamsearchview.SearchDataFilter
import com.multistream.multistreamsearchview.SearchViewLayout
import com.multistream.multistreamsearchview.SelectionData

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    companion object {
        val GAMES = 0
        val CHANNELS = 1
        val STREAMS = 2

        val TWITCH = 0
        val MIXER = 1
        val YOUTUBE = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.searchLayout.addSourceDownloader(object :
            DataSource.SourceDownloader<SearchViewLayout.SearchData> {

            override var isEnabled: Boolean = true

            override suspend fun getData(): List<SearchViewLayout.SearchData> {
                return getData(GAMES, TWITCH)
            }
        })

        binding.searchLayout.addSourceDownloader(object :
            DataSource.SourceDownloader<SearchViewLayout.SearchData> {

            override var isEnabled: Boolean = true

            override suspend fun getData(): List<SearchViewLayout.SearchData> {
                return getData2(STREAMS, MIXER)
            }
        })

        val platform = SelectionData.Builder()
            .setFilterSelectionName("Twitch")
            .addSelectionListener(object :
                SelectionData.OnSelectionListener<SearchViewLayout.SearchData> {
                override suspend fun getData(data: List<SearchViewLayout.SearchData>): List<SearchViewLayout.SearchData> {
                    return data.filter { it.platform == TWITCH }
                }
            }).build(SearchViewLayout.SearchData::class.java)

        val platform2 = SelectionData.Builder()
            .setFilterSelectionName("Mixer")
            .addSelectionListener(object :
                SelectionData.OnSelectionListener<SearchViewLayout.SearchData> {
                override suspend fun getData(data: List<SearchViewLayout.SearchData>): List<SearchViewLayout.SearchData> {
                    return data.filter { it.platform == MIXER }
                }
            }).build(SearchViewLayout.SearchData::class.java)


        val listOfSelectionData = listOf(platform, platform2)

        binding.searchLayout.addFilter("Choose platform", listOfSelectionData, true, isMultipleSelectionEnabled = true)

        val category = SelectionData.Builder()
            .setFilterSelectionName("Games")
            .addSelectionListener(object :
                SelectionData.OnSelectionListener<SearchViewLayout.SearchData> {
                override suspend fun getData(data: List<SearchViewLayout.SearchData>): List<SearchViewLayout.SearchData> {
                    val newList = data.filter { it.category == GAMES }
                    return newList
                }
            }).build(SearchViewLayout.SearchData::class.java)

        val category2 = SelectionData.Builder()
            .setFilterSelectionName("Channels")
            .addSelectionListener(object :
                SelectionData.OnSelectionListener<SearchViewLayout.SearchData> {
                override suspend fun getData(data: List<SearchViewLayout.SearchData>): List<SearchViewLayout.SearchData> {
                    return data.filter { it.category == CHANNELS }
                }
            }).build(SearchViewLayout.SearchData::class.java)

        val category3 = SelectionData.Builder()
            .setFilterSelectionName("Streams")
            .addSelectionListener(object :
                SelectionData.OnSelectionListener<SearchViewLayout.SearchData> {
                override suspend fun getData(data: List<SearchViewLayout.SearchData>): List<SearchViewLayout.SearchData> {
                    return data.filter { it.category == STREAMS }
                }
            }).build(SearchViewLayout.SearchData::class.java)

        val listOfSelectionData1 = listOf(category, category2, category3)
        binding.searchLayout.addFilter("Choose category", listOfSelectionData1, true, true)
        binding.searchLayout.invalidateFilters()
    }

    fun getData(category: Int, platform: Int): List<SearchViewLayout.SearchData> {
        return listOf(
            SearchViewLayout.SearchData(
                "Greek",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                category,
                platform
            ),
            SearchViewLayout.SearchData(
                "Andy",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                category,
                platform
            ),
            SearchViewLayout.SearchData(
                "Drd",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                category,
                platform
            ),
            SearchViewLayout.SearchData(
                "Shr",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                category,
                platform
            )
        )
    }

    fun getData2(category: Int, platform: Int): List<SearchViewLayout.SearchData> {
        return listOf(
            SearchViewLayout.SearchData(
                "Greek",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                GAMES,
                platform
            ),
            SearchViewLayout.SearchData(
                "Andy",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                GAMES,
                platform
            ),
            SearchViewLayout.SearchData(
                "Drd",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                CHANNELS,
                platform
            ),
            SearchViewLayout.SearchData(
                "Shr",
                "https://static-cdn.jtvnw.net/jtv_user_pictures/774f1524-f873-4e60-b767-b17653a74ab5-profile_image-70x70.png",
                CHANNELS,
                platform
            )
        )
    }
}
