package com.dolabom.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolabom.app.core.Korean
import com.dolabom.app.core.RiderType

/** 화면 1: 오늘 뒷좌석에 누가 타는지 고른다. 넓은 화면에서는 왼쪽에 선택, 오른쪽에 좌석 도식을 둔다. */
@Composable
fun SetupScreen(
    state: UiState,
    onSelectType: (RiderType) -> Unit,
    onNameChange: (String) -> Unit,
    onStart: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= 900.dp
        if (wide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 56.dp, top = 44.dp, bottom = 44.dp),
                horizontalArrangement = Arrangement.spacedBy(56.dp),
            ) {
                SetupForm(
                    state = state,
                    onSelectType = onSelectType,
                    onNameChange = onNameChange,
                    onStart = onStart,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                SetupSidebar(state = state, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                SetupForm(
                    state = state,
                    onSelectType = onSelectType,
                    onNameChange = onNameChange,
                    onStart = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    pinButton = false,
                )
                SetupSidebar(state = state, modifier = Modifier.fillMaxWidth(), diagramHeight = 300.dp)
            }
        }
    }
}

@Composable
private fun SetupForm(
    state: UiState,
    onSelectType: (RiderType) -> Unit,
    onNameChange: (String) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    pinButton: Boolean = true,
) {
    val name = state.riderName.trim().ifEmpty { defaultRiderName(state.riderType) }

    // 넓은 화면에서는 버튼을 아래에 고정하고 윗부분만 스크롤한다. 높이가 모자라도 버튼은 항상 보인다.
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        val fieldsModifier = if (pinButton) {
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        } else {
            Modifier
        }
        Column(modifier = fieldsModifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(text = "동승자 설정")
            Text(
                text = "오늘 뒷좌석에는\n누가 타나요?",
                fontSize = 36.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.8).sp,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChoiceCard(
                label = riderTypeLabel(RiderType.CHILD),
                detail = "카시트 · 뒷좌석",
                icon = Icons.Outlined.Person,
                selected = state.riderType == RiderType.CHILD,
                onClick = { onSelectType(RiderType.CHILD) },
                modifier = Modifier.weight(1f),
            )
            ChoiceCard(
                label = riderTypeLabel(RiderType.PET),
                detail = "켄넬 · 뒷좌석",
                icon = Icons.Outlined.Favorite,
                selected = state.riderType == RiderType.PET,
                onClick = { onSelectType(RiderType.PET) },
                modifier = Modifier.weight(1f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "이름 또는 부르는 말", fontSize = 13.sp, color = Ink.TextMuted)
            TextField(
                value = state.riderName,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 22.sp, color = Ink.Text),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Ink.Blue,
                    unfocusedIndicatorColor = Ink.LineStrong,
                    cursorColor = Ink.Blue,
                    focusedTextColor = Ink.Text,
                    unfocusedTextColor = Ink.Text,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "음성 안내 예시 · \"${Korean.subject(name)} 잠들었어요.\"",
                fontSize = 14.sp,
                color = Ink.TextMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        }

        Button(
            onClick = onStart,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
        ) {
            Text(text = "지켜보기 시작", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ChoiceCard(
    label: String,
    detail: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Ink.SurfaceRaised else Ink.Surface,
        border = BorderStroke(1.dp, if (selected) Ink.Blue else Ink.LineStrong),
        modifier = modifier.heightIn(min = 116.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Ink.Text else Ink.TextMuted,
                    modifier = Modifier.size(24.dp),
                )
                if (selected) {
                    Box(modifier = Modifier.size(8.dp).background(Ink.Blue, CircleShape))
                } else {
                    Box(modifier = Modifier.size(8.dp).border(1.dp, Ink.LineBright, CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) Ink.Text else Ink.TextSoft,
                )
                Text(text = detail, fontSize = 13.sp, color = Ink.TextMuted)
            }
        }
    }
}

@Composable
private fun SetupSidebar(
    state: UiState,
    modifier: Modifier = Modifier,
    diagramHeight: Dp? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        val diagramModifier = if (diagramHeight != null) {
            Modifier.fillMaxWidth().height(diagramHeight)
        } else {
            Modifier.fillMaxWidth().weight(1f)
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Ink.Surface,
            border = BorderStroke(1.dp, Ink.Line),
            modifier = diagramModifier,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SeatDiagram(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    highlight = Ink.Blue,
                    driverPresent = true,
                )
                Eyebrow(
                    text = "뒷좌석 · 왼쪽",
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp),
                )
                DotLabel(
                    text = "${state.vehicleSourceLabel} · ${state.vehicleStatus}",
                    dot = Ink.Green,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 20.dp),
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile(label = "차량 신호", value = "뒷문 · 주차 · 온도", modifier = Modifier.weight(1f))
            InfoTile(label = "뒷좌석 인식", value = "소리 · 움직임 (시연)", modifier = Modifier.weight(1f))
            InfoTile(label = "프라이버시", value = "차 안에서만 처리", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Ink.Line),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = label, fontSize = 12.sp, color = Ink.TextMuted)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Ink.Text)
        }
    }
}
