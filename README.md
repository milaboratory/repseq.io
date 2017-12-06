[![Javadocs](http://javadoc.io/badge/io.repseq/repseqio.svg)](http://javadoc.io/doc/io.repseq/repseqio)
[![Build Status](https://travis-ci.org/repseqio/repseqio.svg?branch=develop)](https://travis-ci.org/repseqio/repseqio)

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

# Creating library

See this [wiki page](https://github.com/repseqio/repseqio/wiki/Creating-repseqio-formatted-JSON-library).

Here is the example pipeline starting from plain fasta files:

```shell
# Importing fasta file for each gene type
# (redundant meta information specified for each gene type is required because
# library produced on each step is self-contained and requires all meta fields to be defined)

repseqio fromFasta --taxon-id 9606 \
    --species-name hs --species-name homsap \
    --chain TRB --name-index 0 \
    --gene-type V --gene-feature VRegion \
    my_genes.v.fasta my_library.v.json

repseqio fromFasta --taxon-id 9606 \
    --species-name hs --species-name homsap \
    --chain TRB --name-index 0 \
    --gene-type D --gene-feature DRegion \
    my_genes.d.fasta my_library.d.json

repseqio fromFasta --taxon-id 9606 \
    --species-name hs --species-name homsap \
    --chain TRB --name-index 0 \
    --gene-type J --gene-feature JRegion \
    my_genes.j.fasta my_library.j.json

# Merging several libraries into single file

repseqio merge my_library.v.json my_library.d.json my_library.j.json my_library.json

# Inferring intermediate anchor points (like CDR3Begin) using automated homology-driven procedure;
# built-in repseqio library will be used as reference

repseqio inferPoints -g VRegion -g JRegion -f my_library.json my_library.json
```

# Format

See this [wiki page](https://github.com/repseqio/repseqio/wiki/repseqio-JSON-library-format).

# Documentation

Usage:

```
Usage: repseqio [options] [command] [command options]
  Options:
    -h, --help
       Displays this help message.
    --version
       Output full version information.
    -v
       Output short version information.
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

    generateClones      Generate synthetic clonotypes, and write in in jclns format.
      Usage: generateClones [options] model_name|model_file_name [output.jclns]
        Options:
          -f, --force
             Force overwrite of output file(s).
          -h, --help
             Displays help for this command.
             Default: false
          -a, --in-frame
             In-frame clones only.
          -b, --no-stops
             Output clones without stop codons in CDR3 (valid only with -a /
             --in-frame).
        * -c, --number-of-clones
             Number of clones to generate.
             Default: 0
          -s, --seed
             Random generator seed (0 to use current time as random seed).

    normalizeClones      Normalize clone abundances in jclns file.
      Usage: normalizeClones [options] [input.jclns [output.jclns]]
        Options:
          -f, --force
             Force overwrite of output file(s).
          -h, --help
             Displays help for this command.
             Default: false

    exportCloneSequence      Normalize clone abundances in jclns file.
      Usage: exportCloneSequence [options] [input.jclns [output.jclns]]
        Options:
          -q, --abundance-factor
             Repeat each clonal sequence round(f*clone.abundance) times, where
             round means mathematical rounding of non-integer numbers.
          -d, --add-description
             Add description fields to fasta header (available values
             NFeature[gene_feature], AAFeature[gene_feature] - for current
             gene,NFeature[chain,gene_feature], AAFeature[chain,gene_feature] - for multi-gene clones, JSONClone,
             JSONGene, JSONClone.field_name, JSONGene.field_name, Chain). Example:
             NFeature[CDR3], AAFeature[FR3]
             Default: []
          -c, --chain
             Which chains to export
             Default: ALL
          -f, --force
             Force overwrite of output file(s).
        * -g, --gene-feature
             Gene feature to export (e.g. CDR3, VDJRegion, VDJTranscript,
             VDJTranscript+CExon1 etc...)
          -h, --help
             Displays help for this command.
             Default: false

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

    tsv      Export genes region coordinates to TSV file. To output 1-based coordinates add `-1` / `--one-based` option.
      Usage: tsv [options] input_library.json|default [output.txt]
        Options:
          -c, --chain
             Chain pattern, regexp string, all genes with matching chain record
             will be exported.
          -f, --force
             Force overwrite of output file(s).
        * -g, --gene-feature
             Gene feature(s) to export (e.g. VRegion, JRegion, VTranscript,
             etc...). To specify several features use this option several times or
             separate multiple regions with commas.
          -h, --help
             Displays help for this command.
             Default: false
          -n, --name
             Gene name pattern, regexp string, all genes with matching gene name
             will be exported.
          -1, --one-based
             Use one-based coordinates instead of zero-based and output
             inclusive end position.
             Default: false
          -s, --species
             Species name, used in the same way as --taxon-id.
          -t, --taxon-id
             Taxon id (filter multi-library file to leave single library for
             specified taxon id)

    inferPoints      Try to infer anchor point positions from gene sequences of other libraries. If no reference libraries are specified, built-in library will be used.
      Usage: inferPoints [options] input_library.json [reference_library1.json [reference_library2.json [....]]] output.json
        Options:
          -f, --force
             Force overwrite of output file(s).
        * -g, --gene-feature
             Reference gene feature to use (e.g. VRegion, JRegion, VTranscript,
             etc...). This feature will be used to align target genes with reference
             genes. Target genes must have this gene feature. This option can be used
             several times, to specify several target gene features. Inference will be
             performed in order options are specified.
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

    fromFasta      Creates boilerplate JSON library from existing fasta file.
      Usage: fromFasta [options] input.fasta output.json
        Options:
        * -c, --chain
             Chain.
          -f, --force
             Force overwrite of output file(s).
          -j, --functionality-index
             Functionality mark index (0-based) in `|`-separated FASTA
             description line (e.g. 3 for IMGT files). If this option is omitted, all genes
             are considered functional.
          --functionality-regexp
             Functionality regexp, gene is considered functional if field
             defined by -j / --functionality-index parameter matches this expression.
             Default: [\(\[]?[Ff].?
          --gene-feature
             Defines gene feature which sequecnes are contained in the file
             (e.g. VRegion, VGene, JRegion etc..).
        * -g, --gene-type
             Gene type (V/D/J/C)
          -h, --help
             Displays help for this command.
             Default: false
          -i, --ignore-duplicates
             Ignore duplicate genes
        * -n, --name-index
             Gene name index (0-based) in `|`-separated FASTA description line
             (e.g. 1 for IMGT files).
             Default: 0
          -s, --species-name
             Species names (can be used multiple times)
             Default: []
        * -t, --taxon-id
             Taxon id
          -L
             Amino-acid pattern of anchor point. Have higher priority than -P
             for the same anchor point.
             Syntax: -Lkey=value
             Default: {}
          -P
             Positions of anchor points in padded / non-padded file. To define
             position relative to the end of sequence use negative values: -1 = sequence
             end, -2 = last but one letter. Example: -PFR1Begin=0 -PVEnd=-1 ,
             equivalent of --gene-feature VRegion
             Syntax: -Pkey=value
             Default: {}

    fromPaddedFasta      Converts library from padded fasta file (IMGT-like) to json library. This command can operate in two modes
             (1) if 3 file-parameters are specified, it will create separate non-padded fasta and put links inside newly created library pointing to it,
             (2) if 2 file-parameters are specified, create only library file, and embed sequences directly into it.
             To use library generated using mode (1) one need both output files, (see also 'repseqio compile').
             If library is intended for further editing and/or submission to version control system option (1) is recommended.
      Usage: fromPaddedFasta [options] input_padded.fasta [output.fasta] output.json[.gz]
        Options:
        * -c, --chain
             Chain.
          -f, --force
             Force overwrite of output file(s).
          -j, --functionality-index
             Functionality mark index (0-based) in `|`-separated FASTA
             description line (e.g. 3 for IMGT files). If this option is omitted, all genes
             are considered functional.
          --functionality-regexp
             Functionality regexp, gene is considered functional if field
             defined by -j / --functionality-index parameter matches this expression.
             Default: [\(\[]?[Ff].?
          --gene-feature
             Defines gene feature which sequecnes are contained in the file
             (e.g. VRegion, VGene, JRegion etc..).
        * -g, --gene-type
             Gene type (V/D/J/C)
          -h, --help
             Displays help for this command.
             Default: false
          -i, --ignore-duplicates
             Ignore duplicate genes
        * -n, --name-index
             Gene name index (0-based) in `|`-separated FASTA description line
             (e.g. 1 for IMGT files).
             Default: 0
          -p, --padding-character
             Padding character
             Default: .
          -s, --species-name
             Species names (can be used multiple times)
             Default: []
        * -t, --taxon-id
             Taxon id
          -L
             Amino-acid pattern of anchor point. Have higher priority than -P
             for the same anchor point.
             Syntax: -Lkey=value
             Default: {}
          -P
             Positions of anchor points in padded / non-padded file. To define
             position relative to the end of sequence use negative values: -1 = sequence
             end, -2 = last but one letter. Example: -PFR1Begin=0 -PVEnd=-1 ,
             equivalent of --gene-feature VRegion
             Syntax: -Pkey=value
             Default: {}
```
