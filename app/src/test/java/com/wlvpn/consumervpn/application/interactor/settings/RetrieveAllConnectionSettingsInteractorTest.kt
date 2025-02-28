package com.wlvpn.consumervpn.application.interactor.settings


import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.lang.RuntimeException
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RetrieveAllConnectionSettingsInteractorTest {

    // Mocks
    private val connectionSettingsRepository: ConnectionSettingsRepository = mockk()
    private val protocolSettingsRepository: ProtocolSettingsRepository = mockk()
    private val externalVpnSettingsGateway: ExternalVpnSettingsGateway = mockk()

    // Fixtures
    private val openVpnSettingsFixture = ProtocolSettings.OpenVpn()
    private val wireGuardSettingsFixture = ProtocolSettings.Wireguard()
    private val iKEv2SettingsFixture = ProtocolSettings.IKEv2()
    private val openVpnPortFixture = listOf(1111,222,333)
    private val iKEv2PortFixture = listOf(1234,4321,4123)
    private val wireGuardPortFixture = listOf(5678,5867,8856)
    private val dummyErrorFixture = RuntimeException("Dummy error")

    // Arguments
    private val connectionSettingsArguments
        get() = Stream.of(
            Arguments.of(
                openVpnSettingsFixture,
                ConnectionSettings(selectedProtocol = Protocol.OpenVpn),
                openVpnPortFixture
            ),
            Arguments.of(
                wireGuardSettingsFixture,
                ConnectionSettings(selectedProtocol = Protocol.WireGuard),
                wireGuardPortFixture
            ),
            Arguments.of(
                iKEv2SettingsFixture,
                ConnectionSettings(selectedProtocol = Protocol.IKEv2),
                iKEv2PortFixture
            )
        )

    // SUT
    private val sut = RetrieveAllConnectionSettingsInteractor(
        connectionSettingsRepository,
        protocolSettingsRepository,
        externalVpnSettingsGateway
    )

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Success {

        // Scoping arguments to inner class to be used in the parametrized test
        private fun connectionSettingsArguments() = connectionSettingsArguments

        @ParameterizedTest
        @MethodSource("connectionSettingsArguments")
        fun `On retrieve all connections settings, emit Success`(
            protocolSettings: ProtocolSettings,
            connectionSettings: ConnectionSettings,
            portList: List<Int>
        ) {
            every {
                connectionSettingsRepository.getConnectionSettings()
            } answers {
                flowOf(connectionSettings)
            }

            every {
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
            } answers {
                flowOf(protocolSettings)
            }

            every {
                externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
            } answers {
                flowOf(portList)
            }

            val result = runBlocking { sut.execute()
                .first() }

            assert(result is RetrieveAllConnectionSettingsContract.Status.Success)

            verify(exactly = 1) {
                connectionSettingsRepository.getConnectionSettings()
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
                externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
            }
        }

        @ParameterizedTest
        @MethodSource("connectionSettingsArguments")
        fun `On retrieve port list error, emit Success with an empty port list`(
            protocolSettings: ProtocolSettings,
            connectionSettings: ConnectionSettings,
        ) {
            every {
                connectionSettingsRepository.getConnectionSettings()
            } answers {
                flowOf(connectionSettings)
            }

            every {
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
            } answers {
                flowOf(protocolSettings)
            }

            every {
                externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
            } answers {
                flow {
                    throw dummyErrorFixture
                }
            }

            val result = runBlocking { sut.execute().first() }


            assert(result is RetrieveAllConnectionSettingsContract.Status.Success
                    && result.availableVpnPorts.isEmpty())

            verify(exactly = 1) {
                connectionSettingsRepository.getConnectionSettings()
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
                externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Error {

        // Scoping arguments to inner class to be used in the parametrized test
        private fun connectionSettingsArguments() = connectionSettingsArguments

        @Test
        fun `On retrieve ConnectionSettings error, emit UnableToCompleteFailure`() {
            every {
                connectionSettingsRepository.getConnectionSettings()
            } answers {
                flow {
                    throw dummyErrorFixture
                }
            }

            val result = runBlocking { sut.execute().first() }

            assert(result is RetrieveAllConnectionSettingsContract.Status.UnableToRetrieveSettingsFailure)

            verify(exactly = 1) {
                connectionSettingsRepository.getConnectionSettings()
            }

            verify(exactly = 0) {
                protocolSettingsRepository.getSettingsByProtocol(any())
                externalVpnSettingsGateway.fetchAvailableVpnPorts(any())
            }
        }

        @ParameterizedTest
        @MethodSource("connectionSettingsArguments")
        fun `On retrieve ProtocolSettings error, emit UnableToCompleteFailure`(
            protocolSettings: ProtocolSettings,
            connectionSettings: ConnectionSettings,
            portList: List<Int>
        ) {
            every {
                connectionSettingsRepository.getConnectionSettings()
            } answers {
                flowOf(connectionSettings)
            }

            every {
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
            } answers {
                flow {
                    throw dummyErrorFixture
                }
            }

            every {
                externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
            } answers {
                flowOf(portList)
            }

            val result = runBlocking { sut.execute().first() }

            assert(result is RetrieveAllConnectionSettingsContract.Status.UnableToRetrieveSettingsFailure)

            verify(exactly = 1) {
                connectionSettingsRepository.getConnectionSettings()
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
            }

            verify(exactly = 0) {
                externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
            }
        }


    }
}