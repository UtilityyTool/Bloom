# Contributing to Bloom

Thanks for helping. Two rules matter more than anything else here.

## 1. Smooth or it doesn't ship

Bloom runs inside Discord, so any slowness feels like Discord being broken.

- Nothing Bloom does may block drawing a frame. Heavy work goes to a background thread.
- Startup work waits until Discord has drawn its first screen.
- Plugins follow the time and memory limits in [docs/plugin-rules.md](docs/plugin-rules.md). Bloom's own code is held to the same limits.
- If a change can affect performance, test it on a mid-range phone, not only a fast PC.

## 2. The user's account comes first

- Never touch the login token or password.
- Never send anything to Discord that the normal app wouldn't.
- When in doubt, make it opt-in and warn the user.

## License

By contributing, you agree your code is released under the [GPL-3.0](LICENSE).
