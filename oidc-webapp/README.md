# OIDC web application

This recipe turns Wren:AM into an OpenID Connect provider. Wren:IG is the
confidential web client which handles the authorization code exchange and keeps
the client secret and tokens out of the browser.

## Configuration overview

Three bits of configuration matter here:

- The Wren:AM OAuth 2.0 provider service controls realm-wide protocol behavior,
  supported scopes, claims, token behavior, and consent policy.
- The `oidc-webapp` client registration says what the application may request
  and where Wren:AM may send the authorization response.
- Wren:IG is the application at runtime. It starts authorization,
  authenticates the client, exchanges the code, and obtains UserInfo.

Consent is configured on both the provider and the client. The provider does
not allow clients to skip it, and `oidc-webapp` does not use implied consent.

Wren:AM compares the redirect URI exactly. The scheme, host, port, and path
must all match the registered value.

Scope `openid` requests OpenID Connect authentication. Scope `profile`
asks for the standard profile claims. `openid` is required by the protocol but
is hidden on the consent screen; the user only sees `profile`.

## Configuration files

The Wren:AM files are in `conf/wrenam`:

- [`config.batch`](conf/wrenam/config.batch) – adds the provider service, then
  registers the client
- [`oauth2-provider.properties`](conf/wrenam/oauth2-provider.properties) –
  contains the provider's consent, scope, claim, and token settings
- [`oidc-webapp.properties`](conf/wrenam/oidc-webapp.properties) – defines the
  client ID, secret, authentication method, response type, scopes, and exact
  redirect URI

The Wren:IG files are in `conf/wrenig`:

- [`config/config.json`](conf/wrenig/config/config.json) – contains the base
  Wren:IG setup
- [`config/routes/`](conf/wrenig/config/routes/) – redirects the entry point
  and runs the OpenID Connect flow
- [`UserInfoResponseHandler.groovy`](conf/wrenig/scripts/groovy/org/wrensecurity/wrenig/handler/UserInfoResponseHandler.groovy)
  – returns the UserInfo claims already obtained by `OAuth2ClientFilter`

A fresh Wren:AM volume imports the configuration in this order:

1. Adds the OAuth 2.0 provider service to the top-level realm.
2. Registers `oidc-webapp` as an OAuth 2.0 client.

After that, `config.batch` is done. It is not part of the login flow.

## Docker containers

This recipe has two containers:

- `wrenam` – OpenID Connect provider, authorization server, and UserInfo
  provider
- `wrenig` – confidential client and browser-facing protected application

Start both containers:

```sh
docker compose up --build --wait
```

## Try it

### 1. Open the protected application

Open [the protected application](https://protected.wrensecurity.local/).
Wren:IG redirects the browser to Wren:AM.

Sign in as `demo` with password `changeit`.

### 2. Approve access

Wren:AM displays a consent screen for the `profile` scope. Approve the
request.

### 3. Check UserInfo

After the callback and code exchange, the browser is redirected to
`https://protected.wrensecurity.local/userinfo`.

Wren:IG displays the UserInfo JSON returned by Wren:AM:

```json
{"name":"demo","sub":"demo","family_name":"demo"}
```

## Where to look in Wren:AM

Sign in to the Wren:AM administration console and select **Top Level Realm**.

Open **Services > OAuth2 Provider** and check:

- clients cannot skip consent
- refresh does not replace refresh tokens
- supported scopes include `openid` and `profile`
- supported claims include `name`, `given_name`, `family_name`, `locale`, and
  `zoneinfo`

Under **Applications > OAuth 2.0 > oidc-webapp**, the client is registered as
a confidential client using client-secret-basic
authentication, response type `code`, scopes `openid profile`, and redirect
URI `https://protected.wrensecurity.local:443/openid/callback`.

Wren:IG is not configured in the Wren:AM console. Its issuer, client ID,
secret, scopes, and callback path must remain aligned with the client
registration.

Those are the settings worth knowing for this recipe. The
[Wren:AM documentation](https://docs.wrensecurity.org/wrenam/latest/) covers
the rest of the provider and client properties.

## Stop and reset

Stop the containers and keep the Wren:AM data:

```sh
docker compose down
```

Remove the volume and import everything again:

```sh
docker compose down -v
docker compose up --build --wait
```

This deletes the imported provider and client along with sessions and other
local Wren:AM data.
