package com.example.demo.models

import com.fasterxml.jackson.databind.ObjectMapper
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

    var text: String? = null

    var experienceLevel: Experience? = null

    var area = 0

    fun readJson(json:String){
        val objectMapper = ObjectMapper()
        val f = objectMapper.readValue(json, Filter::class.java)
        this.area = f.area
        this.text = f.text
        this.experienceLevel = f.experienceLevel
    }

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

    override fun toString(): String {
        return "Filter(experienceLevel=$experienceLevel, text=$text, area=$area)"
    }
}