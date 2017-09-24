package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.model.Person;

@Component
public class PersonDao extends Dao<Person>{

	public List<?> getAll(){
		Query query = getEntityManager().createQuery("select a from " + Person.class.getCanonicalName() + " a", Person.class);
		query.setHint("org.hibernate.cacheable", true);
		return query.getResultList();
	}
	
	public Person getObjectById(Object id){
		Query query = getEntityManager().createQuery("select a from " + Person.class.getCanonicalName() + " a where id=:id");
		query.setParameter("id", id);
		query.setHint("org.hibernate.cacheable", true);
		try {
			return (Person) query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}
}
