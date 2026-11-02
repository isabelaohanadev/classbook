package br.com.classbook.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.DisciplinaResponse;
import br.com.classbook.exception.ConflitoException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Disciplina;
import br.com.classbook.repository.DisciplinaRepository;
import br.com.classbook.repository.MaterialDigitalRepository;

@Service
public class DisciplinaService {

	private final DisciplinaRepository disciplinaRepository;

	private final MaterialDigitalRepository materialDigitalRepository;

	public DisciplinaService(DisciplinaRepository disciplinaRepository,
			MaterialDigitalRepository materialDigitalRepository) {
		this.disciplinaRepository = disciplinaRepository;
		this.materialDigitalRepository = materialDigitalRepository;
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
		Disciplina disciplina = buscarOuLancar(id);
		if (disciplinaRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
			throw new ConflitoException("Já existe uma disciplina com o código " + request.codigo() + ".");
		}
		preencher(disciplina, request);
		return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
	}

	@Transactional
	public void excluir(Long id) {
		Disciplina disciplina = buscarOuLancar(id);
		if (materialDigitalRepository.existsByDisciplinaId(id)) {
			throw new ConflitoException("Disciplina " + id + " possui materiais vinculados.");
		}
		disciplinaRepository.delete(disciplina);
	}

	private Disciplina buscarOuLancar(Long id) {
		return disciplinaRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina " + id + " não encontrada."));
	}

	private void preencher(Disciplina disciplina, DisciplinaRequest request) {
		disciplina.setNome(request.nome());
		disciplina.setCodigo(request.codigo());
		disciplina.setDescricao(request.descricao());
		disciplina.setSerieEscolar(request.serieEscolar());
	}

}
