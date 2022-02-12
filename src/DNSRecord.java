// Everything after the header and question parts of the DNS message are stored as records.
// This should have all the fields listed in the spec
// as well as a Date object storing when this record was created by your program.

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/*
                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                                               |
    /                                               /
    /                      NAME                     /
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TYPE                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                     CLASS                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TTL                      |
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                   RDLENGTH                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
    /                     RDATA                     /
    /                                               /
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 */
public class DNSRecord {

    public String[] recordName_;
    public int recordType_;
    public int recordClass_;
    public int recordTTL_;
    public Calendar recordLastsTime_;
    public int recordLength_;
    public byte[] recordData_;


    public static DNSRecord decodeRecord(ByteArrayInputStream is, DNSMessage msg){
        DNSRecord record = new DNSRecord();

        record.recordName_ = msg.readDomainName(is);
        record.recordType_ = DNSHelper.getNBytes(is, 2);
        record.recordClass_ = DNSHelper.getNBytes(is, 2);
        record.recordTTL_ = DNSHelper.getNBytes(is, 4);
        record.recordLength_ = DNSHelper.getNBytes(is, 2);

        // record data has N = recordLength_ bytes of data
        record.recordData_ = new byte[record.recordLength_];
        // read all of them and store in the array
        for (int i = 0; i < record.recordLength_; i++) {
            record.recordData_[i] = (byte) is.read();
        }

        // use the TTL, record the lasts time for this record
        // if time > lasts time, timestampValid == false
        record.recordLastsTime_ = Calendar.getInstance();
        record.recordLastsTime_.add(Calendar.SECOND, record.recordTTL_);

        return record;
    }

    public void writeBytes(ByteArrayOutputStream byteOS, Map<String, Integer> dnMap) throws IOException {
        // write name
        DNSMessage.writeDomainName(byteOS, dnMap, recordName_);
        // write type
        byteOS.write(DNSHelper.intToBytes(recordType_));
        // write class
        byteOS.write(DNSHelper.intToBytes(recordClass_));
        // write ttl
        byte[] ttlArr = new byte[4];
        for (int i = 0; i < 4; i++){
            ttlArr[i] = (byte) (recordTTL_ >> (8 * (3 - i)));
        }
        for (byte ttlByte : ttlArr) {
            byteOS.write(ttlByte);
        }
        // write length
        byteOS.write(DNSHelper.intToBytes(recordLength_));
        // write data
        for (byte data: recordData_) {
            byteOS.write(data);
        }
    }

    @Override
    public String toString(){
        String record = "DNS Record: name: " + Arrays.toString(recordName_)
                + " , type: " + recordType_
                + " , class: " + recordClass_
                + " , ttl: " + recordTTL_
                + " , rdlength: " + recordLength_
                + " , rdata: " + Arrays.toString(recordData_);
        return record;
    }

    // return whether the creation date + the time to live is after the current time.
    // The Date and Calendar classes will be useful for this.
    public boolean timestampValid(){
        Calendar newTime = Calendar.getInstance();
        return newTime.before(recordLastsTime_);
    }
}
