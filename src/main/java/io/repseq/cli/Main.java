package io.repseq.cli;

import com.milaboratory.cli.JCommanderBasedMain;
import com.milaboratory.util.VersionInfo;
import io.repseq.core.VDJCLibrary;
import io.repseq.core.VDJCLibraryRegistry;
import io.repseq.seqbase.SequenceResolvers;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if (System.getProperty("localOnly") == null) {
            Path cachePath = Paths.get(System.getProperty("user.home"), ".repseqio", "cache");
            SequenceResolvers.initDefaultResolver(cachePath);
        }

        // Setting up main helper
        JCommanderBasedMain main = new JCommanderBasedMain("repseqio",
                new ListAction(),
                new FilterAction(),
                new MergeAction(),
                new CompileAction(),
                new FastaAction(),
                new TsvAction(),
                new InferAnchorPointsAction(),
                new DebugAction(),
                new FormatAction(),
                new StatAction(),
                new FromPaddedFastaAction());

        main.setVersionInfoCallback(new Runnable() {
            @Override
            public void run() {
                VersionInfo milib = VersionInfo.getVersionInfoForArtifact("milib");
                VersionInfo repseqio = VersionInfo.getVersionInfoForArtifact("repseqio");

                StringBuilder builder = new StringBuilder();

                builder.append("RepSeq.IO.CLI v")
                        .append(repseqio.getVersion())
                        .append(" (built ")
                        .append(repseqio.getTimestamp())
                        .append("; rev=")
                        .append(repseqio.getRevision())
                        .append("; branch=")
                        .append(repseqio.getBranch())
                        .append(")")
                        .append("\n");

                builder.append("MiLib v")
                        .append(milib.getVersion())
                        .append(" (rev=").append(milib.getRevision())
                        .append("; branch=").append(milib.getBranch())
                        .append(")")
                        .append("\n");

                builder.append("Built-in libraries:\n");

                VDJCLibraryRegistry reg = VDJCLibraryRegistry.createDefaultRegistry();
                reg.loadAllLibraries("default");
                for (VDJCLibrary lib : reg.getLoadedLibraries())
                    builder.append(lib.getLibraryId()).append("\n");

                System.out.print(builder.toString());
            }
        });

        main.main(args);
    }
}
