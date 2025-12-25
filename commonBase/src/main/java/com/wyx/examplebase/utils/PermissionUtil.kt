package com.wyx.examplebase.utils

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback
import java.security.Permissions

class PermissionUtil {

    fun requestPermission(context: FragmentActivity, vararg permissions: String, callback: RequestCallback?) {
        PermissionX.init(context)
            .permissions(permissions = permissions)
            .request(callback)
    }

}