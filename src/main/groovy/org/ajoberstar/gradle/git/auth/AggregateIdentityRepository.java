package org.ajoberstar.gradle.git.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;

public class AggregateIdentityRepository implements IdentityRepository {
	private final Collection<? extends IdentityRepository> repos;

	public AggregateIdentityRepository(IdentityRepository... repos) {
		this(Arrays.asList(repos));
	}

	public AggregateIdentityRepository(Collection<? extends IdentityRepository> repos) {
		this.repos = repos;
	}

	@Override
	public String getName() {
		return "aggregate";
	}

	@Override
	public int getStatus() {
		boolean anyRunning = false;
		boolean anyNotRunning = false;
		for (IdentityRepository repo : repos) {
			switch (repo.getStatus()) {
				case IdentityRepository.RUNNING:
					anyRunning = true;
					break;
				case IdentityRepository.NOTRUNNING:
					anyNotRunning = true;
					break;
				case IdentityRepository.UNAVAILABLE:
					break;
				default:
					throw new IllegalStateException("Invalid status for repo" + repo.getName() + ": " + repo.getStatus());
			}
		}

		if (anyRunning) {
			return IdentityRepository.RUNNING;
		} else if (anyNotRunning) {
			return IdentityRepository.NOTRUNNING;
		} else {
			return IdentityRepository.UNAVAILABLE;
		}
	}

	@Override
	public Vector<Identity> getIdentities() {
		Vector<Identity> ids = new Vector<Identity>();
		for (IdentityRepository repo : repos) {
			ids.addAll(repo.getIdentities());
		}
		return ids;
	}

	@Override
	public boolean add(byte[] identity) {
		boolean anySuccess = false;
		for (IdentityRepository repo : repos) {
			if (repo.add(identity)) {
				anySuccess = true;
			}
		}
		return anySuccess;
	}

	@Override
	public boolean remove(byte[] blob) {
		boolean anySuccess = false;
		for (IdentityRepository repo : repos) {
			if (repo.remove(blob)) {
				anySuccess = true;
			}
		}
		return anySuccess;
	}

	public void removeAll() {
		for (IdentityRepository repo : repos) {
			repo.removeAll();
		}
	}
}
