package com.dresta0056.free.ui.detail

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dresta0056.free.ui.theme.FreeTheme

@Composable
fun DetailScreen(modifier: Modifier = Modifier) {
    Text("Detail Screen")
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DetailScreenPreview() {
    FreeTheme {
        DetailScreen()
    }
}