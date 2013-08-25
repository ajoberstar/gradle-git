package org.ajoberstar.gradle.git.auth;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;

public class LocalIdentityRepository implements IdentityRepository {
	private Vector<Identity> ids;

	public LocalIdentityRepository() {
		this.ids = new Vector<Identity>();
	}

	public LocalIdentityRepository(Identity... ids) {
		this(Arrays.asList(ids));
	}

	public LocalIdentityRepository(Iterable<? extends Identity> ids) {
		this();
		for (Identity id : ids) {
			ids.add(id);
		}
	}

	@Override
	public String getName() {
		return "local";
	}

	@Override
	public int getStatus() {
		return IdentityRepository.RUNNING;
	}

	@Override
	public Vector<Identity> getIdentities() {
		Vector<Identity> ids = new Vector<Identity>();
		ids.addAll(this.ids);
		return ids;
	}

	@Override
	public boolean add(byte[] identity) {

		return true;
	}

	@Override
	public boolean remove(byte[] blob) {

		return true;
	}

	public void removeAll() {
		this.ids = new Vector<Identity>();
	}
}
