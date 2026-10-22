package bandlogger

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp


/** Compose supplies the fitted draw size in physical pixels, including Windows DPI. */
private class QuaternityPainter : Painter() {
    override val intrinsicSize = Size(Branding.LOGO_WIDTH.toFloat(), Branding.LOGO_HEIGHT.toFloat())
    override fun DrawScope.onDraw() {
        Branding.drawLogo(drawContext.canvas.nativeCanvas, size.width, size.height)
    }
}

@Composable
fun QuaternityMark(modifier: Modifier = Modifier) {
    val painter = remember { QuaternityPainter() }
    Image(painter, "Official Quaternity logo", modifier, contentScale = ContentScale.Fit)
}
@Composable
fun ProjectIdentity(project: Project, compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (Branding.hasLogo(project)) QuaternityMark(Modifier.size(if (compact) 36.dp else 64.dp))
        Text(project.name, style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall)
    }
}
