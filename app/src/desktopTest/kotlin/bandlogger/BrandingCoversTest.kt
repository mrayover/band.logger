package bandlogger

import java.nio.file.*
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.rendering.PDFRenderer
import kotlin.test.*

class BrandingCoversTest {
    private fun project(covers: Boolean = true): Project {
        val member=Member("m","Marcus",emptyList(),listOf("Guitar","C"))
        val sax=Member("s","Jordan",emptyList(),listOf("Bb"))
        val tunes=listOf(Tune("a","Alpha & One","alpha"),Tune("b","Beta","beta"),Tune("c","Missing tune","missing"))
        fun chart(id: String,part: String)=MasterItem(id,tunes.first { it.id==id }.title,"chart-$id-$part",part,0,"Z:/never-read","unused","Q $id -r00- $part.pdf","a".repeat(64))
        val master=LibraryMaster("master","p","Quaternity","2026-10-21T00:00:00Z","Z:/never-read",listOf(
            MasterMember("m","Marcus",listOf("C"),"m.zip","b".repeat(64),listOf(chart("a","C"),chart("b","C"))),
            MasterMember("s","Jordan",listOf("Bb"),"s.zip","c".repeat(64),listOf(chart("a","Bb")))))
        val event=BandEvent("event","p","Synthetic Venue & Friends",EventType.GIG,"2026-10-22",EventSet("one",listOf(EventTuneEntry("1","a"),EventTuneEntry("2","b"),EventTuneEntry("3","a"))),listOf(EventSet("empty"),EventSet("three",listOf(EventTuneEntry("4","b"),EventTuneEntry("5","c")))))
        return Project("p","Quaternity","Q",members=listOf(member,sax),catalog=Catalog(tunes=tunes),events=listOf(event),masters=listOf(master),brandId=Branding.QUATERNITY,includeBrandedSetCovers=covers)
    }
    private fun plan(p: Project)=EventExportPlanner.preview(p,"event")
    private fun rows(xml: String): List<org.w3c.dom.Element> {
        val f=DocumentBuilderFactory.newInstance();f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true)
        val nodes=f.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray())).documentElement.childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? org.w3c.dom.Element }
    }
    private fun zip(path: Path): Map<String,ByteArray> {
        val entries=linkedMapOf<String,ByteArray>()
        ZipInputStream(Files.newInputStream(path)).use { input -> while(true){val entry=input.nextEntry ?: break;entries[entry.name]=input.readBytes();input.closeEntry()} }
        return entries
    }
    @Test fun bundledSvgIsExactAuthoritativeArtworkAndRendersAtItsOriginalRatio() {
        assertEquals("7bbb4839c373391a24e9dce81ac7517e634c1428762fd2e884803150208af524",Branding.logoSha256)
        val image=ImageIO.read(Branding.logoPng.inputStream())
        assertEquals(1332,image.width);assertEquals(1411,image.height)
        assertTrue((0 until image.width step 10).any { x -> (0 until image.height step 10).any { y -> (image.getRGB(x,y) and 0xFFFFFF)==0xF7C917 } })
        assertTrue(Branding.logoBytes().toString(Charsets.UTF_8).contains("viewBox=\"0 0 1332 1411\""))
    }
    @Test fun logoSelectionIsProjectSpecificAndSurvivesRename() {
        val p=project();assertTrue(Branding.hasLogo(p));assertTrue(Branding.hasLogo(p.copy(name="Renamed ensemble")))
        assertFalse(Branding.hasLogo(Project("other","Another band")))
        assertFalse(Branding.hasLogo(Project("other","Quaternity","Q"))) // current-schema opt-out stays explicit
        assertTrue(Branding.hasLogo(quaternity("starter",::newId)))
        val loaded=WorkspaceCodec.decode(WorkspaceCodec.encode(Workspace().saveProject(p).saveProject(Project("other","Other"))))
        assertTrue(Branding.hasLogo(loaded.projects.first()));assertFalse(Branding.hasLogo(loaded.projects.last()))
    }
    @Test fun coverPdfContainsSavedDetailsEmbeddedLatoAndExactRenderedLogo() {
        val p=project();val bytes=CoverGenerator.generate(p,plan(p)).first().score.pdf
        Loader.loadPDF(bytes).use { pdf ->
            assertEquals(1,pdf.numberOfPages)
            val text=PDFTextStripper().getText(pdf)
            listOf("Quaternity","Synthetic Venue & Friends","2026-10-22","Set 1","PERFORMANCE").forEach { assertTrue(text.contains(it),text) }
            val page=pdf.getPage(0)
            page.resources.fontNames.forEach { assertTrue(page.resources.getFont(it).isEmbedded) }
            val logo=page.resources.xObjectNames.mapNotNull { page.resources.getXObject(it) as? PDImageXObject }.single().image
            val source=ImageIO.read(Branding.logoPng.inputStream())
            assertEquals(source.width,logo.width);assertEquals(source.height,logo.height)
            for(x in 0 until source.width step 37)for(y in 0 until source.height step 43) assertEquals(source.getRGB(x,y),logo.getRGB(x,y))
        }
    }
    @Test fun rehearsalUndatedAndLongTitleRenderWithoutInventedMetadata() {
        val p=project();val event=p.events.single().copy(type=EventType.REHEARSAL,date=null,title="Rehearsal Café & Friends - a detailed saved rehearsal title for all three sets")
        Loader.loadPDF(CoverGenerator.pdf(p,event,3)).use { pdf ->
            val text=PDFTextStripper().getText(pdf)
            assertTrue(text.contains("REHEARSAL"));assertTrue(text.contains("Café"));assertTrue(text.contains("Date not set"));assertTrue(text.contains("Set 3"))
            assertFalse(text.contains("2026-10-22"));assertFalse(text.contains("19:00"))
            assertEquals(612,PDFRenderer(pdf).renderImage(0).width)
        }
    }
    @Test fun unbrandedProjectsUseTheirOwnNameWithoutQuaternityArtwork() {
        val p=project().copy(name="Other Ensemble",brandId=null)
        Loader.loadPDF(CoverGenerator.pdf(p,p.events.single(),1)).use { pdf ->
            assertTrue(PDFTextStripper().getText(pdf).contains("Other Ensemble"))
            assertFalse(pdf.getPage(0).resources.xObjectNames.any())
        }
    }
    @Test fun onlyNonemptySetsReceiveOneCoverWithStableReadableNames() {
        val p=project();val first=CoverGenerator.generate(p,plan(p));val second=CoverGenerator.generate(p,plan(p))
        assertEquals(listOf(1,3),first.map { it.number });assertEquals(listOf("one","three"),first.map { it.record.setId })
        assertEquals(first.map { it.score.filename },second.map { it.score.filename })
        assertTrue(first.all { it.score.filename.contains("2026-10-22") && !it.score.filename.contains("event-") && it.record.logoSha256==Branding.logoSha256 })
        first.forEach { assertEquals(CoverGenerator.sha256(it.score.pdf),it.record.sha256) }
    }
    @Test fun mixedXmlHasOneCoverPerSetExactReferencesRepeatsAndMissingPlaceholders() {
        val p=project();val planned=plan(p);val covers=CoverGenerator.generate(p,planned).associate { it.number to it.score }
        val member=planned.members.first();val xml=EventExportPlanner.xml(member,covers);val content=rows(xml)
        assertEquals(listOf(true,false,false,false,true,false,false),content.map { it.hasAttribute("data") })
        assertEquals(listOf("Q a -r00- C.pdf","Q b -r00- C.pdf","Q a -r00- C.pdf","Q b -r00- C.pdf"),content.filter { it.tagName=="score" && !it.hasAttribute("data") }.map { it.getAttribute("path") })
        assertEquals(listOf("Missing chart — Missing tune"),content.filter { it.tagName=="placeholder" }.map { it.getAttribute("title") })
        content.filter { it.hasAttribute("data") }.forEach { node -> assertContentEquals(covers.values.single { it.filename==node.getAttribute("path") }.pdf,Base64.getDecoder().decode(node.getAttribute("data"))) }
        EventExporter.validateXml(xml,covers.values.toList())
    }
    @Test fun strictValidationRejectsAnyEmbeddedMusicChartOrMissingCover() {
        val p=project();val planned=plan(p);val covers=CoverGenerator.generate(p,planned).associate { it.number to it.score }
        val xml=EventExportPlanner.xml(planned.members.first(),covers)
        assertFails { EventExporter.validateXml(xml) }
        val changed=xml.replace("path=\"Q a -r00- C.pdf\"","path=\"Q a -r00- C.pdf\" data=\"${Base64.getEncoder().encodeToString(syntheticPdf())}\"")
        assertFails { EventExporter.validateXml(changed,covers.values.toList()) }
        assertFails { EventExportPlanner.xml(planned.members.first(),mapOf(1 to covers.getValue(1))) }
        assertFails { EventExporter.validateXml(EventExportPlanner.xml(planned.members.first()),covers.values.toList()) }
    }
    @Test fun exportedZipContainsOnlyOneWholeEventXmlAndInstructionsWithIntactCoverData() {
        val p=project();val root=Files.createTempDirectory("cover-zip")
        val output=EventExporter.generate(p,plan(p),root.toString(),true,true)
        assertEquals(2,output.zipNames.size);assertEquals(2,output.record.covers.size)
        output.zipNames.forEach { name ->
            val files=zip(output.folder.resolve(name));assertEquals(2,files.size);assertEquals(1,files.keys.count { it.endsWith(".4ss") });assertFalse(files.keys.any { it.endsWith(".pdf") })
            val content=rows(files.entries.single { it.key.endsWith(".4ss") }.value.toString(Charsets.UTF_8))
            val covers=content.filter { it.hasAttribute("data") };assertEquals(2,covers.size)
            covers.forEach { node -> val bytes=Base64.getDecoder().decode(node.getAttribute("data"));assertEquals(output.record.covers.single { it.filename==node.getAttribute("path") }.sha256,CoverGenerator.sha256(bytes));Loader.loadPDF(bytes).use { assertEquals(1,it.numberOfPages) } }
            assertTrue(files.getValue("READ-ME.txt").toString(Charsets.UTF_8).contains("may create additional cover copies"))
        }
        Files.newDirectoryStream(output.folder).use { assertEquals(3,it.count()) }
    }
    @Test fun coverDisabledOutputPreservesPlainHeadingsAndReferenceOnlyContract() {
        val p=project(false);val planned=plan(p);val root=Files.createTempDirectory("plain-cover-fallback")
        val output=EventExporter.generate(p,planned,root.toString(),true,true)
        assertTrue(output.record.covers.isEmpty())
        output.zipNames.forEach { name ->
            val member=planned.members.single { "${it.baseName}.zip"==name };val files=zip(output.folder.resolve(name))
            val xml=files.entries.single { it.key.endsWith(".4ss") }.value.toString(Charsets.UTF_8)
            assertEquals(EventExportPlanner.xml(member),xml);assertFalse(xml.contains("data="));assertEquals(listOf("Set 1","Set 3"),rows(xml).filter { it.tagName=="placeholder" && it.getAttribute("title").startsWith("Set ") }.map { it.getAttribute("title") })
            EventExporter.validateXml(xml)
        }
    }
    @Test fun preferenceChangesInvalidatePreviewAndSurviveRestart() {
        val p=project(false);val before=plan(p);val root=Files.createTempDirectory("cover-preference")
        assertFails { EventExporter.generate(p.copy(includeBrandedSetCovers=true),before,root.toString(),true,true) }
        val store=FileWorkspaceStore(root.resolve("workspace.json"));store.save(Workspace().saveProject(p.copy(includeBrandedSetCovers=true)))
        val loaded=FileWorkspaceStore(root.resolve("workspace.json")).load().projects.single()
        assertTrue(loaded.includeBrandedSetCovers);assertTrue(plan(loaded).includeCovers)
        assertFalse(Project("new","New").includeBrandedSetCovers)
    }
    @Test fun brandedRevisionsReplacementAndDeletionKeepTrackingAndHistoricalMasters() {
        val root=Files.createTempDirectory("tracked-covers");var p=project().copy(exportDestination=root.toString())
        val store=FileWorkspaceStore(root.resolve("workspace.json"));fun save(value: Project){store.save(Workspace().saveProject(value));p=value}
        val first=EventExports.publish(p,plan(p),root.toString(),true,true,EventConflict.NEW,::save)
        val second=EventExports.publish(p,plan(p),root.toString(),true,true,EventConflict.REPLACE,::save)
        assertFalse(Files.exists(first.folder));assertTrue(Files.exists(second.folder));assertEquals(2,p.eventExports.last().covers.size)
        p=store.load().projects.single();val masters=p.masters
        EventExports.deleteEvent(p,"event",::save)
        assertFalse(Files.exists(second.folder));assertTrue(p.events.isEmpty());assertEquals(masters,p.masters)
        assertTrue(p.eventExports.all { it.state==EventExportState.DELETED && it.covers.size==2 })
    }
    @Test fun schemaEightMigrationPreservesAllHistoryAndImmutableOriginalBytes() {
        val root=Files.createTempDirectory("branding-migration");val p=project(false).copy(brandId=null)
        val old=WorkspaceCodec.encode(Workspace().saveProject(p).saveProject(Project("other","Other"))).replace("\"schemaVersion\": 9","\"schemaVersion\": 8")
        val path=root.resolve("workspace.json");Files.writeString(path,old);val store=FileWorkspaceStore(path)
        val loaded=store.load();assertEquals(9,loaded.schemaVersion);assertEquals(old,Files.readString(path))
        assertEquals(p.copy(brandId=Branding.QUATERNITY),loaded.projects.first());assertNull(loaded.projects.last().brandId)
        store.save(loaded);store.save(store.load());assertEquals(old,Files.readString(root.resolve("workspace.pre-branding-covers.backup.json")))
        assertFalse(loaded.projects.first().includeBrandedSetCovers)
    }
    @Test fun currentSchemaExplicitLogoOptOutIsNotReappliedByName() {
        val p=project().copy(brandId=null)
        assertNull(WorkspaceCodec.decode(WorkspaceCodec.encode(Workspace().saveProject(p))).projects.single().brandId)
    }
    @Test fun renderReviewArtifactsFromTheActualApplicationGenerator() {
        val directory=System.getProperty("bandlogger.coverReviewDir") ?: return
        val root=Files.createDirectories(Path.of(directory));val p=project()
        val samples=listOf("gig-cover" to p.events.single(),"rehearsal-cover" to p.events.single().copy(type=EventType.REHEARSAL,title="Rehearsal Café & Friends",date=null),"long-title-cover" to p.events.single().copy(title="A Long Saved Performance Title for an Evening of Music with Friends and Special Guests"))
        samples.forEach { (name,event) -> val bytes=CoverGenerator.pdf(p,event,1);Files.write(root.resolve("$name.pdf"),bytes);Loader.loadPDF(bytes).use { ImageIO.write(PDFRenderer(it).renderImageWithDPI(0,100f),"PNG",root.resolve("$name.png").toFile()) } }
        Files.write(root.resolve("official-logo.png"),Branding.logoPng)
    }

    @Test fun legacyEventSidecarWithoutCoverFieldsStillVerifiesAndCleansUp() {
        val root=Files.createTempDirectory("legacy-plain-event");var p=project(false).copy(exportDestination=root.toString())
        fun save(value: Project){p=value}
        val output=EventExports.publish(p,plan(p),root.toString(),true,true,EventConflict.NEW,::save)
        val sidecar=output.folder.resolve("event-export.json")
        val old=Files.readString(sidecar).replace(",\n    \"covers\": []", "")
        assertFalse(old.contains("\"covers\""));Files.writeString(sidecar,old)
        val recovery=EventExports.discover(p.copy(eventExports=emptyList()))
        assertEquals(output.record.id,recovery.verified.single().id)
        EventExports.deleteEvent(p,"event",::save)
        assertTrue(p.eventExports.all { it.state==EventExportState.DELETED });assertFalse(Files.exists(output.folder))
    }
}
