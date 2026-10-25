/**
 * Controllers REST, um por recurso ({@code XxxController}), mapeados em {@code /api/<recurso>}.
 * Recebem e devolvem só DTOs, validam o corpo com {@code @Valid} e delegam ao service,
 * sem regra de negócio, sem acesso a repositórios e sem montar respostas de erro.
 */
package br.com.classbook.controller;
