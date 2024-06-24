package com.study.batch.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job importUserJob(Step step1, JobExecutionListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
                .start(step1)
                .listener(listener)
                .build();
    }

    @Bean
    public Step step1(Tasklet tasklet) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(tasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Tasklet tasklet(PersonRepository personRepository) {
        return (contribution, chunkContext) -> {
            personRepository.save(new Person("John", "Doe"));
            personRepository.save(new Person("Jane", "Smith"));

            personRepository.findAll().forEach(person ->
                    System.out.println("Found: " + person.getFirstName() + " " + person.getLastName())
            );

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener();
    }
}