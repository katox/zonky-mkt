package zonky_mkt.model;

import java.time.OffsetDateTime;

public class PageRequest {

    private OffsetDateTime sinceTs;
    private Integer pageNo;

    public PageRequest(OffsetDateTime sinceTs, Integer pageNo) {
        this.sinceTs = sinceTs;
        this.pageNo = pageNo;
    }

    public OffsetDateTime getSinceTs() {
        return sinceTs;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageRequest that = (PageRequest) o;

        if (sinceTs != null ? !sinceTs.equals(that.sinceTs) : that.sinceTs != null) return false;
        return pageNo != null ? pageNo.equals(that.pageNo) : that.pageNo == null;
    }

    @Override
    public int hashCode() {
        int result = sinceTs != null ? sinceTs.hashCode() : 0;
        result = 31 * result + (pageNo != null ? pageNo.hashCode() : 0);
        return result;
    }
}
