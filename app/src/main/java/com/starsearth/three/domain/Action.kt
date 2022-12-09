package com.starsearth.three.domain

class Action {

    var title : String
    var description : String? = null
    var rowType : ROW_TYPE


    constructor(title: String, rowType: ROW_TYPE) {
        this.title = title
        this.rowType = rowType
    }

    constructor(title: String, description: String, rowType: ROW_TYPE) {
        this.title = title
        this.description = description
        this.rowType = rowType
    }

    companion object {
        enum class ROW_TYPE {
            ROW_TYPE_KEY,
            TIME_12HR,
            DATE,
            BATTERY_LEVEL,
            CAMERA_OCR,
            CAMERA_OBJECT_DETECTION
        }
    }
}