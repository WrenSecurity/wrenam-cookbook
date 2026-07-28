# Policy decision point

This recipe uses Wren:AM to make authorization decisions and Wren:IG to
enforce them. The root page is open to any signed-in user. `/internal` is only
available to members of the `employees` group.

## Configuration overview

The work is split between two components:

- Wren:AM stores the identities, group membership, and policies. It makes the
  decision.
- Wren:IG protects the application. It sends the current session and requested
  resource to Wren:AM, then allows or rejects the request.

Each policy matches:

- a resource such as `https://protected.wrensecurity.local:443/internal`
- an action such as `GET`
- a subject such as all authenticated users or members of `employees`

Both policies explicitly allow access. Anything that does not match remains
denied.

## Configuration files

The Wren:AM files are in `conf/wrenam`:

- [`config.batch`](conf/wrenam/config.batch) – creates the employee identity
  and group, assigns membership, and imports the policies
- [`pdp-policies.xml`](conf/wrenam/pdp-policies.xml) – defines the
  authenticated-user and employee-only policies

The Wren:IG files are in `conf/wrenig`:

- [`config/config.json`](conf/wrenig/config/config.json) – defines the base
  Wren:IG configuration
- [`config/routes/01-policy.json`](conf/wrenig/config/routes/01-policy.json) –
  validates the Wren:AM session, requests a policy decision, and returns the
  allowed response

A fresh Wren:AM volume imports the test data and policies in this order:

1. Creates user `employee`.
2. Creates group `employees`.
3. Adds `employee` to `employees`.
4. Imports both authorization policies into the top-level realm.

Once those objects exist, the batch has no role in policy evaluation.

## Docker containers

This recipe has two containers:

- `wrenam` – identity store and policy decision point
- `wrenig` – policy enforcement point and protected application

Start both containers:

```sh
docker compose up --build --wait
```

## Try it

Use the following accounts:

| User | Password | Group |
| --- | --- | --- |
| `demo` | `changeit` | None |
| `employee` | `changeit` | `employees` |

### 1. Test an authenticated user

Open [the protected root](https://protected.wrensecurity.local/) and sign in
as `demo`.

The root resource returns:

```text
Access granted
```

Open [the internal resource](https://protected.wrensecurity.local/internal)
with the same session. Wren:IG returns HTTP `403`.

### 2. Test an employee

Sign out, return to the internal resource, and sign in as `employee`.

The internal resource now returns HTTP `200` with:

```text
Access granted
```

## Where to look in Wren:AM

Sign in to the Wren:AM administration console and select **Top Level Realm**.

Inspect the identities to see user `employee`, group `employees`, and their
membership. Inspect **Authorization > Policy Sets** and open application
`iPlanetAMWebAgentService` to see:

- an authenticated-user policy allowing `GET` on the protected root
- an employee-group policy allowing `GET` on `/internal`

The resource strings in Wren:AM must match the canonical resource strings sent
by Wren:IG. The policy application name in
[`01-policy.json`](conf/wrenig/config/routes/01-policy.json) must also match the
Wren:AM policy set.

That is all you need to follow this recipe. The
[Wren:AM documentation](https://docs.wrensecurity.org/wrenam/latest/) has the
full policy, subject, condition, and application reference.

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

This deletes the employee identity and group, both policies, sessions, and the
rest of the local Wren:AM data.
