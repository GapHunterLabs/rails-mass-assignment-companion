# Rails Mass Assignment Companion

Warning on `params.permit!` (or `xxx_params.permit!`) — Rails' own
official documentation states: "Extreme care should be taken when
using permit!, as it will allow all current and future model
attributes to be mass-assigned". This is the exact anti-pattern strong
parameters exist to prevent: it disables the whitelist entirely,
permitting whatever attributes an attacker sends in the request body,
including ones the developer never intended to be settable (the
well-known real-world case: a `role`/`admin` boolean flipped via mass
assignment).

Confirmed real gap: none of RuboCop's 7 `Security` cops (`Eval`,
`Open`, `IoMethods`, `MarshalLoad`, `YAMLLoad`, `JSONLoad`,
`CompoundHash`) cover Rails strong parameters/mass assignment —
confirmed by reading RuboCop's own Security cops documentation page
directly before building this.

## Why it exists

```ruby
def user_params
  params.permit!
end
```

compiles and runs fine — it looks like a shortcut, and it works,
right up until a request body includes `admin: true` and it goes
straight through to `User.new(user_params)`.

## Why built this way

- **100% static text analysis** — a regex-based line scanner, not a
  real Ruby parser, so it works whether the Ruby plugin is installed
  or not.

## v0.1 scope — stated honestly, not exhaustively

Doesn't resolve whether the receiver is actually an
`ActionController::Parameters` instance, so an unrelated custom
`.permit!` method on some other object is a possible (rare) false
positive.

## Usage

Open any `.rb` file. A `params.permit!`/`xxx_params.permit!` call
shows a warning.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
