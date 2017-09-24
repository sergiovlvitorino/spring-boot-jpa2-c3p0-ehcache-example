package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.dao;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class Dao<T> {
	
	@Autowired
	private EntityManager entityManager;
	
	@Transactional
	public void persist(T t) {
		entityManager.getTransaction().begin();
		entityManager.persist(t);
		entityManager.getTransaction().commit();
	}
	
	@Transactional
	public void merge(T t) {
		entityManager.getTransaction().begin();
		entityManager.merge(t);
		entityManager.getTransaction().commit();
	}
	
	@Transactional
	public void delete(T t) {
		entityManager.getTransaction().begin();
		entityManager.remove(t);		
		entityManager.getTransaction().commit();
	}
	
	protected EntityManager getEntityManager(){
		return entityManager;
	}
	
}
