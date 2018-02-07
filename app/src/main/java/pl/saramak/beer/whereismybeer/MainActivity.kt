package pl.saramak.beer.whereismybeer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.tomtom.online.sdk.map.MapFragment
import com.tomtom.online.sdk.map.TomtomMap
import com.tomtom.online.sdk.location.FusedLocationSource
import android.widget.Toast
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.Icon
import com.tomtom.online.sdk.map.RouteBuilder
import com.tomtom.online.sdk.routing.data.*
import com.tomtom.online.sdk.search.data.SearchResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), BearInfoView {





    override fun proceedWithError(text: String) {
        Toast.makeText(this, "Error " + text, Toast.LENGTH_SHORT).show()
    }

    override fun displayRoutes(routeResult: RouteResult) {
        val route = routeResult.routes[0]
        val routeBuilder = RouteBuilder(route.getCoordinates())
                .startIcon(Icon.Factory.fromResources(this, R.drawable.ic_map_route_departure))
                .endIcon(Icon.Factory.fromResources(this, R.drawable.beerglassicon))
                .isActive(true)
        mapFragment.applyToMap {
            addRoute(routeBuilder)
            displayRoutesOverview()
        }

    }





    lateinit var mapFragment: MapFragment
    lateinit var beerPresenter : BeerPresenter
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


    /**
     * Resume location source and setups services
     */
    override fun onResume() {
        super.onResume()
        beerPresenter.setupServices(this);

    }

    /**
     * Deactivate location source, because battery consumption and unregister listeners.
     */
    override fun onPause() {
        super.onPause()
        beerPresenter.onPause();

    }
    override fun showResult(myPosition: LatLng,r : SearchResponse) {
        val result = r.searchResults[0]
        Toast.makeText(this@MainActivity, result.name + " \n " + result.addressLine1, Toast.LENGTH_SHORT).show()
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

