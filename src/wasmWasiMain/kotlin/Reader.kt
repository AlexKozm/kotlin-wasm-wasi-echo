import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

// https://github.com/WebAssembly/WASI/blob/wasi-0.1/preview1/docs.md#-fd_readfd-fd-iovs-iovec_array---resultsize-errno
@OptIn(ExperimentalWasmInterop::class)
@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiFdRead(
    // A file descriptor handle
    fd: Int,
    // iovs: iovec_array
    // iovec_array: List<iovec> (iovec_array: List<iovec>)
    // iovec: Record (https://github.com/WebAssembly/WASI/blob/wasi-0.1/preview1/docs.md#-iovec-record)
    listOfVectorsAddresses: Int,
    numOfVectors: Int,
    resultReadSizeAddress: Int
): Int

private const val STDIN_FD = 0


class WasiStdInputSource : RawSource {
    private var closed = false

    @OptIn(UnsafeWasmMemoryApi::class)
    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        if (closed) throw IllegalStateException("Source is closed")
        if (byteCount <= 0) throw IllegalArgumentException("byteCount should be positive")

        return withScopedMemoryAllocator { allocator ->
            // Not sure should I check if Long and UInt are in Int when I call toInt()
            val vector = allocator.allocateVector(byteCount.toInt())
            val resultReadSizePointer = allocator.allocate(4)

            val errno = wasiFdRead(
                STDIN_FD,
                vector.ptr.address.toInt(),
                1,
                resultReadSizePointer.address.toInt()
            )
            if (errno != 0) throw IOException("fd_read failed with errno $errno")

            val bytesRead = Pointer(resultReadSizePointer.address).loadInt()
            if (bytesRead == 0) return -1L // EOF

            sink.write(ByteArray(bytesRead) {
                // Probably could also be optimized
                Pointer(vector.dataPtr.address + it.toUInt()).loadByte()
            })
            bytesRead.toLong()
        }
    }

    override fun close() { closed = true }
}