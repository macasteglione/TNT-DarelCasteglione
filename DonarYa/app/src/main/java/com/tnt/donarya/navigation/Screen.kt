package com.tnt.donarya.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object MerenderoList : Screen("merendero_list")
    object MerenderoDetail : Screen("merendero_detail/{merenderoId}") {
        fun createRoute(id: String) = "merendero_detail/$id"
    }

    object PublishNeed : Screen("publish_need")
    object MerenderoHome : Screen("merendero_home")
    object Profile : Screen("profile/{role}") {
        fun createRoute(role: String) = "profile/$role"
    }

    object Register : Screen("register/{rolInicial}") {
        fun createRoute(rol: String) = "register/$rol"
    }

    object EditNeed : Screen("edit_need/{needId}") {
        fun createRoute(needId: String) = "edit_need/$needId"
    }

    object Notifications : Screen("notifications")
    object EditProfile : Screen("edit_profile")
    object MapPicker : Screen("map_picker")
}
