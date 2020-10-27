package com.starsearth.three.domain

class Action {

    lateinit var title : String
    var description : String? = null
    lateinit var rowType : ROW_TYPE


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
            CAMERA_OCR
        }
    }
}