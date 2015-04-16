package cn.edu.nju.ws.expedia.util;

import java.security.MessageDigest;

/**
 * 生成md5 hash值
 * @author Saisai Gong
 *
 */
public class MD5 {
    private MessageDigest md = null;
    private void close() {
        this.md = null;
    }
    public MD5() {
        try {
            this.md = MessageDigest.getInstance("MD5");
        } catch (Throwable e) {
			/* Impossible */
        }
    }
    public void addString(String str) {
        try {
            this.md.update(str.getBytes("UTF-8"));
        } catch (Throwable e) {
			/* Impossible */
        }
    }
    public byte[] getResult() {
        return this.md.digest();
    }
    public static byte[]  makeMD5(String str){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));
        } catch (Throwable e) {
			/* Impossible */
        }
        byte[] result = md.digest();
        md=null;
        return result;
    }

    public static String makeMD5Str(String str){
        return asHex(makeMD5(str)) ;
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex
     * number.
     *
     * @param hash
     *            Array of bytes to convert to hex-string
     * @return Generated hex string
     * @author Timothy W Macinta
     */
    public static String asHex(byte hash[]) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            buf[x++] = MD5.HEX_CHARS[(hash[i] >> 4) & 0xf];
            buf[x++] = MD5.HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    private static byte[] asBytes(String hashStr) {
        byte[] hash = new byte[hashStr.length() / 2];
        for (int i = 0; i < hashStr.length(); i += 2) {
            hash[i / 2] = (byte) (((byte) (MD5.asByte(hashStr.charAt(i)) << 4)) | (MD5
                    .asByte(hashStr.charAt(i + 1))));
        }
        return hash;
    }

    private static byte asByte(char hexChar) {
        byte result = 0x00;
        switch (hexChar) {
            case '0':
                result = 0x00;
                break;
            case '1':
                result = 0x01;
                break;
            case '2':
                result = 0x02;
                break;
            case '3':
                result = 0x03;
                break;
            case '4':
                result = 0x04;
                break;
            case '5':
                result = 0x05;
                break;
            case '6':
                result = 0x06;
                break;
            case '7':
                result = 0x07;
                break;
            case '8':
                result = 0x08;
                break;
            case '9':
                result = 0x09;
                break;
            case 'a':
                result = 0x0A;
                break;
            case 'b':
                result = 0x0B;
                break;
            case 'c':
                result = 0x0C;
                break;
            case 'd':
                result = 0x0D;
                break;
            case 'e':
                result = 0x0E;
                break;
            case 'f':
                result = 0x0F;
                break;
        }
        return result;
    }
}
