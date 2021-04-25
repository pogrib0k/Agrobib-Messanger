package com.nubip.agrobib_messenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uuid: String, val username: String, val email: String, val profileImageUrl: String) :
    Parcelable {
    constructor() : this("", "", "", "")
}