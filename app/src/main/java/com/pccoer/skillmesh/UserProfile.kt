package com.pccoer.skillmesh

data class UserProfile(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val location: String = "",
    val profilePictureUrl: String = "" // You might store a URL to the image in Firebase Storage
)