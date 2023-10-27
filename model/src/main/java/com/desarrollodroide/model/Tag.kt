package com.desarrollodroide.model


data class Tag (
    val id: Int,
    val name: String,
    var selected: Boolean,
){
    constructor(name: String) : this(-1, name, false)

}
