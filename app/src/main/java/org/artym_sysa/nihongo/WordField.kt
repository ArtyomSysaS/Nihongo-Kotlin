package org.artym_sysa.nihongo

import com.google.gson.annotations.SerializedName

data class WordField(
        @SerializedName("key")
        var key: String,
        @SerializedName("value")
        var value: String
)