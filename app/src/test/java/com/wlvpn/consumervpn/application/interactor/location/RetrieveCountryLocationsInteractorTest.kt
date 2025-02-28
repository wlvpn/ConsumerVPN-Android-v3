package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RetrieveCountryLocationsInteractorTest {

    private val serverGatewayMock: ExternalServersGateway = mockk(relaxed = false)
    private val connectionSettingsRepository: ConnectionSettingsRepository = mockk(relaxed = false)


    private lateinit var sut: RetrieveCountryLocationsInteractor

    @BeforeAll
    fun init() {
        sut = RetrieveCountryLocationsInteractor(
            serverGatewayMock,
            connectionSettingsRepository
        )
    }

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Test
    fun `on Success retrieve expected country locations and selected target`(
    ) {
        every {
            serverGatewayMock.retrieveCountryLocations()
        }.returns(flowOf(emptyList()))

        every {
            serverGatewayMock.retrieveCityLocations()
        }.returns(flowOf(emptyList()))


        every {
            connectionSettingsRepository.getConnectionSettings()
        }.returns(flowOf(ConnectionSettings()))

        val result = runBlocking {  sut.execute().first() }
        assert(result is RetrieveCountryLocationsContract.Status.Success)

        verify(exactly = 1) {
            serverGatewayMock.retrieveCountryLocations()
            serverGatewayMock.retrieveCityLocations()
            connectionSettingsRepository.getConnectionSettings()
        }
    }

    @DisplayName("given on error")
    @Nested
    inner class GivenOnError {

        @Test
        fun `on error retrieving country locations emit UnableToRetrieveCountryLocations`() {
            every {
                serverGatewayMock.retrieveCountryLocations()
            }.returns(flow {throw ExternalServersGateway.UnableToRetrieveCountryServersLocationsFailure() })

            every {
                serverGatewayMock.retrieveCityLocations()
            }.returns(flowOf(emptyList()))

            every {
                connectionSettingsRepository.getConnectionSettings()
            }.returns(flowOf(ConnectionSettings()))


            val result = runBlocking { sut.execute().first() }
            assert( result  is RetrieveCountryLocationsContract.Status.UnableToRetrieveCountryLocationsFailure)

            verify(exactly = 1) {
                serverGatewayMock.retrieveCountryLocations()
                serverGatewayMock.retrieveCityLocations()
                connectionSettingsRepository.getConnectionSettings()
            }
        }
    }
}