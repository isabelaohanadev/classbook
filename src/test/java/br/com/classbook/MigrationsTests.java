package br.com.classbook;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Confere que o Flyway aplicou, com sucesso e sem pendências, todas as migrations de {@code db/migration}
 * (Issue #49). No CI o banco é criado vazio a cada execução, então este teste, junto com a subida do
 * contexto (que roda o {@code ddl-auto=validate}), mostra que o schema sobe do zero com todas as entidades.
 */
@SpringBootTest
class MigrationsTests {

	@Autowired
	private Flyway flyway;

	@Test
	void todasAsMigrationsDoProjetoForamAplicadasComSucesso() {
		MigrationInfo[] migrations = flyway.info().all();

		// Pendente, com falha ou aplicada sem o arquivo correspondente: qualquer estado além de SUCCESS reprova.
		assertThat(migrations).isNotEmpty().allSatisfy(migration -> assertThat(migration.getState())
				.as("%s (%s)", migration.getScript(), migration.getVersion()).isEqualTo(MigrationState.SUCCESS));
	}

}
