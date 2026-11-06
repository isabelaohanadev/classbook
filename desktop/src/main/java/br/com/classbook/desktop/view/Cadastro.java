package br.com.classbook.desktop.view;

import java.util.List;
import java.util.function.Function;

import br.com.classbook.desktop.api.ClienteApi;
import br.com.classbook.desktop.api.Recurso;
import br.com.classbook.desktop.modelo.AutorRequest;
import br.com.classbook.desktop.modelo.AutorResponse;
import br.com.classbook.desktop.modelo.CategoriaRequest;
import br.com.classbook.desktop.modelo.CategoriaResponse;
import br.com.classbook.desktop.modelo.DisciplinaRequest;
import br.com.classbook.desktop.modelo.DisciplinaResponse;
import br.com.classbook.desktop.modelo.LivroRequest;
import br.com.classbook.desktop.modelo.LivroResponse;
import br.com.classbook.desktop.modelo.MaterialDigitalRequest;
import br.com.classbook.desktop.modelo.MaterialDigitalResponse;

/**
 * O que muda de um recurso para outro na {@code tela-de-cadastro.fxml} e na {@link JanelaDeFormulario}: o
 * nome, a rota da API, as colunas da tabela e o FXML do formulário (Issue #58).
 */
record Cadastro<Q, R>(String titulo, Substantivo nome, Function<ClienteApi, Recurso<Q, R>> recurso,
		String formulario, Function<R, Long> id, Function<R, String> descricao, List<Coluna<R>> colunas) {

	record Coluna<R>(String titulo, Function<R, String> valor) {
	}

	static Cadastro<AutorRequest, AutorResponse> autores() {
		return new Cadastro<>("Autores", new Substantivo("autor", false), ClienteApi::autores,
				"formulario-autor.fxml", AutorResponse::id, AutorResponse::nome, List.of(
						new Coluna<>("Nome", AutorResponse::nome),
						new Coluna<>("Nacionalidade", AutorResponse::nacionalidade),
						new Coluna<>("Nascimento", autor -> Campos.texto(autor.dataNascimento()))));
	}

	static Cadastro<LivroRequest, LivroResponse> livros() {
		return new Cadastro<>("Catálogo de livros", new Substantivo("livro", false), ClienteApi::livros,
				"formulario-livro.fxml", LivroResponse::id, LivroResponse::titulo, List.of(
						new Coluna<>("Título", LivroResponse::titulo),
						new Coluna<>("Autor", livro -> livro.autor().nome()),
						new Coluna<>("Categoria", livro -> livro.categoria() == null ? "" : livro.categoria().nome()),
						new Coluna<>("Ano", livro -> Campos.texto(livro.anoPublicacao()))));
	}

	static Cadastro<CategoriaRequest, CategoriaResponse> categorias() {
		return new Cadastro<>("Categorias", new Substantivo("categoria", true), ClienteApi::categorias,
				"formulario-categoria.fxml", CategoriaResponse::id, CategoriaResponse::nome, List.of(
						new Coluna<>("Nome", CategoriaResponse::nome),
						new Coluna<>("Tipo", categoria -> categoria.tipo().rotulo()),
						new Coluna<>("Faixa etária", CategoriaResponse::faixaEtaria)));
	}

	static Cadastro<DisciplinaRequest, DisciplinaResponse> disciplinas() {
		return new Cadastro<>("Disciplinas", new Substantivo("disciplina", true), ClienteApi::disciplinas,
				"formulario-disciplina.fxml", DisciplinaResponse::id, DisciplinaResponse::nome, List.of(
						new Coluna<>("Código", DisciplinaResponse::codigo),
						new Coluna<>("Nome", DisciplinaResponse::nome),
						new Coluna<>("Série", disciplina -> disciplina.serieEscolar().rotulo())));
	}

	static Cadastro<MaterialDigitalRequest, MaterialDigitalResponse> materiais() {
		return new Cadastro<>("Materiais digitais", new Substantivo("material digital", false), ClienteApi::materiais,
				"formulario-material.fxml", MaterialDigitalResponse::id, MaterialDigitalResponse::titulo, List.of(
						new Coluna<>("Título", MaterialDigitalResponse::titulo),
						new Coluna<>("Formato", material -> material.formato().rotulo()),
						new Coluna<>("Disciplina", material -> material.disciplina().codigo()),
						new Coluna<>("URL", MaterialDigitalResponse::url)));
	}

}
