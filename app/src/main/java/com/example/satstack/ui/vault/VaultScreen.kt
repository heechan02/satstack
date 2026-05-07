package com.example.satstack.ui.vault

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.satstack.R
import kotlinx.coroutines.delay

// androidx.biometric:biometric
@Composable
fun VaultScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val executor = ContextCompat.getMainExecutor(context)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onAuthSuccess()
        }
    }

    val prompt = remember { BiometricPrompt(activity, executor, callback) }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sat Stack")
            .setSubtitle("Authenticate to access your stack")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    LaunchedEffect(Unit) {
        delay(2000)
        prompt.authenticate(promptInfo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Sat Stack",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.satstack_logo),
                contentDescription = "Bitcoin Logo",
                modifier = Modifier.size(52.dp)
            )
        }

        Text(
            text = "Track your Bitcoin DCA Journey",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(0.8f))

        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = "Authenticate",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(90.dp)
                .clickable { prompt.authenticate(promptInfo) }
        )

        Spacer(modifier = Modifier.weight(0.4f))
    }
}
