/*
 * Copyright (c) 2014-2015, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact the Inventors using one of the following
 * email addresses: chudakovdm@mail.ru, chudakovdm@gmail.com
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package io.repseq.reference;


/**
 * Enum for main recombination segment groups. TRBV, TRBJ, etc...
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @author Shugay Mikhail (mikhail.shugay@gmail.com)
 */
public enum GeneGroup implements java.io.Serializable {
    //TRA
    TRAC("TRAC", GeneType.Constant, Chain.TRA), TRAJ("TRAJ", GeneType.Joining, Chain.TRA),
    TRAV("TRAV", GeneType.Variable, Chain.TRA),

    //TRB
    TRBC("TRBC", GeneType.Constant, Chain.TRB), TRBD("TRBD", GeneType.Diversity, Chain.TRB),
    TRBJ("TRBJ", GeneType.Joining, Chain.TRB), TRBV("TRBV", GeneType.Variable, Chain.TRB),

    //TRG
    TRGC("TRGC", GeneType.Constant, Chain.TRG), TRGJ("TRGJ", GeneType.Joining, Chain.TRG),
    TRGV("TRGV", GeneType.Variable, Chain.TRG),

    //TRD
    TRDC("TRDC", GeneType.Constant, Chain.TRD), TRDD("TRDD", GeneType.Diversity, Chain.TRD),
    TRDJ("TRDJ", GeneType.Joining, Chain.TRD), TRDV("TRDV", GeneType.Variable, Chain.TRD),

    //IGL
    IGLC("IGLC", GeneType.Constant, Chain.IGL), IGLJ("IGLJ", GeneType.Joining, Chain.IGL),
    IGLV("IGLV", GeneType.Variable, Chain.IGL),

    //IGK
    IGKC("IGKC", GeneType.Constant, Chain.IGK), IGKJ("IGKJ", GeneType.Joining, Chain.IGK),
    IGKV("IGKV", GeneType.Variable, Chain.IGK),

    //IGH
    IGHC("IGHC", GeneType.Constant, Chain.IGH), IGHD("IGHD", GeneType.Diversity, Chain.IGH),
    IGHJ("IGHJ", GeneType.Joining, Chain.IGH), IGHV("IGHV", GeneType.Variable, Chain.IGH);

    final String id;
    final GeneType type;
    final Chain chain;

    private GeneGroup(String id, GeneType type, Chain chain) {
        this.id = id;
        this.type = type;
        this.chain = chain;
    }

    public GeneType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Chain getChain() {
        return chain;
    }

    public static GeneGroup get(Chain g, GeneType type) {
        for (GeneGroup sg : values())
            if (sg.chain == g && sg.type == type)
                return sg;
        return null;
    }

    public static GeneGroup getFromName(String name) {
        name = name.toUpperCase();
        for (GeneGroup group : values())
            if (group.id.equals(name))
                return group;
        return null;
    }
}
