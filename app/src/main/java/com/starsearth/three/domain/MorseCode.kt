package com.starsearth.three.domain

class MorseCode {

    var mcArray = ArrayList<MorseCodeCell>()
    var alphabetToMCMap = HashMap<String, String>()

    constructor() {
        populate("morse_code")
    }

    fun populate(type: String?) {
        if (type == "actions") {
            mcArray.addAll(elements = populateActions())
        }
        else if (type == "morse_code") {
            mcArray.addAll(elements = populateMorseCodeAlphanumeric())
        }
        else {
            mcArray.addAll(elements = populateActions())
            mcArray.addAll(elements = populateMorseCodeAlphanumeric())
        }

        for (morseCodeCell in mcArray) {
            if (morseCodeCell.displayChar != null) {
                morseCodeCell.displayChar?.let { alphabetToMCMap.put(it, morseCodeCell.morseCode!!) }
            }
            else {
                alphabetToMCMap.put(morseCodeCell.alphanumeric!!, morseCodeCell.morseCode!!)
            }
        }

        //mcTreeNode = createTree()
    }

    fun populateActions() : ArrayList<MorseCodeCell> {
        val array = ArrayList<MorseCodeCell>()
        array.add(MorseCodeCell(alphanumeric =  "TIME", morseCode = ".", type = "action"))
        array.add(MorseCodeCell(alphanumeric =  "DATE", morseCode = "..", type = "action"))
        array.add(MorseCodeCell(alphanumeric = "CAMERA", morseCode = "...", type = "action"))
        return array
    }

    fun populateMorseCodeAlphanumeric() : ArrayList<MorseCodeCell> {
        val array = ArrayList<MorseCodeCell>()
        array.add(MorseCodeCell(alphanumeric = "A", morseCode= ".-"))
        array.add(MorseCodeCell(alphanumeric = "B", morseCode = "-..."))
        array.add(MorseCodeCell(alphanumeric = "C", morseCode =  "-.-."))
        array.add(MorseCodeCell(alphanumeric = "D", morseCode =  "-.."))
        array.add(MorseCodeCell(alphanumeric = "E", morseCode =  "."))
        array.add(MorseCodeCell(alphanumeric= "F", morseCode= "..-."))
        array.add(MorseCodeCell(alphanumeric= "G", morseCode= "--."))
        array.add(MorseCodeCell(alphanumeric= "H", morseCode= "...."))
        array.add(MorseCodeCell(alphanumeric= "I", morseCode= ".."))
        array.add(MorseCodeCell(alphanumeric= "J", morseCode= ".---"))
        array.add(MorseCodeCell(alphanumeric= "K", morseCode= "-.-"))
        array.add(MorseCodeCell(alphanumeric= "L", morseCode= ".-.."))
        array.add(MorseCodeCell(alphanumeric= "M", morseCode= "--"))
        array.add(MorseCodeCell(alphanumeric= "N", morseCode= "-."))
        array.add(MorseCodeCell(alphanumeric= "O", morseCode= "---"))
        array.add(MorseCodeCell(alphanumeric= "P", morseCode= ".--."))
        array.add(MorseCodeCell(alphanumeric= "Q", morseCode= "--.-"))
        array.add(MorseCodeCell(alphanumeric= "R", morseCode= ".-."))
        array.add(MorseCodeCell(alphanumeric= "S", morseCode= "..."))
        array.add(MorseCodeCell(alphanumeric= "T", morseCode= "-"))
        array.add(MorseCodeCell(alphanumeric= "U", morseCode= "..-"))
        array.add(MorseCodeCell(alphanumeric= "V", morseCode= "...-"))
        array.add(MorseCodeCell(alphanumeric= "W", morseCode= ".--"))
        array.add(MorseCodeCell(alphanumeric= "X", morseCode= "-..-"))
        array.add(MorseCodeCell(alphanumeric= "Y", morseCode= "-.--"))
        array.add(MorseCodeCell(alphanumeric= "Z", morseCode= "--.."))
        array.add(MorseCodeCell(alphanumeric= "1", morseCode= ".----"))
        array.add(MorseCodeCell(alphanumeric= "2", morseCode= "..---"))
        array.add(MorseCodeCell(alphanumeric= "3", morseCode= "...--"))
        array.add(MorseCodeCell(alphanumeric= "4", morseCode= "....-"))
        array.add(MorseCodeCell(alphanumeric= "5", morseCode= "....."))
        array.add(MorseCodeCell(alphanumeric= "6", morseCode= "-...."))
        array.add(MorseCodeCell(alphanumeric= "7", morseCode= "--..."))
        array.add(MorseCodeCell(alphanumeric= "8", morseCode= "---.."))
        array.add(MorseCodeCell(alphanumeric= "9", morseCode= "----."))
        array.add(MorseCodeCell(alphanumeric= "0", morseCode= "-----"))
        array.add(MorseCodeCell(alphanumeric =  "Space (␣)", morseCode =  ".......", type = "morse_code", displayChar =  "␣"))
        return array
    }

}