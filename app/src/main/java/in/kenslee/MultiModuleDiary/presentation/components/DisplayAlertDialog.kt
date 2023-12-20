package `in`.kenslee.MultiModuleDiary.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DisplayAlertDialog(
    title: String,
    message: String,
    dialogOpened : Boolean,
    closeDialog: () -> Unit,
    onYesClicked: () -> Unit
) {
    if(dialogOpened){
        AlertDialog(
            title = {
                Text(
                    text = title,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    fontWeight = MaterialTheme.typography.titleSmall.fontWeight,
                )
            },
            onDismissRequest = closeDialog,
            confirmButton = {
                Button(onClick = {
                    onYesClicked()
                    closeDialog()
                }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    closeDialog()
                }) {
                    Text(text = "No")
                }
            }
        )
    }
}

@Preview
@Composable
fun DisplayAlertDialogPreview(){
    DisplayAlertDialog(
        title = "Material Dialog" ,
        message = "Do you really want to use material dialog?",
        dialogOpened = true,
        closeDialog = {},
        onYesClicked = {}
    )
}
