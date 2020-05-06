package com.multistream.multistreamsearchview

class SelectionData<T> {

    var dataName: String? = "Empty"

    var selectionListener: OnSelectionListener<T>? = null

    interface OnSelectionListener<T> {
        fun getData() : List<T>
    }
}