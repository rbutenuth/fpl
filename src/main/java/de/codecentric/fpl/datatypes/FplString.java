package de.codecentric.fpl.datatypes;

/**
 * An FPL String
 */
public class FplString extends EvaluatesToThisValue {
    private final String content;

    /**
     * @param content Content of the String, not null.
     */
    public FplString(String content) {
        if (content == null) {
            throw new NullPointerException("content null");
        }
        this.content = content;
    }

    /**
     * @return Content, never null.
     */
    public String getContent() {
        return content;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return content.hashCode();
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
        return content.equals(other.content);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < content.length(); i++) {
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
}
