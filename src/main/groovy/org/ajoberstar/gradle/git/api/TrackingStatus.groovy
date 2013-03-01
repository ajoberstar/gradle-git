package org.ajoberstar.gradle.git.api;

@groovy.transform.Immutable
class TrackingStatus {
    /**
     * Remote tracking branch
     */
    Branch remoteBranch

    /**
     * number of commits that the local branch is ahead of the
     */
    int aheadCount

    /**
     * number of commits that the local branch is behind of the remote-tracking branch
     */
    int behindCount
}
