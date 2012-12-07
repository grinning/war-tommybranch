package com.tommytony.war.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.tommytony.war.War;

public class WarLogFormatter extends Formatter {
	
	@Override
    public String format(LogRecord arg0) {
		//why are you creating an instance of the formatter EVERYTIME you call this method?
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        StringBuilder b = new StringBuilder();
        b.append(dateFormat.format(new Date()));
        b.append(" [");
        b.append(arg0.getLevel());
        b.append("] ");
        b.append(arg0.getMessage());
        b.append(System.getProperty("line.separator"));
        return b.toString();
    }
	
	public String quickFormat(LogRecord arg0) {
		//Date length(4 + 1 + 2 + 1 + 2 + 1 + 2 + 1 + 2 + 1 + 2)
		//Rest of message length(2 + arg0.getLevel().getName().length() + 2 + arg0.getMessage().length() + 1)
		StringBuilder b = new StringBuilder(23 + arg0.getLevel().getName().length() + arg0.getMessage().length());
		Calendar c = Calendar.getInstance();
		b.append(c.get(Calendar.YEAR) - 1900); b.append('/'); b.append(c.get(Calendar.MONTH));
		b.append('/'); b.append(c.get(Calendar.DAY_OF_MONTH)); b.append(' ');
		b.append(Calendar.HOUR_OF_DAY); b.append(':'); b.append(Calendar.MINUTE); b.append(':');
		b.append(Calendar.SECOND);
		b.append(" ["); b.append(arg0.getLevel().getName()); b.append("] "); b.append(arg0.getMessage());
		b.append(System.getProperty("line.separator"));
		return b.toString();
	}
}
