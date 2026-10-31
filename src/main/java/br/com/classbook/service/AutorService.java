package br.com.classbook.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.classbook.dto.AutorRequest;
import br.com.classbook.dto.AutorResponse;
import br.com.classbook.exception.RecursoNaoEncontradoException;
import br.com.classbook.model.Autor;
import br.com.classbook.repository.AutorRepository;

@Service
public class AutorService {

	private final AutorRepository autorRepository;

	public AutorService(AutorRepository autorRepository) {
		this.autorRepository = autorRepository;
	}

	@Transactional
	public AutorResponse criar(AutorRequest request) {
		Autor autor = new Autor();
		preencher(autor, request);
		return AutorResponse.de(autorRepository.save(autor));
	}

	@Transactional(readOnly = true)
	public List<AutorResponse> listar() {
		return autorRepository.findAll(Sort.by("nome")).stream().map(AutorResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public AutorResponse buscarPorId(Long id) {
		return AutorResponse.de(buscarOuLancar(id));
	}

	@Transactional
	public AutorResponse atualizar(Long id, AutorRequest request) {
		Autor autor = buscarOuLancar(id);
		preencher(autor, request);
		return AutorResponse.de(autorRepository.save(autor));
	}

	@Transactional
	public void excluir(Long id) {
		Autor autor = buscarOuLancar(id);
		autorRepository.delete(autor);
	}

	private Autor buscarOuLancar(Long id) {
		return autorRepository.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Autor " + id + " não encontrado."));
	}

	private void preencher(Autor autor, AutorRequest request) {
		autor.setNome(request.nome());
		autor.setNacionalidade(request.nacionalidade());
		autor.setDataNascimento(request.dataNascimento());
		autor.setBiografia(request.biografia());
	}

}
