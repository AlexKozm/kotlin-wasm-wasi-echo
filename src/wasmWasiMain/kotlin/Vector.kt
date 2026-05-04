import kotlin.wasm.unsafe.MemoryAllocator
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

@OptIn(UnsafeWasmMemoryApi::class)
class Vector(val ptr: Pointer, val dataPtr: Pointer) {
    val lenPtr get() = ptr + 4
}

@OptIn(UnsafeWasmMemoryApi::class)
fun MemoryAllocator.allocateVector(size: Int): Vector {
    val readBuffer = allocate(size)
    val vectorDataPointer = allocate(8)
    val vec = Vector(vectorDataPointer, readBuffer)

    vec.ptr.storeInt(readBuffer.address.toInt())
    vec.lenPtr.storeInt(size)
    return vec
}