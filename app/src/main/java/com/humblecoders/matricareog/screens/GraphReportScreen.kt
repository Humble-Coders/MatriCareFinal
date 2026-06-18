package com.humblecoders.matricareog.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.humblecoders.matricareog.model.MatriCareState
import com.humblecoders.matricareog.repository.MatriCareRepository
import com.humblecoders.matricareog.util.ChartPoint
import com.humblecoders.matricareog.util.HealthRangeUtils
import com.humblecoders.matricareog.viewmodels.MatriCareViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphReportScreen(
    onBackClick: () -> Unit,
    viewModel: MatriCareViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val predictionHistory by viewModel.predictionHistory.collectAsState()
    val riskHistory by viewModel.riskHistory.collectAsState()
    val availableParameters by viewModel.availableParameters.collectAsState()
    val selectedParameter by viewModel.selectedParameter.collectAsState()
    val isPredictionLoading by viewModel.isPredictionHistoryLoading.collectAsState()
    val isRiskLoading by viewModel.isRiskHistoryLoading.collectAsState()
    val predictionError by viewModel.predictionHistoryError.collectAsState()
    val riskError by viewModel.riskHistoryError.collectAsState()
    val totalRecords by viewModel.totalRecords.collectAsState()
    val lastUpdateDate by viewModel.lastUpdateDate.collectAsState()
    val riskSummary by viewModel.riskSummary.collectAsState()
    
    // Initialize data when screen is first loaded
    LaunchedEffect(Unit) {
        viewModel.initializeData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "Health History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE91E63)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE91E63)
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshCurrentTab() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFFE91E63)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuickStatsCard(
                    totalRecords = totalRecords,
                    lastUpdate = lastUpdateDate,
                    riskSummary = riskSummary
                )
            }

            item {
                TabSection(
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::selectTab
                )
            }

            if (selectedTab == 0 && availableParameters.isNotEmpty()) {
                item {
                    ParameterSelectionRow(
                        parameters = availableParameters,
                        selectedParameter = selectedParameter,
                        onParameterSelected = viewModel::selectParameter
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    predictionHistoryContentItems(
                        predictionHistory = predictionHistory,
                        isLoading = isPredictionLoading,
                        error = predictionError,
                        chartData = uiState,
                        selectedParameter = selectedParameter,
                        onRetry = { viewModel.refreshPredictionHistory() },
                        onClearError = { viewModel.clearPredictionHistoryError() }
                    )
                }
                1 -> {
                    riskHistoryContentItems(
                        riskHistory = riskHistory,
                        isLoading = isRiskLoading,
                        error = riskError,
                        onRetry = { viewModel.refreshRiskHistory() },
                        onClearError = { viewModel.clearRiskHistoryError() }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun QuickStatsCard(
    totalRecords: Int,
    lastUpdate: String?,
    riskSummary: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Health Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatsItem(
                    label = "Total Records",
                    value = totalRecords.toString(),
                    icon = Icons.Default.Assignment
                )
                StatsItem(
                    label = "Last Update",
                    value = lastUpdate ?: "No data",
                    icon = Icons.Default.Schedule
                )
            }

            if (riskSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Risk Assessment Summary",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    riskSummaryEntriesOrdered(riskSummary).forEach { (riskLevel, count) ->
                        RiskSummaryChip(
                            riskLevel = riskLevel,
                            count = count,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFE91E63),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun RiskSummaryChip(
    riskLevel: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (riskLevel) {
        "High Risk" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        "Moderate Risk" -> Color(0xFFFFF3E0) to Color(0xFFFF6F00)
        "No Risk" -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = riskLevel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = textColor,
                shape = CircleShape,
                modifier = Modifier.size(22.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Stable order so risk chips align consistently (label above count). */
private fun riskSummaryEntriesOrdered(riskSummary: Map<String, Int>): List<Pair<String, Int>> {
    val order = listOf("No Risk", "Moderate Risk", "High Risk")
    val ordered = order.mapNotNull { key -> riskSummary[key]?.let { key to it } }
    val rest = riskSummary.filterKeys { it !in order }.entries.map { it.key to it.value }
    return ordered + rest
}

@Composable
private fun TabSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabButton(
            text = "Prediction History",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.History
        )
        TabButton(
            text = "Risk History",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Security
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFE91E63) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ParameterSelectionRow(
    parameters: List<String>,
    selectedParameter: String,
    onParameterSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Select Parameter",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            parameters.forEach { parameter ->
                ParameterChip(
                    parameter = parameter,
                    isSelected = parameter == selectedParameter,
                    onClick = { onParameterSelected(parameter) }
                )
            }
        }
    }
}

@Composable
private fun ParameterChip(
    parameter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFFE91E63) else Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text = parameter,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private fun LazyListScope.predictionHistoryContentItems(
    predictionHistory: List<MatriCareRepository.PredictionHistoryItem>,
    isLoading: Boolean,
    error: String?,
    chartData: MatriCareState,
    selectedParameter: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    item {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Prediction History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Charts and assessments from your saved medical records",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF666666)
            )
        }
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                Text(
                    text = "${selectedParameter.trim()} Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Values over time for the selected parameter",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (chartData) {
                    is MatriCareState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE91E63))
                        }
                    }
                    is MatriCareState.Success -> {
                        val chartPoints = HealthRangeUtils.chartPointsForParameter(
                            chartData.chartData,
                            selectedParameter
                        )
                        if (chartPoints.isNotEmpty()) {
                            Column {
                                LineChartView(
                                    points = chartPoints,
                                    dataSetLabel = selectedParameter,
                                    modifier = Modifier.fillMaxWidth().height(220.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ChartRiskLegend()
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No data available for $selectedParameter",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                    is MatriCareState.Error -> {
                        ErrorMessage(
                            message = chartData.message,
                            onRetry = onRetry,
                            isAuthError = chartData.message.contains("not authenticated", ignoreCase = true)
                        )
                    }
                }
            }
        }
    }

    item {
        Text(
            text = "Medical History Records",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap a record to expand full details",
            fontSize = 13.sp,
            color = Color(0xFF757575)
        )
    }

    when {
        isLoading -> item {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE91E63))
            }
        }
        error != null -> item {
            ErrorMessage(message = error, onRetry = onRetry, onDismiss = onClearError)
        }
        predictionHistory.isEmpty() -> item {
            EmptyStateCard(
                title = "No Medical History",
                message = "Complete your medical history assessment to see data here.",
                icon = Icons.Default.HistoryEdu
            )
        }
        else -> items(predictionHistory, key = { it.id }) { item ->
            PredictionHistoryCard(item = item)
        }
    }
}

