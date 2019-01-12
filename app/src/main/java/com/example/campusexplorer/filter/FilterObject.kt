package com.example.campusexplorer.filter

import kotlin.properties.Delegates

class FilterObject(name: String, active: Boolean) {

    var name: String by Delegates.notNull()
    var active: Boolean by Delegates.notNull()

    init {
        this.name = name;
        this.active = active
    }
}