package team.segroup11.review.github

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryDiscoveryTest {
    @Test fun rechecksWhenGitFinishesStartupAfterTheFirstEmptyResult() {
        val updates = RepositoryDiscovery()
        updates.started()
        assertFalse(updates.finished(hasRemote = false))
        assertTrue(updates.repositoryChanged())
        updates.started()
        assertFalse(updates.finished(hasRemote = true))
        assertFalse(updates.repositoryChanged())
    }

    @Test fun coalescesChangesDuringDiscoveryAndPreservesExistingChoices() {
        val updates = RepositoryDiscovery()
        updates.started()
        repeat(3) { assertFalse(updates.repositoryChanged()) }
        assertTrue(updates.finished(hasRemote = false))
        updates.started()
        assertFalse(updates.repositoryChanged())
        assertFalse(updates.finished(hasRemote = true))
        repeat(3) { assertFalse(updates.repositoryChanged()) }
    }
}
