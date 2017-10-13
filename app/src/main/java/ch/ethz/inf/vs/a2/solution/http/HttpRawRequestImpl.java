package ch.ethz.inf.vs.a2.solution.http;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;

/**
 * Created by pieroguicciardi on 13.10.17.
 */

public class HttpRawRequestImpl implements HttpRawRequest{

    @Override
    public String generateRequest(String host, int port, String path) {
        return "GET "+path+" HTTP/1.1\r\n" +
                "Host: "+host+":"+port+"\r\n" +
                "Connection: close\r\n" +
                "Accept: text/html\r\n\r\n";
    }
}
