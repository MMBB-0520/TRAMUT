package com.example.tramut.userInterface.loginTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.ui.theme.Purple40

@Composable
fun bottomChooseBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        Pair("ic_home0", "ic_home1"),
        Pair("ic_stu0", "ic_stu1"),
        Pair("ic_staff0", "ic_staff1"),
        Pair("ic_admin0", "ic_admin1")
    )
    val labels = listOf("Home", "Student", "Staff", "Admin")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, iconPair ->
            val isSelected = selectedIndex == index
            val iconRes = if (isSelected) iconPair.second else iconPair.first

            Column(
                modifier = Modifier
                    .clickable { onItemSelected(index) }
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = getDrawableId(iconRes)),
                    contentDescription = labels[index],
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = labels[index],
                    fontSize = 12.sp,
                    color = if (isSelected) Purple40 else MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun getDrawableId(name: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}

@Preview
@Composable
fun BottomChooseBarPreview() {
    bottomChooseBar(selectedIndex = 0, onItemSelected = {})
}
//