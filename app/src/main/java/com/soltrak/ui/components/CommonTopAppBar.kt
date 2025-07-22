package com.soltrak.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.soltrak.ui.theme.PrimaryOrange
import com.soltrak.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onResetClick: (() -> Unit)? = null,
    menuContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current

    /*TopAppBar(
        title = {
            Text(title, color = TextWhite)
        },
        navigationIcon = {
            IconButton(onClick = {
                onBackClick?.invoke() ?: (context as? Activity)?.finish()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
        },
        actions = {
            if (onResetClick != null) {
                IconButton(onClick = onResetClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = TextWhite
                    )
                }
            }
            menuContent?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryOrange
        )
    )*/
}

