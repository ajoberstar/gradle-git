package org.ajoberstar.gradle.util;

public class RemoteEvaluator {
  public String evaluate(Object remote) {
    return remote == null ? "origin" : ObjectUtil.unpackString(remote);
  }
}