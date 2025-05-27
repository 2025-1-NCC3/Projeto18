package br.fecap.pi.ubershield.network.frontend;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.widget.ImageView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;

import br.fecap.pi.ubershield.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private LinearLayout configButton, activityButton, accountButton, carLayout, boxLayout, routeLayout;

    // Constantes para passar dados para a MapsActivity
    public static final String EXTRA_PLACE_NAME = "br.fecap.pi.ubershield.network.frontend.EXTRA_PLACE_NAME";
    public static final String EXTRA_PLACE_ADDRESS = "br.fecap.pi.ubershield.network.frontend.EXTRA_PLACE_ADDRESS";
    public static final String EXTRA_PLACE_LATITUDE = "br.fecap.pi.ubershield.network.frontend.EXTRA_PLACE_LATITUDE";
    public static final String EXTRA_PLACE_LONGITUDE = "br.fecap.pi.ubershield.network.frontend.EXTRA_PLACE_LONGITUDE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa o Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }

        // Referencia os Botões e Layouts
        accountButton = findViewById(R.id.accountButton);
        activityButton = findViewById(R.id.activityButton);
        configButton = findViewById(R.id.configButton);
        carLayout = findViewById(R.id.carLayout);
        boxLayout = findViewById(R.id.boxLayout);
        routeLayout = findViewById(R.id.routeLayout);

        // Trocando Telas
        configButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppConfigActivity.class);
            startActivity(intent);
        });

        activityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        accountButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });

        // Configuração do AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Verifique se o fragmento foi encontrado antes de tentar usá-lo
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
            ));

            // Acessa o EditText interno do fragmento
            final EditText searchEditText = autocompleteFragment.getView().findViewById(com.google.android.libraries.places.R.id.places_autocomplete_search_input);

            if (searchEditText != null) {
                searchEditText.setBackgroundResource(R.drawable.background_grey);
                searchEditText.setTextAppearance(this, R.style.defaultText);
                searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                searchEditText.setTextSize(24f);
                searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.black));

                ImageView searchIconImageView = autocompleteFragment.getView().findViewById(com.google.android.libraries.places.R.id.places_autocomplete_search_button);
                if (searchIconImageView != null) {
                    searchIconImageView.setImageDrawable(null);
                    searchIconImageView.setVisibility(View.GONE);
                }

                ImageView clearIconImageView = autocompleteFragment.getView().findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button);
                if (clearIconImageView != null) {
                    clearIconImageView.setImageDrawable(null);
                    clearIconImageView.setVisibility(View.GONE);
                }

                Drawable customSearchIcon = ContextCompat.getDrawable(this, R.drawable.icon_search);

                if (customSearchIcon != null) {
                    int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
                    customSearchIcon.setBounds(0, 0, iconSize, iconSize);

                    searchEditText.setCompoundDrawables(customSearchIcon, null, null, null);
                    searchEditText.setCompoundDrawablePadding(48);
                }

                int leftPadding = (customSearchIcon != null ? searchEditText.getCompoundPaddingLeft() : 0) + (int) (12 * getResources().getDisplayMetrics().density); // Adicionei 12dp de padding extra

                searchEditText.setPadding(searchEditText.getCompoundPaddingLeft() + -88, 24, 12,24);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    autocompleteFragment.getView().setElevation(4f);
                }
            }
            autocompleteFragment.setCountries("BR");
            autocompleteFragment.setHint("Para onde vamos?");


            // Adicione um ouvinte para lidar com a seleção do lugar
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.i(TAG, "Lugar Selecionado: " + place.getName());
                    Log.i(TAG, "Endereço Completo: " + place.getAddress());
                    if (place.getLatLng() != null) {
                        Log.i(TAG, "Latitude: " + place.getLatLng().latitude + ", Longitude: " + place.getLatLng().longitude);
                    }

                    Toast.makeText(MainActivity.this, "Endereço selecionado: " + place.getAddress(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);

                    // Passa as informações do lugar para a MapsActivity
                    intent.putExtra(EXTRA_PLACE_NAME, place.getName());
                    intent.putExtra(EXTRA_PLACE_ADDRESS, place.getAddress());
                    if (place.getLatLng() != null) {
                        intent.putExtra(EXTRA_PLACE_LATITUDE, place.getLatLng().latitude);
                        intent.putExtra(EXTRA_PLACE_LONGITUDE, place.getLatLng().longitude);
                    }
                    startActivity(intent);
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.e(TAG, "Erro na seleção do lugar: " + status.getStatusMessage());
                    Toast.makeText(MainActivity.this, "Erro: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else { // Este 'else' agora corresponde corretamente ao 'if (autocompleteFragment != null)' principal
            Log.e(TAG, "AutocompleteSupportFragment não encontrado. Verifique o ID no XML.");
            // Você pode querer desabilitar a barra de pesquisa ou mostrar uma mensagem alternativa se o fragmento não for encontrado
        }


        // Todos os outros botões levam para o Mapa
        View.OnClickListener commonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ao clicar nesses, vai para o mapa sem um endereço pré-selecionado.
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        };

        routeLayout.setOnClickListener(commonClickListener);
        boxLayout.setOnClickListener(commonClickListener);
        carLayout.setOnClickListener(commonClickListener);
    }
}