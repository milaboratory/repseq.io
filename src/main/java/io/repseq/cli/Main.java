package io.repseq.cli;

import com.milaboratory.cli.JCommanderBasedMain;
import com.milaboratory.util.VersionInfo;
import io.repseq.seqbase.SequenceResolvers;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        Path cachePath = Paths.get(System.getProperty("user.home"), ".repseqio", "cache");
        SequenceResolvers.initDefaultResolver(cachePath);

        // Setting up main helper
        JCommanderBasedMain main = new JCommanderBasedMain("repseqio",
                new ListAction(),
                new FilterAction(),
                new FastaAction(),
                new DebugAction(),
                new FormatAction(),
                new StatAction());

        main.setVersionInfoCallback(new Runnable() {
            @Override
            public void run() {
                VersionInfo milib = VersionInfo.getVersionInfoForArtifact("milib");
                VersionInfo mitools = VersionInfo.getVersionInfoForArtifact("repseq.io");

                StringBuilder builder = new StringBuilder();

                builder.append("RepSeq.IO.CLI v")
                        .append(mitools.getVersion())
                        .append(" (built ")
                        .append(mitools.getTimestamp())
                        .append("; rev=")
                        .append(mitools.getRevision())
                        .append("; branch=")
                        .append(mitools.getBranch())
                        .append(")")
                        .append("\n");

                builder.append("MiLib v")
                        .append(milib.getVersion())
                        .append(" (rev=").append(milib.getRevision())
                        .append("; branch=").append(milib.getBranch())
                        .append(")");

                System.out.println(builder.toString());
            }
        });

        main.main(args);
    }
}
