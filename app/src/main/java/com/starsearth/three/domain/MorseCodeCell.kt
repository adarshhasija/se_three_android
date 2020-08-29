package com.starsearth.three.domain

class MorseCodeCell {

    var alphanumeric : String? = null
    var morseCode : String? = null
    var type : String? = null
    var displayChar : String? = null

    constructor (alphanumeric : String, morseCode : String) {
        this.alphanumeric = alphanumeric
        this.morseCode = morseCode
    }

    constructor(alphanumeric: String, morseCode: String, type : String) {
        this.alphanumeric = alphanumeric
        this.morseCode = morseCode
        this.type = type
    }

    constructor (alphanumeric : String, morseCode : String, type : String, displayChar : String) {
        this.alphanumeric = alphanumeric
        this.morseCode = morseCode
        this.type = type
        this.displayChar = displayChar
    }
}