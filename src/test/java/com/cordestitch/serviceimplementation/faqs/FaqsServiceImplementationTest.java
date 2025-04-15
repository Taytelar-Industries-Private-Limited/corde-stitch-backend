package com.cordestitch.serviceimplementation.faqs;

import com.cordestitch.entity.faqs.FaqsEntity;
import com.cordestitch.exception.faqs.FaqsAlreadyExistException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.repository.faqs.FaqsRepository;
import com.cordestitch.request.faqs.FaqsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.faqs.FaqsResponse;
import com.cordestitch.service.serviceimplementation.faqs.FaqsServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FaqsServiceImplementationTest {
    @InjectMocks
    private FaqsServiceImplementation faqsServiceImplementation;

    @Mock
    private FaqsRepository faqsRepository;

    @Mock
    private Generator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(generator.generateId("UID123")).thenReturn("UID123");
    }

    @Test
    void addFaqs_Success() {
        FaqsRequest request = getFaqsRequest();
        when(faqsRepository.findByQuestionAndAnswer(any(),any())).thenReturn(null);
        SuccessResponse response = faqsServiceImplementation.addFaqs(request);
        assertEquals(Constants.FAQS_ADDED_SUCCESSFULLY,response.getMessage());
    }

    @Test
    void addFaqs_Faqs_Already_Exists_Exception() {
        FaqsRequest request = getFaqsRequest();
        when(faqsRepository.findByQuestionAndAnswer(any(),any())).thenReturn(getFaqsEntity());
        FaqsAlreadyExistException exception = assertThrows(FaqsAlreadyExistException.class, ()->faqsServiceImplementation.addFaqs(request));
        assertEquals(Constants.FAQS_ALREADY_EXIST,exception.getMessage());
    }

    @Test
    void getAllFaqs(){
        when(faqsRepository.findAll()).thenReturn(List.of(getFaqsEntity()));
        List<FaqsResponse> faqsEntities = faqsServiceImplementation.getAllFaqs();
        assertEquals(1, faqsEntities.size());
    }

    @Test
    void updateFaqs_Success(){
        FaqsRequest request = getFaqsRequest();
        when(faqsRepository.findByFaqsId(any())).thenReturn(getFaqsEntity());
        SuccessResponse response = faqsServiceImplementation.updateFaqs(request);
        assertEquals(Constants.FAQS_UPDATED_SUCCESSFULLY,response.getMessage());
    }

    @Test
    void updateFaqs_Faqs_Not_Found_Exception(){
        FaqsRequest request = getFaqsRequest();
        when(faqsRepository.findByFaqsId(any())).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->faqsServiceImplementation.updateFaqs(request));
        assertEquals(Constants.FAQS_NOT_FOUND,exception.getMessage());
    }

    @Test
    void deleteFaqs_Success(){
        FaqsRequest request = getFaqsRequest();
        when(faqsRepository.findByQuestionAndAnswer(any(),any())).thenReturn(getFaqsEntity());
        SuccessResponse response = faqsServiceImplementation.deleteFaqs(request);
        assertEquals(Constants.FAQS_DELETED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void deleteFaqs_Faqs_Not_Found_Exception(){
        FaqsRequest request = getFaqsRequest();
        when(faqsRepository.findByQuestionAndAnswer(any(),any())).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, ()->faqsServiceImplementation.deleteFaqs(request));
        assertEquals(Constants.FAQS_NOT_FOUND, exception.getMessage());
    }
    private FaqsEntity getFaqsEntity() {
        FaqsEntity faqsEntity = new FaqsEntity();
        faqsEntity.setFaqsId("Faq123");
        faqsEntity.setQuestion("what is best rating for pants?");
        faqsEntity.setAnswer("4 out of 5");
        return faqsEntity;
    }

    private FaqsRequest getFaqsRequest() {
        FaqsRequest faqsRequest = new FaqsRequest();
        faqsRequest.setFaqsId("UID123");
        faqsRequest.setQuestion("What is the best pant?");
        faqsRequest.setAnswer("It depends on your preferences.");
        return faqsRequest;
    }
}