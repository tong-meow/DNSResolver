import javax.print.attribute.standard.JobKOctets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/*
                                1  1  1  1  1  1
  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                                               |
/                     QNAME                     /
/                                               /
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                     QTYPE                     |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
|                     QCLASS                    |
+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 */

public class DNSQuestion {

    public String[] qName_;
    public int qType_;
    public int qClass_;

    // read a question from the input stream.
    // due to compression, may have to ask the DNSMessage containing this
    // question to read some fields.
    public static DNSQuestion decodeQuestion(ByteArrayInputStream is, DNSMessage msg){
        DNSQuestion question = new DNSQuestion();
        // get name
        question.qName_ = msg.readDomainName(is);
        // get type --> 2 bytes
        question.qType_ = DNSHelper.getNBytes(is, 2);
        // get class --> 2 bytes
        question.qClass_ = DNSHelper.getNBytes(is, 2);
        return question;
    }

    // write the question bytes which will be sent to the client.
    // the hash map is used for us to compress the message.
    public void writeBytes(ByteArrayOutputStream byteOS, Map<String,Integer> domainNameLocations) throws IOException {
        // write name
        DNSMessage.writeDomainName(byteOS, domainNameLocations, qName_);
        // write type
        byteOS.write(DNSHelper.intToBytes(qType_));
        // write class
        byteOS.write(DNSHelper.intToBytes(qClass_));
    }

    // Let your IDE generate these.
    // They're needed to use a question as a HashMap key,
    // and to get a human readable string.
    @Override
    public String toString(){
        String question = "DNS Question: qName: " + Arrays.toString(qName_)
                + " , qType: " + qType_
                + " , qClass: " + qClass_;
        return question;
    }

    // reference: used '31' as seed
    public int hashCode(){
        return 31 * (Objects.hash(qType_, qClass_)) + Arrays.hashCode(qName_);
    }

    @Override
    public boolean equals(Object rhs){
        if (rhs == null || this.getClass() != rhs.getClass())
            return false;
        DNSQuestion rhsQuestion = (DNSQuestion) rhs;
        return (Arrays.equals(qName_, rhsQuestion.qName_)) &&
                (qType_ == rhsQuestion.qType_) && (qClass_ == rhsQuestion.qClass_);
    }
}
