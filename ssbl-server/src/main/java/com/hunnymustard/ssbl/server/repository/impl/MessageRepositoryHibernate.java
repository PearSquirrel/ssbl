package com.hunnymustard.ssbl.server.repository.impl;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hunnymustard.ssbl.model.Message;
import com.hunnymustard.ssbl.model.User;
import com.hunnymustard.ssbl.server.repository.MessageRepository;

@Repository("messageRepository")
@Transactional(propagation = Propagation.REQUIRED, readOnly=false)
public class MessageRepositoryHibernate extends HibernateRepository<Message, Integer>
		implements MessageRepository {

	@Override
	public Message find(Integer key) {
		return (Message) getSession().get(Message.class, key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Message> findByNew(User user) {
		return (List<Message>) getSession().createCriteria(Message.class)
				.createAlias("sender", "sender")
				.add(Restrictions.gt("sentTime", user.getLastMessageTime()))
				.add(Restrictions.ne("sender.userId", user.getUserId()))
				.addOrder(Order.desc("sentTime"))
				.list();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Message> findByAll(User user) {
		return (List<Message>) getSession().createCriteria(Message.class)
				.createAlias("sender", "sender")
				.add(Restrictions.ne("sender.userId", user.getUserId()))
				 .setProjection(Projections.distinct(Projections.property("conversationId")))
				 .list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Message> findByConversation(Integer conversationId, Integer size, Integer additional) {
		return (List<Message>) getSession().createCriteria(Message.class)
				.setFirstResult(size)
				.setMaxResults(additional)
				.createAlias("conversation", "conversation")
				.add(Restrictions.eq("conversation.conversationId", conversationId))
				.addOrder(Order.desc("sentTime"))
				.list();
	}
}
