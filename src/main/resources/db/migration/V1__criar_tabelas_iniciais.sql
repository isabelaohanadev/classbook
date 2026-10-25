-- Migration inicial: cria as cinco tabelas conforme docs/MODELAGEM.md (revisada nas Issues #3 e #4).
-- Depois de integrada na main, este arquivo não muda mais: alterações do schema entram em novas
-- migrations (ver "Migrations (Flyway)" no CONTRIBUTING.md).
--
-- As colunas de enum (categoria.tipo, disciplina.serie_escolar, material_digital.formato) não têm
-- CHECK: a lista de valores fica só no enum Java. As exclusões são bloqueadas (ON DELETE RESTRICT)
-- enquanto houver registros que apontam para o registro excluído.

CREATE TABLE autor (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    nacionalidade   VARCHAR(80),
    data_nascimento DATE,
    biografia       TEXT
);

CREATE TABLE categoria (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome         VARCHAR(100) NOT NULL,
    descricao    VARCHAR(500),
    faixa_etaria VARCHAR(30),
    tipo         VARCHAR(30)  NOT NULL
);

-- Nome único sem diferenciar maiúsculas de minúsculas: "Literatura" e "literatura" são a mesma categoria.
CREATE UNIQUE INDEX uk_categoria_nome ON categoria (LOWER(nome));

CREATE TABLE livro (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo         VARCHAR(200) NOT NULL,
    isbn           VARCHAR(13)  NOT NULL,
    ano_publicacao INTEGER,
    descricao      TEXT,
    editora        VARCHAR(120),
    autor_id       BIGINT       NOT NULL,
    categoria_id   BIGINT,
    CONSTRAINT uk_livro_isbn UNIQUE (isbn),
    CONSTRAINT fk_livro_autor FOREIGN KEY (autor_id) REFERENCES autor (id) ON DELETE RESTRICT,
    CONSTRAINT fk_livro_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE RESTRICT
);

CREATE TABLE disciplina (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    codigo        VARCHAR(20)  NOT NULL,
    descricao     VARCHAR(500),
    serie_escolar VARCHAR(30)  NOT NULL,
    CONSTRAINT uk_disciplina_codigo UNIQUE (codigo)
);

CREATE TABLE material_digital (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo          VARCHAR(200) NOT NULL,
    formato         VARCHAR(20)  NOT NULL,
    url             VARCHAR(500) NOT NULL,
    descricao       TEXT,
    data_publicacao DATE,
    disciplina_id   BIGINT       NOT NULL,
    CONSTRAINT fk_material_digital_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplina (id) ON DELETE RESTRICT
);

-- O PostgreSQL não cria índice para chave estrangeira. Estes atendem aos filtros da pesquisa de
-- livros (autorId, categoriaId) e à verificação de "registro em uso" antes de cada exclusão.
CREATE INDEX idx_livro_autor_id ON livro (autor_id);
CREATE INDEX idx_livro_categoria_id ON livro (categoria_id);
CREATE INDEX idx_material_digital_disciplina_id ON material_digital (disciplina_id);
