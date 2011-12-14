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

import java.util.Vector;

/**
 * Classe representant une liste de markers dans le XML fourni par ecoemballages
 * @author Marc de Verdelhan
 *
 */
public class ListeMarkers extends Vector<Marker> {
    
    /** Constructeur de la classe */
    public ListeMarkers() {
    }
    
    /**
     * Permet d'ajouter un marker à la liste
     * @param m le marker à ajouter
     */
    public void addMarker(Marker m) {
        this.addElement(m);
    }
    
    /**
     * @return une chaîne de caractères représentant la liste des markers
     */
    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();
        
        buf.append( "--- Markers ---" ).append(newline);
        for(int i = 0; i < this.size(); i++){
            buf.append(this.elementAt(i)).append(newline);
        }
        return buf.toString();
    }    
}
