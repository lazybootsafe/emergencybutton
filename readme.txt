In order to make this app work you need to create a Config class with this:

package com.emergency.button;

public class Config {
	static String secret = "your secret code here";
}


So that way the webserver knows it's your app sending the messages.




What's New

=============

2012-01-26

Beautiful UX Redesign by Derek Keeler - derekfordesign@gmail.com

Added a new email sending server.

Coming soon: multiple email/sms recipients!

--------
2011-11-01

Improved widget graphics.

Fixed a bug with orientation changes while sending emergency messages.

Fixed a rare bug when sending SMS's failed.

Maximum wait for location increased from 20 to 40 seconds.