package br.com.classbook.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.DisciplinaRequest;
import br.com.classbook.dto.DisciplinaResponse;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Disciplina;
import br.com.classbook.repository.DisciplinaRepository;

@Service
public class DisciplinaService {

	private final DisciplinaRepository disciplinaRepository;

	public DisciplinaService(DisciplinaRepository disciplinaRepository) {
		this.disciplinaRepository = disciplinaRepository;
	}

	@Transactional
	public DisciplinaResponse criar(DisciplinaRequest request) {
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
		preencher(disciplina, request);
		return DisciplinaResponse.de(disciplinaRepository.save(disciplina));
	}

	@Transactional
	public void excluir(Long id) {
		Disciplina disciplina = buscarOuLancar(id);
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
