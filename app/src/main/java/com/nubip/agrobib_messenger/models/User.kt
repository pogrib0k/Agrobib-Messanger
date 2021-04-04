package com.nubip.agrobib_messenger.models

class User(val uuid: String, val username: String, val email: String, val profileImageUrl: String) {
    constructor() : this("", "", "", "")
}