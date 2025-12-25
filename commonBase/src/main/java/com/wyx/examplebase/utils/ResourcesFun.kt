package com.wyx.examplebase.utils

import androidx.annotation.StringRes
import com.wyx.examplebase.app.BaseApplication

fun getString(@StringRes stringRes: Int): String {
    return BaseApplication.sContext.getString(stringRes)
}