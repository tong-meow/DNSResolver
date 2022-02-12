import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// header class:
//  - decode the header;
//  - write response's header

/*
The header contains the following fields:
                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 */


public class DNSHeader {
    public int id_;
    public int qr_;
    public int opcode_;
    public int aa_;
    public int tc_;
    public int rd_;
    public int ra_;
    public int z_;
    public int rCode_;
    public int qdCount_;
    public int anCount_;
    public int nsCount_;
    public int arCount_;


    // read the header from an input stream (ByteArrayInputStream)
    public static DNSHeader decodeHeader(ByteArrayInputStream is) {

        DNSHeader header = new DNSHeader();

        // get id_  --> 16 bits (2 bytes)
        header.id_ = DNSHelper.getNBytes(is, 2);

        int opByte = DNSHelper.getNBytes(is, 1);
        // get qr_  --> 1 bit
        header.qr_ = (opByte & 0xff ) >> 7;
        // get opcode_  --> 4 bits
        header.opcode_ = (opByte >> 3) & 0xf;
        // get aa_  --> 1 bit
        header.aa_ = ((opByte << 5) & 0xff ) >> 7;
        // get tc_  --> 1 bit
        header.tc_ = ((opByte << 6) & 0xff ) >> 7;
        // get rd_  --> 1 bit
        header.rd_ = ((opByte << 7) & 0xff ) >> 7;

        int zByte = DNSHelper.getNBytes(is, 1);
        // get ra_  --> 1 bit
        header.ra_ = (zByte & 0xff ) >> 7;
        // get z_  --> 3 bits
        header.z_ = (((zByte << 3) & 0xff ) >> 7) & 0xf;
        // get rCode_  --> 4 bits
        header.rCode_ = (header.rCode_ | zByte) & 0xf;

        // get qdCount_  --> 16 bits
        header.qdCount_ = DNSHelper.getNBytes(is, 2);

        // get anCount_  --> 16 bits
        header.anCount_ = DNSHelper.getNBytes(is, 2);

        // get nsCount_  --> 16 bits
        header.nsCount_ = DNSHelper.getNBytes(is, 2);

        // get arCount_  --> 16 bits
        header.arCount_ = DNSHelper.getNBytes(is, 2);

        return header;
    }

    // create the header for the response.
    public static DNSHeader buildResponseHeader(DNSMessage request, DNSMessage response){

        DNSHeader header = new DNSHeader(); // response header
        header.id_ = request.header_.id_;
        header.qr_ = 1;
        header.opcode_ = 0;
        header.aa_ = 0;
        header.tc_ = 0;
        header.rd_ = 1;
        header.ra_ = 1;
        header.z_ = 0;
        header.rCode_ = 0;
        header.qdCount_ = response.requests_.length;
        header.anCount_ = response.responses_.length;
        header.nsCount_ = response.nsRecords_.length;
        header.arCount_ = response.addRecords_.length;

        return header;
    }

    // encode the header to bytes to be sent back to the client.
    // The OutputStream interface has methods to write a single byte
    // or an array of bytes.
    public void writeBytes(ByteArrayOutputStream os) throws IOException {

        os.write(DNSHelper.intToBytes(id_));

        byte[] opArr = new byte[2];
        // opArr = (qr + opCode + aa + tc + rd) + (ra + z + rcode)
        //   | qr | opCode | aa | tc | rd |
        // = |  1 |  000   | 0  | 0  | 1  |
        // = 1000001 (binary) = 129 (decimal)
        opArr[0] = (byte) 129;
        //   | ra |    z   |    rcode     |
        // = | 1  |   000  |     0000     |
        // = 1000000 (binary) = 128 (decimal)
        opArr[1] = (byte) 128;
        os.write(opArr);

        os.write(DNSHelper.intToBytes(qdCount_));
        os.write(DNSHelper.intToBytes(anCount_));
        os.write(DNSHelper.intToBytes(nsCount_));
        os.write(DNSHelper.intToBytes(arCount_));

    }

    // Return a human readable string version of a header object.
    @Override
    public String toString(){
        String result = "DNS Header: id: " + id_ +
                ", qr: " + qr_ + ", opCode: " + opcode_ +
                ", aa: " + aa_ + ", tc: " + tc_ +
                ", rd: " + rd_ + ", ra: " + ra_ +
                ", z: " + z_ + ", rCode: " + rCode_ +
                ", qdCount: " + qdCount_ + ", anCount: " + anCount_ +
                ", {nsCount: " + nsCount_ + ", arCount: " + arCount_;
        return result;
    }
}
