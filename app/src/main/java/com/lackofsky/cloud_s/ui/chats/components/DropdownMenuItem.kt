package com.lackofsky.cloud_s.ui.chats.components

import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.ui.chats.ChatDialogViewModel


@Composable
fun DropdownMenuItem(label:String, onCLick: ()->Boolean, @DrawableRes drawableIcon: Int){
    TextButton(
        onClick = {
            onCLick()
        },
        elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
        contentPadding = PaddingValues(
            horizontal = 16.dp, // Set horizontal padding to 20dp
            vertical = 5.dp // Set vertical padding to 10dp
        ), border = BorderStroke(1.dp, Color.Transparent),
        modifier = Modifier.background(Color.Transparent, RectangleShape)

    ) {
        Row(
        ) {
            Icon(
                painter = painterResource(id = drawableIcon),
                contentDescription = "Icon",
                modifier = Modifier.padding(4.dp)
                    //.height(height = 36.dp)//,
                //tint = Color(0xff1d1b20)
            )
            Text(
                label,
                fontSize = 18.sp, color = Color.Black
            )
        }
    }
}