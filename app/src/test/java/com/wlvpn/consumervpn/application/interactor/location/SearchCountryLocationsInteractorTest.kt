package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract.Status
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract.Status.EmptySearchTerm
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract.Status.NoSearchResults
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract.Status.SearchResults
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract.Status.UnableToRetrieveLocationsFailure
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

private val DEFAULT_COUNTRY_LIST = listOf(
    ServerLocation.Country(name = "United States", code = "us"),
    ServerLocation.Country(name = "Mexico", code = "mx"),
    ServerLocation.Country(name = "Canada", code = "ca"),
    ServerLocation.Country(name = "Brazil", code = "br"),
    ServerLocation.Country(name = "United Kingdom", code = "uk")
)

private val DEFAULT_CITY_LIST = listOf(
    ServerLocation.City(name = "Dallas", country = ServerLocation.Country(code = "us")),
    ServerLocation.City(name = "Mexico City", country = ServerLocation.Country(code = "mx")),
    ServerLocation.City(name = "Toronto", country = ServerLocation.Country(code = "ca")),
    ServerLocation.City(name = "Sao Paulo", country = ServerLocation.Country(code = "br")),
    ServerLocation.City(name = "London", country = ServerLocation.Country(code = "uk")),
)

private val dummyException = RuntimeException("Dummy exception")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SearchCountryLocationsInteractorTest {

    private val serverGatewayMock: ExternalServersGateway = mockk(relaxed = false)
    private val connectionSettingsRepository: ConnectionSettingsRepository = mockk(relaxed = false)
    private lateinit var sut: SearchCountryLocationsContract.Interactor

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()

        // We recreate the interactor for each test to reset its cache
        sut = SearchCountryLocationsInteractor(
            serverGatewayMock,
            connectionSettingsRepository
        )
    }

    @DisplayName("given on success")
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GivenOnSuccess {

        fun successTestArguments() = Stream.of(
            Arguments.of("", EmptySearchTerm),
            Arguments.of(" ", EmptySearchTerm),
            Arguments.of(
                "a",
                SearchResults(
                    listOf(
                        ServerLocation.Country(name = "United States", code = "us",
                            cities = listOf(    ServerLocation.City(name = "Dallas", country = ServerLocation.Country(code = "us")),
                            )),
                        ServerLocation.Country(name = "Canada", code = "ca", cities = listOf(
                            ServerLocation.City(name = "Toronto", country = ServerLocation.Country(code = "ca")),
                            )),
                        ServerLocation.Country(name = "Brazil", code = "br", cities = listOf(
                            ServerLocation.City(name = "Sao Paulo", country = ServerLocation.Country(code = "br")),
                            )),
                    ), savedTarget = ConnectionTarget.Country(),
                )
            ),
            Arguments.of(
                "me",
                SearchResults(
                    listOf(
                        ServerLocation.Country(name = "Mexico", code = "mx", cities = listOf(
                            ServerLocation.City(name = "Mexico City", country = ServerLocation.Country(code = "mx")),

                            )
                        )
                    ),savedTarget = ConnectionTarget.Country()
                )
            ),
            Arguments.of(
                "UnIteD", SearchResults(
                    listOf(
                        ServerLocation.Country(name = "United States", code = "us", cities =
                        listOf(ServerLocation.City(name = "Dallas", country = ServerLocation.Country(code = "us")),
                        )),
                        ServerLocation.Country(name = "United Kingdom", code = "uk", cities =
                        listOf(    ServerLocation.City(name = "London", country = ServerLocation.Country(code = "uk")),
                        )
                        )
                    ),savedTarget = ConnectionTarget.Country()
                )
            ),

            Arguments.of("Bosnia", NoSearchResults),


        )

        @ParameterizedTest
        @MethodSource("successTestArguments")
        fun `on Success retrieve expected country locations and selected server`(
            searchTerm: String,
            expectedStatus: Status
        ) {
            every {
                serverGatewayMock.retrieveCountryLocations()
            }.returns(flowOf(DEFAULT_COUNTRY_LIST))

            every {
                serverGatewayMock.retrieveCityLocations()
            }.returns(flowOf(DEFAULT_CITY_LIST))


            every {
                connectionSettingsRepository.getConnectionSettings()
            }.returns(flowOf(ConnectionSettings()))

            val status = runBlocking { sut.execute(searchTerm).first() }
            assert(
                when (status) {
                    is EmptySearchTerm ->
                        expectedStatus is EmptySearchTerm
                    is NoSearchResults ->
                        expectedStatus is NoSearchResults
                    is SearchResults ->
                        (expectedStatus is SearchResults &&
                                expectedStatus.locationList == status.locationList)
                    else -> false
                }
            )

            when (status) {
                is EmptySearchTerm -> verify(exactly = 0) {
                    serverGatewayMock.retrieveCountryLocations()
                    serverGatewayMock.retrieveCityLocations()
                    connectionSettingsRepository.getConnectionSettings()
                }
                else -> verify(exactly = 1) {
                    serverGatewayMock.retrieveCountryLocations()
                    serverGatewayMock.retrieveCityLocations()
                    connectionSettingsRepository.getConnectionSettings()
                }
            }
        }
    }

    @DisplayName("given on error")
    @Nested
    inner class GivenOnError {

        @Test
        fun `on error retrieving country locations emit UnableToRetrieveLocationsFailure`() {
            every {
                serverGatewayMock.retrieveCountryLocations()
            }.returns(flow {throw ExternalServersGateway.UnableToRetrieveCountryServersLocationsFailure() })
            
            every {
                serverGatewayMock.retrieveCityLocations()
            }.returns(flowOf(DEFAULT_CITY_LIST))

            every {
                connectionSettingsRepository.getConnectionSettings()
            }.returns(flowOf(ConnectionSettings()))

            val status = runBlocking { sut.execute(DEFAULT_SEARCH_TERM).first() }
            assert(status is UnableToRetrieveLocationsFailure)

            verify(exactly = 1) {
                serverGatewayMock.retrieveCountryLocations()
                serverGatewayMock.retrieveCityLocations()
                connectionSettingsRepository.getConnectionSettings()
            }
        }

        @Test
        fun `on error retrieving city locations emit UnableToRetrieveLocationsFailure`() {
            every {
                serverGatewayMock.retrieveCountryLocations()
            }.returns(flowOf(DEFAULT_COUNTRY_LIST))


            every {
                serverGatewayMock.retrieveCityLocations()
            }.returns( flow { throw ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure() } )

            every {
                connectionSettingsRepository.getConnectionSettings()
            }.returns(flowOf(ConnectionSettings()))

            val status = runBlocking { sut.execute(DEFAULT_SEARCH_TERM).first() }
            assert(status is UnableToRetrieveLocationsFailure)

            verify(exactly = 1) {
                serverGatewayMock.retrieveCountryLocations()
                serverGatewayMock.retrieveCityLocations()
                connectionSettingsRepository.getConnectionSettings()
            }
        }
    }
}