package com.multistream.multistreamsearchview

class DataSource<T> {

    val sourceDownloads: MutableList<SourceDownloader<T>>? by lazy { mutableListOf<SourceDownloader<T>>() }

    suspend fun getAllData(): List<T> {
        val mutableList: MutableList<T> = mutableListOf()
        sourceDownloads?.forEach {
            if (it.isEnabled)  mutableList.addAll(it.getData())

        }
        return mutableList
    }

    fun addSourceDownloader(sourceDownloader: SourceDownloader<T>) {
        sourceDownloads?.add(sourceDownloader)
    }

    interface SourceDownloader<T> {

        var isEnabled: Boolean

        suspend fun getData(): List<T>
    }
}