package pl.saramak.beer.whereismybeer

import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.routing.data.RouteResult
import com.tomtom.online.sdk.search.data.SearchResponse

interface BearInfoView {
    fun showResult(myPostition:LatLng, result: SearchResponse);
    fun displayRoutes(routeResult: RouteResult)
    fun proceedWithError(message: String)

}