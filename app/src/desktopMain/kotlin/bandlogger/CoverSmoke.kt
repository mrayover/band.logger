package bandlogger

import java.nio.file.*
import java.util.Base64
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper

/** Executable diagnostics use synthetic projects only, never the user's workspace. */
object CoverSmoke {
    fun run(seed: Project, dir: Path) {
        check(Branding.logoSha256=="7bbb4839c373391a24e9dce81ac7517e634c1428762fd2e884803150208af524")
        val store=FileWorkspaceStore(dir.resolve("covers-smoke.json"))
        val root=System.getProperty("bandlogger.coverSmokeDestination") ?: Files.createDirectories(dir.resolve("cover-exports")).toString()
        var project=seed.copy(brandId=Branding.QUATERNITY,includeBrandedSetCovers=true,exportDestination=root,eventExports=emptyList())
        var event=project.events.single().copy(title="Branded Synthetic Gig",date="2026-10-22").addSet(::newId)
        event=event.addTune(project,event.entries.first().tuneId,::newId,event.sets.last().id)
            .addTune(project,event.entries.first().tuneId,::newId,event.sets.last().id)
        project=project.copy(events=listOf(event))
        fun save(value: Project){store.save(Workspace().saveProject(value));project=value}
        save(project)
        val plan=EventExportPlanner.preview(project,event.id)
        val branded=EventExports.publish(project,plan,root,true,true,EventConflict.NEW,::save)
        check(branded.record.covers.size==2)
        inspect(project,plan,branded)
        project=store.load().projects.single();check(project.includeBrandedSetCovers)
        project=project.copy(includeBrandedSetCovers=false)
        val plainPlan=EventExportPlanner.preview(project,event.id)
        val plain=EventExports.publish(project,plainPlan,root,true,true,EventConflict.REVISION,::save)
        check(plain.record.covers.isEmpty());inspect(project,plainPlan,plain)
        check(store.load().projects.single().eventExports.size==2)
        Files.writeString(dir.resolve("covers-passed.txt"),"Authoritative SVG rendered offline; generated PDF/logo/Lato, complete mixed cover/reference XML, one ZIP/member, fallback and persisted history passed.")
    }
    private fun inspect(project: Project, plan: EventExportPlan, output: EventExportResult) {
        output.zipNames.forEach { name ->
            val member=plan.members.single { "${it.baseName}.zip"==name }
            val files=linkedMapOf<String,ByteArray>()
            ZipInputStream(Files.newInputStream(output.folder.resolve(name))).use { input ->
                while(true){val e=input.nextEntry ?: break;files[e.name]=input.readBytes();input.closeEntry()}
            }
            check(files.size==2 && files.keys.none { it.endsWith(".pdf") })
            val xml=files.getValue("${member.baseName}.4ss").toString(Charsets.UTF_8)
            val factory=DocumentBuilderFactory.newInstance();factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true)
            val nodes=factory.newDocumentBuilder().parse(xml.byteInputStream()).documentElement.childNodes
            val scores=(0 until nodes.length).mapNotNull { nodes.item(it) as? org.w3c.dom.Element }
            val coverMap=linkedMapOf<Int,ProofScore>()
            scores.filter { it.hasAttribute("data") }.forEach { row ->
                val record=output.record.covers.single { it.filename==row.getAttribute("path") }
                val bytes=Base64.getDecoder().decode(row.getAttribute("data"))
                check(CoverGenerator.sha256(bytes)==record.sha256 && record.logoSha256==Branding.logoSha256)
                Loader.loadPDF(bytes).use { pdf ->
                    val text=PDFTextStripper().getText(pdf)
                    check(pdf.numberOfPages==1 && text.contains(project.name) && text.contains(plan.event.title) && text.contains("Set ${record.setNumber}"))
                    check(pdf.getPage(0).resources.xObjectNames.any())
                }
                coverMap[record.setNumber]=ProofScore(row.getAttribute("title"),record.filename,bytes)
            }
            check(coverMap.size==output.record.covers.size)
            EventExporter.validateXml(xml,coverMap.values.toList())
            check(xml==EventExportPlanner.xml(member,coverMap))
            check(scores.filter { it.tagName=="score" && !it.hasAttribute("data") }.map { it.getAttribute("path") }==member.entries.mapNotNull { it.filename })
        }
    }
    fun restart(dir: Path) {
        val store=FileWorkspaceStore(dir.resolve("covers-smoke.json"));var project=store.load().projects.single()
        check(project.brandId==Branding.QUATERNITY && !project.includeBrandedSetCovers)
        check(project.eventExports.size==2 && project.eventExports.first().covers.size==2 && project.eventExports.last().covers.isEmpty())
        val id=project.events.single().id
        EventExports.deleteEvent(project,id) { store.save(Workspace().saveProject(it));project=it }
        check(project.events.isEmpty() && project.eventExports.all { it.state==EventExportState.DELETED })
        check(project.eventExports.none { Files.exists(Path.of(it.folder)) })
        Files.writeString(dir.resolve("covers-restart-passed.txt"),"Separate process reopened branding/cover preference and mixed history, then deleted both verified tracked cover/plain exports with no orphaned packages.")
    }
}
