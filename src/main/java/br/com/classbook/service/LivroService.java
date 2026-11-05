package br.com.classbook.service;

import java.time.Year;
import java.util.List;

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

	public LivroService(LivroRepository livroRepository, AutorRepository autorRepository,
			CategoriaRepository categoriaRepository, DisciplinaRepository disciplinaRepository) {
		this.livroRepository = livroRepository;
		this.autorRepository = autorRepository;
		this.categoriaRepository = categoriaRepository;
		this.disciplinaRepository = disciplinaRepository;
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
	public List<LivroResponse> listar(String titulo, Long autorId, Long categoriaId) {
		// "" (nunca null) para o PostgreSQL conseguir tipar o lower(concat(...)) do repository.
		String tituloFiltro = titulo == null || titulo.isBlank() ? "" : semCuringas(titulo);
		List<Livro> livros = livroRepository.pesquisar(tituloFiltro, autorId, categoriaId, Sort.by("titulo"));
		return livros.stream().map(LivroResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public LivroResponse buscarPorId(Long id) {
		return LivroResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public LivroResponse atualizar(Long id, LivroRequest request) {
		Livro livro = buscarOuLancar(id);
		if (livroRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
			throw new ConflitoException("Já existe um livro com o ISBN " + request.isbn() + ".");
		}
		preencher(livro, request);
		return LivroResponse.de(livroRepository.save(livro));
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
