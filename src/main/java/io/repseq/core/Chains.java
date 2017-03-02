package io.repseq.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

/**
 * Immutable set of strings
 */
public final class Chains implements Iterable<String> {
    /**
     * Special chains object represents all possible alleles.
     */
    public static final Chains ALL = new Chains((HashSet<String>) null);

    public static final Chains EMPTY = new Chains();

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
        if (chains == null)
            throw new RuntimeException("Serialization of ALL chains is not implemented.");
        return chains;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<String> iterator() {
        return chains == null ? Collections.EMPTY_LIST.iterator() : Collections.unmodifiableCollection(chains).iterator();
    }

    public Chains merge(Chains other) {
        if (chains == null || other.chains == null)
            return ALL;

        if (other.chains.equals(this.chains))
            return this;

        HashSet<String> s = new HashSet<>();
        s.addAll(this.chains);
        s.addAll(other.chains);
        return new Chains(s);
    }

    public Chains intersection(Chains other) {
        if (chains == null && other.chains == null)
            return ALL;

        if (chains == null || (other.chains != null && this.chains.containsAll(other.chains)))
            return other;

        if (other.chains == null || other.chains.containsAll(this.chains))
            return this;

        HashSet<String> s = new HashSet<>();
        s.addAll(this.chains);
        s.retainAll(other.chains);
        return new Chains(s);
    }

    public boolean isEmpty() {
        return chains != null && chains.isEmpty();
    }

    public boolean contains(String chain) {
        if (this.chains == null)
            return true;
        return chains.contains(chain);
    }

    public boolean intersects(Chains other) {
        if (other.chains == null && this.chains == null)
            return true;

        if (other.chains == null)
            return !this.chains.isEmpty();

        if (this.chains == null)
            return !other.chains.isEmpty();

        for (String s2e : other.chains)
            if (chains.contains(s2e))
                return true;

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chains)) return false;

        Chains strings = (Chains) o;

        return chains != null ? chains.equals(strings.chains) : strings.chains == null;

    }

    @Override
    public int hashCode() {
        return chains != null ? chains.hashCode() : 0;
    }

    @Override
    public String toString() {
        if (chains == null)
            return "ALL";
        else if (chains.isEmpty())
            return "";
        else {
            String[] c = chains.toArray(new String[chains.size()]);
            Arrays.sort(c);
            StringBuilder b = new StringBuilder();
            for (int i = 0; ; i++) {
                b.append(c[i]);
                if (i == c.length - 1)
                    return b.toString();
                b.append(",");
            }
        }
    }
}
