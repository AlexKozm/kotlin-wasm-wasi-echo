# Kotlin WASM WASI Echo

Kotlin echo program that compiles to a WebAssembly binary targeting the WASI environment and
executes with Wasmtime.

## Run

```sh
./gradlew runWasm --console=plain
```

## Test

```sh
./gradlew wasmWasiNodeTest
```

Currently, there is only one test for check that kotlin.io.readln() is not implemented in
test environment. If I have more time, I would also write integration tests.

## Troubleshooting

### Some version exception during runWasm task

> FAILURE: Build failed with an exception.
> 
> * What went wrong:
> 25.0.2

Solution: do not use jdk 25 for gradle. 
I fixed it by adding into [gradle.properties](gradle.properties) line with another installed 
jdk. Example: `#org.gradle.java.home=/home/alex/.jdks/jbr-17.0.14`
