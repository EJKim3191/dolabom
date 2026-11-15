package com.dolabom.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 차량 OS 공통 크롬. 왼쪽 세로 아이콘 레일과 얇은 상단 바를 두고, 그 안에 화면을 그린다.
 * 레일은 지금 어느 화면인지 보여 주기만 하고, 화면 이동은 상태가 정한다.
 */
@Composable
internal fun CarChrome(
    screen: Screen,
    state: UiState,
    onToggleVoice: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Rail(screen = screen)
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Ink.Line),
        )
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            TopBar(state = state, onToggleVoice = onToggleVoice)
            HorizontalDivider(color = Ink.Line)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun Rail(screen: Screen) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "돌아봄",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Ink.Text,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        RailItem(icon = Icons.Outlined.Person, label = "동승자", active = screen == Screen.SETUP)
        RailItem(icon = Icons.Outlined.Face, label = "뒷좌석 상태", active = screen == Screen.MONITOR)
        RailItem(icon = Icons.Outlined.Notifications, label = "알림", active = screen == Screen.ALERT)
        RailItem(icon = Icons.AutoMirrored.Outlined.List, label = "기록", active = false)
        Spacer(modifier = Modifier.weight(1f))
        RailItem(icon = Icons.Outlined.Settings, label = "설정", active = false)
    }
}

@Composable
private fun RailItem(icon: ImageVector, label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(if (active) Ink.SurfaceActive else Color.Transparent, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) Ink.Text else Ink.IconIdle,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun TopBar(state: UiState, onToggleVoice: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "돌아봄", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink.Text)
        val rider = state.rider
        if (rider != null) {
            Box(modifier = Modifier.width(1.dp).height(12.dp).background(Ink.LineStrong))
            Text(
                text = "뒷좌석 · ${riderTypeLabel(rider.type)} · ${rider.name}",
                fontSize = 13.sp,
                color = Ink.TextMuted,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = onToggleVoice,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.height(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (state.voiceOn) Ink.Green else Ink.LineBright, CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.voiceOn) "음성 안내 켬" else "음성 안내 끔",
                fontSize = 13.sp,
                color = if (state.voiceOn) Ink.Text else Ink.TextMuted,
            )
        }
        ClockText()
    }
}

/** 상단 바 시계. 차량 OS 공통 표시처럼 보이도록 10초마다 갱신한다. */
@Composable
private fun ClockText() {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(10_000L)
        }
    }
    Text(
        text = now.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)),
        fontSize = 13.sp,
        color = Ink.Text,
    )
}

/** 작은 글자 머리말. 자간을 넓혀 구역 이름으로 쓴다. */
@Composable
internal fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Ink.TextMuted) {
    Text(text = text, fontSize = 12.sp, letterSpacing = 1.5.sp, color = color, modifier = modifier)
}

/** 상태 점 하나와 글자. 색만으로 구분하지 않도록 글자를 함께 둔다. */
@Composable
internal fun DotLabel(text: String, dot: Color, modifier: Modifier = Modifier, textColor: Color = Ink.TextMuted) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(dot, CircleShape))
        Eyebrow(text = text, color = textColor)
    }
}
