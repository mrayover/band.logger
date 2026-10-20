package bandlogger

import kotlin.io.encoding.Base64

data class ProofScore(val title: String, val filename: String, val pdf: ByteArray)
object ForScore {
    private fun escape(value: String): String {
        require(value.none { it.code < 32 && it != '\t' && it != '\n' && it != '\r' }) { "XML cannot contain control characters." }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&apos;")
            .replace("\t", "&#9;").replace("\n", "&#10;").replace("\r", "&#13;")
    }
    fun generate(title: String, scores: List<ProofScore>): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        append("<forScore kind=\"setlist\" version=\"1.0\" title=\"${escape(title)}\">\n")
        scores.forEach {
            require(it.filename.endsWith(".pdf", true) && !it.filename.contains('/') && !it.filename.contains('\\')) { "Supply a PDF filename, not a path." }
            require(it.pdf.take(5).toByteArray().decodeToString() == "%PDF-") { "Expected PDF bytes." }
            append("  <score title=\"${escape(it.title)}\" path=\"${escape(it.filename)}\" data=\"${Base64.encode(it.pdf)}\" />\n")
        }
        append("</forScore>\n")
    }
}
// Entirely synthetic one-page PDF; offsets are computed from ASCII bytes.
fun syntheticPdf(): ByteArray {
    val content = "BT /F1 20 Tf 50 750 Td (band.logger - synthetic import proof) Tj 0 -35 Td (No copyrighted music. Chunk 1.) Tj ET\n"
    val objects = listOf(
        "<< /Type /Catalog /Pages 2 0 R >>",
        "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        "<< /Length ${content.length} >>\nstream\n${content}endstream"
    )
    val out = StringBuilder("%PDF-1.4\n")
    val offsets = objects.mapIndexed { i, obj -> out.length.also { out.append("${i + 1} 0 obj\n$obj\nendobj\n") } }
    val xref = out.length
    out.append("xref\n0 6\n0000000000 65535 f \n")
    offsets.forEach { out.append(it.toString().padStart(10, '0') + " 00000 n \n") }
    out.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n$xref\n%%EOF\n")
    return out.toString().encodeToByteArray()
}
fun proofXml(): String = ForScore.generate("band.logger & forScore — technical proof", listOf(
    ProofScore("Synthetic & safe <one>", "Synthetic & safe.pdf", syntheticPdf()),
    ProofScore("Second page \"order check\"", "Second proof.pdf", syntheticPdf())
))