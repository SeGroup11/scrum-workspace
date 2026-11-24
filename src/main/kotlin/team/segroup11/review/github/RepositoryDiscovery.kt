package team.segroup11.review.github

/** EDT-only. Wait for Git mapping/configuration events while no remote is available. */
internal class RepositoryDiscovery {
    private var scanning = false
    private var waitingForRemote = true
    private var changedDuringScan = false

    fun started() {
        scanning = true
        changedDuringScan = false
    }

    fun repositoryChanged(): Boolean {
        if (!waitingForRemote) return false
        if (scanning) changedDuringScan = true
        return !scanning
    }

    fun finished(hasRemote: Boolean): Boolean {
        scanning = false
        waitingForRemote = !hasRemote
        return waitingForRemote && changedDuringScan
    }
}
