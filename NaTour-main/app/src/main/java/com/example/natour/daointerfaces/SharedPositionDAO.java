package com.example.natour.daointerfaces;

import android.content.Context;

import com.example.natour.callbackinterfaces.GetSharedPositionListCallback;
import com.example.natour.callbackinterfaces.SetSharedPositionCallback;
import com.google.android.gms.maps.model.LatLng;

public interface SharedPositionDAO {
    void getSharedPositionList(int id, Context context, GetSharedPositionListCallback callback);
    void setSharedPosition(int id, LatLng position, Context context, SetSharedPositionCallback callback);
}
