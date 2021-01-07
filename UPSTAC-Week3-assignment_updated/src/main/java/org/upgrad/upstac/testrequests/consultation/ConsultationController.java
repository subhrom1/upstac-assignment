package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);
    @Autowired
    TestRequestFlowService testRequestFlowService;
    @Autowired
    private TestRequestUpdateService testRequestUpdateService;
    @Autowired
    private TestRequestQueryService testRequestQueryService;
    @Autowired
    private UserLoggedInService userLoggedInService;


    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations() {

        // Calls method to find list of all LAB_TEST_COMPLETED TestRequests.
        return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor() {

        //Fetches the current logged in user (doctor)
        User doctor = userLoggedInService.getLoggedInUser();

        //Returns the List of TestRequests assigned to current doctor
        return testRequestQueryService.findByDoctor(doctor);
    }


    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {

        try {
            //Fetches the current logged in user (doctor)
            User doctor = userLoggedInService.getLoggedInUser();

            //Assigns test request with given id to current doctor for consultation
            TestRequest assignedTestReq = testRequestUpdateService.assignForConsultation(id, doctor);

            //returns the assigned TestRequest
            return assignedTestReq;

        } catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id, @RequestBody CreateConsultationRequest testResult) {

        try {
            //Fetches the current logged in user (doctor)
            User doctor = userLoggedInService.getLoggedInUser();

            //Updates consultation with request body (CreateConsultationRequest object)
            TestRequest updatedTestReq = testRequestUpdateService.updateConsultation(id, testResult, doctor);

            //returns the updated TestRequest
            return updatedTestReq;

        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        } catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }


}
