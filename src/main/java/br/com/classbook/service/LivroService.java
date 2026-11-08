package br.com.classbook.service;

import java.time.Year;
import java.util.List;

import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.model.Categoria;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.Livro;
import br.com.classbook.repository.AutorRepository;
import br.com.classbook.repository.CategoriaRepository;
import br.com.classbook.repository.DisciplinaRepository;
import br.com.classbook.repository.LivroRepository;

@Service
public class LivroService {

	private final LivroRepository livroRepository;

	private final AutorRepository autorRepository;

	private final CategoriaRepository categoriaRepository;

	private final DisciplinaRepository disciplinaRepository;

	private final MescladorDePatch mesclador;

	public LivroService(LivroRepository livroRepository, AutorRepository autorRepository,
			CategoriaRepository categoriaRepository, DisciplinaRepository disciplinaRepository,
			MescladorDePatch mesclador) {
		this.livroRepository = livroRepository;
		this.autorRepository = autorRepository;
		this.categoriaRepository = categoriaRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.mesclador = mesclador;
	}

	@Transactional
	public LivroResponse criar(LivroRequest request) {
		if (livroRepository.existsByIsbn(request.isbn())) {
			throw new ConflitoException("Já existe um livro com o ISBN " + request.isbn() + ".");
		}
		Livro livro = new Livro();
		preencher(livro, request);
		return LivroResponse.de(livroRepository.save(livro));
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listar(String titulo, String busca, Long autorId, Long categoriaId) {
		// "" (nunca null) para o PostgreSQL conseguir tipar o lower(concat(...)) do repository.
		String tituloFiltro = titulo == null || titulo.isBlank() ? "" : semCuringas(titulo);
		String buscaFiltro = busca == null || busca.isBlank() ? "" : semCuringas(busca.strip());
		List<Livro> livros = livroRepository.pesquisar(tituloFiltro, buscaFiltro, autorId, categoriaId,
				Sort.by("titulo"));
		return livros.stream().map(LivroResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public LivroResponse buscarPorId(Long id) {
		return LivroResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public LivroResponse atualizar(Long id, LivroRequest request) {
		return gravar(buscarOuLancar(id), request);
	}

	/**
	 * Só os campos presentes em {@code alteracoes} mudam; as regras são as do {@link #atualizar}. Um
	 * {@code null} em {@code categoriaId} ou {@code disciplinaId} tira o livro da categoria ou da disciplina.
	 */
	@Transactional
	public LivroResponse atualizarParcialmente(Long id, JsonNode alteracoes) {
		Livro livro = buscarOuLancar(id);
		return gravar(livro, mesclador.mesclar(comoRequest(livro), alteracoes, LivroRequest.class));
	}

	@Transactional
	public void excluir(Long id) {
		Livro livro = buscarOuLancar(id);
		livroRepository.delete(livro);
	}

	/**
	 * Escapa os curingas do {@code LIKE} ({@code %} e {@code _}) e o próprio caractere de escape, para o
	 * título ser procurado como texto: "100%" acha "100% Jovem", e não todos os livros.
	 */
	private static String semCuringas(String titulo) {
		return titulo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private Livro buscarOuLancar(Long id) {
		return livroRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + id + " não encontrado."));
	}

	private LivroResponse gravar(Livro livro, LivroRequest request) {
		if (livroRepository.existsByIsbnAndIdNot(request.isbn(), livro.getId())) {
			throw new ConflitoException("Já existe um livro com o ISBN " + request.isbn() + ".");
		}
		preencher(livro, request);
		return LivroResponse.de(livroRepository.save(livro));
	}

	/** O estado atual do livro no formato do {@code PUT}, base da mesclagem do {@code PATCH}. */
	private static LivroRequest comoRequest(Livro livro) {
		Categoria categoria = livro.getCategoria();
		Disciplina disciplina = livro.getDisciplina();
		return new LivroRequest(livro.getTitulo(), livro.getIsbn(), livro.getAnoPublicacao(), livro.getDescricao(),
				livro.getEditora(), livro.getAutor().getId(), categoria == null ? null : categoria.getId(),
				disciplina == null ? null : disciplina.getId());
	}

	private void preencher(Livro livro, LivroRequest request) {
		// O @Past não serve para um Integer: o "não futuro" do ano é conferido aqui (docs/MODELAGEM.md).
		if (request.anoPublicacao() != null && request.anoPublicacao() > Year.now().getValue()) {
			throw new DadosInvalidosException("O ano de publicação " + request.anoPublicacao() + " está no futuro.");
		}
		Autor autor = autorRepository.findById(request.autorId())
				.orElseThrow(() -> new DadosInvalidosException("Autor " + request.autorId() + " não existe."));
		Categoria categoria = null;
		if (request.categoriaId() != null) {
			categoria = categoriaRepository.findById(request.categoriaId()).orElseThrow(
					() -> new DadosInvalidosException("Categoria " + request.categoriaId() + " não existe."));
		}
		Disciplina disciplina = null;
		if (request.disciplinaId() != null) {
			disciplina = disciplinaRepository.findById(request.disciplinaId()).orElseThrow(
					() -> new DadosInvalidosException("Disciplina " + request.disciplinaId() + " não existe."));
		}
		livro.setTitulo(request.titulo());
		livro.setIsbn(request.isbn());
		livro.setAnoPublicacao(request.anoPublicacao());
		livro.setDescricao(request.descricao());
		livro.setEditora(request.editora());
		livro.setAutor(autor);
		livro.setCategoria(categoria);
		livro.setDisciplina(disciplina);
	}

}
