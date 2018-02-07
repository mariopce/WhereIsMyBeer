package pl.saramak.beer.whereismybeer

import android.content.Context


interface BeerPresenter {
    companion object {
        val MIN_ACCURENCY_TO_SEARCH = 20.0f
        val NEXT_SEARCH_DISTANCE = 100.0f
    }
    fun setupServices(context: Context)
    fun onPause()
}