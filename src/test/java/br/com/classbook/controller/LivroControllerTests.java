package br.com.classbook.controller;

import static br.com.classbook.Json.texto;
import static br.com.classbook.Unico.codigo;
import static br.com.classbook.Unico.nome;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Year;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.AutorRequest;
import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.LivroRequest;
import br.com.classbook.model.SerieEscolar;
import br.com.classbook.model.TipoCategoria;

/**
 * Confere as rotas de {@code /api/livros} de ponta a ponta, com o contexto Spring completo e o
 * PostgreSQL de teste. A transação é desfeita ao final de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LivroControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void cadastroValidoRespondeCreatedComLocationEOAutorResumido() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
				.andExpect(jsonPath("$.autor.id").value(autorId))
				.andExpect(jsonPath("$.autor.nome").value("Machado de Assis"));
	}

	@Test
	void cadastroComAutorIdAusenteRespondeBadRequest() throws Exception {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, null, null, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComAnoDePublicacaoNoFuturoRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Livro do Futuro", "9780306406157", Year.now().getValue() + 1, null,
				null, autorId, null, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComAutorInexistenteRespondeBadRequest() throws Exception {
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, -1L, null, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cadastroComIsbnJaCadastradoRespondeConflict() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "978-0-306-40615-7", null, null, null, autorId, null, null));
		LivroRequest duplicado = new LivroRequest("Outro Título", "9780306406157", null, null, null, autorId, null, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(duplicado)))
				.andExpect(status().isConflict());
	}

	@Test
	void listagemRespondeOkComLivrosOrdenadosPorTitulo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null, null));
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"))
				.andExpect(jsonPath("$[1].titulo").value("Memórias Póstumas"));
	}

	@Test
	void pesquisaPorTituloRetornaCorrespondentesIgnorandoCaixa() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null, null));
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780316000000", null, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros").param("titulo", "casmurro"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
	}

	@Test
	void pesquisaSemCorrespondenciaRespondeOkComListaVazia() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros").param("titulo", "inexistente"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void pesquisaPorAutorIdRetornaSomenteOsLivrosDesseAutor() throws Exception {
		Long machado = cadastrarAutor("Machado de Assis");
		Long alencar = cadastrarAutor("José de Alencar");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, machado, null, null));
		cadastrarLivro(new LivroRequest("Senhora", "9780316000000", null, null, null, alencar, null, null));

		mockMvc.perform(get("/api/livros").param("autorId", String.valueOf(machado)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
	}

	@Test
	void pesquisaPorAutorIdInexistenteRespondeOkComListaVazia() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros").param("autorId", "-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void pesquisaComAutorIdQueNaoEhNumeroRespondeBadRequest() throws Exception {
		mockMvc.perform(get("/api/livros").param("autorId", "abc")).andExpect(status().isBadRequest());
	}

	@Test
	void pesquisaPorCategoriaIdRetornaSomenteOsLivrosDessaCategoria() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long literatura = cadastrarCategoria(nome("Literatura"));
		Long referencia = cadastrarCategoria(nome("Referência"));
		cadastrarLivro(
				new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, literatura, null));
		cadastrarLivro(new LivroRequest("Dicionário", "9780316000000", null, null, null, autorId, referencia, null));

		mockMvc.perform(get("/api/livros").param("categoriaId", String.valueOf(literatura)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
	}

	@Test
	void pesquisaComCategoriaIdQueNaoEhNumeroRespondeBadRequest() throws Exception {
		mockMvc.perform(get("/api/livros").param("categoriaId", "abc")).andExpect(status().isBadRequest());
	}

	@Test
	void pesquisaComTituloAutorIdECategoriaIdCombinadosRespeitaTodosOsFiltros() throws Exception {
		Long machado = cadastrarAutor("Machado de Assis");
		Long alencar = cadastrarAutor("José de Alencar");
		Long literatura = cadastrarCategoria(nome("Literatura"));
		cadastrarLivro(
				new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, machado, literatura, null));
		cadastrarLivro(
				new LivroRequest("Dom Sebastião", "9780316000000", null, null, null, alencar, literatura, null));
		cadastrarLivro(
				new LivroRequest("Memórias Póstumas", "9780321000000", null, null, null, machado, null, null));

		mockMvc.perform(get("/api/livros").param("titulo", "dom").param("autorId", String.valueOf(machado))
				.param("categoriaId", String.valueOf(literatura)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
	}

	@Test
	void listagemSemLivrosRespondeOkComListaVazia() throws Exception {
		mockMvc.perform(get("/api/livros"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void consultaPorIdExistenteRespondeOkComOLivro() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Dom Casmurro"));
	}

	@Test
	void consultaPorIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(get("/api/livros/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void edicaoValidaRespondeOkComOLivroAtualizado() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título Antigo", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título Novo", "9780316000000", 2020, null, null, autorId, null, null);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Título Novo"))
				.andExpect(jsonPath("$.isbn").value("9780316000000"));
	}

	@Test
	void edicaoComIdInexistenteRespondeNotFound() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null);

		mockMvc.perform(put("/api/livros/{id}", -1).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isNotFound());
	}

	@Test
	void edicaoComIsbnDeOutroLivroRespondeConflict() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));
		Long id = cadastrarLivro(
				new LivroRequest("Memórias Póstumas", "9780316000000", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Memórias Póstumas", "9780306406157", null, null, null, autorId, null, null);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isConflict());
	}

	@Test
	void edicaoComDadosInvalidosRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("   ", "9780306406157", null, null, null, autorId, null, null);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void exclusaoDeLivroExistenteRespondeNoContent() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		mockMvc.perform(delete("/api/livros/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/livros/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoComIdInexistenteRespondeNotFound() throws Exception {
		mockMvc.perform(delete("/api/livros/{id}", -1)).andExpect(status().isNotFound());
	}

	@Test
	void exclusaoDeLivroMantemOAutor() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long livroId = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		mockMvc.perform(delete("/api/livros/{id}", livroId)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/autores/{id}", autorId)).andExpect(status().isOk());
	}

	@Test
	void cadastroComCategoriaValidaRespondeCreatedComACategoriaResumida() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId,
				categoriaId, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.categoria.id").value(categoriaId))
				.andExpect(jsonPath("$.categoria.nome").value(nome("Literatura")));
	}

	@Test
	void cadastroComCategoriaInexistenteRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, -1L, null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void edicaoSemCategoriaIdRemoveACategoriaDoLivro() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		Long id = cadastrarLivro(
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoria").value(nullValue()));
	}

	@Test
	void cadastroSemDisciplinaRespondeCreatedComADisciplinaNula() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null,
				null);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.disciplina").value(nullValue()));
	}

	@Test
	void cadastroComDisciplinaValidaRespondeCreatedComADisciplinaResumidaComCodigo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long disciplinaId = cadastrarDisciplina("Língua Portuguesa", codigo("LP-1"));
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null,
				disciplinaId);

		MvcResult resultado = mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.disciplina.id").value(disciplinaId))
				.andExpect(jsonPath("$.disciplina.nome").value("Língua Portuguesa"))
				.andExpect(jsonPath("$.disciplina.codigo").value(codigo("LP-1")))
				.andReturn();

		Long id = corpoComoJson(resultado).get("id").asLong();
		mockMvc.perform(get("/api/livros/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.disciplina.id").value(disciplinaId));
	}

	@Test
	void cadastroComDisciplinaInexistenteRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		LivroRequest request = new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null,
				-1L);

		mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void edicaoSemDisciplinaIdRemoveADisciplinaDoLivro() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long disciplinaId = cadastrarDisciplina("Língua Portuguesa", codigo("LP-1"));
		Long id = cadastrarLivro(
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, disciplinaId));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.disciplina").value(nullValue()));
	}

	@Test
	void edicaoComDisciplinaInexistenteRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(
				new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		LivroRequest edicao = new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, -1L);

		mockMvc.perform(put("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicao)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listagemTrazADisciplinaDeCadaLivro() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long disciplinaId = cadastrarDisciplina("Língua Portuguesa", codigo("LP-1"));
		cadastrarLivro(new LivroRequest("A com disciplina", "9780306406157", null, null, null, autorId, null,
				disciplinaId));
		cadastrarLivro(new LivroRequest("B sem disciplina", "0306406152", null, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros").param("autorId", String.valueOf(autorId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].disciplina.id").value(disciplinaId))
				.andExpect(jsonPath("$[1].disciplina").value(nullValue()));
	}

	@Test
	void buscaLivrePeloNomeDoAutorEPeloNomeDaCategoriaRespondeOsLivrosCorrespondentes() throws Exception {
		Long autorId = cadastrarAutor(nome("Autora Busca"));
		Long outroAutor = cadastrarAutor(nome("Outro Autor"));
		Long categoriaId = cadastrarCategoria(nome("Categoria Busca"));
		cadastrarLivro(new LivroRequest("A primeira obra", "9780306406157", 1899, null, null, autorId, categoriaId, null));
		cadastrarLivro(new LivroRequest("B segunda obra", "9780316000000", null, null, null, autorId, null, null));
		cadastrarLivro(new LivroRequest("C obra alheia", "9780321000000", 1899, null, null, outroAutor, null, null));

		// O sufixo de nome() é único por execução, então só os livros deste teste podem bater.
		mockMvc.perform(get("/api/livros").param("busca", nome("autora busca")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].titulo").value("A primeira obra"))
				.andExpect(jsonPath("$[1].titulo").value("B segunda obra"));
		mockMvc.perform(get("/api/livros").param("busca", nome("categoria busca")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("A primeira obra"));
	}

	@Test
	void buscaLivrePeloAnoCombinaComOFiltroDeAutorEIgnoraBuscaEmBranco() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", 1899, null, null, autorId, null, null));
		cadastrarLivro(new LivroRequest("Memórias Póstumas", "9780316000000", 1881, null, null, autorId, null, null));

		mockMvc.perform(get("/api/livros").param("busca", "1899").param("autorId", String.valueOf(autorId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
		mockMvc.perform(get("/api/livros").param("busca", "   ").param("autorId", String.valueOf(autorId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
		mockMvc.perform(get("/api/livros").param("busca", "2050").param("autorId", String.valueOf(autorId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void edicaoParcialAlteraSoOsCamposEnviados() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		Long disciplinaId = cadastrarDisciplina("Língua Portuguesa", codigo("LP-1"));
		Long id = cadastrarLivro(new LivroRequest("Título Antigo", "9780306406157", 1899, "Resumo", "Editora A",
				autorId, categoriaId, disciplinaId));

		editarParcialmente(id, "{'titulo': 'Título Novo'}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.titulo").value("Título Novo"))
				.andExpect(jsonPath("$.isbn").value("9780306406157"))
				.andExpect(jsonPath("$.anoPublicacao").value(1899))
				.andExpect(jsonPath("$.descricao").value("Resumo"))
				.andExpect(jsonPath("$.editora").value("Editora A"))
				.andExpect(jsonPath("$.autor.id").value(autorId))
				.andExpect(jsonPath("$.categoria.id").value(categoriaId))
				.andExpect(jsonPath("$.disciplina.id").value(disciplinaId));
		mockMvc.perform(get("/api/livros/{id}", id))
				.andExpect(jsonPath("$.titulo").value("Título Novo"))
				.andExpect(jsonPath("$.categoria.id").value(categoriaId));
	}

	@Test
	void edicaoParcialComCategoriaIdNullTiraOLivroDaCategoriaEMantemADisciplina() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		Long disciplinaId = cadastrarDisciplina("Língua Portuguesa", codigo("LP-1"));
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId,
				disciplinaId));

		editarParcialmente(id, "{'categoriaId': null}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoria").value(nullValue()))
				.andExpect(jsonPath("$.disciplina.id").value(disciplinaId))
				.andExpect(jsonPath("$.titulo").value("Título"));
	}

	@Test
	void edicaoParcialComDisciplinaIdNullTiraOLivroDaDisciplinaEMantemACategoria() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		Long disciplinaId = cadastrarDisciplina("Língua Portuguesa", codigo("LP-1"));
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId,
				disciplinaId));

		editarParcialmente(id, "{'disciplinaId': null}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.disciplina").value(nullValue()))
				.andExpect(jsonPath("$.categoria.id").value(categoriaId));
	}

	@Test
	void edicaoParcialQueTiraACategoriaLiberaAExclusaoDela() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId,
				null));
		mockMvc.perform(delete("/api/categorias/{id}", categoriaId)).andExpect(status().isConflict());

		editarParcialmente(id, "{'categoriaId': null}").andExpect(status().isOk());

		mockMvc.perform(delete("/api/categorias/{id}", categoriaId)).andExpect(status().isNoContent());
	}

	@Test
	void edicaoParcialTrocaACategoriaEOAutor() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long outroAutorId = cadastrarAutor("Clarice Lispector");
		Long categoriaId = cadastrarCategoria(nome("Literatura"));
		Long outraCategoriaId = cadastrarCategoria(nome("Referência"));
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, categoriaId,
				null));

		editarParcialmente(id, "{'autorId': " + outroAutorId + ", 'categoriaId': " + outraCategoriaId + "}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.autor.id").value(outroAutorId))
				.andExpect(jsonPath("$.autor.nome").value("Clarice Lispector"))
				.andExpect(jsonPath("$.categoria.id").value(outraCategoriaId));
	}

	@Test
	void edicaoParcialAceitaOTipoDeConteudoMergePatchJson() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		mockMvc.perform(patch("/api/livros/{id}", id).contentType("application/merge-patch+json")
				.content(texto("{'editora': 'Nova Editora'}")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.editora").value("Nova Editora"));
	}

	@Test
	void edicaoParcialNormalizaOIsbnComoOCadastro() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'isbn': '978-0-316-00000-0'}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isbn").value("9780316000000"));
	}

	@Test
	void edicaoParcialMantendoOProprioIsbnRespondeOk() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'isbn': '978-0-306-40615-7', 'titulo': 'Outro título'}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isbn").value("9780306406157"))
				.andExpect(jsonPath("$.titulo").value("Outro título"));
	}

	@Test
	void edicaoParcialComIsbnDeOutroLivroRespondeConflict() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		cadastrarLivro(new LivroRequest("Dom Casmurro", "9780306406157", null, null, null, autorId, null, null));
		Long id = cadastrarLivro(
				new LivroRequest("Memórias Póstumas", "9780316000000", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'isbn': '9780306406157'}").andExpect(status().isConflict());
		mockMvc.perform(get("/api/livros/{id}", id)).andExpect(jsonPath("$.isbn").value("9780316000000"));
	}

	@Test
	void edicaoParcialComIsbnForaDoFormatoRespondeBadRequestApontandoOCampo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'isbn': '123'}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("isbn"));
	}

	@Test
	void edicaoParcialComAutorNullRespondeBadRequestApontandoOCampo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'autorId': null}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("autorId"));
	}

	@Test
	void edicaoParcialComAutorCategoriaOuDisciplinaInexistenteRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'autorId': -1}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Autor -1 não existe."));
		editarParcialmente(id, "{'categoriaId': -1}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Categoria -1 não existe."));
		editarParcialmente(id, "{'disciplinaId': -1}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Disciplina -1 não existe."));
	}

	@Test
	void edicaoParcialComAnoNoFuturoRespondeBadRequest() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));
		int anoFuturo = Year.now().getValue() + 1;

		editarParcialmente(id, "{'anoPublicacao': " + anoFuturo + "}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("O ano de publicação " + anoFuturo + " está no futuro."));
	}

	@Test
	void edicaoParcialComAnoNullLimpaOAno() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", 1899, null, null, autorId, null, null));

		editarParcialmente(id, "{'anoPublicacao': null}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.anoPublicacao").value(nullValue()));
	}

	@Test
	void edicaoParcialComAnoQueNaoEhNumeroRespondeBadRequestApontandoOCampo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", 1899, null, null, autorId, null, null));

		editarParcialmente(id, "{'anoPublicacao': 'abc'}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("anoPublicacao"));
		mockMvc.perform(get("/api/livros/{id}", id)).andExpect(jsonPath("$.anoPublicacao").value(1899));
	}

	@Test
	void edicaoParcialComTituloMaiorQueAColunaRespondeBadRequestApontandoOCampo() throws Exception {
		Long autorId = cadastrarAutor("Machado de Assis");
		Long id = cadastrarLivro(new LivroRequest("Título", "9780306406157", null, null, null, autorId, null, null));

		editarParcialmente(id, "{'titulo': '" + "a".repeat(201) + "'}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("titulo"));
	}

	@Test
	void edicaoParcialComIdInexistenteRespondeNotFound() throws Exception {
		editarParcialmente(-1L, "{'titulo': 'Novo título'}").andExpect(status().isNotFound());
	}

	private ResultActions editarParcialmente(Long id, String json) throws Exception {
		return mockMvc.perform(patch("/api/livros/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(texto(json)));
	}

	private Long cadastrarDisciplina(String nome, String codigo) throws Exception {
		MvcResult resultado = mockMvc.perform(post("/api/disciplinas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new DisciplinaRequest(nome, codigo, null, SerieEscolar.PRIMEIRA_SERIE))))
				.andReturn();
		return corpoComoJson(resultado).get("id").asLong();
	}

	private Long cadastrarAutor(String nome) throws Exception {
		MvcResult resultado = mockMvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AutorRequest(nome, null, null, null))))
				.andReturn();
		return corpoComoJson(resultado).get("id").asLong();
	}

	private Long cadastrarCategoria(String nome) throws Exception {
		MvcResult resultado = mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper
						.writeValueAsString(new CategoriaRequest(nome, null, null, TipoCategoria.LITERATURA))))
				.andReturn();
		return corpoComoJson(resultado).get("id").asLong();
	}

	private Long cadastrarLivro(LivroRequest request) throws Exception {
		MvcResult resultado = mockMvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andReturn();
		return corpoComoJson(resultado).get("id").asLong();
	}

	private JsonNode corpoComoJson(MvcResult resultado) throws Exception {
		return objectMapper.readTree(resultado.getResponse().getContentAsString());
	}

}
