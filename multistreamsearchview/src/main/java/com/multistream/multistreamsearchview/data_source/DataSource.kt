package com.multistream.multistreamsearchview.data_source

import android.view.View
import com.multistream.multistreamsearchview.R
import com.multistream.multistreamsearchview.search_view.SearchViewLayout
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DataSource<T> {

    var itemsData: List<T>? = null

    val sourceDownloads: MutableList<SourceDownloader<*>> by lazy { mutableListOf<SourceDownloader<*>>() }

    inline fun<reified T> addDefault(clazz: Class<out T>) {
        val allSourceDownloader = Builder()
            .setIconDrawable(R.drawable.recent_icon)
            .setName("All")
            .isAllSource(true)
            .build(clazz)

        sourceDownloads.add(allSourceDownloader)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getAllData() {
        coroutineScope {
            val mutableList: MutableList<T> = mutableListOf()
            val jobs  =  if (sourceDownloads.isNotEmpty() && sourceDownloads.first().isEnabled) {
                (1 until sourceDownloads.count()).map {
                    val sourceDownload = sourceDownloads[it]
                    async { sourceDownload.getData() }
                }
            } else {
                sourceDownloads.map {
                    async { if (it.isEnabled) it.getData() else null }
                }
            }
            jobs.awaitAll().forEach {
                if(!it.isNullOrEmpty()) mutableList.addAll(it as List<T>)
            }
            itemsData = mutableList
        }
    }

    fun addSourceDownloader(sourceDownloader: SourceDownloader<T>) {
        sourceDownloads.add(sourceDownloader)
    }

    interface SourceDownloader<T> {

        var isEnabled: Boolean

        var iconDrawable: Int

        var name: String

        var id: Int

        suspend fun getData(): List<T>
    }

    class Builder {

        private var isEnabled: Boolean = false

        private var iconDrawable: Int = 0

        private var name: String? = null

        private var isAll = false

        fun setName(name: String): Builder {
            this.name = name
            return this
        }

        fun setIconDrawable(id: Int): Builder {
            this.iconDrawable = id
            return this
        }

        fun enableDownloader(isEnabled: Boolean): Builder {
            this.isEnabled = isEnabled
            return this
        }

        fun isAllSource(isAllEnabled: Boolean) : Builder {
            this.isAll = isAllEnabled
            return this
        }

        fun <T> build(clazz: Class<T>, getData: (() -> List<T>)? = null): SourceDownloader<T> {
            return object : SourceDownloader<T> {

                override var id: Int = if (isAll) R.id.all_data_source else View.generateViewId()

                override var isEnabled: Boolean = this@Builder.isEnabled

                override var iconDrawable: Int = this@Builder.iconDrawable

                override var name: String = this@Builder.name ?: "no name"

                override suspend fun getData(): List<T> {
                    return if (getData == null) emptyList() else getData()
                }
            }
        }
    }
}