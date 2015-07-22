package com.kongkim.hbaseorm.common;

/**
 * 每次append都会自动加上separator的StringBuilder
 * 
 * @author xieweiinfo
 */
public class StringBuilderWithSeparator {
    private final StringBuilder sb = new StringBuilder();
    private final String separator;

    public StringBuilderWithSeparator() {
        this.separator = CommonConstants.DEFAULT_SEPERATOR;
    }

    public StringBuilderWithSeparator(String separator) {
        this.separator = separator;
    }

    public StringBuilderWithSeparator append(Object str) {
        sb.append(str);
        sb.append(separator);
        return this;
    }

    @Override
    public String toString() {
        return sb.substring(0, sb.length() != 0 ? sb.length() - separator.length() : 0).toString();
    }

}
