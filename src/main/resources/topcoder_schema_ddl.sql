ALTER TABLE oj.problem ADD problem_name varchar(255) NULL;
ALTER TABLE oj.problem ADD srm varchar(255) NULL;
ALTER TABLE oj.problem ADD "date" timestamp NULL;
ALTER TABLE oj.problem ADD writer varchar(255) NULL;
ALTER TABLE oj.problem ADD tags varchar(255) NULL;
ALTER TABLE oj.problem ADD div1_level varchar(255) NULL;
ALTER TABLE oj.problem ADD div1_success_rate varchar(255) NULL;
ALTER TABLE oj.problem ADD div2_level varchar(255) NULL;
ALTER TABLE oj.problem ADD div2_success_rate varchar(255) NULL;
ALTER TABLE oj.problem ADD problem_description text NULL;
ALTER TABLE oj.problem ADD link varchar(255) NULL;
ALTER TABLE oj.problem ADD id varchar(255) NULL;
ALTER TABLE oj.problem ADD "statement" text NULL;
ALTER TABLE oj.problem ADD definition text NULL;
ALTER TABLE oj.problem ADD notes text NULL;
ALTER TABLE oj.problem ADD "constraints" text NULL;
ALTER TABLE oj.problem ADD examples text NULL;
ALTER TABLE oj.problem ALTER COLUMN id SET NOT NULL;
ALTER TABLE oj.problem ADD CONSTRAINT problem_unique UNIQUE (id);

CREATE TABLE oj.submission (
	id varchar(255) NOT NULL,
	task_id varchar(255) NOT NULL,
	sources text NULL
);
ALTER TABLE oj.submission ADD submit_time timestamp NULL;
ALTER TABLE oj.submission ADD author varchar(255) NULL;
ALTER TABLE oj.submission ADD CONSTRAINT problem_unique UNIQUE (id);

CREATE TABLE oj.test (
	id varchar(255) NULL,
	"input" text NULL,
	expected_output text NULL,
	task_id varchar(255) NULL,
	num int4 NULL
);
ALTER TABLE oj.test ADD CONSTRAINT problem_unique UNIQUE (id);
