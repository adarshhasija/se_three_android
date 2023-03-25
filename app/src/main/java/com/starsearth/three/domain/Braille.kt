package com.starsearth.three.domain

import kotlinx.android.synthetic.main.fragment_action.*

class Braille {

    //Keeping all travsal related indexes here in case we have a wearable version in future
    var mArrayBrailleGridsForCharsInWord : java.util.ArrayList<BrailleCell> = java.util.ArrayList()
    var mArrayWordsInString : java.util.ArrayList<String> = java.util.ArrayList()
    var mArrayWordsInStringIndex : Int = 0
    var mArrayBrailleGridsForCharsInWordIndex = 0 //in the case of braille
    var mIndex = -1
    var mAlphanumericHighlightStartIndex = 0 //Cannot use braille grids array index as thats not a 1-1 relation

    var alphabetToBrailleDictionary : HashMap<String, String> = HashMap()

    fun reset() {
        resetIndexes()
        //tvMorseCode reset
        resetBrailleGridArray()
    }

    fun resetIndexes() {
        mArrayWordsInStringIndex = 0
        mArrayBrailleGridsForCharsInWordIndex = 0
        mIndex = -1
        mAlphanumericHighlightStartIndex = 0
    }

    fun resetBrailleGridArray() {
        mArrayBrailleGridsForCharsInWord.clear()
        mArrayBrailleGridsForCharsInWord.addAll(convertAlphanumericToBrailleWithContractions(mArrayWordsInString.first()))
    }

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
        var array: ArrayList<BrailleCell> = ArrayList() //only keeping these array adds to retain syntax so we dont have  to  type it again
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
        array.add(BrailleCell("#", "3456"))
        array.add(BrailleCell("^", "6"))
        array.add(BrailleCell(",", "2"))
        array.add(BrailleCell(";", "23"))
        array.add(BrailleCell(":", "25"))
        array.add(BrailleCell("?", "26"))
        array.add(BrailleCell("!", "235"))
        array.add(BrailleCell("-", "36"))
        array.add(BrailleCell("can", "14"))
        array.add(BrailleCell("ing", "346"))
        array.add(BrailleCell("com", "36"))

        //Dunno why  we doing it this way but we keep it like this for now
        for (brailleCell in array) {
            alphabetToBrailleDictionary[brailleCell.english] = brailleCell.brailleDots
        }
        array.clear() //Dont want it to take memory. It will be garbage collected anyway
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

    fun convertAlphanumericToBrailleWithContractions(alphanumericString : String) : ArrayList<BrailleCell> {
        val brailleFinalArray : ArrayList<BrailleCell> = ArrayList()
        var brailleCharacterString = ""
        var isFirstNumberSubstringPassed = false
        var substring = ""
        var brailleDotsString = ""
        var mainStringIndex = -1
        for (startIndex in 0 until alphanumericString.length) {
            mainStringIndex =
                if (mainStringIndex > startIndex) {
                    mainStringIndex
                }
                else {
                    startIndex
                }
            if (mainStringIndex >= alphanumericString.length) {
                break
            }
            for (endIndex in alphanumericString.lastIndex downTo mainStringIndex) {
                substring = alphanumericString.subSequence(mainStringIndex, endIndex+1).toString() //The endIndex is not inclusive in this function. We get a substring till the index before endIndex
                brailleDotsString = alphabetToBrailleDictionary[substring.lowercase()] ?: ""
                if (brailleDotsString.isBlank() == false) {
                    mainStringIndex += substring.length
                    break
                }
            }
            if (substring.length == 1) {
                if ((substring.first()).isUpperCase()) { brailleDotsString = alphabetToBrailleDictionary["^"] + " " + brailleDotsString }
                if ((substring.first()).isDigit() && isFirstNumberSubstringPassed == false) {
                    //standalone number OR first number in a sequence
                    brailleDotsString = alphabetToBrailleDictionary["#"] + " " + brailleDotsString
                    isFirstNumberSubstringPassed = true
                }
                else if ((substring.first()).isDigit() == false) {
                    //a letter or special character
                    isFirstNumberSubstringPassed = false
                }
            }
            else {
                //its a long string. a contraction. likely not a number
                isFirstNumberSubstringPassed = false
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
            brailleFinalArray.add(BrailleCell(substring, brailleCharacterString))
        }

        return brailleFinalArray
    }

    fun getIndexInStringOfLastCharacterInTheGrid(brailleStringForCharacter: String) : Int {
        if (brailleStringForCharacter.length >= 10) {
            return 11 //as per brailleIndexOrderForVerticalReading and brailleIndexOrderForHorizontalReading, this is the key of the last elemment in the grid
        }
        else {
            return 5 //as per brailleIndexOrderForVerticalReading and brailleIndexOrderForHorizontalReading, this is the key of the last elemment in the grid
        }
    }



    fun isEndOfEntireTextReached(brailleStringIndex: Int) : Boolean {
        if (mIndex > 0 && brailleStringIndex == -1 //This combination means we are looking at the ending of the braille grid, not the start
            && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)
            && mArrayWordsInStringIndex >= (mArrayWordsInString.size - 1)) {
            return true
        }
        return false
    }

