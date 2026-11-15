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

/**
 * 돌아봄 색. 순수 블랙 바탕에 무채색 면과 1px 선만 쓰고, 색은 파랑 하나로 "지금 눌러야 할 것"만 가리킨다.
 * 상태는 작은 점 하나(초록·호박·주황·빨강)로만 구분한다.
 */
internal object Ink {
    val Background = Color(0xFF0A0A0B)
    val Surface = Color(0xFF0F0F10)
    val SurfaceRaised = Color(0xFF121214)
    val SurfaceActive = Color(0xFF161618)
    val Line = Color(0xFF1C1C1F)
    val LineStrong = Color(0xFF2A2A2E)
    val LineBright = Color(0xFF3A3A40)
    val Text = Color(0xFFF2F2F3)
    val TextSoft = Color(0xFFC9C9CE)
    val TextMuted = Color(0xFF8A8A90)
    val IconIdle = Color(0xFF6E6E75)
    val Blue = Color(0xFF2E7CF6)
    val Green = Color(0xFF37C86A)
    val Amber = Color(0xFFF5B541)
    val Orange = Color(0xFFF28C5B)
    val Red = Color(0xFFF26B5B)
}

private val DolabomColors = darkColorScheme(
    primary = Ink.Blue,
    onPrimary = Color.White,
    secondary = Ink.Green,
    onSecondary = Ink.Background,
    background = Ink.Background,
    onBackground = Ink.Text,
    surface = Ink.Surface,
    onSurface = Ink.Text,
    surfaceVariant = Ink.SurfaceRaised,
    onSurfaceVariant = Ink.TextMuted,
    outline = Ink.Line,
    outlineVariant = Ink.LineStrong,
    error = Ink.Red,
    onError = Color.White,
)

@Composable
fun DolabomTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DolabomColors, content = content)
}

/** 상태의 심각도를 색으로 나타낸다. 색만으로 구분하지 않도록 화면에는 항상 글자도 함께 쓴다. */
internal fun severityColor(severity: Severity): Color = when (severity) {
    Severity.INFO -> Ink.Green
    Severity.NOTICE -> Ink.Amber
    Severity.WARNING -> Ink.Orange
    Severity.EMERGENCY -> Ink.Red
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

/** 내용을 묶는 둥근 판. 채우지 않고 1dp 선으로만 구분한다. */
@Composable
internal fun Panel(
    modifier: Modifier = Modifier,
    color: Color = Ink.Surface,
    border: BorderStroke? = BorderStroke(1.dp, Ink.Line),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
