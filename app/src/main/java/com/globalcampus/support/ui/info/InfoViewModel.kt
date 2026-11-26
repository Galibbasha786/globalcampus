package com.globalcampus.support.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcampus.support.data.model.Article
import com.globalcampus.support.data.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InfoState(
    val loading: Boolean = true,
    val articles: List<Article> = emptyList(),
    val bookmarks: Set<String> = emptySet(),
    val query: String = "",
    val selectedCategory: String = "All",
    val seeding: Boolean = false,
    val seedMessage: String? = null,
    val error: String? = null
) {
    val filtered: List<Article>
        get() = articles.filter { a ->
            (selectedCategory == "All" || a.category == selectedCategory) &&
                    (query.isBlank() ||
                            a.title.contains(query, true) ||
                            a.body.contains(query, true) ||
                            a.tags.any { it.contains(query, true) })
        }
}

class InfoViewModel : ViewModel() {

    private val repo = ArticleRepository()
    private val _state = MutableStateFlow(InfoState())
    val state: StateFlow<InfoState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val articles = repo.getArticles()
            val bookmarks = repo.getBookmarkedIds()
            _state.value = _state.value.copy(
                loading = false,
                articles = articles.getOrDefault(emptyList()),
                bookmarks = bookmarks.getOrDefault(emptySet()),
                error = articles.exceptionOrNull()?.message
            )
        }
    }

    fun seed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(seeding = true, seedMessage = null)
            val result = repo.seedArticles()
            _state.value = _state.value.copy(
                seeding = false,
                seedMessage = if (result.isSuccess)
                    "Seeded ${result.getOrDefault(0)} sample articles ✅"
                else
                    "Seed failed: ${result.exceptionOrNull()?.message}"
            )
            load()
        }
    }

    fun clearSeedMessage() {
        _state.value = _state.value.copy(seedMessage = null)
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun setCategory(c: String) {
        _state.value = _state.value.copy(selectedCategory = c)
    }

    fun toggleBookmark(articleId: String) {
        val currently = _state.value.bookmarks.contains(articleId)
        viewModelScope.launch {
            repo.toggleBookmark(articleId, !currently)
            _state.value = _state.value.copy(
                bookmarks = if (currently)
                    _state.value.bookmarks - articleId
                else
                    _state.value.bookmarks + articleId
            )
        }
    }
}