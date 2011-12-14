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

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.android.anddeche.dtdecoemballages.ListeMarkers;
import com.android.anddeche.dtdecoemballages.Marker;

public class SAXEcoemballagesParser extends DefaultHandler {    

    /** List des markers */
    private ListeMarkers listeDesMarkers;

    /** Pile utilisée pour le parsing */
    private Stack stack;

    private Locator locator;

    /** Constructeur de la classe */
    public SAXEcoemballagesParser() {
        this.stack = new Stack();
    }

    /**
     * @return la liste listeDesMarkers
     */
    public ListeMarkers getListeDesMarkers() {
        return listeDesMarkers;
    }

    /**
     * @param locator the locator to set
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * 
     */
    public void startElement(String uri, String localName, String qName, Attributes attribs) {

        if(localName.equals("markers")) {
            // Mise dans la pile d'un élément <markers>
            stack.push(new ListeMarkers());
        }
        else {
            if(localName.equals("marker")) {
                // Mise dans la pile d'un élément <marker> (sans S final cette fois)
                stack.push(new Marker());
                String tmp = resolveAttrib(uri, "nom", attribs, "inconnu");
                ((Marker)stack.peek()).setNom(tmp);
                tmp = resolveAttrib(uri, "adresse", attribs, "inconnue");
                ((Marker)stack.peek()).setAdresse(tmp);
                tmp = resolveAttrib(uri, "lat", attribs, "0");
                ((Marker)stack.peek()).setLat(tmp);
                tmp = resolveAttrib(uri, "lng", attribs, "0");
                ((Marker)stack.peek()).setLng(tmp);
                tmp = resolveAttrib(uri, "distance", attribs, "0");
                ((Marker)stack.peek()).setDistance(tmp);
                tmp = resolveAttrib(uri, "mapeos", attribs, "inconnu");
                ((Marker)stack.peek()).setMapeos(tmp);
                tmp = resolveAttrib(uri, "cl", attribs, "inconnu");
                ((Marker)stack.peek()).setCl(tmp);
                tmp = resolveAttrib(uri, "sinoe", attribs, "0");
                ((Marker)stack.peek()).setSinoe(tmp);
                tmp = resolveAttrib(uri, "insee", attribs, "0");
                ((Marker)stack.peek()).setInsee(tmp);
            } else {
                // TODO GERER LE CAS D'ERREUR
                Log.d("SAX ECO", "ELEMENT INATTENDU");
            }
        }
    }
    
    /**
     * 
     */
    public void endElement(String uri, String localName, String qName) {
        // pop stack and add to 'parent' element, which is next on the stack
        // important to pop stack first, then peek at top element!
        Object tmp = stack.pop();
        
        if( localName.equals("markers")) {
            this.listeDesMarkers = (ListeMarkers)tmp;
        }
        else {
            if(localName.equals("marker") ) {
                ((ListeMarkers)stack.peek()).addMarker((Marker)tmp);
            }
            else {
                // TODO GERER LE CAS D'ERREUR
                Log.d("SAX ECO", "ELEMENT INATTENDU");
                stack.push(tmp);
            }
        }
    }

    
    
    
    /**
     * Permet de récupérer un attribut du nom de localname dans une balise XML
     * @param uri
     * @param localName le nom de l'attribut dont on veut récupérer la valeur
     * @param attribs
     * @param defaultValue valeur par défaut pour l'attribut
     * @return
     */
    private String resolveAttrib(String uri, String localName, Attributes attribs, String defaultValue) {
        String tmp = attribs.getValue(uri, localName);
        if(tmp == null) {
            tmp = defaultValue;
        }
        return tmp;
    }
}


