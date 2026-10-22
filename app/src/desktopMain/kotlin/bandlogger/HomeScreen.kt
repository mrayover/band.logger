package bandlogger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(project: Project, editProject: () -> Unit, openDistribution: () -> Unit, createEvent: (EventType) -> Unit, openEvents: () -> Unit) {

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Keep the reference's two columns at desktop sizes; stack when space is limited.
        val narrow = maxWidth < 500.dp
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (narrow) {
                HomeCard("Create Rehearsal", "Plan what to work on", primary, 0, Modifier.fillMaxWidth()) { createEvent(EventType.REHEARSAL) }
                HomeCard("Create Gig", "Build a performance", primary, 1, Modifier.fillMaxWidth()) { createEvent(EventType.GIG) }
                HomeCard("Events", null, secondary, 2, Modifier.fillMaxWidth()) { openEvents() }
                HomeCard("Pending Distribution", null, secondary, 3, Modifier.fillMaxWidth(), openDistribution)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HomeCard("Create Rehearsal", "Plan what to work on", primary, 0, Modifier.weight(1f)) { createEvent(EventType.REHEARSAL) }
                    HomeCard("Create Gig", "Build a performance", primary, 1, Modifier.weight(1f)) { createEvent(EventType.GIG) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HomeCard("Events", null, secondary, 2, Modifier.weight(1f)) { openEvents() }
                    HomeCard("Pending Distribution", null, secondary, 3, Modifier.weight(1f), openDistribution)
                }
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("${project.members.size} members · Filename prefix: ${project.prefix.ifBlank { "None" }}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = editProject) { Text("Edit project") }
    }

}

@Composable
private fun HomeCard(title: String, subtitle: String?, accent: Color, icon: Int, modifier: Modifier, onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val hovered by interactions.collectIsHoveredAsState()
    Card(onClick = onClick, modifier = modifier.height(184.dp), shape = RoundedCornerShape(18.dp),
        interactionSource = interactions,
        border = if (focused) BorderStroke(2.dp, accent) else if (hovered) BorderStroke(1.dp, accent.copy(alpha = 0.7f)) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            HomeIcon(icon, accent)
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (subtitle != null) {
                Spacer(Modifier.height(12.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f), textAlign = TextAlign.Center)
            }
        }
    }
}

// Decorative vector icons inherit project accents; the containing button supplies its text label.
@Composable
private fun HomeIcon(kind: Int, color: Color) {
    Canvas(Modifier.size(30.dp)) {
        val unit = size.width / 24f
        val stroke = Stroke(width = 1.7f * unit, cap = StrokeCap.Round)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(color, Offset(x1 * unit, y1 * unit), Offset(x2 * unit, y2 * unit), stroke.width, StrokeCap.Round)
        when (kind) {
            0 -> {
                drawCircle(color, 8.3f * unit, style = stroke)
                line(12f, 6.5f, 12f, 12f); line(12f, 12f, 16f, 12f)
            }
            1 -> {
                drawRoundRect(color, Offset(9f * unit, 3f * unit), Size(6f * unit, 11f * unit), CornerRadius(3f * unit), style = stroke)
                drawArc(color, 0f, 180f, false, Offset(6f * unit, 6f * unit), Size(12f * unit, 12f * unit), style = stroke)
                line(12f, 18f, 12f, 21f); line(9f, 21f, 15f, 21f)
            }
            2 -> {
                drawRoundRect(color, Offset(4f * unit, 5f * unit), Size(16f * unit, 16f * unit), CornerRadius(2f * unit), style = stroke)
                line(8f, 3f, 8f, 7f); line(16f, 3f, 16f, 7f); line(4f, 10f, 20f, 10f)
                listOf(8f to 14f, 12f to 14f, 16f to 14f, 8f to 17f, 12f to 17f).forEach { (x, y) -> drawCircle(color, 0.7f * unit, Offset(x * unit, y * unit)) }
            }
            else -> {
                val outline = Path().apply {
                    moveTo(12f * unit, 3f * unit); lineTo(20f * unit, 7.5f * unit)
                    lineTo(20f * unit, 16.5f * unit); lineTo(12f * unit, 21f * unit)
                    lineTo(4f * unit, 16.5f * unit); lineTo(4f * unit, 7.5f * unit); close()
                }
                drawPath(outline, color, style = stroke)
                line(4f, 7.5f, 12f, 12f); line(20f, 7.5f, 12f, 12f); line(12f, 12f, 12f, 21f)
            }
        }
    }
}
