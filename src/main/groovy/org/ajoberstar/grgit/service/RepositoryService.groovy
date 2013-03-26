package org.ajoberstar.grgit.service

class RepositoryService {
	HistoryService history
	StageService stage

	BranchService branches
	RemoteService remotes
	StashService stashes
	TagService tags
	NoteService notes

	Status status
	boolean isBare()
	void clean(Map parms)
	void garbageCollect(Map parms)
}
