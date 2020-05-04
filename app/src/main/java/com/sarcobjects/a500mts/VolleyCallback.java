package com.sarcobjects.a500mts;

public interface VolleyCallback<R> {
    void onSuccessResponse(R results);
    void onErrorResponse(String msg, int stringKey);
}
