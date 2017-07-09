package io.repseq.learn;

/**
 * Created by mikesh on 7/9/17.
 */
public class SegmentTuple {
    private final String vId, dId, jId;

    public SegmentTuple(String vId, String jId, String dId) {
        this.vId = vId;
        this.jId = jId;
        this.dId = dId;
    }

    public SegmentTuple(String vId, String jId) {
        this(vId, jId, null);
    }

    public String getvId() {
        return vId;
    }

    public String getdId() {
        return dId;
    }

    public String getjId() {
        return jId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SegmentTuple that = (SegmentTuple) o;

        if (vId != null ? !vId.equals(that.vId) : that.vId != null) return false;
        if (dId != null ? !dId.equals(that.dId) : that.dId != null) return false;
        return jId != null ? jId.equals(that.jId) : that.jId == null;
    }

    @Override
    public int hashCode() {
        int result = vId != null ? vId.hashCode() : 0;
        result = 31 * result + (dId != null ? dId.hashCode() : 0);
        result = 31 * result + (jId != null ? jId.hashCode() : 0);
        return result;
    }
}
