package pl.saramak.beer.whereismybeer

import android.content.Context
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.NonNull
import com.google.android.gms.location.LocationListener
import com.tomtom.online.sdk.location.LocationSource
import com.tomtom.online.sdk.map.MapFragment
import com.tomtom.online.sdk.map.TomtomMap
import com.tomtom.online.sdk.location.LocationRequestsFactory
import com.tomtom.online.sdk.location.FusedLocationSource
import timber.log.Timber
import android.R.attr.radius
import android.content.ServiceConnection
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.routing.data.*
import com.tomtom.online.sdk.search.data.SearchQuery
import io.reactivex.internal.disposables.DisposableHelper.isDisposed
import com.tomtom.online.sdk.search.data.SearchResponse
import com.tomtom.online.sdk.search.extensions.SearchService
import com.tomtom.online.sdk.search.extensions.SearchServiceConnectionCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import com.tomtom.online.sdk.search.extensions.SearchServiceManager






class MainActivity : AppCompatActivity(), LocationListener, SearchServiceConnectionCallback {
    private lateinit var searchService: SearchService

    override fun onBindSearchService(searchService: SearchService) {
        this.searchService = searchService
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            Timber.d("accuracy " +  it.accuracy)
            if (it.accuracy <= 10.0){
                Timber.i("lat %d, lng %d", it.latitude, it.longitude);
                performSearch(it, createQueryWithPosition(LatLng(it.latitude, it.longitude)));
            }
        }

    }

    private lateinit var searchServiceConnection: ServiceConnection

    protected fun startAndBindToSearchService() {
        searchServiceConnection = SearchServiceManager.createAndBind(this,
                this)

    }

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
                        val location = r.searchResults[0].location
                        Timber.i("location result " + location)

                        showRoute(getRouteQuery(TravelMode.PEDESTRIAN));

                    }
                })
    }

     fun getRouteQuery(orgin: LatLng, dest: LatLng) : RouteQuery {

       val queryBuilder = RouteQueryBuilder(orgin, dest)
                .withMaxAlternatives(0)
                .withReport(Report.EFFECTIVE_SETTINGS)
                .withInstructionsType(InstructionsType.TEXT)
                .withTravelMode(TravelMode.PEDESTRIAN);

        return queryBuilder.;
    }

    val STANDARD_RADIUS = 10 * 1000 //10 km
    protected fun createQueryWithPosition(position: LatLng?): SearchQuery {
        return SearchQuery.builder()
                .term("Bar")
                .location(position).radius(STANDARD_RADIUS)
                .build()

    }

    public lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.applyToMap({
            isMyLocationEnabled = true

        })

    }

    private lateinit var locationSource: FusedLocationSource

    override fun onResume() {
        super.onResume()
        locationSource = getLocationSource(this)
        locationSource.activate()
    }

    override fun onPause() {
        super.onPause()
        locationSource.deactivate()
    }

    @NonNull
    fun getLocationSource(context: Context): FusedLocationSource {
        return FusedLocationSource(context, this, LocationRequestsFactory.create().createSearchLocationRequest())
    }

    fun MapFragment.applyToMap(function: TomtomMap.() -> Unit) {
        mapFragment.getAsyncMap(function)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mapFragment.applyToMap {
            onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}

