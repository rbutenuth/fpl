package de.codecentric.fpl.datatypes;

/**
 * An FPL String
 */
public class FplString implements EvaluatesToThisValue, Comparable<FplString> {
    private final char value[];
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
        value = content.toCharArray();
    }

    /**
     * @return Content, never null.
     */
    public String getContent() {
        return new String(value);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            char val[] = value;

            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FplString) {
            FplString anotherString = (FplString)obj;
            int n = value.length;
            if (n == anotherString.value.length) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
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
        int len1 = value.length;
        int len2 = other.value.length;
        int lim = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = other.value;

        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
	}
}
