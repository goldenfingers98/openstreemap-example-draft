package tn.ksoftwares.openstreemap

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IGeoPoint
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import tn.ksoftwares.openstreemap.ui.theme.OpenstreemaptutoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("OSM", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE
        setContent {
            OpenstreemaptutoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavigationScreen()
                }
            }
        }
    }
}

@Composable
fun NavigationScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.LightGray),
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            LocationSearchBar()
            NavigationBottomSheetScaffold {
                OSMMapView()
            }
        }
    }
}

@Composable
fun OSMMapView() {
    var mapCenter: IGeoPoint by rememberSaveable { mutableStateOf(GeoPoint(36.0, 10.25)) }
    var zoom by rememberSaveable { mutableStateOf(9.0) }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                setUseDataConnection(true)
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                val roadManager = OSRMRoadManager(context, Configuration.getInstance().userAgentValue)
                CoroutineScope(Dispatchers.IO).launch {
                    val roads = roadManager.getRoads(
                        arrayListOf(
                            GeoPoint(36.84924, 10.19023),
                            GeoPoint(36.8586, 10.2662),
                        )
                    )
                    withContext(Dispatchers.Main) {
                        roads.forEach { road ->
                            val roadOverlay = RoadManager.buildRoadOverlay(road)
                            overlays.add(roadOverlay)
                        }
                    }
                }


                val gpsMyLocationProvider = GpsMyLocationProvider(context)
                val locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, this)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
            }
        },
        update = { view ->
            view.controller.setCenter(mapCenter)
            view.controller.setZoom(zoom)
        }
    )
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LocationSearchBar() {
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val defaultSearchbarHorizontalPadding = 10.dp
    var searchbarHorizontalPadding by remember { mutableStateOf(defaultSearchbarHorizontalPadding) }
    SearchBar(
        modifier = Modifier.fillMaxWidth()
            .padding(PaddingValues(searchbarHorizontalPadding, 0.dp)),
        query = text,
        onQueryChange = {
            text = it
        },
        onSearch = {
            active = false
        },
        active = active,
        onActiveChange = { isActive ->
            active = isActive
            searchbarHorizontalPadding = if (isActive) 0.dp else defaultSearchbarHorizontalPadding
        },
        placeholder = {
            Text(text = "Where to Go?")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
        },
        trailingIcon = {
            if (active) {
                Icon(
                    modifier = Modifier.clickable {
                        if (text.isNotEmpty()) {
                            text = ""
                        } else {
                            active = false
                            searchbarHorizontalPadding = defaultSearchbarHorizontalPadding
                        }
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon"
                )
            }
        }
    ) {

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NavigationBottomSheetScaffold(content: @Composable (PaddingValues) -> Unit) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetSwipeEnabled = true,
        sheetContent = { },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DateTimePicker() {
    var dateTimeText by rememberSaveable { mutableStateOf("") }
    val timePickerState = rememberTimePickerState(0, 0, true)
    TextField(
        modifier = Modifier.fillMaxWidth()
            .clickable {
            },
        value = dateTimeText,
        readOnly = true,
        onValueChange = {

        }
    )
    TimePicker(
        state = timePickerState,
        modifier = Modifier.
    )
}