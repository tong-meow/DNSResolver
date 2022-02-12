import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DNSMessage {

    public byte[] originalData_;
    public DNSHeader header_;
    public DNSQuestion[] requests_;
    public DNSRecord[] responses_;
    public DNSRecord[] nsRecords_;
    public DNSRecord[] addRecords_;

    public static DNSMessage decodeMessage(byte[] bytesArr) throws IOException {
        DNSMessage message = new DNSMessage();

        // originalData_ = byte array
        message.originalData_ = bytesArr;
        ByteArrayInputStream input = new ByteArrayInputStream(bytesArr);

        // get the header
        message.header_ = DNSHeader.decodeHeader(input);
        // get the requests
        message.requests_ = new DNSQuestion[message.header_.qdCount_];
        for (int i = 0; i < message.requests_.length; i++) {
            message.requests_[i] = DNSQuestion.decodeQuestion(input, message);
        }
        // get the responses
        message.responses_ = new DNSRecord[message.header_.anCount_];
        for (int i = 0; i < message.responses_.length; i++) {
            message.responses_[i] = DNSRecord.decodeRecord(input, message);
        }
        // get the nsRecords
        message.nsRecords_ = new DNSRecord[message.header_.nsCount_];
        for (int i = 0; i < message.nsRecords_.length; i++) {
            message.nsRecords_[i] = DNSRecord.decodeRecord(input, message);
        }
        // get the additional records
        message.addRecords_ = new DNSRecord[message.header_.arCount_];
        for (int i = 0; i < message.addRecords_.length; i++) {
            message.addRecords_[i] = DNSRecord.decodeRecord(input, message);
        }

        return message;
    }

    // read the pieces of a domain name starting from the current position of the input stream
    public String[] readDomainName(ByteArrayInputStream is){
        ArrayList<String> domainName = new ArrayList<>();
        while (true) {
            byte length = (byte) is.read();
            if (length == 0) {
                break;
            }
            else if (length > 0){
                StringBuilder data = new StringBuilder();
                for (int i = 0; i < length; i++){
                    data.append((char) is.read());
                }
                domainName.add(data.toString());
            }
            else{
                length = (byte) ((length << 8) | is.read());
                return readDomainName(length);
            }
        }
        String[] domainNamesArr = new String[domainName.size()];
        for (int i = 0; i < domainName.size(); i++) {
            domainNamesArr[i] = domainName.get(i);
        }
        return domainNamesArr;
    }

    // same, but used when there's compression and we need to find the domain from earlier in
    // the message. This method should make a ByteArrayInputStream that starts at the
    // specified byte and call the other version of this method.
    public String[] readDomainName(int firstByte){
        ByteArrayInputStream is = new ByteArrayInputStream(originalData_);
        is.skip(firstByte);
        return readDomainName(is);
    }

    public static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers){
        DNSMessage response = new DNSMessage();
        response.requests_ = request.requests_;
        response.responses_ = answers;
        response.nsRecords_ = request.nsRecords_;
        response.addRecords_ = request.addRecords_;
        response.header_ = DNSHeader.buildResponseHeader(request, response);
        return response;
    }

    public byte[] toBytes() throws IOException{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Map<String, Integer> domainLct = new HashMap<>();
        header_.writeBytes(os);
        // write question
        for (DNSQuestion req: requests_) {
            req.writeBytes(os, domainLct);
        }
        // write record
        for (DNSRecord rep: responses_) {
            rep.writeBytes(os, domainLct);
        }
        // write nsRecord
        for (DNSRecord ns: nsRecords_) {
            ns.writeBytes(os, domainLct);
        }
        // write additional record
        for (DNSRecord add: addRecords_) {
            add.writeBytes(os, domainLct);
        }
        return os.toByteArray();
    }

    public static void writeDomainName(ByteArrayOutputStream os, Map<String,Integer> domainLct,
                                       String[] domainPieces){
        // convert the domain name pieces to a string with dot
        String domainName = octetsToString(domainPieces);
        // check if our map has this domain name as a key
        if (domainLct.containsKey(domainName)){
            int value = domainLct.get(domainName);
            byte leftByte = (byte) ((value >> 8) | 0xc0);
            byte rightByte = (byte) value;
            os.write(leftByte);
            os.write(rightByte);
        }
        else {
            domainLct.put(domainName, os.size());
            for (String domainPiece : domainPieces) {
                os.write(domainPiece.length());
                char[] piece = domainPiece.toCharArray();
                for (char c : piece) {
                    os.write(c);
                }
            }
            os.write(0);
        }
    }

    public static String octetsToString(String[] octets){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < octets.length; i++) {
            sb.append(octets[i]);
            if (i < octets.length - 1){
                sb.append(".");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString(){
        String result = "DNS Message: header: " + header_
                + " , requests: " + Arrays.toString(requests_)
                + " , nsRecords: " + Arrays.toString(nsRecords_)
                + " , adRecords: " + Arrays.toString(addRecords_);
        return result;
    }
}
