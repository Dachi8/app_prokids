package com.example

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test de arranque real de la pantalla principal.
 *
 * Reproduce exactamente lo que ocurre al abrir la app en el dispositivo:
 * se crea la Application (con sus ContentProviders, incluido el de Firebase),
 * se crea la Activity y se infla la interfaz de Compose.
 *
 * Si cualquier inicialización revienta (por ejemplo el proveedor de Firebase
 * sin `google-services.json`), la app se cerraría nada más abrirla y este
 * test fallaría.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityLaunchTest {

    @Test
    fun `la pantalla principal arranca sin crash`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }
}
