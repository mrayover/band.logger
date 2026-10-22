package bandlogger

import kotlin.test.*

class CatalogTest {
    private var next = 0
    private fun id() = "id-${++next}"
    private fun file(path: String, hash: String = path) = ScannedPdf(path, hash, 20, 100)
    private fun scan(files: List<ScannedPdf>, catalog: Catalog = Catalog(source = "folder")) = catalog.reconcile(files, "Q", 100, ::id)
    @Test fun parserAndCanonicalNaming() {
        val meta = ChartNames.parse("q Along Came Betty -R02- bB.PDF", "Q")!!
        assertEquals(ChartMetadata("Along Came Betty", "Bb", 2), meta)
        assertEquals("Q Along Came Betty -r02- Bb.pdf", ChartNames.filename("Q", meta))
        assertEquals("Title", ChartNames.parse("Title -r00- C.pdf", "")?.title)
        listOf("Q Bad.pdf", "Q Song -rX- C.pdf", "Q Song -r00- Other.pdf", "Wrong Song -r00- C.pdf", "Q Song -r00- Bb-2.pdf").forEach { assertNull(ChartNames.parse(it, "Q"), it) }
    }
    @Test fun groupingPreservesPartsAndIndependentRevisions() {
        val catalog = scan(listOf(file("Q One Tune -r00- C.pdf"), file("Q one   tune -r02- Bb.pdf"), file("Q One Tune -r01- C.pdf"), file("Q One-Tune -r00- C.pdf")))
        assertEquals(2, catalog.tunes.size) // Punctuation is deliberately significant.
        assertEquals(3, catalog.charts.count { it.tuneId == catalog.tunes.first { t -> t.key == "one tune" }.id })
        assertEquals(4, catalog.needsReview().size)
        assertTrue(catalog.acceptNew().active().all { catalog.acceptNew().status(it) == "Ready" })
    }
    @Test fun repeatedScanDoesNotDuplicateOrResetReviews() {
        val files = listOf(file("Q Tune -r00- C.pdf"))
        val first = scan(files).acceptNew()
        assertEquals(first, scan(files, first))
    }
    @Test fun contentChangesKeepHistoryAndRequireDifferentRevision() {
        val first = scan(listOf(file("Q Tune -r00- C.pdf", "a"))).acceptNew()
        val changed = scan(listOf(file("Q Tune -r00- C.pdf", "b")), first)
        assertEquals(2, changed.charts.size)
        assertFalse(changed.charts.first().current)
        val current = changed.active().single()
        assertEquals("Revision conflict", changed.status(current))
        assertFailsWith<IllegalArgumentException> { changed.review(current.id, current.metadata!!, "", ::id) }
        val fixed = changed.review(current.id, current.metadata!!.copy(revision = 1), "New version", ::id)
        assertEquals("Ready", fixed.status(fixed.active().single()))
        assertEquals("a", fixed.charts.first().sha256)
    }
    @Test fun conflictingCopiesNeverSilentlyReplaceOneAnother() {
        val catalog = scan(listOf(file("a/Q Tune -r00- C.pdf", "a"), file("b/Q Tune -r00- C.pdf", "b")))
        assertEquals(2, catalog.needsReview().size)
        assertTrue(catalog.acceptNew().active().all { catalog.status(it) == "Revision conflict" })
        val excluded = catalog.exclude(catalog.charts.first().id, true)
        val fixed = excluded.acceptNew()
        assertEquals(1, fixed.active().size)
        assertEquals("Ready", fixed.status(fixed.active().single()))
    }
    @Test fun reviewMalformedNameWithoutRenaming() {
        val c = scan(listOf(file("Odd file.pdf")))
        val fixed = c.review(c.charts.single().id, ChartMetadata("My Tune", "Bass", 3), "Reviewed", ::id)
        assertEquals("Odd file.pdf", fixed.charts.single().relativePath)
        assertEquals("Ready", fixed.status(fixed.charts.single()))
        assertEquals(fixed, scan(listOf(file("Odd file.pdf")), fixed))
    }
    @Test fun missingAndRestoredFilesKeepIdentityAndAcceptance() {
        val files = listOf(file("Q Tune -r00- C.pdf"))
        val first = scan(files).acceptNew()
        val missing = scan(emptyList(), first)
        assertEquals("Missing file", missing.status(missing.charts.single()))
        assertEquals(first, scan(files, missing))
    }
    @Test fun sourceSwitchKeepsHistoryWithoutMixingActiveFiles() {
        val first = scan(listOf(file("Q Tune -r00- C.pdf")))
        val second = scan(listOf(file("Q Other -r00- Bb.pdf")), first.copy(source = "other"))
        assertEquals(2, second.charts.size)
        assertEquals("Q Other -r00- Bb.pdf", second.active().single().relativePath)
        assertEquals("Previous folder", second.status(second.charts.first()))
    }
    @Test fun newCatalogCannotLeakIntoAnotherProject() {
        val a = Project("a", "One", catalog = scan(listOf(file("Q Tune -r00- C.pdf"))))
        val b = Project("b", "Two")
        val state = Workspace().saveProject(a).saveProject(b)
        val restored = WorkspaceCodec.decode(WorkspaceCodec.encode(state))
        assertTrue(restored.projects[1].catalog.charts.isEmpty())
        assertEquals(1, restored.projects[0].catalog.charts.size)
    }
    @Test fun unreadablePdfsCannotBeAccepted() {
        val c = scan(listOf(file("Q Tune -r00- C.pdf").copy(error = "No PDF header")))
        assertEquals("Unreadable PDF", c.acceptNew().status(c.charts.single()))
        assertFailsWith<IllegalArgumentException> { c.review(c.charts.single().id, ChartMetadata("Tune", "C", 0), "", ::id) }
    }
    @Test fun chunkOneDataMigratesWithoutLosingProjects() {
        val old = """{"schemaVersion":1,"projects":[{"id":"q","name":"Quaternity","members":[{"id":"m","name":"Marcus","roles":["Guitar"],"preferences":["Guitar","C"]}]}],"selectedId":"q"}"""
        val migrated = WorkspaceCodec.decode(old)
        assertEquals(10, migrated.schemaVersion)
        assertEquals("Marcus", migrated.projects.single().members.single().name)
        assertTrue(migrated.projects.single().catalog.charts.isEmpty())
    }
}