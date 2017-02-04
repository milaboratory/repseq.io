[![Build Status](https://travis-ci.org/repseqio/repseqio.svg)](https://travis-ci.org/repseqio/repseqio)

# RepSeq.IO.CLI

Command line helper to manipulate RepSeq.IO formatted V/D/J/C reference data.

# Install

#### Using Homebrew on Mac OS X or Linux (linuxbrew)

Install

    brew install repseqio/all/repseqio

Upgrade

    brew update
    brew upgrade repseqio

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
      Usage: list [options] library.json[.gz]
        Options:
          -h, --help
             Displays help for this command.
             Default: false

    filter      Filter libraries and library records.
      Usage: filter [options] input_library.json[.gz] output_library.json[.gz]
        Options:
          -c, --chain
             Chain pattern, regexp string, all genes with matching chain record
             will be collected.
          -f, --force
             Force overwrite of output file(s).
          -h, --help
             Displays help for this command.
             Default: false
          -s, --species
             Species name, used in the same way as --taxon-id.
          -t, --taxon-id
             Taxon id (filter multi-library file to leave single library for
             specified taxon id)

    merge      Merge several libraries into single library.
      Usage: merge [options] [input1.json[.gz] [ input2.json[.gz] [...] ] ] output.json[.gz]
        Options:
          -f, --force
             Force overwrite of output file(s).
          -h, --help
             Displays help for this command.
             Default: false

    compile      Compile a library into self-contained compiled library file, by embedding sequence information into "sequenceFragments" section.
      Usage: compile [options] input.json[.gz] output.json[.gz]
        Options:
          -f, --force
             Force overwrite of output file(s).
          -h, --help
             Displays help for this command.
             Default: false
          -s, --surrounding
             Length of surrounding sequences to include into library. Number of
             upstream and downstream nucleotides around V/D/J/C segments to embed into
             output library's "sequenceFragments" section. More nucleotides will be
             included, more surrounding sequences will be possible to request using gene
             features with offset (like JRegion(-12, +3)), at the same time size of
             output file will be greater.
             Default: 30

    fasta      Export sequences of genes to fasta file.
      Usage: fasta [options] input_library.json|default [output.fasta]
        Options:
          -c, --chain
             Chain pattern, regexp string, all genes with matching chain record
             will be exported.
          -f, --force
             Force overwrite of output file(s).
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

    inferPoints      Try to infer anchor point positions from gene sequences of other libraries. If no reference libraries are specified, built-in library will be used.
      Usage: inferPoints [options] input_library.json [reference_library1.json [reference_library2.json [....]]] output.json
        Options:
          -a, --copy-all
             Copy not modified records..
          -f, --force
             Force overwrite of output file(s).
        * -g, --gene-feature
             Reference gene feature to use (e.g. VRegion, JRegion, VTranscript,
             etc...). This feature will be used to align target genes with reference
             genes. Target genes must have this gene feature.
          -h, --help
             Displays help for this command.
             Default: false
          -m, --min-score
             Absolute minimal score. Alignment is performed using amino acid
             sequences (target is queried using all three reading frames) using BLOSUM62
             matrix. (default 200 for V gene, 50 for J gene)
          -n, --name
             Gene name pattern, regexp string, all genes with matching gene name
             will be exported.
          -o, --only-modified
             Output only modified records.

    debug      Outputs extensive information on genes in the library.
      Usage: debug [options] input_library.json[.gz]
        Options:
          -a, --all
             Check all genes, used with -p option.
          -h, --help
             Displays help for this command.
             Default: false
          -n, --name
             Gene name pattern, regexp string, all genes with matching gene name
             will be exported.
          -p, --problems
             Print only genes with problems, checks only functional genes by
             default (see -a option).

    format      Format JSON in library; sort libraries in multi-library files, sort genes inside libraries.
      Usage: format [options] library.json[.gz]
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

    fromPaddedFasta      Converts library from padded fasta file (IMGT-like) to non-padded fasta and json library files. Json library contain links to non-padded fasta file, so to use library one need both output file, or library can be compiled using 'repseqio compile'.
      Usage: fromPaddedFasta [options] input_padded.fasta output.fasta output.json[.gz]
        Options:
        * -c, --chain
             Chain.
          -f, --force
             Force overwrite of output file(s).
          -j, --functionality-index
             Functionality mark index (0-based) in FASTA description line (e.g.
             3 for IMGT files).
          --functionality-regexp
             Functionality regexp.
             Default: [\(\[]?[Ff].?
        * -g, --gene-type
             Gene type (V/D/J/C)
          -h, --help
             Displays help for this command.
             Default: false
          -i, --ignore-duplicates
             Ignore duplicate genes
        * -n, --name-index
             Gene name index (0-based) in FASTA description line (e.g. 1 for
             IMGT files).
             Default: 0
          -p, --padding-character
             Padding character
             Default: .
        * -t, --taxon-id
             Taxon id
          -L
             Amino-acid pattern of anchor point. Have higher priority than -P
             for the same anchor point.
             Syntax: -Lkey=value
             Default: {}
          -P
             Positions of anchor points in padded file. To define position
             relative to еру end of sequence use negative values: -1 = sequence end, -2 =
             last but one letter. Example: -PFR1Begin=0 -PVEnd=-1
             Syntax: -Pkey=value
             Default: {}
```
