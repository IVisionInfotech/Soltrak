package com.soltrak.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import com.soltrak.R

@Composable
fun SupportScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current

    val phone = "+91 98984 58844"
    val email = "pulkit@bizorbit.co.in"
    val web = "www.BizOrbit.co.in"

    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.bizorbit_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(100.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                "BizOrbit Technologies",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "10/G, Bodycare Complex,\nNr. Supath 2 Complex,\nAshram Road, Ahmedabad - 380013,\nGujarat, India",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            ClickableText(
                text = AnnotatedString(
                    "Phone : $phone",
                    spanStyle = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ClickableText(
                text = AnnotatedString(
                    "Email : $email",
                    spanStyle = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$email")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ClickableText(
                text = AnnotatedString(
                    "Web : $web",
                    spanStyle = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                onClick = {
                    val url = "https://$web"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            )
        }
    }
}
