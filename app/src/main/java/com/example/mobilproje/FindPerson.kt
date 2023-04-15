package com.example.mobilproje

import java.io.Serializable

enum class LookingStatus(val intValue: Int) {
    LOOKING_HOUSE(0),
    LOOKING_FRIEND(1),
    NOT_LOOKING(2);
}



data class FindPerson(
    var department: String = "",
    var curClass: String = "",
    var duration: String = "",
    var distance: String = "",
    var userName:String = "",
    var lookingStatus: LookingStatus = LookingStatus.NOT_LOOKING,
): Serializable {
    constructor() : this( "", "", "", "", "",LookingStatus.NOT_LOOKING)
}

