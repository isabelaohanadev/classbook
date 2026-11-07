-- Liga o livro à disciplina (docs/MODELAGEM.md, relação Livro -> Disciplina).
-- A disciplina é opcional: livros já cadastrados continuam válidos, sem disciplina. Como nas demais
-- chaves estrangeiras, excluir uma disciplina que ainda tem livros é bloqueado (ON DELETE RESTRICT).

ALTER TABLE livro ADD COLUMN disciplina_id BIGINT;

ALTER TABLE livro
    ADD CONSTRAINT fk_livro_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplina (id) ON DELETE RESTRICT;

-- O PostgreSQL não cria índice para chave estrangeira. Este atende à verificação de "registro em uso"
-- antes de excluir uma disciplina.
CREATE INDEX idx_livro_disciplina_id ON livro (disciplina_id);
