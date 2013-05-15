package org.ajoberstar.grgit.operation

class BranchAdd {
	enum TrackingMode {
		NO_TRACK,
		TRACK,
		SET_UPSTREAM
	}
	String name
	Commit startPoint
	boolean force
	TrackingMode trackingMode

	BranchAdd(Repository repo) {
		
	}

	void call() {

	}
}
