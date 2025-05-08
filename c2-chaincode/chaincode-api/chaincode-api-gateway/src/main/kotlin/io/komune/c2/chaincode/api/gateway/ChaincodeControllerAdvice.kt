package io.komune.c2.chaincode.api.gateway

import io.komune.c2.chaincode.dsl.invoke.InvokeException
import io.komune.c2.chaincode.api.gateway.chaincode.model.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ChaincodeControllerAdvice {

	@ExceptionHandler(InvokeException::class)
	fun handleException(invokeException: InvokeException): ResponseEntity<ErrorResponse> {
		val error = ErrorResponse(invokeException.message)
		return ResponseEntity(error, HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(Exception::class)
	fun handleException(invokeException: Exception): ResponseEntity<ErrorResponse> {
		val error = ErrorResponse(invokeException.message ?: "Unknown error")
		return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
	}

	@ExceptionHandler(Throwable::class)
	fun handleThrowable(invokeException: Throwable): ResponseEntity<ErrorResponse> {
		val error = ErrorResponse(invokeException.message ?: "Unknown error")
		return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
	}
}
