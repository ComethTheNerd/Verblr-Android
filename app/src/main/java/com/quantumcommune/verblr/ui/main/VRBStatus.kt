package com.quantumcommune.verblr.ui.main

data class VRBStatus(
    val icon : Int? = null,
    val label : String,
    val action : (() -> Unit)? = null
)