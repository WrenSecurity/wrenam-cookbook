# Build Wren:IG with the SAML Fedlet configuration
FROM wrensecurity/wrenig:5.0.1

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

# Copy IG routes, scripts, Fedlet configuration, and SAML debug configuration
COPY --chown=1000:root conf/wrenig/config/ /srv/wrenig/config/
COPY --chown=1000:root conf/wrenig/scripts/ /srv/wrenig/scripts/
COPY --chown=1000:root conf/wrenig/fedlet/ /srv/wrenig/SAML/
COPY --chown=1000:root \
    conf/wrenig/runtime/debugconfig.properties \
    conf/wrenig/runtime/debugfiles.properties \
    /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/

# Decode the committed SAML keystore and restrict it to the Wren:IG user
RUN base64 -d /srv/wrenig/SAML/keystore.jceks.b64 > /srv/wrenig/SAML/keystore.jceks \
    && chown 1000:root /srv/wrenig/SAML/keystore.jceks \
    && chmod 0600 /srv/wrenig/SAML/keystore.jceks

# Run Wren:IG as the unprivileged user defined by the base image
USER 1000

# Document the internal HTTPS port
EXPOSE 8443

# Check protected SAML route availability
HEALTHCHECK --interval=5s --timeout=5s --start-period=20s --retries=24 \
    CMD curl --fail --silent --output /dev/null \
        --cacert /usr/local/share/ca-certificates/wrensec-test-ca.crt \
        https://protected.wrensecurity.local:8443/
