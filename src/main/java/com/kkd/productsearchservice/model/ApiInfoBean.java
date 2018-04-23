package com.kkd.productsearchservice.model;

import org.springframework.stereotype.Component;

import springfox.documentation.service.Contact;

@Component
public class ApiInfoBean {
	private String title;

	private String description;

	private String version;

	private String termOfServiceUrl;

	private Contact contact;

	private String license;

	private String licenseUrl;

	public ApiInfoBean() {

	}

	// Parameterized constructor for the ApiInfoBean class
	public ApiInfoBean(String title, String description, String version, String termOfServiceUrl, Contact contact,
			String license, String licenseUrl) {
		super();
		this.title = title;
		this.description = description;
		this.version = version;
		this.termOfServiceUrl = termOfServiceUrl;
		this.contact = contact;
		this.license = license;
		this.licenseUrl = licenseUrl;
	}

	// getter and setter methods for the variable
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTermOfServiceUrl() {
		return termOfServiceUrl;
	}

	public void setTermOfServiceUrl(String termOfServiceUrl) {
		this.termOfServiceUrl = termOfServiceUrl;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getLicenseUrl() {
		return licenseUrl;
	}

	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}

	@Override
	public String toString() {
		return "ApiInfo [title=" + title + ", description=" + description + ", version=" + version
				+ ", termOfServiceUrl=" + termOfServiceUrl + ", contact=" + contact + ", license=" + license
				+ ", licenseUrl=" + licenseUrl + "]";
	}

}
