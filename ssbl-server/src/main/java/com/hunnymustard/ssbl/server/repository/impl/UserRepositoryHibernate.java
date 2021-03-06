package com.hunnymustard.ssbl.server.repository.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hunnymustard.ssbl.model.Location;
import com.hunnymustard.ssbl.model.User;
import com.hunnymustard.ssbl.server.repository.UserRepository;
import com.hunnymustard.ssbl.util.DistanceComparator;

@Repository("userRepository")
@Transactional(propagation = Propagation.REQUIRED, readOnly=false)
public class UserRepositoryHibernate extends HibernateRepository<User, Integer> implements UserRepository {

	@Override
	public User find(Integer key) {
		return (User) getSession().get(User.class, key);
	}

	@Override
	public User findByCredentials(String username, String password) {
		return (User) getSession().createCriteria(User.class)
				.add(Restrictions.eq("username", username))
				.add(Restrictions.eq("password", password))				
				.uniqueResult();
	}
	
	@Override
	public User findByParameters(String username, Integer id) {
		return (User) getSession().createCriteria(User.class)
				.add(Restrictions.eq("username", username))
				.add(Restrictions.idEq(id))
				.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<User> findByProximity(Location cur, Double radius) {
		// https://docs.jboss.org/hibernate/search/4.2/reference/en-US/html/spatial.html way to improve
		// using spatial hibernate queries.
		String hql = "from User user inner join fetch user.location as loc where user.private = false and acos("
				+ "sin(:lat1/57.2958) * sin(loc.latitude/57.2958) + cos(:lat1/57.2958) "
				+ "* cos(loc.latitude/57.2958) * cos((loc.longitude - :lon1)/57.2958)) * 3956 <= :dist";
		
		Query query = getSession().createQuery(hql);
		query.setDouble("lat1", cur.getLatitude());
		query.setDouble("lon1", cur.getLongitude());
		query.setDouble("dist", radius);
		
		List<User> users = (List<User>) query.list();
		Collections.sort(users, new DistanceComparator(cur));	
		return users;
	}

	@SuppressWarnings("unchecked")
	public List<User> findByExample(User example) {
		return (List<User>) getSession()
				.createCriteria(User.class)
				.add(Example.create(example).enableLike(MatchMode.ANYWHERE))
				.list();
	}
}
