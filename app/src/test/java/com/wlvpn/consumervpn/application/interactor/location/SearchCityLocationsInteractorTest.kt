package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

private const val DEFAULT_SEARCH_TERM = "any"


private val DEFAULT_CITY_LIST = listOf(
    ServerLocation.City(name = "Dallas", country = ServerLocation.Country(code = "us")),
    ServerLocation.City(name = "Mexico City", country = ServerLocation.Country(code = "mx")),
    ServerLocation.City(name = "Toronto", country = ServerLocation.Country(code = "ca")),
    ServerLocation.City(name = "Sao Paulo", country = ServerLocation.Country(code = "br")),
    ServerLocation.City(name = "London", country = ServerLocation.Country(code = "uk")),
)

private val DEFAULT_COUNTRY_LIST = listOf(
    ServerLocation.Country(name = "Italy", code = "it"),
    ServerLocation.Country(name = "Spain", code = "es"),
    ServerLocation.Country(name = "Japan", code = "jp"),
    ServerLocation.Country(name = "Mexico", code = "mx"),
    ServerLocation.Country(name = "United Kingdom", code = "uk")
)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SearchCityLocationsInteractorTest {

    private val serverGatewayMock: ExternalServersGateway = mockk(relaxed = false)
    private val connectionSettingsRepository: ConnectionSettingsRepository = mockk(relaxed = false)

    private lateinit var sut: SearchCityLocationsContract.Interactor

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()

        // We recreate the interactor for each test to reset its cache
        sut = SearchCityLocationsInteractor(
            serverGatewayMock,
            connectionSettingsRepository
        )
    }

    @DisplayName("given on success")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GivenOnSuccess {

        fun successTestArguments() = Stream.of(
            Arguments.of("", SearchCityLocationsContract.Status.EmptySearchTerm),
            Arguments.of(" ", SearchCityLocationsContract.Status.EmptySearchTerm),
            Arguments.of(
                "a",
                SearchCityLocationsContract.Status.SearchResults(
                    listOf(
                        ServerLocation.City(
                            name = "Dallas",
                            country = ServerLocation.Country(code = "us")
                        ),
                        ServerLocation.City(
                            name = "Sao Paulo",
                            country = ServerLocation.Country(code = "br")
                        ),
                        ), savedTarget = ConnectionTarget.City()),
                    ),
            Arguments.of(
                "me",
                SearchCityLocationsContract.Status.SearchResults(
                    listOf(
                        ServerLocation.City(
                            name = "Mexico City",
                            country = ServerLocation.Country(code = "mx")
                        ),
                        ), savedTarget = ConnectionTarget.City()
                    ),
                ),
            Arguments.of("Bosnia", SearchCityLocationsContract.Status.NoSearchResults)
        )

        @ParameterizedTest
        @MethodSource("successTestArguments")
        fun `on Success retrieve expected city locations and selected server`(
            searchTerm: String,
            expectedStatus: SearchCityLocationsContract.Status
        ) {

            every {
                serverGatewayMock.retrieveCityLocations()
            }.returns(flowOf(DEFAULT_CITY_LIST))

            every {
                serverGatewayMock.retrieveCountryLocations()
            }.returns(flowOf(DEFAULT_COUNTRY_LIST))

            every {
                connectionSettingsRepository.getConnectionSettings()
            }.returns(flowOf(ConnectionSettings()))

            val status = runBlocking { sut.execute(searchTerm).first() }
            assert(
                when (status) {
                    is SearchCityLocationsContract.Status.EmptySearchTerm ->
                        expectedStatus is SearchCityLocationsContract.Status.EmptySearchTerm
                    is SearchCityLocationsContract.Status.NoSearchResults ->
                        expectedStatus is SearchCityLocationsContract.Status.NoSearchResults
                    is SearchCityLocationsContract.Status.SearchResults ->
                        (expectedStatus is SearchCityLocationsContract.Status.SearchResults &&
                                expectedStatus.locationList == status.locationList)
                    else -> false
                }
            )

            when (status) {
                is SearchCityLocationsContract.Status.EmptySearchTerm -> verify(exactly = 0) {
                    serverGatewayMock.retrieveCityLocations()
                    serverGatewayMock.retrieveCountryLocations()
                    connectionSettingsRepository.getConnectionSettings()
                }
                else -> verify(exactly = 1) {
                    serverGatewayMock.retrieveCityLocations()
                    serverGatewayMock.retrieveCountryLocations()
                    connectionSettingsRepository.getConnectionSettings()
                }
            }
        }
    }

    @DisplayName("given on error")
    @Nested
    inner class GivenOnError {

        @Test
        fun `on error retrieving city locations emit UnableToRetrieveLocationsFailure`() {
            every {
                serverGatewayMock.retrieveCityLocations()
            }.returns(flow { throw ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure() })

            every {
                serverGatewayMock.retrieveCountryLocations()
            }.returns(flowOf(DEFAULT_COUNTRY_LIST))

            every {
                connectionSettingsRepository.getConnectionSettings()
            }.returns(flowOf(ConnectionSettings()))

            val status = runBlocking { sut.execute(DEFAULT_SEARCH_TERM).first() }
            assert(status is SearchCityLocationsContract.Status.UnableToRetrieveLocationsFailure)

            verify(exactly = 1) {
                serverGatewayMock.retrieveCityLocations()
                serverGatewayMock.retrieveCountryLocations()
                connectionSettingsRepository.getConnectionSettings()
            }
        }
    }
}