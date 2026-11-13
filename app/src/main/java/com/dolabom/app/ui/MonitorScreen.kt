package com.dolabom.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolabom.app.core.Clock
import com.dolabom.app.core.DemoScenario
import com.dolabom.app.core.DemoScenarios
import com.dolabom.app.core.RiderType
import com.dolabom.app.core.Severity

/** 화면 2: 주행 중 뒷좌석 상태. 넓은 화면에서는 두 칸으로, 좁은 화면에서는 한 줄로 쌓아 보여 준다. */
@Composable
fun MonitorScreen(
    state: UiState,
    onEndTrip: () -> Unit,
    onScenario: (DemoScenario) -> Unit,
    onAdjustTemp: (Float) -> Unit,
    onToggleParked: () -> Unit,
    onToggleDriver: () -> Unit,
    onToggleRearDoor: () -> Unit,
    onSkipMinute: () -> Unit,
    onToggleVoice: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 900.dp

        if (wide) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TopBar(state = state, onEndTrip = onEndTrip)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // 왼쪽: 지금 상태와 기록. 오른쪽: 차량 신호와 시연 패널.
                    // 화면 높이가 모자라면 칸마다 따로 스크롤된다.
                    Column(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        StatusPanel(state = state, modifier = Modifier.fillMaxWidth())
                        LogPanel(state = state)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SignalsPanel(state = state)
                        DemoPanel(
                            state = state,
                            onScenario = onScenario,
                            onAdjustTemp = onAdjustTemp,
                            onToggleParked = onToggleParked,
                            onToggleDriver = onToggleDriver,
                            onToggleRearDoor = onToggleRearDoor,
                            onSkipMinute = onSkipMinute,
                            onToggleVoice = onToggleVoice,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TopBar(state = state, onEndTrip = onEndTrip)
                StatusPanel(state = state, modifier = Modifier.fillMaxWidth())
                SignalsPanel(state = state)
                DemoPanel(
                    state = state,
                    onScenario = onScenario,
                    onAdjustTemp = onAdjustTemp,
                    onToggleParked = onToggleParked,
                    onToggleDriver = onToggleDriver,
                    onToggleRearDoor = onToggleRearDoor,
                    onSkipMinute = onSkipMinute,
                    onToggleVoice = onToggleVoice,
                )
                LogPanel(state = state)
            }
        }
    }
}

@Composable
private fun TopBar(state: UiState, onEndTrip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "돌아봄",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        val rider = state.rider
        if (rider != null) {
            Text(
                text = "뒷좌석 · ${riderTypeLabel(rider.type)} · ${rider.name}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onEndTrip, modifier = Modifier.heightIn(min = 56.dp)) {
            Text(text = "주행 종료", fontSize = 18.sp)
        }
    }
}

/** 지금 상태를 가장 크게 보여 준다. 운전 중에 한눈에 읽히도록 한 문장만 강조한다. */
@Composable
private fun StatusPanel(state: UiState, modifier: Modifier = Modifier) {
    val assessment = state.assessment
    val severity = assessment?.severity ?: Severity.INFO
    val accent = severityColor(severity)

    Panel(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, accent),
    ) {
        Text(
            text = severityLabel(severity),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
        Text(
            text = assessment?.headline ?: "확인하는 중이에요",
            fontSize = 46.sp,
            lineHeight = 56.sp,
            fontWeight = FontWeight.Bold,
        )
        // 편안하거나 잠든 상태는 한 문장이면 충분하다. 살펴봐야 할 때만 설명을 덧붙인다.
        if (assessment != null && assessment.severity >= Severity.NOTICE) {
            Text(
                text = assessment.spoken,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val observation = state.observation
        val riderType = state.rider?.type
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValueTile(label = "소리", value = soundLabel(observation.sound), modifier = Modifier.weight(1f))
            ValueTile(label = "움직임", value = motionLabel(observation.motion), modifier = Modifier.weight(1f))
            if (riderType == RiderType.CHILD) {
                ValueTile(
                    label = "안전띠",
                    value = if (observation.beltFastened) "맴" else "풀림",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val waypoint = state.waypoint
        if (waypoint != null) {
            Text(
                text = "경유지 요청: ${waypoint.name} (${waypoint.detail})\n" +
                    "내비게이션 연동 전이라 요청만 기록했어요.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun SignalsPanel(state: UiState) {
    val vehicle = state.vehicle
    Panel(modifier = Modifier.fillMaxWidth()) {
        Text(text = "차량 신호", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValueTile(
                label = "주행",
                value = if (vehicle.parked) "주차" else "주행 중",
                modifier = Modifier.weight(1f),
            )
            ValueTile(
                label = "운전자",
                value = if (vehicle.driverPresent) "탑승" else "하차",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValueTile(label = "뒷문", value = doorLabel(vehicle.rearDoorOpen), modifier = Modifier.weight(1f))
            ValueTile(
                label = "실내 온도",
                value = temperatureLabel(vehicle.cabinTempC),
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = "출처: ${state.vehicleSourceLabel} · ${state.vehicleStatus}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ValueTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LogPanel(state: UiState) {
    Panel(modifier = Modifier.fillMaxWidth()) {
        Text(text = "기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (state.log.isEmpty()) {
            Text(
                text = "아직 기록이 없어요.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        for (entry in state.log.take(VISIBLE_LOG_ENTRIES)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = entry.clock,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = entry.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/** 시연 패널: 실제 인식이 붙기 전까지 상황을 손으로 바꿔 흐름을 보여 준다. */
@Composable
private fun DemoPanel(
    state: UiState,
    onScenario: (DemoScenario) -> Unit,
    onAdjustTemp: (Float) -> Unit,
    onToggleParked: () -> Unit,
    onToggleDriver: () -> Unit,
    onToggleRearDoor: () -> Unit,
    onSkipMinute: () -> Unit,
    onToggleVoice: () -> Unit,
) {
    val riderType = state.rider?.type ?: state.riderType
    val vehicle = state.vehicle

    Panel(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text = "시연 패널 · 가짜 데이터로 상황을 바꿉니다",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (scenario in DemoScenarios.available(riderType)) {
                FilledTonalButton(
                    onClick = { onScenario(scenario) },
                    modifier = Modifier.heightIn(min = 60.dp),
                ) {
                    Text(text = scenario.label, fontSize = 18.sp)
                }
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DemoControl(label = "온도 -1℃", onClick = { onAdjustTemp(-1f) })
            DemoControl(label = "온도 +1℃", onClick = { onAdjustTemp(1f) })
            DemoControl(label = if (vehicle.parked) "주행으로" else "주차로", onClick = onToggleParked)
            DemoControl(label = if (vehicle.driverPresent) "운전자 하차" else "운전자 복귀", onClick = onToggleDriver)
            DemoControl(label = if (vehicle.rearDoorOpen == true) "뒷문 닫기" else "뒷문 열기", onClick = onToggleRearDoor)
            DemoControl(label = "1분 건너뛰기", onClick = onSkipMinute)
            DemoControl(label = if (state.voiceOn) "음성 끄기" else "음성 켜기", onClick = onToggleVoice)
        }
        if (state.leftBehindSeconds > 0) {
            Text(
                text = "방치 시간 ${Clock.mmss(state.leftBehindSeconds.toLong())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DemoControl(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.heightIn(min = 60.dp)) {
        Text(text = label, fontSize = 16.sp)
    }
}

private const val VISIBLE_LOG_ENTRIES = 8
