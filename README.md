# RepSeq.IO.CLI

Command line helper to manipulate RepSeq.IO formatted V/D/J/C reference data.

# Install

#### Using Homebrew on Mac OS X or Linux (linuxbrew)

    brew install repseqio/all/repseqio

#### Using zip

Unpack zip file with latest release version to a folder and add it to your `PATH` variable or create symlink to `repseqio` script in `/usr/local/bin`, `~/bin` or similar folder.

# Reference

See this [repository](https://github.com/repseqio/library) for actual references.

# Format

RepSeq.IO formatted reference is a JSON file that contain positions of V/D/J/C genes in some well known sequence (like NG_001332.2) or in file delivered with the reference. Each type of gene has predefined set of Anchor Points (see [here](http://mixcr.readthedocs.io/en/latest/geneFeatures.html)) which are used to encode gene position.

See this file for example of RepSeq.IO formatted VDJCLibrary:
https://github.com/repseqio/library/blob/master/human/TRB.json

Detailed format documentation and JSON Schemas coming soon.

# Documentation

Usage:

```
Usage: repseqio [options] [command] [command options]
  Options:
    -h, --help
       Displays this help message.
    -v, --version
       Output version information.
  Commands:
    list      Format JSON in library; sort libraries in multi-library files, sort genes inside libraries.
      Usage: list [options] library.json
        Options:
          -h, --help
             Displays help for this command.
             Default: false

    filter      Filter libraries and library records.
      Usage: filter [options] input_library.json output_library.json
        Options:
          -c, --chain
             Chain pattern, regexp string, all genes with matching chain record
             will be collected.
          -h, --help
             Displays help for this command.
             Default: false
          -s, --species
             Species name, used in the same way as --taxon-id.
          -t, --taxon-id
             Taxon id (filter multi-library file to leave single library for
             specified taxon id)

    fasta      Export sequences of genes to fasta file.
      Usage: fasta [options] input_library.json [output.fasta]
        Options:
          -c, --chain
             Chain pattern, regexp string, all genes with matching chain record
             will be exported.
        * -g, --gene-feature
             Gene feature to export (e.g. VRegion, JRegion, VTranscript, etc...)
          -h, --help
             Displays help for this command.
             Default: false
          -n, --name
             Gene name pattern, regexp string, all genes with matching gene name
             will be exported.
          -s, --species
             Species name, used in the same way as --taxon-id.
          -t, --taxon-id
             Taxon id (filter multi-library file to leave single library for
             specified taxon id)

    debug      Outputs extensive information on genes in the library.
      Usage: debug [options] input_library.json
        Options:
          -h, --help
             Displays help for this command.
             Default: false
          -n, --name
             Gene name pattern, regexp string, all genes with matching gene name
             will be exported.

    format      Format JSON in library; sort libraries in multi-library files, sort genes inside libraries.
      Usage: format [options] library.json
        Options:
          -c, --compact
             Compact.
          -h, --help
             Displays help for this command.
             Default: false

    stat      Print library statistics.
      Usage: stat [options] input_library.json
        Options:
          -h, --help
             Displays help for this command.
             Default: false
```
