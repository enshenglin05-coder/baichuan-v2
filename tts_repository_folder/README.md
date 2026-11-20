# TTS Mini Root (tts_repository_folder)

An independent, strictly controlled TTS execution, reading, and infrastructure root.

## Architecture Rules
- Independent project root isolated from Android and parent Gradle build.
- Root resolver enforces strict path security (no `..`, absolute traversal, or symbolic link escapes).
- Reader connects exclusively to permitted child directories (`data/`, `config/`, `runtime/`, `commands/`, `src/`).
- Command dispatcher contains an explicit, empty command registry; unknown commands are strictly rejected.
- Infrastructure only: no speech synthesis, playback, automatic processing, or business workflows.
