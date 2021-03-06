package com.usa.ri.gov.ies.admin.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.usa.ri.gov.ies.admin.entity.AppAccountEntity;
import com.usa.ri.gov.ies.admin.entity.PlanEntity;
import com.usa.ri.gov.ies.admin.model.AppAccountModel;
import com.usa.ri.gov.ies.admin.model.PlanModel;
import com.usa.ri.gov.ies.admin.repositary.AppAccountRepository;
import com.usa.ri.gov.ies.admin.repositary.PlanAccountRepository;
import com.usa.ri.gov.ies.constants.ApplicationConstants;
import com.usa.ri.gov.ies.properties.AppProperties;
import com.usa.ri.gov.ies.util.EmailUtil;
import com.usa.ri.gov.ies.util.PasswordUtil;

@Service("adminService")
public class AppAccountServiceImpl implements AppAccountService {
	private static Logger logger = LoggerFactory.getLogger(AppAccountServiceImpl.class);
	@Autowired(required = true)
	private EmailUtil emailUtil;

	@Autowired(required = true)
	private AppAccountRepository appAccountRepository;

	@Autowired(required = true)
	private PlanAccountRepository planAccountRepository;

	@Autowired(required = true)
	private AppProperties properties;

	/**
	 * this method is registers record into app_account table
	 */
	@Override
	public boolean registerApplicant(AppAccountModel AppAccount) {
		logger.debug("AdminServiceImpl: registerApplicant method Started");
		String encrypted;
		AppAccountEntity entity = new AppAccountEntity();
		// copying model obj values into enitity obj
		BeanUtils.copyProperties(AppAccount, entity);
		// encrypt password
		encrypted = PasswordUtil.encrypt(AppAccount.getPassword());
		// set the encrypted password in entity
		entity.setPassword(encrypted);
		// set the status active
		entity.setActiveSw(ApplicationConstants.ACTIVE_SW);

		try {
			// save entity record into table
			entity = appAccountRepository.save(entity);
			String fileName = properties.getProperties().get(ApplicationConstants.REG_EMAIL_FILE_NAME);
			String subject = properties.getProperties().get(ApplicationConstants.REG_EMAIL_SUBJECT);
			String text = getEmailBodyContent(AppAccount, fileName);
			emailUtil.sendEmail(entity.getEmailId(), subject, text);
		} catch (Exception e) {
			logger.debug("AdminServiceImpl: registerApplicant method Ended");
			logger.info("AdminService: registerApplicant Executed");
			logger.warn(" AdminService: registerApplicant() " + e.getMessage());
			return false;
		}
		logger.debug("AdminServiceImpl: registerApplicant method Ended");
		logger.info("AdminService: registerApplicant Executed");
		return (entity.getAppId() > 0) ? true : false;
	}// registerApplicant

	/**
	 * this method creates format of body for email
	 * 
	 * @param accModel
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public String getEmailBodyContent(AppAccountModel accModel, String fileName) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		StringBuffer body = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			if (line != null && !"".equals(line) && "<br/>".equals(line)) {
				// process
				if (line.contains("USER_NAME")) {
					line = line.replace("USER_NAME", accModel.getFirstName() + " " + accModel.getLastName());
				}
				if (line.contains("APP_URL")) {
					line = line.replace("APP_URL", "<a href='http://localhost:7070/IES/'>IES URL</a>");
				}
				if (line.contains("APP_USER_EMAIL")) {
					line = line.replace("APP_USER_EMAIL", accModel.getEmailId());
				}
				if (line.contains("APP_USER_PWD")) {
					line = line.replace("APP_USER_PWD", accModel.getPassword());
				}
				// add processed to body
				body.append(line);
			} // if
			line = reader.readLine();
		}
		reader.close();
		return body.toString();
	}

	/**
	 * this method is used to register the plan details into db table
	 */
	@Override
	public boolean registerPlan(PlanModel planModel) {
		logger.debug("AdminServiceImpl: registerPlan() started");
		PlanEntity entity = new PlanEntity();
		// copy plan Details from model to Entity
		BeanUtils.copyProperties(planModel, entity);
		// set status of plan
		entity.setActiveSw(ApplicationConstants.ACTIVE_SW);
		// set plan Created By
		entity.setCreatedBy(ApplicationConstants.PLAN_CREATED_BY);
		// set plan Updated by
		entity.setUpdatedBy(ApplicationConstants.PLAN_UPDATED_BY);
		// save entity to db table
		if (isUniquePlan(planModel.getPlanName())) {
			entity = planAccountRepository.save(entity);
			logger.info("AdminServiceImpl: isUniquePlan() executed given plan is unique");
			return true;
		}
		logger.debug("AdminServiceImpl: registerPlan() ended");
		logger.info("AdminServiceImpl: registerPlan() Executed");
		logger.info("AdminServiceImpl: isUniquePlan() executed  given plan is duplicate");
		return false;
	}

