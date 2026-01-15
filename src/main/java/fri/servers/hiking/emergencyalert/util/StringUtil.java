package fri.servers.hiking.emergencyalert.util;

public final class StringUtil
{
    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() <= 0;
    }
    
    public static boolean isNotEmpty(String s) {
        return isEmpty(s) == false;
    }
    
    private StringUtil() {} // do not instantiate
}