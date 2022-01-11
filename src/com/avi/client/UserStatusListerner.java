package com.avi.client;

public interface UserStatusListerner {
	public void online(String userName);
	public void offline(String userName);
}
