package fri.servers.hiking.emergencyalert.util;

import java.nio.charset.Charset;

public final class Platform
{
    public static final String ENCODING;
    public static final Charset CHARSET;
    public static final String NEWLINE;
    
    static {
        final String platformEncoding = System.getProperty("file.encoding");
        ENCODING = StringUtil.isNotEmpty(platformEncoding)
                ? platformEncoding
                : "UTF-8";
        CHARSET = Charset.forName(ENCODING);
        NEWLINE = System.getProperty("line.separator");
    }
    
    private Platform() {} // do not instantiate
}