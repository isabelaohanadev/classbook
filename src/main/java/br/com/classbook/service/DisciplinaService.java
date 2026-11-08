package br.com.classbook.service;

import java.util.List;

import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.DisciplinaResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Disciplina;
import br.com.classbook.repository.DisciplinaRepository;
import br.com.classbook.repository.LivroRepository;
import br.com.classbook.repository.MaterialDigitalRepository;

@Service
public class DisciplinaService {

	private final DisciplinaRepository disciplinaRepository;

	private final MaterialDigitalRepository materialDigitalRepository;

	private final LivroRepository livroRepository;

	private final MescladorDePatch mesclador;

	public DisciplinaService(DisciplinaRepository disciplinaRepository,
			MaterialDigitalRepository materialDigitalRepository, LivroRepository livroRepository,
			MescladorDePatch mesclador) {
		this.disciplinaRepository = disciplinaRepository;
		this.materialDigitalRepository = materialDigitalRepository;
		this.livroRepository = livroRepository;
		this.mesclador = mesclador;
	}

	@Transactional
	public DisciplinaResponse criar(DisciplinaRequest request) {
		if (disciplinaRepository.existsByCodigo(request.codigo())) {
			throw new ConflitoException("Já existe uma disciplina com o código " + request.codigo() + ".");
		}
		Disciplina disciplina = new Disciplina();
		preencher(disciplina, request);
		return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
	}

	@Transactional(readOnly = true)
	public List<DisciplinaResponse> listar() {
		return disciplinaRepository.findAll(Sort.by("codigo")).stream().map(DisciplinaResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public DisciplinaResponse buscarPorId(Long id) {
		return DisciplinaResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public DisciplinaResponse atualizar(Long id, DisciplinaRequest request) {
		return gravar(buscarOuLancar(id), request);
	}

	/** Só os campos presentes em {@code alteracoes} mudam; as regras são as do {@link #atualizar}. */
	@Transactional
	public DisciplinaResponse atualizarParcialmente(Long id, JsonNode alteracoes) {
		Disciplina disciplina = buscarOuLancar(id);
		return gravar(disciplina, mesclador.mesclar(comoRequest(disciplina), alteracoes, DisciplinaRequest.class));
	}

	@Transactional
	public void excluir(Long id) {
		Disciplina disciplina = buscarOuLancar(id);
		if (materialDigitalRepository.existsByDisciplinaId(id)) {
			throw new ConflitoException("Disciplina " + id + " possui materiais vinculados.");
		}
		if (livroRepository.existsByDisciplinaId(id)) {
			throw new ConflitoException("Disciplina " + id + " possui livros vinculados.");
		}
		disciplinaRepository.delete(disciplina);
	}

	private Disciplina buscarOuLancar(Long id) {
		return disciplinaRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina " + id + " não encontrada."));
	}

	private DisciplinaResponse gravar(Disciplina disciplina, DisciplinaRequest request) {
		if (disciplinaRepository.existsByCodigoAndIdNot(request.codigo(), disciplina.getId())) {
			throw new ConflitoException("Já existe uma disciplina com o código " + request.codigo() + ".");
		}
		preencher(disciplina, request);
		return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
	}

	/** O estado atual da disciplina no formato do {@code PUT}, base da mesclagem do {@code PATCH}. */
	private static DisciplinaRequest comoRequest(Disciplina disciplina) {
		return new DisciplinaRequest(disciplina.getNome(), disciplina.getCodigo(), disciplina.getDescricao(),
				disciplina.getSerieEscolar());
	}

	private void preencher(Disciplina disciplina, DisciplinaRequest request) {
		disciplina.setNome(request.nome());
		disciplina.setCodigo(request.codigo());
		disciplina.setDescricao(request.descricao());
		disciplina.setSerieEscolar(request.serieEscolar());
	}

}
