package com.wlvpn.consumervpn.data.gateway

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.wlvpn.consumervpn.domain.gateway.SplitTunnelGateway
import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings
import java.util.Locale

class UserAppSplitTunnelGateway(
    private val packageManager: PackageManager
) : SplitTunnelGateway {

    override suspend fun getInstalledApps(): List<SplitTunnelSettings.App> {
        val allAppsList = mutableListOf<SplitTunnelSettings.App>()

        val installedApplications = packageManager.getInstalledApplications(0)

        for (appInfo in installedApplications) {

            // Check internet permission
            if (!hasInternetPermission(appInfo.packageName)) continue

            val appName = appInfo.loadLabel(packageManager).toString()

            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
                    || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)

            allAppsList.add(
                SplitTunnelSettings.App(
                    name = appName,
                    packageName = appInfo.packageName,
                    isSystemApp = isSystemApp
                )
            )
        }

        return allAppsList.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    private fun hasInternetPermission(packageName: String): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }
            packageInfo.requestedPermissions?.contains(android.Manifest.permission.INTERNET) == true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}