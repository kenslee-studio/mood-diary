package `in`.kenslee.write

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import `in`.kenslee.ui.components.DisplayAlertDialog
import `in`.kenslee.utils.model.Diary
import `in`.kenslee.utils.toInstant
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WriteTopBar(
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
    onBackPressed: () -> Unit,
    selectedDiary: Diary?,
    onDeleteConfirm: () -> Unit,
    moodName: () -> String
) {

    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()

    var dateTimeUpdated by remember{
        mutableStateOf(false)
    }

    var currentDate by remember { mutableStateOf(LocalDate.now())}
    var currentTime by remember { mutableStateOf(LocalTime.now())}
    val formattedDate = remember(currentDate){
        DateTimeFormatter
            .ofPattern("dd MMM yyyy,")
            .format(currentDate)
            .uppercase()
    }
    val formattedTime = remember(currentTime){
        DateTimeFormatter
            .ofPattern(" hh:mm a")
            .format(currentTime)
            .uppercase()
    }

    val selectedDiaryDateTime by remember (selectedDiary){
        derivedStateOf{
            selectedDiary?.let {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date.from(it.date.toInstant())).uppercase()
            } ?: ""
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = moodName(),
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if(selectedDiary != null && dateTimeUpdated){
                                formattedDate + formattedTime
                            }
                            else if(selectedDiary != null){
                                selectedDiaryDateTime
                            }else {
                                formattedDate + formattedTime
                            },
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onBackPressed()
                },
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back button"
                    )
                }
            )
        },
        actions = {
            if(dateTimeUpdated){
                IconButton(
                    onClick = {
                        currentDate = LocalDate.now()
                        currentTime = LocalTime.now()
                        dateTimeUpdated = false
                        onDateTimeUpdated(
                            ZonedDateTime.of(
                                currentDate ,
                                currentTime ,
                                ZoneId.systemDefault()
                            )
                        )
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "clear button"
                        )
                    }
                )

            }else{
                IconButton(
                    onClick = {
                        dateDialog.show()
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "date range button"
                        )
                    }
                )

            }

            selectedDiary?.let {
                DeleteDiaryAction(
                    selectedDiary = selectedDiary,
                    onDeleteClicked = onDeleteConfirm
                )
            }
        }
    )
    
    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date{localDate ->
            currentDate = localDate
            timeDialog.show()
        },
        config = CalendarConfig(monthSelection = true , yearSelection = true)
    )

    ClockDialog(
        state = timeDialog,
        selection = ClockSelection.HoursMinutes{ hours , minutes ->
            currentTime = LocalTime.of(hours , minutes)
            dateTimeUpdated = true
            onDateTimeUpdated(
                ZonedDateTime.of(
                    currentDate ,
                    currentTime ,
                    ZoneId.systemDefault()
                )
            )
        }
    )
}

@Composable
fun DeleteDiaryAction(
    selectedDiary: Diary?,
    onDeleteClicked: () -> Unit
) {
    var expanded by remember{ mutableStateOf(false) }
    var openDialog by remember{ mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
           expanded = false
        },
        content = {
            DropdownMenuItem(
                text = { Text(text = "Delete") },
                onClick = {
                    expanded = false
                    openDialog = true
                }
            )
        }
    )
    DisplayAlertDialog(
        title = "Delete",
        message = "Are you sure you want to permanently delete this diary note '${selectedDiary?.title}'.",
        dialogOpened = openDialog,
        closeDialog = {
            openDialog = false
        },
        onYesClicked = {
            onDeleteClicked()
        }
    )
    IconButton(
        onClick = {
            expanded = !expanded
        },
        content = {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource( `in`.kenslee.ui.R.string.vertical_more_button)
            )
        }
    )
}
