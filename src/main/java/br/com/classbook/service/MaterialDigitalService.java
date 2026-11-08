package br.com.classbook.service;

import java.util.List;

import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.MaterialDigitalRequest;
import br.com.classbook.dto.MaterialDigitalResponse;
import br.com.classbook.exception.DadosInvalidosException;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Disciplina;
import br.com.classbook.model.MaterialDigital;
import br.com.classbook.repository.DisciplinaRepository;
import br.com.classbook.repository.MaterialDigitalRepository;

@Service
public class MaterialDigitalService {

	private final MaterialDigitalRepository materialDigitalRepository;

	private final DisciplinaRepository disciplinaRepository;

	private final MescladorDePatch mesclador;

	public MaterialDigitalService(MaterialDigitalRepository materialDigitalRepository,
			DisciplinaRepository disciplinaRepository, MescladorDePatch mesclador) {
		this.materialDigitalRepository = materialDigitalRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.mesclador = mesclador;
	}

	@Transactional
	public MaterialDigitalResponse criar(MaterialDigitalRequest request) {
		MaterialDigital material = new MaterialDigital();
		preencher(material, request);
		return MaterialDigitalResponse.de(materialDigitalRepository.save(material));
	}

	@Transactional(readOnly = true)
	public List<MaterialDigitalResponse> listar() {
		return materialDigitalRepository.findAll(Sort.by("titulo")).stream().map(MaterialDigitalResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public MaterialDigitalResponse buscarPorId(Long id) {
		return MaterialDigitalResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public MaterialDigitalResponse atualizar(Long id, MaterialDigitalRequest request) {
		return gravar(buscarOuLancar(id), request);
	}

	/** Só os campos presentes em {@code alteracoes} mudam; as regras são as do {@link #atualizar}. */
	@Transactional
	public MaterialDigitalResponse atualizarParcialmente(Long id, JsonNode alteracoes) {
		MaterialDigital material = buscarOuLancar(id);
		return gravar(material, mesclador.mesclar(comoRequest(material), alteracoes, MaterialDigitalRequest.class));
	}

	@Transactional
	public void excluir(Long id) {
		MaterialDigital material = buscarOuLancar(id);
		materialDigitalRepository.delete(material);
	}

	private MaterialDigital buscarOuLancar(Long id) {
		return materialDigitalRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Material " + id + " não encontrado."));
	}

	private MaterialDigitalResponse gravar(MaterialDigital material, MaterialDigitalRequest request) {
		preencher(material, request);
		return MaterialDigitalResponse.de(materialDigitalRepository.save(material));
	}

	/** O estado atual do material no formato do {@code PUT}, base da mesclagem do {@code PATCH}. */
	private static MaterialDigitalRequest comoRequest(MaterialDigital material) {
		return new MaterialDigitalRequest(material.getTitulo(), material.getFormato(), material.getUrl(),
				material.getDescricao(), material.getDataPublicacao(), material.getDisciplina().getId());
	}

	private void preencher(MaterialDigital material, MaterialDigitalRequest request) {
		Disciplina disciplina = disciplinaRepository.findById(request.disciplinaId())
				.orElseThrow(() -> new DadosInvalidosException(
						"Disciplina " + request.disciplinaId() + " não existe."));
		material.setTitulo(request.titulo());
		material.setFormato(request.formato());
		material.setUrl(request.url());
		material.setDescricao(request.descricao());
		material.setDataPublicacao(request.dataPublicacao());
		material.setDisciplina(disciplina);
	}

}
