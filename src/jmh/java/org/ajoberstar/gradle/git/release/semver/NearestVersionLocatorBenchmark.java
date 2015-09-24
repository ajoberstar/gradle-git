/*
 * Copyright 2012-2015 the original author or authors.
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
package org.ajoberstar.gradle.git.release.semver;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.InitOp;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static org.ajoberstar.gradle.git.release.semver.NearestVersionLocatorSpec.*;

/**
 * Benchmarks for {@link NearestVersionLocator}.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NearestVersionLocatorBenchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(NearestVersionLocatorBenchmark.class);

    File repoDir;
    Grgit grgit;
    SecureRandom random = new SecureRandom();
    NearestVersionLocator locator = new NearestVersionLocator();

    @Param({"notags", "noreachable", "large", "head"})
    String type;

    @Param({"1500"})
    int commits;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        repoDir = Files.createTempDirectory("repo").toFile();
        InitOp init = new InitOp();
        init.setDir(repoDir);
        grgit = init.call();
        switch (type) {
            case "notags": {
                ncommits(commits);
                break;
            }
            case "noreachable": {
                ncommits(commits);
                addBranch("tagged", grgit);
                ncommits(5);
                checkout("tagged", grgit);
                ncommits(1);
                tag("v1.0.0");
                checkout("master", grgit);
                break;
            }
            case "large": {
                int chunks = Math.round(commits / 100);
                for (int i = 0; i < 100; i++) {
                    ncommits(chunks);
                    tag(String.format("v1.0.%d", i));
                }
                break;
            }
            case "head": {
                ncommits(commits);
                tag("v1.0.0");
                break;
            }
            default: {
                throw new IllegalStateException("Unknown parameter " + type);
            }
        }
    }

    private void tag(String name) {
        addTag(name, grgit);
    }

    private void ncommits(int n) {
        for (int i = 0; i < n; i++) {
            commit(random, grgit);
        }
    }

    @TearDown
    public void tearDown() {
        cleanupRepo(repoDir);
    }

    @Benchmark
    public NearestVersion locate() {
        return locator.locate(grgit);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .warmupIterations(5)
                .measurementIterations(10)
                .include(NearestVersionLocatorBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
