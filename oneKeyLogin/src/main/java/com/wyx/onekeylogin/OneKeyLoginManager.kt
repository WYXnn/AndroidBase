package com.wyx.onekeylogin

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.unicom.online.account.shield.UniAccountHelper
import org.json.JSONObject

object OneKeyLoginManager {

    private var mIsInit = false

    fun init(context: Context, key: String) {
        if (mIsInit) {
            return
        }
        UniAccountHelper.getInstance().init(context, key, false)
            .setCryptoGM(false)
            .setUseCacheFlag(false)
        mIsInit = true
    }

    fun getToken(time: Int, onSuccess : (result : SimResultBean) -> Unit, onFailed : (msg : String) -> Unit) {
        if (!mIsInit) {
            Log.e("OneKeyLoginManager", "must call init first")
            return
        }
        UniAccountHelper.getInstance().cuGetToken(time) {
            try {
                val jsonObject = JSONObject(it)
                val resultCode = jsonObject.optLong("resultCode")
                if (resultCode == 100.toLong()) {
                    val gson = Gson()
                    val result = gson.fromJson(it, SimResultBean::class.java)
                    onSuccess(result)
                } else {
                    onFailed(it)
                }
            } catch (e : Exception) {
                e.printStackTrace()
                onFailed(e.message ?: "")
            }

        }
    }



}