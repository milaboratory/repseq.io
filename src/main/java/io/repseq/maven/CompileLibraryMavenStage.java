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

        Path cacheFolder = root.resolve(".cache");
        Path buildFolder = root.resolve("target").resolve("library");
        Path outputFolder = root.resolve("target").resolve("classes").resolve("libraries");
        Path libraryRepoFolder = root.resolve("library");

        Process gitTagProcess = new ProcessBuilder("git", "describe", "--always", "--tags")
                .directory(libraryRepoFolder.toFile())
                .start();
        String currentTag = IOUtils.toString(gitTagProcess.getInputStream(), StandardCharsets.UTF_8)
                .replace("\n", "").replace("\r", "");
        gitTagProcess.waitFor();

        String[] targetTags = {"v1.0", currentTag};

        for (String tag : targetTags) {
            new ProcessBuilder("git", "checkout", tag)
                    .directory(libraryRepoFolder.toFile())
                    .inheritIO()
                    .start()
                    .waitFor();
            process(libraryRepoFolder, cacheFolder, buildFolder, outputFolder, tag, tag.equals(currentTag));
        }
    }

    public static void process(Path libraryRepoFolder, Path cacheFolder, Path buildFolder, Path outputFolder,
                               String tag, boolean isDefault) throws IOException, InterruptedException {
        SequenceResolvers.initDefaultResolver(cacheFolder);

        Files.createDirectories(buildFolder);

        List<Path> compiledPaths = compileDir(libraryRepoFolder, buildFolder);

        List<VDJCLibraryData> libs = new ArrayList<>();

        for (Path path : compiledPaths)
            libs.addAll(asList(ONE_LINE.readValue(path.toFile(),
                    VDJCLibraryData[].class)));

        VDJCLibraryData[] mergeResult = VDJCDataUtils.merge(libs);

        log.info("Merged successfully.");

        Files.createDirectories(outputFolder);

        String fullLibraryName = "repseqio." + tag;
        Path resultPath = outputFolder.resolve(fullLibraryName + ".json");

        log.info("Writing {}", resultPath);

        VDJCDataUtils.writeToFile(mergeResult, resultPath, true);

        if (isDefault) {
            Path aliasPath = outputFolder.resolve("default.alias");

            log.info("Writing {}", aliasPath);

            try (FileOutputStream fos = new FileOutputStream(aliasPath.toFile())) {
                fos.write(fullLibraryName.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public static List<Path> compileDir(Path libraryRepoFolder, Path to) throws IOException {
        AtomicInteger counter = new AtomicInteger();
        List<Path> resultFiles = new ArrayList<>();
        compileDir(to, counter, resultFiles, libraryRepoFolder);
        return resultFiles;
    }

    public static void compileDir(Path to, AtomicInteger counter, List<Path> resultFiles, Path parent) throws IOException {
        for (Path path : Files.newDirectoryStream(parent)) {
            if (Files.isDirectory(path))
                compileDir(to, counter, resultFiles, path);
            else if (path.getFileName().toString().endsWith(".json")) {
                Path resultPath = to.resolve("lib" + counter.incrementAndGet() + ".json");
                resultFiles.add(resultPath);
                CompileAction.compile(path, resultPath, 30);
            }
        }
    }
}
