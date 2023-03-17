package com.starsearth.three.domain

class Braille {

    var array: ArrayList<BrailleCell> = ArrayList<BrailleCell>()
    var alphabetToBrailleDictionary : HashMap<String, String> = HashMap()

    //Braille grid
    //1 4
    //2 5
    //3 6
    //String indexes
    //01\n34\n67

    //Braille grid
    //1 4  7 10
    //2 5  8 11
    //3 6  9 12
    //String indexes for numbers (2 grids)
    //01 34\n67 9(10)\n(12)(13) (14)(15)
    companion object {
        val mappingBrailleGridToStringIndex = mapOf( //this is used when setting up dots in a string. Based on its grid number, it has to be placed at a specific index in  the string
            1 to 0,
            2 to 3,
            3 to 6,
            4 to 1,
            5 to 4,
            6 to 7,
        )

        val mappingBrailleGridNumbersToStringIndex = mapOf( //numbers use 2 grids
            1 to 0,
            2 to 6,
            3 to 12,
            4 to 1,
            5 to 7,
            6 to 13,
            7 to 3,
            8 to 9,
            9 to 15,
            10 to 4,
            11 to 10,
            12 to 16,
        )

        val brailleIndexOrderForVerticalReading = mapOf( //Simillar to map above except that this is using standard index traversal when the reaaader parses through it
            0 to 0,
            1 to 3,
            2 to 6,
            3 to 1,
            4 to 4,
            5 to 7,
        )

        val brailleIndexOrderForNumbersVerticalReading = mapOf( //numbers use 2 grids
            0 to 0,
            1 to 6,
            2 to 12,
            3 to 1,
            4 to 7,
            5 to 13,
            6 to 3,
            7 to 9,
            8 to 15,
            9 to 4,
            10 to 10,
            11 to 16,
        )

        val brailleIndexOrderForHorizontalReading = mapOf( //Simillar to map above except that this is using standard index traversal when the reaaader parses through it
            0 to 0,
            1 to 1,
            2 to 3,
            3 to 4,
            4 to 6,
            5 to 7,
        )

        val brailleIndexOrderForNumbersHorizontalReading = mapOf( //numbers use 2 grids
            0 to 0,
            1 to 1,
            2 to 6,
            3 to 7,
            4 to 12,
            5 to 13,
            6 to 3,
            7 to 4,
            8 to 9,
            9 to 10,
            10 to 15,
            11 to 16,
        )
    }

    constructor() {
        populate()
    }


    fun populate() {
        array.add(BrailleCell("a", "1"))
        array.add(BrailleCell("b", "12"))
        array.add(BrailleCell("c", "14"))
        array.add(BrailleCell("d", "145"))
        array.add(BrailleCell("e", "15"))
        array.add(BrailleCell("f", "124"))
        array.add(BrailleCell("g", "1245"))
        array.add(BrailleCell("h", "125"))
        array.add(BrailleCell("i", "24"))
        array.add(BrailleCell("j", "245"))
        array.add(BrailleCell("k", "13"))
        array.add(BrailleCell("l", "123"))
        array.add(BrailleCell("m", "134"))
        array.add(BrailleCell("n", "1345"))
        array.add(BrailleCell("o", "135"))
        array.add(BrailleCell("p", "1234"))
        array.add(BrailleCell("q", "12345"))
        array.add(BrailleCell("r", "1235"))
        array.add(BrailleCell("s", "234"))
        array.add(BrailleCell("t", "2345"))
        array.add(BrailleCell("u", "136"))
        array.add(BrailleCell("v", "1236"))
        array.add(BrailleCell("w", "2456"))
        array.add(BrailleCell("x", "1346"))
        array.add(BrailleCell("y", "13456"))
        array.add(BrailleCell("z", "1356"))
        array.add(BrailleCell("1", "1")) //3456 will be added at a code level
        array.add(BrailleCell("2", "12"))
        array.add(BrailleCell("3", "14"))
        array.add(BrailleCell("4", "145"))
        array.add(BrailleCell("5", "15"))
        array.add(BrailleCell("6", "124"))
        array.add(BrailleCell("7", "1245"))
        array.add(BrailleCell("8", "125"))
        array.add(BrailleCell("9", "24"))
        array.add(BrailleCell("0", "245"))
        array.add(BrailleCell(",", "2"))
        array.add(BrailleCell(";", "23"))
        array.add(BrailleCell(":", "25"))
        array.add(BrailleCell("?", "26"))
        array.add(BrailleCell("!", "235"))
        array.add(BrailleCell("-", "36"))

        //Dunno why  we doing it this way but we keep it like this for now
        for (brailleCell in array) {
            alphabetToBrailleDictionary[brailleCell.english] = brailleCell.brailleDots
        }
    }

