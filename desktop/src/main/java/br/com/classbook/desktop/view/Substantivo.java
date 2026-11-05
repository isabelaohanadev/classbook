package br.com.classbook.desktop.view;

/**
 * Nome de um recurso na interface, com o gênero, para os textos saírem certos: "Autor cadastrado.",
 * "Categoria cadastrada.", "Nenhuma disciplina cadastrada." (Issues #58 e #61).
 */
record Substantivo(String singular, boolean feminino) {

	String cadastrado() {
		return comMaiuscula() + (feminino ? " cadastrada." : " cadastrado.");
	}

	String atualizado() {
		return comMaiuscula() + (feminino ? " atualizada." : " atualizado.");
	}

	String excluido() {
		return comMaiuscula() + (feminino ? " excluída." : " excluído.");
	}

	String nenhumCadastrado() {
		return (feminino ? "Nenhuma " : "Nenhum ") + singular + (feminino ? " cadastrada." : " cadastrado.");
	}

	String tituloDoCadastro() {
		return (feminino ? "Nova " : "Novo ") + singular;
	}

	String tituloDaEdicao() {
		return "Editar " + singular;
	}

	String perguntaDeExclusao(String descricao) {
		return "Excluir " + (feminino ? "a " : "o ") + singular + " \"" + descricao + "\"?";
	}

	private String comMaiuscula() {
		return Character.toUpperCase(singular.charAt(0)) + singular.substring(1);
	}

}
