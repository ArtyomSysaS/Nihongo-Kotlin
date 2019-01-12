package org.artym_sysa.nihongo

import com.google.gson.annotations.SerializedName


data class TestRecord(
        @SerializedName("word_id")
        var wordId: Long,
        @SerializedName("mode")
        var mode: TestActivity.MODE,
        @SerializedName("type")
        var type: TestActivity.TYPE,
        @SerializedName("result")
        var result: Boolean = true,
        @SerializedName("incorrectAnswer")
        var incorrectAnswer: String = ""
)