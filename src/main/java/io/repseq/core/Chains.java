/*
 * Copyright 2019 MiLaboratory, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.repseq.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.milaboratory.primitivio.annotations.Serializable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable set of strings
 */
@Serializable(asJson = true)
public final class Chains implements Iterable<String> {
    /**
     * Special chains object represents all possible alleles.
     */
    public static final Chains ALL = new Chains((HashSet<String>) null);
    public static final NamedChains ALL_NAMED = new NamedChains("ALL", ALL);

    public static final Chains EMPTY = new Chains();
    public static final NamedChains EMPTY_NAMED = new NamedChains("EMPTY", EMPTY);

    public static final Chains TRA = new Chains("TRA");
    public static final NamedChains TRA_NAMED = new NamedChains("TRA", TRA);
    public static final Chains TRB = new Chains("TRB");
    public static final NamedChains TRB_NAMED = new NamedChains("TRB", TRB);
    public static final Chains TRG = new Chains("TRG");
    public static final NamedChains TRG_NAMED = new NamedChains("TRG", TRG);
    public static final Chains TRD = new Chains("TRD");
    public static final NamedChains TRD_NAMED = new NamedChains("TRD", TRD);
    public static final Chains TRAD = new Chains("TRA", "TRD");
    public static final NamedChains TRAD_NAMED = new NamedChains("TRAD", TRAD);
    public static final Chains TCR = new Chains("TRA", "TRB", "TRG", "TRD");
    public static final NamedChains TCR_NAMED = new NamedChains("TCR", TCR);

    public static final Chains IGH = new Chains("IGH");
    public static final NamedChains IGH_NAMED = new NamedChains("IGH", IGH);
    public static final Chains IGK = new Chains("IGK");
    public static final NamedChains IGK_NAMED = new NamedChains("IGK", IGK);
    public static final Chains IGL = new Chains("IGL");
    public static final NamedChains IGL_NAMED = new NamedChains("IGL", IGL);
    public static final Chains IG = new Chains("IGH", "IGK", "IGL");
    public static final NamedChains IG_NAMED = new NamedChains("IG", IG);

    public static final class NamedChains {
        public final String name;
        public final Chains chains;

        public NamedChains(Chains chains) {
            this(chains.chains.stream().sorted().collect(Collectors.joining("_")), chains);
        }

        public NamedChains(String name, Chains chains) {
            this.name = name;
            this.chains = chains;
        }
    }

    public static final List<NamedChains> WELL_KNOWN_CHAINS = new ArrayList<>();
    public static final Map<String, Chains> WELL_KNOWN_CHAINS_MAP = new HashMap<>();

    static {
        WELL_KNOWN_CHAINS.add(TRA_NAMED);
        WELL_KNOWN_CHAINS.add(TRB_NAMED);
        WELL_KNOWN_CHAINS.add(TRG_NAMED);
        WELL_KNOWN_CHAINS.add(TRD_NAMED);
        WELL_KNOWN_CHAINS.add(TCR_NAMED);

        WELL_KNOWN_CHAINS.add(TRAD_NAMED);

        WELL_KNOWN_CHAINS.add(IGH_NAMED);
        WELL_KNOWN_CHAINS.add(IGK_NAMED);
        WELL_KNOWN_CHAINS.add(IGL_NAMED);
        WELL_KNOWN_CHAINS.add(IG_NAMED);

        for (NamedChains wkc : WELL_KNOWN_CHAINS)
            WELL_KNOWN_CHAINS_MAP.put(wkc.name, wkc.chains);
    }

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
//        if (chains == null)
//            return null; //throw new RuntimeException("Serialization of ALL chains is not implemented.");
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

    public boolean contains(Chains other) {
        return intersection(other).equals(other);
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

    /**
     * Parse chains including TCR, TR, IG and ALL abbreviations, can parse coma-separated list.
     *
     * Example: "TR", "TCR,IGH"
     *
     * @param value string representation
     */
    public static Chains parse(String value) {
        String[] split = value.split(",");
        Chains chains = new Chains();
        for (String s : split)
            chains = chains.merge(parse0(s.trim()));
        return chains;
    }

    private static Chains parse0(String value) {
        switch (value.toLowerCase().trim()) {
            case "tcr":
            case "tr":
                return Chains.TCR;
            case "ig":
                return Chains.IG;
            case "all":
                return Chains.ALL;
        }
        return new Chains(value);
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
