package bandlogger

import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM

/** The SVG is the supplied authoritative file, bundled byte-for-byte and rendered offline. */
object Branding {
    const val QUATERNITY = "quaternity"
    const val LOGO_RESOURCE = "branding/QuaternityLogo.svg"
    const val LOGO_WIDTH = 1332
    const val LOGO_HEIGHT = 1411
    fun hasLogo(project: Project) = project.brandId == QUATERNITY
    fun logoBytes(): ByteArray = requireNotNull(Branding::class.java.classLoader.getResourceAsStream(LOGO_RESOURCE)) { "Bundled Quaternity logo is missing." }.use { it.readBytes() }
    val logoSha256: String by lazy { CoverGenerator.sha256(logoBytes()) }
    val logoPng: ByteArray by lazy {
        Data.makeFromBytes(logoBytes()).use { data ->
            SVGDOM(data).use { svg ->
                Surface.makeRasterN32Premul(LOGO_WIDTH,LOGO_HEIGHT).use { surface ->
                    surface.canvas.clear(Color.TRANSPARENT)
                    svg.setContainerSize(LOGO_WIDTH.toFloat(),LOGO_HEIGHT.toFloat())
                    svg.render(surface.canvas)
                    surface.makeImageSnapshot().use { image -> requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { it.bytes } }
                }
            }
        }
    }
}
