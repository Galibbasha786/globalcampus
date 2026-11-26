package com.globalcampus.support.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val COMMUNITY = "community"
    const val HELP = "help"
    const val CHAT = "chat"
    const val CHAT_COMMUNITY = "chat_community"
    const val PROFILE = "profile"
    const val INFO_HUB = "info_hub"
    const val ARTICLE_DETAIL = "article/{articleId}"
    const val POST_DETAIL = "post/{postId}"
    const val VISA_TOOLS = "visa_tools"
    const val PHRASEBOOK = "phrasebook"

    // Innovative Features
    const val PLANNER = "arrival_planner"
    const val BUDDY = "buddy_matching"
    const val BUDGET = "budget_planner"
    const val EVENTS = "campus_events"
    const val NAVIGATOR = "campus_navigator"
    const val DOC_EXPLAINER = "doc_explainer"
    const val LOST_FOUND = "lost_found"
    const val ACADEMIC = "academic_toolkit"
    const val VIDEO_CALL = "video_call"

    fun articleDetail(id: String) = "article/$id"
    fun postDetail(id: String) = "post/$id"
}
