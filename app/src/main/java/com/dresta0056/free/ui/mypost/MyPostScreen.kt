package com.dresta0056.free.ui.mypost

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dresta0056.free.ui.theme.FreeTheme

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Text("About Screen")
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AboutScreenPreview() {
    FreeTheme {
        AboutScreen()
    }
}