    fun isPositionBehindText() : Boolean {
        if (mIndex < 0
            && mArrayBrailleGridsForCharsInWordIndex <= 0
            && mArrayWordsInStringIndex <= 0) {
            return true
        }
        return false
    }

    fun isStartOfWordReachedWhileMovingBack() : Boolean {
        if (mIndex <= -1
            && mArrayBrailleGridsForCharsInWordIndex <= 0) {
            return true
        }
        return false
    }

    fun isEndOfWordReachedWhileMovingForward(brailleStringIndex: Int) : Boolean {
        if (mIndex > -1 && brailleStringIndex == -1 //Indicates we are looking at the end of the array. index is 0 or more and brailleStringIndex is invalid
            && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)) {
            return true
        }
        return false
    }

    fun setIndexesForEndOfStringEndOfBrailleGrid(alphanumericString: String, brailleString: String) {
        mIndex = getIndexInStringOfLastCharacterInTheGrid(brailleString)
        mArrayBrailleGridsForCharsInWordIndex = mArrayBrailleGridsForCharsInWord.size - 1
        mArrayWordsInStringIndex = mArrayWordsInString.size - 1
        val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
        mAlphanumericHighlightStartIndex = alphanumericString.length - exactWord.length
    }

    fun goToPreviousWord(brailleString: String) : String {
        mArrayWordsInStringIndex--
        val alphanumericString = mArrayWordsInString[mArrayWordsInStringIndex]
        mArrayBrailleGridsForCharsInWord.clear()
        mArrayBrailleGridsForCharsInWord.addAll(convertAlphanumericToBrailleWithContractions(alphanumericString))
        mArrayBrailleGridsForCharsInWordIndex = mArrayBrailleGridsForCharsInWord.size - 1
        //tvAlphanumerics?.text = mInputText
        //tvBraille?.text = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].brailleDots
        //flashAlphanumericLabelChange()
        //flashBrailleGridChange()
        //val brailleString = tvBraille.text.toString()
        mIndex = getIndexInStringOfLastCharacterInTheGrid(brailleString)
        val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
        mAlphanumericHighlightStartIndex = alphanumericString.length - exactWord.length

        return alphanumericString
    }

    fun goToNextWord() : String {
        mArrayWordsInStringIndex++
        val alphanumericString = mArrayWordsInString[mArrayWordsInStringIndex]
        mArrayBrailleGridsForCharsInWordIndex = 0
        mArrayBrailleGridsForCharsInWord.clear()
        mArrayBrailleGridsForCharsInWord.addAll(convertAlphanumericToBrailleWithContractions(alphanumericString))
        mIndex = 0
        mAlphanumericHighlightStartIndex = 0

        return alphanumericString
    }

    fun goToPreviousCharacter(brailleString: String) {
        mArrayBrailleGridsForCharsInWordIndex--
        val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
        mAlphanumericHighlightStartIndex -= exactWord.length
        mIndex = getIndexInStringOfLastCharacterInTheGrid(brailleString)
    }

    fun goToNextCharacter() {
        val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
        mAlphanumericHighlightStartIndex += exactWord.length

        //move to next character
        mArrayBrailleGridsForCharsInWordIndex++
        mIndex = 0
        //tvBraille?.text = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].brailleDots
        //flashBrailleGridChange()
    }

    fun getStartAndEndIndexInFullStringOfHighlightedPortion() : HashMap<String, Any> {
        var text = ""
        var startIndexForHighlighting = 0
        var endIndexForHighlighting = 0
        for (word in mArrayWordsInString) {
            text += word
            text += " "
        }
        text = text.trim() //This is to trim the last space at the end of the last for loop above
        for (i in mArrayWordsInString.indices) {
            if (i < mArrayWordsInStringIndex) {
                startIndexForHighlighting += mArrayWordsInString[i].length //Need to increment by length of  the word that was completed
                startIndexForHighlighting++ //account for space after the word
            }
        }
        startIndexForHighlighting += mAlphanumericHighlightStartIndex //account for exactly where we are in the word
        val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
        endIndexForHighlighting = if (mIndex > -1) {
            //Means we have started traversing and there is something to highlight
            startIndexForHighlighting + exactWord.length
        }
        else {
            startIndexForHighlighting
        }

        val returnMap = HashMap<String, Any>()
        returnMap["text"] = text
        returnMap["start_index"] = startIndexForHighlighting
        returnMap["end_index"] = endIndexForHighlighting

        return returnMap
    }

}