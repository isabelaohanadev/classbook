package br.com.classbook.service;

import java.util.List;

import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.CategoriaRequest;
import br.com.classbook.dto.CategoriaResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Categoria;
import br.com.classbook.repository.CategoriaRepository;
import br.com.classbook.repository.LivroRepository;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	private final LivroRepository livroRepository;

	private final MescladorDePatch mesclador;

	public CategoriaService(CategoriaRepository categoriaRepository, LivroRepository livroRepository,
			MescladorDePatch mesclador) {
		this.categoriaRepository = categoriaRepository;
		this.livroRepository = livroRepository;
		this.mesclador = mesclador;
	}

	@Transactional
	public CategoriaResponse criar(CategoriaRequest request) {
		if (categoriaRepository.existsByNomeIgnoreCase(request.nome())) {
			throw new ConflitoException("Já existe uma categoria com o nome " + request.nome() + ".");
		}
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
		return gravar(buscarOuLancar(id), request);
	}

	/** Só os campos presentes em {@code alteracoes} mudam; as regras são as do {@link #atualizar}. */
	@Transactional
	public CategoriaResponse atualizarParcialmente(Long id, JsonNode alteracoes) {
		Categoria categoria = buscarOuLancar(id);
		return gravar(categoria, mesclador.mesclar(comoRequest(categoria), alteracoes, CategoriaRequest.class));
	}

	@Transactional
	public void excluir(Long id) {
		Categoria categoria = buscarOuLancar(id);
		if (livroRepository.existsByCategoriaId(id)) {
			throw new ConflitoException("Categoria " + id + " possui livros vinculados.");
		}
		categoriaRepository.delete(categoria);
	}

	private Categoria buscarOuLancar(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada."));
	}

	private CategoriaResponse gravar(Categoria categoria, CategoriaRequest request) {
		if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(request.nome(), categoria.getId())) {
			throw new ConflitoException("Já existe uma categoria com o nome " + request.nome() + ".");
		}
		preencher(categoria, request);
		return CategoriaResponse.de(categoriaRepository.save(categoria));
	}

	/** O estado atual da categoria no formato do {@code PUT}, base da mesclagem do {@code PATCH}. */
	private static CategoriaRequest comoRequest(Categoria categoria) {
		return new CategoriaRequest(categoria.getNome(), categoria.getDescricao(), categoria.getFaixaEtaria(),
				categoria.getTipo());
	}

	private void preencher(Categoria categoria, CategoriaRequest request) {
		categoria.setNome(request.nome());
		categoria.setDescricao(request.descricao());
		categoria.setFaixaEtaria(request.faixaEtaria());
		categoria.setTipo(request.tipo());
	}

}
