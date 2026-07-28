# GitHub social login

This recipe adds GitHub to the Wren:AM login page. GitHub authenticates
the user. Wren:AM then reads the GitHub profile, maps the login to local
attribute `uid`, and creates or reuses a local account.

## Configuration overview

There are three Wren:AM objects behind that single GitHub button:

- OAuth module `githubLogin` communicates with GitHub, requests scopes,
  retrieves the profile, and maps it to a Wren:AM identity.
- Authentication chain `githubLogin` runs the OAuth module as a required
  authentication step.
- Social Authentication service publishes that chain as a GitHub option on
  the XUI login page.

The module and chain do the actual authentication. The Social Authentication
service is only responsible for putting the provider on the login page.

This recipe maps GitHub field `login` to local attribute `uid` and allows
automatic account creation. A returning GitHub login therefore resolves to the
same local account.

Wren:AM and GitHub maintain separate browser sessions. Logging out of Wren:AM
does not log the user out of GitHub.

## Configuration files

The Wren:AM files are in `conf/wrenam`:

- [`config.batch`](conf/wrenam/config.batch) – creates the module and chain,
  then enables the social provider
- [`github-login.properties`](conf/wrenam/github-login.properties) – defines
  GitHub endpoints, scopes, callback URI, account creation, and profile mapping
- [`github-login.entries`](conf/wrenam/github-login.entries) – adds module
  `githubLogin` to authentication chain `githubLogin` as required
- [`social-authentication.properties`](conf/wrenam/social-authentication.properties)
  – publishes the provider name, chain, icon, and enabled state

A fresh Wren:AM volume imports the objects in this order:

1. Creates OAuth module `githubLogin`.
2. Applies the GitHub module properties.
3. Creates authentication chain `githubLogin`.
4. Adds the OAuth module to the chain as required.
5. Adds the Social Authentication service and enables GitHub.

The client ID and secret are left out of the repository on purpose. Enter them
through the administration console; the Wren:AM volume keeps them between
container restarts.

## Docker container

There is only one container:

- `wrenam` – OAuth client, social authentication service, identity store, and
  XUI

Start the container:

```sh
docker compose up --build --wait
```

## Try it

### 1. Create a GitHub OAuth App

Create a GitHub OAuth App with the following callback URL:

```text
https://wrenam.wrensecurity.local:8443/auth/oauth2c/OAuthProxy.jsp
```

### 2. Configure the client credentials

Open [the Wren:AM login page](https://wrenam.wrensecurity.local:8443/auth/XUI/#login/)
and sign in as `amAdmin` with password `password`.

Open **Realms > Top Level Realm > Authentication > Modules > githubLogin**.
Enter the GitHub OAuth App's **Client ID** and **Client secret**, then save and
sign out.

### 3. Authenticate through GitHub

Return to [the Wren:AM login page](https://wrenam.wrensecurity.local:8443/auth/XUI/#login/)
and select the GitHub icon.

GitHub requests scopes `read:user user:email` and returns the browser to
Wren:AM. Wren:AM creates or reuses the mapped local account and establishes a
Wren:AM session.

### 4. Check the logout behavior

Logging out ends the Wren:AM session. It does not end the GitHub session.

## Where to look in Wren:AM

Sign in to the Wren:AM administration console and select **Top Level Realm**.

Inspect:

- **Authentication > Modules > githubLogin** for GitHub endpoints, scopes,
  callback URI, profile mapping, and client credentials
- **Authentication > Chains > githubLogin** for the required OAuth module
- **Services > Social Authentication** for the displayed name, chain, icon,
  and enabled provider

The legacy OAuth module initializes OpenID Connect support internally even
though GitHub uses OAuth in this flow. Keep **OpenID Connect crypto context type** set to `client_secret`.

That is enough to explain this GitHub setup. The
[Wren:AM documentation](https://docs.wrensecurity.org/wrenam/latest/) covers
the other module, chain, social provider, and account-mapping settings.

## Stop and reset

Stop the container and keep the Wren:AM data and GitHub credentials:

```sh
docker compose down
```

Remove the volume and import the recipe again:

```sh
docker compose down -v
docker compose up --build --wait
```

This deletes the saved GitHub credentials, mapped local users, sessions, and
the rest of the local Wren:AM data.
