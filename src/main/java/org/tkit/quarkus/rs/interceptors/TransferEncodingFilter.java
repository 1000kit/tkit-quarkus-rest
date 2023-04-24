package org.tkit.quarkus.rs.interceptors;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

public class TransferEncodingFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) {
        clientResponseContext.getHeaders().remove("Transfer-Encoding");
    }
}
