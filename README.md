# Wren:AM cookbook

Cookbook consists of recipes (samples) demonstrating key Wren:AM features.
The environment for each recipe is defined in a Docker Compose file. A single
command starts the required containers and imports the included configuration.

The following recipes are available:

- [`social-login`](social-login/) – delegate user authentication to GitHub
  through Wren:AM's OAuth social authentication flow
- [`token-exchange`](token-exchange/) – translate a Wren:AM session
  into a signed SAML 2.0 assertion through the Security Token Service
- [`policy-decision-point`](policy-decision-point/) – make authorization
  decisions in Wren:AM and enforce them with Wren:IG
- [`oidc-webapp`](oidc-webapp/) – protect a web application with the OIDC
  authorization code flow using Wren:AM and Wren:IG
- [`saml-sso`](saml-sso/) – provide SP-initiated SAML 2.0 single sign-on with
  Wren:AM as the identity provider and Wren:IG as the service provider

Each recipe has its own README with setup and test instructions.

## Prerequisites

Before continuing, make sure the following requirements are met:

- [Docker Engine](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/) are installed
- The selected recipe `docker/wrenam/ca.crt` is trusted by the browser or operating system
- The following names are present in the workstation hosts file:

```text
127.0.0.1 wrenam.wrensecurity.local protected.wrensecurity.local
```

## License

This cookbook is licensed under the
[Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International](LICENSE)
license.
