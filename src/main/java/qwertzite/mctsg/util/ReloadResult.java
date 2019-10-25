package qwertzite.mctsg.util;

import java.util.LinkedList;
import java.util.List;

public class ReloadResult {
	public int targetNum = 0;
	public int successed = 0;
	public int failed = 0;
	public List<String> message = new LinkedList<>();
	
	public void success() {
		this.targetNum++;
		this.successed++;
	}
	
	public void failed(String messagge) {
		this.targetNum++;
		this.failed++;
		this.message.add(messagge);
	}
}
