# Contributing to Bloom

Thanks for helping. Three rules matter more than anything else here.

## 1. Original code only

Every line in Bloom is written for Bloom.

- **Don't copy code from other Discord mods**: Vencord, BetterDiscord, Sunbird / phoenix-unlock, Aliucord, Revenge, or any other.
- **Don't paste code you found online** unless its license allows it *and* it's credited in the file and in the pull request.
- **Ideas are fine, code isn't.** "Vencord has a plugin that does X" is a fine reason to build X. Reading their code to build ours is not.
- Learn how things work from **official docs and public explanations**, then write it yourself.

Pull requests that look copied will be closed.

## 2. Smooth or it doesn't ship

Bloom runs inside Discord, so any slowness feels like Discord being broken.

- Nothing Bloom does may block drawing a frame. Heavy work goes to a background thread.
- Startup work waits until Discord has drawn its first screen.
- Plugins follow the time and memory limits in [docs/plugin-rules.md](docs/plugin-rules.md). Bloom's own code is held to the same limits.
- If a change can affect performance, test it on a mid-range phone, not only a fast PC.

## 3. The user's account comes first

- Never touch the login token or password.
- Never send anything to Discord that the normal app wouldn't.
- When in doubt, make it opt-in and warn the user.

## License

By contributing, you agree your code is released under the [GPL-3.0](LICENSE).
