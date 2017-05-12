package io.repseq.core;

import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.KnownSequenceFragmentData;
import io.repseq.dto.VDJCDataUtils;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceAddress;
import io.repseq.seqbase.SequenceResolver;
import io.repseq.seqbase.SequenceResolvers;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Registry of VDJCLibraries. Central storage for VDJCLibraries objects. VDJCLibraries can be created only using
 * VDJCLibraryRegistry.
 */
public final class VDJCLibraryRegistry {
    /**
     * Name or alias-name of default V, D, J, C gene library. Now it is built-in RepSeq.IO library,
     * hosted at https://github.com/repseqio/library . This name is uset to retrive library by
     */
    public static final String DEFAULT_LIBRARY_NAME = "default";
    /**
     * If this field is null -> default sequence resolver is used
     */
    final SequenceResolver sequenceResolver;
    /**
     * Resolvers to search for VDJCLibrary with particular name
     */
    final List<LibraryResolver> libraryResolvers = new ArrayList<>();
    /**
     * Collected from all loaded VDJCLibrary
     */
    final Map<String, Long> speciesNames = new HashMap<>();
    /**
     * Collected from all loaded VDJCLibrary
     */
    final Map<Long, List<String>> speciesNamesReverse = new HashMap<>();
    /**
     * Loaded libraries
     */
    final Map<VDJCLibraryId, VDJCLibrary> libraries = new HashMap<>();
    /**
     * Store successfully loaded libraries
     */
    final HashSet<LibraryLoadRequest> loadedLibraries = new HashSet<>();
    /**
     * Library name aliases
     */
    final HashMap<String, String> aliases = new HashMap<>();

    /**
     * Creates new VDJCLibraryRegistry with default sequence resolver
     */
    public VDJCLibraryRegistry() {
        this(null);
    }

    /**
     * Creates new VDJCLibraryRegistry with specific sequence resolver.
     *
     * @param sequenceResolver sequece resolver or null to use default resolver
     */
    public VDJCLibraryRegistry(SequenceResolver sequenceResolver) {
        this.sequenceResolver = sequenceResolver;
    }

    /**
     * Return sequence resolver used by this registry
     *
     * @return sequence resolver used by this registry
     */
    public SequenceResolver getSequenceResolver() {
        return sequenceResolver == null ? SequenceResolvers.getDefault() : sequenceResolver;
    }

    /**
     * Returns collection of libraries that are currently loaded by this registry.
     *
     * @return collection of libraries that are currently loaded by this registry
     */
    public Collection<VDJCLibrary> getLoadedLibraries() {
        return libraries.values();
    }

    /**
     * Returns list of libraries that are currently loaded by this registry and has specified name.
     *
     * @return list of libraries that are currently loaded by this registry and has specified name
     */
    public List<VDJCLibrary> getLoadedLibrariesByName(String libraryName) {
        ArrayList<VDJCLibrary> libs = new ArrayList<>();

        for (Map.Entry<VDJCLibraryId, VDJCLibrary> entry : libraries.entrySet())
            if (entry.getKey().getLibraryName().equals(libraryName))
                libs.add(entry.getValue());

        return libs;
    }

    /**
     * Returns list of libraries that are currently loaded by this registry and has specified name pattern.
     *
     * @return list of libraries that are currently loaded by this registry and has specified name pattern
     */
    public List<VDJCLibrary> getLoadedLibrariesByNamePattern(Pattern libraryNamePattern) {
        ArrayList<VDJCLibrary> libs = new ArrayList<>();

        for (Map.Entry<VDJCLibraryId, VDJCLibrary> entry : libraries.entrySet())
            if (libraryNamePattern.matcher(entry.getKey().getLibraryName()).matches())
                libs.add(entry.getValue());

        return libs;
    }

