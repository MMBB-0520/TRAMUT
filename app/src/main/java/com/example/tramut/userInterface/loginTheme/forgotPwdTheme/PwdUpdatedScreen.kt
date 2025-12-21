package com.example.tramut.userInterface.loginTheme.forgotPwdTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.BlueMain
import com.example.tramut.ui.theme.GrayText
import com.example.tramut.ui.theme.StaffRed

@Composable
fun PasswordUpdatedScreen(
    onStudentLoginClick: () -> Unit = {},
    onStaffLoginClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 34.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Image(
            painter = painterResource(id = R.drawable.tarumt),
            contentDescription = "Logo",
            modifier = Modifier
                .size(386.dp, 232.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Password Updated",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your password has been successfully updated.",
            fontSize = 13.sp,
            color = GrayText
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStudentLoginClick,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueMain)
        ) {
            Text(text = "Login as Student", color = Color.White, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
                onClick = onStaffLoginClick,
        modifier = Modifier
            .width(287.dp)
            .height(43.dp)
            .align(Alignment.CenterHorizontally),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = StaffRed)
        ) {
        Text(text = "Login as Staff", color = Color.White, fontSize = 14.sp)
    }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPasswordUpdatedScreen() {
    PasswordUpdatedScreen()
}

