package com.example.tramut.userInterface.studentTheme

object FacilityData {
    fun getCategoriesForDepartment(department: String): List<String> {
        return when (department) {
            "Sports" -> listOf(
                "Badminton", "Squash", "Gym",
                "Guest/Karaoke Room", "Swimming Pool", "Snooker",
                "Pickleball", "Table Tennis", "Tennis", "Futsal"
            )
            "Library" -> listOf(
                "Discussion Room",
                "Discussion Room with PC", "Individual Study Room"
            )
            "Cyber Center" -> listOf(
                "Discussion Room (1 PC)",
                "Discussion Room (2 PCs)", "Discussion Room with Projector (2 PCs)",
                "Discussion Room with Projector (2 PCs)[HDMI]"
            )
            else -> listOf("All $department Facilities")
        }
    }
}