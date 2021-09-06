package org.tkit.quarkus.rs.util;

import lombok.Builder;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.engines.PassthroughTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Custom version of apache v4.3 builder with retry handler.
 */
@Builder(builderClassName = "Builder")
public class CustomClientHttpEngineBuilder43 {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomClientHttpEngineBuilder43.class);

    private int connectionPoolSize;
    @Builder.Default
    private int maxPooledPerRoute = 0;
    @Builder.Default
    private long connectionTTL = -1;
    @Builder.Default
    private TimeUnit connectionTTLUnit = TimeUnit.MILLISECONDS;
    @Builder.Default
    private long socketTimeout = -1;
    @Builder.Default
    private TimeUnit socketTimeoutUnits = TimeUnit.MILLISECONDS;
    @Builder.Default
    private long establishConnectionTimeout = -1;
    @Builder.Default
    private TimeUnit establishConnectionTimeoutUnits = TimeUnit.MILLISECONDS;
    @Builder.Default
    private int connectionCheckoutTimeoutMs = -1;
    @Builder.Default
    private HostnameVerifier verifier = null;
    private HttpHost defaultProxy;
    private KeyStore truststore;
    private KeyStore clientKeyStore;
    private String clientPrivateKeyPassword;
    @Builder.Default
    private boolean disableTrustManager;
    @Builder.Default
    private ResteasyClientBuilder.HostnameVerificationPolicy policy = ResteasyClientBuilder.HostnameVerificationPolicy.WILDCARD;
    private int maxRetries;
    @Builder.Default
    private List<String> sniHostNames = new ArrayList<>();
    private SSLContext sslContext;
    private int responseBufferSize;

    public ClientHttpEngine buildEngine() {
        if (verifier == null) {

            switch (policy) {
            case ANY:
                verifier = new NoopHostnameVerifier();
                break;
            case WILDCARD:
                verifier = new DefaultHostnameVerifier();
                break;
            case STRICT:
                verifier = new DefaultHostnameVerifier();
                break;
            }
        }
        try {
            SSLConnectionSocketFactory sslsf = null;
            SSLContext theContext = sslContext;
            if (disableTrustManager) {
                theContext = SSLContext.getInstance("SSL");
                theContext.init(null, new TrustManager[] { new PassthroughTrustManager() }, new SecureRandom());
                verifier = new NoopHostnameVerifier();
                sslsf = new SSLConnectionSocketFactory(theContext, verifier);
            } else if (theContext != null) {
                sslsf = new SSLConnectionSocketFactory(theContext, verifier) {
                    @Override
                    protected void prepareSocket(SSLSocket socket) throws IOException {
                        if (!sniHostNames.isEmpty()) {
                            List<SNIServerName> sniNames = new ArrayList<>(sniHostNames.size());
                            for (String sniHostName : sniHostNames) {
                                sniNames.add(new SNIHostName(sniHostName));
                            }

                            SSLParameters sslParameters = socket.getSSLParameters();
                            sslParameters.setServerNames(sniNames);
                            socket.setSSLParameters(sslParameters);
                        }
                    }
                };
            } else if (clientKeyStore != null || truststore != null) {
                SSLContext ctx = SSLContexts.custom().useProtocol(SSLConnectionSocketFactory.TLS).setSecureRandom(null)
                        .loadKeyMaterial(clientKeyStore,
                                clientPrivateKeyPassword != null ? clientPrivateKeyPassword.toCharArray() : null)
                        .loadTrustMaterial(truststore, TrustSelfSignedStrategy.INSTANCE).build();
                sslsf = new SSLConnectionSocketFactory(ctx, verifier) {
                    @Override
                    protected void prepareSocket(SSLSocket socket) throws IOException {
                        prepareSocketForSni(socket);
                    }
                };
            } else {
                final SSLContext tlsContext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
                tlsContext.init(null, null, null);
                sslsf = new SSLConnectionSocketFactory(tlsContext, verifier);
            }

            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();

            HttpClientConnectionManager cm = null;
            if (connectionPoolSize > 0) {
                PoolingHttpClientConnectionManager tcm = new PoolingHttpClientConnectionManager(registry, null, null,
                        null, connectionTTL, connectionTTLUnit);
                tcm.setMaxTotal(connectionPoolSize);
                if (maxPooledPerRoute == 0) {
                    maxPooledPerRoute = connectionPoolSize;
                }
                tcm.setDefaultMaxPerRoute(maxPooledPerRoute);
                cm = tcm;

            } else {
                cm = new BasicHttpClientConnectionManager(registry);
            }

            RequestConfig.Builder rcBuilder = RequestConfig.custom();
            if (socketTimeout > -1) {
                rcBuilder.setSocketTimeout((int) socketTimeoutUnits.toMillis(socketTimeout));
            }
            if (establishConnectionTimeout > -1) {
                rcBuilder.setConnectTimeout((int) establishConnectionTimeoutUnits.toMillis(establishConnectionTimeout));
            }
            if (connectionCheckoutTimeoutMs > -1) {
                rcBuilder.setConnectionRequestTimeout(connectionCheckoutTimeoutMs);
            }

            return createEngine(cm, rcBuilder, defaultProxy, responseBufferSize, verifier, theContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ClientHttpEngine createEngine(HttpClientConnectionManager cm, RequestConfig.Builder rcBuilder,
            HttpHost defaultProxy, int responseBufferSize, HostnameVerifier verifier, SSLContext theContext) {
        HttpClientBuilder httpBuilder = HttpClientBuilder.create();
        httpBuilder.setConnectionManager(cm).setDefaultRequestConfig(rcBuilder.build()).setProxy(defaultProxy);
        // .setRoutePlanner(new DefaultProxyRoutePlanner(defaultProxy))
        if (maxRetries > 0) {
            httpBuilder.setRetryHandler((exception, executionCount, context) -> {
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                LOGGER.warn("Retry handler: {} for {}", executionCount, request.getRequestLine(), exception);
                if (executionCount > maxRetries) {
                    LOGGER.warn("Maximum retries({}) reached for {}", maxRetries, request.getRequestLine());
                    return false;
                }
                if (exception instanceof org.apache.http.NoHttpResponseException) {
                    LOGGER.warn("No response from server, retry {}", executionCount);
                    return true;
                }
                if (exception instanceof java.net.SocketException) {
                    LOGGER.warn("Socket exception, retry {} ", executionCount);
                    return true;
                }
                return false;
            });
        }
        httpBuilder.disableContentCompression();
        HttpClient httpClient = httpBuilder.build();
        ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient, true);
        engine.setResponseBufferSize(responseBufferSize);
        engine.setHostnameVerifier(verifier);
        // this may be null. We can't really support this with Apache Client.
        engine.setSslContext(theContext);
        return engine;
    }

    protected void prepareSocketForSni(SSLSocket socket) {
        if (!sniHostNames.isEmpty()) {
            List<SNIServerName> sniNames = new ArrayList<>(sniHostNames.size());
            for (String sniHostName : sniHostNames) {
                sniNames.add(new SNIHostName(sniHostName));
            }

            SSLParameters sslParameters = socket.getSSLParameters();
            sslParameters.setServerNames(sniNames);
            socket.setSSLParameters(sslParameters);
        }
    }
}