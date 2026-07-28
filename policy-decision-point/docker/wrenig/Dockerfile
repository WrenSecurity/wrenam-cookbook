# Allow the Wren:IG base image to be overridden at build time
ARG WRENIG_IMAGE=wrensecurity/wrenig:5.0.1
FROM ${WRENIG_IMAGE}

USER root

# Install Tomcat connector configuration and the protected origin HTTPS certificate
COPY --chown=1000:root docker/wrenig/server.p12 /usr/local/tomcat/conf/protected.wrensecurity.local.p12
COPY --chown=1000:root docker/wrenig/tomcat/server.xml /usr/local/tomcat/conf/server.xml

# Copy the cookbook CA for Wren:IG connections to Wren:AM
COPY docker/wrenig/ca.crt /usr/local/share/ca-certificates/wrensec-test-ca.crt

# Create the protected virtual host and trust the cookbook CA in Java
RUN mkdir -p /usr/local/tomcat/conf/Catalina/protected.wrensecurity.local \
    && chown -R 1000:root /usr/local/tomcat/conf/Catalina \
    && keytool -importcert -noprompt -trustcacerts -alias wrensec-test-ca \
      -file /usr/local/share/ca-certificates/wrensec-test-ca.crt \
      -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit

# Run Wren:IG as the unprivileged user defined by the base image
USER 1000

# Document the internal HTTPS port
EXPOSE 8443

# Check protected HTTPS route availability
HEALTHCHECK --interval=5s --timeout=5s --start-period=20s --retries=24 \
    CMD curl --fail --silent --output /dev/null \
        --cacert /usr/local/share/ca-certificates/wrensec-test-ca.crt \
        https://protected.wrensecurity.local:8443/
