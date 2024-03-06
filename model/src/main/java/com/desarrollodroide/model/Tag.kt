package com.desarrollodroide.model


data class Tag (
    val id: Int,
    val name: String,
    var selected: Boolean,
    val nBookmarks: Int
){
    constructor(name: String) : this(-1, name, false, 0)

}
