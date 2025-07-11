import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.edamametech.android.coffeedriptimer.R
import com.edamametech.android.coffeedriptimer.notifier.cancelTimerNotification
import com.edamametech.android.coffeedriptimer.notifier.scheduleTimerNotification
import com.edamametech.android.coffeedriptimer.notifier.TimerNotification
import com.edamametech.android.coffeedriptimer.ui.theme.CoffeeDripTimerTheme

enum class RoastOfBeans(val label: Int, val adjustWaterAmount: Double) {
    MEDIUM(R.string.medium_roast, 0.8),
    DARK(R.string.dark_roast, 1.0)
}

data class BrewStep (
    val waterAmountFactor: Double,
    val waitDurationFactor: Long
)

const val waitDurationUnit = 60000L
/* in msec, TODO: 60000L (1 minute) for release or e.g. 5000L for debug */
const val lastStepHighlightDuration = 5000L
/* 5 seconds */

enum class BrewStepTypes(val step: BrewStep) {
    SHORT(BrewStep(5.0, 2)),
    LONG(BrewStep(7.5, 3))
}

val brewSteps = mapOf(
    RoastOfBeans.MEDIUM to arrayOf(
        BrewStepTypes.SHORT,
        BrewStepTypes.LONG,
        BrewStepTypes.LONG
        ),
    RoastOfBeans.DARK to arrayOf(
        BrewStepTypes.SHORT,
        BrewStepTypes.SHORT,
        BrewStepTypes.SHORT,
        BrewStepTypes.SHORT
    )
)
const val maxBrewSteps = 4

fun toDoubleOrZero(input: String): Double {
    return if (input.isNotEmpty() && input != ".") {
        input.toDouble()
    } else {
        0.0
    }
}

@Composable
fun CoffeeDripTimerScreen() {
    var amountOfBeans by rememberSaveable { mutableStateOf("10") }
    var roastOfBeans by rememberSaveable { mutableStateOf(RoastOfBeans.DARK) }
    var startedAt:Long? by rememberSaveable { mutableStateOf(null) }
    var configurable:Boolean by rememberSaveable { mutableStateOf(true) }
    var currentAt:Long? by remember { mutableStateOf(null) }

    val handler = Handler(Looper.getMainLooper())
    val updater = object: Runnable {
        override fun run() {
            if (startedAt != null) {
                handler.postDelayed(this, 1000)
                updateCurrentAt()
            }
        }
        private fun updateCurrentAt() {
            currentAt = System.currentTimeMillis()
        }
    }
    fun startTimer() {
        startedAt = System.currentTimeMillis()
        handler.post(updater)
    }
    if (startedAt != null && currentAt == null) {
        // after a restart of the MainActivity
        handler.post(updater)
    }

    fun cancelTimer() {
        startedAt = null
        configurable = true
    }

    fun freezeConfiguration() {
        configurable = false
    }

    val context = LocalContext.current
    fun scheduleNotifications() {
        var notifyAt = startedAt ?: System.currentTimeMillis()
        var targetAmount = 0.0
        val beans = toDoubleOrZero(amountOfBeans) * roastOfBeans.adjustWaterAmount
        val nSteps = brewSteps[roastOfBeans]?.size ?: 0
        for ((i, s) in (brewSteps[roastOfBeans] ?: arrayOf<BrewStepTypes>()).withIndex()) {
            targetAmount += s.step.waterAmountFactor * beans
            if (notifyAt >= System.currentTimeMillis()) {
                val message = String.format(
                    getString(context,
                        if (i == 0) {
                            R.string.timer_amount_format_first
                        } else if (i < nSteps - 1) {
                            R.string.timer_amount_format_wait
                        } else {
                            R.string.timer_amount_format_done
                        }
                    ),
                    targetAmount
                )
                if (i == 0) {
                    TimerNotification(context).showTimerNotification(message)
                } else {
                    scheduleTimerNotification(context, notifyAt, i, message)
                }
            }
            notifyAt += s.step.waitDurationFactor * waitDurationUnit
        }
    }

    fun cancelNotifications() {
        cancelTimerNotification(context, 1, maxBrewSteps - 1)
    }

    fun updateAmount(amount: String) {
        amountOfBeans = amount
        if (startedAt != null) {
            cancelNotifications()
            scheduleNotifications()
        }
    }

    fun updateRoast(roast: RoastOfBeans) {
        roastOfBeans = roast
        if (startedAt != null) {
            cancelNotifications()
            scheduleNotifications()
        }
    }

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            AmountOfBeansInput(
                amount = amountOfBeans,
                enabled = configurable,
                onValueChange = { updateAmount(it) },
                modifier = Modifier.weight(0.5F)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RoastOfBeansInput(
                roast = roastOfBeans,
                enabled = configurable,
                onValueChange = { updateRoast(it) },
                modifier = Modifier.weight(0.5F)
            )
        }
        BrewStepsDisplay(
            amount = amountOfBeans,
            roast = roastOfBeans,
            startedAt = startedAt,
            currentAt = currentAt,
            configurable = configurable,
            freezeConfiguration = { freezeConfiguration() },
            onComplete = { cancelTimer() },
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1F)
        )
        Row {
            StartTimerButton(
                onClick = {
                    startTimer()
                    scheduleNotifications()
                          },
                enabled = (startedAt == null),
                modifier = Modifier.weight(0.75F)
            )
            Spacer(modifier = Modifier.width(4.dp))
            CancelTimerButton(
                onClick = {
                    cancelTimer()
                    cancelNotifications()
                          },
                enabled = (startedAt != null),
                modifier = Modifier.weight(0.25F)
            )
        }
    }
}

