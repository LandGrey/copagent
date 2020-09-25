package code.landgrey.copagent.utils;


public abstract class StringUtils {
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    public static boolean hasLength(String str) {
        return hasLength((CharSequence)str);
    }

    public static String replace(String inString, String oldPattern, String newPattern) {
        if(hasLength(inString) && hasLength(oldPattern) && newPattern != null) {
            StringBuilder sb = new StringBuilder();
            int pos = 0;
            int index = inString.indexOf(oldPattern);

            for(int patLen = oldPattern.length(); index >= 0; index = inString.indexOf(oldPattern, pos)) {
                sb.append(inString.substring(pos, index));
                sb.append(newPattern);
                pos = index + patLen;
            }

            sb.append(inString.substring(pos));
            return sb.toString();
        } else {
            return inString;
        }
    }
}