private fun LazyListScope.riskHistoryContentItems(
    riskHistory: List<MatriCareRepository.RiskHistoryItem>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    item {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "AI Risk Assessments",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap a record to expand full details",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF666666)
            )
        }
    }

    when {
        isLoading -> item {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE91E63))
            }
        }
        error != null -> item {
            ErrorMessage(message = error, onRetry = onRetry, onDismiss = onClearError)
        }
        riskHistory.isEmpty() -> item {
            EmptyStateCard(
                title = "No Risk Assessments",
                message = "Complete your medical assessment to get AI-powered risk predictions.",
                icon = Icons.Default.Security
            )
        }
        else -> items(riskHistory, key = { it.id }) { item ->
            RiskHistoryCard(item = item)
        }
    }
}

// Chart data is built in HealthRangeUtils.chartPointsForParameter

@Composable
private fun ChartRiskLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = Color(0xFF2E7D32), label = "In range (no risk)")
        Spacer(modifier = Modifier.width(16.dp))
        LegendDot(color = Color(0xFFD32F2F), label = "Out of range (risk)")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF757575))
    }
}

@Composable
private fun PredictionHistoryCard(
    item: MatriCareRepository.PredictionHistoryItem
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                    if (item.riskLevel.isNotBlank()) {
                        Text(
                            text = if (item.riskLevel.contains("High", true) || item.riskLevel.contains("Moderate", true)) "Risk Detected" else "No Risk",
                            fontSize = 12.sp,
                            color = if (item.riskLevel.contains("High", true) || item.riskLevel.contains("Moderate", true)) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(28.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    MedicalRecordDetails(
                        age = item.age,
                        systolicBP = item.systolicBP,
                        diastolicBP = item.diastolicBP,
                        pulseRate = item.pulseRate,
                        bodyTemperature = item.bodyTemperature,
                        glucose = item.glucose,
                        hemoglobinLevel = item.hemoglobinLevel,
                        hba1c = item.hba1c,
                        respirationRate = item.respirationRate,
                        gravida = item.gravida,
                        para = item.para,
                        liveBirths = item.liveBirths,
                        abortions = item.abortions,
                        childDeaths = item.childDeaths,
                        riskLevel = item.riskLevel
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskHistoryCard(
    item: MatriCareRepository.RiskHistoryItem
) {
    var expanded by remember { mutableStateOf(false) }
    val (cardColor, textColor, icon) = when (item.riskLevel) {
        "High Risk" -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            Icons.Default.Warning
        )
        "Moderate Risk" -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFFF6F00),
            Icons.Default.Info
        )
        "Low Risk", "No Risk" -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        else -> Triple(
            Color(0xFFF5F5F5),
            Color(0xFF757575),
            Icons.Default.Help
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = item.riskLevel,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = textColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (item.confidence > 0) {
                        Text(
                            text = "Confidence: ${(item.confidence * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    MedicalRecordDetails(
                        age = item.age,
                        systolicBP = item.systolicBP,
                        diastolicBP = item.diastolicBP,
                        pulseRate = item.pulseRate,
                        bodyTemperature = item.bodyTemperature,
                        glucose = item.glucose,
                        hemoglobinLevel = item.hemoglobinLevel,
                        hba1c = item.hba1c,
                        respirationRate = item.respirationRate,
                        gravida = item.gravida,
                        para = item.para,
                        liveBirths = item.liveBirths,
                        abortions = item.abortions,
                        childDeaths = item.childDeaths,
                        riskLevel = item.riskLevel,
                        contentColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalRecordDetails(
    age: Int,
    systolicBP: Int,
    diastolicBP: Int,
    pulseRate: Int,
    bodyTemperature: Double,
    glucose: Double,
    hemoglobinLevel: Double,
    hba1c: Double,
    respirationRate: Int,
    gravida: Int,
    para: Int,
    liveBirths: Int,
    abortions: Int,
    childDeaths: Int,
    riskLevel: String,
    contentColor: Color = Color(0xFFE91E63)
) {
    HorizontalDivider(color = contentColor.copy(alpha = 0.15f))
    Spacer(modifier = Modifier.height(12.dp))

    if (age > 0) {
        DetailRow("Age", "$age years", contentColor)
        Spacer(modifier = Modifier.height(8.dp))
    }
    if (riskLevel.isNotBlank()) {
        DetailRow("Risk Level", riskLevel, contentColor)
        Spacer(modifier = Modifier.height(8.dp))
    }

    Text("Vital Signs", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF666666))
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricValueCell(label = "BP", value = "$systolicBP/$diastolicBP", modifier = Modifier.weight(1f))
        MetricValueCell(label = "HR", value = "$pulseRate BPM", modifier = Modifier.weight(1f))
        MetricValueCell(label = "Temp", value = "${bodyTemperature}°F", modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricValueCell(label = "Respiration", value = "$respirationRate/min", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text("Lab Values", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF666666))
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricValueCell(label = "Glucose", value = "$glucose mg/dL", modifier = Modifier.weight(1f))
        MetricValueCell(label = "Hemoglobin", value = "%.1f g/dL".format(hemoglobinLevel), modifier = Modifier.weight(1f))
        MetricValueCell(label = "HbA1c", value = "$hba1c%", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text("Obstetric History", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF666666))
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Gravida: $gravida  •  Para: $para\nLive Births: $liveBirths  •  Abortions: $abortions\nChild Deaths: $childDeaths",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = contentColor,
        lineHeight = 22.sp
    )
}

@Composable
private fun DetailRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color(0xFF666666))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

/** Label above value, equal-width columns for aligned grids (vitals / labs). */
@Composable
private fun MetricValueCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    isAuthError: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAuthError) "Authentication Error" else "Error",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(if (isAuthError) "Retry Login" else "Retry")
                }
                if (onDismiss != null) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun LineChartView(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    dataSetLabel: String = "Data"
) {
    val labels = points.map { it.label }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                setTouchEnabled(true)
                setPinchZoom(true)
                description = Description().apply { text = "" }
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    isGranularityEnabled = true
                    setDrawGridLines(false)
                    textColor = android.graphics.Color.BLACK
                }
                axisLeft.textColor = android.graphics.Color.BLACK
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            val entries = points.mapIndexed { index, point ->
                Entry(index.toFloat(), point.value.toFloat())
            }

            val circleColors = points.map { point ->
                HealthRangeUtils.riskColorInt(point.inRange)
            }

            val dataSet = LineDataSet(entries, dataSetLabel).apply {
                color = android.graphics.Color.LTGRAY
                valueTextColor = android.graphics.Color.BLACK
                circleRadius = 6f
                setDrawValues(true)
                setDrawCircles(true)
                setCircleColors(circleColors)
                lineWidth = 2f
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}