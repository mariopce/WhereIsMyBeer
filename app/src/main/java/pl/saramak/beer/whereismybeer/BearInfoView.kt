package pl.saramak.beer.whereismybeer

import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.routing.data.RouteResult
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult

interface BearInfoView {
    fun showResult(myPostition:LatLng, result: FuzzySearchResult);
    fun displayRoutes(routeResult: RouteResult, result: FuzzySearchResult)
    fun proceedWithError(message: String)
}