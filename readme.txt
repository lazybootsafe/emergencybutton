In order to make this app work you need to create a Config class with this:

package com.emergency.button;

public class Config {
	static String secret = "your secret code here";
}


So that way the webserver knows it's your app sending the messages.