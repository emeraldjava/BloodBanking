package net.bloodbanking.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.bloodbanking.constants.AppConstants;
import net.bloodbanking.constants.ErrorConstants;
import net.bloodbanking.dao.LoginDao;
import net.bloodbanking.dto.BloodGroupMstDTO;
import net.bloodbanking.dto.EnquiryFormDTO;
import net.bloodbanking.dto.FeedbackDTO;
import net.bloodbanking.dto.LocationAddressDTO;
import net.bloodbanking.dto.RegistrationDTO;
import net.bloodbanking.dto.SecurityQuestionDTO;
import net.bloodbanking.dto.StatusMstDTO;
import net.bloodbanking.dto.UserTypeMappingDTO;
import net.bloodbanking.dto.UserTypeMstDTO;
import net.bloodbanking.entity.BloodGroupMst;
import net.bloodbanking.entity.EnquiryForm;
import net.bloodbanking.entity.Feedback;
import net.bloodbanking.entity.LocationAddress;
import net.bloodbanking.entity.Registration;
import net.bloodbanking.entity.SecurityQuestion;
import net.bloodbanking.entity.StatusMst;
import net.bloodbanking.entity.UserTypeMapping;
import net.bloodbanking.entity.UserTypeMst;
import net.bloodbanking.enums.ReferenceTypeEnum;
import net.bloodbanking.exception.ApplicationException;
import net.bloodbanking.exception.ApplicationMessage;
import net.bloodbanking.service.LoginService;
import net.bloodbanking.utils.DateUtil;
import net.bloodbanking.validator.EnquiryValidator;
import net.bloodbanking.validator.FeedbackValidator;
import net.bloodbanking.validator.LoginValidator;

@Service("loginService")
public class LoginServiceImpl implements LoginService {
	
	@Autowired
	private LoginDao loginDao;
	
	@Autowired
	private LoginValidator loginValidator;
	
	@Autowired
	private EnquiryValidator enquiryValidator;
	
	@Autowired
	private FeedbackValidator feedbackValidator;
	
	@Override
	public RegistrationDTO loadRegistration(RegistrationDTO registrationDTO) throws ApplicationException{
		loginValidator.validateLoadRegistration(registrationDTO);
		Registration registration = loginDao.loadRegistration(registrationDTO);
		registrationDTO.setRegistrationId(registration.getRegistrationId());
		registrationDTO.setBloodGroup(registration.getBloodGroup());
		registrationDTO.setBirthDate(DateUtil.convertDateToDateStr(registration.getBirthDate(), DateUtil.DATE_FORMAT_dd_MM_yyyy_SEP_HIPHEN));
		registrationDTO.setGender(registration.getGender());
		registrationDTO.setUserName(registration.getUserName());
		registrationDTO.setPassword(registration.getPassword());
		registrationDTO.setUsertypeId(registration.getUsertypeId());
		registrationDTO.setSecurityQue(registration.getSecurityQue());
		registrationDTO.setAnswer(registration.getAnswer());
		
		StatusMstDTO statusMstDTO = new StatusMstDTO();
		statusMstDTO.setStatus(registration.getStatusMst().getStatus());
		statusMstDTO.setDescription(registration.getStatusMst().getDescription());
		registrationDTO.setStatusMstDTO(statusMstDTO);
		
		LocationAddressDTO locationAddressDTO = new LocationAddressDTO();
		locationAddressDTO.setReferenceId(registrationDTO.getRegistrationId().toString());
		locationAddressDTO.setReferenceType(ReferenceTypeEnum.USER.getCode());
		registrationDTO.setLocationAddressDTO(this.loadLocationAddress(locationAddressDTO));
		
		UserTypeMstDTO userTypeMstDTO = new UserTypeMstDTO();
		userTypeMstDTO.setUsertypeId(registrationDTO.getUsertypeId().intValue());
		userTypeMstDTO = this.loadUserType(userTypeMstDTO);
		registrationDTO.setUsertypeName(userTypeMstDTO.getUsertypeName());
		
		BloodGroupMstDTO bloodGroupMstDTO = new BloodGroupMstDTO();
		bloodGroupMstDTO.setBloodGroupId(registrationDTO.getBloodGroup());
		bloodGroupMstDTO = this.loadBloodGroup(bloodGroupMstDTO);
		registrationDTO.setBloodGroupName(bloodGroupMstDTO.getBloodGroupName());
		return registrationDTO;
	}
	
	@Override
	public RegistrationDTO preProcessLogin(RegistrationDTO registrationDTO) throws ApplicationException{
		loginValidator.validatePreProcessLogin(registrationDTO);
		return this.loadRegistration(registrationDTO);
	}
	
	@Override
	public Boolean verifySecurityQuestion(RegistrationDTO registrationDTO) throws ApplicationException{
		loginValidator.validateVerifySecurityQuestion(registrationDTO);
		return true;
	}
	
