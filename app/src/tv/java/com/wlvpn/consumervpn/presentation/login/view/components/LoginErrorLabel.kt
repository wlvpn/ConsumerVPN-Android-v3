package com.wlvpn.consumervpn.presentation.login.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens

@Composable
fun LoginErrorLabel(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    text: String,
    textAlign: TextAlign = TextAlign.Start
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Image(
            modifier = Modifier
                .width(LocalDimens.current.normal)
                .height(LocalDimens.current.normal),
            painter = painterResource(id = R.drawable.ic_warning),
            contentDescription = "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
        )

        Text(
            modifier = Modifier
                .padding(start = LocalDimens.current.xxSmall),
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = textAlign
        )
    }
}