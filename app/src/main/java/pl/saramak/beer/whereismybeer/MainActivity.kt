package pl.saramak.beer.whereismybeer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.routing.data.RouteResult
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult


class MainActivity : AppCompatActivity(), BearInfoView {

    lateinit var mapFragment: MapFragment
    lateinit var beerPresenter: BeerPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        beerPresenter = NearestBeerPresenter(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.applyToMap({
            //Set current location enabled
            isMyLocationEnabled = true

        })

    }

    val mapMargin = 100.0
    /**
     * Resume location source and setups services
     */
    override fun onResume() {
        super.onResume()
        beerPresenter.setupServices(this);
        mapFragment.applyToMap({
            //Set current location enabled
            isMyLocationEnabled = true
            setPadding(mapMargin, mapMargin, mapMargin, mapMargin);
            locationSource.addLocationUpdateListener(beerPresenter)
        })
    }

    /**
     * Deactivate location source, because battery consumption and unregister listeners.
     */
    override fun onPause() {
        super.onPause();
        mapFragment.applyToMap({
            locationSource.removeAllLocationUpdateListeners()
        })
        beerPresenter.onPause();

    }


    override fun proceedWithError(text: String) {
        Toast.makeText(this, "Error " + text, Toast.LENGTH_SHORT).show()
    }

    override fun displayRoutes(routeResult: RouteResult, result: FuzzySearchResult) {
        val route = routeResult.routes[0]
        val routeBuilder = RouteBuilder(route.getCoordinates())
                .startIcon(Icon.Factory.fromResources(this, R.drawable.ic_map_route_departure))
                .isActive(true)
        mapFragment.applyToMap {
            clear()
            addRoute(routeBuilder)
            val marker = addMarker(MarkerBuilder(result.position)
                    .icon(Icon.Factory.fromResources(this@MainActivity, R.drawable.beerglassiconsmall))
                    .markerBalloon(SimpleMarkerBalloon(result.poi.name +
                            "\n" + formatAddress(result) + "\n "
                            + route.summary.lengthInMeters + "m.")))
            marker.select()
            displayRoutesOverview()
            zoomOut()
        }

    }

    private fun formatAddress(result: FuzzySearchResult) : String {
        val address = result.address.freeformAddress;
        if (address.contains(",")){
            val splitedAddress = address.split(",");
            return splitedAddress[0] + "\n" + splitedAddress[1];
        }else{
            return address;
        }
    }

    override fun showResult(myPosition: LatLng, result: FuzzySearchResult) {
        Toast.makeText(this@MainActivity, result.poi.name + " \n " + result.address.freeformAddress, Toast.LENGTH_SHORT).show()
    }

    /**
     * helper function in kotlin to support labdas
     */
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

