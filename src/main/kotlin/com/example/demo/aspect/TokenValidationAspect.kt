//package com.example.demo.aspect
//
//import com.example.demo.repository.HHOAuthRepository
//import org.aspectj.lang.annotation.Aspect
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.annotation.Around
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.stereotype.Component
//
//@Aspect
//@Component
//class TokenValidationAspect(
//        private val hhOAuthRepository: HHOAuthRepository
//) {
//    @Around("execution(* com.example.demo.controller.*.*(.., @org.springframework.web.bind.annotation.RequestHeader (*), ..)) && !within(com.example.demo.controller.SignController)")
//    fun validateToken(joinPoint: ProceedingJoinPoint): Any {
//        val args = joinPoint.args
//
//        val tokenIndex = args.indexOfFirst { it is String && it.startsWith("Bearer ") }
//
//        if (tokenIndex == -1) {
//            return ResponseEntity("Invalid or missing token", HttpStatus.UNAUTHORIZED)
//        }
//
//        val token = args[tokenIndex] as String
//
//        if (token.split(" ").size != 2) {
//            return ResponseEntity("Invalid or missing token", HttpStatus.UNAUTHORIZED)
//        }
//
//        val newToken = token.split(" ")[1]
//        args[tokenIndex] = newToken
//
//        if(hhOAuthRepository.findByToken(newToken) == null){
//            return ResponseEntity("Invalid or missing token", HttpStatus.UNAUTHORIZED)
//        }
//
//        return joinPoint.proceed(args)
//    }
//}
