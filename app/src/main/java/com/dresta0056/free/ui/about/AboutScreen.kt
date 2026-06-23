package com.dresta0056.free.ui.about

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dresta0056.free.ui.theme.FreeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("About Free") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "FREE",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Free is a frugal giveaway app for people who have unused things but do not want to throw them away.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "This app helps you let go of things you no longer need, so someone else can reuse them.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Free is not about shopping. Free is not about selling. Free is not about collecting more things. It is about using what already exists before buying more.",
                style = MaterialTheme.typography.bodyLarge
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ConductSection(
                title = "Do",
                items = listOf(
                    "Post items that are still usable.",
                    "Give clear descriptions.",
                    "Add honest item conditions.",
                    "Use respectful contact information.",
                    "Delete items that are no longer available."
                ),
                isPositive = true
            )

            ConductSection(
                title = "Don't",
                items = listOf(
                    "Sell items.",
                    "Ask for payment.",
                    "Post dangerous or illegal items.",
                    "Spam the feed.",
                    "Post fake listings.",
                    "Harass other users."
                ),
                isPositive = false
            )
        }
    }
}

@Composable
private fun ConductSection(
    title: String,
    items: List<String>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = if (isPositive) Icons.Filled.Check else Icons.Filled.Close
    val iconTint = if (isPositive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(2.dp))
        items.forEach { item ->
            Row {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconTint
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AboutScreenPreview() {
    FreeTheme {
        AboutScreen()
    }
}
