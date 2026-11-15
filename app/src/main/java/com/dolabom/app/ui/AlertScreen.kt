package com.dolabom.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolabom.app.core.CareEngine
import com.dolabom.app.core.CareState
import com.dolabom.app.core.Clock
import com.dolabom.app.core.Place
import com.dolabom.app.core.Suggestions

/** 화면 3: 알림과 쉼터 제안. 왼쪽에 한 문장과 버튼, 오른쪽에 제안 장소 지도나 방치 단계를 둔다. */
@Composable
fun AlertScreen(
    state: UiState,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    val assessment = state.assessment ?: return
    val accent = severityColor(assessment.severity)
    val suggestion = assessment.suggestion
    val place = suggestion?.let { Suggestions.place(it) }
    val leftBehind = assessment.state == CareState.LEFT_BEHIND

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 900.dp
        if (wide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        AlertHeadline(state = state, accent = accent)
                        AlertMetrics(state = state, accent = accent)
                    }
                    AlertButtons(
                        acceptLabel = if (suggestion != null) Suggestions.acceptLabel(suggestion) else "확인했어요",
                        showDismiss = place != null,
                        onAccept = onAccept,
                        onDismiss = onDismiss,
                    )
                }
                Box(modifier = Modifier.weight(1.1f).fillMaxHeight()) {
                    AlertAside(
                        state = state,
                        place = place,
                        leftBehind = leftBehind,
                        suggestionTitle = suggestion?.let { Suggestions.title(it) },
                        accent = accent,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AlertHeadline(state = state, accent = accent)
                AlertMetrics(state = state, accent = accent)
                AlertAside(
                    state = state,
                    place = place,
                    leftBehind = leftBehind,
                    suggestionTitle = suggestion?.let { Suggestions.title(it) },
                    accent = accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )
                AlertButtons(
                    acceptLabel = if (suggestion != null) Suggestions.acceptLabel(suggestion) else "확인했어요",
                    showDismiss = place != null,
                    onAccept = onAccept,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun AlertHeadline(state: UiState, accent: Color) {
    val assessment = state.assessment ?: return
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        DotLabel(text = "${severityLabel(assessment.severity)} · 방금", dot = accent)
        Text(
            text = assessment.headline,
            fontSize = 52.sp,
            lineHeight = 62.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-1.2).sp,
        )
        if (assessment.spoken.isNotBlank()) {
            Text(text = assessment.spoken, fontSize = 18.sp, lineHeight = 29.sp, color = Ink.TextMuted)
        }
    }
}

@Composable
private fun AlertMetrics(state: UiState, accent: Color) {
    val observation = state.observation
    val sound = soundLabel(observation.sound)
    val motion = motionLabel(observation.motion)
    MetricsRow(
        items = listOf(
            Triple("소리", sound, if (sound == "조용함") Ink.Text else accent),
            Triple("움직임", motion, if (motion == "차분함" || motion == "잠듦") Ink.Text else accent),
            Triple("실내 온도", temperatureLabel(state.vehicle.cabinTempC), Ink.Text),
        ),
    )
}

@Composable
private fun AlertButtons(
    acceptLabel: String,
    showDismiss: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onAccept,
            shape = CircleShape,
            modifier = Modifier
                .weight(1.3f)
                .heightIn(min = 64.dp),
        ) {
            Text(text = acceptLabel, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        // 장소를 제안할 때만 거절 버튼을 둔다. 그 밖의 알림은 확인 버튼 하나로 닫는다.
        if (showDismiss) {
            GhostButton(
                label = "계속 갈게요",
                onClick = onDismiss,
                height = 64.dp,
                fontSize = 16,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** 오른쪽 칸: 제안 장소가 있으면 지도, 하차 후 방치면 단계, 그 밖에는 제안 한 줄. */
@Composable
private fun AlertAside(
    state: UiState,
    place: Place?,
    leftBehind: Boolean,
    suggestionTitle: String?,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Ink.Surface,
        border = BorderStroke(1.dp, Ink.Line),
        modifier = modifier,
    ) {
        when {
            place != null -> Box(modifier = Modifier.fillMaxSize()) {
                RouteMap(modifier = Modifier.fillMaxSize(), accent = Ink.Blue)
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(Ink.Background.copy(alpha = 0.88f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Eyebrow(text = "제안 · ${place.name}")
                    Text(text = suggestionTitle ?: place.name, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(text = place.detail, fontSize = 12.sp, color = Ink.TextMuted)
                }
            }

            leftBehind -> {
                val assessment = state.assessment
                LeftBehindSteps(
                    stage = assessment?.leftBehindStage ?: 0,
                    seconds = state.leftBehindSeconds,
                    cabinTemp = temperatureLabel(state.vehicle.cabinTempC),
                    accent = accent,
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Eyebrow(text = "제안")
                Text(
                    text = suggestionTitle ?: "확인해 주세요",
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/** 하차 후 방치 3단계. 지금 어느 단계인지와 다음 단계로 넘어가는 기준을 함께 보여 준다. */
@Composable
private fun LeftBehindSteps(stage: Int, seconds: Int, cabinTemp: String, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Eyebrow(text = "하차 후 방치")
        Text(
            text = "방치 ${Clock.mmss(seconds.toLong())} · 실내 온도 $cabinTemp",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
        StepRow(reached = stage >= 1, current = stage == 1, accent = accent, title = "1단계 · 운전자에게 알림", detail = "지금 이 화면과 음성으로 알렸어요.")
        StepRow(
            reached = stage >= 2, current = stage == 2, accent = accent,
            title = "2단계 · 가족에게 알림",
            detail = "${CareEngine.FAMILY_ALERT_AFTER_SEC}초 뒤 또는 ${CareEngine.HOT_CABIN_C.toInt()}℃ 이상 · 지금은 실제로 보내지 않아요",
        )
        StepRow(
            reached = stage >= 3, current = stage == 3, accent = accent,
            title = "3단계 · 119 신고 안내",
            detail = "${CareEngine.EMERGENCY_AFTER_SEC}초 뒤 또는 ${CareEngine.DANGER_CABIN_C.toInt()}℃ 이상 · 지금은 안내 문구만 보여 줘요",
        )
    }
}

@Composable
private fun StepRow(reached: Boolean, current: Boolean, accent: Color, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(8.dp)
                .background(if (reached) accent else Ink.LineBright, CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (current) FontWeight.Medium else FontWeight.Normal,
                color = if (reached) Ink.Text else Ink.TextMuted,
            )
            Text(text = detail, fontSize = 13.sp, lineHeight = 19.sp, color = Ink.TextMuted)
        }
    }
}