    fun getNextIndexForBrailleTraversal(brailleStringLength: Int, currentIndex : Int, isDirectionHorizontal : Boolean) : Int {
        if (isDirectionHorizontal == false) {
            if (brailleStringLength > 10) {
                return brailleIndexOrderForNumbersVerticalReading[currentIndex] ?: -1
            }
            else {
                return brailleIndexOrderForVerticalReading[currentIndex] ?: -1
            }
        }
        else {
            if (brailleStringLength > 10) {
                return brailleIndexOrderForNumbersHorizontalReading[currentIndex] ?: -1
            }
            else {
                return brailleIndexOrderForHorizontalReading[currentIndex] ?: -1
            }
        }
    }

    fun isMidpointReachedForNumber(brailleStringLength: Int, brailleStringIndexForNextItem: Int) : Boolean {
        if (brailleStringIndexForNextItem < 0) {
            return false
        }
        if (brailleStringLength > 10) {
            val brailleGridNumberForNextItem = mappingBrailleGridNumbersToStringIndex.filter { brailleStringIndexForNextItem == it.value }.keys.first()

            if (brailleGridNumberForNextItem == 7) {
                return true
            }
        }
        return false
    }

    fun isEndpointReaached(brailleStringLength: Int, brailleStringIndexForNextItem: Int) : Boolean {
        if (brailleStringLength > 10) {
            val brailleGridNumberForNextItem = mappingBrailleGridNumbersToStringIndex.filter { brailleStringIndexForNextItem == it.value }.keys.first()

            if (brailleGridNumberForNextItem == 13) {
                return true
            }
        }
        else {
            val brailleGridNumberForNextItem = mappingBrailleGridToStringIndex.filter { brailleStringIndexForNextItem == it.value }.keys.first()

            if (brailleGridNumberForNextItem == 7) {
                return true
            }
        }
        return false
    }

    fun convertAlphanumericToBraille(alphanumericString : String) : ArrayList<String>? {
        val brailleStringArray : ArrayList<String> = ArrayList()
        var brailleCharacterString = ""
        var index = -1
        for (character in alphanumericString) {
            index++
            var brailleDotsString : String? = alphabetToBrailleDictionary[(Character.toString(character)).lowercase()]
            if (brailleDotsString == null) {
                return null
            }
            if (character.isUpperCase()) { brailleDotsString = "6 " + brailleDotsString }
            if ((alphanumericString.toIntOrNull() != null && index == 0)
                || (alphanumericString.toIntOrNull() == null && character.isDigit())) {
                    brailleDotsString = "3456 " + brailleDotsString
            }
            val brailleDotsArray = brailleDotsString.split("\\s".toRegex()).toTypedArray() //if its for a number its 2 braille grids
            if (brailleDotsArray.size > 1) {
                //means its a number, and it needs 2 braille grids
                brailleCharacterString = "xx xx\nxx xx\nxx xx"
            }
            else {
                brailleCharacterString = "xx\nxx\nxx"
            }
            if (brailleDotsArray.size > 1) {
                for (number in brailleDotsArray[0]) {
                    val numberAsInt = number.digitToInt()
                    val index : Int = mappingBrailleGridNumbersToStringIndex[numberAsInt]!!
                    val chars = brailleCharacterString.toCharArray()
                    chars[index] = 'o'
                    brailleCharacterString = String(chars)
                }
                for (number in brailleDotsArray[1]) {
                    val numberAsInt = number.digitToInt()
                    val index : Int = mappingBrailleGridNumbersToStringIndex[numberAsInt + 6]!!
                    val chars = brailleCharacterString.toCharArray()
                    chars[index] = 'o'
                    brailleCharacterString = String(chars)
                }
            }
            else {
                for (number in brailleDotsArray[0]) {
                    val numberAsInt = number.digitToInt()
                    val index : Int = mappingBrailleGridToStringIndex[numberAsInt]!!
                    val chars = brailleCharacterString.toCharArray()
                    chars[index] = 'o'
                    brailleCharacterString = String(chars)
                }
            }
            brailleStringArray.add(brailleCharacterString)
        }

        return brailleStringArray
    }

    fun getIndexInStringOfLastCharacterInTheGrid(brailleStringForCharacter: String) : Int {
        if (brailleStringForCharacter.length >= 10) {
            return 11 //as per brailleIndexOrderForVerticalReading and brailleIndexOrderForHorizontalReading, this is the key of the last elemment in the grid
        }
        else {
            return 5 //as per brailleIndexOrderForVerticalReading and brailleIndexOrderForHorizontalReading, this is the key of the last elemment in the grid
        }
    }

}