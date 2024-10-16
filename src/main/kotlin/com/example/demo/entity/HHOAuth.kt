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
    var userId: Long = 0
    var token: String? = null

    var access_token: String=""
    var refresh_token: String=""
}