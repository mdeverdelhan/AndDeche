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

import java.util.ArrayList;

import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Cette classe sert à dessiner la sur-couche (overlay) de la Google Map.
 * Cette sur-couche contient notamment les markers de la carte. 
 * @author Marc de Verdelhan
 *
 */
public class DecheMapOverlay extends ItemizedOverlay {

    /** Liste des items de l'overlay */
    private ArrayList<DecheLocation> mOverlays = new ArrayList<DecheLocation>();
    
    /** Google Map */
    private GoogleMap map;
    
    public DecheMapOverlay(GoogleMap map, int defaultMarker) {
        super(boundCenterBottom(map.getResources().getDrawable(defaultMarker)));
        this.map = map;
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    public void addOverlay(DecheLocation overlay) {
        mOverlays.add(overlay);
        populate();
    }
    
    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index) {
        map.onTap(mOverlays.get(index));
        return super.onTap(index);
    }
}
