import java.io.ByteArrayInputStream;

// this class is used for helper static functions: byte read, byte write
public class DNSHelper {

    public static int getNBytes(ByteArrayInputStream is, int n){
        int result = 0;
        for (int i = n - 1; i >= 0; i--) {
            int current = is.read();
            current = (current & 0xff) << (8 * i);
            result = result | current;
        }
        return result;
    }

    public static byte[] intToBytes(int n){
        byte[] result = new byte[2];
        result[0] = (byte) ((n >> 8) & 0xff);
        result[1] = (byte) (n & 0xff);
        return result;
    }
}
