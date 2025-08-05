package org.desviante.exception;

// Uma exceção de runtime customizada para erros na nossa camada de serviço da API do Google.
public class GoogleApiServiceException extends RuntimeException {
  public GoogleApiServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}