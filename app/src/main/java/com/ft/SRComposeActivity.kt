package com.ft

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ft.sdk.sessionreplay.ImagePrivacy
import com.ft.sdk.sessionreplay.TextAndInputPrivacy
import com.ft.sdk.sessionreplay.TouchPrivacy
import com.ft.sdk.sessionreplay.compose.sessionReplayHide
import com.ft.sdk.sessionreplay.compose.sessionReplayImagePrivacy
import com.ft.sdk.sessionreplay.compose.sessionReplayTextAndInputPrivacy
import com.ft.sdk.sessionreplay.compose.sessionReplayTouchPrivacy

class SRComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = javaClass.simpleName
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFFF5F1E8)) {
                    SessionReplayComposeScreen()
                }
            }
        }
    }
}

@Composable
private fun SessionReplayComposeScreen() {
    var maskedInput by remember { mutableStateOf("13800138000") }
    var visibleInput by remember { mutableStateOf("compose-demo@example.com") }
    var sliderValue by remember { mutableStateOf(0.35f) }
    var switchChecked by remember { mutableStateOf(true) }
    var checkboxChecked by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showSecret by remember { mutableStateOf(true) }
    val feedItems = remember {
        mutableStateListOf(
            "Foldable support validation",
            "Masked input semantics check",
            "Scrollable list capture check",
            "Touch privacy override check"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderCard()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Compose Session Replay Sandbox",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.sessionReplayTouchPrivacy(TouchPrivacy.SHOW)
        )
        Text(
            text = "Use this page to verify Compose text, inputs, toggles, lists and privacy overrides.",
            style = MaterialTheme.typography.body2,
            color = Color(0xFF5B5B5B),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.White,
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Privacy overrides", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = maskedInput,
                    onValueChange = { maskedInput = it },
                    label = { Text("Masked input") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .sessionReplayTextAndInputPrivacy(TextAndInputPrivacy.MASK_ALL)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = visibleInput,
                    onValueChange = { visibleInput = it },
                    label = { Text("Mask all inputs") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .sessionReplayTextAndInputPrivacy(TextAndInputPrivacy.MASK_ALL_INPUTS)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temporarily hide red badge")
                    Switch(
                        checked = showSecret,
                        onCheckedChange = { showSecret = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) { index ->
                val selected = selectedTab == index
                Button(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (selected) "Tab ${index + 1} On" else "Tab ${index + 1}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AndroidViewInteropCard()

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Controls", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Slider ${(sliderValue * 100).toInt()}%")
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    modifier = Modifier.sessionReplayTouchPrivacy(TouchPrivacy.SHOW)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkboxChecked,
                        onCheckedChange = { checkboxChecked = it }
                    )
                    Text("Checkbox")
                    Spacer(modifier = Modifier.size(16.dp))
                    RadioButton(
                        selected = switchChecked,
                        onClick = { switchChecked = true }
                    )
                    Text("Primary")
                    Spacer(modifier = Modifier.size(12.dp))
                    RadioButton(
                        selected = !switchChecked,
                        onClick = { switchChecked = false }
                    )
                    Text("Secondary")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Image and hidden node", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Compose test image",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFD7E9FF))
                            .padding(8.dp)
                            .sessionReplayImagePrivacy(ImagePrivacy.MASK_NONE)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Column {
                        Text("Launcher illustration")
                        Text(
                            text = "Image capture and clipping test",
                            style = MaterialTheme.typography.body2,
                            color = Color(0xFF5B5B5B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD83A2E))
                        .sessionReplayHide(!showSecret),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VIP", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lazy list capture", style = MaterialTheme.typography.subtitle1)
                    Button(onClick = { feedItems.add("Dynamic item ${feedItems.size + 1}") }) {
                        Text("Add")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    items(feedItems) { item ->
                        FeedRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun AndroidViewInteropCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "AndroidView interop", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(12.dp))
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                factory = { context ->
                    TextView(context).apply {
                        text = "Native TextView inside Compose"
                        gravity = Gravity.CENTER
                        textSize = 16f
                        setTextColor(AndroidColor.WHITE)
                        setBackgroundColor(AndroidColor.rgb(30, 91, 82))
                    }
                },
                update = {
                    it.text = "Native TextView inside Compose"
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This block verifies Session Replay AndroidView interop mapping.",
                style = MaterialTheme.typography.body2,
                color = Color(0xFF5B5B5B)
            )
        }
    }
}

@Composable
private fun HeaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .semantics { }
            .background(Color(0xFF1E5B52))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = "Session Replay Compose",
                style = MaterialTheme.typography.h5,
                color = Color.White
            )
            Text(
                text = "Visual capture playground for Compose semantics mapping",
                style = MaterialTheme.typography.body1,
                color = Color(0xFFF8F2E8)
            )
        }
    }
}

@Composable
private fun FeedRow(item: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .semantics { }
            .background(Color(0xFFF7F2EA))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .semantics { }
                .background(Color(0xFF1E5B52)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.take(1), color = Color.White)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(item)
            Text(
                text = "Compose row for replay verification",
                style = MaterialTheme.typography.body2,
                color = Color(0xFF6B665F)
            )
        }
    }
}
