package com.oa.statistics.entity;

public class StatResultVO {
    private String name;
    private Object value;
    private String label;

    public StatResultVO() {}
    public StatResultVO(String name, Object value) { this.name = name; this.value = value; }
    public StatResultVO(String name, Object value, String label) { this.name = name; this.value = value; this.label = label; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}