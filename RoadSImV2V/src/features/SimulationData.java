package features;

public class SimulationData {
	long startTick = 0;
	long initialTick = 0;
	long currentTick = 0;
	boolean useLog = true;
	boolean useGUI = true;
	//List<LogData> logData;
	String logAlgorithm;
	int numMsgsReceived = 0;
	int numMsgsSent = 0;
	
	public long getStartTick() {
		return startTick;
	}
	public void setStartTick(long startTick) {
		this.startTick = startTick;
	}
	public long getInitialTick() {
		return initialTick;
	}
	public void setInitialTick(long initialTick) {
		this.initialTick = initialTick;
	}
	public long getCurrentTick() {
		return currentTick;
	}
	public void setCurrentTick(long currentTick) {
		this.currentTick = currentTick;
	}
	public boolean isUseLog() {
		return useLog;
	}
	public void setUseLog(boolean useLog) {
		this.useLog = useLog;
	}
	public boolean isUseGUI() {
		return useGUI;
	}
	public void setUseGUI(boolean useGUI) {
		this.useGUI = useGUI;
	}
	public String getLogAlgorithm() {
		return logAlgorithm;
	}
	public void setLogAlgorithm(String logAlgorithm) {
		this.logAlgorithm = logAlgorithm;
	}
	public int getNumMsgsReceived() {
		return numMsgsReceived;
	}
	public void setNumMsgsReceived(int numMsgsReceived) {
		this.numMsgsReceived = numMsgsReceived;
	}
	public int getNumMsgsSent() {
		return numMsgsSent;
	}
	public void setNumMsgsSent(int numMsgsSent) {
		this.numMsgsSent = numMsgsSent;
	}
	
}
