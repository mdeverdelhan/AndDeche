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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.anddeche.dtdecoemballages.ListeMarkers;
import com.android.anddeche.dtdecoemballages.Marker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GoogleMap extends MapActivity {
    
    /** Message de mise à jour des horaires */
    private static final int MAJ_DONNEES_DECHETERIE = 0x101; 
    
    /** Vue de la carte */
    private MapView mapView;
    /** Controleur de la carte */
    private MapController mc;
    
    /** Unité kilométrique pour le rayon */
    private static double UNIT_RADIUS = 0.62137119223733395;
    
    /** Valeur initiale du zoom */
    private int zoomInitial;
    /** Centre initial de la carte */
    private GeoPoint centreInitial;
    
    /** Liste des sur-couches (overlays) pour la carte */
    private List<Overlay> mapOverlays;
    /** Sur-couche du point initial */
    private DecheMapOverlay centreOverlay;
    /** Sur-couche des déchèteries */
    private DecheMapOverlay decheMapOverlay;
    
    /** Bouton [+] */
    private Button btnMore;
    /** Bouton [S'y rendre] */
    private Button btnSYRendre;
    /** Bouton du lien SINOE (site de l'ADEME) */
    private Button btnLienSinoe;
    
    /** URL SINOE de la decheterie */
    private String urlSinoe;
    /** URL de l'itineraire */
    private String urlItineraire;
    /** Layout des horaires */
    private RelativeLayout horairesLayout;
    /** Layout des déchets acceptés/refusés */
    private RelativeLayout acceptesLayout;
    
    /** Handler pour l'attente des messages de fin de thread */
    Handler viewUpdateHandler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GoogleMap.MAJ_DONNEES_DECHETERIE:
                // Affichage des jours d'ouverture et des horaires
                TextView texteJoursOuverts = (TextView) horairesLayout.findViewById(R.id.texteJoursOuverts);
                texteJoursOuverts.setText(msg.getData().getString("OUVERT"));
                TextView texteHoraires = (TextView) horairesLayout.findViewById(R.id.texteHoraires);
                texteHoraires.setText(msg.getData().getString("HORAIRES"));

                // Affichage des déchets acceptes
                String acceptes = msg.getData().getString("ACCEPTES");
                if ("".equals(acceptes)) {
                    TextView labelAcceptes = (TextView) acceptesLayout.findViewById(R.id.labelAcceptes);
                    labelAcceptes.setVisibility(View.GONE);
                    TextView texteAcceptes = (TextView) acceptesLayout.findViewById(R.id.texteAcceptes);
                    texteAcceptes.setVisibility(View.GONE);
                } else {
                    TextView texteAcceptes = (TextView) acceptesLayout.findViewById(R.id.texteAcceptes);
                    texteAcceptes.setText(acceptes);
                }
                
                // Affichage des déchets refusés
                String refuses = msg.getData().getString("REFUSES");
                if ("".equals(refuses)) {
                    TextView labelRefuses = (TextView) acceptesLayout.findViewById(R.id.labelRefuses);
                    labelRefuses.setVisibility(View.GONE);
                    TextView texteRefuses = (TextView) acceptesLayout.findViewById(R.id.texteRefuses);
                    texteRefuses.setVisibility(View.GONE);
                } else {
                    TextView texteRefuses = (TextView) acceptesLayout.findViewById(R.id.texteRefuses);
                    texteRefuses.setText(refuses);
                }
                break;
            default:
                Log.d("handleMessage", "Message non prévu");
                break;
            }
            super.handleMessage(msg);
        }
    }; 
    
    /** Listener sur les boutons */
    private OnClickListener btnListener = new OnClickListener()
    {
        /**
         * @param v "bouton" sur lequel on a cliqué
         */
        public void onClick(View bouton)
        {
            if(btnMore.equals(bouton)) {
                // Clic sur le bouton [+]
                btnMore.setVisibility(View.GONE);
                horairesLayout.setVisibility(View.VISIBLE);
                acceptesLayout.setVisibility(View.VISIBLE);
                btnSYRendre.setVisibility(View.VISIBLE);
                btnLienSinoe.setVisibility(View.VISIBLE);
            } else if(btnSYRendre.equals(bouton)) {
                // Clic sur le bouton "S'y rendre"
                GoogleMap.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlItineraire)));
            } else if(btnLienSinoe.equals(bouton)) {
                // Clic sur le lien SINOE
                GoogleMap.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlSinoe)));
            }
        }
    };
    
    /**
     * Lance un thread permettant la récupération des données (horaires, déchèts acceptés, etc.)
     * depuis la fiche SINOE de la déchèterie
     */
    private void threadGetDataFromSinoe() {
        // Thread de lecture des horaires
        new Thread() {
            /**
             * Run du thread
             */
            public void run() {
                try {
                    URL url = new URL(urlSinoe);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "ISO-8859-1"));

                    // Construction pattern pour les jours ouverts/horaires/jours fermés
                    Pattern patternDebutHoraires = Pattern.compile("^[\\W]*<td width=\"80%\"><div class=\"champConsult\">&nbsp;");
                    Pattern patternFinHoraires = Pattern.compile("</div></td>[\\W]*$");
                    Pattern patternBrFinHoraires = Pattern.compile("<br>[\\W]*$");
                    Pattern patternBrHoraires = Pattern.compile("<br>");
                    Matcher matcherDebut, matcherFin, matcherBrFin, matcherBr;
                    
                    String strTmp;
                    String strJoursOuverts = null;
                    String strHoraires = null;

                    // Recherche du motif pour les jours ouverts
                    while ((strJoursOuverts == null) && ((strTmp = in.readLine()) != null)) {
                        matcherDebut = patternDebutHoraires.matcher(strTmp);
                        while (matcherDebut.find()) {
                            // Motif trouvé, extraction des jours ouverts
                            strJoursOuverts = matcherDebut.replaceFirst("");
                            matcherFin = patternFinHoraires.matcher(strJoursOuverts);
                            strJoursOuverts = matcherFin.replaceFirst("");
                            matcherBrFin = patternBrFinHoraires.matcher(strJoursOuverts);
                            strJoursOuverts = matcherBrFin.replaceFirst("");
                            matcherBr = patternBrHoraires.matcher(strJoursOuverts);
                            strJoursOuverts = matcherBr.replaceAll("\n");
                        }
                    }

                    // Recherche du motif pour les horaires
                    while ((strHoraires == null) && ((strTmp = in.readLine()) != null)) {
                        matcherDebut = patternDebutHoraires.matcher(strTmp);
                        while (matcherDebut.find()) {
                            // Motif trouvé, extraction des horaires
                            strHoraires = matcherDebut.replaceFirst("");
                            matcherFin = patternFinHoraires.matcher(strHoraires);
                            strHoraires = matcherFin.replaceFirst("");
                            matcherBrFin = patternBrFinHoraires.matcher(strHoraires);
                            strHoraires = matcherBrFin.replaceFirst("");
                            matcherBr = patternBrHoraires.matcher(strHoraires);
                            strHoraires = matcherBr.replaceAll("\n");
                        }
                    }
                    
                    // Construction des patterns pour les déchets acceptés/refusés
                    Pattern patternDebutDechet = Pattern.compile("^[\\W]*<td align=\"left\">&nbsp;");
                    Pattern patternFinDechet = Pattern.compile("</td>[\\W]*$");
                    Pattern patternDebutAccepte = Pattern.compile("^[\\W]*<td align=\"center\">&nbsp;");
                    Pattern patternFinAccepte = Pattern.compile("</td>[\\W]*$");
                    Matcher matcherDebutDechet, matcherFinDechet, matcherDebutAccepte, matcherFinAccepte;
                    
                    // Recherche des déchets acceptés/refusés
                    StringBuffer sbAccepte = new StringBuffer("");
                    StringBuffer sbRefuse = new StringBuffer("");
                    while ((strTmp = in.readLine()) != null) {
                        matcherDebutDechet = patternDebutDechet.matcher(strTmp);
                        while (matcherDebutDechet.find()) {
                            // Passage de lignes inutiles
                            in.readLine();    strTmp = in.readLine();
                            // Arrivée à la ligne du déchet, remplacement des balises <td> adjacentes
                            matcherDebutDechet = patternDebutDechet.matcher(strTmp);
                            String dechet = matcherDebutDechet.replaceFirst("");
                            matcherFinDechet = patternFinDechet.matcher(dechet);
                            dechet = matcherFinDechet.replaceFirst("");
                            // Passage de lignes inutiles
                            in.readLine(); strTmp = in.readLine();
                            // Arrivée à la ligne du "accepté ?", remplacement des balises <td> adjacentes
                            matcherDebutAccepte = patternDebutAccepte.matcher(strTmp);
                            String ouiNon = matcherDebutAccepte.replaceFirst("");
                            matcherFinAccepte = patternFinAccepte.matcher(ouiNon);
                            ouiNon = matcherFinAccepte.replaceFirst("");
                            // En fonction du "oui" ou du "non" on place le déchet dans les "acceptés" ou les "refusés"
                            if("Oui".equals(ouiNon)) {
                                sbAccepte.append("  "+dechet+"\n");
                            } else if("Non".equals(ouiNon)) {
                                sbRefuse.append("  "+dechet+"\n");
                            }
                            // Passage de lignes inutiles
                            in.readLine(); in.readLine(); in.readLine(); in.readLine(); in.readLine(); in.readLine();
                        }
                    }
                    
                    in.close();
                    // Envoi des données
                    Bundle bundle = new Bundle();
                    bundle.putString("OUVERT", strJoursOuverts);
                    bundle.putString("HORAIRES", strHoraires);
                    bundle.putString("ACCEPTES", sbAccepte.toString());
                    bundle.putString("REFUSES", sbRefuse.toString());
                    Message m = new Message();
                    m.setData(bundle);
                    m.what = GoogleMap.MAJ_DONNEES_DECHETERIE;
                    GoogleMap.this.viewUpdateHandler.sendMessage(m); 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    /**
     * Retourne l'URL du service Web d'ecoemballages
     * @param latitude latitude de la position
     * @param longitude longitude de la position
     * @param rayon rayon de recherche des déchèteries
     * @return l'URL du service Web d'Ecoemballages
     */
    private URL buildUrlEcoemballages(double latitude, double longitude, int rayon) {
        // Construction de l'URL du service Web d'Ecoemballages
        URL url = null;
        try {
            StringBuffer bufferUrl = new StringBuffer("http://tri-recyclage.ecoemballages.fr/googlemaps/find-decheteries.php?");
            bufferUrl.append("lat="+latitude);
            bufferUrl.append("&lng="+longitude);
            bufferUrl.append("&radius="+rayon*UNIT_RADIUS);

            url = new URL(bufferUrl.toString());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return url;
    }
    
    /**
     * Retourne la liste des déchèteries après parsing d'ecoemballages
     * @param url URL à parser
     * @return une liste de markers tirée de l'URL
     */
    private ListeMarkers parseXmlEcoemballages(URL url) {
        // Création du parseur SAX
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = null;
        try {
            sp = spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Récupération du lecteur XML du parseur SAX nouvellement créé
        XMLReader xr = null;
        try {
            xr = sp.getXMLReader();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        /* Create a new ContentHandler and apply it to the XML-Reader*/
        SAXEcoemballagesParser parserEcoemballages = new SAXEcoemballagesParser();
        xr.setContentHandler(parserEcoemballages);
        
        // Début du parsing du contenu de l'URL
        InputSource in = new InputSource();            
        try {
            // Récupération du contenu de l'URL
            in.setByteStream(url.openStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        in.setEncoding("ISO-8859-1"); // Changement du charset
        try {
            xr.parse(in); // Parsing
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Fin du parsing
        return parserEcoemballages.getListeDesMarkers();
    }
    
    
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.maplayout);
        
        mapView = (MapView) findViewById(R.id.googlemapview);
        mc = mapView.getController();

        // Affichage du controleur de zoom
        LinearLayout zoomLayout = (LinearLayout) findViewById(R.id.zoomcontrols);  
        View zoomView = mapView.getZoomControls();
        zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
        mapView.displayZoomControls(true);
        
        // Récupération des paramètres
        Bundle bundle = this.getIntent().getExtras();
        double latitude = bundle.getDouble("LATITUDE");
        double longitude = bundle.getDouble("LONGITUDE");
        int rayon = bundle.getInt("RAYON");
        
        // Construction de l'URL à parser
        URL url = buildUrlEcoemballages(latitude, longitude, rayon);
        // Récupération de la liste des markers
        ListeMarkers listeDesMarkers = parseXmlEcoemballages(url);
        //Log.d("Mark", listeDesMarkers.toString());
                
        //TODO CALCULER LE ZOOM EN FCT DU RADIUS
        zoomInitial = 12;
        //zoomInitial = 6;
        
        // Récupération de l'ensemble des couches de la carte
        mapOverlays = mapView.getOverlays();
        
        centreOverlay = new DecheMapOverlay(this, R.drawable.blue_marker); // Création de la couche du point initial
        // Ajout du point initial (centre) à sa couche
        centreInitial = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
        DecheLocation overlayitem = new DecheLocation(centreInitial, "Vous êtes ici !", "", "", "-1");
        centreOverlay.addOverlay(overlayitem);
        // Ajout de la couche du centre initial à la superposition des couches
        mapOverlays.add(centreOverlay);
        
        // S'il y a des déchèteries, on crée la couche des déchèteries 
        if(listeDesMarkers.size() != 0) {
            decheMapOverlay = new DecheMapOverlay(this, R.drawable.marker_decheterie);
            // Ajouts des déchèteries à la couche des déchèteries
            Iterator<Marker> iterator = listeDesMarkers.iterator();
            while(iterator.hasNext()) {
                Marker marker = iterator.next();
                GeoPoint point = new GeoPoint((int) (Double.parseDouble(marker.getLat()) * 1E6), (int) (Double.parseDouble(marker.getLng()) * 1E6));
                DecheLocation item = new DecheLocation(point, marker.getNom(), marker.getAdresse(), marker.getDistance(), marker.getSinoe());
                decheMapOverlay.addOverlay(item);
            }
            // Ajout de la couche des déchèteries à la superposition des couches
            mapOverlays.add(decheMapOverlay);
        } else {
            Toast.makeText(this, "Aucune déchèterie trouvée pour ce rayon", Toast.LENGTH_LONG).show();
        }
        
        mc.animateTo(centreInitial);
        mc.setZoom(zoomInitial);
        mapView.invalidate();
    }
    
    /**
     * Gestion de la frappe sur la Google Map
     * @param dl la déchèterie sur laquelle la frappe a été détectée
     */
    public void onTap(DecheLocation dl) {
        mc.animateTo(dl.getPoint());
        
        if ("-1".equals(dl.getSinoe())) {
            // Centre initial
            Toast.makeText(this, dl.getNom(), Toast.LENGTH_SHORT).show();
        } else {
            // Autre point (déchèterie)
            
            // Création de la boite de dialogue
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Suppression de la zone de titre pour les Dialog des déchèteries
            dialog.setContentView(R.layout.dialogdechelayout);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            
            
            // Affichage du nom, de la distance et de l'adresse
            TextView viewNom = (TextView) dialog.findViewById(R.id.nom);
            viewNom.setText(dl.getNom());
            TextView viewDistance = (TextView) dialog.findViewById(R.id.distance);
            viewDistance.setText("["+Double.toString(Math.round(Double.parseDouble(dl.getDistance())*10)/10.0)+" km]");
            TextView viewAdresse = (TextView) dialog.findViewById(R.id.adresse);
            viewAdresse.setText(dl.getAdresse());
            
            
            // Construction des liens
            urlSinoe = "http://www.sinoe.org/exploitgeneassistee/consultActeurService/consultService.php?MODE=SEUL&IDSERV="+dl.getSinoe();
            urlItineraire = "http://maps.google.com/maps?f=d&source=s_d&saddr="+centreInitial.getLatitudeE6()/1E6+"%20"+centreInitial.getLongitudeE6()/1E6+"&daddr="+dl.getPoint().getLatitudeE6()/1E6+"%20"+dl.getPoint().getLongitudeE6()/1E6+"&hl=fr";
            

            horairesLayout = (RelativeLayout) dialog.findViewById(R.id.horairesLayout);
            horairesLayout.setVisibility(View.GONE);

            acceptesLayout = (RelativeLayout) dialog.findViewById(R.id.acceptesLayout);
            acceptesLayout.setVisibility(View.GONE);
            
            
            // Mise en place du listener sur les boutons
            btnMore = (Button) dialog.findViewById(R.id.btnMore);
            btnMore.setOnClickListener(btnListener);
            btnSYRendre = (Button) dialog.findViewById(R.id.btnSYRendre);
            btnSYRendre.setVisibility(View.GONE);
            btnSYRendre.setOnClickListener(btnListener);
            btnLienSinoe = (Button) dialog.findViewById(R.id.btnLienSinoe);
            btnLienSinoe.setVisibility(View.GONE);
            btnLienSinoe.setOnClickListener(btnListener);

            threadGetDataFromSinoe(); // Récupération des données de la fiche SINOE (horaires, déchets acceptés, etc.)
            dialog.show(); // Affichage de la boite de dialogue de la déchèterie
        }
    } 
    
      
    /**
     * Gestion des événements sur la Google Map
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // Appui sur le bouton du milieu (entre les 4 flèches) :
                // on revient au zoom et au centre initiaux
                mc.setZoom(zoomInitial);
                mc.animateTo(centreInitial);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Impératif pour être en accord avec les conditions d'utilisation du système Google Maps sur Android
     * @return false si une route n'est pas affichée sur la carte (ce qui est le cas ici)
     */
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
}
