import kotlin.test.Test
import kotlin.test.assertFailsWith


class EnvironmentTest {
    @Test
    fun `kotlin.io.readln() is not implemented`() {
        assertFailsWith<NotImplementedError> {
            readln()
        }
    }
}