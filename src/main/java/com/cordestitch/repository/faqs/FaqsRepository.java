package com.cordestitch.repository.faqs;

import com.cordestitch.entity.faqs.FaqsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqsRepository extends MongoRepository<FaqsEntity, String> {
    FaqsEntity findByQuestionAndAnswer(String question, String answer);
    FaqsEntity findByFaqsId(String faqsId);
}
