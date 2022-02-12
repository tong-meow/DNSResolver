import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

public class DNSServer {

    public static void main(String[] args) throws SocketException {

        // open client socket
        DatagramSocket clientSkt = new DatagramSocket(8053);
        // open google socket
        DatagramSocket googleSkt = new DatagramSocket(53);
        // create the cache
        DNSCache cache = new DNSCache();
        // start listening
        System.out.println("DNS server is running. Start listening on Port: 8053...");

        while(true) {
            // create packet
            byte[] buffer = new byte[512];
            DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
            ArrayList<DNSRecord> responses = new ArrayList<>();
            // try to receive packets...
            try {
                clientSkt.receive(pkt);
                System.out.println("> [1] packet received.");
                // decode the packet data
                DNSMessage msg = DNSMessage.decodeMessage(buffer);
                System.out.println("> [2] packet data decoded successfully.");
                // get the requests
                DNSQuestion[] requests = msg.requests_;
                System.out.println("> [3] requests are received.");
                // iterate all the requests 1 by 1
                for (DNSQuestion request : requests) {
                    System.out.println("> [4] request: " +
                            DNSMessage.octetsToString(request.qName_));
                    // if cache table contains the results
                    if (cache.contains(request)) {
                        System.out.println("> [5] answer is found in cache.");
                        DNSRecord answer = cache.getRecord(request);
                        responses.add(answer);
                    }
                    // if cache doesn't have the result, ask google then
                    else {
                        System.out.println("> [5] answer is not found in cache." +
                                           "start requesting from Google.");
                        InetAddress google = InetAddress.getByName("8.8.8.8");
                        byte[] originalData = msg.originalData_;
                        DatagramPacket requestPkt = new DatagramPacket(
                                originalData, originalData.length, google, 53);
                        googleSkt.send(requestPkt);
                        System.out.println("> [6] request is sent to Google.");
                        byte[] ggResponse = new byte[512];
                        DatagramPacket ggPkt = new DatagramPacket(ggResponse,ggResponse.length);
                        googleSkt.receive(ggPkt);
                        System.out.println("> [7] get response from Google.");
                        DNSMessage ggMsg = DNSMessage.decodeMessage(ggResponse);
                        if (ggMsg.responses_.length != 0 && ggMsg.responses_[0] != null) {
                            // add the response from Google to local cache
                            cache.add(request, ggMsg.responses_[0]);
                            responses.add(ggMsg.responses_[0]);
                        }
                        // handle the error: if the user request for illegal domain name
                        else {
                            System.out.println("> [!] Error: user requests for illegal domain name.");
                        }
                    }
                }
                // send back responses
                DNSRecord[] responseArr = new DNSRecord[responses.size()];
                for (int i = 0; i < responses.size(); i++){
                    responseArr[i] = responses.get(i);
                }
                DNSMessage response = DNSMessage.buildResponse(msg, responseArr);
                byte[] responseBuffer = response.toBytes();
                DatagramPacket responsePkt = new DatagramPacket(responseBuffer,
                        responseBuffer.length, pkt.getAddress(), pkt.getPort());
                clientSkt.send(responsePkt);
                System.out.println("> [!] response is sent to client.");
//                System.out.println("> " + msg.toString());
//                System.out.println("> " + response.toString());
                System.out.println("> [!] request successfully replied!");
                System.out.println("------------------------------");
            } catch (IOException e) {
                System.out.println("[!] Error occurred: " + e.getMessage());
                break;
            }
        }
        // after listening, close the sockets
        clientSkt.close();
        googleSkt.close();

    }
}