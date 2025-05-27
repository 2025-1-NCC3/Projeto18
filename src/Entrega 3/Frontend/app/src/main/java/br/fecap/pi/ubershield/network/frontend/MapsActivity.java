package br.fecap.pi.ubershield.network.frontend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

// Importações do Google Maps
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

// Importações para a rota
import com.google.maps.android.PolyUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import android.graphics.Color; // Para a cor da linha

// Classe UberShieldService
import br.fecap.pi.ubershield.R;
import br.fecap.pi.ubershield.network.backend.UberShieldService;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private long lastLocationTime = 0;
    private boolean isOverlayActive = false;

    private UberShieldService uberShieldService;

    // Variáveis para o Google Map
    private GoogleMap mMap;
    private String destinationPlaceName;
    private String destinationPlaceAddress;
    private double destinationPlaceLatitude;
    private double destinationPlaceLongitude;
    private boolean hasDestinationData = false;

    // Variáveis para Rotas
    private LatLng currentLocationLatLng;
    private OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Lógica de RECEBIMENTO DE ENDEREÇO da MainActivity
        Intent intent = getIntent();
        if (intent != null) {
            destinationPlaceName = intent.getStringExtra(MainActivity.EXTRA_PLACE_NAME);
            destinationPlaceAddress = intent.getStringExtra(MainActivity.EXTRA_PLACE_ADDRESS);

            if (intent.hasExtra(MainActivity.EXTRA_PLACE_LATITUDE) && intent.hasExtra(MainActivity.EXTRA_PLACE_LONGITUDE)) {
                destinationPlaceLatitude = intent.getDoubleExtra(MainActivity.EXTRA_PLACE_LATITUDE, 0.0);
                destinationPlaceLongitude = intent.getDoubleExtra(MainActivity.EXTRA_PLACE_LONGITUDE, 0.0);
                hasDestinationData = true; // Define a flag como true se houver dados de destino
                Log.d(TAG, "Dados de destino recebidos: " + destinationPlaceName + " (" + destinationPlaceLatitude + ", " + destinationPlaceLongitude + ")");
            } else {
                Log.d(TAG, "Nenhum dado de Latitude/Longitude de destino recebido.");
            }
        }

        // Inicializa o provedor de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializa o serviço de segurança
        uberShieldService = new UberShieldService(this);

        // Botão de segurança
        ImageButton safetyButton = findViewById(R.id.safetyButton);
        safetyButton.setOnClickListener(v -> {
            checkSmsPermission(); // Verificar permissão SMS
            uberShieldService.startSafetyCheck();
            isOverlayActive = true;
        });

        // Inicializa o SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "SupportMapFragment não encontrado. Verifique o ID no XML.");
        }

        // Inicia atualizações de localização
        startLocationUpdates();

        // Verifica e solicita a permissão de SMS ao iniciar a MapsActivity
        checkSmsPermission();
    }

    // Método para obter a localização atual
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                // Opcional: Adicione um marcador para a localização atual
                                mMap.addMarker(new MarkerOptions().position(currentLocationLatLng).title("Sua Localização Atual"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15));

                                // Se houver dados de destino, chame drawRoute após ter a localização atual
                                if (hasDestinationData) {
                                    LatLng destinationLocation = new LatLng(destinationPlaceLatitude, destinationPlaceLongitude);
                                    drawRoute(currentLocationLatLng, destinationLocation);
                                    // Adiciona marcador de destino aqui também para garantir que ele seja desenhado após a rota
                                    mMap.addMarker(new MarkerOptions()
                                            .position(destinationLocation)
                                            .title(destinationPlaceName)
                                            .snippet(destinationPlaceAddress));
                                }
                            } else {
                                Toast.makeText(MapsActivity.this, "Não foi possível obter a localização atual.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Permissão de localização não concedida.", Toast.LENGTH_SHORT).show();
        }
    }

    // Implementação do GoogleMap Callback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Habilita o botão de localização do usuário no mapa
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } catch (SecurityException e) {
                Log.e(TAG, "Erro ao habilitar MyLocation: " + e.getMessage());
                Toast.makeText(this, "Permissão de localização necessária para MyLocation", Toast.LENGTH_SHORT).show();
            }
        }

        // Outras configurações de UI do mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        getCurrentLocation();

        if (hasDestinationData) {
            LatLng destinationLocation = new LatLng(destinationPlaceLatitude, destinationPlaceLongitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
        } else if (lastLocation != null) {
            LatLng currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            Toast.makeText(this, "Nenhum destino selecionado. Exibindo sua localização atual.", Toast.LENGTH_LONG).show();
        } else {
            LatLng saoPaulo = new LatLng(-23.55723583987216, -46.636961944989615);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(saoPaulo, 10));
            Toast.makeText(this, "Nenhum destino ou localização atual. Mapa centralizado em São Paulo.", Toast.LENGTH_LONG).show();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    if (mMap != null) {
                    }

                    if (lastLocation != null) {
                        float distance = currentLocation.distanceTo(lastLocation);
                        if (distance < 10) {
                            long timeStopped = System.currentTimeMillis() - lastLocationTime;
                            if (timeStopped > 60000 && !isOverlayActive) {
                                Log.d(TAG, "Detectado tempo de parada excedente.");
                                checkSmsPermission();
                                runOnUiThread(() -> {
                                    uberShieldService.startSafetyCheck();
                                    isOverlayActive = true;
                                });
                            }
                        } else {
                            lastLocationTime = System.currentTimeMillis();
                        }
                    } else {
                        lastLocationTime = System.currentTimeMillis();
                    }
                    lastLocation = currentLocation;
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOverlayActive = false;
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Solicitando permissão SEND_SMS.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Permissão SEND_SMS já concedida.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    } catch (SecurityException e) {
                        Log.e(TAG, "Erro ao habilitar MyLocation: " + e.getMessage());
                    }
                }
                startLocationUpdates();
                Toast.makeText(this, "Permissão de localização concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de SMS concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão de SMS negada. Não será possível enviar alertas de emergência via SMS.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- NOVO CÓDIGO PARA TRAÇAR ROTA ---

    private void drawRoute(LatLng origin, LatLng destination) {
        if (origin == null || destination == null) {
            Toast.makeText(this, "Localização atual ou destino não disponível.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use sua chave de API do AndroidManifest.xml ou BuildConfig
        // Certifique-se que a API Directions está habilitada no Google Cloud Console
        String apiKey = getString(R.string.api_key);
        // OU se estiver usando secrets-gradle-plugin:
        // String apiKey = BuildConfig.MAPS_API_KEY;

        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=" + apiKey;

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Erro ao buscar rota: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedPolyline = overviewPolyline.getString("points");

                            List<LatLng> polylinePoints = PolyUtil.decode(encodedPolyline);

                            runOnUiThread(() -> {
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(polylinePoints)
                                        .width(10)
                                        .color(Color.BLUE)
                                        .geodesic(true);
                                mMap.addPolyline(polylineOptions);

                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(origin);
                                builder.include(destination);
                                for (LatLng point : polylinePoints) {
                                    builder.include(point);
                                }
                                LatLngBounds bounds = builder.build();
                                int padding = 100;
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                mMap.animateCamera(cu);
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Nenhuma rota encontrada.", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Erro ao processar dados da rota: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MapsActivity.this, "Falha na resposta da API de rotas: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}