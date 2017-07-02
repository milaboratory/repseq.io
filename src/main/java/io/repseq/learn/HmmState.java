package io.repseq.learn;

/**
 * Created by mikesh on 7/2/17.
 */
public class HmmState implements Comparable<HmmState> {
    private final HmmStateFamily stateFamily;
    private final int index;

    public HmmState(HmmStateFamily stateFamily, int index) {
        this.stateFamily = stateFamily;
        this.index = index;
    }

    public HmmStateFamily getStateFamily() {
        return stateFamily;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(HmmState o) {
        int res = Integer.compare(stateFamily.order,
                o.stateFamily.order);
        return res == 0 ? Integer.compare(index, o.index) : res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HmmState hmmState = (HmmState) o;

        if (index != hmmState.index) return false;
        return stateFamily == hmmState.stateFamily;
    }

    @Override
    public int hashCode() {
        int result = stateFamily.hashCode();
        result = 31 * result + index;
        return result;
    }
}
