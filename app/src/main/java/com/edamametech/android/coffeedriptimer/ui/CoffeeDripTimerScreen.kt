import android.graphics.drawable.PaintDrawable
import android.os.Handler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.getString
import com.edamametech.android.coffeedriptimer.R
import com.edamametech.android.coffeedriptimer.notifier.cancelTimerNotification
import com.edamametech.android.coffeedriptimer.notifier.scheduleTimerNotification
import com.edamametech.android.coffeedriptimer.notifier.TimerNotification
import com.edamametech.android.coffeedriptimer.ui.theme.CoffeeDripTimerTheme

enum class RoastOfBeans(val label: Int) {
    MEDIUM(R.string.medium_roast),
    DARK(R.string.dark_roast)
}

data class BrewStep (
    val waterAmountFactor: Double,
    val waitDurationFactor: Long
)

const val waitDurationUnit = 60000L
// in msec, TODO: 60000L (1 minute) for release or e.g. 5000L for debug

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

@Composable
fun CoffeeDripTimerScreen(modifier: Modifier = Modifier) {
    var amountOfBeans by rememberSaveable { mutableStateOf("10") }
    var roastOfBeans by rememberSaveable { mutableStateOf(RoastOfBeans.DARK) }
    var startedAt:Long? by rememberSaveable { mutableStateOf(null) }
    var currentAt:Long? by remember { mutableStateOf(null) }

    fun updateAmount(amount: String) {
        if (Regex("\\d*([.]\\d*)?").matches(amount)) {
            amountOfBeans = amount
        }
    }

    fun updateRoast(roast: RoastOfBeans) {
        roastOfBeans = roast
    }

    val handler = Handler()
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
        handler.postDelayed(updater, 1000)
    }

    fun cancelTimer() {
        startedAt = null
    }

    val context = LocalContext.current
    fun scheduleNotifications() {
        var notifyAt = startedAt ?: System.currentTimeMillis()
        var targetAmount = 0.0
        val beans = if (amountOfBeans.isNotEmpty()) {
            amountOfBeans.toDouble()
        } else {
            0.0
        }
        val nSteps = brewSteps[roastOfBeans]?.size ?: 0
        for ((i, s) in (brewSteps[roastOfBeans] ?: arrayOf<BrewStepTypes>()).withIndex()) {
            targetAmount += s.step.waterAmountFactor * beans
            val message = if (i < nSteps - 1) {
                String.format(
                    getString(context, R.string.timer_amount_format_wait),
                    targetAmount
                )
            } else {
                String.format(
                    getString(context, R.string.timer_amount_format_done),
                    targetAmount
                )
            }
            if (i == 0) {
                TimerNotification(context).showTimerNotification(message)
            } else {
                scheduleTimerNotification(context, notifyAt, i, message)
            }
            notifyAt += s.step.waitDurationFactor * waitDurationUnit
        }
    }

    fun cancelNotifications() {
        val nSteps = brewSteps[roastOfBeans]?.size ?: 0
        if (nSteps > 1) {
            cancelTimerNotification(context, 1, nSteps - 1)
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AmountOfBeansInput(
                amount = amountOfBeans,
                enabled = (startedAt == null),
                onValueChange = { updateAmount(it) },
                modifier = Modifier.weight(0.5F)
            )
            RoastOfBeansInput(
                roast = roastOfBeans,
                enabled = (startedAt == null),
                onValueChange = { updateRoast(it) },
                modifier = Modifier.weight(0.5F)
            )
        }
        BrewStepsDisplay(
            amount = amountOfBeans,
            roast = roastOfBeans,
            startedAt = startedAt,
            currentAt = currentAt,
            onComplete = { cancelTimer() }
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
    TextField(
        value = amount,
        enabled = enabled,
        leadingIcon = { Image(
            painterResource(R.drawable.beans),
            stringResource(R.string.coffee_beans),
            alpha = if (enabled) { DefaultAlpha } else { DefaultAlpha * 0.5F }
        ) },
        label = { Text(stringResource(R.string.amount_of_beans)) },
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
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
        Button(
            enabled = enabled,
            onClick = { expanded = true },
            content = {
                Text(stringResource(roast.label))
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (x in RoastOfBeans.entries) {
                if (x != roast) {
                    DropdownMenuItem(
                        text = { Text(stringResource(x.label)) },
                        onClick = {
                            onValueChange(x)
                            expanded = false
                        }
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
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var targetAmount = 0.0
    val beans = if (amount.isNotEmpty()) {
        amount.toDouble()
    } else {
        0.0
    }
    val nSteps = brewSteps[roast]?.size ?: 0
    var waitUntil = startedAt ?: 0
    Column {
        var remaining = 0L
        for ((i, s) in (brewSteps[roast] ?: arrayOf<BrewStepTypes>()).withIndex()) {
            targetAmount += s.step.waterAmountFactor * beans
            Row {
                Image(
                    painterResource(R.drawable.kettle),
                    stringResource(R.string.pour_hot_water)
                )
                Text(
                    text = String.format(stringResource(R.string.step_amount_format), targetAmount)
                )
            }
            if (i < nSteps - 1) {
                val stepWait = s.step.waitDurationFactor * waitDurationUnit
                remaining = if (startedAt == null || currentAt == null) {
                    stepWait
                } else if (currentAt < waitUntil) {
                    stepWait
                } else if (currentAt < waitUntil + stepWait) {
                    waitUntil + stepWait - currentAt
                } else {
                    0L
                }
                val min = (remaining + 500) / 1000 / 60
                val sec = ((remaining + 500) / 1000).rem(60)
                Text(
                    text = String.format(stringResource(R.string.step_wait_format), min, sec),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                waitUntil += stepWait
            }
        }
        if (remaining == 0L) {
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
        content = { Text(stringResource(R.string.start)) },
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
        content = { Text(stringResource(R.string.cancel)) },
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