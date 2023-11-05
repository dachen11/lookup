package com.dachen11.lookup.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by e06836 on 6/6/15.
 */

public class BaseList
{
    private int startRow;
    private int endRow;
    private long totalRow;
    private List<? extends BibleText> entities;

    //add Jan 1, 2020 to store extra info: for buildIndex
    private Map<String, Object> extra = new HashMap<>();

    public BaseList()
    {
        super();
    }

    
    public BaseList(
             int startRow
    )
    {
        super();
        setStartRow(startRow);
    }

    
    public int getStartRow()
    {
        return startRow;
    }

    public void setStartRow(int startRow)
    {
        this.startRow = startRow;
    }

    
    public int getEndRow()
    {
        return endRow;
    }

    public void setEndRow(int endRow)
    {
        this.endRow = endRow;
    }

    
    public long getTotalRow()
    {
        return totalRow;
    }

    public void setTotalRow(long totalRow)
    {
        this.totalRow = totalRow;
    }

    
    public List<? extends BibleText> getEntities()
    {
        return entities;
    }

    public void setEntities(List<? extends BibleText> entities)
    {
        this.entities = entities;
    }

    
    public Map<String, Object> getExtra()
    {
        return extra;
    }

    public void setExtra(Map<String, Object> extra)
    {
        this.extra = extra;
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("start", startRow)
                .add("total", totalRow)

                .toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(startRow, totalRow);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseList other = (BaseList) obj;
        return Objects.equals(this.startRow, other.startRow)
                && Objects.equals(this.totalRow, other.totalRow);

    }

    public void incTotalRow() {
        totalRow ++;
    }

    public void incEndRow() {
        endRow++;
    }
}
