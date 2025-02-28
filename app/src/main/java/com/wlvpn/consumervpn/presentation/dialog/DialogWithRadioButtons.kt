package com.wlvpn.consumervpn.presentation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogWithRadioButtons(
    title: String,
    cancelText: String,
    radioOptions: List<String>,
    initialSelectedValue: String,
    onOptionSelected: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    AlertDialog(
        title = {
            Text(text = title)
        },
        text = {
            RadioButtonGroup(radioOptions, initialSelectedValue, onOptionSelected, onDismissRequest)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(cancelText)
            }
        }
    )
}

@Composable
private fun RadioButtonGroup(
    radioOptions: List<String>,
    initialSelectedValue: String,
    onOptionSelected: (String) -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val (selectedOption) = remember { mutableStateOf(initialSelectedValue) }
// Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = {
                            onOptionSelected(text)
                            onDismissRequest() },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun DialogWithRadioButtonsPreview() {
    DialogWithRadioButtons("Title", "Cancel",
        listOf("Option 1", "Option 2"), initialSelectedValue = "Option2")
}
