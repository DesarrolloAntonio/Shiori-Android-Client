package com.desarrollodroide.model


data class Tag (
    val id: Int,
    val name: String,
    var selected: Boolean,
    val nBookmarks: Int
){
    constructor(
        id: Int,
        name: String
    ) : this(id, name, false, 0)

}
