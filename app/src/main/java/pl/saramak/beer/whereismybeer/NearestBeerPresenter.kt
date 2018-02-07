package pl.saramak.beer.whereismybeer

import android.annotation.SuppressLint
import android.content.Context
import android.content.ServiceConnection
import android.location.Location
import android.support.annotation.NonNull
import android.widget.Toast
import com.google.android.gms.location.LocationListener
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.location.FusedLocationSource
import com.tomtom.online.sdk.location.LocationRequestsFactory
import com.tomtom.online.sdk.routing.OnlineRoutingApi
import com.tomtom.online.sdk.routing.RoutingApi
import com.tomtom.online.sdk.routing.data.*
import com.tomtom.online.sdk.search.data.SearchQuery
import com.tomtom.online.sdk.search.data.SearchResponse
import com.tomtom.online.sdk.search.extensions.SearchService
import com.tomtom.online.sdk.search.extensions.SearchServiceConnectionCallback
import com.tomtom.online.sdk.search.extensions.SearchServiceManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Executors

class NearestBeerPresenter(val view: BearInfoView) : BeerPresenter, SearchServiceConnectionCallback, LocationListener {


    private var lastLocation : Location? = null
    lateinit var searchService: SearchService
    lateinit var routePlannerAPI: RoutingApi
    lateinit var locationSource: FusedLocationSource
    val networkScheduler = Schedulers.from(Executors.newFixedThreadPool(4))

    private val compositeDisposable = CompositeDisposable()

    override fun setupServices(context: Context) {
        startAndBindToSearchService(context)
        routePlannerAPI = OnlineRoutingApi.create(context)
        locationSource = getLocationSource(context)
        locationSource.activate()
    }

    @NonNull
    fun getLocationSource(context: Context): FusedLocationSource {
        return FusedLocationSource(context, this, LocationRequestsFactory.create().createSearchLocationRequest())
    }

    private lateinit var searchServiceConnection: ServiceConnection


    protected fun startAndBindToSearchService(context: Context) {
        searchServiceConnection = SearchServiceManager.createAndBind(context,
                this)

    }

    override fun onBindSearchService(searchService: SearchService) {
        this.searchService = searchService
    }

    override fun onLocationChanged(location: Location?) {

        location?.let {
            Timber.d("accuracy " +  it.accuracy)
            if (it.accuracy <= BeerPresenter.MIN_ACCURENCY_TO_SEARCH && (lastLocation == null || lastLocation!!.distanceTo(location) > BeerPresenter.NEXT_SEARCH_DISTANCE)){
                Timber.i("lat "+ it.latitude +" lng "+ it.longitude);
                lastLocation = it
                performSearch(LatLng(it.latitude, it.longitude), createQueryWithPosition(LatLng(it.latitude, it.longitude)));
            }
        }

    }

    override fun onPause() {
        locationSource.deactivate()
    }
    val STANDARD_RADIUS = 10 * 1000 //10 km
    protected fun createQueryWithPosition(position: LatLng?): SearchQuery {
        return SearchQuery.builder()
                .term("Bar")
                .location(position).radius(STANDARD_RADIUS)
                .build()

    }
    @SuppressLint("CheckResult")
    private fun performSearch(mYPos : LatLng, query: SearchQuery) {
        searchService.search(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(object : Consumer<Throwable> {
                    override fun accept(t: Throwable) {
                        Timber.e(t)
                    }


                })
                .subscribe(object : Consumer<SearchResponse> {
                    override fun accept(r: SearchResponse) {
                        Timber.i("Result " + r.searchResults[0])
                        val result = r.searchResults[0]
                        val location = r.searchResults[0].location
                        Timber.i("location result " + location)
                        findRoute(getRouteQuery(mYPos, location))
                    }
                })
    }

    fun getRouteQuery(orgin: LatLng, dest: LatLng) : RouteQuery {

        val queryBuilder = RouteQueryBuilder(orgin, dest)
                .withMaxAlternatives(0)
                .withReport(Report.EFFECTIVE_SETTINGS)
                .withInstructionsType(InstructionsType.TEXT)
                .withTravelMode(TravelMode.PEDESTRIAN);

        return queryBuilder;
    }

    private fun findRoute(routeQuery: RouteQuery) {
        val subscribe = routePlannerAPI.planRoute(routeQuery).subscribeOn(networkScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RouteResult> { routeResult -> view.displayRoutes(routeResult) }, Consumer<Throwable> { view.proceedWithError(it.message!!) })
        compositeDisposable.add(subscribe)
    }

}