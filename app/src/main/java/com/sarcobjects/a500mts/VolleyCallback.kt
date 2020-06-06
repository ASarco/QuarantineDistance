package com.sarcobjects.a500mts

interface VolleyCallback<R> {
    fun onSuccessResponse(results: R)
    fun onErrorResponse(msg: String?, stringKey: Int)
}