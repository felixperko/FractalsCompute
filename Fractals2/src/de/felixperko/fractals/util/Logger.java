package de.felixperko.fractals.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Deprecated
public class Logger {
	
	//public static State<Integer> state = new State<>("loggercontrol", 0);
	static Map<String, List<Message>> logMap = java.util.Collections.synchronizedMap(new HashMap<>());
	static ArrayList<Message> addMessages = new ArrayList<>(); 
	static ArrayList<Message> log = new ArrayList<>();

	public static void log(Message message) {
		if (message == null)
			throw new IllegalArgumentException("The message can't be null");
		String str = message.getLogString();
		System.out.println(str);
		//TODO reenable when states exist
		//state.setValue(0); //causes StateChangeActions to update logs
		addMessages.add(message);
	}
	
//	public static int addNewMessages() {
//		if (addMessages.isEmpty())
//			return 0;
//		int c = 0;
//		Iterator<Message> it = addMessages.iterator();
//		while (it.hasNext()) {
//			Message message = it.next();
//			if (message == null)
//				continue;
//			String catName = message
//					.getCategory()
//					.getName();
//			List<Message> list = logMap.get(catName);
//			if (list == null) {
//				list = new ArrayList<>();
//				logMap.put(catName, list);
//			}
//			list.add(message);
//			log.add(message);
//			it.remove();
//			c++;
//		}
//		return c;
//	}
	
	public static List<Message> getLog(){
//		List<String> res = new ArrayList<>();
//		logMap.forEach((k,v) -> v.forEach(msg -> res.add(logString(k,msg))));
//		return res;
		return log;
	}
	
	public static List<Message> getLog(String category){
		return logMap.get(category);
	}
}
