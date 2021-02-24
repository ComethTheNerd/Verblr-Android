package com.quantumcommune.verblr

import org.json.JSONObject

// [dho] adapted from : https://stackoverflow.com/a/50468095/300037 - 17/05/20
class APIResponse(json: String) : JSONObject(json) {

    val message: String? = this.optString("message");
//
//    val type: String? = this.optString("type")
//    val data = this.optJSONArray("data")
//        ?.let { 0.until(it.length()).map { i -> it.optJSONObject(i) } } // returns an array of JSONObject
//        ?.map { Foo(it.toString()) } // transforms each JSONObject of the array into Foo
}
//
//class Foo(json: String) : JSONObject(json) {
//    val id = this.optInt("id")
//    val title: String? = this.optString("title")
//}

// val foos = APIResponse(jsonString)