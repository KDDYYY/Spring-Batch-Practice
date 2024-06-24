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
    public Job importUserJob(Step step1, Step step2, JobExecutionListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
                .start(step1)
                .next(step2)
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
    public Step step2(Tasklet tasklet2) {
        return new StepBuilder("step2", jobRepository)
                .tasklet(tasklet2, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Tasklet tasklet1(PersonRepository personRepository) {
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
    public Tasklet tasklet2(PersonRepository personRepository) {
        return (contribution, chunkContext) -> {
            personRepository.save(new Person("Alice", "Johnson"));
            personRepository.save(new Person("Bob", "Brown"));

            personRepository.findAll().forEach(person ->
                    System.out.println("Processed: " + person.getFirstName() + " " + person.getLastName())
            );

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener();
    }
}