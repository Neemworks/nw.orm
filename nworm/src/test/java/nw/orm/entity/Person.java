package nw.orm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import nw.orm.core.IEntity;

@Entity
@Table(name = "PERSON")
public class Person extends IEntity {
	
	private static final long serialVersionUID = -9207662556028009982L;

	@Column(name = "FULL_NAME", length = 1024)
	private String fullName;
	
	private int age;
	
	@Enumerated(EnumType.STRING)
	private Sex sex = Sex.MALE;
	
	public Person() {
		
	}
	
	public Person(String fullName, int age, Sex sex) {
		this.fullName = fullName;
		this.sex = sex;
		this.age = age;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

}