	/**
	 * this method is used to check unique plan
	 */
	@Override
	public boolean isUniquePlan(String plan) {
		PlanEntity entity = planAccountRepository.findByPlanName(plan);
		return entity == null ? true : false;
	}

	/**
	 * this method gets plans records from db
	 */
	@Override
	public List<PlanModel> viewPlanAccounts() {
		logger.debug("AdminServiceImpl: viewlanAccounts() stared");
		List<PlanModel> listPlan = new ArrayList<PlanModel>();
		List<PlanEntity> listEntity;
		// get the list of plans from database
		listEntity = planAccountRepository.findAll();
		// copy the plans list from entity to model objs
		for (PlanEntity planEntity : listEntity) {
			PlanModel planModel = new PlanModel();
			BeanUtils.copyProperties(planEntity, planModel);
			listPlan.add(planModel);

		}
		logger.debug("AdminServiceImpl: viewlanAccounts() ended");
		logger.info("AdminServiceImpl: viewlanAccounts() executed");
		return listPlan;
	}

	/**
	 * this method updates the status of account
	 */
	@Override
	public boolean updateAccountActiveSw(String appId, String activeSw) {
		logger.debug("AppAccountServiceImple: updateAccountActiveSw() started");
		AppAccountEntity entity;
		entity = appAccountRepository.findById(Integer.parseInt(appId)).get();
		if (entity != null) {
			entity.setActiveSw(activeSw);
			appAccountRepository.save(entity);
			logger.debug("AppAccountServiceImpl: updateAccountActiveSw() Ended");
			logger.info("AppAccountServiceImple: ActiveSw updated");
			return true;
		} else {
			logger.debug("AppAccountServiceImpl: updateAccountActiveSw() Ended");
			logger.info("AppAccountServiceImple: ActiveSw updated");
			return false;
		}

	}

	/**
	 * this method updates the status of plan
	 */
	@Override
	public boolean updatePlanActiveSw(String planId, String activeSw) {
		logger.debug("AppAccountServiceImple: updatePlanActiveSw() started");
		PlanEntity entity;
		entity = planAccountRepository.findById(Integer.parseInt(planId)).get();
		if (entity != null) {
			entity.setActiveSw(activeSw);
			planAccountRepository.save(entity);

		}
		logger.debug("AppAccountServiceImpl: updatePlanActiveSw() Ended");
		logger.info("AppAccountServiceImple: ActiveSw updated");
		return true;
	}

	/**
	 * this method checks email is unique or duplicate
	 * 
	 */
	@Override
	public String findByEmail(String email) {
		AppAccountEntity entity = appAccountRepository.findByEmailId(email);
		System.out.println(entity);
		String status=entity == null ? "Unique" : "Duplicate";
		return status;
	}

	/**
	 * this method is used to very login credentials
	 */
	@Override
	public String verifyLoginCredentials(AppAccountModel accModel) {
		logger.debug("AppAccountServiceImpl: verifyLoginCredentials() started");
		AppAccountEntity entity = null;
		String encryptedPwd = PasswordUtil.encrypt(accModel.getPassword());

		entity = appAccountRepository.findByEmailIdAndPassword(accModel.getEmailId(), encryptedPwd);

		// validating credentials
		if (entity != null) {
			if (entity.getPassword().equals(encryptedPwd)) {
				if ((entity.getActiveSw()).equalsIgnoreCase(ApplicationConstants.ACTIVE_SW)) {
					accModel.setRole(entity.getRole());
					// login criteria satisfies return null
					logger.debug("AppAccountServiceImpl: verifyLoginCredentials() ended");

					return properties.getProperties().get(ApplicationConstants.LOGIN_SUCCESS);
				} else {
					logger.debug("AppAccountServiceImpl: verifyLoginCredentials() ended");
					return properties.getProperties().get(ApplicationConstants.LOGIN_FAILED_DEACTIVED_ACCOUNT);
				} // active condition
			} else {
				logger.debug("AppAccountServiceImpl: verifyLoginCredentials() ended");

				return properties.getProperties().get(ApplicationConstants.LOGIN_FAILED_INVALID_CREDENTIALS);
			} // pwd condition
		} // email availability
		logger.debug("AppAccountServiceImpl: verifyLoginCredentials() ended");

		return properties.getProperties().get(ApplicationConstants.LOGIN_FAILED_INVALID_CREDENTIALS);
	}

