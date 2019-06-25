package com.ksy.vision.portfolio

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class PermissionUtil {

    // 이미 획득한 권한은 묻지않고 획득하지않은권한만 물어볼수 있게
    // 여러개의 권한을 요청할수 있기 때문에 vararg 가변인수로 처리
    fun requestPermission( activity: Activity, requestCode:Int, vararg permissions:String):Boolean {
        var granted = true
        val permissionNeeded = ArrayList<String>() // 요청할 권한리스트를 담아두기위해서


        permissions.forEach {
            val permissionCheck = ContextCompat.checkSelfPermission(activity, it)
            val hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED
            granted = granted and hasPermission // and == &&
            if (!hasPermission) {
                permissionNeeded.add(it)
            }
        }

        if (granted) return true
        else { // 권한 요청작업
            ActivityCompat.requestPermissions(
                activity, permissionNeeded.toTypedArray(), requestCode
            )
            return false
        }
    }

    fun permissionGranted(
        requestCode: Int , permissionCode:Int, grantResults: IntArray
    ):Boolean{
        return  requestCode == permissionCode && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

}