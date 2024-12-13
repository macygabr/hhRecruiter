package com.example.demo.models.user

import com.fasterxml.jackson.annotation.JsonIgnore
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
    val id: Long = 0

    var text: String? = null

    @Enumerated(EnumType.STRING)
    var experience: Experience? = null

    var area: Int? = null

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    var user: User? = null

    fun readJson(json:String){
        val objectMapper = ObjectMapper()
        val f = objectMapper.readValue(json, Filter::class.java)
        this.area = f.area
        this.text = f.text
        this.experience = f.experience
    }

    fun toQueryString(): String {
        val params = mutableListOf<String>()

        if(experience != null) params.add("experience=$experience")
        params.add("search_field=name")
        if(text != null) params.add("text=$text")
        if(area != null) params.add("area=$area")
        return if (params.isNotEmpty()) {
            params.joinToString("&")
        } else {
            ""
        }
    }

    fun copyFrom(other: Filter): Filter {
        this.text = other.text
        this.experience = other.experience
        this.area = other.area
        return this
    }

    override fun toString(): String {
        return "Filter(experience=$experience, text=$text, area=$area)"
    }
}