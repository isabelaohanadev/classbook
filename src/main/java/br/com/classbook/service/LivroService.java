package br.com.classbook.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.LivroRequest;
import br.com.classbook.dto.LivroResponse;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.model.Livro;
import br.com.classbook.repository.AutorRepository;
import br.com.classbook.repository.LivroRepository;

@Service
public class LivroService {

	private final LivroRepository livroRepository;

	private final AutorRepository autorRepository;

	public LivroService(LivroRepository livroRepository, AutorRepository autorRepository) {
		this.livroRepository = livroRepository;
		this.autorRepository = autorRepository;
	}

	@Transactional
	public LivroResponse criar(LivroRequest request) {
		Livro livro = new Livro();
		preencher(livro, request);
		return LivroResponse.de(livroRepository.save(livro));
	}

	@Transactional(readOnly = true)
	public List<LivroResponse> listar() {
		return livroRepository.findAll(Sort.by("titulo")).stream().map(LivroResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public LivroResponse buscarPorId(Long id) {
		return LivroResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public LivroResponse atualizar(Long id, LivroRequest request) {
		Livro livro = buscarOuLancar(id);
		preencher(livro, request);
		return LivroResponse.de(livroRepository.save(livro));
	}

	@Transactional
	public void excluir(Long id) {
		Livro livro = buscarOuLancar(id);
		livroRepository.delete(livro);
	}

	private Livro buscarOuLancar(Long id) {
		return livroRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + id + " não encontrado."));
	}

	private void preencher(Livro livro, LivroRequest request) {
		Autor autor = autorRepository.findById(request.autorId())
				.orElseThrow(() -> new DadosInvalidosException("Autor " + request.autorId() + " não existe."));
		livro.setTitulo(request.titulo());
		livro.setIsbn(request.isbn());
		livro.setAnoPublicacao(request.anoPublicacao());
		livro.setDescricao(request.descricao());
		livro.setEditora(request.editora());
		livro.setAutor(autor);
	}

}
