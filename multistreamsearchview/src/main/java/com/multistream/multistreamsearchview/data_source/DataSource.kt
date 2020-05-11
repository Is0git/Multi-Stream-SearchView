package com.multistream.multistreamsearchview.data_source

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DataSource<T> {

    var itemsData: List<T>? = null

    val sourceDownloads: MutableList<SourceDownloader<T>> by lazy { mutableListOf<SourceDownloader<T>>() }

    suspend fun getAllData() {
        coroutineScope {
            val mutableList: MutableList<T> = mutableListOf()
            val jobs  =  sourceDownloads.map {
             async { if (it.isEnabled) it.getData() else null }
            }
            jobs.awaitAll().forEach {
                if(!it.isNullOrEmpty()) mutableList.addAll(it)
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

        suspend fun getData(): List<T>
    }

    class Builder {

        private var isEnabled: Boolean = true

        private var iconDrawable: Int = 0

        private var name: String? = null

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

        fun <T> build(clazz: Class<T>, getData: () -> List<T>): SourceDownloader<T> {
            return object : SourceDownloader<T> {
                override var isEnabled: Boolean = this@Builder.isEnabled

                override var iconDrawable: Int = this@Builder.iconDrawable

                override var name: String = this@Builder.name ?: "no name"

                override suspend fun getData(): List<T> {
                    return getData()
                }
            }
        }
    }
}