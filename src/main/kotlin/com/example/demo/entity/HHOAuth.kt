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
    var access_token: String=""

    @Column(unique = true)
    var refresh_token: String = ""

    var expiresIn:Int? = null

    @ManyToMany
    @JoinTable(
        name = "oauth_responses",
        joinColumns = [JoinColumn(name = "oauth_id")],
        inverseJoinColumns = [JoinColumn(name = "response_id")]
    )
    var listResponses: Set<Responses> = HashSet()
}