package bandlogger

import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.test.*

class CumulativeEventExportTest {
    private val a = Tune("a", "Existing tune", "existing tune")
    private val b = Tune("b", "New & Dreaming", "new dreaming")
    private val members = listOf(Member("c", "Concert", emptyList(), listOf("C")), Member("bb", "Sax", emptyList(), listOf("Bb")))
    private fun item(t: Tune, part: String, rev: Int = 0) = MasterItem(t.id, t.title, "${t.id}-$part-$rev", part, rev,
        "Z:/unavailable", "not-read.pdf", "Exact ${t.id} r$rev $part.pdf", rev.toString().repeat(64).take(64))
    private fun publication(id: String, charts: Map<String, List<MasterItem>>, time: String = "2026-10-21T12:00:00Z") =
        LibraryMaster(id, "p", "Band", time, "Z:/not-read/$id", members.filter { it.id in charts }.map {
            MasterMember(it.id, it.name, it.preferences, "${it.id}.zip", "a".repeat(64), charts.getValue(it.id))
        })
    private fun project(): Project {
        val event = BandEvent("event", "p", "Show", EventType.GIG, null,
            EventSet("set1", listOf(EventTuneEntry("1", a.id), EventTuneEntry("2", b.id))),
            listOf(EventSet("set2", listOf(EventTuneEntry("3", b.id), EventTuneEntry("4", a.id)))))
        return Project("p", "Band", members=members, catalog=Catalog(tunes=listOf(a,b)), events=listOf(event),
            masters=listOf(publication("master", mapOf("c" to listOf(item(a,"C")), "bb" to listOf(item(a,"Bb"))))))
    }
    private fun add(p: Project, charts: Map<String, List<MasterItem>>, time: String = "2026-10-21T12:00:00Z", anchors: Map<String,String> = members.associate { it.id to "master" }): Project =
        p.recordSupplement(LibrarySupplement(p.supplements.size+1, publication("supp-${p.supplements.size+1}", charts,time), anchors, p.supplements.lastOrNull()?.id,"f".repeat(64)))
    private fun plan(p: Project) = EventExportPlanner.preview(p,"event")

