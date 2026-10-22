package bandlogger

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory

data class GeneratedCover(val number: Int, val score: ProofScore, val record: EventCoverRecord)

/** Produces only generated cover PDFs. It has no chart-source or installed-library reader. */
object CoverGenerator {
    fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    fun generate(project: Project, plan: EventExportPlan): List<GeneratedCover> = plan.event.sets.mapIndexedNotNull { index, set ->
        if (set.entries.isEmpty()) null else {
            val number = index + 1
            val type = if (plan.event.type == EventType.GIG) "Gig" else "Rehearsal"
            val title = "${project.name} - ${plan.event.title} - Set $number"
            val filename = "band.logger Cover - ${EventExportPlanner.safeLabel(project.name,32)} - $type - ${plan.event.date ?: "Undated"} - ${EventExportPlanner.safeLabel(plan.event.title,40)} - Set $number.pdf"
            require(plan.members.none { member -> member.entries.any { it.filename.equals(filename,true) } }) { "A cover filename conflicts with a chart reference. Use plain set headings." }
            val pdf = pdf(project,plan.event,number)
            GeneratedCover(number,ProofScore(title,filename,pdf),EventCoverRecord(set.id,number,filename,sha256(pdf),if (Branding.hasLogo(project)) Branding.logoSha256 else null))
        }
    }
    fun pdf(project: Project, event: BandEvent, number: Int): ByteArray {
        require(number in 1..event.sets.size && event.sets[number-1].entries.isNotEmpty())
        PDDocument().use { doc ->
            val page = PDPage(PDRectangle.LETTER); doc.addPage(page)
            fun font(name: String): PDType0Font {
                val bytes=requireNotNull(CoverGenerator::class.java.classLoader.getResourceAsStream("fonts/$name.ttf")).use { it.readBytes() }
                val ttf=org.apache.fontbox.ttf.TTFParser().parse(org.apache.pdfbox.io.RandomAccessReadBuffer(bytes))
                // Disable substitutions before PDFBox builds the font's Unicode mapping.
                ttf.setEnableGsub(false)
                doc.registerTrueTypeFontForClosing(ttf)
                return PDType0Font.load(doc,ttf,true)
            }
            val regular=font("Lato-Regular"); val bold=font("Lato-Bold")
            val navy=java.awt.Color(0x17,0x1B,0x24); val ivory=java.awt.Color(0xF4,0xF2,0xEA)
            val gold=java.awt.Color(0xE7,0xC6,0x6A); val steel=java.awt.Color(0x76,0x98,0xAD)
            PDPageContentStream(doc,page).use { out ->
                fun rect(x: Float,y: Float,w: Float,h: Float,color: java.awt.Color) { out.setNonStrokingColor(color);out.addRect(x,y,w,h);out.fill() }
                fun width(text: String,font: PDType0Font,size: Float)=font.getStringWidth(text)/1000*size
                fun text(text: String,font: PDType0Font,size: Float,y: Float,color: java.awt.Color) {
                    val x=(612-width(text,font,size))/2
                    out.beginText();out.setFont(font,size);out.setNonStrokingColor(color);out.newLineAtOffset(x,y);out.showText(text);out.endText()
                }
                fun lines(value: String,size: Float): List<String> {
                    val result=mutableListOf<String>();var line=""
                    value.replace(Regex("\\s+")," ").trim().forEach { char ->
                        if (width(line+char,bold,size)>516 && line.isNotEmpty()) {
                            val space=line.lastIndexOf(' ')
                            if (space>0) { result+=line.substring(0,space);line=line.substring(space+1) } else { result+=line;line="" }
                        }
                        line+=char
                    }
                    if(line.isNotEmpty())result+=line
                    return result
                }
                rect(0f,0f,612f,792f,ivory);rect(0f,724f,612f,68f,navy);rect(40f,712f,532f,3f,gold)
                var bandSize=22f
                while(width(project.name,bold,bandSize)>528 && bandSize>10)bandSize-=0.5f
                require(width(project.name,bold,bandSize)<=528) { "Project name is too long for a cover. Use plain headings." }
                text(project.name,bold,bandSize,750f,ivory)
                if(Branding.hasLogo(project)) {
                    val image=ImageIO.read(Branding.logoPng.inputStream())
                    out.drawImage(LosslessFactory.createFromImage(doc,image),196f,438f,220f,220f*Branding.LOGO_HEIGHT/Branding.LOGO_WIDTH)
                } else text(project.name,bold,bandSize,550f,navy)
                text(if(event.type==EventType.GIG) "PERFORMANCE" else "REHEARSAL",regular,12f,413f,steel)
                var size=32f;var titleLines=lines(event.title,size)
                while(titleLines.size*size*1.22f>155 && size>16) {size-=0.5f;titleLines=lines(event.title,size)}
                require(titleLines.size*size*1.22f<=155) { "Event title is too long for a cover. Use plain headings." }
                titleLines.forEachIndexed { i,line -> text(line,bold,size,375f-i*size*1.22f,navy) }
                text(event.date ?: "Date not set",regular,18f,202f,navy)
                rect(40f,48f,532f,120f,navy);rect(40f,165f,532f,3f,gold)
                text("Set $number",bold,48f,91f,ivory)
            }
            doc.documentInformation.title="${project.name} - ${event.title} - Set $number"
            doc.documentInformation.creator="band.logger"
            return ByteArrayOutputStream().use { doc.save(it);it.toByteArray() }
        }
    }
}
