//
//package com.pccoer.skillmesh.data
//
//val categories = listOf("Programming", "Design", "Music", "Art")
//val subcategories = mapOf(
//    "Programming" to listOf("Web Development", "Mobile Development", "Data Science"),
//    "Design" to listOf("UI/UX Design", "Graphic Design", "Interior Design"),
//    "Music" to listOf("Vocal", "Instrumental", "Production"),
//    "Art" to listOf("Painting", "Sculpture", "Photography")
//)

package com.pccoer.skillmesh.data

data class SkillOffer(
    val title: String = "",
    val category: String = "",
    val subcategory: String = "",
    val description: String = ""
)

