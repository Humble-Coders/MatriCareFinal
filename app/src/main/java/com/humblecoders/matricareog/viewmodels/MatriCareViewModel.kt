package com.humblecoders.matricareog.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humblecoders.matricareog.model.MatriCareState
import com.humblecoders.matricareog.repository.MatriCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatriCareViewModel(
    private val repository: MatriCareRepository
) : ViewModel() {

    // UI State for charts
    private val _uiState = MutableStateFlow<MatriCareState>(MatriCareState.Loading)
    val uiState: StateFlow<MatriCareState> = _uiState.asStateFlow()

    // Tab selection: 0 = Prediction History, 1 = Risk History
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Prediction History State
    private val _predictionHistory = MutableStateFlow<List<MatriCareRepository.PredictionHistoryItem>>(emptyList())
    val predictionHistory: StateFlow<List<MatriCareRepository.PredictionHistoryItem>> = _predictionHistory.asStateFlow()

    // Risk History State
    private val _riskHistory = MutableStateFlow<List<MatriCareRepository.RiskHistoryItem>>(emptyList())
    val riskHistory: StateFlow<List<MatriCareRepository.RiskHistoryItem>> = _riskHistory.asStateFlow()

    // Available Parameters for Charts
    private val _availableParameters = MutableStateFlow<List<String>>(emptyList())
    val availableParameters: StateFlow<List<String>> = _availableParameters.asStateFlow()

    // Selected Parameter for Chart Display
    private val _selectedParameter = MutableStateFlow("Haemoglobin")
    val selectedParameter: StateFlow<String> = _selectedParameter.asStateFlow()

    // Loading states for each tab
    private val _isPredictionHistoryLoading = MutableStateFlow(false)
    val isPredictionHistoryLoading: StateFlow<Boolean> = _isPredictionHistoryLoading.asStateFlow()

    private val _isRiskHistoryLoading = MutableStateFlow(false)
    val isRiskHistoryLoading: StateFlow<Boolean> = _isRiskHistoryLoading.asStateFlow()

    // Error states
    private val _predictionHistoryError = MutableStateFlow<String?>(null)
    val predictionHistoryError: StateFlow<String?> = _predictionHistoryError.asStateFlow()

    private val _riskHistoryError = MutableStateFlow<String?>(null)
    val riskHistoryError: StateFlow<String?> = _riskHistoryError.asStateFlow()

    // Statistics for quick overview
    private val _totalRecords = MutableStateFlow(0)
    val totalRecords: StateFlow<Int> = _totalRecords.asStateFlow()

    private val _lastUpdateDate = MutableStateFlow<String?>(null)
    val lastUpdateDate: StateFlow<String?> = _lastUpdateDate.asStateFlow()

    private val _riskSummary = MutableStateFlow<Map<String, Int>>(emptyMap())
    val riskSummary: StateFlow<Map<String, Int>> = _riskSummary.asStateFlow()

    // Remove automatic loading in init to prevent auth issues
    // Data will be loaded when explicitly called from UI
    
    private fun loadInitialData() {
        loadAvailableParameters()
        loadPredictionHistory()
        loadRiskHistory()
        loadChartData() // For backward compatibility
    }
    
    // Call this method from UI when ready
    fun initializeData() {
        loadInitialData()
    }

    // Load available parameters
    fun loadAvailableParameters() {
        viewModelScope.launch {
            try {
                repository.getAvailableParameters().collect { result ->
                    result.fold(
                        onSuccess = { parameters ->
                            _availableParameters.value = parameters
                            if (parameters.isNotEmpty() && _selectedParameter.value !in parameters) {
                                _selectedParameter.value = parameters.first()
                            }
                        },
                        onFailure = { exception ->
                            // Check if it's an authentication error
                            if (exception.message?.contains("not authenticated", ignoreCase = true) == true) {
                                // Don't set parameters error for auth issues, let UI handle retry
                                _availableParameters.value = emptyList()
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                // Handle any uncaught exceptions
                _availableParameters.value = emptyList()
            }
        }
    }

    // Load prediction history (all medical data)
    fun loadPredictionHistory() {
        viewModelScope.launch {
            _isPredictionHistoryLoading.value = true
            _predictionHistoryError.value = null

            try {
                repository.getUserPredictionHistory().collect { result ->
                    result.fold(
                        onSuccess = { historyList ->
                            _predictionHistory.value = historyList
                            _totalRecords.value = historyList.size
                            _lastUpdateDate.value = historyList.firstOrNull()?.date
                            _predictionHistoryError.value = null
                        },
                        onFailure = { exception ->
                            val errorMessage = when {
                                exception.message?.contains("not authenticated", ignoreCase = true) == true -> 
                                    "User not authenticated. Please try again."
                                exception.message?.contains("network", ignoreCase = true) == true -> 
                                    "Network error. Please check your connection and try again."
                                else -> exception.message ?: "Failed to load prediction history"
                            }
                            _predictionHistoryError.value = errorMessage
                            _predictionHistory.value = emptyList()
                        }
                    )
                    _isPredictionHistoryLoading.value = false
                }
            } catch (e: Exception) {
                _predictionHistoryError.value = "Unexpected error: ${e.message}"
                _predictionHistory.value = emptyList()
                _isPredictionHistoryLoading.value = false
            }
        }
    }

    // Load risk history (ML predictions only)
    fun loadRiskHistory() {
        viewModelScope.launch {
            _isRiskHistoryLoading.value = true
            _riskHistoryError.value = null

            repository.getUserRiskHistory().collect { result ->
                result.fold(
                    onSuccess = { riskList ->
                        _riskHistory.value = riskList
                        calculateRiskSummary(riskList)
                        _riskHistoryError.value = null
                    },
                    onFailure = { exception ->
                        _riskHistoryError.value = exception.message ?: "Failed to load risk history"
                        _riskHistory.value = emptyList()
                        _riskSummary.value = emptyMap()
                    }
                )
                _isRiskHistoryLoading.value = false
            }
        }
    }

    // Calculate risk summary statistics
    private fun calculateRiskSummary(riskList: List<MatriCareRepository.RiskHistoryItem>) {
        val summary = riskList.groupBy { it.riskLevel }.mapValues { it.value.size }
        _riskSummary.value = summary
    }

    // Load chart data for selected parameter
    fun loadChartData() {
        viewModelScope.launch {
            _uiState.value = MatriCareState.Loading

            try {
                if (_selectedTab.value == 1) {
                    // For Risk History tab, use the original chart data method
                    repository.getChartData().collect { result ->
                        _uiState.value = result.fold(
                            onSuccess = { chartData ->
                                MatriCareState.Success(chartData)
                            },
                            onFailure = { exception ->
                                val errorMessage = when {
                                    exception.message?.contains("not authenticated", ignoreCase = true) == true -> 
                                        "User not authenticated. Please try again."
                                    exception.message?.contains("network", ignoreCase = true) == true -> 
                                        "Network error. Please check your connection and try again."
                                    else -> exception.message ?: "Failed to load chart data"
                                }
                                MatriCareState.Error(errorMessage)
                            }
                        )
                    }
                } else {
                    // For Prediction History tab, use parameter-specific chart data
                    val parameterName = _selectedParameter.value.lowercase()
                    repository.getParameterChartData(parameterName).collect { result ->
                        _uiState.value = result.fold(
                            onSuccess = { chartData ->
                                MatriCareState.Success(chartData)
                            },
                            onFailure = { exception ->
                                val errorMessage = when {
                                    exception.message?.contains("not authenticated", ignoreCase = true) == true -> 
                                        "User not authenticated. Please try again."
                                    exception.message?.contains("network", ignoreCase = true) == true -> 
                                        "Network error. Please check your connection and try again."
                                    else -> exception.message ?: "Failed to load chart data for $parameterName"
                                }
                                MatriCareState.Error(errorMessage)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MatriCareState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    // Tab selection methods
    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        when (tabIndex) {
            0 -> {
                // Prediction History tab selected
                if (_predictionHistory.value.isEmpty()) {
                    loadPredictionHistory()
                }
                loadChartData() // Load chart for selected parameter
            }
            1 -> {
                // Risk History tab selected
                if (_riskHistory.value.isEmpty()) {
                    loadRiskHistory()
                }
                loadChartData() // Load default chart data
            }
        }
    }

    // Parameter selection for Prediction History charts
    fun selectParameter(parameter: String) {
        _selectedParameter.value = parameter
        if (_selectedTab.value == 0) { // Only update chart if on Prediction History tab
            loadChartData()
        }
    }

    // Refresh methods
    fun refreshCurrentTab() {
        when (_selectedTab.value) {
            0 -> {
                loadPredictionHistory()
                loadChartData()
            }
            1 -> {
                loadRiskHistory()
                loadChartData()
            }
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    fun refreshPredictionHistory() {
        loadPredictionHistory()
        if (_selectedTab.value == 0) {
            loadChartData()
        }
    }

    fun refreshRiskHistory() {
        loadRiskHistory()
        if (_selectedTab.value == 1) {
            loadChartData()
        }
    }

    // Clear error methods
    fun clearPredictionHistoryError() {
        _predictionHistoryError.value = null
    }

    fun clearRiskHistoryError() {
        _riskHistoryError.value = null
    }

    // Get specific prediction history item by ID
    fun getPredictionHistoryItem(id: String): MatriCareRepository.PredictionHistoryItem? {
        return _predictionHistory.value.find { it.id == id }
    }

    // Get specific risk history item by ID
    fun getRiskHistoryItem(id: String): MatriCareRepository.RiskHistoryItem? {
        return _riskHistory.value.find { it.id == id }
    }

    // Get filtered prediction history by date range
    fun getFilteredPredictionHistory(startDate: Long, endDate: Long): List<MatriCareRepository.PredictionHistoryItem> {
        return _predictionHistory.value.filter { item ->
            item.timestamp in startDate..endDate
        }
    }

    // Get filtered risk history by risk level
    fun getFilteredRiskHistory(riskLevel: String): List<MatriCareRepository.RiskHistoryItem> {
        return if (riskLevel == "All") {
            _riskHistory.value
        } else {
            _riskHistory.value.filter { it.riskLevel == riskLevel }
        }
    }

    // Get latest values for quick stats
    fun getLatestValues(): Map<String, Any?> {
        val latest = _predictionHistory.value.firstOrNull()
        return if (latest != null) {
            mapOf(
                "age" to latest.age,
                "systolicBP" to latest.systolicBP,
                "diastolicBP" to latest.diastolicBP,
                "glucose" to latest.glucose,
                "hemoglobin" to latest.hemoglobinLevel,
                "pulseRate" to latest.pulseRate,
                "temperature" to latest.bodyTemperature,
                "respiration" to latest.respirationRate,
                "gravida" to latest.gravida,
                "para" to latest.para
            )
        } else {
            emptyMap()
        }
    }

    // Get trend for specific parameter (increasing/decreasing/stable)
    fun getParameterTrend(parameter: String): String {
        val history = _predictionHistory.value.take(5) // Last 5 records
        if (history.size < 2) return "Insufficient Data"

        val values = when (parameter.lowercase()) {
            "hemoglobin", "haemoglobin" -> history.map { it.hemoglobinLevel }
            "glucose" -> history.map { it.glucose }
            "systolic" -> history.map { it.systolicBP.toDouble() }
            "diastolic" -> history.map { it.diastolicBP.toDouble() }
            "pulse" -> history.map { it.pulseRate.toDouble() }
            "temperature" -> history.map { it.bodyTemperature }
            "respiration" -> history.map { it.respirationRate.toDouble() }
            else -> return "Unknown Parameter"
        }

        val recent = values.take(3).average()
        val older = values.drop(2).average()

        return when {
            recent > older * 1.05 -> "Increasing"
            recent < older * 0.95 -> "Decreasing"
            else -> "Stable"
        }
    }
}