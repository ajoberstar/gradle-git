package org.ajoberstar.gradle.git.api;

@groovy.transform.Immutable
class TrackingStatus {

    /**
     * full remote-tracking branch name
     */
    String remoteBranchRef

    /**
     * number of commits that the local branch is ahead of the
     */
    int aheadCount

    /**
     * number of commits that the local branch is behind of the remote-tracking branch
     */
    int behindCount
}
