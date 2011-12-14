/*
* Copyright (C) 2009 Marc de Verdelhan (http://www.verdelhan.eu/)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.anddeche;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AndDeche extends Activity {

    /** Tag des logs de debug */
    private static String LOGTAG = "LOG_DECHE";
    
    /** Location manager */
    private LocationManager lm;
    
    /** Bouton 5 km */
    private Button btn5km;
    /** Bouton 10 km */
    private Button btn10km;
    /** Bouton 20 km */
    private Button btn20km;
    /** Bouton 30 km */
    private Button btn30km;
    /** Bouton 45 km */
    private Button btn45km;
    
    /** Bouton des info */
    private Button btnInfo;
    
    
    /**
     * Classe d'implementation d'un Location Listener
     * @author marc
     *
     */
    private class MyLocationListener implements LocationListener 
    {
        @Override
        public void onLocationChanged(Location loc) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }
    }    
    
    
    /** Listener sur les boutons */
    private OnClickListener btnListener = new OnClickListener()
    {
        /**
         * @param v "vue" sur laquelle on a cliqué
         */
        public void onClick(View v)
        {
            // Récupération de la position depuis le location provider GPS
            Location loc = AndDeche.this.lm.getLastKnownLocation("gps");
            if(loc == null) {
                // Pas de GPS ? Récupération de la position depuis le location provider réseau
                loc = AndDeche.this.lm.getLastKnownLocation("network");
            }
            if(loc != null) {
                //TODO RAJOUTER UN TEST DE NULLITE SUR LE loc
                Log.d(LOGTAG, "Latitude : "+loc.getLatitude());
                Log.d(LOGTAG, "Longitude : "+loc.getLongitude());
                //TODO A virer
                // Baden
                //loc.setLatitude(47.6192032);
                //loc.setLongitude(-2.8841403);
                // Paris
                //loc.setLatitude(48.847796);
                //loc.setLongitude(2.344379);
                // Lyonnais
                //loc.setLatitude(45.807502);
                //loc.setLongitude(4.806687);
                
    
                
                // Lancement de l'activité GoogleMap, passage des paramètres latitude, longitude, radius
                Intent intent = new Intent(AndDeche.this, GoogleMap.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("LATITUDE", loc.getLatitude());
                bundle.putDouble("LONGITUDE", loc.getLongitude());
                bundle.putInt("RAYON", getRayon(v));
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                // Impossible de récupérer la position
                Toast.makeText(AndDeche.this, "Impossible de récupérer la position", Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    /**
     * 
     * @param bouton bouton à partir duquel on recupère le rayon
     * @return le rayon
     */
    private int getRayon(View bouton) {
        // Identification du bouton sur lequel on a cliqué
        // Calcul du radius en fonction de ce bouton
        if(btn5km.equals(bouton)) {
            return 5;
        } else if(btn10km.equals(bouton)) {
            return 10;
        } else if(btn20km.equals(bouton)) {
            return 20;
        } else if(btn30km.equals(bouton)) {
            return 30;
        } else if(btn45km.equals(bouton)) {
            return 45;
        } else {
            Log.d(LOGTAG, "Bouton inattendu");
            return 10; // Rayon par défaut : 10 km
        }
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        
        // Création des boutons de rayon
        btn5km = (Button) findViewById(R.id.btn5km);
        btn5km.setOnClickListener(btnListener);
        btn10km = (Button) findViewById(R.id.btn10km);
        btn10km.setOnClickListener(btnListener);
        btn20km = (Button) findViewById(R.id.btn20km);
        btn20km.setOnClickListener(btnListener);
        btn30km = (Button) findViewById(R.id.btn30km);
        btn30km.setOnClickListener(btnListener);
        btn45km = (Button) findViewById(R.id.btn45km);
        btn45km.setOnClickListener(btnListener);
        
        // Création du bouton d'info
        btnInfo = (Button) findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Builder alertInfo = new AlertDialog.Builder(AndDeche.this);
                alertInfo.setTitle("AndDeche!");
                alertInfo.setMessage("Développé par Marc de Verdelhan "
                                + "dans le cadre du SFRJTD 2009.\n\n"
                                + "http://www.verdelhan.eu/");
                alertInfo.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                            //Put your code in here for a neutral response
                    }
                });
                alertInfo.show();
            }
        });

        // Récupération du LocationManager
        this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.lm.requestLocationUpdates("gps", 0, 0, new MyLocationListener());
    }
}
