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
    fun header(title: String): String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<forScore kind=\"setlist\" version=\"1.0\" title=\"${escape(title)}\">\n"
    fun scoreElement(score: ProofScore): String {
        require(score.filename.endsWith(".pdf", true) && !score.filename.contains('/') && !score.filename.contains('\\')) { "Supply a PDF filename, not a path." }
        require(score.pdf.take(5).toByteArray().decodeToString() == "%PDF-") { "Expected PDF bytes." }
        return "  <score title=\"${escape(score.title)}\" path=\"${escape(score.filename)}\" data=\"${Base64.encode(score.pdf)}\" />\n"
    }
    fun validReferenceFilename(filename: String): Boolean = filename.isNotBlank() && filename == filename.trim() &&
        filename.endsWith(".pdf", true) && filename.length > 4 && filename.none { it in "<>:\"/\\|?*" || it.code < 32 }
    fun referenceElement(title: String, filename: String): String {
        require(validReferenceFilename(filename)) { "Supply the exact safe PDF filename, not a path." }
        return "  <score title=\"${escape(title)}\" path=\"${escape(filename)}\" />\n"
    }
    fun placeholderElement(title: String): String = "  <placeholder title=\"${escape(title)}\" />\n"
    const val footer = "</forScore>\n"
    fun generate(title: String, scores: List<ProofScore>): String = buildString {
        append(header(title))
        scores.forEach { append(scoreElement(it)) }
        append(footer)
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