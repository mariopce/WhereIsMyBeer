package pl.saramak.beer.whereismybeer

import android.content.Context
import android.location.Location
import com.tomtom.online.sdk.location.LocationUpdateListener


interface BeerPresenter : LocationUpdateListener {

    companion object {
        val MIN_ACCURENCY_TO_SEARCH = 50.0f
        val NEXT_SEARCH_DISTANCE = 100.0f
    }
    fun setupServices(context: Context)
    fun onPause()
}