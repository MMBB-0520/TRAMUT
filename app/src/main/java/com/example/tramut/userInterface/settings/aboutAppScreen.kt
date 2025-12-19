package com.example.tramut.userInterface.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
){
    Column(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.tarumt),
            contentDescription = "Logo",
            modifier = Modifier
                .size(386.dp, 232.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp)
        )
        Text(
            text = "TARCApp offers services with just a touch of your finger. It enables the public to access various services on the website. Students can enjoy fast access to personalised information and services in the students intranet.\n" +
                    "\n" +
                    "The app is developed and managed by Tunku Abdul Rahman University Of Management And Technology (TAR UMT), Communication and Information Technology Centre (CITC). Please contact us at citcweb@tarc.edu.my if you have any enquiries.",
            modifier = Modifier
                .padding(20.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "V2.0.24",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}


@Preview (showBackground = true, showSystemUi = true)
@Composable
fun P() {
    AboutAppScreen(
    )
}
