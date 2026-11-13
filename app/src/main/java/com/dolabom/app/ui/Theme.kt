package com.dolabom.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.dolabom.app.core.Severity

private val DolabomColors = darkColorScheme(
    primary = Color(0xFF58A6FF),
    onPrimary = Color(0xFF08131F),
    secondary = Color(0xFF7EE0C3),
    onSecondary = Color(0xFF08131F),
    background = Color(0xFF10151C),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF1A222D),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF243040),
    onSurfaceVariant = Color(0xFFA7B3C2),
    outline = Color(0xFF445266),
    error = Color(0xFFFF6B63),
    onError = Color(0xFF1F0A08),
)

@Composable
fun DolabomTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DolabomColors, content = content)
}

/** 상태의 심각도를 색으로 나타낸다. 색만으로 구분하지 않도록 화면에는 항상 글자도 함께 쓴다. */
internal fun severityColor(severity: Severity): Color = when (severity) {
    Severity.INFO -> Color(0xFF7EE0C3)
    Severity.NOTICE -> Color(0xFFF2CC60)
    Severity.WARNING -> Color(0xFFFFA657)
    Severity.EMERGENCY -> Color(0xFFFF6B63)
}

/**
 * 차량 화면은 가로 2560dp처럼 아주 넓어서, 그대로 그리면 글자와 버튼이 작게 보인다.
 * 화면 너비에 맞춰 전체를 1~2배로 키운다. 휴대폰 크기에서는 그대로 둔다.
 */
@Composable
internal fun ScaledForCar(content: @Composable () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val scale = (screenWidthDp / CAR_BASE_WIDTH_DP).coerceIn(1f, 2f)
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density * scale, density.fontScale),
        content = content,
    )
}

private const val CAR_BASE_WIDTH_DP = 1280f

/** 내용을 묶는 둥근 판. */
@Composable
internal fun Panel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
