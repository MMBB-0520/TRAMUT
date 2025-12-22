package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun UserReviewGuidelinesScreen(){
    Column(
        modifier = Modifier.padding(25.dp)
    ) {

        Text("• Reviews must be based on your own completed booking experience")
        Spacer(modifier = Modifier.height(10.dp))
        Text("• Do not include offensive, abusive, or inappropriate language")
        Spacer(modifier = Modifier.height(10.dp))
        Text("• Do not include personal or sensitive information")
        Spacer(modifier = Modifier.height(10.dp))
        Text("• All reviews are subject to administrative review and may be edited or removed if necessary")
    }
}
@Preview(showBackground = true)
@Composable
fun UserReviewGuidelinesScreenPreview(){
    UserReviewGuidelinesScreen()
}