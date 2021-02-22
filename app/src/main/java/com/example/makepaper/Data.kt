package com.example.makepaper

import android.content.Context

class MyPreference(context: Context){
    val PREFERENCE_NAME = "UserDetail"
    val UID = "userID"
    val NAME = "Name"
    val LOG_STATUS = "isLogged"
    val EMAIL  = "Email"
    val DBSTS  = "HasDB"
    val ACCTYPE = "AccountType"

    val PREFERENCE = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun setPreference(name: String, email: String, uid: String, isLogged: Boolean, accType: String, hasDB: Boolean = true) {
        val editor = PREFERENCE.edit()

        editor.putString(NAME, name)
        editor.putString(EMAIL, email)
        editor.putString(UID, uid)
        editor.putBoolean(LOG_STATUS, isLogged)
        editor.putString(ACCTYPE, accType)
        editor.putBoolean(DBSTS, hasDB)
        editor.apply()
    }

    fun logOut(){
        val editor = PREFERENCE.edit()

        editor.putString(EMAIL, null)
        editor.putString(UID, null)
        editor.putBoolean(LOG_STATUS, false)
        editor.putString(ACCTYPE, null)

        editor.apply()
    }

    fun getID(): String? {
        return PREFERENCE.getString(UID, "No User ID")
    }

    fun getName(): String? {
        return PREFERENCE.getString(NAME, "No Name")
    }

    fun getEmail() : String? {
        return PREFERENCE.getString(EMAIL, "")
    }

    fun getUserID(): String? {
        return PREFERENCE.getString(UID, "noID")
    }

    fun checkLogged(): Boolean{
        return PREFERENCE.getBoolean(LOG_STATUS, false)
    }

    fun checkDB() : Boolean{
        return PREFERENCE.getBoolean(DBSTS, false)
    }

    fun getAccountType(): String? {
        return PREFERENCE.getString(ACCTYPE, "No Acc")
    }
}
