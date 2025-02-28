package com.wlvpn.consumervpn.presentation.login.view.components

import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_TAB
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.tv.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors

@Composable
fun LoginTextField(
    modifier: Modifier,
    value: String,
    label: String,
    showError: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    onAction: () -> Unit,
    onValueChange: (String) -> Unit,
) {
      OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KEYCODE_ENTER ||
                        it.nativeKeyEvent.keyCode == KEYCODE_TAB
                    ) {
                        onAction()
                    }
                    false
                },
            value = value,
            isError = showError,
            trailingIcon = { trailingIcon() },
            label = { Text(label, color = LocalColors.current.scheme.onBackground) },
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedTextColor = LocalColors.current.scheme.onBackground,
                unfocusedTextColor = LocalColors.current.scheme.onBackground,

                unfocusedContainerColor = LocalColors.current.scheme.background,
                focusedContainerColor = LocalColors.current.scheme.background,

                errorContainerColor = LocalColors.current.scheme.background,

                cursorColor = LocalColors.current.scheme.onBackground,

                focusedIndicatorColor = LocalColors.current.scheme.onBackground,
                unfocusedIndicatorColor = LocalColors.current.scheme.onBackground,

                focusedPrefixColor = LocalColors.current.scheme.onBackground,
                unfocusedPrefixColor = LocalColors.current.scheme.onBackground,

                unfocusedLabelColor = LocalColors.current.scheme.onBackground,
                focusedLabelColor = LocalColors.current.scheme.onBackground,

                errorTextColor = LocalColors.current.scheme.onBackground
            ),
            onValueChange = { changedValue -> onValueChange(changedValue) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
              onNext = { onAction() },
              onPrevious = { onAction() }
            )
        )
}

