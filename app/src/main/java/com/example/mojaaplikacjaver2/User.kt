package com.example.mojaaplikacjaver2

import android.os.Parcel
import android.os.Parcelable
import java.security.Timestamp

data class User(                                                                        // definiujemy class user, ktora przechowuje inforamcje o pojedynczym uzytkowinu (lista dawek)
    var dawki: List<Dawka>?=null
)
data class Dawka(                                                                       // clasa dawka, ktora definije przyjeta pojedyncza dawke
    var data:com.google. firebase.Timestamp? = null,                                // format zapisu daty jakiego uzywa firebase
    var dawka: Double=0.0
)
