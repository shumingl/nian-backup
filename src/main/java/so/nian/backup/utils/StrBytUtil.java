package so.nian.backup.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 常用工具类
 *
 * @author xiaoweizhu
 */
public class StrBytUtil {

    static boolean FLAG = false;

    static String REGEXPHONE =
            "^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0-9])|(16[0-9])|(14[0-9])|(12[0-9]))\\d{8}$";

    static String REGEXCARDNO = "^\\d{16,19}$";

    static String REGEXPHONE_CVN2 = "^\\d{3}$";

    static String REGEXPHONE_VALIDDATE = "^\\d{4}$";

    static String REGEXCERT =
            "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";

    static String REGEXDATE =
            "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-))$";

    static private final int BASELENGTH = 128;
    static private final int LOOKUPLENGTH = 16;
    static final private byte[] hexNumberTable = new byte[BASELENGTH];
    static final private char[] lookUpHexAlphabet = new char[LOOKUPLENGTH];

    static {
        for (int i = 0; i < BASELENGTH; i++) {
            hexNumberTable[i] = -1;
        }
        for (int i = '9'; i >= '0'; i--) {
            hexNumberTable[i] = (byte) (i - '0');
        }
        for (int i = 'F'; i >= 'A'; i--) {
            hexNumberTable[i] = (byte) (i - 'A' + 10);
        }
        for (int i = 'f'; i >= 'a'; i--) {
            hexNumberTable[i] = (byte) (i - 'a' + 10);
        }

        for (int i = 0; i < 10; i++) {
            lookUpHexAlphabet[i] = (char) ('0' + i);
        }
        for (int i = 10; i <= 15; i++) {
            lookUpHexAlphabet[i] = (char) ('A' + i - 10);
        }
    }

    /**
     * Encode a byte array to hex string
     *
     * @param binaryData array of byte to encode
     * @return return encoded string
     */
    static public String HexEncode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        int lengthEncode = lengthData * 2;
        char[] encodedData = new char[lengthEncode];
        int temp;
        for (int i = 0; i < lengthData; i++) {
            temp = binaryData[i];
            if (temp < 0) {
                temp += 256;
            }
            encodedData[i * 2] = lookUpHexAlphabet[temp >> 4];
            encodedData[i * 2 + 1] = lookUpHexAlphabet[temp & 0xf];
        }
        return new String(encodedData);
    }

    /**
     * Decode hex string to a byte array
     *
     * @param encoded encoded string
     * @return return array of byte to encode
     */
    static public byte[] HexDecode(String encoded) {
        if (encoded == null) {
            return null;
        }
        int lengthData = encoded.length();
        if (lengthData % 2 != 0) {
            return null;
        }

        char[] binaryData = encoded.toCharArray();
        int lengthDecode = lengthData / 2;
        byte[] decodedData = new byte[lengthDecode];
        byte temp1, temp2;
        char tempChar;
        for (int i = 0; i < lengthDecode; i++) {
            tempChar = binaryData[i * 2];
            temp1 = (tempChar < BASELENGTH) ? hexNumberTable[tempChar] : -1;
            if (temp1 == -1) {
                return null;
            }
            tempChar = binaryData[i * 2 + 1];
            temp2 = (tempChar < BASELENGTH) ? hexNumberTable[tempChar] : -1;
            if (temp2 == -1) {
                return null;
            }
            decodedData[i] = (byte) ((temp1 << 4) | temp2);
        }
        return decodedData;
    }


    /**
     * 获取固定字节长度的字符串，不足右补空格.
     *
     * @param str     字符串
     * @param byteLen 长度
     * @return String字符串
     */
    public static String getFixByteLenStr(String str, int byteLen) {
        byte space = 32;
        byte[] data = new byte[byteLen];
        Arrays.fill(data, space);
        // 如果是空的，直接返回byteLen位数的空格
        if (str == null || str.equals("")) {
            return new String(data);
        }
        byte[] strByte = null;
        try {
            strByte = str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        if (strByte.length < byteLen) {
            byteLen = strByte.length;
        }
        System.arraycopy(strByte, 0, data, 0, byteLen);
        return new String(data);
    }


    /**
     * string 转换成十六进制 string 不带空格
     *
     * @param str 待转换的ASCII字符串
     */
    public static String str2HexStrNoSpace(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * byte [] 转换成十六进制 string
     *
     * @param b byte数组
     * @return String
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            // sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * String 字符串转换为Byte[] 值
     *
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * byte [] 转字符16进制的string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * String 16进制 转 byte []
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


}
