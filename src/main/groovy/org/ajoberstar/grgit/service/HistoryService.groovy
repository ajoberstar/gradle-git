package org.ajoberstar.grgit.service

class HistoryService {
	List<DiffEntry> diff(Map parms)
	List<Commit> log(Map parms)
	List<RefLogEntry> reflog(Map parms)
	BlameResult blame(Map parms)

	void commit(Map parms)
	void revert(Map parms)
	void cherryPick(Map parms)
	void merge(Map parms)
	void rebase(Map parms)
}
