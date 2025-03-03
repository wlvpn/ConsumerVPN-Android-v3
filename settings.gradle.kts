pluginManagement {
    repositories {
        val packagecloud_vpn_token: String by settings

        google()
        mavenCentral()
        gradlePluginPortal()
        maven(
            url = "https://packagecloud.io/priv/"
                    + packagecloud_vpn_token
                    + "/cloak/android-vpn-sdk/maven2"
        )
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val packagecloud_vpn_token:String by settings
        mavenLocal()
        google()
        mavenCentral()
        maven(
            url = "https://packagecloud.io/priv/"
                    + packagecloud_vpn_token
                    + "/cloak/android-vpn-sdk/maven2"
        )
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")

        // Detekt gitlab addon
        maven(url = "https://gitlab.com/api/v4/projects/25796063/packages/maven")
    }
}

rootProject.name = "consumervpn3"
include(":app")
