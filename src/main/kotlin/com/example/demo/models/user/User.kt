package com.example.demo.models.user

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private var id: Long? = null

    @Column(name = "password", nullable = false)
    private var password: String? = null

    @Column(name = "email", unique = true, nullable = false)
    var username: String? = null
        private set

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private var role: Role? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    var filter: Filter? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    var hhAuthInfo: HhAuthInfo? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    var resume: Resume? = null

    override fun toString(): String {
        return try {
            ObjectMapper().writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Error during JSON serialization: " + e.message)
        }
    }
}