package io.repseq.core;

public class GeneNotFoundException extends RuntimeException {
    final String geneName;
    final VDJCLibrary library;

    public GeneNotFoundException(String geneName, VDJCLibrary library) {
        super("Gene with name " + geneName + " not found in library " + library.getLibraryId() + ".");
        this.geneName = geneName;
        this.library = library;
    }
}
