package com.wlvpn.consumervpn.presentation.splittunneling

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun AppIcon(
    appName: String,
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Load icon asynchronously
    val appDrawable by produceState<Drawable?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.d(e, "No icon available for $packageName")
                null
            }
        }
    }

    if (appDrawable != null) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView ->
                imageView.setImageDrawable(appDrawable)
            }
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(LocalColors.current.scheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1).uppercase(),
                color = LocalColors.current.scheme.onPrimaryContainer,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