    @Test fun masterOnlyAndUnrecordedCatalogTuneRemainUnchanged() {
        val p=project(); val value=plan(p)
        assertTrue(value.members.all { it.matchedCount==2 && it.missingCount==2 })
        assertEquals("Library Master",value.members.first().entries.first().distribution?.sourceLabel)
        assertTrue(value.members.all { it.entries[1].filename==null })
    }
    @Test fun introducedTuneResolvesExactPerMemberPartsAndSource() {
        val p=add(project(),mapOf("c" to listOf(item(b,"C")),"bb" to listOf(item(b,"Bb"))))
        val value=plan(p)
        assertTrue(value.members.all { it.matchedCount==4 && it.missingCount==0 })
        assertEquals(listOf("Exact b r0 C.pdf","Exact b r0 Bb.pdf"),value.members.map { it.entries[1].filename })
        assertTrue(value.members.all { it.entries[1].distribution?.sourceLabel=="Supplement 001" })
        assertTrue(value.members.all { it.entries[0].distribution?.sourceLabel=="Library Master" })
        assertTrue(value.needsUndatedAcknowledgement); assertFalse(value.needsMissingAcknowledgement)
        assertFailsWith<IllegalArgumentException> { value.requireAcknowledgements(false,false) }
        value.requireAcknowledgements(false,true)
    }
    @Test fun memberWithoutAllocationStaysMissing() {
        val p=add(project(),mapOf("c" to listOf(item(b,"C"))))
        assertEquals(listOf(4,2),plan(p).members.map { it.matchedCount })
        assertTrue(plan(p).members[1].entries[1].reason!!.contains("applicable supplements"))
    }
    @Test fun numberedPublicationsOverrideDespiteReversedTimestamps() {
        val first=add(project(),mapOf("bb" to listOf(item(b,"Bb"))),"2026-10-23T00:00:00Z")
        val second=add(first,mapOf("bb" to listOf(item(b,"Bb",1))),"2026-10-20T00:00:00Z")
        val result=plan(second).members[1]
        assertEquals("Exact b r1 Bb.pdf",result.entries[1].filename)
        assertEquals("Supplement 002",result.entries[1].distribution?.sourceLabel)
        assertEquals("Exact a r0 Bb.pdf",result.entries[0].filename)
        assertEquals("Exact b r1 Bb.pdf",result.entries[2].filename)
    }
    @Test fun newPartKeepsIndependentRevisionHistoryAndLatestDeliveredTuneSelection() {
        var p=add(project(),mapOf("bb" to listOf(item(b,"Bb",1))))
        p=add(p,mapOf("bb" to listOf(item(b,"C"))))
        val r=DistributionResolver.resolve(p)[1]
        assertEquals(1,r.byPart.getValue(b.id to "Bb").chart.revision)
        assertEquals(0,r.byPart.getValue(b.id to "C").chart.revision)
        assertEquals("C",r.byTune.getValue(b.id).chart.part)
        // Current preference labels cannot select undistributed or earlier charts.
        assertEquals("Exact b r0 C.pdf",plan(p).members[1].entries[1].filename)
    }
    @Test fun oldAnchorsCannotOverrideNewMasterAndSkippedMembersKeepTheirEpoch() {
        var p=add(project(),mapOf("c" to listOf(item(b,"C")),"bb" to listOf(item(b,"Bb"))))
        val later=publication("new-master",mapOf("c" to listOf(item(a,"Guitar",2))),"2026-10-22T00:00:00Z")
        p=p.recordMaster(later)
        p=add(p,mapOf("c" to listOf(item(b,"C",3)),"bb" to listOf(item(b,"Bb",1))))
        val value=plan(p)
        assertEquals("new-master",value.members[0].baselineId)
        assertEquals("Exact a r2 Guitar.pdf",value.members[0].entries[0].filename)
        assertNull(value.members[0].entries[1].filename)
        assertEquals("master",value.members[1].baselineId)
        assertEquals("Exact b r1 Bb.pdf",value.members[1].entries[1].filename)
        p=add(p,mapOf("c" to listOf(item(b,"Guitar",4))),anchors=mapOf("c" to "new-master"))
        assertEquals("Exact b r4 Guitar.pdf",plan(p).members[0].entries[1].filename)
    }
    @Test fun noMasterRetainsPlaceholderOnlyBehavior() {
        val p=project().copy(members=members+Member("new","New",emptyList(),emptyList()))
        val value=plan(add(p,mapOf("c" to listOf(item(b,"C")))))
        assertNull(value.members.last().baselineId)
        assertEquals(4,value.members.last().missingCount)
        assertTrue(value.needsMissingAcknowledgement)
    }
    @Test fun inconsistentChainHashAnchorAndForeignRecordsRejectBeforeFileCreation() {
        val p=add(project(),mapOf("c" to listOf(item(b,"C"))))
        val s=p.supplements.single()
        val malformed=listOf(s.copy(number=2),s.copy(previousId="wrong"),s.copy(selectionHash="bad"),
            s.copy(baselineMasterIds=mapOf("c" to "absent")),
            s.copy(publication=s.publication.copy(projectId="other")),
            s.copy(publication=s.publication.copy(members=s.publication.members.map { it.copy(zipSha256="bad") })))
        val dir=Files.createTempDirectory("bad-event-history")
        malformed.forEach { record ->
            val invalid=p.copy(supplements=listOf(record))
            val failure=assertFailsWith<IllegalArgumentException> { EventExporter.generate(invalid,plan(p),dir.toString(),true,true) }
            assertTrue(failure.message!!.contains("inconsistent"))
            assertFalse(Files.exists(dir.resolve("Gigs")))
        }
        assertFailsWith<IllegalArgumentException> { plan(p.copy(supplements=listOf(s,s))) }
    }
    @Test fun unsafeSupplementFilenameIsVisibleMissingRatherThanFallbackToMaster() {
        val p=add(project(),mapOf("c" to listOf(item(a,"C",1).copy(filename="../unsafe.pdf"))))
        assertNull(plan(p).members[0].entries[0].filename)
        assertTrue(plan(p).members[0].entries[0].reason!!.contains("unsafe"))
    }
    @Test fun savedEventsHistoryAndProjectIsolationSurviveReopeningAndExport() {
        val p=add(project(),mapOf("c" to listOf(item(b,"C")),"bb" to listOf(item(b,"Bb"))))
        val other=Project("other","Other",members=members,catalog=p.catalog,
            events=p.events.map { it.copy(projectId="other") },masters=p.masters.map { it.copy(projectId="other") })
        val dir=Files.createTempDirectory("cumulative-event")
        val store=FileWorkspaceStore(dir.resolve("workspace.json")); store.save(Workspace().saveProject(p).saveProject(other))
        val before=Files.readAllBytes(dir.resolve("workspace.json"))
        val reopened=store.load(); assertEquals(p,reopened.projects.first())
        val result=EventExporter.generate(reopened.projects.first(),plan(p),dir.toString(),false,true)
        assertContentEquals(before,Files.readAllBytes(dir.resolve("workspace.json")))
        assertEquals(listOf(2,2),plan(reopened.projects.last()).members.map { it.matchedCount })
        result.zipNames.forEach { name -> ZipFile(result.folder.resolve(name).toFile()).use { zip ->
            assertEquals(2,zip.size()); assertFalse(zip.entries().asSequence().any { it.name.endsWith(".pdf",true) })
            val xml=zip.getInputStream(zip.entries().asSequence().single { it.name.endsWith(".4ss") }).use { it.readBytes().decodeToString() }
            EventExporter.validateXml(xml); assertFalse(xml.contains("data=")); assertFalse(xml.contains("JVBER"))
            val factory=javax.xml.parsers.DocumentBuilderFactory.newInstance().apply { setFeature("http://apache.org/xml/features/disallow-doctype-decl",true) }
            val doc=factory.newDocumentBuilder().parse(org.xml.sax.InputSource(java.io.StringReader(xml)))
            val rows=doc.documentElement.childNodes
            val elements=(0 until rows.length).mapNotNull { rows.item(it) as? org.w3c.dom.Element }
            assertEquals(listOf("Set 1",a.title,b.title,"Set 2",b.title,a.title),elements.map { it.getAttribute("title") })
            assertEquals(listOf("1","2","3","4"),plan(p).members.first().entries.map { it.entryId })
        } }
    }
    @Test fun localApprovedAndUnapprovedRevisionsCannotImplyDelivery() {
        val original=project()
        val local=listOf(a,b).mapIndexed { index,tune -> ChartRecord("local-$index","Z:/local","Local-$index.pdf","e".repeat(64),10,1,1,
            tune.id,ChartMetadata(tune.title,"C",9),accepted=index==0) }
        val p=original.copy(catalog=original.catalog.copy(source="Z:/local",charts=local))
        assertEquals("Exact a r0 C.pdf",plan(p).members.first().entries.first().filename)
        assertNull(plan(p).members.first().entries[1].filename)
        val recorded=add(p,mapOf("c" to listOf(item(b,"C",1))))
        assertEquals("Exact b r1 C.pdf",plan(recorded).members.first().entries[1].filename)
    }
    @Test fun newlyRecordedSupplementInvalidatesAnOldPreview() {
        val p=project(); val old=plan(p)
        val updated=add(p,mapOf("c" to listOf(item(b,"C"))))
        val dir=Files.createTempDirectory("stale-cumulative-preview")
        assertFailsWith<IllegalArgumentException> { EventExporter.generate(updated,old,dir.toString(),true,true) }
        assertFalse(Files.exists(dir.resolve("Gigs")))
    }
}
