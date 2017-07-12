package io.repseq.learn;

/**
 * Created by mikesh on 7/12/17.
 */
public class DTrimming {
    public static final DTrimming NOT_FOUND = new DTrimming(-1, -1);

    private final int pos5, pos3;

    public DTrimming(int pos5, int pos3) {
        this.pos5 = pos5;
        this.pos3 = pos3;
    }

    public int getPos5() {
        return pos5;
    }

    public int getPos3() {
        return pos3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DTrimming dTrimming = (DTrimming) o;

        if (pos5 != dTrimming.pos5) return false;
        return pos3 == dTrimming.pos3;
    }

    @Override
    public int hashCode() {
        int result = pos5;
        result = 31 * result + pos3;
        return result;
    }
}
