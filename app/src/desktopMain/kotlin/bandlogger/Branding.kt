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
    // One immutable vector recording shared by every size/density. SVGDOM and its
    // mutable viewport are discarded after recording; no bitmap is used by the UI.
    private val logoPicture: Picture by lazy {
        Data.makeFromBytes(logoBytes()).use { data ->
            SVGDOM(data).use { svg ->
                svg.setContainerSize(LOGO_WIDTH.toFloat(), LOGO_HEIGHT.toFloat())
                PictureRecorder().use { recorder ->
                    svg.render(recorder.beginRecording(Rect.makeWH(LOGO_WIDTH.toFloat(), LOGO_HEIGHT.toFloat())))
                    recorder.finishRecordingAsPicture()
                }
            }
        }
    }

    /** Fits the full original viewBox into device pixels, without a background or clipping. */
    fun drawLogo(canvas: Canvas, width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val scale = minOf(width / LOGO_WIDTH, height / LOGO_HEIGHT)
        canvas.save()
        try {
            canvas.translate((width - LOGO_WIDTH * scale) / 2f, (height - LOGO_HEIGHT * scale) / 2f)
            canvas.scale(scale, scale)
            canvas.drawPicture(logoPicture)
        } finally {
            canvas.restore()
        }
    }

    // PDF-only lossless raster: 1332 px across 220 pt is ~436 dpi, sufficient for
    // ordinary printing and iPad display. It is never downsampled for desktop UI.
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
