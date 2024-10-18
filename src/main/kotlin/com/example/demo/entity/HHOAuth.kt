package com.example.demo.entity

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor


@Entity
@Table(name = "hhOAuth")
@NoArgsConstructor
@AllArgsConstructor
class HHOAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(unique = true, nullable = false)
    var userId: Long = 0

    @Column(unique = true)
    var token: String? = null

    @Column(unique = true)
    var access_token: String?=null

    @Column(unique = true)
    var refresh_token: String?=null

    @Column(unique = true)
    var resumeId: String = ""

    var expiresIn:Int? = null
}