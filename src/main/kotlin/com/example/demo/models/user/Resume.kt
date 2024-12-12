package com.example.demo.models.user

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
@Table(name = "resume")
@NoArgsConstructor
@AllArgsConstructor
class Resume (
        @Id
        val id: String = "",

        val title: String = "",

        @JsonIgnore
        @OneToOne
        @JoinColumn(name = "user_id")
        var user: User? = null
)