	/**
	 * this method loads all application acconts from db
	 */
	public List<AppAccountModel> viewAppAccounts() {
		logger.debug("AdminServiceImpl: viewlanAccounts() stared");
		List<AppAccountModel> listAppAccounts = new ArrayList<AppAccountModel>();
		List<AppAccountEntity> listEntity;
		// get the list of plans from database
		listEntity = appAccountRepository.findAll();
		// copy the plans list from entity to model objs
		for (AppAccountEntity appAccountEntity : listEntity) {
			AppAccountModel appAccountModel = new AppAccountModel();
			BeanUtils.copyProperties(appAccountEntity, appAccountModel);
			listAppAccounts.add(appAccountModel);
		}
		logger.info("AppAccountService: passwordrecovery() executed Successfully");
		logger.debug("AdminServiceImpl: viewAppAccounts() ended");
		logger.info("AdminServiceImpl: viewAppAccounts() executed");
		return listAppAccounts;
	}

	/**
	 * this method process the account reovery
	 */
	public String passwordRecovery(String email) {
		AppAccountEntity entity = null;
		AppAccountModel accModel = new AppAccountModel();
		try {
			entity = appAccountRepository.findByEmailId(email);
			String password = PasswordUtil.decrypt(entity.getPassword());
			entity.setPassword(password);
			// copy enity properties to model properties
			BeanUtils.copyProperties(entity, accModel);
			String subject = properties.getProperties().get(ApplicationConstants.PWD_RECOVERY_EMAIL_SUBJECT);
			String fileName = properties.getProperties().get(ApplicationConstants.PWD_RECOVERY_EMAIL_FILE_NAME);
			String body = getEmailBodyContent(accModel, fileName);
			// send login details to user
			emailUtil.sendEmail(email, subject, body);
		} catch (Exception e) {
			logger.error("AdminService: passwordRecovery() failed" + e);
			return ApplicationConstants.FAILED;
		}
		return ApplicationConstants.SUCCESS;
	}

	/**
	 * this method used to load account by Id
	 */
	@Override
	public AppAccountModel findByAccountId(int appId) {
		logger.debug("AppAccountServiceImpl: findByAccountId() started");
		AppAccountModel accModel = new AppAccountModel();
		AppAccountEntity entity = appAccountRepository.findById(appId).get();
		// copy enity to model obj
		BeanUtils.copyProperties(entity, accModel);
		logger.debug("AppAccountServiceImpl: findByAccountId() ended");
		logger.info("AppAccountServiceImpl: findByAccountId() executed");
		return accModel;
	}

	/**
	 * this method used to update the aplication account record in db
	 */
	@Override
	public String editAccountRecord(AppAccountModel accModel) {
		logger.debug("AdminServiceImpl: editAccontRecord() started ");
		AppAccountEntity entity = new AppAccountEntity();
		// copy model properties value into entity
		BeanUtils.copyProperties(accModel, entity);
		// set the encrypted password to entity
		entity.setPassword(PasswordUtil.encrypt(accModel.getPassword()));
		try {
			// save the record
			entity = appAccountRepository.save(entity);
			// send update mail confirmation
			String subject = properties.getProperties().get(ApplicationConstants.ACC_EDIT_EMAIL_SUBJECT);
			String fileName = properties.getProperties().get(ApplicationConstants.ACC_EDIT_EMAIL_FILENAME);
			String body = getEmailBodyContent(accModel, fileName);
			// send login details to user
			emailUtil.sendEmail(accModel.getEmailId(), subject, body);
		} catch (Exception e) {
			logger.error("AdminServiceImpl: editAccontRecord() failed " + e);
			return ApplicationConstants.FAILED;
		}
		logger.debug("AdminServiceImpl: editAccontRecord() ended ");
		return ApplicationConstants.SUCCESS;
	}

	/**
	 * this method used to load plan details by Id
	 */
	@Override
	public PlanModel findByPlanId(int planId) {
		logger.debug("AppAccountServiceImpl: findByPlanId() started");
		PlanModel planModel = new PlanModel();
		PlanEntity entity = planAccountRepository.findById(planId).get();
		// copy enity to model obj
		BeanUtils.copyProperties(entity, planModel);
		logger.debug("AppAccountServiceImpl: findByplanId() ended");
		logger.info("AppAccountServiceImpl: findByplanId() executed");
		return planModel;
	}

	/**
	 * this method is used to edit plan details in db
	 */
	@Override
	public String editPlanAccount(PlanModel planModel) {
		logger.debug("AdminServiceImpl: editPlanAccount() started");
		PlanEntity entity = new PlanEntity();
		// copy model properties value into entity
		BeanUtils.copyProperties(planModel, entity);
		try {
			// save the record
			entity = planAccountRepository.save(entity);
		} catch (Exception e) {
			logger.error("AdminServiceImpl: editPlanAccount() failed " + e);
			return ApplicationConstants.FAILED;
		}
		logger.debug("AdminServiceImpl: editPlanAccount() ended");
		return ApplicationConstants.SUCCESS;
	}

}
