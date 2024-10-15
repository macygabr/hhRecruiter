package com.example.demo.entity

import jakarta.persistence.*


@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    var email: String=""
    var name: String=""
    var access_token: String? = null
    var refresh_token: String? = null

    constructor()

    constructor(id: Long, email: String, name: String, access_token: String?, refresh_token: String?) {
        this.id = id
        this.email = email
        this.name = name
        this.access_token = access_token
        this.refresh_token = refresh_token
    }
}

