package io.repseq.core;

import com.milaboratory.util.GlobalObjectMappers;
import io.repseq.dto.KnownSequenceFragmentData;
import io.repseq.dto.VDJCGeneData;
import io.repseq.dto.VDJCLibraryData;
import io.repseq.seqbase.SequenceAddress;
import io.repseq.seqbase.SequenceResolver;
import io.repseq.seqbase.SequenceResolvers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Registry of VDJCLibraries. Central storage for VDJCLibraries objects. VDJCLibraries can be created only using
 * VDJCLibraryRegistry.
 */
public final class VDJCLibraryRegistry {
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
     * Loaded libraries
     */
    final Map<VDJCLibraryId, VDJCLibrary> libraries = new HashMap<>();
    /**
     * Store successfully loaded libraries
     */
    final HashSet<LibraryLoadRequest> loadedLibraries = new HashSet<>();

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
     * Adds path resolver to search for libraries with {libraryName}.json file names in specified folder.
     *
     * @param searchPath path to search for {libraryName}.json files
     */
    public void addPathResolver(Path searchPath) {
        addLibraryResolver(new FolderLibraryResolver(searchPath.toAbsolutePath()));
    }

    /**
     * Adds path resolver to search for libraries with {libraryName}.json file names in specified folder.
     *
     * @param searchPath path to search for {libraryName}.json files
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
     * Used in {@link #getLibrary(String, String, long, String)}
     */
    private VDJCLibrary tryGetLibrary(String libraryName, String species, long taxonId, String checksum) {
        // Try resolve species if it was provided in string form
        if (species != null) {
            Long tId = tryResolveSpecies(species);
            if (tId == null)
                return null;
            taxonId = tId;
        }

        // Key to search in map
        VDJCLibraryId libraryId = new VDJCLibraryId(libraryName, taxonId, checksum);

        // Try get from map
        VDJCLibrary vdjcLibrary = libraries.get(libraryId);

        // If not found return null
        if (vdjcLibrary == null)
            return null;

        // Check for checksum if it was provided
        if (checksum != null && !checksum.equals(vdjcLibrary.getChecksum()))
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
    private synchronized VDJCLibrary getLibrary(String libraryName, String species, long taxonId, String checksum) {
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
        throw new RuntimeException("Can't find library for following library name and species: " + libraryName + " + " + (species != null ? species : taxonId));
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

    private void tryResolve(LibraryResolver resolver, String libraryName) {
        // Check if this combination of resolver and libraryName was already being processed
        LibraryLoadRequest request = new LibraryLoadRequest(resolver, libraryName);
        if (loadedLibraries.contains(request))
            return;

        // Try resolve
        VDJCLibraryData[] resolved = resolver.resolve(libraryName);

        // If not resolved proceed to next resolver
        if (resolved == null)
            return;

        // Marking this request as already processed
        loadedLibraries.add(request);

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

        // Check if such library is already registered
        if (libraries.containsKey(library.getLibraryId()))
            throw new RuntimeException("Duplicate library: " + library.getLibraryId());

        // Loading known sequence fragments from VDJCLibraryData to current SequenceResolver
        SequenceResolver resolver = getSequenceResolver();
        for (KnownSequenceFragmentData fragment : data.getSequenceFragments())
            resolver.resolve(new SequenceAddress(context, fragment.getUri())).setRegion(fragment.getRange(), fragment.getSequence());

        // Adding genes
        for (VDJCGeneData gene : data.getGenes())
            VDJCLibrary.addGene(library, gene);

        // Adding common species names
        Long taxonId = data.getTaxonId();
        for (String speciesName : data.getSpeciesNames()) {
            String cSpeciesName = canonicalizeSpeciesName(speciesName);
            if (speciesNames.containsKey(cSpeciesName) && !speciesNames.get(cSpeciesName).equals(taxonId))
                throw new IllegalArgumentException("Mismatch in common species name between several libraries. (Library name = " + name + "; name = " + speciesName + ").");
            speciesNames.put(cSpeciesName, taxonId);
        }

        // Adding this library to collection
        libraries.put(library.getLibraryId(), library);

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
        String name = file.getFileName().toString();
        name = name.toLowerCase().replaceAll("(?i).json$", "");
        registerLibraries(file, name);
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
            // Getting libraries from file
            VDJCLibraryData[] libraries = GlobalObjectMappers.ONE_LINE.readValue(file.toFile(), VDJCLibraryData[].class);

            // Registering libraries
            for (VDJCLibraryData library : libraries)
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
     * Returns default VDJCLibraryRegistry.
     *
     * @return default VDJCLibraryRegistry
     */
    public static VDJCLibraryRegistry getDefault() {
        return defaultRegistry;
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
     * Load library data from {libraryName}.json files in specified folder.
     */
    public static final class FolderLibraryResolver implements LibraryResolver {
        final Path path;

        public FolderLibraryResolver(Path path) {
            this.path = path;
        }

        @Override
        public Path getContext(String libraryName) {
            return path;
        }

        @Override
        public VDJCLibraryData[] resolve(String libraryName) {
            try {
                Path filePath = path.resolve(libraryName + ".json");
                if (!Files.exists(filePath))
                    return null;

                // Getting libraries from file
                return GlobalObjectMappers.ONE_LINE.readValue(filePath.toFile(), VDJCLibraryData[].class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Load library data from {libraryName}.json files in specified folder.
     */
    public static final class ClasspathLibraryResolver implements LibraryResolver {
        final String path;
        final ClassLoader classLoader;

        public ClasspathLibraryResolver(String path, ClassLoader classLoader) {
            this.path = path;
            this.classLoader = classLoader;
        }

        @Override
        public Path getContext(String libraryName) {
            return null;
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