@Composable
fun AmountOfBeansInput(
    amount: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember { mutableStateOf(TextFieldValue(amount)) }

    OutlinedTextField(
        value = textValue,
        enabled = enabled,
        label = { Text(stringResource(R.string.amount_of_beans)) },
        leadingIcon = { Image(
            painterResource(R.drawable.beans),
            stringResource(R.string.coffee_beans),
            alpha = if (enabled) { DefaultAlpha } else { DefaultAlpha * 0.5F }
        ) },
        trailingIcon = { Text(
            stringResource(R.string.amount_unit),
            style = MaterialTheme.typography.bodyLarge
        )},
        onValueChange = {
            if (Regex("\\d*([.]\\d*)?").matches(it.text)) {
                textValue = it
                onValueChange(it.text)
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .onFocusChanged {
                if (it.isFocused) {
                    textValue = textValue.copy(
                        selection = TextRange(textValue.text.length)
                    )
                } else {
                    textValue = textValue.copy(
                        composition = null
                    )
                }
            }
    )
}

@Composable
fun RoastOfBeansInput(
    roast: RoastOfBeans,
    enabled: Boolean,
    onValueChange: (RoastOfBeans) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            enabled = enabled,
            onClick = { expanded = true },
            content = {
                Text(
                    stringResource(roast.label),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for (x in RoastOfBeans.entries) {
                if (x != roast) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(x.label),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onValueChange(x)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }

        }
    }
}

@Composable
fun BrewStepsDisplay(
    amount: String,
    roast: RoastOfBeans,
    startedAt: Long?,
    currentAt: Long?,
    configurable: Boolean,
    freezeConfiguration: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            Text(
                stringResource(R.string.pour_hot_water),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.5F)
            )
            Text(
                stringResource(R.string.step_wait),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Right,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.5F)
            )
        }
        var targetAmount = 0.0
        val beans = toDoubleOrZero(amount) * roast.adjustWaterAmount
        val nSteps = brewSteps[roast]?.size ?: 0
        var waitUntil = startedAt ?: 0
        var remaining = 0L
        for ((i, s) in (brewSteps[roast] ?: arrayOf<BrewStepTypes>()).withIndex()) {
            targetAmount += s.step.waterAmountFactor * beans

            var current = false
            val stepWait = s.step.waitDurationFactor * waitDurationUnit
            if (startedAt == null || currentAt == null) {
                remaining = stepWait
            } else if (currentAt < waitUntil) {
                remaining = stepWait
            } else if (currentAt < waitUntil + stepWait && i < nSteps - 1) {
                remaining = waitUntil + stepWait - currentAt
                current = true
            } else if (currentAt < waitUntil + lastStepHighlightDuration) {
                remaining = waitUntil + lastStepHighlightDuration - currentAt
                current = true
            } else {
                remaining = 0L
            }

            if (current && i > 0 && configurable) {
              freezeConfiguration()
            }

            val foregroundColor = if (!current) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.primary
            }
            val backgroundColor = if (!current) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(R.drawable.kettle),
                        stringResource(R.string.pour_hot_water)
                    )
                    Text(
                        text = " " + String.format(stringResource(R.string.step_amount_format), targetAmount),
                        color = foregroundColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (i < nSteps - 1) {
                    val min = (remaining + 500) / 1000 / 60
                    val sec = ((remaining + 500) / 1000).rem(60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ){
                        Icon(
                            painterResource(R.drawable.timer),
                            stringResource(R.string.step_wait)
                        )
                        Text(
                            text = " " + String.format(stringResource(R.string.step_wait_format), min, sec),
                            color = foregroundColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    waitUntil += stepWait
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ){
                        Icon(
                            painterResource(R.drawable.dripper),
                            stringResource(R.string.dripper)
                        )
                        Text(
                            text = " " + stringResource(R.string.drain),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

        }
        if (startedAt != null && remaining == 0L) {
            onComplete()
        }
    }
}

@Composable
fun StartTimerButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        content = {
            Text(
                stringResource(R.string.start),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}

@Composable
fun CancelTimerButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        content = {
            Text(
                stringResource(R.string.cancel),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}
@Preview(showBackground = true)
@Composable
fun CoffeeDripTimerScreenPreview() {
    CoffeeDripTimerTheme {
        CoffeeDripTimerScreen()
    }
}
