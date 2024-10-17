package com.example.demo.entity

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor


@Entity
@Table(name = "filter")
@NoArgsConstructor
@AllArgsConstructor
class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var experienceLevel:String = ""

    var tags: Array<String> = emptyArray<String>()
}