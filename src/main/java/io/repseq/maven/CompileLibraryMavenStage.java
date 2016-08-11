package io.repseq.maven;

import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.cli.CompileAction;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceResolvers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(args[0]);

        Path cache = root.resolve(".cache");
        SequenceResolvers.initDefaultResolver(cache);

        AtomicInteger counter = new AtomicInteger();
        List<Path> compiledPaths = new ArrayList<>();

        Path target = root.resolve("target").resolve("library");
        Files.createDirectories(target);

        compileDir(target, counter, compiledPaths, root.resolve("library"));

        List<VDJCLibraryData> libs = new ArrayList<>();

        for (Path path : compiledPaths)
            libs.addAll(asList(ONE_LINE.readValue(path.toFile(),
                    VDJCLibraryData[].class)));

        VDJCLibraryData[] mergeResult = VDJCDataUtils.merge(libs);

        Path resultPath = root.resolve("target").resolve("classes").resolve("library");
        Files.createDirectories(resultPath);
        resultPath = resultPath.resolve("default.json");

        GlobalObjectMappers.ONE_LINE.writeValue(resultPath.toFile(), mergeResult);

        log.info("Merged successfully.");
    }

    public static void compileDir(Path to, AtomicInteger counter, List<Path> compiledPaths, Path parent) throws IOException {
        for (Path path : Files.newDirectoryStream(parent)) {
            if (Files.isDirectory(path))
                compileDir(to, counter, compiledPaths, path);
            else if (path.getFileName().toString().endsWith(".json")) {
                Path resultPath = to.resolve("lib" + counter.incrementAndGet() + ".josn");
                compiledPaths.add(resultPath);
                CompileAction.compile(path, resultPath, 30);
            }
        }
    }
}
