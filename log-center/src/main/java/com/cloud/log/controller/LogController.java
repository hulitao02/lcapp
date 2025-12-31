package com.cloud.log.controller;

import com.cloud.log.service.LogService;
import com.cloud.model.common.Page;
import com.cloud.model.log.Log;
import com.cloud.model.log.constants.LogModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class LogController {

	@Autowired
	private LogService logService;

	@PostMapping("/logs-anon/internal")
	public void save(@RequestBody Log log) {
		logService.save(log);
	}

	public static void main(String[] args) {
		String s = "dfmrffhyrgsjud";
//		Set<Character> set = new HashSet<>();
//		int count = 0;
//		Map<Character,Integer> map = new HashMap<>();
//		for(int i=0;i<s.length();i++){
//			char c = s.charAt(i);
//			if(i== 0){
//				map.put(c,0);
//			}
//			if(map.containsKey(c)){
//				Integer integer = map.get(c);
//				integer++;
//				map.put(c,integer);
//			}else{
//				map.put(c,1);
//			}
//		}
//		System.out.println(map);
		Character aChar = getChar(s);
		System.out.println(aChar);

	}

	public static Character getChar(String str){
		Map<Character, Integer> map = new HashMap<>();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (map.containsKey(c)){
				Integer t = map.get(c);
				map.put(c,t+1);
			}else {
				map.put(c,1);
			}
		}
		//这里将map.entrySet()转换成list
		List<Map.Entry<Character,Integer>> list = new ArrayList<Map.Entry<Character,Integer>>(map.entrySet());
		//然后通过比较器来实现排序
		Collections.sort(list,new Comparator<Map.Entry<Character,Integer>>() {
			@Override
			public int compare(Map.Entry<Character,Integer> o1, Map.Entry<Character,Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return list.get(0).getKey();
	}

	/**
	 * 日志模块
	 *
	 * @return
	 */
	@PreAuthorize("hasAuthority('log:query')")
	@GetMapping("/logs-modules")
	public Map<String, String> logModule() {
		return LogModule.MODULES;
	}

	/**
	 * 日志查询
	 *
	 * @param params
	 * @return
	 */
	@PreAuthorize("hasAuthority('log:query')")
	@GetMapping("/logs")
	public Page<Log> findLogs(@RequestParam Map<String, Object> params) {
		return logService.findLogs(params);
	}



}
