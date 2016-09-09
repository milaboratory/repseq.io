package io.repseq.maven;

import io.repseq.cli.CompileAction;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceResolvers;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.milaboratory.util.GlobalObjectMappers.ONE_LINE;
import static java.util.Arrays.asList;

/**
 * Class executed during maven build process
 */
public class CompileLibraryMavenStage {
    private static final Logger log = LoggerFactory.getLogger(CompileLibraryMavenStage.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        Path root = Paths.get(args[0]);

        Path cache = root.resolve(".cache");
        SequenceResolvers.initDefaultResolver(cache);

        AtomicInteger counter = new AtomicInteger();
        List<Path> compiledPaths = new ArrayList<>();

        Path target = root.resolve("target").resolve("library");
        Files.createDirectories(target);

        Path libraryRepoFolder = root.resolve("library");

        compileDir(target, counter, compiledPaths, libraryRepoFolder);

        List<VDJCLibraryData> libs = new ArrayList<>();

        for (Path path : compiledPaths)
            libs.addAll(asList(ONE_LINE.readValue(path.toFile(),
                    VDJCLibraryData[].class)));

        VDJCLibraryData[] mergeResult = VDJCDataUtils.merge(libs);

        log.info("Merged successfully.");

        Path libResourcePath = root.resolve("target").resolve("classes").resolve("libraries");
        Files.createDirectories(libResourcePath);

        Process gitTagProcess = new ProcessBuilder("git", "describe", "--always", "--tags")
                .directory(libraryRepoFolder.toFile())
                .start();
        String gitTag = IOUtils.toString(gitTagProcess.getInputStream(), StandardCharsets.UTF_8)
                .replace("\n", "").replace("\r", "");
        gitTagProcess.waitFor();

        String fullLibraryName = "repseqio." + gitTag;
        Path resultPath = libResourcePath.resolve(fullLibraryName + ".json");

        log.info("Writing {}", resultPath);

        VDJCDataUtils.writeToFile(mergeResult, resultPath, true);

        Path aliasPath = libResourcePath.resolve("default.alias");

        log.info("Writing {}", aliasPath);

        try (FileOutputStream fos = new FileOutputStream(aliasPath.toFile())) {
            fos.write(fullLibraryName.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void compileDir(Path to, AtomicInteger counter, List<Path> compiledPaths, Path parent) throws IOException {
        for (Path path : Files.newDirectoryStream(parent)) {
            if (Files.isDirectory(path))
                compileDir(to, counter, compiledPaths, path);
            else if (path.getFileName().toString().endsWith(".json")) {
                Path resultPath = to.resolve("lib" + counter.incrementAndGet() + ".json");
                compiledPaths.add(resultPath);
                CompileAction.compile(path, resultPath, 30);
            }
        }
    }
}
