package br.com.classbook.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.CategoriaResponse;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Categoria;
import br.com.classbook.repository.CategoriaRepository;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	@Transactional
	public CategoriaResponse criar(CategoriaRequest request) {
		Categoria categoria = new Categoria();
		preencher(categoria, request);
		return CategoriaResponse.de(categoriaRepository.save(categoria));
	}

	@Transactional(readOnly = true)
	public List<CategoriaResponse> listar() {
		return categoriaRepository.findAll(Sort.by("nome")).stream().map(CategoriaResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public CategoriaResponse buscarPorId(Long id) {
		return CategoriaResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
		Categoria categoria = buscarOuLancar(id);
		preencher(categoria, request);
		return CategoriaResponse.de(categoriaRepository.save(categoria));
	}

	@Transactional
	public void excluir(Long id) {
		Categoria categoria = buscarOuLancar(id);
		categoriaRepository.delete(categoria);
	}

	private Categoria buscarOuLancar(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada."));
	}

	private void preencher(Categoria categoria, CategoriaRequest request) {
		categoria.setNome(request.nome());
		categoria.setDescricao(request.descricao());
		categoria.setFaixaEtaria(request.faixaEtaria());
		categoria.setTipo(request.tipo());
	}

}
