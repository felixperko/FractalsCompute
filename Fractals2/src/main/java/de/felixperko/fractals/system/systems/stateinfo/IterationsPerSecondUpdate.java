package de.felixperko.fractals.system.systems.stateinfo;

public class IterationsPerSecondUpdate extends AbstractSharedDataUpdate {
	
	private static final long serialVersionUID = -5913572900658331883L;

	int timeslice;
	long startTime, endTime;
	int ips_total;
	int[] ips_threads;

	public IterationsPerSecondUpdate(int timeslice, long startTime, long endTime, int ips_total, int[] ips_threads) {
		this.timeslice = timeslice;
		this.startTime = startTime;
		this.endTime = endTime;
		this.ips_total = ips_total;
		this.ips_threads = ips_threads;
	}

	public int getTimeslice() {
		return timeslice;
	}

	public void setTimeslice(int timeslice) {
		this.timeslice = timeslice;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getIps_total() {
		return ips_total;
	}

	public void setIps_total(int ips_total) {
		this.ips_total = ips_total;
	}

	public int[] getIps_threads() {
		return ips_threads;
	}

	public void setIps_threads(int[] ips_threads) {
		this.ips_threads = ips_threads;
	}
}
