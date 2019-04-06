package br.usjt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListaActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_GPS = 1001;
    private static long LOCATION_INTERVAL = 2 * 60;
    private static int LOCATION_DISTANCE = 0;
    private static int LOCATION_MAX_SIZE = 50;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView locationListView;
    private double latitudeAtual;
    private double longitudeAtual;

    ArrayList<LatLongLocalizacao> localizacoes = new ArrayList<>();

    private ArrayAdapter<LatLongLocalizacao> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);


        iniciarLocationManager();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                latitudeAtual = lat;
                longitudeAtual = lon;

                LatLongLocalizacao latLongLocalizacao = new LatLongLocalizacao();
                latLongLocalizacao.setLatitude(lat);
                latLongLocalizacao.setLongitude(lon);

                localizacoes.add(latLongLocalizacao);
                adapter.notifyDataSetChanged();

            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        locationListView = findViewById(R.id.locationListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, localizacoes);
        locationListView.setAdapter(adapter);

        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri gmmIntentUri = Uri.parse(String.format("geo:%f,%f",
                        latitudeAtual, longitudeAtual));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //a permissão já foi dada?
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            //somente ativa
            //a localização é obtida via hardware, intervalo de 0 segundos e 0 metros entre atualizações
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
        } else {
            //permissão ainda não foi nada, solicita ao usuário
            //quando o usuário responder, o método onRequestPermissionsResult vai ser chamado
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permissão concedida, ativamos o GPS
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                }
            } else {
//usuário negou, não ativamos
                Toast.makeText(this, getString(R.string.no_gps_no_app),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void iniciarLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

}
