package com.dolabom.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolabom.app.core.Korean
import com.dolabom.app.core.RiderType

/** 화면 1: 오늘 뒷좌석에 누가 타는지 고른다. */
@Composable
fun SetupScreen(
    state: UiState,
    onSelectType: (RiderType) -> Unit,
    onNameChange: (String) -> Unit,
    onStart: () -> Unit,
) {
    val name = state.riderName.trim().ifEmpty { defaultRiderName(state.riderType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "돌아봄",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "오늘 뒷좌석에는 누가 타나요?",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChoiceButton(
                label = riderTypeLabel(RiderType.CHILD),
                selected = state.riderType == RiderType.CHILD,
                onClick = { onSelectType(RiderType.CHILD) },
            )
            ChoiceButton(
                label = riderTypeLabel(RiderType.PET),
                selected = state.riderType == RiderType.PET,
                onClick = { onSelectType(RiderType.PET) },
            )
        }

        OutlinedTextField(
            value = state.riderName,
            onValueChange = onNameChange,
            label = { Text("이름 또는 부르는 말") },
            singleLine = true,
            modifier = Modifier.widthIn(min = 360.dp),
        )
        Text(
            text = "이렇게 알려 드려요: \"${Korean.subject(name)} 잠들었어요.\"",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onStart,
            modifier = Modifier
                .widthIn(min = 360.dp)
                .heightIn(min = 80.dp),
        ) {
            Text(text = "지켜보기 시작", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "차량 신호: ${state.vehicleSourceLabel} · ${state.vehicleStatus}\n" +
                "뒷좌석 인식: 아직 가짜 데이터예요. 주행 화면의 시연 패널로 상황을 바꿀 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 둘 중 하나를 고르는 큰 버튼. 고른 쪽은 채워서 보여 준다. */
@Composable
private fun ChoiceButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val modifier = Modifier
        .widthIn(min = 220.dp)
        .heightIn(min = 96.dp)
    if (selected) {
        Button(onClick = onClick, modifier = modifier) {
            Text(text = label, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(text = label, fontSize = 28.sp)
        }
    }
}
