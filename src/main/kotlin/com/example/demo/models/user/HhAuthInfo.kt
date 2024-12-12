package com.example.demo.models.user

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import lombok.Builder
import lombok.Data


@Entity
@Table(name = "hh_auth_info")
@Data
data class HhAuthInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true)
    var access_token: String? = null,

    @Column(unique = true)
    var refresh_token: String? = null,

    var expiresIn: Int? = null,

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    var user: User? = null
)
