package com.dev.bruno.sentimentanalysis.tweets.service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.dev.bruno.sentimentanalysis.tweets.dao.TweetDAO;
import com.dev.bruno.sentimentanalysis.tweets.exception.AppException;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.dev.bruno.sentimentanalysis.tweets.stream.TweetTopic;

@Stateless
public class TweetService {

	@Inject
	private TweetDAO dao;
	
	@Inject
	private TweetTopic topic;

	public void insert(Long id, String text) {
		if (id == null || text == null) {
			throw new AppException("id e text são campos obrigatórios.");
		}
		
		Tweet tweet = new Tweet();
		
		tweet.setId(hash(String.valueOf(id)));
		tweet.setText(text);
		
		//dao.insert(tweet);
		topic.send(tweet, "tweets-insert");
	}

	public void update(Tweet tweet) {
		if (tweet == null) {
			throw new AppException("Tweet não informado.");
		}

		if (tweet.getId() == null || tweet.getText() == null) {
			throw new AppException("id e text são campos obrigatórios.");
		}
		
		dao.update(tweet);
	}

	public Tweet get(String id) {
		if (id == null) {
			throw new AppException("id é campo obrigatório.");
		}
		
		return dao.get(id);
	}

	public List<Tweet> listNullHumanSentiment(Integer limit) {
		return dao.listNullHumanSentiment(limit);
	}
	
	private String hash(String text) {
		try {	
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(text.getBytes(),0,text.length());
		
			return new BigInteger(1,m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void processTweets() {
		List<Tweet> tweets = dao.listNullMachineSentiment(100);
		
		for(Tweet tweet : tweets) {
			topic.send(tweet, "tweets-evaluation");
		}
	}
}