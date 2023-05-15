package com.starsearth.three.domain

class Content {

    var title : String
    var description : String? = null
    var tags : ArrayList<String> = ArrayList()
    var rowType : ROW_TYPE


    constructor(title: String, rowType: ROW_TYPE) {
        this.title = title
        this.rowType = rowType
    }

    constructor(title: String, description: String, tags: ArrayList<String>, rowType: ROW_TYPE) {
        this.title = title
        this.description = description
        this.tags.addAll(tags)
        this.rowType = rowType
    }

    companion object {
        enum class ROW_TYPE {
            ROW_TYPE_KEY,
            TIME_12HR,
            DATE,
            BATTERY_LEVEL,
            CAMERA_OCR,
            CAMERA_OBJECT_DETECTION,
            MANUAL,
            CONTENT,
            TAG_FOR_SEARCH
        }
    }
}