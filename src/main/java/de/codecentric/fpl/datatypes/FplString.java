package de.codecentric.fpl.datatypes;

import java.nio.charset.StandardCharsets;

/**
 * An FPL String
 */
public class FplString implements EvaluatesToThisValue, Comparable<FplString> {
    private final byte[] value;
	private int hash;

	/**
	 * @param content Content of String
	 * @return FplString with given content or <code>null</code> when content is <code>null</code>
	 */
	public static FplString make(String content) {
		return content == null ? null : new FplString(content);
	}
	
    /**
     * @param content Content of the String, not null.
     */
    public FplString(String content) {
        if (content == null) {
            throw new NullPointerException("content null");
        }
        value = content.getBytes(StandardCharsets.UTF_16);
    }

    /**
     * @return Content, never null.
     */
    public String getContent() {
        return new String(value, StandardCharsets.UTF_16);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            hash = h = hashCode(value);
        }
        return h;
    }

    private int hashCode(byte[] value) {
        int h = 0;
        int length = value.length >> 1;
        for (int i = 0; i < length; i++) {
            h = 31 * h + getChar(i);
        }
        return h;
    }
    
    private char getChar(int index) {
        index <<= 1;
        return (char)(((value[index++] & 0xff) << 8) |
                      ((value[index]   & 0xff) << 0));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FplString other = (FplString) obj;
        int length = value.length;
        if (length != other.value.length) { 
        	return false;
        }
        for (int i = 0; i < length; i++) {
        	if (value[i] != other.value[i]) {
        		return false;
        	}
        }
        return true;
    }

	@Override
	public String typeName() {
		return "string";
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        String content = getContent();
        int length = content.length();
        for (int i = 0; i < length; i++) {
            char ch = content.charAt(i);
            if (ch == '"') {
                sb.append("\\\"");
            } else if (ch == '\t') {
                sb.append("\\t");
            } else if (ch == '\n') {
                sb.append("\\n");
            } else if (ch == '\r') {
                sb.append("\\r");
            } else {
                sb.append(ch);
            }
        }
        sb.append('"');
        return sb.toString();
    }
    
	@Override
	public int compareTo(FplString other) {
        return compareValues(other);
	}
	
    private int compareValues(FplString other) {
        int len1 = value.length >> 1;
        int len2 = other.value.length >> 1;
        int lim = Math.min(len1, len2);
        for (int k = 0; k < lim; k++) {
            char c1 = getChar(k);
            char c2 = other.getChar(k);
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }
}
