package com.nubip.agrobib_messenger.models

public class Message(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) {
    constructor() : this("", "", "", "", 0L)
}