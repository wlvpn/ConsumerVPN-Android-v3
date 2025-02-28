package com.wlvpn.consumervpn.application.interactor.settings


import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.util.stream.Stream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SaveProtocolSettingsInteractorTest {

    // Mocks
    private val protocolSettingsRepository: ProtocolSettingsRepository = mockk()

    // Fixtures
    private val connectionSettingsFixture = ConnectionSettings()
    private val openVpnSettingsFixture = ProtocolSettings.OpenVpn()
    private val wireGuardSettingsFixture = ProtocolSettings.Wireguard()
    private val iKEv2SettingsFixture = ProtocolSettings.IKEv2()

    // Arguments
    private val protocolSettingsOpenVpnArguments
        get() = Stream.of(
            Arguments.of(openVpnSettingsFixture)
        )
    private val protocolSettingsWireGuardArguments
        get() = Stream.of(
            Arguments.of(wireGuardSettingsFixture)
        )
    private val protocolSettingsIKEv2Arguments
        get() = Stream.of(
            Arguments.of(iKEv2SettingsFixture)
        )

    // SUT
    private val sut = SaveProtocolSettingsInteractor(
        protocolSettingsRepository
    )

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Success {

        // Scoping arguments to inner class to be used in the parameterized test
        private fun protocolSettingsOpenVpnArguments() = protocolSettingsOpenVpnArguments
        private fun protocolSettingsWireGuardArguments() = protocolSettingsWireGuardArguments
        private fun protocolSettingsIKEv2Arguments() = protocolSettingsIKEv2Arguments

        @ParameterizedTest
        @MethodSource("protocolSettingsOpenVpnArguments")
        fun `On saving openVpn settings, emit Success`(
            protocolSettings: ProtocolSettings
        ) {
            every {
                protocolSettingsRepository.saveProtocolSettings(any())
            } answers {
                flowOf(Unit)
            }


            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.OpenVpn
            }

            val result = runBlocking { sut.execute(protocolSettings).first() }
            assert(result is SaveProtocolSettingsContract.Status.Success)

            verify(exactly = 1) {
                protocolSettingsRepository.saveProtocolSettings(any())
            }
        }

        @ParameterizedTest
        @MethodSource("protocolSettingsWireGuardArguments")
        fun `On saving wireGuard settings, emit Success`(
            protocolSettings: ProtocolSettings
        ) {
            every {
                protocolSettingsRepository.saveProtocolSettings(any())
            } answers {
                flowOf(Unit)
            }

            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.WireGuard
            }

            val result = runBlocking { sut.execute(protocolSettings).first() }

            assert(result is SaveProtocolSettingsContract.Status.Success)

            verify(exactly = 1) {
                protocolSettingsRepository.saveProtocolSettings(any())
            }
        }

        @ParameterizedTest
        @MethodSource("protocolSettingsIKEv2Arguments")
        fun `On saving IKEv2 settings, emit Success`(
            protocolSettings: ProtocolSettings
        ) {
            every {
                protocolSettingsRepository.saveProtocolSettings(any())
            } answers {
                flowOf(Unit)
            }


            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.IKEv2
            }

            val result = runBlocking { sut.execute(protocolSettings).first() }
            assert(result is SaveProtocolSettingsContract.Status.Success)

            verify(exactly = 1) {
                protocolSettingsRepository.saveProtocolSettings(any())
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Error {

        // Scoping arguments to inner class to be used in the parametrized test
        private fun protocolSettingsOpenVpnArguments() = protocolSettingsOpenVpnArguments

        private val dummyErrorFixture = RuntimeException("Dummy error")

        @ParameterizedTest
        @MethodSource("protocolSettingsOpenVpnArguments")
        fun `On saving ProtocolSettings fails, emit UnableToCompleteFailure`(
            protocolSettings: ProtocolSettings
        ) {
            every {
                protocolSettingsRepository.saveProtocolSettings(any())
            } answers {
                flow {
                    throw dummyErrorFixture
                }
            }



            val result = runBlocking { sut.execute(protocolSettings).first() }
            assert(result is SaveProtocolSettingsContract.Status.UnableToCompleteFailure)

            verify(exactly = 1) {
                protocolSettingsRepository.saveProtocolSettings(any())
            }
        }

        @ParameterizedTest
        @MethodSource("protocolSettingsOpenVpnArguments")
        fun `On saving protocol settings with error on analyticsEvent, emit Success`(
            protocolSettings: ProtocolSettings
        ) {
            every {
                protocolSettingsRepository.saveProtocolSettings(any())
            } answers {
                flowOf(Unit)
            }

            connectionSettingsFixture.apply {
                selectedProtocol = Protocol.OpenVpn
            }

            val result = runBlocking { sut.execute(protocolSettings).first() }
            assert(result is SaveProtocolSettingsContract.Status.Success)

            verify(exactly = 1) {
                protocolSettingsRepository.saveProtocolSettings(any())
            }
        }
    }
}