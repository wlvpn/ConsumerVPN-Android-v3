package com.wlvpn.consumervpn.application.interactor.login

import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.value.UserCredentials
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@ExperimentalCoroutinesApi
@FlowPreview
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LoginInteractorTest {

    // Mocks
    private val loginGateway: LoginGateway = mockk()
    private val externalServersGateway: ExternalServersGateway = mockk()
    // Fixtures
    private val validCredentials = UserCredentials(
        username = "username@mail.com",
        password = "password"
    )
    private val dummyException = RuntimeException("dummy exception")

    // SUT
    private val sut: LoginContract.Interactor =
        LoginInteractor(
            loginGateway,
            externalServersGateway
        )

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Test
    fun `emit success on successful login`() {

        every {
            loginGateway.login(validCredentials)
        } answers {
            flowOf(Unit)
        }

        every {
            externalServersGateway.updateServers()
        } answers {
            flowOf(Unit)
        }

        val result = runBlocking { sut.execute(validCredentials).first() }

        assert(result is LoginContract.Status.Success)

        verify(exactly = 1) {
            loginGateway.login(validCredentials)
        }
    }

    private fun configureLoginGatewayFailures() =
    listOf(
        Pair(LoginGateway.InvalidCredentialsFailure(),
            LoginContract.Status.InvalidCredentialsFailure),
        Pair(LoginGateway.UnexpectedFailure(message = ""),
            LoginContract.Status.UnableToLoginFailure()),
        Pair(LoginGateway.ConnectionFailure(), LoginContract.Status.ConnectionFailure),
        Pair(LoginGateway.InternalServerFailure(message = ""),
            LoginContract.Status.UnableToLoginFailure()),
        Pair(LoginGateway.NotAuthorizedFailure(), LoginContract.Status.NotAuthorizedFailure),
        Pair(LoginGateway.TooManyRequests(), LoginContract.Status.TooManyRequestsFailure)
    )

    @ParameterizedTest
    @MethodSource("configureLoginGatewayFailures")
    fun `emit invalid credentials failure`(pair: Pair<Exception, LoginContract.Status>) {

        every {
            loginGateway.login(any())
        } answers {
            flow {
                throw pair.first
            }
        }

        val result = runBlocking { sut.execute(validCredentials).first() }

        assert(result == pair.second)

        verify(exactly = 1) {
            loginGateway.login(validCredentials)
        }
    }
}