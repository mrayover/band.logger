package bandlogger

import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM
import kotlin.math.roundToInt
import kotlin.test.*

class BrandingRenderingTest {
    private fun png(width: Int, height: Int, draw: (Canvas) -> Unit): ByteArray =
        Surface.makeRasterN32Premul(width, height).use { surface ->
            surface.canvas.clear(Color.TRANSPARENT)
            draw(surface.canvas)
            surface.makeImageSnapshot().use { image ->
                requireNotNull(image.encodeToData(EncodedImageFormat.PNG)).use { it.bytes }
            }
        }

    @Test fun vectorPlaybackMatchesOriginalSvgAtEveryUiSizeAndWindowsDensity() {
        for (density in listOf(1f, 1.25f, 1.5f, 2f)) {
            for (dp in listOf(20, 36, 64, 120)) {
                val pixels = (dp * density).roundToInt()
                val actual = png(pixels, pixels) { Branding.drawLogo(it, pixels.toFloat(), pixels.toFloat()) }
                // Independent reference: parse the authoritative SVG anew at the target
                // viewport, rather than resampling the old fixed-resolution PNG.
                val expected = png(pixels, pixels) { canvas ->
                    Data.makeFromBytes(Branding.logoBytes()).use { data ->
                        SVGDOM(data).use { svg ->
                            svg.setContainerSize(pixels.toFloat(), pixels.toFloat())
                            svg.render(canvas)
                        }
                    }
                }
                val image = ImageIO.read(actual.inputStream())
                val reference = ImageIO.read(expected.inputStream())
                assertEquals(pixels, image.width)
                assertEquals(pixels, image.height)
                var difference = 0L
                for (y in 0 until pixels) for (x in 0 until pixels) {
                    val a = image.getRGB(x, y)
                    val b = reference.getRGB(x, y)
                    for (shift in listOf(0, 8, 16, 24)) {
                        difference += kotlin.math.abs(((a ushr shift) and 255) - ((b ushr shift) and 255))
                    }
                }
                // Small float-transform/antialias rounding is allowed, not geometry/color changes.
                assertTrue(difference.toDouble() / (pixels * pixels * 4) < 0.5, "$dp dp at $density: $difference")
                assertEquals(0, image.getRGB(0, 0) ushr 24)
                assertEquals(0, image.getRGB(pixels - 1, 0) ushr 24)
                assertTrue((0 until pixels).any { x -> (0 until pixels).any { y -> image.getRGB(x, y) ushr 24 != 0 } })
                System.getProperty("bandlogger.coverReviewDir")?.let { directory ->
                    val root = Files.createDirectories(Path.of(directory))
                    Files.write(root.resolve("logo-$dp-${(density * 100).toInt()}.png"), actual)
                }
            }
        }
    }

    @Test fun vectorDrawingFitsNonSquareBoundsAndRestoresTheCallerCanvas() {
        val actual = png(180, 80) { canvas ->
            val before = canvas.localToDeviceAsMatrix33
            Branding.drawLogo(canvas, 180f, 80f)
            assertEquals(before, canvas.localToDeviceAsMatrix33)
            Branding.drawLogo(canvas, 0f, 80f)
            assertEquals(before, canvas.localToDeviceAsMatrix33)
        }
        val image = ImageIO.read(actual.inputStream())
        // Original viewBox is centered with transparent space, not stretched to the bounds.
        for (y in 0 until 80) {
            assertEquals(0, image.getRGB(0, y) ushr 24)
            assertEquals(0, image.getRGB(179, y) ushr 24)
        }
        assertTrue(image.getRGB(90, 25) ushr 24 > 0)
    }
}
