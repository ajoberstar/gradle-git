/* Copyright 2012-2013 the original author or authors.
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
package org.ajoberstar.gradle.git.util

import groovy.util.logging.Slf4j
import org.ajoberstar.gradle.git.api.Branch
import org.ajoberstar.gradle.git.api.Commit
import org.ajoberstar.gradle.git.api.Person
import org.eclipse.jgit.errors.MissingObjectException
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk

/**
 * Utility methods for Git objects.
 */
 @Slf4j
class GitUtil {
	private GitUtil() {
		throw new AssertionError('This class cannot be instantiated.')
	}

	private static Map<RevCommit, Commit> cache = [:]

	/**
	 * Converts a JGit RevCommit to a Commit.
	 * @param rev the JGit commit to convert
	 * @return a org.ajoberstar Commit
	 */
	static Commit revCommitToCommit(RevCommit rev) {
		if (cache.containsKey(rev)) {
			return cache[rev]
		} else {
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
			Commit commit = new Commit(props)
			cache[rev] = commit
			return commit
		}
	}

	/**
	 * Converts JGit's {@link Ref} to {@link org.ajoberstar.gradle.git.api.Branch} object
	 * @return the branch
	 * @since 0.3.0
	 */
	static Branch refToBranch(Repository repo, Ref ref) {
		String refName = ref.name
		RevWalk walk = new RevWalk(repo)
		Commit commit
		try {
			RevCommit rev = walk.parseCommit(ref.objectId)
			commit = revCommitToCommit(rev)
		} catch (MissingObjectException e) {
			log.debug("Could not find commit for ref: $refName", e)
		}
		String shortName = Repository.shortenRefName(refName)
		return new Branch(shortName, refName, commit)
	}

     /**
      * Build {@link Branch} object from git name
      * @return the branch
      * @since 0.3.0
      */
     static Branch gitNameToBranch(Repository repo, String gitName) {
         Ref ref = repo.getRef(gitName)
         if (ref) return refToBranch(repo, ref)

         def commit
         try {
             RevCommit rev = new RevWalk(repo).parseCommit(repo.resolve(gitName))
             commit = revCommitToCommit(rev)
         } catch (MissingObjectException e) {
             log.debug("Could not find commit for ref: $gitName", e)
         }

         return new Branch(name: null, refName: null, commit: commit)
     }

 }
