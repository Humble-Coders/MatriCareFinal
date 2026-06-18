package com.humblecoders.matricareog.util

import com.humblecoders.matricareog.model.ChartData

/** Pregnancy normal ranges — aligned with ReportAnalysisScreen metrics. */
object HealthRangeUtils {

    private const val GREEN = 0xFF2E7D32.toInt()
    private const val RED = 0xFFD32F2F.toInt()

    fun isInNormalRange(parameter: String, value: Double): Boolean {
        return when (parameter.lowercase()) {
            "hemoglobin" -> value in 11.0..16.0
            "hba1c" -> value in 4.0..5.7
            "glucose", "blood glucose" -> value in 70.0..140.0
            "heart rate", "pulse" -> value in 60.0..100.0
            "body temperature", "temperature" -> value in 97.0..99.5
            "respiration rate", "respiration" -> value in 12.0..20.0
            else -> value in 11.0..16.0
        }
    }

    fun isBloodPressureInRange(systolic: Double, diastolic: Double): Boolean {
        return systolic in 90.0..140.0 && diastolic in 60.0..90.0
    }

    fun riskColorInt(inRange: Boolean): Int = if (inRange) GREEN else RED

    fun chartPointsForParameter(chartData: ChartData, parameter: String): List<ChartPoint> {
        return when (parameter.lowercase()) {
            "hemoglobin" -> chartData.hemoglobinData
                .filter { it.hemoglobin > 0 }
                .map { ChartPoint(it.hemoglobin, it.date, isInNormalRange("hemoglobin", it.hemoglobin)) }
            "hba1c" -> chartData.hba1cData
                .filter { it.hba1c > 0 }
                .map { ChartPoint(it.hba1c, it.date, isInNormalRange("hba1c", it.hba1c)) }
            "glucose", "blood glucose" -> chartData.glucoseData
                .filter { it.glucose > 0 }
                .map { ChartPoint(it.glucose, it.date, isInNormalRange("glucose", it.glucose)) }
            "blood pressure" -> chartData.bloodPressureData
                .filter { it.systolicBP > 0 && it.diastolicBP > 0 }
                .map {
                    ChartPoint(
                        value = it.systolicBP,
                        label = it.date,
                        inRange = isBloodPressureInRange(it.systolicBP, it.diastolicBP)
                    )
                }
            "heart rate", "pulse" -> chartData.pulseData
                .filter { it.pulseRate > 0 }
                .map { ChartPoint(it.pulseRate, it.date, isInNormalRange("pulse", it.pulseRate)) }
            "body temperature", "temperature" -> chartData.temperatureData
                .filter { it.bodyTemperature > 0 }
                .map { ChartPoint(it.bodyTemperature, it.date, isInNormalRange("temperature", it.bodyTemperature)) }
            "respiration rate", "respiration" -> chartData.respirationData
                .filter { it.respirationRate > 0 }
                .map { ChartPoint(it.respirationRate, it.date, isInNormalRange("respiration", it.respirationRate)) }
            else -> chartData.hemoglobinData
                .filter { it.hemoglobin > 0 }
                .map { ChartPoint(it.hemoglobin, it.date, isInNormalRange("hemoglobin", it.hemoglobin)) }
        }
    }
}

data class ChartPoint(
    val value: Double,
    val label: String,
    val inRange: Boolean
)
