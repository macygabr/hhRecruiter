package com.example.demo.entity

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
@Table(name = "responses")
@NoArgsConstructor
@AllArgsConstructor
class Responses{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(unique = true, nullable = false)
    var vacancy_id:String =""

    @ManyToMany(mappedBy = "listResponses")
    var oauthUsers: Set<HHOAuth> = HashSet()
}
