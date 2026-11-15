package com.dolabom.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolabom.app.core.CareState
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
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 900.dp

        if (wide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 48.dp, top = 36.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                // 왼쪽: 지금 상태와 기록. 오른쪽: 차량과 시연 패널.
                // 화면 높이가 모자라면 칸마다 따로 스크롤된다.
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    StatusHeader(state = state)
                    ObservationRow(state = state)
                    WaypointNote(state = state)
                    LogList(state = state)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    VideoSlot()
                    VehiclePanel(state = state)
                    DemoPanel(
                        state = state,
                        onScenario = onScenario,
                        onAdjustTemp = onAdjustTemp,
                        onToggleParked = onToggleParked,
                        onToggleDriver = onToggleDriver,
                        onToggleRearDoor = onToggleRearDoor,
                        onSkipMinute = onSkipMinute,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        GhostButton(label = "주행 종료", onClick = onEndTrip, height = 48.dp, fontSize = 15)
                    }
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
                StatusHeader(state = state)
                ObservationRow(state = state)
                WaypointNote(state = state)
                VideoSlot()
                VehiclePanel(state = state)
                DemoPanel(
                    state = state,
                    onScenario = onScenario,
                    onAdjustTemp = onAdjustTemp,
                    onToggleParked = onToggleParked,
                    onToggleDriver = onToggleDriver,
                    onToggleRearDoor = onToggleRearDoor,
                    onSkipMinute = onSkipMinute,
                )
                LogList(state = state)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    GhostButton(label = "주행 종료", onClick = onEndTrip, height = 48.dp, fontSize = 15)
                }
            }
        }
    }
}

/** 지금 상태를 가장 크게 보여 준다. 운전 중에 한눈에 읽히도록 한 문장만 강조한다. */
@Composable
private fun StatusHeader(state: UiState) {
    val assessment = state.assessment
    val severity = assessment?.severity ?: Severity.INFO
    val accent = severityColor(severity)
    val ringValue = when (assessment?.state) {
        null -> "확인 중"
        CareState.COMFORTABLE -> "편안"
        CareState.ASLEEP -> "잠듦"
        CareState.RESTLESS -> "불안"
        CareState.MOTION_SICK -> "멀미"
        CareState.HOT -> "더움"
        CareState.BELT_UNFASTENED -> "안전띠"
        CareState.UNRESPONSIVE -> "무반응"
        CareState.LEFT_BEHIND -> "방치"
    }
    val ringProgress = if (assessment?.state == CareState.LEFT_BEHIND) {
        (state.leftBehindSeconds / 180f).coerceIn(0.05f, 1f)
    } else {
        1f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        StatusRing(color = accent, label = "지금 상태", value = ringValue, progress = ringProgress)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DotLabel(text = severityLabel(severity), dot = accent)
            Text(
                text = assessment?.headline ?: "확인하는 중이에요",
                fontSize = 52.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-1.2).sp,
            )
            // 편안하거나 잠든 상태는 한 문장이면 충분하다. 살펴봐야 할 때만 설명을 덧붙인다.
            val detail = when {
                assessment == null -> "뒷좌석 인식과 차량 신호를 모으고 있어요."
                assessment.severity >= Severity.NOTICE && assessment.spoken.isNotBlank() -> assessment.spoken
                else -> "특별히 살펴볼 게 없어요. 계속 지켜볼게요."
            }
            Text(text = detail, fontSize = 17.sp, lineHeight = 27.sp, color = Ink.TextMuted)
        }
    }
}

@Composable
private fun ObservationRow(state: UiState) {
    val observation = state.observation
    val items = mutableListOf(
        Triple("소리", soundLabel(observation.sound), Ink.Text),
        Triple("움직임", motionLabel(observation.motion), Ink.Text),
    )
    if (state.rider?.type == RiderType.CHILD) {
        items += Triple(
            "안전띠",
            if (observation.beltFastened) "매임" else "풀림",
            if (observation.beltFastened) Ink.Text else Ink.Orange,
        )
    }
    MetricsRow(items = items)
}

@Composable
private fun WaypointNote(state: UiState) {
    val waypoint = state.waypoint ?: return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink.Blue.copy(alpha = 0.10f), CircleShape)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(Ink.Blue, CircleShape))
        Text(
            text = "경유지 요청 · ${waypoint.name} (${waypoint.detail}) · 내비게이션 연동 전이라 요청만 기록했어요.",
            fontSize = 14.sp,
            color = Ink.TextSoft,
        )
    }
}

@Composable
private fun LogList(state: UiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow(text = "기록")
        if (state.log.isEmpty()) {
            Text(text = "아직 기록이 없어요.", fontSize = 15.sp, color = Ink.TextMuted)
        }
        state.log.take(VISIBLE_LOG_ENTRIES).forEachIndexed { index, entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = entry.clock,
                    fontSize = 15.sp,
                    color = Ink.TextMuted,
                    modifier = Modifier.width(48.dp),
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (index == 0) Ink.Text else Ink.LineBright, CircleShape),
                )
                Text(
                    text = entry.text,
                    fontSize = 15.sp,
                    color = if (index == 0) Ink.TextSoft else Ink.TextMuted,
                )
            }
        }
    }
}

