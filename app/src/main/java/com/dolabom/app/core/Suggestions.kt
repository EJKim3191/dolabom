package com.dolabom.app.core

/** 제안 종류별 문구와 예시 장소. 장소는 아직 실제 검색 결과가 아니라 시연용 예시다. */
object Suggestions {

    fun title(kind: SuggestionKind): String = when (kind) {
        SuggestionKind.REST_STOP -> "가까운 곳에서 잠깐 쉬어 가요"
        SuggestionKind.PET_WALK -> "산책할 수 있는 곳에 들러요"
        SuggestionKind.COOL_CABIN -> "실내 온도를 낮춰 주세요"
        SuggestionKind.CHECK_BELT -> "안전한 곳에 세우고 안전띠를 확인해요"
        SuggestionKind.EMERGENCY_ROOM -> "가까운 응급실로 안내할게요"
        SuggestionKind.VET_24H -> "가까운 24시 동물병원으로 안내할게요"
        SuggestionKind.RETURN_TO_CAR -> "지금 차로 돌아가 주세요"
    }

    /** 경유지로 추가할 장소. 장소가 필요 없는 제안이면 null. */
    fun place(kind: SuggestionKind): Place? = when (kind) {
        SuggestionKind.REST_STOP -> Place("졸음쉼터", "약 2분 거리 · 예시 장소")
        SuggestionKind.PET_WALK -> Place("반려동물 동반 휴게소", "약 4분 거리 · 예시 장소")
        SuggestionKind.CHECK_BELT -> Place("졸음쉼터", "약 2분 거리 · 예시 장소")
        SuggestionKind.EMERGENCY_ROOM -> Place("응급실", "약 7분 거리 · 예시 장소")
        SuggestionKind.VET_24H -> Place("24시 동물병원", "약 6분 거리 · 예시 장소")
        SuggestionKind.COOL_CABIN, SuggestionKind.RETURN_TO_CAR -> null
    }

    /** 제안을 받아들이는 버튼 문구 */
    fun acceptLabel(kind: SuggestionKind): String = when (kind) {
        SuggestionKind.REST_STOP, SuggestionKind.PET_WALK -> "들렀다 가자"
        SuggestionKind.CHECK_BELT -> "세울 곳 안내"
        SuggestionKind.EMERGENCY_ROOM, SuggestionKind.VET_24H -> "바로 안내"
        SuggestionKind.COOL_CABIN -> "확인했어요"
        SuggestionKind.RETURN_TO_CAR -> "지금 돌아가요"
    }
}
