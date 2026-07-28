# Token exchange

This recipe uses Wren:AM's REST Security Token Service to exchange an
authenticated Wren:AM session for a signed SAML 2.0 assertion.

## Configuration overview

The REST STS accepts one token type and returns another:

- Input token type `OPENAM` is an authenticated Wren:AM session.
- Output token type `SAML2` is a signed SAML 2.0 assertion.
- The REST STS instance contains the issuer, audience, recipient, lifetime,
  accepted token types, and signing key.
- The federation service sets the signature and digest algorithms used for
  generated SAML assertions.

This is token translation, not browser SAML single sign-on. Wren:AM returns
the assertion to the caller instead of posting it to a service provider.

The input session remains valid after a successful exchange. The recipe does
not consume or revoke it.

## Configuration files

The Wren:AM files are in `conf/wrenam`:

- [`config.batch`](conf/wrenam/config.batch) – selects the SAML algorithms and
  publishes the REST STS instance
- [`saml-algorithms.properties`](conf/wrenam/saml-algorithms.properties) –
  selects RSA-SHA256 signatures and SHA-256 digests
- [`token-exchange.properties`](conf/wrenam/token-exchange.properties)
  – defines REST STS instance `session-to-saml`

A fresh Wren:AM volume imports the configuration in this order:

1. Updates the global federation service with the selected SAML algorithms.
2. Creates REST STS instance `session-to-saml` in the top-level realm.

The batch only publishes the service. It never handles a token exchange.

## Docker container

There is one container:

- `wrenam` – authentication service, REST STS, and SAML token issuer

Start the container:

```sh
docker compose up --build --wait
```

The procedure requires `curl`.

## Try it

### 1. Authenticate the demo user

Run the following command from the recipe directory:

```sh
curl --fail-with-body \
  --cacert docker/wrenam/ca.crt \
  --request POST \
  --header 'Accept-API-Version: resource=2.0, protocol=1.0' \
  --header 'Content-Type: application/json' \
  --header 'X-OpenAM-Username: demo' \
  --header 'X-OpenAM-Password: changeit' \
  'https://wrenam.wrensecurity.local:8443/auth/json/realms/root/authenticate'
```

The response contains a new Wren:AM session in `tokenId`:

```json
{
  "tokenId": "AQIC5w...",
  "successUrl": "/auth/console"
}
```

Copy the complete `tokenId` value.

### 2. Exchange the session for SAML

Replace `PASTE_SESSION_TOKEN_HERE` with the copied value:

```sh
curl --fail-with-body \
  --cacert docker/wrenam/ca.crt \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{
    "input_token_state": {
      "token_type": "OPENAM",
      "session_id": "PASTE_SESSION_TOKEN_HERE"
    },
    "output_token_state": {
      "token_type": "SAML2",
      "subject_confirmation": "BEARER"
    }
  }' \
  'https://wrenam.wrensecurity.local:8443/auth/rest-sts/session-to-saml?_action=translate'
```

Wren:AM returns the signed assertion in `issued_token`:

```json
{
  "issued_token": "<saml:Assertion ...>...</saml:Assertion>"
}
```

The assertion has:

- subject `demo`
- issuer `urn:wren:am:cookbook:token-exchange`
- audience `urn:wren:am:cookbook:service-provider`
- bearer subject confirmation
- ten-minute lifetime
- illustrative recipient `https://service.example.com/saml/acs`

The recipient is assertion content only. This request does not perform a
browser SAML POST. See the [`saml-sso`](../saml-sso/) recipe for browser SAML
single sign-on.

## Where to look in Wren:AM

Sign in to the Wren:AM administration console and open **Top Level Realm > STS > REST STS Instances**.

Open instance `session-to-saml` and check:

- input token type `OPENAM`
- output token type `SAML2`
- deployment URL element `session-to-saml`
- configured issuer, audience, and assertion consumer URL
- signing key alias `wrenam.wrensecurity.local`
- ten-minute token lifetime

The SAML signature and digest algorithms are global federation settings rather
than fields owned by this individual STS instance.

Those settings are enough for this exchange. The
[Wren:AM documentation](https://docs.wrensecurity.org/wrenam/latest/) covers
the rest of the REST STS behavior.

## Stop and reset

Stop the container and keep the Wren:AM data:

```sh
docker compose down
```

Remove the volume and import the STS configuration again:

```sh
docker compose down -v
docker compose up --build --wait
```

This deletes the REST STS instance, sessions, and the rest of the local
Wren:AM data.