    /**
     * Resolves species name to taxon id
     *
     * @param name species name
     * @return taxon id
     * @throws IllegalArgumentException if can't resolve
     */
    public long resolveSpecies(String name) {
        name = canonicalizeSpeciesName(name);
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
        }
        Long taxonId = speciesNames.get(name);
        if (taxonId == null)
            throw new IllegalArgumentException("Can't resolve species name: " + name);
        return taxonId;
    }

    /**
     * Try resolves species name to taxon id. Return null if failed
     *
     * @param name species name
     * @return taxon id or null if not found
     */
    public Long tryResolveSpecies(String name) {
        name = canonicalizeSpeciesName(name);
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
        }
        return speciesNames.get(name);
    }

    /**
     * Returns list of known species names for a given taxon id.
     *
     * @param taxonId taxon id
     * @return list of known species names for a given taxon id
     */
    public List<String> getSpeciesNames(long taxonId) {
        return speciesNamesReverse.containsKey(taxonId) ? speciesNamesReverse.get(taxonId) : Collections.<String>emptyList();
    }

    /**
     * Canonicalize a species name to ignore case and some special characters
     *
     * @param speciesName non canonicalized species name
     * @return canonicalized species name
     */
    private String canonicalizeSpeciesName(String speciesName) {
        return speciesName.toLowerCase();
    }

    /**
     * Register library resolver to be used for automatic load of libraries by name and species
     *
     * @param resolver resolver to add
     */
    public void addLibraryResolver(LibraryResolver resolver) {
        libraryResolvers.add(resolver);
    }


    /**
     * Adds path resolver to search for libraries with {libraryName}.json[.gz] file names in specified folder.
     *
     * @param searchPath path to search for {libraryName}.json[.gz] files
     */
    public void addPathResolver(Path searchPath) {
        addLibraryResolver(new FolderLibraryResolver(searchPath.toAbsolutePath(), false));
    }

    /**
     * Adds path resolver to search for libraries with {libraryName}[.*].json[.gz] file names in specified folder.
     *
     * @param searchPath path to search for {libraryName}[.*].json[.gz] files
     */
    public void addPathResolverWithPartialSearch(Path searchPath) {
        addLibraryResolver(new FolderLibraryResolver(searchPath.toAbsolutePath(), true));
    }

    /**
     * Adds path resolver to search for libraries with {libraryName}[.*].json[.gz] file names in specified folder.
     *
     * @param searchPath path to search for {libraryName}[.*].json[.gz] files
     */
    public void addPathResolverWithPartialSearch(String searchPath) {
        addPathResolverWithPartialSearch(Paths.get(searchPath));
    }

    /**
     * Adds path resolver to search for libraries with {libraryName}.json[.gz] file names in specified folder.
     *
     * @param searchPath path to search for {libraryName}.json files[.gz]
     */
    public void addPathResolver(String searchPath) {
        addPathResolver(Paths.get(searchPath));
    }

    /**
     * Adds classpath resolver to search for libraries with {libraryName}.json names in the specified folder
     *
     * @param searchPath root address to search for {libraryName}.json files inn classpath
     */
    public void addClasspathResolver(String searchPath) {
        addClasspathResolver(searchPath, VDJCLibraryRegistry.class.getClassLoader());
    }

    /**
     * Adds classpath resolver to search for libraries with {libraryName}.json names in the specified folder
     *
     * @param searchPath  root address to search for {libraryName}.json files inn classpath
     * @param classLoader class loader
     */
    public void addClasspathResolver(String searchPath, ClassLoader classLoader) {
        if (!searchPath.endsWith("/"))
            searchPath = searchPath + "/";
        addLibraryResolver(new ClasspathLibraryResolver(searchPath, classLoader));
    }

    /**
     * Return gene by it's global id. Libraries may be loaded during gene resolution.
     *
     * @param geneId global gene id
     * @return gene instance
     * @throws IllegalArgumentException if gene or library not found
     */
    public VDJCGene getGene(VDJCGeneId geneId) {
        VDJCGene gene = getLibrary(geneId.libraryId).get(geneId.geneName);
        if (gene == null)
            throw new IllegalArgumentException("Can't find gene: " + geneId);
        return gene;
    }

    /**
     * Returns library with specified name and specified species.
     *
     * If not opened yet, library will be loaded using library providers added to this registry.
     *
     * @param libraryName library name
     * @param species     species name
     * @return library
     * @throws RuntimeException         if no library found
     * @throws IllegalArgumentException if species can't be resolved
     */
    public VDJCLibrary getLibrary(String libraryName, String species) {
        return getLibrary(libraryName, species, -1, null);
    }

    /**
     * Returns library with specified name and specified species.
     *
     * If not opened yet, library will be loaded using library providers added to this registry.
     *
     * @param libraryName library name
     * @param taxonId     taxon id
     * @return library
     * @throws RuntimeException if no library found
     */
    public VDJCLibrary getLibrary(String libraryName, long taxonId) {
        return getLibrary(libraryName, null, taxonId, null);
    }

    /**
     * Returns library with specified name and specified species.
     *
     * If not opened yet, library will be loaded using library providers added to this registry.
     *
     * @param libraryId identifier of the library
     * @return library
     * @throws RuntimeException if no library found or checksum check failed
     */
    public VDJCLibrary getLibrary(VDJCLibraryId libraryId) {
        return getLibrary(libraryId.getLibraryName(), null, libraryId.getTaxonId(), libraryId.getChecksum());
    }

    /**
     * Used in {@link #getLibrary(String, String, long, byte[])}
     */
    private VDJCLibrary tryGetLibrary(String libraryName, String species, long taxonId, byte[] checksum) {
        // Try resolve species if it was provided in string form
        if (species != null) {
            Long tId = tryResolveSpecies(species);
            if (tId == null)
                return null;
            taxonId = tId;
        }

        // Key to search in map
        VDJCLibraryId libraryId = new VDJCLibraryId(libraryName, taxonId);

        // Try get from map
        VDJCLibrary vdjcLibrary = libraries.get(libraryId);

        // If not found try aliases
        if (vdjcLibrary == null) {
            if (aliases.containsKey(libraryName))
                return tryGetLibrary(aliases.get(libraryName), null, taxonId, checksum);
            else
                return null;
        }

        // Check for checksum if it was provided
        if (checksum != null && !Arrays.equals(checksum, vdjcLibrary.getChecksum()))
            throw new RuntimeException("Different checksums.");

        // OK
        return vdjcLibrary;
    }

    /**
     * Root method for library resolution
     *
     * @param libraryName library name
     * @param species     if not null will also try to resolve species; if not null taxonId can be any value
     * @param taxonId     taxon id, used if species parameter is not provided
     * @param checksum    if not null will perform checksum check
     * @return library
     * @throws RuntimeException if failed to resolve library
     */
    private synchronized VDJCLibrary getLibrary(String libraryName, String species, long taxonId, byte[] checksum) {
        VDJCLibrary vdjcLibrary;

        // Search for already loaded libraries and if found return it
        if ((vdjcLibrary = tryGetLibrary(libraryName, species, taxonId, checksum)) != null)
            return vdjcLibrary;

        // Try load library using provided resolvers
        for (LibraryResolver resolver : libraryResolvers) {
            // Try resolve library using this resolver
            tryResolve(resolver, libraryName);

            // Check whether required library was loaded
            vdjcLibrary = tryGetLibrary(libraryName, species, taxonId, checksum);

            // If found return it
            if (vdjcLibrary != null)
                return vdjcLibrary;

            // If not - continue
        }

        // If library was not found nor loaded throw exception
        throw new RuntimeException("Can't find library for following library name and species: " + libraryName +
                " + " + (species != null ? species : taxonId));
    }

    /**
     * Try resolve library using all available resolvers, and register all resolved libraries
     *
     * @param name library name
     */
    public void loadAllLibraries(String name) {
        for (LibraryResolver resolver : libraryResolvers)
            tryResolve(resolver, name);
    }

    /**
     * Load all possible libraries using all available resolvers (resolvers having such feature)
     */
    public void loadAllLibraries() {
        List<String> allNames = new ArrayList<>();

        for (LibraryResolver resolver : libraryResolvers)
            if (resolver instanceof LibraryNameListProvider)
                allNames.addAll(((LibraryNameListProvider) resolver).getLibraryNameList());

        for (String name : allNames)
            loadAllLibraries(name);
    }

    private void tryResolve(LibraryResolver resolver, String libraryName) {
        // Check if this combination of resolver and libraryName was already being processed
        LibraryLoadRequest request = new LibraryLoadRequest(resolver, libraryName);
        if (loadedLibraries.contains(request))
            return;

        // Try resolve
        VDJCLibraryData[] resolved = resolver.resolve(libraryName);

        // Marking this request as already processed
        loadedLibraries.add(request);

        // If not resolved
        if (resolved == null) {
            if (resolver instanceof AliasResolver) {
                String newLibraryName = ((AliasResolver) resolver).resolveAlias(libraryName);
                if (newLibraryName == null)
                    return; // proceed to next resolver
                tryResolve(resolver, newLibraryName);
                if (aliases.containsKey(libraryName) && !aliases.get(libraryName).equals(newLibraryName))
                    throw new RuntimeException("Conflicting aliases " + libraryName + " -> " + newLibraryName +
                            " / " + aliases.get(libraryName));
                aliases.put(libraryName, newLibraryName);
            }
            return; // proceed to next resolver
        }

        // Registering loaded library entries
        for (VDJCLibraryData vdjcLibraryData : resolved) {
            VDJCLibraryId sal = new VDJCLibraryId(libraryName, vdjcLibraryData.getTaxonId());

            // Check whether library is already loaded manually or using higher priority resolver
            // (or using previous resolution call with the same library name)
            if (libraries.containsKey(sal)) // If so - ignore it
                continue;

            // Registering library
            registerLibrary(resolver.getContext(libraryName), libraryName, vdjcLibraryData);
        }
    }

    /**
     * Creates and registers single library from VDJCLibraryData
     *
     * @param context context to use for resolution of sequences
     * @param name    library name
     * @param data    library data
     * @return created library
     */
    public synchronized VDJCLibrary registerLibrary(Path context, String name, VDJCLibraryData data) {
        // Creating library object
        VDJCLibrary library = new VDJCLibrary(data, name, this, context);

        // Getting library id
        VDJCLibraryId rootId = library.getLibraryIdWithoutChecksum();

        // Check if such library is already registered
        if (libraries.containsKey(rootId))
            throw new RuntimeException("Duplicate library: " + rootId);

        // Loading known sequence fragments from VDJCLibraryData to current SequenceResolver
        SequenceResolver resolver = getSequenceResolver();
        for (KnownSequenceFragmentData fragment : data.getSequenceFragments())
            resolver.resolve(new SequenceAddress(context, fragment.getUri())).setRegion(fragment.getRange(),
                    fragment.getSequence());

        // Adding genes
        for (VDJCGeneData gene : data.getGenes())
            VDJCLibrary.addGene(library, gene);

        // Adding common species names
        Long taxonId = data.getTaxonId();
        for (String speciesName : data.getSpeciesNames()) {
            String cSpeciesName = canonicalizeSpeciesName(speciesName);
            if (speciesNames.containsKey(cSpeciesName) && !speciesNames.get(cSpeciesName).equals(taxonId))
                throw new IllegalArgumentException("Mismatch in common species name between several libraries. " +
                        "(Library name = " + name + "; name = " + speciesName + ").");
            speciesNames.put(cSpeciesName, taxonId);
            List<String> names = speciesNamesReverse.get(taxonId);
            if (names == null)
                speciesNamesReverse.put(taxonId, names = new ArrayList<>());
            names.add(speciesName);
        }

        // Adding this library to collection
        libraries.put(library.getLibraryIdWithoutChecksum(), library);

        return library;
    }

    /**
     * Register libraries from specific file
     *
     * @param file libraries json file
     */
    public void registerLibraries(String file) {
        registerLibraries(Paths.get(file));
    }

    /**
     * Register libraries from specific file with specified name
     *
     * @param file libraries json file
     * @param name library name
     */
    public void registerLibraries(String file, String name) {
        registerLibraries(Paths.get(file), name);
    }

    /**
     * Register libraries from specific file
     *
     * @param file libraries json file
     */
    public void registerLibraries(Path file) {
        registerLibraries(file, libraryNameFromFileName(file.getFileName().toString()));
    }

    /**
     * Register libraries from specific file with specified name
     *
     * @param file libraries json file
     * @param name library name
     */
    public void registerLibraries(Path file, String name) {
        file = file.toAbsolutePath();
        try {
            // Registering libraries
            for (VDJCLibraryData library : VDJCDataUtils.readArrayFromFile(file))
                registerLibrary(file.getParent(), name, library);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default registry
     */
    private static volatile VDJCLibraryRegistry defaultRegistry;

    static {
        resetDefaultRegistry();
    }

    /**
     * Resets default VDJLibrary registry
     */
    public static void resetDefaultRegistry() {
        resetDefaultRegistry(null);
    }

    /**
     * Resets default VDJLibrary registry and sets specific sequence resolver
     */
    public static void resetDefaultRegistry(SequenceResolver resolver) {
        defaultRegistry = new VDJCLibraryRegistry(resolver);
        defaultRegistry.addClasspathResolver("libraries/");
    }

    /**
     * Creates new instance of basic default registry
     *
     * @return new instance of basic default registry
     */
    public static VDJCLibraryRegistry createDefaultRegistry() {
        VDJCLibraryRegistry registry = new VDJCLibraryRegistry();
        registry.addClasspathResolver("libraries/");
        return registry;
    }

    /**
     * Returns default VDJCLibraryRegistry.
     *
     * @return default VDJCLibraryRegistry
     */
    public static VDJCLibraryRegistry getDefault() {
        return defaultRegistry;
    }

    /**
     * Retrieves default library (see description for {@link #DEFAULT_LIBRARY_NAME}) from default registry for the
     * specified species name.
     *
     * Equivalent of calling:
     * getDefault().getLibrary(DEFAULT_LIBRARY_NAME, species);
     *
     * @param species species name
     * @return library
     * @throws IllegalArgumentException if species can't be resolved
     */
    public static VDJCLibrary getDefaultLibrary(String species) {
        return getDefault().getLibrary(DEFAULT_LIBRARY_NAME, species);
    }

    /**
     * Retrieves default library (see description for {@link #DEFAULT_LIBRARY_NAME}) from default registry for the
     * specified species.
     *
     * Equivalent of calling:
     * getDefault().getLibrary(DEFAULT_LIBRARY_NAME, taxonId);
     *
     * @param taxonId taxon id
     * @return library
     */
    public static VDJCLibrary getDefaultLibrary(long taxonId) {
        return getDefault().getLibrary(DEFAULT_LIBRARY_NAME, taxonId);
    }

    /**
     * Return the list of library resolvers used by this registry
     *
     * @return list of library resolvers used by this registry
     */
    public List<LibraryResolver> getLibraryResolvers() {
        return Collections.unmodifiableList(libraryResolvers);
    }

    /**
     * Tries to resolve library name to array of VDJCLibraryData[] objects
     *
     * E.g. searches existence of file with {libraryName}.json name in specific folder.
     */
    public interface LibraryResolver {
        VDJCLibraryData[] resolve(String libraryName);

        Path getContext(String libraryName);
    }

    /**
     * Interface implemented by {@link LibraryResolver} if it can also resolve library name aliases
     */
    public interface AliasResolver {
        String resolveAlias(String libraryName);
    }

    /**
     * Interface implemented by {@link LibraryResolver} if it can provide list of all base library names it can resolve
     */
    public interface LibraryNameListProvider {
        List<String> getLibraryNameList();
    }

    static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(?i).json(?:\\.gz)?$");

    static String libraryNameFromFileName(String fileName) {
        return FILE_EXTENSION_PATTERN.matcher(fileName).replaceAll("");
    }

    /**
     * Load library data from {libraryName}.json files in specified folder.
     */
    public static final class FolderLibraryResolver implements LibraryResolver, AliasResolver, LibraryNameListProvider {
        private final Path path;
        private final boolean searchForPartialNames;

        public FolderLibraryResolver(Path path, boolean searchForPartialNames) {
            this.path = path;
            this.searchForPartialNames = searchForPartialNames;
        }

        public Path getPath() {
            return path;
        }

        @Override
        public Path getContext(String libraryName) {
            return path;
        }

        @Override
        public List<String> getLibraryNameList() {
            if (!Files.exists(path))
                return Collections.EMPTY_LIST;

            List<String> result = new ArrayList<>();

            try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
                for (Path subPath : paths) {
                    String name = subPath.getFileName().toString();

                    if (!name.toLowerCase().endsWith(".json") && !name.toLowerCase().endsWith(".json.gz"))
                        continue;

                    result.add(libraryNameFromFileName(name));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return result;
        }

        @Override
        public String resolveAlias(String libraryName) {
            if (searchForPartialNames) {
                if (!Files.exists(path))
                    return null;

                List<String> candidates = new ArrayList<>();
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
                    for (Path subPath : paths) {
                        String name = subPath.getFileName().toString();

                        if (!name.toLowerCase().endsWith(".json") && !name.toLowerCase().endsWith(".json.gz"))
                            continue;

                        name = libraryNameFromFileName(name);

                        if (name.startsWith(libraryName + "."))
                            candidates.add(name);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (candidates.isEmpty())
                    return null;

                Collections.sort(candidates, VDJCDataUtils.SMART_COMPARATOR_INVERSE);

                return candidates.get(0);
            }
            return null;
        }

        @Override
        public VDJCLibraryData[] resolve(String libraryName) {
            try {
                Path filePath = path.resolve(libraryName + ".json");

                if (Files.exists(filePath))
                    // Getting libraries from file
                    return GlobalObjectMappers.ONE_LINE.readValue(filePath.toFile(), VDJCLibraryData[].class);

                filePath = path.resolve(libraryName + ".json.gz");
                if (Files.exists(filePath))
                    try (InputStream os = new BufferedInputStream(new GZIPInputStream(new FileInputStream(filePath.toFile())))) {
                        // Getting libraries from gzipped file
                        return GlobalObjectMappers.ONE_LINE.readValue(os, VDJCLibraryData[].class);
                    }

                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Load library data from {libraryName}.json files in specified folder.
     */
    public static final class ClasspathLibraryResolver implements LibraryResolver, AliasResolver, LibraryNameListProvider {
        private final String path;
        private final ClassLoader classLoader;

        public ClasspathLibraryResolver(String path, ClassLoader classLoader) {
            this.path = path;
            this.classLoader = classLoader;
        }

        public String getPath() {
            return path;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        @Override
        public Path getContext(String libraryName) {
            return null;
        }

        @Override
        public List<String> getLibraryNameList() {
            Reflections reflections = new Reflections("libraries", new ResourcesScanner());
            Set<String> resources = reflections.getResources(Pattern.compile(".*\\.json"));
            List<String> result = new ArrayList<>();
            for (String resource : resources)
                result.add(libraryNameFromFileName(resource).replace("libraries/", ""));
            return result;
        }

        @Override
        public String resolveAlias(String libraryName) {
            try (InputStream stream = classLoader.getResourceAsStream(path + libraryName + ".alias")) {
                if (stream == null)
                    return null;
                return IOUtils.toString(stream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public VDJCLibraryData[] resolve(String libraryName) {
            try (InputStream stream = classLoader.getResourceAsStream(path + libraryName + ".json")) {
                if (stream == null)
                    return null;

                // Getting libraries from file
                return GlobalObjectMappers.ONE_LINE.readValue(stream, VDJCLibraryData[].class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class LibraryLoadRequest {
        // TODO weak reference ?
        final LibraryResolver resolver;
        final String libraryName;

        public LibraryLoadRequest(LibraryResolver resolver, String libraryName) {
            this.resolver = resolver;
            this.libraryName = libraryName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LibraryLoadRequest)) return false;

            LibraryLoadRequest that = (LibraryLoadRequest) o;

            if (resolver != null ? resolver == that.resolver : that.resolver != null) return false;
            return libraryName != null ? libraryName.equals(that.libraryName) : that.libraryName == null;

        }

        @Override
        public int hashCode() {
            int result = resolver != null ? System.identityHashCode(resolver) : 0;
            result = 31 * result + (libraryName != null ? libraryName.hashCode() : 0);
            return result;
        }
    }
}
