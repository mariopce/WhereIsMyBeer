package pl.saramak.beer.whereismybeer

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.routing.data.RouteResponse
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity


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

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode){
            mapFragment.applyToMap {
                uiSettings.currentLocationView.hide()
                routeSettings.displayRoutesOverview()
            }
        }else{
            mapFragment.applyToMap {
                uiSettings.currentLocationView.show()
            }
        }
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


    override fun proceedWithError(message: String) {
        Toast.makeText(this, "Error $message", Toast.LENGTH_SHORT).show()
    }

    override fun displayRoutes(routeResponse: RouteResponse, result: FuzzySearchResult) {
        val route = routeResponse.routes[0]
        val routeBuilder = RouteBuilder(route.getCoordinates())
                .startIcon(Icon.Factory.fromResources(this, R.drawable.ic_map_route_departure)).style(RouteStyle.DEFAULT_ROUTE_STYLE)
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

    private fun formatAddress(result: FuzzySearchResult): String {
        val address = result.address.freeformAddress;
        if (address.contains(",")) {
            val splitedAddress = address.split(",");
            return splitedAddress[0] + "\n" + splitedAddress[1];
        } else {
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

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager
                        .hasSystemFeature(
                                PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            enterPIPMode()
        } else {
            super.onBackPressed()
        }
    }

    private fun enterPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager
                        .hasSystemFeature(
                                PackageManager.FEATURE_PICTURE_IN_PICTURE)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()

                val aspectRatio = Rational(mapFragment.view!!.width, mapFragment.view!!.height)
                params.setAspectRatio(aspectRatio)
                this.enterPictureInPictureMode(params.build())
            } else {
                this.enterPictureInPictureMode()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPIPMode()
    }
}

