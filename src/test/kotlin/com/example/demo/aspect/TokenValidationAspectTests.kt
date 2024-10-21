package com.example.demo.aspect


import com.example.demo.controller.MonitoringController
import com.example.demo.controller.OAuthController
import com.example.demo.entity.HHOAuth
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.service.OAuthService
import com.example.demo.service.ResumeService
import com.example.demo.service.VacancyService
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@WebMvcTest(MonitoringController::class)
@EnableAspectJAutoProxy
class TokenValidationAspectTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    private val vacancyService: VacancyService? = null

    @MockBean
    private val oauthService: OAuthService? = null

    @MockBean
    private val resumeService: ResumeService? = null

    @MockBean
    private val oauthController: OAuthController? = null

    @MockBean
    private val hhOAuthRepository: HHOAuthRepository? = null

    @SpyBean
    lateinit var tokenValidationAspect: TokenValidationAspect

    @Test
    fun `invalid token`() {
        mockMvc.perform(get("/monitoring/start").header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("Invalid or missing token"))
    }

    @Test
    fun `invalid authorization`() {
        mockMvc.perform(get("/monitoring/start").header("Authorization", "Bearer token test"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("Invalid or missing token"))
    }

    @Test
    fun `valid token`() {
        val joinPoint = mock(ProceedingJoinPoint::class.java)
        val token = "Bearer test_token"
        `when`(joinPoint.args).thenReturn(arrayOf(token))
        val mockRepository = mock(HHOAuthRepository::class.java)
        `when`(mockRepository.findByToken("test_token")).thenReturn(HHOAuth())
        `when`(joinPoint.proceed(any())).thenReturn(ResponseEntity.ok("Success"))

        val aspect = TokenValidationAspect(mockRepository)
        val result = aspect.validateToken(joinPoint)

        verify(joinPoint).proceed(arrayOf("test_token"))
        assertEquals(ResponseEntity.ok("Success"), result)
    }
}