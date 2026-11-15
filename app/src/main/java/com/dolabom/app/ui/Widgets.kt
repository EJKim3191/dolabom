package com.dolabom.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 위에서 본 차량 좌석 배치. 뒷좌석 왼쪽을 강조해 누가 어디 앉아 있는지 보여 준다.
 * 운전석은 운전자가 타고 있을 때만 채운다.
 */
@Composable
internal fun SeatDiagram(
    modifier: Modifier = Modifier,
    highlight: Color = Ink.Blue,
    driverPresent: Boolean = true,
    rearOccupied: Boolean = true,
) {
    Canvas(modifier = modifier) {
        val scale = minOf(size.width / 300f, size.height / 420f)
        val dx = (size.width - 300f * scale) / 2f
        val dy = (size.height - 420f * scale) / 2f
        withTransform({
            translate(dx, dy)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            val line = Stroke(width = 1.5f / scale * density.coerceAtLeast(1f))
            // 차체
            val body = Path().apply {
                moveTo(60f, 60f)
                cubicTo(60f, 20f, 240f, 20f, 240f, 60f)
                lineTo(252f, 150f)
                lineTo(252f, 360f)
                cubicTo(252f, 400f, 48f, 400f, 48f, 360f)
                lineTo(48f, 150f)
                close()
            }
            drawPath(body, Ink.LineStrong, style = line)
            // 앞유리, 뒷유리
            val windshield = Path().apply {
                moveTo(80f, 120f)
                cubicTo(80f, 95f, 220f, 95f, 220f, 120f)
                lineTo(226f, 190f)
                lineTo(74f, 190f)
                close()
            }
            drawPath(windshield, Ink.Line, style = line)
            val rearWindow = Path().apply {
                moveTo(74f, 300f)
                lineTo(226f, 300f)
                lineTo(222f, 360f)
                cubicTo(222f, 378f, 78f, 378f, 78f, 360f)
                close()
            }
            drawPath(rearWindow, Ink.Line, style = line)

            // 앞좌석: 왼쪽이 운전석
            seat(84f, 200f, fill = if (driverPresent) Ink.Text.copy(alpha = 0.06f) else null,
                stroke = if (driverPresent) Ink.IconIdle else Ink.LineBright, line = line)
            seat(160f, 200f, fill = null, stroke = Ink.LineBright, line = line)
            if (driverPresent) drawCircle(Ink.TextSoft, radius = 7f, center = Offset(112f, 232f))

            // 뒷좌석: 왼쪽이 동승자 자리
            seat(84f, 280f, fill = if (rearOccupied) highlight.copy(alpha = 0.14f) else null,
                stroke = if (rearOccupied) highlight else Ink.LineBright, line = line)
            seat(160f, 280f, fill = null, stroke = Ink.LineBright, line = line)
            if (rearOccupied) {
                drawCircle(highlight, radius = 6f, center = Offset(112f, 312f))
                drawCircle(highlight.copy(alpha = 0.5f), radius = 14f, center = Offset(112f, 312f), style = line)
                drawCircle(highlight.copy(alpha = 0.2f), radius = 24f, center = Offset(112f, 312f), style = line)
            }
            // 뒷문 표시
            drawLine(Ink.LineBright, Offset(40f, 296f), Offset(76f, 296f), strokeWidth = line.width)
            drawLine(Ink.LineBright, Offset(224f, 296f), Offset(260f, 296f), strokeWidth = line.width)
        }
    }
}

private fun DrawScope.seat(x: Float, y: Float, fill: Color?, stroke: Color, line: Stroke) {
    val topLeft = Offset(x, y)
    val size = Size(56f, 64f)
    val radius = CornerRadius(12f, 12f)
    if (fill != null) drawRoundRect(fill, topLeft, size, radius)
    drawRoundRect(stroke, topLeft, size, radius, style = line)
}

/** 원형 상태 링. 가운데에 작은 머리말과 상태 한 단어를 둔다. */
@Composable
internal fun StatusRing(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    progress: Float = 1f,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val inset = strokeWidth
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(Ink.Line, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(strokeWidth))
            drawArc(
                color, -90f, 360f * progress.coerceIn(0f, 1f), false,
                Offset(inset, inset), arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Eyebrow(text = label)
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = Ink.Text)
        }
    }
}

