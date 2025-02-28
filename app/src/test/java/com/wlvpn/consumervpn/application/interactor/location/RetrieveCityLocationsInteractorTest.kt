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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RetrieveCityLocationsInteractorTest {

    private val serverGatewayMock: ExternalServersGateway = mockk(relaxed = false)
    private val connectionSettingsRepositoryMock: ConnectionSettingsRepository = mockk(relaxed = false)

    private val dummyConnectionSettingsFixture = ConnectionSettings()
    private val dummyExceptionFixture = RuntimeException()

    private lateinit var sut: RetrieveCityLocationsInteractor


    @BeforeAll
    fun init() {
        sut = RetrieveCityLocationsInteractor(
            serverGatewayMock,
            connectionSettingsRepositoryMock
        )
    }

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Test
    fun `on Success retrieve city locations`() {
        every {
            serverGatewayMock.retrieveCityLocations()
        }.returns(flowOf(listOf()))

        every {
            connectionSettingsRepositoryMock.getConnectionSettings()
        }.returns(flowOf(dummyConnectionSettingsFixture))

        val result = runBlocking { sut.execute().first() }

        assert(result is RetrieveCityLocationsContract.Status.Success)

        verify(exactly = 1) {
            serverGatewayMock.retrieveCityLocations()
        }
    }

    @Test
    fun `on error retrieving city locations emit UnableToRetrieveCityLocations`() {
        every {
            serverGatewayMock.retrieveCityLocations()
        }.returns( flow { throw ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure() })

        every {
            connectionSettingsRepositoryMock.getConnectionSettings()
        }.returns(flowOf(dummyConnectionSettingsFixture))

        val result = runBlocking { sut.execute().first() }
        assert(result is RetrieveCityLocationsContract.Status.UnableToRetrieveCityLocationsFailure)

        verify(exactly = 1) {
            serverGatewayMock.retrieveCityLocations()
        }
    }

    @Test
    fun `on error retrieving connection settings emit the error`() {
        every {
            serverGatewayMock.retrieveCityLocations()
        }.returns(flowOf(listOf()))

        every {
            connectionSettingsRepositoryMock.getConnectionSettings()
        }.returns( flow { throw dummyExceptionFixture} )

        assertThrows<RuntimeException> {
            runBlocking { sut.execute().first() }
        }

        verify(exactly = 1) {
            serverGatewayMock.retrieveCityLocations()
        }
    }
}