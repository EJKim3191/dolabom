package com.dolabom.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/** 화면 세 개(동승자 선택, 주행 중 뒷좌석 상태, 알림과 쉼터 제안)를 상태에 따라 바꿔 보여 준다. */
@Composable
fun DolabomApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    DolabomTheme {
        ScaledForCar {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    CarChrome(screen = state.screen, state = state, onToggleVoice = viewModel::toggleVoice) {
                        when (state.screen) {
                            Screen.SETUP -> SetupScreen(
                                state = state,
                                onSelectType = viewModel::selectRiderType,
                                onNameChange = viewModel::setRiderName,
                                onStart = viewModel::startTrip,
                            )

                            Screen.MONITOR -> MonitorScreen(
                                state = state,
                                onEndTrip = viewModel::endTrip,
                                onScenario = viewModel::applyScenario,
                                onAdjustTemp = viewModel::adjustCabinTemp,
                                onToggleParked = viewModel::toggleParked,
                                onToggleDriver = viewModel::toggleDriverPresent,
                                onToggleRearDoor = viewModel::toggleRearDoor,
                                onSkipMinute = viewModel::skipMinute,
                            )

                            Screen.ALERT -> AlertScreen(
                                state = state,
                                onAccept = viewModel::acceptSuggestion,
                                onDismiss = viewModel::dismissAlert,
                            )
                        }
                    }
                }
            }
        }
    }
}
