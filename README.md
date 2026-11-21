# loader

Portable Loader abstraction for babashka, the JVM and Jolt: find, resolve,
load and unload namespaces, vars, classes and resources through one contract.

Design and implementation notes: [src/kmet/loader/loader.md](src/kmet/loader/loader.md).

## Tests

    bb test
    jolt -A:test test

The `:test` alias supplies the SCI backend on Jolt; the library itself
declares no SCI dependency (loader.md §6.5).

## License

Copyright © 2026 — present Marko Kocic <marko@euptera.com>

Licensed under the Eclipse Public License 2.0 ([EPL-2.0](LICENSE)).
