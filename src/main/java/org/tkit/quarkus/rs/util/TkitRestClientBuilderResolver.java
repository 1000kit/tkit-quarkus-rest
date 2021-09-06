package org.tkit.quarkus.rs.util;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;

/**
 * @author msomora
 */
public class TkitRestClientBuilderResolver extends RestClientBuilderResolver {

    @Override
    public RestClientBuilder newBuilder() {
        return new TkitRestClientBuilder();
    }

}
