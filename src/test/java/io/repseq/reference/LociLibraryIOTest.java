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

import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static io.repseq.reference.GeneFeature.GermlineJCDR3Part;
import static org.junit.Assert.*;

public class LociLibraryIOTest {
    @Test
    public void test1() throws Exception {
        test(false);
    }

    @Test
    public void test2() throws Exception {
        test(true);
    }

    public void test(boolean compressed) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UUID uuid = UUID.randomUUID();
        LociLibraryWriter writer = new LociLibraryWriter(bos);
        writer.writeMagic();
        writer.writeMetaInfo("G", "B");
        writer.writeCommonSpeciesName(Species.HomoSapiens, "hsa");
        writer.writeSequencePart("A1", 0, new NucleotideSequence("ATTAGACAATTAGACA"), compressed);
        writer.writeBeginOfLocus(Species.HomoSapiens, Locus.TRB, uuid);
        writer.writeAllele(GeneType.Joining, "TRBJ2-4*01", true, true, "A1", new int[]{1, 5, 8}, null, null, null);
        writer.writeAllele(GeneType.Joining, "TRBJ2-4*02", false, true, null, null, "TRBJ2-4*01",
                Mutations.decodeNuc("ST1G").getRAWMutations(), GermlineJCDR3Part);
        writer.writeMetaInfo("C", "D");
        writer.writeEndOfLocus();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        //Testing library
        LociLibrary library = LociLibraryReader.read(bis, false);
        assertEquals("B", library.getProperty("G"));

        //Testing container
        LocusContainer container = library.getLocus("hsa", Locus.TRB);
        assertNotNull(container);
        assertEquals("D", container.getProperty("C"));
        assertEquals(uuid, container.getUUID());

        Gene gene = container.getGenes(GeneType.Joining).get(0);
        assertNotNull(gene);
        gene = container.getGene("TRBJ2-4");
        assertNotNull(gene);
        assertEquals(GeneGroup.TRBJ, gene.getGroup());
        assertEquals(0, gene.getIndex());

        Allele allele = gene.getAlleles().get(0);
        assertNotNull(allele);
        allele = container.getAllele(GeneType.Joining, 0);
        assertNotNull(allele);
        allele = container.getAllele("TRBJ2-4*01");
        assertNotNull(allele);
        assertTrue(allele.isFunctional());
        assertTrue(allele.isReference());

        assertEquals(new NucleotideSequence("TTAG"), allele.getFeature(GermlineJCDR3Part));

        allele = container.getAllele("TRBJ2-4*02");
        assertNotNull(allele);
        assertTrue(allele.isFunctional());
        assertFalse(allele.isReference());
        assertEquals(new NucleotideSequence("TGAG"), allele.getFeature(GermlineJCDR3Part));

        //TODO: uncomment after fix for FR4
        //assertEquals(new NucleotideSequence("ACA"), allele.getFeature(GeneFeature.FR4));
        //assertEquals(new NucleotideSequence("TTAGACA"), allele.getFeature(GeneFeature.JRegion));
    }
}
