-- Spring Batch 6 renamed the job instance sequence; preserve its current value.
ALTER SEQUENCE batch_job_seq RENAME TO batch_job_instance_seq;
