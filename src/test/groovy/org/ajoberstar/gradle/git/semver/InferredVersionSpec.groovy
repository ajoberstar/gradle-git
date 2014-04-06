package org.ajoberstar.gradle.git.semver

import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared

import org.ajoberstar.grgit.Grgit

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.energizedwork.spock.extensions.TempDirectory

class InferredVersionSpec extends Specification {
	@TempDirectory(clean=true)
	@Shared File repoDir

	@Shared Grgit grgit

	def setupSpec() {
		grgit = Grgit.init(dir: repoDir)

		commit()
		commit()
		grgit.branch.add(name: 'unreachable')

		commit()
		grgit.tag.add(name: '0.0.1-milestone.3')
		grgit.branch.add(name: 'no-normal')

		commit()
		grgit.tag.add(name: '0.1.0')

		commit()
		grgit.branch.add(name: 'RB_0.1')

		commit()
		commit()
		grgit.tag.add(name: '0.2.0')

		grgit.checkout(branch: 'RB_0.1')

		commit()
		grgit.tag.add(name: 'v0.1.1+2010.01.01.12.00.00')

		commit()
		commit()
		commit()
		commit()
		grgit.tag.add(name: 'v0.1.2-milestone.1')

		commit()
		commit()
		commit()
		grgit.checkout(branch: 'master')

		commit()
		grgit.tag.add(name: 'v1.0.0')
		grgit.branch.add(name: 'RB_1.0')

		commit()
		grgit.tag.add(name: '1.1.0-rc.1+abcde')
	}

	@Unroll('when on #head, #stage version of #scope change infers #expected')
	def 'infers correct version'() {
		given:
		grgit.checkout(branch: head)
		def version = new InferredVersion()
		version.useBuildMetadataForStage = { false }
		version.grgit = grgit
		when:
		version.infer(scope, stage)
		then:
		version.toString() == expected
		where:
		head          | scope   | stage       | expected
		'unreachable' | 'patch' | 'dev'       | '0.0.1-dev.2'
		'unreachable' | 'patch' | 'milestone' | '0.0.1-milestone.1'
		'unreachable' | 'patch' | 'rc'        | '0.0.1-rc.1'
		'unreachable' | 'patch' | 'final'     | '0.0.1'
		'unreachable' | 'minor' | 'dev'       | '0.1.0-dev.2'
		'unreachable' | 'major' | 'dev'       | '1.0.0-dev.2'
		'no-normal'   | 'patch' | 'dev'       | '0.0.1-dev.3'
		'no-normal'   | 'patch' | 'milestone' | '0.0.1-milestone.4'
		'no-normal'   | 'patch' | 'rc'        | '0.0.1-rc.1'
		'no-normal'   | 'patch' | 'final'     | '0.0.1'
		'no-normal'   | 'minor' | 'milestone' | '0.1.0-milestone.1'
		'no-normal'   | 'major' | 'milestone' | '1.0.0-milestone.1'
		'RB_0.1'      | 'patch' | 'dev'       | '0.1.2-dev.7'
		'RB_0.1'      | 'patch' | 'milestone' | '0.1.2-milestone.2'
		'RB_0.1'      | 'patch' | 'rc'        | '0.1.2-rc.1'
		'RB_0.1'      | 'patch' | 'final'     | '0.1.2'
		'RB_0.1'      | 'minor' | 'final'     | '0.2.0'
		'RB_0.1'      | 'major' | 'final'     | '1.0.0'
		'RB_1.0'      | 'minor' | 'dev'       | '1.1.0-dev.0'
		'RB_1.0'      | 'minor' | 'milestone' | '1.1.0-milestone.1'
		'RB_1.0'      | 'minor' | 'rc'        | '1.1.0-rc.1'
		'RB_1.0'      | 'minor' | 'final'     | '1.1.0'
		'RB_1.0'      | 'patch' | 'final'     | '1.0.1'
		'RB_1.0'      | 'major' | 'final'     | '2.0.0'
		'master'      | 'minor' | 'dev'       | '1.1.0-dev.1'
		'master'      | 'minor' | 'milestone' | '1.1.0-milestone.1'
		'master'      | 'minor' | 'rc'        | '1.1.0-rc.2'
		'master'      | 'minor' | 'final'     | '1.1.0'
		'master'      | 'patch' | 'rc'        | '1.0.1-rc.1'
		'master'      | 'major' | 'rc'        | '2.0.0-rc.1'
	}

	private void commit() {
		new File(grgit.repository.rootDir, '1.txt') << '1'
		grgit.add(patterns: ['1.txt'])
		grgit.commit(message: 'do')
	}
}
