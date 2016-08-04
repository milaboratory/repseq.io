package io.repseq.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

/**
 * Immutable set of strings
 */
public final class Chains implements Iterable<String> {
    public static final Chains TRA = new Chains("TRA");
    public static final Chains TRB = new Chains("TRB");
    public static final Chains TRG = new Chains("TRG");
    public static final Chains TRD = new Chains("TRD");
    public static final Chains TCR = new Chains("TRA", "TRB", "TRG", "TRD");

    public static final Chains IGH = new Chains("IGH");
    public static final Chains IGK = new Chains("IGK");
    public static final Chains IGL = new Chains("IGL");
    public static final Chains IG = new Chains("IGH", "IGK", "IGL");

    final Set<String> chains;

    public Chains(String... chains) {
        this.chains = new HashSet<>(Arrays.asList(chains));
    }

    @JsonCreator
    public Chains(Set<String> chains) {
        this.chains = new HashSet<>(chains);
    }

    private Chains(HashSet<String> chains) {
        this.chains = chains;
    }

    @JsonValue
    private Set<String> getChains() {
        return chains;
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableCollection(chains).iterator();
    }

    public Chains merge(Chains other) {
        HashSet<String> s = new HashSet<>();
        s.addAll(this.chains);
        s.addAll(other.chains);
        return new Chains(s);
    }

    public boolean intersects(Chains other) {
        for (String s2e : other.chains)
            if (chains.contains(s2e))
                return true;
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chains)) return false;

        Chains chains1 = (Chains) o;

        return chains.equals(chains1.chains);

    }

    @Override
    public int hashCode() {
        return chains.hashCode();
    }
}
