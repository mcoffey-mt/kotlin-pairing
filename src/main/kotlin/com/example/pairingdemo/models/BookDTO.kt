package com.example.pairingdemo.models

data class BookDTO(val id: String, val name: String, val type: String, val available: Boolean = false){
    fun toBook() = Book(id, name, type);
}