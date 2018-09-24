package pl.saramak.beer.whereismybeer

import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.routing.data.RouteResponse
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult

interface BearInfoView {
    fun showResult(myPosition:LatLng, result: FuzzySearchResult);
    fun displayRoutes(routeResponse: RouteResponse, result: FuzzySearchResult)
    fun proceedWithError(message: String)
}