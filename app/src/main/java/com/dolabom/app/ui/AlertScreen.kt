package com.dolabom.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolabom.app.core.CareEngine
import com.dolabom.app.core.CareState
import com.dolabom.app.core.Clock
import com.dolabom.app.core.Suggestions

/** 화면 3: 알림과 쉼터 제안. 한 문장과 버튼 한두 개만 크게 보여 준다. */
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 56.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 설명은 길어지면 스크롤되고, 버튼은 항상 화면 아래에 보인다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = severityLabel(assessment.severity),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = assessment.headline,
                fontSize = 52.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = assessment.spoken,
                fontSize = 28.sp,
                lineHeight = 38.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (leftBehind) {
                LeftBehindSteps(
                    stage = assessment.leftBehindStage,
                    seconds = state.leftBehindSeconds,
                    cabinTemp = temperatureLabel(state.vehicle.cabinTempC),
                )
            } else if (suggestion != null) {
                Panel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Suggestions.title(suggestion),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (place != null) {
                        Text(
                            text = "${place.name} · ${place.detail}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .widthIn(min = 320.dp)
                    .heightIn(min = 96.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color(0xFF10151C),
                ),
            ) {
                Text(
                    text = if (suggestion != null) Suggestions.acceptLabel(suggestion) else "확인했어요",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            // 장소를 제안할 때만 거절 버튼을 둔다. 그 밖의 알림은 확인 버튼 하나로 닫는다.
            if (place != null) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .widthIn(min = 240.dp)
                        .heightIn(min = 96.dp),
                ) {
                    Text(text = "괜찮아요", fontSize = 28.sp)
                }
            }
        }
    }
}

/** 하차 후 방치 3단계. 지금 어느 단계인지와 다음 단계로 넘어가는 기준을 함께 보여 준다. */
@Composable
private fun LeftBehindSteps(stage: Int, seconds: Int, cabinTemp: String) {
    Panel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "방치 시간 ${Clock.mmss(seconds.toLong())} · 실내 온도 $cabinTemp",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        StepRow(
            reached = stage >= 1,
            text = "1단계 · 운전자에게 알림",
        )
        StepRow(
            reached = stage >= 2,
            text = "2단계 · 가족에게 알림 (${CareEngine.FAMILY_ALERT_AFTER_SEC}초 뒤 또는 " +
                "${CareEngine.HOT_CABIN_C.toInt()}℃ 이상) · 지금은 실제로 보내지 않아요",
        )
        StepRow(
            reached = stage >= 3,
            text = "3단계 · 119 신고 안내 (${CareEngine.EMERGENCY_AFTER_SEC}초 뒤 또는 " +
                "${CareEngine.DANGER_CABIN_C.toInt()}℃ 이상) · 지금은 안내 문구만 보여 줘요",
        )
    }
}

@Composable
private fun StepRow(reached: Boolean, text: String) {
    Text(
        text = (if (reached) "● " else "○ ") + text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
        color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