/**
 * 간단한 지도. 얇은 회색 길 위에 현재 경로를 파랗게 긋고, 제안한 장소에 핀을 찍는다.
 * 내비게이션 연동 전이라 모양만 보여 주는 예시다.
 */
@Composable
internal fun RouteMap(modifier: Modifier = Modifier, accent: Color = Ink.Blue) {
    Canvas(modifier = modifier) {
        val scale = maxOf(size.width / 600f, size.height / 560f)
        val dx = (size.width - 600f * scale) / 2f
        val dy = (size.height - 560f * scale) / 2f
        withTransform({
            translate(dx, dy)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            val thin = Stroke(width = 2f)
            fun road(vararg points: Pair<Float, Float>) {
                val path = Path()
                points.forEachIndexed { i, (x, y) -> if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }
                drawPath(path, Ink.Line, style = thin)
            }
            road(-20f to 120f, 140f to 110f, 210f to 160f, 330f to 150f, 420f to 90f, 620f to 80f)
            road(60f to -20f, 80f to 130f, 40f to 260f, 70f to 400f, 30f to 580f)
            road(-20f to 330f, 120f to 320f, 200f to 360f, 310f to 350f, 380f to 300f, 620f to 290f)
            road(470f to -20f, 450f to 150f, 500f to 300f, 470f to 450f, 520f to 580f)
            road(-20f to 470f, 160f to 460f, 250f to 500f, 420f to 490f, 620f to 470f)

            val route = Path().apply {
                moveTo(300f, 580f); lineTo(305f, 480f); lineTo(280f, 360f); lineTo(300f, 250f)
                lineTo(330f, 150f); lineTo(350f, 60f)
            }
            drawPath(route, Ink.LineStrong, style = Stroke(10f, cap = StrokeCap.Round))
            val done = Path().apply {
                moveTo(300f, 580f); lineTo(305f, 480f); lineTo(280f, 360f); lineTo(300f, 250f)
            }
            drawPath(done, accent, style = Stroke(6f, cap = StrokeCap.Round))
            val ahead = Path().apply { moveTo(300f, 250f); lineTo(330f, 150f); lineTo(350f, 60f) }
            drawPath(ahead, accent.copy(alpha = 0.35f), style = Stroke(6f, cap = StrokeCap.Round))

            // 현재 위치 화살표
            val arrow = Path().apply {
                moveTo(305f, 468f); lineTo(314f, 490f); lineTo(305f, 485f); lineTo(296f, 490f); close()
            }
            drawPath(arrow, Ink.Text)
            // 제안 장소 핀
            drawCircle(accent.copy(alpha = 0.16f), radius = 22f, center = Offset(300f, 250f))
            drawCircle(accent, radius = 8f, center = Offset(300f, 250f))
            drawCircle(Color.White, radius = 3f, center = Offset(300f, 250f))
        }
    }
}

/** 머리말과 값 한 쌍. */
@Composable
internal fun Metric(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Ink.Text) {
    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Eyebrow(text = label)
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** 위아래 선 사이에 값을 나란히 둔다. 칸 사이는 세로 선으로 나눈다. */
@Composable
internal fun MetricsRow(modifier: Modifier = Modifier, items: List<Triple<String, String, Color>>) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = Ink.Line)
        Row(verticalAlignment = Alignment.CenterVertically) {
            items.forEachIndexed { index, (label, value, color) ->
                if (index > 0) {
                    VerticalDivider(color = Ink.Line, modifier = Modifier.height(56.dp))
                }
                Metric(
                    label = label,
                    value = value,
                    valueColor = color,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (index > 0) 20.dp else 0.dp),
                )
            }
        }
        HorizontalDivider(color = Ink.Line)
    }
}
