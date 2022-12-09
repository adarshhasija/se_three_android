package com.starsearth.three.utils

import android.content.Context
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CustomVibrationPatternsUtils {

    companion object {

        fun getCurrentTimeInDotsDashes() : HashMap<String,Any> {
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
            var finalInstructionStringArray : MutableList<String> = mutableListOf()
            var i = 0
            while (i < hhDashes) {
                finalString += "-"
                finalInstructionStringArray.add("+5 Hours")
                i++
            }
            i = 0
            while (i < hhDots) {
                finalString += "."
                finalInstructionStringArray.add("+1 Hour")
                i++
            }
            finalString += "|"
            finalInstructionStringArray.add("= " + hh.toString() + " Hours")
            i = 0
            while (i < minsDashes) {
                finalString += "-"
                finalInstructionStringArray.add("+5 Minutes")
                i++
            }
            i = 0
            while (i < minDots) {
                finalString += "."
                finalInstructionStringArray.add("+1 Minute")
                i++
            }
            finalString += "|"
            finalInstructionStringArray.add("= " + mins.toString() + " Minutes")
            finalString += if (amPm == "PM") { "-" } else { "." }
            finalInstructionStringArray.add(amPm)
            finalString += "|"
            val minsString = if (mins < 10) {
                "0" + mins
            }
            else {
                mins
            }
            finalInstructionStringArray.add(hh.toString() + ":" + minsString.toString() + " " + amPm)
            var returnMap = HashMap<String, Any>()
            returnMap["FINAL_STRING"] = finalString
            returnMap["FINAL_INSTRUCTIONS"] = finalInstructionStringArray
            return returnMap
        }

        fun getDateAndDayInDotsDashes() : HashMap<String,Any> {
            val calendar = Calendar.getInstance()
            val date = calendar[Calendar.DATE]
            val dateDashes = date/5
            val dateDots = date - (dateDashes*5)
            val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
            var finalString = ""
            var finalInstructionStringArray : MutableList<String> = mutableListOf()
            var i = 0
            while (i < dateDashes) {
                finalString += "-"
                finalInstructionStringArray.add("+5 days")
                i++
            }
            i = 0
            while (i < dateDots) {
                finalString += "."
                finalInstructionStringArray.add("+1 day")
                i++
            }
            finalString += "|"
            finalInstructionStringArray.add("= "+date.toString())
            i = 0
            while (i < dayOfWeek) {
                finalString += "."
                if (i == 0) { finalInstructionStringArray.add("Sunday") }
                else if (i > 0) { finalInstructionStringArray.add("Sunday + "+i.toString()) }
                i++
            }
            finalString += "|"
            val dayOfWeekString = SimpleDateFormat("EEEE").format(date)
            finalInstructionStringArray.add("= " + dayOfWeekString)
            var returnMap = HashMap<String, Any>()
            returnMap["FINAL_STRING"] = finalString
            returnMap["FINAL_INSTRUCTIONS"] = finalInstructionStringArray
            return returnMap
        }

        fun getBatteryLevelInDotsAndDashes(context: Context) : HashMap<String,Any> {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val numDashes = batLevel/5
            val numDots = batLevel - (numDashes*5)
            var i = 0
            var finalString = ""
            var finalInstructionStringArray : MutableList<String> = mutableListOf()
            while (i < numDashes) {
                finalString += "-"
                finalInstructionStringArray.add("+ 5%")
                i++
            }
            i = 0
            while (i < numDots) {
                finalString += "."
                finalInstructionStringArray.add("+ 1%")
                i++
            }
            finalString += "|"
            finalInstructionStringArray.add("= " + batLevel.toString() + "%")
            var returnMap = HashMap<String, Any>()
            returnMap["FINAL_STRING"] = finalString
            returnMap["FINAL_INSTRUCTIONS"] = finalInstructionStringArray
            return returnMap
        }
    }
}