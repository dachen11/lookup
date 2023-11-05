package com.dachen11.lookup.model;

public class LabelValue {
    private String value;
    private String label;


    public LabelValue()
    {
        super();
    }

    public LabelValue(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}
