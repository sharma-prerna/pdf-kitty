-- PDF Toolkit initial schema

CREATE TABLE processing_job (
    id                    UUID PRIMARY KEY,
    operation_type        VARCHAR(32)  NOT NULL,
    status                VARCHAR(16)  NOT NULL,
    original_file_name    VARCHAR(512),
    output_file_name      VARCHAR(512),
    error_message         VARCHAR(2000),
    parameters            VARCHAR(2000),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processing_started_at TIMESTAMPTZ,
    completed_at          TIMESTAMPTZ
);

CREATE INDEX idx_processing_job_status     ON processing_job (status);
CREATE INDEX idx_processing_job_created_at ON processing_job (created_at DESC);

CREATE TABLE uploaded_file (
    id          UUID PRIMARY KEY,
    job_id      UUID         NOT NULL REFERENCES processing_job (id) ON DELETE CASCADE,
    file_name   VARCHAR(512) NOT NULL,
    file_path   VARCHAR(1024) NOT NULL,
    file_size   BIGINT       NOT NULL,
    mime_type   VARCHAR(255),
    sort_order  INT          NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_uploaded_file_job ON uploaded_file (job_id);

CREATE TABLE output_file (
    id          UUID PRIMARY KEY,
    job_id      UUID         NOT NULL REFERENCES processing_job (id) ON DELETE CASCADE,
    file_name   VARCHAR(512) NOT NULL,
    file_path   VARCHAR(1024) NOT NULL,
    file_size   BIGINT       NOT NULL,
    mime_type   VARCHAR(255),
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_output_file_job ON output_file (job_id);