	@Override
	public List<UserTypeMstDTO> listUserTypes(UserTypeMstDTO userTypeMstDTO) {
		List<UserTypeMst> list = loginDao.listUserTypes(userTypeMstDTO);
		List<UserTypeMstDTO> dtoList = null;
		if(CollectionUtils.isNotEmpty(list)){
			dtoList = new ArrayList<UserTypeMstDTO>();
			UserTypeMstDTO dto = null;
			for(UserTypeMst entity: list){
				dto = new UserTypeMstDTO();
				dto.setUsertypeId(entity.getUsertypeId());
				dto.setUsertypeName(entity.getUsertypeName());
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	@Override
	public List<SecurityQuestionDTO> listSecurityQuestions(SecurityQuestionDTO securityQuestionDTO) {
		List<SecurityQuestion> list = loginDao.listSecurityQuestions(securityQuestionDTO);
		List<SecurityQuestionDTO> dtoList = null;
		if(CollectionUtils.isNotEmpty(list)){
			dtoList = new ArrayList<SecurityQuestionDTO>();
			SecurityQuestionDTO dto = null;
			for(SecurityQuestion entity: list){
				dto = new SecurityQuestionDTO();
				dto.setSecurityQuestionId(entity.getSecurityQuestionId());
				dto.setSecurityQuestion(entity.getSecurityQuestion());
				dtoList.add(dto);
			}
		}
		return dtoList;
	}

	@Override
	public List<BloodGroupMstDTO> listBloodGroups(BloodGroupMstDTO bloodGroupMstDTO) {
		List<BloodGroupMst> list = loginDao.listBloodGroups(bloodGroupMstDTO);
		List<BloodGroupMstDTO> dtoList = null;
		if(CollectionUtils.isNotEmpty(list)){
			dtoList = new ArrayList<BloodGroupMstDTO>();
			BloodGroupMstDTO dto = null;
			for(BloodGroupMst entity: list){
				dto = new BloodGroupMstDTO();
				dto.setBloodGroupId(entity.getBloodGroupId());
				dto.setBloodGroupName(entity.getBloodGroupName());
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	@Override
	public void processForgotPassword(RegistrationDTO registrationDTO) throws ApplicationException{
		loginValidator.validateProcessForgotPassword(registrationDTO);
		Registration registration = loginDao.loadRegistration(registrationDTO);
		registration.setPassword(registrationDTO.getPassword());
		loginDao.update(registration);
	}
	
	@Override
	public void processSignup(RegistrationDTO registrationDTO) throws ApplicationException{
		loginValidator.validateProcessSignup(registrationDTO);
		Registration registration = new Registration();
		registration.setBloodGroup(registrationDTO.getBloodGroup());
		registration.setBirthDate(DateUtil.convertDateStrToDate(registrationDTO.getBirthDate(), DateUtil.DATE_FORMAT_dd_MM_yyyy_SEP_HIPHEN));
		registration.setGender(registrationDTO.getGender());
		registration.setUserName(registrationDTO.getUserName());
		registration.setPassword(registrationDTO.getPassword());
		registration.setUsertypeId(registrationDTO.getUsertypeId());
		registration.setSecurityQue(registrationDTO.getSecurityQue());
		registration.setAnswer(registrationDTO.getAnswer());
		StatusMst statusMst = new StatusMst();
		statusMst.setStatus(AppConstants.ACTIVE);
		registration.setStatusMst(statusMst);
		loginDao.save(registration);
		
		LocationAddress locationAddress = new LocationAddress();
		locationAddress.setReferenceId(String.valueOf(registration.getRegistrationId()));
		locationAddress.setReferenceType(ReferenceTypeEnum.USER.getCode());
		locationAddress.setName(registrationDTO.getLocationAddressDTO().getName());
		locationAddress.setMobileNumber(registrationDTO.getLocationAddressDTO().getMobileNumber());
		locationAddress.setEmailId(registrationDTO.getLocationAddressDTO().getEmailId());
		locationAddress.setAddress(registrationDTO.getLocationAddressDTO().getAddress());
		locationAddress.setState(registrationDTO.getLocationAddressDTO().getState());
		locationAddress.setCity(registrationDTO.getLocationAddressDTO().getCity());
		locationAddress.setPincode(registrationDTO.getLocationAddressDTO().getPincode());
		loginDao.save(locationAddress);
	}
	
	@Override
	public void processEnquiry(EnquiryFormDTO enquiryFormDTO) throws ApplicationException{
		enquiryValidator.validateProcessEnquiry(enquiryFormDTO);
		EnquiryForm enquiryForm = new EnquiryForm();
		enquiryForm.setMessage(enquiryFormDTO.getMessage());
		StatusMst statusMst = new StatusMst();
		statusMst.setStatus(AppConstants.ACTIVE);
		enquiryForm.setStatusMst(statusMst);
		enquiryForm.setCreatedDate(new Date());
		loginDao.save(enquiryForm);
		
		LocationAddress locationAddress = new LocationAddress();
		locationAddress.setReferenceId(String.valueOf(enquiryForm.getInqId()));
		locationAddress.setReferenceType(ReferenceTypeEnum.ENQUIRY.getCode());
		locationAddress.setName(enquiryFormDTO.getLocationAddressDTO().getName());
		locationAddress.setMobileNumber(enquiryFormDTO.getLocationAddressDTO().getMobileNumber());
		locationAddress.setEmailId(enquiryFormDTO.getLocationAddressDTO().getEmailId());
		loginDao.save(locationAddress);
	}
	
	@Override
	public void processFeedback(FeedbackDTO feedbackDTO) throws ApplicationException{
		feedbackValidator.validateProcessFeedback(feedbackDTO);
		
		Feedback feedback = new Feedback();
		feedback.setFeedback(feedbackDTO.getFeedback());
		loginDao.save(feedback);
		
		LocationAddress locationAddress = new LocationAddress();
		locationAddress.setReferenceId(String.valueOf(feedback.getFid()));
		locationAddress.setReferenceType(ReferenceTypeEnum.FEEDBACK.getCode());
		locationAddress.setName(feedbackDTO.getLocationAddressDTO().getName());
		locationAddress.setEmailId(feedbackDTO.getLocationAddressDTO().getEmailId());
		loginDao.save(locationAddress);
	}
	
	@Override
	public UserTypeMstDTO loadUserType(UserTypeMstDTO userTypeMstDTO) {
		// TODO, place validation for userTypeId here...
		UserTypeMst userTypeMst = loginDao.loadUserType(userTypeMstDTO);
		userTypeMstDTO.setUsertypeName(userTypeMst.getUsertypeName());
		return userTypeMstDTO;
	}
	
	@Override
	public BloodGroupMstDTO loadBloodGroup(BloodGroupMstDTO bloodGroupMstDTO) {
		// TODO, place validation...
		BloodGroupMst bloodGroupMst = loginDao.loadBloodGroup(bloodGroupMstDTO);
		bloodGroupMstDTO.setBloodGroupName(bloodGroupMst.getBloodGroupName());
		return bloodGroupMstDTO;
	}
	
	@Override
	public LocationAddressDTO loadLocationAddress(LocationAddressDTO locationAddressDTO) {
		// TODO, place validation...
		LocationAddress locationAddress = loginDao.loadLocationAddress(locationAddressDTO);
		locationAddressDTO.setLocationAddressId(locationAddress.getLocationAddressId());
		locationAddressDTO.setName(locationAddress.getName());
		locationAddressDTO.setMobileNumber(locationAddress.getMobileNumber());
		locationAddressDTO.setEmailId(locationAddress.getEmailId());
		locationAddressDTO.setAddress(locationAddress.getAddress());
		locationAddressDTO.setState(locationAddress.getState());
		locationAddressDTO.setCity(locationAddress.getCity());
		locationAddressDTO.setPincode(locationAddress.getPincode());
		return locationAddressDTO;
	}
	
	@Override
	public List<UserTypeMappingDTO> loadPrivileges(UserTypeMstDTO userTypeMstDTO){
		List<UserTypeMapping> list = loginDao.listUserTypeMappings(userTypeMstDTO);
		List<UserTypeMappingDTO> dtoList = null;
		if(CollectionUtils.isNotEmpty(list)){
			dtoList = new ArrayList<UserTypeMappingDTO>();
			UserTypeMappingDTO dto = null;
			for(UserTypeMapping entity: list){
				dto = new UserTypeMappingDTO();
				dto.setLeftMenuName(entity.getUserLeftMenu().getLeftMenuName());
				dto.setLeftMenuDescription(entity.getUserLeftMenu().getLeftMenuDescription());
				dto.setSubMenuName(entity.getUserSubMenu().getSubMenuName());
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	@Override
	public void processEditProfile(RegistrationDTO registrationDTO) throws ApplicationException{
		// TODO, place validation here...
		Registration registration = loginDao.loadRegistration(registrationDTO);
		if(null == registration){
			throw new ApplicationException(ErrorConstants.INVALID_USER);
		}
		LocationAddress locationAddress = new LocationAddress();
		locationAddress.setReferenceId(String.valueOf(registration.getRegistrationId()));
		locationAddress.setReferenceType(ReferenceTypeEnum.USER.getCode());
		locationAddress.setName(registrationDTO.getLocationAddressDTO().getName());
		locationAddress.setMobileNumber(registrationDTO.getLocationAddressDTO().getMobileNumber());
		locationAddress.setEmailId(registrationDTO.getLocationAddressDTO().getEmailId());
		locationAddress.setAddress(registrationDTO.getLocationAddressDTO().getAddress());
		locationAddress.setState(registrationDTO.getLocationAddressDTO().getState());
		locationAddress.setCity(registrationDTO.getLocationAddressDTO().getCity());
		locationAddress.setPincode(registrationDTO.getLocationAddressDTO().getPincode());
		loginDao.save(locationAddress);
	}
}