/**
 * 뒷좌석 영상 자리. 핸드폰 앱을 뒷좌석 카메라로 연결하면 여기에 실시간 영상이 들어온다.
 * 아직 연결 기능이 없어 자리만 잡아 둔다.
 */
@Composable
private fun VideoSlot() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .dashedBorder(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DotLabel(text = "뒷좌석 영상 · 폰 카메라 연결 전", dot = Ink.LineBright)
            Text(
                text = "핸드폰 앱을 뒷좌석 카메라로 연결하면 여기에 실시간 영상이 보여요.",
                fontSize = 13.sp,
                color = Ink.TextMuted,
            )
        }
    }
}

private fun Modifier.dashedBorder(): Modifier = this.then(
    Modifier.drawBehind {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
        )
        drawRoundRect(
            color = Ink.LineStrong,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
            style = stroke,
        )
    },
)

/** 좌석 도식과 차량 신호 목록. */
@Composable
private fun VehiclePanel(state: UiState) {
    val vehicle = state.vehicle
    val assessment = state.assessment
    val accent = severityColor(assessment?.severity ?: Severity.INFO)

    Panel(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            SeatDiagram(
                modifier = Modifier
                    .width(120.dp)
                    .height(184.dp),
                highlight = accent,
                driverPresent = vehicle.driverPresent,
                rearOccupied = state.observation.occupantPresent,
            )
            Column(modifier = Modifier.weight(1f)) {
                SignalRow(label = "주행", value = if (vehicle.parked) "주차" else "주행 중")
                HorizontalDivider(color = Ink.Line)
                SignalRow(label = "운전자", value = if (vehicle.driverPresent) "탑승" else "하차")
                HorizontalDivider(color = Ink.Line)
                SignalRow(
                    label = "뒷문",
                    value = doorLabel(vehicle.rearDoorOpen),
                    valueColor = if (vehicle.rearDoorOpen == true) Ink.Amber else Ink.Text,
                )
                HorizontalDivider(color = Ink.Line)
                SignalRow(label = "실내 온도", value = temperatureLabel(vehicle.cabinTempC))
                Spacer(modifier = Modifier.height(14.dp))
                DotLabel(text = "${state.vehicleSourceLabel} · ${state.vehicleStatus}", dot = Ink.Green)
            }
        }
    }
}

@Composable
private fun SignalRow(label: String, value: String, valueColor: Color = Ink.Text) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 15.sp, color = Ink.TextMuted)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

/** 시연 패널: 실제 인식이 붙기 전까지 상황을 손으로 바꿔 흐름을 보여 준다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DemoPanel(
    state: UiState,
    onScenario: (DemoScenario) -> Unit,
    onAdjustTemp: (Float) -> Unit,
    onToggleParked: () -> Unit,
    onToggleDriver: () -> Unit,
    onToggleRearDoor: () -> Unit,
    onSkipMinute: () -> Unit,
) {
    val riderType = state.rider?.type ?: state.riderType
    val vehicle = state.vehicle

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow(text = "시연 패널 · 가짜 데이터로 상황을 바꿉니다")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (scenario in DemoScenarios.available(riderType)) {
                GhostButton(label = scenario.label, onClick = { onScenario(scenario) })
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GhostButton(label = "온도 −1℃", onClick = { onAdjustTemp(-1f) }, muted = true)
            GhostButton(label = "온도 +1℃", onClick = { onAdjustTemp(1f) }, muted = true)
            GhostButton(label = if (vehicle.parked) "주행으로" else "주차로", onClick = onToggleParked, muted = true)
            GhostButton(
                label = if (vehicle.driverPresent) "운전자 하차" else "운전자 복귀",
                onClick = onToggleDriver,
                muted = true,
            )
            GhostButton(
                label = if (vehicle.rearDoorOpen == true) "뒷문 닫기" else "뒷문 열기",
                onClick = onToggleRearDoor,
                muted = true,
            )
            GhostButton(label = "1분 건너뛰기", onClick = onSkipMinute, muted = true)
        }
        if (state.leftBehindSeconds > 0) {
            Text(
                text = "방치 시간 ${Clock.mmss(state.leftBehindSeconds.toLong())}",
                fontSize = 13.sp,
                color = Ink.TextMuted,
            )
        }
    }
}

/** 1dp 선으로만 그린 알약 버튼. */
@Composable
internal fun GhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 44.dp,
    fontSize: Int = 14,
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, Ink.LineStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (muted) Ink.TextSoft else Ink.Text),
        contentPadding = PaddingValues(horizontal = 18.dp),
        modifier = modifier.heightIn(min = height),
    ) {
        Text(text = label, fontSize = fontSize.sp, fontWeight = if (muted) FontWeight.Normal else FontWeight.Medium)
    }
}

private const val VISIBLE_LOG_ENTRIES = 6
