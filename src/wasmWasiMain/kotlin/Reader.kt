import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@OptIn(ExperimentalWasmInterop::class)
@WasmImport("wasi_snapshot_preview1", "fd_read")
external fun wasiFdRead(fd: Int, vectorPtr: Int, vectorLen: Int, resultReadSizePtr: Int): Int

private const val STDIN_FD = 0


class WasiStdInputSource : RawSource {
    private var closed = false

    @OptIn(UnsafeWasmMemoryApi::class)
    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        if (closed) throw IllegalStateException("Source is closed")
        if (byteCount <= 0) return 0

        return withScopedMemoryAllocator { allocator ->
            val readBuffer = allocator.allocate(byteCount.toInt())
            val vectorDataPointer = allocator.allocate(8)
            val resultReadSizePointer = allocator.allocate(4)

            vectorDataPointer.storeInt(readBuffer.address.toInt())
            (vectorDataPointer + 4).storeInt(byteCount.toInt())

            val errno = wasiFdRead(
                STDIN_FD,
                vectorDataPointer.address.toInt(),
                byteCount.toInt(),
                resultReadSizePointer.address.toInt()
            )
            if (errno != 0) throw IOException("fd_read failed with errno $errno")

            val bytesRead = Pointer(resultReadSizePointer.address).loadInt()
            if (bytesRead == 0) return -1L // EOF

            for (i in 0 until bytesRead) {
                sink.writeByte(Pointer(readBuffer.address + i.toUInt()).loadByte())
            }
            bytesRead.toLong()
        }
    }

    override fun close() { closed = true }
}