package com.cordestitch.service.serviceimplementation.faqs;

import com.cordestitch.entity.faqs.FaqsEntity;
import com.cordestitch.exception.faqs.FaqsAlreadyExistException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.repository.faqs.FaqsRepository;
import com.cordestitch.request.faqs.FaqsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.faqs.FaqsResponse;
import com.cordestitch.service.service.faqs.FaqsService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqsServiceImplementation implements FaqsService {

    private final Generator generator;

    private final FaqsRepository faqsRepository;

    @Override
    public SuccessResponse addFaqs(FaqsRequest faqsRequest) {
        log.info("Faqs Request: {}", faqsRequest);

        FaqsEntity entity = faqsRepository.findByQuestionAndAnswer(faqsRequest.getQuestion(), faqsRequest.getAnswer());
        log.info(Constants.FAQS_ENTITY_DATA, entity);
        if (!isNull(entity)) {
            log.info(Constants.FAQS_ALREADY_EXIST);
            throw new FaqsAlreadyExistException(Constants.FAQS_ALREADY_EXIST);
        }

        FaqsEntity faqsEntity = new FaqsEntity();
        faqsEntity.setFaqsId(generator.generateId(Constants.FAQS_ID));
        faqsEntity.setQuestion(faqsRequest.getQuestion());
        faqsEntity.setAnswer(faqsRequest.getAnswer());
        faqsRepository.save(faqsEntity);

        SuccessResponse response =  new SuccessResponse(Constants.FAQS_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
        log.info("Faqs Response : {}", response);
        return response;
    }

    @Override
    public List<FaqsResponse> getAllFaqs() {

        List<FaqsEntity> faqsEntities = faqsRepository.findAll();
        log.info("List Of Faqs Data : {}", faqsEntities);

        return faqsEntities.stream()
                .map(faqsEntity -> {
                    FaqsResponse faqsResponse = new FaqsResponse();
                    faqsResponse.setFaqsId(faqsEntity.getFaqsId());
                    faqsResponse.setQuestion(faqsEntity.getQuestion());
                    faqsResponse.setAnswer(faqsEntity.getAnswer());
                    return faqsResponse;
                })
                .toList();
    }

    @Override
    public SuccessResponse updateFaqs(FaqsRequest faqsRequest) {
        log.info("Update Faqs Request: {}", faqsRequest);

        FaqsEntity entity = faqsRepository.findByFaqsId(faqsRequest.getFaqsId());
        log.info(Constants.FAQS_ENTITY_DATA, entity);
        if (isNull(entity)) {
            log.info(Constants.FAQS_NOT_FOUND);
            throw new ResourceNotFoundException(Constants.FAQS_NOT_FOUND);
        }

        entity.setQuestion(faqsRequest.getQuestion());
        entity.setAnswer(faqsRequest.getAnswer());
        faqsRepository.save(entity);

        SuccessResponse response =  new SuccessResponse(Constants.FAQS_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
        log.info("Update Faqs Response : {}", response);
        return response;
    }

    @Override
    public SuccessResponse deleteFaqs(FaqsRequest faqsRequest) {
        log.info("Delete Faqs Request: {}", faqsRequest);

        FaqsEntity entity = faqsRepository.findByQuestionAndAnswer(faqsRequest.getQuestion(), faqsRequest.getAnswer());
        log.info(Constants.FAQS_ENTITY_DATA, entity);
        if (isNull(entity)) {
            log.info(Constants.FAQS_NOT_FOUND);
            throw new ResourceNotFoundException(Constants.FAQS_NOT_FOUND);
        }

        faqsRepository.delete(entity);

        SuccessResponse response =  new SuccessResponse(Constants.FAQS_DELETED_SUCCESSFULLY, HttpStatus.OK.value());
        log.info("Delete Faqs Response : {}", response);
        return response;
    }
}
