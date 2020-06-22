package com.multistream.multistreamsearchview.util

import com.multistream.multistreamsearchview.recent_search.HistoryListAdapter


object TimeResolver {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS
    fun getTimeAgo(timeGiven: Long): HistoryListAdapter.Time? {
        var time = timeGiven
        if (time < 1000000000000L) { // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }
        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> {
                HistoryListAdapter.Time.JUST_NOW
            }
            diff < 2 * MINUTE_MILLIS -> {
                HistoryListAdapter.Time.MINUTE_AGO
            }
            diff < 50 * MINUTE_MILLIS -> {
             HistoryListAdapter.Time.MINUTES_AGO
            }
            diff < 90 * MINUTE_MILLIS -> {
               HistoryListAdapter.Time.HOUR_AGO
            }
            diff < 24 * HOUR_MILLIS -> {
                HistoryListAdapter.Time.HOURS_AGO
            }
            diff < 48 * HOUR_MILLIS -> {
               HistoryListAdapter.Time.YESTERDAY
            }
            diff <   WEEK_MILLIS -> {
                HistoryListAdapter.Time.WEEK_AGO
            }
            diff <   2 * WEEK_MILLIS -> {
               HistoryListAdapter.Time.WEEKS_AGO
            }
            else -> {
                HistoryListAdapter.Time.LONG_TIME_AGO
            }
        }
    }
}