package so.nian.backup.utils;

/**
 * 16进制值与String/Byte之间的转换
 *
 * @author shumingl
 */
public class HexUtil {

    private static final int HEXVAL[] = new int['g'];

    static {
        HEXVAL['0'] = 0;
        HEXVAL['1'] = 1;
        HEXVAL['2'] = 2;
        HEXVAL['3'] = 3;
        HEXVAL['4'] = 4;
        HEXVAL['5'] = 5;
        HEXVAL['6'] = 6;
        HEXVAL['7'] = 7;
        HEXVAL['8'] = 8;
        HEXVAL['9'] = 9;
        HEXVAL['a'] = 10;
        HEXVAL['b'] = 11;
        HEXVAL['c'] = 12;
        HEXVAL['d'] = 13;
        HEXVAL['e'] = 14;
        HEXVAL['f'] = 15;

    }

    final static char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 字符串转换成十六进制字符串
     *
     * @param string 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String string2HexString(String string) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = string.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            //sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexString Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexString2String(String hexString) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexString.toCharArray();
        byte[] bytes = new byte[hexString.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param bytes byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String bytes2HexString(byte[] bytes) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            //sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param string Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexString2Bytes(String string) {
        int m = 0, n = 0;
        int l = string.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + string.substring(i * 2, m) + string.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param string 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String stringToUnicode(String string)
            throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param hexString 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hexString) {
        int t = hexString.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hexString.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    public static byte[] hex2byte(String digest) {
        byte[] bs = new byte[digest.length() / 2];
        int j = 0;
        for (int i = 0; i < digest.length(); ) {
            int v = HEXVAL[digest.charAt(i++)] * 16
                    + HEXVAL[digest.charAt(i++)];
            bs[j] = (byte) (v & 0xff);
            j++;
        }
        return bs;
    }

    public static String byte2hex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            byte b = digest[i];
            char c1 = HEX[((b & 0xF0) >> 4)];
            char c2 = HEX[(b & 0x0F)];
            sb.append(c1).append(c2);
        }
        return sb.toString();
    }

}