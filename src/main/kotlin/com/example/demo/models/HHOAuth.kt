package com.example.demo.models

import jakarta.persistence.*


@Entity
@Table(name = "hhOAuth")
data class HHOAuth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true, nullable = false)
    var userId: Long = 0,

    @Column(unique = true)
    var token: String? = null,

    @Column(unique = true)
    var access_token: String? = null,

    @Column(unique = true)
    var refresh_token: String? = null,

    var expiresIn: Int? = null,

    @Column(unique = true)
    var resumeId: String? = null,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "filter_id")
    var filter: Filter = Filter()

){
    fun toDTO() : String{
        return ""
    }

    override fun toString(): String {
        return "HHOAuth(resumeId=$resumeId, filter=$filter)"
    }

}
