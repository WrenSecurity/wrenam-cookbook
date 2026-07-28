FROM wrensecurity/wrenam:15.3.0-M1

USER root

# Copy initialization scripts and properties
COPY --chown=1000:root --chmod=0755 docker/wrenam/bootstrap/config.sh /opt/wrenam/config.sh
COPY --chown=1000:root --chmod=0755 docker/wrenam/bootstrap/entrypoint.sh /opt/wrenam/entrypoint.sh
COPY --chown=1000:root --chmod=0755 docker/wrenam/bootstrap/ssoadm.sh /usr/local/bin/ssoadm
COPY --chown=1000:root docker/wrenam/bootstrap/init.properties /srv/wrenam-init/init.properties

# Copy Tomcat configuration and the Wren:AM HTTPS certificate
COPY --chown=1000:root docker/wrenam/tomcat/server.xml /usr/local/tomcat/conf/server.xml
COPY --chown=1000:root docker/wrenam/server.p12 /usr/local/tomcat/conf/wrenam-cookbook.p12

# Copy the cookbook CA for local HTTPS bootstrap and health checks
COPY --chown=1000:root docker/wrenam/ca.crt /usr/local/share/ca-certificates/wrensec-test-ca.crt

# Trust the cookbook CA in Linux for bootstrap and health check curl
RUN update-ca-certificates

# Trust the cookbook CA in Java for configurator and ssoadm
RUN keytool -importcert \
    -noprompt \
    -trustcacerts \
    -alias wrensec-test-ca \
    -file /usr/local/share/ca-certificates/wrensec-test-ca.crt \
    -cacerts \
    -storepass changeit

# Copy GitHub login and social authentication settings for import at first startup
COPY --chown=1000:root conf/wrenam/config.batch /srv/wrenam-init/config.batch
COPY --chown=1000:root \
    conf/wrenam/github-login.entries \
    conf/wrenam/github-login.properties \
    conf/wrenam/social-authentication.properties \
    /srv/wrenam-init/setup/

# Run Wren:AM as the unprivileged user defined by the base image
USER 1000

# Check recipe initialization and Wren:AM availability
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=30 \
    CMD ! pgrep --exact config.sh >/dev/null \
        && curl --fail --silent https://wrenam.wrensecurity.local:8443/auth/isAlive.jsp \
        | grep -q 'Server is ALIVE'

# Run the initialization check alongside Tomcat startup
ENTRYPOINT ["/opt/wrenam/entrypoint.sh"]
CMD ["catalina.sh", "run"]
