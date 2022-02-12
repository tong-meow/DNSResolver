import java.util.HashMap;
import java.util.Map;

// cache class:
//  - store the Map<DNSQuestion, DNSRecord>;
//  - used to check if a record is in the local cache;
//  - used to add new record into the cache

public class DNSCache {
    private static Map<DNSQuestion, DNSRecord> cache_;

    public DNSCache() {
        cache_ = new HashMap<>();
    }

    public boolean contains (DNSQuestion request){
        if (cache_.containsKey(request)){
            // When you look up an entry, if it is too old
            // (its TTL has expired),
            // remove it and return "not found."
            if (cache_.get(request).timestampValid()){
                return true;
            } else {
                cache_.remove(request);
                return false;
            }
        }
        return false;
    }

    public DNSRecord getRecord(DNSQuestion request){
        return cache_.get(request);
    }

    public void add(DNSQuestion request, DNSRecord record){
        cache_.put(request, record);
    }
}
