package com.example.demo.models.vacancy

data class Vacancy(
    var id: String = "0",
    var salary: Salary? = null,
    var requirement: String = "",
    var name:String="",
    var schedule: Schedule = Schedule("",""),

    var has_test:Boolean = false,
    var response_letter_required: Boolean = false,

    var relations: List<String> = emptyList(),
    var type: String? = null,
    var description: String? = null,
    var url: String=""
){
    fun readItem(item: Map<*, *>) : Vacancy {
        id = item["id"] as? String ?: throw IllegalArgumentException("Id cannot be null")

        name = item["name"] as? String ?: throw IllegalArgumentException("name cannot be null")

        val scheduleMap = item["schedule"] as? Map<*, *>
        schedule = scheduleMap?.let {
            Schedule(
                id = it["id"] as? String ?: "",
                name = it["name"] as? String ?: ""
            )
        }!!


        val salaryMap = item["salary"] as? Map<*, *>
        salary = Salary(
            from = salaryMap?.get("from") as? Int,
            to = salaryMap?.get("to") as? Int,
            currency = salaryMap?.get("currency") as? String,
            gross = salaryMap?.get("gross") as? Boolean ?: false
        )

        val snippet = item["snippet"] as? Map<*, *>
        requirement = snippet?.get("requirement") as? String ?: ""

        has_test = item["has_test"] as? Boolean ?: false
        response_letter_required = item["response_letter_required"] as? Boolean ?: false
        relations = (item["relations"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        val typeMap = item["type"] as? Map<*, *>
        type = typeMap?.get("id") as? String ?: "Unknown"

        description = item["description"] as? String
        url = item["alternate_url"] as? String ?: throw IllegalArgumentException("URL cannot be null")
        return this
    }
}