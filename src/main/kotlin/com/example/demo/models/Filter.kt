package com.example.demo.models

import com.example.demo.models.requests.Request
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

    var experienceLevel:String? = null

    var text: String? = null

    var area: String?=null

    fun toQueryString(): String {
        val params = mutableListOf<String>()

        if(experienceLevel != null) params.add("experienceLevel=$experienceLevel")
        if(text != null) params.add("text=$text")
        if(area != null) params.add("area=$area")

        return if (params.isNotEmpty()) {
            params.joinToString("&")
        } else {
            ""
        }
    }
}