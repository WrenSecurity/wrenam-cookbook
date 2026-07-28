# SAML single sign-on

In this recipe, Wren:AM is a hosted SAML 2.0 identity provider and Wren:IG is
the service provider. Wren:IG starts single sign-on, validates the signed
assertion, and creates an application session on the server.

## Configuration overview

SAML needs a description of both sides of the trust:

- The hosted identity provider record has Wren:AM's entity ID, endpoints,
  signing key, and released attributes.
- The remote service provider record gives Wren:AM the Wren:IG entity ID and
  assertion consumer endpoint.
- The Wren:IG Fedlet configuration contains the matching service provider and
  identity provider records used by Wren:IG.
- Circle of trust `samltest` puts the two entities in the same federation.

Standard metadata describes protocol endpoints, bindings, entity IDs, and
public keys. Extended metadata holds Wren:AM-specific behavior such as
meta-aliases, signing choices, and attribute mappings.

The two copies must agree. Change an entity ID, endpoint, certificate, binding,
or attribute map on one side only and the flow will break.

## Configuration files

The Wren:AM files are in `conf/wrenam`:

- [`config.batch`](conf/wrenam/config.batch) – creates the circle of trust and
  imports the two SAML entities
- [`samltest-idp-meta.xml`](conf/wrenam/realms/samltest-idp-meta.xml) –
  standard metadata for the hosted Wren:AM identity provider
- [`samltest-idp-extended.xml`](conf/wrenam/realms/samltest-idp-extended.xml) –
  Wren:AM settings for signing, meta-alias `/idp`, and attribute mapping
- [`samltest-sp-meta.xml`](conf/wrenam/realms/samltest-sp-meta.xml) – standard
  metadata for the remote Wren:IG service provider
- [`samltest-sp-extended.xml`](conf/wrenam/realms/samltest-sp-extended.xml) –
  Wren:AM settings for the remote service provider

The Wren:IG files are in `conf/wrenig`:

- [`fedlet/`](conf/wrenig/fedlet/) – Fedlet metadata, circle of trust,
  keystore, and federation properties
- [`config/routes/`](conf/wrenig/config/routes/) – starts SSO, processes the
  response, and exposes the assertion
- [`CaptureSamlResponse.groovy`](conf/wrenig/scripts/groovy/org/wrensecurity/wrenig/filter/CaptureSamlResponse.groovy)
  – keeps a copy of the posted XML for the `/assertion` page
- [`runtime/`](conf/wrenig/runtime/) – enables the Wren:IG SAML runtime
  configuration

A fresh Wren:AM volume imports the federation in this order:

1. Creates circle of trust `samltest`.
2. Imports hosted identity provider `samltest-idp`.
3. Imports remote service provider `samltest-sp`.

The batch is only used for that import. Wren:AM handles later SAML requests
from its stored configuration.

## Docker containers

This recipe has two containers:

- `wrenam` – SAML identity provider
- `wrenig` – SAML service provider and protected application

Start both containers:

```sh
docker compose up --build --wait
```

## Try it

### 1. Start single sign-on

Open [the protected application](https://protected.wrensecurity.local/).
Wren:IG creates a SAML authentication request and redirects the browser to
Wren:AM.

### 2. Authenticate the demo user

Sign in as `demo` with password `changeit`.

Wren:AM issues a signed SAML response and posts it to the Wren:IG assertion
consumer endpoint.

### 3. Inspect the assertion

After Wren:IG validates the response, it redirects the browser to
`https://protected.wrensecurity.local/assertion`.

The browser's XML viewer displays the SAML response issued by Wren:AM,
including its signed assertion. The assertion contains subject `demo` and the
released `uid` attribute.

`/assertion` exists only so the recipe can show the XML. Validation still
belongs to `SamlFederationHandler`. The small capture script does not validate
anything.

## Where to look in Wren:AM

Sign in to the Wren:AM administration console and open **Top Level Realm > Applications > SAML 2.0**.

Inspect:

- circle of trust `samltest`
- hosted identity provider `samltest-idp`
- remote service provider `samltest-sp`

The identity provider uses meta-alias `/idp`, signing key alias `test`, and
attribute map `uid=uid`. The service provider belongs to the same circle of
trust, requires signed assertions, and uses the corresponding `uid` mapping.

The committed identity provider metadata contains the public certificate for
Wren:AM's standard `test` signing key.

Those are the parts used by this recipe. See the
[Wren:AM documentation](https://docs.wrensecurity.org/wrenam/latest/) for all
metadata, binding, signing, and certificate rollover options.

## Stop and reset

Stop the containers and keep the Wren:AM data:

```sh
docker compose down
```

Remove the volume and import the federation again:

```sh
docker compose down -v
docker compose up --build --wait
```

This deletes the imported federation, sessions, and the rest of the local
Wren:AM data.
