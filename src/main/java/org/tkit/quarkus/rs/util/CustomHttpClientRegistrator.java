package org.tkit.quarkus.rs.util;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

/**
 * @author msomora
 */

@ApplicationScoped
@Slf4j
public class CustomHttpClientRegistrator {

    void onStart(@Observes StartupEvent ev) {
        log.info("Registering TkitRestClientBuilderResolver...");
        RestClientBuilderResolver.setInstance(new TkitRestClientBuilderResolver());
    }
}
