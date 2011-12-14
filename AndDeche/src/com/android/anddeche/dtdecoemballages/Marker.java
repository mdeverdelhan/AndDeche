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

package com.android.anddeche.dtdecoemballages;

/**
 * Classe representant un marker dans le XML fourni par ecoemballages
 * @author Marc de Verdelhan
 *
 */
public class Marker {
    /**
     * Les attributs suivants correspondent aux paramètres des balises XML "marker".
     * Exemple :
     * <marker nom="DECHETERIE DE MARGUERITTES" adresse="Route de Poulx, BP 17, 30320, Marguerittes"
     *  lat="43.895740509033" lng="4.421750068665" distance="4.02906002766" mapeos="30TBC"
     *  cl="CL030013" sinoe="2780" insee="30156"/>
     */
    private String nom;            // Nom de la déchèterie
    private String adresse;        // Adresse de la déchèterie
    private String lat;            // Latitude du point de la déchèterie
    private String lng;            // Longitude du point de la déchèterie
    private String distance;    // Distance par rapport à la position de référence
    private String mapeos;        //
    private String cl;            //
    private String sinoe;        // Numéro sur le site de SINOE : http://www.sinoe.org/
    private String insee;        // Numéro INSEE de la commune
    

    
    public Marker() {
        // Rien à faire
    }
    
    
    /**
     * @return the nom
     */
    public String getNom() {
        return nom;
    }


    /**
     * @return the adresse
     */
    public String getAdresse() {
        return adresse;
    }


    /**
     * @return the lat
     */
    public String getLat() {
        return lat;
    }


    /**
     * @return the lng
     */
    public String getLng() {
        return lng;
    }


    /**
     * @return the distance
     */
    public String getDistance() {
        return distance;
    }


    /**
     * @return the mapeos
     */
    public String getMapeos() {
        return mapeos;
    }


    /**
     * @return the cl
     */
    public String getCl() {
        return cl;
    }


    /**
     * @return the sinoe
     */
    public String getSinoe() {
        return sinoe;
    }


    /**
     * @return the insee
     */
    public String getInsee() {
        return insee;
    }

    
    /**
     * @param nom the nom to set
     */
    public void setNom(String nom) {
        this.nom = nom;
    }


    /**
     * @param adresse the adresse to set
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }


    /**
     * @param lat the lat to set
     */
    public void setLat(String lat) {
        this.lat = lat;
    }


    /**
     * @param lng the lng to set
     */
    public void setLng(String lng) {
        this.lng = lng;
    }


    /**
     * @param distance the distance to set
     */
    public void setDistance(String distance) {
        this.distance = distance;
    }


    /**
     * @param mapeos the mapeos to set
     */
    public void setMapeos(String mapeos) {
        this.mapeos = mapeos;
    }


    /**
     * @param cl the cl to set
     */
    public void setCl(String cl) {
        this.cl = cl;
    }


    /**
     * @param sinoe the sinoe to set
     */
    public void setSinoe(String sinoe) {
        this.sinoe = sinoe;
    }


    /**
     * @param insee the insee to set
     */
    public void setInsee(String insee) {
        this.insee = insee;
    }

    /**
     * @return une chaîne de caractères représentant un marker
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Marker: Nom='" + this.nom + "', ");
        buf.append("Adresse='" + this.adresse + "', ");
        buf.append("Lat='" + this.lat + "', ");
        buf.append("Lng='" + this.lng + "', ");
        buf.append("Distance='" + this.distance + "', ");
        buf.append("Mapeos='" + this.mapeos + "', ");
        buf.append("Cl='" + this.cl + "', ");
        buf.append("Sinoe='" + this.sinoe + "', ");
        buf.append("Insee='" + this.insee + "'");
        return buf.toString();
                        
    }
}
