package `in`.kenslee.MultiModuleDiary.presentation.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.kenslee.MultiModuleDiary.R
import `in`.kenslee.MultiModuleDiary.presentation.components.GoogleButton

@Composable
fun AuthenticationContent(
    loadingState: Boolean,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(9f)
                .fillMaxWidth()
                .padding(40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(120.dp),
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google Logo"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.auth_welcome),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                text = stringResource(R.string.auth_please),
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
        }
        Column(
            modifier = Modifier.weight(weight = 2f).padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ){
            GoogleButton(
                onClick = onClick,
                loadingState = loadingState
            )
        }
    }
}