package bandlogger

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image

@Composable
fun QuaternityMark(modifier: Modifier = Modifier) {
    val skiaImage = remember { Image.makeFromEncoded(Branding.logoPng) }
    val bitmap = remember { skiaImage.toComposeImageBitmap() }
    DisposableEffect(skiaImage) { onDispose { skiaImage.close() } }
    Image(bitmap, "Official Quaternity logo", modifier.background(Color(0xFFF4F2EA)).padding(4.dp), contentScale = ContentScale.Fit)
}
@Composable
fun ProjectIdentity(project: Project, compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (Branding.hasLogo(project)) QuaternityMark(Modifier.size(if (compact) 36.dp else 64.dp))
        Text(project.name, style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displaySmall)
    }
}
