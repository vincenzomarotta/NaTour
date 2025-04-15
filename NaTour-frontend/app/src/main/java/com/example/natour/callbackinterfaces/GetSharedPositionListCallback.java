package com.example.natour.callbackinterfaces;

import com.example.natour.entity.SharedPosition;

import java.util.List;

public interface GetSharedPositionListCallback {
    void onSuccess(List<SharedPosition> sharedPositionList);
    void onFailure();
}
