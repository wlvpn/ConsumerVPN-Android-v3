package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaveConnectionSettingsInteractorTest {
    // Mocks
    private val connectionSettingsRepository: ConnectionSettingsRepository = mockk()

    // Fixtures
    private val connectionSettingsFixture = ConnectionSettings()

    // SUT
    private val sut = SaveConnectionSettingsInteractor(
        connectionSettingsRepository
    )

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Success {
        @Test
        fun `On saving openVpn settings, emit Success`() {
            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.OpenVpn
            }

            every {
                connectionSettingsRepository.saveConnectionSettings(any())
            } answers {
                flowOf(Unit)
            }

            val result = runBlocking { sut.execute(connectionSettingsFixture).first() }

            assert(result is SaveConnectionSettingsContract.Status.Success)

            verify(exactly = 1) {
                connectionSettingsRepository.saveConnectionSettings(any())
            }
        }

        @Test
        fun `On saving wireGuard settings, emit Success`() {
            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.WireGuard
            }

            every {
                connectionSettingsRepository.saveConnectionSettings(any())
            } answers {
                flowOf(Unit)
            }

            val result = runBlocking { sut.execute(connectionSettingsFixture).first() }
            assert(result is SaveConnectionSettingsContract.Status.Success)

            verify(exactly = 1) {
                connectionSettingsRepository.saveConnectionSettings(any())
            }
        }

        @Test
        fun `On saving IKEv2 settings, emit Success`() {
            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.IKEv2
            }

            every {
                connectionSettingsRepository.saveConnectionSettings(any())
            } answers {
                flowOf(Unit)
            }

            val result = runBlocking { sut.execute(connectionSettingsFixture).first() }
            assert(result is SaveConnectionSettingsContract.Status.Success)

            verify(exactly = 1) {
                connectionSettingsRepository.saveConnectionSettings(any())
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Error {
        private val dummyErrorFixture = RuntimeException("Dummy error")

        @Test
        fun `On saving ConnectionSettings fails, emit UnableToCompleteFailure`() {
            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.OpenVpn
            }

            every {
                connectionSettingsRepository.saveConnectionSettings(any())
            } answers {
                flow {
                    throw dummyErrorFixture
                }
            }

            val result = runBlocking { sut.execute(connectionSettingsFixture).first() }
            assert(result is SaveConnectionSettingsContract.Status.UnableToSaveFailure)

            verify(exactly = 1) {
                connectionSettingsRepository.saveConnectionSettings(any())
            }
        }

        @Test
        fun `On saving protocol settings with error on analyticsEvent, emit Success`() {
            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.OpenVpn
            }

            every {
                connectionSettingsRepository.saveConnectionSettings(any())
            } answers {
                flowOf(Unit)
            }

            val result = runBlocking { sut.execute(connectionSettingsFixture).first() }
            assert(result is SaveConnectionSettingsContract.Status.Success)

            verify(exactly = 1) {
                connectionSettingsRepository.saveConnectionSettings(any())
            }
        }
    }
}