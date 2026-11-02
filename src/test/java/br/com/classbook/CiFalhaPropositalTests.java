package br.com.classbook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Falha de propósito para comprovar que o CI reprova o PR (Issue #75). Branch descartável: não integrar. */
class CiFalhaPropositalTests {

	@Test
	void somaCorrigida() {
		assertThat(1 + 1).isEqualTo(2);
	}

}
