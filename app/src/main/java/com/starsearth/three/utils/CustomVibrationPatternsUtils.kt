package com.starsearth.three.utils

import java.util.*

class CustomVibrationPatternsUtils {

    companion object {

        fun getCurrentTimeInDotsDashes() : String {
            val calendar = Calendar.getInstance()
            val hh =
                    if (calendar[Calendar.HOUR_OF_DAY] > 12) {
                        calendar[Calendar.HOUR_OF_DAY] - 12
                    }
                    else if (calendar[Calendar.HOUR_OF_DAY] == 0) {
                        12
                    }
                    else {
                        calendar[Calendar.HOUR_OF_DAY]
                    }
            val hhDashes = hh/5
            val hhDots = hh - (hhDashes*5)
            val mins = calendar[Calendar.MINUTE]
            val minsDashes = mins/5
            val minDots = mins - (minsDashes*5)
            val amPm =
                if (calendar[Calendar.HOUR_OF_DAY] >= 12) {
                    "PM"
                }
                else {
                    "AM"
                }
            var finalString = ""
            var i = 0
            while (i < hhDashes) {
                finalString += "-"
                i++
            }
            i = 0
            while (i < hhDots) {
                finalString += "."
                i++
            }
            finalString += "|"
            i = 0
            while (i < minsDashes) {
                finalString += "-"
                i++
            }
            i = 0
            while (i < minDots) {
                finalString += "."
                i++
            }
            finalString += "|"
            finalString += if (amPm == "PM") { "-" } else { "." }
            finalString += "|"
            return finalString
        }

        fun getDateAndDayInDotsDashes() : String {
            val calendar = Calendar.getInstance()
            val date = calendar[Calendar.DATE]
            val dateDashes = date/5
            val dateDots = date - (dateDashes*5)
            val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
            var finalString = ""
            var i = 0
            while (i < dateDashes) {
                finalString += "-"
                i++
            }
            i = 0
            while (i < dateDots) {
                finalString += "."
                i++
            }
            finalString += "|"
            i = 0
            while (i < dayOfWeek) {
                finalString += "."
                i++
            }
            finalString += "|"
            return finalString
        }
    }
}