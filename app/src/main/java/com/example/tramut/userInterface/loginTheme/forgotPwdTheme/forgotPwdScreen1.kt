package com.example.tramut.userInterface.loginTheme.forgotPwdTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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


@Composable
fun ForgetPasswordScreen1 (
    emailInput: String,
    errorMessage: String?,
    icInput: String,
    onIcInputChange: (String) -> Unit = {},
    onEmailInputChange: (String) -> Unit = {},
    onCancelForgetPwdClick: () -> Unit = {},
    onRequestPwdResetClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .background(Background)
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
            text = "Retrieve Password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Provide the email address linked with your account to reset your password",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))


        OutlinedTextField(
            value = emailInput,
            onValueChange = onEmailInputChange,
            placeholder = {
                Text(
                text = "Registered Email",
                    fontSize = 15.sp,
                    color = Color.Gray
                ) },
            modifier = Modifier
                .fillMaxWidth()
            ,shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = icInput,
            onValueChange = onIcInputChange,
            placeholder = {
                Text(
                    text = "NRIC",
                    fontSize = 15.sp,
                    color = Color.Gray
                ) },
            modifier = Modifier
                .fillMaxWidth()
            ,shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage ?: "",
            fontSize = 13.sp,
            color = if (errorMessage != null) Color.Red else Color.Gray
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = onRequestPwdResetClick,
            enabled = emailInput.isNotBlank() && icInput.isNotBlank(),
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                text = "Request Password Reset Link",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onCancelForgetPwdClick ,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text(
                text = "Cancel",
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

// 预览功能
@Preview(showBackground = true)
@Composable
fun PreviewForgetPasswordScreen() {
    ForgetPasswordScreen1(
        emailInput = "",
        icInput = "",
        errorMessage = null,
        onIcInputChange = {},
        onEmailInputChange = {},
        onCancelForgetPwdClick = {},
        onRequestPwdResetClick = {}
    )
}