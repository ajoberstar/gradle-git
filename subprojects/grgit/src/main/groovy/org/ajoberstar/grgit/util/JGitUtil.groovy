/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.grgit.util

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Person
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.exception.GrGitException
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.errors.AmbiguousObjectException
import org.eclipse.jgit.errors.IncorrectObjectTypeException
import org.eclipse.jgit.errors.RevisionSyntaxException
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit

class JGitUtil {
	private JGitUtil() {
		throw new AssertionError('Cannot instantiate this class.')
	}

	static ObjectId resolveObject(Repository repo, String revstr) {
		try {
			ObjectId object = repo.git.repository.resolve(revstr)
			if (object == null) {
				throw new GrGitException("No commit found for revision string: ${revstr}")
			} else {
				return object
			}
		} catch (AmbiguousObjectException e) {
			throw new GrGitException("Revision string is ambiguous: ${revstr}", e)
		} catch (RevisionSyntaxException e) {
			throw new GrGitException("Revision string syntax isn't supported: ${revstr}", e)
		} catch (IncorrectObjectTypeException e) {
			throw new GrGitException("Revision string did not point to a commit: ${revstr}", e)
		} catch (IOException e) {
			throw new GrGitException("Problem resolving revision string: ${revstr}", e)
		}
	}

	/**
	 * Converts a JGit RevCommit to a Commit.
	 * @param rev the JGit commit to convert
	 * @return a org.ajoberstar Commit
	 */
	static Commit convertCommit(RevCommit rev) {
		Map props = [:]
		props.id = ObjectId.toString(rev.id)
		props.abbreviatedId = props.id[0..6]
		PersonIdent committer = rev.committerIdent
		props.committer = new Person(committer.name, committer.emailAddress)
		PersonIdent author = rev.authorIdent
		props.author = new Person(author.name, author.emailAddress)
		props.time = rev.commitTime
		props.fullMessage = rev.fullMessage
		props.shortMessage = rev.shortMessage
		return new Commit(props)
	}

	static Status convertStatus(org.eclipse.jgit.api.Status jgitStatus) {
		return new Status(
			jgitStatus.added,
			jgitStatus.changed,
			jgitStatus.removed,
			jgitStatus.untracked,
			jgitStatus.modified,
			jgitStatus.missing
		)
	}
}
